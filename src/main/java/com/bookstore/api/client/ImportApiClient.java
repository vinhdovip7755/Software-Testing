package com.bookstore.api.client;

import com.bookstore.api.contract.ImportCreateRequestDTO;
import com.bookstore.dto.ImportDetailDTO;
import com.bookstore.dto.ImportTicketDTO;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Arrays;
import java.util.List;

public class ImportApiClient {
    public List<ImportTicketDTO> getAllImports() {
        return ApiClient.get("/api/imports", new TypeReference<>() {
        });
    }

    public List<ImportDetailDTO> getDetailsByImportId(int importId) {
        return ApiClient.get("/api/imports/" + importId + "/details", new TypeReference<>() {
        });
    }

    public void createImport(ImportTicketDTO ticket, ImportDetailDTO[] details) {
        ApiClient.postNoContent(
                "/api/imports",
                new ImportCreateRequestDTO(ticket, Arrays.asList(details))
        );
    }

    public void approveImport(int importId) {
        ApiClient.postNoContent("/api/imports/" + importId + "/approve");
    }

    public void cancelImport(int importId) {
        ApiClient.postNoContent("/api/imports/" + importId + "/cancel");
    }
}
