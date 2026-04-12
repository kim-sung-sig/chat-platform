package com.example.chat.ops.contract.rbac;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class RolePermissionMatrix {
    private static final Map<ProjectRole, Set<OpsAction>> PERMISSIONS = new EnumMap<>(ProjectRole.class);

    static {
        PERMISSIONS.put(ProjectRole.OWNER, EnumSet.allOf(OpsAction.class));
        PERMISSIONS.put(ProjectRole.OPERATOR, EnumSet.of(
                OpsAction.TICKET_SYNC,
                OpsAction.PLAN_CREATE,
                OpsAction.AGENT_CONTROL,
                OpsAction.ALERT_INGEST,
                OpsAction.NOTIFICATION_PUBLISH,
                OpsAction.AUDIT_READ
        ));
        PERMISSIONS.put(ProjectRole.REVIEWER, EnumSet.of(
                OpsAction.PLAN_APPROVE,
                OpsAction.AUDIT_READ
        ));
        PERMISSIONS.put(ProjectRole.VIEWER, EnumSet.of(OpsAction.AUDIT_READ));
    }

    private RolePermissionMatrix() {
    }

    public static boolean isAllowed(ProjectRole role, OpsAction action) {
        return PERMISSIONS.getOrDefault(role, EnumSet.noneOf(OpsAction.class)).contains(action);
    }
}
