export function SkeletonCard({ rows = 3 }) {
  return (
    <div className="card animate-pulse space-y-3">
      <div className="skeleton h-5 w-2/5 rounded-lg" />
      {Array.from({ length: rows }).map((_, i) => (
        <div key={i} className={`skeleton h-3 rounded-lg ${i % 2 === 0 ? 'w-full' : 'w-4/5'}`} />
      ))}
    </div>
  )
}

export function SkeletonStatCards({ count = 4 }) {
  return (
    <div className={`grid gap-4 grid-cols-2 ${count >= 4 ? 'lg:grid-cols-4' : `sm:grid-cols-${count}`}`}>
      {Array.from({ length: count }).map((_, i) => (
        <div key={i} className="card animate-pulse space-y-3">
          <div className="skeleton h-11 w-11 rounded-xl" />
          <div className="skeleton h-3 w-3/5 rounded" />
          <div className="skeleton h-7 w-2/5 rounded" />
        </div>
      ))}
    </div>
  )
}

export function SkeletonTable({ rows = 5, cols = 5 }) {
  return (
    <div className="space-y-3 animate-pulse">
      {Array.from({ length: rows }).map((_, i) => (
        <div key={i} className="flex gap-4">
          {Array.from({ length: cols }).map((_, j) => (
            <div key={j} className={`skeleton h-3 rounded flex-1 ${j === 0 ? 'max-w-[80px]' : ''}`} />
          ))}
        </div>
      ))}
    </div>
  )
}
