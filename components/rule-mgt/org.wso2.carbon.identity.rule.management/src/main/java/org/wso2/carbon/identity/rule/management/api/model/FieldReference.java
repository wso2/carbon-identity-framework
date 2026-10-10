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

package org.wso2.carbon.identity.rule.management.api.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * A reference to a field whose value is read when the rule is evaluated, rather than given in the rule.
 * <p>
 * Named the same way the left-hand side of an expression is: a field, and for a field that names a family of
 * values, the qualifier that picks one of them.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FieldReference implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final String qualifier;

    @JsonCreator
    public FieldReference(@JsonProperty("name") String name, @JsonProperty("qualifier") String qualifier) {

        this.name = name;
        this.qualifier = qualifier;
    }

    @JsonProperty("name")
    public String getName() {

        return name;
    }

    @JsonProperty("qualifier")
    public String getQualifier() {

        return qualifier;
    }
}
