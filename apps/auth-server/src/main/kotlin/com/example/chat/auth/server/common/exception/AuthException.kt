
package com.example.chat.auth.server.common.exception

import com.example.chat.common.core.exception.BaseException
import com.example.chat.common.core.exception.ErrorCode

class AuthException(
    errorCode: ErrorCode,
    args: Array<out Any?>? =
        null,
    cause: Throwable? =
        null
) : BaseException(errorCode, args, cause)
