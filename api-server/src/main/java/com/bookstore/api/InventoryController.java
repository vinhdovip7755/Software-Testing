package com.bookstore.api;

import com.bookstore.bus.BookBUS;
import com.bookstore.bus.InventoryLogBUS;
import com.bookstore.dao.BookLotDAO;
import com.bookstore.dto.BookDTO;
import com.bookstore.dto.BookLotDTO;
import com.bookstore.dto.InventoryLogDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {
    private final AuthSessionStore sessionStore;

    public InventoryController(AuthSessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    @GetMapping("/books")
    public List<BookDTO> getInventoryBooks(@RequestHeader("Authorization") String authorization) {
        requireInventoryView(authorization);
        return new BookBUS().selectAllBooks();
    }

    @GetMapping("/books/{bookId}/lots")
    public List<BookLotDTO> getBookLots(
            @RequestHeader("Authorization") String authorization,
            @PathVariable int bookId
    ) {
        requireInventoryView(authorization);
        if (bookId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã sách không hợp lệ.");
        }
        return new BookLotDAO().getByBookId(bookId);
    }

    @GetMapping("/logs")
    public List<InventoryLogDTO> getInventoryLogs(@RequestHeader("Authorization") String authorization) {
        requireInventoryView(authorization);
        return new InventoryLogBUS().getAll();
    }

    private void requireInventoryView(String authorization) {
        sessionStore.requireAnyView(authorization, "MANAGE_INVENTORY");
    }
}
