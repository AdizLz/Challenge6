package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    /**
     * Obtiene todos los usuarios
     */
    public Collection<User> getAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, name, email, created_at FROM users ORDER BY created_at DESC";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getString("id"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                users.add(user);
            }

            logger.debug("📋 Se obtuvieron {} usuarios", users.size());

        } catch (SQLException e) {
            logger.error("❌ Error al obtener usuarios", e);
        }

        return users;
    }

    /**
     * Obtiene un usuario por ID
     */
    public User get(String id) {
        String sql = "SELECT id, name, email FROM users WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getString("id"));
                    user.setName(rs.getString("name"));
                    user.setEmail(rs.getString("email"));

                    logger.debug("✅ Usuario encontrado: {}", id);
                    return user;
                }
            }

        } catch (SQLException e) {
            logger.error("❌ Error al buscar usuario: {}", id, e);
        }

        logger.debug("⚠️ Usuario no encontrado: {}", id);
        return null;
    }

    /**
     * Agrega un nuevo usuario
     */
    public void add(User user) {
        String sql = "INSERT INTO users (id, name, email) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getId());
            pstmt.setString(2, user.getName());
            pstmt.setString(3, user.getEmail());

            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                logger.info("✅ Usuario creado: {} ({})", user.getName(), user.getId());
            }

        } catch (SQLException e) {
            logger.error("❌ Error al crear usuario: {}", user.getId(), e);
            throw new RuntimeException("Error al crear usuario: " + e.getMessage());
        }
    }

    /**
     * Actualiza un usuario existente
     */
    public void update(String id, User user) {
        String sql = "UPDATE users SET name = ?, email = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, id);

            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                logger.info("✅ Usuario actualizado: {}", id);
            } else {
                logger.warn("⚠️ No se encontró usuario para actualizar: {}", id);
            }

        } catch (SQLException e) {
            logger.error("❌ Error al actualizar usuario: {}", id, e);
            throw new RuntimeException("Error al actualizar usuario: " + e.getMessage());
        }
    }

    /**
     * Elimina un usuario
     */
    public void delete(String id) {
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);

            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                logger.info("✅ Usuario eliminado: {}", id);
            } else {
                logger.warn("⚠️ No se encontró usuario para eliminar: {}", id);
            }

        } catch (SQLException e) {
            logger.error("❌ Error al eliminar usuario: {}", id, e);
            throw new RuntimeException("Error al eliminar usuario: " + e.getMessage());
        }
    }

    /**
     * Verifica si un usuario existe
     */
    public boolean exists(String id) {
        String sql = "SELECT COUNT(*) FROM users WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            logger.error("❌ Error al verificar existencia de usuario: {}", id, e);
        }

        return false;
    }

    /**
     * Busca usuarios por nombre o email
     */
    public Collection<User> search(String query) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, name, email FROM users WHERE LOWER(name) LIKE ? OR LOWER(email) LIKE ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + query.toLowerCase() + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    User user = new User();
                    user.setId(rs.getString("id"));
                    user.setName(rs.getString("name"));
                    user.setEmail(rs.getString("email"));
                    users.add(user);
                }
            }

            logger.debug("🔍 Búsqueda '{}' encontró {} usuarios", query, users.size());

        } catch (SQLException e) {
            logger.error("❌ Error al buscar usuarios", e);
        }

        return users;
    }
}