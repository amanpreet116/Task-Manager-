import { useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { ArrowLeft, Pencil, Trash2 } from 'lucide-react'
import { backend } from '../../api/backend'
import { errorMessage } from '../../api/client'
import { Modal } from '../../components/ui/Modal'
import { EmptyState, ErrorState } from '../../components/ui/States'
import { StatusBadge } from '../../components/ui/StatusBadge'
import { useAuth } from '../auth/AuthProvider'
import { useDeleteTask, useTask, useTasks, useUpdateTask } from '../../hooks/useTasks'
import { formatDate } from '../../lib/utils'
import { TaskForm } from './TaskForm'

export function TaskDetail() {
  const { id = '' } = useParams()
  const navigate = useNavigate()
  const { user } = useAuth()
  const { data: task, isLoading, isError, error, refetch } = useTask(id)
  const { data: myTasks = [] } = useTasks()
  const update = useUpdateTask(id), remove = useDeleteTask()
  const [editing, setEditing] = useState(false)
  const [confirming, setConfirming] = useState(false)
  if (isLoading) return <div role="status" className="animate-pulse">Loading task…</div>
  if (isError) return <ErrorState message={errorMessage(error)} retry={() => void refetch()}/>
  if (!task) return <EmptyState title="Task not found" detail="This task may have been removed."/>
  const canEdit = myTasks.some((mine) => mine.id === task.id)
  const canDelete = user?.role === 'ADMIN'
  return <div className="mx-auto max-w-3xl space-y-6"><Link to="/" className="inline-flex items-center gap-2 text-sm text-slate-500 hover:text-brand-600"><ArrowLeft size={16}/>Back to tasks</Link>
    <article className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm dark:border-slate-800 dark:bg-slate-900 sm:p-8"><div className="flex flex-wrap items-start justify-between gap-4"><div><StatusBadge status={task.status}/><h1 className="page-title mt-4 break-words">{task.title}</h1></div><div className="flex gap-2">{canEdit && <button className="button-secondary inline-flex items-center gap-2" onClick={() => setEditing(true)}><Pencil size={16}/>Edit</button>}{canDelete && <button className="button-danger inline-flex items-center gap-2" onClick={() => setConfirming(true)}><Trash2 size={16}/>Delete</button>}</div></div>
      <dl className="mt-7 grid gap-4 border-t border-slate-200 pt-6 text-sm dark:border-slate-800 sm:grid-cols-2"><div><dt className="text-slate-500">Created</dt><dd className="mt-1 font-medium">{formatDate(task.createdAt)}</dd></div><div><dt className="text-slate-500">Last updated</dt><dd className="mt-1 font-medium">{formatDate(task.updatedAt)}</dd></div></dl><div className="mt-8"><h2 className="font-semibold">Description</h2><p className="mt-3 whitespace-pre-wrap break-words text-slate-600 dark:text-slate-300">{task.description || 'No description added.'}</p></div></article>
    <aside className="rounded-xl border border-blue-200 bg-blue-50 p-4 text-sm text-blue-900 dark:border-blue-900 dark:bg-blue-950/40 dark:text-blue-100"><strong>About notifications</strong><p className="mt-1">When a task is created or its status changes, the backend queues an event and publishes it asynchronously, typically within about {backend.outboxDelaySeconds} seconds. This page does not receive a live delivery confirmation.</p></aside>
    {editing && <Modal title="Edit task" onClose={() => setEditing(false)}><TaskForm task={task} busy={update.isPending} onCancel={() => setEditing(false)} onSave={async (values) => { await update.mutateAsync(values); setEditing(false) }}/></Modal>}
    {confirming && <Modal title="Delete task?" onClose={() => setConfirming(false)}><p className="text-sm text-slate-600 dark:text-slate-300">This will permanently delete “{task.title}”.</p><div className="mt-6 flex justify-end gap-3"><button className="button-secondary" onClick={() => setConfirming(false)}>Cancel</button><button className="button-danger" disabled={remove.isPending} onClick={async () => { try { await remove.mutateAsync({ id: task.id, title: task.title }); navigate('/') } catch { /* Toast displays the error; keep the dialog open. */ } }}>{remove.isPending ? 'Deleting…' : 'Delete task'}</button></div></Modal>}
  </div>
}
