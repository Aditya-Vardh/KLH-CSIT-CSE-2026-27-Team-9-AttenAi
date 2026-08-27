import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import toast from 'react-hot-toast'
import {
  RiDashboardLine, RiTeamLine, RiCalendarCheckLine, RiCalendarLine,
  RiBarChartLine, RiBellLine, RiRobot2Line, RiSettings3Line,
  RiCloseLine, RiShieldUserLine, RiUserHeartLine, RiLogoutBoxLine,
  RiGroupLine, RiFileListLine,
} from 'react-icons/ri'

const nav = (to, icon, label) => ({ to, icon, label })

const ADMIN_NAV = [
  nav('/admin',      RiShieldUserLine,  'Admin Dashboard'),
  nav('/hr',         RiUserHeartLine,   'HR Dashboard'),
  nav('/employees',  RiTeamLine,        'Employees'),
  nav('/departments',RiGroupLine,       'Departments'),
  nav('/reports',    RiBarChartLine,    'Reports'),
  nav('/audit',      RiFileListLine,    'Audit Logs'),
]
const HR_NAV = [
  nav('/hr',         RiUserHeartLine,   'HR Dashboard'),
  nav('/employees',  RiTeamLine,        'Employees'),
  nav('/reports',    RiBarChartLine,    'Reports'),
]
const MANAGER_NAV = [
  nav('/my',         RiDashboardLine,   'My Dashboard'),
  nav('/employees',  RiTeamLine,        'Team'),
  nav('/leave',      RiCalendarLine,    'Leave Approvals'),
]
// Common nav items shown to every role — role-nav items are shown above these
// and are de-duplicated at render time to avoid showing the same route twice.
const COMMON_NAV = [
  nav('/dashboard',     RiDashboardLine,     'Dashboard'),
  nav('/attendance',    RiCalendarCheckLine, 'Attendance'),
  nav('/leave',         RiCalendarLine,      'Leave'),
  nav('/notifications', RiBellLine,          'Notifications'),
  nav('/ai',            RiRobot2Line,        'AI Assistant'),
  nav('/settings',      RiSettings3Line,     'Settings'),
]

function Group({ label, items, onClose }) {
  return (
    <div className="mb-1">
      {label && <p className="section-label">{label}</p>}
      {items.map(({ to, icon: Icon, label: lbl }) => (
        <NavLink key={to} to={to} onClick={onClose}
          className={({ isActive }) =>
            (isActive ? 'nav-item-active' : 'nav-item') + ' mb-0.5 flex'
          }>
          <Icon size={16} className="shrink-0" />
          <span className="truncate">{lbl}</span>
        </NavLink>
      ))}
    </div>
  )
}

export default function Sidebar({ open, onClose }) {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const role = user?.role ?? 'EMPLOYEE'

  const roleNav   = role === 'ADMIN' ? ADMIN_NAV
                  : role === 'HR'    ? HR_NAV
                  : role === 'MANAGER' ? MANAGER_NAV
                  : []
  const roleLabel = role === 'ADMIN'   ? 'Administration'
                  : role === 'HR'      ? 'Management'
                  : role === 'MANAGER' ? 'Management'
                  : ''
  const initials  = [user?.firstName?.[0], user?.lastName?.[0]].filter(Boolean).join('').toUpperCase() || '?'

  const handleLogout = () => { logout(); toast.success('Signed out'); navigate('/login') }

  return (
    <>
      {/* Mobile overlay */}
      {open && (
        <div
          className="fixed inset-0 bg-background/80 backdrop-blur-sm z-20 lg:hidden"
          onClick={onClose}
        />
      )}

      <aside className={`
        fixed inset-y-0 left-0 z-30 w-64 flex flex-col
        bg-bg1 border-r border-border-subtle
        transform transition-transform duration-200 ease-snappy
        ${open ? 'translate-x-0' : '-translate-x-full'}
        lg:translate-x-0 lg:static lg:z-auto
      `}>

        {/* Brand */}
        <div className="flex items-center justify-between px-5 pt-5 pb-4 shrink-0">
          <div className="flex items-center gap-2.5">
            {/* Gold circle logo mark */}
            <div className="w-8 h-8 rounded-xl bg-primary flex items-center justify-center shadow-glow-sm">
              <RiCalendarCheckLine size={15} className="text-text-inverse" />
            </div>
            <span className="text-xl font-bold text-primary tracking-tight">AttendAI</span>
          </div>
          <button onClick={onClose} className="lg:hidden btn-icon">
            <RiCloseLine size={20} />
          </button>
        </div>

        {/* User card */}
        <div className="mx-3 mb-3 p-3 rounded-xl bg-bg2 border border-border-subtle">
          <div className="flex items-center gap-3">
            {/* Avatar — gold monogram */}
            <div className="w-10 h-10 rounded-xl bg-primary flex items-center justify-center
                            text-text-inverse font-bold text-sm shadow-sm shrink-0">
              {initials}
            </div>
            <div className="min-w-0">
              <p className="text-sm font-bold text-text truncate">
                {user?.firstName} {user?.lastName}
              </p>
              <p className="text-sm text-text-faint truncate">{user?.email}</p>
            </div>
          </div>
          <div className="mt-2.5">
            <span className="badge-warning text-sm">{role}</span>
          </div>
        </div>

        <div className="divider mx-3 mb-1" />

        {/* Nav */}
        <nav className="flex-1 overflow-y-auto no-scrollbar px-3 py-2">
          {roleNav.length > 0 && (
            <Group label={roleLabel} items={roleNav} onClose={onClose} />
          )}
          {/* Filter out items already shown in the role-specific section */}
          <Group
            label="Workspace"
            items={COMMON_NAV.filter(
              item => !roleNav.some(r => r.to === item.to)
            )}
            onClose={onClose}
          />
        </nav>

        {/* Logout */}
        <div className="px-3 pb-4 pt-1 shrink-0">
          <div className="divider mb-3" />
          <button
            onClick={handleLogout}
            className="w-full flex items-center gap-3 px-3 py-2.5 rounded-xl
                       text-sm font-normal text-error
                       hover:bg-error/10 hover:text-error
                       active:bg-error/20
                       transition-all duration-100"
          >
            <RiLogoutBoxLine size={16} />
            Sign Out
          </button>
        </div>
      </aside>
    </>
  )
}
