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

package org.wso2.carbon.identity.mgt.endpoint.util.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

/**
 * Local claim model that includes properties from the claim management API response.
 */
public class LocalClaim {

    private String id = null;
    private String claimURI = null;
    private String dialectURI = null;
    private String description = null;
    private Integer displayOrder = null;
    private String displayName = null;
    private Boolean required = null;
    private Boolean readOnly = null;
    private String regEx = null;
    private Boolean supportedByDefault = null;
    private String dataType = null;
    private Boolean multiValued = null;
    private List<Property> properties = null;
    private List<LabelValue> canonicalValues = null;
    private InputFormat inputFormat = null;

    /**
     * Gets the unique identifier for the claim.
     *
     * @return The claim identifier.
     */
    @JsonProperty("id")
    public String getId() {

        return id;
    }

    /**
     * Sets the unique identifier for the claim.
     *
     * @param id The claim identifier.
     */
    public void setId(String id) {

        this.id = id;
    }

    /**
     * Gets the claim URI.
     *
     * @return The claim URI.
     */
    @JsonProperty("claimURI")
    public String getClaimURI() {

        return claimURI;
    }

    /**
     * Sets the claim URI.
     *
     * @param claimURI The claim URI.
     */
    public void setClaimURI(String claimURI) {

        this.claimURI = claimURI;
    }

    /**
     * Gets the dialect URI.
     *
     * @return The dialect URI.
     */
    @JsonProperty("dialectURI")
    public String getDialectURI() {

        return dialectURI;
    }

    /**
     * Sets the dialect URI.
     *
     * @param dialectURI The dialect URI.
     */
    public void setDialectURI(String dialectURI) {

        this.dialectURI = dialectURI;
    }

    /**
     * Gets the claim description.
     *
     * @return The claim description.
     */
    @JsonProperty("description")
    public String getDescription() {

        return description;
    }

    /**
     * Sets the claim description.
     *
     * @param description The claim description.
     */
    public void setDescription(String description) {

        this.description = description;
    }

    /**
     * Gets the display order of the claim.
     *
     * @return The display order.
     */
    @JsonProperty("displayOrder")
    public Integer getDisplayOrder() {

        return displayOrder;
    }

    /**
     * Sets the display order of the claim.
     *
     * @param displayOrder The display order.
     */
    public void setDisplayOrder(Integer displayOrder) {

        this.displayOrder = displayOrder;
    }

    /**
     * Gets the display name of the claim.
     *
     * @return The display name.
     */
    @JsonProperty("displayName")
    public String getDisplayName() {

        return displayName;
    }

    /**
     * Sets the display name of the claim.
     *
     * @param displayName The display name.
     */
    public void setDisplayName(String displayName) {

        this.displayName = displayName;
    }

    /**
     * Gets whether the claim is required.
     *
     * @return True if the claim is required, false otherwise.
     */
    @JsonProperty("required")
    public Boolean getRequired() {

        return required;
    }

    /**
     * Sets whether the claim is required.
     *
     * @param required True if the claim is required, false otherwise.
     */
    public void setRequired(Boolean required) {

        this.required = required;
    }

    /**
     * Gets whether the claim is read-only.
     *
     * @return True if the claim is read-only, false otherwise.
     */
    @JsonProperty("readOnly")
    public Boolean getReadOnly() {

        return readOnly;
    }

    /**
     * Sets whether the claim is read-only.
     *
     * @param readOnly True if the claim is read-only, false otherwise.
     */
    public void setReadOnly(Boolean readOnly) {

        this.readOnly = readOnly;
    }

    /**
     * Gets the regular expression for claim validation.
     *
     * @return The validation regex.
     */
    @JsonProperty("regEx")
    public String getRegEx() {

        return regEx;
    }

    /**
     * Sets the regular expression for claim validation.
     *
     * @param regEx The validation regex.
     */
    public void setRegEx(String regEx) {

        this.regEx = regEx;
    }

    /**
     * Gets the flag indicating if the claim is supported by default.
     *
     * @return True if supported by default, false otherwise.
     */
    @JsonProperty("supportedByDefault")
    public Boolean getSupportedByDefault() {

        return supportedByDefault;
    }

    /**
     * Sets the flag indicating if the claim is supported by default.
     *
     * @param supportedByDefault True if supported by default, false otherwise.
     */
    public void setSupportedByDefault(Boolean supportedByDefault) {

        this.supportedByDefault = supportedByDefault;
    }

    /**
     * Gets the data type of the claim.
     *
     * @return The data type.
     */
    @JsonProperty("dataType")
    public String getDataType() {

        return dataType;
    }

    /**
     * Sets the data type of the claim.
     *
     * @param dataType The data type.
     */
    public void setDataType(String dataType) {

        this.dataType = dataType;
    }

    /**
     * Gets the flag indicating if the claim can have multiple values.
     *
     * @return True if multi-valued, false otherwise.
     */
    @JsonProperty("multiValued")
    public Boolean getMultiValued() {

        return multiValued;
    }

    /**
     * Sets the flag indicating if the claim can have multiple values.
     *
     * @param multiValued True if multi-valued, false otherwise.
     */
    public void setMultiValued(Boolean multiValued) {

        this.multiValued = multiValued;
    }

    /**
     * Gets the list of properties associated with the claim.
     *
     * @return The list of properties.
     */
    @JsonProperty("properties")
    public List<Property> getProperties() {

        return properties;
    }

    /**
     * Sets the list of properties associated with the claim.
     *
     * @param properties The list of properties.
     */
    public void setProperties(List<Property> properties) {

        this.properties = properties;
    }

    /**
     * Gets the list of canonical values for the claim.
     *
     * @return The list of canonical values.
     */
    @JsonProperty("canonicalValues")
    public List<LabelValue> getCanonicalValues() {

        return canonicalValues;
    }

    /**
     * Sets the list of canonical values for the claim.
     *
     * @param canonicalValues The list of canonical values.
     */
    public void setCanonicalValues(List<LabelValue> canonicalValues) {

        this.canonicalValues = canonicalValues;
    }

    /**
     * Gets the input format for the claim.
     *
     * @return The input format.
     */
    @JsonProperty("inputFormat")
    public InputFormat getInputFormat() {

        return inputFormat;
    }

    /**
     * Sets the input format for the claim.
     *
     * @param inputFormat The input format.
     */
    public void setInputFormat(InputFormat inputFormat) {

        this.inputFormat = inputFormat;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LocalClaim that = (LocalClaim) o;
        return Objects.equals(this.id, that.id) &&
                Objects.equals(this.claimURI, that.claimURI) &&
                Objects.equals(this.dialectURI, that.dialectURI) &&
                Objects.equals(this.description, that.description) &&
                Objects.equals(this.displayOrder, that.displayOrder) &&
                Objects.equals(this.displayName, that.displayName) &&
                Objects.equals(this.required, that.required) &&
                Objects.equals(this.readOnly, that.readOnly) &&
                Objects.equals(this.regEx, that.regEx) &&
                Objects.equals(this.supportedByDefault, that.supportedByDefault) &&
                Objects.equals(this.dataType, that.dataType) &&
                Objects.equals(this.multiValued, that.multiValued) &&
                Objects.equals(this.properties, that.properties) &&
                Objects.equals(this.canonicalValues, that.canonicalValues) &&
                Objects.equals(this.inputFormat, that.inputFormat);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, claimURI, dialectURI, description, displayOrder, displayName, required, readOnly, regEx,
                supportedByDefault, dataType, multiValued, properties, canonicalValues, inputFormat);
    }

    @Override
    public String toString() {

        return "LocalClaim{" +
                "id='" + id + '\'' +
                ", claimURI='" + claimURI + '\'' +
                ", dialectURI='" + dialectURI + '\'' +
                ", description='" + description + '\'' +
                ", displayOrder=" + displayOrder +
                ", displayName='" + displayName + '\'' +
                ", required=" + required +
                ", readOnly=" + readOnly +
                ", regEx='" + regEx + '\'' +
                ", supportedByDefault=" + supportedByDefault +
                ", dataType='" + dataType + '\'' +
                ", multiValued=" + multiValued +
                ", properties=" + properties +
                ", canonicalValues=" + canonicalValues +
                ", inputFormat=" + inputFormat +
                '}';
    }
}
