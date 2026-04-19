import { Navigate, Route, BrowserRouter, Routes, useNavigate, useLocation } from 'react-router-dom'
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
  const navigate = useNavigate()
  const location = useLocation()

  return (
    <AppShell navbar={{ width: 200, breakpoint: 'sm' }} padding="md">
      <AppShell.Navbar p="sm">
        <Stack gap="xs">
          {NAV_ITEMS.map((item) => (
            <NavLink
              key={item.path}
              label={item.label}
              active={location.pathname === item.path}
              onClick={() => navigate(item.path)}
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
