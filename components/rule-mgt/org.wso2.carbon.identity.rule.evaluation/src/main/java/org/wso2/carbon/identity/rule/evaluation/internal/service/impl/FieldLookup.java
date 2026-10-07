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

package org.wso2.carbon.identity.rule.evaluation.internal.service.impl;

/**
 * Identifies a field within one evaluation.
 * <p>
 * A field name alone stops being enough once a field is qualified: two expressions over the same field
 * with different qualifiers are different values, and collapsing them would silently drop one. A field with
 * no qualifier tokenises to its bare name, so every field that existed before qualifiers behaves as it always did.
 */
final class FieldLookup {

    /**
     * Separator between a field name and its qualifier. A unit separator is used because neither a field
     * name nor a qualifier may contain a control character, so the token cannot be ambiguous.
     */
    private static final char SEPARATOR = (char) 0x1F;

    private FieldLookup() {

    }

    /**
     * Build the token identifying a field within one evaluation.
     *
     * @param name Field name.
     * @param qualifier  Qualifier selecting a value within that field, or null when the field is not qualified.
     * @return Token that distinguishes this field from every other in the same rule.
     */
    static String token(String name, String qualifier) {

        return qualifier == null ? name : name + SEPARATOR + qualifier;
    }
}
