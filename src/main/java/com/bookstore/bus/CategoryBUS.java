package com.bookstore.bus;

import com.bookstore.dao.CategoryDAO;
import com.bookstore.dto.CategoryDTO;

import java.util.List;

public class CategoryBUS {
    private final CategoryDAO categoryDAO = new CategoryDAO();

    public List<CategoryDTO> selectAllCategories() {
        return categoryDAO.selectAllCategories();
    }

    public String addCategory(CategoryDTO category) {
        if (category.getCategoryName() == null || category.getCategoryName().trim().isEmpty()) {
            return "Tên thể loại không được để trống!";
        }

        int generatedId = categoryDAO.add(category);

        if (generatedId != -1) {
            return "Thêm thể loại thành công!";
        }
        return "Thêm thể loại thất bại!";
    }

    public String updateCategory(CategoryDTO category) {
        if (category.getCategoryName() == null || category.getCategoryName().trim().isEmpty()) {
            return "Tên thể loại không được để trống!";
        }

        if (categoryDAO.update(category)) {
            return "Cập nhật thành công!";
        }
        return "Cập nhật thất bại!";
    }

    public CategoryDTO getById(int id) {
        return categoryDAO.getCategoryById(id);
    }
}
