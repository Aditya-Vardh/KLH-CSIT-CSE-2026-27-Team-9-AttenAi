import { Link } from 'react-router-dom'
import { RiHome2Line, RiErrorWarningLine } from 'react-icons/ri'

export default function NotFoundPage() {
  return (
    <div className="min-h-screen bg-background flex items-center justify-center p-6">
      <div className="text-center">
        <div className="w-20 h-20 rounded-2xl bg-primary-10 border border-primary-20
                        flex items-center justify-center mx-auto mb-6">
          <RiErrorWarningLine size={36} className="text-primary opacity-60" />
        </div>
        <h1 className="text-4xl font-bold text-primary opacity-30 tracking-tight">404</h1>
        <h2 className="text-2xl font-bold text-text mt-2">Page not found</h2>
        <p className="text-base font-normal text-text-muted mt-2 max-w-xs mx-auto">
          The page you&apos;re looking for doesn&apos;t exist or has been moved.
        </p>
        <Link to="/dashboard" className="btn-primary mt-8 inline-flex">
          <RiHome2Line size={16} /> Back to Dashboard
        </Link>
      </div>
    </div>
  )
}
