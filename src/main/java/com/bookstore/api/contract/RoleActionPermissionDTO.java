package com.bookstore.api.contract;

public class RoleActionPermissionDTO {
    private int actionId;
    private String actionCode;
    private String actionName;
    private boolean canView;
    private boolean canAction;

    public RoleActionPermissionDTO() {
    }

    public RoleActionPermissionDTO(
            int actionId,
            String actionCode,
            String actionName,
            boolean canView,
            boolean canAction
    ) {
        this.actionId = actionId;
        this.actionCode = actionCode;
        this.actionName = actionName;
        this.canView = canView;
        this.canAction = canAction;
    }

    public int getActionId() {
        return actionId;
    }

    public void setActionId(int actionId) {
        this.actionId = actionId;
    }

    public String getActionCode() {
        return actionCode;
    }

    public void setActionCode(String actionCode) {
        this.actionCode = actionCode;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public boolean isCanView() {
        return canView;
    }

    public void setCanView(boolean canView) {
        this.canView = canView;
    }

    public boolean isCanAction() {
        return canAction;
    }

    public void setCanAction(boolean canAction) {
        this.canAction = canAction;
    }
}
