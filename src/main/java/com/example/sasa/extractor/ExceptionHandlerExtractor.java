package com.example.sasa.extractor;

import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
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
                    handlerInfo.put("handler", adviceClass.getSimpleName() + "#" + method.getName());
                    handlerInfo.put("beanType", adviceClass.getSimpleName());
                    handlerInfo.put("methodName", method.getName());
                    handlerInfo.put("beanName", beanName);

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
}