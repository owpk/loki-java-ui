import { apiUrl } from './client'
import type { LokiQueryRequest, LokiRawQueryResponse } from '../types'

export async function queryLogs(request: LokiQueryRequest): Promise<LokiRawQueryResponse> {
  const res = await fetch(apiUrl('/api/loki/query'), {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  })
  if (!res.ok) throw new Error(`query failed: ${res.status} ${res.statusText}`)
  return res.json() as Promise<LokiRawQueryResponse>
}
