package Model;

import java.sql.*;
import java.util.ArrayList;

public class ProductDAO {

    // Costante per evitare duplicazione della stringa "SELECT "
    private static final String SELECT = "SELECT ";

    // Colonne esplicite per evitare SELECT *
    private static final String COLUMNS =
            "ID_Prodotto, Nome, Descrizione, Prezzo, Quantita_Disponibile, Sconto, Immagine, Categoria";

    public void doSave(ProductBean productBean) {
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO Prodotto (Nome, Descrizione, Prezzo, Quantita_Disponibile, Sconto, Immagine, Categoria) VALUES(?,?,?,?,?,?,?)",
                     Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, productBean.getName());
            ps.setString(2, productBean.getDescription());
            ps.setDouble(3, productBean.getPrice());
            ps.setInt(4, productBean.getQuantity());
            ps.setInt(5, productBean.getSales());
            ps.setString(6, productBean.getImage());
            ps.setString(7, productBean.getCategory());

            if (ps.executeUpdate() != 1) {
                throw new RuntimeException("INSERT error.");
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    productBean.setId(id);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void doUpdate(ProductBean productBean) {
        String sql = "UPDATE Prodotto SET Nome = ?, Descrizione = ?, Prezzo = ?, " +
                "Quantita_Disponibile = ?, Sconto = ?, Immagine = ?, Categoria = ? " +
                "WHERE ID_Prodotto = ?";

        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, productBean.getName());
            ps.setString(2, productBean.getDescription());
            ps.setDouble(3, productBean.getPrice());
            ps.setInt(4, productBean.getQuantity());
            ps.setInt(5, productBean.getSales());
            ps.setString(6, productBean.getImage());
            ps.setString(7, productBean.getCategory());
            ps.setInt(8, productBean.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isAlreadyRegistered(String name, String description) {
        // Solo 1 colonna è sufficiente per verificare l'esistenza
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT ID_Prodotto FROM Prodotto WHERE Nome=? AND Descrizione=?")) {

            ps.setString(1, name);
            ps.setString(2, description);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ProductBean doRetrieveById(int id) {
        String sql = SELECT + COLUMNS + " FROM Prodotto WHERE ID_Prodotto=?";
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
            return null;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ArrayList<ProductBean> doRetrieveAll() {
        String sql = SELECT + COLUMNS + " FROM Prodotto ORDER BY ID_Prodotto";
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ArrayList<ProductBean> productsList = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    productsList.add(mapRow(rs));
                }
            }
            return productsList;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ArrayList<ProductBean> doRetrieveSales() {
        String sql = SELECT + COLUMNS + " FROM Prodotto WHERE Sconto > 0";
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ArrayList<ProductBean> productsList = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    productsList.add(mapRow(rs));
                }
            }
            return productsList;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ArrayList<ProductBean> doRetrieveByFilter(int minPrice, int maxPrice, String category) {
        String sql;
        if (category.equalsIgnoreCase("all")) {
            sql = SELECT + COLUMNS + " FROM Prodotto WHERE Prezzo >= ? AND Prezzo <= ?";
        } else {
            sql = SELECT + COLUMNS + " FROM Prodotto WHERE Prezzo >= ? AND Prezzo <= ? AND Categoria = ?";
        }

        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, minPrice);
            ps.setInt(2, maxPrice);
            if (!category.equalsIgnoreCase("all")) {
                ps.setString(3, category);
            }

            ArrayList<ProductBean> productsList = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    productsList.add(mapRow(rs));
                }
            }
            return productsList;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Helper per evitare duplicazione del mapping
    private ProductBean mapRow(ResultSet rs) throws SQLException {
        ProductBean product = new ProductBean();
        product.setId(rs.getInt("ID_Prodotto"));
        product.setName(rs.getString("Nome"));
        product.setDescription(rs.getString("Descrizione"));
        product.setPrice(rs.getDouble("Prezzo"));
        product.setQuantity(rs.getInt("Quantita_Disponibile"));
        product.setSales(rs.getInt("Sconto"));
        product.setImage(rs.getString("Immagine"));
        product.setCategory(rs.getString("Categoria"));
        return product;
    }
}