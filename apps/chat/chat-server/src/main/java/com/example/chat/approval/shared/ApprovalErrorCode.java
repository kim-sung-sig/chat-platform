package com.example.chat.approval.shared;

public enum ApprovalErrorCode {
    APPROVAL_DOC_NOT_FOUND("APPROVAL-DOC-NOT-FOUND"),
    APPROVAL_LINE_OUT_OF_RANGE("APPROVAL-LINE-OUT-OF-RANGE"),
    APPROVAL_ALREADY_FINALIZED("APPROVAL-ALREADY-FINALIZED");

    private final String code;

    ApprovalErrorCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
