package Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PaymentDAO {

    public void doSave(PaymentBean payment)
    {
        try (Connection con = ConPool.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO Pagamento (Ordine, Data_Pagamento, Numero_Carta, CVV, Scadenza, Titolare_Carta) VALUES (?,?,?,?,?,?)");

            ps.setInt(1, payment.getOrder());
            ps.setString(2, payment.getDatePayment());
            ps.setString(3, payment.getCardNumber());
            ps.setString(4, payment.getCVV());
            ps.setString(5, payment.getDeadline());
            ps.setString(6, payment.getHolder());

            if (ps.executeUpdate() != 1) {
                throw new RuntimeException("INSERT error.");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}