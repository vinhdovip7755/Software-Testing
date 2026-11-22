package com.bookstore.bus;

import com.bookstore.dao.CategoryDAO;
import com.bookstore.dto.CategoryDTO;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CategoryBUSTest {
    private static class InMemoryCategoryDAO extends CategoryDAO {
        private final List<CategoryDTO> storage = new ArrayList<>();
        private int nextId = 1;

        @Override
        public List<CategoryDTO> selectAllCategories() {
            return new ArrayList<>(storage);
        }

        @Override
        public int add(CategoryDTO category) {
            int id = nextId++;
            storage.add(new CategoryDTO(id, category.getCategoryName()));
            return id;
        }

        @Override
        public boolean update(CategoryDTO category) {
            for (CategoryDTO existing : storage) {
                if (existing.getCategoryId() == category.getCategoryId()) {
                    existing.setCategoryName(category.getCategoryName());
                    return true;
                }
            }
            return true;
        }

        @Override
        public boolean isNameExist(String name, int ignoreId) {
            return storage.stream().anyMatch(c ->
                    c.getCategoryId() != ignoreId && c.getCategoryName().equalsIgnoreCase(name));
        }
    }

    private final CategoryBUS categoryBUS = new CategoryBUS(new InMemoryCategoryDAO());

    @Test
    void addCategoryRejectsNullDto() {
        assertEquals("Dữ liệu thể loại không hợp lệ!", categoryBUS.addCategory(null));
    }

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
    void addCategoryRejectsOverMaxLength() {
        CategoryDTO category = new CategoryDTO(0, "A".repeat(CategoryBUS.MAX_NAME_LENGTH + 1));
        assertEquals("Tên thể loại không được vượt quá 100 ký tự!", categoryBUS.addCategory(category));
    }

    @Test
    void addCategoryAcceptsDigitsPerT238() {
        CategoryDTO category = new CategoryDTO(0, "123");
        assertEquals("Thêm thể loại thành công!", categoryBUS.addCategory(category));
    }

    @Test
    void addCategoryAcceptsSymbolsWithoutLettersPerT238() {
        CategoryDTO category = new CategoryDTO(0, "---");
        assertEquals("Thêm thể loại thành công!", categoryBUS.addCategory(category));
    }

    @Test
    void addCategoryAcceptsLettersAndDigitsPerT238() {
        CategoryDTO category = new CategoryDTO(0, "Truyện 123");
        assertEquals("Thêm thể loại thành công!", categoryBUS.addCategory(category));
    }

    @Test
    void addCategoryAcceptsSpecialSymbolsPerT238() {
        CategoryDTO category = new CategoryDTO(0, "Khoa học & Công nghệ");
        assertEquals("Thêm thể loại thành công!", categoryBUS.addCategory(category));
    }

    @Test
    void updateCategoryRejectsInvalidId() {
        CategoryDTO category = new CategoryDTO(0, "Văn học");
        assertEquals("ID thể loại không hợp lệ!", categoryBUS.updateCategory(category));
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
    void updateCategoryAcceptsDigitsPerT238() {
        CategoryDTO category = new CategoryDTO(1, "123");
        assertEquals("Cập nhật thành công!", categoryBUS.updateCategory(category));
    }

    @Test
    void updateCategoryAcceptsLettersAndDigitsPerT238() {
        CategoryDTO category = new CategoryDTO(1, "Truyện 123");
        assertEquals("Cập nhật thành công!", categoryBUS.updateCategory(category));
    }

    @Test
    void updateCategoryAcceptsSpecialSymbolsPerT238() {
        CategoryDTO category = new CategoryDTO(1, "Khoa học & Công nghệ");
        assertEquals("Cập nhật thành công!", categoryBUS.updateCategory(category));
    }
}
