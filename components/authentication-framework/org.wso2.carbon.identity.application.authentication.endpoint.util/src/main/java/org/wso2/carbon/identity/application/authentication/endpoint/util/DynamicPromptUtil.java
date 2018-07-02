/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.application.authentication.endpoint.util;

import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * Utility class to handle dynamic prompt.
 */
public class DynamicPromptUtil {

    public static Map inflateJson(String dataStr) throws IOException, DataFormatException {

        Map data = null;
        if (dataStr != null) {
            byte[] base64DecodedBytes = Base64.getDecoder().decode(dataStr);

            Inflater inflater = new Inflater();
            inflater.setInput(base64DecodedBytes);

            String original;

            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(base64DecodedBytes.length)) {
                byte[] buffer = new byte[1024];
                while (!inflater.finished()) {
                    int count = inflater.inflate(buffer);
                    outputStream.write(buffer, 0, count);
                }
                outputStream.close();
                byte[] output = outputStream.toByteArray();
                original = new String(output);
            }

            Gson gson = new Gson();
            data = gson.fromJson(original, Map.class);
        }
        return data;
    }
}
