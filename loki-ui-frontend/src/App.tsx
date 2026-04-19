import { NavLink as RouterNavLink, Navigate, Route, BrowserRouter, Routes } from 'react-router-dom'
import { AppShell, NavLink, Stack } from '@mantine/core'
import { StreamPage } from './pages/StreamPage'
import { RangePage } from './pages/RangePage'
import { LogQLPage } from './pages/LogQLPage'
import { AnalysisPage } from './pages/AnalysisPage'

const NAV_ITEMS = [
  { path: '/stream', label: 'Stream' },
  { path: '/range', label: 'Range Query' },
  { path: '/logql', label: 'LogQL' },
  { path: '/analysis', label: 'Analysis' },
]

function Layout() {
  return (
    <AppShell navbar={{ width: 200, breakpoint: 'sm' }} padding="md">
      <AppShell.Navbar p="sm">
        <Stack gap="xs">
          {NAV_ITEMS.map((item) => (
            <NavLink<typeof RouterNavLink>
              key={item.path}
              component={RouterNavLink}
              to={item.path}
              label={item.label}
            />
          ))}
        </Stack>
      </AppShell.Navbar>
      <AppShell.Main>
        <Routes>
          <Route path="/" element={<Navigate to="/stream" replace />} />
          <Route path="/stream" element={<StreamPage />} />
          <Route path="/range" element={<RangePage />} />
          <Route path="/logql" element={<LogQLPage />} />
          <Route path="/analysis" element={<AnalysisPage />} />
        </Routes>
      </AppShell.Main>
    </AppShell>
  )
}

export function App() {
  return (
    <BrowserRouter>
      <Layout />
    </BrowserRouter>
  )
}
