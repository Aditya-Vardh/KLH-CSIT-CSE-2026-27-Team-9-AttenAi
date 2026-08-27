import { useForm } from 'react-hook-form'
import { useAuth } from '../../context/AuthContext'
import { useNavigate, Link } from 'react-router-dom'
import toast from 'react-hot-toast'
import { useState } from 'react'
import { RiMailLine, RiLockLine, RiUserLine, RiShieldLine } from 'react-icons/ri'
import LoadingSpinner from '../../components/LoadingSpinner'

export default function RegisterPage() {
  const { register: authRegister } = useAuth()
  const navigate  = useNavigate()
  const [loading, setLoading] = useState(false)
  const { register, handleSubmit, formState: { errors } } = useForm()

  const onSubmit = async (data) => {
    setLoading(true)
    try {
      const user = await authRegister({ ...data, role: data.role || 'EMPLOYEE' })
      toast.success(`Welcome, ${user.firstName}!`)
      navigate('/dashboard')
    } catch (err) {
      toast.error(err.response?.data?.message || 'Registration failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div>
      <div className="mb-5">
        <h2 className="text-3xl font-bold text-text">Create account</h2>
        <p className="text-sm font-normal text-text-muted mt-1">Get started with AttendAI today</p>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <div className="grid grid-cols-2 gap-3">
          <div className="form-group">
            <label className="label">First name</label>
            <div className="relative">
              <RiUserLine className="absolute left-3.5 top-1/2 -translate-y-1/2 text-text-faint" size={14} />
              <input className={`input pl-9 ${errors.firstName ? 'input-error' : ''}`}
                placeholder="John"
                {...register('firstName', { required: 'Required' })} />
            </div>
            {errors.firstName && <p className="error-msg">{errors.firstName.message}</p>}
          </div>
          <div className="form-group">
            <label className="label">Last name</label>
            <input className={`input ${errors.lastName ? 'input-error' : ''}`}
              placeholder="Doe"
              {...register('lastName', { required: 'Required' })} />
            {errors.lastName && <p className="error-msg">{errors.lastName.message}</p>}
          </div>
        </div>

        <div className="form-group">
          <label className="label">Email address</label>
          <div className="relative">
            <RiMailLine className="absolute left-3.5 top-1/2 -translate-y-1/2 text-text-faint" size={14} />
            <input type="email" className={`input pl-9 ${errors.email ? 'input-error' : ''}`}
              placeholder="you@company.com"
              {...register('email', { required: 'Email is required' })} />
          </div>
          {errors.email && <p className="error-msg">{errors.email.message}</p>}
        </div>

        <div className="form-group">
          <label className="label">Password</label>
          <div className="relative">
            <RiLockLine className="absolute left-3.5 top-1/2 -translate-y-1/2 text-text-faint" size={14} />
            <input type="password" className={`input pl-9 ${errors.password ? 'input-error' : ''}`}
              placeholder="Minimum 8 characters"
              {...register('password', {
                required: 'Password is required',
                minLength: { value: 8, message: 'At least 8 characters' },
              })} />
          </div>
          {errors.password && <p className="error-msg">{errors.password.message}</p>}
        </div>

        <div className="form-group">
          <label className="label">Role</label>
          <div className="relative">
            <RiShieldLine className="absolute left-3.5 top-1/2 -translate-y-1/2 text-text-faint" size={14} />
            <select className="input pl-9" {...register('role')}>
              <option value="EMPLOYEE">Employee</option>
              <option value="HR">HR</option>
              <option value="ADMIN">Admin</option>
            </select>
          </div>
        </div>

        <button type="submit" disabled={loading}
          className="btn-primary w-full py-3 text-base mt-2">
          {loading ? <><LoadingSpinner /><span>Creating…</span></> : 'Create Account'}
        </button>
      </form>

      <p className="text-center text-sm font-normal text-text-muted mt-5">
        Already have an account?{' '}
        <Link to="/login" className="text-primary font-bold hover:brightness-125 transition-all">
          Sign in
        </Link>
      </p>
    </div>
  )
}
