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
        if (!supplier.getSupplierPhone().trim().matches("^0\\d{9}$"))
            return "Số điện thoại không hợp lệ! (Phải có 10 chữ số và bắt đầu bằng số 0)";

        if (supplierDAO.isNameExist(supplier.getSupplierName().trim(), 0))
            return "Tên nhà cung cấp này đã tồn tại trong hệ thống!";
        if (supplierDAO.isPhoneExist(supplier.getSupplierPhone().trim(), 0))
            return "Số điện thoại này đã thuộc về nhà cung cấp khác!";

        int generatedId = supplierDAO.add(supplier);

        if (generatedId != -1) {
            return "Thêm nhà cung cấp thành công!";
        }
        return "Thêm nhà cung cấp thất bại!";
    }

    public String updateSupplier(SupplierDTO supplier) {
        if (supplier.getSupplierName() == null || supplier.getSupplierName().trim().isEmpty())
            return "Tên nhà cung cấp không được để trống!";
        if (supplier.getSupplierPhone() == null || supplier.getSupplierPhone().trim().isEmpty())
            return "Số điện thoại không được để trống!";
        if (!supplier.getSupplierPhone().trim().matches("^0\\d{9}$"))
            return "Số điện thoại không hợp lệ! (Phải có 10 chữ số và bắt đầu bằng số 0)";

        if (supplierDAO.isNameExist(supplier.getSupplierName().trim(), supplier.getSupplierId()))
            return "Tên nhà cung cấp này đã tồn tại trong hệ thống!";
        if (supplierDAO.isPhoneExist(supplier.getSupplierPhone().trim(), supplier.getSupplierId()))
            return "Số điện thoại này đã thuộc về nhà cung cấp khác!";

        if (supplierDAO.update(supplier)) {
            return "Cập nhật thành công!";
        }
        return "Cập nhật thất bại!";
    }

    public SupplierDTO getById(int id) {
        return supplierDAO.getBySupplierId(id);
    }
}