package io.github.bigdaditor.sasa.extractor.api;

import io.github.bigdaditor.sasa.annotation.ApiDescription;
import io.github.bigdaditor.sasa.extractor.impl.DefaultDescriptionExtractor;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * {@link ApiDescription} 어노테이션 정보를 추출하는 인터페이스.
 *
 * <p>컨트롤러 메서드와 클래스에서 {@link ApiDescription} 어노테이션을 읽어
 * API 설명 정보를 추출합니다.</p>
 *
 * <h2>우선순위</h2>
 * <ol>
 *   <li>메서드 레벨 {@code @ApiDescription}</li>
 *   <li>클래스 레벨 {@code @ApiDescription}</li>
 * </ol>
 *
 * <h2>자동 요약 생성</h2>
 * <p>{@code summary}가 지정되지 않은 경우, {@code value}의 첫 문장을 자동으로 추출합니다.
 * 문장은 마침표(.), 물음표(?), 느낌표(!)로 구분됩니다.</p>
 *
 * <h2>사용 예시</h2>
 * <pre>{@code
 * DescriptionExtractor extractor = new DefaultDescriptionExtractor();
 * Method method = UserController.class.getMethod("getUser", Long.class);
 * Map<String, Object> desc = extractor.extract(method, UserController.class);
 *
 * // 결과 예시:
 * // {
 * //   "description": "사용자 정보를 조회합니다. ID로 특정 사용자를 찾습니다.",
 * //   "summary": "사용자 정보를 조회합니다."
 * // }
 * }</pre>
 *
 * @author bigdaditor
 * @see ApiDescription
 * @see DefaultDescriptionExtractor
 * @since 0.0.1
 */
public interface DescriptionExtractor {

    /**
     * 메서드에서 {@link ApiDescription} 정보를 추출합니다.
     *
     * <p>메서드 레벨 어노테이션이 클래스 레벨보다 우선합니다.
     * 어노테이션이 없거나 값이 비어있으면 빈 맵을 반환합니다.</p>
     *
     * @param method   핸들러 메서드
     * @param beanType 컨트롤러 클래스
     * @return description 정보 맵 (description, summary 포함). 어노테이션이 없으면 빈 맵
     */
    Map<String, Object> extract(Method method, Class<?> beanType);
}