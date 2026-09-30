package com.bookstore.dao;

import com.bookstore.dto.AuthorDTO;
import com.bookstore.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class AuthorDAO {
    public List<AuthorDTO> selectAllAuthors() {
        List<AuthorDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM author ORDER BY author_id";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                AuthorDTO author = new AuthorDTO();
                author.setAuthorId(rs.getInt("author_id"));
                author.setAuthorName(rs.getString("author_name"));
                author.setNationality(rs.getString("nationality"));

                list.add(author);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public int add(AuthorDTO author){
        String sql= "INSERT  INTO author (author_name, nationality)" +
                "VALUES (?,?)";
        try(Connection c = DatabaseConnection.getConnection();
            PreparedStatement ps  = c.prepareStatement(sql,PreparedStatement.RETURN_GENERATED_KEYS)){

            ps.setString(1, author.getAuthorName());
            ps.setString(2, author.getNationality());


            int rowsAffected = ps.executeUpdate();


            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }


    public boolean update(AuthorDTO author){
        String sql  = "UPDATE author SET author_name= ?, nationality = ? "  +
                "WHERE author_id = ?";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);){
            ps.setString(1, author.getAuthorName());
            ps.setString(2, author.getNationality());
            ps.setInt(3, author.getAuthorId());

            int rowsAffected = ps.executeUpdate();
            return rowsAffected >0;
        } catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean exists(String authorname){
        String sql ="SELECT * FROM author WHERE author_name = ?";
        try(Connection c  = DatabaseConnection.getConnection();
            PreparedStatement ps = c.prepareStatement(sql);){

            ps.setString(1, authorname);
            try(ResultSet rs = ps.executeQuery();){
                return rs.next();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
}