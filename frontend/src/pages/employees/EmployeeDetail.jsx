import { useEffect, useState } from 'react'
import { useParams, Link, useNavigate } from 'react-router-dom'
import api from '../../lib/axios'
import PageHeader from '../../components/PageHeader'
import LoadingSpinner from '../../components/LoadingSpinner'
import toast from 'react-hot-toast'
import {
  RiEditLine, RiArrowLeftLine, RiDeleteBinLine,
  RiMailLine, RiPhoneLine, RiMapPinLine, RiCalendarLine,
  RiShieldLine, RiBriefcaseLine, RiTeamLine,
} from 'react-icons/ri'

const STATUS_BADGE = {
  ACTIVE:     'badge-green',
  INACTIVE:   'badge-gray',
  ON_LEAVE:   'badge-yellow',
  TERMINATED: 'badge-red',
}

function InfoRow({ icon: Icon, label, value }) {
  return (
    <div className="flex items-start gap-3 py-3 border-b border-slate-50 last:border-0">
      <div className="w-8 h-8 rounded-lg bg-slate-100 flex items-center justify-center text-slate-400 shrink-0">
        <Icon size={15} />
      </div>
      <div>
        <p className="text-xs text-slate-400 font-medium uppercase tracking-wide">{label}</p>
        <p className="text-sm font-semibold text-slate-800 mt-0.5">{value || <span className="text-slate-300 font-normal">—</span>}</p>
      </div>
    </div>
  )
}

export default function EmployeeDetail() {
  const { id }   = useParams()
  const navigate = useNavigate()
  const [emp,     setEmp]     = useState(null)
  const [loading, setLoading] = useState(true)
  const [deleting, setDeleting] = useState(false)

  useEffect(() => {
    api.get(`/employees/${id}`)
      .then(r => setEmp(r.data))
      .catch(() => toast.error('Employee not found'))
      .finally(() => setLoading(false))
  }, [id])

  const handleDelete = async () => {
    if (!window.confirm(`Delete ${emp?.fullName}? This cannot be undone.`)) return
    setDeleting(true)
    try {
      await api.delete(`/employees/${id}`)
      toast.success('Employee deleted')
      navigate('/employees')
    } catch (err) {
      toast.error(err.response?.data?.message || 'Delete failed')
      setDeleting(false)
    }
  }

  if (loading) return <LoadingSpinner full />
  if (!emp) return (
    <div className="card text-center py-16">
      <p className="text-slate-500">Employee not found.</p>
      <Link to="/employees" className="btn-secondary mt-4 inline-flex">← Back to Employees</Link>
    </div>
  )

  const initials = [emp.firstName?.[0], emp.lastName?.[0]].filter(Boolean).join('').toUpperCase()

  return (
    <div className="space-y-5 animate-fade-in">
      <PageHeader
        title={emp.fullName}
        subtitle={emp.employeeCode}
        action={
          <div className="flex gap-2">
            <Link to="/employees" className="btn-ghost">
              <RiArrowLeftLine size={16} /> Back
            </Link>
            <Link to={`/employees/${id}/edit`} className="btn-secondary">
              <RiEditLine size={16} /> Edit
            </Link>
            <button onClick={handleDelete} disabled={deleting} className="btn-danger py-2 px-3">
              <RiDeleteBinLine size={16} />
            </button>
          </div>
        }
      />

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-5">
        {/* Profile card */}
        <div className="card flex flex-col items-center text-center py-8 gap-4">
          <div className="w-20 h-20 rounded-2xl bg-gradient-to-br from-primary-400 to-violet-600
                          flex items-center justify-center text-white text-2xl font-extrabold shadow-glow">
            {initials}
          </div>
          <div>
            <h2 className="text-xl font-extrabold text-slate-800">{emp.fullName}</h2>
            <p className="text-sm text-slate-500">{emp.designation?.title ?? 'No designation'}</p>
          </div>
          <span className={STATUS_BADGE[emp.status] ?? 'badge-gray'}>{emp.status}</span>
          <div className="w-full mt-2 text-sm text-slate-500">
            <p>{emp.department?.name ?? 'No department'}</p>
          </div>
        </div>

        {/* Personal info */}
        <div className="card">
          <h3 className="font-bold text-slate-800 mb-2 pb-2 border-b border-slate-100">Personal Info</h3>
          <InfoRow icon={RiMailLine}     label="Email"         value={emp.email} />
          <InfoRow icon={RiPhoneLine}    label="Phone"         value={emp.phone} />
          <InfoRow icon={RiMapPinLine}   label="Address"       value={emp.address} />
          <InfoRow icon={RiCalendarLine} label="Date of Birth" value={emp.dateOfBirth} />
        </div>

        {/* Employment info */}
        <div className="card">
          <h3 className="font-bold text-slate-800 mb-2 pb-2 border-b border-slate-100">Employment Info</h3>
          <InfoRow icon={RiTeamLine}      label="Department"    value={emp.department?.name} />
          <InfoRow icon={RiBriefcaseLine} label="Designation"   value={emp.designation?.title} />
          <InfoRow icon={RiCalendarLine}  label="Joined"        value={emp.joiningDate} />
          <InfoRow icon={RiShieldLine}    label="Leave Quota"   value={emp.annualLeaveQuota ? `${emp.annualLeaveQuota} days/year` : null} />
          <InfoRow icon={RiMailLine}      label="User ID"       value={emp.userId ? `#${emp.userId}` : 'Not linked'} />
        </div>
      </div>
    </div>
  )
}
