import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Navbar() {
  const { user, logout, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  const initials = user?.name
    ?.split(" ")
    .map((p) => p[0])
    .slice(0, 2)
    .join("")
    .toUpperCase();

  return (
    <header className="sticky top-0 z-40 border-b border-surface-300/70 bg-surface-0/80 backdrop-blur-md">
      <div className="mx-auto flex max-w-6xl items-center justify-between px-6 py-3.5">
        <Link to="/" className="flex items-center gap-2.5">
          <span className="flex h-9 w-9 items-center justify-center rounded-xl bg-gradient-to-br from-brand-500 to-brand-700 text-sm font-bold text-white shadow-glow">
            RC
          </span>
          <span className="text-[15px] font-display font-bold tracking-tight text-ink-50">
            RealEstate<span className="text-brand-400">Chain</span>
          </span>
        </Link>

        {isAuthenticated ? (
          <nav className="flex items-center gap-1">
            <Link to="/properties" className="btn-ghost">
              My Properties
            </Link>
            <Link to="/marketplace" className="btn-ghost">
              Marketplace
            </Link>
            <Link to="/properties/new" className="btn-primary ml-2">
              + Add Property
            </Link>
            <div className="ml-3 flex items-center gap-2.5 border-l border-surface-300 pl-3">
              <div className="flex h-8 w-8 items-center justify-center rounded-full bg-brand-500/20 text-xs font-bold text-brand-300 ring-1 ring-brand-500/30">
                {initials || "?"}
              </div>
              <button onClick={handleLogout} className="btn-ghost">
                Log out
              </button>
            </div>
          </nav>
        ) : (
          <nav className="flex items-center gap-3">
            <Link to="/login" className="btn-ghost">
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
