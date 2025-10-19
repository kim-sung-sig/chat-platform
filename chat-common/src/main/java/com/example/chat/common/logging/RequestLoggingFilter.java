package com.example.chat.common.logging;

/**
 * NOTE: RequestLoggingFilter was intentionally moved out of `chat-common` because it
 * depends on Servlet/Spring Web classes which are not available to the lightweight
 * `chat-common` module. Keep MdcUtil here (pure SLF4J/MDC utility).
 *
 * The implementations for runtime modules are located in:
 * - chat-message-server/src/main/java/com/example/chat/message/logging/RequestLoggingFilter.java
 * - chat-websocket-server/src/main/java/com/example/chat/websocket/logging/RequestLoggingFilter.java
 *
 * This placeholder prevents accidental usage of web APIs from the common module.
 */
@SuppressWarnings({"unused", "DeprecatedIsStillUsed"})
@Deprecated
public final class RequestLoggingFilter {
    private RequestLoggingFilter() {}
}