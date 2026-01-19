package io.github.bigdaditor.sasa.extractor.impl;

import io.github.bigdaditor.sasa.extractor.api.TypeSchemaExtractor;
import io.github.bigdaditor.sasa.extractor.api.ValidationExtractor;

import java.lang.reflect.*;
import java.util.*;

/**
 * 타입 스키마 추출 기본 구현
 */
public class DefaultTypeSchemaExtractor implements TypeSchemaExtractor {

    private final ValidationExtractor validationExtractor;

    public DefaultTypeSchemaExtractor() {
        this.validationExtractor = new DefaultValidationExtractor();
    }

    public DefaultTypeSchemaExtractor(ValidationExtractor validationExtractor) {
        this.validationExtractor = validationExtractor;
    }

    @Override
    public Map<String, Object> extractTypeSchema(Class<?> type) {
        Map<String, Object> schema = new LinkedHashMap<>();

        if (isSimpleType(type)) {
            return schema;
        }

        List<Map<String, Object>> fields = new ArrayList<>();
        for (Field field : type.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                continue;
            }

            Map<String, Object> fieldInfo = new LinkedHashMap<>();
            fieldInfo.put("name", field.getName());
            fieldInfo.put("type", field.getType().getSimpleName());
            fieldInfo.put("fullType", field.getType().getName());

            // 제네릭 타입 정보
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
            Map<String, Object> validations = validationExtractor.extract(field);
            if (!validations.isEmpty()) {
                fieldInfo.put("validations", validations);
            }

            fields.add(fieldInfo);
        }

        if (!fields.isEmpty()) {
            schema.put("fields", fields);
            schema.put("example", generateJsonExample(fields));
        }

        return schema;
    }

    @Override
    public boolean isSimpleType(Class<?> type) {
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

    private Map<String, Object> generateJsonExample(List<Map<String, Object>> fields) {
        Map<String, Object> example = new LinkedHashMap<>();

        for (Map<String, Object> field : fields) {
            String fieldName = (String) field.get("name");
            String fieldType = (String) field.get("type");

            @SuppressWarnings("unchecked")
            List<String> genericTypes = (List<String>) field.get("genericTypes");

            Object exampleValue = generateExampleValue(fieldType, genericTypes);
            example.put(fieldName, exampleValue);
        }

        return example;
    }

    private Object generateExampleValue(String type, List<String> genericTypes) {
        return switch (type) {
            case "String" -> "string";
            case "Integer", "int" -> 0;
            case "Long", "long" -> 0L;
            case "Double", "double" -> 0.0;
            case "Float", "float" -> 0.0f;
            case "Boolean", "boolean" -> false;
            case "List", "ArrayList", "LinkedList" -> {
                if (genericTypes != null && !genericTypes.isEmpty()) {
                    yield List.of(generateExampleValue(genericTypes.get(0), null));
                }
                yield List.of();
            }
            case "Set", "HashSet", "LinkedHashSet" -> {
                if (genericTypes != null && !genericTypes.isEmpty()) {
                    yield Set.of(generateExampleValue(genericTypes.get(0), null));
                }
                yield Set.of();
            }
            case "Map", "HashMap", "LinkedHashMap" -> Map.of();
            case "LocalDateTime" -> "2024-01-01T00:00:00";
            case "LocalDate" -> "2024-01-01";
            case "LocalTime" -> "00:00:00";
            case "Date" -> "2024-01-01T00:00:00Z";
            default -> type.toLowerCase();
        };
    }
}
