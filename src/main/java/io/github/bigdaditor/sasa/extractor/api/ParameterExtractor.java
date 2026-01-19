package io.github.bigdaditor.sasa.extractor.api;

import io.github.bigdaditor.sasa.extractor.impl.DefaultParameterExtractor;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * 메서드 파라미터 정보를 추출하는 인터페이스.
 *
 * <p>Spring MVC 핸들러 메서드의 파라미터에서 다음 어노테이션들을 인식하여 정보를 추출합니다:</p>
 * <ul>
 *   <li>{@code @RequestParam} - 쿼리 파라미터</li>
 *   <li>{@code @PathVariable} - 경로 변수</li>
 *   <li>{@code @RequestHeader} - HTTP 헤더</li>
 *   <li>{@code @RequestBody} - 요청 본문</li>
 * </ul>
 *
 * <h2>추출 정보</h2>
 * <ul>
 *   <li>parameterType - 파라미터 유형 (REQUEST_PARAM, PATH_VARIABLE, REQUEST_HEADER, REQUEST_BODY, OTHER)</li>
 *   <li>paramName - 파라미터 이름</li>
 *   <li>type - 파라미터 타입 (간단한 이름)</li>
 *   <li>fullType - 파라미터 타입 (전체 패키지 경로)</li>
 *   <li>required - 필수 여부</li>
 *   <li>defaultValue - 기본값 (있는 경우)</li>
 *   <li>schema - DTO인 경우 필드 스키마</li>
 * </ul>
 *
 * <h2>사용 예시</h2>
 * <pre>{@code
 * ParameterExtractor extractor = new DefaultParameterExtractor();
 * Method method = UserController.class.getMethod("getUser", Long.class);
 * List<Map<String, Object>> params = extractor.extract(method);
 * }</pre>
 *
 * @author bigdaditor
 * @see DefaultParameterExtractor
 * @since 0.0.1
 */
public interface ParameterExtractor {

    /**
     * 메서드의 파라미터 정보를 추출합니다.
     *
     * @param method 대상 메서드
     * @return 파라미터 정보 리스트. 각 파라미터는 Map 형태로 parameterType, paramName, type, required 등을 포함
     */
    List<Map<String, Object>> extract(Method method);
}