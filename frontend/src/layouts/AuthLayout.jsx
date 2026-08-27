import { Outlet, Navigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { RiCalendarCheckLine, RiShieldLine, RiTimeLine, RiTeamLine } from 'react-icons/ri'

const BULLETS = [
  { icon: RiTimeLine,         text: 'Real-time check-in with working-hours calculation' },
  { icon: RiTeamLine,         text: 'Role-based dashboards for Admin, HR and Employees' },
  { icon: RiShieldLine,       text: 'JWT-secured microservices architecture' },
  { icon: RiCalendarCheckLine,text: 'AI-powered leave management and analytics' },
]

export default function AuthLayout() {
  const { user } = useAuth()
  if (user) return <Navigate to="/dashboard" replace />

  return (
    <div className="min-h-screen flex bg-background">

      {/* ── Left panel — decorative, desktop only ── */}
      <div className="hidden lg:flex lg:w-5/12 xl:w-1/2 flex-col justify-between
                      p-12 border-r border-border-subtle relative overflow-hidden">
        {/* Subtle warm grid */}
        <div className="absolute inset-0 opacity-[0.03]"
          style={{
            backgroundImage:
              'linear-gradient(rgb(239 213 129 / 0.6) 1px, transparent 1px), ' +
              'linear-gradient(90deg, rgb(239 213 129 / 0.6) 1px, transparent 1px)',
            backgroundSize: '48px 48px',
          }}
        />
        {/* Glow blobs */}
        <div className="absolute -top-32 -left-32 w-80 h-80 rounded-full
                        bg-primary/5 blur-3xl pointer-events-none" />
        <div className="absolute bottom-0 right-0 w-72 h-72 rounded-full
                        bg-secondary/5 blur-3xl pointer-events-none" />

        <div className="relative z-10">
          {/* Logo */}
          <div className="flex items-center gap-3 mb-16">
            <div className="w-10 h-10 rounded-xl bg-primary flex items-center justify-center shadow-glow-sm">
              <RiCalendarCheckLine size={20} className="text-text-inverse" />
            </div>
            <span className="text-2xl font-bold text-primary">AttendAI</span>
          </div>

          {/* Headline */}
          <h1 className="text-4xl font-bold text-text leading-tight mb-4">
            Smart attendance<br />
            <span className="hero-text-gradient">for modern teams</span>
          </h1>
          <p className="text-base font-normal text-text-muted mb-12 max-w-sm">
            One platform for check-in, leave management, HR analytics and AI assistance.
          </p>

          {/* Bullets */}
          <div className="space-y-4">
            {BULLETS.map(b => (
              <div key={b.text} className="flex items-start gap-3">
                <div className="w-8 h-8 rounded-lg bg-primary-10 border border-primary-20
                                flex items-center justify-center shrink-0 mt-0.5">
                  <b.icon size={15} className="text-primary" />
                </div>
                <p className="text-sm font-normal text-text-muted leading-relaxed">{b.text}</p>
              </div>
            ))}
          </div>
        </div>

        {/* Bottom stats */}
        <div className="relative z-10 flex gap-8 mt-12">
          {[
            { v: '7',    l: 'Microservices' },
            { v: '< 1s', l: 'Response' },
            { v: 'SOA',  l: 'Architecture' },
          ].map(s => (
            <div key={s.l}>
              <p className="text-2xl font-bold text-primary">{s.v}</p>
              <p className="text-sm text-text-faint mt-0.5">{s.l}</p>
            </div>
          ))}
        </div>
      </div>

      {/* ── Right panel — form ── */}
      <div className="flex-1 flex items-center justify-center p-6">
        <div className="w-full max-w-md">
          {/* Mobile logo */}
          <div className="flex items-center gap-2.5 mb-8 lg:hidden">
            <div className="w-9 h-9 rounded-xl bg-primary flex items-center justify-center shadow-glow-sm">
              <RiCalendarCheckLine size={17} className="text-text-inverse" />
            </div>
            <span className="text-xl font-bold text-primary">AttendAI</span>
          </div>

          <div className="card shadow-card-lg">
            <Outlet />
          </div>
        </div>
      </div>
    </div>
  )
}
