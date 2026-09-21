import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import { useAuth } from '../auth/AuthProvider'

type ActivityType = 'created' | 'updated' | 'deleted'
export interface ActivityItem { id: string; type: ActivityType; title: string; at: string }
interface ActivityContextValue { items: ActivityItem[]; record: (type: ActivityType, title: string) => void; clear: () => void }
const ActivityContext = createContext<ActivityContextValue | null>(null)

// TODO: Replace this local source with GET /api/users/me/activity (scoped to the
// authenticated user, paginated). SSE/WebSocket can invalidate that query later.
export function ActivityProvider({ children }: { children: ReactNode }) {
  const { user } = useAuth()
  const [items, setItems] = useState<ActivityItem[]>([])
  useEffect(() => setItems([]), [user?.email])
  const value = useMemo<ActivityContextValue>(() => ({
    items,
    record: (type, title) => setItems((current) => [{ id: crypto.randomUUID(), type, title, at: new Date().toISOString() }, ...current].slice(0, 30)),
    clear: () => setItems([]),
  }), [items])
  return <ActivityContext.Provider value={value}>{children}</ActivityContext.Provider>
}
export function useActivity(): ActivityContextValue {
  const value = useContext(ActivityContext)
  if (!value) throw new Error('useActivity must be inside ActivityProvider')
  return value
}
