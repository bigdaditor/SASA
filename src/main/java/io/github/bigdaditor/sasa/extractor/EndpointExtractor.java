package io.github.bigdaditor.sasa.extractor;

import io.github.bigdaditor.sasa.SasaConfig;
import org.springframework.http.MediaType;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Endpoint 정보 추출 (paths, methods, parameters, response 등)
 */
public class EndpointExtractor {

    /**
     * 모든 엔드포인트 정보 추출
     */
    public static List<Map<String, Object>> extractEndpoints(RequestMappingHandlerMapping mapping, SasaConfig config) {
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
            if (!shouldIncludeEndpoint(paths, methods, config)) {
                continue;
            }

            Map<String, Object> endpoint = extractEndpointInfo(info, hm);
            endpoints.add(endpoint);
        }

        return endpoints;
    }

    /**
     * 단일 엔드포인트 정보 추출
     */
    private static Map<String, Object> extractEndpointInfo(RequestMappingInfo info, HandlerMethod hm) {
        Map<String, Object> endpoint = new LinkedHashMap<>();

        // Paths
        Set<String> paths = info.getPathPatternsCondition() != null
                ? info.getPathPatternsCondition().getPatternValues()
                : Set.of();
        endpoint.put("paths", paths);

        // Methods
        Set<String> methods = info.getMethodsCondition().getMethods().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());
        if (methods.isEmpty()) {
            methods = Set.of("ANY");
        }
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

        // Handler (구조화된 정보)
        Map<String, Object> handler = new LinkedHashMap<>();
        handler.put("controller", hm.getBeanType().getSimpleName());
        handler.put("method", hm.getMethod().getName());
        handler.put("fullControllerName", hm.getBeanType().getName());
        endpoint.put("handler", handler);

        // Request 파라미터 정보 추출
        List<Map<String, Object>> parameters = ParameterExtractor.extractParameters(hm.getMethod());
        endpoint.put("parameters", parameters);

        // Response 정보 추가 (제네릭 타입 포함)
        Map<String, Object> responseInfo = ResponseExtractor.extractResponseInfo(hm.getMethod());
        endpoint.put("response", responseInfo);

        return endpoint;
    }

    /**
     * 엔드포인트를 포함할지 필터링
     */
    private static boolean shouldIncludeEndpoint(Set<String> paths, Set<String> methods, SasaConfig config) {
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

        return shouldInclude;
    }
}