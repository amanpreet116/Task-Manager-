import { useEffect, useRef, type ReactNode } from 'react'
import { createPortal } from 'react-dom'
import { X } from 'lucide-react'

export function Modal({ title, onClose, children }: { title: string; onClose: () => void; children: ReactNode }) {
  const panel = useRef<HTMLDivElement>(null)
  useEffect(() => {
    const previous = document.activeElement as HTMLElement | null
    const originalOverflow = document.body.style.overflow
    document.body.style.overflow = 'hidden'
    panel.current?.querySelector<HTMLElement>('input, textarea, select, button')?.focus()
    const onKey = (event: KeyboardEvent) => {
      if (event.key === 'Escape') onClose()
      if (event.key === 'Tab' && panel.current) {
        const elements = [...panel.current.querySelectorAll<HTMLElement>('button:not([disabled]), input:not([disabled]), textarea:not([disabled]), select:not([disabled]), a[href]')]
        const first = elements[0], last = elements[elements.length - 1]
        if (event.shiftKey && document.activeElement === first) { event.preventDefault(); last?.focus() }
        else if (!event.shiftKey && document.activeElement === last) { event.preventDefault(); first?.focus() }
      }
    }
    document.addEventListener('keydown', onKey)
    return () => { document.removeEventListener('keydown', onKey); document.body.style.overflow = originalOverflow; previous?.focus() }
  }, [onClose])
  return createPortal(<div className="fixed inset-0 z-40 grid place-items-center overflow-y-auto bg-slate-950/60 p-4" onMouseDown={(event) => { if (event.target === event.currentTarget) onClose() }}><div ref={panel} role="dialog" aria-modal="true" aria-labelledby="modal-title" className="w-full max-w-lg rounded-2xl bg-white p-6 shadow-2xl dark:bg-slate-900"><div className="mb-6 flex items-center justify-between"><h2 id="modal-title" className="text-xl font-bold">{title}</h2><button type="button" className="icon-button" onClick={onClose} aria-label="Close dialog"><X size={20} /></button></div>{children}</div></div>, document.body)
}
