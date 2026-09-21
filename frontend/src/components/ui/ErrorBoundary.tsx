import { Component, type ErrorInfo, type ReactNode } from 'react'
interface State { failed: boolean }
export class ErrorBoundary extends Component<{ children: ReactNode }, State> {
  state: State = { failed: false }
  static getDerivedStateFromError(): State { return { failed: true } }
  componentDidCatch(error: Error, info: ErrorInfo) { console.error('Unhandled UI error', error, info) }
  render() {
    if (this.state.failed) return <main className="grid min-h-screen place-items-center bg-slate-50 p-6 text-center dark:bg-slate-950"><div><h1 className="text-2xl font-bold">Something went wrong</h1><p className="mt-2 text-slate-500">Please reload the page and try again.</p><button className="button-primary mt-5" onClick={() => window.location.reload()}>Reload page</button></div></main>
    return this.props.children
  }
}
