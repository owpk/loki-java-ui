import { useState, useRef, useCallback } from 'react'
import { streamLogs } from '../api/stream'
import type { LogEvent, LogFilterStreamRequest } from '../types'

const MAX_LOGS = 1000

export function useStream() {
  const [logs, setLogs] = useState<LogEvent[]>([])
  const [running, setRunning] = useState(false)
  const abortRef = useRef<AbortController | null>(null)

  const start = useCallback((request: LogFilterStreamRequest) => {
    abortRef.current?.abort()
    const ctrl = new AbortController()
    abortRef.current = ctrl
    setLogs([])
    setRunning(true)
    streamLogs(
      request,
      (log) => setLogs((prev) => [...prev.slice(-(MAX_LOGS - 1)), log]),
      () => setRunning(false),
      ctrl.signal,
    )
  }, [])

  const stop = useCallback(() => {
    abortRef.current?.abort()
    setRunning(false)
  }, [])

  return { logs, running, start, stop }
}
