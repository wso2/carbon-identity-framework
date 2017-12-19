/*
 *
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.identity.mgt.endpoint.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;


/**
 * Claim
 */
public class Claim {

    private String uri = null;
    private String value = null;
    private String description = null;
    private String displayName = null;
    private String dialect = null;
    private Boolean required = null;
    private Boolean readOnly = null;
    private String validationRegex = null;


    /**
     **/
    public Claim uri(String uri) {
        this.uri = uri;
        return this;
    }


    @JsonProperty("uri")
    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }


    /**
     **/
    public Claim value(String value) {
        this.value = value;
        return this;
    }


    @JsonProperty("value")
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }


    /**
     **/
    public Claim description(String description) {
        this.description = description;
        return this;
    }


    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    /**
     **/
    public Claim displayName(String displayName) {
        this.displayName = displayName;
        return this;
    }


    @JsonProperty("display-name")
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }


    /**
     **/
    public Claim dialect(String dialect) {
        this.dialect = dialect;
        return this;
    }


    @JsonProperty("dialect")
    public String getDialect() {
        return dialect;
    }

    public void setDialect(String dialect) {
        this.dialect = dialect;
    }


    /**
     **/
    public Claim required(Boolean required) {
        this.required = required;
        return this;
    }


    @JsonProperty("required")
    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }


    /**
     **/
    public Claim readOnly(Boolean readOnly) {
        this.readOnly = readOnly;
        return this;
    }


    @JsonProperty("read-only")
    public Boolean getReadOnly() {
        return readOnly;
    }

    public void setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
    }

    /**
     **/
    @JsonProperty("validation-regex")
    public String getValidationRegex() {
        return validationRegex;
    }

    public void setValidationRegex(String validationRegex) {
        this.validationRegex = validationRegex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Claim claim = (Claim) o;
        return Objects.equals(this.uri, claim.uri) &&
                Objects.equals(this.value, claim.value) &&
                Objects.equals(this.description, claim.description) &&
                Objects.equals(this.displayName, claim.displayName) &&
                Objects.equals(this.dialect, claim.dialect) &&
                Objects.equals(this.required, claim.required) &&
                Objects.equals(this.readOnly, claim.readOnly);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri, value, description, displayName, dialect, required, readOnly);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Claim {\n");

        sb.append("    uri: ").append(toIndentedString(uri)).append("\n");
        sb.append("    value: ").append(toIndentedString(value)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
        sb.append("    dialect: ").append(toIndentedString(dialect)).append("\n");
        sb.append("    required: ").append(toIndentedString(required)).append("\n");
        sb.append("    readOnly: ").append(toIndentedString(readOnly)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}

