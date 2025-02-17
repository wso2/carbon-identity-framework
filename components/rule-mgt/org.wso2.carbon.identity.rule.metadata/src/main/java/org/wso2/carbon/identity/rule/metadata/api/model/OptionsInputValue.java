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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Represents a value meta of a field of options input type.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OptionsInputValue extends Value {

    @JsonIgnore
    private InputType inputType;
    private final List<OptionsValue> values;

    @JsonCreator
    public OptionsInputValue(@JsonProperty("valueType") ValueType valueType,
                             @JsonProperty("values") List<OptionsValue> values) {

        super(InputType.OPTIONS, valueType);
        this.values = values;
    }

    public List<OptionsValue> getValues() {

        return values;
    }
}
