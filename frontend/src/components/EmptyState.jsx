export default function EmptyState({ title = 'Nothing here yet', desc = '', icon: Icon, action }) {
  return (
    <div className="flex flex-col items-center justify-center py-16 text-center">
      {Icon && (
        <div className="w-14 h-14 rounded-2xl bg-primary-10 border border-primary-20
                        flex items-center justify-center mb-4">
          <Icon size={26} className="text-primary opacity-60" />
        </div>
      )}
      <p className="text-base font-bold text-text">{title}</p>
      {desc && (
        <p className="text-sm font-normal text-text-muted mt-1 max-w-xs">{desc}</p>
      )}
      {action && <div className="mt-5">{action}</div>}
    </div>
  )
}
