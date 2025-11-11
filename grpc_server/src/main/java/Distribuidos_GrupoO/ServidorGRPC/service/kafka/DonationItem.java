package Distribuidos_GrupoO.ServidorGRPC.service.kafka;

public class DonationItem {
    private String category;
    private String description;
    private String quantity;

    public DonationItem() {}

    public DonationItem(String category, String description, String quantity) {
        this.category = category;
        this.description = description;
        this.quantity = quantity;
    }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }
}