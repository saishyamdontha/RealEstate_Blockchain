"""Diagnoser (b): one LLM call over the incident's error lines. No tools.

Sees exactly what the rules baseline sees, to keep the comparison fair:
  - ERROR/WARN records in the incident window (deduplicated, capped)
  - per-service log volume: baseline vs incident window (lets it notice silence)

  RCA_MODEL=qwen3:8b python3 -m rca.evaluate --diagnoser llm
  RCA_MODEL=llama3.2 python3 -m rca.evaluate --diagnoser llm
"""
import json, os, re, urllib.request
from collections import OrderedDict

MODEL = os.environ.get("RCA_MODEL", "qwen3:8b")
HOST = os.environ.get("OLLAMA_HOST", "http://localhost:11434").rstrip("/")
MAX_LINES = int(os.environ.get("RCA_MAX_LINES", "40"))
TIMEOUT = int(os.environ.get("RCA_TIMEOUT", "180"))
THINK = os.environ.get("RCA_THINK", "0") == "1"   # qwen3 reasoning mode: experimental variable
COMPONENTS = ["db", "chain", "wallet", "backend", "none"]

SYSTEM = """You are an on-call SRE diagnosing an incident in this system:
- backend: Spring Boot API. Includes its own configuration (DB credentials, contract address) and its data consistency logic.
- db: PostgreSQL database used by the backend.
- chain: local Ethereum JSON-RPC node (Anvil) hosting the PropertyLedger smart contract, including the node's state.
- wallet: the single account the backend uses to sign and pay for every transaction (its ETH balance).
- none: nothing is wrong; errors are absent or normal background noise.

Pick the ONE component where the root cause lives (not merely where symptoms appear).
Example: if the backend logs "password authentication failed", the database is fine and the backend is misconfigured -> backend.

Answer with JSON only:
{"component": one of ["db","chain","wallet","backend","none"],
 "root_cause": "one sentence",
 "evidence": ["exact substring copied from the log lines below", ...]}
Evidence strings MUST be copied character-for-character from the log lines. Quote 1-3 short strings. Never invent text."""

SCHEMA = {"type": "object",
          "properties": {"component": {"type": "string", "enum": COMPONENTS},
                         "root_cause": {"type": "string"},
                         "evidence": {"type": "array", "items": {"type": "string"}}},
          "required": ["component", "root_cause", "evidence"]}

_NORM = [(re.compile(r"0x[0-9a-fA-F]+"), "0x…"), (re.compile(r"\b[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}\b"), "<uuid>"),
         (re.compile(r"\d+"), "N")]


def _norm(s):
    for rx, rep in _NORM:
        s = rx.sub(rep, s)
    return s


def build_prompt(inc):
    groups = OrderedDict()   # normalized text -> [count, first raw record]
    for r in inc.errors():
        key = (r.get("service"), r.get("level"), _norm(r.get("msg", "")))
        if key in groups:
            groups[key][0] += 1
        else:
            groups[key] = [1, r]
    # ERRORs first, then WARNs; most frequent first within each
    items = sorted(groups.values(), key=lambda g: (g[1].get("level") != "ERROR", -g[0]))
    lines = []
    for count, r in items[:MAX_LINES]:
        msg = r.get("msg", "")[:400]
        stack = [s for s in r.get("stack", []) if s.startswith("Caused by")][-2:] or r.get("stack", [])[:2]
        extra = "".join(f"\n      {s[:300]}" for s in stack)
        lines.append(f"[{r.get('service')}] {r.get('level')} (x{count}) {msg}{extra}")
    omitted = max(0, len(items) - MAX_LINES)

    # Same signal the rules baseline uses: services that were logging before and are now completely silent.
    # (Raw volume is NOT shown: it swings with random traffic and misled models on no-fault trials.)
    before = {r.get("service") for r in inc.baseline}
    during = {r.get("service") for r in inc.window}
    silent = sorted(before - during)
    restarted = inc.restarted()
    dur = int((inc.end - inc.start).total_seconds())
    body = (f"Incident window: {dur}s.\n\nServices that were logging before but are completely silent now: "
            f"{', '.join(silent) if silent else 'none'}\n"
            f"Services that (re)started during the incident window: {', '.join(restarted) if restarted else 'none'}\n\n"
            f"Distinct ERROR/WARN log lines during the incident ({len(items)} distinct"
            + (f", {omitted} omitted" if omitted else "") + "):\n"
            + ("\n".join(lines) if lines else "(none)"))
    return body


def _call(prompt):
    payload = {"model": MODEL, "stream": False, "format": SCHEMA, "think": THINK,
               "options": {"temperature": 0, "num_ctx": 8192},
               "messages": [{"role": "system", "content": SYSTEM}, {"role": "user", "content": prompt}]}
    req = urllib.request.Request(f"{HOST}/api/chat", data=json.dumps(payload).encode(),
                                 headers={"Content-Type": "application/json"})
    with urllib.request.urlopen(req, timeout=TIMEOUT) as r:
        return json.load(r)


def diagnose(inc):
    prompt = build_prompt(inc)
    resp = _call(prompt)
    content = resp.get("message", {}).get("content", "")
    content = re.sub(r"<think>.*?</think>", "", content, flags=re.S).strip()
    try:
        out = json.loads(content)
    except json.JSONDecodeError:
        m = re.search(r"\{.*\}", content, re.S)
        out = json.loads(m.group(0)) if m else {}
    comp = str(out.get("component", "")).strip().lower()
    ev = out.get("evidence", [])
    if not isinstance(ev, list):
        ev = [str(ev)]
    return {"component": comp if comp in COMPONENTS else f"invalid:{comp}",
            "root_cause": str(out.get("root_cause", ""))[:500],
            "evidence": [str(e) for e in ev][:5],
            "model": MODEL, "think": THINK,
            "tokens": {"prompt": resp.get("prompt_eval_count"), "output": resp.get("eval_count")}}
