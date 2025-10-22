export interface BackendLog {
  funcSystem?: string
  instance?: string
  appVersion?: string
  userLogin?: string
  logTs: string
  logLevel?: LogLevel
  mdc?: string
  logger?: string
  logThread?: string
  logMessage?: string
  logException?: string
  logStack?: string
}

export interface LogQueryFilter {
  field: string
  operator: string
  value: string
}

export interface LogFilterRequest {
  filters?: LogQueryFilter[]
  timeRange?: string
  start?: string
  end?: string
  order?: 'ASC' | 'DESC'
  limit?: number | 20
  offset?: number
}

export interface LogListResponse {
  items: BackendLog[]
  total: number 
}

export type LogLevel = 'TRACE' | 'DEBUG' | 'INFO' | 'WARN' | 'ERROR'
