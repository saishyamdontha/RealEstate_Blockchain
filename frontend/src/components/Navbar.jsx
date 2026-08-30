import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Navbar() {
  const { user, logout, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <header className="border-b border-slate-200 bg-white">
      <div className="mx-auto flex max-w-6xl items-center justify-between px-6 py-4">
        <Link to="/" className="flex items-center gap-2 text-lg font-semibold text-slate-900">
          <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-brand-600 text-white">
            R
          </span>
          RealEstate<span className="text-brand-600">Chain</span>
        </Link>

        {isAuthenticated ? (
          <nav className="flex items-center gap-6 text-sm">
            <Link to="/properties" className="text-slate-600 hover:text-slate-900">
              My Properties
            </Link>
            <Link to="/marketplace" className="text-slate-600 hover:text-slate-900">
              Marketplace
            </Link>
            <Link to="/properties/new" className="btn-primary">
              + Add Property
            </Link>
            <div className="flex items-center gap-3 border-l border-slate-200 pl-6">
              <span className="text-slate-500">{user?.name}</span>
              <button onClick={handleLogout} className="btn-secondary">
                Log out
              </button>
            </div>
          </nav>
        ) : (
          <nav className="flex items-center gap-3 text-sm">
            <Link to="/login" className="btn-secondary">
              Log in
            </Link>
            <Link to="/register" className="btn-primary">
              Sign up
            </Link>
          </nav>
        )}
      </div>
    </header>
  );
}
