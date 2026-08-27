import { useEffect, useState, useCallback } from 'react'
import { Link } from 'react-router-dom'
import api from '../../lib/axios'
import StatCard from '../../components/StatCard'
import PageHeader from '../../components/PageHeader'
import EmptyState from '../../components/EmptyState'
import { SkeletonStatCards, SkeletonCard } from '../../components/SkeletonCard'
import toast from 'react-hot-toast'
import {
  RiTeamLine, RiCalendarLine, RiCheckLine, RiCloseLine,
  RiAddLine, RiTimeLine, RiArrowRightLine, RiUserLine,
  RiCalendarCheckLine,
} from 'react-icons/ri'

function ActivityItem({ icon: Icon, text, time, accent }) {
  const cls =
    accent === 'accent'  ? 'bg-accent-10 text-accent' :
    accent === 'primary' ? 'bg-primary-10 text-primary' :
    accent === 'error'   ? 'bg-error/10 text-error' : 'bg-bg3 text-text-muted'
  return (
    <div className="flex items-start gap-3 py-3 border-b border-border-subtle last:border-0">
      <div className={`w-8 h-8 rounded-lg flex items-center justify-center shrink-0 ${cls}`}>
        <Icon size={14} />
      </div>
      <div>
        <p className="text-sm font-normal text-text leading-snug">{text}</p>
        <p className="text-sm font-normal text-text-faint mt-0.5">{time}</p>
      </div>
    </div>
  )
}

/**
 * Given a list of leave requests with only employeeId, fetch the corresponding
 * employee records from the employee-service and return a map { id → fullName }.
 * Silently falls back to "Employee #<id>" when the lookup fails.
 */
async function resolveEmployeeNames(pendingList) {
  const ids = [...new Set(pendingList.map(r => r.employeeId))]
  if (!ids.length) return {}
  try {
    // Fetch all employees in one page (up to 200) and build a lookup map
    const { data } = await api.get('/employees', {
      params: { size: 200, page: 0 },
    })
    const nameMap = {}
    ;(data.content ?? []).forEach(e => {
      nameMap[e.id] = e.fullName ?? `${e.firstName ?? ''} ${e.lastName ?? ''}`.trim()
    })
    return nameMap
  } catch {
    return {}
  }
}

export default function HrDashboard() {
  const [pending,   setPending]   = useState([])
  const [nameMap,   setNameMap]   = useState({})   // employeeId → fullName
  const [empCount,  setEmpCount]  = useState(null)
  const [lateCount, setLateCount] = useState(null)
  const [recentEmp, setRecentEmp] = useState([])
  const [loading,   setLoading]   = useState(true)
  const todayStr = new Date().toISOString().slice(0, 10)

  const load = useCallback(async () => {
    setLoading(true)
    try {
      const [lr, er, late, rec] = await Promise.allSettled([
        api.get('/leave/requests/pending'),
        api.get('/employees?size=1'),
        api.get('/attendance/late-arrivals', { params: { from: todayStr, to: todayStr } }),
        api.get('/employees?size=5&sort=id,desc'),
      ])

      const pendingList = lr.status === 'fulfilled' ? (lr.value.data ?? []) : []
      setPending(pendingList)
      if (er.status   === 'fulfilled') setEmpCount(er.value.data.totalElements ?? 0)
      if (late.status === 'fulfilled') setLateCount(late.value.data.length ?? 0)
      if (rec.status  === 'fulfilled') setRecentEmp(rec.value.data.content ?? [])

      // Resolve employee names for the pending table (non-blocking second pass)
      if (pendingList.length > 0) {
        resolveEmployeeNames(pendingList).then(setNameMap)
      }
    } finally {
      setLoading(false)
    }
  }, [todayStr])

  useEffect(() => { load() }, [load])

  const empName = (id) => nameMap[id] ?? `Employee #${id}`
  const empInitials = (id) => {
    const n = nameMap[id]
    if (!n) return String(id).slice(-2)
    const parts = n.trim().split(' ')
    return (parts[0]?.[0] ?? '') + (parts[1]?.[0] ?? '')
  }

  const approve = async (id) => {
    try {
      await api.patch(`/leave/requests/${id}/approve`)
      setPending(p => p.filter(r => r.id !== id))
      toast.success('Leave approved')
    } catch (err) { toast.error(err.response?.data?.message || 'Failed to approve') }
  }

  const reject = async (id) => {
    try {
      await api.patch(`/leave/requests/${id}/reject`)
      setPending(p => p.filter(r => r.id !== id))
      toast.success('Leave rejected')
    } catch (err) { toast.error(err.response?.data?.message || 'Failed to reject') }
  }

  if (loading) return (
    <div className="space-y-6">
      <SkeletonStatCards count={3} />
      <SkeletonCard rows={6} />
    </div>
  )

  return (
    <div className="space-y-6 animate-fade-up">
      <PageHeader
        title="HR Dashboard"
        subtitle="People operations overview"
        action={
          <Link to="/employees/new" className="btn-primary">
            <RiAddLine size={16} /> Add Employee
          </Link>
        }
      />

      {/* Stats */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <StatCard
          label="Pending Approvals" value={pending.length} icon={RiCalendarLine} color="primary"
          sub={pending.length === 0 ? 'All clear — nothing to review' : 'Needs your review'}
        />
        <StatCard
          label="Total Employees" value={empCount} icon={RiTeamLine} color="primary"
          sub={empCount === 0 ? 'No employees yet — add one' : undefined}
        />
        <StatCard
          label="Late Today" value={lateCount ?? '—'} icon={RiTimeLine}
          color={lateCount > 0 ? 'error' : 'accent'}
          sub={lateCount === 0 ? 'Everyone on time today!' : `${lateCount} late arrival${lateCount !== 1 ? 's' : ''}`}
        />
      </div>

      {/* Pending table + Activity feed */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-5">

        {/* Pending leaves — spans 2 cols */}
        <div className="card lg:col-span-2">
          <div className="flex items-center justify-between mb-5">
            <div>
              <h4 className="text-xl font-bold text-text flex items-center gap-2">
                Pending Requests
                {pending.length > 0 && (
                  <span className="badge-warning">{pending.length}</span>
                )}
              </h4>
              <p className="text-sm font-normal text-text-muted mt-0.5">
                Approve or reject employee leave
              </p>
            </div>
            <Link to="/leave" className="text-sm font-bold text-primary hover:brightness-125 flex items-center gap-1">
              All <RiArrowRightLine size={12} />
            </Link>
          </div>

          {pending.length === 0 ? (
            <EmptyState
              title="All caught up!"
              desc="No pending leave requests. When employees submit requests they'll appear here."
              icon={RiCalendarLine}
            />
          ) : (
            <div className="table-container">
              <table className="table">
                <thead>
                  <tr>
                    <th>Employee</th>
                    <th>Type</th>
                    <th>Dates</th>
                    <th>Days</th>
                    <th className="hidden md:table-cell">Reason</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {pending.map(r => (
                    <tr key={r.id}>
                      <td>
                        <div className="flex items-center gap-2.5">
                          <div className="w-8 h-8 rounded-lg bg-primary-10 border border-primary-20
                                          text-primary text-sm font-bold flex items-center
                                          justify-center shrink-0 uppercase">
                            {empInitials(r.employeeId)}
                          </div>
                          <div className="min-w-0">
                            <p className="font-bold text-sm text-text truncate">
                              {empName(r.employeeId)}
                            </p>
                            <p className="text-sm text-text-faint">#{r.employeeId}</p>
                          </div>
                        </div>
                      </td>
                      <td>
                        <span className="badge-blue">{r.leaveType?.name ?? '—'}</span>
                      </td>
                      <td className="font-mono text-sm whitespace-nowrap">
                        {r.startDate} → {r.endDate}
                      </td>
                      <td>
                        <span className="font-bold">{r.totalDays}d</span>
                      </td>
                      <td className="hidden md:table-cell text-sm text-text-faint max-w-[140px] truncate">
                        {r.reason || <span className="italic text-text-faint">—</span>}
                      </td>
                      <td>
                        <div className="flex gap-1.5">
                          <button
                            onClick={() => approve(r.id)}
                            className="inline-flex items-center gap-1 px-2.5 py-1.5 rounded-lg
                                       bg-accent-10 text-accent border border-accent-20 text-sm font-bold
                                       hover:bg-accent-20 active:scale-95 transition-all"
                          >
                            <RiCheckLine size={12} /> Approve
                          </button>
                          <button
                            onClick={() => reject(r.id)}
                            className="inline-flex items-center gap-1 px-2.5 py-1.5 rounded-lg
                                       bg-error/10 text-error border border-error/20 text-sm font-bold
                                       hover:bg-error/20 active:scale-95 transition-all"
                          >
                            <RiCloseLine size={12} /> Reject
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        {/* Activity feed */}
        <div className="card">
          <h4 className="text-xl font-bold text-text mb-4">Activity</h4>
          {recentEmp.length === 0 && pending.length === 0 && !lateCount ? (
            <EmptyState
              title="No recent activity"
              desc="Activity appears as employees are added, check in, and submit requests."
              icon={RiTimeLine}
            />
          ) : (
            <div>
              {lateCount > 0 && (
                <ActivityItem
                  icon={RiTimeLine}
                  text={`${lateCount} employee${lateCount !== 1 ? 's' : ''} arrived late today`}
                  time="Today"
                  accent="error"
                />
              )}
              {pending.slice(0, 2).map(r => (
                <ActivityItem
                  key={r.id}
                  icon={RiCalendarLine}
                  text={`${empName(r.employeeId)} requested ${r.leaveType?.name ?? 'leave'}`}
                  time={r.startDate}
                  accent="primary"
                />
              ))}
              {recentEmp.slice(0, 3).map(emp => (
                <ActivityItem
                  key={emp.id}
                  icon={RiUserLine}
                  text={`${emp.fullName} joined the team`}
                  time={emp.joiningDate ?? 'Recently'}
                  accent="accent"
                />
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Quick actions */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
        {[
          { to: '/employees',  label: 'Employees',      sub: 'Manage team',        icon: RiTeamLine },
          { to: '/attendance', label: 'Attendance',     sub: 'View records',       icon: RiCalendarCheckLine },
          { to: '/leave',      label: 'Leave Overview', sub: 'All requests',       icon: RiCalendarLine },
          { to: '/reports',    label: 'Analytics',      sub: 'Reports & insights', icon: RiUserLine },
        ].map(item => (
          <Link
            key={item.to}
            to={item.to}
            className="flex items-center gap-3 p-4 rounded-2xl bg-bg2 border border-border-subtle
                       hover:bg-primary-10 hover:border-primary-20
                       active:scale-[0.98] transition-all duration-150 text-text-muted hover:text-primary"
          >
            <item.icon size={19} className="shrink-0" />
            <div>
              <p className="text-sm font-bold text-text">{item.label}</p>
              <p className="text-sm font-normal opacity-60">{item.sub}</p>
            </div>
          </Link>
        ))}
      </div>
    </div>
  )
}
