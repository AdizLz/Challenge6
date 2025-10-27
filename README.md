# Reto6 - API de usuarios (Spark Java)

Pequeña API REST que gestiona usuarios en memoria (Entrega sprint 1).

Requisitos mínimos:
- Java 17
- Maven

Cómo compilar y ejecutar (Windows):

```cmd
cd C:\Users\Soporte\Downloads\Reto6
mvn clean package -DskipTests
java -jar target\Reto6-1.0-SNAPSHOT.jar
```

Endpoints:
- GET /users — Lista todos los usuarios
- GET /users/:id — Recupera el usuario con id
- POST /users/:id — Añade un usuario (body JSON)
- PUT /users/:id — Edita un usuario existente (body JSON)
- OPTIONS /users/:id — Comprueba si existe el usuario
- DELETE /users/:id — Elimina un usuario

Notas:
- El almacenamiento es en memoria (ConcurrentHashMap). Hay un usuario semilla con id "1".
- Ver `DECISIONS.md` para decisiones de diseño.

