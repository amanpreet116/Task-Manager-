import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { Link, Navigate } from 'react-router-dom'
import { CheckCheck } from 'lucide-react'
import { login, register } from '../../api/auth'
import { errorMessage } from '../../api/client'
import { loginSchema, registerSchema, type LoginValues } from '../../lib/schemas'
import { useAuth } from './AuthProvider'
import { useState } from 'react'

function AuthForm({ mode }: { mode: 'login' | 'register' }) {
  const { user, signIn } = useAuth()
  const [serverError, setServerError] = useState('')
  const { register: field, handleSubmit, formState: { errors, isSubmitting } } = useForm<LoginValues>({ resolver: zodResolver(mode === 'login' ? loginSchema : registerSchema) })
  if (user) return <Navigate to="/" replace />
  const onSubmit = async (values: LoginValues) => {
    setServerError('')
    try {
      if (mode === 'register') await register(values)
      const response = await login(values)
      signIn(response.token)
    } catch (error) { setServerError(errorMessage(error)) }
  }
  return <main className="min-h-screen bg-slate-50 px-4 py-12 dark:bg-slate-950 grid place-items-center">
    <section className="w-full max-w-md rounded-2xl border border-slate-200 bg-white p-8 shadow-xl shadow-slate-200/50 dark:border-slate-800 dark:bg-slate-900 dark:shadow-none" aria-labelledby="auth-title">
      <div className="mb-8 inline-flex h-12 w-12 items-center justify-center rounded-xl bg-brand-600 text-white"><CheckCheck aria-hidden="true" /></div>
      <h1 id="auth-title" className="text-3xl font-bold tracking-tight">{mode === 'login' ? 'Welcome back' : 'Create your account'}</h1>
      <p className="mt-2 text-sm text-slate-500">Keep your work organized in one place.</p>
      <form onSubmit={handleSubmit(onSubmit)} noValidate className="mt-8 space-y-5">
        <div><label htmlFor="email" className="field-label">Email</label><input id="email" type="email" autoComplete="email" className="field" aria-invalid={!!errors.email} {...field('email')} />{errors.email && <p className="field-error">{errors.email.message}</p>}</div>
        <div><label htmlFor="password" className="field-label">Password</label><input id="password" type="password" autoComplete={mode === 'login' ? 'current-password' : 'new-password'} className="field" aria-invalid={!!errors.password} {...field('password')} />{errors.password && <p className="field-error">{errors.password.message}</p>}</div>
        {serverError && <p role="alert" className="alert-error">{serverError}</p>}
        <button className="button-primary w-full" type="submit" disabled={isSubmitting}>{isSubmitting ? 'Please wait…' : mode === 'login' ? 'Sign in' : 'Create account'}</button>
      </form>
      <p className="mt-6 text-center text-sm text-slate-500">{mode === 'login' ? 'New here?' : 'Already registered?'} <Link className="font-semibold text-brand-600 hover:underline" to={mode === 'login' ? '/register' : '/login'}>{mode === 'login' ? 'Create an account' : 'Sign in'}</Link></p>
    </section>
  </main>
}
export function LoginPage() { return <AuthForm mode="login" /> }
export function RegisterPage() { return <AuthForm mode="register" /> }
