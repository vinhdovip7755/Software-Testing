package com.bookstore.dao;

import com.bookstore.dto.CategoryDTO;
import com.bookstore.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {
    public List<CategoryDTO> selectAllCategories() {
        List<CategoryDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM category";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new CategoryDTO(
                        rs.getInt("category_id"),
                        rs.getString("category_name")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public int add(CategoryDTO category){
        String sql = "INSERT INTO category(category_name)" + "VALUES (?)";
        try(Connection c = DatabaseConnection.getConnection();
            PreparedStatement ps = c.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)){

            ps.setString(1, category.getCategoryName());

            int rowsAffected = ps.executeUpdate();

            if(rowsAffected >0){
                try(ResultSet generatedKeys = ps.getGeneratedKeys()){
                    if(generatedKeys.next()){
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return -1;
    }

    public boolean update(CategoryDTO category){
        String sql = "UPDATE category SET category_name  = ? " + "WHERE category_id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement  ps = c.prepareStatement(sql)){
            ps.setString(1,category.getCategoryName());
            ps.setInt(2, category.getCategoryId());

            int rowsAffected =  ps.executeUpdate();
            return rowsAffected>0;
        } catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public CategoryDTO getCategoryById(int categoryId){
        String sql = "SELECT * FROM category WHERE category_id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);){
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()){
                if(rs.next()){
                    return new CategoryDTO(
                            rs.getInt("category_id"),
                            rs.getString("category_name")
                    );
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
