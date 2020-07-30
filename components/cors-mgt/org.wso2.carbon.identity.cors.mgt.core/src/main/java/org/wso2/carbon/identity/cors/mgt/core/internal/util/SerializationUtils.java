/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 * NOTE: The code/logic in this class is copied from https://bitbucket.org/thetransactioncompany/cors-filter.
 * All credits goes to the original authors of the project https://bitbucket.org/thetransactioncompany/cors-filter.
 */

package org.wso2.carbon.identity.cors.mgt.core.internal.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static org.wso2.carbon.identity.cors.mgt.core.internal.Constants.SERIALIZATION_DELIMITER;

/**
 * Utility class for serialization operations of Set<String>.
 */
public class SerializationUtils {

    /**
     * Private constructor of SerializationUtils.
     */
    private SerializationUtils() {

    }

    /**
     * Serialises the items of a set into a string. Each item must have a meaningful {@code toString()} method.
     *
     * @param stringSet The set to serialise. Must not be {@code null}.
     * @return The serialised set as string.
     */
    public static String serializeStringSet(final Set<String> stringSet) {

        StringBuilder stringBuilder = new StringBuilder();
        Iterator<String> iterator = stringSet.iterator();
        while (iterator.hasNext()) {
            stringBuilder.append(iterator.next());
            if (iterator.hasNext()) {
                stringBuilder.append(SERIALIZATION_DELIMITER);
            }
        }

        return stringBuilder.toString();
    }

    /**
     * Deserialize a string into a Set.
     *
     * @param serializedString The string to deserialize. Must not be {@code null}.
     * @return The deserialized Set<String>.
     */
    public static Set<String> deserializeStringSet(String serializedString) {

        if (serializedString == null) {
            return new HashSet<>();
        }

        String trimmedValue = serializedString.trim();

        if (serializedString.isEmpty()) {
            return new HashSet<>();
        }

        return new HashSet<>(Arrays.asList(trimmedValue.split(SERIALIZATION_DELIMITER)));
    }
}
