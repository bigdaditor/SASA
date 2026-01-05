package com.example.sasa;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SasaConfigTest {

    @Test
    void testBuilderDefaults() {
        SasaConfig config = SasaConfig.builder().build();

        assertFalse(config.isEnableConsoleOutput(), "Console output should be disabled by default");
        assertTrue(config.isEnableFileOutput(), "File output should be enabled by default");
        assertEquals("build/api-spec.json", config.getOutputFilePath(), "Default output path");
        assertEquals("SASA", config.getApplicationName(), "Default application name");
        assertTrue(config.getIncludePathPatterns().isEmpty(), "Include patterns should be empty");
        assertTrue(config.getExcludePathPatterns().isEmpty(), "Exclude patterns should be empty");
        assertTrue(config.getIncludeHttpMethods().isEmpty(), "Include methods should be empty");
        assertTrue(config.getExcludeHttpMethods().isEmpty(), "Exclude methods should be empty");
        assertNull(config.getCustomEndpointFilter(), "Custom filter should be null");
    }

    @Test
    void testBuilderSetters() {
        SasaConfig config = SasaConfig.builder()
                .enableConsoleOutput(true)
                .enableFileOutput(false)
                .outputFilePath("custom/path.json")
                .applicationName("MyApp")
                .build();

        assertTrue(config.isEnableConsoleOutput());
        assertFalse(config.isEnableFileOutput());
        assertEquals("custom/path.json", config.getOutputFilePath());
        assertEquals("MyApp", config.getApplicationName());
    }

    @Test
    void testIncludePath() {
        SasaConfig config = SasaConfig.builder()
                .includePath("/api/**")
                .build();

        assertTrue(config.shouldIncludePath("/api/users"), "Should include matching path");
        assertTrue(config.shouldIncludePath("/api/users/123"), "Should include nested path");
        assertFalse(config.shouldIncludePath("/admin/settings"), "Should exclude non-matching path");
    }

    @Test
    void testExcludePath() {
        SasaConfig config = SasaConfig.builder()
                .excludePath("/actuator/**")
                .excludePath("/error")
                .build();

        assertTrue(config.shouldIncludePath("/api/users"), "Should include non-excluded path");
        assertFalse(config.shouldIncludePath("/actuator/health"), "Should exclude actuator path");
        assertFalse(config.shouldIncludePath("/error"), "Should exclude error path");
    }

    @Test
    void testWildcardPatternSingleStar() {
        SasaConfig config = SasaConfig.builder()
                .includePath("/user/*")
                .build();

        assertTrue(config.shouldIncludePath("/user/123"), "Should match single segment");
        assertFalse(config.shouldIncludePath("/user/123/profile"), "Should not match nested segments");
    }

    @Test
    void testWildcardPatternDoubleStar() {
        SasaConfig config = SasaConfig.builder()
                .includePath("/api/**")
                .build();

        assertTrue(config.shouldIncludePath("/api/v1/users"), "Should match nested paths");
        assertTrue(config.shouldIncludePath("/api/v1/users/123/profile"), "Should match deep nested paths");
    }

    @Test
    void testIncludeAndExcludeCombined() {
        SasaConfig config = SasaConfig.builder()
                .includePath("/api/**")
                .excludePath("/api/internal/**")
                .build();

        assertTrue(config.shouldIncludePath("/api/users"), "Should include matching path");
        assertFalse(config.shouldIncludePath("/api/internal/metrics"), "Exclude should take precedence");
    }

    @Test
    void testIncludeHttpMethod() {
        SasaConfig config = SasaConfig.builder()
                .includeHttpMethod("GET")
                .includeHttpMethod("POST")
                .build();

        assertTrue(config.shouldIncludeMethod("GET"), "Should include GET");
        assertTrue(config.shouldIncludeMethod("POST"), "Should include POST");
        assertFalse(config.shouldIncludeMethod("DELETE"), "Should exclude DELETE");
    }

    @Test
    void testExcludeHttpMethod() {
        SasaConfig config = SasaConfig.builder()
                .excludeHttpMethod("DELETE")
                .excludeHttpMethod("PATCH")
                .build();

        assertTrue(config.shouldIncludeMethod("GET"), "Should include GET");
        assertTrue(config.shouldIncludeMethod("POST"), "Should include POST");
        assertFalse(config.shouldIncludeMethod("DELETE"), "Should exclude DELETE");
        assertFalse(config.shouldIncludeMethod("PATCH"), "Should exclude PATCH");
    }

    @Test
    void testHttpMethodCaseInsensitive() {
        SasaConfig config = SasaConfig.builder()
                .includeHttpMethod("get")
                .includeHttpMethod("Post")
                .build();

        assertTrue(config.shouldIncludeMethod("GET"), "Should handle uppercase");
        assertTrue(config.shouldIncludeMethod("POST"), "Should handle mixed case");
    }

    @Test
    void testExcludeActuatorHelper() {
        SasaConfig config = SasaConfig.builder()
                .excludeActuator()
                .build();

        assertFalse(config.shouldIncludePath("/actuator/health"), "Should exclude actuator");
        assertFalse(config.shouldIncludePath("/actuator/metrics"), "Should exclude actuator metrics");
        assertTrue(config.shouldIncludePath("/api/users"), "Should include other paths");
    }

    @Test
    void testExcludeErrorHelper() {
        SasaConfig config = SasaConfig.builder()
                .excludeError()
                .build();

        assertFalse(config.shouldIncludePath("/error"), "Should exclude error path");
        assertTrue(config.shouldIncludePath("/api/users"), "Should include other paths");
    }

    @Test
    void testOnlyGetMethodsHelper() {
        SasaConfig config = SasaConfig.builder()
                .onlyGetMethods()
                .build();

        assertTrue(config.shouldIncludeMethod("GET"), "Should include GET");
        assertFalse(config.shouldIncludeMethod("POST"), "Should exclude POST");
        assertFalse(config.shouldIncludeMethod("PUT"), "Should exclude PUT");
        assertFalse(config.shouldIncludeMethod("DELETE"), "Should exclude DELETE");
    }

    @Test
    void testOnlyReadMethodsHelper() {
        SasaConfig config = SasaConfig.builder()
                .onlyReadMethods()
                .build();

        assertTrue(config.shouldIncludeMethod("GET"), "Should include GET");
        assertTrue(config.shouldIncludeMethod("HEAD"), "Should include HEAD");
        assertTrue(config.shouldIncludeMethod("OPTIONS"), "Should include OPTIONS");
        assertFalse(config.shouldIncludeMethod("POST"), "Should exclude POST");
        assertFalse(config.shouldIncludeMethod("PUT"), "Should exclude PUT");
        assertFalse(config.shouldIncludeMethod("DELETE"), "Should exclude DELETE");
    }

    @Test
    void testCustomFilter() {
        SasaConfig config = SasaConfig.builder()
                .customFilter(path -> path.startsWith("/api/v1"))
                .build();

        assertNotNull(config.getCustomEndpointFilter(), "Custom filter should be set");
        assertTrue(config.getCustomEndpointFilter().test("/api/v1/users"), "Custom filter should accept matching path");
        assertFalse(config.getCustomEndpointFilter().test("/api/v2/users"), "Custom filter should reject non-matching path");
    }

    @Test
    void testBuilderChaining() {
        SasaConfig config = SasaConfig.builder()
                .enableConsoleOutput(true)
                .enableFileOutput(true)
                .outputFilePath("test.json")
                .applicationName("TestApp")
                .includePath("/api/**")
                .excludePath("/api/internal/**")
                .includeHttpMethod("GET")
                .excludeHttpMethod("DELETE")
                .excludeActuator()
                .excludeError()
                .build();

        assertNotNull(config, "Builder chaining should work");
        assertTrue(config.isEnableConsoleOutput());
        assertEquals("TestApp", config.getApplicationName());
        assertTrue(config.shouldIncludePath("/api/users"));
        assertFalse(config.shouldIncludePath("/api/internal/metrics"));
        assertFalse(config.shouldIncludePath("/actuator/health"));
        assertFalse(config.shouldIncludePath("/error"));
        assertTrue(config.shouldIncludeMethod("GET"));
        assertFalse(config.shouldIncludeMethod("DELETE"));
    }

    @Test
    void testOnlyGetMethodsOverridesPreviousIncludes() {
        SasaConfig config = SasaConfig.builder()
                .includeHttpMethod("POST")
                .includeHttpMethod("PUT")
                .onlyGetMethods() // Should clear previous includes
                .build();

        assertTrue(config.shouldIncludeMethod("GET"), "Should include GET");
        assertFalse(config.shouldIncludeMethod("POST"), "Should not include POST");
        assertFalse(config.shouldIncludeMethod("PUT"), "Should not include PUT");
    }

    @Test
    void testOnlyReadMethodsOverridesPreviousIncludes() {
        SasaConfig config = SasaConfig.builder()
                .includeHttpMethod("POST")
                .onlyReadMethods() // Should clear previous includes
                .build();

        assertTrue(config.shouldIncludeMethod("GET"));
        assertTrue(config.shouldIncludeMethod("HEAD"));
        assertTrue(config.shouldIncludeMethod("OPTIONS"));
        assertFalse(config.shouldIncludeMethod("POST"));
    }

    @Test
    void testEmptyPathPatternsIncludesAll() {
        SasaConfig config = SasaConfig.builder().build();

        assertTrue(config.shouldIncludePath("/api/users"), "Should include all paths by default");
        assertTrue(config.shouldIncludePath("/admin/settings"), "Should include all paths by default");
    }

    @Test
    void testEmptyHttpMethodsIncludesAll() {
        SasaConfig config = SasaConfig.builder().build();

        assertTrue(config.shouldIncludeMethod("GET"), "Should include all methods by default");
        assertTrue(config.shouldIncludeMethod("POST"), "Should include all methods by default");
        assertTrue(config.shouldIncludeMethod("DELETE"), "Should include all methods by default");
    }

    @Test
    void testMultipleIncludePatterns() {
        SasaConfig config = SasaConfig.builder()
                .includePath("/api/**")
                .includePath("/public/**")
                .build();

        assertTrue(config.shouldIncludePath("/api/users"));
        assertTrue(config.shouldIncludePath("/public/info"));
        assertFalse(config.shouldIncludePath("/admin/settings"));
    }

    @Test
    void testMultipleExcludePatterns() {
        SasaConfig config = SasaConfig.builder()
                .excludePath("/api/internal/**")
                .excludePath("/api/admin/**")
                .build();

        assertTrue(config.shouldIncludePath("/api/users"));
        assertFalse(config.shouldIncludePath("/api/internal/metrics"));
        assertFalse(config.shouldIncludePath("/api/admin/users"));
    }
}
