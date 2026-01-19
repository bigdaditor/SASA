package io.github.bigdaditor.sasa.util;

/**
 * SASA 상수 정의
 */
public final class Constants {

    private Constants() {
        // 인스턴스화 방지
    }

    // === Output 관련 ===
    public static final String DEFAULT_OUTPUT_PATH = "build/api-spec.json";
    public static final String DEFAULT_APP_NAME = "SASA";
    public static final String DEFAULT_VERSION = "0.0.1-SNAPSHOT";

    // === Parameter Types ===
    public static final String PARAM_TYPE_REQUEST_BODY = "REQUEST_BODY";
    public static final String PARAM_TYPE_REQUEST_PARAM = "REQUEST_PARAM";
    public static final String PARAM_TYPE_PATH_VARIABLE = "PATH_VARIABLE";
    public static final String PARAM_TYPE_REQUEST_HEADER = "REQUEST_HEADER";
    public static final String PARAM_TYPE_OTHER = "OTHER";

    // === HTTP Methods ===
    public static final String METHOD_ANY = "ANY";

    // === Example Values ===
    public static final String EXAMPLE_STRING = "string";
    public static final String EXAMPLE_DATETIME = "2024-01-01T00:00:00";
    public static final String EXAMPLE_DATE = "2024-01-01";
    public static final String EXAMPLE_TIME = "00:00:00";

    // === Console Output ===
    public static final String CONSOLE_HEADER = "\n=== SASA: API Specification ===";
    public static final String CONSOLE_FOOTER = "=== SASA: End ===\n";
}
