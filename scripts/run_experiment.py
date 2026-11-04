#!/usr/bin/env python3
"""Generate labeled incidents: reset -> load -> inject -> hold -> revert -> save.

Each trial writes trials/<trial_id>/:
  truth.json   ground truth (fault, component, category, root cause, timestamps)
  logs.jsonl   all container logs (structured, from collect_logs.py)
  load.jsonl   client-side view of every user request (status, latency)

  scripts/run_experiment.py --faults all --repeats 1          # ~2.5 min per trial
  scripts/run_experiment.py --faults db_stop,chain_pause --repeats 3
  scripts/run_experiment.py --faults all --dry-run           # print plan only
"""
import argparse, json, os, random, signal, subprocess, sys, time, urllib.request

sys.path.insert(0, os.path.dirname(__file__))
import faults as F
from load import LoadGen, now

HERE = os.path.dirname(os.path.abspath(__file__))


def wait_ready(timeout=180):
    t0 = time.time()
    while time.time() - t0 < timeout:
        try:
            with urllib.request.urlopen(F.API + "/api/user/properties/for-sale", timeout=5) as r:
                if r.status == 200:
                    return True
        except Exception:
            pass
        time.sleep(2)
    return False


def reset_stack():
    F.sh("docker compose down -v --remove-orphans")
    F.sh("docker compose up -d")
    if not F.DRY_RUN and not wait_ready():
        raise RuntimeError("stack did not become ready after reset")


def run_trial(fault, trial_id, root, warmup, hold, cooldown, seed):
    d = os.path.join(root, trial_id)
    os.makedirs(d, exist_ok=True)
    print(f"[{trial_id}] reset", flush=True)
    reset_stack()
    collector = None if F.DRY_RUN else subprocess.Popen(
        [sys.executable, os.path.join(HERE, "collect_logs.py"), "--out", os.path.join(d, "logs.jsonl")],
        stderr=subprocess.DEVNULL)
    load = None if F.DRY_RUN else LoadGen(os.path.join(d, "load.jsonl"), seed=seed).start()
    truth = {"trial_id": trial_id, **fault.truth(), "warmup_s": warmup, "hold_s": hold, "cooldown_s": cooldown}
    try:
        print(f"[{trial_id}] warmup {warmup}s", flush=True); sleep(warmup)
        truth["inject_ts"] = now()
        try:
            fault.inject()
        except Exception as e:
            truth["inject_error"] = str(e)[:500]
        print(f"[{trial_id}] injected {fault.name}, holding {hold}s", flush=True); sleep(hold)
        truth["revert_ts"] = now()
        try:
            fault.revert()
        except Exception as e:
            truth["revert_error"] = str(e)[:500]
        print(f"[{trial_id}] reverted, cooldown {cooldown}s", flush=True); sleep(cooldown)
        truth["end_ts"] = now()
    finally:
        if load:
            load.stop()
        if collector:
            collector.send_signal(signal.SIGINT)
            try:
                collector.wait(timeout=15)
            except subprocess.TimeoutExpired:
                collector.kill()
        with open(os.path.join(d, "truth.json"), "w") as f:
            json.dump(truth, f, indent=2)
    print(f"[{trial_id}] done{' (INJECT ERROR: ' + truth['inject_error'] + ')' if 'inject_error' in truth else ''}",
          flush=True)
    return truth


def sleep(s):
    if not F.DRY_RUN:
        time.sleep(s)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--faults", default="all", help="comma list or 'all'")
    ap.add_argument("--repeats", type=int, default=1)
    ap.add_argument("--warmup", type=int, default=30)
    ap.add_argument("--hold", type=int, default=45)
    ap.add_argument("--cooldown", type=int, default=20)
    ap.add_argument("--out", default="trials")
    ap.add_argument("--seed", type=int, default=42)
    ap.add_argument("--dry-run", action="store_true")
    a = ap.parse_args()
    F.DRY_RUN = a.dry_run

    names = list(F.FAULTS) if a.faults == "all" else a.faults.split(",")
    unknown = [n for n in names if n not in F.FAULTS]
    if unknown:
        sys.exit(f"unknown faults: {unknown}")
    plan = [(n, r) for r in range(a.repeats) for n in names]
    random.Random(a.seed).shuffle(plan)      # randomized order: no drift/ordering bias
    stamp = time.strftime("%Y%m%dT%H%M%SZ", time.gmtime())
    print(f"{len(plan)} trials, ~{len(plan) * (a.warmup + a.hold + a.cooldown + 35) / 60:.0f} min", flush=True)

    index = []
    for i, (name, rep) in enumerate(plan):
        tid = f"{stamp}-{i:03d}-{name}"
        index.append(run_trial(F.FAULTS[name], tid, a.out, a.warmup, a.hold, a.cooldown, a.seed + i))
    os.makedirs(a.out, exist_ok=True)
    with open(os.path.join(a.out, f"index-{stamp}.jsonl"), "w") as f:
        for t in index:
            f.write(json.dumps(t) + "\n")
    print(f"index written: {a.out}/index-{stamp}.jsonl")
    if not F.DRY_RUN:
        reset_stack()   # leave a clean stack behind


if __name__ == "__main__":
    main()
