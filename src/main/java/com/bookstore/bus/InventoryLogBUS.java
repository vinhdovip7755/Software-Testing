package com.bookstore.bus;
import com.bookstore.dao.InventoryLogDAO;
import com.bookstore.dto.InventoryLogDTO;
import java.util.List;

public class InventoryLogBUS {
    private InventoryLogDAO dao = new InventoryLogDAO();

    public List<InventoryLogDTO> getAll() { return dao.getAll(); }

    public void addLog(InventoryLogDTO dto) {
        dao.insert(dto);
    }
}