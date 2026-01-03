package com.example.sasa.extractor;

import java.lang.reflect.*;
import java.util.*;

/**
 * Response 타입 정보 추출 (제네릭 타입 포함)
 */
public class ResponseExtractor {

    /**
     * 제네릭 타입을 포함한 Response 정보 추출
     */
    public static Map<String, Object> extractResponseInfo(Method method) {
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
                    Map<String, Object> schema = TypeSchemaExtractor.extractTypeSchema(actualClass);
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
                        Map<String, Object> schema = TypeSchemaExtractor.extractTypeSchema(elementType);
                        responseInfo.put("schema", schema);
                    }
                }
            }
        } else {
            // 제네릭이 아닌 일반 타입
            Map<String, Object> schema = TypeSchemaExtractor.extractTypeSchema(returnType);
            if (!schema.isEmpty()) {
                responseInfo.put("schema", schema);
            }
        }

        return responseInfo;
    }

    /**
     * 단순 Response 타입 정보 추출 (제네릭 없이)
     */
    public static Map<String, Object> extractSimpleResponseInfo(Class<?> returnType) {
        Map<String, Object> responseInfo = new LinkedHashMap<>();

        // 기본 타입 정보
        responseInfo.put("type", returnType.getSimpleName());
        responseInfo.put("fullType", returnType.getName());

        // 필드 정보 추출 (primitive, wrapper, String, Collection 등은 제외)
        if (!TypeSchemaExtractor.isSimpleType(returnType)) {
            Map<String, Object> schema = TypeSchemaExtractor.extractTypeSchema(returnType);
            if (!schema.isEmpty()) {
                responseInfo.put("schema", schema);
            }
        }

        return responseInfo;
    }
}