package com.bookstore.api;

import com.bookstore.bus.BookBUS;
import com.bookstore.dto.BookDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {
    private final BookBUS bookBUS = new BookBUS();
    private final AuthSessionStore sessionStore;

    public BookController(AuthSessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    @GetMapping
    public List<BookDTO> getBooks(@RequestHeader("Authorization") String authorization) {
        sessionStore.requireAnyView(authorization,
                "MANAGE_PRODUCT", "MANAGE_AUTHOR", "MANAGE_CATEGORY", "MANAGE_SUPPLIER", "MANAGE_IMPORT",
                "MANAGE_SELLING");
        return bookBUS.selectAllBooks();
    }

    @GetMapping("/active")
    public List<BookDTO> getActiveBooks(@RequestHeader("Authorization") String authorization) {
        sessionStore.requireAnyView(authorization,
                "MANAGE_PRODUCT", "MANAGE_AUTHOR", "MANAGE_CATEGORY", "MANAGE_SUPPLIER", "MANAGE_IMPORT",
                "MANAGE_SELLING");
        return bookBUS.selectActiveBooks();
    }

    @GetMapping("/{id}")
    public BookDTO getBook(@RequestHeader("Authorization") String authorization, @PathVariable("id") int id) {
        sessionStore.requireAnyView(authorization,
                "MANAGE_PRODUCT", "MANAGE_AUTHOR", "MANAGE_CATEGORY", "MANAGE_SUPPLIER", "MANAGE_IMPORT",
                "MANAGE_SELLING");
        if (id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã sách không hợp lệ.");
        }
        BookDTO book = bookBUS.selectById(id);
        if (book == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sách.");
        }
        return book;
    }

    @PostMapping
    public ResponseEntity<String> addBook(
            @RequestHeader("Authorization") String authorization,
            @RequestBody BookDTO book
    ) {
        sessionStore.requireAction(authorization, "MANAGE_PRODUCT");
        if (book == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thông tin sách không hợp lệ.");
        }
        BookDTO sanitized = copyAllowedMetadata(book, 0, true);
        return ApiResponses.fromBusinessMessage(bookBUS.addBook(sanitized));
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateBook(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("id") int id,
            @RequestBody BookDTO book
    ) {
        sessionStore.requireAction(authorization, "MANAGE_PRODUCT");
        if (id <= 0 || book == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thông tin cập nhật sách không hợp lệ.");
        }
        BookDTO sanitized = copyAllowedMetadata(book, id, false);
        return ApiResponses.fromBusinessMessage(bookBUS.updateBook(sanitized));
    }

    static BookDTO copyAllowedMetadata(BookDTO source, int bookId, boolean isCreate) {
        BookDTO copy = new BookDTO();
        copy.setBookId(isCreate ? 0 : bookId);
        copy.setBookName(source.getBookName());
        copy.setPublicationYear(source.getPublicationYear());
        copy.setCoverPrice(source.getCoverPrice());
        copy.setSellingPrice(isCreate ? source.getCoverPrice() : 0);
        // Never allow client-supplied quantity mass assignment.
        copy.setQuantity(0);
        copy.setTranslator(source.getTranslator());
        copy.setImage(source.getImage());
        copy.setDescription(source.getDescription());
        copy.setStatus(source.getStatus());
        copy.setCategoryId(source.getCategoryId());
        copy.setTagDetail(source.getTagDetail());
        copy.setSupplierId(source.getSupplierId());
        copy.setAuthorIdsList(source.getAuthorIdsList());
        return copy;
    }
}
