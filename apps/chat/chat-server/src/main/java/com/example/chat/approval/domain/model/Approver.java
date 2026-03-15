package com.example.chat.approval.domain.model;

import java.util.Objects;

public class Approver {
    private final ApproverType type;
    private final String refId;

    public Approver(ApproverType type, String refId) {
        this.type = Objects.requireNonNull(type, "type");
        this.refId = Objects.requireNonNull(refId, "refId");
    }

    public ApproverType getType() {
        return type;
    }

    public String getRefId() {
        return refId;
    }
}
