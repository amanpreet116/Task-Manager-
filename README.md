# Task Manager

Spring Boot task and notification services with a React frontend. The task service uses PostgreSQL, Kafka, and LocalStack (for S3 attachments). The notification service uses its own PostgreSQL database and Kafka.

## Requirements

- Java 17 or newer and Maven
- Docker with Docker Compose
- Node.js 18 or newer and npm
- Free local ports: 5433, 8080, 8081, 9092, 4566, and 5173 (8090 for Kafka UI)

Run the following commands from the repository root unless a step says otherwise.

## First-time setup

Start PostgreSQL, Kafka, and LocalStack:

```sh
docker compose up -d
```

Compose maps PostgreSQL to host port **5433** to avoid conflicts with a local PostgreSQL installation on 5432. On first startup, the database initialization script creates both `taskmanager_db` and `notification_db`. Compose stores PostgreSQL data in the `postgres-data` volume. If you previously created a separate `task-manager-postgres` container, leave it stopped; Compose uses its own database container.

Build and install all Maven modules, including the shared events library. Run this from the **repository root**, before starting either Spring service:

```sh
mvn install -DskipTests
```

If Maven later reports `Could not find artifact com.taskmanager:shared-events:jar:0.0.1-SNAPSHOT`, install that local module directly and retry the service command:

```sh
mvn -f shared-events/pom.xml clean install -DskipTests
```

Install frontend dependencies:

```sh
cd frontend
cp .env.example .env
npm install
cd ..
```

The default `.env` uses the Vite proxy to reach the task API on port 8080.

## Run the application

After first-time setup, start the infrastructure from the repository root:

```sh
docker compose up -d
```

Open **three separate terminals** in the repository root and run one command in each:

**Terminal 1 — task API (port 8080):**

```sh
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/taskmanager_db \
  mvn -f task-manager/pom.xml spring-boot:run
```

**Terminal 2 — notification API (port 8081):**

```sh
NOTIFICATION_DB_URL=jdbc:postgresql://localhost:5433/notification_db \
  mvn -f notification-service/pom.xml spring-boot:run
```

**Terminal 3 — React frontend (port 5173):**

```sh
cd frontend
npm run dev
```

Open [http://localhost:5173](http://localhost:5173) and create an account. The Compose PostgreSQL volume starts with no users; accounts from another local PostgreSQL instance are not available here. The frontend proxies task and authentication requests to port 8080. Its recent activity list is local to the browser and does not call the notification service. Kafka UI is available at [http://localhost:8090](http://localhost:8090).

If login cannot reach the server, confirm the task API started successfully. This should return HTTP `401` for a request without a token:

```sh
curl -s -o /dev/null -w '%{http_code}\n' http://localhost:8080/api/tasks/mine
```

The default database credentials and local service addresses are defined in the Spring `application.yml` files. Change them through the corresponding environment variables if your local setup differs.

## Stop

Stop the three foreground processes with `Ctrl+C`, then stop the containers:

```sh
docker compose down
```

`docker compose down` preserves the PostgreSQL, Kafka, and LocalStack volumes. To start again, follow **Run the application**. Avoid `docker compose down -v` unless you intend to delete local data.

## Production frontend build

```sh
cd frontend
npm run build
```

The static bundle is written to `frontend/dist`. In production, set `VITE_API_BASE_URL` to the task API origin before building and configure Spring CORS to allow the frontend origin. The activity page does not call the notification service.

## Frontend checks

```sh
cd frontend
npm test
npm run lint
npm run format:check
npm run build
```

See [frontend/README.md](frontend/README.md) for the folder structure, backend assumptions, and CORS notes. The frontend activity page records local actions in the current tab and does not call the notification service.
