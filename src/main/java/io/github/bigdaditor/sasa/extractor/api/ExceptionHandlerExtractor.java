package io.github.bigdaditor.sasa.extractor.api;

import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;

/**
 * Exception Handler 정보 추출 인터페이스
 */
public interface ExceptionHandlerExtractor {

    /**
     * ApplicationContext에서 exception handler 정보를 추출
     *
     * @param applicationContext Spring ApplicationContext
     * @return exception handler 정보 리스트
     */
    List<Map<String, Object>> extract(ApplicationContext applicationContext);
}
