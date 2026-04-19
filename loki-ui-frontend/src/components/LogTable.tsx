import { useRef } from 'react'
import { useVirtualizer } from '@tanstack/react-virtual'
import { Text } from '@mantine/core'
import type { LogEvent } from '../types'

interface Props {
  logs: LogEvent[]
  onRowClick: (log: LogEvent) => void
}

// Уникальные ключи из всех логов для формирования колонок
function collectKeys(logs: LogEvent[]): string[] {
  const keys = new Set<string>()
  logs.forEach((log) => {
    Object.keys(log).forEach((key) => {
      if (key !== '_timestamp' && key !== 'timestamp') {
        keys.add(key)
      }
    })
  })
  return Array.from(keys)
}

function LogRow({ 
  log, 
  onClick, 
  columns 
}: { 
  log: LogEvent; 
  onClick: () => void;
  columns: string[];
}) {
  // Извлекаем timestamp
  const timestamp = log['_timestamp'] || log['timestamp'] || new Date().toISOString()

  return (
    <div
      onClick={onClick}
      style={{
        display: 'flex',
        gap: '12px',
        padding: '6px 10px',
        cursor: 'pointer',
        borderBottom: '1px solid var(--mantine-color-gray-2)',
        alignItems: 'center',
        whiteSpace: 'nowrap',
      }}
    >
      {/* Timestamp колонка */}
      <Text size="xs" c="dimmed" style={{ whiteSpace: 'nowrap', minWidth: '100px' }}>
        {new Date(timestamp).toLocaleTimeString()}
      </Text>
      
      {/* Данные */}
      <div style={{ display: 'flex', gap: '12px', fontSize: '12px' }}>
        {columns.map((key) => {
          const value = log[key]
          if (value === undefined) return null
          
          // Подкраска ключей: жирный шрифт и цвет
          return (
            <span key={key} style={{ display: 'inline-flex', alignItems: 'center', gap: '4px' }}>
              <Text component="span" size="xs" fw={600} c="blue.7">
                {key}:
              </Text>
              <Text component="span" size="xs" ff="monospace" c="dark">
                {value}
              </Text>
            </span>
          )
        })}
      </div>
    </div>
  )
}

export function LogTable({ logs, onRowClick }: Props) {
  const parentRef = useRef<HTMLDivElement>(null)
  
  // Собираем все уникальные ключи
  const columns = collectKeys(logs)

  const virtualizer = useVirtualizer({
    count: logs.length,
    getScrollElement: () => parentRef.current,
    estimateSize: () => 44,
    overscan: 10,
  })

  const virtualItems = virtualizer.getVirtualItems()

  return (
    <div
      ref={parentRef}
      style={{
        height: '60vh',
        width: '100%',
        overflow: 'auto',
        border: '1px solid var(--mantine-color-gray-3)',
        borderRadius: 4,
      }}
    >
      {/* Заголовки колонок */}
      <div
        style={{
          position: 'sticky',
          top: 0,
          zIndex: 1,
          backgroundColor: 'var(--mantine-color-body)',
          borderBottom: '2px solid var(--mantine-color-gray-3)',
          display: 'flex',
          gap: '12px',
          padding: '8px 10px',
          fontWeight: 600,
          fontSize: '12px',
        }}
      >
        <div style={{ minWidth: '100px' }}>TIME</div>
        <div>LOG DATA</div>
      </div>

      {/* Fallback: если virtualizer не работает, рендерим все строки напрямую */}
      {virtualItems.length === 0 && logs.length > 0 ? (
        <div>
          {logs.map((log, index) => (
            <LogRow 
              key={index} 
              log={log} 
              onClick={() => onRowClick(log)} 
              columns={columns}
            />
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
                <LogRow 
                  log={log} 
                  onClick={() => onRowClick(log)} 
                  columns={columns}
                />
              </div>
            )
          })}
        </div>
      )}
    </div>
  )
}