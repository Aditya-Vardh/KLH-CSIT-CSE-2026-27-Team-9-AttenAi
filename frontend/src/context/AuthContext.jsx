import { createContext, useContext, useState, useEffect, useCallback } from 'react'
import api from '../lib/axios'

const AuthContext = createContext(null)

/**
 * Stored user shape:
 *   { id, email, firstName, lastName, role, employeeId? }
 *
 * `id`         = auth-service User.id  (Long)
 * `employeeId` = employee-service Employee.id  (Long, null for ADMIN/HR who have no employee record)
 *
 * We eagerly resolve employeeId after login/register and cache it in localStorage
 * so every page can use it without a round-trip.
 */
export function AuthProvider({ children }) {
  const [user, setUser]       = useState(null)
  const [loading, setLoading] = useState(true)

  // Rehydrate from localStorage on mount
  useEffect(() => {
    const stored = localStorage.getItem('user')
    if (stored) {
      try { setUser(JSON.parse(stored)) } catch { localStorage.clear() }
    }
    setLoading(false)
  }, [])

  // After auth, try to resolve the employee record and attach employeeId
  const enrichWithEmployeeId = useCallback(async (userSummary) => {
    try {
      const { data } = await api.get(`/employees/by-user/${userSummary.id}`)
      return { ...userSummary, employeeId: data.id }
    } catch {
      // ADMIN or HR may not have an employee record — that's fine
      return { ...userSummary, employeeId: null }
    }
  }, [])

  const login = useCallback(async (email, password) => {
    const { data } = await api.post('/auth/login', { email, password })
    // Store token FIRST so the /employees/by-user call is authenticated
    localStorage.setItem('accessToken', data.accessToken)
    localStorage.setItem('refreshToken', data.refreshToken)

    // Now enrich with employeeId (uses the stored token via axios interceptor)
    const enriched = await enrichWithEmployeeId(data.user)
    localStorage.setItem('user', JSON.stringify(enriched))
    setUser(enriched)
    return enriched
  }, [enrichWithEmployeeId])

  const register = useCallback(async (payload) => {
    const { data } = await api.post('/auth/register', payload)
    // Store token FIRST so the /employees/by-user call is authenticated
    localStorage.setItem('accessToken', data.accessToken)
    localStorage.setItem('refreshToken', data.refreshToken)

    // Enrich with employeeId (newly registered users typically won't have one yet)
    const enriched = await enrichWithEmployeeId(data.user)
    localStorage.setItem('user', JSON.stringify(enriched))
    setUser(enriched)
    return enriched
  }, [enrichWithEmployeeId])

  const logout = useCallback(() => {
    localStorage.clear()
    setUser(null)
  }, [])

  /**
   * Call this after creating an employee record linked to the current user,
   * so that employeeId becomes available without re-login.
   */
  const refreshEmployeeId = useCallback(async () => {
    if (!user) return
    const enriched = await enrichWithEmployeeId(user)
    localStorage.setItem('user', JSON.stringify(enriched))
    setUser(enriched)
    return enriched
  }, [user, enrichWithEmployeeId])

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout, refreshEmployeeId }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => useContext(AuthContext)
