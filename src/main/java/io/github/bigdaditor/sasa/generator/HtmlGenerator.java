package io.github.bigdaditor.sasa.generator;

import java.util.*;

/**
 * API Spec JSON을 HTML 문서로 변환하는 생성기
 */
public class HtmlGenerator {

    /**
     * API Spec을 HTML로 변환
     */
    public static String generateHtml(Map<String, Object> apiSpec) {
        StringBuilder html = new StringBuilder();

        // HTML 헤더
        html.append(generateHtmlHeader(apiSpec));

        // 네비게이션
        html.append(generateNavigation(apiSpec));

        // 본문 시작
        html.append("<div class='container'>\n");

        // 개요
        html.append(generateOverview(apiSpec));

        // 엔드포인트 목록
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> endpoints = (List<Map<String, Object>>) apiSpec.get("endpoints");
        if (endpoints != null && !endpoints.isEmpty()) {
            html.append(generateEndpointsSection(endpoints));
        }

        // Exception Handlers
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> exceptionHandlers = (List<Map<String, Object>>) apiSpec.get("exceptionHandlers");
        if (exceptionHandlers != null && !exceptionHandlers.isEmpty()) {
            html.append(generateExceptionHandlersSection(exceptionHandlers));
        }

        html.append("</div>\n");

        // HTML 푸터
        html.append(generateHtmlFooter());

        return html.toString();
    }

    /**
     * HTML 헤더 생성
     */
    private static String generateHtmlHeader(Map<String, Object> apiSpec) {
        String appName = (String) apiSpec.getOrDefault("applicationName", "API");

        return """
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>%s - API Documentation</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
            line-height: 1.6;
            color: #1a1a1a;
            background: #ffffff;
        }

        .header {
            background: #000000;
            color: white;
            padding: 2rem 0;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }

        .header .container {
            max-width: 1200px;
            margin: 0 auto;
            padding: 0 2rem;
        }

        .header h1 {
            font-size: 2.5rem;
            margin-bottom: 0.5rem;
        }

        .header .meta {
            opacity: 0.9;
            font-size: 0.9rem;
        }

        .nav {
            background: white;
            border-bottom: 1px solid #e1e8ed;
            position: sticky;
            top: 0;
            z-index: 100;
            box-shadow: 0 2px 4px rgba(0,0,0,0.05);
        }

        .nav .container {
            max-width: 1200px;
            margin: 0 auto;
            padding: 0 2rem;
            display: flex;
            gap: 2rem;
        }

        .nav a {
            padding: 1rem 0;
            color: #1a1a1a;
            text-decoration: none;
            font-weight: 500;
            border-bottom: 2px solid transparent;
            transition: all 0.2s;
        }

        .nav a:hover {
            color: #000000;
            border-bottom-color: #000000;
        }

        .container {
            max-width: 1200px;
            margin: 2rem auto;
            padding: 0 2rem;
        }

        .section {
            background: white;
            border-radius: 8px;
            padding: 2rem;
            margin-bottom: 2rem;
            box-shadow: 0 2px 8px rgba(0,0,0,0.05);
        }

        .section-title {
            font-size: 1.8rem;
            margin-bottom: 1.5rem;
            color: #1a1a1a;
            border-bottom: 2px solid #000000;
            padding-bottom: 0.5rem;
        }

        .endpoint {
            border: 1px solid #e1e8ed;
            border-radius: 8px;
            margin-bottom: 1.5rem;
            overflow: hidden;
        }

        .endpoint-header {
            background: #f7fafc;
            padding: 1rem 1.5rem;
            border-bottom: 1px solid #e1e8ed;
            cursor: pointer;
            display: flex;
            align-items: center;
            gap: 1rem;
        }

        .endpoint-header:hover {
            background: #edf2f7;
        }

        .method-badge {
            padding: 0.25rem 0.75rem;
            border-radius: 4px;
            font-weight: 600;
            font-size: 0.85rem;
            text-transform: uppercase;
        }

        .method-GET { background: #10b981; color: white; }
        .method-POST { background: #3b82f6; color: white; }
        .method-PUT { background: #f59e0b; color: white; }
        .method-DELETE { background: #ef4444; color: white; }
        .method-PATCH { background: #8b5cf6; color: white; }
        .method-ANY { background: #6b7280; color: white; }

        .endpoint-path {
            font-family: 'Courier New', monospace;
            font-weight: 600;
            color: #2d3748;
            flex: 1;
        }

        .endpoint-body {
            padding: 1.5rem;
            display: none;
        }

        .endpoint-body.active {
            display: block;
        }

        .info-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
            gap: 1.5rem;
            margin-bottom: 1.5rem;
        }

        .info-box {
            background: #f7fafc;
            padding: 1rem;
            border-radius: 6px;
            border-left: 3px solid #6b7280;
        }

        .info-box h4 {
            color: #1a1a1a;
            margin-bottom: 0.5rem;
            font-size: 0.9rem;
            text-transform: uppercase;
            font-weight: 600;
        }

        .parameter {
            background: #f9fafb;
            padding: 0.75rem;
            margin: 0.5rem 0;
            border-radius: 4px;
            border-left: 3px solid #6b7280;
        }

        .parameter-name {
            font-weight: 600;
            color: #2d3748;
        }

        .parameter-type {
            color: #4b5563;
            font-family: monospace;
            font-size: 0.9rem;
        }

        .parameter-meta {
            color: #6b7280;
            font-size: 0.85rem;
            margin-top: 0.25rem;
        }

        .schema {
            background: #1e293b;
            color: #e2e8f0;
            padding: 1rem;
            border-radius: 6px;
            overflow-x: auto;
            font-family: 'Courier New', monospace;
            font-size: 0.9rem;
        }

        .schema-field {
            padding: 0.25rem 0;
        }

        .field-name {
            color: #38bdf8;
        }

        .field-type {
            color: #a78bfa;
        }

        .validation {
            color: #fbbf24;
            font-size: 0.85rem;
            margin-left: 1rem;
        }

        .badge {
            display: inline-block;
            padding: 0.2rem 0.5rem;
            border-radius: 3px;
            font-size: 0.75rem;
            font-weight: 600;
            margin: 0 0.25rem;
        }

        .badge-required { background: #fee2e2; color: #991b1b; }
        .badge-optional { background: #dbeafe; color: #1e40af; }

        .exception-handler {
            background: #fef3c7;
            border-left: 4px solid #f59e0b;
            padding: 1rem;
            margin: 0.75rem 0;
            border-radius: 4px;
        }

        .exception-types {
            font-family: monospace;
            color: #dc2626;
            font-weight: 600;
        }

        .overview-stats {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 1rem;
            margin: 1.5rem 0;
        }

        .stat-card {
            background: #000000;
            color: white;
            padding: 1.5rem;
            border-radius: 8px;
            text-align: center;
        }

        .stat-value {
            font-size: 2.5rem;
            font-weight: 700;
            margin-bottom: 0.5rem;
        }

        .stat-label {
            opacity: 0.9;
            text-transform: uppercase;
            font-size: 0.85rem;
            font-weight: 500;
        }

        code {
            background: #f1f5f9;
            padding: 0.2rem 0.4rem;
            border-radius: 3px;
            font-family: monospace;
            font-size: 0.9rem;
            color: #dc2626;
        }

        .footer {
            text-align: center;
            padding: 2rem;
            color: #6b7280;
            font-size: 0.9rem;
        }

        .toggle-icon {
            transition: transform 0.2s;
        }

        .toggle-icon.active {
            transform: rotate(90deg);
        }

        .endpoint-description {
            background: #f0f9ff;
            border-left: 4px solid #0ea5e9;
            padding: 1rem;
            margin-bottom: 1.5rem;
            border-radius: 0 6px 6px 0;
        }

        .endpoint-description .summary {
            font-weight: 600;
            color: #0369a1;
            margin-bottom: 0.5rem;
        }

        .endpoint-description .full-description {
            color: #374151;
            line-height: 1.6;
        }
    </style>
</head>
<body>
    <div class="header">
        <div class="container">
            <h1>%s API Documentation</h1>
            <div class="meta">
                Version: %s | Generated: %s
            </div>
        </div>
    </div>
""".formatted(
            appName,
            appName,
            apiSpec.getOrDefault("version", "1.0.0"),
            apiSpec.getOrDefault("generatedAt", "")
        );
    }

    /**
     * 네비게이션 생성
     */
    private static String generateNavigation(Map<String, Object> apiSpec) {
        return """
    <div class="nav">
        <div class="container">
            <a href="#overview">Overview</a>
            <a href="#endpoints">Endpoints</a>
            <a href="#exceptions">Exception Handlers</a>
        </div>
    </div>
""";
    }

    /**
     * 개요 섹션 생성
     */
    private static String generateOverview(Map<String, Object> apiSpec) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> endpoints = (List<Map<String, Object>>) apiSpec.get("endpoints");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> exceptionHandlers = (List<Map<String, Object>>) apiSpec.get("exceptionHandlers");

        int endpointCount = endpoints != null ? endpoints.size() : 0;
        int exceptionCount = exceptionHandlers != null ? exceptionHandlers.size() : 0;

        return """
    <div id="overview" class="section">
        <h2 class="section-title">Overview</h2>
        <div class="overview-stats">
            <div class="stat-card">
                <div class="stat-value">%d</div>
                <div class="stat-label">Endpoints</div>
            </div>
            <div class="stat-card">
                <div class="stat-value">%d</div>
                <div class="stat-label">Exception Handlers</div>
            </div>
        </div>
    </div>
""".formatted(endpointCount, exceptionCount);
    }

    /**
     * 엔드포인트 섹션 생성
     */
    private static String generateEndpointsSection(List<Map<String, Object>> endpoints) {
        StringBuilder html = new StringBuilder();
        html.append("    <div id=\"endpoints\" class=\"section\">\n");
        html.append("        <h2 class=\"section-title\">Endpoints</h2>\n");

        for (int i = 0; i < endpoints.size(); i++) {
            html.append(generateEndpoint(endpoints.get(i), i));
        }

        html.append("    </div>\n");
        return html.toString();
    }

    /**
     * 개별 엔드포인트 생성
     */
    private static String generateEndpoint(Map<String, Object> endpoint, int index) {
        @SuppressWarnings("unchecked")
        Set<String> paths = (Set<String>) endpoint.get("paths");
        @SuppressWarnings("unchecked")
        Set<String> methods = (Set<String>) endpoint.get("methods");

        String path = paths != null && !paths.isEmpty() ? paths.iterator().next() : "";
        String method = methods != null && !methods.isEmpty() ? methods.iterator().next() : "GET";

        StringBuilder html = new StringBuilder();
        html.append(String.format("        <div class=\"endpoint\">\n"));
        html.append(String.format("            <div class=\"endpoint-header\" onclick=\"toggleEndpoint(%d)\">\n", index));
        html.append(String.format("                <span class=\"toggle-icon\" id=\"icon-%d\">▶</span>\n", index));
        html.append(String.format("                <span class=\"method-badge method-%s\">%s</span>\n", method, method));
        html.append(String.format("                <span class=\"endpoint-path\">%s</span>\n", escapeHtml(path)));
        html.append("            </div>\n");
        html.append(String.format("            <div class=\"endpoint-body\" id=\"endpoint-%d\">\n", index));

        // Description 정보
        @SuppressWarnings("unchecked")
        Map<String, Object> description = (Map<String, Object>) endpoint.get("description");
        if (description != null && !description.isEmpty()) {
            html.append(generateDescriptionSection(description));
        }

        // Handler 정보
        html.append("                <div class=\"info-grid\">\n");
        html.append("                    <div class=\"info-box\">\n");
        html.append("                        <h4>Handler</h4>\n");
        html.append(String.format("                        <code>%s</code>\n", endpoint.get("handler")));
        html.append("                    </div>\n");
        html.append("                </div>\n");

        // Parameters
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> parameters = (List<Map<String, Object>>) endpoint.get("parameters");
        if (parameters != null && !parameters.isEmpty()) {
            html.append(generateParameters(parameters));
        }

        // Response
        @SuppressWarnings("unchecked")
        Map<String, Object> response = (Map<String, Object>) endpoint.get("response");
        if (response != null) {
            html.append(generateResponse(response));
        }

        html.append("            </div>\n");
        html.append("        </div>\n");

        return html.toString();
    }

    /**
     * Description 섹션 생성
     */
    private static String generateDescriptionSection(Map<String, Object> description) {
        StringBuilder html = new StringBuilder();

        String summary = (String) description.get("summary");
        String fullDescription = (String) description.get("description");

        html.append("                <div class=\"endpoint-description\">\n");

        if (summary != null && !summary.isEmpty()) {
            html.append(String.format("                    <div class=\"summary\">%s</div>\n",
                    escapeHtml(summary)));
        }

        if (fullDescription != null && !fullDescription.isEmpty() &&
                !fullDescription.equals(summary)) {
            html.append(String.format("                    <div class=\"full-description\">%s</div>\n",
                    escapeHtml(fullDescription)));
        }

        html.append("                </div>\n");

        return html.toString();
    }

    /**
     * Parameters 섹션 생성
     */
    private static String generateParameters(List<Map<String, Object>> parameters) {
        StringBuilder html = new StringBuilder();
        html.append("                <div class=\"info-box\">\n");
        html.append("                    <h4>Parameters</h4>\n");

        for (Map<String, Object> param : parameters) {
            String paramType = (String) param.get("parameterType");
            if ("OTHER".equals(paramType)) continue;

            html.append("                    <div class=\"parameter\">\n");
            html.append(String.format("                        <div class=\"parameter-name\">%s</div>\n", param.get("name")));
            html.append(String.format("                        <div class=\"parameter-type\">%s</div>\n", param.get("type")));
            html.append(String.format("                        <div class=\"parameter-meta\">Type: %s", paramType));

            Boolean required = (Boolean) param.get("required");
            if (required != null) {
                html.append(required ?
                    " <span class=\"badge badge-required\">REQUIRED</span>" :
                    " <span class=\"badge badge-optional\">OPTIONAL</span>");
            }
            html.append("</div>\n");

            // Schema for REQUEST_BODY
            if ("REQUEST_BODY".equals(paramType)) {
                @SuppressWarnings("unchecked")
                Map<String, Object> schema = (Map<String, Object>) param.get("schema");
                if (schema != null) {
                    html.append(generateSchema(schema, 5));
                }
            }

            html.append("                    </div>\n");
        }

        html.append("                </div>\n");
        return html.toString();
    }

    /**
     * Response 섹션 생성
     */
    private static String generateResponse(Map<String, Object> response) {
        StringBuilder html = new StringBuilder();
        html.append("                <div class=\"info-box\">\n");
        html.append("                    <h4>Response</h4>\n");
        html.append(String.format("                    <div><strong>Type:</strong> <code>%s</code></div>\n", response.get("type")));

        String genericType = (String) response.get("genericType");
        if (genericType != null) {
            html.append(String.format("                    <div><strong>Generic Type:</strong> <code>%s</code></div>\n", genericType));
        }

        String elementType = (String) response.get("elementType");
        if (elementType != null) {
            html.append(String.format("                    <div><strong>Element Type:</strong> <code>%s</code></div>\n", elementType));
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> schema = (Map<String, Object>) response.get("schema");
        if (schema != null) {
            html.append(generateSchema(schema, 5));
        }

        html.append("                </div>\n");
        return html.toString();
    }

    /**
     * Schema 생성
     */
    private static String generateSchema(Map<String, Object> schema, int indent) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> fields = (List<Map<String, Object>>) schema.get("fields");
        @SuppressWarnings("unchecked")
        Map<String, Object> example = (Map<String, Object>) schema.get("example");

        if (fields == null || fields.isEmpty()) return "";

        StringBuilder html = new StringBuilder();
        String indentStr = " ".repeat(indent * 4);

        // Schema (필드 타입 정보)
        html.append(indentStr).append("<div style=\"margin-top: 1rem;\">\n");
        html.append(indentStr).append("    <strong>Schema:</strong>\n");
        html.append(indentStr).append("    <div class=\"schema\">\n");
        html.append(indentStr).append("        {\n");

        for (int i = 0; i < fields.size(); i++) {
            Map<String, Object> field = fields.get(i);
            html.append(indentStr).append("            ");
            html.append(String.format("<span class=\"field-name\">\"%s\"</span>: ", field.get("name")));
            html.append(String.format("<span class=\"field-type\">\"%s\"</span>", field.get("type")));

            @SuppressWarnings("unchecked")
            Map<String, Object> validations = (Map<String, Object>) field.get("validations");
            if (validations != null && !validations.isEmpty()) {
                html.append(" <span class=\"validation\">");
                html.append(formatValidations(validations));
                html.append("</span>");
            }

            if (i < fields.size() - 1) html.append(",");
            html.append("\n");
        }

        html.append(indentStr).append("        }\n");
        html.append(indentStr).append("    </div>\n");

        // Example JSON
        if (example != null && !example.isEmpty()) {
            html.append(indentStr).append("    <strong style=\"margin-top: 1rem; display: block;\">Example JSON:</strong>\n");
            html.append(indentStr).append("    <div class=\"schema\">\n");
            html.append(indentStr).append("        ").append(formatJsonExample(example, indentStr + "        "));
            html.append(indentStr).append("    </div>\n");
        }

        html.append(indentStr).append("</div>\n");

        return html.toString();
    }

    /**
     * JSON 예시 포맷팅
     */
    private static String formatJsonExample(Map<String, Object> example, String baseIndent) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");

        List<String> keys = new ArrayList<>(example.keySet());
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            Object value = example.get(key);

            json.append(baseIndent).append("    ");
            json.append(String.format("<span class=\"field-name\">\"%s\"</span>: ", escapeHtml(key)));
            json.append(formatJsonValue(value));

            if (i < keys.size() - 1) json.append(",");
            json.append("\n");
        }

        json.append(baseIndent).append("}\n");
        return json.toString();
    }

    /**
     * JSON 값 포맷팅
     */
    private static String formatJsonValue(Object value) {
        if (value == null) {
            return "<span class=\"field-type\">null</span>";
        } else if (value instanceof String) {
            return String.format("<span class=\"field-type\">\"%s\"</span>", escapeHtml((String) value));
        } else if (value instanceof Number || value instanceof Boolean) {
            return String.format("<span class=\"field-type\">%s</span>", value);
        } else if (value instanceof List) {
            return "<span class=\"field-type\">[]</span>";
        } else if (value instanceof Map) {
            return "<span class=\"field-type\">{}</span>";
        } else {
            return String.format("<span class=\"field-type\">\"%s\"</span>", escapeHtml(value.toString()));
        }
    }

    /**
     * Validation 정보 포맷팅
     */
    private static String formatValidations(Map<String, Object> validations) {
        List<String> parts = new ArrayList<>();

        if (Boolean.TRUE.equals(validations.get("notNull"))) parts.add("@NotNull");
        if (Boolean.TRUE.equals(validations.get("notEmpty"))) parts.add("@NotEmpty");
        if (Boolean.TRUE.equals(validations.get("notBlank"))) parts.add("@NotBlank");
        if (Boolean.TRUE.equals(validations.get("email"))) parts.add("@Email");

        @SuppressWarnings("unchecked")
        Map<String, Object> size = (Map<String, Object>) validations.get("size");
        if (size != null) {
            parts.add(String.format("@Size(%s-%s)", size.get("min"), size.get("max")));
        }

        String pattern = (String) validations.get("pattern");
        if (pattern != null) {
            parts.add("@Pattern");
        }

        return String.join(" ", parts);
    }

    /**
     * Exception Handlers 섹션 생성
     */
    private static String generateExceptionHandlersSection(List<Map<String, Object>> handlers) {
        StringBuilder html = new StringBuilder();
        html.append("    <div id=\"exceptions\" class=\"section\">\n");
        html.append("        <h2 class=\"section-title\">Exception Handlers</h2>\n");

        for (Map<String, Object> handler : handlers) {
            html.append("        <div class=\"exception-handler\">\n");

            @SuppressWarnings("unchecked")
            List<String> exceptionTypes = (List<String>) handler.get("exceptionTypes");
            html.append("            <div class=\"exception-types\">");
            html.append(String.join(", ", exceptionTypes));
            html.append("</div>\n");

            html.append(String.format("            <div><strong>Handler:</strong> <code>%s</code></div>\n", handler.get("handler")));
            html.append(String.format("            <div><strong>Type:</strong> %s</div>\n", handler.get("adviceType")));

            html.append("        </div>\n");
        }

        html.append("    </div>\n");
        return html.toString();
    }

    /**
     * HTML 푸터 생성
     */
    private static String generateHtmlFooter() {
        return """
    <div class="footer">
        Generated by <strong>SASA</strong> - Spring API Spec Analyzer
    </div>

    <script>
        function toggleEndpoint(index) {
            const body = document.getElementById('endpoint-' + index);
            const icon = document.getElementById('icon-' + index);

            if (body.classList.contains('active')) {
                body.classList.remove('active');
                icon.classList.remove('active');
            } else {
                body.classList.add('active');
                icon.classList.add('active');
            }
        }

        // Smooth scroll for navigation
        document.querySelectorAll('a[href^="#"]').forEach(anchor => {
            anchor.addEventListener('click', function (e) {
                e.preventDefault();
                const target = document.querySelector(this.getAttribute('href'));
                if (target) {
                    target.scrollIntoView({ behavior: 'smooth', block: 'start' });
                }
            });
        });
    </script>
</body>
</html>
""";
    }

    /**
     * HTML 이스케이프
     */
    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}