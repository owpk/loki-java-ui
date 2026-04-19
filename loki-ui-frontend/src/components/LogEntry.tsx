import { Drawer, Text, Badge, Group, Stack, Code } from '@mantine/core'
import type { LogEvent } from '../types'

interface Props {
  log: LogEvent | null
  onClose: () => void
}

export function LogEntry({ log, onClose }: Props) {
  return (
    <Drawer opened={!!log} onClose={onClose} title="Log Entry" position="right" size="lg">
      {log && (
        <Stack>
          <Text size="sm" c="dimmed">{log.timestamp}</Text>
          <Code block style={{ wordBreak: 'break-all', whiteSpace: 'pre-wrap' }}>
            {log.line}
          </Code>
          <Text fw={500}>Labels</Text>
          <Group gap="xs">
            {Object.entries(log.labels).map(([k, v]) => (
              <Badge key={k} variant="light">{k}={v}</Badge>
            ))}
          </Group>
        </Stack>
      )}
    </Drawer>
  )
}
