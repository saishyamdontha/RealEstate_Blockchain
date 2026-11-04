#!/usr/bin/env python3
"""Fault catalogue for the local stack (docker-compose.yml).

Every fault has:
  inject()  - breaks exactly one thing
  revert()  - undoes it (or marks that a full reset is needed)
  truth     - the ground-truth label a diagnoser must recover

Run directly to try a single fault by hand:
  scripts/faults.py list
  scripts/faults.py inject db_stop     # breaks it, prints the truth record
  scripts/faults.py revert db_stop
  scripts/faults.py inject db_stop --dry-run   # print commands only
"""
import argparse, json, os, subprocess, sys, time, urllib.request, urllib.error
from dataclasses import dataclass, field, asdict

API = os.environ.get("API", "http://localhost:8080")
WALLET = "0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266"      # Anvil test account #0 = backend signer
TEN_K_ETH = "0x21E19E0C9BAB2400000"
DRY_RUN = False


def sh(cmd, env=None, check=True):
    """Run a shell command (or just print it in dry-run mode)."""
    if DRY_RUN:
        print(f"  $ {cmd}" + (f"   [env {env}]" if env else ""))
        return ""
    full_env = {**os.environ, **(env or {})}
    r = subprocess.run(cmd, shell=True, env=full_env, capture_output=True, text=True)
    if check and r.returncode != 0:
        raise RuntimeError(f"command failed ({r.returncode}): {cmd}\n{r.stderr.strip()}")
    return r.stdout.strip()


def api(method, path, rid, timeout=15):
    """Call the backend; never raises, returns HTTP status (0 = no response)."""
    if DRY_RUN:
        print(f"  HTTP {method} {path}  [X-Request-Id: {rid}]")
        return 200, ""
    req = urllib.request.Request(API + path, method=method, headers={"X-Request-Id": rid,
                                 "Content-Type": "application/json"}, data=b"" if method == "POST" else None)
    try:
        with urllib.request.urlopen(req, timeout=timeout) as r:
            return r.status, r.read().decode()
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode()
    except Exception as e:  # timeout, connection refused
        return 0, str(e)


def restart_backend(env=None):
    """Recreate ONLY the backend container, optionally with FAULT_* overrides."""
    sh("docker compose up -d --no-deps --force-recreate backend", env=env)


@dataclass
class Fault:
    name: str
    component: str          # ground-truth root-cause component
    category: str           # dependency_down | dependency_hang | dependency_slow | config | resource | state | data | none
    root_cause: str         # one-sentence ground truth, used by the scorer / LLM judge
    inject: callable = field(repr=False)
    revert: callable = field(repr=False)
    needs_reset: bool = False   # revert alone can't restore a clean stack

    def truth(self):
        d = asdict(self)
        for k in ("inject", "revert"):
            d.pop(k)
        return d


def _desync_flow():
    """Run one escrow sale WITHOUT recording the buyer -> finalize leaves the DB stale."""
    rid = f"fault-desync-{int(time.time())}"
    uid = rid
    body = json.dumps({"name": "desync", "email": f"{uid}@t.local", "password": "x",
                       "uniqueId": uid, "walletAddress": WALLET}).encode()
    if DRY_RUN:
        print(f"  HTTP register + add property + list(no buyerUniqueId) + earnest + inspection + fund + finalize [{rid}]")
        return
    req = urllib.request.Request(API + "/api/auth/register", data=body, method="POST",
                                 headers={"Content-Type": "application/json", "X-Request-Id": rid})
    user_id = json.load(urllib.request.urlopen(req, timeout=15))["id"]
    prop = json.dumps({"propertyId": f"P-{uid}", "address": f"{uid} Desync St", "city": "X",
                       "propertyType": "House", "latitude": uid + "a", "longitude": uid + "b",
                       "titleDeedNumber": f"TD-{uid}"}).encode()
    req = urllib.request.Request(f"{API}/api/user/add/property/{user_id}/{uid}", data=prop, method="POST",
                                 headers={"Content-Type": "application/json", "X-Request-Id": rid})
    urllib.request.urlopen(req, timeout=15).read()
    props = json.load(urllib.request.urlopen(urllib.request.Request(
        f"{API}/api/user/{user_id}/properties", headers={"X-Request-Id": rid}), timeout=15))
    cid = [p["blockchainPropertyId"] for p in props if p["propertyId"] == f"P-{uid}"][0]
    w = WALLET
    for path in (f"/ledger/{cid}/list?buyerAddress={w}&lenderAddress={w}&inspectorAddress={w}"
                 f"&priceWei=10000000000000000&earnestAmountWei=1000000000000000",   # no buyerUniqueId!
                 f"/ledger/{cid}/earnest?earnestAmountWei=1000000000000000",
                 f"/ledger/{cid}/inspection?passed=true",
                 f"/ledger/{cid}/fund?remainingWei=9000000000000000",
                 f"/ledger/{cid}/finalize"):
        api("POST", path, rid)


DB_LOCK_SQL = ("BEGIN; LOCK TABLE properties IN ACCESS EXCLUSIVE MODE; "
               "SELECT pg_sleep(600); COMMIT;")
DB_UNLOCK_SQL = ("SELECT pg_terminate_backend(pid) FROM pg_stat_activity "
                 "WHERE query LIKE '%pg_sleep(600)%' AND pid <> pg_backend_pid();")

FAULTS = {f.name: f for f in [
    Fault("none", "none", "none", "No fault injected; any alert is a false positive.",
          inject=lambda: None, revert=lambda: None),

    Fault("db_stop", "db", "dependency_down",
          "PostgreSQL container is stopped; backend cannot get DB connections.",
          inject=lambda: sh("docker compose stop db"),
          revert=lambda: sh("docker compose start db")),

    Fault("db_pause", "db", "dependency_hang",
          "PostgreSQL container is frozen (paused); DB calls hang until timeout.",
          inject=lambda: sh("docker compose pause db"),
          revert=lambda: sh("docker compose unpause db")),

    Fault("db_lock", "db", "dependency_slow",
          "A long transaction holds an exclusive lock on the properties table; property queries block.",
          inject=lambda: sh(f'docker compose exec -d -T db psql -U postgres -d re2_app -c "{DB_LOCK_SQL}"'),
          revert=lambda: sh(f'docker compose exec -T db psql -U postgres -d re2_app -c "{DB_UNLOCK_SQL}"')),

    Fault("db_bad_password", "backend", "config",
          "Backend restarted with a wrong database password; it cannot authenticate to PostgreSQL.",
          inject=lambda: restart_backend({"FAULT_BACKEND_DB_PASSWORD": "wrong-password"}),
          revert=lambda: restart_backend()),

    Fault("chain_stop", "chain", "dependency_down",
          "Blockchain node (Anvil) is stopped; all contract calls fail with connect errors/timeouts.",
          inject=lambda: sh("docker compose stop chain"),
          revert=lambda: sh("docker compose start chain"), needs_reset=True),

    Fault("chain_pause", "chain", "dependency_hang",
          "Blockchain node is frozen (paused); contract calls hang until timeout.",
          inject=lambda: sh("docker compose pause chain"),
          revert=lambda: sh("docker compose unpause chain")),

    Fault("chain_reset", "chain", "state",
          "Blockchain node restarted and lost its in-memory state; the contract no longer exists at its address.",
          inject=lambda: sh("docker compose restart chain"),
          revert=lambda: None, needs_reset=True),

    Fault("wallet_drained", "wallet", "resource",
          "Backend signer wallet has zero ETH; reads work but every transaction fails for lack of gas.",
          inject=lambda: sh(f"docker compose exec -T chain cast rpc anvil_setBalance {WALLET} 0x0 "
                            "--rpc-url http://localhost:8545"),
          revert=lambda: sh(f"docker compose exec -T chain cast rpc anvil_setBalance {WALLET} {TEN_K_ETH} "
                            "--rpc-url http://localhost:8545")),

    Fault("wrong_contract", "backend", "config",
          "Backend restarted with a wrong contract address; calls go to an address with no contract code.",
          inject=lambda: restart_backend({"FAULT_BACKEND_CONTRACT": "0x000000000000000000000000000000000000dEaD"}),
          revert=lambda: restart_backend()),

    Fault("backend_crash", "backend", "resource",
          "Backend process was killed; the API is unreachable.",
          inject=lambda: sh("docker compose kill backend"),
          revert=lambda: sh("docker compose start backend")),

    Fault("escrow_desync", "backend", "data",
          "An escrow sale was finalized on-chain without a recorded buyer; DB ownership no longer matches the chain.",
          inject=_desync_flow, revert=lambda: None),
]}


def main():
    global DRY_RUN
    ap = argparse.ArgumentParser()
    ap.add_argument("action", choices=["list", "inject", "revert"])
    ap.add_argument("fault", nargs="?")
    ap.add_argument("--dry-run", action="store_true")
    a = ap.parse_args()
    DRY_RUN = a.dry_run
    if a.action == "list":
        for f in FAULTS.values():
            print(f"{f.name:16} {f.component:8} {f.category:16} {f.root_cause}")
        return
    if a.fault not in FAULTS:
        sys.exit(f"unknown fault {a.fault!r}; try: {', '.join(FAULTS)}")
    f = FAULTS[a.fault]
    getattr(f, a.action)()
    print(json.dumps({"action": a.action, "ts": time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime()), **f.truth()}))


if __name__ == "__main__":
    main()
