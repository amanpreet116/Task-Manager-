import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { AuthProvider } from './features/auth/AuthProvider'
import { ActivityProvider } from './features/activity/ActivityProvider'
import { ToastProvider } from './components/ui/ToastProvider'
import { ErrorBoundary } from './components/ui/ErrorBoundary'
import { AppRoutes } from './routes/AppRoutes'
import './styles.css'

const queryClient = new QueryClient({ defaultOptions: { queries: { retry: 1, staleTime: 30_000 } } })
ReactDOM.createRoot(document.getElementById('root')!).render(<React.StrictMode><ErrorBoundary><BrowserRouter><QueryClientProvider client={queryClient}><AuthProvider><ActivityProvider><ToastProvider><AppRoutes/></ToastProvider></ActivityProvider></AuthProvider></QueryClientProvider></BrowserRouter></ErrorBoundary></React.StrictMode>)
