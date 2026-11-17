package com.bookstore.util;

public final class ImportApprovalPolicy {
    private ImportApprovalPolicy() {
    }

    public static boolean canAct(int roleId, int actorId, int creatorId) {
        return (roleId == 1 || roleId == 2)
                && actorId > 0
                && creatorId > 0
                && actorId != creatorId;
    }
}
