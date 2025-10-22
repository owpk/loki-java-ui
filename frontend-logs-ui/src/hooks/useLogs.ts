import { useCallback, useEffect, useRef, useState } from 'react'
import { fetchLogs, streamLogs } from '../api/logs'
import type { BackendLog, LogFilterRequest, LogListResponse } from '../types'

export function useLogs(app: string, initialFilter: LogFilterRequest) {
	const [logs, setLogs] = useState<BackendLog[]>([])
	const [filter, setFilter] = useState<LogFilterRequest>(initialFilter)
	const [loading, setLoading] = useState(false)
	const [total, setTotal] = useState(0)
	const [streaming, setStreaming] = useState(false)
	const [eventSource, setEventSource] = useState<EventSource | null>(null)
	const lastTsRef = useRef<string>('')

	/**
	 * Загружает логи. 
	 * Если передан `direction = 'forward'`, берет timestamp последней записи как end.
	 * Если передан `direction = 'backward'`, берет timestamp первой записи как start.
	 * Если `reset = true`, очищает список и загружает заново.
	 */
	const load = useCallback(
		async (opts?: { direction?: 'forward'; reset?: boolean }) => {
			if (loading || streaming) return

			setLoading(true)

			try {
				let req: LogFilterRequest = {
					...filter,
					limit: filter.limit ?? 100,
				}

				// Используем значение из ref для forward загрузки
				if (opts?.direction === 'forward' && lastTsRef.current) {
					req.end = lastTsRef.current
				}

				const res: LogListResponse = await fetchLogs(app, req)

				setTotal(res.total)
				const lastNewLog = res.items[res.items.length - 1]
				// Сохраняем в ref
				lastTsRef.current = lastNewLog.logTs

				if (opts?.direction === 'forward') {
					setLogs(prev => [...prev, ...res.items])
				} else {
					setLogs(res.items)
					// При сбросе очищаем ref
					lastTsRef.current = lastNewLog.logTs
				}
			} finally {
				setLoading(false)
			}
		},
		[streaming, app, filter]
	)

	// начальная загрузка
	// обычная загрузка
	useEffect(() => {
		if (!streaming) {
			load({ reset: true })
		}
	}, [load, streaming])

	// стрим
	useEffect(() => {
		if (streaming) {
			setLogs([])
			const es = streamLogs(app, filter, (log) => {
				setLogs(prev => [log, ...prev])
			})
			setEventSource(es)
			return () => {
				es.close()
				setEventSource(null)
			}
		} else if (eventSource) {
			eventSource.close()
			setEventSource(null)
		}
		// eslint-disable-next-line
	}, [streaming, app, filter])

	return {
		logs,
		total,
		loading,
		filter,
		setFilter,
		loadMore: () => load({ direction: 'forward' }),
		reload: () => load({ reset: true }),
		refresh: () => load({ reset: false }),
		streaming,
		setStreaming,
	}
}