import { Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider, useAuth } from './context/AuthContext'

// Layouts
import AuthLayout  from './layouts/AuthLayout'
import DashLayout  from './layouts/DashLayout'

// Public
import LandingPage   from './pages/landing/LandingPage'
import NotFoundPage  from './pages/NotFoundPage'

// Auth pages
import LoginPage    from './pages/auth/LoginPage'
import RegisterPage from './pages/auth/RegisterPage'

// Dashboard pages
import Dashboard         from './pages/dashboard/Dashboard'
import AdminDashboard    from './pages/dashboard/AdminDashboard'
import HrDashboard       from './pages/dashboard/HrDashboard'
import EmployeeDashboard from './pages/dashboard/EmployeeDashboard'

// Feature pages
import EmployeesPage     from './pages/employees/EmployeesPage'
import EmployeeForm      from './pages/employees/EmployeeForm'
import EmployeeDetail    from './pages/employees/EmployeeDetail'
import DepartmentsPage   from './pages/departments/DepartmentsPage'
import AttendancePage    from './pages/attendance/AttendancePage'
import CheckInOutPage    from './pages/attendance/CheckInOutPage'
import LeavePage         from './pages/leave/LeavePage'
import LeaveFormPage     from './pages/leave/LeaveFormPage'
import ReportsPage       from './pages/reports/ReportsPage'
import NotificationsPage from './pages/notifications/NotificationsPage'
import AiAssistantPage   from './pages/ai/AiAssistantPage'
import AuditLogsPage     from './pages/audit/AuditLogsPage'
import SettingsPage      from './pages/settings/SettingsPage'

/* ── Protected route wrapper ─────────────────────────────── */
function ProtectedRoute({ children, roles }) {
  const { user, loading } = useAuth()

  if (loading) return (
    <div className="flex items-center justify-center h-screen bg-slate-50">
      <div className="flex flex-col items-center gap-3">
        <div className="w-10 h-10 rounded-full border-[3px] border-primary-200 border-t-primary-600 animate-spin" />
        <p className="text-sm text-slate-400 animate-pulse-soft">Loading…</p>
      </div>
    </div>
  )

  if (!user) return <Navigate to="/login" replace />

  if (roles && !roles.includes(user.role)) {
    if (user.role === 'ADMIN')   return <Navigate to="/admin" replace />
    if (user.role === 'HR')      return <Navigate to="/hr"    replace />
    if (user.role === 'MANAGER') return <Navigate to="/my"    replace />
    return <Navigate to="/my" replace />
  }

  return children
}

export default function App() {
  return (
    <AuthProvider>
      <Routes>

        {/* ── Public landing ─────────────────────────── */}
        <Route path="/" element={<LandingPage />} />

        {/* ── Auth (login / register) ────────────────── */}
        <Route element={<AuthLayout />}>
          <Route path="/login"    element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
        </Route>

        {/* ── Protected app shell ────────────────────── */}
        <Route element={<ProtectedRoute><DashLayout /></ProtectedRoute>}>
          <Route path="/dashboard" element={<Dashboard />} />

          {/* Role-specific dashboards */}
          <Route path="/admin" element={
            <ProtectedRoute roles={['ADMIN']}><AdminDashboard /></ProtectedRoute>
          } />
          <Route path="/hr" element={
            <ProtectedRoute roles={['ADMIN', 'HR']}><HrDashboard /></ProtectedRoute>
          } />
          <Route path="/my" element={<EmployeeDashboard />} />

          {/* Employee management */}
          <Route path="/employees" element={
            <ProtectedRoute roles={['ADMIN', 'HR']}><EmployeesPage /></ProtectedRoute>
          } />
          <Route path="/employees/new" element={
            <ProtectedRoute roles={['ADMIN', 'HR']}><EmployeeForm /></ProtectedRoute>
          } />
          <Route path="/employees/:id"      element={<EmployeeDetail />} />
          <Route path="/employees/:id/edit" element={
            <ProtectedRoute roles={['ADMIN', 'HR']}><EmployeeForm /></ProtectedRoute>
          } />

          {/* Departments */}
          <Route path="/departments" element={
            <ProtectedRoute roles={['ADMIN', 'HR']}><DepartmentsPage /></ProtectedRoute>
          } />

          {/* Attendance */}
          <Route path="/attendance"         element={<AttendancePage />} />
          <Route path="/attendance/checkin" element={<CheckInOutPage />} />

          {/* Leave */}
          <Route path="/leave"     element={<LeavePage />} />
          <Route path="/leave/new" element={<LeaveFormPage />} />

          {/* Other features */}
          <Route path="/reports" element={
            <ProtectedRoute roles={['ADMIN', 'HR']}><ReportsPage /></ProtectedRoute>
          } />
          <Route path="/notifications" element={<NotificationsPage />} />
          <Route path="/ai"            element={<AiAssistantPage />} />
          <Route path="/audit"         element={
            <ProtectedRoute roles={['ADMIN']}><AuditLogsPage /></ProtectedRoute>
          } />
          <Route path="/settings"      element={<SettingsPage />} />
        </Route>

        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </AuthProvider>
  )
}
