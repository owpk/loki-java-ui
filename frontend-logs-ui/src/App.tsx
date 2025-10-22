import LogsPage from './pages/LogsPage'

export default function App() {
  return (
    <div className="min-h-screen bg-gray-100">
      <header className="bg-white shadow p-4">
        <h1 className="text-2xl font-semibold">FS OCRV Logs UI (MVP)</h1>
      </header>
      <main className="p-4">
        <LogsPage />
      </main>
    </div>
  )
}
