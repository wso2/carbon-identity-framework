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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.rule.metadata.internal.deserializer.ValueTypeDeserializer;

import java.util.List;

/**
 * Represents a value meta of a field of options reference type.
 */
@JsonDeserialize(builder = OptionsReferenceValue.Builder.class)
public class OptionsReferenceValue extends Value {

    private final String valueReferenceAttribute;
    private final String valueDisplayAttribute;
    private final List<Link> links;

    private OptionsReferenceValue(Builder builder) {

        super(InputType.OPTIONS, builder.valueType);
        this.valueReferenceAttribute = builder.valueReferenceAttribute;
        this.valueDisplayAttribute = builder.valueDisplayAttribute;
        this.links = builder.links;
    }

    public String getValueReferenceAttribute() {

        return valueReferenceAttribute;
    }

    public String getValueDisplayAttribute() {

        return valueDisplayAttribute;
    }

    public List<Link> getLinks() {

        return links;
    }

    /**
     * Builder for {@link OptionsReferenceValue}.
     */
    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Builder {

        @JsonIgnore
        private InputType inputType;
        @JsonDeserialize(using = ValueTypeDeserializer.class)
        private ValueType valueType;
        private String valueReferenceAttribute;
        private String valueDisplayAttribute;
        private List<Link> links;

        @JsonProperty("valueType")
        public Builder valueType(ValueType valueType) {

            this.valueType = valueType;
            return this;
        }

        @JsonProperty("valueReferenceAttribute")
        public Builder valueReferenceAttribute(String valueReferenceAttribute) {

            this.valueReferenceAttribute = valueReferenceAttribute;
            return this;
        }

        @JsonProperty("valueDisplayAttribute")
        public Builder valueDisplayAttribute(String valueDisplayAttribute) {

            this.valueDisplayAttribute = valueDisplayAttribute;
            return this;
        }

        @JsonProperty("links")
        public Builder links(List<Link> links) {

            this.links = links;
            return this;
        }

        public OptionsReferenceValue build() {

            if (StringUtils.isBlank(valueReferenceAttribute)) {
                throw new IllegalArgumentException("'valueReferenceAttribute' cannot be empty.");
            }

            if (StringUtils.isBlank(valueDisplayAttribute)) {
                throw new IllegalArgumentException("'valueDisplayAttribute' cannot be empty.");
            }

            return new OptionsReferenceValue(this);
        }
    }
}
