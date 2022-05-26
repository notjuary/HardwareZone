package Model;

import java.sql.*;

public class UtenteDAO {

    public void doSave(UtenteBean utenteBean) {

        try (Connection con = ConPool.getConnection()) {

            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO Utente (Nome, Cognome, Data_Nascita, Email, Accesso, Telefono, Citta, Provincia, Codice_Postale, Indirizzo, Data_Registrazione, Stato) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, utenteBean.getNome());
            ps.setString(2, utenteBean.getCognome());
            ps.setString(3, utenteBean.getDataNascita());
            ps.setString(4, utenteBean.getEmail());
            ps.setString(5, utenteBean.getPassword());
            ps.setString(6, utenteBean.getTelefono());
            ps.setString(7, utenteBean.getCitta());
            ps.setString(8, utenteBean.getProvincia());
            ps.setString(9, utenteBean.getCodice_postale());
            ps.setString(10, utenteBean.getIndirizzo());
            ps.setString(11, utenteBean.getDataRegistrazione());
            ps.setBoolean(12, utenteBean.isActive());


            if (ps.executeUpdate() != 1) {
                throw new RuntimeException("INSERT error.");
            }

            ResultSet rs = ps.getGeneratedKeys();
            rs.next();
            int id = rs.getInt(1);
            utenteBean.setId(id);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
