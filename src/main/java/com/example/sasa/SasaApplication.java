package com.example.sasa;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * SASA: Spring API Spec Analyzer
 *
 * Spring MVC 런타임에 등록된 엔드포인트를 인트로스펙션하여 API 스펙을 JSON으로 추출하는 라이브러리
 */
public class SasaApplication {

    /**
     * 기본 설정으로 API 스펙을 추출하여 콘솔에 출력하고 파일로 저장
     *
     * @param mapping Spring MVC RequestMappingHandlerMapping
     */
    public static void generateApiSpec(RequestMappingHandlerMapping mapping) {
        generateApiSpec(mapping, null, SasaConfig.builder().build());
    }

    /**
     * ApplicationContext와 함께 API 스펙을 추출 (exception handler 정보 포함)
     *
     * @param mapping Spring MVC RequestMappingHandlerMapping
     * @param applicationContext Spring ApplicationContext (optional, for exception handler extraction)
     */
    public static void generateApiSpec(RequestMappingHandlerMapping mapping, ApplicationContext applicationContext) {
        generateApiSpec(mapping, applicationContext, SasaConfig.builder().build());
    }

    /**
     * 커스텀 설정으로 API 스펙을 추출
     *
     * @param mapping Spring MVC RequestMappingHandlerMapping
     * @param config SASA 설정
     */
    public static void generateApiSpec(RequestMappingHandlerMapping mapping, SasaConfig config) {
        generateApiSpec(mapping, null, config);
    }

    /**
     * 커스텀 설정과 ApplicationContext로 API 스펙을 추출
     *
     * @param mapping Spring MVC RequestMappingHandlerMapping
     * @param applicationContext Spring ApplicationContext (optional, for exception handler extraction)
     * @param config SASA 설정
     */
    public static void generateApiSpec(RequestMappingHandlerMapping mapping, ApplicationContext applicationContext, SasaConfig config) {
        try {
            ObjectMapper mapper = createObjectMapper();
            Map<String, Object> apiSpec = extractApiSpec(mapping, applicationContext, config);

            String json = mapper.writeValueAsString(apiSpec);

            // 콘솔 출력
            if (config.isEnableConsoleOutput()) {
                System.out.println("\n=== SASA: API Specification ===");
                System.out.println(json);
                System.out.println("=== SASA: End ===\n");
            }

            // 파일 저장
            if (config.isEnableFileOutput()) {
                saveToFile(mapper, apiSpec, config.getOutputFilePath());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate API spec", e);
        }
    }

    public static Map<String, Object> extractApiSpec(RequestMappingHandlerMapping mapping, SasaConfig config) {
        return extractApiSpec(mapping, null, config);
    }

    public static Map<String, Object> extractApiSpec(RequestMappingHandlerMapping mapping, ApplicationContext applicationContext, SasaConfig config) {
        List<Map<String, Object>> endpoints = new ArrayList<>();

        for (var entry : mapping.getHandlerMethods().entrySet()) {
            RequestMappingInfo info = entry.getKey();
            HandlerMethod hm = entry.getValue();

            // Paths
            Set<String> paths = info.getPathPatternsCondition() != null
                    ? info.getPathPatternsCondition().getPatternValues()
                    : Set.of();

            // Methods
            Set<String> methods = info.getMethodsCondition().getMethods().stream()
                    .map(Enum::name)
                    .collect(Collectors.toSet());
            if (methods.isEmpty()) {
                methods = Set.of("ANY");
            }

            // 필터링 적용
            boolean shouldInclude = false;
            for (String path : paths) {
                if (config.shouldIncludePath(path)) {
                    for (String method : methods) {
                        if (config.shouldIncludeMethod(method)) {
                            shouldInclude = true;
                            break;
                        }
                    }
                }
                if (shouldInclude) break;
            }

            // 커스텀 필터 적용
            if (shouldInclude && config.getCustomEndpointFilter() != null) {
                String firstPath = paths.isEmpty() ? "" : paths.iterator().next();
                shouldInclude = config.getCustomEndpointFilter().test(firstPath);
            }

            if (!shouldInclude) {
                continue; // 필터 조건을 통과하지 못하면 스킵
            }

            Map<String, Object> endpoint = new LinkedHashMap<>();
            endpoint.put("paths", paths);
            endpoint.put("methods", methods);

            // Consumes
            Set<String> consumes = info.getConsumesCondition().getConsumableMediaTypes().stream()
                    .map(MediaType::toString)
                    .collect(Collectors.toSet());
            endpoint.put("consumes", consumes);

            // Produces
            Set<String> produces = info.getProducesCondition().getProducibleMediaTypes().stream()
                    .map(MediaType::toString)
                    .collect(Collectors.toSet());
            endpoint.put("produces", produces);

            // Handler
            endpoint.put("handler", hm.getBeanType().getSimpleName() + "#" + hm.getMethod().getName());
            endpoint.put("beanType", hm.getBeanType().getSimpleName());
            endpoint.put("methodName", hm.getMethod().getName());

            // Request 파라미터 정보 추출
            List<Map<String, Object>> parameters = extractParameters(hm.getMethod());
            endpoint.put("parameters", parameters);

            // Response 정보 추가 (제네릭 타입 포함)
            Map<String, Object> responseInfo = extractResponseInfoWithGenerics(hm.getMethod());
            endpoint.put("response", responseInfo);

            endpoints.add(endpoint);
        }

        // Exception handlers 추출
        List<Map<String, Object>> exceptionHandlers = new ArrayList<>();
        if (applicationContext != null) {
            exceptionHandlers = extractExceptionHandlers(applicationContext);
        }

        Map<String, Object> spec = new LinkedHashMap<>();
        spec.put("applicationName", config.getApplicationName());
        spec.put("version", "0.0.1-SNAPSHOT");
        spec.put("generatedAt", LocalDateTime.now().toString());
        spec.put("endpoints", endpoints);
        spec.put("exceptionHandlers", exceptionHandlers);

        return spec;
    }

    /**
     * ApplicationContext에서 모든 @ExceptionHandler를 추출
     */
    private static List<Map<String, Object>> extractExceptionHandlers(ApplicationContext applicationContext) {
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
                    Map<String, Object> responseInfo = extractResponseInfo(method.getReturnType());
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
     * Response 타입 정보 추출
     */
    private static Map<String, Object> extractResponseInfo(Class<?> returnType) {
        Map<String, Object> responseInfo = new LinkedHashMap<>();

        // 기본 타입 정보
        responseInfo.put("type", returnType.getSimpleName());
        responseInfo.put("fullType", returnType.getName());

        // ResponseEntity 등의 제네릭 타입 처리는 런타임에 어려우므로 기본 정보만 제공
        // 추후 필요시 메서드의 Generic Return Type을 분석할 수 있음

        // 필드 정보 추출 (primitive, wrapper, String, Collection 등은 제외)
        if (!isSimpleType(returnType)) {
            List<Map<String, String>> fields = new ArrayList<>();
            for (Field field : returnType.getDeclaredFields()) {
                // static, synthetic 필드 제외
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                    continue;
                }

                Map<String, String> fieldInfo = new LinkedHashMap<>();
                fieldInfo.put("name", field.getName());
                fieldInfo.put("type", field.getType().getSimpleName());
                fieldInfo.put("fullType", field.getType().getName());
                fields.add(fieldInfo);
            }
            responseInfo.put("fields", fields);
        }

        return responseInfo;
    }

    /**
     * 메서드 파라미터 정보 추출 (Request body, params 등)
     */
    private static List<Map<String, Object>> extractParameters(Method method) {
        List<Map<String, Object>> parameters = new ArrayList<>();
        Parameter[] methodParams = method.getParameters();

        for (Parameter param : methodParams) {
            Map<String, Object> paramInfo = new LinkedHashMap<>();

            // 파라미터 이름
            paramInfo.put("name", param.getName());
            paramInfo.put("type", param.getType().getSimpleName());
            paramInfo.put("fullType", param.getType().getName());

            // 어노테이션 확인
            RequestBody requestBody = param.getAnnotation(RequestBody.class);
            RequestParam requestParam = param.getAnnotation(RequestParam.class);
            PathVariable pathVariable = param.getAnnotation(PathVariable.class);
            RequestHeader requestHeader = param.getAnnotation(RequestHeader.class);

            if (requestBody != null) {
                paramInfo.put("parameterType", "REQUEST_BODY");
                paramInfo.put("required", requestBody.required());
                // RequestBody의 DTO 상세 정보 추출
                Map<String, Object> bodySchema = extractTypeSchema(param.getType());
                paramInfo.put("schema", bodySchema);
            } else if (requestParam != null) {
                paramInfo.put("parameterType", "REQUEST_PARAM");
                paramInfo.put("paramName", requestParam.value().isEmpty() ? requestParam.name() : requestParam.value());
                paramInfo.put("required", requestParam.required());
                paramInfo.put("defaultValue", requestParam.defaultValue());
            } else if (pathVariable != null) {
                paramInfo.put("parameterType", "PATH_VARIABLE");
                paramInfo.put("paramName", pathVariable.value().isEmpty() ? pathVariable.name() : pathVariable.value());
                paramInfo.put("required", pathVariable.required());
            } else if (requestHeader != null) {
                paramInfo.put("parameterType", "REQUEST_HEADER");
                paramInfo.put("paramName", requestHeader.value().isEmpty() ? requestHeader.name() : requestHeader.value());
                paramInfo.put("required", requestHeader.required());
                paramInfo.put("defaultValue", requestHeader.defaultValue());
            } else {
                // 어노테이션이 없는 경우 (ex: HttpServletRequest, HttpSession 등)
                paramInfo.put("parameterType", "OTHER");
            }

            parameters.add(paramInfo);
        }

        return parameters;
    }

    /**
     * 제네릭 타입을 포함한 Response 정보 추출
     */
    private static Map<String, Object> extractResponseInfoWithGenerics(Method method) {
        Map<String, Object> responseInfo = new LinkedHashMap<>();

        Type genericReturnType = method.getGenericReturnType();
        Class<?> returnType = method.getReturnType();

        responseInfo.put("type", returnType.getSimpleName());
        responseInfo.put("fullType", returnType.getName());

        // ResponseEntity<T> 같은 제네릭 타입 처리
        if (genericReturnType instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) genericReturnType;
            Type[] typeArguments = paramType.getActualTypeArguments();

            if (typeArguments.length > 0) {
                Type actualType = typeArguments[0];

                if (actualType instanceof Class) {
                    Class<?> actualClass = (Class<?>) actualType;
                    responseInfo.put("genericType", actualClass.getSimpleName());
                    responseInfo.put("genericFullType", actualClass.getName());

                    // DTO 상세 스키마 추출
                    Map<String, Object> schema = extractTypeSchema(actualClass);
                    responseInfo.put("schema", schema);
                } else if (actualType instanceof ParameterizedType) {
                    // List<UserDTO> 같은 중첩 제네릭
                    ParameterizedType nestedParamType = (ParameterizedType) actualType;
                    Class<?> rawType = (Class<?>) nestedParamType.getRawType();
                    responseInfo.put("genericType", rawType.getSimpleName());

                    if (nestedParamType.getActualTypeArguments().length > 0 &&
                        nestedParamType.getActualTypeArguments()[0] instanceof Class) {
                        Class<?> elementType = (Class<?>) nestedParamType.getActualTypeArguments()[0];
                        responseInfo.put("elementType", elementType.getSimpleName());
                        responseInfo.put("elementFullType", elementType.getName());

                        // 컬렉션 요소의 스키마 추출
                        Map<String, Object> schema = extractTypeSchema(elementType);
                        responseInfo.put("schema", schema);
                    }
                }
            }
        } else {
            // 제네릭이 아닌 일반 타입
            Map<String, Object> schema = extractTypeSchema(returnType);
            if (!schema.isEmpty()) {
                responseInfo.put("schema", schema);
            }
        }

        return responseInfo;
    }

    /**
     * 타입의 상세 스키마 추출 (DTO 필드 정보)
     */
    private static Map<String, Object> extractTypeSchema(Class<?> type) {
        Map<String, Object> schema = new LinkedHashMap<>();

        // 단순 타입은 스키마 불필요
        if (isSimpleType(type)) {
            return schema;
        }

        List<Map<String, Object>> fields = new ArrayList<>();
        for (Field field : type.getDeclaredFields()) {
            // static, synthetic 필드 제외
            if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                continue;
            }

            Map<String, Object> fieldInfo = new LinkedHashMap<>();
            fieldInfo.put("name", field.getName());
            fieldInfo.put("type", field.getType().getSimpleName());
            fieldInfo.put("fullType", field.getType().getName());

            // 제네릭 타입 정보 (List<String>, Map<String, Integer> 등)
            Type genericType = field.getGenericType();
            if (genericType instanceof ParameterizedType) {
                ParameterizedType paramType = (ParameterizedType) genericType;
                Type[] typeArgs = paramType.getActualTypeArguments();
                List<String> genericTypes = new ArrayList<>();
                for (Type typeArg : typeArgs) {
                    if (typeArg instanceof Class) {
                        genericTypes.add(((Class<?>) typeArg).getSimpleName());
                    }
                }
                if (!genericTypes.isEmpty()) {
                    fieldInfo.put("genericTypes", genericTypes);
                }
            }

            // Validation annotations 추출
            Map<String, Object> validations = extractValidationAnnotations(field);
            if (!validations.isEmpty()) {
                fieldInfo.put("validations", validations);
            }

            fields.add(fieldInfo);
        }

        if (!fields.isEmpty()) {
            schema.put("fields", fields);
        }

        return schema;
    }

    /**
     * 필드의 validation annotations 추출
     */
    private static Map<String, Object> extractValidationAnnotations(Field field) {
        Map<String, Object> validations = new LinkedHashMap<>();
        Annotation[] annotations = field.getAnnotations();

        for (Annotation annotation : annotations) {
            String annotationType = annotation.annotationType().getSimpleName();

            try {
                switch (annotationType) {
                    case "NotNull":
                        validations.put("notNull", true);
                        extractMessage(annotation, validations);
                        break;

                    case "NotEmpty":
                        validations.put("notEmpty", true);
                        extractMessage(annotation, validations);
                        break;

                    case "NotBlank":
                        validations.put("notBlank", true);
                        extractMessage(annotation, validations);
                        break;

                    case "Size":
                        Map<String, Object> size = new LinkedHashMap<>();
                        size.put("min", getAnnotationValue(annotation, "min", 0));
                        size.put("max", getAnnotationValue(annotation, "max", Integer.MAX_VALUE));
                        validations.put("size", size);
                        extractMessage(annotation, validations);
                        break;

                    case "Min":
                        validations.put("min", getAnnotationValue(annotation, "value", 0L));
                        extractMessage(annotation, validations);
                        break;

                    case "Max":
                        validations.put("max", getAnnotationValue(annotation, "value", Long.MAX_VALUE));
                        extractMessage(annotation, validations);
                        break;

                    case "Email":
                        validations.put("email", true);
                        extractMessage(annotation, validations);
                        break;

                    case "Pattern":
                        String regex = (String) getAnnotationValue(annotation, "regexp", "");
                        if (!regex.isEmpty()) {
                            validations.put("pattern", regex);
                        }
                        extractMessage(annotation, validations);
                        break;

                    case "Positive":
                        validations.put("positive", true);
                        extractMessage(annotation, validations);
                        break;

                    case "PositiveOrZero":
                        validations.put("positiveOrZero", true);
                        extractMessage(annotation, validations);
                        break;

                    case "Negative":
                        validations.put("negative", true);
                        extractMessage(annotation, validations);
                        break;

                    case "NegativeOrZero":
                        validations.put("negativeOrZero", true);
                        extractMessage(annotation, validations);
                        break;

                    case "Past":
                        validations.put("past", true);
                        extractMessage(annotation, validations);
                        break;

                    case "PastOrPresent":
                        validations.put("pastOrPresent", true);
                        extractMessage(annotation, validations);
                        break;

                    case "Future":
                        validations.put("future", true);
                        extractMessage(annotation, validations);
                        break;

                    case "FutureOrPresent":
                        validations.put("futureOrPresent", true);
                        extractMessage(annotation, validations);
                        break;

                    case "DecimalMin":
                        validations.put("decimalMin", getAnnotationValue(annotation, "value", "0"));
                        validations.put("decimalMinInclusive", getAnnotationValue(annotation, "inclusive", true));
                        extractMessage(annotation, validations);
                        break;

                    case "DecimalMax":
                        validations.put("decimalMax", getAnnotationValue(annotation, "value", "0"));
                        validations.put("decimalMaxInclusive", getAnnotationValue(annotation, "inclusive", true));
                        extractMessage(annotation, validations);
                        break;

                    case "Digits":
                        Map<String, Object> digits = new LinkedHashMap<>();
                        digits.put("integer", getAnnotationValue(annotation, "integer", 0));
                        digits.put("fraction", getAnnotationValue(annotation, "fraction", 0));
                        validations.put("digits", digits);
                        extractMessage(annotation, validations);
                        break;
                }
            } catch (Exception e) {
                // Validation annotation 추출 실패 시 무시
            }
        }

        return validations;
    }

    /**
     * 어노테이션에서 message 속성 추출
     */
    private static void extractMessage(Annotation annotation, Map<String, Object> validations) {
        try {
            String message = (String) getAnnotationValue(annotation, "message", "");
            if (!message.isEmpty() && !message.startsWith("{")) {
                validations.put("message", message);
            }
        } catch (Exception e) {
            // message 추출 실패 시 무시
        }
    }

    /**
     * 어노테이션 속성 값 가져오기
     */
    private static Object getAnnotationValue(Annotation annotation, String attribute, Object defaultValue) {
        try {
            Method method = annotation.annotationType().getDeclaredMethod(attribute);
            Object value = method.invoke(annotation);
            return value != null ? value : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * 단순 타입인지 확인 (primitive, wrapper, String, Collection 등)
     */
    private static boolean isSimpleType(Class<?> type) {
        return type.isPrimitive() ||
                type.equals(String.class) ||
                type.equals(Integer.class) ||
                type.equals(Long.class) ||
                type.equals(Double.class) ||
                type.equals(Float.class) ||
                type.equals(Boolean.class) ||
                type.equals(Character.class) ||
                type.equals(Byte.class) ||
                type.equals(Short.class) ||
                type.equals(Void.class) ||
                type.getName().startsWith("java.util.") ||
                type.getName().startsWith("java.lang.") ||
                type.getName().startsWith("org.springframework.");
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    private static void saveToFile(ObjectMapper mapper, Map<String, Object> spec, String filePath) throws Exception {
        Path path = Paths.get(filePath);
        Path parent = path.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
        mapper.writeValue(new File(filePath), spec);
        System.out.println("API Spec saved to: " + filePath);
    }
}
