package io.github.bigdaditor.sasa.extractor.impl;

import io.github.bigdaditor.sasa.SasaConfig;
import io.github.bigdaditor.sasa.extractor.api.DescriptionExtractor;
import io.github.bigdaditor.sasa.extractor.api.EndpointExtractor;
import io.github.bigdaditor.sasa.extractor.api.ParameterExtractor;
import io.github.bigdaditor.sasa.extractor.api.ResponseExtractor;
import org.springframework.http.MediaType;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Endpoint 정보 추출 기본 구현
 */
public class DefaultEndpointExtractor implements EndpointExtractor {

    private final ParameterExtractor parameterExtractor;
    private final ResponseExtractor responseExtractor;
    private final DescriptionExtractor descriptionExtractor;

    public DefaultEndpointExtractor() {
        this.parameterExtractor = new DefaultParameterExtractor();
        this.responseExtractor = new DefaultResponseExtractor();
        this.descriptionExtractor = new DefaultDescriptionExtractor();
    }

    public DefaultEndpointExtractor(ParameterExtractor parameterExtractor,
                                    ResponseExtractor responseExtractor,
                                    DescriptionExtractor descriptionExtractor) {
        this.parameterExtractor = parameterExtractor;
        this.responseExtractor = responseExtractor;
        this.descriptionExtractor = descriptionExtractor;
    }

    @Override
    public List<Map<String, Object>> extract(RequestMappingHandlerMapping mapping, SasaConfig config) {
        List<Map<String, Object>> endpoints = new ArrayList<>();

        for (var entry : mapping.getHandlerMethods().entrySet()) {
            RequestMappingInfo info = entry.getKey();
            HandlerMethod handlerMethod = entry.getValue();

            Set<String> paths = extractPaths(info);
            Set<String> methods = extractMethods(info);

            if (!shouldIncludeEndpoint(paths, methods, config)) {
                continue;
            }

            Map<String, Object> endpoint = extractEndpointInfo(info, handlerMethod);
            endpoints.add(endpoint);
        }

        return endpoints;
    }

    private Set<String> extractPaths(RequestMappingInfo info) {
        return info.getPathPatternsCondition() != null
                ? info.getPathPatternsCondition().getPatternValues()
                : Set.of();
    }

    private Set<String> extractMethods(RequestMappingInfo info) {
        Set<String> methods = info.getMethodsCondition().getMethods().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());
        return methods.isEmpty() ? Set.of("ANY") : methods;
    }

    private Map<String, Object> extractEndpointInfo(RequestMappingInfo info, HandlerMethod handlerMethod) {
        Map<String, Object> endpoint = new LinkedHashMap<>();

        endpoint.put("paths", extractPaths(info));
        endpoint.put("methods", extractMethods(info));

        Set<String> consumes = info.getConsumesCondition().getConsumableMediaTypes().stream()
                .map(MediaType::toString)
                .collect(Collectors.toSet());
        endpoint.put("consumes", consumes);

        Set<String> produces = info.getProducesCondition().getProducibleMediaTypes().stream()
                .map(MediaType::toString)
                .collect(Collectors.toSet());
        endpoint.put("produces", produces);

        Map<String, Object> handler = new LinkedHashMap<>();
        handler.put("controller", handlerMethod.getBeanType().getSimpleName());
        handler.put("method", handlerMethod.getMethod().getName());
        handler.put("fullControllerName", handlerMethod.getBeanType().getName());
        endpoint.put("handler", handler);

        Map<String, Object> descriptionInfo = descriptionExtractor.extract(
                handlerMethod.getMethod(), handlerMethod.getBeanType());
        if (!descriptionInfo.isEmpty()) {
            endpoint.put("description", descriptionInfo);
        }

        List<Map<String, Object>> parameters = parameterExtractor.extract(handlerMethod.getMethod());
        endpoint.put("parameters", parameters);

        Map<String, Object> responseInfo = responseExtractor.extractResponseInfo(handlerMethod.getMethod());
        endpoint.put("response", responseInfo);

        return endpoint;
    }

    private boolean shouldIncludeEndpoint(Set<String> paths, Set<String> methods, SasaConfig config) {
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

        if (shouldInclude && config.getCustomEndpointFilter() != null) {
            String firstPath = paths.isEmpty() ? "" : paths.iterator().next();
            shouldInclude = config.getCustomEndpointFilter().test(firstPath);
        }

        return shouldInclude;
    }
}
