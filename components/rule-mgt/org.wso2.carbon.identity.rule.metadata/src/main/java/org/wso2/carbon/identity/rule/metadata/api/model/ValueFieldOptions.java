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

package org.wso2.carbon.identity.rule.metadata.api.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The other fields a field's value may be read from when the rule is evaluated, instead of being given in the rule.
 * <p>
 * Kept apart from {@link Value}, which describes how a value given in the rule is entered: a field that can be
 * compared with others takes both, so the two sit side by side on the {@link FieldDefinition}.
 */
public class ValueFieldOptions {

    private final List<String> names;

    public ValueFieldOptions(List<String> names) {

        if (names == null || names.isEmpty()) {
            throw new IllegalArgumentException("Value field names cannot be null or empty.");
        }
        this.names = Collections.unmodifiableList(new ArrayList<>(names));
    }

    /**
     * The names of the fields a value may be read from.
     *
     * @return The field names, never empty.
     */
    public List<String> getNames() {

        return names;
    }
}
