import { useEffect, useState } from 'react'
import { BackendLog } from '../types'
import FlexibleButton from './FlexibleButton'
import LogDetails from './LogDetails'
import './LogTable.css'

// Функция для форматирования времени в формат MM:SS
function formatTime(seconds: number): string {
    const mins = Math.floor(seconds / 60)
    const secs = seconds % 60
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}


interface Props {
	logs: BackendLog[]
	onLoadMore?: () => void
	loading?: boolean
	total: number
	streaming?: boolean
	onToggleStreaming?: () => void
	onRefresh?: () => void
}

export default function LogTable({ logs, onLoadMore, loading, total, streaming, onToggleStreaming, onRefresh }: Props) {
	const [selectedLog, setSelectedLog] = useState<BackendLog | null>(null)
	const [streamingTime, setStreamingTime] = useState(0)

	useEffect(() => {
		let interval: number | null = null
		if (streaming) {
			setStreamingTime(0)
			interval = window.setInterval(() => {
				setStreamingTime(prev => prev + 1)
			}, 1000)
		}
		return () => {
			if (interval) {
				window.clearInterval(interval)
			}
		}
	}, [streaming])

	if (selectedLog) {
		return <LogDetails log={selectedLog} onBack={() => setSelectedLog(null)} />
	}

	return (
		<div className="log-table bg-white shadow-xl rounded-2xl overflow-hidden text-gray-600">
			<div className="p-2 flex flex-col sm:flex-row items-center gap-2">
				<div className="flex items-center gap-2">
					<FlexibleButton
						onClick={onToggleStreaming}
						color={streaming ? 'bg-red-600 text-white' : 'bg-green-600 hover:bg-green-700 text-gray'}>
						{streaming ? `Стоп (${formatTime(streamingTime)})` : 'Включить стриминг'}
					</FlexibleButton>					<FlexibleButton
						disabled={streaming}
						onClick={onRefresh}
						color='bg-green-600 hover:bg-green-700'>
						Обновить
					</FlexibleButton>
				</div>

				<div className="flex items-center gap-2">
					<FlexibleButton
						onClick={() => window.scrollTo({ top: document.body.scrollHeight, behavior: 'smooth' })}
						color='bg-gray-600 hover:bg-gray-700 text-white'>
						↓ Вниз
					</FlexibleButton>
				</div>
			</div>
			<table className="min-w-full text-left">
				<thead className="bg-gray-50 border-b">
					<tr>
						<th className="p-2">Time</th>
						<th className="p-2">Level</th>
						<th className="p-2">Logger</th>
						<th className="p-2">Message</th>
					</tr>
				</thead>
				<tbody>
					{logs?.length > 0 && logs.map((l, idx) => {
						let levelClass = ''
						switch ((l.logLevel || '').toUpperCase()) {
							case 'ERROR':
								levelClass = 'text-red-500'; break;
							case 'WARN':
								levelClass = 'text-yellow-500'; break;
							case 'INFO':
								levelClass = 'text-gray-400'; break;
							case 'DEBUG':
								levelClass = 'text-blue-700'; break;
							default:
								levelClass = 'text-gray-500';
						}
						return (
							<tr key={idx}
								onClick={() => setSelectedLog(l)}
								className="border-b border-gray-200 hover:bg-gray-100 transition-colors duration-150 ease-in-out cursor-pointer">
								<td className="p-2 align-top text-sm">{l.logTs}</td>
								<td className={`p-2 align-top text-sm font-medium rounded ${levelClass}`}>{l.logLevel}</td>
								<td className="p-2 align-top text-sm">{l.logger}</td>
								<td className="p-2 align-top text-sm whitespace-pre-wrap">{l.logMessage}</td>
							</tr>
						)
					})}
					{logs.length === 0 && (
						<tr>
							<td colSpan={5} className="p-4 text-center text-gray-400">No logs found</td>
						</tr>
					)}
				</tbody>
			</table>

			<div className="p-2 flex flex-col sm:flex-row justify-between items-center gap-2">

				<FlexibleButton
					onClick={() => window.scrollTo({ top: 0, behavior: 'smooth' })}
					color='bg-gray-600 hover:bg-gray-700 text-white'>
					↑ Вверх
				</FlexibleButton>
				{!streaming && onLoadMore && (
					<button
						onClick={onLoadMore}
						disabled={loading}
						className="rounded-2xl px-4 py-2 bg-gray-200 rounded hover:bg-gray-300 transition-colors disabled:opacity-50"
					>
						{loading ? 'Загрузка...' : 'Загрузить ещё'}
					</button>
				)}
			</div>
		</div>
	)
}
