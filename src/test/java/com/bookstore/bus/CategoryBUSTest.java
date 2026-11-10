package com.bookstore.bus;

import com.bookstore.dto.CategoryDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CategoryBUSTest {
    private final CategoryBUS categoryBUS = new CategoryBUS();

    @Test
    void addCategoryRejectsNullName() {
        CategoryDTO category = new CategoryDTO(0, null);

        assertEquals("Tên thể loại không được để trống!", categoryBUS.addCategory(category));
    }

    @Test
    void addCategoryRejectsBlankName() {
        CategoryDTO category = new CategoryDTO(0, "   ");

        assertEquals("Tên thể loại không được để trống!", categoryBUS.addCategory(category));
    }

    @Test
    void addCategoryRejectsInvalidName() {
        CategoryDTO category = new CategoryDTO(0, "123");

        assertEquals("Tên thể loại không hợp lệ!", categoryBUS.addCategory(category));
    }

    @Test
    void addCategoryRejectsNameWithoutLetters() {
        CategoryDTO category = new CategoryDTO(0, "---");

        assertEquals("Tên thể loại không hợp lệ!", categoryBUS.addCategory(category));
    }

    @Test
    void addCategoryRejectsNameContainingDigits() {
        CategoryDTO category = new CategoryDTO(0, "Truyện 123");

        assertEquals("Tên thể loại không hợp lệ!", categoryBUS.addCategory(category));
    }

    @Test
    void addCategoryRejectsNameContainingUnsupportedSymbols() {
        CategoryDTO category = new CategoryDTO(0, "Khoa học & Công nghệ");

        assertEquals("Tên thể loại không hợp lệ!", categoryBUS.addCategory(category));
    }

    @Test
    void updateCategoryRejectsNullName() {
        CategoryDTO category = new CategoryDTO(1, null);

        assertEquals("Tên thể loại không được để trống!", categoryBUS.updateCategory(category));
    }

    @Test
    void updateCategoryRejectsBlankName() {
        CategoryDTO category = new CategoryDTO(1, "   ");

        assertEquals("Tên thể loại không được để trống!", categoryBUS.updateCategory(category));
    }

    @Test
    void updateCategoryRejectsWhitespaceOnlyName() {
        CategoryDTO category = new CategoryDTO(1, "\t\n");

        assertEquals("Tên thể loại không được để trống!", categoryBUS.updateCategory(category));
    }

    @Test
    void updateCategoryRejectsInvalidName() {
        CategoryDTO category = new CategoryDTO(1, "123");

        assertEquals("Tên thể loại không hợp lệ!", categoryBUS.updateCategory(category));
    }

    @Test
    void updateCategoryRejectsNameContainingDigits() {
        CategoryDTO category = new CategoryDTO(1, "Truyện 123");

        assertEquals("Tên thể loại không hợp lệ!", categoryBUS.updateCategory(category));
    }

    @Test
    void updateCategoryRejectsNameContainingUnsupportedSymbols() {
        CategoryDTO category = new CategoryDTO(1, "Khoa học & Công nghệ");

        assertEquals("Tên thể loại không hợp lệ!", categoryBUS.updateCategory(category));
    }
}
