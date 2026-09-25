package com.bookstore.dao;

import com.bookstore.dto.BookDTO;
import com.bookstore.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {
    public List<BookDTO> selectAllBooks() {
        List<BookDTO> list = new ArrayList<>();
        String sql = "SELECT b.*, IFNULL(bp.selling_price, b.selling_price) AS current_selling_price, c.category_name, " +
                "GROUP_CONCAT(DISTINCT a.author_name SEPARATOR ',') as author_names, " +
                "GROUP_CONCAT(DISTINCT ba.author_id SEPARATOR ',') as author_ids " +
                "FROM book b " +
                "LEFT JOIN price bp ON b.book_id = bp.book_id AND bp.is_active = 1 " +
                "JOIN category c ON b.category_id = c.category_id " +
                "LEFT JOIN book_author ba ON b.book_id = ba.book_id " +
                "LEFT JOIN author a ON ba.author_id = a.author_id " +
                "GROUP BY b.book_id, bp.selling_price";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToBookDTO(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public int add(BookDTO book) {
        String sql = "INSERT INTO book(book_name, publication_year, selling_price, quantity, translator, image, description, status, category_id, tag_detail, supplier_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, book.getBookName());
            ps.setInt(2, book.getPublicationYear());
            ps.setDouble(3, book.getCoverPrice());
            ps.setInt(4, book.getQuantity());
            ps.setString(5, book.getTranslator());
            ps.setString(6, book.getImage());
            ps.setString(7, book.getDescription());
            ps.setInt(8, book.getStatus());
            ps.setInt(9, book.getCategoryId());
            ps.setString(10, book.getTagDetail());
            ps.setInt(11, book.getSupplierId());

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
        return 0;
    }

    public int addBookWithAuthorsTransaction(BookDTO book, List<Integer> authorIds) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            String sql = "INSERT INTO book(book_name, publication_year, selling_price, quantity, translator, image, description, status, category_id, tag_detail, supplier_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            int generatedId = 0;
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, book.getBookName());
                ps.setInt(2, book.getPublicationYear());
                ps.setDouble(3, book.getCoverPrice());
                ps.setInt(4, book.getQuantity());
                ps.setString(5, book.getTranslator());
                ps.setString(6, book.getImage());
                ps.setString(7, book.getDescription());
                ps.setInt(8, book.getStatus());
                ps.setInt(9, book.getCategoryId());
                ps.setString(10, book.getTagDetail());
                ps.setInt(11, book.getSupplierId());

                if (ps.executeUpdate() > 0) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            generatedId = rs.getInt(1);
                        }
                    }
                }
            }

            if (generatedId <= 0) {
                conn.rollback();
                return 0;
            }

            if (authorIds != null && !authorIds.isEmpty()) {
                String sqlAuthor = "INSERT INTO book_author (book_id, author_id) VALUES (?, ?)";
                try (PreparedStatement psAuthor = conn.prepareStatement(sqlAuthor)) {
                    for (Integer aId : authorIds) {
                        psAuthor.setInt(1, generatedId);
                        psAuthor.setInt(2, aId);
                        psAuthor.addBatch();
                    }
                    psAuthor.executeBatch();
                }
            }

            conn.commit();
            return generatedId;
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return 0;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) {}
            }
        }
    }

    public boolean updateBookWithAuthorsTransaction(BookDTO book, List<Integer> authorIds) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            String sql = "UPDATE book SET book_name = ?, publication_year = ?, selling_price = ?, quantity = ?, translator = ?, image = ?, description = ?, status = ?, category_id = ?, tag_detail = ?, supplier_id = ? " +
                    "WHERE book_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, book.getBookName());
                ps.setInt(2, book.getPublicationYear());
                ps.setDouble(3, book.getCoverPrice());
                ps.setInt(4, book.getQuantity());
                ps.setString(5, book.getTranslator());
                ps.setString(6, book.getImage());
                ps.setString(7, book.getDescription());
                ps.setInt(8, book.getStatus());
                ps.setInt(9, book.getCategoryId());
                ps.setString(10, book.getTagDetail());
                ps.setInt(11, book.getSupplierId());
                ps.setInt(12, book.getBookId());

                if (ps.executeUpdate() <= 0) {
                    conn.rollback();
                    return false;
                }
            }

            String deleteAuthorsSql = "DELETE FROM book_author WHERE book_id = ?";
            try (PreparedStatement psDel = conn.prepareStatement(deleteAuthorsSql)) {
                psDel.setInt(1, book.getBookId());
                psDel.executeUpdate();
            }

            if (authorIds != null && !authorIds.isEmpty()) {
                String sqlAuthor = "INSERT INTO book_author (book_id, author_id) VALUES (?, ?)";
                try (PreparedStatement psAuthor = conn.prepareStatement(sqlAuthor)) {
                    for (Integer aId : authorIds) {
                        psAuthor.setInt(1, book.getBookId());
                        psAuthor.setInt(2, aId);
                        psAuthor.addBatch();
                    }
                    psAuthor.executeBatch();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) {}
            }
        }
    }

    public boolean update(BookDTO book) {
        String sql = "UPDATE book SET book_name = ?, publication_year = ?, selling_price = ?, quantity = ?, translator = ?, image = ?, description = ?, status = ?, category_id = ?, tag_detail = ?, supplier_id = ? " +
                "WHERE book_id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, book.getBookName());
            ps.setInt(2, book.getPublicationYear());
            ps.setDouble(3, book.getCoverPrice());
            ps.setInt(4, book.getQuantity());
            ps.setString(5, book.getTranslator());
            ps.setString(6, book.getImage());
            ps.setString(7, book.getDescription());
            ps.setInt(8, book.getStatus());
            ps.setInt(9, book.getCategoryId());
            ps.setString(10, book.getTagDetail());
            ps.setInt(11, book.getSupplierId());
            ps.setInt(12, book.getBookId());

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public int getQuantityByID(int bookId) {
        int quantity = 0;
        String sql = "SELECT quantity FROM book WHERE book_id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    quantity = rs.getInt("quantity");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return quantity;
    }

    public void updateQuantity(int bookId, int newQuantity) {
        try {
            Connection c = DatabaseConnection.getConnection();
            String sql = "UPDATE book SET quantity = ? WHERE book_id = ?";
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setInt(1, newQuantity);
            ps.setInt(2, bookId);
            ps.executeUpdate();
            DatabaseConnection.closeConnection(c);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private BookDTO mapResultSetToBookDTO(ResultSet rs) throws Exception {
        BookDTO book = new BookDTO(
                rs.getInt("book_id"),
                rs.getString("book_name"),
                rs.getDouble("current_selling_price"),
                rs.getInt("quantity"),
                rs.getString("translator"),
                rs.getString("image"),
                rs.getString("description"),
                rs.getInt("status"),
                rs.getInt("category_id"),
                rs.getString("category_name"),
                rs.getString("tag_detail"),
                rs.getInt("supplier_id")
        );
        book.setPublicationYear(rs.getInt("publication_year"));
        book.setCoverPrice(rs.getDouble("selling_price"));
        book.setAuthorIdsFromString(rs.getString("author_ids"));
        book.setAuthorsName(rs.getString("author_names"));
        return book;
    }

    public List<BookDTO> getByCategoryId(int categoryId) {
        List<BookDTO> list = new ArrayList<>();
        String sql = "SELECT b.*, IFNULL(bp.selling_price, b.selling_price) AS current_selling_price, c.category_name, " +
                "GROUP_CONCAT(DISTINCT a.author_name SEPARATOR ', ') as author_names, " +
                "GROUP_CONCAT(DISTINCT ba.author_id SEPARATOR ',') as author_ids " +
                "FROM book b " +
                "LEFT JOIN price bp ON b.book_id = bp.book_id AND bp.is_active = 1 " +
                "JOIN category c ON b.category_id = c.category_id " +
                "LEFT JOIN book_author ba ON b.book_id = ba.book_id " +
                "LEFT JOIN author a ON ba.author_id = a.author_id " +
                "WHERE b.status = 1 AND b.category_id = ? " +
                "GROUP BY b.book_id, bp.selling_price";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToBookDTO(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<BookDTO> getByCategoryName(String categoryName) {
        List<BookDTO> list = new ArrayList<>();

        String sql = "SELECT b.*, IFNULL(bp.selling_price, b.selling_price) AS current_selling_price, c.category_name, " +
                "GROUP_CONCAT(DISTINCT a.author_name SEPARATOR ', ') as author_names, " +
                "GROUP_CONCAT(DISTINCT ba.author_id SEPARATOR ',') as author_ids " +
                "FROM book b " +
                "LEFT JOIN price bp ON b.book_id = bp.book_id AND bp.is_active = 1 " +
                "JOIN category c ON b.category_id = c.category_id " +
                "LEFT JOIN book_author ba ON b.book_id = ba.book_id " +
                "LEFT JOIN author a ON ba.author_id = a.author_id " +
                "WHERE b.status = 1 AND c.category_name = ? " +
                "GROUP BY b.book_id, bp.selling_price";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, categoryName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToBookDTO(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<BookDTO> searchByName(String bookname) {
        List<BookDTO> list = new ArrayList<>();
        String sql = "SELECT b.*, IFNULL(bp.selling_price, b.selling_price) AS current_selling_price, c.category_name, " +
                "GROUP_CONCAT(DISTINCT a.author_name SEPARATOR ', ') as author_names, " +
                "GROUP_CONCAT(DISTINCT ba.author_id SEPARATOR ',') as author_ids " +
                "FROM book b " +
                "LEFT JOIN price bp ON b.book_id = bp.book_id AND bp.is_active = 1 " +
                "JOIN category c ON b.category_id = c.category_id " +
                "LEFT JOIN book_author ba ON b.book_id = ba.book_id " +
                "LEFT JOIN author a ON ba.author_id = a.author_id " +
                "WHERE b.status = 1 AND b.book_name LIKE ? " +
                "GROUP BY b.book_id, bp.selling_price";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, "%" + bookname + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToBookDTO(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}