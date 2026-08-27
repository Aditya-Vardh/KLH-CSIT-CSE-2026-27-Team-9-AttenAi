/**
 * StatCard — dark-palette stat tile.
 *
 * color: 'primary' | 'accent' | 'secondary' | 'error' | 'muted'
 * Maps to the brand token set: primary=gold, accent=bright-green, secondary=olive, error=red.
 */
const THEMES = {
  primary:   { icon: 'bg-primary-10  text-primary  border-primary-20',  bar: 'from-primary   to-primary-50'   },
  accent:    { icon: 'bg-accent-10   text-accent   border-accent-20',   bar: 'from-accent    to-accent/50'    },
  secondary: { icon: 'bg-secondary-10 text-accent  border-secondary-20',bar: 'from-secondary to-secondary-50' },
  error:     { icon: 'bg-error/10    text-error    border-error/20',    bar: 'from-error     to-error/50'     },
  muted:     { icon: 'bg-bg3         text-text-muted border-border-subtle', bar: 'from-text-faint to-transparent' },
  // legacy aliases from old palette — map to nearest token
  blue:    'primary',
  green:   'accent',
  yellow:  'primary',
  red:     'error',
  purple:  'primary',
  indigo:  'primary',
  teal:    'secondary',
}

function resolve(color) {
  const t = THEMES[color] ?? THEMES.primary
  return typeof t === 'string' ? THEMES[t] : t
}

export default function StatCard({ label, value, icon: Icon, color = 'primary', sub, trend }) {
  const theme = resolve(color)
  return (
    <div className="card-hover group cursor-default select-none">
      <div className="flex items-start justify-between">
        <div className={`p-3 rounded-xl border ${theme.icon}
                        transition-transform duration-200 ease-spring
                        group-hover:scale-110`}>
          <Icon size={20} />
        </div>
        {trend != null && (
          <span className={`text-sm font-bold px-2 py-1 rounded-full leading-none border ${
            trend > 0
              ? 'bg-accent-10 text-accent border-accent-20'
              : trend < 0
              ? 'bg-error/10 text-error border-error/20'
              : 'bg-bg3 text-text-muted border-border-subtle'
          }`}>
            {trend > 0 ? '+' : ''}{trend}
          </span>
        )}
      </div>

      <div className="mt-4">
        <p className="text-sm font-normal text-text-muted">{label}</p>
        <p className="text-3xl font-bold tabular-nums tracking-tight mt-1 text-text">
          {value ?? (
            <span className="skeleton inline-block w-16 h-8 rounded-lg align-middle" />
          )}
        </p>
        {sub && (
          <p className="text-sm font-normal text-text-faint mt-1">{sub}</p>
        )}
      </div>

      {/* Accent underline */}
      <div className={`mt-4 h-px w-full rounded-full bg-gradient-to-r ${theme.bar}
                      opacity-30 group-hover:opacity-70 transition-opacity duration-300`} />
    </div>
  )
}
