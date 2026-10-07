package Model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public void doSave(UserBean userBean) {
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO Utente (Nome, Cognome, Data_Nascita, Email, Accesso, Telefono, Citta, Provincia, Codice_Postale, Indirizzo, Data_Registrazione, Stato, Amministratore) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)",
                     Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, userBean.getName());
            ps.setString(2, userBean.getSurname());
            ps.setString(3, userBean.getBirthday());
            ps.setString(4, userBean.getEmail());
            ps.setString(5, userBean.getPassword());
            ps.setString(6, userBean.getPhone());
            ps.setString(7, userBean.getCity());
            ps.setString(8, userBean.getProvince());
            ps.setString(9, userBean.getPostalCode());
            ps.setString(10, userBean.getAddress());
            ps.setString(11, userBean.getRegister());
            ps.setString(12, userBean.isActive());
            ps.setString(13, userBean.isAdmin());

            if (ps.executeUpdate() != 1) {
                throw new RuntimeException("INSERT error.");
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    userBean.setId(id);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public UserBean doRetrieveById(int id) {
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM Utente WHERE ID_Utente=?")) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    UserBean user = new UserBean();
                    user.setId(rs.getInt(1));
                    user.setName(rs.getString(2));
                    user.setSurname(rs.getString(3));
                    user.setBirthday(rs.getString(4));
                    user.setEmail(rs.getString(5));
                    user.setPassword(rs.getString(6));
                    user.setPhone(rs.getString(7));
                    user.setCity(rs.getString(8));
                    user.setProvince(rs.getString(9));
                    user.setPostalCode(rs.getString(10));
                    user.setAddress(rs.getString(11));
                    user.setRegister(rs.getString(12));
                    user.setState(rs.getString(13));
                    user.setAdmin(rs.getString(14));
                    return user;
                }
            }
            return null;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public UserBean doRetrieveByEmailAndPassword(String email, String password) {
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM Utente WHERE Email=? AND Accesso=SHA1(?)")) {

            ps.setString(1, email);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    UserBean user = new UserBean();
                    user.setId(rs.getInt(1));
                    user.setName(rs.getString(2));
                    user.setSurname(rs.getString(3));
                    user.setBirthday(rs.getString(4));
                    user.setEmail(rs.getString(5));
                    user.setPassword(rs.getString(6));
                    user.setPhone(rs.getString(7));
                    user.setCity(rs.getString(8));
                    user.setProvince(rs.getString(9));
                    user.setPostalCode(rs.getString(10));
                    user.setAddress(rs.getString(11));
                    user.setRegister(rs.getString(12));
                    user.setState(rs.getString(13));
                    user.setAdmin(rs.getString(14));
                    return user;
                }
            }
            return null;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void doUpdate(UserBean utente) {
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE Utente SET Nome = ?, Cognome = ?, Data_Nascita = ?, " +
                             "Email = ?, Accesso = ?, Telefono = ?, Citta = ?, Provincia = ?, " +
                             "Codice_Postale = ?, Indirizzo = ?, Stato = ?, Amministratore = ? " +
                             "WHERE Id_Utente = ?")) {

            ps.setString(1, utente.getName());
            ps.setString(2, utente.getSurname());
            ps.setString(3, utente.getBirthday());
            ps.setString(4, utente.getEmail());
            ps.setString(5, utente.getPassword());
            ps.setString(6, utente.getPhone());
            ps.setString(7, utente.getCity());
            ps.setString(8, utente.getProvince());
            ps.setString(9, utente.getPostalCode());
            ps.setString(10, utente.getAddress());
            ps.setString(11, utente.isActive());
            ps.setString(12, utente.isAdmin());
            ps.setInt(13, utente.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void doUpdateState(UserBean utente) {
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE Utente SET Stato = ? WHERE Id_Utente = ?")) {

            ps.setString(1, utente.isActive());
            ps.setInt(2, utente.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void doUpdateAdmin(UserBean utente) {
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE Utente SET Amministratore = 'true' WHERE Id_Utente = ?")) {

            ps.setInt(1, utente.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isAlreadyRegistered(String email) {
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT Email FROM Utente WHERE Email=?")) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<UserBean> doRetrieveAll() {
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM Utente ORDER BY ID_Utente")) {

            ArrayList<UserBean> usersList = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UserBean user = new UserBean();
                    user.setId(rs.getInt(1));
                    user.setName(rs.getString(2));
                    user.setSurname(rs.getString(3));
                    user.setBirthday(rs.getString(4));
                    user.setEmail(rs.getString(5));
                    user.setPassword(rs.getString(6));
                    user.setPhone(rs.getString(7));
                    user.setCity(rs.getString(8));
                    user.setProvince(rs.getString(9));
                    user.setPostalCode(rs.getString(10));
                    user.setAddress(rs.getString(11));
                    user.setRegister(rs.getString(12));
                    user.setState(rs.getString(13));
                    user.setAdmin(rs.getString(14));
                    usersList.add(user);
                }
            }
            return usersList;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}