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

import org.apache.commons.lang.StringUtils;
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
    public void testNullAndEmptyJWT() throws ParseException {

        try (MockedStatic<IdentityUtil> identityUtilMock = mockStatic(IdentityUtil.class)) {
            setRealMethodCalls(identityUtilMock);
            IdentityUtil.validateJWTDepth(null);
            IdentityUtil.validateJWTDepth("");
            IdentityUtil.validateJWTDepth("   ");
        }
    }

    @Test
    public void testInvalidJWTFormats() throws ParseException {

        try (MockedStatic<IdentityUtil> identityUtilMock = mockStatic(IdentityUtil.class)) {
            setRealMethodCalls(identityUtilMock);
            IdentityUtil.validateJWTDepth("invalidjwt");
            IdentityUtil.validateJWTDepth("onlyonepart.");
            IdentityUtil.validateJWTDepth("header.");
            IdentityUtil.validateJWTDepth("header.invalid@base64!.signature");
        }
    }

    @Test
    public void testNonJSONPayload() throws ParseException {

        try (MockedStatic<IdentityUtil> identityUtilMock = mockStatic(IdentityUtil.class)) {
            setRealMethodCalls(identityUtilMock);
            String invalidJson = "not json at all";
            String jwt = createJWT(invalidJson);
            IdentityUtil.validateJWTDepth(jwt);
        }
    }

    @Test
    public void testMalformedJSON() throws ParseException {

        try (MockedStatic<IdentityUtil> identityUtilMock = mockStatic(IdentityUtil.class)) {
            setRealMethodCalls(identityUtilMock);
            String malformedObject = "{\"incomplete\":";
            String malformedArray = "[{\"incomplete\"";
            IdentityUtil.validateJWTDepth(createJWT(malformedObject));
            IdentityUtil.validateJWTDepth(createJWT(malformedArray));
        }
    }

    @Test
    public void testNonFlatStructuresBelowMaxDepth() throws ParseException {

        try (MockedStatic<IdentityUtil> identityUtilMock = mockStatic(IdentityUtil.class)) {
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

            // Object with nested array (depth 255).
            IdentityUtil.validateJWTDepth(createJWT(generateDeepNestedJSONArray(255)));

            // Mixed Object (depth 255).
            IdentityUtil.validateJWTDepth(createJWT(generateMixedDeepJSON(255)));
        }
    }

    @Test
    public void testNonFlatStructuresOverMaxDepth() {

        try (MockedStatic<IdentityUtil> identityUtilMock = mockStatic(IdentityUtil.class)) {
            setRealMethodCalls(identityUtilMock);

            // Object with nested object (depth 256).
            assertThrows(ParseException.class,
                    () -> IdentityUtil.validateJWTDepth(createJWT(generateDeepNestedJSONObject(256))));

            // Object with nested array (depth 256).
            assertThrows(ParseException.class,
                    () -> IdentityUtil.validateJWTDepth(createJWT(generateDeepNestedJSONArray(256))));

            // Mixed Object (depth 256).
            assertThrows(ParseException.class,
                    () -> IdentityUtil.validateJWTDepth(createJWT(generateMixedDeepJSON(256))));
        }
    }

    @Test
    public void testLargeWidthButAllowedDepth() throws ParseException {

        try (MockedStatic<IdentityUtil> identityUtilMock = mockStatic(IdentityUtil.class)) {
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
            setRealMethodCalls(identityUtilMock);
            String specialChars = "{\"message\":{\"text\":\"Hello {\\\"world\\\"}: [1,2,3]\"}}";
            IdentityUtil.validateJWTDepth(createJWT(specialChars));

            String nullValues = "{\"user\":{\"name\":null,\"profile\":null}}";
            IdentityUtil.validateJWTDepth(createJWT(nullValues));
        }
    }


    @Test
    public void testValidateJWTDepthOfJWTPayload() throws ParseException {

        try (MockedStatic<IdentityUtil> identityUtilMock = mockStatic(IdentityUtil.class)) {
            setRealMethodCalls(identityUtilMock);

            IdentityUtil.validateJWTDepthOfJWTPayload(StringUtils.EMPTY);

            // Object with nested object (depth 256).
            IdentityUtil.validateJWTDepthOfJWTPayload(generateDeepNestedJSONObject(1));

            // Object with nested object (depth 256).
            IdentityUtil.validateJWTDepthOfJWTPayload(generateDeepNestedJSONObject(255));

            // Object with nested object (depth 256).
            assertThrows(ParseException.class,
                    () -> IdentityUtil.validateJWTDepthOfJWTPayload(generateDeepNestedJSONObject(256)));

            // Object with nested array (depth 256).
            assertThrows(ParseException.class,
                    () -> IdentityUtil.validateJWTDepthOfJWTPayload(generateDeepNestedJSONArray(256)));
        }
    }

    private void setRealMethodCalls(MockedStatic<IdentityUtil> identityUtilMock) {

        identityUtilMock.when(() -> IdentityUtil.validateJWTDepth(any()))
                .thenCallRealMethod();
        identityUtilMock.when(() -> IdentityUtil.validateJsonDepth(any(), anyInt()))
                .thenCallRealMethod();
        identityUtilMock.when(() -> IdentityUtil.validateJWTDepthOfJWTPayload(any()))
                .thenCallRealMethod();
    }

    private String generateDeepNestedJSONObject(int depth) {

        StringBuilder jsonBuilder = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            jsonBuilder.append("{\"level").append(i).append("\":");
        }
        jsonBuilder.append("\"deepValue\"");
        for (int i = 0; i < depth; i++) {
            jsonBuilder.append("}");
        }
        return jsonBuilder.toString();
    }

    private String generateDeepNestedJSONArray(int depth) {

        StringBuilder jsonBuilder = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            jsonBuilder.append("[");
        }
        jsonBuilder.append("1");
        for (int i = 0; i < depth; i++) {
            jsonBuilder.append("]");
        }
        return jsonBuilder.toString();
    }

    private String generateMixedDeepJSON(int depth) {

        // Edge cases are not handled since only used for testing.
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{\"root\":[");
        depth -= 2; // Account for root object and array.
        for (int i = 0; i < depth / 2; i++) {
            jsonBuilder.append("{\"level").append(i).append("\":[");
        }
        if (depth % 2 == 1) {
            jsonBuilder.append("{\"level").append(depth / 2).append("\":");
        }
        jsonBuilder.append("1");
        if (depth % 2 == 1) {
            jsonBuilder.append("}");
        }
        for (int i = 0; i < depth / 2; i++) {
            jsonBuilder.append("]}");
        }
        jsonBuilder.append("]}");
        return jsonBuilder.toString();
    }
}
