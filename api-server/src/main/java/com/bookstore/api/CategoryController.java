package com.bookstore.api;

import com.bookstore.bus.CategoryBUS;
import com.bookstore.dto.CategoryDTO;
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
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryBUS categoryBUS = new CategoryBUS();
    private final AuthSessionStore sessionStore;

    public CategoryController(AuthSessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    @GetMapping
    public List<CategoryDTO> getCategories(@RequestHeader("Authorization") String authorization) {
        sessionStore.requireAnyView(authorization, "MANAGE_CATEGORY", "MANAGE_PRODUCT", "MANAGE_IMPORT",
                "MANAGE_SELLING", "MANAGE_PROMOTION");
        return categoryBUS.selectAllCategories();
    }

    @GetMapping("/{id}")
    public CategoryDTO getCategory(@RequestHeader("Authorization") String authorization, @PathVariable int id) {
        sessionStore.requireAnyView(authorization, "MANAGE_CATEGORY", "MANAGE_PRODUCT", "MANAGE_IMPORT",
                "MANAGE_SELLING", "MANAGE_PROMOTION");
        CategoryDTO category = categoryBUS.getById(id);
        if (category == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thể loại.");
        }
        return category;
    }

    @PostMapping
    public ResponseEntity<String> addCategory(
            @RequestHeader("Authorization") String authorization,
            @RequestBody CategoryDTO category
    ) {
        sessionStore.requireAction(authorization, "MANAGE_CATEGORY");
        return ApiResponses.fromBusinessMessage(categoryBUS.addCategory(category));
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateCategory(
            @RequestHeader("Authorization") String authorization,
            @PathVariable int id,
            @RequestBody CategoryDTO category
    ) {
        sessionStore.requireAction(authorization, "MANAGE_CATEGORY");
        category.setCategoryId(id);
        return ApiResponses.fromBusinessMessage(categoryBUS.updateCategory(category));
    }
}
