package io.github.bigdaditor.sasa.extractor;

import io.github.bigdaditor.sasa.dto.UserDTO;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ResponseExtractorTest {

    // Test methods for extractResponseInfo

    @Test
    void testExtractSimpleStringResponse() throws Exception {
        Method method = TestController.class.getMethod("getString");
        Map<String, Object> response = ResponseExtractor.extractResponseInfo(method);

        assertEquals("String", response.get("type"));
        assertEquals("java.lang.String", response.get("fullType"));
        assertNull(response.get("genericType"));
    }

    @Test
    void testExtractIntegerResponse() throws Exception {
        Method method = TestController.class.getMethod("getInteger");
        Map<String, Object> response = ResponseExtractor.extractResponseInfo(method);

        assertEquals("Integer", response.get("type"));
        assertEquals("java.lang.Integer", response.get("fullType"));
    }

    @Test
    void testExtractVoidResponse() throws Exception {
        Method method = TestController.class.getMethod("voidMethod");
        Map<String, Object> response = ResponseExtractor.extractResponseInfo(method);

        assertEquals("void", response.get("type"));
        assertEquals("void", response.get("fullType"));
    }

    @Test
    void testExtractListOfStringResponse() throws Exception {
        Method method = TestController.class.getMethod("getListOfStrings");
        Map<String, Object> response = ResponseExtractor.extractResponseInfo(method);

        assertEquals("List", response.get("type"));
        assertEquals("java.util.List", response.get("fullType"));
        // For raw List<String>, there's no schema extraction
    }

    @Test
    void testExtractResponseEntityOfString() throws Exception {
        Method method = TestController.class.getMethod("getResponseEntityOfString");
        Map<String, Object> response = ResponseExtractor.extractResponseInfo(method);

        assertEquals("ResponseEntity", response.get("type"));
        assertEquals("org.springframework.http.ResponseEntity", response.get("fullType"));
        assertEquals("String", response.get("genericType"));
        assertEquals("java.lang.String", response.get("genericFullType"));
    }

    @Test
    void testExtractResponseEntityOfUserDTO() throws Exception {
        Method method = TestController.class.getMethod("getResponseEntityOfUserDTO");
        Map<String, Object> response = ResponseExtractor.extractResponseInfo(method);

        assertEquals("ResponseEntity", response.get("type"));
        assertEquals("org.springframework.http.ResponseEntity", response.get("fullType"));
        assertEquals("UserDTO", response.get("genericType"));
        assertEquals("io.github.bigdaditor.sasa.dto.UserDTO", response.get("genericFullType"));

        // Verify schema extraction
        assertNotNull(response.get("schema"));
        @SuppressWarnings("unchecked")
        Map<String, Object> schema = (Map<String, Object>) response.get("schema");
        assertFalse(schema.isEmpty());
    }

    @Test
    void testExtractResponseEntityOfListOfUserDTO() throws Exception {
        Method method = TestController.class.getMethod("getResponseEntityOfListOfUserDTO");
        Map<String, Object> response = ResponseExtractor.extractResponseInfo(method);

        assertEquals("ResponseEntity", response.get("type"));
        assertEquals("List", response.get("genericType"));
        assertEquals("UserDTO", response.get("elementType"));
        assertEquals("io.github.bigdaditor.sasa.dto.UserDTO", response.get("elementFullType"));

        // Verify schema extraction for element type
        assertNotNull(response.get("schema"));
        @SuppressWarnings("unchecked")
        Map<String, Object> schema = (Map<String, Object>) response.get("schema");
        assertFalse(schema.isEmpty());
    }

    @Test
    void testExtractUserDTOResponse() throws Exception {
        Method method = TestController.class.getMethod("getUserDTO");
        Map<String, Object> response = ResponseExtractor.extractResponseInfo(method);

        assertEquals("UserDTO", response.get("type"));
        assertEquals("io.github.bigdaditor.sasa.dto.UserDTO", response.get("fullType"));

        // DTO should have schema
        assertNotNull(response.get("schema"));
        @SuppressWarnings("unchecked")
        Map<String, Object> schema = (Map<String, Object>) response.get("schema");
        assertTrue(schema.containsKey("fields"), "Schema should contain fields");
    }

    @Test
    void testExtractListOfUserDTOResponse() throws Exception {
        Method method = TestController.class.getMethod("getListOfUserDTO");
        Map<String, Object> response = ResponseExtractor.extractResponseInfo(method);

        assertEquals("List", response.get("type"));
        // Note: For direct List<UserDTO> without ResponseEntity,
        // the extraction behavior depends on runtime type information
    }

    // Test methods for extractSimpleResponseInfo

    @Test
    void testExtractSimpleResponseInfoForString() {
        Map<String, Object> response = ResponseExtractor.extractSimpleResponseInfo(String.class);

        assertEquals("String", response.get("type"));
        assertEquals("java.lang.String", response.get("fullType"));
        assertNull(response.get("schema"), "Simple types should not have schema");
    }

    @Test
    void testExtractSimpleResponseInfoForInteger() {
        Map<String, Object> response = ResponseExtractor.extractSimpleResponseInfo(Integer.class);

        assertEquals("Integer", response.get("type"));
        assertEquals("java.lang.Integer", response.get("fullType"));
        assertNull(response.get("schema"));
    }

    @Test
    void testExtractSimpleResponseInfoForUserDTO() {
        Map<String, Object> response = ResponseExtractor.extractSimpleResponseInfo(UserDTO.class);

        assertEquals("UserDTO", response.get("type"));
        assertEquals("io.github.bigdaditor.sasa.dto.UserDTO", response.get("fullType"));

        // DTO should have schema
        assertNotNull(response.get("schema"));
        @SuppressWarnings("unchecked")
        Map<String, Object> schema = (Map<String, Object>) response.get("schema");
        assertFalse(schema.isEmpty());
    }

    @Test
    void testExtractSimpleResponseInfoForResponseEntity() {
        Map<String, Object> response = ResponseExtractor.extractSimpleResponseInfo(ResponseEntity.class);

        assertEquals("ResponseEntity", response.get("type"));
        assertEquals("org.springframework.http.ResponseEntity", response.get("fullType"));
        // ResponseEntity is a Spring framework class, so no schema
        assertNull(response.get("schema"));
    }

    @Test
    void testExtractSimpleResponseInfoForVoid() {
        Map<String, Object> response = ResponseExtractor.extractSimpleResponseInfo(void.class);

        assertEquals("void", response.get("type"));
        assertEquals("void", response.get("fullType"));
    }

    // Test controller class with various response types
    @SuppressWarnings("unused")
    static class TestController {
        public String getString() {
            return "test";
        }

        public Integer getInteger() {
            return 42;
        }

        public void voidMethod() {
        }

        public List<String> getListOfStrings() {
            return List.of("a", "b");
        }

        public ResponseEntity<String> getResponseEntityOfString() {
            return ResponseEntity.ok("test");
        }

        public ResponseEntity<UserDTO> getResponseEntityOfUserDTO() {
            return ResponseEntity.ok(new UserDTO());
        }

        public ResponseEntity<List<UserDTO>> getResponseEntityOfListOfUserDTO() {
            return ResponseEntity.ok(List.of(new UserDTO()));
        }

        public UserDTO getUserDTO() {
            return new UserDTO();
        }

        public List<UserDTO> getListOfUserDTO() {
            return List.of(new UserDTO());
        }
    }
}
