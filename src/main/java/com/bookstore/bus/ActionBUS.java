package com.bookstore.bus;

import com.bookstore.dao.ActionDAO;
import com.bookstore.dto.ActionDTO;

import java.util.List;

public class ActionBUS {
    private ActionDAO actionDAO = new ActionDAO();

    public List<ActionDTO> selectAllActions() {
        return actionDAO.selectAllActions();
    }
}
