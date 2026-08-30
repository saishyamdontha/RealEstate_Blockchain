const STYLES = {
  PENDING: "bg-amber-100 text-amber-800",
  APPROVE: "bg-emerald-100 text-emerald-800",
  REJECT: "bg-rose-100 text-rose-800",
  TRANSFERRED: "bg-slate-200 text-slate-700",
};

export default function StatusBadge({ status }) {
  const style = STYLES[status] || "bg-slate-100 text-slate-600";
  return <span className={`badge ${style}`}>{status}</span>;
}
