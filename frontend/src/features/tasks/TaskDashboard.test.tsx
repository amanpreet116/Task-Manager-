import { http, HttpResponse } from 'msw'
import { screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { server } from '../../test/setup'
import { renderApp, testToken } from '../../test/render'
import { TaskDashboard } from './TaskDashboard'

describe('task dashboard', () => {
  it('renders tasks returned by the current-user endpoint', async () => {
    sessionStorage.setItem('task-manager.jwt', testToken())
    server.use(http.get('/api/tasks/mine', () => HttpResponse.json([{ id: '1', title: 'Prepare release', description: 'Ship the update', status: 'IN_PROGRESS', ownerId: 'owner-1', createdAt: '2026-01-15T10:00:00', updatedAt: '2026-01-15T10:00:00', subtasks: [] }])))
    renderApp(<TaskDashboard/>)
    expect(await screen.findByRole('heading', { name: 'Prepare release' })).toBeInTheDocument()
    expect(screen.getByText('Ship the update')).toBeInTheDocument()
    expect(screen.getAllByText('In progress').length).toBeGreaterThan(0)
  })
})
