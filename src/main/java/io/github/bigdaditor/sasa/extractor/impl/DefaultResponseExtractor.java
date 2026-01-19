package io.github.bigdaditor.sasa.extractor.impl;

import io.github.bigdaditor.sasa.extractor.api.ResponseExtractor;
import io.github.bigdaditor.sasa.extractor.api.TypeSchemaExtractor;

import java.lang.reflect.*;
import java.util.*;

/**
 * 응답 정보 추출 기본 구현
 */
public class DefaultResponseExtractor implements ResponseExtractor {

    private final TypeSchemaExtractor typeSchemaExtractor;

    public DefaultResponseExtractor() {
        this.typeSchemaExtractor = new DefaultTypeSchemaExtractor();
    }

    public DefaultResponseExtractor(TypeSchemaExtractor typeSchemaExtractor) {
        this.typeSchemaExtractor = typeSchemaExtractor;
    }

    @Override
    public Map<String, Object> extractResponseInfo(Method method) {
        Map<String, Object> responseInfo = new LinkedHashMap<>();

        Type genericReturnType = method.getGenericReturnType();
        Class<?> returnType = method.getReturnType();

        responseInfo.put("type", returnType.getSimpleName());
        responseInfo.put("fullType", returnType.getName());

        if (genericReturnType instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) genericReturnType;
            Type[] typeArguments = paramType.getActualTypeArguments();

            if (typeArguments.length > 0) {
                Type actualType = typeArguments[0];

                if (actualType instanceof Class) {
                    Class<?> actualClass = (Class<?>) actualType;
                    responseInfo.put("genericType", actualClass.getSimpleName());
                    responseInfo.put("genericFullType", actualClass.getName());

                    Map<String, Object> schema = typeSchemaExtractor.extractTypeSchema(actualClass);
                    responseInfo.put("schema", schema);
                } else if (actualType instanceof ParameterizedType) {
                    ParameterizedType nestedParamType = (ParameterizedType) actualType;
                    Class<?> rawType = (Class<?>) nestedParamType.getRawType();
                    responseInfo.put("genericType", rawType.getSimpleName());

                    if (nestedParamType.getActualTypeArguments().length > 0 &&
                        nestedParamType.getActualTypeArguments()[0] instanceof Class) {
                        Class<?> elementType = (Class<?>) nestedParamType.getActualTypeArguments()[0];
                        responseInfo.put("elementType", elementType.getSimpleName());
                        responseInfo.put("elementFullType", elementType.getName());

                        Map<String, Object> schema = typeSchemaExtractor.extractTypeSchema(elementType);
                        responseInfo.put("schema", schema);
                    }
                }
            }
        } else {
            Map<String, Object> schema = typeSchemaExtractor.extractTypeSchema(returnType);
            if (!schema.isEmpty()) {
                responseInfo.put("schema", schema);
            }
        }

        return responseInfo;
    }

    @Override
    public Map<String, Object> extractSimpleResponseInfo(Class<?> returnType) {
        Map<String, Object> responseInfo = new LinkedHashMap<>();

        responseInfo.put("type", returnType.getSimpleName());
        responseInfo.put("fullType", returnType.getName());

        if (!typeSchemaExtractor.isSimpleType(returnType)) {
            Map<String, Object> schema = typeSchemaExtractor.extractTypeSchema(returnType);
            if (!schema.isEmpty()) {
                responseInfo.put("schema", schema);
            }
        }

        return responseInfo;
    }
}
