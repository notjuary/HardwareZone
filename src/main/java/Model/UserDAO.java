package Model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public void doSave(UserBean utenteBean) {

        try (Connection con = ConPool.getConnection()) {

            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO Utente (Nome, Cognome, Data_Nascita, Email, Accesso, Telefono, Citta, Provincia, Codice_Postale, Indirizzo, Data_Registrazione, Stato, Amministratore) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, utenteBean.getName());
            ps.setString(2, utenteBean.getSurname());
            ps.setString(3, utenteBean.getBirthday());
            ps.setString(4, utenteBean.getEmail());
            ps.setString(5, utenteBean.getPassword());
            ps.setString(6, utenteBean.getPhone());
            ps.setString(7, utenteBean.getCity());
            ps.setString(8, utenteBean.getProvince());
            ps.setString(9, utenteBean.getPostalCode());
            ps.setString(10, utenteBean.getAddress());
            ps.setString(11, utenteBean.getRegister());
            ps.setString(12, utenteBean.isActive());
            ps.setString(13, utenteBean.isAdmin());


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

    public UserBean doRetrieveById(int id) {

        try (Connection con = ConPool.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT * " +
                            "FROM Utente " +
                            "WHERE ID_Utente=?");

            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                UserBean utente = new UserBean();
                utente.setId(rs.getInt(1));
                utente.setName(rs.getString(2));
                utente.setSurname(rs.getString(3));
                utente.setBirthday(rs.getString(4));
                utente.setEmail(rs.getString(5));
                utente.setPassword(rs.getString(6));
                utente.setPhone(rs.getString(7));
                utente.setCity(rs.getString(8));
                utente.setProvince(rs.getString(9));
                utente.setPostalCode(rs.getString(10));
                utente.setAddress(rs.getString(11));
                utente.setRegister(rs.getString(12));
                utente.setState(rs.getString(13));
                utente.setAdmin(rs.getString(14));

                return utente;
            }

            return null;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int doRetrieveByEmailAndPassword(String email, String password) {

        try (Connection con = ConPool.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT ID_Utente " +
                            "FROM Utente " +
                            "WHERE Email=? AND Accesso=SHA1(?)");

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

    public  void doUpdate(UserBean utente)
    {
        try (Connection con = ConPool.getConnection()) {

            Statement st = con.createStatement();
            String query = "UPDATE Utente SET Nome = '" + utente.getName() + "', Cognome = '" + utente.getSurname() + "', Data_Nascita = '" + utente.getBirthday() + "', Email = '" + utente.getEmail() + "', Accesso = '" + utente.getPassword() + "', Telefono = '" + utente.getPhone() +  "', Citta = '" + utente.getCity() + "', Provincia = '" + utente.getProvince() + "', Codice_Postale = '" + utente.getPostalCode() + "', Indirizzo = '" + utente.getAddress() + "', Stato = '" + utente.isActive() + "', Amministratore = '" + utente.isAdmin() + "' WHERE Id_Utente = " + utente.getId();
            st.executeUpdate(query);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isAlreadyRegistered(String email) {

        try (Connection con = ConPool.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT Email FROM Utente WHERE Email=?");

            ps.setString(1, email);

            ResultSet rs = ps.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<UserBean> doRetrieveAll() {

        try (Connection con = ConPool.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT * FROM Utente ORDER BY ID_Utente");

            ArrayList<UserBean> listaUtenti = new ArrayList<UserBean>();
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                UserBean utente = new UserBean();

                utente.setId(rs.getInt(1));
                utente.setName(rs.getString(2));
                utente.setSurname(rs.getString(3));
                utente.setBirthday(rs.getString(4));
                utente.setEmail(rs.getString(5));
                utente.setPassword(rs.getString(6));
                utente.setPhone(rs.getString(7));
                utente.setCity(rs.getString(8));
                utente.setProvince(rs.getString(9));
                utente.setPostalCode(rs.getString(10));
                utente.setAddress(rs.getString(11));
                utente.setRegister(rs.getString(12));
                utente.setState(rs.getString(13));
                utente.setAdmin(rs.getString(14));

                listaUtenti.add(utente);
            }

            return listaUtenti;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}