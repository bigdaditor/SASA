package io.github.bigdaditor.sasa.extractor;

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
 * Exception Handler 정보 추출 (@ControllerAdvice, @ExceptionHandler)
 */
public class ExceptionHandlerExtractor {

    /**
     * ApplicationContext에서 모든 @ExceptionHandler를 추출
     */
    public static List<Map<String, Object>> extractExceptionHandlers(ApplicationContext applicationContext) {
        List<Map<String, Object>> handlers = new ArrayList<>();

        // @ControllerAdvice와 @RestControllerAdvice 빈 찾기
        Map<String, Object> adviceBeans = applicationContext.getBeansWithAnnotation(ControllerAdvice.class);
        adviceBeans.putAll(applicationContext.getBeansWithAnnotation(RestControllerAdvice.class));

        for (Map.Entry<String, Object> entry : adviceBeans.entrySet()) {
            String beanName = entry.getKey();
            Object adviceBean = entry.getValue();
            Class<?> adviceClass = adviceBean.getClass();

            // 프록시 클래스인 경우 실제 클래스 찾기
            if (adviceClass.getName().contains("$$")) {
                adviceClass = adviceClass.getSuperclass();
            }

            // 모든 메서드 검사
            for (Method method : adviceClass.getDeclaredMethods()) {
                ExceptionHandler exceptionHandler = AnnotatedElementUtils.findMergedAnnotation(method, ExceptionHandler.class);

                if (exceptionHandler != null) {
                    Map<String, Object> handlerInfo = new LinkedHashMap<>();

                    // 처리하는 예외 타입들
                    Class<? extends Throwable>[] exceptionTypes = exceptionHandler.value();
                    if (exceptionTypes.length == 0) {
                        // value가 없으면 메서드 파라미터에서 추출
                        exceptionTypes = extractExceptionTypesFromMethodParams(method);
                    }

                    List<String> exceptionTypeNames = Arrays.stream(exceptionTypes)
                            .map(Class::getSimpleName)
                            .collect(Collectors.toList());

                    handlerInfo.put("exceptionTypes", exceptionTypeNames);

                    // Handler (구조화된 정보)
                    Map<String, Object> handler = new LinkedHashMap<>();
                    handler.put("controller", adviceClass.getSimpleName());
                    handler.put("method", method.getName());
                    handler.put("fullControllerName", adviceClass.getName());
                    handlerInfo.put("handler", handler);

                    // HTTP Status 추출
                    HttpStatus httpStatus = extractHttpStatus(method, exceptionTypes);
                    if (httpStatus != null) {
                        Map<String, Object> statusInfo = new LinkedHashMap<>();
                        statusInfo.put("code", httpStatus.value());
                        statusInfo.put("reasonPhrase", httpStatus.getReasonPhrase());
                        handlerInfo.put("httpStatus", statusInfo);
                    }

                    // Response 정보 추가
                    Map<String, Object> responseInfo = ResponseExtractor.extractSimpleResponseInfo(method.getReturnType());
                    handlerInfo.put("response", responseInfo);

                    // ControllerAdvice인지 RestControllerAdvice인지
                    boolean isRestControllerAdvice = AnnotatedElementUtils.hasAnnotation(adviceClass, RestControllerAdvice.class);
                    handlerInfo.put("adviceType", isRestControllerAdvice ? "RestControllerAdvice" : "ControllerAdvice");

                    handlers.add(handlerInfo);
                }
            }
        }

        return handlers;
    }

    /**
     * 메서드 파라미터에서 예외 타입 추출
     */
    @SuppressWarnings("unchecked")
    private static Class<? extends Throwable>[] extractExceptionTypesFromMethodParams(Method method) {
        List<Class<? extends Throwable>> exceptionTypes = new ArrayList<>();

        for (Class<?> paramType : method.getParameterTypes()) {
            if (Throwable.class.isAssignableFrom(paramType)) {
                exceptionTypes.add((Class<? extends Throwable>) paramType);
            }
        }

        return exceptionTypes.toArray(new Class[0]);
    }

    /**
     * HTTP Status 추출 (@ResponseStatus annotation 또는 예외 타입 기반 추론)
     */
    private static HttpStatus extractHttpStatus(Method method, Class<? extends Throwable>[] exceptionTypes) {
        // 1. 메서드에 @ResponseStatus가 있는지 확인
        ResponseStatus responseStatus = AnnotatedElementUtils.findMergedAnnotation(method, ResponseStatus.class);
        if (responseStatus != null) {
            return responseStatus.value();
        }

        // 2. 예외 타입 기반으로 추론
        if (exceptionTypes.length > 0) {
            Class<? extends Throwable> primaryException = exceptionTypes[0];
            return inferHttpStatusFromExceptionType(primaryException);
        }

        // 3. 기본값 (알 수 없는 경우)
        return null;
    }

    /**
     * 예외 타입으로부터 HTTP Status 추론
     */
    private static HttpStatus inferHttpStatusFromExceptionType(Class<? extends Throwable> exceptionType) {
        String exceptionName = exceptionType.getSimpleName();

        // 일반적인 예외 타입 매핑
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

        // 기본값: INTERNAL_SERVER_ERROR
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}