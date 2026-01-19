package io.github.bigdaditor.sasa.extractor.api;

import java.util.Map;

/**
 * 타입 스키마 추출 인터페이스
 */
public interface TypeSchemaExtractor {

    /**
     * 클래스의 타입 스키마를 추출
     *
     * @param type 대상 클래스
     * @return 스키마 정보 맵 (fields, example 포함)
     */
    Map<String, Object> extractTypeSchema(Class<?> type);

    /**
     * 단순 타입인지 확인
     *
     * @param type 대상 클래스
     * @return 단순 타입 여부
     */
    boolean isSimpleType(Class<?> type);
}
