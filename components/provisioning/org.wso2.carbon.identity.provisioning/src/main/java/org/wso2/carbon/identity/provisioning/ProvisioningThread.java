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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.central.log.mgt.utils.LogConstants;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.provisioning.dao.CacheBackedProvisioningMgtDAO;
import org.wso2.carbon.idp.mgt.util.IdPManagementUtil;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.DiagnosticLog;

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
    private boolean jitProvisioningEnabledForIdP;
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

    /**
     * Parameterized constructor to propagate JIT provisioning state to the connector.
     *
     * @param provisioningEntity Provisioning entity containing details of the provisioning operation.
     * @param spTenantDomainName Service provider tenant domain name.
     * @param provisioningEntityTenantDomainName Tenant domain name of the provisioning entity.
     * @param connector Outbound provisioning connector to perform the provisioning operation.
     * @param connectorType Type of the outbound provisioning connector.
     * @param idPName Name of the identity provider through which provisioning is triggered.
     * @param dao Data access object to store and manage provisioning entity identifiers.
     * @param jitProvisioningEnabledForIdP Flag indicating whether JIT provisioning is enabled or not.
     */
    public ProvisioningThread(ProvisioningEntity provisioningEntity, String spTenantDomainName,
                              String provisioningEntityTenantDomainName,
                              AbstractOutboundProvisioningConnector connector, String connectorType, String idPName,
                              CacheBackedProvisioningMgtDAO dao, boolean jitProvisioningEnabledForIdP) {

        this(provisioningEntity, spTenantDomainName, provisioningEntityTenantDomainName, connector, connectorType,
                idPName, dao);
        this.jitProvisioningEnabledForIdP = jitProvisioningEnabledForIdP;
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
            if (provisioningEntity.isJitProvisioning() && !jitProvisioningEnabledForIdP) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Skipping outbound provisioning for entity: %s via IDP: %s, connector: " +
                            "%s. Reason: JIT provisioning is not enabled for this provisioning connector.",
                            ProvisioningUtil.maskIfRequired(provisioningEntity.getEntityName()), idPName,
                            connectorType));
                }
                if (LoggerUtils.isDiagnosticLogsEnabled()) {
                    LoggerUtils.triggerDiagnosticLogEvent(new DiagnosticLog.DiagnosticLogBuilder(
                            LogConstants.OutboundProvisioning.OUTBOUND_PROVISIONING_COMPONENT,
                            LogConstants.OutboundProvisioning.EXECUTE_OUTBOUND_PROVISIONING)
                            .inputParam(LogConstants.OutboundProvisioning.CONNECTION, idPName)
                            .inputParam(LogConstants.OutboundProvisioning.CONNECTOR_TYPE, connectorType)
                            .inputParam(LogConstants.OutboundProvisioning.ENTITY_NAME,
                                    ProvisioningUtil.maskIfRequired(provisioningEntity.getEntityName()))
                            .resultMessage("Skipping outbound provisioning. JIT provisioning is not enabled" +
                                    " for this connector.")
                            .logDetailLevel(DiagnosticLog.LogDetailLevel.APPLICATION)
                            .resultStatus(DiagnosticLog.ResultStatus.SUCCESS));
                }
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
            if (LoggerUtils.isDiagnosticLogsEnabled()) {
                String actionId = provisioningEntity.getEntityType() == ProvisioningEntityType.GROUP
                        ? LogConstants.OutboundProvisioning.PROVISION_GROUP
                        : LogConstants.OutboundProvisioning.PROVISION_USER;
                DiagnosticLog.DiagnosticLogBuilder diagLogBuilder = new DiagnosticLog.DiagnosticLogBuilder(
                        LogConstants.OutboundProvisioning.OUTBOUND_PROVISIONING_COMPONENT, actionId);
                diagLogBuilder.inputParam(LogConstants.OutboundProvisioning.CONNECTION, idPName)
                        .inputParam(LogConstants.OutboundProvisioning.CONNECTOR_TYPE, connectorType)
                        .inputParam(LogConstants.OutboundProvisioning.ENTITY_NAME,
                                ProvisioningUtil.maskIfRequired(provisioningEntity.getEntityName()))
                        .inputParam(LogConstants.OutboundProvisioning.ENTITY_TYPE,
                                provisioningEntity.getEntityType() != null
                                        ? provisioningEntity.getEntityType().toString() : null)
                        .inputParam(LogConstants.OutboundProvisioning.PROVISIONING_OPERATION,
                                provisioningEntity.getOperation() != null
                                        ? provisioningEntity.getOperation().toString() : null);
                if (provisionedIdentifier != null && provisionedIdentifier.getIdentifier() != null) {
                    diagLogBuilder.inputParam(LogConstants.OutboundProvisioning.PROVISIONED_IDENTIFIER,
                            provisionedIdentifier.getIdentifier());
                }
                diagLogBuilder.resultMessage("Outbound provisioning completed successfully.")
                        .logDetailLevel(DiagnosticLog.LogDetailLevel.APPLICATION)
                        .resultStatus(DiagnosticLog.ResultStatus.SUCCESS);
                LoggerUtils.triggerDiagnosticLogEvent(diagLogBuilder);
            }
        } catch (Exception e) {
            if (LoggerUtils.isDiagnosticLogsEnabled()) {
                String actionId = provisioningEntity.getEntityType() == ProvisioningEntityType.GROUP
                        ? LogConstants.OutboundProvisioning.PROVISION_GROUP
                        : LogConstants.OutboundProvisioning.PROVISION_USER;
                LoggerUtils.triggerDiagnosticLogEvent(new DiagnosticLog.DiagnosticLogBuilder(
                        LogConstants.OutboundProvisioning.OUTBOUND_PROVISIONING_COMPONENT, actionId)
                        .inputParam(LogConstants.OutboundProvisioning.CONNECTION, idPName)
                        .inputParam(LogConstants.OutboundProvisioning.CONNECTOR_TYPE, connectorType)
                        .inputParam(LogConstants.OutboundProvisioning.ENTITY_NAME,
                                ProvisioningUtil.maskIfRequired(provisioningEntity.getEntityName()))
                        .inputParam(LogConstants.OutboundProvisioning.ENTITY_TYPE,
                                provisioningEntity.getEntityType() != null
                                        ? provisioningEntity.getEntityType().toString() : null)
                        .inputParam(LogConstants.OutboundProvisioning.PROVISIONING_OPERATION,
                                provisioningEntity.getOperation() != null
                                        ? provisioningEntity.getOperation().toString() : null)
                        .resultMessage("Outbound provisioning failed.")
                        .logDetailLevel(DiagnosticLog.LogDetailLevel.APPLICATION)
                        .resultStatus(DiagnosticLog.ResultStatus.FAILED));
            }
            String maskedEntityName = ProvisioningUtil.maskIfRequired(provisioningEntity.getEntityName());
            String errMsg = "Outbound provisioning failed for connection: " + idPName + ", connector: " + connectorType
                    + ", entity: " + maskedEntityName + ", entity type: " + provisioningEntity.getEntityType()
                    + ", operation: " + provisioningEntity.getOperation();
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
