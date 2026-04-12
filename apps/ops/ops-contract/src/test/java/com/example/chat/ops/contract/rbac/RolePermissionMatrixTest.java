package com.example.chat.ops.contract.rbac;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RolePermissionMatrixTest {

    @Test
    @DisplayName("OWNER can approve plans")
    void ownerCanApprovePlan() {
        assertThat(RolePermissionMatrix.isAllowed(ProjectRole.OWNER, OpsAction.PLAN_APPROVE)).isTrue();
    }

    @Test
    @DisplayName("REVIEWER can approve plans")
    void reviewerCanApprovePlan() {
        assertThat(RolePermissionMatrix.isAllowed(ProjectRole.REVIEWER, OpsAction.PLAN_APPROVE)).isTrue();
    }

    @Test
    @DisplayName("VIEWER cannot approve plans")
    void viewerCannotApprovePlan() {
        assertThat(RolePermissionMatrix.isAllowed(ProjectRole.VIEWER, OpsAction.PLAN_APPROVE)).isFalse();
    }
}
