import { useAuth } from '../context/AuthContext'
import { useNavigate, useLocation } from 'react-router-dom'
import { RiMenuLine, RiBellLine } from 'react-icons/ri'
import { useEffect, useState } from 'react'
import api from '../lib/axios'

const ROUTE_LABELS = {
  '/dashboard':         'Dashboard',
  '/admin':             'Admin Dashboard',
  '/hr':                'HR Dashboard',
  '/my':                'My Dashboard',
  '/employees':         'Employees',
  '/employees/new':     'New Employee',
  '/attendance':        'Attendance',
  '/attendance/checkin':'Check In / Out',
  '/leave':             'Leave',
  '/leave/new':         'Apply for Leave',
  '/reports':           'Reports & Analytics',
  '/notifications':     'Notifications',
  '/ai':                'AI Assistant',
  '/settings':          'Settings',
}

export default function Topbar({ onMenuClick }) {
  const { user }   = useAuth()
  const navigate   = useNavigate()
  const { pathname } = useLocation()
  const [unread, setUnread] = useState(0)

  const title    = Object.entries(ROUTE_LABELS)
    .find(([k]) => pathname === k || pathname.startsWith(k + '/'))?.[1] ?? 'AttendAI'
  const initials = [user?.firstName?.[0], user?.lastName?.[0]]
    .filter(Boolean).join('').toUpperCase() || '?'

  useEffect(() => {
    const empId = user?.employeeId
    if (!empId) return
    api.get(`/notifications/employee/${empId}/unread-count`)
      .then(r => setUnread(r.data.unreadCount ?? 0))
      .catch(() => {})
  }, [user?.employeeId, pathname])

  return (
    <header className="h-16 bg-bg1/90 backdrop-blur-md border-b border-border-subtle
                       flex items-center justify-between px-4 lg:px-6 shrink-0 sticky top-0 z-10">
      {/* Left */}
      <div className="flex items-center gap-3">
        <button onClick={onMenuClick} className="lg:hidden btn-icon" aria-label="Menu">
          <RiMenuLine size={20} />
        </button>
        <h2 className="text-base font-bold text-text hidden sm:block">{title}</h2>
      </div>

      {/* Right */}
      <div className="flex items-center gap-2">
        {/* Bell */}
        <button
          onClick={() => navigate('/notifications')}
          className="btn-icon relative"
          aria-label="Notifications"
        >
          <RiBellLine size={20} />
          {unread > 0 && (
            <span className="absolute -top-0.5 -right-0.5 min-w-[17px] h-[17px]
                             rounded-full bg-accent text-text-inverse
                             text-sm font-bold flex items-center justify-center
                             px-1 leading-none shadow-glow-accent">
              {unread > 9 ? '9+' : unread}
            </span>
          )}
        </button>

        {/* Profile */}
        <button
          onClick={() => navigate('/settings')}
          className="flex items-center gap-2.5 px-3 py-1.5 rounded-xl
                     hover:bg-primary-10 transition-all duration-100"
        >
          <div className="w-8 h-8 rounded-lg bg-primary flex items-center justify-center
                          text-text-inverse text-sm font-bold shadow-glow-sm">
            {initials}
          </div>
          <div className="hidden sm:block text-left">
            <p className="text-sm font-bold text-text leading-none">
              {user?.firstName} {user?.lastName}
            </p>
            <p className="text-sm text-text-faint mt-0.5">{user?.role}</p>
          </div>
        </button>
      </div>
    </header>
  )
}
