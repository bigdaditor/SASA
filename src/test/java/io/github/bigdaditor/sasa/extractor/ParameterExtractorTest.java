package io.github.bigdaditor.sasa.extractor;

import io.github.bigdaditor.sasa.dto.UserDTO;
import io.github.bigdaditor.sasa.extractor.api.ParameterExtractor;
import io.github.bigdaditor.sasa.extractor.impl.DefaultParameterExtractor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ParameterExtractorTest {

    private ParameterExtractor parameterExtractor;

    @BeforeEach
    void setUp() {
        parameterExtractor = new DefaultParameterExtractor();
    }

    @Test
    void testExtractRequestBodyParameter() throws Exception {
        Method method = TestController.class.getMethod("createUser", UserDTO.class);
        List<Map<String, Object>> parameters = parameterExtractor.extract(method);

        assertEquals(1, parameters.size());
        Map<String, Object> param = parameters.get(0);
        assertEquals("REQUEST_BODY", param.get("parameterType"));
        assertEquals("UserDTO", param.get("type"));
        assertEquals("io.github.bigdaditor.sasa.dto.UserDTO", param.get("fullType"));
        assertTrue((Boolean) param.get("required"));
        assertNotNull(param.get("schema"), "Should have schema for DTO");
    }

    @Test
    void testExtractRequestParamWithNameAndDefaultValue() throws Exception {
        Method method = TestController.class.getMethod("searchUsers", String.class, Integer.class);
        List<Map<String, Object>> parameters = parameterExtractor.extract(method);

        assertEquals(2, parameters.size());

        // First parameter: query
        Map<String, Object> queryParam = parameters.get(0);
        assertEquals("REQUEST_PARAM", queryParam.get("parameterType"));
        assertEquals("query", queryParam.get("paramName"));
        assertEquals("String", queryParam.get("type"));
        assertTrue((Boolean) queryParam.get("required"));

        // Second parameter: page with default value
        Map<String, Object> pageParam = parameters.get(1);
        assertEquals("REQUEST_PARAM", pageParam.get("parameterType"));
        assertEquals("page", pageParam.get("paramName"));
        assertEquals("Integer", pageParam.get("type"));
        assertFalse((Boolean) pageParam.get("required"));
        assertEquals("0", pageParam.get("defaultValue"));
    }

    @Test
    void testExtractPathVariableParameter() throws Exception {
        Method method = TestController.class.getMethod("getUser", Long.class);
        List<Map<String, Object>> parameters = parameterExtractor.extract(method);

        assertEquals(1, parameters.size());
        Map<String, Object> param = parameters.get(0);
        assertEquals("PATH_VARIABLE", param.get("parameterType"));
        assertEquals("id", param.get("paramName"));
        assertEquals("Long", param.get("type"));
        assertTrue((Boolean) param.get("required"));
    }

    @Test
    void testExtractRequestHeaderParameter() throws Exception {
        Method method = TestController.class.getMethod("getWithAuth", String.class);
        List<Map<String, Object>> parameters = parameterExtractor.extract(method);

        assertEquals(1, parameters.size());
        Map<String, Object> param = parameters.get(0);
        assertEquals("REQUEST_HEADER", param.get("parameterType"));
        assertEquals("Authorization", param.get("paramName"));
        assertEquals("String", param.get("type"));
        assertTrue((Boolean) param.get("required"));
    }

    @Test
    void testExtractRequestHeaderWithDefaultValue() throws Exception {
        Method method = TestController.class.getMethod("getWithUserAgent", String.class);
        List<Map<String, Object>> parameters = parameterExtractor.extract(method);

        assertEquals(1, parameters.size());
        Map<String, Object> param = parameters.get(0);
        assertEquals("REQUEST_HEADER", param.get("parameterType"));
        assertEquals("User-Agent", param.get("paramName"));
        assertFalse((Boolean) param.get("required"));
        assertEquals("Unknown", param.get("defaultValue"));
    }

    @Test
    void testExtractOtherTypeParameters() throws Exception {
        Method method = TestController.class.getMethod("getWithContext", HttpServletRequest.class, HttpSession.class);
        List<Map<String, Object>> parameters = parameterExtractor.extract(method);

        assertEquals(2, parameters.size());

        Map<String, Object> requestParam = parameters.get(0);
        assertEquals("OTHER", requestParam.get("parameterType"));
        assertEquals("HttpServletRequest", requestParam.get("type"));

        Map<String, Object> sessionParam = parameters.get(1);
        assertEquals("OTHER", sessionParam.get("parameterType"));
        assertEquals("HttpSession", sessionParam.get("type"));
    }

    @Test
    void testExtractMixedParameters() throws Exception {
        Method method = TestController.class.getMethod("updateUser", Long.class, UserDTO.class, String.class);
        List<Map<String, Object>> parameters = parameterExtractor.extract(method);

        assertEquals(3, parameters.size());

        // PATH_VARIABLE
        Map<String, Object> idParam = parameters.get(0);
        assertEquals("PATH_VARIABLE", idParam.get("parameterType"));
        assertEquals("id", idParam.get("paramName"));

        // REQUEST_BODY
        Map<String, Object> bodyParam = parameters.get(1);
        assertEquals("REQUEST_BODY", bodyParam.get("parameterType"));
        assertNotNull(bodyParam.get("schema"));

        // REQUEST_HEADER
        Map<String, Object> headerParam = parameters.get(2);
        assertEquals("REQUEST_HEADER", headerParam.get("parameterType"));
    }

    @Test
    void testExtractNoParameters() throws Exception {
        Method method = TestController.class.getMethod("getAllUsers");
        List<Map<String, Object>> parameters = parameterExtractor.extract(method);

        assertTrue(parameters.isEmpty(), "Should return empty list for methods with no parameters");
    }

    @Test
    void testRequestParamWithoutName() throws Exception {
        Method method = TestController.class.getMethod("searchByKeyword", String.class);
        List<Map<String, Object>> parameters = parameterExtractor.extract(method);

        assertEquals(1, parameters.size());
        Map<String, Object> param = parameters.get(0);
        assertEquals("REQUEST_PARAM", param.get("parameterType"));
        // When @RequestParam has no value/name, it should be empty
        assertEquals("", param.get("paramName"));
    }

    @Test
    void testPathVariableWithoutName() throws Exception {
        Method method = TestController.class.getMethod("delete", Long.class);
        List<Map<String, Object>> parameters = parameterExtractor.extract(method);

        assertEquals(1, parameters.size());
        Map<String, Object> param = parameters.get(0);
        assertEquals("PATH_VARIABLE", param.get("parameterType"));
        assertEquals("", param.get("paramName"));
    }

    @Test
    void testRequestBodyNotRequired() throws Exception {
        Method method = TestController.class.getMethod("optionalUpdate", UserDTO.class);
        List<Map<String, Object>> parameters = parameterExtractor.extract(method);

        assertEquals(1, parameters.size());
        Map<String, Object> param = parameters.get(0);
        assertEquals("REQUEST_BODY", param.get("parameterType"));
        assertFalse((Boolean) param.get("required"));
    }

    // Test controller class with various parameter types
    @SuppressWarnings("unused")
    static class TestController {
        public void createUser(@RequestBody UserDTO user) {
        }

        public void searchUsers(@RequestParam("query") String query,
                                @RequestParam(value = "page", required = false, defaultValue = "0") Integer page) {
        }

        public void getUser(@PathVariable("id") Long id) {
        }

        public void getWithAuth(@RequestHeader("Authorization") String auth) {
        }

        public void getWithUserAgent(@RequestHeader(value = "User-Agent", required = false, defaultValue = "Unknown") String userAgent) {
        }

        public void getWithContext(HttpServletRequest request, HttpSession session) {
        }

        public void updateUser(@PathVariable("id") Long id,
                               @RequestBody UserDTO user,
                               @RequestHeader("X-Request-ID") String requestId) {
        }

        public void getAllUsers() {
        }

        public void searchByKeyword(@RequestParam String keyword) {
        }

        public void delete(@PathVariable Long id) {
        }

        public void optionalUpdate(@RequestBody(required = false) UserDTO user) {
        }
    }
}