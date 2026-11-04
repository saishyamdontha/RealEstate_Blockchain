#!/usr/bin/env python3
"""Stream (or dump) all docker compose logs into structured JSONL.

One record per log event:
  {"ts": "...Z", "service": "backend", "level": "ERROR", "rid": "a1b2c3d4", "msg": "..."}

- Java stack-trace lines ("\tat ...", "Caused by: ...") are folded into the
  previous record of the same service instead of becoming 50 fake events.
- Standard library only.

Usage:
  scripts/collect_logs.py                      # follow live, write logs/<timestamp>.jsonl
  scripts/collect_logs.py --since 10m --no-follow --out /tmp/window.jsonl
"""
import argparse, datetime as dt, json, os, re, subprocess, sys

# "backend-1  | 2026-11-04T16:52:25.351234567Z <line>"
PREFIX = re.compile(r"^(?P<svc>[\w.-]+?)(?:-\d+)?\s+\|\s(?P<ts>\d{4}-\d\d-\d\dT[\d:.]+Z)\s?(?P<line>.*)$")
# Spring Boot line with our pattern: "...Z ERROR [rid=a1b2c3d4] 1 --- [app] [thread] logger : msg"
SPRING = re.compile(r"\b(?P<lvl>TRACE|DEBUG|INFO|WARN|ERROR)\s+\[rid=(?P<rid>[^\]]*)\]\s+\d+\s+---\s+\[[^\]]*\]\s+\[[^\]]*\]\s+(?P<logger>\S+)\s+:\s(?P<msg>.*)$")
# Postgres: "... UTC [57] ERROR:  ..." / Anvil, nginx etc: best-effort keywords
GENERIC_LVL = re.compile(r"\b(PANIC|FATAL|ERROR|WARN(?:ING)?|LOG|INFO|DEBUG)\b:?")
CONTINUATION = re.compile(r"^(\s+at |\tat |Caused by: |\s*\.\.\. \d+ (more|common frames))")

LEVEL_MAP = {"PANIC": "ERROR", "FATAL": "ERROR", "WARNING": "WARN", "LOG": "INFO"}


def parse(raw):
    """Return (service, record_or_None, is_continuation_text)."""
    m = PREFIX.match(raw.rstrip("\n"))
    if not m:
        return None, None, None
    svc, ts, line = m["svc"], m["ts"], m["line"]
    if CONTINUATION.match(line):
        return svc, None, line.strip()
    s = SPRING.search(line)
    if s:
        rec = {"ts": ts, "service": svc, "level": s["lvl"], "rid": s["rid"] or None,
               "logger": s["logger"], "msg": s["msg"]}
    else:
        g = GENERIC_LVL.search(line)
        lvl = LEVEL_MAP.get(g[1], g[1]) if g else "INFO"
        rec = {"ts": ts, "service": svc, "level": lvl, "rid": None, "msg": line.strip()}
    return svc, rec, None


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--out", help="output JSONL (default logs/<utc timestamp>.jsonl)")
    ap.add_argument("--since", default=None, help="e.g. 10m, 1h (passed to docker compose logs)")
    ap.add_argument("--no-follow", action="store_true", help="dump and exit instead of streaming")
    args = ap.parse_args()

    out = args.out or os.path.join("logs", dt.datetime.utcnow().strftime("%Y%m%dT%H%M%SZ") + ".jsonl")
    os.makedirs(os.path.dirname(out) or ".", exist_ok=True)

    cmd = ["docker", "compose", "logs", "--timestamps", "--no-color"]
    if not args.no_follow:
        cmd.append("--follow")
    if args.since:
        cmd += ["--since", args.since]

    pending = {}  # service -> last record, held until we know it has no continuation lines
    n = 0
    with open(out, "a") as f, subprocess.Popen(cmd, stdout=subprocess.PIPE, text=True, bufsize=1) as p:
        def flush(svc):
            nonlocal n
            rec = pending.pop(svc, None)
            if rec:
                f.write(json.dumps(rec) + "\n"); f.flush(); n += 1
        try:
            for raw in p.stdout:
                svc, rec, cont = parse(raw)
                if svc is None:
                    continue
                if cont is not None:
                    if svc in pending:
                        pending[svc].setdefault("stack", []).append(cont)
                    continue
                flush(svc)
                pending[svc] = rec
        except KeyboardInterrupt:
            pass
        for svc in list(pending):
            flush(svc)
    print(f"wrote {n} records to {out}", file=sys.stderr)


if __name__ == "__main__":
    main()
