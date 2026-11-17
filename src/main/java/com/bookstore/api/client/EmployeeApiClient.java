package com.bookstore.api.client;

import com.bookstore.api.contract.EmployeeActivityDTO;
import com.bookstore.dto.EmployeeDTO;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

public final class EmployeeApiClient {
    public List<EmployeeDTO> getEmployees() {
        return ApiClient.get("/api/employees", new TypeReference<>() {
        });
    }

    public List<EmployeeDTO> getEmployeesWithoutAccount() {
        return ApiClient.get("/api/employees/without-account", new TypeReference<>() {
        });
    }

    public EmployeeActivityDTO getActivity(int employeeId) {
        return ApiClient.get("/api/employees/" + employeeId + "/activity", new TypeReference<>() {
        });
    }

    public String addEmployee(EmployeeDTO employee) {
        return ApiClient.post("/api/employees", employee, new TypeReference<>() {
        });
    }

    public String updateEmployee(EmployeeDTO employee) {
        return ApiClient.put("/api/employees/" + employee.getEmployeeId(), employee, new TypeReference<>() {
        });
    }
}
