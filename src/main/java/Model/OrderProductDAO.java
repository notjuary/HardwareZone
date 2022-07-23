package Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
}
