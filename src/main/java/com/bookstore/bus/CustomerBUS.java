package com.bookstore.bus;

import com.bookstore.dao.CustomerDAO;
import com.bookstore.dto.CustomerDTO;

import java.util.List;

public class CustomerBUS {
    private final CustomerDAO customerDAO = new CustomerDAO();

    public CustomerDTO selectByPhone(String phone) {
        return customerDAO.selectByPhone(phone);
    }

    public List<CustomerDTO> selectAllCustomers() {
        return customerDAO.selectAllCustomers();
    }

    public String updateCustomerInfo(CustomerDTO c) {
        if (c.getCustomerName().trim().isEmpty()) {
            return "Tên khách hàng không được để trống!";
        }

        if (c.getCustomerPhone().trim().isEmpty()) {
            return "Số điện thoại không được để trống!";
        }

        if (!c.getCustomerPhone().matches("^0\\d{9}$")) {
            return "Số điện thoại không hợp lệ (Phải có 10 số và bắt đầu bằng 0)!";
        }

        if (customerDAO.isPhoneExist(c.getCustomerPhone(), c.getCustomerId())) {
            return "Số điện thoại này đã thuộc về khách hàng khác!";
        }

        if (customerDAO.isNameExist(c.getCustomerName(), c.getCustomerId())) {
            return "Tên khách hàng này đã tồn tại trong hệ thống!";
        }

        if (customerDAO.updateCustomerInfo(c)) {
            return "Cập nhật thành công!";
        }
        return "Cập nhật thất bại!";
    }

    public Object[] getCustomerStatistics(int customerId) {
        return customerDAO.getCustomerStatistics(customerId);
    }

    public String insertCustomer(CustomerDTO c) {
        if (c.getCustomerName().trim().isEmpty()) {
            return "Tên khách hàng không được để trống!";
        }
        if (c.getCustomerPhone().trim().isEmpty()) {
            return "Số điện thoại không được để trống!";
        }

        if (!c.getCustomerPhone().matches("^0\\d{9}$")) {
            return "Số điện thoại không hợp lệ (Phải có 10 số và bắt đầu bằng 0)!";
        }

        if (customerDAO.isPhoneExist(c.getCustomerPhone(), 0)) {
            return "Số điện thoại này đã tồn tại!";
        }
        if (customerDAO.isNameExist(c.getCustomerName(), 0)) {
            return "Tên khách hàng này đã tồn tại!";
        }

        if (customerDAO.insertCustomer(c)) {
            return "Thêm khách hàng thành công!";
        }
        return "Thêm khách hàng thất bại!";
    }
}
