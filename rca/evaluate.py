#!/usr/bin/env python3
"""Score a diagnoser against ground truth across all trials.

  python3 -m rca.evaluate --diagnoser rules
  python3 -m rca.evaluate --diagnoser rules --trials trials --show-wrong

Metrics per trial:
  correct          predicted component == true component
  evidence_valid   every quoted evidence string really occurs in the window logs
                   (catches hallucinated evidence; vacuously true if none quoted)
  latency_s        wall-clock time the diagnoser took
Writes results/<diagnoser>-<utc>.jsonl and prints a per-fault table.
"""
import argparse, glob, importlib, json, os, sys, time
from collections import defaultdict

from rca.incident import load_incident, load_truth


def evidence_valid(inc, evidence):
    texts = [inc.text(r) for r in inc.window]
    return all(any(e and e in t for t in texts) for e in evidence)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--diagnoser", default="rules", help="module in rca/ exposing diagnose(incident)")
    ap.add_argument("--trials", default="trials")
    ap.add_argument("--out", default="results")
    ap.add_argument("--show-wrong", action="store_true")
    a = ap.parse_args()

    diag = importlib.import_module(f"rca.{a.diagnoser}")
    dirs = sorted(d for d in glob.glob(os.path.join(a.trials, "*/")) if os.path.exists(os.path.join(d, "truth.json")))
    if not dirs:
        sys.exit(f"no trials with truth.json under {a.trials}/")

    rows = []
    for d in dirs:
        truth = load_truth(d)
        if "inject_error" in truth or "inject_ts" not in truth:
            print(f"skip {truth.get('trial_id', d)}: invalid trial", file=sys.stderr)
            continue
        inc = load_incident(d)
        t0 = time.time()
        try:
            pred = diag.diagnose(inc)
        except Exception as e:
            pred = {"component": "error", "root_cause": f"diagnoser crashed: {e}", "evidence": []}
        lat = time.time() - t0
        rows.append({
            "trial_id": truth["trial_id"], "fault": truth["name"], "category": truth["category"],
            "true_component": truth["component"], "pred_component": pred.get("component"),
            "correct": pred.get("component") == truth["component"],
            "evidence_valid": evidence_valid(inc, pred.get("evidence", [])),
            "latency_s": round(lat, 3), "window_records": len(inc.window),
            "pred": pred, "true_root_cause": truth["root_cause"],
        })

    os.makedirs(a.out, exist_ok=True)
    out = os.path.join(a.out, f"{a.diagnoser}-{time.strftime('%Y%m%dT%H%M%SZ', time.gmtime())}.jsonl")
    with open(out, "w") as f:
        for r in rows:
            f.write(json.dumps(r) + "\n")

    by = defaultdict(list)
    for r in rows:
        by[r["fault"]].append(r)
    print(f"\n{'fault':16} {'true':8} {'n':>3} {'acc':>5}  {'evid':>5}  predicted")
    for fault in sorted(by):
        rs = by[fault]
        acc = sum(r["correct"] for r in rs) / len(rs)
        ev = sum(r["evidence_valid"] for r in rs) / len(rs)
        preds = defaultdict(int)
        for r in rs:
            preds[r["pred_component"]] += 1
        print(f"{fault:16} {rs[0]['true_component']:8} {len(rs):>3} {acc:>5.0%}  {ev:>5.0%}  {dict(preds)}")
    n = len(rows)
    acc = sum(r["correct"] for r in rows) / n
    faults = [r for r in rows if r["fault"] != "none"]
    nones = [r for r in rows if r["fault"] == "none"]
    print(f"\noverall accuracy {acc:.0%} over {n} trials"
          + (f" | fault trials {sum(r['correct'] for r in faults)/len(faults):.0%}" if faults else "")
          + (f" | false alarms on 'none': {sum(not r['correct'] for r in nones)}/{len(nones)}" if nones else ""))
    print(f"results: {out}")
    if a.show_wrong:
        for r in rows:
            if not r["correct"]:
                print(f"\n✗ {r['trial_id']}  true={r['true_component']} pred={r['pred_component']} "
                      f"rule={r['pred'].get('rule')}\n  evidence={r['pred'].get('evidence')}")


if __name__ == "__main__":
    main()
