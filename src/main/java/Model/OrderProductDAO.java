package Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class OrderProductDAO {
    public void doSave(OrderProductBean orderProductBean)
    {
        try (Connection con = ConPool.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO Ordine_Prodotto VALUES (?,?,?,?)");

            ps.setInt(1, orderProductBean.getProduct());
            ps.setInt(2, orderProductBean.getQuantity());
            ps.setDouble(3, orderProductBean.getPrice());
            ps.setInt(4, orderProductBean.getOrder());

            if (ps.executeUpdate() != 1) {
                throw new RuntimeException("INSERT error.");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ArrayList<OrderProductBean> doRetrieveById(int id) {
        try (Connection con = ConPool.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT * " +
                            "FROM Ordine_Prodotto " +
                            "WHERE Ordine=?");

            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();
            ArrayList<OrderProductBean> products = new ArrayList<>();

            while (rs.next()) {
                OrderProductBean product = new OrderProductBean();
                product.setProduct(rs.getInt(1));
                product.setQuantity(rs.getInt(2));
                product.setPrice(rs.getDouble(3));
                product.setOrder(rs.getInt(4));
                products.add(product);
            }

            return products;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
