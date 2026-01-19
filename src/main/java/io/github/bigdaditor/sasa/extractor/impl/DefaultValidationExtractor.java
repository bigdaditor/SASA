package io.github.bigdaditor.sasa.extractor.impl;

import io.github.bigdaditor.sasa.extractor.api.ValidationExtractor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Validation 어노테이션 정보 추출 기본 구현
 */
public class DefaultValidationExtractor implements ValidationExtractor {

    @Override
    public Map<String, Object> extract(Field field) {
        Map<String, Object> validations = new LinkedHashMap<>();
        Annotation[] annotations = field.getAnnotations();

        for (Annotation annotation : annotations) {
            String annotationType = annotation.annotationType().getSimpleName();

            try {
                switch (annotationType) {
                    case "NotNull":
                        validations.put("notNull", true);
                        extractMessage(annotation, validations);
                        break;

                    case "NotEmpty":
                        validations.put("notEmpty", true);
                        extractMessage(annotation, validations);
                        break;

                    case "NotBlank":
                        validations.put("notBlank", true);
                        extractMessage(annotation, validations);
                        break;

                    case "Size":
                        Map<String, Object> size = new LinkedHashMap<>();
                        size.put("min", getAnnotationValue(annotation, "min", 0));
                        size.put("max", getAnnotationValue(annotation, "max", Integer.MAX_VALUE));
                        validations.put("size", size);
                        extractMessage(annotation, validations);
                        break;

                    case "Min":
                        validations.put("min", getAnnotationValue(annotation, "value", 0L));
                        extractMessage(annotation, validations);
                        break;

                    case "Max":
                        validations.put("max", getAnnotationValue(annotation, "value", Long.MAX_VALUE));
                        extractMessage(annotation, validations);
                        break;

                    case "Email":
                        validations.put("email", true);
                        extractMessage(annotation, validations);
                        break;

                    case "Pattern":
                        String regex = (String) getAnnotationValue(annotation, "regexp", "");
                        if (!regex.isEmpty()) {
                            validations.put("pattern", regex);
                        }
                        extractMessage(annotation, validations);
                        break;

                    case "Positive":
                        validations.put("positive", true);
                        extractMessage(annotation, validations);
                        break;

                    case "PositiveOrZero":
                        validations.put("positiveOrZero", true);
                        extractMessage(annotation, validations);
                        break;

                    case "Negative":
                        validations.put("negative", true);
                        extractMessage(annotation, validations);
                        break;

                    case "NegativeOrZero":
                        validations.put("negativeOrZero", true);
                        extractMessage(annotation, validations);
                        break;

                    case "Past":
                        validations.put("past", true);
                        extractMessage(annotation, validations);
                        break;

                    case "PastOrPresent":
                        validations.put("pastOrPresent", true);
                        extractMessage(annotation, validations);
                        break;

                    case "Future":
                        validations.put("future", true);
                        extractMessage(annotation, validations);
                        break;

                    case "FutureOrPresent":
                        validations.put("futureOrPresent", true);
                        extractMessage(annotation, validations);
                        break;

                    case "DecimalMin":
                        validations.put("decimalMin", getAnnotationValue(annotation, "value", "0"));
                        validations.put("decimalMinInclusive", getAnnotationValue(annotation, "inclusive", true));
                        extractMessage(annotation, validations);
                        break;

                    case "DecimalMax":
                        validations.put("decimalMax", getAnnotationValue(annotation, "value", "0"));
                        validations.put("decimalMaxInclusive", getAnnotationValue(annotation, "inclusive", true));
                        extractMessage(annotation, validations);
                        break;

                    case "Digits":
                        Map<String, Object> digits = new LinkedHashMap<>();
                        digits.put("integer", getAnnotationValue(annotation, "integer", 0));
                        digits.put("fraction", getAnnotationValue(annotation, "fraction", 0));
                        validations.put("digits", digits);
                        extractMessage(annotation, validations);
                        break;
                }
            } catch (Exception e) {
                // Validation annotation 추출 실패 시 무시
            }
        }

        return validations;
    }

    private void extractMessage(Annotation annotation, Map<String, Object> validations) {
        try {
            String message = (String) getAnnotationValue(annotation, "message", "");
            if (!message.isEmpty() && !message.startsWith("{")) {
                validations.put("message", message);
            }
        } catch (Exception e) {
            // message 추출 실패 시 무시
        }
    }

    private Object getAnnotationValue(Annotation annotation, String attribute, Object defaultValue) {
        try {
            Method method = annotation.annotationType().getDeclaredMethod(attribute);
            Object value = method.invoke(annotation);
            return value != null ? value : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
