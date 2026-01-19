package io.github.bigdaditor.sasa.extractor.api;

import io.github.bigdaditor.sasa.extractor.impl.DefaultResponseExtractor;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * 메서드 응답 타입 정보를 추출하는 인터페이스.
 *
 * <p>Spring MVC 핸들러 메서드의 반환 타입을 분석하여 응답 정보를 추출합니다.
 * 제네릭 타입({@code ResponseEntity<T>}, {@code List<T>} 등)도 분석합니다.</p>
 *
 * <h2>추출 정보</h2>
 * <ul>
 *   <li>type - 반환 타입 (간단한 이름)</li>
 *   <li>fullType - 반환 타입 (전체 패키지 경로)</li>
 *   <li>genericType - 제네릭 타입 파라미터 (있는 경우)</li>
 *   <li>genericFullType - 제네릭 타입 파라미터 전체 경로</li>
 *   <li>elementType - 컬렉션 요소 타입 (List 등인 경우)</li>
 *   <li>schema - DTO인 경우 필드 스키마</li>
 * </ul>
 *
 * <h2>지원하는 타입 패턴</h2>
 * <ul>
 *   <li>단순 타입: {@code String}, {@code Integer}, {@code UserDto}</li>
 *   <li>ResponseEntity: {@code ResponseEntity<UserDto>}</li>
 *   <li>컬렉션: {@code List<UserDto>}, {@code Set<String>}</li>
 *   <li>중첩 제네릭: {@code ResponseEntity<List<UserDto>>}</li>
 * </ul>
 *
 * <h2>사용 예시</h2>
 * <pre>{@code
 * ResponseExtractor extractor = new DefaultResponseExtractor();
 * Method method = UserController.class.getMethod("getUser", Long.class);
 * Map<String, Object> response = extractor.extractResponseInfo(method);
 * }</pre>
 *
 * @author bigdaditor
 * @see DefaultResponseExtractor
 * @since 0.0.1
 */
public interface ResponseExtractor {

    /**
     * 메서드의 응답 정보를 추출합니다.
     *
     * <p>제네릭 타입 정보를 포함하여 상세한 응답 타입 정보를 반환합니다.
     * DTO 타입인 경우 스키마 정보도 함께 추출됩니다.</p>
     *
     * @param method 대상 메서드
     * @return 응답 정보 맵 (type, fullType, genericType, schema 등 포함)
     */
    Map<String, Object> extractResponseInfo(Method method);

    /**
     * 단순 클래스 타입의 응답 정보를 추출합니다.
     *
     * <p>제네릭 정보 없이 클래스 타입만으로 기본 응답 정보를 추출합니다.</p>
     *
     * @param returnType 반환 타입 클래스
     * @return 응답 정보 맵 (type, fullType, schema 포함)
     */
    Map<String, Object> extractSimpleResponseInfo(Class<?> returnType);
}