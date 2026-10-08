package Model;

import java.sql.*;
import java.util.ArrayList;

public class CategoryDAO {

    private static final String SELECT = "SELECT ";

    public void doSave(String category) {
        String sql = "INSERT INTO Categoria VALUES (?)";
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, category);

            if (ps.executeUpdate() != 1) {
                throw new RuntimeException("INSERT error.");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ArrayList<CategoryBean> doRetrieveAll() {
        String sql = SELECT + "Nome_Categoria FROM Categoria ORDER BY Nome_Categoria";
        try (Connection con = ConPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ArrayList<CategoryBean> categoryList = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CategoryBean category = new CategoryBean();
                    category.setNome(rs.getString("Nome_Categoria"));
                    categoryList.add(category);
                }
            }
            return categoryList;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}