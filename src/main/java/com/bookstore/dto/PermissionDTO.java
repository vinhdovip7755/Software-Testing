package com.bookstore.dto;

public class PermissionDTO {
    private String actionCode;
    private boolean isView;
    private boolean isAction;

    public PermissionDTO(String actionCode, boolean isView, boolean isAction) {
        this.actionCode = actionCode;
        this.isView = isView;
        this.isAction = isAction;
    }

    public String getActionCode() {
        return actionCode;
    }

    public void setActionCode(String actionCode) {
        this.actionCode = actionCode;
    }

    public boolean isView() {
        return isView;
    }

    public void setView(boolean view) {
        isView = view;
    }

    public boolean isAction() {
        return isAction;
    }

    public void setAction(boolean action) {
        isAction = action;
    }
}
