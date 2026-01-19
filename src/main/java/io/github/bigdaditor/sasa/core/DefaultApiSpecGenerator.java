package io.github.bigdaditor.sasa.core;

import io.github.bigdaditor.sasa.SasaConfig;
import io.github.bigdaditor.sasa.extractor.api.EndpointExtractor;
import io.github.bigdaditor.sasa.extractor.api.ExceptionHandlerExtractor;
import io.github.bigdaditor.sasa.extractor.impl.DefaultEndpointExtractor;
import io.github.bigdaditor.sasa.extractor.impl.DefaultExceptionHandlerExtractor;
import io.github.bigdaditor.sasa.generator.HtmlGenerator;
import io.github.bigdaditor.sasa.generator.impl.JsonOutputGenerator;
import io.github.bigdaditor.sasa.output.FileOutputWriter;
import io.github.bigdaditor.sasa.output.OutputWriter;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.time.LocalDateTime;
import java.util.*;

/**
 * API Spec 생성기 기본 구현
 */
public class DefaultApiSpecGenerator implements ApiSpecGenerator {

    private final SasaConfig config;
    private final EndpointExtractor endpointExtractor;
    private final ExceptionHandlerExtractor exceptionHandlerExtractor;
    private final JsonOutputGenerator jsonGenerator;
    private final OutputWriter outputWriter;

    public DefaultApiSpecGenerator() {
        this(SasaConfig.builder().build());
    }

    public DefaultApiSpecGenerator(SasaConfig config) {
        this.config = config;
        this.endpointExtractor = new DefaultEndpointExtractor();
        this.exceptionHandlerExtractor = new DefaultExceptionHandlerExtractor();
        this.jsonGenerator = new JsonOutputGenerator();
        this.outputWriter = new FileOutputWriter();
    }

    public DefaultApiSpecGenerator(SasaConfig config,
                                   EndpointExtractor endpointExtractor,
                                   ExceptionHandlerExtractor exceptionHandlerExtractor) {
        this.config = config;
        this.endpointExtractor = endpointExtractor;
        this.exceptionHandlerExtractor = exceptionHandlerExtractor;
        this.jsonGenerator = new JsonOutputGenerator();
        this.outputWriter = new FileOutputWriter();
    }

    @Override
    public Map<String, Object> generate(RequestMappingHandlerMapping mapping) {
        return generate(mapping, null);
    }

    @Override
    public Map<String, Object> generate(RequestMappingHandlerMapping mapping, ApplicationContext applicationContext) {
        List<Map<String, Object>> endpoints = endpointExtractor.extract(mapping, config);

        List<Map<String, Object>> exceptionHandlers = new ArrayList<>();
        if (applicationContext != null) {
            exceptionHandlers = exceptionHandlerExtractor.extract(applicationContext);
        }

        Map<String, Object> spec = new LinkedHashMap<>();
        spec.put("applicationName", config.getApplicationName());
        spec.put("version", "0.0.1-SNAPSHOT");
        spec.put("generatedAt", LocalDateTime.now().toString());
        spec.put("endpoints", endpoints);
        spec.put("exceptionHandlers", exceptionHandlers);

        return spec;
    }

    @Override
    public void generateAndOutput(RequestMappingHandlerMapping mapping) {
        generateAndOutput(mapping, null);
    }

    @Override
    public void generateAndOutput(RequestMappingHandlerMapping mapping, ApplicationContext applicationContext) {
        Map<String, Object> apiSpec = generate(mapping, applicationContext);

        // 콘솔 출력
        if (config.isEnableConsoleOutput()) {
            String json = jsonGenerator.generate(apiSpec);
            System.out.println("\n=== SASA: API Specification ===");
            System.out.println(json);
            System.out.println("=== SASA: End ===\n");
        }

        // 파일 저장
        if (config.isEnableFileOutput()) {
            String jsonPath = config.getOutputFilePath();
            String htmlPath = jsonPath.replace(".json", ".html");

            // JSON
            String json = jsonGenerator.generate(apiSpec);
            outputWriter.write(json, jsonPath);

            // HTML
            String html = HtmlGenerator.generateHtml(apiSpec);
            outputWriter.write(html, htmlPath);
        }
    }

    @Override
    public SasaConfig getConfig() {
        return config;
    }
}
