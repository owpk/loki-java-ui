import { Stack, Group, TextInput, Select, Button, ActionIcon } from '@mantine/core'
import type { LogQueryFilter, FilterOperator } from '../types'

const OPERATORS: { value: FilterOperator; label: string }[] = [
  { value: '=', label: '=' },
  { value: '!=', label: '!=' },
  { value: '=~', label: '=~' },
  { value: '!~', label: '!~' },
]

interface Props {
  value: LogQueryFilter[]
  onChange: (filters: LogQueryFilter[]) => void
}

export function FilterForm({ value, onChange }: Props) {
  const add = () => onChange([...value, { field: '', operator: '=', value: '' }])
  const remove = (i: number) => onChange(value.filter((_, idx) => idx !== i))
  const update = (i: number, patch: Partial<LogQueryFilter>) =>
    onChange(value.map((f, idx) => (idx === i ? { ...f, ...patch } : f)))

  return (
    <Stack gap="xs">
      {value.map((f, i) => (
        <Group key={i} gap="xs" align="flex-end">
          <TextInput
            placeholder="field"
            value={f.field}
            onChange={(e) => update(i, { field: e.target.value })}
            style={{ flex: 1 }}
          />
          <Select
            data={OPERATORS}
            value={f.operator}
            onChange={(v) => update(i, { operator: v as FilterOperator })}
            w={80}
          />
          <TextInput
            placeholder="value"
            value={f.value}
            onChange={(e) => update(i, { value: e.target.value })}
            style={{ flex: 2 }}
          />
          <ActionIcon
            color="red"
            variant="subtle"
            aria-label="remove"
            onClick={() => remove(i)}
          >
            ✕
          </ActionIcon>
        </Group>
      ))}
      <Button variant="subtle" onClick={add} w="fit-content">
        Add filter
      </Button>
    </Stack>
  )
}
