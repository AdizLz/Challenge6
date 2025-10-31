# Reto6 — Online Store (API + UI) - Java + Spark

This repository contains an online store demo implemented with Java and the Spark framework. The README below explains how to run, test and troubleshoot the application.

Project overview
- Backend: Java 17 + Spark Java
- Templates: Mustache (HTML views)
- JSON: Gson
- Logging: SLF4J + Logback
- Persistence: PostgreSQL (recommended) or H2 embedded (fallback)

Implemented features
- Users API
  - GET /users — list users
  - GET /users/:id — get user by id
  - POST /users/:id — create user
  - PUT /users/:id — update user
  - DELETE /users/:id — delete user
  - OPTIONS /users/:id — check existence

- Items API
  - GET /api/items — list items (JSON) (returns id, name, price)
  - GET /api/items/:id — get full item (JSON)
  - GET /items — HTML view: items list
  - GET /items/:id — HTML view: item detail (includes offer form)

- Offers API
  - POST /api/offers — create an offer (persists to DB)
  - GET /api/offers — list all offers (JSON)
  - GET /api/offers/item/:itemId — list offers for a specific item (JSON)

- Frontend resources (under `src/main/resources/public`):
  - `script.js` — handles offer form and AJAX submission
  - `styles.css` — main styles

Requirements
- Java 17 (JDK)
- Maven
- PostgreSQL (optional; application falls back to H2 if Postgres is not configured)

Repository structure (relevant parts)
```
src/
  main/
    java/org/example/   (Main, DatabaseManager, UserService, ItemService, OfferService, etc.)
    resources/
      items.json
      ofertas.json
      public/ (script.js, styles.css)
      templates/ (mustache templates)
README.md
pom.xml
```

Database configuration (Postgres)
The application will use PostgreSQL when you set the `DB_URL` environment variable to a Postgres JDBC URL. If `DB_URL` is not defined, the app uses an embedded H2 database located at `./data/reto6`.

Example: your database name is `auction_store` and the password is `12345`.

On Windows (cmd.exe), in the same terminal where you will run the app, execute:

```cmd
set DB_URL=jdbc:postgresql://localhost:5432/auction_store
set DB_USER=postgres
set DB_PASS=12345
```

To set these variables permanently for new terminals, use `setx`:

```cmd
setx DB_URL "jdbc:postgresql://localhost:5432/auction_store"
setx DB_USER "postgres"
setx DB_PASS "12345"
```

Note: after `setx` you need to close and reopen the terminal to see the variables.

Create the database (if it does not exist)
If the `auction_store` database does not exist yet, create it with the `postgres` superuser:

```cmd
psql -U postgres -c "CREATE DATABASE auction_store;"
```

(Assumes `psql` is available in your PATH. You can also create the DB with pgAdmin.)

The application will automatically create the required tables (`users`, `items`, `offers`) the first time it starts.

Build and run
1) Build & package (from project root):

```cmd
mvn -DskipTests clean package
```

2) Run the generated JAR:

```cmd
java -jar target\Reto6-1.0-SNAPSHOT.jar
```

If `DB_URL` points to PostgreSQL, the app will try to connect to Postgres on startup. Check the console logs for DatabaseManager/Hikari messages if the connection fails.

Useful URLs (default)
- Web UI: http://localhost:55603/items
- API base: http://localhost:55603/api/
- Health check: http://localhost:55603/health

Endpoints and curl examples
- List items (JSON):

```cmd
curl http://localhost:55603/api/items
```

- Get item by id:

```cmd
curl http://localhost:55603/api/items/item1
```

- List users:

```cmd
curl http://localhost:55603/users
```

- Create/submit an offer (example):

```cmd
curl -X POST http://localhost:55603/api/offers -H "Content-Type: application/json" -d "{\"name\":\"Sample\",\"email\":\"sample@example.com\",\"id\":\"item1\",\"amount\":120.50}"
```

Expected response: `201 Created` and JSON with the created offer, or JSON with `{ "message": "..." }` in case of error.

Frontend usage
- Open `http://localhost:55603/items` in a browser.
- Click an item to view its detail at `/items/:id`.
- Use the "Make an Offer" button to open the offer form and submit an offer.

Persistence behavior
- Creating an offer via `/api/offers` stores the offer in the `offers` table (Postgres if configured, otherwise H2).
- `OfferService` loads initial offers from `src/main/resources/ofertas.json` if the `offers` table is empty.
- `DatabaseManager` loads `src/main/resources/items.json` into the `items` table if it is empty on startup.

Troubleshooting
- "No suitable driver" on startup:
  - Ensure `DB_URL` references Postgres and the Postgres JDBC dependency is present in `pom.xml` (it is included by default). For H2 no configuration is needed.

- Authentication failure when connecting to Postgres:
  - Verify `DB_USER` and `DB_PASS` and that the Postgres server process is running.
  - Test with `psql -U <user> -h localhost -d auction_store`.

- Items or offers not appearing:
  - Confirm that `items.json` and `ofertas.json` exist in `src/main/resources`.
  - Check logs: `DatabaseManager` and `OfferService` produce messages during startup.

Design notes (short)
- `DatabaseManager` uses HikariCP for connection pooling and detects Postgres via `DB_URL`. If not set, it falls back to H2 for local development.
- `OfferService` persists offers to the `offers` table and seeds offers from `ofertas.json` if the table is empty.
- Frontend focuses on a simple, validated client-side form that posts to `/api/offers`.

Files to review / possible improvements
- `src/main/resources/templates/public/styless.css` — looks like a typo or duplicate (`styless.css`). It's not necessary if templates reference `/styles.css` and can be removed.
- `src/main/resources/ofertas.json` — contains sample offers for initial seeding; remove or modify if you don't want seed data.

Quick test steps
1) Build and run as shown above.
2) In a second terminal, POST a sample offer and then GET /api/offers to verify persistence:

```cmd
curl -X POST http://localhost:55603/api/offers -H "Content-Type: application/json" -d "{\"name\":\"Test\",\"email\":\"test@ex.com\",\"id\":\"item1\",\"amount\":333.33}"
curl http://localhost:55603/api/offers
```

Recommended next steps (optional)
- Remove `templates/public/styless.css` to avoid confusion.
- Add unit tests for `OfferService` and `ItemService`.
- Add server-side validations (email format, amount limits).
- Push the repository to GitHub and share access with your team.

If you want me to apply any optional changes now (remove the typo CSS file, run the app locally with H2 here, etc.), tell me which option to perform and I will proceed.
