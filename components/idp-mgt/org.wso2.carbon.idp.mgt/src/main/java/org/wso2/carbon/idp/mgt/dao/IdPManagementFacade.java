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

package org.wso2.carbon.idp.mgt.dao;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.model.*;
import org.wso2.carbon.identity.base.AuthenticatorPropertyConstants;
import org.wso2.carbon.identity.core.model.ExpressionNode;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementClientException;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementServerException;
import org.wso2.carbon.idp.mgt.model.ConnectedAppsResult;
import org.wso2.carbon.idp.mgt.util.IdPManagementConstants;
import org.wso2.carbon.idp.mgt.util.UserDefinedAuthenticatorEndpointConfigManager;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IdPManagementFacade {

    private final IdPManagementDAO dao;
    private final UserDefinedAuthenticatorEndpointConfigManager endpointConfigurationManager =
            new UserDefinedAuthenticatorEndpointConfigManager();
    private static final Log LOG = LogFactory.getLog(IdPManagementFacade.class);

    public IdPManagementFacade(IdPManagementDAO dao) {

        this.dao = dao;
    }

    public List<IdentityProvider> getIdPs(Connection dbConnection, int tenantId, String tenantDomain)
            throws IdentityProviderManagementException {

        return dao.getIdPs(dbConnection, tenantId, tenantDomain);
    }

    public List<IdentityProvider> getIdPsSearch(int tenantId, List<ExpressionNode> expressionConditions,
                                                int limit, int offset, String sortOrder, String sortBy,
                                                List<String> requiredAttributes)
            throws IdentityProviderManagementServerException, IdentityProviderManagementClientException {

        return dao.getIdPsSearch(tenantId, expressionConditions, limit, offset, sortOrder, sortBy, requiredAttributes);
    }

    public List<IdentityProvider> getIdPsSearch(int tenantId, List<ExpressionNode> expressionConditions,
                                                int limit, int offset, String sortOrder, String sortBy)
            throws IdentityProviderManagementServerException, IdentityProviderManagementClientException {

        return dao.getIdPsSearch(tenantId, expressionConditions, limit, offset, sortOrder,
                sortBy);
    }

    public List<IdentityProvider> getIdPsSearch(Connection dbConnection, int tenantId, String tenantDomain,
                                                String filter) throws IdentityProviderManagementException {

        return dao.getIdPsSearch(dbConnection, tenantId, tenantDomain, filter);
    }

    public List<IdentityProvider> getTrustedTokenIssuerSearch(int tenantId, List<ExpressionNode> expressionNode,
            int limit, int offset, String sortOrder, String sortBy, List<String> requiredAttributes)
            throws IdentityProviderManagementServerException, IdentityProviderManagementClientException {

        return dao.getTrustedTokenIssuerSearch(tenantId, expressionNode, limit, offset, sortOrder, sortBy,
                requiredAttributes);
    }

    public int getCountOfFilteredTokenIssuers(int tenantId, List<ExpressionNode> expressionConditions)
            throws IdentityProviderManagementServerException, IdentityProviderManagementClientException {

        return dao.getCountOfFilteredTokenIssuers(tenantId, expressionConditions);
    }

    public int getCountOfFilteredIdPs(int tenantId, List<ExpressionNode> expressionConditions)
            throws IdentityProviderManagementServerException, IdentityProviderManagementClientException {

        return dao.getCountOfFilteredIdPs(tenantId, expressionConditions);
    }

    public IdentityProvider getIdPByName(Connection dbConnection, String idPName, int tenantId, String tenantDomain)
            throws IdentityProviderManagementException {

        return populateEndpointConfig(dao.getIdPByName(dbConnection, idPName, tenantId, tenantDomain), tenantDomain);
    }

    public IdentityProvider getIDPbyId(Connection dbConnection, int idpId, int tenantId, String tenantDomain)
            throws IdentityProviderManagementException {

        return populateEndpointConfig(dao.getIDPbyId(dbConnection, idpId, tenantId, tenantDomain), tenantDomain);
    }

    public IdentityProvider getIDPbyResourceId(Connection dbConnection, String resourceId, int tenantId,
                                               String tenantDomain) throws IdentityProviderManagementException {

        return populateEndpointConfig(dao.getIDPbyResourceId(dbConnection, resourceId, tenantId, tenantDomain),
                tenantDomain);
    }

    public String getIDPNameByResourceId(String resourceId) throws IdentityProviderManagementException {

        return dao.getIDPNameByResourceId(resourceId);
    }

    public IdentityProvider getIdPByAuthenticatorPropertyValue(Connection dbConnection, String property, String value,
                                                               String authenticator, int tenantId, String tenantDomain)
            throws IdentityProviderManagementException {

        return populateEndpointConfig(dao.getIdPByAuthenticatorPropertyValue(dbConnection, property, value,
                authenticator, tenantId, tenantDomain), tenantDomain);
    }

    public IdentityProvider getIdPByAuthenticatorPropertyValue(Connection dbConnection, String property, String value,
                                                               int tenantId, String tenantDomain)
            throws IdentityProviderManagementException {

        return populateEndpointConfig(dao.getIdPByAuthenticatorPropertyValue(dbConnection, property, value, tenantId,
                tenantDomain), tenantDomain);
    }

    public IdentityProvider getIdPByRealmId(String realmId, int tenantId, String tenantDomain)
            throws IdentityProviderManagementException {

        return populateEndpointConfig(dao.getIdPByRealmId(realmId, tenantId, tenantDomain), tenantDomain);
    }

    public IdentityProvider getEnabledIdPByRealmId(String realmId, int tenantId, String tenantDomain)
            throws IdentityProviderManagementException {

        return populateEndpointConfig(dao.getEnabledIdPByRealmId(realmId, tenantId, tenantDomain), tenantDomain);
    }

    public String addIdPWithResourceId(IdentityProvider identityProvider, int tenantId)
            throws IdentityProviderManagementException {

        String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
        addEndpointConfig(identityProvider, tenantDomain);
        try {
            return dao.addIdPWithResourceId(identityProvider, tenantId);
        } catch (IdentityProviderManagementException e) {
            deleteEndpointConfig(identityProvider, tenantDomain);
            throw e;
        }
    }

    public void updateIdPWithResourceId(String resourceId, IdentityProvider newIdentityProvider,
                                        IdentityProvider currentIdentityProvider, int tenantId)
            throws IdentityProviderManagementException {

        updateEndpointConfig(newIdentityProvider, currentIdentityProvider,
                IdentityTenantUtil.getTenantDomain(tenantId));
        try {
            dao.updateIdPWithResourceId(resourceId, newIdentityProvider, currentIdentityProvider, tenantId);
        } catch (IdentityProviderManagementException e) {
            updateEndpointConfig(currentIdentityProvider, newIdentityProvider,
                    IdentityTenantUtil.getTenantDomain(tenantId));
            throw e;
        }
    }

    public boolean isIdpReferredBySP(String idPName, int tenantId) throws IdentityProviderManagementException {

        return dao.isIdpReferredBySP(idPName, tenantId);
    }

    public boolean isAuthenticatorReferredBySP(String idpName, String authenticatorName, int tenantId) throws IdentityProviderManagementException {

        return dao.isAuthenticatorReferredBySP(idpName, authenticatorName, tenantId);
    }

    public boolean isOutboundConnectorReferredBySP(String idpName, String connectorName, int tenantId) throws IdentityProviderManagementException {

        return dao.isOutboundConnectorReferredBySP(idpName, connectorName, tenantId);
    }

    public void deleteIdP(String idPName, int tenantId, String tenantDomain)
            throws IdentityProviderManagementException {

        IdentityProvider identityProvider = getIdPByName(null, idPName, tenantId, tenantDomain);
        dao.deleteIdP(idPName, tenantId, tenantDomain);
        try {
            deleteEndpointConfig(identityProvider, tenantDomain);
        } catch (IdentityProviderManagementException e) {
            // Error will not be thrown, since the IDP is already deleted. But there will be stale action.
            LOG.warn(String.format(IdPManagementConstants.WarningMessage.WARN_STALE_IDP_ACTION, idPName));
        }
    }

    public void deleteIdPs(int tenantId) throws IdentityProviderManagementException {

        // TODO: Replace loops with batch operations once issue:https://github.com/wso2/product-is/issues/21783 is done.
        List<IdentityProvider> idpList = getIdPs(null, tenantId,
                IdentityTenantUtil.getTenantDomain(tenantId));
        dao.deleteIdPs(tenantId);
        try {
            for (IdentityProvider idp : idpList) {
                deleteEndpointConfig(idp, IdentityTenantUtil.getTenantDomain(tenantId));
            }
        } catch (IdentityProviderManagementException e) {
            // Error will not be thrown, since the IdPs are already deleted. But there will be stale actions.
            LOG.warn(IdPManagementConstants.WarningMessage.WARN_STALE_IDP_ACTIONS);
        }
    }

    public void deleteIdPByResourceId(String resourceId, int tenantId, String tenantDomain)
            throws IdentityProviderManagementException {

        IdentityProvider identityProvider = getIDPbyResourceId(null, resourceId, tenantId, tenantDomain);
        dao.deleteIdPByResourceId(resourceId, tenantId, tenantDomain);
        try {
            deleteEndpointConfig(identityProvider, tenantDomain);
        } catch (IdentityProviderManagementException e) {
            // Error will not be thrown, since the IDP is already deleted. But there will be stale action.
            LOG.warn(String.format(IdPManagementConstants.WarningMessage.WARN_STALE_IDP_ACTION,
                    identityProvider.getIdentityProviderName()));
        }
    }

    public void forceDeleteIdP(String idPName, int tenantId, String tenantDomain)
            throws IdentityProviderManagementException {

        IdentityProvider identityProvider = getIdPByName(null, idPName, tenantId, tenantDomain);
        dao.forceDeleteIdP(idPName, tenantId, tenantDomain);
        try {
            deleteEndpointConfig(identityProvider, tenantDomain);
        } catch (IdentityProviderManagementException e) {
            // Error will not be thrown, since the IDP is already deleted. But there will be stale action.
            LOG.warn(String.format(IdPManagementConstants.WarningMessage.WARN_STALE_IDP_ACTION, idPName));
        }
    }

    public void forceDeleteIdPByResourceId(String resourceId, int tenantId, String tenantDomain)
            throws IdentityProviderManagementException {

        IdentityProvider identityProvider = getIDPbyResourceId(null, resourceId, tenantId, tenantDomain);
        dao.forceDeleteIdPByResourceId(resourceId, tenantId, tenantDomain);
        try {
            deleteEndpointConfig(identityProvider, tenantDomain);
        } catch (IdentityProviderManagementException e) {
            // Error will not be thrown, since the IDP is already deleted. But there will be stale action.
            LOG.warn(String.format(IdPManagementConstants.WarningMessage.WARN_STALE_IDP_ACTION,
                    identityProvider.getIdentityProviderName()));
        }
    }

    public void deleteTenantRole(int tenantId, String role, String tenantDomain)
            throws IdentityProviderManagementException {

        dao.deleteTenantRole(tenantId, role, tenantDomain);
    }

    public void renameTenantRole(String newRoleName, String oldRoleName, int tenantId, String tenantDomain)
            throws IdentityProviderManagementException {

        dao.renameTenantRole(newRoleName, oldRoleName, tenantId, tenantDomain);
    }

    public boolean isIdPAvailableForAuthenticatorProperty(String authenticatorName, String propertyName,
                                                          String idPEntityId, int tenantId)
            throws IdentityProviderManagementException {

        return dao.isIdPAvailableForAuthenticatorProperty(authenticatorName, propertyName, idPEntityId, tenantId);
    }

    public ConnectedAppsResult getConnectedApplications(String resourceId, int limit, int offset)
            throws IdentityProviderManagementException {

        return dao.getConnectedApplications(resourceId, limit, offset);
    }

    public ConnectedAppsResult getConnectedAppsOfLocalAuthenticator(String authenticatorId, int tenantId,
                                                                    Integer limit, Integer offset)
            throws IdentityProviderManagementException {

        return dao.getConnectedAppsOfLocalAuthenticator(authenticatorId, tenantId, limit, offset);
    }

    public String getIdPNameByMetadataProperty(Connection dbConnection, String property, String value, int tenantId)
            throws IdentityProviderManagementException {

        return dao.getIdPNameByMetadataProperty(dbConnection, property, value, tenantId);
    }

    public Map<String, String> getIdPNamesById(int tenantId, Set<String> idpIds)
            throws IdentityProviderManagementException {

        return dao.getIdPNamesById(tenantId, idpIds);
    }

    public List<IdPGroup> getIdPGroupsByIds(List<String> idpGroupIds, int tenantId)
            throws IdentityProviderManagementException {

        return dao.getIdPGroupsByIds(idpGroupIds, tenantId);
    }

    public List<FederatedAuthenticatorConfig> getAllUserDefinedFederatedAuthenticators(int tenantId)
            throws IdentityProviderManagementException {

        // TODO: Replace loops with batch operations once issue:https://github.com/wso2/product-is/issues/21783 is done.
        List<FederatedAuthenticatorConfig> configList = dao.getAllUserDefinedFederatedAuthenticators(tenantId);
        for (FederatedAuthenticatorConfig config : configList) {
            endpointConfigurationManager.resolveEndpointConfig(config, IdentityTenantUtil.getTenantDomain(tenantId));
        }
        return configList;
    }

    public void deleteIdpProperties(int idpId, List<String> propertyNames, String tenantDomain)
            throws IdentityProviderManagementException {

        dao.deleteIdpProperties(idpId, propertyNames, tenantDomain);
    }

    private IdentityProvider populateEndpointConfig(IdentityProvider identityProvider, String tenantDomain)
            throws IdentityProviderManagementException {

        if (identityProvider == null || identityProvider.getFederatedAuthenticatorConfigs().length != 1) {
            return identityProvider;
        }
        endpointConfigurationManager.resolveEndpointConfig(identityProvider.getFederatedAuthenticatorConfigs()[0],
                tenantDomain);
        return identityProvider;
    }

    private void addEndpointConfig(IdentityProvider identityProvider, String tenantDomain)
            throws IdentityProviderManagementException {

        if (identityProvider == null || identityProvider.getFederatedAuthenticatorConfigs().length != 1) {
            return;
        }
        endpointConfigurationManager.addEndpointConfig(identityProvider.getFederatedAuthenticatorConfigs()[0],
                tenantDomain);
    }

    private void updateEndpointConfig(IdentityProvider newIdentityProvider, IdentityProvider oldIdentityProvider,
                                      String tenantDomain)
            throws IdentityProviderManagementException {

        if (newIdentityProvider == null || newIdentityProvider.getFederatedAuthenticatorConfigs().length != 1) {
            return;
        }
        FederatedAuthenticatorConfig newFederatedAuth = newIdentityProvider.getFederatedAuthenticatorConfigs()[0];
        if (newFederatedAuth.getDefinedByType() == AuthenticatorPropertyConstants.DefinedByType.SYSTEM) {
            return;
        }

        FederatedAuthenticatorConfig oldFederatedAuth = oldIdentityProvider.getFederatedAuthenticatorConfigs()[0];
        if (StringUtils.equals(newFederatedAuth.getName(), oldFederatedAuth.getName())) {
            endpointConfigurationManager.updateEndpointConfig(newIdentityProvider.getFederatedAuthenticatorConfigs()[0],
                    oldIdentityProvider.getFederatedAuthenticatorConfigs()[0],
                    tenantDomain);
        } else {
            endpointConfigurationManager.deleteEndpointConfig(oldFederatedAuth, tenantDomain);
            endpointConfigurationManager.addEndpointConfig(newFederatedAuth, tenantDomain);
        }
    }

    private void deleteEndpointConfig(IdentityProvider identityProvider, String tenantDomain)
            throws IdentityProviderManagementException {

        if (identityProvider == null || identityProvider.getFederatedAuthenticatorConfigs().length != 1) {
            return;
        }
        endpointConfigurationManager.deleteEndpointConfig(identityProvider.getFederatedAuthenticatorConfigs()[0],
                tenantDomain);
    }
}

