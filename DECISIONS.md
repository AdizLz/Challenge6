Decisions and rationale

1) Packaging
- Chosen: JAR (executable fat JAR produced with maven-shade-plugin).
- Rationale: Easy to run and distribute; meets the requirement to deliver runnable code.

2) Dependencies
- Spark Java (2.9.4): lightweight web framework required by the task.
- Gson (2.10.1): simple JSON serialization/deserialization.
- Logback (1.2.11): SLF4J implementation for logging.
- Rationale: Minimal set to implement REST endpoints and logging.

3) Data storage
- In-memory ConcurrentHashMap in `UserService`.
- Rationale: Sprint deliverable doesn't require persistence; in-memory store keeps implementation simple and deterministic for tests.
- Note: No seeded users are present by default; the store starts empty to avoid surprises in automated tests.

4) Routes and HTTP semantics
- Implemented routes exactly as requested using Spark verbs and paths.
- Status codes chosen:
  - 200 OK for successful GET and OPTIONS (when found)
  - 201 Created for successful POST
  - 204 No Content for successful DELETE
  - 404 Not Found when resource is absent
  - 409 Conflict when POSTing an already existing user
  - 400 Bad Request for invalid JSON
- Rationale: Aligns with common REST practices and provides clear client feedback.

5) Port
- Server runs on port 55603 (configured in `Main.java`).
- Rationale: Chosen to avoid conflicts and match the environment where the project was developed.

6) Static files (CSS/JS)
- Location: `src/main/resources/public` (served with `staticFiles.location("/public")`).
- Rationale: Standard location for Spark static files. Previously `public` was inside `templates/` — moved to `resources/public` so Spark serves them correctly.

7) Limitations & Next steps
- No persistent storage (file or DB) — add a repository layer for production.
- No authentication/authorization — add JWT or OAuth if required.
- No validation beyond basic JSON parsing — add field validation.
- Add unit/integration tests (e.g., JUnit + RestAssured) for CI.

If any of these choices should be different to match team conventions (port, dependency versions, packaging), I can update the code and document the change here with the justification.
