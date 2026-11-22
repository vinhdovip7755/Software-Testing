package com.bookstore.bus;

import com.bookstore.dao.CategoryDAO;
import com.bookstore.dto.CategoryDTO;

import java.util.List;

public class CategoryBUS {
    public static final int MAX_NAME_LENGTH = 100;
    private final CategoryDAO categoryDAO;

    public CategoryBUS() {
        this(new CategoryDAO());
    }

    CategoryBUS(CategoryDAO categoryDAO) {
        this.categoryDAO = categoryDAO;
    }

    public List<CategoryDTO> selectAllCategories() {
        return categoryDAO.selectAllCategories();
    }

    public String addCategory(CategoryDTO category) {
        if (category == null) {
            return "Dữ liệu thể loại không hợp lệ!";
        }
        if (category.getCategoryName() == null || category.getCategoryName().trim().isEmpty()) {
            return "Tên thể loại không được để trống!";
        }

        String normalizedName = category.getCategoryName().trim();
        if (normalizedName.length() > MAX_NAME_LENGTH) {
            return "Tên thể loại không được vượt quá " + MAX_NAME_LENGTH + " ký tự!";
        }

        if (categoryDAO.isNameExist(normalizedName, 0)) {
            return "Tên thể loại này đã tồn tại trong hệ thống!";
        }

        category.setCategoryName(normalizedName);
        int generatedId = categoryDAO.add(category);

        if (generatedId != -1) {
            return "Thêm thể loại thành công!";
        }
        return "Thêm thể loại thất bại!";
    }

    public String updateCategory(CategoryDTO category) {
        if (category == null) {
            return "Dữ liệu thể loại không hợp lệ!";
        }
        if (category.getCategoryId() <= 0) {
            return "ID thể loại không hợp lệ!";
        }
        if (category.getCategoryName() == null || category.getCategoryName().trim().isEmpty()) {
            return "Tên thể loại không được để trống!";
        }

        String normalizedName = category.getCategoryName().trim();
        if (normalizedName.length() > MAX_NAME_LENGTH) {
            return "Tên thể loại không được vượt quá " + MAX_NAME_LENGTH + " ký tự!";
        }

        if (categoryDAO.isNameExist(normalizedName, category.getCategoryId())) {
            return "Tên thể loại này đã tồn tại trong hệ thống!";
        }

        category.setCategoryName(normalizedName);
        if (categoryDAO.update(category)) {
            return "Cập nhật thành công!";
        }
        return "Cập nhật thất bại!";
    }

    public CategoryDTO getById(int id) {
        return categoryDAO.getCategoryById(id);
    }
}
