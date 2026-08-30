import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { registerUser } from "../api/client";
import { useAuth } from "../context/AuthContext";

export default function Register() {
  const [form, setForm] = useState({
    name: "",
    email: "",
    phone: "",
    password: "",
    uniqueId: "",
  });
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  const { login } = useAuth();
  const navigate = useNavigate();

  const update = (field) => (e) => setForm((f) => ({ ...f, [field]: e.target.value }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      const user = await registerUser(form);
      login(user);
      navigate("/properties");
    } catch (err) {
      setError(err.message || "Registration failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="mx-auto flex min-h-[calc(100vh-73px)] max-w-md flex-col justify-center px-6 py-12">
      <div className="card p-8">
        <h1 className="text-xl font-semibold text-slate-900">Create an account</h1>
        <p className="mt-1 text-sm text-slate-500">
          Register to list or buy property on-chain.
        </p>

        <form onSubmit={handleSubmit} className="mt-6 space-y-4">
          <div>
            <label className="label">Full name</label>
            <input required className="input" value={form.name} onChange={update("name")} placeholder="Jane Doe" />
          </div>
          <div>
            <label className="label">Unique ID</label>
            <input
              required
              className="input"
              value={form.uniqueId}
              onChange={update("uniqueId")}
              placeholder="jane-doe-001"
            />
            <p className="mt-1 text-xs text-slate-400">
              A short handle used to identify you in property transfers (e.g. as buyer/seller).
            </p>
          </div>
          <div>
            <label className="label">Email</label>
            <input type="email" required className="input" value={form.email} onChange={update("email")} placeholder="you@example.com" />
          </div>
          <div>
            <label className="label">Phone</label>
            <input required className="input" value={form.phone} onChange={update("phone")} placeholder="1234567890" />
          </div>
          <div>
            <label className="label">Password</label>
            <input type="password" required className="input" value={form.password} onChange={update("password")} placeholder="••••••••" />
          </div>

          {error && (
            <p className="rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-700">{error}</p>
          )}

          <button type="submit" disabled={loading} className="btn-primary w-full">
            {loading ? "Creating account..." : "Sign up"}
          </button>
        </form>

        <p className="mt-6 text-center text-sm text-slate-500">
          Already have an account?{" "}
          <Link to="/login" className="font-medium text-brand-600 hover:text-brand-700">
            Log in
          </Link>
        </p>
      </div>
    </div>
  );
}
