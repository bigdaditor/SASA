package io.github.bigdaditor.sasa;

import io.github.bigdaditor.sasa.extractor.EndpointExtractor;
import io.github.bigdaditor.sasa.extractor.ExceptionHandlerExtractor;
import io.github.bigdaditor.sasa.generator.HtmlGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

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

                // HTML 파일 생성
                String htmlPath = config.getOutputFilePath().replace(".json", ".html");
                saveHtmlFile(apiSpec, htmlPath);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate API spec", e);
        }
    }

    /**
     * API 스펙 추출 (기본 설정)
     */
    public static Map<String, Object> extractApiSpec(RequestMappingHandlerMapping mapping, SasaConfig config) {
        return extractApiSpec(mapping, null, config);
    }

    /**
     * API 스펙 추출 (ApplicationContext 포함)
     */
    public static Map<String, Object> extractApiSpec(RequestMappingHandlerMapping mapping, ApplicationContext applicationContext, SasaConfig config) {
        // Endpoints 추출
        List<Map<String, Object>> endpoints = EndpointExtractor.extractEndpoints(mapping, config);

        // Exception handlers 추출
        List<Map<String, Object>> exceptionHandlers = new ArrayList<>();
        if (applicationContext != null) {
            exceptionHandlers = ExceptionHandlerExtractor.extractExceptionHandlers(applicationContext);
        }

        // API 스펙 생성
        Map<String, Object> spec = new LinkedHashMap<>();
        spec.put("applicationName", config.getApplicationName());
        spec.put("version", "0.0.1-SNAPSHOT");
        spec.put("generatedAt", LocalDateTime.now().toString());
        spec.put("endpoints", endpoints);
        spec.put("exceptionHandlers", exceptionHandlers);

        return spec;
    }

    /**
     * ObjectMapper 생성
     */
    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    /**
     * 파일로 저장
     */
    private static void saveToFile(ObjectMapper mapper, Map<String, Object> spec, String filePath) throws Exception {
        Path path = Paths.get(filePath);
        Path parent = path.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
        mapper.writeValue(new File(filePath), spec);
        System.out.println("API Spec (JSON) saved to: " + path.toAbsolutePath());
    }

    /**
     * HTML 파일로 저장
     */
    private static void saveHtmlFile(Map<String, Object> spec, String filePath) throws Exception {
        Path path = Paths.get(filePath);
        Path parent = path.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }

        String html = HtmlGenerator.generateHtml(spec);
        Files.write(path, html.getBytes(StandardCharsets.UTF_8));
        System.out.println("API Spec (HTML) saved to: " + path.toAbsolutePath());
    }
}