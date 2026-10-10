/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.StringUtils;

/**
 * Represents a field in a rule.
 */
public class Field {

    private final String name;
    private final String displayName;
    private final Value qualifier;

    @JsonCreator
    public Field(@JsonProperty("name") String name, @JsonProperty("displayName") String displayName) {

        this(name, displayName, null);
    }

    /**
     * A field that names a family of values rather than one -- a claim, for instance -- and so needs a qualifier
     * to say which of its values a rule means.
     *
     * @param name        The field name.
     * @param displayName The name shown for the field.
     * @param qualifier   How the qualifier is entered, or null for a field naming a single value.
     */
    public Field(String name, String displayName, Value qualifier) {

        validate(name);
        this.name = name;
        this.displayName = displayName;
        this.qualifier = qualifier;
    }

    public String getName() {

        return name;
    }

    public String getDisplayName() {

        return displayName;
    }

    /**
     * How a qualifier is chosen for this field, for fields that name a family of values rather than one --
     * a claim, for instance, where the qualifier is the claim URI. Absent on fields that name one value.
     *
     * @return Meta describing how to supply the qualifier, or null when the field is not qualified.
     */
    public Value getQualifier() {

        return qualifier;
    }

    private void validate(String name) {

        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("Field 'name' cannot be null or empty.");
        }
    }
}
