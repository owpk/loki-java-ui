import { useState } from 'react'
import { Stack, Group, Button, NumberInput, Select, Title, Text } from '@mantine/core'
import { DateTimePicker } from '@mantine/dates'
import { LogTable } from '../components/LogTable'
import { LogEntry } from '../components/LogEntry'
import { QueryEditor } from '../components/QueryEditor'
import { useQueryRange } from '../hooks/useQueryRange'
import type { LogEvent } from '../types'

const SINCE_OPTIONS = [
  { value: '5m', label: 'Last 5 min' },
  { value: '15m', label: 'Last 15 min' },
  { value: '1h', label: 'Last 1 hour' },
  { value: '6h', label: 'Last 6 hours' },
  { value: '24h', label: 'Last 24 hours' },
]

type RangeMode = 'since' | 'custom'

export function RangePage() {
  const [mode, setMode] = useState<RangeMode>('since')
  const [since, setSince] = useState('1h')
  const [start, setStart] = useState<Date | null>(null)
  const [end, setEnd] = useState<Date | null>(null)
  const [expression, setExpression] = useState('')
  const [limit, setLimit] = useState<number | string>(100)
  const [direction, setDirection] = useState<'forward' | 'backward'>('backward')
  const [selected, setSelected] = useState<LogEvent | null>(null)
  const { data, loading, error, fetch } = useQueryRange()

  const handleSubmit = () => {
    void fetch({
      query_expression: expression,
      limit: Number(limit),
      direction,
      ...(mode === 'since'
        ? { since }
        : { start: start?.toISOString(), end: end?.toISOString() }),
    })
  }

  const logs: LogEvent[] =
    data?.data.result.flatMap((r) =>
      r.values.map(([ts, line]) => ({ timestamp: ts, line, labels: r.stream })),
    ) ?? []

  return (
    <Stack>
      <Title order={3}>Range Query</Title>
      <QueryEditor
        value={expression}
        onChange={setExpression}
        placeholder='{app="myapp"} |= "error"'
      />
      <Group align="flex-end">
        <Select
          label="Mode"
          data={[
            { value: 'since', label: 'Since' },
            { value: 'custom', label: 'Custom range' },
          ]}
          value={mode}
          onChange={(v) => setMode(v as RangeMode)}
          w={160}
        />
        {mode === 'since' ? (
          <Select
            label="Period"
            data={SINCE_OPTIONS}
            value={since}
            onChange={(v) => setSince(v!)}
            w={160}
          />
        ) : (
          <>
            <DateTimePicker label="Start" value={start} onChange={setStart} />
            <DateTimePicker label="End" value={end} onChange={setEnd} />
          </>
        )}
        <NumberInput label="Limit" value={limit} onChange={setLimit} w={100} min={1} max={5000} />
        <Select
          label="Direction"
          data={[
            { value: 'backward', label: 'Backward' },
            { value: 'forward', label: 'Forward' },
          ]}
          value={direction}
          onChange={(v) => setDirection(v as 'forward' | 'backward')}
          w={130}
        />
        <Button onClick={handleSubmit} loading={loading}>
          Query
        </Button>
      </Group>
      {error && <Text c="red">{error}</Text>}
      <LogTable logs={logs} onRowClick={setSelected} />
      <LogEntry log={selected} onClose={() => setSelected(null)} />
    </Stack>
  )
}
