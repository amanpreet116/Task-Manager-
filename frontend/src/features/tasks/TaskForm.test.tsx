import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { renderApp } from '../../test/render'
import type { TaskValues } from '../../lib/schemas'
import { TaskForm } from './TaskForm'

describe('task form', () => {
  it('requires a title and submits a valid task', async () => {
    const save = vi.fn(async (_values: TaskValues) => {})
    const user = userEvent.setup()
    renderApp(<TaskForm busy={false} onCancel={() => {}} onSave={save}/>)
    await user.click(screen.getByRole('button', { name: 'Create task' }))
    expect(await screen.findByText('Title is required')).toBeInTheDocument()
    expect(save).not.toHaveBeenCalled()
    await user.type(screen.getByLabelText(/Title/), 'Prepare release')
    await user.type(screen.getByLabelText('Description'), 'Ship the update')
    await user.click(screen.getByRole('button', { name: 'Create task' }))
    expect(save.mock.calls[0]?.[0]).toEqual({ title: 'Prepare release', description: 'Ship the update', status: 'TODO' })
  })
})
