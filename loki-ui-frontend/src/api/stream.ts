import { fetchEventSource } from '@microsoft/fetch-event-source'
import { apiUrl } from './client'
import type { LogEvent, LogFilterStreamRequest } from '../types'

// Heartbeat сообщение от сервера
interface HeartbeatMessage {
  line: 'heartbeat'
}

function isHeartbeat(msg: unknown): msg is HeartbeatMessage {
  return typeof msg === 'object' && msg !== null && 'line' in msg && (msg as HeartbeatMessage).line === 'heartbeat'
}

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
        const msg = JSON.parse(ev.data)
        
        // Пропускаем heartbeat сообщения
        if (isHeartbeat(msg)) return
        
        // Остальное считаем произвольным JSON логи
        onMessage(msg as LogEvent)
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