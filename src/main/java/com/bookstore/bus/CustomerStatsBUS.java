package com.bookstore.bus;

import com.bookstore.dao.CustomerStatsDAO;
import com.bookstore.dto.CustomerHistoryDTO;
import com.bookstore.dto.CustomerStatsDTO;

import java.sql.Timestamp;
import java.util.List;

public class CustomerStatsBUS {
    private CustomerStatsDAO customerStatsDAO = new CustomerStatsDAO();

    public List<CustomerStatsDTO> getTopCustomersByQuantity(Timestamp fromDate, Timestamp toDate) {
        return customerStatsDAO.getTopCustomersByQuantity(fromDate, toDate);
    }

    public List<CustomerStatsDTO> getTopCustomersBySpending(Timestamp fromDate, Timestamp toDate) {
        return customerStatsDAO.getTopCustomersBySpending(fromDate, toDate);
    }

    public List<CustomerHistoryDTO> getCustomerHistory(int customerId, java.sql.Timestamp fromDate, java.sql.Timestamp toDate) {
        return customerStatsDAO.getCustomerHistory(customerId, fromDate, toDate);
    }
}
