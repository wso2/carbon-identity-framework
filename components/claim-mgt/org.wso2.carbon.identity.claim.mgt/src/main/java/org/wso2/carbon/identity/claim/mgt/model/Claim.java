/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.claim.mgt.model;
import java.util.Map;

public class Claim extends org.wso2.carbon.user.core.claim.Claim{

    /**
     * This is to store keep the custom defined Meta data, which is separated
     * from the default Meta data attributes
     */
    private Map<String,String> customMetaData;

    /**
     * This is to indicate that this is a read only attribute with a boolean value
     */

    private boolean readOnly;

    /**
     * This is to indicate that this is a checked attribute with a boolean value.
     */
    private boolean checkedAttribute;

    /**
     * This is the value that should be displayed on the UI - when claims are
     * taken to the front end.
     */
    private String displayTag;

    /**
     * This is a detailed description of the claim.
     */
    private String description;

    /**
     * This are claim asked at the time of registration. Privileged users will
     * be able to mark claims those are not supported by default as 'supported'
     * later. This is a sub-set of the claims read from the claim-config.xml.
     */
    private boolean supportedByDefault;

    /**
     * These are the claims required at the time user registration.
     */
    private boolean required;

    /**
     * Regular expression to validate the claim value - if nothing is provided, no
     * validation will take place.
     */
    private String regEx;

    /**
     * Dialect URI for the claim. Dialect will be useful to group claims.
     */
    private String dialectURI;

    /**
     * This is the value of the claim
     */
    private String value;

    /**
     * This is the display order in the Carbon UI framework
     */
    private int displayOrder;

    public String getDisplayTag() {
        return displayTag;
    }

    public void setDisplayTag(String displayTag) {
        this.displayTag = displayTag;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isSupportedByDefault() {
        return supportedByDefault;
    }

    public void setSupportedByDefault(boolean supportedByDefault) {
        this.supportedByDefault = supportedByDefault;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getRegEx() {
        return regEx;
    }

    public void setRegEx(String regEx) {
        this.regEx = regEx;
    }

    public String getDialectURI() {
        return dialectURI;
    }

    public void setDialectURI(String dialectURI) {
        this.dialectURI = dialectURI;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean isCheckedAttribute() {
        return checkedAttribute;
    }

    public void setCheckedAttribute(boolean checkedAttribute) {
        this.checkedAttribute = checkedAttribute;
    }

    public Map<String, String> getCustomMetaData() {
        return customMetaData;
    }

    public void setCustomMetaData(Map<String, String> customMetaData) {
        this.customMetaData = customMetaData;
    }
}
