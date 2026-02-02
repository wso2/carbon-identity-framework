/*
 * Copyright (c) 2014-2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.provisioning;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.provisioning.dao.CacheBackedProvisioningMgtDAO;
import org.wso2.carbon.idp.mgt.util.IdPManagementUtil;
import org.wso2.carbon.user.api.UserStoreException;
import java.util.UUID;
import java.util.concurrent.Callable;

import static org.wso2.carbon.identity.provisioning.ProvisioningUtil.isUserTenantBasedOutboundProvisioningEnabled;

public class ProvisioningThread implements Callable<Boolean> {

    private ProvisioningEntity provisioningEntity;
    private String tenantDomainName;
    private String provisioningEntityTenantDomainName;
    private AbstractOutboundProvisioningConnector connector;
    private String connectorType;
    private String idPName;
    private CacheBackedProvisioningMgtDAO dao;
    private static final Log log = LogFactory.getLog(ProvisioningThread.class);

    public ProvisioningThread(ProvisioningEntity provisioningEntity, String tenantDomainName,
                              AbstractOutboundProvisioningConnector connector, String connectorType, String idPName,
                              CacheBackedProvisioningMgtDAO dao) {
        super();
        this.provisioningEntity = provisioningEntity;
        this.tenantDomainName = tenantDomainName;
        this.connector = connector;
        this.connectorType = connectorType;
        this.idPName = idPName;
        this.dao = dao;
    }

    public ProvisioningThread(ProvisioningEntity provisioningEntity, String spTenantDomainName,
                              String provisioningEntityTenantDomainName,
                              AbstractOutboundProvisioningConnector connector, String connectorType, String idPName,
                              CacheBackedProvisioningMgtDAO dao) {

        this(provisioningEntity, spTenantDomainName, connector, connectorType, idPName, dao);
        this.provisioningEntityTenantDomainName = provisioningEntityTenantDomainName;
    }

    @Override
    public Boolean call() throws IdentityProvisioningException {

        boolean success = false;
        String tenantDomainName = this.tenantDomainName;
        String provisioningEntityTenantDomainName = this.provisioningEntityTenantDomainName;
        boolean isUserTenantBasedOutboundProvisioningEnabled = isUserTenantBasedOutboundProvisioningEnabled();

        try {

            PrivilegedCarbonContext.startTenantFlow();
            if (isUserTenantBasedOutboundProvisioningEnabled && provisioningEntityTenantDomainName != null) {
                PrivilegedCarbonContext.getThreadLocalCarbonContext()
                        .setTenantDomain(provisioningEntityTenantDomainName, true);
            } else {
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomainName, true);
            }

            /* Skip outbound provisioning triggered for JIT provisioning flow, where the JIT outbound is disabled for
               the configured connector. */
            if (provisioningEntity.isJitProvisioning() && !connector.isJitProvisioningEnabled()) {
                return true;
            }
            ProvisionedIdentifier provisionedIdentifier = null;
            // real provisioning happens now.
            provisionedIdentifier = connector.provision(provisioningEntity);

            if (provisioningEntity.getOperation() == ProvisioningOperation.DELETE) {
                deleteProvisionedEntityIdentifier(idPName, connectorType, provisioningEntity,
                        tenantDomainName);
            } else if (provisioningEntity.getOperation() == ProvisioningOperation.POST) {

                if (provisionedIdentifier == null || provisionedIdentifier.getIdentifier() == null) {
                    provisionedIdentifier = new ProvisionedIdentifier();
                    provisionedIdentifier.setIdentifier(UUID.randomUUID().toString());
                }

                provisioningEntity.setIdentifier(provisionedIdentifier);

                // store provisioned identifier for future reference.
                storeProvisionedEntityIdentifier(idPName, connectorType, provisioningEntity,
                        tenantDomainName);
            } else if (provisioningEntity.getEntityType() == ProvisioningEntityType.GROUP &&
                    (provisioningEntity.getOperation() == ProvisioningOperation.PUT
                            || provisioningEntity.getOperation() == ProvisioningOperation.PATCH)) {
                String newGroupName = ProvisioningUtil.getAttributeValue(provisioningEntity,
                                                                IdentityProvisioningConstants.NEW_GROUP_NAME_CLAIM_URI);
                if (newGroupName != null){
                    // update provisioned entity name for future reference. this is applicable for only
                    // group name update
                    dao.updateProvisionedEntityName(provisioningEntity);
                }
            }
            success = true;
        } catch (Exception e) {
            String errMsg = "Fail the Provisioning for Entity " + provisioningEntity.getEntityName() +
                    " For operation = " + provisioningEntity.getOperation();
            log.warn(errMsg);
            throw new IdentityProvisioningException(errMsg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();

            if (isUserTenantBasedOutboundProvisioningEnabled && provisioningEntityTenantDomainName != null) {
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(provisioningEntityTenantDomainName, true);
            }else if (tenantDomainName != null) {
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomainName, true);
            }
        }

        return success;
    }

    /**
     * @param idpName
     * @param connectorType
     * @param provisioningEntity
     * @param tenantDomain
     * @throws IdentityApplicationManagementException
     */
    private void storeProvisionedEntityIdentifier(String idpName, String connectorType,
                                                  ProvisioningEntity provisioningEntity, String tenantDomain)
            throws IdentityApplicationManagementException {
        int tenantId;
        try {
            tenantId = IdPManagementUtil.getTenantIdOfDomain(tenantDomain);
            dao.addProvisioningEntity(idpName, connectorType, provisioningEntity, tenantId, tenantDomain);
        } catch (UserStoreException e) {
            throw new IdentityApplicationManagementException(
                    "Error while storing provisioning identifier.", e);
        }
    }

    /**
     * @param idpName
     * @param connectorType
     * @param provisioningEntity
     * @param tenantDomain
     * @return
     * @throws IdentityApplicationManagementException
     */
    private void deleteProvisionedEntityIdentifier(String idpName, String connectorType,
                                                   ProvisioningEntity provisioningEntity, String tenantDomain)
            throws IdentityApplicationManagementException {
        int tenantId;
        try {
            tenantId = IdPManagementUtil.getTenantIdOfDomain(tenantDomain);
            dao.deleteProvisioningEntity(idpName, connectorType, provisioningEntity, tenantId, tenantDomain);
        } catch (UserStoreException e) {
            throw new IdentityApplicationManagementException(
                    "Error while deleting provisioning identifier.", e);
        }
    }
}
