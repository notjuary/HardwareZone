package Model;

import java.sql.*;

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
}