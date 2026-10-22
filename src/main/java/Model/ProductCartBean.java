package Model;

import java.io.Serializable;

// FIX S2441: Rendi la classe Serializable
public class ProductCartBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private int quantity, id;

    public ProductCartBean() {
        // Costruttore vuoto richiesto dalla specifica JavaBeans per l'istanziazione via reflection
    }

    public int getId() {

        return id;
    }

    public void setId(int id) {

        this.id = id;
    }

    public int getQuantity() {

        return quantity;
    }

    public void setQuantity(int quantity) {

        this.quantity = quantity;
    }
}