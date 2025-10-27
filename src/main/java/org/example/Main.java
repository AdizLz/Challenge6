package org.example;

import static spark.Spark.*;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    private static final Gson gson = new Gson();
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        port(55603); // Spark asigna un puerto
        UserService service = new UserService();
        ItemService itemService = new ItemService();

        before((req, res) -> res.type("application/json"));

        // --- RUTAS DE USUARIOS ---
        get("/users", (req, res) -> gson.toJson(service.getAll()));

        get("/users/:id", (req, res) -> {
            String id = req.params(":id");
            User u = service.get(id);
            if (u == null) {
                res.status(404);
                return gson.toJson(new Message("User not found"));
            }
            return gson.toJson(u);
        });

        post("/users/:id", (req, res) -> {
            String id = req.params(":id");
            if (service.exists(id)) {
                res.status(409);
                return gson.toJson(new Message("User already exists"));
            }
            try {
                User user = gson.fromJson(req.body(), User.class);
                if (user == null) {
                    res.status(400);
                    return gson.toJson(new Message("Invalid JSON or empty body"));
                }
                user.setId(id);
                service.add(user);
                res.status(201);
                return gson.toJson(user);
            } catch (JsonSyntaxException e) {
                res.status(400);
                return gson.toJson(new Message("Invalid JSON"));
            }
        });

        put("/users/:id", (req, res) -> {
            String id = req.params(":id");
            if (!service.exists(id)) {
                res.status(404);
                return gson.toJson(new Message("User not found"));
            }
            try {
                User user = gson.fromJson(req.body(), User.class);
                if (user == null) {
                    res.status(400);
                    return gson.toJson(new Message("Invalid JSON or empty body"));
                }
                user.setId(id);
                service.update(id, user);
                return gson.toJson(user);
            } catch (JsonSyntaxException e) {
                res.status(400);
                return gson.toJson(new Message("Invalid JSON"));
            }
        });

        options("/users/:id", (req, res) -> {
            String id = req.params(":id");
            if (service.exists(id)) {
                res.status(200);
                return gson.toJson(new Message("Exists"));
            } else {
                res.status(404);
                return gson.toJson(new Message("Not found"));
            }
        });

        delete("/users/:id", (req, res) -> {
            String id = req.params(":id");
            if (!service.exists(id)) {
                res.status(404);
                return gson.toJson(new Message("User not found"));
            }
            service.delete(id);
            res.status(204);
            return "";
        });

        // --- RUTAS DE ITEMS ---
        path("/items", () -> {
            get("", (req, res) -> {
                List<Map<String, String>> out = new ArrayList<>();
                for (Item it : itemService.getAll()) {
                    Map<String, String> m = new HashMap<>();
                    m.put("id", it.getId());
                    m.put("name", it.getName());
                    m.put("price", it.getPrice());
                    out.add(m);
                }
                return gson.toJson(out);
            });

            get("/:id", (req, res) -> {
                String id = req.params(":id");
                Item it = itemService.get(id);
                if (it == null) {
                    res.status(404);
                    return gson.toJson(new Message("Item not found"));
                }
                Map<String, String> m = new HashMap<>();
                m.put("id", it.getId());
                m.put("description", it.getDescription());
                return gson.toJson(m);
            });
        });

        after((req, res) -> logger.info("{} {} -> {}", req.requestMethod(), req.pathInfo(), res.status()));

        // Mostrar puerto asignado
        System.out.println("Server started on port: " + port());
    }

    static class Message {
        private final String message;
        Message(String message) { this.message = message; }
        public String getMessage() { return message; }
    }
}
