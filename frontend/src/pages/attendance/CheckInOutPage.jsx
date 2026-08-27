import { useEffect, useState } from 'react'
import { useAuth } from '../../context/AuthContext'
import api from '../../lib/axios'
import PageHeader from '../../components/PageHeader'
import LoadingSpinner from '../../components/LoadingSpinner'
import toast from 'react-hot-toast'
import {
  RiLoginCircleLine, RiLogoutCircleLine,
  RiCheckboxCircleLine, RiErrorWarningLine, RiTimeLine,
} from 'react-icons/ri'

function RecordRow({ label, val, mono, accent }) {
  return (
    <div className="flex justify-between items-center p-3 bg-bg2 rounded-xl border border-border-subtle">
      <span className="text-sm font-normal text-text-muted">{label}</span>
      <span className={[
        'text-sm font-bold',
        mono   ? 'font-mono'   : '',
        accent ? 'text-accent' : 'text-text',
      ].join(' ')}>
        {val}
      </span>
    </div>
  )
}

export default function CheckInOutPage() {
  const { user } = useAuth()
  const [today,        setToday]  = useState(null)
  const [pageLoading,  setPage]   = useState(true)
  const [actionLoading,setAction] = useState(false)
  const [clock,        setClock]  = useState(new Date())
  const nowStr = new Date().toISOString().slice(0, 10)

  // Live clock — ticks every second
  useEffect(() => {
    const t = setInterval(() => setClock(new Date()), 1000)
    return () => clearInterval(t)
  }, [])

  // Load today's record on mount
  useEffect(() => {
    const empId = user?.employeeId
    if (!empId) { setPage(false); return }
    api.get(`/attendance/${empId}/${nowStr}`)
      .then(r => setToday(r.data))
      .catch(e => { if (e.response?.status !== 404) toast.error("Failed to load today's record") })
      .finally(() => setPage(false))
  }, [user?.employeeId, nowStr])

  const checkIn = async () => {
    const empId = user?.employeeId
    if (!empId) { toast.error('No employee record linked'); return }
    setAction(true)
    try {
      const r = await api.post('/attendance/check-in', { employeeId: empId })
      setToday(r.data)
      toast.success('Checked in! Have a great day 🎉')
    } catch (err) { toast.error(err.response?.data?.message || 'Check-in failed') }
    finally { setAction(false) }
  }

  const checkOut = async () => {
    const empId = user?.employeeId
    if (!empId) { toast.error('No employee record linked'); return }
    setAction(true)
    try {
      const r = await api.post('/attendance/check-out', { employeeId: empId })
      setToday(r.data)
      toast.success(`Done! ${r.data.workingHours?.toFixed(1)}h logged 💪`)
    } catch (err) { toast.error(err.response?.data?.message || 'Check-out failed') }
    finally { setAction(false) }
  }

  const checkedIn  = Boolean(today?.checkInTime)
  const checkedOut = Boolean(today?.checkOutTime)

  if (pageLoading) return (
    <div className="flex items-center justify-center h-64">
      <LoadingSpinner full />
    </div>
  )

  return (
    <div className="space-y-5 animate-fade-up">
      <PageHeader title="Check In / Out" subtitle={nowStr} />

      <div className="max-w-sm mx-auto space-y-4">

        {/* Live clock card */}
        <div className="rounded-2xl p-8 text-center shadow-glow-lg border border-primary-20
                        bg-gradient-to-br from-bg2 via-bg3 to-bg2">
          {/* Accent top bar shows current state */}
          <div className={[
            'absolute inset-x-0 top-0 h-1.5 rounded-t-2xl transition-all duration-500',
            checkedOut ? 'bg-accent' : checkedIn ? 'bg-primary' : 'bg-border-subtle',
          ].join(' ')} />

          <p className="text-4xl font-bold tabular-nums text-text drop-shadow-sm">
            {clock.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' })}
          </p>
          <p className="text-sm font-normal text-text-muted mt-2">
            {clock.toLocaleDateString('en-US', {
              weekday: 'long', year: 'numeric', month: 'long', day: 'numeric',
            })}
          </p>
        </div>

        {/* Today's record */}
        <div className="card space-y-2.5">
          <h4 className="text-xl font-bold text-text flex items-center gap-2 mb-1">
            <RiTimeLine size={17} className="text-primary" />
            Today's Record
          </h4>

          {today ? (
            <>
              <RecordRow label="Check In"  val={today.checkInTime  ?? '—'} mono />
              <RecordRow label="Check Out" val={today.checkOutTime ?? 'Not yet'} mono />
              {today.workingHours != null && (
                <RecordRow label="Working Hours" val={`${today.workingHours.toFixed(1)}h`} accent />
              )}
              {today.status && (
                <RecordRow label="Status" val={today.status} />
              )}
              {today.lateArrival && (
                <div className="flex items-center gap-2 p-3 bg-primary-10 rounded-xl border border-primary-20">
                  <RiErrorWarningLine size={15} className="text-primary shrink-0" />
                  <span className="text-sm font-normal text-primary">
                    Late by {today.lateMinutes} minutes
                  </span>
                </div>
              )}
            </>
          ) : (
            <p className="text-sm font-normal text-text-faint text-center py-5">
              No record yet for today.
            </p>
          )}
        </div>

        {/* Action button */}
        {!user?.employeeId ? (
          <div className="p-4 rounded-xl bg-primary-10 border border-primary-20 text-sm text-primary">
            ⚠ Not linked to an employee record. Contact HR.
          </div>
        ) : !checkedIn ? (
          <button
            onClick={checkIn}
            disabled={actionLoading}
            className="btn-primary w-full justify-center py-4 text-base shadow-glow"
          >
            {actionLoading
              ? <><LoadingSpinner /><span>Please wait…</span></>
              : <><RiLoginCircleLine size={20} />Check In</>
            }
          </button>
        ) : !checkedOut ? (
          <button
            onClick={checkOut}
            disabled={actionLoading}
            className="btn-danger w-full justify-center py-4 text-base"
          >
            {actionLoading
              ? <><LoadingSpinner /><span>Please wait…</span></>
              : <><RiLogoutCircleLine size={20} />Check Out</>
            }
          </button>
        ) : (
          <div className="flex items-center justify-center gap-2 p-4
                          bg-accent-10 rounded-2xl border border-accent-20">
            <RiCheckboxCircleLine size={22} className="text-accent" />
            <span className="text-accent font-bold text-sm">
              Done for today! Great work 🎉
            </span>
          </div>
        )}
      </div>
    </div>
  )
}
