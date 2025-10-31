package org.example;

public class Offer {
    // dbId corresponde al id autogenerado en la tabla offers (opcional en JSON)
    private Long dbId;

    // En la API actual el campo 'id' representa el id del ITEM asociado
    private String id;
    private String name;
    private String email;
    private double amount;

    public Offer() { }

    public Offer(String id, String name, String email, double amount) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.amount = amount;
    }

    public Long getDbId() { return dbId; }
    public void setDbId(Long dbId) { this.dbId = dbId; }

    // NOTE: Mantener getId() para referirse al item id, así coincide con el uso en Main
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
}

