import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import { useNavigate } from 'react-router-dom'
import { useQueryClient } from '@tanstack/react-query'
import { tokenStore, userFromToken } from '../../lib/token'
import type { User } from '../../types'

interface AuthContextValue { user: User | null; signIn: (token: string) => void; signOut: () => void }
const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [user, setUser] = useState<User | null>(() => {
    const token = tokenStore.get()
    return token ? userFromToken(token) : null
  })
  const signOut = useCallback(() => { tokenStore.clear(); queryClient.clear(); setUser(null); navigate('/login', { replace: true }) }, [navigate, queryClient])
  const signIn = useCallback((token: string) => {
    const parsed = userFromToken(token)
    if (!parsed) throw new Error('The server returned an invalid or expired token')
    queryClient.clear(); tokenStore.set(token); setUser(parsed); navigate('/', { replace: true })
  }, [navigate, queryClient])
  useEffect(() => { window.addEventListener('auth:expired', signOut); return () => window.removeEventListener('auth:expired', signOut) }, [signOut])
  const value = useMemo(() => ({ user, signIn, signOut }), [user, signIn, signOut])
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
export function useAuth(): AuthContextValue {
  const value = useContext(AuthContext)
  if (!value) throw new Error('useAuth must be inside AuthProvider')
  return value
}
