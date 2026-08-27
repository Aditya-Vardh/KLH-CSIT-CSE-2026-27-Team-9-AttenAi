import { useEffect, useState } from 'react'
import { useAuth } from '../../context/AuthContext'
import { Link } from 'react-router-dom'
import api from '../../lib/axios'
import PageHeader from '../../components/PageHeader'
import EmptyState from '../../components/EmptyState'
import { SkeletonTable } from '../../components/SkeletonCard'
import toast from 'react-hot-toast'
import { RiCalendarCheckLine, RiLoginCircleLine } from 'react-icons/ri'

const STATUS_BADGE = {
  PRESENT:  'badge-green', ABSENT: 'badge-red', LATE: 'badge-yellow',
  HALF_DAY: 'badge-blue',  ON_LEAVE: 'badge-indigo',
}

export default function AttendancePage() {
  const { user } = useAuth()
  const [records, setRecords] = useState([])
  const [loading, setLoading] = useState(true)
  const today = new Date()

  useEffect(() => {
    const empId = user?.employeeId
    if (!empId) { setLoading(false); return }
    const from = new Date(today.getFullYear(), today.getMonth(), 1).toISOString().slice(0, 10)
    const to   = today.toISOString().slice(0, 10)
    api.get(`/attendance/history/${empId}`, { params: { from, to, size: 31 } })
      .then(r => setRecords(r.data.content ?? []))
      .catch(() => toast.error('Failed to load attendance'))
      .finally(() => setLoading(false))
  }, [user?.employeeId])

  if (!user?.employeeId) return (
    <div className="card"><EmptyState title="No employee record" desc="Contact HR to link your account." icon={RiCalendarCheckLine} /></div>
  )

  const presentCount = records.filter(r => ['PRESENT','LATE'].includes(r.status)).length
  const lateCount    = records.filter(r => r.status === 'LATE').length
  const totalHours   = records.reduce((s, r) => s + (r.workingHours ?? 0), 0)
  const avgHours     = records.filter(r => r.workingHours).length
    ? (totalHours / records.filter(r => r.workingHours).length).toFixed(1) : '—'

  return (
    <div className="space-y-5 animate-fade-up">
      <PageHeader
        title="Attendance"
        subtitle={today.toLocaleDateString('en-US', { month: 'long', year: 'numeric' })}
        action={<Link to="/attendance/checkin" className="btn-primary"><RiLoginCircleLine size={16} />Check In / Out</Link>}
      />

      {!loading && records.length > 0 && (
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
          {[
            { label: 'Days Present', value: presentCount, c: 'text-emerald-700 bg-emerald-50 border-emerald-100' },
            { label: 'Days Late',    value: lateCount,    c: 'text-amber-700  bg-amber-50  border-amber-100'  },
            { label: 'Avg Hours',    value: `${avgHours}h`, c: 'text-blue-700  bg-blue-50   border-blue-100'  },
            { label: 'Total Records',value: records.length, c: 'text-violet-700 bg-violet-50 border-violet-100' },
          ].map(s => (
            <div key={s.label} className={`rounded-2xl p-4 border ${s.c}`}>
              <p className="text-2xl font-extrabold tabular-nums">{s.value}</p>
              <p className="text-xs font-semibold mt-1 opacity-70">{s.label}</p>
            </div>
          ))}
        </div>
      )}

      <div className="card">
        <h4 className="font-bold text-slate-800 mb-5">Monthly Records</h4>
        {loading ? <SkeletonTable rows={10} cols={6} /> :
          records.length === 0 ? (
            <EmptyState title="No records yet" desc="Check in to start tracking attendance." icon={RiCalendarCheckLine}
              action={<Link to="/attendance/checkin" className="btn-primary"><RiLoginCircleLine size={14} />Check In Now</Link>} />
          ) : (
            <div className="table-container">
              <table className="table">
                <thead><tr><th>Date</th><th>Check In</th><th>Check Out</th><th>Hours</th><th>Status</th><th>Late</th></tr></thead>
                <tbody>
                  {records.map(r => (
                    <tr key={r.id}>
                      <td className="font-semibold text-slate-800">{r.attendanceDate}</td>
                      <td className="font-mono text-xs">{r.checkInTime  ?? <span className="text-slate-300">—</span>}</td>
                      <td className="font-mono text-xs">{r.checkOutTime ?? <span className="text-slate-300">—</span>}</td>
                      <td>{r.workingHours != null ? <span className="font-semibold">{r.workingHours.toFixed(1)}h</span> : <span className="text-slate-300">—</span>}</td>
                      <td><span className={STATUS_BADGE[r.status] ?? 'badge-gray'}>{r.status}</span></td>
                      <td>{r.lateArrival ? <span className="badge-yellow">{r.lateMinutes}m</span> : <span className="text-slate-300">—</span>}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )
        }
      </div>
    </div>
  )
}
