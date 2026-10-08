package Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CartDAO {

    public void doSave(int user, int product, int quantity) {
        String sql = "INSERT INTO Carrello VALUES (?,?,?)";
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, product);
            ps.setInt(2, quantity);
            ps.setInt(3, user);

            if (ps.executeUpdate() != 1) {
                throw new RuntimeException("INSERT error.");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void doDelete(int user) {
        String sql = "DELETE FROM Carrello WHERE Utente=?";
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, user);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // FIX: ritorna List invece di ArrayList
    public List<ProductCartBean> getCart(int user) {
        String sql = "SELECT Prodotto, Quantità FROM Carrello WHERE Utente=?";
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, user);

            try (ResultSet rs = ps.executeQuery()) {
                List<ProductCartBean> products = new ArrayList<>();
                while (rs.next()) {
                    ProductCartBean product = new ProductCartBean();
                    product.setId(rs.getInt("Prodotto"));
                    product.setQuantity(rs.getInt("Quantità"));
                    products.add(product);
                }
                return products;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}