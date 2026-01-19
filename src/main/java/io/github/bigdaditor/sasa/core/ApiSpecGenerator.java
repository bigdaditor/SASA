package io.github.bigdaditor.sasa.core;

import io.github.bigdaditor.sasa.SasaConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Map;

/**
 * API 스펙 생성을 위한 핵심 인터페이스.
 *
 * <p>Spring MVC의 {@link RequestMappingHandlerMapping}에서 런타임 매핑 정보를 추출하여
 * API 스펙을 생성합니다. 기본 구현체는 {@link DefaultApiSpecGenerator}입니다.</p>
 *
 * <h2>사용 예시</h2>
 * <pre>{@code
 * ApiSpecGenerator generator = new DefaultApiSpecGenerator(config);
 *
 * // 스펙만 추출
 * Map<String, Object> spec = generator.generate(mapping, context);
 *
 * // 스펙 추출 후 파일로 저장
 * generator.generateAndOutput(mapping, context);
 * }</pre>
 *
 * <h2>커스텀 구현</h2>
 * <p>이 인터페이스를 구현하여 커스텀 API 스펙 생성 로직을 작성할 수 있습니다:</p>
 * <pre>{@code
 * public class CustomApiSpecGenerator implements ApiSpecGenerator {
 *     // 커스텀 구현
 * }
 * }</pre>
 *
 * @author bigdaditor
 * @see DefaultApiSpecGenerator
 * @see SasaConfig
 * @since 0.0.1
 */
public interface ApiSpecGenerator {

    /**
     * API 스펙을 추출합니다.
     *
     * <p>ApplicationContext 없이 기본 엔드포인트 정보만 추출합니다.
     * 예외 핸들러 정보가 필요한 경우 {@link #generate(RequestMappingHandlerMapping, ApplicationContext)}를
     * 사용하세요.</p>
     *
     * @param mapping Spring MVC RequestMappingHandlerMapping
     * @return API 스펙을 담은 Map (applicationName, version, generatedAt, endpoints 포함)
     */
    Map<String, Object> generate(RequestMappingHandlerMapping mapping);

    /**
     * API 스펙을 추출합니다 (예외 핸들러 정보 포함).
     *
     * <p>ApplicationContext를 통해 @ControllerAdvice로 정의된 예외 핸들러 정보도 함께 추출합니다.</p>
     *
     * @param mapping            Spring MVC RequestMappingHandlerMapping
     * @param applicationContext Spring ApplicationContext (예외 핸들러 추출용, null 가능)
     * @return API 스펙을 담은 Map (applicationName, version, generatedAt, endpoints, exceptionHandlers 포함)
     */
    Map<String, Object> generate(RequestMappingHandlerMapping mapping, ApplicationContext applicationContext);

    /**
     * API 스펙을 추출하고 설정된 경로에 JSON/HTML 파일로 저장합니다.
     *
     * <p>ApplicationContext 없이 기본 엔드포인트 정보만 추출합니다.</p>
     *
     * @param mapping Spring MVC RequestMappingHandlerMapping
     * @see SasaConfig#getOutputFilePath()
     */
    void generateAndOutput(RequestMappingHandlerMapping mapping);

    /**
     * API 스펙을 추출하고 설정된 경로에 JSON/HTML 파일로 저장합니다.
     *
     * <p>ApplicationContext를 통해 예외 핸들러 정보도 함께 추출합니다.
     * 출력 파일 경로는 {@link SasaConfig#getOutputFilePath()}로 설정됩니다.</p>
     *
     * @param mapping            Spring MVC RequestMappingHandlerMapping
     * @param applicationContext Spring ApplicationContext (예외 핸들러 추출용, null 가능)
     * @see SasaConfig#isEnableFileOutput()
     * @see SasaConfig#isEnableConsoleOutput()
     */
    void generateAndOutput(RequestMappingHandlerMapping mapping, ApplicationContext applicationContext);

    /**
     * 현재 설정을 반환합니다.
     *
     * @return SASA 설정 객체
     */
    SasaConfig getConfig();
}