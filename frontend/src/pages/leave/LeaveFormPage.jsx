import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { useAuth } from '../../context/AuthContext'
import { useNavigate } from 'react-router-dom'
import api from '../../lib/axios'
import PageHeader from '../../components/PageHeader'
import LoadingSpinner from '../../components/LoadingSpinner'
import toast from 'react-hot-toast'
import { RiCalendarLine, RiArrowLeftLine, RiSendPlane2Line } from 'react-icons/ri'

export default function LeaveFormPage() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [types,   setTypes]   = useState([])
  const [loading, setLoading] = useState(false)
  const [typesLoading, setTypesLoading] = useState(true)
  const { register, handleSubmit, watch, formState: { errors } } = useForm()
  const startDate = watch('startDate')

  useEffect(() => {
    api.get('/leave/types')
      .then(r => setTypes(r.data ?? []))
      .catch(() => toast.error('Failed to load leave types'))
      .finally(() => setTypesLoading(false))
  }, [])

  const onSubmit = async (data) => {
    const empId = user?.employeeId
    if (!empId) { toast.error('No employee record linked. Contact HR.'); return }
    if (data.endDate < data.startDate) { toast.error('End date cannot be before start date'); return }
    setLoading(true)
    try {
      await api.post('/leave/requests', { employeeId: empId, leaveTypeId: Number(data.leaveTypeId), startDate: data.startDate, endDate: data.endDate, reason: data.reason || null })
      toast.success('Leave request submitted!')
      navigate('/leave')
    } catch (err) { toast.error(err.response?.data?.message || 'Submission failed') }
    finally { setLoading(false) }
  }

  return (
    <div className="space-y-5 animate-fade-up">
      <PageHeader title="Apply for Leave" />
      <form onSubmit={handleSubmit(onSubmit)}>
        <div className="card max-w-lg space-y-5">
          {!user?.employeeId && (
            <div className="p-3 rounded-xl bg-amber-50 border border-amber-200 text-sm text-amber-700">
              ⚠ No employee record linked. Contact HR before applying.
            </div>
          )}

          <div className="form-group">
            <label className="label">Leave Type <span className="text-red-400">*</span></label>
            {typesLoading ? <div className="input text-slate-400 text-sm">Loading…</div> : (
              <select className={`input ${errors.leaveTypeId ? 'input-error' : ''}`}
                {...register('leaveTypeId', { required: 'Select a leave type' })}>
                <option value="">— Select leave type —</option>
                {types.map(t => <option key={t.id} value={t.id}>{t.name} ({t.defaultDays} days/yr)</option>)}
              </select>
            )}
            {errors.leaveTypeId && <p className="error-msg">{errors.leaveTypeId.message}</p>}
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="form-group">
              <label className="label">From <span className="text-red-400">*</span></label>
              <input type="date" className={`input ${errors.startDate ? 'input-error' : ''}`}
                {...register('startDate', { required: 'Required' })} />
              {errors.startDate && <p className="error-msg">{errors.startDate.message}</p>}
            </div>
            <div className="form-group">
              <label className="label">To <span className="text-red-400">*</span></label>
              <input type="date" className={`input ${errors.endDate ? 'input-error' : ''}`}
                min={startDate || undefined}
                {...register('endDate', { required: 'Required' })} />
              {errors.endDate && <p className="error-msg">{errors.endDate.message}</p>}
            </div>
          </div>

          <div className="form-group">
            <label className="label">Reason <span className="text-slate-400 font-normal text-xs">(optional)</span></label>
            <textarea className="input h-24 resize-none" placeholder="Brief description…" {...register('reason')} />
          </div>

          <div className="flex gap-3 pt-1 border-t border-slate-100">
            <button type="submit" disabled={loading || !user?.employeeId} className="btn-primary">
              {loading ? <><LoadingSpinner /><span>Submitting…</span></> : <><RiSendPlane2Line size={15} />Submit Request</>}
            </button>
            <button type="button" onClick={() => navigate(-1)} className="btn-secondary">
              <RiArrowLeftLine size={15} /> Cancel
            </button>
          </div>
        </div>
      </form>
    </div>
  )
}
