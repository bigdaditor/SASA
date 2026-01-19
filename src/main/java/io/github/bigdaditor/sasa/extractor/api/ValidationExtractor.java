package io.github.bigdaditor.sasa.extractor.api;

import io.github.bigdaditor.sasa.extractor.impl.DefaultValidationExtractor;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Bean Validation 어노테이션 정보를 추출하는 인터페이스.
 *
 * <p>Jakarta Bean Validation API의 어노테이션을 분석하여 검증 규칙 정보를 추출합니다.</p>
 *
 * <h2>지원하는 어노테이션</h2>
 * <ul>
 *   <li>{@code @NotNull} - null 불가</li>
 *   <li>{@code @NotEmpty} - 빈 값 불가 (문자열, 컬렉션)</li>
 *   <li>{@code @NotBlank} - 공백 문자열 불가</li>
 *   <li>{@code @Size(min, max)} - 크기 제한</li>
 *   <li>{@code @Min}, {@code @Max} - 숫자 범위</li>
 *   <li>{@code @Email} - 이메일 형식</li>
 *   <li>{@code @Pattern} - 정규식 패턴</li>
 *   <li>{@code @Positive}, {@code @Negative} - 양수/음수</li>
 *   <li>{@code @Past}, {@code @Future} - 날짜 제약</li>
 *   <li>{@code @Digits} - 숫자 자릿수</li>
 *   <li>{@code @DecimalMin}, {@code @DecimalMax} - 소수점 범위</li>
 * </ul>
 *
 * <h2>사용 예시</h2>
 * <pre>{@code
 * ValidationExtractor extractor = new DefaultValidationExtractor();
 * Field field = UserDto.class.getDeclaredField("email");
 * Map<String, Object> validations = extractor.extract(field);
 *
 * // 결과 예시:
 * // {
 * //   "notBlank": true,
 * //   "email": true,
 * //   "size": { "min": 0, "max": 100 }
 * // }
 * }</pre>
 *
 * @author bigdaditor
 * @see DefaultValidationExtractor
 * @since 0.0.1
 */
public interface ValidationExtractor {

    /**
     * 필드의 검증 어노테이션 정보를 추출합니다.
     *
     * @param field 대상 필드
     * @return 검증 정보 맵. 검증 어노테이션이 없으면 빈 맵 반환
     */
    Map<String, Object> extract(Field field);
}