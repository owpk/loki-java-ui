# Loki UI — Design Spec

**Date:** 2026-04-18  
**Status:** Approved

---

## Overview

Two standalone projects on top of the existing `jloki` library:

1. **`loki-ui-server`** — Spring Boot application that pulls in `jloki-spring-boot-starter-web` and exposes the Loki API. Acts as the backend for the UI. Can be extended with custom controllers over time.
2. **`loki-ui-frontend`** — Vite + React + TypeScript + Mantine SPA. Deployed independently (nginx, CDN, etc.), communicates with the backend via HTTP and SSE.

Both projects live in the `loki-java-ui` repository alongside `jloki/`.

---

## Repository Structure

```
loki-java-ui/
  jloki/                   # existing library (untouched)
  loki-ui-server/          # standalone Spring Boot project
  loki-ui-frontend/        # standalone Vite + React + Mantine project
  docs/
    superpowers/specs/
      2026-04-18-loki-ui-frontend-design.md
```

---

## Backend — `loki-ui-server`

### Purpose

Thin Spring Boot application that:
- Imports `jloki-spring-boot-starter-web` — this auto-registers all Loki API controllers
- Provides a place to add custom controllers as the project evolves
- Configures CORS so the separately-deployed frontend can reach it
- Has a stub `SecurityConfig` (permit-all) ready for future auth extension

### Exposed endpoints (from jloki-spring-boot-starter-web)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/loki/stream` | SSE real-time log tail |
| POST | `/api/loki/query` | Instant LogQL query |
| POST | `/api/loki/queryRange` | Range LogQL query |
| GET | `/api/loki/analysis/stream` | SSE stream of LLM analysis results |

### Project structure

```
loki-ui-server/
  build.gradle
  src/main/java/owpk/lokiui/
    LokiUiApplication.java
    config/
      SecurityConfig.java      # stub: permitAll, ready for extension
      WebConfig.java           # CORS configuration
  src/main/resources/
    application.yml            # loki.*  and server.* settings
```

### Security extension point

`SecurityConfig` is a `@Configuration` class with a `SecurityFilterChain` bean that currently allows all requests. When auth is needed, replace the `permitAll()` chain with JWT/OAuth2/Basic Auth configuration here.

### CORS

`WebConfig` registers a `CorsConfigurationSource` bean. The allowed origin is configurable via `loki.ui.allowed-origins` in `application.yml` (defaults to `*` for development).

---

## Frontend — `loki-ui-frontend`

### Tech stack

- **Vite** (build tool + dev server)
- **React 18** + **TypeScript**
- **Mantine v7** (UI components)
- **React Router v6** (routing)
- **@microsoft/fetch-event-source** (SSE over POST)
- **@tanstack/react-virtual** (log table virtualization)

### Project structure

```
loki-ui-frontend/
  src/
    api/
      stream.ts          # POST /api/loki/stream → SSE
      query.ts           # POST /api/loki/query
      queryRange.ts      # POST /api/loki/queryRange
      analysis.ts        # GET /api/loki/analysis/stream → SSE
      client.ts          # base URL from VITE_API_URL env var
    pages/
      StreamPage.tsx
      RangePage.tsx
      LogQLPage.tsx
      AnalysisPage.tsx
    components/
      LogTable.tsx        # virtualized table: timestamp | line | labels
      LogEntry.tsx        # detail drawer/modal on row click
      FilterForm.tsx      # dynamic field/operator/value rows (StreamPage)
      QueryEditor.tsx     # monospace textarea for LogQL input
    hooks/
      useStream.ts        # manages SSE connection + log buffer for StreamPage
      useQueryRange.ts    # fetch + state for RangePage
      useAnalysis.ts      # SSE connection for AnalysisPage
    types.ts              # TypeScript types mirroring Java models
    App.tsx               # AppShell + Router
    main.tsx
  .env.example            # VITE_API_URL=http://localhost:8080
```

### Routes

| Path | Page | Description |
|------|------|-------------|
| `/stream` | StreamPage | Real-time SSE log tail with filter form |
| `/range` | RangePage | Historical query by time range |
| `/logql` | LogQLPage | Free-form LogQL editor (query or queryRange) |
| `/analysis` | AnalysisPage | LLM analysis result stream |

Default redirect: `/` → `/stream`

### Layout

Mantine `AppShell` with a collapsible `Navbar` on the left containing navigation links to the four routes. Main content area renders the active page.

### Pages in detail

**StreamPage**
- `FilterForm`: dynamic list of `LogQueryFilter` rows (field / operator / value). Operators: `=`, `!=`, `=~`, `!~`. Add/remove rows. Plus optional raw `query` string and `limit` field.
- Start/Stop button to open/close the SSE connection.
- `LogTable` below with live-appended rows (newest at bottom or top, configurable).

**RangePage**
- Date-time pickers for `start` / `end`, or a `since` shortcut selector (`5m`, `15m`, `1h`, `6h`, `24h`).
- `limit`, `step`, `direction` fields.
- Optional `query_expression` textarea.
- Submit button → fetch → populate `LogTable`.

**LogQLPage**
- `QueryEditor` (monospace textarea) for raw LogQL expression.
- Mode toggle: **Instant** (query) or **Range** (queryRange). Range mode reveals start/end pickers.
- Submit → results in `LogTable`.

**AnalysisPage**
- Connect/Disconnect button for SSE stream.
- Feed of `LogAnalysisResult` cards: analysis text, model name, timestamp, collapsible list of analyzed log lines.

### Key components

**LogTable**
- Virtualized with `@tanstack/react-virtual` to handle thousands of rows without freezing.
- Columns: `timestamp` (formatted), `line` (truncated, monospace), `labels` (badge list).
- Click row → opens `LogEntry` in a Mantine `Drawer`.

**LogEntry**
- Shows full `line`, all `labels` as key-value pairs, raw timestamp.

**FilterForm**
- Each row: text input (field) + select (operator) + text input (value) + remove button.
- "Add filter" button appends a new row.

**QueryEditor**
- Mantine `Textarea` with `fontFamily: monospace`, auto-grow.

### Types (`types.ts`)

```typescript
export interface LogEvent {
  timestamp: string;
  line: string;
  labels: Record<string, string>;
}

export interface LogQueryFilter {
  field: string;
  operator: '=' | '!=' | '=~' | '!~';
  value: string;
}

export interface LogFilterStreamRequest {
  filters?: LogQueryFilter[];
  query?: string;
  start?: number;
  limit?: number;
}

export interface LokiQueryRangeRequest {
  query_expression: string;
  start?: string;
  end?: string;
  since?: string;
  limit?: number;
  step?: string;
  interval?: string;
  direction?: 'forward' | 'backward';
}

export interface LokiQueryRequest {
  query_expression: string;
  limit?: number;
  time?: string;
  direction?: 'forward' | 'backward';
}

export interface LogAnalysisResult {
  analysis: string;
  model: string;
  analyzedLogs: string[];
  timestamp: string;
}
```

### Environment

```
VITE_API_URL=http://localhost:8080
```

`client.ts` reads `import.meta.env.VITE_API_URL` as the base URL for all API calls.

---

## Development workflow

1. Start `loki-ui-server` on port `8080` (connects to a running Loki instance).
2. Start `loki-ui-frontend` dev server (`npm run dev`) on port `5173`.
3. Frontend calls backend directly (CORS configured in backend).

## Production deployment

- Build frontend: `npm run build` → `dist/` folder → serve as static files (nginx / CDN).
- Run backend as a standalone JAR.
- Set `VITE_API_URL` to the backend's public URL at build time.

---

## Out of scope (for now)

- Authentication / authorization (stub in place)
- Push logs UI (`PushLogRequest`)
- Metrics / charts from Loki metric queries
- Log search highlighting
