package io.github.bigdaditor.sasa.extractor.impl;

import io.github.bigdaditor.sasa.annotation.ApiDescription;
import io.github.bigdaditor.sasa.extractor.api.DescriptionExtractor;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * API Description 추출 기본 구현
 */
public class DefaultDescriptionExtractor implements DescriptionExtractor {

    @Override
    public Map<String, Object> extract(Method method, Class<?> beanType) {
        Map<String, Object> result = new LinkedHashMap<>();

        // 1. 메서드 레벨 어노테이션 우선
        ApiDescription methodAnnotation = method.getAnnotation(ApiDescription.class);
        if (methodAnnotation != null) {
            populateDescription(result, methodAnnotation);
            return result;
        }

        // 2. 클래스 레벨 어노테이션
        ApiDescription classAnnotation = beanType.getAnnotation(ApiDescription.class);
        if (classAnnotation != null) {
            populateDescription(result, classAnnotation);
        }

        return result;
    }

    private void populateDescription(Map<String, Object> result, ApiDescription annotation) {
        String value = annotation.value();
        String summary = annotation.summary();

        if (!value.isEmpty()) {
            result.put("description", value);
        }

        if (!summary.isEmpty()) {
            result.put("summary", summary);
        } else if (!value.isEmpty()) {
            result.put("summary", extractFirstSentence(value));
        }
    }

    private String extractFirstSentence(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        int endIndex = -1;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '.' || c == '?' || c == '!') {
                endIndex = i + 1;
                break;
            }
        }

        if (endIndex > 0 && endIndex < text.length()) {
            return text.substring(0, endIndex).trim();
        }

        return text.length() > 100 ? text.substring(0, 100) + "..." : text;
    }
}
