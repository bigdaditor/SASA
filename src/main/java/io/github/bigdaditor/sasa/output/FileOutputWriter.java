package io.github.bigdaditor.sasa.output;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 파일 출력 작성기
 */
public class FileOutputWriter implements OutputWriter {

    @Override
    public void write(String content, String filePath) {
        try {
            Path path = Paths.get(filePath);
            ensureDirectoryExists(path);
            Files.write(path, content.getBytes(StandardCharsets.UTF_8));
            System.out.println("Saved to: " + path.toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to write to file: " + filePath, e);
        }
    }

    private void ensureDirectoryExists(Path path) throws IOException {
        Path parent = path.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
    }
}
