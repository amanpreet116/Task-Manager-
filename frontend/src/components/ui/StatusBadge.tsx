import { statusLabel } from '../../lib/utils'
import type { TaskStatus } from '../../types'
const styles: Record<TaskStatus, string> = {
  TODO: 'bg-amber-50 text-amber-800 dark:bg-amber-900/30 dark:text-amber-200',
  IN_PROGRESS: 'bg-blue-50 text-blue-800 dark:bg-blue-900/30 dark:text-blue-200',
  DONE: 'bg-emerald-50 text-emerald-800 dark:bg-emerald-900/30 dark:text-emerald-200',
}
export function StatusBadge({ status }: { status: TaskStatus }) { return <span className={`inline-flex rounded-full px-2.5 py-1 text-xs font-semibold ${styles[status]}`}>{statusLabel(status)}</span> }
