package io.github.bigdaditor.sasa.extractor.api;

import io.github.bigdaditor.sasa.SasaConfig;
import io.github.bigdaditor.sasa.extractor.impl.DefaultEndpointExtractor;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.List;
import java.util.Map;

/**
 * Spring MVC 엔드포인트 정보를 추출하는 인터페이스.
 *
 * <p>런타임에 등록된 {@link RequestMappingHandlerMapping}에서 모든 엔드포인트의
 * 경로, HTTP 메서드, 파라미터, 응답 타입 등의 정보를 추출합니다.</p>
 *
 * <h2>추출 정보</h2>
 * <ul>
 *   <li>paths - 엔드포인트 경로 목록</li>
 *   <li>methods - HTTP 메서드 목록 (GET, POST, PUT, DELETE 등)</li>
 *   <li>handler - 핸들러 메서드 정보 (Controller#methodName)</li>
 *   <li>consumes - 요청 Content-Type</li>
 *   <li>produces - 응답 Content-Type</li>
 *   <li>parameters - 파라미터 정보 (ParameterExtractor로 추출)</li>
 *   <li>response - 응답 타입 정보 (ResponseExtractor로 추출)</li>
 *   <li>description - API 설명 (DescriptionExtractor로 추출)</li>
 * </ul>
 *
 * <h2>사용 예시</h2>
 * <pre>{@code
 * EndpointExtractor extractor = new DefaultEndpointExtractor();
 * List<Map<String, Object>> endpoints = extractor.extract(mapping, config);
 *
 * for (Map<String, Object> endpoint : endpoints) {
 *     System.out.println(endpoint.get("paths"));
 *     System.out.println(endpoint.get("methods"));
 * }
 * }</pre>
 *
 * @author bigdaditor
 * @see DefaultEndpointExtractor
 * @see ParameterExtractor
 * @see ResponseExtractor
 * @since 0.0.1
 */
public interface EndpointExtractor {

    /**
     * 모든 엔드포인트 정보를 추출합니다.
     *
     * <p>설정에 따라 특정 경로나 HTTP 메서드를 필터링할 수 있습니다.</p>
     *
     * @param mapping Spring MVC RequestMappingHandlerMapping
     * @param config  SASA 설정 (필터링 조건 포함)
     * @return 엔드포인트 정보 리스트. 각 엔드포인트는 Map 형태로 paths, methods, handler 등의 정보를 포함
     * @see SasaConfig#getIncludePaths()
     * @see SasaConfig#getExcludePaths()
     */
    List<Map<String, Object>> extract(RequestMappingHandlerMapping mapping, SasaConfig config);
}