import { useEffect, useState, useCallback } from "react";
import { Link } from "react-router-dom";
import { getPropertiesByUser, markPropertyForSale, deleteProperty } from "../api/client";
import { useAuth } from "../context/AuthContext";
import StatusBadge from "../components/StatusBadge";

export default function MyProperties() {
  const { user } = useAuth();
  const [properties, setProperties] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [actionError, setActionError] = useState(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await getPropertiesByUser(user.id);
      setProperties(data);
    } catch (err) {
      setError(err.message || "Failed to load properties");
    } finally {
      setLoading(false);
    }
  }, [user.id]);

  useEffect(() => {
    load();
  }, [load]);

  const handleMarkForSale = async (propertyId) => {
    setActionError(null);
    try {
      await markPropertyForSale(propertyId, user.uniqueId);
      load();
    } catch (err) {
      setActionError(err.message || "Failed to mark for sale");
    }
  };

  const handleDelete = async (propertyId) => {
    setActionError(null);
    try {
      await deleteProperty(user.id, propertyId);
      load();
    } catch (err) {
      setActionError(err.message || "Failed to delete property");
    }
  };

  return (
    <div className="mx-auto max-w-6xl px-6 py-14">
      <div className="flex items-end justify-between">
        <div>
          <span className="section-eyebrow">Portfolio</span>
          <h1 className="mt-1 text-2xl font-bold tracking-tight text-ink-50">My properties</h1>
        </div>
        <Link to="/properties/new" className="btn-primary">
          + Add property
        </Link>
      </div>

      {actionError && (
        <p className="mt-4 rounded-xl border border-rose-500/30 bg-rose-500/10 px-4 py-3 text-sm text-rose-300">
          {actionError}
        </p>
      )}

      {loading && (
        <div className="mt-8 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {[1, 2, 3].map((i) => (
            <div key={i} className="card h-48 animate-pulse p-5">
              <div className="h-4 w-24 rounded bg-surface-300" />
              <div className="mt-3 h-3 w-40 rounded bg-surface-300" />
              <div className="mt-2 h-3 w-32 rounded bg-surface-300" />
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
            🏠
          </div>
          <p className="text-sm text-ink-300">You haven't added any properties yet.</p>
          <Link to="/properties/new" className="btn-primary mt-1">
            + Add your first property
          </Link>
        </div>
      )}

      <div className="mt-8 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {properties.map((p) => (
          <div key={p.id} className="card card-hover p-5">
            <div className="flex items-start justify-between gap-2">
              <h3 className="font-display text-base font-semibold text-ink-50">{p.propertyId}</h3>
              <StatusBadge status={p.propertyStatus} />
            </div>
            <p className="mt-1.5 text-sm text-ink-300">
              {p.address}, {p.city}
            </p>
            <p className="mt-1 text-xs text-ink-500">
              {p.propertyType} · Deed {p.titleDeedNumber}
            </p>
            <p className="mt-2 text-xs text-ink-500">
              On-chain ID: <span className="text-ink-300">{p.blockchainPropertyId ?? "—"}</span>
            </p>

            <div className="mt-4 flex flex-wrap gap-2">
              {!p.forSale ? (
                <button onClick={() => handleMarkForSale(p.id)} className="btn-secondary px-3 py-1.5 text-xs">
                  Mark for sale
                </button>
              ) : (
                <span className="badge bg-brand-500/15 text-brand-300 ring-1 ring-brand-500/30">
                  Listed for sale
                </span>
              )}
              {p.blockchainPropertyId != null && (
                <Link
                  to={`/escrow/${p.blockchainPropertyId}`}
                  className="btn-ghost border border-surface-400 px-3 py-1.5 text-xs"
                >
                  Escrow flow
                </Link>
              )}
              <button
                onClick={() => handleDelete(p.id)}
                disabled={p.forSale}
                className="btn-ghost px-3 py-1.5 text-xs text-rose-400 hover:bg-rose-500/10 hover:text-rose-300 disabled:text-ink-500 disabled:hover:bg-transparent"
                title={p.forSale ? "Cannot delete a property listed for sale" : ""}
              >
                Delete
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
