package Model;

import java.sql.*;
import java.util.ArrayList;

public class OrderDAO {

    private static final String SELECT = "SELECT ";
    private static final String COLUMNS = "ID_Ordine, Utente, Totale";

    public int doSave(OrderBean order) {
        String sql = "INSERT INTO Ordine (Utente, Totale) VALUES (?,?)";
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, order.getUser());
            ps.setDouble(2, order.getTotal());

            if (ps.executeUpdate() != 1) {
                throw new RuntimeException("INSERT error.");
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    order.setId(id);
                    return id;
                }
            }
            return -1;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ArrayList<OrderBean> doRetrieveById(int id) {
        String sql = SELECT + COLUMNS + " FROM Ordine WHERE Utente=?";
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            ArrayList<OrderBean> orders = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapRow(rs));
                }
            }
            return orders;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public OrderBean doRetrieveByIdOrder(int id) {
        String sql = SELECT + COLUMNS + " FROM Ordine WHERE ID_Ordine=?";
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
            return null;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ArrayList<OrderBean> doRetrieveAll() {
        String sql = SELECT + COLUMNS + " FROM Ordine";
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ArrayList<OrderBean> orders = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapRow(rs));
                }
            }
            return orders;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private OrderBean mapRow(ResultSet rs) throws SQLException {
        OrderBean orderBean = new OrderBean();
        orderBean.setId(rs.getInt("ID_Ordine"));
        orderBean.setUser(rs.getInt("Utente"));
        orderBean.setTotal(rs.getDouble("Totale"));
        return orderBean;
    }
}