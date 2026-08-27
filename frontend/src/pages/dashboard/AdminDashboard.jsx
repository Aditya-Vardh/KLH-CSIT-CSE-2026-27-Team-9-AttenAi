import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../../lib/axios'
import StatCard from '../../components/StatCard'
import PageHeader from '../../components/PageHeader'
import EmptyState from '../../components/EmptyState'
import { SkeletonStatCards, SkeletonCard } from '../../components/SkeletonCard'
import { Bar } from 'react-chartjs-2'
import {
  Chart, CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend,
} from 'chart.js'
import {
  RiTeamLine, RiCalendarLine, RiCalendarCheckLine, RiBuilding2Line,
  RiAddLine, RiArrowRightLine, RiBarChartLine, RiCheckLine, RiCloseLine,
  RiTimeLine, RiUserLine, RiGroupLine,
} from 'react-icons/ri'
import toast from 'react-hot-toast'

Chart.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend)

const MONTHS = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec']

// ── tiny helper to render a single activity event ──────────────────────────
function ActivityItem({ icon: Icon, text, time, accent }) {
  return (
    <div className="flex items-start gap-3 py-3 border-b border-border-subtle last:border-0">
      <div className={`w-8 h-8 rounded-lg flex items-center justify-center shrink-0 ${
        accent === 'accent'     ? 'bg-accent-10 text-accent' :
        accent === 'primary'    ? 'bg-primary-10 text-primary' :
        accent === 'error'      ? 'bg-error/10 text-error' :
        accent === 'secondary'  ? 'bg-secondary-10 text-accent' :
        'bg-bg3 text-text-muted'
      }`}>
        <Icon size={15} />
      </div>
      <div className="flex-1 min-w-0">
        <p className="text-sm font-normal text-text leading-snug">{text}</p>
        <p className="text-sm font-normal text-text-faint mt-0.5">{time}</p>
      </div>
    </div>
  )
}

export default function AdminDashboard() {
  const [stats,    setStats]    = useState(null)
  const [monthly,  setMonthly]  = useState([])
  const [pending,  setPending]  = useState([])
  const [recentEmp,setRecentEmp]= useState([])
  const [loading,  setLoading]  = useState(true)

  const today    = new Date()
  const todayStr = today.toISOString().slice(0, 10)

  useEffect(() => {
    const loadAll = async () => {
      try {
        const [empRes, leaveRes, deptRes] = await Promise.allSettled([
          api.get('/employees?size=5&sort=id,desc'),
          api.get('/leave/requests/pending'),
          api.get('/departments?size=1'),
        ])

        const totalEmployees = empRes.status === 'fulfilled'
          ? (empRes.value.data.totalElements ?? 0) : 0
        const pendingLeaves  = leaveRes.status === 'fulfilled'
          ? (leaveRes.value.data.length ?? 0) : 0
        const pendingList    = leaveRes.status === 'fulfilled'
          ? leaveRes.value.data.slice(0, 5) : []
        const deptCount      = deptRes.status === 'fulfilled'
          ? (deptRes.value.data.totalElements ?? 0) : null
        const recentList     = empRes.status === 'fulfilled'
          ? (empRes.value.data.content ?? []) : []

        // Present today
        let presentToday = null
        if (totalEmployees > 0) {
          try {
            const allEmp = await api.get('/employees?size=200&page=0')
            const ids = allEmp.data.content.map(e => e.id)
            if (ids.length > 0) {
              const analRes = await api.get('/attendance/analytics', {
                params: { employeeIds: ids, from: todayStr, to: todayStr },
              })
              presentToday = analRes.data.totalPresentDays ?? 0

              // Also build 6-month chart
              const from = new Date(today.getFullYear(), today.getMonth() - 5, 1)
                .toISOString().slice(0, 10)
              const histRes = await api.get('/attendance/analytics', {
                params: { employeeIds: ids, from, to: todayStr },
              })
              const buckets = {}
              ;(histRes.data.dailyBreakdown ?? []).forEach(d => {
                const m = d.date.slice(0, 7)
                if (!buckets[m]) buckets[m] = { present: 0, total: 0 }
                buckets[m].present += d.present
                buckets[m].total   += d.present + d.absent
              })
              const sorted = Object.entries(buckets).sort(([a],[b]) => a.localeCompare(b))
              setMonthly(sorted.map(([key, val]) => ({
                month: MONTHS[parseInt(key.split('-')[1]) - 1],
                pct:   val.total > 0 ? Math.round((val.present / val.total) * 100) : 0,
              })))
            }
          } catch { /* non-critical */ }
        }

        setStats({ totalEmployees, pendingLeaves, presentToday, deptCount })
        setPending(pendingList)
        setRecentEmp(recentList)
      } catch {
        toast.error('Failed to load dashboard')
      } finally {
        setLoading(false)
      }
    }
    loadAll()
  }, [])

  const approve = async (id) => {
    try {
      await api.patch(`/leave/requests/${id}/approve`)
      setPending(p => p.filter(r => r.id !== id))
      setStats(s => s ? { ...s, pendingLeaves: Math.max(0, s.pendingLeaves - 1) } : s)
      toast.success('Leave approved')
    } catch (err) { toast.error(err.response?.data?.message || 'Failed') }
  }
  const reject = async (id) => {
    try {
      await api.patch(`/leave/requests/${id}/reject`)
      setPending(p => p.filter(r => r.id !== id))
      setStats(s => s ? { ...s, pendingLeaves: Math.max(0, s.pendingLeaves - 1) } : s)
      toast.success('Leave rejected')
    } catch (err) { toast.error(err.response?.data?.message || 'Failed') }
  }

  // Chart config — gold bars on dark background
  const chartData = {
    labels: monthly.length > 0 ? monthly.map(m => m.month) : ['—'],
    datasets: [{
      label: 'Attendance %',
      data:  monthly.length > 0 ? monthly.map(m => m.pct) : [0],
      backgroundColor: 'rgb(239 213 129 / 0.75)',
      hoverBackgroundColor: '#efd581',
      borderRadius: { topLeft: 5, topRight: 5 },
      borderSkipped: false,
    }],
  }
  const chartOpts = {
    responsive: true,
    maintainAspectRatio: true,
    plugins: {
      legend: { display: false },
      tooltip: {
        backgroundColor: '#251f07',
        borderColor: 'rgb(239 213 129 / 0.25)',
        borderWidth: 1,
        titleColor: '#efd581',
        bodyColor: '#fcf8e7',
        padding: 10,
        cornerRadius: 8,
        callbacks: { label: ctx => ` ${ctx.parsed.y}% attendance` },
      },
    },
    scales: {
      y: {
        min: 0, max: 100,
        grid:   { color: 'rgb(239 213 129 / 0.06)' },
        ticks:  { callback: v => v + '%', color: 'rgb(252 248 231 / 0.40)', font: { size: 11 } },
        border: { display: false },
      },
      x: {
        grid:   { display: false },
        ticks:  { color: 'rgb(252 248 231 / 0.40)', font: { size: 11 } },
        border: { display: false },
      },
    },
  }

  if (loading) return (
    <div className="space-y-6">
      <SkeletonStatCards count={4} />
      <SkeletonCard rows={5} />
    </div>
  )

  return (
    <div className="space-y-6 animate-fade-up">
      <PageHeader
        title="Admin Dashboard"
        subtitle="System-wide overview"
        action={
          <Link to="/employees/new" className="btn-primary">
            <RiAddLine size={16} /> Add Employee
          </Link>
        }
      />

      {/* ── Stats ── */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          label="Total Employees"
          value={stats?.totalEmployees}
          icon={RiTeamLine} color="primary"
          sub={stats?.totalEmployees === 0 ? 'Add your first employee' : undefined}
        />
        <StatCard
          label="Pending Leaves"
          value={stats?.pendingLeaves}
          icon={RiCalendarLine} color="primary"
          sub={stats?.pendingLeaves === 0 ? 'No approvals needed' : 'Needs attention'}
        />
        <StatCard
          label="Present Today"
          value={stats?.presentToday ?? '—'}
          icon={RiCalendarCheckLine} color="accent"
          sub={
            stats?.presentToday != null
              ? `of ${stats.totalEmployees} employees`
              : 'No check-ins yet today'
          }
        />
        <StatCard
          label="Departments"
          value={stats?.deptCount ?? '—'}
          icon={RiBuilding2Line} color="secondary"
          sub={stats?.deptCount === 0 ? 'No departments yet' : 'Active org units'}
        />
      </div>

      {/* ── Chart + Pending ── */}
      <div className="grid grid-cols-1 lg:grid-cols-5 gap-5">

        {/* Attendance trend */}
        <div className="card lg:col-span-3">
          <div className="flex items-center justify-between mb-5">
            <div>
              <h4 className="text-xl font-bold text-text">Attendance Trend</h4>
              <p className="text-sm font-normal text-text-muted mt-0.5">Last 6 months — % of workforce present</p>
            </div>
            <Link to="/reports" className="btn-ghost text-sm gap-1">
              <RiBarChartLine size={14} /> Full report
            </Link>
          </div>

          {/* Chart always renders — empty data shows the axes which is intentional */}
          <Bar data={chartData} options={chartOpts} />

          {monthly.length === 0 && (
            <p className="text-center text-sm font-normal text-text-faint mt-3">
              Chart will populate once employees check in. Add demo data via the seed script to preview.
            </p>
          )}
        </div>

        {/* Pending leaves */}
        <div className="card lg:col-span-2 flex flex-col">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h4 className="text-xl font-bold text-text">Pending Leaves</h4>
              <p className="text-sm font-normal text-text-muted mt-0.5">Quick approve / reject</p>
            </div>
            <Link to="/hr"
              className="text-sm font-bold text-primary hover:brightness-125 flex items-center gap-1">
              All <RiArrowRightLine size={12} />
            </Link>
          </div>

          {pending.length === 0 ? (
            <div className="flex-1 flex flex-col items-center justify-center py-8 gap-2 text-center">
              <div className="w-12 h-12 rounded-xl bg-accent-10 border border-accent-20
                              flex items-center justify-center mb-1">
                <RiCalendarLine size={22} className="text-accent" />
              </div>
              <p className="text-base font-bold text-text">All caught up!</p>
              <p className="text-sm font-normal text-text-muted">No pending leave requests</p>
            </div>
          ) : (
            <div className="space-y-2 flex-1">
              {pending.map(r => (
                <div key={r.id}
                  className="flex items-center justify-between gap-2 p-3 rounded-xl
                             bg-bg2 hover:bg-bg3 transition-colors duration-100">
                  <div className="min-w-0">
                    <p className="text-sm font-bold text-text truncate">Emp #{r.employeeId}</p>
                    <p className="text-sm font-normal text-text-faint mt-0.5">
                      {r.leaveType?.name} · {r.totalDays}d · <span className="font-mono">{r.startDate}</span>
                    </p>
                  </div>
                  <div className="flex gap-1 shrink-0">
                    <button onClick={() => approve(r.id)}
                      className="w-7 h-7 rounded-lg bg-accent-10 text-accent border border-accent-20
                                 hover:bg-accent-20 active:scale-95 transition-all flex items-center justify-center"
                      title="Approve"><RiCheckLine size={13} /></button>
                    <button onClick={() => reject(r.id)}
                      className="w-7 h-7 rounded-lg bg-error/10 text-error border border-error/20
                                 hover:bg-error/20 active:scale-95 transition-all flex items-center justify-center"
                      title="Reject"><RiCloseLine size={13} /></button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* ── Activity feed + Recent employees ── */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">

        {/* Recent activity — derived from real data, no mocks */}
        <div className="card">
          <h4 className="text-xl font-bold text-text mb-4">Recent Activity</h4>
          {recentEmp.length === 0 && pending.length === 0 ? (
            <EmptyState
              title="No activity yet"
              desc="Activity will appear here as employees are added, check in, and submit leave requests."
              icon={RiTimeLine}
            />
          ) : (
            <div>
              {recentEmp.slice(0, 3).map(emp => (
                <ActivityItem
                  key={`emp-${emp.id}`}
                  icon={RiUserLine}
                  text={`${emp.fullName} added as ${emp.designation?.title ?? emp.status}`}
                  time={emp.joiningDate ?? 'Recently'}
                  accent="primary"
                />
              ))}
              {pending.slice(0, 3).map(r => (
                <ActivityItem
                  key={`leave-${r.id}`}
                  icon={RiCalendarLine}
                  text={`Employee #${r.employeeId} requested ${r.leaveType?.name ?? 'leave'} (${r.totalDays}d)`}
                  time={r.startDate}
                  accent="error"
                />
              ))}
              {stats?.presentToday > 0 && (
                <ActivityItem
                  icon={RiCalendarCheckLine}
                  text={`${stats.presentToday} employee${stats.presentToday !== 1 ? 's' : ''} checked in today`}
                  time="Today"
                  accent="accent"
                />
              )}
            </div>
          )}
        </div>

        {/* Recent employees preview */}
        <div className="card">
          <div className="flex items-center justify-between mb-4">
            <h4 className="text-xl font-bold text-text">Recently Added</h4>
            <Link to="/employees"
              className="text-sm font-bold text-primary hover:brightness-125 flex items-center gap-1">
              All employees <RiArrowRightLine size={12} />
            </Link>
          </div>
          {recentEmp.length === 0 ? (
            <EmptyState
              title="No employees yet"
              desc="Add your first employee to get started."
              icon={RiTeamLine}
              action={<Link to="/employees/new" className="btn-primary"><RiAddLine size={14} />Add Employee</Link>}
            />
          ) : (
            <div className="space-y-3">
              {recentEmp.slice(0, 5).map(emp => {
                const initials = [emp.firstName?.[0], emp.lastName?.[0]].filter(Boolean).join('').toUpperCase()
                return (
                  <Link to={`/employees/${emp.id}`} key={emp.id}
                    className="flex items-center gap-3 p-3 rounded-xl bg-bg2 hover:bg-bg3
                               transition-colors duration-100 group">
                    <div className="w-9 h-9 rounded-lg bg-primary flex items-center justify-center
                                    text-text-inverse font-bold text-sm shrink-0">
                      {initials}
                    </div>
                    <div className="flex-1 min-w-0">
                      <p className="text-sm font-bold text-text truncate">{emp.fullName}</p>
                      <p className="text-sm font-normal text-text-faint truncate">
                        {emp.department?.name ?? 'No department'} · {emp.status}
                      </p>
                    </div>
                    <RiArrowRightLine size={14} className="text-text-faint group-hover:text-primary transition-colors" />
                  </Link>
                )
              })}
            </div>
          )}
        </div>
      </div>

      {/* ── Quick nav ── */}
      <div className="grid grid-cols-3 sm:grid-cols-6 gap-3">
        {[
          { to:'/employees',     label:'Employees',  icon:RiTeamLine },
          { to:'/attendance',    label:'Attendance', icon:RiCalendarCheckLine },
          { to:'/leave',         label:'Leave',      icon:RiCalendarLine },
          { to:'/reports',       label:'Reports',    icon:RiBarChartLine },
          { to:'/hr',            label:'HR Board',   icon:RiGroupLine },
          { to:'/notifications', label:'Alerts',     icon:RiTimeLine },
        ].map(item => (
          <Link key={item.to} to={item.to}
            className="flex flex-col items-center gap-2 p-3.5 rounded-2xl text-center
                       bg-bg2 border border-border-subtle
                       hover:bg-primary-10 hover:border-primary-20
                       active:scale-95 transition-all duration-150 text-text-muted hover:text-primary">
            <item.icon size={19} />
            <span className="text-sm font-normal">{item.label}</span>
          </Link>
        ))}
      </div>
    </div>
  )
}
