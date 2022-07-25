package Model;

import java.sql.*;
import java.util.ArrayList;

public class ProductDAO {

    public void doSave(ProductBean productBean) {

        try (Connection con = ConPool.getConnection()) {

            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO Prodotto (Nome, Descrizione, Prezzo, Quantità_Disponibile, Sconto, Immagine, Categoria) VALUES(?,?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);

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

            ResultSet rs = ps.getGeneratedKeys();
            rs.next();
            int id = rs.getInt(1);
            productBean.setId(id);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void doUpdate(ProductBean productBean)
    {
        try (Connection con = ConPool.getConnection()) {

            Statement st = con.createStatement();
            String query = "UPDATE Prodotto SET Nome = '" + productBean.getName() + "', Descrizione = '" + productBean.getDescription() + "', Prezzo = '" + productBean.getPrice() + "', Quantità_Disponibile = '" + productBean.getQuantity() + "', Sconto = '" + productBean.getSales() + "', Immagine = '" + productBean.getImage() +  "', Categoria = '" + productBean.getCategory() +
                    "'WHERE ID_Prodotto = " + productBean.getId();
            st.executeUpdate(query);

            st.executeUpdate(query);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isAlreadyRegistered(String name, String description) {
        try (Connection con = ConPool.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT * " +
                            "FROM Prodotto " +
                            "WHERE Nome=? AND Descrizione=?");

            ps.setString(1, name);
            ps.setString(2, description);

            ResultSet rs = ps.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ProductBean doRetrieveById(int id) {

        try (Connection con = ConPool.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT * " +
                            "FROM Prodotto " +
                            "WHERE ID_Prodotto=?");

            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                ProductBean product = new ProductBean();
                product.setId(rs.getInt(1));
                product.setName(rs.getString(2));
                product.setDescription(rs.getString(3));
                product.setPrice(rs.getDouble(4));
                product.setQuantity(rs.getInt(5));
                product.setSales(rs.getInt(6));
                product.setImage(rs.getString(7));
                product.setCategory(rs.getString(8));

                return product;
            }

            return null;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ArrayList<ProductBean> doRetrieveAll() {

        try (Connection con = ConPool.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT * FROM Prodotto ORDER BY ID_Prodotto");

            ArrayList<ProductBean> productsList = new ArrayList<ProductBean>();
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                ProductBean product = new ProductBean();

                product.setId(rs.getInt(1));
                product.setName(rs.getString(2));
                product.setDescription(rs.getString(3));
                product.setPrice(rs.getDouble(4));
                product.setQuantity(rs.getInt(5));
                product.setSales(rs.getInt(6));
                product.setImage(rs.getString(7));
                product.setCategory(rs.getString(8));

                productsList.add(product);
            }

            return productsList;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ArrayList<ProductBean> doRetrieveSales() {

        try (Connection con = ConPool.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT * " +
                            "FROM Prodotto " +
                            "WHERE Sconto > 0");

            ArrayList<ProductBean> productsList = new ArrayList<ProductBean>();
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                ProductBean product = new ProductBean();

                product.setId(rs.getInt(1));
                product.setName(rs.getString(2));
                product.setDescription(rs.getString(3));
                product.setPrice(rs.getDouble(4));
                product.setQuantity(rs.getInt(5));
                product.setSales(rs.getInt(6));
                product.setImage(rs.getString(7));
                product.setCategory(rs.getString(8));

                productsList.add(product);
            }

            return productsList;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ArrayList<ProductBean> doRetrieveByFilter(int minPrice, int maxPrice, String category) {

        try (Connection con = ConPool.getConnection()) {

            PreparedStatement ps;

            if (category.equalsIgnoreCase("all")) {
                ps = con.prepareStatement(
                        "SELECT * " +
                                "FROM Prodotto " +
                                "WHERE Prezzo > ? AND Prezzo < ?");

                ps.setInt(1, minPrice);
                ps.setInt(2, maxPrice);
            }

            else {
                ps = con.prepareStatement(
                        "SELECT * " +
                                "FROM Prodotto " +
                                "WHERE Prezzo > ? AND Prezzo < ? AND Categoria = ?");

                ps.setInt(1, minPrice);
                ps.setInt(2, maxPrice);
                ps.setString(3, category);
            }

            ArrayList<ProductBean> productsList = new ArrayList<ProductBean>();
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                ProductBean product = new ProductBean();

                product.setId(rs.getInt(1));
                product.setName(rs.getString(2));
                product.setDescription(rs.getString(3));
                product.setPrice(rs.getDouble(4));
                product.setQuantity(rs.getInt(5));
                product.setSales(rs.getInt(6));
                product.setImage(rs.getString(7));
                product.setCategory(rs.getString(8));

                productsList.add(product);
            }

            return productsList;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}