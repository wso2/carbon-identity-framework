/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.identity.configuration.mgt.core.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.configuration.mgt.core.search.PrimitiveCondition;
import org.wso2.carbon.identity.configuration.mgt.core.search.SearchBean;
import org.wso2.carbon.identity.configuration.mgt.core.search.exception.PrimitiveConditionValidationException;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;

import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.RESOURCE_SEARCH_BEAN_FIELD_ATTRIBUTE_KEY;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.RESOURCE_SEARCH_BEAN_FIELD_ATTRIBUTE_VALUE;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.RESOURCE_SEARCH_BEAN_FIELD_RESOURCE_ID;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.RESOURCE_SEARCH_BEAN_FIELD_RESOURCE_NAME;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.RESOURCE_SEARCH_BEAN_FIELD_RESOURCE_TYPE_ID;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.RESOURCE_SEARCH_BEAN_FIELD_RESOURCE_TYPE_NAME;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.RESOURCE_SEARCH_BEAN_FIELD_TENANT_DOMAIN;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.RESOURCE_SEARCH_BEAN_FIELD_TENANT_ID;

public class ResourceSearchBean implements SearchBean {

    private static final Log log = LogFactory.getLog(ResourceSearchBean.class);
    private int tenantId;
    private String tenantDomain;
    private String resourceTypeId;
    private String resourceTypeName;
    private String resourceId;
    private String resourceName;
    private String attributeKey;
    private String attributeValue;

    /**
     * Map field name to the DB table identifier.
     *
     * @return
     */
    public String getDBQualifiedFieldName(String fieldName) {

        String dbQualifiedFieldName = null;
        switch (fieldName) {
            case RESOURCE_SEARCH_BEAN_FIELD_TENANT_ID:
                dbQualifiedFieldName = "R.TENANT_ID";
                break;
            case RESOURCE_SEARCH_BEAN_FIELD_RESOURCE_TYPE_ID:
                dbQualifiedFieldName = "T.ID";
                break;
            case RESOURCE_SEARCH_BEAN_FIELD_RESOURCE_TYPE_NAME:
                dbQualifiedFieldName = "T.NAME";
                break;
            case RESOURCE_SEARCH_BEAN_FIELD_RESOURCE_ID:
                dbQualifiedFieldName = "R.ID";
                break;
            case RESOURCE_SEARCH_BEAN_FIELD_RESOURCE_NAME:
                dbQualifiedFieldName = "R.NAME";
                break;
            case RESOURCE_SEARCH_BEAN_FIELD_ATTRIBUTE_KEY:
                dbQualifiedFieldName = "A.ATTR_KEY";
                break;
            case RESOURCE_SEARCH_BEAN_FIELD_ATTRIBUTE_VALUE:
                dbQualifiedFieldName = "A.ATTR_VALUE";
                break;
        }
        return dbQualifiedFieldName;
    }

    /**
     * This method allow mapping of {@link PrimitiveCondition}.
     *
     * @param primitiveCondition Primitive search expression to be mapped.
     */
    public PrimitiveCondition mapPrimitiveCondition(PrimitiveCondition primitiveCondition)
            throws PrimitiveConditionValidationException {

        // Map tenant domain to tenant id
        if (primitiveCondition.getProperty().equals(RESOURCE_SEARCH_BEAN_FIELD_TENANT_DOMAIN)) {
            try {
                primitiveCondition.setValue(IdentityTenantUtil.getTenantId(
                        (String) primitiveCondition.getValue()
                ));
            } catch (IdentityRuntimeException e) {
                if (log.isDebugEnabled()) {
                    log.debug(
                            "Error while retrieving tenant id for the tenant domain: "
                                    + primitiveCondition.getValue() + ".", e
                    );
                }
                throw new PrimitiveConditionValidationException(
                        "Unable to retrieve the tenant for the " + RESOURCE_SEARCH_BEAN_FIELD_TENANT_DOMAIN + ": "
                                + primitiveCondition.getValue()
                );
            }
            primitiveCondition.setProperty(RESOURCE_SEARCH_BEAN_FIELD_TENANT_ID);
        }
        return primitiveCondition;
    }

    public String getTenantDomain() {

        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {

        this.tenantDomain = tenantDomain;
    }

    public int getTenantId() {

        return tenantId;
    }

    public void setTenantId(int tenantId) {

        this.tenantId = tenantId;
    }

    public String getResourceTypeId() {

        return resourceTypeId;
    }

    public void setResourceTypeId(String resourceTypeId) {

        this.resourceTypeId = resourceTypeId;
    }

    public String getResourceTypeName() {

        return resourceTypeName;
    }

    public void setResourceTypeName(String resourceTypeName) {

        this.resourceTypeName = resourceTypeName;
    }

    public String getResourceId() {

        return resourceId;
    }

    public void setResourceId(String resourceId) {

        this.resourceId = resourceId;
    }

    public String getResourceName() {

        return resourceName;
    }

    public void setResourceName(String resourceName) {

        this.resourceName = resourceName;
    }

    public String getAttributeKey() {

        return attributeKey;
    }

    public void setAttributeKey(String attributeKey) {

        this.attributeKey = attributeKey;
    }

    public String getAttributeValue() {

        return attributeValue;
    }

    public void setAttributeValue(String attributeValue) {

        this.attributeValue = attributeValue;
    }
}
