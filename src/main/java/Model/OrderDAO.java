package Model;

import java.sql.*;
import java.util.ArrayList;

public class OrderDAO {

    public int doSave(OrderBean order)
    {
        try (Connection con = ConPool.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO Ordine (Utente, Totale) VALUES (?,?)",
            Statement.RETURN_GENERATED_KEYS);

            ps.setInt(1, order.getUser());
            ps.setDouble(2, order.getTotal());

            if (ps.executeUpdate() != 1) {
                throw new RuntimeException("INSERT error.");
            }

            ResultSet rs = ps.getGeneratedKeys();
            rs.next();
            int id = rs.getInt(1);
            order.setId(id);
            return id;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ArrayList<OrderBean> doRetrieveById(int id)
    {
        try (Connection con = ConPool.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT * " +
                            "FROM Ordine " +
                            "WHERE Utente=?");

            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();
            ArrayList<OrderBean> orders = new ArrayList<>();

            while (rs.next()) {
                OrderBean orderBean = new OrderBean();
                orderBean.setId(rs.getInt(1));
                orderBean.setUser(rs.getInt(2));
                orderBean.setTotal(rs.getDouble(3));
                orders.add(orderBean);
            }

            return orders;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public OrderBean doRetrieveByIdOrder(int id)
    {
        try (Connection con = ConPool.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT * " +
                            "FROM Ordine " +
                            "WHERE ID_Ordine=?");

            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            rs.next();

            OrderBean orderBean = new OrderBean();
            orderBean.setId(rs.getInt(1));
            orderBean.setUser(rs.getInt(2));
            orderBean.setTotal(rs.getDouble(3));

            return orderBean;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ArrayList<OrderBean> doRetrieveAll()
    {
        try (Connection con = ConPool.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT * " +
                            "FROM Ordine ");

            ResultSet rs = ps.executeQuery();
            ArrayList<OrderBean> orders = new ArrayList<>();

            while (rs.next()) {
                OrderBean orderBean = new OrderBean();
                orderBean.setId(rs.getInt(1));
                orderBean.setUser(rs.getInt(2));
                orderBean.setTotal(rs.getDouble(3));
                orders.add(orderBean);
            }

            return orders;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}