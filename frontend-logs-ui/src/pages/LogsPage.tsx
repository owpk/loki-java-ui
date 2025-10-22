
import FilterPanel from '../components/FilterPanel'
import LogTable from '../components/LogTable'
import { useLogs } from '../hooks/useLogs'
import { LogFilterRequest } from '../types'

export default function LogsPage() {
	const initialFilter: LogFilterRequest = { timeRange: '1h', order: 'DESC', limit: 20 }
	const { logs, loading, total, setFilter, loadMore, streaming, refresh, setStreaming } = useLogs('mock-service', initialFilter)

	return (
		<div className="grid grid-cols-4 gap-4">
			<div className="col-span-1">
				<FilterPanel onApply={f => setFilter(f)} initial={initialFilter} disabled={streaming} />
			</div>
			<div className="col-span-3">
				<LogTable
					logs={logs}
					onLoadMore={() => loadMore()}
					loading={loading}
					total={total}
					streaming={streaming}
					onToggleStreaming={() => setStreaming(s => !s)}
					onRefresh={() => refresh()}
				/>
			</div>
		</div>
	)
}
