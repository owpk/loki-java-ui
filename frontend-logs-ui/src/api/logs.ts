// SSE stream for logs
export function streamLogs(app: string, filter: any, onMessage: (log: any) => void): EventSource {
	const url = `${base}/${encodeURIComponent(app)}/stream?delaySec=2`;
	const es = new EventSource(url, { withCredentials: true });
	es.onmessage = (event) => {
		try {
			const log = JSON.parse(event.data);
			onMessage(log);
		} catch { }
	};
	return es;
}
import { LogFilterRequest, LogListResponse } from '../types';

const base = '/api/logs'

export async function fetchLogs(app: string, filter: LogFilterRequest): Promise<LogListResponse> {
	const res = await fetch(`${base}/${encodeURIComponent(app)}`, {
		method: 'POST',
		headers: { 'Content-Type': 'application/json' },
		body: JSON.stringify(filter),
	})
	if (!res.ok) {
		throw new Error(`fetchLogs failed: ${res.status} ${res.statusText}`)
	}
	return res.json()
}
