package com.aidsp.platform.core.interceptor;

import com.aidsp.platform.core.dto.RestResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Set;

/**
 * 全局响应包装 Advice。
 *
 * <p>对 {@code com.aidsp.platform} 包下的 Controller 返回值统一包装为 {@link RestResponse}。
 * 排除：{@link ResponseEntity}（文件下载等）、{@link String}（避免误转）、已是 {@link RestResponse}、Knife4j / Swagger 路径。
 */
@RestControllerAdvice(basePackages = "com.aidsp.platform")
public class GlobalRestResponseAdvice implements ResponseBodyAdvice<Object> {

    private static final Set<String> EXCLUDE_PATH_PREFIXES = Set.of(
            "/v3/api-docs",
            "/v3/api-docs/swagger-config",
            "/swagger-ui",
            "/swagger-resources",
            "/doc.html",
            "/webjars"
    );

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        Class<?> declaringClass = returnType.getDeclaringClass();
        if (declaringClass != null && declaringClass.getName().startsWith("org.springframework")) {
            return false;
        }
        // 排除 ResponseEntity
        if (ResponseEntity.class.isAssignableFrom(returnType.getParameterType())) {
            return false;
        }
        // 排除 String
        if (String.class.equals(returnType.getParameterType())) {
            return false;
        }
        // 按请求路径排除 Knife4j / Swagger
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            String path = attrs.getRequest().getRequestURI();
            if (path != null) {
                for (String prefix : EXCLUDE_PATH_PREFIXES) {
                    if (path.startsWith(prefix)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        // 已是 RestResponse
        if (body instanceof RestResponse<?>) {
            return body;
        }
        return RestResponse.success(body);
    }
}
