import { useState } from 'react'
import { Stack, Group, Button, NumberInput, TextInput, Title } from '@mantine/core'
import { FilterForm } from '../components/FilterForm'
import { LogTable } from '../components/LogTable'
import { LogEntry } from '../components/LogEntry'
import { useStream } from '../hooks/useStream'
import type { LogEvent, LogQueryFilter } from '../types'

export function StreamPage() {
  const [filters, setFilters] = useState<LogQueryFilter[]>([])
  const [query, setQuery] = useState('')
  const [limit, setLimit] = useState<number | string>(100)
  const [selected, setSelected] = useState<LogEvent | null>(null)
  const { logs, running, start, stop } = useStream()

  const handleToggle = () => {
    if (running) {
      stop()
    } else {
      start({
        filters: filters.filter((f) => f.field && f.value),
        query: query || undefined,
        limit: Number(limit),
      })
    }
  }

  return (
    <Stack>
      <Title order={3}>Real-time Stream</Title>
      <FilterForm value={filters} onChange={setFilters} />
      <Group>
        <TextInput
          placeholder="Raw query (optional)"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          style={{ flex: 1 }}
        />
        <NumberInput
          placeholder="Limit"
          value={limit}
          onChange={setLimit}
          w={100}
          min={1}
          max={5000}
        />
        <Button onClick={handleToggle} color={running ? 'red' : 'green'}>
          {running ? 'Stop' : 'Start'}
        </Button>
      </Group>
      <LogTable logs={logs} onRowClick={setSelected} />
      <LogEntry log={selected} onClose={() => setSelected(null)} />
    </Stack>
  )
}
