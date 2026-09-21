import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { backend } from '../../api/backend'
import { taskSchema, type TaskValues } from '../../lib/schemas'
import type { TaskResponseDto } from '../../types'

export function TaskForm({ task, onSave, onCancel, busy }: { task?: TaskResponseDto; onSave: (values: TaskValues) => Promise<void>; onCancel: () => void; busy: boolean }) {
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<TaskValues>({
    resolver: zodResolver(taskSchema),
    defaultValues: { title: task?.title || '', description: task?.description || '', status: task?.status || 'TODO' },
  })
  return <form onSubmit={handleSubmit(onSave)} noValidate className="space-y-5">
    <div><label className="field-label" htmlFor="task-title">Title <span aria-hidden="true">*</span></label><input id="task-title" className="field" maxLength={backend.limits.title} aria-invalid={!!errors.title} {...register('title')} />{errors.title && <p className="field-error">{errors.title.message}</p>}</div>
    <div><label className="field-label" htmlFor="task-description">Description</label><textarea id="task-description" rows={4} className="field" {...register('description')} />{errors.description && <p className="field-error">{errors.description.message}</p>}</div>
    <div><label className="field-label" htmlFor="task-status">Status</label><select id="task-status" className="field" {...register('status')}>{backend.statuses.map((status) => <option key={status} value={status}>{backend.statusLabels[status]}</option>)}</select></div>
    <div className="flex justify-end gap-3 pt-2"><button type="button" className="button-secondary" onClick={onCancel}>Cancel</button><button type="submit" className="button-primary" disabled={busy || isSubmitting}>{busy || isSubmitting ? 'Saving…' : task ? 'Save changes' : 'Create task'}</button></div>
  </form>
}
