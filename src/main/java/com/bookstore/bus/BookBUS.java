package com.bookstore.bus;

import com.bookstore.dao.BookAuthorDAO;
import com.bookstore.dao.BookDAO;
import com.bookstore.dto.BookDTO;

import java.time.Year;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class BookBUS {
    public static final double MAX_PRICE = 999_999_999_999_999.0;
    private static final int MAX_NAME_LENGTH = 255;
    private static final int MAX_IMAGE_LENGTH = 255;

    private final BookDAO bookDAO = new BookDAO();
    private final BookAuthorDAO bookAuthorDAO = new BookAuthorDAO();

    public BookDTO selectById(int bookId) {
        return bookDAO.selectById(bookId);
    }

    public List<BookDTO> selectAllBooks() {
        return bookDAO.selectAllBooks();
    }

    public List<BookDTO> selectActiveBooks() {
        return bookDAO.selectAllBooks().stream()
                .filter(b -> b.getStatus() == 1)
                .toList();
    }

    public String addBook(BookDTO book) {
        String validationError = validateBook(book, false);
        if (validationError != null) {
            return validationError;
        }

        BookDTO sanitized = sanitizeBook(book, false);
        String duplicateCheck = validateDuplicate(sanitized, false);
        if (duplicateCheck != null) {
            return duplicateCheck;
        }

        int generatedId = bookDAO.addBookWithAuthorsTransaction(sanitized, sanitized.getAuthorIdsList());
        if (generatedId > 0) {
            book.setBookId(generatedId);
            book.setQuantity(0);
            book.setSellingPrice(sanitized.getCoverPrice());
            return "Thêm sách thành công!";
        }
        return "Thêm sách thất bại!";
    }

    public String updateBook(BookDTO book) {
        String validationError = validateBook(book, true);
        if (validationError != null) {
            return validationError;
        }

        BookDTO sanitized = sanitizeBook(book, true);
        String duplicateCheck = validateDuplicate(sanitized, true);
        if (duplicateCheck != null) {
            return duplicateCheck;
        }

        if (bookDAO.updateBookWithAuthorsTransaction(sanitized, sanitized.getAuthorIdsList())) {
            return "Cập nhật thành công!";
        }
        return "Cập nhật thất bại!";
    }

    private String validateBook(BookDTO book, boolean isUpdate) {
        if (book == null) {
            return "Thông tin sách không hợp lệ!";
        }
        if (isUpdate && book.getBookId() <= 0) {
            return "Mã sách không hợp lệ!";
        }
        if (book.getBookName() == null || book.getBookName().trim().isEmpty()) {
            return "Tên sách không được để trống!";
        }
        if (book.getBookName().trim().length() > MAX_NAME_LENGTH) {
            return "Tên sách không được vượt quá 255 ký tự!";
        }
        if (book.getAuthorIdsList() == null || book.getAuthorIdsList().isEmpty()) {
            return "Vui lòng chọn ít nhất một tác giả cho sách!";
        }
        for (Integer authorId : book.getAuthorIdsList()) {
            if (authorId == null || authorId <= 0) {
                return "Danh sách tác giả không hợp lệ!";
            }
        }
        if (book.getCategoryId() <= 0) {
            return "Vui lòng chọn thể loại cho sách!";
        }
        if (book.getSupplierId() <= 0) {
            return "Vui lòng chọn nhà cung cấp cho sách!";
        }
        if (!Double.isFinite(book.getCoverPrice()) || book.getCoverPrice() <= 0 || book.getCoverPrice() > MAX_PRICE) {
            return "Giá bìa của sách phải là số dương hợp lệ!";
        }
        if (book.getStatus() != 0 && book.getStatus() != 1) {
            return "Trạng thái sách không hợp lệ!";
        }
        int publicationYear = book.getPublicationYear();
        int maxYear = Year.now().getValue() + 1;
        if (publicationYear != 0 && (publicationYear < 1000 || publicationYear > maxYear)) {
            return "Năm xuất bản không hợp lệ!";
        }
        if (book.getImage() != null && !book.getImage().trim().isEmpty()) {
            String img = book.getImage().trim();
            if (!isSafeImageFilename(img)) {
                return "Tên tệp ảnh bìa không hợp lệ!";
            }
        }
        return null;
    }

    private boolean isSafeImageFilename(String filename) {
        if (filename.length() > MAX_IMAGE_LENGTH) {
            return false;
        }
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")
                || filename.contains(":") || filename.indexOf('\0') >= 0) {
            return false;
        }
        for (int i = 0; i < filename.length(); i++) {
            if (Character.isISOControl(filename.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private BookDTO sanitizeBook(BookDTO source, boolean isUpdate) {
        BookDTO sanitized = new BookDTO();
        sanitized.setBookId(isUpdate ? source.getBookId() : 0);
        sanitized.setBookName(source.getBookName().trim());
        sanitized.setPublicationYear(source.getPublicationYear());
        sanitized.setCoverPrice(source.getCoverPrice());
        sanitized.setSellingPrice(isUpdate ? source.getSellingPrice() : source.getCoverPrice());
        sanitized.setQuantity(0);
        sanitized.setTranslator(source.getTranslator() == null || source.getTranslator().trim().isEmpty()
                ? null
                : source.getTranslator().trim());
        sanitized.setImage(source.getImage() == null || source.getImage().trim().isEmpty()
                ? null
                : source.getImage().trim());
        sanitized.setDescription(source.getDescription() == null || source.getDescription().trim().isEmpty()
                ? null
                : source.getDescription().trim());
        sanitized.setStatus(source.getStatus());
        sanitized.setCategoryId(source.getCategoryId());
        sanitized.setTagDetail(source.getTagDetail() == null || source.getTagDetail().trim().isEmpty()
                ? null
                : source.getTagDetail().trim());
        sanitized.setSupplierId(source.getSupplierId());
        sanitized.setAuthorIdsList(new ArrayList<>(new LinkedHashSet<>(source.getAuthorIdsList())));
        return sanitized;
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
        Set<Integer> firstSet = firstAuthors == null ? Set.of() : new HashSet<>(firstAuthors);
        Set<Integer> secondSet = secondAuthors == null ? Set.of() : new HashSet<>(secondAuthors);
        return firstSet.equals(secondSet);
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
