package com.example.chat.ops.contract.error;

import com.example.chat.common.core.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OpsErrorCode implements ErrorCode {
    INVALID_ROLE("OPS-AUTH-001", "Invalid project role header", 400),
    FORBIDDEN_ACTION("OPS-AUTH-002", "Role is not allowed to perform this action", 403),
    PLUGIN_ALREADY_REGISTERED("OPS-PLUG-001", "Plugin already registered", 409),
    PLUGIN_NOT_FOUND("OPS-PLUG-002", "Plugin not found", 404),
    GITHUB_SIGNATURE_INVALID("OPS-GH-001", "Invalid GitHub webhook signature", 401),
    PLAN_NOT_FOUND("OPS-PLAN-001", "Plan not found", 404),
    INVALID_APPROVAL_DECISION("OPS-APR-001", "Invalid approval decision", 400);

    private final String code;
    private final String message;
    private final int status;
}
