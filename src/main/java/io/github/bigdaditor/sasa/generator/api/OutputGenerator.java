package io.github.bigdaditor.sasa.generator.api;

import java.util.Map;

/**
 * API Spec 출력 생성 인터페이스
 */
public interface OutputGenerator {

    /**
     * API Spec을 문자열로 변환
     *
     * @param apiSpec API 스펙 맵
     * @return 변환된 문자열 (JSON, HTML 등)
     */
    String generate(Map<String, Object> apiSpec);

    /**
     * 출력 파일 확장자
     *
     * @return 확장자 (예: ".json", ".html")
     */
    String getFileExtension();

    /**
     * Content-Type
     *
     * @return MIME 타입
     */
    String getContentType();
}
