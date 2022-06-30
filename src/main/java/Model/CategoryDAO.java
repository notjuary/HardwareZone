package Model;

import java.sql.*;
import java.util.ArrayList;

public class CategoryDAO {

    public void doSave(String category)
    {
        try (Connection con = ConPool.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO Categoria VALUES (?)");

            ps.setString(1, category);

            if (ps.executeUpdate() != 1) {
                throw new RuntimeException("INSERT error.");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ArrayList<CategoryBean> doRetrieveAll()
    {
        try (Connection con = ConPool.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT * FROM Categoria ORDER BY Nome_Categoria");

            ArrayList<CategoryBean> categoryList = new ArrayList<CategoryBean>();
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                CategoryBean category = new CategoryBean();
                category.setNome(rs.getString(1));
                categoryList.add(category);
            }

            return categoryList;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
