import axios from 'axios'
import { backend } from './backend'
import { tokenStore } from '../lib/token'

export const api = axios.create({ baseURL: backend.baseUrl, timeout: 15000 })
api.interceptors.request.use((config) => {
  const token = tokenStore.get()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})
api.interceptors.response.use(
  (response) => response,
  (error: unknown) => {
    const path = axios.isAxiosError(error) ? error.config?.url : undefined
    if (axios.isAxiosError(error) && error.response?.status === 401 &&
      path !== backend.paths.login && path !== backend.paths.register) {
      tokenStore.clear()
      window.dispatchEvent(new Event('auth:expired'))
    }
    return Promise.reject(error)
  },
)

export function errorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const message = (error.response?.data as { message?: unknown } | undefined)?.message
    if (typeof message === 'string') return message
    if (!error.response) return 'Cannot reach the server. Check your connection and try again.'
  }
  return error instanceof Error ? error.message : 'Something went wrong. Please try again.'
}
