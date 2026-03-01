package com.example.chat.common.logging.config;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.Ordered;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

import com.example.chat.common.logging.filter.ReactiveTracingFilter;
import com.example.chat.common.logging.filter.TracingFilter;

/**
 * EnableTracingLogging 이 선언된 서비스에 Tracing 필터를 등록한다.
 *
 * 환경 자동 감지:
 * Servlet 환경 -> TracingFilter 를 FilterRegistrationBean 으로 등록
 * Reactive 환경 -> ReactiveTracingFilter 를 WebFilter Bean 으로 등록
 *
 * AutoConfiguration 대신 명시적 어노테이션 기반으로 활성화하여
 * 선언하지 않으면 동작하지 않는다는 의도를 코드에 드러낸다.
 */
public class TracingLoggingRegistrar implements ImportBeanDefinitionRegistrar {

    private static final String SERVLET_CLASS = "jakarta.servlet.http.HttpServletRequest";
    private static final String REACTOR_CLASS = "reactor.core.publisher.Mono";

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
            BeanDefinitionRegistry registry) {
        if (isServletEnvironment()) {
            registerServletTracingFilter(registry);
        } else if (isReactiveEnvironment()) {
            registerReactiveTracingFilter(registry);
        }
    }

    private void registerServletTracingFilter(BeanDefinitionRegistry registry) {
        if (registry.containsBeanDefinition("tracingFilterRegistration")) {
            return;
        }
        BeanDefinitionBuilder filterBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(TracingFilter.class);
        filterBuilder
                .setAutowireMode(org.springframework.beans.factory.support.AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR);
        registry.registerBeanDefinition("tracingFilter", filterBuilder.getBeanDefinition());

        BeanDefinitionBuilder registrationBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(FilterRegistrationBean.class)
                .addPropertyReference("filter", "tracingFilter")
                .addPropertyValue("urlPatterns", new String[] { "/*" })
                .addPropertyValue("order", Ordered.HIGHEST_PRECEDENCE + 1)
                .addPropertyValue("name", "tracingFilterRegistration");
        registry.registerBeanDefinition("tracingFilterRegistration",
                registrationBuilder.getBeanDefinition());
    }

    private void registerReactiveTracingFilter(BeanDefinitionRegistry registry) {
        if (registry.containsBeanDefinition("reactiveTracingFilter")) {
            return;
        }
        BeanDefinitionBuilder builder = BeanDefinitionBuilder
                .genericBeanDefinition(ReactiveTracingFilter.class);
        builder.setAutowireMode(org.springframework.beans.factory.support.AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR);
        registry.registerBeanDefinition("reactiveTracingFilter", builder.getBeanDefinition());
    }

    private boolean isServletEnvironment() {
        return ClassUtils.isPresent(SERVLET_CLASS, getClass().getClassLoader());
    }

    private boolean isReactiveEnvironment() {
        return ClassUtils.isPresent(REACTOR_CLASS, getClass().getClassLoader());
    }
}
