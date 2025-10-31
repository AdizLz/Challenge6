package org.example;

import static spark.Spark.*;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.ModelAndView;
import spark.template.mustache.MustacheTemplateEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    private static final Gson gson = new Gson();
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        // Puerto configurable mediante variable de entorno PORT, por defecto 55603
        String portEnv = System.getenv("PORT");
        if (portEnv != null && !portEnv.isBlank()) {
            try {
                port(Integer.parseInt(portEnv));
                logger.info("Puerto configurado desde PORT env: {}", portEnv);
            } catch (NumberFormatException e) {
                logger.warn("Valor de PORT inválido ('{}'), usando 55603 por defecto", portEnv);
                port(55603);
            }
        } else {
            port(55603);
        }

        // ============================================
        // 🔥 INICIALIZAR BASE DE DATOS POSTGRESQL
        // ============================================
        try {
            logger.info("🚀 Inicializando base de datos PostgreSQL...");
            DatabaseManager.init();
            DatabaseManager.testConnection();
            logger.info("✅ Base de datos lista para usar");
        } catch (Exception e) {
            logger.error("❌ Error crítico al inicializar base de datos", e);
            logger.error("💡 Verifica que PostgreSQL esté corriendo y la contraseña sea correcta");
            System.exit(1); // Salir si no hay base de datos
        }

        // Agregar shutdown hook para cerrar conexión al salir
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("🔌 Cerrando conexión a base de datos...");
            DatabaseManager.close();
        }));

        // Inicializar servicios (ahora usan PostgreSQL)
        UserService service = new UserService();
        ItemService itemService = new ItemService();
        OfferService offerService = new OfferService();

        // Configurar carpeta de archivos estáticos (CSS, JS)
        staticFiles.location("/public");

        // ============================================
        // RUTAS JSON (API)
        // ============================================
        before("/api/*", (req, res) -> res.type("application/json"));

        // --- RUTAS DE USUARIOS ---
        path("/users", () -> {
            get("", (req, res) -> {
                res.type("application/json");
                return gson.toJson(service.getAll());
            });

            get("/:id", (req, res) -> {
                res.type("application/json");
                String id = req.params(":id");
                User u = service.get(id);
                if (u == null) {
                    res.status(404);
                    return gson.toJson(new Message("User not found"));
                }
                return gson.toJson(u);
            });

            post("/:id", (req, res) -> {
                res.type("application/json");
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
                } catch (RuntimeException e) {
                    res.status(500);
                    return gson.toJson(new Message("Database error: " + e.getMessage()));
                }
            });

            put("/:id", (req, res) -> {
                res.type("application/json");
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
                } catch (RuntimeException e) {
                    res.status(500);
                    return gson.toJson(new Message("Database error: " + e.getMessage()));
                }
            });

            options("/:id", (req, res) -> {
                res.type("application/json");
                String id = req.params(":id");
                if (service.exists(id)) {
                    res.status(200);
                    return gson.toJson(new Message("Exists"));
                } else {
                    res.status(404);
                    return gson.toJson(new Message("Not found"));
                }
            });

            delete("/:id", (req, res) -> {
                res.type("application/json");
                String id = req.params(":id");
                if (!service.exists(id)) {
                    res.status(404);
                    return gson.toJson(new Message("User not found"));
                }
                try {
                    service.delete(id);
                    res.status(204);
                    return "";
                } catch (RuntimeException e) {
                    res.status(500);
                    return gson.toJson(new Message("Database error: " + e.getMessage()));
                }
            });
        });

        // --- RUTAS API DE ITEMS (JSON) ---
        path("/api/items", () -> {
            get("", (req, res) -> {
                res.type("application/json");
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
                res.type("application/json");
                String id = req.params(":id");
                Item it = itemService.get(id);
                if (it == null) {
                    res.status(404);
                    return gson.toJson(new Message("Item not found"));
                }
                return gson.toJson(it);
            });

            // NUEVO: POST para crear items
            post("", (req, res) -> {
                res.type("application/json");
                try {
                    Item item = gson.fromJson(req.body(), Item.class);
                    if (item == null || item.getId() == null || item.getName() == null) {
                        res.status(400);
                        return gson.toJson(new Message("Invalid item data"));
                    }
                    if (itemService.exists(item.getId())) {
                        res.status(409);
                        return gson.toJson(new Message("Item already exists"));
                    }
                    itemService.add(item);
                    res.status(201);
                    return gson.toJson(item);
                } catch (JsonSyntaxException e) {
                    res.status(400);
                    return gson.toJson(new Message("Invalid JSON"));
                } catch (RuntimeException e) {
                    res.status(500);
                    return gson.toJson(new Message("Database error: " + e.getMessage()));
                }
            });

            // NUEVO: PUT para actualizar items
            put("/:id", (req, res) -> {
                res.type("application/json");
                String id = req.params(":id");
                if (!itemService.exists(id)) {
                    res.status(404);
                    return gson.toJson(new Message("Item not found"));
                }
                try {
                    Item item = gson.fromJson(req.body(), Item.class);
                    if (item == null) {
                        res.status(400);
                        return gson.toJson(new Message("Invalid JSON"));
                    }
                    itemService.update(id, item);
                    return gson.toJson(item);
                } catch (JsonSyntaxException e) {
                    res.status(400);
                    return gson.toJson(new Message("Invalid JSON"));
                } catch (RuntimeException e) {
                    res.status(500);
                    return gson.toJson(new Message("Database error: " + e.getMessage()));
                }
            });

            // NUEVO: DELETE para eliminar items
            delete("/:id", (req, res) -> {
                res.type("application/json");
                String id = req.params(":id");
                if (!itemService.exists(id)) {
                    res.status(404);
                    return gson.toJson(new Message("Item not found"));
                }
                try {
                    itemService.delete(id);
                    res.status(204);
                    return "";
                } catch (RuntimeException e) {
                    res.status(500);
                    return gson.toJson(new Message("Database error: " + e.getMessage()));
                }
            });
        });

        // --- RUTAS DE OFERTAS (API) ---
        path("/api/offers", () -> {
            // POST: Crear una nueva oferta
            post("", (req, res) -> {
                res.type("application/json");
                try {
                    Offer offer = gson.fromJson(req.body(), Offer.class);

                    if (offer == null || offer.getName() == null ||
                            offer.getEmail() == null || offer.getId() == null) {
                        res.status(400);
                        return gson.toJson(new Message("Invalid offer data"));
                    }

                    // Verificar que el item existe
                    if (!itemService.exists(offer.getId())) {
                        res.status(404);
                        return gson.toJson(new Message("Item not found"));
                    }

                    offerService.add(offer);
                    res.status(201);
                    return gson.toJson(offer);

                } catch (JsonSyntaxException e) {
                    res.status(400);
                    return gson.toJson(new Message("Invalid JSON"));
                } catch (RuntimeException e) {
                    res.status(500);
                    return gson.toJson(new Message("Database error: " + e.getMessage()));
                }
            });

            // GET: Obtener todas las ofertas
            get("", (req, res) -> {
                res.type("application/json");
                Map<String, Object> response = new HashMap<>();
                response.put("offers", offerService.getAll());
                return gson.toJson(response);
            });

            // NUEVO: GET ofertas por item
            get("/item/:itemId", (req, res) -> {
                res.type("application/json");
                String itemId = req.params(":itemId");
                List<Offer> offers = offerService.getByItemId(itemId);
                Map<String, Object> response = new HashMap<>();
                response.put("itemId", itemId);
                response.put("count", offers.size());
                response.put("offers", offers);
                return gson.toJson(response);
            });
        });

        // ============================================
        // RUTAS HTML (VISTAS CON MUSTACHE)
        // ============================================

        // Vista: Lista de items
        get("/items", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("items", itemService.getAll());
            return new ModelAndView(model, "items-list.mustache");
        }, new MustacheTemplateEngine());

        // Vista: Detalle de un item
        get("/items/:id", (req, res) -> {
            String id = req.params(":id");
            Item item = itemService.get(id);

            Map<String, Object> model = new HashMap<>();

            if (item == null) {
                res.status(404);
                model.put("id", "");
                model.put("name", "Item no encontrado");
                model.put("description", "");
                model.put("price", "");
                model.put("errorMessage", "Item con id '" + id + "' no fue encontrado.");
                return new ModelAndView(model, "item-detail.mustache");
            }

            model.put("id", item.getId());
            model.put("name", item.getName());
            model.put("description", item.getDescription());
            model.put("price", item.getPrice());

            // NUEVO: Agregar información de ofertas
            List<Offer> offers = offerService.getByItemId(id);
            model.put("offerCount", offers.size());

            Offer highestOffer = offerService.getHighestOffer(id);
            if (highestOffer != null) {
                model.put("highestOffer", highestOffer.getAmount());
            }

            return new ModelAndView(model, "item-detail.mustache");
        }, new MustacheTemplateEngine());

        // Vista: Lista de ofertas (HTML)
        get("/offers", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            List<Map<String, Object>> viewOffers = new ArrayList<>();

            for (Offer o : offerService.getAll()) {
                Map<String, Object> m = new HashMap<>();
                m.put("name", o.getName());
                m.put("email", o.getEmail());
                m.put("id", o.getId());
                m.put("amount", o.getAmount());

                Item it = itemService.get(o.getId());
                m.put("itemName", it != null ? it.getName() : "(Item no encontrado)");

                viewOffers.add(m);
            }

            model.put("offers", viewOffers);
            model.put("totalOffers", viewOffers.size());

            return new ModelAndView(model, "offers-list.mustache");
        }, new MustacheTemplateEngine());

        // Página de inicio - redirige a items
        get("/", (req, res) -> {
            res.redirect("/items");
            return null;
        });

        // NUEVO: Ruta de health check
        get("/health", (req, res) -> {
            res.type("application/json");
            Map<String, Object> health = new HashMap<>();
            health.put("status", "UP");
            health.put("database", "PostgreSQL");

            try {
                DatabaseManager.testConnection();
                health.put("dbConnection", "OK");
            } catch (Exception e) {
                health.put("dbConnection", "ERROR: " + e.getMessage());
            }

            return gson.toJson(health);
        });

        // ============================================
        // MANEJO DE ERRORES
        // ============================================

        // Error 404 - Not Found
        notFound((req, res) -> {
            res.type("application/json");
            res.status(404);
            return gson.toJson(new Message("Endpoint not found: " + req.pathInfo()));
        });

        // Error 500 - Internal Server Error
        internalServerError((req, res) -> {
            res.type("application/json");
            res.status(500);
            return gson.toJson(new Message("Internal server error"));
        });

        // Manejo de excepciones generales
        exception(Exception.class, (e, req, res) -> {
            res.type("application/json");
            res.status(500);
            logger.error("Unhandled exception", e);
            res.body(gson.toJson(new Message("Server error: " + e.getMessage())));
        });

        // Log de todas las peticiones
        after((req, res) -> {
            logger.info("{} {} -> {}", req.requestMethod(), req.pathInfo(), res.status());
        });

        System.out.println("===========================================");
        System.out.println("🚀 Server started on port: " + port());
        System.out.println("🌐 Web interface: http://localhost:" + port() + "/items");
        System.out.println("📡 API endpoints: http://localhost:" + port() + "/api/");
        System.out.println("💚 Health check: http://localhost:" + port() + "/health");
        System.out.println("🗄️  Database: PostgreSQL (auction_store)");
        System.out.println("===========================================");
    }

    static class Message {
        private final String message;
        Message(String message) { this.message = message; }
        public String getMessage() { return message; }
    }
}

