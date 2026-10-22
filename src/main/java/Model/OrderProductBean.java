package Model;

import java.io.Serializable;

public class OrderProductBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private int product, quantity, order;
    private double price;

    public OrderProductBean() {
        // Costruttore vuoto richiesto dalla specifica JavaBeans
    }

    public int getProduct() {
        return product;
    }

    public void setProduct(int product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}