package com.bookstore.dto;

public class ActionDTO {
    private int actionId;
    private String actionCode;
    private String actionName;

    public ActionDTO(int actionId, String actionCode, String actionName) {
        this.actionId = actionId;
        this.actionCode = actionCode;
        this.actionName = actionName;
    }

    public int getActionId() { return actionId; }
    public void setActionId(int actionId) { this.actionId = actionId; }

    public String getActionCode() { return actionCode; }
    public void setActionCode(String actionCode) { this.actionCode = actionCode; }

    public String getActionName() { return actionName; }
    public void setActionName(String actionName) { this.actionName = actionName; }
}