import type { ReactElement } from 'react'
import { render } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { AuthProvider } from '../features/auth/AuthProvider'
import { ActivityProvider } from '../features/activity/ActivityProvider'
import { ToastProvider } from '../components/ui/ToastProvider'

export function renderApp(element: ReactElement, route = '/') {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(<MemoryRouter initialEntries={[route]}><QueryClientProvider client={client}><AuthProvider><ActivityProvider><ToastProvider>{element}</ToastProvider></ActivityProvider></AuthProvider></QueryClientProvider></MemoryRouter>)
}
export function testToken() {
  const payload = btoa(JSON.stringify({ sub: 'test@example.com', email: 'test@example.com', role: 'USER', exp: Math.floor(Date.now() / 1000) + 3600 }))
  return `header.${payload}.signature`
}
