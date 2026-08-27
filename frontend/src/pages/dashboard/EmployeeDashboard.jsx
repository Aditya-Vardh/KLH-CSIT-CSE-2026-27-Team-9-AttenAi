import { useEffect, useState } from 'react'
import { useAuth } from '../../context/AuthContext'
import { Link } from 'react-router-dom'
import api from '../../lib/axios'
import StatCard from '../../components/StatCard'
import PageHeader from '../../components/PageHeader'
import { SkeletonStatCards } from '../../components/SkeletonCard'
import toast from 'react-hot-toast'
import { Doughnut } from 'react-chartjs-2'
import { Chart, ArcElement, Tooltip, Legend } from 'chart.js'
import {
  RiCalendarCheckLine, RiCalendarLine, RiTimeLine,
  RiLoginCircleLine, RiLogoutCircleLine, RiRobot2Line,
  RiCheckboxCircleLine, RiErrorWarningLine, RiSparklingLine,
} from 'react-icons/ri'

Chart.register(ArcElement, Tooltip, Legend)

function Row({ label, val, mono, accent }) {
  return (
    <div className="flex justify-between items-center">
      <span className="text-sm font-normal text-text-muted">{label}</span>
      <span className={`text-sm font-bold ${mono ? 'font-mono' : ''} ${accent ? 'text-accent' : 'text-text'}`}>
        {val}
      </span>
    </div>
  )
}

export default function EmployeeDashboard() {
  const { user } = useAuth()
  const [summary,      setSummary]      = useState(null)
  const [todayRec,     setTodayRec]     = useState(null)
  const [checkLoading, setCheckLoading] = useState(false)
  const [pageLoading,  setPageLoading]  = useState(true)
  const [clock,        setClock]        = useState(new Date())

  const today    = new Date()
  const todayStr = today.toISOString().slice(0, 10)
  const hour     = today.getHours()
  const greeting = hour < 12 ? 'Good morning' : hour < 17 ? 'Good afternoon' : 'Good evening'

  useEffect(() => {
    const t = setInterval(() => setClock(new Date()), 1000)
    return () => clearInterval(t)
  }, [])

  useEffect(() => {
    if (!user?.employeeId) { setPageLoading(false); return }
    const empId = user.employeeId
    Promise.allSettled([
      api.get(`/attendance/monthly/${empId}`, { params: { year: today.getFullYear(), month: today.getMonth() + 1 } }),
      api.get(`/attendance/${empId}/${todayStr}`),
    ]).then(([s, t]) => {
      if (s.status === 'fulfilled') setSummary(s.value.data)
      if (t.status === 'fulfilled') setTodayRec(t.value.data)
    }).finally(() => setPageLoading(false))
  }, [user?.employeeId])

  const checkIn = async () => {
    if (!user?.employeeId) { toast.error('No employee record linked'); return }
    setCheckLoading(true)
    try {
      const r = await api.post('/attendance/check-in', { employeeId: user.employeeId })
      setTodayRec(r.data)
      setSummary(s => s ? { ...s, presentDays: s.presentDays + 1 } : s)
      toast.success('Checked in! Have a great day 🎉')
    } catch (err) { toast.error(err.response?.data?.message || 'Check-in failed') }
    finally { setCheckLoading(false) }
  }
  const checkOut = async () => {
    if (!user?.employeeId) { toast.error('No employee record linked'); return }
    setCheckLoading(true)
    try {
      const r = await api.post('/attendance/check-out', { employeeId: user.employeeId })
      setTodayRec(r.data)
      toast.success(`Done for today! ${r.data.workingHours?.toFixed(1)}h logged 💪`)
    } catch (err) { toast.error(err.response?.data?.message || 'Check-out failed') }
    finally { setCheckLoading(false) }
  }

  const checkedIn  = Boolean(todayRec?.checkInTime)
  const checkedOut = Boolean(todayRec?.checkOutTime)
  const pct        = summary?.attendancePercentage ?? 0
  const pctColor   = pct >= 90 ? 'accent' : pct >= 75 ? 'primary' : 'error'

  const doughnutData = {
    labels: ['Present', 'Absent', 'Late'],
    datasets: [{
      data: [summary?.presentDays ?? 0, summary?.absentDays ?? 0, summary?.lateDays ?? 0],
      backgroundColor: ['#55e638', '#f87171', '#efd581'],
      borderWidth: 0, spacing: 3, hoverOffset: 4,
    }],
  }
  const doughnutOpts = {
    plugins: {
      legend: {
        position: 'bottom',
        labels: {
          usePointStyle: true, padding: 16,
          color: 'rgb(252 248 231 / 0.55)',
          font: { size: 12, family: 'Sedan' },
        },
      },
    },
    cutout: '74%',
  }

  if (pageLoading) return <div className="space-y-6"><SkeletonStatCards count={3} /></div>

  return (
    <div className="space-y-6 animate-fade-up">
      <PageHeader
        title={`${greeting}, ${user?.firstName} 👋`}
        subtitle={today.toLocaleDateString('en-US', { weekday:'long', year:'numeric', month:'long', day:'numeric' })}
      />

      {/* Stats */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <StatCard label="Present Days"  value={summary?.presentDays ?? '—'} icon={RiCalendarCheckLine} color="accent"
          sub={summary ? 'This month' : 'No check-ins recorded yet'} />
        <StatCard label="Absent Days"   value={summary?.absentDays  ?? '—'} icon={RiCalendarLine}      color="error"
          sub={summary ? 'This month' : 'Start checking in to track this'} />
        <StatCard label="Attendance %"  value={summary ? `${pct}%` : '—'}  icon={RiTimeLine}          color={pctColor}
          sub={summary ? `${summary.totalWorkingDays ?? 0} working days this month` : 'Check in to build your record'} />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-5">

        {/* ── Check-in card ── */}
        <div className="card flex flex-col items-center text-center gap-5 py-8 relative overflow-hidden">
          {/* state-driven accent top bar */}
          <div className={`absolute inset-x-0 top-0 h-1 rounded-t-2xl transition-all duration-500 ${
            checkedOut ? 'bg-accent' : checkedIn ? 'bg-primary' : 'bg-border-default'
          }`} />

          {/* Live clock */}
          <div className="mt-2">
            <p className="text-4xl font-bold tabular-nums text-text">
              {clock.toLocaleTimeString([], { hour:'2-digit', minute:'2-digit', second:'2-digit' })}
            </p>
            <p className="text-sm font-normal text-text-faint mt-1">
              {clock.toLocaleDateString('en-US', { weekday:'long', month:'short', day:'numeric' })}
            </p>
          </div>

          {/* Today record */}
          {todayRec ? (
            <div className="w-full bg-bg2 rounded-xl p-4 space-y-2 border border-border-subtle">
              <Row label="Check In"  val={todayRec.checkInTime  ?? '—'} mono />
              <Row label="Check Out" val={todayRec.checkOutTime ?? 'Not yet'} mono />
              {todayRec.workingHours != null && (
                <Row label="Hours" val={`${todayRec.workingHours.toFixed(1)}h`} accent />
              )}
              {todayRec.lateArrival && (
                <div className="flex items-center gap-1.5 text-primary text-sm font-normal pt-1">
                  <RiErrorWarningLine size={13} />
                  Late by {todayRec.lateMinutes} minutes
                </div>
              )}
            </div>
          ) : (
            <div className="w-full bg-bg2 rounded-xl p-4 border border-border-subtle">
              <p className="text-sm font-normal text-text-faint">No record yet — check in to start today.</p>
            </div>
          )}

          {!user?.employeeId ? (
            <p className="text-sm font-normal text-primary bg-primary-10 px-3 py-2 rounded-xl border border-primary-20">
              ⚠ No employee record linked to your account
            </p>
          ) : !checkedIn ? (
            <button onClick={checkIn} disabled={checkLoading}
              className="btn-primary w-full justify-center py-3.5 text-base shadow-glow">
              {checkLoading ? '…' : <><RiLoginCircleLine size={18} />Check In</>}
            </button>
          ) : !checkedOut ? (
            <button onClick={checkOut} disabled={checkLoading}
              className="btn-danger w-full justify-center py-3.5 text-base">
              {checkLoading ? '…' : <><RiLogoutCircleLine size={18} />Check Out</>}
            </button>
          ) : (
            <div className="flex items-center gap-2 text-accent font-bold text-sm">
              <RiCheckboxCircleLine size={20} />Done for today — great work!
            </div>
          )}
        </div>

        {/* ── Doughnut ── */}
        <div className="card">
          <div className="flex items-center justify-between mb-4">
            <h4 className="text-xl font-bold text-text">This Month</h4>
            {summary && (
              <span className={`text-2xl font-bold tabular-nums ${
                pct >= 90 ? 'text-accent' : pct >= 75 ? 'text-primary' : 'text-error'
              }`}>{pct}%</span>
            )}
          </div>
          {(summary?.presentDays || summary?.absentDays) ? (
            <Doughnut data={doughnutData} options={doughnutOpts} />
          ) : (
            <div className="h-48 flex flex-col items-center justify-center gap-3">
              <div className="w-32 h-32 rounded-full border-4 border-dashed border-border-default
                              flex items-center justify-center">
                <p className="text-sm font-normal text-text-faint text-center px-2">
                  Chart populates after your first check-in
                </p>
              </div>
            </div>
          )}
        </div>

        {/* ── Quick actions ── */}
        <div className="card flex flex-col gap-2.5">
          <h4 className="text-xl font-bold text-text mb-1">Quick Actions</h4>
          {[
            { to:'/attendance',    icon:RiCalendarCheckLine, label:'Attendance History' },
            { to:'/leave/new',     icon:RiCalendarLine,      label:'Apply for Leave' },
            { to:'/leave',         icon:RiTimeLine,          label:'Leave Balance' },
            { to:'/ai',            icon:RiRobot2Line,        label:'Ask AI Assistant' },
            { to:'/notifications', icon:RiSparklingLine,     label:'Notifications' },
          ].map(item => (
            <Link key={item.to} to={item.to}
              className="flex items-center gap-3 p-3 rounded-xl bg-bg2 border border-border-subtle
                         hover:bg-primary-10 hover:border-primary-20
                         active:scale-[0.98] transition-all duration-100 text-text-muted hover:text-primary">
              <item.icon size={16} className="shrink-0" />
              <span className="text-sm font-normal">{item.label}</span>
            </Link>
          ))}
        </div>
      </div>
    </div>
  )
}
