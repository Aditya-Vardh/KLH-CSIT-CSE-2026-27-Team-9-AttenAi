import { useAuth } from '../../context/AuthContext'
import { Navigate } from 'react-router-dom'

export default function Dashboard() {
  const { user } = useAuth()
  if (user?.role === 'ADMIN') return <Navigate to="/admin" replace />
  if (user?.role === 'HR')    return <Navigate to="/hr"    replace />
  return <Navigate to="/my" replace />
}
