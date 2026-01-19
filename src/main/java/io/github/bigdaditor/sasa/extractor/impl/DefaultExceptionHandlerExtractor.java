package io.github.bigdaditor.sasa.extractor.impl;

import io.github.bigdaditor.sasa.extractor.api.ExceptionHandlerExtractor;
import io.github.bigdaditor.sasa.extractor.api.ResponseExtractor;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Exception Handler 정보 추출 기본 구현
 */
public class DefaultExceptionHandlerExtractor implements ExceptionHandlerExtractor {

    private final ResponseExtractor responseExtractor;

    public DefaultExceptionHandlerExtractor() {
        this.responseExtractor = new DefaultResponseExtractor();
    }

    public DefaultExceptionHandlerExtractor(ResponseExtractor responseExtractor) {
        this.responseExtractor = responseExtractor;
    }

    @Override
    public List<Map<String, Object>> extract(ApplicationContext applicationContext) {
        List<Map<String, Object>> handlers = new ArrayList<>();

        Map<String, Object> adviceBeans = applicationContext.getBeansWithAnnotation(ControllerAdvice.class);
        adviceBeans.putAll(applicationContext.getBeansWithAnnotation(RestControllerAdvice.class));

        for (Map.Entry<String, Object> entry : adviceBeans.entrySet()) {
            Object adviceBean = entry.getValue();
            Class<?> adviceClass = adviceBean.getClass();

            // 프록시 클래스인 경우 실제 클래스 찾기
            if (adviceClass.getName().contains("$$")) {
                adviceClass = adviceClass.getSuperclass();
            }

            for (Method method : adviceClass.getDeclaredMethods()) {
                ExceptionHandler exceptionHandler = AnnotatedElementUtils.findMergedAnnotation(method, ExceptionHandler.class);

                if (exceptionHandler != null) {
                    Map<String, Object> handlerInfo = new LinkedHashMap<>();

                    Class<? extends Throwable>[] exceptionTypes = exceptionHandler.value();
                    if (exceptionTypes.length == 0) {
                        exceptionTypes = extractExceptionTypesFromMethodParams(method);
                    }

                    List<String> exceptionTypeNames = Arrays.stream(exceptionTypes)
                            .map(Class::getSimpleName)
                            .collect(Collectors.toList());

                    handlerInfo.put("exceptionTypes", exceptionTypeNames);

                    Map<String, Object> handler = new LinkedHashMap<>();
                    handler.put("controller", adviceClass.getSimpleName());
                    handler.put("method", method.getName());
                    handler.put("fullControllerName", adviceClass.getName());
                    handlerInfo.put("handler", handler);

                    HttpStatus httpStatus = extractHttpStatus(method, exceptionTypes);
                    if (httpStatus != null) {
                        Map<String, Object> statusInfo = new LinkedHashMap<>();
                        statusInfo.put("code", httpStatus.value());
                        statusInfo.put("reasonPhrase", httpStatus.getReasonPhrase());
                        handlerInfo.put("httpStatus", statusInfo);
                    }

                    Map<String, Object> responseInfo = responseExtractor.extractSimpleResponseInfo(method.getReturnType());
                    handlerInfo.put("response", responseInfo);

                    boolean isRestControllerAdvice = AnnotatedElementUtils.hasAnnotation(adviceClass, RestControllerAdvice.class);
                    handlerInfo.put("adviceType", isRestControllerAdvice ? "RestControllerAdvice" : "ControllerAdvice");

                    handlers.add(handlerInfo);
                }
            }
        }

        return handlers;
    }

    @SuppressWarnings("unchecked")
    private Class<? extends Throwable>[] extractExceptionTypesFromMethodParams(Method method) {
        List<Class<? extends Throwable>> exceptionTypes = new ArrayList<>();

        for (Class<?> paramType : method.getParameterTypes()) {
            if (Throwable.class.isAssignableFrom(paramType)) {
                exceptionTypes.add((Class<? extends Throwable>) paramType);
            }
        }

        return exceptionTypes.toArray(new Class[0]);
    }

    private HttpStatus extractHttpStatus(Method method, Class<? extends Throwable>[] exceptionTypes) {
        ResponseStatus responseStatus = AnnotatedElementUtils.findMergedAnnotation(method, ResponseStatus.class);
        if (responseStatus != null) {
            return responseStatus.value();
        }

        if (exceptionTypes.length > 0) {
            Class<? extends Throwable> primaryException = exceptionTypes[0];
            return inferHttpStatusFromExceptionType(primaryException);
        }

        return null;
    }

    private HttpStatus inferHttpStatusFromExceptionType(Class<? extends Throwable> exceptionType) {
        String exceptionName = exceptionType.getSimpleName();

        if (exceptionName.contains("NotFound") || exceptionName.contains("NoSuchElement")) {
            return HttpStatus.NOT_FOUND;
        } else if (exceptionName.contains("IllegalArgument") ||
                   exceptionName.contains("Validation") ||
                   exceptionName.contains("MethodArgumentNotValid") ||
                   exceptionName.contains("ConstraintViolation")) {
            return HttpStatus.BAD_REQUEST;
        } else if (exceptionName.contains("Unauthorized") ||
                   exceptionName.contains("Authentication")) {
            return HttpStatus.UNAUTHORIZED;
        } else if (exceptionName.contains("Forbidden") ||
                   exceptionName.contains("AccessDenied")) {
            return HttpStatus.FORBIDDEN;
        } else if (exceptionName.contains("Conflict") ||
                   exceptionName.contains("Duplicate")) {
            return HttpStatus.CONFLICT;
        } else if (exceptionName.contains("UnsupportedOperation")) {
            return HttpStatus.NOT_IMPLEMENTED;
        } else if (exceptionName.equals("NullPointerException") ||
                   exceptionName.equals("RuntimeException") ||
                   exceptionName.equals("Exception")) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
