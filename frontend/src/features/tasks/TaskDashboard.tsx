import { useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { LayoutGrid, List, Plus, Search } from 'lucide-react'
import { backend } from '../../api/backend'
import { errorMessage } from '../../api/client'
import { Modal } from '../../components/ui/Modal'
import { EmptyState, ErrorState, LoadingCards } from '../../components/ui/States'
import { StatusBadge } from '../../components/ui/StatusBadge'
import { useCreateTask, useTasks } from '../../hooks/useTasks'
import { formatDate } from '../../lib/utils'
import type { TaskResponseDto, TaskStatus } from '../../types'
import { TaskForm } from './TaskForm'

const pageSize = 9
function TaskCard({ task }: { task: TaskResponseDto }) {
  return <Link to={`/tasks/${task.id}`} className="block min-h-44 rounded-xl border border-slate-200 bg-white p-5 shadow-sm transition hover:border-brand-500 hover:shadow-md focus-visible:outline focus-visible:outline-2 focus-visible:outline-brand-500 dark:border-slate-800 dark:bg-slate-900">
    <StatusBadge status={task.status}/><h3 className="mt-4 line-clamp-2 font-semibold">{task.title}</h3><p className="mt-2 line-clamp-2 text-sm text-slate-500">{task.description || 'No description'}</p><p className="mt-5 text-xs text-slate-400">Created {formatDate(task.createdAt)}</p>
  </Link>
}
export function TaskDashboard() {
  const { data: tasks = [], isLoading, isError, error, refetch } = useTasks()
  const create = useCreateTask()
  const [open, setOpen] = useState(false)
  const [search, setSearch] = useState('')
  const [status, setStatus] = useState<'ALL' | TaskStatus>('ALL')
  const [sort, setSort] = useState<'newest' | 'oldest'>('newest')
  const [page, setPage] = useState(1)
  const [view, setView] = useState<'cards' | 'kanban'>('cards')
  const filtered = useMemo(() => tasks.filter((task) => (status === 'ALL' || task.status === status) && task.title.toLowerCase().includes(search.trim().toLowerCase())).sort((a, b) => sort === 'newest' ? new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime() : new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()), [tasks, status, search, sort])
  const totalPages = Math.max(1, Math.ceil(filtered.length / pageSize))
  const visible = filtered.slice((Math.min(page, totalPages) - 1) * pageSize, Math.min(page, totalPages) * pageSize)
  const setFilter = (value: 'ALL' | TaskStatus) => { setStatus(value); setPage(1) }
  return <div className="space-y-7"><div className="flex flex-wrap items-start justify-between gap-4"><div><p className="eyebrow">WORKSPACE</p><h1 className="page-title">My tasks</h1><p className="mt-2 text-slate-500">Plan, track, and finish what matters.</p></div><button className="button-primary inline-flex items-center gap-2" onClick={() => setOpen(true)}><Plus size={18}/>New task</button></div>
    <div className="grid gap-3 sm:grid-cols-3">{backend.statuses.map((item) => <button key={item} onClick={() => setFilter(item)} className={`rounded-xl border bg-white p-5 text-left shadow-sm dark:bg-slate-900 ${status === item ? 'border-brand-500 ring-2 ring-brand-500/10' : 'border-slate-200 dark:border-slate-800'}`}><div className="flex items-center justify-between"><StatusBadge status={item}/><span className="text-2xl font-bold">{tasks.filter((task) => task.status === item).length}</span></div></button>)}</div>
    <div className="flex flex-wrap gap-3"><div className="relative min-w-48 flex-1 sm:max-w-sm"><Search size={18} className="absolute left-3 top-3 text-slate-400" aria-hidden="true"/><input aria-label="Search tasks by title" placeholder="Search tasks" className="field pl-10" value={search} onChange={(event) => { setSearch(event.target.value); setPage(1) }}/></div><select aria-label="Filter by status" className="field w-auto" value={status} onChange={(event) => setFilter(event.target.value as 'ALL' | TaskStatus)}><option value="ALL">All statuses</option>{backend.statuses.map((item) => <option key={item} value={item}>{backend.statusLabels[item]}</option>)}</select><select aria-label="Sort by created date" className="field w-auto" value={sort} onChange={(event) => { setSort(event.target.value as 'newest' | 'oldest'); setPage(1) }}><option value="newest">Newest first</option><option value="oldest">Oldest first</option></select><div className="flex rounded-lg border border-slate-200 p-1 dark:border-slate-700"><button className={`icon-button ${view === 'cards' ? 'text-brand-600' : ''}`} aria-label="Card view" aria-pressed={view === 'cards'} onClick={() => setView('cards')}><List size={18}/></button><button className={`icon-button ${view === 'kanban' ? 'text-brand-600' : ''}`} aria-label="Kanban view" aria-pressed={view === 'kanban'} onClick={() => setView('kanban')}><LayoutGrid size={18}/></button></div></div>
    {isLoading ? <LoadingCards/> : isError ? <ErrorState message={errorMessage(error)} retry={() => void refetch()}/> : filtered.length === 0 ? <EmptyState title="No tasks found" detail={tasks.length === 0 ? 'Create your first task to get started.' : 'Try another search or filter.'}/> : view === 'cards' ? <><div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">{visible.map((task) => <TaskCard key={task.id} task={task}/>)}</div><div className="flex items-center justify-between text-sm text-slate-500"><span>Showing {Math.min((page - 1) * pageSize + 1, filtered.length)}–{Math.min(page * pageSize, filtered.length)} of {filtered.length}</span><div className="flex gap-2"><button className="button-secondary" disabled={page <= 1} onClick={() => setPage(page - 1)}>Previous</button><button className="button-secondary" disabled={page >= totalPages} onClick={() => setPage(page + 1)}>Next</button></div></div></> : <div className="grid gap-4 lg:grid-cols-3">{backend.statuses.map((item) => <section key={item} aria-label={`${backend.statusLabels[item]} tasks`} className="rounded-xl bg-slate-100 p-4 dark:bg-slate-900/70"><div className="mb-4 flex items-center justify-between"><h2 className="font-semibold">{backend.statusLabels[item]}</h2><span className="text-sm text-slate-500">{filtered.filter((task) => task.status === item).length}</span></div><div className="space-y-3">{filtered.filter((task) => task.status === item).map((task) => <TaskCard key={task.id} task={task}/>)}</div></section>)}</div>}
    {open && <Modal title="Create task" onClose={() => setOpen(false)}><TaskForm busy={create.isPending} onCancel={() => setOpen(false)} onSave={async (values) => { await create.mutateAsync(values); setOpen(false) }}/></Modal>}
  </div>
}
