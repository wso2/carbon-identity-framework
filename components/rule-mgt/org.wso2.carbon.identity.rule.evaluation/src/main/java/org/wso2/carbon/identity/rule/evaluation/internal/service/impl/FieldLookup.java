/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.rule.evaluation.internal.service.impl;

/**
 * Builds the key identifying a field, with its qualifier, within one evaluation.
 */
final class FieldLookup {

    /**
     * Unit separator: neither a field name nor a qualifier contains control characters.
     */
    private static final char SEPARATOR = (char) 0x1F;

    private FieldLookup() {

    }

    /**
     * Build the key identifying a field within one evaluation.
     *
     * @param name      Field name.
     * @param qualifier Qualifier of the field, or null when the field is not qualified.
     * @return The field name, followed by the qualifier when there is one.
     */
    static String token(String name, String qualifier) {

        return qualifier == null ? name : name + SEPARATOR + qualifier;
    }
}
