package Model;

import java.sql.*;
import java.util.ArrayList;

public class OrderProductDAO {

    private static final String SELECT = "SELECT ";
    private static final String COLUMNS =
            "Prodotto, Quantità, Prezzo, Ordine";

    public void doSave(OrderProductBean orderProductBean) {
        String sql = "INSERT INTO Ordine_Prodotto (Prodotto, Quantità, Prezzo, Ordine) VALUES (?,?,?,?)";
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

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
        String sql = SELECT + COLUMNS + " FROM Ordine_Prodotto WHERE Ordine=?";
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            ArrayList<OrderProductBean> products = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderProductBean product = new OrderProductBean();
                    product.setProduct(rs.getInt("Prodotto"));
                    product.setQuantity(rs.getInt("Quantità"));
                    product.setPrice(rs.getDouble("Prezzo"));
                    product.setOrder(rs.getInt("Ordine"));
                    products.add(product);
                }
            }

            return products;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}