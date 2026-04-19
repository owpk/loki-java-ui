import { apiUrl } from './client'
import type { LogAnalysisResult } from '../types'

export function streamAnalysis(
  onMessage: (result: LogAnalysisResult) => void,
  onError: (err: Event) => void,
  onOpen: () => void,
): EventSource {
  const es = new EventSource(apiUrl('/api/loki/analysis/stream'))
  es.onopen = () => onOpen()
  es.onmessage = (ev) => {
    try {
      onMessage(JSON.parse(ev.data as string) as LogAnalysisResult)
    } catch {
      // ignore malformed events
    }
  }
  es.onerror = (err) => onError(err)
  return es
}
