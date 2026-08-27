import { useEffect, useState, useCallback } from 'react'
import api from '../../lib/axios'
import PageHeader from '../../components/PageHeader'
import EmptyState from '../../components/EmptyState'
import { SkeletonTable } from '../../components/SkeletonCard'
import toast from 'react-hot-toast'
import { useForm } from 'react-hook-form'
import LoadingSpinner from '../../components/LoadingSpinner'
import {
  RiBuilding2Line, RiAddLine, RiEditLine, RiSaveLine, RiCloseLine,
  RiRefreshLine,
} from 'react-icons/ri'

export default function DepartmentsPage() {
  const [departments, setDepartments] = useState([])
  const [total,       setTotal]       = useState(0)
  const [loading,     setLoading]     = useState(true)
  const [saving,      setSaving]      = useState(false)
  const [editId,      setEditId]      = useState(null)  // null = creating new
  const [showForm,    setShowForm]    = useState(false)

  const { register, handleSubmit, reset, formState: { errors } } = useForm()

  const load = useCallback(() => {
    setLoading(true)
    api.get('/departments', { params: { size: 100 } })
      .then(r => {
        setDepartments(r.data.content ?? [])
        setTotal(r.data.totalElements ?? 0)
      })
      .catch(() => toast.error('Failed to load departments'))
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => { load() }, [load])

  const openCreate = () => {
    setEditId(null)
    reset({ name: '', description: '' })
    setShowForm(true)
  }

  const openEdit = (dept) => {
    setEditId(dept.id)
    reset({ name: dept.name, description: dept.description ?? '' })
    setShowForm(true)
  }

  const onSubmit = async (data) => {
    setSaving(true)
    try {
      if (editId) {
        await api.put(`/departments/${editId}`, data)
        toast.success('Department updated')
      } else {
        await api.post('/departments', data)
        toast.success('Department created')
      }
      setShowForm(false)
      load()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Save failed')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="space-y-5 animate-fade-up">
      <PageHeader
        title="Departments"
        subtitle={`${total} department${total !== 1 ? 's' : ''}`}
        action={
          <div className="flex gap-2">
            <button onClick={load} className="btn-icon" title="Refresh">
              <RiRefreshLine size={17} />
            </button>
            <button onClick={openCreate} className="btn-primary">
              <RiAddLine size={16} /> New Department
            </button>
          </div>
        }
      />

      {/* Inline form */}
      {showForm && (
        <div className="card max-w-lg">
          <h4 className="text-xl font-bold text-text mb-4">
            {editId ? 'Edit Department' : 'New Department'}
          </h4>
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div className="form-group">
              <label className="label">Name *</label>
              <input
                className={`input ${errors.name ? 'input-error' : ''}`}
                placeholder="e.g. Engineering"
                {...register('name', { required: 'Department name is required' })}
              />
              {errors.name && <p className="error-msg">{errors.name.message}</p>}
            </div>
            <div className="form-group">
              <label className="label">Description</label>
              <input
                className="input"
                placeholder="Optional description"
                {...register('description')}
              />
            </div>
            <div className="flex gap-3 pt-1 border-t border-border-subtle">
              <button type="submit" disabled={saving} className="btn-primary">
                {saving
                  ? <><LoadingSpinner /><span>Saving…</span></>
                  : <><RiSaveLine size={15} />{editId ? 'Update' : 'Create'}</>
                }
              </button>
              <button
                type="button"
                onClick={() => setShowForm(false)}
                className="btn-secondary"
              >
                <RiCloseLine size={15} /> Cancel
              </button>
            </div>
          </form>
        </div>
      )}

      {/* Table */}
      <div className="card">
        {loading ? (
          <SkeletonTable rows={6} cols={3} />
        ) : departments.length === 0 ? (
          <EmptyState
            title="No departments yet"
            desc="Create your first department to organise employees."
            icon={RiBuilding2Line}
            action={
              <button onClick={openCreate} className="btn-primary">
                <RiAddLine size={14} /> New Department
              </button>
            }
          />
        ) : (
          <div className="table-container">
            <table className="table">
              <thead>
                <tr>
                  <th>Name</th>
                  <th className="hidden sm:table-cell">Description</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {departments.map(dept => (
                  <tr key={dept.id}>
                    <td className="font-bold text-text">{dept.name}</td>
                    <td className="hidden sm:table-cell text-sm text-text-muted">
                      {dept.description || <span className="italic text-text-faint">—</span>}
                    </td>
                    <td>
                      <span className={dept.active !== false ? 'badge-green' : 'badge-gray'}>
                        {dept.active !== false ? 'Active' : 'Inactive'}
                      </span>
                    </td>
                    <td>
                      <button
                        onClick={() => openEdit(dept)}
                        className="btn-icon text-text-muted hover:text-primary hover:bg-primary-10"
                        title="Edit"
                      >
                        <RiEditLine size={15} />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  )
}
