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

package org.wso2.carbon.identity.action.management.api.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.wso2.carbon.identity.action.management.api.exception.ActionMgtRuntimeException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * This class models the object typed action property values
 */
public class BinaryObject {

    private String stringValue;
    private InputStream streamValue;

    private BinaryObject(String value) {

        this.stringValue = value;
    }

    private BinaryObject(InputStream value) {

        this.streamValue = value;
    }

    public static BinaryObject convertInputStreamToString(InputStream inputStream) {

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            return new BinaryObject(stringBuilder.toString());
        } catch (IOException e) {
            throw new ActionMgtRuntimeException("Error occurred while reading the input stream", e);
        }
    }

    public static BinaryObject convertObjectToInputStream(Object value) {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            InputStream stream = new ByteArrayInputStream(objectMapper.writeValueAsString(value)
                    .getBytes(StandardCharsets.UTF_8));
            return new BinaryObject(stream);
        } catch (JsonProcessingException e) {
            throw new ActionMgtRuntimeException("Failed to convert object values to JSON.", e);
        }
    }

    public String getStringValue() {

        return stringValue;
    }

    public InputStream getStreamValue() {

        return streamValue;
    }

    public int getLength() {

        try {
            return streamValue.available();
        } catch (IOException e) {
            throw new ActionMgtRuntimeException("Error occurred while reading the input stream", e);
        }
    }
}
