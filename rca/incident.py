"""Load one trial as an *incident* a diagnoser is allowed to see.

A diagnoser gets ONLY:
  - logs inside the incident window [inject_ts, revert_ts]
  - a short baseline window before it (what "normal" looked like)
It never sees truth.json (the answer) or load.jsonl (client-side traffic),
so no diagnoser can cheat. The evaluator keeps the truth separately.

v1 simplification (state it in the write-up): the window is anchored on the
injection time, i.e. we assume an alert fired when the fault started.
"""
import json, os, re
from dataclasses import dataclass, field
from datetime import datetime, timezone, timedelta

BASELINE_S = 30

# Startup markers per service: a service that logs one of these inside the
# incident window was (re)started during the incident -- "what changed?".
STARTUP_MARKERS = {
    "backend": re.compile(r"Started \w+Application in"),
    "chain": re.compile(r"Listening on [\d.]+:8545"),
    "db": re.compile(r"database system is ready to accept connections"),
}


def parse_ts(s):
    """'2026-11-04T17:35:56.079413300Z' or '...56.079Z' -> aware datetime."""
    s = s.rstrip("Z")
    if "." in s:
        head, frac = s.split(".", 1)
        s = f"{head}.{(frac + '000000')[:6]}"
    return datetime.fromisoformat(s).replace(tzinfo=timezone.utc)


@dataclass
class Incident:
    trial_id: str
    start: datetime
    end: datetime
    window: list = field(default_factory=list)     # log records during the incident
    baseline: list = field(default_factory=list)   # log records just before it

    def errors(self, levels=("ERROR", "WARN")):
        return [r for r in self.window if r.get("level") in levels]

    def restarted(self):
        """Services that logged a startup marker inside the incident window."""
        return sorted({svc for r in self.window for svc, rx in STARTUP_MARKERS.items()
                       if r.get("service") == svc and rx.search(r.get("msg", ""))})

    def text(self, r):
        """Everything a diagnoser may quote from one record."""
        return " ".join([r.get("msg", "")] + r.get("stack", []))


def load_incident(trial_dir):
    with open(os.path.join(trial_dir, "truth.json")) as f:
        t = json.load(f)             # read ONLY for the time window, never passed on
    start, end = parse_ts(t["inject_ts"]), parse_ts(t["revert_ts"])
    base_start = start - timedelta(seconds=BASELINE_S)
    inc = Incident(trial_id=t["trial_id"], start=start, end=end)
    path = os.path.join(trial_dir, "logs.jsonl")
    if os.path.exists(path):
        with open(path) as f:
            for line in f:
                try:
                    r = json.loads(line)
                    ts = parse_ts(r["ts"])
                except (ValueError, KeyError):
                    continue
                if start <= ts <= end:
                    inc.window.append(r)
                elif base_start <= ts < start:
                    inc.baseline.append(r)
    return inc


def load_truth(trial_dir):
    with open(os.path.join(trial_dir, "truth.json")) as f:
        return json.load(f)
