package com.example.sasa;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.http.MediaType;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.File;
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
        generateApiSpec(mapping, SasaConfig.builder().build());
    }

    /**
     * 커스텀 설정으로 API 스펙을 추출
     *
     * @param mapping Spring MVC RequestMappingHandlerMapping
     * @param config SASA 설정
     */
    public static void generateApiSpec(RequestMappingHandlerMapping mapping, SasaConfig config) {
        try {
            ObjectMapper mapper = createObjectMapper();
            Map<String, Object> apiSpec = extractApiSpec(mapping, config);

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

    private static Map<String, Object> extractApiSpec(RequestMappingHandlerMapping mapping, SasaConfig config) {
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

            endpoints.add(endpoint);
        }

        Map<String, Object> spec = new LinkedHashMap<>();
        spec.put("applicationName", config.getApplicationName());
        spec.put("version", "0.0.1-SNAPSHOT");
        spec.put("generatedAt", LocalDateTime.now().toString());
        spec.put("endpoints", endpoints);

        return spec;
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
