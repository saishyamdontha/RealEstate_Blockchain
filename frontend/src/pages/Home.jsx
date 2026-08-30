import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Home() {
  const { isAuthenticated } = useAuth();

  return (
    <div className="mx-auto max-w-4xl px-6 py-24 text-center">
      <h1 className="text-4xl font-bold tracking-tight text-slate-900">
        Real estate transactions,{" "}
        <span className="text-brand-600">secured on-chain</span>
      </h1>
      <p className="mx-auto mt-4 max-w-2xl text-slate-500">
        List properties, run buyer/seller/lender/inspector escrow, and finalize sales
        through a smart contract — with off-chain records kept in sync automatically.
      </p>
      <div className="mt-8 flex justify-center gap-3">
        {isAuthenticated ? (
          <>
            <Link to="/properties" className="btn-primary">My properties</Link>
            <Link to="/marketplace" className="btn-secondary">Browse marketplace</Link>
          </>
        ) : (
          <>
            <Link to="/register" className="btn-primary">Get started</Link>
            <Link to="/login" className="btn-secondary">Log in</Link>
          </>
        )}
      </div>
    </div>
  );
}
