#!/usr/bin/env bash
# End-to-end check of the LOCAL stack: register 2 users, add a property,
# run the whole escrow flow, and verify DB ownership followed the chain.
set -euo pipefail
API=${API:-http://localhost:8080}
WALLET=0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266   # Anvil test account #0 (= backend signer)
RUN=$(date +%s)
json() { python3 -c "import sys,json; d=json.load(sys.stdin); print($1)"; }
step() { printf '%-28s' "$1"; }

step "waiting for backend"
for i in $(seq 1 60); do
  curl -sf "$API/api/user/properties/for-sale" >/dev/null && break
  sleep 2; [ "$i" = 60 ] && { echo FAIL; exit 1; }
done; echo ok

reg() {  # name uniqueId -> db id
  curl -sf -X POST "$API/api/auth/register" -H 'Content-Type: application/json' \
    -d "{\"name\":\"$1\",\"email\":\"$1-$RUN@test.local\",\"password\":\"x\",\"uniqueId\":\"$2\",\"walletAddress\":\"$WALLET\"}" \
  | json 'd["id"]'
}
step "register seller+buyer"
SELLER_UID="seller-$RUN"; BUYER_UID="buyer-$RUN"
SELLER_ID=$(reg seller "$SELLER_UID"); BUYER_ID=$(reg buyer "$BUYER_UID"); echo "ok ($SELLER_ID, $BUYER_ID)"

step "add property (on-chain reg)"
curl -sf -X POST "$API/api/user/add/property/$SELLER_ID/$SELLER_UID" -H 'Content-Type: application/json' \
  -d "{\"propertyId\":\"P-$RUN\",\"address\":\"$RUN Smoke St\",\"city\":\"Karlskrona\",\"propertyType\":\"House\",\"latitude\":\"$RUN.1\",\"longitude\":\"$RUN.2\",\"titleDeedNumber\":\"TD-$RUN\"}" >/dev/null
CHAIN_ID=$(curl -sf "$API/api/user/$SELLER_ID/properties" | json '[p["blockchainPropertyId"] for p in d if p["propertyId"]=="P-'$RUN'"][0]')
echo "ok (chain id $CHAIN_ID)"

PRICE=10000000000000000; EARNEST=1000000000000000; REST=9000000000000000
step "list";        curl -sf -X POST "$API/ledger/$CHAIN_ID/list?buyerAddress=$WALLET&lenderAddress=$WALLET&inspectorAddress=$WALLET&priceWei=$PRICE&earnestAmountWei=$EARNEST&buyerUniqueId=$BUYER_UID" >/dev/null; echo ok
step "deposit earnest"; curl -sf -X POST "$API/ledger/$CHAIN_ID/earnest?earnestAmountWei=$EARNEST" >/dev/null; echo ok
step "inspection passed"; curl -sf -X POST "$API/ledger/$CHAIN_ID/inspection?passed=true" >/dev/null; echo ok
step "lender funds";  curl -sf -X POST "$API/ledger/$CHAIN_ID/fund?remainingWei=$REST" >/dev/null; echo ok
step "finalize"
SYNCED=$(curl -sf -X POST "$API/ledger/$CHAIN_ID/finalize" | json 'd["dbSynced"]'); echo "dbSynced=$SYNCED"

step "DB owner = buyer?"
if curl -sf "$API/api/user/$BUYER_ID/properties" | json 'any(p["propertyId"]=="P-'$RUN'" for p in d)' | grep -q True \
   && [ "$SYNCED" = "True" ]; then
  echo "PASS"
else
  echo "FAIL"; exit 1
fi
