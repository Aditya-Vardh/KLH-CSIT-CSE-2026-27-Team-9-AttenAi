import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../../lib/axios'
import PageHeader from '../../components/PageHeader'
import StatCard from '../../components/StatCard'
import EmptyState from '../../components/EmptyState'
import LoadingSpinner from '../../components/LoadingSpinner'
import {
  RiBarChartLine, RiCalendarCheckLine, RiCalendarCloseLine,
  RiTimeLine, RiAlertLine, RiRefreshLine, RiTeamLine,
} from 'react-icons/ri'

const formatPct = value => `${Number(value ?? 0).toFixed(1)}%`

export default function WorkforcePage() {
  const [summary, setSummary] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const load = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const employeesResponse = await api.get('/employees', { params: { size: 200, page: 0 } })
      const employees = employeesResponse.data.content ?? []
      if (!employees.length) {
        setSummary({ employees: [], departments: [], anomalies: [], totalEmployees: 0 })
        return
      }
      const today = new Date()
      const fromDate = new Date(today.getFullYear(), today.getMonth(), 1).toISOString().slice(0, 10)
      const toDate = today.toISOString().slice(0, 10)
      const response = await api.get('/ai/workforce/summary', {
        params: { employeeIds: employees.map(employee => employee.id), fromDate, toDate },
      })
      setSummary(response.data)
    } catch (requestError) {
      setError(requestError.response?.data?.message || 'Workforce analytics are currently unavailable.')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { load() }, [load])

  if (loading) return (
    <div className="flex items-center justify-center min-h-64 text-text-muted">
      <LoadingSpinner /><span className="ml-2">Loading workforce intelligence...</span>
    </div>
  )

  if (error) return (
    <EmptyState
      title="Workforce intelligence is unavailable"
      desc={error}
      icon={RiAlertLine}
      action={<button className="btn-primary" onClick={load}><RiRefreshLine size={16} />Retry</button>}
    />
  )

  if (!summary?.totalEmployees) return (
    <EmptyState title="No employees found" desc="Add employees before viewing workforce analytics." icon={RiTeamLine} />
  )

  return (
    <div className="space-y-6 animate-fade-up">
      <PageHeader title="Workforce Intelligence" subtitle="AI-powered workforce analytics" />

      <div className="grid grid-cols-2 lg:grid-cols-6 gap-4">
        <StatCard label="Workforce" value={summary.totalEmployees} icon={RiTeamLine} color="primary" />
        <StatCard label="Attendance" value={formatPct(summary.averageAttendancePct)} icon={RiBarChartLine} color="accent" />
        <StatCard label="Present" value={summary.presentDays} icon={RiCalendarCheckLine} color="accent" />
        <StatCard label="Absent" value={summary.absentDays} icon={RiCalendarCloseLine} color="error" />
        <StatCard label="Late" value={summary.lateDays} icon={RiTimeLine} color="primary" />
        <StatCard label="Change" value={`${summary.attendanceChangePctPoints >= 0 ? '+' : ''}${Number(summary.attendanceChangePctPoints ?? 0).toFixed(1)} pp`} trend={summary.attendanceChangePctPoints} icon={RiBarChartLine} color="secondary" />
      </div>

      <div className="grid grid-cols-1 xl:grid-cols-2 gap-5">
        <section className="card">
          <div className="flex items-center justify-between mb-4">
            <div><h2 className="text-xl font-bold text-text">Anomalies</h2><p className="text-sm text-text-muted">Deterministic attendance signals for this period.</p></div>
            <RiAlertLine className="text-primary" size={22} />
          </div>
          {!summary.anomalies?.length ? <EmptyState title="No anomalies detected" desc="No employees crossed the configured thresholds." icon={RiCalendarCheckLine} /> : (
            <div className="space-y-3">
              {summary.anomalies.map((item, index) => (
                <div key={`${item.employeeId}-${item.type}-${index}`} className="flex items-center justify-between gap-3 p-3 rounded-xl bg-bg2 border border-border-subtle">
                  <div className="min-w-0"><p className="font-bold text-text truncate">{item.employeeName}</p><p className="text-sm text-text-muted">{item.department} · {item.type.replaceAll('_', ' ')}</p></div>
                  <span className={item.severity === 'HIGH' ? 'badge-error' : 'badge-warning'}>{Number(item.metric).toFixed(1)}</span>
                </div>
              ))}
            </div>
          )}
        </section>

        <section className="card">
          <div className="flex items-center justify-between mb-4"><div><h2 className="text-xl font-bold text-text">Department insights</h2><p className="text-sm text-text-muted">Attendance grouped by verified department data.</p></div><RiTeamLine className="text-primary" size={22} /></div>
          {!summary.departments?.length ? <EmptyState title="No department data" desc="Department information is not available for this period." icon={RiTeamLine} /> : (
            <div className="overflow-x-auto"><table className="w-full text-sm"><thead><tr className="text-left text-text-faint border-b border-border-subtle"><th className="pb-2">Department</th><th className="pb-2">People</th><th className="pb-2">Attendance</th><th className="pb-2">Absent</th></tr></thead><tbody>{summary.departments.map(department => <tr key={department.department} className="border-b border-border-subtle last:border-0"><td className="py-3 font-bold text-text">{department.department}</td><td className="py-3 text-text-muted">{department.employeeCount}</td><td className="py-3 text-text">{formatPct(department.averageAttendancePct)}</td><td className="py-3 text-text-muted">{department.absentDays}</td></tr>)}</tbody></table></div>
          )}
        </section>
      </div>

      <section className="card">
        <div className="flex items-center justify-between mb-4"><div><h2 className="text-xl font-bold text-text">Employee insights</h2><p className="text-sm text-text-muted">Period: {summary.fromDate} to {summary.toDate}</p></div><Link to="/reports" className="btn-secondary">Open Reports</Link></div>
        {!summary.employees?.length ? <EmptyState title="No attendance data available for this period" icon={RiCalendarCheckLine} /> : <div className="overflow-x-auto"><table className="w-full text-sm"><thead><tr className="text-left text-text-faint border-b border-border-subtle"><th className="pb-2">Employee</th><th className="pb-2">Department</th><th className="pb-2">Attendance</th><th className="pb-2">Late</th><th className="pb-2">Absent</th><th className="pb-2">Change</th><th className="pb-2">Status</th></tr></thead><tbody>{summary.employees.map(employee => <tr key={employee.employeeId} className="border-b border-border-subtle last:border-0"><td className="py-3 font-bold text-text">{employee.employeeName}</td><td className="py-3 text-text-muted">{employee.department}</td><td className="py-3 text-text">{formatPct(employee.attendancePct)}</td><td className="py-3 text-text-muted">{employee.lateDays}</td><td className="py-3 text-text-muted">{employee.absentDays}</td><td className={employee.changePctPoints < 0 ? 'py-3 text-error' : 'py-3 text-accent'}>{employee.changePctPoints >= 0 ? '+' : ''}{Number(employee.changePctPoints).toFixed(1)} pp</td><td className={employee.status === 'OK' ? 'badge-success' : 'badge-warning'}>{employee.status.replaceAll('_', ' ')}</td></tr>)}</tbody></table></div>}
      </section>
    </div>
  )
}