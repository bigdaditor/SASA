package io.github.bigdaditor.sasa.extractor;

import io.github.bigdaditor.sasa.annotation.ApiDescription;
import io.github.bigdaditor.sasa.extractor.api.DescriptionExtractor;
import io.github.bigdaditor.sasa.extractor.impl.DefaultDescriptionExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DescriptionExtractorTest {

    private DescriptionExtractor descriptionExtractor;

    @BeforeEach
    void setUp() {
        descriptionExtractor = new DefaultDescriptionExtractor();
    }

    @Test
    void testExtractMethodLevelDescription() throws Exception {
        Method method = TestController.class.getMethod("methodWithDescription");
        Map<String, Object> result = descriptionExtractor.extract(method, TestController.class);

        assertEquals("Method description", result.get("description"));
        assertEquals("Method description", result.get("summary"));
    }

    @Test
    void testExtractClassLevelDescription() throws Exception {
        Method method = ClassWithDescription.class.getMethod("methodWithoutDescription");
        Map<String, Object> result = descriptionExtractor.extract(method, ClassWithDescription.class);

        assertEquals("Class level description", result.get("description"));
        assertEquals("Class level description", result.get("summary"));
    }

    @Test
    void testMethodLevelOverridesClassLevel() throws Exception {
        Method method = ClassWithDescription.class.getMethod("methodWithOwnDescription");
        Map<String, Object> result = descriptionExtractor.extract(method, ClassWithDescription.class);

        assertEquals("Method overrides class", result.get("description"));
        assertEquals("Method overrides class", result.get("summary"));
    }

    @Test
    void testExtractWithValueAndSummary() throws Exception {
        Method method = TestController.class.getMethod("methodWithValueAndSummary");
        Map<String, Object> result = descriptionExtractor.extract(method, TestController.class);

        assertEquals("Full description here", result.get("description"));
        assertEquals("Short summary", result.get("summary"));
    }

    @Test
    void testExtractNoAnnotation() throws Exception {
        Method method = TestController.class.getMethod("methodWithoutAnnotation");
        Map<String, Object> result = descriptionExtractor.extract(method, TestController.class);

        assertTrue(result.isEmpty(), "Should return empty map when no annotation");
    }

    @Test
    void testExtractEmptyAnnotation() throws Exception {
        Method method = TestController.class.getMethod("methodWithEmptyAnnotation");
        Map<String, Object> result = descriptionExtractor.extract(method, TestController.class);

        assertTrue(result.isEmpty(), "Should return empty map when annotation has no values");
    }

    @Test
    void testFirstSentenceExtraction() throws Exception {
        Method method = TestController.class.getMethod("methodWithMultipleSentences");
        Map<String, Object> result = descriptionExtractor.extract(method, TestController.class);

        assertEquals("First sentence. Second sentence. Third sentence.", result.get("description"));
        assertEquals("First sentence.", result.get("summary"));
    }

    @Test
    void testFirstSentenceExtractionWithQuestionMark() throws Exception {
        Method method = TestController.class.getMethod("methodWithQuestion");
        Map<String, Object> result = descriptionExtractor.extract(method, TestController.class);

        assertEquals("Is this a question? Yes it is.", result.get("description"));
        assertEquals("Is this a question?", result.get("summary"));
    }

    @Test
    void testLongDescriptionWithoutSentenceEnding() throws Exception {
        Method method = TestController.class.getMethod("methodWithLongDescription");
        Map<String, Object> result = descriptionExtractor.extract(method, TestController.class);

        String summary = (String) result.get("summary");
        assertTrue(summary.length() <= 103, "Summary should be truncated");
        assertTrue(summary.endsWith("..."), "Should end with ellipsis");
    }

    // Test classes with various annotation configurations

    @SuppressWarnings("unused")
    static class TestController {

        @ApiDescription("Method description")
        public void methodWithDescription() {
        }

        @ApiDescription(value = "Full description here", summary = "Short summary")
        public void methodWithValueAndSummary() {
        }

        public void methodWithoutAnnotation() {
        }

        @ApiDescription
        public void methodWithEmptyAnnotation() {
        }

        @ApiDescription("First sentence. Second sentence. Third sentence.")
        public void methodWithMultipleSentences() {
        }

        @ApiDescription("Is this a question? Yes it is.")
        public void methodWithQuestion() {
        }

        @ApiDescription("This is a very long description without any sentence ending that goes on and on and on and on and on and on and on")
        public void methodWithLongDescription() {
        }
    }

    @ApiDescription("Class level description")
    @SuppressWarnings("unused")
    static class ClassWithDescription {

        public void methodWithoutDescription() {
        }

        @ApiDescription("Method overrides class")
        public void methodWithOwnDescription() {
        }
    }
}