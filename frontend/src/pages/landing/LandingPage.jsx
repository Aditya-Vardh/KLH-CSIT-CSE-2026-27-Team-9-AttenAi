import { Link } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import {
  RiCalendarCheckLine, RiTeamLine, RiRobot2Line, RiBarChartLine,
  RiShieldLine, RiBellLine, RiArrowRightLine, RiCheckLine,
  RiTimeLine, RiLoginCircleLine, RiBuilding2Line,
  RiDatabase2Line, RiCloudLine, RiGlobalLine,
} from 'react-icons/ri'

/* ─ data ──────────────────────────────────────────────────────────────── */
const FEATURES = [
  { icon: RiCalendarCheckLine, title: 'Real-time Attendance',  desc: 'One-tap check-in and check-out with automatic late-arrival detection and working-hours calculation.' },
  { icon: RiTeamLine,          title: 'Employee Management',   desc: 'Full-lifecycle records — create, update, search and organise your workforce by department and role.' },
  { icon: RiCalendarCheckLine, title: 'Leave Management',      desc: 'Employees apply in seconds. HR approves with one click. Balances update automatically.' },
  { icon: RiRobot2Line,        title: 'AI Assistant',          desc: 'Ask your HR chatbot anything — leave balance, attendance history, policies — in plain language.' },
  { icon: RiBarChartLine,      title: 'Analytics & Reports',   desc: 'Daily breakdowns, per-employee ratings, monthly trends and exportable PDF reports.' },
  { icon: RiShieldLine,        title: 'Role-Based Access',     desc: 'Granular JWT-secured control for Admin, HR, and Employee roles via the API Gateway.' },
]

const STEPS = [
  { icon: RiLoginCircleLine,   title: 'Sign In',          desc: 'Log in — the system detects your role and routes you to the right dashboard instantly.' },
  { icon: RiTimeLine,          title: 'Track Attendance', desc: 'Check in and out with one click. Hours, late-arrivals and summaries computed automatically.' },
  { icon: RiBarChartLine,      title: 'Manage & Report',  desc: 'HR approves leave, views analytics and exports PDF reports. AI handles routine queries 24/7.' },
]

const STATS = [
  { val: '7',    lbl: 'Microservices', icon: RiCloudLine },
  { val: '3',    lbl: 'Role Levels',   icon: RiShieldLine },
  { val: 'SOA',  lbl: 'Architecture',  icon: RiDatabase2Line },
  { val: '< 1s', lbl: 'Response Time', icon: RiTimeLine },
]

const TECH = [
  { icon: RiBuilding2Line, label: 'Spring Boot 3', sub: 'Microservices' },
  { icon: RiGlobalLine,    label: 'React 18',      sub: 'Frontend' },
  { icon: RiDatabase2Line, label: 'MySQL',         sub: 'Per-service DB' },
  { icon: RiShieldLine,    label: 'JWT + Gateway', sub: 'Secure by design' },
  { icon: RiCloudLine,     label: 'Eureka',        sub: 'Service discovery' },
  { icon: RiRobot2Line,    label: 'AI Agent',      sub: 'NLP + Chat' },
  { icon: RiBellLine,      label: 'Notifications', sub: 'Email + in-app' },
  { icon: RiBarChartLine,  label: 'Analytics',     sub: 'Reports & charts' },
]

function Blob({ className }) {
  return <div aria-hidden className={`absolute rounded-full blur-3xl pointer-events-none ${className}`} />
}

/* ─ component ────────────────────────────────────────────────────────── */
export default function LandingPage() {
  const { user } = useAuth()

  return (
    <div className="min-h-screen bg-background text-text overflow-x-hidden">

      {/* ══ NAV ══════════════════════════════════════════════════════════ */}
      <header className="sticky top-0 z-50 bg-bg1/90 backdrop-blur-lg border-b border-border-subtle">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex items-center justify-between h-16">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-xl bg-primary flex items-center justify-center shadow-glow-sm">
              <RiCalendarCheckLine size={15} className="text-text-inverse" />
            </div>
            <span className="text-xl font-bold text-primary">AttendAI</span>
          </div>

          <nav className="hidden md:flex items-center gap-6 text-sm font-normal text-text-muted">
            <a href="#features"  className="hover:text-text transition-colors">Features</a>
            <a href="#how"       className="hover:text-text transition-colors">How it works</a>
            <a href="#tech"      className="hover:text-text transition-colors">Tech</a>
          </nav>

          <div className="flex items-center gap-3">
            {user ? (
              <Link to="/dashboard" className="btn-primary text-sm px-4 py-2">
                Dashboard <RiArrowRightLine size={14} />
              </Link>
            ) : (
              <>
                <Link to="/login" className="text-sm font-normal text-text-muted hover:text-text transition-colors px-2">
                  Sign In
                </Link>
                <Link to="/register" className="btn-primary text-sm px-4 py-2">
                  Get Started
                </Link>
              </>
            )}
          </div>
        </div>
      </header>

      {/* ══ HERO ═════════════════════════════════════════════════════════ */}
      <section className="relative min-h-[88vh] flex items-center justify-center overflow-hidden">
        <div className="absolute inset-0 hero-bg" />
        <Blob className="w-[600px] h-[400px] -top-40 -left-40 bg-primary/8" />
        <Blob className="w-[500px] h-[400px] top-1/2 -right-40  bg-secondary/8" />
        <Blob className="w-[400px] h-[400px] bottom-0  left-1/3  bg-accent/5" />

        {/* gold grid */}
        <div className="absolute inset-0 opacity-[0.03]" style={{
          backgroundImage:
            'linear-gradient(rgb(239 213 129 / 1) 1px, transparent 1px), ' +
            'linear-gradient(90deg, rgb(239 213 129 / 1) 1px, transparent 1px)',
          backgroundSize: '56px 56px',
        }} />

        <div className="relative z-10 max-w-5xl mx-auto px-4 sm:px-6 text-center">
          {/* pill */}
          <div className="inline-flex items-center gap-2 px-4 py-1.5 rounded-full
                          border border-primary-20 bg-primary-10
                          text-sm font-normal text-text-muted mb-8 animate-fade-in">
            <span className="w-1.5 h-1.5 rounded-full bg-accent animate-pulse-soft" />
            Smart Attendance &amp; HR Management
          </div>

          {/* headline */}
          <h1 className="hero-text-gradient text-5xl sm:text-5xl lg:text-4xl font-bold
                         tracking-tight mb-6 text-balance
                         animate-fade-up animation-delay-100 animation-fill-both">
            Attendance that<br />actually works
          </h1>

          <p className="text-base font-normal text-text-muted max-w-2xl mx-auto
                        leading-relaxed mb-10 text-balance
                        animate-fade-up animation-delay-200 animation-fill-both">
            Real-time check-in, AI-powered leave management, role-based dashboards
            and rich analytics — all in one platform.
          </p>

          <div className="flex flex-col sm:flex-row items-center justify-center gap-4
                          animate-fade-up animation-delay-300 animation-fill-both">
            <Link to={user ? '/dashboard' : '/register'}
              className="btn-primary text-base px-8 py-3.5 shadow-glow">
              {user ? 'Go to Dashboard' : 'Start for free'}
              <RiArrowRightLine size={18} />
            </Link>
            <Link to="/login"
              className="inline-flex items-center gap-2 px-8 py-3.5 rounded-xl
                         border border-border-default text-text-muted text-base font-normal
                         hover:bg-primary-10 hover:text-text hover:border-border-strong
                         transition-all duration-150">
              Sign In
            </Link>
          </div>

          {/* proof strip */}
          <div className="flex flex-wrap items-center justify-center gap-6 mt-14
                          text-sm font-normal text-text-faint
                          animate-fade-up animation-delay-400 animation-fill-both">
            {['JWT Security','Spring Boot','React 18','AI-Powered','SOA Architecture'].map(tag => (
              <span key={tag} className="flex items-center gap-1.5">
                <RiCheckLine size={12} className="text-accent" />
                {tag}
              </span>
            ))}
          </div>
        </div>

        {/* fade edge */}
        <div className="absolute bottom-0 inset-x-0 h-32 bg-gradient-to-t from-background to-transparent" />
      </section>

      {/* ══ STATS STRIP ══════════════════════════════════════════════════ */}
      <section className="border-y border-border-subtle bg-bg1">
        <div className="max-w-5xl mx-auto px-4 sm:px-6 py-10 grid grid-cols-2 sm:grid-cols-4 gap-8">
          {STATS.map(s => (
            <div key={s.lbl} className="text-center">
              <s.icon size={17} className="text-primary mx-auto mb-2" />
              <p className="text-3xl font-bold text-primary">{s.val}</p>
              <p className="text-sm font-normal text-text-muted mt-0.5">{s.lbl}</p>
            </div>
          ))}
        </div>
      </section>

      {/* ══ FEATURES ═════════════════════════════════════════════════════ */}
      <section id="features" className="py-24 px-4 sm:px-6 relative">
        <Blob className="w-[500px] h-[400px] top-0 -right-48 bg-primary/5" />
        <div className="max-w-6xl mx-auto">
          <div className="text-center mb-16">
            <p className="text-sm font-bold text-primary uppercase tracking-widest mb-3">Platform Features</p>
            <h2 className="text-3xl font-bold text-text text-balance">
              Everything HR needs, nothing it doesn't
            </h2>
            <p className="text-base font-normal text-text-muted mt-4 max-w-xl mx-auto">
              Built on a microservices architecture. Fast, reliable, and extensible.
            </p>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
            {FEATURES.map((f, i) => (
              <div key={f.title}
                className="feature-card rounded-2xl p-6 hover:bg-primary-10 transition-all duration-200 group
                           animate-fade-up animation-fill-both"
                style={{ animationDelay: `${i * 70}ms` }}>
                <div className="w-11 h-11 rounded-xl bg-primary-10 border border-primary-20
                                flex items-center justify-center mb-4
                                group-hover:scale-110 transition-transform duration-200">
                  <f.icon size={21} className="text-primary" />
                </div>
                <h3 className="text-base font-bold text-text mb-2">{f.title}</h3>
                <p className="text-sm font-normal text-text-muted leading-relaxed">{f.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* ══ HOW IT WORKS ═════════════════════════════════════════════════ */}
      <section id="how" className="py-24 px-4 sm:px-6 bg-bg1 relative">
        <Blob className="w-[500px] h-[300px] bottom-0 -left-32 bg-secondary/6" />
        <div className="max-w-5xl mx-auto">
          <div className="text-center mb-16">
            <p className="text-sm font-bold text-accent uppercase tracking-widest mb-3">How it works</p>
            <h2 className="text-3xl font-bold text-text">Up and running in minutes</h2>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8 relative">
            {/* connecting line */}
            <div aria-hidden className="hidden md:block absolute top-14
                                        left-[calc(33.3%+2rem)] right-[calc(33.3%+2rem)]
                                        h-px bg-gradient-to-r from-primary-30 via-accent/30 to-primary-30" />

            {STEPS.map((step, i) => (
              <div key={step.title} className="text-center animate-fade-up animation-fill-both"
                style={{ animationDelay: `${i * 100}ms` }}>
                <div className="relative inline-flex items-center justify-center
                                w-28 h-28 rounded-full
                                bg-primary-10 border border-primary-20 mb-6 mx-auto">
                  <step.icon size={30} className="text-primary" />
                  <span className="absolute -top-2 -right-2 w-7 h-7 rounded-full
                                   bg-primary text-text-inverse text-sm font-bold
                                   flex items-center justify-center shadow-glow-sm">
                    {i + 1}
                  </span>
                </div>
                <h3 className="text-xl font-bold text-text mb-2">{step.title}</h3>
                <p className="text-sm font-normal text-text-muted leading-relaxed max-w-xs mx-auto">
                  {step.desc}
                </p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* ══ TECH STACK ═══════════════════════════════════════════════════ */}
      <section id="tech" className="py-24 px-4 sm:px-6">
        <div className="max-w-5xl mx-auto">
          <div className="text-center mb-14">
            <p className="text-sm font-bold text-secondary uppercase tracking-widest mb-3">Built on</p>
            <h2 className="text-3xl font-bold text-text">Production-grade architecture</h2>
            <p className="text-base font-normal text-text-muted mt-4 max-w-xl mx-auto">
              7 Spring Boot microservices behind an API Gateway, Eureka service discovery,
              JWT authentication, and a React frontend.
            </p>
          </div>

          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
            {TECH.map(t => (
              <div key={t.label}
                className="feature-card rounded-2xl p-5 hover:bg-primary-10 transition-all duration-150">
                <t.icon size={20} className="text-primary mb-3" />
                <p className="text-sm font-bold text-text">{t.label}</p>
                <p className="text-sm font-normal text-text-faint mt-0.5">{t.sub}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* ══ CTA BANNER ═══════════════════════════════════════════════════ */}
      <section className="py-24 px-4 sm:px-6 relative overflow-hidden bg-bg1">
        <Blob className="w-[600px] h-[400px] -top-20 left-1/2 -translate-x-1/2 bg-primary/8" />
        <div className="max-w-3xl mx-auto text-center relative z-10">
          <h2 className="text-4xl font-bold text-balance mb-6">
            <span className="hero-text-gradient">Ready to modernise</span>
            <br />your HR operations?
          </h2>
          <p className="text-base font-normal text-text-muted mb-10 text-balance">
            Get started with AttendAI today — no setup fees, no complexity.
          </p>
          <div className="flex flex-col sm:flex-row items-center justify-center gap-4">
            <Link to={user ? '/dashboard' : '/register'}
              className="btn-primary text-base px-10 py-4 shadow-glow">
              {user ? 'Open Dashboard' : 'Create Account'}
              <RiArrowRightLine size={18} />
            </Link>
            {!user && (
              <Link to="/login"
                className="text-base font-normal text-text-muted hover:text-text transition-colors">
                Already have an account? Sign in →
              </Link>
            )}
          </div>
        </div>
      </section>

      {/* ══ FOOTER ═══════════════════════════════════════════════════════ */}
      <footer className="border-t border-border-subtle bg-background">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 py-10
                        flex flex-col sm:flex-row items-center justify-between gap-4">
          <div className="flex items-center gap-2">
            <div className="w-7 h-7 rounded-lg bg-primary flex items-center justify-center">
              <RiCalendarCheckLine size={13} className="text-text-inverse" />
            </div>
            <span className="text-base font-bold text-primary">AttendAI</span>
          </div>
          <p className="text-sm font-normal text-text-faint text-center">
            Smart Attendance &amp; HR Management · Spring Boot + React · SOA Architecture
          </p>
          <div className="flex items-center gap-5 text-sm font-normal text-text-faint">
            <Link to="/login"    className="hover:text-text-muted transition-colors">Sign In</Link>
            <Link to="/register" className="hover:text-text-muted transition-colors">Register</Link>
          </div>
        </div>
      </footer>
    </div>
  )
}
