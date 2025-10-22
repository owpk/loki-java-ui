import React from 'react'
import { LogFilterRequest, LogQueryFilter } from '../types'
import FlexibleButton from './FlexibleButton'


interface Props {
	onApply: (f: LogFilterRequest) => void
	initial?: LogFilterRequest
	disabled?: boolean
}

export default function FilterPanel({ onApply, initial, disabled }: Props) {

	const [level, setLevel] = React.useState('')
	const [message, setMessage] = React.useState('')
	const [end, setEnd] = React.useState('')
	const [start, setStart] = React.useState('')
	const [timeRange, setTimeRange] = React.useState(initial?.timeRange || '1h')

	const apply = () => {
		const filters: LogQueryFilter[] = []
		if (level) filters.push({ field: 'level', operator: '=', value: level })
		if (message) filters.push({ field: 'message', operator: '=~', value: message })

		onApply({
			filters,
			timeRange,
			order: 'DESC',
			limit: 100,
			start, end
		})
	}

			return (
				<div className="bg-white p-4 rounded-2xl shadow-xl space-y-3 text-gray-600">
					<div>
						<label className="block text-sm font-medium text-gray-700">Level</label>
						<input value={level} onChange={e => setLevel(e.target.value)} className="rounded-2xl mt-1 block w-full border border-gray-300 rounded p-2 focus:border-blue-400 focus:ring-0" placeholder="ERROR" disabled={disabled} />
					</div>

					<div>
						<label className="block text-sm font-medium text-gray-700">from</label>
						<input value={start} onChange={e => setStart(e.target.value)} className="rounded-2xl mt-1 block w-full border border-gray-300 rounded p-2 focus:border-blue-400 focus:ring-0" placeholder="2025-01-01T00:00:00Z" disabled={disabled} />
					</div>

					<div>
						<label className="block text-sm font-medium text-gray-700">to</label>
						<input value={end} onChange={e => setEnd(e.target.value)} className="rounded-2xl mt-1 block w-full border border-gray-300 rounded p-2 focus:border-blue-400 focus:ring-0" placeholder="2025-01-01T00:00:00Z" disabled={disabled} />
					</div>

					<div>
						<label className="block text-sm font-medium text-gray-700">Message (regex)</label>
						<input value={message} onChange={e => setMessage(e.target.value)} 
							className="rounded-2xl mt-1 block w-full border border-gray-300 rounded p-2 focus:border-blue-400 focus:ring-0" 
							placeholder=".*timeout.*" 
							disabled={disabled} />
					</div>

					<div>
						<label className="block text-sm font-medium text-gray-700">Time Range</label>
						<select value={timeRange} onChange={e => setTimeRange(e.target.value)} className="rounded-2xl mt-1 block w-full border border-gray-300 rounded p-2 focus:border-blue-400 focus:ring-0" disabled={disabled}>
							<option value="1s">1s</option>
							<option value="5s">5s</option>
							<option value="10s">10s</option>
							<option value="1m">1m</option>
							<option value="5m">5m</option>
							<option value="30m">30m</option>
							<option value="1h">1h</option>
							<option value="24h">24h</option>
							<option value="7d">7d</option>
							<option value="30d">30d</option>
							<option value="1y">1y</option>
						</select>
					</div>

					<div className="flex justify-end">
						<FlexibleButton onClick={apply} color='bg-blue-600 hover:bg-blue-700'>Apply</FlexibleButton>
						{/* <button onClick={apply} className="rounded-2xl px-4 py-2 bg-blue-600 text-white rounded" disabled={disabled}>Apply</button> */}
					</div>
				</div>
			)
}
