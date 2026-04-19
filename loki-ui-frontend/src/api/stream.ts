import { fetchEventSource } from '@microsoft/fetch-event-source'
import { apiUrl } from './client'
import type { LogEvent, LogFilterStreamRequest } from '../types'

export function streamLogs(
  request: LogFilterStreamRequest,
  onMessage: (log: LogEvent) => void,
  onError: (err: unknown) => void,
  signal: AbortSignal,
): void {
  void fetchEventSource(apiUrl('/api/loki/stream?delaySec=2'), {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
    signal,
    onmessage(ev) {
      try {
        onMessage(JSON.parse(ev.data) as LogEvent)
      } catch {
        // ignore malformed events
      }
    },
    onerror(err) {
      onError(err)
      throw err // stop automatic retry
    },
  })
}
