import type { User } from '../types'

const key = 'task-manager.jwt'
// sessionStorage limits token lifetime to this tab, but JavaScript can still read it.
// A server-issued HttpOnly cookie is preferable if the backend adds cookie authentication.
export const tokenStore = {
  get: () => sessionStorage.getItem(key),
  set: (token: string) => sessionStorage.setItem(key, token),
  clear: () => sessionStorage.removeItem(key),
}

export function userFromToken(token: string): User | null {
  try {
    const payload = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/'))) as Record<string, unknown>
    if (typeof payload.exp === 'number' && payload.exp * 1000 <= Date.now()) return null
    if (typeof payload.sub !== 'string') return null
    const role = payload.role === 'ADMIN' ? 'ADMIN' : 'USER'
    return { email: typeof payload.email === 'string' ? payload.email : payload.sub, subject: payload.sub, role }
  } catch { return null }
}
