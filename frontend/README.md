# Task Manager frontend

React 18 + Vite + TypeScript client for the Spring Boot task service. Full infrastructure and service startup instructions are in the [project README](../README.md).

## Run

From this directory:

```sh
cp .env.example .env
npm install
npm run dev
```

Open <http://localhost:5173>. Leave `VITE_API_BASE_URL` empty in local development: Vite proxies `/api` to `http://localhost:8080`. Set it to the task API origin for production builds. Restart Vite after changing `.env`.

```sh
npm test
npm run lint
npm run format:check
npm run build
```

`npm run build` runs strict TypeScript checking and writes `dist/`. In production, serve `dist/` through a web server and allow its origin in Spring CORS configuration if the API is on a different origin. For example, add a Spring Security `CorsConfigurationSource` allowing your frontend origin, `GET`, `POST`, `PUT`, `DELETE`, and `OPTIONS`, and the `Authorization` and `Content-Type` headers. The Vite proxy avoids browser CORS checks in local development.

## Structure

- `src/api`: one Axios client, backend contract, auth and task requests.
- `src/features`: auth pages, task views, and session-local activity.
- `src/components/ui`: dialogs, badges, toasts, and state displays.
- `src/hooks`: TanStack Query task hooks and mutations.
- `src/lib`: token storage, validation, and formatting.
- `src/routes`: protected routes, layout, and 404 page.
- `src/test`: MSW setup and test helpers.

## Backend assumptions to verify

All paths, status values, title limit, permissions, and outbox delay are centralized in `src/api/backend.ts`; DTOs are in `src/types/index.ts`.

1. `POST /api/auth/login` accepts `{ email, password }` and returns `{ token }`; `POST /api/auth/register` accepts the same fields. Registration creates a `USER` account. The register page signs in after registration.
2. Authentication is a JWT Bearer token. JWT claims contain `sub` and `email` (email address), `role` (`USER` or `ADMIN`), and `exp`. The client stores the token in tab-scoped `sessionStorage`. A server-issued HttpOnly cookie would provide stronger protection against token theft via injected JavaScript, but this backend uses Bearer tokens.
3. `GET /api/tasks/mine` returns an array of tasks owned by the authenticated user. The backend's `GET /api/tasks` is admin-only, so the dashboard uses `/mine`.
4. Task requests contain `title`, `description`, and `status`; title is required with a 255-character maximum. Status values are `TODO`, `IN_PROGRESS`, and `DONE`.
5. Task responses contain `id`, `title`, `description`, `status`, `ownerId`, `createdAt`, `updatedAt`, and `subtasks`. Dates are ISO-like strings.
6. `GET /api/tasks/{id}` and `PUT /api/tasks/{id}` are available to the owner; only admins may call `DELETE /api/tasks/{id}`. The UI hides delete for regular users. Server authorization remains authoritative.
7. The outbox worker processes events on a roughly 30-second schedule. There is no live notification delivery status in this UI. Recent activity records only actions completed in the current browser session. To replace it with a server feed, add an authenticated, owner-scoped `GET /api/users/me/activity` endpoint, ideally paginated; SSE or WebSocket can later invalidate its query.
8. The backend returns errors as JSON with a `message` field. The API is on port 8080 locally.

This frontend intentionally does not call the notification service. Its existing notification endpoint is a global feed and does not match the user-scoped activity requirement.
