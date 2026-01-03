package com.example.chat.common.security.jwt.annotation;

import com.example.chat.common.security.jwt.config.JwtSecurityAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * JWT ë³´ì•ˆ ?¤ì •???œì„±?”í•˜???´ë…¸?Œì´??
 * JWT ?¸ì¦ ?„í„° ë°?ë³´ì•ˆ ?¤ì •???ë™?¼ë¡œ êµ¬ì„±?©ë‹ˆ??
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(JwtSecurityAutoConfiguration.class)
public @interface EnableJwtSecurity {
}
