import { createContext, useCallback, useContext, useState, type ReactNode } from 'react'
import { X } from 'lucide-react'

type ToastKind = 'success' | 'error'
interface Toast { id: number; message: string; kind: ToastKind }
const ToastContext = createContext<((message: string, kind?: ToastKind) => void) | null>(null)
export function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<Toast[]>([])
  const dismiss = useCallback((id: number) => setToasts((current) => current.filter((item) => item.id !== id)), [])
  const notify = useCallback((message: string, kind: ToastKind = 'success') => {
    const id = Date.now() + Math.random()
    setToasts((current) => [...current, { id, message, kind }])
    window.setTimeout(() => dismiss(id), 5000)
  }, [dismiss])
  return <ToastContext.Provider value={notify}>{children}<div className="fixed bottom-4 right-4 z-50 flex max-w-[calc(100vw-2rem)] flex-col gap-2" aria-live="polite">{toasts.map((item) => <div key={item.id} role={item.kind === 'error' ? 'alert' : 'status'} className={`flex items-center gap-3 rounded-xl px-4 py-3 text-sm text-white shadow-lg ${item.kind === 'error' ? 'bg-rose-700' : 'bg-slate-900 dark:bg-slate-700'}`}><span>{item.message}</span><button onClick={() => dismiss(item.id)} aria-label="Dismiss notification"><X size={16} /></button></div>)}</div></ToastContext.Provider>
}
export function useToast() { const value = useContext(ToastContext); if (!value) throw new Error('useToast must be inside ToastProvider'); return value }
