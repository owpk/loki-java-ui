import { Textarea } from '@mantine/core'

interface Props {
  value: string
  onChange: (value: string) => void
  placeholder?: string
}

export function QueryEditor({ value, onChange, placeholder }: Props) {
  return (
    <Textarea
      value={value}
      onChange={(e) => onChange(e.target.value)}
      placeholder={placeholder ?? 'Enter LogQL query, e.g. {app="myapp"} |= "error"'}
      minRows={4}
      autosize
      styles={{ input: { fontFamily: 'monospace' } }}
    />
  )
}
