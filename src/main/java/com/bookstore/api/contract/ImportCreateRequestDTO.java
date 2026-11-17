package com.bookstore.api.contract;

import com.bookstore.dto.ImportDetailDTO;
import com.bookstore.dto.ImportTicketDTO;

import java.util.List;

public class ImportCreateRequestDTO {
    private ImportTicketDTO ticket;
    private List<ImportDetailDTO> details;

    public ImportCreateRequestDTO() {
    }

    public ImportCreateRequestDTO(ImportTicketDTO ticket, List<ImportDetailDTO> details) {
        this.ticket = ticket;
        this.details = details;
    }

    public ImportTicketDTO getTicket() {
        return ticket;
    }

    public void setTicket(ImportTicketDTO ticket) {
        this.ticket = ticket;
    }

    public List<ImportDetailDTO> getDetails() {
        return details;
    }

    public void setDetails(List<ImportDetailDTO> details) {
        this.details = details;
    }
}
