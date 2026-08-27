import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import api from '../../lib/axios'
import PageHeader from '../../components/PageHeader'
import LoadingSpinner from '../../components/LoadingSpinner'
import toast from 'react-hot-toast'
import { RiSaveLine, RiArrowLeftLine } from 'react-icons/ri'

export default function EmployeeForm() {
  const { id }     = useParams()
  const navigate   = useNavigate()
  const isEdit     = Boolean(id)
  const [depts,    setDepts]    = useState([])
  const [desigs,   setDesigs]   = useState([])
  const [loading,  setLoading]  = useState(false)
  const [fetching, setFetching] = useState(isEdit)

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm({
    defaultValues: { status: 'ACTIVE', annualLeaveQuota: 24 }
  })

  useEffect(() => {
    // Load departments and designations
    Promise.allSettled([
      api.get('/departments?size=100&active=true'),
      api.get('/designations?size=100&active=true'),
    ]).then(([d, des]) => {
      setDepts(d.status === 'fulfilled' ? (d.value.data.content ?? []) : [])
      setDesigs(des.status === 'fulfilled' ? (des.value.data.content ?? []) : [])
    })

    if (isEdit) {
      api.get(`/employees/${id}`)
        .then(r => {
          const emp = r.data
          reset({
            firstName:       emp.firstName,
            lastName:        emp.lastName,
            employeeCode:    emp.employeeCode,
            email:           emp.email,
            phone:           emp.phone  ?? '',
            address:         emp.address ?? '',
            dateOfBirth:     emp.dateOfBirth ?? '',
            joiningDate:     emp.joiningDate ?? '',
            status:          emp.status ?? 'ACTIVE',
            annualLeaveQuota: emp.annualLeaveQuota ?? 24,
            // IDs come as numbers from backend; keep as numbers for the form
            departmentId:    emp.department?.id  ?? '',
            designationId:   emp.designation?.id ?? '',
          })
        })
        .catch(() => toast.error('Failed to load employee'))
        .finally(() => setFetching(false))
    }
  }, [id])

  const onSubmit = async (rawData) => {
    setLoading(true)
    try {
      // Convert string IDs from <select> to numbers (or null if empty)
      const payload = {
        ...rawData,
        departmentId:  rawData.departmentId  ? Number(rawData.departmentId)  : null,
        designationId: rawData.designationId ? Number(rawData.designationId) : null,
        annualLeaveQuota: Number(rawData.annualLeaveQuota) || 24,
        dateOfBirth:   rawData.dateOfBirth || null,
        phone:         rawData.phone || null,
        address:       rawData.address || null,
      }

      if (isEdit) {
        await api.put(`/employees/${id}`, payload)
        toast.success('Employee updated successfully')
      } else {
        await api.post('/employees', payload)
        toast.success('Employee created successfully')
      }
      navigate('/employees')
    } catch (err) {
      toast.error(err.response?.data?.message || 'Error saving employee')
    } finally {
      setLoading(false)
    }
  }

  if (fetching) return (
    <div className="flex items-center justify-center h-64">
      <LoadingSpinner full />
    </div>
  )

  return (
    <div className="space-y-4 animate-fade-in">
      <PageHeader title={isEdit ? 'Edit Employee' : 'New Employee'} />

      <form onSubmit={handleSubmit(onSubmit)}>
        <div className="card max-w-2xl space-y-5">
          {/* Personal info */}
          <div>
            <h3 className="text-sm font-bold text-text-muted uppercase tracking-wider mb-4 pb-2 border-b border-border-subtle">
              Personal Information
            </h3>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="form-group">
                <label className="label">First Name *</label>
                <input className={`input ${errors.firstName ? 'input-error' : ''}`}
                  placeholder="John"
                  {...register('firstName', { required: 'First name is required' })} />
                {errors.firstName && <p className="error-msg">{errors.firstName.message}</p>}
              </div>
              <div className="form-group">
                <label className="label">Last Name *</label>
                <input className={`input ${errors.lastName ? 'input-error' : ''}`}
                  placeholder="Doe"
                  {...register('lastName', { required: 'Last name is required' })} />
                {errors.lastName && <p className="error-msg">{errors.lastName.message}</p>}
              </div>
              <div className="form-group">
                <label className="label">Email *</label>
                <input type="email" className={`input ${errors.email ? 'input-error' : ''}`}
                  placeholder="john@company.com"
                  {...register('email', { required: 'Email is required' })} />
                {errors.email && <p className="error-msg">{errors.email.message}</p>}
              </div>
              <div className="form-group">
                <label className="label">Phone</label>
                <input className="input" placeholder="+1-555-000-0000"
                  {...register('phone')} />
              </div>
              <div className="form-group">
                <label className="label">Date of Birth</label>
                <input type="date" className="input" {...register('dateOfBirth')} />
              </div>
              <div className="form-group sm:col-span-2">
                <label className="label">Address</label>
                <input className="input" placeholder="123 Main St, City, Country"
                  {...register('address')} />
              </div>
            </div>
          </div>

          {/* Employment info */}
          <div>
            <h3 className="text-sm font-bold text-text-muted uppercase tracking-wider mb-4 pb-2 border-b border-border-subtle">
              Employment Details
            </h3>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="form-group">
                <label className="label">Employee Code *</label>
                <input className={`input font-mono ${errors.employeeCode ? 'input-error' : ''}`}
                  placeholder="EMP-001"
                  {...register('employeeCode', {
                    required: 'Employee code is required',
                    pattern: { value: /^[A-Z0-9\-]+$/, message: 'Uppercase letters, digits and hyphens only' }
                  })} />
                {errors.employeeCode && <p className="error-msg">{errors.employeeCode.message}</p>}
              </div>
              <div className="form-group">
                <label className="label">Joining Date *</label>
                <input type="date" className={`input ${errors.joiningDate ? 'input-error' : ''}`}
                  {...register('joiningDate', { required: 'Joining date is required' })} />
                {errors.joiningDate && <p className="error-msg">{errors.joiningDate.message}</p>}
              </div>
              <div className="form-group">
                <label className="label">Department</label>
                <select className="input" {...register('departmentId')}>
                  <option value="">— Select Department —</option>
                  {depts.map(d => <option key={d.id} value={d.id}>{d.name}</option>)}
                </select>
              </div>
              <div className="form-group">
                <label className="label">Designation</label>
                <select className="input" {...register('designationId')}>
                  <option value="">— Select Designation —</option>
                  {desigs.map(d => <option key={d.id} value={d.id}>{d.title}</option>)}
                </select>
              </div>
              <div className="form-group">
                <label className="label">Status</label>
                <select className="input" {...register('status')}>
                  <option value="ACTIVE">Active</option>
                  <option value="INACTIVE">Inactive</option>
                  <option value="ON_LEAVE">On Leave</option>
                  <option value="TERMINATED">Terminated</option>
                </select>
              </div>
              <div className="form-group">
                <label className="label">Annual Leave Quota (days)</label>
                <input type="number" min="0" max="365" className="input"
                  {...register('annualLeaveQuota')} />
              </div>
            </div>
          </div>

          {/* Actions */}
          <div className="flex items-center gap-3 pt-2 border-t border-border-subtle">
            <button type="submit" disabled={loading} className="btn-primary">
              {loading
                ? <><LoadingSpinner /><span>Saving…</span></>
                : <><RiSaveLine size={16} />{isEdit ? 'Update Employee' : 'Create Employee'}</>
              }
            </button>
            <button type="button" onClick={() => navigate(-1)} className="btn-secondary">
              <RiArrowLeftLine size={16} /> Cancel
            </button>
          </div>
        </div>
      </form>
    </div>
  )
}
