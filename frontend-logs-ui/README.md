# frontend-logs-ui (MVP)

Minimal React + TypeScript + Vite + Tailwind scaffold for Logs UI.

## Quick start

1. unzip and `cd frontend-logs-ui`
2. `npm install`
3. `npm run dev`

Vite is configured with a proxy so requests to `/api/*` are forwarded to `http://localhost:8080`.

The app expects your backend endpoints:
- `POST /api/logs/{app}` returning `LogListResponse` JSON.

