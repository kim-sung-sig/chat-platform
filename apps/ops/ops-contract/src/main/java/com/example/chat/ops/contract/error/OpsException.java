package com.example.chat.ops.contract.error;

import com.example.chat.common.core.exception.BaseException;

public class OpsException extends BaseException {
    public OpsException(OpsErrorCode errorCode) {
        super(errorCode);
    }

    public OpsException(OpsErrorCode errorCode, Throwable cause) {
        super(errorCode, null, cause);
    }
}
