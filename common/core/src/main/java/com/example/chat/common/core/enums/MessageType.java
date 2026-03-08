package com.example.chat.common.core.enums;

/** 메시지 타입 */
public enum MessageType {
    TEXT, IMAGE, FILE, SYSTEM, VIDEO, AUDIO;

    public boolean isSystemType() { return this == SYSTEM; }
}
