import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

const FEATURES = [
  {
    title: "On-chain escrow",
    desc: "A four-party smart contract governs every sale — buyer, seller, lender, and inspector each play a defined role before funds move.",
  },
  {
    title: "Synced records",
    desc: "Off-chain property data stays in lockstep with on-chain state, so what you see in the app always reflects the ledger.",
  },
  {
    title: "Transparent by design",
    desc: "Every listing, deposit, and transfer is a verifiable transaction — not a promise buried in a database.",
  },
];

function AuroraBackground() {
  return (
    <div className="pointer-events-none absolute inset-0 overflow-hidden">
      <div className="absolute left-1/2 top-0 h-[500px] w-[700px] -translate-x-1/2 -translate-y-1/3">
        <div className="absolute left-1/4 top-1/4 h-72 w-72 animate-aurora1 rounded-full bg-brand-600/30 blur-[100px]" />
        <div className="absolute right-1/4 top-1/3 h-80 w-80 animate-aurora2 rounded-full bg-indigo-500/25 blur-[100px]" />
        <div className="absolute left-1/2 top-1/2 h-64 w-64 -translate-x-1/2 animate-aurora3 rounded-full bg-fuchsia-500/15 blur-[100px]" />
      </div>
      <div className="absolute inset-0 bg-grid-pattern bg-[size:32px_32px] [mask-image:radial-gradient(ellipse_60%_50%_at_50%_0%,black_30%,transparent_100%)]" />
    </div>
  );
}

export default function Home() {
  const { isAuthenticated } = useAuth();

  return (
    <div>
      <section className="relative overflow-hidden border-b border-surface-300/70 bg-surface-0">
        <AuroraBackground />
        <div className="relative mx-auto max-w-4xl px-6 py-24 text-center sm:py-32">
          <span className="inline-flex items-center gap-1.5 rounded-full border border-surface-400 bg-surface-100/80 px-3 py-1 text-xs font-medium text-ink-300 backdrop-blur-sm">
            <span className="h-1.5 w-1.5 animate-pulse-slow rounded-full bg-emerald-400" />
            Live on Sepolia testnet
          </span>
          <h1 className="mt-6 text-4xl font-bold leading-tight tracking-tight text-ink-50 sm:text-5xl">
            Property transactions,{" "}
            <span className="bg-gradient-to-r from-brand-400 via-indigo-300 to-brand-300 bg-clip-text text-transparent">
              trustless by default
            </span>
          </h1>
          <p className="mx-auto mt-5 max-w-xl text-base leading-relaxed text-ink-300 sm:text-lg">
            List properties, run buyer/seller/lender/inspector escrow, and finalize
            sales through a smart contract — with off-chain records kept perfectly
            in sync.
          </p>
          <div className="mt-9 flex flex-wrap items-center justify-center gap-3">
            {isAuthenticated ? (
              <>
                <Link to="/properties" className="btn-primary px-6 py-3 text-[15px]">
                  My properties
                </Link>
                <Link to="/marketplace" className="btn-secondary px-6 py-3 text-[15px]">
                  Browse marketplace
                </Link>
              </>
            ) : (
              <>
                <Link to="/register" className="btn-primary px-6 py-3 text-[15px]">
                  Get started
                </Link>
                <Link to="/login" className="btn-secondary px-6 py-3 text-[15px]">
                  Log in
                </Link>
              </>
            )}
          </div>
        </div>
      </section>

      <section className="mx-auto max-w-6xl px-6 py-20">
        <div className="grid gap-6 sm:grid-cols-3">
          {FEATURES.map((f) => (
            <div key={f.title} className="card card-hover p-6">
              <h3 className="font-display text-base font-semibold text-ink-50">{f.title}</h3>
              <p className="mt-2 text-sm leading-relaxed text-ink-300">{f.desc}</p>
            </div>
          ))}
        </div>
      </section>
    </div>
  );
}
