import { useEffect, useState, useCallback } from 'react'
import { Link } from 'react-router-dom'
import api from '../../lib/axios'
import PageHeader from '../../components/PageHeader'
import EmptyState from '../../components/EmptyState'
import { SkeletonTable } from '../../components/SkeletonCard'
import toast from 'react-hot-toast'
import { RiTeamLine, RiSearchLine, RiEditLine, RiEyeLine, RiDeleteBinLine, RiAddLine, RiRefreshLine } from 'react-icons/ri'

const STATUS_BADGE = { ACTIVE:'badge-green', INACTIVE:'badge-gray', ON_LEAVE:'badge-yellow', TERMINATED:'badge-red' }

export default function EmployeesPage() {
  const [data,     setData]     = useState({ content: [], totalElements: 0, totalPages: 1, last: true })
  const [search,   setSearch]   = useState('')
  const [page,     setPage]     = useState(0)
  const [loading,  setLoading]  = useState(true)
  const [deleting, setDeleting] = useState(null)

  const load = useCallback(() => {
    setLoading(true)
    api.get('/employees', { params: { search: search || undefined, page, size: 15 } })
      .then(r => setData(r.data))
      .catch(() => toast.error('Failed to load employees'))
      .finally(() => setLoading(false))
  }, [search, page])

  useEffect(() => { load() }, [load])

  const handleDelete = async (emp) => {
    if (!window.confirm(`Delete ${emp.fullName}? This cannot be undone.`)) return
    setDeleting(emp.id)
    try { await api.delete(`/employees/${emp.id}`); toast.success(`${emp.fullName} deleted`); load() }
    catch (err) { toast.error(err.response?.data?.message || 'Delete failed') }
    finally { setDeleting(null) }
  }

  return (
    <div className="space-y-4 animate-fade-up">
      <PageHeader title="Employees" subtitle={`${data.totalElements} total`}
        action={<Link to="/employees/new" className="btn-primary"><RiAddLine size={16} />Add Employee</Link>} />

      <div className="card">
        <div className="flex items-center gap-3 mb-5">
          <div className="relative flex-1 max-w-sm">
            <RiSearchLine className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400" size={15} />
            <input className="input pl-10" placeholder="Search name, email, code…"
              value={search} onChange={e => { setSearch(e.target.value); setPage(0) }} />
          </div>
          <button onClick={load} className="btn-icon" title="Refresh"><RiRefreshLine size={17} /></button>
        </div>

        {loading ? <SkeletonTable rows={8} cols={6} /> :
          data.content.length === 0 ? (
            <EmptyState title="No employees found" desc={search ? `No results for "${search}"` : 'Add your first employee.'}
              icon={RiTeamLine} action={<Link to="/employees/new" className="btn-primary"><RiAddLine size={14} />Add Employee</Link>} />
          ) : (
            <>
              <div className="table-container">
                <table className="table">
                  <thead><tr>
                    <th>Code</th><th>Name</th>
                    <th className="hidden sm:table-cell">Department</th>
                    <th className="hidden md:table-cell">Designation</th>
                    <th>Status</th>
                    <th className="hidden md:table-cell">Joined</th>
                    <th>Actions</th>
                  </tr></thead>
                  <tbody>
                    {data.content.map(emp => (
                      <tr key={emp.id}>
                        <td><span className="font-mono text-xs bg-slate-100 text-slate-600 px-2 py-1 rounded-lg">{emp.employeeCode}</span></td>
                        <td>
                          <div className="flex items-center gap-2.5">
                            <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-primary-400 to-violet-500
                                            flex items-center justify-center text-white text-xs font-bold shrink-0">
                              {emp.firstName?.[0]}{emp.lastName?.[0]}
                            </div>
                            <div>
                              <p className="font-semibold text-slate-800">{emp.fullName}</p>
                              <p className="text-xs text-slate-400">{emp.email}</p>
                            </div>
                          </div>
                        </td>
                        <td className="hidden sm:table-cell text-slate-500">{emp.department?.name ?? <span className="text-slate-300">—</span>}</td>
                        <td className="hidden md:table-cell text-slate-500">{emp.designation?.title ?? <span className="text-slate-300">—</span>}</td>
                        <td><span className={STATUS_BADGE[emp.status] ?? 'badge-gray'}>{emp.status}</span></td>
                        <td className="hidden md:table-cell text-xs text-slate-500">{emp.joiningDate ?? '—'}</td>
                        <td>
                          <div className="flex gap-1">
                            <Link to={`/employees/${emp.id}`} className="btn-icon text-slate-400 hover:text-primary-600 hover:bg-primary-50" title="View"><RiEyeLine size={15} /></Link>
                            <Link to={`/employees/${emp.id}/edit`} className="btn-icon text-slate-400 hover:text-primary-600 hover:bg-primary-50" title="Edit"><RiEditLine size={15} /></Link>
                            <button onClick={() => handleDelete(emp)} disabled={deleting === emp.id}
                              className="btn-icon text-slate-400 hover:text-red-500 hover:bg-red-50" title="Delete">
                              <RiDeleteBinLine size={15} />
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
              <div className="flex items-center justify-between mt-4 pt-4 border-t border-slate-100">
                <span className="text-sm text-slate-500">Page {page+1} of {data.totalPages ?? 1} · {data.totalElements} records</span>
                <div className="flex gap-2">
                  <button disabled={page === 0} onClick={() => setPage(p => p-1)} className="btn-secondary py-1.5 px-3 text-xs disabled:opacity-40">← Prev</button>
                  <button disabled={data.last} onClick={() => setPage(p => p+1)} className="btn-secondary py-1.5 px-3 text-xs disabled:opacity-40">Next →</button>
                </div>
              </div>
            </>
          )
        }
      </div>
    </div>
  )
}
