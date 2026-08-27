import { useEffect, useState, useCallback } from 'react'
import api from '../../lib/axios'
import PageHeader from '../../components/PageHeader'
import EmptyState from '../../components/EmptyState'
import { SkeletonTable } from '../../components/SkeletonCard'
import toast from 'react-hot-toast'
import { RiFileListLine, RiRefreshLine, RiSearchLine } from 'react-icons/ri'

const ACTION_BADGE = {
  EMPLOYEE_CREATED:    'badge-green',
  EMPLOYEE_UPDATED:    'badge-blue',
  EMPLOYEE_DELETED:    'badge-red',
  DEPARTMENT_CREATED:  'badge-green',
  DEPARTMENT_UPDATED:  'badge-blue',
  LEAVE_APPROVED:      'badge-green',
  LEAVE_REJECTED:      'badge-red',
}

function fmtDate(iso) {
  if (!iso) return '—'
  return new Date(iso).toLocaleString('en-US', {
    month: 'short', day: 'numeric', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
  })
}

export default function AuditLogsPage() {
  const [logs,       setLogs]       = useState([])
  const [total,      setTotal]      = useState(0)
  const [page,       setPage]       = useState(0)
  const [totalPages, setTotalPages] = useState(1)
  const [loading,    setLoading]    = useState(true)

  // Filters
  const [action,     setAction]     = useState('')
  const [entityType, setEntityType] = useState('')
  const [actor,      setActor]      = useState('')

  const load = useCallback((pg = 0) => {
    setLoading(true)
    api.get('/audit', {
      params: {
        action:     action     || undefined,
        entityType: entityType || undefined,
        actor:      actor      || undefined,
        page: pg,
        size: 25,
      },
    })
      .then(r => {
        setLogs(r.data.content ?? [])
        setTotal(r.data.totalElements ?? 0)
        setTotalPages(r.data.totalPages ?? 1)
        setPage(pg)
      })
      .catch(err => {
        if (err.response?.status === 403) {
          toast.error('Audit logs are restricted to Admins')
        } else {
          toast.error('Failed to load audit logs')
        }
      })
      .finally(() => setLoading(false))
  }, [action, entityType, actor])

  useEffect(() => { load(0) }, [load])

  const handleSearch = (e) => { e.preventDefault(); load(0) }

  return (
    <div className="space-y-5 animate-fade-up">
      <PageHeader
        title="Audit Logs"
        subtitle={`${total} events recorded`}
        action={
          <button onClick={() => load(page)} className="btn-icon" title="Refresh">
            <RiRefreshLine size={17} />
          </button>
        }
      />

      {/* Filters */}
      <form onSubmit={handleSearch} className="card flex flex-wrap gap-3 items-end">
        <div className="form-group flex-1 min-w-[160px]">
          <label className="label">Action keyword</label>
          <div className="relative">
            <RiSearchLine className="absolute left-3.5 top-1/2 -translate-y-1/2 text-text-faint" size={14} />
            <input
              className="input pl-9"
              placeholder="e.g. EMPLOYEE"
              value={action}
              onChange={e => setAction(e.target.value)}
            />
          </div>
        </div>
        <div className="form-group flex-1 min-w-[140px]">
          <label className="label">Entity type</label>
          <input
            className="input"
            placeholder="e.g. Employee"
            value={entityType}
            onChange={e => setEntityType(e.target.value)}
          />
        </div>
        <div className="form-group flex-1 min-w-[180px]">
          <label className="label">Actor email</label>
          <input
            className="input"
            placeholder="admin@…"
            value={actor}
            onChange={e => setActor(e.target.value)}
          />
        </div>
        <button type="submit" className="btn-primary">
          <RiSearchLine size={15} /> Search
        </button>
      </form>

      {/* Table */}
      <div className="card p-0 overflow-hidden">
        {loading ? (
          <div className="p-6">
            <SkeletonTable rows={10} cols={5} />
          </div>
        ) : logs.length === 0 ? (
          <div className="p-6">
            <EmptyState
              title="No audit events found"
              desc="Audit events are recorded automatically as employees are created, updated, and leave requests are reviewed."
              icon={RiFileListLine}
            />
          </div>
        ) : (
          <>
            <div className="table-container rounded-none border-0">
              <table className="table">
                <thead>
                  <tr>
                    <th>Action</th>
                    <th>Entity</th>
                    <th className="hidden md:table-cell">Actor</th>
                    <th className="hidden sm:table-cell">Role</th>
                    <th>When</th>
                    <th className="hidden lg:table-cell">Details</th>
                  </tr>
                </thead>
                <tbody>
                  {logs.map(log => (
                    <tr key={log.id}>
                      <td>
                        <span className={ACTION_BADGE[log.action] ?? 'badge-muted'}>
                          {log.action?.replace(/_/g, ' ')}
                        </span>
                      </td>
                      <td className="text-sm">
                        <span className="font-bold text-text">{log.entityType ?? '—'}</span>
                        {log.entityId && (
                          <span className="text-text-faint ml-1">#{log.entityId}</span>
                        )}
                      </td>
                      <td className="hidden md:table-cell text-sm text-text-muted truncate max-w-[160px]">
                        {log.actorEmail}
                      </td>
                      <td className="hidden sm:table-cell">
                        {log.actorRole
                          ? <span className="badge-warning">{log.actorRole}</span>
                          : <span className="text-text-faint">—</span>
                        }
                      </td>
                      <td className="text-sm text-text-muted whitespace-nowrap">
                        {fmtDate(log.createdAt)}
                      </td>
                      <td className="hidden lg:table-cell text-sm text-text-faint max-w-[200px] truncate font-mono">
                        {log.metadata ?? '—'}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {/* Pagination */}
            <div className="flex items-center justify-between px-6 py-3 border-t border-border-subtle">
              <span className="text-sm text-text-muted">
                Page {page + 1} of {totalPages} · {total} events
              </span>
              <div className="flex gap-2">
                <button
                  disabled={page === 0}
                  onClick={() => load(page - 1)}
                  className="btn-secondary py-1.5 px-3 text-sm disabled:opacity-40"
                >
                  ← Prev
                </button>
                <button
                  disabled={page >= totalPages - 1}
                  onClick={() => load(page + 1)}
                  className="btn-secondary py-1.5 px-3 text-sm disabled:opacity-40"
                >
                  Next →
                </button>
              </div>
            </div>
          </>
        )}
      </div>
    </div>
  )
}
