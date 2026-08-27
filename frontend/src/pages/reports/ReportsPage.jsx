import { useState, useEffect } from 'react'
import api from '../../lib/axios'
import PageHeader from '../../components/PageHeader'
import EmptyState from '../../components/EmptyState'
import StatCard from '../../components/StatCard'
import LoadingSpinner from '../../components/LoadingSpinner'
import toast from 'react-hot-toast'
import { Bar } from 'react-chartjs-2'
import {
  Chart, CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend,
} from 'chart.js'
import {
  RiBarChartLine, RiSearchLine, RiDownloadLine,
  RiTeamLine, RiCalendarCheckLine, RiTimeLine, RiCalendarLine,
} from 'react-icons/ri'

Chart.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend)

const RATING_BADGE = {
  EXCELLENT: 'badge-success',
  GOOD:      'badge-blue',
  AVERAGE:   'badge-warning',
  POOR:      'badge-error',
}
const MONTHS = [
  'January', 'February', 'March', 'April', 'May', 'June',
  'July', 'August', 'September', 'October', 'November', 'December',
]

export default function ReportsPage() {
  const [employees,  setEmployees]  = useState([])
  const [selected,   setSelected]   = useState([])
  const [year,       setYear]       = useState(new Date().getFullYear())
  const [month,      setMonth]      = useState(new Date().getMonth() + 1)
  const [analytics,  setAnalytics]  = useState(null)
  const [loading,    setLoading]    = useState(false)
  const [empLoading, setEmpLoading] = useState(true)

  useEffect(() => {
    api.get('/employees', { params: { size: 200, page: 0 } })
      .then(r => {
        const list = r.data.content ?? []
        setEmployees(list)
        setSelected(list.map(e => e.id))
      })
      .catch(() => toast.error('Failed to load employees'))
      .finally(() => setEmpLoading(false))
  }, [])

  const loadAnalytics = async () => {
    if (!selected.length) { toast.error('Select at least one employee'); return }
    setLoading(true)
    try {
      const from = `${year}-${String(month).padStart(2, '0')}-01`
      const to   = new Date(year, month, 0).toISOString().slice(0, 10)
      const r = await api.get('/attendance/analytics', {
        params: { employeeIds: selected, from, to },
      })
      setAnalytics(r.data)
    } catch (err) {
      toast.error(err.response?.data?.message || 'Analytics request failed')
    } finally { setLoading(false) }
  }

  const downloadPdf = async () => {
    try {
      const r = await api.get('/ai/report/attendance', {
        params: { employeeIds: selected, year, month },
        responseType: 'blob',
      })
      const a = Object.assign(document.createElement('a'), {
        href: URL.createObjectURL(r.data),
        download: `attendance-${year}-${String(month).padStart(2, '0')}.pdf`,
      })
      a.click()
      URL.revokeObjectURL(a.href)
    } catch { toast.error('PDF generation failed') }
  }

  const toggle = id => setSelected(s => s.includes(id) ? s.filter(x => x !== id) : [...s, id])

  // Chart data mapped to brand palette tokens (hex values from tailwind.config.js)
  const chartData = analytics ? {
    labels: analytics.dailyBreakdown?.map(d => d.date.slice(5)) ?? [],
    datasets: [
      { label: 'Present', data: analytics.dailyBreakdown?.map(d => d.present) ?? [], backgroundColor: '#55e638', borderRadius: 4 },
      { label: 'Absent',  data: analytics.dailyBreakdown?.map(d => d.absent)  ?? [], backgroundColor: '#f87171', borderRadius: 4 },
      { label: 'Late',    data: analytics.dailyBreakdown?.map(d => d.late)    ?? [], backgroundColor: '#efd581', borderRadius: 4 },
    ],
  } : null

  const chartOpts = {
    responsive: true,
    plugins: {
      legend: {
        position: 'top',
        labels: {
          usePointStyle: true,
          color: 'rgb(252 248 231 / 0.55)',
          font: { family: 'Sedan', size: 12 },
        },
      },
    },
    scales: {
      x: { grid: { display: false }, ticks: { color: 'rgb(252 248 231 / 0.35)' } },
      y: { grid: { color: 'rgb(239 213 129 / 0.08)' }, ticks: { color: 'rgb(252 248 231 / 0.35)' } },
    },
  }

  return (
    <div className="space-y-5 animate-fade-up">
      <PageHeader title="Reports & Analytics" />

      {/* Filter card */}
      <div className="card space-y-4">
        <h4 className="text-xl font-bold text-text">Generate Report</h4>

        <div className="flex flex-wrap gap-3 items-end">
          <div className="form-group">
            <label className="label">Month</label>
            <select className="input w-36" value={month} onChange={e => setMonth(+e.target.value)}>
              {MONTHS.map((m, i) => <option key={i + 1} value={i + 1}>{m}</option>)}
            </select>
          </div>
          <div className="form-group">
            <label className="label">Year</label>
            <input
              type="number" className="input w-24" value={year}
              min="2020" max="2030"
              onChange={e => setYear(+e.target.value)}
            />
          </div>
          <button onClick={loadAnalytics} disabled={loading || empLoading} className="btn-primary">
            {loading
              ? <><LoadingSpinner /><span>Loading…</span></>
              : <><RiSearchLine size={15} />Load Analytics</>
            }
          </button>
          <button onClick={downloadPdf} className="btn-secondary">
            <RiDownloadLine size={15} />Download PDF
          </button>
        </div>

        {/* Employee selector */}
        <div>
          <div className="flex items-center justify-between mb-2">
            <label className="label mb-0">Employees</label>
            <div className="flex gap-3 text-sm">
              <button
                onClick={() => setSelected(employees.map(e => e.id))}
                className="text-primary font-bold hover:brightness-125"
              >
                All
              </button>
              <button
                onClick={() => setSelected([])}
                className="text-text-muted hover:text-text font-normal"
              >
                Clear
              </button>
            </div>
          </div>

          {empLoading ? (
            <div className="flex items-center gap-2 text-text-muted text-sm">
              <LoadingSpinner />Loading employees…
            </div>
          ) : employees.length === 0 ? (
            <p className="text-sm text-text-faint">No employees found.</p>
          ) : (
            <div className="flex flex-wrap gap-1.5 max-h-32 overflow-y-auto no-scrollbar">
              {employees.map(e => (
                <button
                  key={e.id}
                  onClick={() => toggle(e.id)}
                  className={[
                    'px-3 py-1 rounded-full text-sm font-bold border transition-all duration-100 active:scale-95',
                    selected.includes(e.id)
                      ? 'bg-primary text-text-inverse border-primary'
                      : 'bg-bg2 text-text-muted border-border-subtle hover:border-primary-30 hover:text-text',
                  ].join(' ')}
                >
                  {e.fullName}
                </button>
              ))}
            </div>
          )}
          <p className="text-sm text-text-faint mt-1.5">
            {selected.length} / {employees.length} selected
          </p>
        </div>
      </div>

      {/* Analytics results */}
      {analytics && (
        <div className="space-y-5 animate-fade-up">
          {/* Summary stat cards */}
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
            <StatCard
              label="Avg Attendance"
              value={`${analytics.averageAttendancePercentage ?? 0}%`}
              icon={RiBarChartLine}
              color="primary"
            />
            <StatCard
              label="Total Present"
              value={analytics.totalPresentDays ?? 0}
              icon={RiCalendarCheckLine}
              color="accent"
            />
            <StatCard
              label="Total Absent"
              value={analytics.totalAbsentDays ?? 0}
              icon={RiCalendarLine}
              color="error"
            />
            <StatCard
              label="Total Late"
              value={analytics.totalLateDays ?? 0}
              icon={RiTimeLine}
              color="primary"
            />
          </div>

          {/* Bar chart */}
          <div className="card">
            <h4 className="text-xl font-bold text-text mb-4">Daily Breakdown</h4>
            {chartData ? (
              <Bar data={chartData} options={chartOpts} />
            ) : (
              <p className="text-sm text-text-faint text-center py-8">No daily data available.</p>
            )}
          </div>

          {/* Employee stats table */}
          <div className="card p-0 overflow-hidden">
            <div className="px-6 py-4 border-b border-border-subtle">
              <h4 className="text-xl font-bold text-text flex items-center gap-2">
                <RiTeamLine size={17} />Employee Stats
              </h4>
            </div>
            <div className="table-container rounded-none border-0">
              <table className="table">
                <thead>
                  <tr>
                    <th>Employee</th>
                    <th>Present</th>
                    <th>Late</th>
                    <th>Attendance %</th>
                    <th>Rating</th>
                  </tr>
                </thead>
                <tbody>
                  {analytics.employeeStats?.map(s => (
                    <tr key={s.employeeId}>
                      <td className="font-bold text-text">{s.employeeName}</td>
                      <td>{s.presentDays}</td>
                      <td>{s.lateDays}</td>
                      <td>
                        <div className="flex items-center gap-2">
                          <div className="flex-1 bg-bg3 rounded-full h-1.5 max-w-[72px]">
                            <div
                              className="bg-primary h-1.5 rounded-full"
                              style={{ width: `${Math.min(s.attendancePercentage, 100)}%` }}
                            />
                          </div>
                          <span className="font-bold text-sm text-text">
                            {s.attendancePercentage}%
                          </span>
                        </div>
                      </td>
                      <td>
                        <span className={RATING_BADGE[s.rating] ?? 'badge-muted'}>
                          {s.rating}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {/* Empty state */}
      {!analytics && !loading && (
        <div className="card">
          <EmptyState
            title="No report generated yet"
            desc="Select employees, month, year then click Load Analytics."
            icon={RiBarChartLine}
          />
        </div>
      )}
    </div>
  )
}
