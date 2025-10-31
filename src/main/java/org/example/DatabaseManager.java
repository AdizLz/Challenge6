package org.example;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private static HikariDataSource dataSource;

    /**
     * Inicializa la conexión a PostgreSQL
     */
    public static void init() {
        try {
            HikariConfig config = new HikariConfig();

            // Configuración de conexión a PostgreSQL
            String dbUrl = System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/auction_store");
            String dbUser = System.getenv().getOrDefault("DB_USER", "postgres");
            String dbPassword = System.getenv().getOrDefault("DB_PASSWORD", "12345");

            // Si DB_PASSWORD está vacío, intenta leer de propiedades del sistema
            if (dbPassword.isEmpty()) {
                dbPassword = System.getProperty("db.password", "12345");
            }

            logger.info("🔌 Intentando conectar a: {}", dbUrl);
            logger.info("👤 Usuario: {}", dbUser);

            config.setJdbcUrl(dbUrl);
            config.setUsername(dbUser);
            config.setPassword(dbPassword);
            config.setDriverClassName("org.postgresql.Driver");

            // Configuración del pool de conexiones
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);

            // Propiedades adicionales para PostgreSQL
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            dataSource = new HikariDataSource(config);

            logger.info("Conexión a PostgreSQL establecida correctamente");

            // Crear las tablas
            createTables();

            // Cargar datos iniciales
            loadInitialData();

        } catch (Exception e) {
            logger.error("Error al conectar a PostgreSQL: {}", e.getMessage());
            logger.error("Verifica que PostgreSQL esté corriendo y que la contraseña sea correcta");
            throw new RuntimeException("No se pudo conectar a la base de datos", e);
        }
    }

    /**
     * Obtiene una conexión de la base de datos
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("Base de datos no inicializada. Llama a init() primero.");
        }
        return dataSource.getConnection();
    }

    /**
     * Crea las tablas necesarias
     */
    private static void createTables() {
        logger.info("Creando tablas...");

        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                id VARCHAR(50) PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                email VARCHAR(100) NOT NULL UNIQUE,
                password VARCHAR(255),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;

        String createItemsTable = """
            CREATE TABLE IF NOT EXISTS items (
                id VARCHAR(50) PRIMARY KEY,
                name VARCHAR(200) NOT NULL,
                description TEXT,
                price VARCHAR(50),
                image_url VARCHAR(500),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;

        String createOffersTable = """
            CREATE TABLE IF NOT EXISTS offers (
                id SERIAL PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                email VARCHAR(100) NOT NULL,
                item_id VARCHAR(50) NOT NULL,
                amount DECIMAL(10, 2) NOT NULL,
                status VARCHAR(20) DEFAULT 'pending',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE
            )
        """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createUsersTable);
            stmt.execute(createItemsTable);
            stmt.execute(createOffersTable);

            logger.info("Tablas creadas correctamente");

        } catch (SQLException e) {
            logger.error("Error al crear tablas", e);
            throw new RuntimeException("Error al crear tablas", e);
        }
    }

    /**
     * Carga los datos iniciales desde items.json
     */
    private static void loadInitialData() {
        logger.info("Cargando datos iniciales...");

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Verificar si ya hay items
            var rs = stmt.executeQuery("SELECT COUNT(*) FROM items");
            rs.next();
            int count = rs.getInt(1);

            if (count > 0) {
                logger.info("Los items ya están cargados ({} items)", count);
                return;
            }

            // Insertar items desde el JSON
            String[] insertQueries = {
                    """
                INSERT INTO items (id, name, description, price) VALUES 
                ('item1', 'Gorra autografiada por Peso Pluma', 
                 'Una gorra autografiada por el famoso Peso Pluma.', 
                 '$621.34 USD')
                """,
                    """
                INSERT INTO items (id, name, description, price) VALUES 
                ('item2', 'Casco autografiado por Rosalía', 
                 'Un casco autografiado por la famosa cantante Rosalía, una verdadera MOTOMAMI!', 
                 '$734.57 USD')
                """,
                    """
                INSERT INTO items (id, name, description, price) VALUES 
                ('item3', 'Chamarra de Bad Bunny', 
                 'Una chamarra de la marca favorita de Bad Bunny, autografiada por el propio artista.', 
                 '$521.89 USD')
                """,
                    """
                INSERT INTO items (id, name, description, price) VALUES 
                ('item4', 'Guitarra de Fernando Delgadillo', 
                 'Una guitarra acústica de alta calidad utilizada por el famoso cantautor Fernando Delgadillo.', 
                 '$823.12 USD')
                """,
                    """
                INSERT INTO items (id, name, description, price) VALUES 
                ('item5', 'Jersey firmado por Snoop Dogg', 
                 'Un jersey autografiado por el legendario rapero Snoop Dogg.', 
                 '$355.67 USD')
                """,
                    """
                INSERT INTO items (id, name, description, price) VALUES 
                ('item6', 'Prenda de Cardi B autografiada', 
                 'Un crop-top usado y autografiado por la famosa rapera Cardi B. en su última visita a México', 
                 '$674.23 USD')
                """,
                    """
                INSERT INTO items (id, name, description, price) VALUES 
                ('item7', 'Guitarra autografiada por Coldplay', 
                 'Una guitarra eléctrica autografiada por la popular banda británica Coldplay, un día antes de su concierto en Monterrey en 2022.', 
                 '$458.91 USD')
                """
            };

            for (String query : insertQueries) {
                stmt.execute(query);
            }

            logger.info("Items cargados correctamente (7 items)");

        } catch (SQLException e) {
            logger.warn("Error al cargar datos iniciales: {}", e.getMessage());
        }
    }

    /**
     * Cierra la conexión a la base de datos
     */
    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Conexión a PostgreSQL cerrada");
        }
    }

    /**
     * Método de prueba - ejecuta una consulta simple
     */
    public static void testConnection() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            var rs = stmt.executeQuery("SELECT version()");
            if (rs.next()) {
                logger.info("PostgreSQL conectado: {}", rs.getString(1));
            }

        } catch (SQLException e) {
            logger.error("❌ Error en test de conexión", e);
        }
    }
}