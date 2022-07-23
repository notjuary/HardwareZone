package Model;

public class OrderBean {

    private int id, user;
    private double total;

    public OrderBean() {}

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
