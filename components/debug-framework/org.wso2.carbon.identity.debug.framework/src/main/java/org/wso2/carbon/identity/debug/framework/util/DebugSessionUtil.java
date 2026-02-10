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

package org.wso2.carbon.identity.debug.framework.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Utility class for Debug Session related operations (Serialization, etc.).
 */
public class DebugSessionUtil {

    private DebugSessionUtil() {
    }

    /**
     * Serializes an object to an InputStream.
     *
     * @param object The object to serialize (must be Serializable).
     * @return InputStream containing the serialized object.
     * @throws IOException If serialization fails.
     */
    public static InputStream serializeObject(Serializable object) throws IOException {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(object);
        }
        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }

    /**
     * Deserializes an object from an InputStream.
     *
     * @param inputStream The InputStream containing the serialized object.
     * @return The deserialized object.
     * @throws IOException            If deserialization fails.
     * @throws ClassNotFoundException If the class of the serialized object cannot be found.
     */
    public static Object deserializeObject(InputStream inputStream) throws IOException, ClassNotFoundException {

        if (inputStream == null) {
            return null;
        }
        try (ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
            return objectInputStream.readObject();
        }
    }
}
