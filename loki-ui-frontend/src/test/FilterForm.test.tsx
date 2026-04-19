import { render, screen, fireEvent } from '@testing-library/react'
import { MantineProvider } from '@mantine/core'
import { FilterForm } from '../components/FilterForm'
import type { LogQueryFilter } from '../types'

function wrap(ui: React.ReactElement) {
  return render(<MantineProvider>{ui}</MantineProvider>)
}

describe('FilterForm', () => {
  it('renders existing filters', () => {
    const filters: LogQueryFilter[] = [{ field: 'app', operator: '=', value: 'myapp' }]
    wrap(<FilterForm value={filters} onChange={() => {}} />)
    expect(screen.getByDisplayValue('app')).toBeInTheDocument()
    expect(screen.getByDisplayValue('myapp')).toBeInTheDocument()
  })

  it('calls onChange with new filter when Add filter is clicked', () => {
    const onChange = vi.fn()
    wrap(<FilterForm value={[]} onChange={onChange} />)
    fireEvent.click(screen.getByText('Add filter'))
    expect(onChange).toHaveBeenCalledWith([{ field: '', operator: '=', value: '' }])
  })

  it('calls onChange without filter when remove is clicked', () => {
    const filters: LogQueryFilter[] = [{ field: 'app', operator: '=', value: 'myapp' }]
    const onChange = vi.fn()
    wrap(<FilterForm value={filters} onChange={onChange} />)
    fireEvent.click(screen.getByRole('button', { name: /remove/i }))
    expect(onChange).toHaveBeenCalledWith([])
  })
})
