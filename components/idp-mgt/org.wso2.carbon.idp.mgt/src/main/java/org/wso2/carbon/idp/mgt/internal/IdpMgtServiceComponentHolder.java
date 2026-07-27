/*
 * Copyright (c) 2016-2025, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
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

package org.wso2.carbon.idp.mgt.internal;

import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.identity.action.management.api.service.ActionManagementService;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;
import org.wso2.carbon.identity.core.ConnectorConfig;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;
import org.wso2.carbon.identity.organization.resource.hierarchy.traverse.service.OrgResourceResolverService;
import org.wso2.carbon.identity.role.mgt.core.RoleManagementService;
import org.wso2.carbon.identity.secret.mgt.core.SecretManager;
import org.wso2.carbon.identity.secret.mgt.core.SecretResolveManager;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.dao.CacheBackedIdPMgtDAO;
import org.wso2.carbon.idp.mgt.dao.IdPManagementDAO;
import org.wso2.carbon.idp.mgt.listener.IdentityProviderMgtListener;
import org.wso2.carbon.idp.mgt.util.MetadataConverter;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.ArrayList;
import java.util.List;

public class IdpMgtServiceComponentHolder {

    public static IdpMgtServiceComponentHolder instance = new IdpMgtServiceComponentHolder();

    public static IdpMgtServiceComponentHolder getInstance() {
        return instance;
    }


    private RealmService realmService = null;

    private ConfigurationContextService configurationContextService = null;
    private volatile List<IdentityProviderMgtListener> idpMgtListeners = new ArrayList<>();
    private volatile List<ConnectorConfig> identityConnectorConfigList = new ArrayList<>();
    private RoleManagementService roleManagementService;
    private ClaimMetadataManagementService claimMetadataManagementService;
    private SecretManager secretManager;
    private SecretResolveManager  secretResolveManager;
    private ActionManagementService actionManagementService;
    private OrganizationManager organizationManager;
    private OrgResourceResolverService orgResourceResolverService;

    private List<MetadataConverter> metadataConverters = new ArrayList<>();

    public RealmService getRealmService() {
        return realmService;
    }

    public void setRealmService(RealmService realmService) {
        this.realmService = realmService;
    }

    public ConfigurationContextService getConfigurationContextService() {
        return configurationContextService;
    }

    public void setConfigurationContextService(ConfigurationContextService configurationContextService) {
        this.configurationContextService = configurationContextService;
    }

    public List<IdentityProviderMgtListener> getIdpMgtListeners() {
        return idpMgtListeners;
    }

    public void setIdpMgtListeners(List<IdentityProviderMgtListener> idpMgtListeners) {
        this.idpMgtListeners = idpMgtListeners;
    }

    public List<MetadataConverter> getMetadataConverters() {
        return metadataConverters;
    }

    public void setMetadataConverters(List<MetadataConverter> metadataConverters) {
        this.metadataConverters = metadataConverters;
    }
    public void addMetadataConverter(MetadataConverter converter) {
        this.metadataConverters.add(converter);
    }
    public void removeMetadataConverter(MetadataConverter converter) {
        this.metadataConverters.remove(converter);
    }

    public void addConnectorConfig(ConnectorConfig identityConnectorConfig) throws IdentityProviderManagementException {

        CacheBackedIdPMgtDAO dao = new CacheBackedIdPMgtDAO(new IdPManagementDAO());

        dao.clearIdpCache(IdentityApplicationConstants.RESIDENT_IDP_RESERVED_NAME,
                IdentityTenantUtil.getTenantId(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME),
                MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);

        this.identityConnectorConfigList.add(identityConnectorConfig);
    }

    public List<ConnectorConfig> getIdentityConnectorConfigList() {

        return identityConnectorConfigList;
    }

    protected void unsetGovernanceConnector(ConnectorConfig connector) {

        identityConnectorConfigList.remove(connector);
    }

    /**
     * Get RoleManagementService instance.
     *
     * @return RoleManagementService instance.
     */
    public RoleManagementService getRoleManagementService() {

        return roleManagementService;
    }

    /**
     * Set RoleManagementService instance.
     *
     * @param roleManagementService RoleManagementService instance.
     */
    public void setRoleManagementService(RoleManagementService roleManagementService) {

        this.roleManagementService = roleManagementService;
    }

    /**
     * Get the SecretManager.
     *
     * @return SecretManager instance.
     */
    public SecretManager getSecretManager() {

        return secretManager;
    }

    /**
     * Set the SecretManager.
     *
     * @param secretManager SecretManager instance.
     */
    public void setSecretManager(SecretManager secretManager) {

        this.secretManager = secretManager;
    }

    /**
     * Get the SecretResolveManager.
     *
     * @return SecretResolveManager instance.
     */
    public SecretResolveManager getSecretResolveManager() {

        return secretResolveManager;
    }

    /**
     * Set the SecretResolveManager.
     *
     * @param secretResolveManager SecretResolveManager instance.
     */
    public void setSecretResolveManager(SecretResolveManager secretResolveManager) {

        this.secretResolveManager = secretResolveManager;
    }

    public ClaimMetadataManagementService getClaimMetadataManagementService() {

        return claimMetadataManagementService;
    }

    public void setClaimMetadataManagementService(ClaimMetadataManagementService claimMetadataManagementService) {

        this.claimMetadataManagementService = claimMetadataManagementService;
    }

    /**
     * Get the Action Management Service.
     *
     * @return ActionManagementService instance.
     */
    public ActionManagementService getActionManagementService() {

        return actionManagementService;
    }

    /**
     * Set the Action Management Service.
     *
     * @param actionManagementService ActionManagementService instance.
     */
    public void setActionManagementService(ActionManagementService actionManagementService) {

        this.actionManagementService = actionManagementService;
    }

    /**
     * Get the OrganizationManager.
     *
     * @return OrganizationManager instance.
     */
    public OrganizationManager getOrganizationManager() {

        return organizationManager;
    }

    /**
     * Set the OrganizationManager.
     *
     * @param organizationManager OrganizationManager instance.
     */
    public void setOrganizationManager(OrganizationManager organizationManager) {

        this.organizationManager = organizationManager;
    }

    /**
     * Get the OrgResourceResolverService.
     *
     * @return OrgResourceResolverService instance.
     */
    public OrgResourceResolverService getOrgResourceResolverService() {

        return orgResourceResolverService;
    }

    /**
     * Set the OrgResourceResolverService.
     *
     * @param orgResourceResolverService OrgResourceResolverService instance.
     */
    public void setOrgResourceResolverService(OrgResourceResolverService orgResourceResolverService) {

        this.orgResourceResolverService = orgResourceResolverService;
    }
}
