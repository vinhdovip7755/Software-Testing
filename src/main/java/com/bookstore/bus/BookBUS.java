package com.bookstore.bus;

import com.bookstore.dao.BookAuthorDAO;
import com.bookstore.dao.BookDAO;
import com.bookstore.dto.BookDTO;

import java.util.List;

public class BookBUS {
    private final BookDAO bookDAO = new BookDAO();
    private final BookAuthorDAO bookAuthorDAO = new BookAuthorDAO();

            public BookDTO selectById(int bookId) {
        for (BookDTO b : bookDAO.selectAllBooks()) {
            if (b.getBookId() == bookId) return b;
        }
        return null;
    }
    public List<BookDTO> selectAllBooks() {
        return bookDAO.selectAllBooks();
    }

    public String addBook(BookDTO book) {
        if (book.getBookName() == null || book.getBookName().trim().isEmpty())
            return "Tên sách không được để trống!";
        if (book.getAuthorIdsList() == null || book.getAuthorIdsList().isEmpty())
            return "Vui lòng chọn ít nhất một tác giả cho sách!";
        if (book.getCategoryId() <= 0)
            return "Vui lòng chọn thể loại cho sách!";
        if (book.getSupplierId() <= 0)
            return "Vui lòng chọn nhà cung cấp cho sách!";

        String duplicateCheck = validateDuplicate(book, false);
        if (duplicateCheck != null) {
            return duplicateCheck;
        }

        int generatedId = bookDAO.add(book);
        if (generatedId > 0) {
            book.setBookId(generatedId);
            return "Thêm sách thành công!";
        }
        return "Thêm sách thất bại!";
    }

    public String updateBook(BookDTO book) {
        if (book.getBookName() == null || book.getBookName().trim().isEmpty())
            return "Tên sách không được để trống!";

        String duplicateCheck = validateDuplicate(book, true);
        if (duplicateCheck != null) {
            return duplicateCheck;
        }

        if (bookDAO.update(book)) {
            return "Cập nhật thành công!";
        }
        return "Cập nhật thất bại!";
    }

    private String validateDuplicate(BookDTO candidate, boolean isUpdate) {
        String normalizedCandidateName = normalizeText(candidate.getBookName());
        List<BookDTO> currentBooks = bookDAO.selectAllBooks();

        for (BookDTO existing : currentBooks) {
            if (isUpdate && existing.getBookId() == candidate.getBookId()) {
                continue;
            }
            String normalizedExistingName = normalizeText(existing.getBookName());

            if (normalizedExistingName.equals(normalizedCandidateName)) {
                if (sameAuthorIds(existing.getAuthorIdsList(), candidate.getAuthorIdsList()) &&
                        existing.getSupplierId() == candidate.getSupplierId() &&
                        normalizeText(existing.getTranslator()).equals(normalizeText(candidate.getTranslator()))) {

                    return "Sách đã tồn tại! (Trùng tên, tác giả, nhà cung cấp và người dịch)";
                }
            }
        }
        return null;
    }

    private boolean sameAuthorIds(List<Integer> firstAuthors, List<Integer> secondAuthors) {
        if (firstAuthors == null && secondAuthors == null) return true;
        if (firstAuthors == null || secondAuthors == null) return false;

        if (firstAuthors.size() != secondAuthors.size()) {
            return false;
        }

        for (Integer id : firstAuthors) {
            if (!secondAuthors.contains(id)) {
                return false;
            }
        }
        return true;
    }

    private String normalizeText(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    public void addAuthorsToBook(int bookId, List<Integer> authorIds) {
        bookAuthorDAO.addAuthorsToBook(bookId, authorIds);
    }

    public void removeAllAuthorsFromBook(int bookId) {
        bookAuthorDAO.removeAllAuthorsFromBook(bookId);
    }
}
