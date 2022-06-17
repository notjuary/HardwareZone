package Model;

import java.sql.*;
import java.util.ArrayList;

public class CategoryDAO {

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
