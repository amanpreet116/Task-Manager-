import { backend } from '../api/backend'
import type { TaskStatus } from '../types'

export const statusLabel = (status: TaskStatus) => backend.statusLabels[status]
export function formatDate(value: string): string {
  return new Intl.DateTimeFormat(undefined, { dateStyle: 'medium' }).format(new Date(value))
}
