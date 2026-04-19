const BASE_URL = (import.meta.env.VITE_API_URL as string | undefined) ?? 'http://localhost:8080'

export function apiUrl(path: string): string {
  return `${BASE_URL}${path}`
}
