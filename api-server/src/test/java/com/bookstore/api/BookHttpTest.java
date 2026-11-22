package com.bookstore.api;

import com.bookstore.api.contract.ImportCreateRequestDTO;
import com.bookstore.bus.BookBUS;
import com.bookstore.bus.ImportBUS;
import com.bookstore.dao.BookDAO;
import com.bookstore.dto.BookDTO;
import com.bookstore.dto.EmployeeDTO;
import com.bookstore.dto.ImportDetailDTO;
import com.bookstore.dto.ImportTicketDTO;
import com.bookstore.dto.PermissionDTO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

    @Test
    void forcesZeroQuantityOnCreateAndStripsQuantityOnUpdate() throws Exception {
        AuthSessionStore sessions = new AuthSessionStore();
        String token = sessions.createToken(new EmployeeDTO(),
                Map.of("MANAGE_PRODUCT", new PermissionDTO("MANAGE_PRODUCT", true, true)));

        try (var books = mockConstruction(BookBUS.class, (mock, context) -> {
            when(mock.addBook(any(BookDTO.class))).thenReturn("Thêm sách thành công!");
            when(mock.updateBook(any(BookDTO.class))).thenReturn("Cập nhật thành công!");
        })) {
            var mvc = MockMvcBuilders.standaloneSetup(new BookController(sessions))
                    .setControllerAdvice(new ApiExceptionHandler()).build();

            String createPayload = """
                    {"bookName":"Sách Test","coverPrice":95000,"quantity":5000,"status":1,
                     "categoryId":1,"supplierId":1,"authorIdsList":[1]}
                    """;
            mvc.perform(post("/api/books")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createPayload))
                    .andExpect(status().isOk());

            ArgumentCaptor<BookDTO> addCaptor = ArgumentCaptor.forClass(BookDTO.class);
            verify(books.constructed().get(0)).addBook(addCaptor.capture());
            assertEquals(0, addCaptor.getValue().getQuantity());
            assertEquals(95000, addCaptor.getValue().getCoverPrice());

            String updatePayload = """
                    {"bookName":"Sách Test","coverPrice":95000,"quantity":999,"status":1,
                     "categoryId":1,"supplierId":1,"authorIdsList":[1]}
                    """;
            mvc.perform(put("/api/books/10")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updatePayload))
                    .andExpect(status().isOk());

            ArgumentCaptor<BookDTO> updateCaptor = ArgumentCaptor.forClass(BookDTO.class);
            verify(books.constructed().get(0)).updateBook(updateCaptor.capture());
            assertEquals(10, updateCaptor.getValue().getBookId());
            assertEquals(0, updateCaptor.getValue().getQuantity());
        }
    }

    @Test
    void bookBusRejectsInvalidUpdateAndNullAuthorElementsAndDetectsDuplicateAuthorSets() {
        BookDTO existing = new BookDTO();
        existing.setBookId(1);
        existing.setBookName("Sách Cũ");
        existing.setSupplierId(1);
        existing.setTranslator("");
        existing.setAuthorIdsList(List.of(1));

        try (var bookDaos = mockConstruction(BookDAO.class,
                (mock, context) -> when(mock.selectAllBooks()).thenReturn(List.of(existing)))) {
            BookBUS bookBUS = new BookBUS();

            BookDTO nullAuthor = validBook();
            nullAuthor.setBookId(2);
            nullAuthor.setAuthorIdsList(Arrays.asList(1, null));
            assertTrue(bookBUS.updateBook(nullAuthor).contains("không hợp lệ"));

            BookDTO emptyAuthors = validBook();
            emptyAuthors.setBookId(2);
            emptyAuthors.setAuthorIdsList(List.of());
            assertTrue(bookBUS.updateBook(emptyAuthors).contains("tác giả"));

            BookDTO negativeCover = validBook();
            negativeCover.setBookId(2);
            negativeCover.setCoverPrice(-100);
            assertTrue(bookBUS.updateBook(negativeCover).contains("Giá bìa"));

            BookDTO invalidStatus = validBook();
            invalidStatus.setBookId(2);
            invalidStatus.setStatus(9);
            assertTrue(bookBUS.updateBook(invalidStatus).contains("Trạng thái"));

            BookDTO traversalImage = validBook();
            traversalImage.setBookId(2);
            traversalImage.setImage("../../etc/passwd");
            assertTrue(bookBUS.updateBook(traversalImage).contains("ảnh"));

            BookDTO dupAuthorSet = validBook();
            dupAuthorSet.setBookName("Sách Cũ");
            dupAuthorSet.setAuthorIdsList(List.of(1, 1));
            assertTrue(bookBUS.addBook(dupAuthorSet).contains("đã tồn tại"));
        }
    }

    @Test
    void importControllerRejectsDuplicateBookIdsOverflowAndUsesDynamicApprovalPolicy() {
        AuthSessionStore sessions = new AuthSessionStore();
        EmployeeDTO customRoleApprover = new EmployeeDTO();
        customRoleApprover.setEmployeeId(4);
        customRoleApprover.setRoleId(4);
        customRoleApprover.setStatus(1);
        String token = sessions.createToken(
                customRoleApprover,
                Map.of(
                        "MANAGE_IMPORT", new PermissionDTO("MANAGE_IMPORT", true, true),
                        "MANAGE_IMPORT_TICKET", new PermissionDTO("MANAGE_IMPORT_TICKET", true, true)
                )
        );

        BookDTO book = new BookDTO();
        book.setBookId(1);
        book.setSupplierId(2);
        book.setCoverPrice(100_000);

        ImportTicketDTO pendingOther = new ImportTicketDTO();
        pendingOther.setImportID(15);
        pendingOther.setEmployeeID(9);
        pendingOther.setStatus(1);

        ImportTicketDTO alreadyApproved = new ImportTicketDTO();
        alreadyApproved.setImportID(16);
        alreadyApproved.setEmployeeID(9);
        alreadyApproved.setStatus(2);

        ImportTicketDTO ownTicket = new ImportTicketDTO();
        ownTicket.setImportID(17);
        ownTicket.setEmployeeID(4);
        ownTicket.setStatus(1);

        try (var books = mockConstruction(BookBUS.class, (mock, context) -> {
            when(mock.selectById(1)).thenReturn(book);
            when(mock.addBook(any(BookDTO.class))).thenReturn("Thêm sách thành công!");
        });
             var imports = mockConstruction(ImportBUS.class, (mock, context) -> {
                 when(mock.getTicketById(15)).thenReturn(pendingOther);
                 when(mock.getTicketById(16)).thenReturn(alreadyApproved);
                 when(mock.getTicketById(17)).thenReturn(ownTicket);
                 when(mock.approveImport(eq(15), any(EmployeeDTO.class), any())).thenReturn(true);
             })) {
            ImportController controller = new ImportController(sessions);

            ImportTicketDTO ticket = new ImportTicketDTO();
            ticket.setSupplierID(2);
            ImportCreateRequestDTO dupRequest = new ImportCreateRequestDTO(
                    ticket,
                    List.of(
                            new ImportDetailDTO(0, 1, 10, 60_000, 100_000, 40),
                            new ImportDetailDTO(0, 1, 20, 10_000, 100_000, 90)
                    )
            );
            ResponseStatusException dupEx = assertThrows(
                    ResponseStatusException.class,
                    () -> controller.createImport("Bearer " + token, dupRequest)
            );
            assertEquals(400, dupEx.getStatusCode().value());

            assertEquals(204, controller.approveImport("Bearer " + token, 15).getStatusCode().value());

            ResponseStatusException approvedEx = assertThrows(
                    ResponseStatusException.class,
                    () -> controller.approveImport("Bearer " + token, 16)
            );
            assertEquals(409, approvedEx.getStatusCode().value());

            ResponseStatusException selfEx = assertThrows(
                    ResponseStatusException.class,
                    () -> controller.approveImport("Bearer " + token, 17)
            );
            assertEquals(403, selfEx.getStatusCode().value());

            BookDTO quickBook = validBook();
            quickBook.setQuantity(500);
            controller.quickAddBook("Bearer " + token, quickBook);
            ArgumentCaptor<BookDTO> quickCaptor = ArgumentCaptor.forClass(BookDTO.class);
            verify(books.constructed().get(0)).addBook(quickCaptor.capture());
            assertEquals(0, quickCaptor.getValue().getQuantity());
        }
    }

    private BookDTO validBook() {
        BookDTO book = new BookDTO();
        book.setBookName("Sách Mới");
        book.setCoverPrice(85_000);
        book.setStatus(1);
        book.setCategoryId(1);
        book.setSupplierId(1);
        book.setAuthorIdsList(List.of(1));
        return book;
    }
}
