package com.bookstore.api;

import com.bookstore.bus.AuthorBUS;
import com.bookstore.dto.AuthorDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/authors")
public class AuthorController {
    private final AuthorBUS authorBUS = new AuthorBUS();
    private final AuthSessionStore sessionStore;

    public AuthorController(AuthSessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    @GetMapping
    public List<AuthorDTO> getAuthors(@RequestHeader("Authorization") String authorization) {
        sessionStore.requireAnyView(authorization, "MANAGE_AUTHOR", "MANAGE_PRODUCT", "MANAGE_IMPORT",
                "MANAGE_SELLING");
        return authorBUS.selectAllAuthors();
    }

    @PostMapping
    public ResponseEntity<String> addAuthor(
            @RequestHeader("Authorization") String authorization,
            @RequestBody AuthorDTO author
    ) {
        sessionStore.requireAction(authorization, "MANAGE_AUTHOR");
        return ApiResponses.fromBusinessMessage(authorBUS.addAuthor(author));
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateAuthor(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("id") int id,
            @RequestBody AuthorDTO author
    ) {
        sessionStore.requireAction(authorization, "MANAGE_AUTHOR");
        author.setAuthorId(id);
        return ApiResponses.fromBusinessMessage(authorBUS.updateAuthor(author));
    }
}
