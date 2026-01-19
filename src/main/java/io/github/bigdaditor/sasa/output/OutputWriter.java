package io.github.bigdaditor.sasa.output;

/**
 * 출력 작성 인터페이스
 */
public interface OutputWriter {

    /**
     * 콘텐츠를 지정된 경로에 저장
     *
     * @param content  저장할 콘텐츠
     * @param filePath 파일 경로
     */
    void write(String content, String filePath);
}
