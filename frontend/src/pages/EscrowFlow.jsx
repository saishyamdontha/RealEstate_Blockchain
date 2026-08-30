import { useState, useEffect, useCallback } from "react";
import { useParams } from "react-router-dom";
import {
  ledgerGetSale,
  ledgerList,
  ledgerDepositEarnest,
  ledgerUpdateInspection,
  ledgerFund,
  ledgerFinalize,
  ledgerCancel,
} from "../api/client";

const STATUS_LABELS = [
  "Listed",
  "Earnest Deposited",
  "Inspection Passed",
  "Inspection Failed",
  "Finalized",
  "Cancelled",
];

function Section({ title, children }) {
  return (
    <div className="card p-5">
      <h3 className="mb-3 text-sm font-semibold text-slate-900">{title}</h3>
      {children}
    </div>
  );
}

function ActionResult({ result }) {
  if (!result) return null;
  const isError = result.type === "error";
  return (
    <p
      className={`mt-3 rounded-lg px-3 py-2 text-sm ${
        isError ? "bg-rose-50 text-rose-700" : "bg-emerald-50 text-emerald-700"
      }`}
    >
      {result.message}
    </p>
  );
}

export default function EscrowFlow() {
  const { propertyId } = useParams();
  const [sale, setSale] = useState(null);
  const [loadingSale, setLoadingSale] = useState(true);
  const [results, setResults] = useState({});

  const loadSale = useCallback(async () => {
    setLoadingSale(true);
    try {
      const data = await ledgerGetSale(propertyId);
      setSale(data);
    } catch {
      setSale(null);
    } finally {
      setLoadingSale(false);
    }
  }, [propertyId]);

  useEffect(() => {
    loadSale();
  }, [loadSale]);

  const runAction = async (key, fn) => {
    setResults((r) => ({ ...r, [key]: null }));
    try {
      const res = await fn();
      setResults((r) => ({ ...r, [key]: { type: "success", message: JSON.stringify(res) } }));
      loadSale();
    } catch (err) {
      setResults((r) => ({ ...r, [key]: { type: "error", message: err.message } }));
    }
  };

  // ---- List form state ----
  const [listForm, setListForm] = useState({
    buyerAddress: "",
    lenderAddress: "",
    inspectorAddress: "",
    priceWei: "",
    earnestAmountWei: "",
  });
  const updateList = (field) => (e) => setListForm((f) => ({ ...f, [field]: e.target.value }));

  // ---- Simple numeric inputs ----
  const [earnestWei, setEarnestWei] = useState("");
  const [remainingWei, setRemainingWei] = useState("");
  const [inspectionPassed, setInspectionPassed] = useState(true);

  return (
    <div className="mx-auto max-w-3xl px-6 py-12">
      <h1 className="text-xl font-semibold text-slate-900">Escrow — Property #{propertyId}</h1>
      <p className="mt-1 text-sm text-slate-500">
        Each step below calls the escrow smart contract directly. If a step reverts
        (e.g. calling from the wrong wallet role), the exact on-chain error is shown.
      </p>

      <div className="card mt-6 p-5">
        <h3 className="text-sm font-semibold text-slate-900">Current sale state</h3>
        {loadingSale && <p className="mt-2 text-sm text-slate-500">Loading...</p>}
        {!loadingSale && !sale && (
          <p className="mt-2 text-sm text-slate-500">
            No sale found for this property yet — list it below to begin.
          </p>
        )}
        {!loadingSale && sale && (
          <dl className="mt-3 grid grid-cols-2 gap-x-6 gap-y-2 text-sm">
            <dt className="text-slate-500">Status</dt>
            <dd className="font-medium text-slate-900">{STATUS_LABELS[sale.status] ?? sale.status}</dd>
            <dt className="text-slate-500">Seller</dt>
            <dd className="truncate font-mono text-xs">{sale.seller}</dd>
            <dt className="text-slate-500">Buyer</dt>
            <dd className="truncate font-mono text-xs">{sale.buyer}</dd>
            <dt className="text-slate-500">Price (wei)</dt>
            <dd>{sale.price}</dd>
            <dt className="text-slate-500">Earnest (wei)</dt>
            <dd>{sale.earnestAmount}</dd>
          </dl>
        )}
      </div>

      <div className="mt-6 space-y-6">
        <Section title="1. List for sale">
          <div className="grid grid-cols-2 gap-3">
            <input className="input" placeholder="Buyer address" value={listForm.buyerAddress} onChange={updateList("buyerAddress")} />
            <input className="input" placeholder="Lender address" value={listForm.lenderAddress} onChange={updateList("lenderAddress")} />
            <input className="input" placeholder="Inspector address" value={listForm.inspectorAddress} onChange={updateList("inspectorAddress")} />
            <input className="input" placeholder="Price (wei)" value={listForm.priceWei} onChange={updateList("priceWei")} />
            <input className="input col-span-2" placeholder="Earnest amount (wei)" value={listForm.earnestAmountWei} onChange={updateList("earnestAmountWei")} />
          </div>
          <button
            className="btn-primary mt-3"
            onClick={() => runAction("list", () => ledgerList(propertyId, listForm))}
          >
            List property
          </button>
          <ActionResult result={results.list} />
        </Section>

        <Section title="2. Deposit earnest (buyer)">
          <input className="input" placeholder="Earnest amount (wei)" value={earnestWei} onChange={(e) => setEarnestWei(e.target.value)} />
          <button
            className="btn-primary mt-3"
            onClick={() => runAction("earnest", () => ledgerDepositEarnest(propertyId, earnestWei))}
          >
            Deposit earnest
          </button>
          <ActionResult result={results.earnest} />
        </Section>

        <Section title="3. Inspection result (inspector)">
          <div className="flex items-center gap-4">
            <label className="flex items-center gap-2 text-sm">
              <input type="radio" checked={inspectionPassed} onChange={() => setInspectionPassed(true)} />
              Passed
            </label>
            <label className="flex items-center gap-2 text-sm">
              <input type="radio" checked={!inspectionPassed} onChange={() => setInspectionPassed(false)} />
              Failed
            </label>
          </div>
          <button
            className="btn-primary mt-3"
            onClick={() => runAction("inspection", () => ledgerUpdateInspection(propertyId, inspectionPassed))}
          >
            Submit inspection result
          </button>
          <ActionResult result={results.inspection} />
        </Section>

        <Section title="4. Fund as lender">
          <input className="input" placeholder="Remaining balance (wei)" value={remainingWei} onChange={(e) => setRemainingWei(e.target.value)} />
          <button
            className="btn-primary mt-3"
            onClick={() => runAction("fund", () => ledgerFund(propertyId, remainingWei))}
          >
            Fund
          </button>
          <ActionResult result={results.fund} />
        </Section>

        <Section title="5. Finalize or cancel">
          <div className="flex gap-3">
            <button className="btn-primary" onClick={() => runAction("finalize", () => ledgerFinalize(propertyId))}>
              Finalize sale
            </button>
            <button className="btn-secondary" onClick={() => runAction("cancel", () => ledgerCancel(propertyId))}>
              Cancel sale
            </button>
          </div>
          <ActionResult result={results.finalize || results.cancel} />
        </Section>
      </div>
    </div>
  );
}
