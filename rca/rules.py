"""Diagnoser (a): hand-written rules, the baseline every LLM must beat.

Rules are written from generic ops knowledge of THIS stack (Spring Boot +
Postgres + web3j/JSON-RPC node), the way an on-call engineer would write
alerts -- not reverse-engineered from the fault list. Order matters: the
first rule that matches any error record wins.

Output (shared by all diagnosers):
  {"component": db|chain|wallet|backend|none,
   "root_cause": "<one sentence>",
   "evidence": ["<verbatim substring of a log record>", ...]}
"""
import re

# (name, component, regex over msg+stack, root-cause sentence)
RULES = [
    ("insufficient_funds", "wallet",
     r"[Ii]nsufficient funds",
     "Signer wallet cannot pay for gas; transactions are rejected by the node."),
    ("db_auth", "backend",
     r"password authentication failed",
     "Backend is configured with wrong database credentials."),
    ("db_unreachable", "db",
     r"(Connection to \S+:5432 refused|PSQLException.*(refused|I/O error)|"
     r"Connection is not available, request timed out|"
     r"Unable to acquire JDBC Connection|CannotCreateTransactionException)",
     "Database is unreachable or not responding; no JDBC connections available."),
    ("rpc_unreachable", "chain",
     r"(org\.web3j|HttpService|:8545|chain:8545).*(ConnectException|SocketTimeoutException|"
     r"Connect timed out|Read timed out|UnknownHost|Connection refused)|"
     r"(ConnectException|SocketTimeoutException|Connect timed out|Read timed out|UnknownHost).*(org\.web3j|HttpService|8545)|"
     r"UnknownHostException: chain\b",
     "Blockchain RPC node is unreachable or not responding."),
    ("contract_missing", "chain",
     r"(no PropertyRegistered event|ContractCallException|Empty value \(0x\)|"
     r"execution reverted|is not a contract)",
     "Contract calls return nothing: no contract at the configured address."),
    ("desync", "backend",
     r"DESYNC",
     "On-chain state and database diverged (finalize without DB update)."),
]
COMPILED = [(n, c, re.compile(p, re.S), rc) for n, c, p, rc in RULES]


def _snippet(text, m, width=160):
    a = max(0, m.start() - 40)
    return text[a:a + width]


def _first_match(errs, inc, rx):
    for r in errs:
        text = inc.text(r)
        m = rx.search(text)
        if m:
            return _snippet(text, m)
    return None


CONTRACT_ERR = re.compile(r"no PropertyRegistered event|ContractCallException|Empty value \(0x\)|is not a contract")


def diagnose(inc):
    errs = inc.errors()
    restarted = inc.restarted()

    # "What changed?" rules: a restart inside the incident window explains the errors that follow.
    if errs and any(r.get("level") == "ERROR" for r in errs):
        if "chain" in restarted:
            ev = _first_match(errs, inc, CONTRACT_ERR)
            if ev:
                return {"component": "chain", "rule": "chain_restarted_state_lost", "evidence": [ev],
                        "root_cause": "Blockchain node restarted during the incident and lost its state; the contract is gone."}
        if "backend" in restarted:
            ev = _first_match(errs, inc, CONTRACT_ERR) or _first_match(
                errs, inc, re.compile(r"password authentication failed|Connection refused|JDBC"))
            if ev:
                return {"component": "backend", "rule": "backend_restarted_config_change", "evidence": [ev],
                        "root_cause": "Backend was restarted during the incident and fails right after: likely a bad configuration change."}

    for name, comp, rx, rc in COMPILED:
        for r in errs:
            text = inc.text(r)
            m = rx.search(text)
            if m:
                return {"component": comp, "root_cause": rc, "rule": name,
                        "evidence": [_snippet(text, m)]}

    # Silence rule: backend was logging before, and is completely silent now.
    had_backend = any(r.get("service") == "backend" for r in inc.baseline)
    has_backend = any(r.get("service") == "backend" for r in inc.window)
    if had_backend and not has_backend:
        return {"component": "backend", "root_cause": "Backend stopped producing any logs; process is likely down.",
                "rule": "backend_silent", "evidence": []}

    # Errors we have no rule for: blame the service that logs them most.
    if any(r.get("level") == "ERROR" for r in errs):
        counts = {}
        for r in errs:
            if r.get("level") == "ERROR":
                counts[r["service"]] = counts.get(r["service"], 0) + 1
        svc = max(counts, key=counts.get)
        first = next(r for r in errs if r["service"] == svc and r.get("level") == "ERROR")
        return {"component": svc, "root_cause": f"Unclassified errors, mostly from {svc}.",
                "rule": "fallback_most_errors", "evidence": [first.get("msg", "")[:160]]}

    return {"component": "none", "root_cause": "No error signals in the incident window.",
            "rule": "no_errors", "evidence": []}
