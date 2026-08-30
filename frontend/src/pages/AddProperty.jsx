import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { addProperty } from "../api/client";
import { useAuth } from "../context/AuthContext";

export default function AddProperty() {
  const { user } = useAuth();
  const navigate = useNavigate();

  const [form, setForm] = useState({
    propertyId: "",
    titleDeedNumber: "",
    propertyType: "House",
    address: "",
    city: "",
    latitude: "",
    longitude: "",
  });
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  const update = (field) => (e) => setForm((f) => ({ ...f, [field]: e.target.value }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      await addProperty(user.id, user.uniqueId, form);
      navigate("/properties");
    } catch (err) {
      setError(err.message || "Failed to add property");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="mx-auto max-w-2xl px-6 py-12">
      <h1 className="text-xl font-semibold text-slate-900">Add a property</h1>
      <p className="mt-1 text-sm text-slate-500">
        This registers the property on-chain before saving it — the request may take a moment
        while the blockchain transaction confirms.
      </p>

      <form onSubmit={handleSubmit} className="card mt-6 space-y-4 p-6">
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="label">Property ID</label>
            <input required className="input" value={form.propertyId} onChange={update("propertyId")} placeholder="PROP-002" />
          </div>
          <div>
            <label className="label">Title deed number</label>
            <input required className="input" value={form.titleDeedNumber} onChange={update("titleDeedNumber")} placeholder="TD-002" />
          </div>
        </div>

        <div>
          <label className="label">Property type</label>
          <select className="input" value={form.propertyType} onChange={update("propertyType")}>
            <option>House</option>
            <option>Apartment</option>
            <option>Land</option>
            <option>Commercial</option>
          </select>
        </div>

        <div>
          <label className="label">Address</label>
          <input required className="input" value={form.address} onChange={update("address")} placeholder="123 Main St" />
        </div>

        <div className="grid grid-cols-3 gap-4">
          <div>
            <label className="label">City</label>
            <input required className="input" value={form.city} onChange={update("city")} placeholder="Springfield" />
          </div>
          <div>
            <label className="label">Latitude</label>
            <input required className="input" value={form.latitude} onChange={update("latitude")} placeholder="12.34" />
          </div>
          <div>
            <label className="label">Longitude</label>
            <input required className="input" value={form.longitude} onChange={update("longitude")} placeholder="56.78" />
          </div>
        </div>

        {error && <p className="rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-700">{error}</p>}

        <div className="flex gap-3 pt-2">
          <button type="submit" disabled={loading} className="btn-primary">
            {loading ? "Registering on-chain..." : "Add property"}
          </button>
          <button type="button" onClick={() => navigate(-1)} className="btn-secondary">
            Cancel
          </button>
        </div>
      </form>
    </div>
  );
}
