import { Activity, Clock3 } from 'lucide-react'
import { useActivity } from './ActivityProvider'

export function ActivityPage() {
  const { items } = useActivity()
  return <div className="mx-auto max-w-3xl space-y-6"><div><p className="eyebrow">THIS SESSION</p><h1 className="page-title">Recent activity</h1><p className="mt-2 text-sm text-slate-500">Actions you completed in this tab. This list clears when you reload.</p></div><div className="rounded-2xl border border-slate-200 bg-white p-6 dark:border-slate-800 dark:bg-slate-900">{items.length ? <ol className="divide-y divide-slate-200 dark:divide-slate-800">{items.map((item) => <li key={item.id} className="flex gap-4 py-4 first:pt-0 last:pb-0"><span className="grid h-9 w-9 shrink-0 place-items-center rounded-full bg-brand-50 text-brand-600 dark:bg-slate-800"><Activity size={18}/></span><div><p className="text-sm"><strong className="capitalize">{item.type}</strong> “{item.title}”</p><p className="mt-1 flex items-center gap-1 text-xs text-slate-500"><Clock3 size={12}/>{new Date(item.at).toLocaleString()}</p></div></li>)}</ol> : <p className="py-8 text-center text-sm text-slate-500">No activity in this session yet.</p>}</div><p className="text-xs text-slate-500">This is local activity only. It does not indicate whether Kafka delivered a notification.</p></div>
}
