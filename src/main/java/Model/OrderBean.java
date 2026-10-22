package Model;

import java.io.Serializable;

public class OrderBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id, user;
    private double total;

    public OrderBean() {
        // Costruttore vuoto richiesto dalla specifica JavaBeans
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUser() {
        return user;
    }

    public void setUser(int user) {
        this.user = user;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}