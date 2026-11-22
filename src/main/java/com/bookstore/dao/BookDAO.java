package com.bookstore.dao;

import com.bookstore.dto.BookDTO;
import com.bookstore.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class BookDAO {
    private static volatile boolean coverPriceColumnEnsured = false;

    private static final String BASE_BOOK_SELECT =
            "SELECT b.*, " +
            "COALESCE(" +
            "  (SELECT p.selling_price FROM price p WHERE p.book_id = b.book_id AND p.is_active = 1 " +
            "   ORDER BY p.effective_date DESC, p.price_id DESC LIMIT 1), " +
            "  b.selling_price" +
            ") AS current_selling_price, " +
            "c.category_name, " +
            "GROUP_CONCAT(DISTINCT a.author_name SEPARATOR ',') AS author_names, " +
            "GROUP_CONCAT(DISTINCT ba.author_id SEPARATOR ',') AS author_ids " +
            "FROM book b " +
            "JOIN category c ON b.category_id = c.category_id " +
            "LEFT JOIN book_author ba ON b.book_id = ba.book_id " +
            "LEFT JOIN author a ON ba.author_id = a.author_id ";

    private static final String BASE_BOOK_SELECT_SPACED_AUTHORS =
            "SELECT b.*, " +
            "COALESCE(" +
            "  (SELECT p.selling_price FROM price p WHERE p.book_id = b.book_id AND p.is_active = 1 " +
            "   ORDER BY p.effective_date DESC, p.price_id DESC LIMIT 1), " +
            "  b.selling_price" +
            ") AS current_selling_price, " +
            "c.category_name, " +
            "GROUP_CONCAT(DISTINCT a.author_name SEPARATOR ', ') AS author_names, " +
            "GROUP_CONCAT(DISTINCT ba.author_id SEPARATOR ',') AS author_ids " +
            "FROM book b " +
            "JOIN category c ON b.category_id = c.category_id " +
            "LEFT JOIN book_author ba ON b.book_id = ba.book_id " +
            "LEFT JOIN author a ON ba.author_id = a.author_id ";

    static void ensureCoverPriceColumn() {
        if (coverPriceColumnEnsured) {
            return;
        }
        synchronized (BookDAO.class) {
            if (coverPriceColumnEnsured) {
                return;
            }
            try (Connection conn = DatabaseConnection.getConnection()) {
                boolean exists = false;
                String checkSql = "SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS " +
                        "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'book' AND COLUMN_NAME = 'cover_price'";
                try (PreparedStatement ps = conn.prepareStatement(checkSql);
                     ResultSet rs = ps.executeQuery()) {
                    exists = rs.next();
                }

                if (!exists) {
                    try (Statement stmt = conn.createStatement()) {
                        try {
                            stmt.executeUpdate("ALTER TABLE book ADD COLUMN cover_price DECIMAL(15,0) NOT NULL DEFAULT 0");
                        } catch (SQLException ex) {
                            if (ex.getErrorCode() != 1060) {
                                throw ex;
                            }
                        }
                        stmt.executeUpdate("UPDATE book SET cover_price = selling_price WHERE cover_price = 0");
                    }
                }
                coverPriceColumnEnsured = true;
            } catch (SQLException e) {
                throw new IllegalStateException("Không thể bảo đảm cột giá bìa cho bảng sách.", e);
            }
        }
    }

    public List<BookDTO> selectAllBooks() {
        ensureCoverPriceColumn();
        List<BookDTO> list = new ArrayList<>();
        String sql = BASE_BOOK_SELECT + "GROUP BY b.book_id";

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

    public BookDTO selectById(int bookId) {
        if (bookId <= 0) {
            return null;
        }
        ensureCoverPriceColumn();
        String sql = BASE_BOOK_SELECT + "WHERE b.book_id = ? GROUP BY b.book_id";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBookDTO(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public int add(BookDTO book) {
        ensureCoverPriceColumn();
        String sql = "INSERT INTO book(book_name, publication_year, cover_price, selling_price, quantity, translator, image, description, status, category_id, tag_detail, supplier_id) " +
                "VALUES (?, ?, ?, ?, 0, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, book.getBookName());
            ps.setInt(2, book.getPublicationYear());
            ps.setDouble(3, book.getCoverPrice());
            ps.setDouble(4, book.getCoverPrice());
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
        ensureCoverPriceColumn();
        Set<Integer> sanitizedAuthors = sanitizeAuthorIds(authorIds);
        if (sanitizedAuthors.isEmpty()) {
            return 0;
        }

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            String sql = "INSERT INTO book(book_name, publication_year, cover_price, selling_price, quantity, translator, image, description, status, category_id, tag_detail, supplier_id) " +
                    "VALUES (?, ?, ?, ?, 0, ?, ?, ?, ?, ?, ?, ?)";
            int generatedId = 0;
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, book.getBookName());
                ps.setInt(2, book.getPublicationYear());
                ps.setDouble(3, book.getCoverPrice());
                ps.setDouble(4, book.getCoverPrice());
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

            String sqlAuthor = "INSERT IGNORE INTO book_author (book_id, author_id) VALUES (?, ?)";
            try (PreparedStatement psAuthor = conn.prepareStatement(sqlAuthor)) {
                for (Integer aId : sanitizedAuthors) {
                    psAuthor.setInt(1, generatedId);
                    psAuthor.setInt(2, aId);
                    psAuthor.addBatch();
                }
                psAuthor.executeBatch();
            }

            conn.commit();
            return generatedId;
        } catch (SQLException e) {
            e.printStackTrace();
            rollbackQuietly(conn);
            return 0;
        } finally {
            closeTransactionalConnection(conn);
        }
    }

    public boolean updateBookWithAuthorsTransaction(BookDTO book, List<Integer> authorIds) {
        ensureCoverPriceColumn();
        Set<Integer> sanitizedAuthors = sanitizeAuthorIds(authorIds);
        if (sanitizedAuthors.isEmpty()) {
            return false;
        }

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            String sql = "UPDATE book SET book_name = ?, publication_year = ?, cover_price = ?, translator = ?, image = ?, description = ?, status = ?, category_id = ?, tag_detail = ?, supplier_id = ? " +
                    "WHERE book_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, book.getBookName());
                ps.setInt(2, book.getPublicationYear());
                ps.setDouble(3, book.getCoverPrice());
                ps.setString(4, book.getTranslator());
                ps.setString(5, book.getImage());
                ps.setString(6, book.getDescription());
                ps.setInt(7, book.getStatus());
                ps.setInt(8, book.getCategoryId());
                ps.setString(9, book.getTagDetail());
                ps.setInt(10, book.getSupplierId());
                ps.setInt(11, book.getBookId());

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

            String sqlAuthor = "INSERT IGNORE INTO book_author (book_id, author_id) VALUES (?, ?)";
            try (PreparedStatement psAuthor = conn.prepareStatement(sqlAuthor)) {
                for (Integer aId : sanitizedAuthors) {
                    psAuthor.setInt(1, book.getBookId());
                    psAuthor.setInt(2, aId);
                    psAuthor.addBatch();
                }
                psAuthor.executeBatch();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            rollbackQuietly(conn);
            return false;
        } finally {
            closeTransactionalConnection(conn);
        }
    }

    public boolean update(BookDTO book) {
        ensureCoverPriceColumn();
        String sql = "UPDATE book SET book_name = ?, publication_year = ?, cover_price = ?, translator = ?, image = ?, description = ?, status = ?, category_id = ?, tag_detail = ?, supplier_id = ? " +
                "WHERE book_id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, book.getBookName());
            ps.setInt(2, book.getPublicationYear());
            ps.setDouble(3, book.getCoverPrice());
            ps.setString(4, book.getTranslator());
            ps.setString(5, book.getImage());
            ps.setString(6, book.getDescription());
            ps.setInt(7, book.getStatus());
            ps.setInt(8, book.getCategoryId());
            ps.setString(9, book.getTagDetail());
            ps.setInt(10, book.getSupplierId());
            ps.setInt(11, book.getBookId());

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
        String sql = "UPDATE book SET quantity = ? WHERE book_id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, newQuantity);
            ps.setInt(2, bookId);
            ps.executeUpdate();
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
        book.setCoverPrice(rs.getDouble("cover_price"));
        book.setAuthorIdsFromString(rs.getString("author_ids"));
        book.setAuthorsName(rs.getString("author_names"));
        return book;
    }

    public List<BookDTO> getByCategoryId(int categoryId) {
        ensureCoverPriceColumn();
        List<BookDTO> list = new ArrayList<>();
        String sql = BASE_BOOK_SELECT_SPACED_AUTHORS + "WHERE b.status = 1 AND b.category_id = ? GROUP BY b.book_id";

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
        ensureCoverPriceColumn();
        List<BookDTO> list = new ArrayList<>();
        String sql = BASE_BOOK_SELECT_SPACED_AUTHORS + "WHERE b.status = 1 AND c.category_name = ? GROUP BY b.book_id";

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
        ensureCoverPriceColumn();
        List<BookDTO> list = new ArrayList<>();
        String sql = BASE_BOOK_SELECT_SPACED_AUTHORS + "WHERE b.status = 1 AND b.book_name LIKE ? GROUP BY b.book_id";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, "%" + (bookname == null ? "" : bookname) + "%");
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

    private Set<Integer> sanitizeAuthorIds(List<Integer> authorIds) {
        Set<Integer> sanitized = new LinkedHashSet<>();
        if (authorIds != null) {
            for (Integer authorId : authorIds) {
                if (authorId != null && authorId > 0) {
                    sanitized.add(authorId);
                }
            }
        }
        return sanitized;
    }

    private void rollbackQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void closeTransactionalConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ignored) {
            } finally {
                DatabaseConnection.closeConnection(conn);
            }
        }
    }
}
