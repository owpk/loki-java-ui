import { Stack, Button, Title, Card, Text, Badge, Group, Collapse } from '@mantine/core'
import { useDisclosure } from '@mantine/hooks'
import { useAnalysis } from '../hooks/useAnalysis'
import type { LogAnalysisResult } from '../types'

function AnalysisCard({ result }: { result: LogAnalysisResult }) {
  const [opened, { toggle }] = useDisclosure(false)
  return (
    <Card withBorder>
      <Group justify="space-between" mb="xs">
        <Badge variant="light">{result.model}</Badge>
        <Text size="xs" c="dimmed">
          {new Date(result.timestamp).toLocaleString()}
        </Text>
      </Group>
      <Text size="sm" mb="xs">
        {result.analysis}
      </Text>
      <Button variant="subtle" size="xs" onClick={toggle}>
        {opened ? 'Hide' : 'Show'} {result.analyzedLogs.length} analyzed logs
      </Button>
      <Collapse in={opened}>
        <Stack gap="xs" mt="xs">
          {result.analyzedLogs.map((line, i) => (
            <Text key={i} size="xs" ff="monospace" c="dimmed">
              {line}
            </Text>
          ))}
        </Stack>
      </Collapse>
    </Card>
  )
}

export function AnalysisPage() {
  const { results, connected, connect, disconnect } = useAnalysis()

  return (
    <Stack>
      <Group justify="space-between">
        <Title order={3}>LLM Analysis</Title>
        <Button onClick={connected ? disconnect : connect} color={connected ? 'red' : 'green'}>
          {connected ? 'Disconnect' : 'Connect'}
        </Button>
      </Group>
      {results.length === 0 && (
        <Text c="dimmed" size="sm">
          {connected ? 'Waiting for analysis results...' : 'Connect to start receiving analysis.'}
        </Text>
      )}
      {results.map((r, i) => (
        <AnalysisCard key={i} result={r} />
      ))}
    </Stack>
  )
}
