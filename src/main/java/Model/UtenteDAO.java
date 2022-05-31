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
            ps.setString(9, utenteBean.getCodicePostale());
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

    public UtenteBean doRetrieveById(int id) {

        try (Connection con = ConPool.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT * FROM Utente WHERE ID_Utente=?");

            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                UtenteBean utente = new UtenteBean();
                utente.setId(rs.getInt(1));
                utente.setNome(rs.getString(2));
                utente.setCognome(rs.getString(3));
                utente.setDataNascita(rs.getString(4));
                utente.setEmail(rs.getString(5));
                utente.setPassword(rs.getString(6));
                utente.setTelefono(rs.getString(7));
                utente.setCitta(rs.getString(8));
                utente.setProvincia(rs.getString(9));
                utente.setCodicePostale(rs.getString(10));
                utente.setIndirizzo(rs.getString(11));
                utente.setDataRegistrazione(rs.getString(12));
                utente.setStato(rs.getBoolean(13));

                return utente;
            }

            return null;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int doRetrieveByUsernamePassword(String email, String password) {

        try (Connection con = ConPool.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT ID_Utente FROM Utente WHERE Email=? AND Accesso=SHA1(?)");

            ps.setString(1, email);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next())
                return rs.getInt(1);
            else
                return -1;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
