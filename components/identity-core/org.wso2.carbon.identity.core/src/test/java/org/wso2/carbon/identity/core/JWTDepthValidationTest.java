package org.wso2.carbon.identity.core;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import static org.testng.Assert.*;
import static org.mockito.Mockito.*;
import org.mockito.MockedStatic;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.util.Base64;
import java.nio.charset.StandardCharsets;

public class JWTDepthValidationTest {

    @BeforeMethod
    public void setUp() {

        // Reset any system properties before each test
        System.clearProperty("jwt.maximum.allowed.depth");
    }

    @AfterMethod
    public void tearDown() {

        System.clearProperty("jwt.maximum.allowed.depth");
    }

    // Helper method to create JWT with given payload
    private String createJWT(String payload) {

        String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String encodedHeader = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(header.getBytes(StandardCharsets.UTF_8));
        String encodedPayload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        return encodedHeader + "." + encodedPayload + ".signature";
    }

    // ===== EDGE CASES =====
    @Test
    public void testNullAndEmptyJWT() {

        assertTrue(IdentityUtil.isWithinAllowedJWTDepth(null));
        assertTrue(IdentityUtil.isWithinAllowedJWTDepth(""));
        assertTrue(IdentityUtil.isWithinAllowedJWTDepth("   "));
    }

    @Test
    public void testInvalidJWTFormats() {

        assertTrue(IdentityUtil.isWithinAllowedJWTDepth("invalidjwt"));
        assertTrue(IdentityUtil.isWithinAllowedJWTDepth("onlyonepart."));
        assertTrue(IdentityUtil.isWithinAllowedJWTDepth("header."));
        assertTrue(IdentityUtil.isWithinAllowedJWTDepth("header.invalid@base64!.signature"));
    }

    @Test
    public void testNonJSONPayload() {

        String invalidJson = "not json at all";
        String jwt = createJWT(invalidJson);
        assertTrue(IdentityUtil.isWithinAllowedJWTDepth(jwt));
    }

    @Test
    public void testMalformedJSON() {

        String malformedObject = "{\"incomplete\":";
        String malformedArray = "[{\"incomplete\"";
        assertTrue(IdentityUtil.isWithinAllowedJWTDepth(createJWT(malformedObject)));
        assertTrue(IdentityUtil.isWithinAllowedJWTDepth(createJWT(malformedArray)));
    }

    @Test
    public void testFlatStructuresAgainstDefaultDepth() {

        // Flat object
        String flatObject = "{\"sub\":\"user123\",\"name\":\"John Doe\",\"exp\":1234567890}";
        assertTrue(IdentityUtil.isWithinAllowedJWTDepth(createJWT(flatObject)));

        // Flat array
        String flatArray = "[\"value1\",\"value2\",\"value3\"]";
        assertTrue(IdentityUtil.isWithinAllowedJWTDepth(createJWT(flatArray)));

        // Empty structures
        assertTrue(IdentityUtil.isWithinAllowedJWTDepth(createJWT("{}")));
        assertTrue(IdentityUtil.isWithinAllowedJWTDepth(createJWT("[]")));
    }

    @Test
    public void testNonFlatStructuresBelowDefaultDepthAgainstDefaultDepth() {

        // Object with nested object
        String objectWithObject = "{\"user\":{\"id\":123,\"name\":\"John\"}}";
        assertTrue(IdentityUtil.isWithinAllowedJWTDepth(createJWT(objectWithObject)));

        // Object with nested array
        String objectWithArray = "{\"items\":[1,2,3,4,5]}";
        assertTrue(IdentityUtil.isWithinAllowedJWTDepth(createJWT(objectWithArray)));

        // Array with nested objects
        String arrayWithObjects = "[{\"id\":1},{\"id\":2}]";
        assertTrue(IdentityUtil.isWithinAllowedJWTDepth(createJWT(arrayWithObjects)));

        // Array with nested arrays
        String arrayWithArrays = "[[1,2],[3,4]]";
        assertTrue(IdentityUtil.isWithinAllowedJWTDepth(createJWT(arrayWithArrays)));

        // Object with nested object (depth 256)
        StringBuilder deepObject = new StringBuilder("{");
        for (int i = 0; i < 254; i++) {
            deepObject.append("\"level").append(i).append("\":{");
        }
        deepObject.append("\"value\":\"deep\""); // deepest level
        for (int i = 0; i < 254; i++) {
            deepObject.append("}");
        }
        deepObject.append("}");
        assertTrue(IdentityUtil.isWithinAllowedJWTDepth(createJWT(deepObject.toString())));

        // Object with nested array (depth 256)
        StringBuilder deepArray = new StringBuilder("{");
        deepArray.append("\"level\" : ");
        for (int i = 0; i < 254; i++) {
            deepArray.append("[");
        }
        deepArray.append("1"); // deepest level
        for (int i = 0; i < 254; i++) {
            deepArray.append("]");
        }
        deepArray.append("}");
        assertTrue(IdentityUtil.isWithinAllowedJWTDepth(createJWT(deepArray.toString())));

        // Mixed Object (depth 256)
        StringBuilder mixedDeep = new StringBuilder("{"); // 1 level here
        mixedDeep.append("\"root\":["); // 1 level here
        for (int i = 0; i < 126; i++) {
            mixedDeep.append("{\"level").append(i).append("\":[");
        }
        mixedDeep.append("1"); // deepest level
        for (int i = 0; i < 126; i++) {
            mixedDeep.append("]}");
        }
        mixedDeep.append("]}");
        assertTrue(IdentityUtil.isWithinAllowedJWTDepth(createJWT(mixedDeep.toString())));
    }

    @Test
    public void testNonFlatStructuresOverDefaultDepthAgainstDefaultDepth() {

        // Object with nested object (depth 256)
        StringBuilder deepObject = new StringBuilder("{");
        for (int i = 0; i < 256; i++) {
            deepObject.append("\"level").append(i).append("\":{");
        }
        deepObject.append("\"value\":\"deep\""); // deepest level
        for (int i = 0; i < 256; i++) {
            deepObject.append("}");
        }
        deepObject.append("}");
        assertFalse(IdentityUtil.isWithinAllowedJWTDepth(createJWT(deepObject.toString())));

        // Object with nested array (depth 256)
        StringBuilder deepArray = new StringBuilder("{");
        deepArray.append("\"level\" : ");
        for (int i = 0; i < 256; i++) {
            deepArray.append("[");
        }
        deepArray.append("1"); // deepest level
        for (int i = 0; i < 256; i++) {
            deepArray.append("]");
        }
        deepArray.append("}");
        assertFalse(IdentityUtil.isWithinAllowedJWTDepth(createJWT(deepArray.toString())));

        // Mixed Object (depth 256)
        StringBuilder mixedDeep = new StringBuilder("{"); // 1 level here
        mixedDeep.append("\"root\":["); // 1 level here
        for (int i = 0; i < 128; i++) {
            mixedDeep.append("{\"level").append(i).append("\":[");
        }
        mixedDeep.append("1"); // deepest level
        for (int i = 0; i < 128; i++) {
           mixedDeep.append("]}");
        }
        mixedDeep.append("]}");
        assertFalse(IdentityUtil.isWithinAllowedJWTDepth(createJWT(mixedDeep.toString())));
    }

    @Test
    public void testStructureDepthExceedConfiguredDepth() {

        try (MockedStatic<IdentityUtil> identityUtilMock = mockStatic(IdentityUtil.class)) {
            identityUtilMock.when(() -> IdentityUtil.getProperty(IdentityCoreConstants.JWT_MAXIMUM_ALLOWED_DEPTH_PROPERTY))
                    .thenReturn("4");
            identityUtilMock.when(() -> IdentityUtil.isWithinAllowedJWTDepth(anyString()))
                    .thenCallRealMethod();

            // Complex mixed structure depth 5
            String depth5Structure = "{\"data\":{\"users\":[{\"profile\":{\"settings\":{}}}]}}";
            assertFalse(IdentityUtil.isWithinAllowedJWTDepth(createJWT(depth5Structure)));

            // Array with deeply nested object (depth 5)
            String arrayDepth5 = "[{\"user\":{\"profile\":{\"settings\":{\"theme\":\"dark\"}}}}]";
            assertFalse(IdentityUtil.isWithinAllowedJWTDepth(createJWT(arrayDepth5)));
        }
    }

    @Test
    public void testStructureDepthNotExceedConfiguredDepth() {

        try (MockedStatic<IdentityUtil> identityUtilMock = mockStatic(IdentityUtil.class)) {
            identityUtilMock.when(() -> IdentityUtil.getProperty(IdentityCoreConstants.JWT_MAXIMUM_ALLOWED_DEPTH_PROPERTY))
                    .thenReturn("4");
            identityUtilMock.when(() -> IdentityUtil.isWithinAllowedJWTDepth(anyString()))
                    .thenCallRealMethod();

            // Complex mixed structure depth 3 - should pass
            String depth4Structure = "{\"data\":{\"users\":[{\"profile\":\"value\"}]}}";
            assertTrue(IdentityUtil.isWithinAllowedJWTDepth(createJWT(depth4Structure)));

            // Object with multiple nested arrays (depth 3) - should pass
            String multipleArraysDepth4 = "{\"matrix\":[[1,2],[3,4]],\"list\":[[\"a\",\"b\"],[\"c\",\"d\"]]}";
            assertTrue(IdentityUtil.isWithinAllowedJWTDepth(createJWT(multipleArraysDepth4)));
        }
    }

    // ===== PERFORMANCE/ATTACK PREVENTION TESTS =====
    @Test
    public void testLargeWidthButAllowedDepth() {

        // Many siblings at depth 2 - should pass
        StringBuilder payload = new StringBuilder("{");
        for (int i = 0; i < 100; i++) {
            if (i > 0) payload.append(",");
            payload.append("\"key").append(i).append("\":{\"value\":").append(i).append("}");
        }
        payload.append("}");
        assertTrue(IdentityUtil.isWithinAllowedJWTDepth(createJWT(payload.toString())));
    }

    @Test
    public void testVeryLongButShallowJWT() {

        // Large JWT but only depth 1
        StringBuilder payload = new StringBuilder("{");
        for (int i = 0; i < 1000; i++) {
            if (i > 0) payload.append(",");
            payload.append("\"field").append(i).append("\":\"value").append(i).append("\"");
        }
        payload.append("}");
        assertTrue(IdentityUtil.isWithinAllowedJWTDepth(createJWT(payload.toString())));
    }

    // ===== SPECIAL CASES =====
    @Test
    public void testEmptyNestedStructures() {

        String emptyObjects = "{\"empty\":{},\"nested\":{\"empty2\":{}}}";
        assertTrue(IdentityUtil.isWithinAllowedJWTDepth(createJWT(emptyObjects))); // depth 2

        String emptyArrays = "{\"empty\":[],\"nested\":{\"empty2\":[]}}";
        assertTrue(IdentityUtil.isWithinAllowedJWTDepth(createJWT(emptyArrays))); // depth 2
    }

    @Test
    public void testSpecialValues() {
        String specialChars = "{\"message\":{\"text\":\"Hello {\\\"world\\\"}: [1,2,3]\"}}";
        assertTrue(IdentityUtil.isWithinAllowedJWTDepth(createJWT(specialChars))); // depth 2

        String nullValues = "{\"user\":{\"name\":null,\"profile\":null}}";
        assertTrue(IdentityUtil.isWithinAllowedJWTDepth(createJWT(nullValues))); // depth 2
    }

    // ===== CONFIGURATION TESTS =====
    @Test
    public void testCustomMaxDepthConfiguration() {

        try (MockedStatic<IdentityUtil> identityUtilMock = mockStatic(IdentityUtil.class)) {
            identityUtilMock.when(() ->
                            IdentityUtil.getProperty(IdentityCoreConstants.JWT_MAXIMUM_ALLOWED_DEPTH_PROPERTY))
                    .thenReturn("5");
            identityUtilMock.when(() -> IdentityUtil.isWithinAllowedJWTDepth(anyString()))
                    .thenCallRealMethod();

            // Depth 5 should pass with limit 5
            String depth5 = "{\"a\":{\"b\":{\"c\":{\"d\":{\"e\":\"value\"}}}}}";
            assertTrue(IdentityUtil.isWithinAllowedJWTDepth(createJWT(depth5)));

            // Depth 6 should fail with limit 5
            String depth6 = "{\"a\":{\"b\":{\"c\":{\"d\":{\"e\":{\"f\":\"value\"}}}}}}";
            assertFalse(IdentityUtil.isWithinAllowedJWTDepth(createJWT(depth6)));
        }
    }

    @Test
    public void testInvalidConfiguration() {

        try (MockedStatic<IdentityUtil> identityUtilMock = mockStatic(IdentityUtil.class)) {
            identityUtilMock.when(() ->
                            IdentityUtil.getProperty(IdentityCoreConstants.JWT_MAXIMUM_ALLOWED_DEPTH_PROPERTY))
                    .thenReturn("invalid_number");
            identityUtilMock.when(() -> IdentityUtil.isWithinAllowedJWTDepth(anyString()))
                    .thenCallRealMethod();

            String depth4 = "{\"a\":{\"b\":{\"c\":{\"d\":\"value\"}}}}";
            // Falls back to default 255, so depth 4 should be allowed
            assertTrue(IdentityUtil.isWithinAllowedJWTDepth(createJWT(depth4)));
        }
    }

    @Test
    public void testNullAndBlankConfiguration() {

        try (MockedStatic<IdentityUtil> identityUtilMock = mockStatic(IdentityUtil.class)) {
            // Test null config -> fallback 255
            identityUtilMock.when(() -> IdentityUtil.getProperty(IdentityCoreConstants.JWT_MAXIMUM_ALLOWED_DEPTH_PROPERTY))
                    .thenReturn(null);
            identityUtilMock.when(() -> IdentityUtil.isWithinAllowedJWTDepth(anyString()))
                    .thenCallRealMethod();

            String depth4 = "{\"a\":{\"b\":{\"c\":{\"d\":\"value\"}}}}";
            assertTrue(IdentityUtil.isWithinAllowedJWTDepth(createJWT(depth4)));
        }

        try (MockedStatic<IdentityUtil> identityUtilMock = mockStatic(IdentityUtil.class)) {
            // Test blank config -> fallback 255
            identityUtilMock.when(() -> IdentityUtil.getProperty(IdentityCoreConstants.JWT_MAXIMUM_ALLOWED_DEPTH_PROPERTY))
                    .thenReturn("");
            identityUtilMock.when(() -> IdentityUtil.isWithinAllowedJWTDepth(anyString()))
                    .thenCallRealMethod();

            String depth4 = "{\"a\":{\"b\":{\"c\":{\"d\":\"value\"}}}}";
            assertTrue(IdentityUtil.isWithinAllowedJWTDepth(createJWT(depth4)));
        }
    }

    @Test
    public void testExtremeDepthLimits() {

        try (MockedStatic<IdentityUtil> identityUtilMock = mockStatic(IdentityUtil.class)) {
            // Test depth limit 0
            identityUtilMock.when(() -> IdentityUtil.getProperty(IdentityCoreConstants.JWT_MAXIMUM_ALLOWED_DEPTH_PROPERTY))
                    .thenReturn("0");
            identityUtilMock.when(() -> IdentityUtil.isWithinAllowedJWTDepth(anyString()))
                    .thenCallRealMethod();

            String depth1 = "{\"simple\":\"value\"}";
            assertFalse(IdentityUtil.isWithinAllowedJWTDepth(createJWT(depth1))); // Even depth 1 rejected
        }

        try (MockedStatic<IdentityUtil> identityUtilMock = mockStatic(IdentityUtil.class)) {
            // Test depth limit 1
            identityUtilMock.when(() -> IdentityUtil.getProperty(IdentityCoreConstants.JWT_MAXIMUM_ALLOWED_DEPTH_PROPERTY))
                    .thenReturn("1");
            identityUtilMock.when(() -> IdentityUtil.isWithinAllowedJWTDepth(anyString()))
                    .thenCallRealMethod();

            String depth1 = "{\"simple\":\"value\"}";
            assertTrue(IdentityUtil.isWithinAllowedJWTDepth(createJWT(depth1))); // Depth 1 allowed

            String depth2 = "{\"nested\":{\"value\":\"test\"}}";
            assertFalse(IdentityUtil.isWithinAllowedJWTDepth(createJWT(depth2))); // Depth 2 rejected
        }
    }
}
