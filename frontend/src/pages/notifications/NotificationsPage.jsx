import { useEffect, useState } from 'react'
import { useAuth } from '../../context/AuthContext'
import api from '../../lib/axios'
import PageHeader from '../../components/PageHeader'
import EmptyState from '../../components/EmptyState'
import { SkeletonCard } from '../../components/SkeletonCard'
import toast from 'react-hot-toast'
import {
  RiBellLine, RiCheckDoubleLine, RiCheckLine,
  RiMailOpenLine, RiMailLine,
} from 'react-icons/ri'

// Maps NotificationType → badge class (all from index.css design-system)
const TYPE_BADGE = {
  LEAVE_APPROVED:       'badge-success',
  LEAVE_REJECTED:       'badge-error',
  LATE_ARRIVAL:         'badge-warning',
  ATTENDANCE_REMINDER:  'badge-blue',
  LEAVE_SUBMITTED:      'badge-blue',
  GENERAL:              'badge-muted',
}

// Maps NotificationType → icon container classes using design-system surface tokens
const TYPE_ICON_BG = {
  LEAVE_APPROVED:       'bg-accent-10     text-accent',
  LEAVE_REJECTED:       'bg-error/10      text-error',
  LATE_ARRIVAL:         'bg-primary-10    text-primary',
  ATTENDANCE_REMINDER:  'bg-secondary-10  text-accent',
  LEAVE_SUBMITTED:      'bg-primary-10    text-primary',
  GENERAL:              'bg-bg3           text-text-muted',
}

function fmtDate(iso) {
  if (!iso) return ''
  const d   = new Date(iso)
  const now = new Date()
  const diff = now - d
  if (diff < 60_000)    return 'just now'
  if (diff < 3_600_000) return `${Math.floor(diff / 60_000)}m ago`
  if (diff < 86_400_000) return `${Math.floor(diff / 3_600_000)}h ago`
  return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric' })
}

export default function NotificationsPage() {
  const { user }  = useAuth()
  const empId     = user?.employeeId
  const [notifs,  setNotifs]  = useState([])
  const [loading, setLoading] = useState(true)
  const [marking, setMarking] = useState(null)   // id currently being marked

  useEffect(() => {
    if (!empId) { setLoading(false); return }
    api.get(`/notifications/employee/${empId}`, { params: { size: 50 } })
      .then(r => setNotifs(r.data.content ?? []))
      .catch(() => {})
      .finally(() => setLoading(false))
  }, [empId])

  const unreadCount = notifs.filter(n => !n.read).length

  const markOne = async (n) => {
    if (n.read) return
    setMarking(n.id)
    try {
      const res = await api.patch(`/notifications/${n.id}/read`, null, {
        params: { employeeId: empId },
      })
      setNotifs(prev =>
        prev.map(x => x.id === n.id ? { ...x, read: true, readAt: res.data.readAt } : x)
      )
    } catch { /* silent — read-tracking is non-critical */ }
    finally { setMarking(null) }
  }

  const markAll = async () => {
    try {
      await api.patch(`/notifications/employee/${empId}/read-all`)
      setNotifs(prev => prev.map(x => ({ ...x, read: true, readAt: new Date().toISOString() })))
      toast.success('All notifications marked as read')
    } catch { toast.error('Failed to mark all as read') }
  }

  if (loading) return (
    <div className="space-y-4 animate-fade-up">
      <SkeletonCard rows={5} />
    </div>
  )

  return (
    <div className="space-y-5 animate-fade-up">
      <PageHeader
        title="Notifications"
        subtitle={
          unreadCount > 0
            ? `${unreadCount} unread notification${unreadCount > 1 ? 's' : ''}`
            : notifs.length > 0 ? 'All caught up' : 'No notifications yet'
        }
        action={
          unreadCount > 0 && (
            <button onClick={markAll} className="btn-secondary gap-2">
              <RiCheckDoubleLine size={16} />
              Mark all read
            </button>
          )
        }
      />

      <div className="card p-0 overflow-hidden">
        {notifs.length === 0 ? (
          <div className="p-6">
            <EmptyState
              title="No notifications"
              desc="You'll see notifications about leave approvals, attendance, and more here."
              icon={RiBellLine}
            />
          </div>
        ) : (
          <div className="divide-y divide-border-subtle">
            {notifs.map((n, idx) => (
              <div
                key={n.id}
                onClick={() => markOne(n)}
                className={[
                  'relative flex items-start gap-4 px-6 py-4',
                  'transition-colors duration-100 cursor-pointer',
                  !n.read
                    ? 'bg-primary-10 hover:bg-primary-20 border-l-2 border-l-primary'
                    : 'hover:bg-bg2 border-l-2 border-l-transparent',
                  idx === 0             ? 'rounded-t-2xl' : '',
                  idx === notifs.length - 1 ? 'rounded-b-2xl' : '',
                ].join(' ')}
                role="button"
                tabIndex={0}
                onKeyDown={e => (e.key === 'Enter' || e.key === ' ') && markOne(n)}
                aria-label={n.read ? 'Notification (read)' : 'Notification (unread) — click to mark as read'}
              >
                {/* Type icon */}
                <div className={[
                  'w-10 h-10 rounded-xl flex items-center justify-center shrink-0 mt-0.5',
                  TYPE_ICON_BG[n.type] ?? 'bg-bg3 text-text-muted',
                ].join(' ')}>
                  {n.read ? <RiMailOpenLine size={18} /> : <RiMailLine size={18} />}
                </div>

                {/* Content */}
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2 flex-wrap mb-1">
                    <span className={TYPE_BADGE[n.type] ?? 'badge-muted'}>
                      {n.type?.replace(/_/g, ' ')}
                    </span>
                    <span className="text-sm text-text-faint">{fmtDate(n.createdAt)}</span>
                    {!n.read && (
                      <span className="inline-flex items-center gap-1 text-sm font-bold
                                       text-primary bg-primary-10 border border-primary-20
                                       px-2 py-0.5 rounded-full">
                        NEW
                      </span>
                    )}
                  </div>
                  <p className={[
                    'text-sm leading-snug',
                    n.read ? 'text-text-muted font-normal' : 'text-text font-bold',
                  ].join(' ')}>
                    {n.subject}
                  </p>
                  {n.sentAt && (
                    <p className="text-sm text-text-faint mt-1 flex items-center gap-1">
                      <RiCheckLine size={11} className="text-accent" />
                      Delivered {fmtDate(n.sentAt)}
                    </p>
                  )}
                </div>

                {/* Read indicator */}
                <div className="shrink-0 flex items-center">
                  {marking === n.id ? (
                    <div className="w-4 h-4 rounded-full border-2 border-primary border-t-transparent animate-spin" />
                  ) : !n.read ? (
                    <div className="w-2.5 h-2.5 rounded-full bg-primary ring-2 ring-primary-20" title="Unread" />
                  ) : (
                    <RiCheckDoubleLine size={14} className="text-text-faint" />
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {notifs.length > 0 && (
        <p className="text-center text-sm text-text-faint">
          Showing {notifs.length} notification{notifs.length !== 1 ? 's' : ''} · Click any to mark as read
        </p>
      )}
    </div>
  )
}
