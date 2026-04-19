import { render, screen, fireEvent } from '@testing-library/react'
import { MantineProvider } from '@mantine/core'
import { LogTable } from '../components/LogTable'
import type { LogEvent } from '../types'

const LOGS: LogEvent[] = [
  { timestamp: '2026-04-18T10:00:00Z', line: 'ERROR something failed', labels: { app: 'api' } },
  { timestamp: '2026-04-18T10:01:00Z', line: 'INFO request ok', labels: { app: 'api' } },
]

function wrap(ui: React.ReactElement) {
  return render(<MantineProvider>{ui}</MantineProvider>)
}

describe('LogTable', () => {
  it('renders log lines', () => {
    wrap(<LogTable logs={LOGS} onRowClick={() => {}} />)
    expect(screen.getByText('ERROR something failed')).toBeInTheDocument()
    expect(screen.getByText('INFO request ok')).toBeInTheDocument()
  })

  it('calls onRowClick with the correct log when a row is clicked', () => {
    const onRowClick = vi.fn()
    wrap(<LogTable logs={LOGS} onRowClick={onRowClick} />)
    fireEvent.click(screen.getByText('ERROR something failed'))
    expect(onRowClick).toHaveBeenCalledWith(LOGS[0])
  })
})
