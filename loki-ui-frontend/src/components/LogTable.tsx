import { useRef } from 'react'
import { useVirtualizer } from '@tanstack/react-virtual'
import { Badge, Group, Text } from '@mantine/core'
import type { LogEvent } from '../types'

interface Props {
  logs: LogEvent[]
  onRowClick: (log: LogEvent) => void
}

function LogRow({ log, onClick }: { log: LogEvent; onClick: () => void }) {
  return (
    <Group
      gap="sm"
      wrap="nowrap"
      onClick={onClick}
      style={{
        padding: '4px 8px',
        cursor: 'pointer',
        borderBottom: '1px solid var(--mantine-color-gray-2)',
      }}
    >
      <Text size="xs" c="dimmed" style={{ whiteSpace: 'nowrap', minWidth: 80 }}>
        {new Date(log.timestamp).toLocaleTimeString()}
      </Text>
      <Text
        size="xs"
        ff="monospace"
        style={{ flex: 1, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}
      >
        {log.line}
      </Text>
      <Group gap="xs" wrap="nowrap">
        {Object.entries(log.labels).slice(0, 3).map(([k, v]) => (
          <Badge key={k} size="xs" variant="outline">
            {k}={v}
          </Badge>
        ))}
      </Group>
    </Group>
  )
}

export function LogTable({ logs, onRowClick }: Props) {
  const parentRef = useRef<HTMLDivElement>(null)

  const virtualizer = useVirtualizer({
    count: logs.length,
    getScrollElement: () => parentRef.current,
    estimateSize: () => 40,
    overscan: 10,
  })

  const virtualItems = virtualizer.getVirtualItems()

  return (
    <div
      ref={parentRef}
      style={{
        height: '60vh',
        overflow: 'auto',
        border: '1px solid var(--mantine-color-gray-3)',
        borderRadius: 4,
      }}
    >
      {/* Fallback: if virtualizer produces no items (e.g. jsdom / no layout), render all rows directly */}
      {virtualItems.length === 0 && logs.length > 0 ? (
        <div>
          {logs.map((log, index) => (
            <LogRow key={index} log={log} onClick={() => onRowClick(log)} />
          ))}
        </div>
      ) : (
        <div style={{ height: virtualizer.getTotalSize(), position: 'relative' }}>
          {virtualItems.map((item) => {
            const log = logs[item.index]
            return (
              <div
                key={item.key}
                data-index={item.index}
                ref={virtualizer.measureElement}
                style={{
                  position: 'absolute',
                  top: item.start,
                  left: 0,
                  right: 0,
                }}
              >
                <LogRow log={log} onClick={() => onRowClick(log)} />
              </div>
            )
          })}
        </div>
      )}
    </div>
  )
}
