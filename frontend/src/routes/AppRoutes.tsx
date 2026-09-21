import { useEffect, useState, type ReactNode } from 'react'
import { Link, Navigate, Outlet, Route, Routes } from 'react-router-dom'
import { Activity, CheckCheck, ClipboardList, LogOut, Menu, Moon, Sun, X } from 'lucide-react'
import { useAuth } from '../features/auth/AuthProvider'
import { LoginPage, RegisterPage } from '../features/auth/AuthPages'
import { TaskDashboard } from '../features/tasks/TaskDashboard'
import { TaskDetail } from '../features/tasks/TaskDetail'
import { ActivityPage } from '../features/activity/ActivityPage'

function Protected({ children }: { children: ReactNode }) {
  const { user } = useAuth()
  return user ? <>{children}</> : <Navigate to="/login" replace />
}
function AppLayout() {
  const { user, signOut } = useAuth()
  const [menuOpen, setMenuOpen] = useState(false)
  const [dark, setDark] = useState(() => localStorage.getItem('task-manager.theme') === 'dark')
  useEffect(() => { document.documentElement.classList.toggle('dark', dark); localStorage.setItem('task-manager.theme', dark ? 'dark' : 'light') }, [dark])
  return <div className="min-h-screen bg-slate-50 text-slate-900 dark:bg-slate-950 dark:text-slate-100"><a href="#main-content" className="sr-only focus:not-sr-only focus:absolute focus:z-50 focus:bg-white focus:p-3">Skip to content</a>
    <header className="sticky top-0 z-20 border-b border-slate-200 bg-white/95 backdrop-blur dark:border-slate-800 dark:bg-slate-900/95"><div className="mx-auto flex max-w-7xl items-center justify-between gap-4 px-4 py-3 sm:px-6"><Link to="/" className="flex items-center gap-2 font-bold"><span className="grid h-9 w-9 place-items-center rounded-lg bg-brand-600 text-white"><CheckCheck size={20}/></span>Task Manager</Link><button className="icon-button sm:hidden" aria-label={menuOpen ? 'Close menu' : 'Open menu'} aria-expanded={menuOpen} onClick={() => setMenuOpen(!menuOpen)}>{menuOpen ? <X/> : <Menu/>}</button><nav aria-label="Main navigation" className={`${menuOpen ? 'flex' : 'hidden'} absolute left-0 right-0 top-full flex-col gap-2 border-b border-slate-200 bg-white p-4 dark:border-slate-800 dark:bg-slate-900 sm:static sm:flex sm:flex-row sm:items-center sm:border-0 sm:bg-transparent sm:p-0`}><Link className="nav-link" to="/" onClick={() => setMenuOpen(false)}><ClipboardList size={17}/>Tasks</Link><Link className="nav-link" to="/activity" onClick={() => setMenuOpen(false)}><Activity size={17}/>Activity</Link><span className="hidden h-5 w-px bg-slate-200 dark:bg-slate-700 sm:block"/><span className="truncate px-2 text-sm text-slate-500" title={user?.email}>{user?.email}</span><button className="icon-button" aria-label={dark ? 'Switch to light mode' : 'Switch to dark mode'} onClick={() => setDark(!dark)}>{dark ? <Sun size={18}/> : <Moon size={18}/>}</button><button className="nav-link" onClick={signOut}><LogOut size={17}/>Logout</button></nav></div></header><main id="main-content" className="mx-auto max-w-7xl px-4 py-8 sm:px-6 sm:py-10"><Outlet/></main></div>
}
function NotFound() { return <main className="grid min-h-screen place-items-center p-6 text-center"><div><p className="eyebrow">404</p><h1 className="page-title">Page not found</h1><p className="mt-2 text-slate-500">The page you requested does not exist.</p><Link to="/" className="button-primary mt-5 inline-block">Go to tasks</Link></div></main> }
export function AppRoutes() { return <Routes><Route path="/login" element={<LoginPage/>}/><Route path="/register" element={<RegisterPage/>}/><Route element={<Protected><AppLayout/></Protected>}><Route index element={<TaskDashboard/>}/><Route path="tasks/:id" element={<TaskDetail/>}/><Route path="activity" element={<ActivityPage/>}/></Route><Route path="*" element={<NotFound/>}/></Routes> }
