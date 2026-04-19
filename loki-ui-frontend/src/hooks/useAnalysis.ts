import { useState, useRef, useCallback } from 'react'
import { streamAnalysis } from '../api/analysis'
import type { LogAnalysisResult } from '../types'

export function useAnalysis() {
  const [results, setResults] = useState<LogAnalysisResult[]>([])
  const [connected, setConnected] = useState(false)
  const esRef = useRef<EventSource | null>(null)

  const connect = useCallback(() => {
    esRef.current?.close()
    setResults([])
    esRef.current = streamAnalysis(
      (r) => setResults((prev) => [r, ...prev]),
      () => setConnected(false),
      () => setConnected(true),
    )
  }, [])

  const disconnect = useCallback(() => {
    esRef.current?.close()
    setConnected(false)
  }, [])

  return { results, connected, connect, disconnect }
}
