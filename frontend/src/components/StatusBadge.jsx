const STYLES = {
  PENDING: "bg-amber-500/15 text-amber-400 ring-1 ring-amber-500/30",
  APPROVE: "bg-emerald-500/15 text-emerald-400 ring-1 ring-emerald-500/30",
  REJECT: "bg-rose-500/15 text-rose-400 ring-1 ring-rose-500/30",
  TRANSFERRED: "bg-surface-300 text-ink-300 ring-1 ring-surface-400",
};

export default function StatusBadge({ status }) {
  const style = STYLES[status] || "bg-surface-300 text-ink-300 ring-1 ring-surface-400";
  return <span className={`badge ${style}`}>{status}</span>;
}
