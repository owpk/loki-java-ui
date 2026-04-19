// Произвольный JSON лог из stream API (после фильтрации heartbeat)
// Также поддерживает формат от query/queryRange API (timestamp, line, labels)
export type LogEvent = Record<string, string>

export type FilterOperator = '=' | '!=' | '=~' | '!~'

export interface LogQueryFilter {
  field: string
  operator: FilterOperator
  value: string
}

export interface LogFilterStreamRequest {
  filters?: LogQueryFilter[]
  query?: string
  start?: number
  limit?: number
}

export interface LokiQueryRequest {
  query_expression: string
  limit?: number
  time?: string
  direction?: 'forward' | 'backward'
}

export interface LokiQueryRangeRequest {
  query_expression: string
  start?: string
  end?: string
  since?: string
  limit?: number
  step?: string
  interval?: string
  direction?: 'forward' | 'backward'
}

export interface LokiRawQueryResponse {
  data: {
    result: Array<{
      stream: Record<string, string>
      values: string[][]
    }>
  }
}

export interface LogAnalysisResult {
  analysis: string
  model: string
  analyzedLogs: string[]
  timestamp: string
}
