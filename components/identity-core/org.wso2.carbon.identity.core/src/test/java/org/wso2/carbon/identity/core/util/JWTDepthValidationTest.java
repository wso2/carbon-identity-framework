/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.core.util;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import static org.testng.Assert.*;
import static org.mockito.Mockito.*;
import org.mockito.MockedStatic;

import java.util.Base64;
import java.nio.charset.StandardCharsets;
import java.util.Queue;

public class JWTDepthValidationTest {

    @BeforeMethod
    public void setUp() {

        // Reset any system properties before each test.
        System.clearProperty("jwt.maximum.allowed.depth");
    }

    @AfterMethod
    public void tearDown() {

        System.clearProperty("jwt.maximum.allowed.depth");
    }

    // Helper method to create JWT with given payload.
    private String createJWT(String payload) {

        String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String encodedHeader = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(header.getBytes(StandardCharsets.UTF_8));
        String encodedPayload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        return encodedHeader + "." + encodedPayload + ".signature";
    }

    @Test
    public void testNullAndEmptyJWT() {

        assertFalse(IdentityUtil.exceedsAllowedJWTDepth(null));
        assertFalse(IdentityUtil.exceedsAllowedJWTDepth(""));
        assertFalse(IdentityUtil.exceedsAllowedJWTDepth("   "));
    }

    @Test
    public void testInvalidJWTFormats() {

        assertFalse(IdentityUtil.exceedsAllowedJWTDepth("invalidjwt"));
        assertFalse(IdentityUtil.exceedsAllowedJWTDepth("onlyonepart."));
        assertFalse(IdentityUtil.exceedsAllowedJWTDepth("header."));
        assertFalse(IdentityUtil.exceedsAllowedJWTDepth("header.invalid@base64!.signature"));
    }

    @Test
    public void testNonJSONPayload() {

        String invalidJson = "not json at all";
        String jwt = createJWT(invalidJson);
        assertFalse(IdentityUtil.exceedsAllowedJWTDepth(jwt));
    }

    @Test
    public void testMalformedJSON() {

        String malformedObject = "{\"incomplete\":";
        String malformedArray = "[{\"incomplete\"";
        assertFalse(IdentityUtil.exceedsAllowedJWTDepth(createJWT(malformedObject)));
        assertFalse(IdentityUtil.exceedsAllowedJWTDepth(createJWT(malformedArray)));
    }

    @Test
    public void testFlatStructuresAgainstDefaultDepth() {

        // Flat object.
        String flatObject = "{\"sub\":\"user123\",\"name\":\"John Doe\",\"exp\":1234567890}";
        assertFalse(IdentityUtil.exceedsAllowedJWTDepth(createJWT(flatObject)));

        // Flat array.
        String flatArray = "[\"value1\",\"value2\",\"value3\"]";
        assertFalse(IdentityUtil.exceedsAllowedJWTDepth(createJWT(flatArray)));

        // Empty structures.
        assertFalse(IdentityUtil.exceedsAllowedJWTDepth(createJWT("{}")));
        assertFalse(IdentityUtil.exceedsAllowedJWTDepth(createJWT("[]")));
    }

    @Test
    public void testNonFlatStructuresBelowDefaultDepthAgainstDefaultDepth() {

        // Object with nested object.
        String objectWithObject = "{\"user\":{\"id\":123,\"name\":\"John\"}}";
        assertFalse(IdentityUtil.exceedsAllowedJWTDepth(createJWT(objectWithObject)));

        // Object with nested array.
        String objectWithArray = "{\"items\":[1,2,3,4,5]}";
        assertFalse(IdentityUtil.exceedsAllowedJWTDepth(createJWT(objectWithArray)));

        // Array with nested objects.
        String arrayWithObjects = "[{\"id\":1},{\"id\":2}]";
        assertFalse(IdentityUtil.exceedsAllowedJWTDepth(createJWT(arrayWithObjects)));

        // Array with nested arrays.
        String arrayWithArrays = "[[1,2],[3,4]]";
        assertFalse(IdentityUtil.exceedsAllowedJWTDepth(createJWT(arrayWithArrays)));

        // Object with nested object (depth 256).
        StringBuilder deepObject = new StringBuilder("{");
        for (int i = 0; i < 254; i++) {
            deepObject.append("\"level").append(i).append("\":{");
        }
        deepObject.append("\"value\":\"deep\""); // Deepest level.
        for (int i = 0; i < 254; i++) {
            deepObject.append("}");
        }
        deepObject.append("}");
        assertFalse(IdentityUtil.exceedsAllowedJWTDepth(createJWT(deepObject.toString())));

        // Object with nested array (depth 256).
        StringBuilder deepArray = new StringBuilder("{");
        deepArray.append("\"level\" : ");
        for (int i = 0; i < 254; i++) {
            deepArray.append("[");
        }
        deepArray.append("1"); // Deepest level.
        for (int i = 0; i < 254; i++) {
            deepArray.append("]");
        }
        deepArray.append("}");
        assertFalse(IdentityUtil.exceedsAllowedJWTDepth(createJWT(deepArray.toString())));

        // Mixed Object (depth 256).
        StringBuilder mixedDeep = new StringBuilder("{"); // 1 level here.
        mixedDeep.append("\"root\":["); // 1 level here.
        for (int i = 0; i < 126; i++) {
            mixedDeep.append("{\"level").append(i).append("\":[");
        }
        mixedDeep.append("1"); // Deepest level.
        for (int i = 0; i < 126; i++) {
            mixedDeep.append("]}");
        }
        mixedDeep.append("]}");
        assertFalse(IdentityUtil.exceedsAllowedJWTDepth(createJWT(mixedDeep.toString())));
    }

    @Test
    public void testNonFlatStructuresOverDefaultDepthAgainstDefaultDepth() {

        // Object with nested object (depth 256).
        StringBuilder deepObject = new StringBuilder("{");
        for (int i = 0; i < 256; i++) {
            deepObject.append("\"level").append(i).append("\":{");
        }
        deepObject.append("\"value\":\"deep\""); // Deepest level.
        for (int i = 0; i < 256; i++) {
            deepObject.append("}");
        }
        deepObject.append("}");
        assertTrue(IdentityUtil.exceedsAllowedJWTDepth(createJWT(deepObject.toString())));

        // Object with nested array (depth 256).
        StringBuilder deepArray = new StringBuilder("{");
        deepArray.append("\"level\" : ");
        for (int i = 0; i < 256; i++) {
            deepArray.append("[");
        }
        deepArray.append("1"); // Deepest level.
        for (int i = 0; i < 256; i++) {
            deepArray.append("]");
        }
        deepArray.append("}");
        assertTrue(IdentityUtil.exceedsAllowedJWTDepth(createJWT(deepArray.toString())));

        // Mixed Object (depth 256).
        StringBuilder mixedDeep = new StringBuilder("{"); // 1 level here.
        mixedDeep.append("\"root\":["); // 1 level here.
        for (int i = 0; i < 128; i++) {
            mixedDeep.append("{\"level").append(i).append("\":[");
        }
        mixedDeep.append("1"); // Deepest level.
        for (int i = 0; i < 128; i++) {
           mixedDeep.append("]}");
        }
        mixedDeep.append("]}");
        assertTrue(IdentityUtil.exceedsAllowedJWTDepth(createJWT(mixedDeep.toString())));
    }

    @Test
    public void testStructureDepthExceedConfiguredDepth() {

        try (MockedStatic<IdentityUtil> identityUtilMock = mockStatic(IdentityUtil.class)) {
            identityUtilMock.when(() ->
                            IdentityUtil.getProperty(IdentityCoreConstants.JWT_MAXIMUM_ALLOWED_DEPTH_PROPERTY))
                    .thenReturn("4");
            setRealMethodCalls(identityUtilMock);

            // Complex mixed structure depth 5.
            String depth5Structure = "{\"data\":{\"users\":[{\"profile\":{\"settings\":{}}}]}}";
            assertTrue(IdentityUtil.exceedsAllowedJWTDepth(createJWT(depth5Structure)));

            // Array with deeply nested object (depth 5).
            String arrayDepth5 = "[{\"user\":{\"profile\":{\"settings\":{\"theme\":\"dark\"}}}}]";
            assertTrue(IdentityUtil.exceedsAllowedJWTDepth(createJWT(arrayDepth5)));
        }
    }

    @Test
    public void testStructureDepthNotExceedConfiguredDepth() {

        try (MockedStatic<IdentityUtil> identityUtilMock = mockStatic(IdentityUtil.class)) {
            identityUtilMock.when(() ->
                            IdentityUtil.getProperty(IdentityCoreConstants.JWT_MAXIMUM_ALLOWED_DEPTH_PROPERTY))
                    .thenReturn("4");
            setRealMethodCalls(identityUtilMock);

            // Complex mixed structure depth 3 - should pass.
            String depth4Structure = "{\"data\":{\"users\":[{\"profile\":\"value\"}]}}";
            assertFalse(IdentityUtil.exceedsAllowedJWTDepth(createJWT(depth4Structure)));

            // Object with multiple nested arrays (depth 3) - should pass.
            String multipleArraysDepth4 = "{\"matrix\":[[1,2],[3,4]],\"list\":[[\"a\",\"b\"],[\"c\",\"d\"]]}";
            assertFalse(IdentityUtil.exceedsAllowedJWTDepth(createJWT(multipleArraysDepth4)));
        }
    }

    @Test
    public void testLargeWidthButAllowedDepth() {

        // Many siblings at depth 2 - should pass.
        StringBuilder payload = new StringBuilder("{");
        for (int i = 0; i < 100; i++) {
            if (i > 0) payload.append(",");
            payload.append("\"key").append(i).append("\":{\"value\":").append(i).append("}");
        }
        payload.append("}");
        assertFalse(IdentityUtil.exceedsAllowedJWTDepth(createJWT(payload.toString())));
    }

    @Test
    public void testVeryLongButShallowJWT() {

        // Large JWT but only depth 1.
        StringBuilder payload = new StringBuilder("{");
        for (int i = 0; i < 1000; i++) {
            if (i > 0) payload.append(",");
            payload.append("\"field").append(i).append("\":\"value").append(i).append("\"");
        }
        payload.append("}");
        assertFalse(IdentityUtil.exceedsAllowedJWTDepth(createJWT(payload.toString())));
    }

    @Test
    public void testEmptyNestedStructures() {

        String emptyObjects = "{\"empty\":{},\"nested\":{\"empty2\":{}}}";
        assertFalse(IdentityUtil.exceedsAllowedJWTDepth(createJWT(emptyObjects)));

        String emptyArrays = "{\"empty\":[],\"nested\":{\"empty2\":[]}}";
        assertFalse(IdentityUtil.exceedsAllowedJWTDepth(createJWT(emptyArrays)));
    }

    @Test
    public void testSpecialValues() {
        String specialChars = "{\"message\":{\"text\":\"Hello {\\\"world\\\"}: [1,2,3]\"}}";
        assertFalse(IdentityUtil.exceedsAllowedJWTDepth(createJWT(specialChars)));

        String nullValues = "{\"user\":{\"name\":null,\"profile\":null}}";
        assertFalse(IdentityUtil.exceedsAllowedJWTDepth(createJWT(nullValues)));
    }

    @Test
    public void testCustomMaxDepthConfiguration() {

        try (MockedStatic<IdentityUtil> identityUtilMock = mockStatic(IdentityUtil.class)) {
            identityUtilMock.when(() ->
                            IdentityUtil.getProperty(IdentityCoreConstants.JWT_MAXIMUM_ALLOWED_DEPTH_PROPERTY))
                    .thenReturn("5");
            setRealMethodCalls(identityUtilMock);

            // Depth 5 should pass with limit 5.
            String depth5 = "{\"a\":{\"b\":{\"c\":{\"d\":{\"e\":\"value\"}}}}}";
            assertFalse(IdentityUtil.exceedsAllowedJWTDepth(createJWT(depth5)));

            // Depth 6 should fail with limit 5.
            String depth6 = "{\"a\":{\"b\":{\"c\":{\"d\":{\"e\":{\"f\":\"value\"}}}}}}";
            assertTrue(IdentityUtil.exceedsAllowedJWTDepth(createJWT(depth6)));
        }
    }

    @Test
    public void testInvalidConfiguration() {

        try (MockedStatic<IdentityUtil> identityUtilMock = mockStatic(IdentityUtil.class)) {
            identityUtilMock.when(() ->
                            IdentityUtil.getProperty(IdentityCoreConstants.JWT_MAXIMUM_ALLOWED_DEPTH_PROPERTY))
                    .thenReturn("invalid_number");
            setRealMethodCalls(identityUtilMock);

            String depth4 = "{\"a\":{\"b\":{\"c\":{\"d\":\"value\"}}}}";
            // Falls back to default 255, so depth 4 should be allowed.
            assertFalse(IdentityUtil.exceedsAllowedJWTDepth(createJWT(depth4)));
        }
    }

    @Test
    public void testNullAndBlankConfiguration() {

        try (MockedStatic<IdentityUtil> identityUtilMock = mockStatic(IdentityUtil.class)) {
            // Test null config -> fallback 255.
            identityUtilMock.when(() ->
                            IdentityUtil.getProperty(IdentityCoreConstants.JWT_MAXIMUM_ALLOWED_DEPTH_PROPERTY))
                    .thenReturn(null);
            setRealMethodCalls(identityUtilMock);

            String depth4 = "{\"a\":{\"b\":{\"c\":{\"d\":\"value\"}}}}";
            assertFalse(IdentityUtil.exceedsAllowedJWTDepth(createJWT(depth4)));
        }

        try (MockedStatic<IdentityUtil> identityUtilMock = mockStatic(IdentityUtil.class)) {
            // Test blank config -> fallback 255.
            identityUtilMock.when(() ->
                            IdentityUtil.getProperty(IdentityCoreConstants.JWT_MAXIMUM_ALLOWED_DEPTH_PROPERTY))
                    .thenReturn("");
            setRealMethodCalls(identityUtilMock);

            String depth4 = "{\"a\":{\"b\":{\"c\":{\"d\":\"value\"}}}}";
            assertFalse(IdentityUtil.exceedsAllowedJWTDepth(createJWT(depth4)));
        }
    }

    @Test
    public void testExtremeDepthLimits() {

        try (MockedStatic<IdentityUtil> identityUtilMock = mockStatic(IdentityUtil.class)) {
            // Test depth limit 0.
            identityUtilMock.when(() ->
                            IdentityUtil.getProperty(IdentityCoreConstants.JWT_MAXIMUM_ALLOWED_DEPTH_PROPERTY))
                    .thenReturn("0");
            setRealMethodCalls(identityUtilMock);

            String depth1 = "{\"simple\":\"value\"}";
            assertTrue(IdentityUtil.exceedsAllowedJWTDepth(createJWT(depth1)));
        }

        try (MockedStatic<IdentityUtil> identityUtilMock = mockStatic(IdentityUtil.class)) {
            // Test depth limit 1.
            identityUtilMock.when(() ->
                            IdentityUtil.getProperty(IdentityCoreConstants.JWT_MAXIMUM_ALLOWED_DEPTH_PROPERTY))
                    .thenReturn("1");
            setRealMethodCalls(identityUtilMock);

            String depth1 = "{\"simple\":\"value\"}";
            assertFalse(IdentityUtil.exceedsAllowedJWTDepth(createJWT(depth1)));

            String depth2 = "{\"nested\":{\"value\":\"test\"}}";
            assertTrue(IdentityUtil.exceedsAllowedJWTDepth(createJWT(depth2)));
        }
    }

    private void setRealMethodCalls(MockedStatic<IdentityUtil> identityUtilMock) {

        identityUtilMock.when(() -> IdentityUtil.exceedsAllowedJWTDepth(anyString()))
                .thenCallRealMethod();
        identityUtilMock.when(() -> IdentityUtil.exceedsAllowedJsonDepth(any()))
                .thenCallRealMethod();
        identityUtilMock.when(IdentityUtil::getAllowedMaxJsonDepth).thenCallRealMethod();
    }
}
