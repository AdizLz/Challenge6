package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ItemService {
    private static final Logger logger = LoggerFactory.getLogger(ItemService.class);

    /**
     * Obtiene todos los items
     */
    public Collection<Item> getAll() {
        List<Item> items = new ArrayList<>();
        String sql = "SELECT id, name, description, price FROM items ORDER BY created_at DESC";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Item item = new Item();
                item.setId(rs.getString("id"));
                item.setName(rs.getString("name"));
                item.setDescription(rs.getString("description"));
                item.setPrice(rs.getString("price"));
                items.add(item);
            }

            logger.debug("📋 Se obtuvieron {} items", items.size());

        } catch (SQLException e) {
            logger.error("❌ Error al obtener items", e);
        }

        return items;
    }

    /**
     * Obtiene un item por ID
     */
    public Item get(String id) {
        String sql = "SELECT id, name, description, price FROM items WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Item item = new Item();
                    item.setId(rs.getString("id"));
                    item.setName(rs.getString("name"));
                    item.setDescription(rs.getString("description"));
                    item.setPrice(rs.getString("price"));

                    logger.debug("Item encontrado: {}", id);
                    return item;
                }
            }

        } catch (SQLException e) {
            logger.error("Error al buscar item: {}", id, e);
        }

        logger.debug("Item no encontrado: {}", id);
        return null;
    }

    /**
     * Verifica si un item existe
     */
    public boolean exists(String id) {
        String sql = "SELECT COUNT(*) FROM items WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            logger.error("Error al verificar existencia de item: {}", id, e);
        }

        return false;
    }

    /**
     * Agrega un nuevo item
     */
    public void add(Item item) {
        String sql = "INSERT INTO items (id, name, description, price) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, item.getId());
            pstmt.setString(2, item.getName());
            pstmt.setString(3, item.getDescription());
            pstmt.setString(4, item.getPrice());

            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                logger.info("Item creado: {} ({})", item.getName(), item.getId());
            }

        } catch (SQLException e) {
            logger.error("Error al crear item: {}", item.getId(), e);
            throw new RuntimeException("Error al crear item: " + e.getMessage());
        }
    }

    /**
     * Actualiza un item existente
     */
    public void update(String id, Item item) {
        String sql = "UPDATE items SET name = ?, description = ?, price = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, item.getName());
            pstmt.setString(2, item.getDescription());
            pstmt.setString(3, item.getPrice());
            pstmt.setString(4, id);

            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                logger.info("Item actualizado: {}", id);
            } else {
                logger.warn("No se encontró item para actualizar: {}", id);
            }

        } catch (SQLException e) {
            logger.error("Error al actualizar item: {}", id, e);
            throw new RuntimeException("Error al actualizar item: " + e.getMessage());
        }
    }

    /**
     * Elimina un item
     */
    public void delete(String id) {
        String sql = "DELETE FROM items WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);

            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                logger.info("Item eliminado: {}", id);
            } else {
                logger.warn("No se encontró item para eliminar: {}", id);
            }

        } catch (SQLException e) {
            logger.error("Error al eliminar item: {}", id, e);
            throw new RuntimeException("Error al eliminar item: " + e.getMessage());
        }
    }
}