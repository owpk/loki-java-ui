import { useState } from 'react'
import { Stack, Group, Button, NumberInput, Title, Text, SegmentedControl } from '@mantine/core'
import { DateTimePicker } from '@mantine/dates'
import { QueryEditor } from '../components/QueryEditor'
import { LogTable } from '../components/LogTable'
import { LogEntry } from '../components/LogEntry'
import { queryLogs } from '../api/query'
import { queryRangeLogs } from '../api/queryRange'
import type { LogEvent, LokiRawQueryResponse } from '../types'

type Mode = 'instant' | 'range'

export function LogQLPage() {
  const [expression, setExpression] = useState('')
  const [mode, setMode] = useState<Mode>('instant')
  const [start, setStart] = useState<Date | null>(null)
  const [end, setEnd] = useState<Date | null>(null)
  const [limit, setLimit] = useState<number | string>(100)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [data, setData] = useState<LokiRawQueryResponse | null>(null)
  const [selected, setSelected] = useState<LogEvent | null>(null)

  const handleSubmit = async () => {
    setLoading(true)
    setError(null)
    try {
      const result =
        mode === 'instant'
          ? await queryLogs({ query_expression: expression, limit: Number(limit) })
          : await queryRangeLogs({
              query_expression: expression,
              start: start?.toISOString(),
              end: end?.toISOString(),
              limit: Number(limit),
            })
      setData(result)
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Unknown error')
    } finally {
      setLoading(false)
    }
  }

  const logs: LogEvent[] =
    data?.data.result.flatMap((r) =>
      r.values.map(([ts, line]) => ({
        ...r.stream,
        timestamp: ts,
        line,
      })),
    ) ?? []

  return (
    <Stack>
      <Title order={3}>LogQL Editor</Title>
      <QueryEditor value={expression} onChange={setExpression} />
      <Group align="flex-end">
        <SegmentedControl
          value={mode}
          onChange={(v) => setMode(v as Mode)}
          data={[
            { value: 'instant', label: 'Instant' },
            { value: 'range', label: 'Range' },
          ]}
        />
        {mode === 'range' && (
          <>
            <DateTimePicker label="Start" value={start} onChange={setStart} />
            <DateTimePicker label="End" value={end} onChange={setEnd} />
          </>
        )}
        <NumberInput label="Limit" value={limit} onChange={setLimit} w={100} min={1} max={5000} />
        <Button onClick={() => void handleSubmit()} loading={loading}>
          Run
        </Button>
      </Group>
      {error && <Text c="red">{error}</Text>}
      <LogTable logs={logs} onRowClick={setSelected} />
      <LogEntry log={selected} onClose={() => setSelected(null)} />
    </Stack>
  )
}
