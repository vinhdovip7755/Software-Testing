package com.bookstore.bus;

import com.bookstore.dao.SupplierDAO;
import com.bookstore.dto.SupplierDTO;
import java.util.List;

public class SupplierBUS {
    private final SupplierDAO supplierDAO = new SupplierDAO();

    public List<SupplierDTO> selectAll() {
        return supplierDAO.selectAllSuppliers();
    }

    public String addSupplier(SupplierDTO supplier) {
        if (supplier.getSupplierName() == null || supplier.getSupplierName().trim().isEmpty())
            return "Tên nhà cung cấp không được để trống!";
        if (supplier.getSupplierPhone() == null || supplier.getSupplierPhone().trim().isEmpty())
            return "Số điện thoại không được để trống!";

        int generatedId = supplierDAO.add(supplier);

        if (generatedId != -1) {
            return "Thêm nhà cung cấp thành công!";
        }
        return "Thêm nhà cung cấp thất bại!";
    }

    public String updateSupplier(SupplierDTO supplier) {
        if (supplier.getSupplierName() == null || supplier.getSupplierName().trim().isEmpty())
            return "Tên nhà cung cấp không được để trống!";

        if (supplierDAO.update(supplier)) {
            return "Cập nhật thành công!";
        }
        return "Cập nhật thất bại!";
    }

    public SupplierDTO getById(int id) {
        return supplierDAO.getBySupplierId(id);
    }
}