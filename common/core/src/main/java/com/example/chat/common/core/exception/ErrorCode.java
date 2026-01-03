package com.example.chat.common.core.exception;

/**
 * ?λ¬ μ½”λ“ ?Έν„°?μ΄??
 * λª¨λ“  ?λ¬ μ½”λ“?????Έν„°?μ΄?¤λ? κµ¬ν„?΄μ•Ό ??
 */
public interface ErrorCode {
    String getCode();
    String getMessage();
    int getStatus();
}
