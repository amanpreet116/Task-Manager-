import { http, HttpResponse } from 'msw'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { server } from '../../test/setup'
import { renderApp, testToken } from '../../test/render'
import { LoginPage } from './AuthPages'

describe('login form', () => {
  it('validates email and signs in with the returned token', async () => {
    const login = vi.fn(() => HttpResponse.json({ token: testToken() }))
    server.use(http.post('/api/auth/login', login))
    const user = userEvent.setup()
    renderApp(<LoginPage/>, '/login')
    await user.click(screen.getByRole('button', { name: 'Sign in' }))
    expect(await screen.findByText('Enter a valid email')).toBeInTheDocument()
    expect(login).not.toHaveBeenCalled()
    await user.type(screen.getByLabelText('Email'), 'test@example.com')
    await user.type(screen.getByLabelText('Password'), 'password123')
    await user.click(screen.getByRole('button', { name: 'Sign in' }))
    await waitFor(() => expect(sessionStorage.getItem('task-manager.jwt')).toBe(testToken()))
    expect(login).toHaveBeenCalledOnce()
  })
})
