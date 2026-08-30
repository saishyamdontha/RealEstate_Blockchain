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
    <div className="mx-auto max-w-6xl px-6 py-12">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold text-slate-900">My properties</h1>
        <Link to="/properties/new" className="btn-primary">
          + Add property
        </Link>
      </div>

      {actionError && (
        <p className="mt-4 rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-700">{actionError}</p>
      )}

      {loading && <p className="mt-6 text-sm text-slate-500">Loading...</p>}
      {error && <p className="mt-6 rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-700">{error}</p>}

      {!loading && !error && properties.length === 0 && (
        <div className="card mt-6 p-10 text-center text-sm text-slate-500">
          You haven't added any properties yet.
        </div>
      )}

      <div className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {properties.map((p) => (
          <div key={p.id} className="card p-5">
            <div className="flex items-start justify-between">
              <h3 className="font-medium text-slate-900">{p.propertyId}</h3>
              <StatusBadge status={p.propertyStatus} />
            </div>
            <p className="mt-1 text-sm text-slate-500">{p.address}, {p.city}</p>
            <p className="mt-1 text-xs text-slate-400">{p.propertyType} · Deed {p.titleDeedNumber}</p>
            <p className="mt-2 text-xs text-slate-400">
              On-chain ID: {p.blockchainPropertyId ?? "—"}
            </p>

            <div className="mt-4 flex flex-wrap gap-2">
              {!p.forSale ? (
                <button onClick={() => handleMarkForSale(p.id)} className="btn-secondary text-xs">
                  Mark for sale
                </button>
              ) : (
                <span className="badge bg-brand-50 text-brand-700">Listed for sale</span>
              )}
              {p.blockchainPropertyId != null && (
                <Link to={`/escrow/${p.blockchainPropertyId}`} className="btn-secondary text-xs">
                  Escrow flow
                </Link>
              )}
              <button
                onClick={() => handleDelete(p.id)}
                disabled={p.forSale}
                className="btn-secondary text-xs text-rose-600 disabled:text-slate-400"
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
