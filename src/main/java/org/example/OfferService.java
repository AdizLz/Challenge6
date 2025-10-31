package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class OfferService {
    private static final Logger logger = LoggerFactory.getLogger(OfferService.class);
    private static final Gson gson = new Gson();

    /**
     * Agrega una nueva oferta (solo en PostgreSQL)
     */
    public void add(Offer offer) {
        String sql = "INSERT INTO offers (name, email, item_id, amount) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, offer.getName());
            pstmt.setString(2, offer.getEmail());
            pstmt.setString(3, offer.getId());
            pstmt.setDouble(4, offer.getAmount());

            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int generatedId = rs.getInt(1);
                        logger.info("✅ Oferta creada con ID: {} para item: {}", generatedId, offer.getId());
                    }
                }
            }

        } catch (SQLException e) {
            logger.error("❌ Error al crear oferta", e);
            throw new RuntimeException("Error al crear oferta: " + e.getMessage());
        }
    }

    /**
     * Lee ofertas desde el archivo ofertas.json
     */
    private List<Offer> getOffersFromJson() {
        List<Offer> offersFromJson = new ArrayList<>();

        try (InputStream is = getClass().getResourceAsStream("/ofertas.json")) {
            if (is == null) {
                logger.debug("Archivo ofertas.json no encontrado");
                return offersFromJson;
            }

            InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            Map<String, List<Offer>> data = gson.fromJson(reader,
                    new TypeToken<Map<String, List<Offer>>>(){}.getType());

            if (data != null && data.get("offers") != null) {
                offersFromJson = data.get("offers");
                logger.debug("{} ofertas leídas desde ofertas.json", offersFromJson.size());
            }

        } catch (Exception e) {
            logger.warn("Error al leer ofertas.json: {}", e.getMessage());
        }

        return offersFromJson;
    }

    /**
     * Obtiene ofertas desde PostgreSQL
     */
    private List<Offer> getOffersFromDatabase() {
        List<Offer> offers = new ArrayList<>();
        String sql = "SELECT name, email, item_id, amount, created_at FROM offers ORDER BY created_at DESC";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Offer offer = new Offer();
                offer.setName(rs.getString("name"));
                offer.setEmail(rs.getString("email"));
                offer.setId(rs.getString("item_id"));
                offer.setAmount(rs.getDouble("amount"));
                offers.add(offer);
            }

            logger.debug("💾 {} ofertas leídas desde PostgreSQL", offers.size());

        } catch (SQLException e) {
            logger.error("Error al obtener ofertas desde PostgreSQL", e);
        }

        return offers;
    }

    /**
     * Obtiene TODAS las ofertas (JSON + PostgreSQL)
     */
    public List<Offer> getAll() {
        List<Offer> allOffers = new ArrayList<>();

        // 1. Primero agregar ofertas del JSON
        List<Offer> jsonOffers = getOffersFromJson();
        allOffers.addAll(jsonOffers);

        // 2. Luego agregar ofertas de PostgreSQL
        List<Offer> dbOffers = getOffersFromDatabase();
        allOffers.addAll(dbOffers);

        logger.info("📋 Total ofertas: {} (JSON: {}, DB: {})",
                allOffers.size(), jsonOffers.size(), dbOffers.size());

        return allOffers;
    }

    /**
     * Obtiene ofertas por item (JSON + PostgreSQL)
     */
    public List<Offer> getByItemId(String itemId) {
        List<Offer> allOffers = new ArrayList<>();

        // 1. Filtrar ofertas del JSON por item
        List<Offer> jsonOffers = getOffersFromJson();
        for (Offer offer : jsonOffers) {
            if (offer.getId().equals(itemId)) {
                allOffers.add(offer);
            }
        }

        // 2. Obtener ofertas de PostgreSQL para ese item
        String sql = "SELECT name, email, item_id, amount FROM offers WHERE item_id = ? ORDER BY amount DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, itemId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Offer offer = new Offer();
                    offer.setName(rs.getString("name"));
                    offer.setEmail(rs.getString("email"));
                    offer.setId(rs.getString("item_id"));
                    offer.setAmount(rs.getDouble("amount"));
                    allOffers.add(offer);
                }
            }

        } catch (SQLException e) {
            logger.error("Error al obtener ofertas por item", e);
        }

        // 3. Ordenar todas por monto (descendente)
        allOffers.sort((o1, o2) -> Double.compare(o2.getAmount(), o1.getAmount()));

        logger.debug("{} ofertas para item {}", allOffers.size(), itemId);

        return allOffers;
    }

    /**
     * Obtiene la oferta más alta para un item (JSON + PostgreSQL)
     */
    public Offer getHighestOffer(String itemId) {
        List<Offer> allOffers = getByItemId(itemId);

        if (allOffers.isEmpty()) {
            return null;
        }

        // Ya están ordenadas por monto descendente
        Offer highest = allOffers.get(0);
        logger.debug("Oferta más alta para {}: ${}", itemId, highest.getAmount());

        return highest;
    }

    /**
     * Cuenta ofertas por item (JSON + PostgreSQL)
     */
    public int countByItemId(String itemId) {
        return getByItemId(itemId).size();
    }
}