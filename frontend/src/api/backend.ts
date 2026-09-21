/** Change backend-specific paths, enum values, field names, and limits here. */
export const backend = {
  baseUrl: import.meta.env.VITE_API_BASE_URL || '',
  paths: {
    login: '/api/auth/login',
    register: '/api/auth/register',
    myTasks: '/api/tasks/mine',
    tasks: '/api/tasks',
    task: (id: string) => `/api/tasks/${encodeURIComponent(id)}`,
  },
  statuses: ['TODO', 'IN_PROGRESS', 'DONE'] as const,
  statusLabels: { TODO: 'To do', IN_PROGRESS: 'In progress', DONE: 'Done' },
  limits: { title: 255, passwordMin: 8, passwordMax: 72 },
  permissions: { deleteRequiresAdmin: true, updateRequiresOwner: true },
  outboxDelaySeconds: 30,
} as const
