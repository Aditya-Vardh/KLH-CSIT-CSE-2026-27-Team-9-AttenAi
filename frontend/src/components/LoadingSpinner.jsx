export default function LoadingSpinner({ full }) {
  if (full) return (
    <div className="flex flex-col items-center justify-center h-64 gap-3">
      <div className="w-10 h-10 rounded-full border-[3px] border-primary-20 border-t-primary animate-spin" />
      <p className="text-sm font-normal text-text-muted animate-pulse-soft">Loading…</p>
    </div>
  )
  return (
    <div className="w-5 h-5 rounded-full border-2 border-primary-20 border-t-primary animate-spin inline-block" />
  )
}
