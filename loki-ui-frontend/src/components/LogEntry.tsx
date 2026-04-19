import { Drawer, Text, Stack, Code } from '@mantine/core'
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
          <Text size="sm" c="dimmed">
            {log['_timestamp'] || log['timestamp'] || new Date().toISOString()}
          </Text>
          <Code block style={{ wordBreak: 'break-all', whiteSpace: 'pre-wrap' }}>
            {JSON.stringify(log, null, 2)}
          </Code>
        </Stack>
      )}
    </Drawer>
  )
}