package io.github.bigdaditor.sasa;

import io.github.bigdaditor.sasa.SasaApplication;
import io.github.bigdaditor.sasa.SasaConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Map;

/**
 * SASA UI를 위한 API 스펙 제공 컨트롤러
 */
@RestController
public class SasaViewController {

    private final RequestMappingHandlerMapping handlerMapping;
    private final ApplicationContext applicationContext;
    private Map<String, Object> cachedApiSpec;

    public SasaViewController(RequestMappingHandlerMapping handlerMapping, ApplicationContext applicationContext) {
        this.handlerMapping = handlerMapping;
        this.applicationContext = applicationContext;
    }

    /**
     * 현재 애플리케이션의 API 스펙을 JSON으로 반환 (exception handler 포함)
     */
    @GetMapping("/sasa/api-spec")
    public Map<String, Object> getApiSpec() {
        // 캐시된 스펙이 없으면 생성
        if (cachedApiSpec == null) {
            SasaConfig config = SasaConfig.builder()
                    .enableConsoleOutput(false)
                    .enableFileOutput(false)
                    .build();
            cachedApiSpec = SasaApplication.extractApiSpec(handlerMapping, applicationContext, config);
        }
        return cachedApiSpec;
    }

    /**
     * API 스펙 캐시를 강제로 새로고침
     */
    @GetMapping("/sasa/api-spec/refresh")
    public Map<String, Object> refreshApiSpec() {
        cachedApiSpec = null;
        return getApiSpec();
    }
}