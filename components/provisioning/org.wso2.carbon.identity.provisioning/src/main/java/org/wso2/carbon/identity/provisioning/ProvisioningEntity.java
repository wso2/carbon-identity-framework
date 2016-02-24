/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.provisioning;

import org.wso2.carbon.identity.application.common.model.ClaimMapping;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class ProvisioningEntity implements Serializable {

    private static final long serialVersionUID = -7300897205165960442L;

    private ProvisioningEntityType entityType;
    private ProvisioningOperation operation;
    private ProvisionedIdentifier identifier;
    private String entityName;
    private boolean jitProvisioning;
    private Map<ClaimMapping, List<String>> attributes;
    private Map<String, String> inboundAttributes;

    /**
     *
     * @param entityType
     * @param entityName
     * @param operation
     * @param attributes
     */
    public ProvisioningEntity(ProvisioningEntityType entityType, String entityName,
                              ProvisioningOperation operation, Map<ClaimMapping, List<String>> attributes) {
        super();
        this.entityType = entityType;
        this.entityName = entityName;
        this.operation = operation;
        this.attributes = attributes;
    }

    /**
     *
     * @param entityTpe
     * @param operation
     */
    public ProvisioningEntity(ProvisioningEntityType entityTpe, ProvisioningOperation operation) {
        this.entityType = entityTpe;
        this.operation = operation;
    }

    /**
     *
     * @param entityTpe
     * @param operation
     */
    public ProvisioningEntity(ProvisioningEntityType entityTpe, ProvisioningOperation operation,
                              Map<ClaimMapping, List<String>> attributes) {
        this.entityType = entityTpe;
        this.operation = operation;
        this.attributes = attributes;
    }

    /**
     * @return
     */
    public ProvisioningEntityType getEntityType() {
        return entityType;
    }

    /**
     * @return
     */
    public ProvisioningOperation getOperation() {
        return operation;
    }

    /**
     * @return
     */
    public Map<ClaimMapping, List<String>> getAttributes() {
        return attributes;
    }

    /**
     * @return
     */
    public ProvisionedIdentifier getIdentifier() {
        return identifier;
    }

    /**
     * @param identifier
     */
    public void setIdentifier(ProvisionedIdentifier identifier) {
        this.identifier = identifier;
    }

    /**
     * @return
     */
    public Map<String, String> getInboundAttributes() {
        return inboundAttributes;
    }

    /**
     * @param inboundAttributes
     */
    public void setInboundAttributes(Map<String, String> inboundAttributes) {
        this.inboundAttributes = inboundAttributes;
    }

    /**
     * @return
     */
    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public boolean isJitProvisioning() {
        return jitProvisioning;
    }

    public void setJitProvisioning(boolean jitProvisioning) {
        this.jitProvisioning = jitProvisioning;
    }

}
