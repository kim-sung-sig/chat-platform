package com.example.chat.ops.contract.rbac;

import com.example.chat.ops.contract.error.OpsErrorCode;
import com.example.chat.ops.contract.error.OpsException;
import java.util.Locale;

public final class OpsAuthorization {
    private OpsAuthorization() {
    }

    public static ProjectRole parseRole(String rawRole) {
        if (rawRole == null || rawRole.isBlank()) {
            throw new OpsException(OpsErrorCode.INVALID_ROLE);
        }

        try {
            return ProjectRole.valueOf(rawRole.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new OpsException(OpsErrorCode.INVALID_ROLE, ex);
        }
    }

    public static void require(String rawRole, OpsAction action) {
        ProjectRole role = parseRole(rawRole);
        if (!RolePermissionMatrix.isAllowed(role, action)) {
            throw new OpsException(OpsErrorCode.FORBIDDEN_ACTION);
        }
    }
}
