import { useEffect, useState } from 'react'
import { useAuth } from '../../context/AuthContext'
import { Link } from 'react-router-dom'
import api from '../../lib/axios'
import PageHeader from '../../components/PageHeader'
import EmptyState from '../../components/EmptyState'
import { SkeletonTable } from '../../components/SkeletonCard'
import toast from 'react-hot-toast'
import { RiCalendarLine, RiAddLine, RiCloseCircleLine } from 'react-icons/ri'

const STATUS_BADGE = { PENDING:'badge-yellow', APPROVED:'badge-green', REJECTED:'badge-red', CANCELLED:'badge-gray', WITHDRAWN:'badge-gray' }

export default function LeavePage() {
  const { user } = useAuth()
  const [requests,   setRequests]   = useState([])
  const [balances,   setBalances]   = useState([])
  const [loading,    setLoading]    = useState(true)
  const [cancelling, setCancelling] = useState(null)
  const empId = user?.employeeId

  useEffect(() => {
    if (!empId) { setLoading(false); return }
    Promise.allSettled([
      api.get(`/leave/requests/employee/${empId}?size=20`),
      api.get(`/leave/balance/${empId}`),
    ]).then(([req, bal]) => {
      if (req.status === 'fulfilled') setRequests(req.value.data.content ?? [])
      if (bal.status === 'fulfilled') setBalances(bal.value.data ?? [])
    }).finally(() => setLoading(false))
  }, [empId])

  const cancel = async (requestId) => {
    if (!empId) return
    setCancelling(requestId)
    try {
      await api.patch(`/leave/requests/${requestId}/cancel`, null, { params: { employeeId: empId } })
      setRequests(r => r.map(x => x.id === requestId ? { ...x, status: 'CANCELLED' } : x))
      toast.success('Leave cancelled')
    } catch (err) { toast.error(err.response?.data?.message || 'Cannot cancel') }
    finally { setCancelling(null) }
  }

  if (!empId && !loading) return (
    <div className="card"><EmptyState title="No employee record" desc="Contact HR to link your account." icon={RiCalendarLine} /></div>
  )

  return (
    <div className="space-y-5 animate-fade-up">
      <PageHeader title="Leave" subtitle="Balance and requests"
        action={<Link to="/leave/new" className="btn-primary"><RiAddLine size={16} />Apply Leave</Link>} />

      {/* Balance cards */}
      {loading ? (
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
          {[1,2,3,4].map(i => (
            <div key={i} className="card animate-pulse">
              <div className="skeleton h-8 w-12 rounded mb-3" />
              <div className="skeleton h-3 w-3/4 rounded mb-1.5" />
              <div className="skeleton h-2 w-full rounded mt-3" />
            </div>
          ))}
        </div>
      ) : balances.length > 0 && (
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
          {balances.map(b => (
            <div key={b.id} className="card-hover text-center group">
              <p className="text-3xl font-extrabold text-primary-600 tabular-nums">{b.remainingDays}</p>
              <p className="text-sm font-semibold text-slate-700 mt-1">{b.leaveTypeName}</p>
              <p className="text-xs text-slate-400 mt-0.5">{b.usedDays} used / {b.allocatedDays} total</p>
              <div className="mt-3 h-1.5 bg-slate-100 rounded-full overflow-hidden">
                <div className="h-full bg-gradient-to-r from-primary-500 to-violet-500 rounded-full transition-all duration-500"
                  style={{ width: `${b.allocatedDays > 0 ? Math.round((b.usedDays / b.allocatedDays) * 100) : 0}%` }} />
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Requests table */}
      <div className="card">
        <h4 className="font-bold text-slate-800 mb-5">My Requests</h4>
        {loading ? <SkeletonTable rows={5} cols={6} /> :
          requests.length === 0 ? (
            <EmptyState title="No leave requests" desc="Apply for your first leave." icon={RiCalendarLine}
              action={<Link to="/leave/new" className="btn-primary"><RiAddLine size={14} />Apply Now</Link>} />
          ) : (
            <div className="table-container">
              <table className="table">
                <thead><tr><th>Type</th><th>From</th><th>To</th><th>Days</th><th>Status</th><th className="hidden md:table-cell">Reason</th><th>Action</th></tr></thead>
                <tbody>
                  {requests.map(r => (
                    <tr key={r.id}>
                      <td><span className="badge-indigo">{r.leaveType?.name ?? '—'}</span></td>
                      <td className="font-mono text-xs">{r.startDate}</td>
                      <td className="font-mono text-xs">{r.endDate}</td>
                      <td><span className="font-bold">{r.totalDays}d</span></td>
                      <td><span className={STATUS_BADGE[r.status] ?? 'badge-gray'}>{r.status}</span></td>
                      <td className="hidden md:table-cell text-xs text-slate-400 max-w-[140px] truncate">{r.reason || '—'}</td>
                      <td>
                        {r.status === 'PENDING' && (
                          <button onClick={() => cancel(r.id)} disabled={cancelling === r.id}
                            className="inline-flex items-center gap-1 px-2 py-1 rounded-lg bg-red-50 text-red-600
                                       text-xs font-semibold hover:bg-red-100 active:scale-95 transition-all disabled:opacity-50">
                            <RiCloseCircleLine size={12} />{cancelling === r.id ? '…' : 'Cancel'}
                          </button>
                        )}
                      </td>
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
