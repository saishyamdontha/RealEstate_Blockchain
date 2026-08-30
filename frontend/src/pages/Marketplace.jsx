import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getPropertiesForSale } from "../api/client";

export default function Marketplace() {
  const [properties, setProperties] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    getPropertiesForSale()
      .then(setProperties)
      .catch((err) => setError(err.message || "Failed to load marketplace"))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="mx-auto max-w-6xl px-6 py-12">
      <h1 className="text-xl font-semibold text-slate-900">Marketplace</h1>
      <p className="mt-1 text-sm text-slate-500">Properties currently listed for sale.</p>

      {loading && <p className="mt-6 text-sm text-slate-500">Loading...</p>}
      {error && <p className="mt-6 rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-700">{error}</p>}

      {!loading && !error && properties.length === 0 && (
        <div className="card mt-6 p-10 text-center text-sm text-slate-500">
          No properties are currently listed for sale.
        </div>
      )}

      <div className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {properties.map((p) => (
          <div key={p.id} className="card p-5">
            <h3 className="font-medium text-slate-900">{p.propertyId}</h3>
            <p className="mt-1 text-sm text-slate-500">{p.address}, {p.city}</p>
            <p className="mt-1 text-xs text-slate-400">{p.propertyType} · Deed {p.titleDeedNumber}</p>
            {p.blockchainPropertyId != null && (
              <Link
                to={`/escrow/${p.blockchainPropertyId}`}
                className="btn-primary mt-4 inline-flex text-xs"
              >
                View escrow status
              </Link>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}
