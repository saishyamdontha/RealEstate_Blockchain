#!/usr/bin/env python3
"""Background user traffic for the local stack.

Mix per tick: mostly cheap reads, sometimes a full escrow sale.
Every request carries X-Request-Id "load-<flow>-<step>", and the client-side
result (status, latency) is written to JSONL -- that is the "user experience"
ground truth to compare against what the logs say.

  scripts/load.py --out logs/load.jsonl --seconds 60
"""
import argparse, json, os, random, sys, threading, time, urllib.request, urllib.error

sys.path.insert(0, os.path.dirname(__file__))
API = os.environ.get("API", "http://localhost:8080")
WALLET = "0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266"


def now():
    t = time.time()
    return time.strftime("%Y-%m-%dT%H:%M:%S", time.gmtime(t)) + f".{int(t * 1000) % 1000:03d}Z"


class LoadGen:
    def __init__(self, out, rate=1.0, escrow_share=0.25, timeout=15, seed=None):
        self.out, self.rate, self.escrow_share, self.timeout = out, rate, escrow_share, timeout
        self.rng = random.Random(seed)
        self.stop_evt = threading.Event()
        self.thread = threading.Thread(target=self._run, daemon=True)
        self.n = 0
        self.lock = threading.Lock()
        os.makedirs(os.path.dirname(out) or ".", exist_ok=True)

    # ---- plumbing ----
    def _req(self, method, path, rid, body=None):
        data = json.dumps(body).encode() if body is not None else (b"" if method in ("POST", "PUT") else None)
        req = urllib.request.Request(API + path, data=data, method=method,
                                     headers={"X-Request-Id": rid, "Content-Type": "application/json"})
        t0 = time.time(); status, text = 0, ""
        try:
            with urllib.request.urlopen(req, timeout=self.timeout) as r:
                status, text = r.status, r.read().decode()
        except urllib.error.HTTPError as e:
            status, text = e.code, e.read().decode(errors="replace")
        except Exception as e:
            status, text = 0, type(e).__name__ + ": " + str(e)
        rec = {"ts": now(), "rid": rid, "method": method, "path": path.split("?")[0],
               "status": status, "ms": int((time.time() - t0) * 1000)}
        if status == 0 or status >= 500:
            rec["error"] = text[:200]
        with self.lock, open(self.out, "a") as f:
            f.write(json.dumps(rec) + "\n")
        return status, text

    # ---- user behaviours ----
    def _read(self, flow):
        self._req("GET", "/api/user/properties/for-sale", f"load-{flow}-forsale")

    def _escrow(self, flow):
        tag = f"{int(time.time())}{flow}"
        ids = {}
        for role in ("seller", "buyer"):
            st, txt = self._req("POST", "/api/auth/register", f"load-{flow}-reg-{role}",
                                {"name": role, "email": f"{role}-{tag}@load.local", "password": "x",
                                 "uniqueId": f"{role}-{tag}", "walletAddress": WALLET})
            if st != 200:
                return
            ids[role] = json.loads(txt)["id"]
        st, _ = self._req("POST", f"/api/user/add/property/{ids['seller']}/seller-{tag}", f"load-{flow}-addprop",
                          {"propertyId": f"P-{tag}", "address": f"{tag} Load St", "city": "Karlskrona",
                           "propertyType": "House", "latitude": f"{tag}.1", "longitude": f"{tag}.2",
                           "titleDeedNumber": f"TD-{tag}"})
        if st != 200:
            return
        st, txt = self._req("GET", f"/api/user/{ids['seller']}/properties", f"load-{flow}-myprops")
        if st != 200:
            return
        try:
            cid = [p["blockchainPropertyId"] for p in json.loads(txt) if p["propertyId"] == f"P-{tag}"][0]
        except (ValueError, IndexError, KeyError):
            return
        w = WALLET
        steps = [("list", f"/ledger/{cid}/list?buyerAddress={w}&lenderAddress={w}&inspectorAddress={w}"
                          f"&priceWei=10000000000000000&earnestAmountWei=1000000000000000&buyerUniqueId=buyer-{tag}"),
                 ("earnest", f"/ledger/{cid}/earnest?earnestAmountWei=1000000000000000"),
                 ("inspect", f"/ledger/{cid}/inspection?passed=true"),
                 ("fund", f"/ledger/{cid}/fund?remainingWei=9000000000000000"),
                 ("finalize", f"/ledger/{cid}/finalize")]
        for name, path in steps:
            st, _ = self._req("POST", path, f"load-{flow}-{name}")
            if st != 200:
                return

    def _run(self):
        while not self.stop_evt.is_set():
            self.n += 1
            if self.rng.random() < self.escrow_share:
                self._escrow(self.n)
            else:
                self._read(self.n)
            self.stop_evt.wait(self.rng.expovariate(self.rate))

    def start(self):
        self.thread.start(); return self

    def stop(self):
        self.stop_evt.set(); self.thread.join(timeout=self.timeout + 5)


if __name__ == "__main__":
    ap = argparse.ArgumentParser()
    ap.add_argument("--out", default="logs/load.jsonl")
    ap.add_argument("--seconds", type=float, default=60)
    ap.add_argument("--rate", type=float, default=1.0, help="user actions per second (avg)")
    a = ap.parse_args()
    g = LoadGen(a.out, rate=a.rate).start()
    try:
        time.sleep(a.seconds)
    except KeyboardInterrupt:
        pass
    g.stop()
    print(f"{g.n} user actions -> {a.out}", file=sys.stderr)
