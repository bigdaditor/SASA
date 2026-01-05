package io.github.bigdaditor.sasa.extractor;

import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * Request 파라미터 정보 추출 (RequestBody, RequestParam, PathVariable, RequestHeader)
 */
public class ParameterExtractor {

    /**
     * 메서드 파라미터 정보 추출 (Request body, params 등)
     */
    public static List<Map<String, Object>> extractParameters(Method method) {
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
                Map<String, Object> bodySchema = TypeSchemaExtractor.extractTypeSchema(param.getType());
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
}