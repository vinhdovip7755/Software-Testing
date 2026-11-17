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
    public BookDTO getBook(@RequestHeader("Authorization") String authorization, @PathVariable int id) {
        sessionStore.requireAnyView(authorization,
                "MANAGE_PRODUCT", "MANAGE_AUTHOR", "MANAGE_CATEGORY", "MANAGE_SUPPLIER", "MANAGE_IMPORT",
                "MANAGE_SELLING");
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
        return ApiResponses.fromBusinessMessage(bookBUS.addBook(book));
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateBook(
            @RequestHeader("Authorization") String authorization,
            @PathVariable int id,
            @RequestBody BookDTO book
    ) {
        sessionStore.requireAction(authorization, "MANAGE_PRODUCT");
        book.setBookId(id);
        return ApiResponses.fromBusinessMessage(bookBUS.updateBook(book));
    }
}
