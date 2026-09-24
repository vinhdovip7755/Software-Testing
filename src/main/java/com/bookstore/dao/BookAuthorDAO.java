package com.bookstore.dao;

import com.bookstore.util.DatabaseConnection;

import java.sql.*;
import java.util.List;

public class BookAuthorDAO {

    public void addAuthorsToBook(int bookId, List<Integer> authorIds) {
        String sql = "INSERT INTO book_author (book_id, author_id) VALUES (?, ?)";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            for (int authorId : authorIds) {
                ps.setInt(1, bookId);
                ps.setInt(2, authorId);
                ps.addBatch();
            }

            ps.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean removeAllAuthorsFromBook(int bookId) {
        String sql = "DELETE FROM book_author WHERE book_id = ?";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, bookId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}