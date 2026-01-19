package io.github.bigdaditor.sasa.generator.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.bigdaditor.sasa.generator.api.OutputGenerator;

import java.util.Map;

/**
 * JSON 출력 생성기
 */
public class JsonOutputGenerator implements OutputGenerator {

    private final ObjectMapper objectMapper;

    public JsonOutputGenerator() {
        this.objectMapper = createObjectMapper();
    }

    public JsonOutputGenerator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String generate(Map<String, Object> apiSpec) {
        try {
            return objectMapper.writeValueAsString(apiSpec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate JSON", e);
        }
    }

    @Override
    public String getFileExtension() {
        return ".json";
    }

    @Override
    public String getContentType() {
        return "application/json";
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
