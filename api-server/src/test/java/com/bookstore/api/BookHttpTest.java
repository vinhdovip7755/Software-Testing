package com.bookstore.api;

import com.bookstore.bus.BookBUS;
import com.bookstore.dto.BookDTO;
import com.bookstore.dto.EmployeeDTO;
import com.bookstore.dto.PermissionDTO;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BookHttpTest {
    @Test
    void resolvesBookIdFromUrlForStockChecks() throws Exception {
        AuthSessionStore sessions = new AuthSessionStore();
        String token = sessions.createToken(new EmployeeDTO(),
                Map.of("MANAGE_SELLING", new PermissionDTO("MANAGE_SELLING", true, true)));
        BookDTO book = new BookDTO();
        book.setBookId(42);
        book.setQuantity(7);
        try (var books = mockConstruction(BookBUS.class,
                (mock, context) -> when(mock.selectById(42)).thenReturn(book))) {
            var mvc = MockMvcBuilders.standaloneSetup(new BookController(sessions))
                    .setControllerAdvice(new ApiExceptionHandler()).build();
            mvc.perform(get("/api/books/42").header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.bookId").value(42))
                    .andExpect(jsonPath("$.quantity").value(7));
        }
    }
}
