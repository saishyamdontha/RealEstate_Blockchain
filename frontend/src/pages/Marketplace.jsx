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
    <div className="mx-auto max-w-6xl px-6 py-14">
      <span className="section-eyebrow">Browse</span>
      <h1 className="mt-1 text-2xl font-bold tracking-tight text-ink-50">Marketplace</h1>
      <p className="mt-1 text-sm text-ink-400">Properties currently listed for sale.</p>

      {loading && (
        <div className="mt-8 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {[1, 2, 3].map((i) => (
            <div key={i} className="card h-48 animate-pulse p-5">
              <div className="h-4 w-24 rounded bg-surface-300" />
              <div className="mt-3 h-3 w-40 rounded bg-surface-300" />
            </div>
          ))}
        </div>
      )}

      {error && (
        <p className="mt-8 rounded-xl border border-rose-500/30 bg-rose-500/10 px-4 py-3 text-sm text-rose-300">
          {error}
        </p>
      )}

      {!loading && !error && properties.length === 0 && (
        <div className="card mt-8 flex flex-col items-center gap-3 p-14 text-center">
          <div className="flex h-12 w-12 items-center justify-center rounded-full bg-surface-300 text-xl">
            🔎
          </div>
          <p className="text-sm text-ink-300">No properties are currently listed for sale.</p>
        </div>
      )}

      <div className="mt-8 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {properties.map((p) => (
          <div key={p.id} className="card card-hover p-5">
            <h3 className="font-display text-base font-semibold text-ink-50">{p.propertyId}</h3>
            <p className="mt-1.5 text-sm text-ink-300">
              {p.address}, {p.city}
            </p>
            <p className="mt-1 text-xs text-ink-500">
              {p.propertyType} · Deed {p.titleDeedNumber}
            </p>
            {p.blockchainPropertyId != null && (
              <Link
                to={`/escrow/${p.blockchainPropertyId}`}
                className="btn-primary mt-4 inline-flex px-3.5 py-2 text-xs"
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
