import { z } from 'zod'
import { backend } from '../api/backend'

export const loginSchema = z.object({ email: z.string().trim().email('Enter a valid email'), password: z.string().min(1, 'Password is required') })
export const registerSchema = loginSchema.extend({ password: z.string().min(backend.limits.passwordMin, `Use at least ${backend.limits.passwordMin} characters`).max(backend.limits.passwordMax) })
export const taskSchema = z.object({
  title: z.string().trim().min(1, 'Title is required').max(backend.limits.title, `Use ${backend.limits.title} characters or fewer`),
  description: z.string(),
  status: z.enum(backend.statuses),
})
export type LoginValues = z.infer<typeof loginSchema>
export type TaskValues = z.infer<typeof taskSchema>
