import { useForm } from 'react-hook-form'
import { useAuth } from '../../context/AuthContext'
import { useNavigate, Link } from 'react-router-dom'
import toast from 'react-hot-toast'
import { useState } from 'react'
import { RiMailLine, RiLockLine, RiEyeLine, RiEyeOffLine } from 'react-icons/ri'
import LoadingSpinner from '../../components/LoadingSpinner'

export default function LoginPage() {
  const { login }   = useAuth()
  const navigate    = useNavigate()
  const [loading, setLoading]   = useState(false)
  const [showPwd, setShowPwd]   = useState(false)
  const { register, handleSubmit, formState: { errors } } = useForm()

  const onSubmit = async (data) => {
    setLoading(true)
    try {
      const user = await login(data.email, data.password)
      toast.success(`Welcome back, ${user.firstName}!`)
      navigate('/dashboard')
    } catch (err) {
      toast.error(err.response?.data?.message || 'Invalid email or password')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div>
      <div className="mb-6">
        <h2 className="text-3xl font-bold text-text">Sign in</h2>
        <p className="text-sm font-normal text-text-muted mt-1">
          Welcome back — enter your credentials below
        </p>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <div className="form-group">
          <label className="label">Email address</label>
          <div className="relative">
            <RiMailLine className="absolute left-3.5 top-1/2 -translate-y-1/2 text-text-faint" size={15} />
            <input
              type="email"
              className={`input pl-10 ${errors.email ? 'input-error' : ''}`}
              placeholder="you@company.com"
              {...register('email', { required: 'Email is required' })}
            />
          </div>
          {errors.email && <p className="error-msg">{errors.email.message}</p>}
        </div>

        <div className="form-group">
          <label className="label">Password</label>
          <div className="relative">
            <RiLockLine className="absolute left-3.5 top-1/2 -translate-y-1/2 text-text-faint" size={15} />
            <input
              type={showPwd ? 'text' : 'password'}
              className={`input pl-10 pr-10 ${errors.password ? 'input-error' : ''}`}
              placeholder="••••••••"
              {...register('password', { required: 'Password is required' })}
            />
            <button
              type="button"
              onClick={() => setShowPwd(v => !v)}
              className="absolute right-3.5 top-1/2 -translate-y-1/2 text-text-faint hover:text-text transition-colors"
            >
              {showPwd ? <RiEyeOffLine size={15} /> : <RiEyeLine size={15} />}
            </button>
          </div>
          {errors.password && <p className="error-msg">{errors.password.message}</p>}
        </div>

        <button
          type="submit"
          disabled={loading}
          className="btn-primary w-full py-3 text-base mt-2"
        >
          {loading ? <><LoadingSpinner /><span>Signing in…</span></> : 'Sign In'}
        </button>
      </form>

      <p className="text-center text-sm font-normal text-text-muted mt-5">
        Don&apos;t have an account?{' '}
        <Link to="/register" className="text-primary font-bold hover:brightness-125 transition-all">
          Create account
        </Link>
      </p>
    </div>
  )
}
