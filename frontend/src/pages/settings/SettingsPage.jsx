import { useState } from 'react'
import { useAuth } from '../../context/AuthContext'
import { useNavigate } from 'react-router-dom'
import api from '../../lib/axios'
import PageHeader from '../../components/PageHeader'
import LoadingSpinner from '../../components/LoadingSpinner'
import { useForm } from 'react-hook-form'
import toast from 'react-hot-toast'
import {
  RiUserLine, RiMailLine, RiShieldLine, RiLockLine,
  RiLogoutBoxLine, RiIdCardLine, RiCheckLine, RiEyeLine, RiEyeOffLine,
  RiKeyLine,
} from 'react-icons/ri'

function InfoRow({ icon: Icon, label, value, mono }) {
  return (
    <div className="flex items-center gap-3 py-3 border-b border-slate-50 last:border-0 group">
      <div className="w-8 h-8 rounded-lg bg-slate-50 border border-slate-100 flex items-center justify-center text-slate-400 shrink-0
                      group-hover:bg-primary-50 group-hover:border-primary-100 group-hover:text-primary-500 transition-colors duration-150">
        <Icon size={14} />
      </div>
      <div className="flex-1 min-w-0">
        <p className="text-2xs font-bold text-slate-400 uppercase tracking-widest">{label}</p>
        <p className={`text-sm font-semibold text-slate-800 mt-0.5 truncate ${mono ? 'font-mono' : ''}`}>
          {value || <span className="text-slate-300 font-normal italic">Not set</span>}
        </p>
      </div>
    </div>
  )
}

export default function SettingsPage() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [pwdLoading, setPwdLoading] = useState(false)
  const [showPwd, setShowPwd] = useState(false)
  const [pwdSuccess, setPwdSuccess] = useState(false)

  const {
    register,
    handleSubmit,
    reset,
    watch,
    formState: { errors },
  } = useForm()
  const newPwd = watch('newPassword', '')

  const handleLogout = () => {
    logout()
    toast.success('Signed out successfully')
    navigate('/login')
  }

  const changePassword = async (data) => {
    if (data.newPassword !== data.confirmPassword) {
      toast.error('Passwords do not match')
      return
    }
    setPwdLoading(true)
    setPwdSuccess(false)
    try {
      // Uses the new POST /auth/change-password endpoint that verifies current password
      await api.post('/auth/change-password', {
        currentPassword: data.currentPassword,
        newPassword:     data.newPassword,
      })
      setPwdSuccess(true)
      toast.success('Password updated successfully')
      reset()
      setTimeout(() => setPwdSuccess(false), 3000)
    } catch (err) {
      const msg = err.response?.data?.message
      if (msg?.toLowerCase().includes('incorrect')) {
        toast.error('Current password is incorrect')
      } else if (msg?.toLowerCase().includes('differ')) {
        toast.error('New password must be different from current password')
      } else {
        toast.error(msg || 'Failed to update password')
      }
    } finally {
      setPwdLoading(false)
    }
  }

  const initials = [user?.firstName?.[0], user?.lastName?.[0]]
    .filter(Boolean).join('').toUpperCase() || '?'

  const roleStyle = {
    ADMIN:    { badge: 'bg-violet-100 text-violet-700 ring-1 ring-violet-200', bar: 'from-violet-600 to-purple-600' },
    HR:       { badge: 'bg-sky-100 text-sky-700 ring-1 ring-sky-200',           bar: 'from-sky-500 to-blue-600' },
    EMPLOYEE: { badge: 'bg-emerald-100 text-emerald-700 ring-1 ring-emerald-200', bar: 'from-emerald-500 to-teal-600' },
  }[user?.role] || { badge: 'bg-slate-100 text-slate-600', bar: 'from-primary-500 to-violet-600' }

  return (
    <div className="space-y-6 animate-fade-up">
      <PageHeader
        title="Settings"
        subtitle="Manage your account preferences and security"
      />

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-5">

        {/* ── Profile card ───────────────────────────────── */}
        <div className="card flex flex-col items-center text-center py-8 gap-4 relative overflow-hidden">
          {/* Subtle top accent */}
          <div className={`absolute inset-x-0 top-0 h-1 bg-gradient-to-r ${roleStyle.bar}`} />

          {/* Avatar */}
          <div className={`w-20 h-20 rounded-2xl bg-gradient-to-br ${roleStyle.bar}
                          flex items-center justify-center text-white text-2xl font-extrabold
                          shadow-glow ring-4 ring-white mt-2`}>
            {initials}
          </div>

          <div>
            <h3 className="text-xl font-extrabold text-slate-900">
              {user?.firstName} {user?.lastName}
            </h3>
            <p className="text-sm text-slate-400 mt-0.5">{user?.email}</p>
          </div>

          <span className={`inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold ${roleStyle.badge}`}>
            <RiShieldLine size={11} />
            {user?.role}
          </span>

          {user?.employeeId && (
            <p className="text-xs text-slate-400 bg-slate-50 rounded-lg px-3 py-1.5 font-mono border border-slate-100">
              Employee #{user.employeeId}
            </p>
          )}

          <div className="w-full mt-2 border-t border-slate-100 pt-4">
            <button
              onClick={handleLogout}
              className="w-full flex items-center justify-center gap-2 px-4 py-2.5 rounded-xl
                         text-sm font-semibold text-red-600
                         bg-red-50 hover:bg-red-100 border border-red-100 hover:border-red-200
                         transition-all duration-150 active:scale-[0.98]"
            >
              <RiLogoutBoxLine size={16} />
              Sign Out
            </button>
          </div>
        </div>

        {/* ── Account info ───────────────────────────────── */}
        <div className="card">
          <div className="flex items-center gap-2 mb-1 pb-3 border-b border-slate-100">
            <div className="w-7 h-7 rounded-lg bg-primary-50 flex items-center justify-center">
              <RiUserLine size={14} className="text-primary-600" />
            </div>
            <h4 className="font-bold text-slate-800">Account Information</h4>
          </div>
          <div className="pt-1">
            <InfoRow icon={RiUserLine}   label="First Name"  value={user?.firstName} />
            <InfoRow icon={RiUserLine}   label="Last Name"   value={user?.lastName} />
            <InfoRow icon={RiMailLine}   label="Email"       value={user?.email} />
            <InfoRow icon={RiShieldLine} label="Role"        value={user?.role} />
            <InfoRow icon={RiIdCardLine} label="User ID"     value={user?.id ? `#${user.id}` : null} mono />
            <InfoRow icon={RiIdCardLine} label="Employee ID" value={user?.employeeId ? `#${user.employeeId}` : 'Not linked'} mono />
          </div>
        </div>

        {/* ── Change password ─────────────────────────────── */}
        <div className="card">
          <div className="flex items-center gap-2 mb-1 pb-3 border-b border-slate-100">
            <div className="w-7 h-7 rounded-lg bg-amber-50 flex items-center justify-center">
              <RiKeyLine size={14} className="text-amber-600" />
            </div>
            <h4 className="font-bold text-slate-800">Change Password</h4>
          </div>

          <form onSubmit={handleSubmit(changePassword)} className="space-y-4 pt-3">
            {/* Current password */}
            <div className="form-group">
              <label className="label">Current Password</label>
              <div className="relative">
                <RiLockLine size={15} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400 pointer-events-none" />
                <input
                  type={showPwd ? 'text' : 'password'}
                  placeholder="Your current password"
                  className={`input pl-9 ${errors.currentPassword ? 'input-error' : ''}`}
                  {...register('currentPassword', { required: 'Current password required' })}
                />
              </div>
              {errors.currentPassword && (
                <p className="error-msg">{errors.currentPassword.message}</p>
              )}
            </div>

            {/* New password */}
            <div className="form-group">
              <label className="label">New Password</label>
              <div className="relative">
                <RiLockLine size={15} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400 pointer-events-none" />
                <input
                  type={showPwd ? 'text' : 'password'}
                  placeholder="Minimum 8 characters"
                  className={`input pl-9 pr-10 ${errors.newPassword ? 'input-error' : ''}`}
                  {...register('newPassword', {
                    required: 'New password is required',
                    minLength: { value: 8, message: 'Minimum 8 characters' },
                  })}
                />
                <button
                  type="button"
                  onClick={() => setShowPwd(v => !v)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 transition-colors"
                >
                  {showPwd ? <RiEyeOffLine size={15} /> : <RiEyeLine size={15} />}
                </button>
              </div>
              {errors.newPassword && (
                <p className="error-msg">{errors.newPassword.message}</p>
              )}
            </div>

            {/* Confirm password */}
            <div className="form-group">
              <label className="label">Confirm New Password</label>
              <div className="relative">
                <RiLockLine size={15} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400 pointer-events-none" />
                <input
                  type={showPwd ? 'text' : 'password'}
                  placeholder="Repeat new password"
                  className={`input pl-9 ${errors.confirmPassword ? 'input-error' : ''}`}
                  {...register('confirmPassword', {
                    required: 'Please confirm password',
                    validate: v => v === newPwd || 'Passwords do not match',
                  })}
                />
              </div>
              {errors.confirmPassword && (
                <p className="error-msg">{errors.confirmPassword.message}</p>
              )}
            </div>

            <button
              type="submit"
              disabled={pwdLoading}
              className={`btn-primary w-full transition-all duration-300 ${
                pwdSuccess ? 'from-emerald-500 to-emerald-600 hover:from-emerald-500 hover:to-emerald-600' : ''
              }`}
            >
              {pwdLoading ? (
                <><LoadingSpinner /><span>Updating…</span></>
              ) : pwdSuccess ? (
                <><RiCheckLine size={16} /><span>Password Updated!</span></>
              ) : (
                <><RiKeyLine size={16} /><span>Update Password</span></>
              )}
            </button>
          </form>
        </div>
      </div>
    </div>
  )
}
