package io.github.bigdaditor.sasa.extractor.impl;

import io.github.bigdaditor.sasa.extractor.api.ParameterExtractor;
import io.github.bigdaditor.sasa.extractor.api.TypeSchemaExtractor;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * 메서드 파라미터 정보 추출 기본 구현
 */
public class DefaultParameterExtractor implements ParameterExtractor {

    private final TypeSchemaExtractor typeSchemaExtractor;

    public DefaultParameterExtractor() {
        this.typeSchemaExtractor = new DefaultTypeSchemaExtractor();
    }

    public DefaultParameterExtractor(TypeSchemaExtractor typeSchemaExtractor) {
        this.typeSchemaExtractor = typeSchemaExtractor;
    }

    @Override
    public List<Map<String, Object>> extract(Method method) {
        List<Map<String, Object>> parameters = new ArrayList<>();
        Parameter[] methodParams = method.getParameters();

        for (Parameter param : methodParams) {
            Map<String, Object> paramInfo = new LinkedHashMap<>();

            paramInfo.put("name", param.getName());
            paramInfo.put("type", param.getType().getSimpleName());
            paramInfo.put("fullType", param.getType().getName());

            RequestBody requestBody = param.getAnnotation(RequestBody.class);
            RequestParam requestParam = param.getAnnotation(RequestParam.class);
            PathVariable pathVariable = param.getAnnotation(PathVariable.class);
            RequestHeader requestHeader = param.getAnnotation(RequestHeader.class);

            if (requestBody != null) {
                paramInfo.put("parameterType", "REQUEST_BODY");
                paramInfo.put("required", requestBody.required());
                Map<String, Object> bodySchema = typeSchemaExtractor.extractTypeSchema(param.getType());
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
                paramInfo.put("parameterType", "OTHER");
            }

            parameters.add(paramInfo);
        }

        return parameters;
    }
}
