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
import org.mockito.MockedStatic;

import java.text.ParseException;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mockStatic;
import static org.testng.Assert.assertThrows;

public class JWTDepthValidationTest {

    private static final int DEFAULT_MAX_DEPTH = 255;

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

        try (MockedStatic<IdentityUtil> identityUtilMock = mockStatic(IdentityUtil.class)) {
            setMaxJWTDepth(identityUtilMock, DEFAULT_MAX_DEPTH);
            setRealMethodCalls(identityUtilMock);

            assertThrows(ParseException.class, () -> IdentityUtil.validateJWTDepth(null));
            assertThrows(ParseException.class, () -> IdentityUtil.validateJWTDepth(""));
            assertThrows(ParseException.class, () -> IdentityUtil.validateJWTDepth("   "));
        }
    }

    @Test
    public void testInvalidJWTFormats() {

        try (MockedStatic<IdentityUtil> identityUtilMock = mockStatic(IdentityUtil.class)) {
            setMaxJWTDepth(identityUtilMock, DEFAULT_MAX_DEPTH);
            setRealMethodCalls(identityUtilMock);
            assertThrows(ParseException.class, () -> IdentityUtil.validateJWTDepth("invalidjwt"));
            assertThrows(ParseException.class, () -> IdentityUtil.validateJWTDepth("onlyonepart."));
            assertThrows(ParseException.class, () -> IdentityUtil.validateJWTDepth("header."));
            assertThrows(ParseException.class, () ->
                    IdentityUtil.validateJWTDepth("header.invalid@base64!.signature"));
        }
    }

    @Test
    public void testNonJSONPayload() {

        try (MockedStatic<IdentityUtil> identityUtilMock = mockStatic(IdentityUtil.class)) {
            setMaxJWTDepth(identityUtilMock, DEFAULT_MAX_DEPTH);
            setRealMethodCalls(identityUtilMock);
            String invalidJson = "not json at all";
            String jwt = createJWT(invalidJson);
            assertThrows(ParseException.class, () -> IdentityUtil.validateJWTDepth(jwt));
        }
    }

    @Test
    public void testMalformedJSON() {

        try (MockedStatic<IdentityUtil> identityUtilMock = mockStatic(IdentityUtil.class)) {
            setMaxJWTDepth(identityUtilMock, DEFAULT_MAX_DEPTH);
            setRealMethodCalls(identityUtilMock);
            String malformedObject = "{\"incomplete\":";
            String malformedArray = "[{\"incomplete\"";
            assertThrows(ParseException.class,
                    () -> IdentityUtil.validateJWTDepth(createJWT(malformedObject)));
            assertThrows(ParseException.class,
                    () -> IdentityUtil.validateJWTDepth(createJWT(malformedArray)));
        }
    }

    @Test
    public void testNonFlatStructuresBelowMaxDepth() throws ParseException {

        try (MockedStatic<IdentityUtil> identityUtilMock = mockStatic(IdentityUtil.class)) {
            setMaxJWTDepth(identityUtilMock, DEFAULT_MAX_DEPTH);
            setRealMethodCalls(identityUtilMock);
            // Object with nested object.
            String objectWithObject = "{\"user\":{\"id\":123,\"name\":\"John\"}}";
            IdentityUtil.validateJWTDepth(createJWT(objectWithObject));

            // Object with nested array.
            String objectWithArray = "{\"items\":[1,2,3,4,5]}";
            IdentityUtil.validateJWTDepth(createJWT(objectWithArray));

            // Array with nested objects.
            String arrayWithObjects = "[{\"id\":1},{\"id\":2}]";
            IdentityUtil.validateJWTDepth(createJWT(arrayWithObjects));

            // Array with nested arrays.
            String arrayWithArrays = "[[1,2],[3,4]]";
            IdentityUtil.validateJWTDepth(createJWT(arrayWithArrays));

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
            IdentityUtil.validateJWTDepth(createJWT(deepObject.toString()));

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
            IdentityUtil.validateJWTDepth(createJWT(deepArray.toString()));

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
            IdentityUtil.validateJWTDepth(createJWT(mixedDeep.toString()));
        }
    }

    @Test
    public void testNonFlatStructuresOverMaxDepth() {

        try (MockedStatic<IdentityUtil> identityUtilMock = mockStatic(IdentityUtil.class)) {
            setMaxJWTDepth(identityUtilMock, DEFAULT_MAX_DEPTH);
            setRealMethodCalls(identityUtilMock);

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
            assertThrows(ParseException.class, () -> IdentityUtil.validateJWTDepth(createJWT(deepObject.toString())));

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
            assertThrows(ParseException.class, () -> IdentityUtil.validateJWTDepth(createJWT(deepArray.toString())));

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
            assertThrows(ParseException.class, () -> IdentityUtil.validateJWTDepth(createJWT(mixedDeep.toString())));
        }
    }

    @Test
    public void testLargeWidthButAllowedDepth() throws ParseException {

        try (MockedStatic<IdentityUtil> identityUtilMock = mockStatic(IdentityUtil.class)) {
            setMaxJWTDepth(identityUtilMock, DEFAULT_MAX_DEPTH);
            setRealMethodCalls(identityUtilMock);
            // Many siblings at depth 2 - should pass.
            StringBuilder payload = new StringBuilder("{");
            for (int i = 0; i < 100; i++) {
                if (i > 0) payload.append(",");
                payload.append("\"key").append(i).append("\":{\"value\":").append(i).append("}");
            }
            payload.append("}");
            IdentityUtil.validateJWTDepth(createJWT(payload.toString()));
        }
    }

    @Test
    public void testVeryLongButShallowJWT() throws ParseException {

        try (MockedStatic<IdentityUtil> identityUtilMock = mockStatic(IdentityUtil.class)) {
            setMaxJWTDepth(identityUtilMock, DEFAULT_MAX_DEPTH);
            setRealMethodCalls(identityUtilMock);
            // Large JWT but only depth 1.
            StringBuilder payload = new StringBuilder("{");
            for (int i = 0; i < 1000; i++) {
                if (i > 0) payload.append(",");
                payload.append("\"field").append(i).append("\":\"value").append(i).append("\"");
            }
            payload.append("}");
            IdentityUtil.validateJWTDepth(createJWT(payload.toString()));
        }
    }

    @Test
    public void testEmptyNestedStructures() throws ParseException {

        try (MockedStatic<IdentityUtil> identityUtilMock = mockStatic(IdentityUtil.class)) {
            setMaxJWTDepth(identityUtilMock, DEFAULT_MAX_DEPTH);
            setRealMethodCalls(identityUtilMock);

            String emptyObjects = "{\"empty\":{},\"nested\":{\"empty2\":{}}}";
            IdentityUtil.validateJWTDepth(createJWT(emptyObjects));

            String emptyArrays = "{\"empty\":[],\"nested\":{\"empty2\":[]}}";
            IdentityUtil.validateJWTDepth(createJWT(emptyArrays));
        }
    }

    @Test
    public void testSpecialValues() throws ParseException {

        try (MockedStatic<IdentityUtil> identityUtilMock = mockStatic(IdentityUtil.class)) {
            setMaxJWTDepth(identityUtilMock, DEFAULT_MAX_DEPTH);
            setRealMethodCalls(identityUtilMock);
            String specialChars = "{\"message\":{\"text\":\"Hello {\\\"world\\\"}: [1,2,3]\"}}";
            IdentityUtil.validateJWTDepth(createJWT(specialChars));

            String nullValues = "{\"user\":{\"name\":null,\"profile\":null}}";
            IdentityUtil.validateJWTDepth(createJWT(nullValues));
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
            assertThrows(NumberFormatException.class, () -> IdentityUtil.validateJWTDepth(createJWT(depth4)));
        }
    }

    @Test
    public void testExtremeDepthLimits() throws ParseException {

        try (MockedStatic<IdentityUtil> identityUtilMock = mockStatic(IdentityUtil.class)) {
            setMaxJWTDepth(identityUtilMock, 0);
            setRealMethodCalls(identityUtilMock);

            String depth1 = "{\"simple\":\"value\"}";
            assertThrows(ParseException.class, () -> IdentityUtil.validateJWTDepth(createJWT(depth1)));
        }

        try (MockedStatic<IdentityUtil> identityUtilMock = mockStatic(IdentityUtil.class)) {
            // Test depth limit 1.
            setMaxJWTDepth(identityUtilMock, 1);
            setRealMethodCalls(identityUtilMock);

            String depth1 = "{\"simple\":\"value\"}";
            IdentityUtil.validateJWTDepth(createJWT(depth1));

            String depth2 = "{\"nested\":{\"value\":\"test\"}}";
            assertThrows(ParseException.class, () -> IdentityUtil.validateJWTDepth(createJWT(depth2)));
        }
    }

    private void setRealMethodCalls(MockedStatic<IdentityUtil> identityUtilMock) {

        identityUtilMock.when(() -> IdentityUtil.validateJWTDepth(any()))
                .thenCallRealMethod();
        identityUtilMock.when(() -> IdentityUtil.validateJsonDepth(any(), anyInt()))
                .thenCallRealMethod();
        identityUtilMock.when(IdentityUtil::getAllowedMaxJWTDepth).thenCallRealMethod();
    }

    private void setMaxJWTDepth(MockedStatic<IdentityUtil> identityUtilMock, int i) {

        identityUtilMock.when(() ->
                        IdentityUtil.getProperty(IdentityCoreConstants.JWT_MAXIMUM_ALLOWED_DEPTH_PROPERTY))
                .thenReturn(Integer.toString(i));
    }
}
