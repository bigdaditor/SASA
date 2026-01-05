package io.github.bigdaditor.sasa;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * SASA 설정을 위한 빌더 클래스
 */
public class SasaConfig {

    private final boolean enableConsoleOutput;
    private final boolean enableFileOutput;
    private final String outputFilePath;
    private final String applicationName;
    private final Set<String> includePathPatterns;
    private final Set<String> excludePathPatterns;
    private final Set<String> includeHttpMethods;
    private final Set<String> excludeHttpMethods;
    private final Predicate<String> customEndpointFilter;

    private SasaConfig(Builder builder) {
        this.enableConsoleOutput = builder.enableConsoleOutput;
        this.enableFileOutput = builder.enableFileOutput;
        this.outputFilePath = builder.outputFilePath;
        this.applicationName = builder.applicationName;
        this.includePathPatterns = builder.includePathPatterns;
        this.excludePathPatterns = builder.excludePathPatterns;
        this.includeHttpMethods = builder.includeHttpMethods;
        this.excludeHttpMethods = builder.excludeHttpMethods;
        this.customEndpointFilter = builder.customEndpointFilter;
    }

    public boolean isEnableConsoleOutput() {
        return enableConsoleOutput;
    }

    public boolean isEnableFileOutput() {
        return enableFileOutput;
    }

    public String getOutputFilePath() {
        return outputFilePath;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public Set<String> getIncludePathPatterns() {
        return includePathPatterns;
    }

    public Set<String> getExcludePathPatterns() {
        return excludePathPatterns;
    }

    public Set<String> getIncludeHttpMethods() {
        return includeHttpMethods;
    }

    public Set<String> getExcludeHttpMethods() {
        return excludeHttpMethods;
    }

    public Predicate<String> getCustomEndpointFilter() {
        return customEndpointFilter;
    }

    /**
     * 경로가 필터 조건을 통과하는지 확인
     */
    public boolean shouldIncludePath(String path) {
        // Exclude 패턴 체크
        if (!excludePathPatterns.isEmpty()) {
            for (String pattern : excludePathPatterns) {
                if (matchesPattern(path, pattern)) {
                    return false;
                }
            }
        }

        // Include 패턴 체크 (설정된 경우)
        if (!includePathPatterns.isEmpty()) {
            for (String pattern : includePathPatterns) {
                if (matchesPattern(path, pattern)) {
                    return true;
                }
            }
            return false; // Include 패턴이 있는데 매치되지 않음
        }

        return true; // 기본적으로 포함
    }

    /**
     * HTTP 메서드가 필터 조건을 통과하는지 확인
     */
    public boolean shouldIncludeMethod(String method) {
        // Exclude 메서드 체크
        if (excludeHttpMethods.contains(method)) {
            return false;
        }

        // Include 메서드 체크 (설정된 경우)
        if (!includeHttpMethods.isEmpty()) {
            return includeHttpMethods.contains(method);
        }

        return true; // 기본적으로 포함
    }

    private boolean matchesPattern(String path, String pattern) {
        // 간단한 wildcard 매칭 (*, **)
        String regex = pattern
                .replace("**", "___DOUBLE_STAR___")
                .replace("*", "[^/]*")
                .replace("___DOUBLE_STAR___", ".*");
        return path.matches(regex);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private boolean enableConsoleOutput = false;
        private boolean enableFileOutput = true;
        private String outputFilePath = "build/api-spec.json";
        private String applicationName = "SASA";
        private Set<String> includePathPatterns = new HashSet<>();
        private Set<String> excludePathPatterns = new HashSet<>();
        private Set<String> includeHttpMethods = new HashSet<>();
        private Set<String> excludeHttpMethods = new HashSet<>();
        private Predicate<String> customEndpointFilter = null;

        /**
         * 콘솔 출력 활성화/비활성화
         */
        public Builder enableConsoleOutput(boolean enable) {
            this.enableConsoleOutput = enable;
            return this;
        }

        /**
         * 파일 출력 활성화/비활성화
         */
        public Builder enableFileOutput(boolean enable) {
            this.enableFileOutput = enable;
            return this;
        }

        /**
         * 출력 파일 경로 설정
         */
        public Builder outputFilePath(String path) {
            this.outputFilePath = path;
            return this;
        }

        /**
         * 애플리케이션 이름 설정
         */
        public Builder applicationName(String name) {
            this.applicationName = name;
            return this;
        }

        /**
         * 포함할 경로 패턴 추가 (예: "/api/**", "/user/*")
         */
        public Builder includePath(String pattern) {
            this.includePathPatterns.add(pattern);
            return this;
        }

        /**
         * 제외할 경로 패턴 추가 (예: "/actuator/**", "/error")
         */
        public Builder excludePath(String pattern) {
            this.excludePathPatterns.add(pattern);
            return this;
        }

        /**
         * 포함할 HTTP 메서드 추가 (예: "GET", "POST")
         */
        public Builder includeHttpMethod(String method) {
            this.includeHttpMethods.add(method.toUpperCase());
            return this;
        }

        /**
         * 제외할 HTTP 메서드 추가 (예: "DELETE", "PATCH")
         */
        public Builder excludeHttpMethod(String method) {
            this.excludeHttpMethods.add(method.toUpperCase());
            return this;
        }

        /**
         * Spring Actuator 엔드포인트 제외
         */
        public Builder excludeActuator() {
            this.excludePathPatterns.add("/actuator/**");
            return this;
        }

        /**
         * Spring Error 엔드포인트 제외
         */
        public Builder excludeError() {
            this.excludePathPatterns.add("/error");
            return this;
        }

        /**
         * GET 메서드만 포함
         */
        public Builder onlyGetMethods() {
            this.includeHttpMethods.clear();
            this.includeHttpMethods.add("GET");
            return this;
        }

        /**
         * 읽기 전용 메서드만 포함 (GET, HEAD, OPTIONS)
         */
        public Builder onlyReadMethods() {
            this.includeHttpMethods.clear();
            this.includeHttpMethods.add("GET");
            this.includeHttpMethods.add("HEAD");
            this.includeHttpMethods.add("OPTIONS");
            return this;
        }

        /**
         * 커스텀 엔드포인트 필터 설정
         */
        public Builder customFilter(Predicate<String> filter) {
            this.customEndpointFilter = filter;
            return this;
        }

        public SasaConfig build() {
            return new SasaConfig(this);
        }
    }
}
