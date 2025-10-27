# Reto6 - API de usuarios (Spark Java)

Small REST API that manages users in memory (Sprint 1 release).

## Description

This API allows you to manage users using an in-memory storage (ConcurrentHashMap).
You can list, create, update, query, and delete users using RESTful endpoints.

The goal is to practice building APIs with Java 17 and Spark Java.

Minimum requirements:
- Java 17
- Maven

How to compile and run (Windows):

```cmd
cd C:\Users\Soporte\Downloads\Reto6
mvn clean package -DskipTests
java -jar target\Reto6-1.0-SNAPSHOT.jar
```
## User JSON Format

```cmd
{
  "id": "1",
  "name": "John Doe",
  "email": "john@example.com"
}
```

Endpoints:
- GET /users — List all users
- GET /users/:id — Retrieves the user with id
- POST /users/:id — Adds a user (JSON body)
- PUT /users/:id — Edits an existing user (JSON body)
- OPTIONS /users/:id — Checks if the user exists
- DELETE /users/:id — Deletes a user

Notes:
- Storage is in memory (ConcurrentHashMap). There is a seed user with id "1".
- See `DECISIONS.md` for design decisions.

http://localhost:55603/users
 — GET → [] (lista vacía de usuarios)

http://localhost:55603/users/1
 — GET → {"message":"User not found"}

http://localhost:55603/items
 — GET → Lista de artículos, ejemplo:
