export default function PageHeader({ title, subtitle, action }) {
  return (
    <div className="flex items-start sm:items-center justify-between gap-4 mb-6">
      <div>
        <h1 className="text-3xl font-bold text-text">{title}</h1>
        {subtitle && (
          <p className="text-sm font-normal text-text-muted mt-0.5">{subtitle}</p>
        )}
      </div>
      {action && <div className="shrink-0">{action}</div>}
    </div>
  )
}
