import { useState, useCallback } from 'react'
import { queryRangeLogs } from '../api/queryRange'
import type { LokiQueryRangeRequest, LokiRawQueryResponse } from '../types'

export function useQueryRange() {
  const [data, setData] = useState<LokiRawQueryResponse | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const fetch = useCallback(async (request: LokiQueryRangeRequest) => {
    setLoading(true)
    setError(null)
    try {
      setData(await queryRangeLogs(request))
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Unknown error')
    } finally {
      setLoading(false)
    }
  }, [])

  return { data, loading, error, fetch }
}
