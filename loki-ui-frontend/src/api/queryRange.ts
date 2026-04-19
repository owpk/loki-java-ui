import { apiUrl } from './client'
import type { LokiQueryRangeRequest, LokiRawQueryResponse } from '../types'

export async function queryRangeLogs(request: LokiQueryRangeRequest): Promise<LokiRawQueryResponse> {
  const res = await fetch(apiUrl('/api/loki/queryRange'), {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  })
  if (!res.ok) throw new Error(`queryRange failed: ${res.status} ${res.statusText}`)
  return res.json() as Promise<LokiRawQueryResponse>
}
