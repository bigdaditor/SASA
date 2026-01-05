package io.github.bigdaditor.sasa.extractor;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

/**
 * DTO 타입 스키마와 Validation annotations 추출
 */
public class TypeSchemaExtractor {

    /**
     * 타입의 상세 스키마 추출 (DTO 필드 정보)
     */
    public static Map<String, Object> extractTypeSchema(Class<?> type) {
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
            // JSON 예시 생성
            schema.put("example", generateJsonExample(fields));
        }

        return schema;
    }

    /**
     * 필드 정보로부터 JSON 예시 생성
     */
    private static Map<String, Object> generateJsonExample(List<Map<String, Object>> fields) {
        Map<String, Object> example = new LinkedHashMap<>();

        for (Map<String, Object> field : fields) {
            String fieldName = (String) field.get("name");
            String fieldType = (String) field.get("type");

            @SuppressWarnings("unchecked")
            List<String> genericTypes = (List<String>) field.get("genericTypes");

            // 타입에 따른 예시 값 생성
            Object exampleValue = generateExampleValue(fieldType, genericTypes);
            example.put(fieldName, exampleValue);
        }

        return example;
    }

    /**
     * 타입에 따른 예시 값 생성
     */
    private static Object generateExampleValue(String type, List<String> genericTypes) {
        switch (type) {
            case "String":
                return "string";
            case "Integer":
            case "int":
                return 0;
            case "Long":
            case "long":
                return 0L;
            case "Double":
            case "double":
                return 0.0;
            case "Float":
            case "float":
                return 0.0f;
            case "Boolean":
            case "boolean":
                return false;
            case "List":
            case "ArrayList":
            case "LinkedList":
                if (genericTypes != null && !genericTypes.isEmpty()) {
                    return List.of(generateExampleValue(genericTypes.get(0), null));
                }
                return List.of();
            case "Set":
            case "HashSet":
            case "LinkedHashSet":
                if (genericTypes != null && !genericTypes.isEmpty()) {
                    return Set.of(generateExampleValue(genericTypes.get(0), null));
                }
                return Set.of();
            case "Map":
            case "HashMap":
            case "LinkedHashMap":
                return Map.of();
            case "LocalDateTime":
                return "2024-01-01T00:00:00";
            case "LocalDate":
                return "2024-01-01";
            case "LocalTime":
                return "00:00:00";
            case "Date":
                return "2024-01-01T00:00:00Z";
            default:
                return type.toLowerCase(); // DTO나 기타 타입은 타입명을 소문자로
        }
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
    public static boolean isSimpleType(Class<?> type) {
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
}