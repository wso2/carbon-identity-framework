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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.util.AnonymousSessionUtil;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.OutboundProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.ProvisioningConnectorConfig;
import org.wso2.carbon.identity.application.common.model.RoleMapping;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationManagementUtil;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.provisioning.cache.ServiceProviderProvisioningConnectorCache;
import org.wso2.carbon.identity.provisioning.cache.ServiceProviderProvisioningConnectorCacheEntry;
import org.wso2.carbon.identity.provisioning.cache.ServiceProviderProvisioningConnectorCacheKey;
import org.wso2.carbon.identity.provisioning.dao.CacheBackedProvisioningMgtDAO;
import org.wso2.carbon.identity.provisioning.dao.ProvisioningManagementDAO;
import org.wso2.carbon.identity.provisioning.internal.IdentityProvisionServiceComponent;
import org.wso2.carbon.identity.provisioning.rules.XACMLBasedRuleHandler;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;
import org.wso2.carbon.idp.mgt.util.IdPManagementUtil;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.claim.Claim;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.wso2.carbon.identity.provisioning.ProvisioningUtil.isUserTenantBasedOutboundProvisioningEnabled;

/**
 *
 *
 */
public class OutboundProvisioningManager {

    private static final Log log = LogFactory.getLog(OutboundProvisioningManager.class);
    private static CacheBackedProvisioningMgtDAO dao = new CacheBackedProvisioningMgtDAO(
            new ProvisioningManagementDAO());

    private static OutboundProvisioningManager provisioningManager = new OutboundProvisioningManager();

    private OutboundProvisioningManager() {

    }

    /**
     * @return
     */
    public static OutboundProvisioningManager getInstance() {
        return provisioningManager;
    }

    /**
     * Get the tenant id of the given tenant domain.
     *
     * @param tenantDomain Tenant Domain
     * @return Tenant Id of domain user belongs to.
     * @throws IdentityApplicationManagementException Error when getting tenant id from tenant
     *                                                domain
     */
    private static int getTenantIdOfDomain(String tenantDomain)
            throws IdentityApplicationManagementException {

        try {
            return IdPManagementUtil.getTenantIdOfDomain(tenantDomain);
        } catch (UserStoreException e) {
            log.error(e.getMessage(), e);
            String msg = "Error occurred while getting Tenant Id from Tenant domain "
                         + tenantDomain;
            throw new IdentityApplicationManagementException(msg);
        }
    }

    /**
     * TODO: Need to cache the output from this method.
     *
     * @return
     * @throws UserStoreException
     */
    private Map<String, RuntimeProvisioningConfig> getOutboundProvisioningConnectors(
            ServiceProvider serviceProvider, String tenantDomain) throws IdentityProvisioningException {

        Map<String, RuntimeProvisioningConfig> connectors = new HashMap<>();

        ServiceProviderProvisioningConnectorCacheKey key;
        ServiceProviderProvisioningConnectorCacheEntry entry;

        // Reading from the cache.
        if (serviceProvider != null && tenantDomain != null) {
            key = new ServiceProviderProvisioningConnectorCacheKey(serviceProvider.getApplicationName());

            entry = ServiceProviderProvisioningConnectorCache.getInstance().getValueFromCache(key, tenantDomain);

            // cache hit
            if (entry != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Provisioning cache HIT for " + serviceProvider + " of "
                            + tenantDomain);
                }
                return entry.getConnectors();
            }
        } else {
            throw new IdentityProvisioningException("Error reading service provider from cache.");
        }

        // NOW build the Map

        // a list of registered provisioning connector factories.
        Map<String, AbstractProvisioningConnectorFactory> registeredConnectorFactories = IdentityProvisionServiceComponent
                .getConnectorFactories();

        // get all registered list of out-bound provisioning connectors registered for the local
        // service provider.
        OutboundProvisioningConfig outboundProvisioningConfiguration = serviceProvider
                .getOutboundProvisioningConfig();

        if (outboundProvisioningConfiguration == null) {
            if (log.isDebugEnabled()) {
                log.debug("No outbound provisioning configuration defined for local service provider.");
            }
            // no out-bound provisioning configuration defined for local service provider.return an
            // empty list.
            return new HashMap<String, RuntimeProvisioningConfig>();
        }

        // get the list of registered provisioning identity providers in out-bound provisioning
        // configuration.
        IdentityProvider[] provisionningIdPList = outboundProvisioningConfiguration
                .getProvisioningIdentityProviders();

        if (provisionningIdPList != null && provisionningIdPList.length > 0) {
            // we have a set of provisioning identity providers registered in our system.

            for (IdentityProvider fIdP : provisionningIdPList) {
                // iterate through the provisioning identity provider list to find out the default
                // provisioning connector of each of the,

                try {

                    AbstractOutboundProvisioningConnector connector;

                    ProvisioningConnectorConfig defaultConnector = fIdP
                            .getDefaultProvisioningConnectorConfig();
                    if (defaultConnector != null) {
                        // if no default provisioning connector defined for this identity provider,
                        // we can safely ignore it - need not to worry about provisioning.

                        String connectorType = fIdP.getDefaultProvisioningConnectorConfig()
                                .getName();

                        boolean enableJitProvisioning = false;

                        if (fIdP.getJustInTimeProvisioningConfig() != null
                            && fIdP.getJustInTimeProvisioningConfig().isProvisioningEnabled()) {
                            enableJitProvisioning = true;
                        }

                        connector = getOutboundProvisioningConnector(fIdP,
                                                                     registeredConnectorFactories, tenantDomain,
                                                                     enableJitProvisioning);
                        // add to the provisioning connectors list. there will be one item for each
                        // provisioning identity provider found in the out-bound provisioning
                        // configuration of the local service provider.
                        if (connector != null) {
                            RuntimeProvisioningConfig proConfig = new RuntimeProvisioningConfig();
                            proConfig
                                    .setProvisioningConnectorEntry(new SimpleEntry<>(
                                            connectorType, connector));
                            proConfig.setBlocking(defaultConnector.isBlocking());
                            proConfig.setPolicyEnabled(defaultConnector.isRulesEnabled());
                            connectors.put(fIdP.getIdentityProviderName(), proConfig);
                        }
                    }

                } catch (IdentityProviderManagementException e) {
                    throw new IdentityProvisioningException("Error while retrieving idp configuration for "
                                                            + fIdP.getIdentityProviderName(), e);
                }
            }
        }

        entry = new ServiceProviderProvisioningConnectorCacheEntry();
        entry.setConnectors(connectors);
        ServiceProviderProvisioningConnectorCache.getInstance().addToCache(key, entry, tenantDomain);

        if (log.isDebugEnabled()) {
            log.debug("Entry added successfully ");
        }

        return connectors;
    }

    /**
     * @param fIdP
     * @param registeredConnectorFactories
     * @param tenantDomainName
     * @param enableJitProvisioning
     * @return
     * @throws IdentityProviderManagementException
     * @throws UserStoreException
     */
    private AbstractOutboundProvisioningConnector getOutboundProvisioningConnector(
            IdentityProvider fIdP,
            Map<String, AbstractProvisioningConnectorFactory> registeredConnectorFactories,
            String tenantDomainName, boolean enableJitProvisioning)
            throws IdentityProviderManagementException, IdentityProvisioningException {

        String idpName = fIdP.getIdentityProviderName();

        // name of the default provisioning connector.
        String connectorType = fIdP.getDefaultProvisioningConnectorConfig().getName();

        // get identity provider configuration.
        fIdP = IdentityProviderManager.getInstance().getEnabledIdPByName(idpName, tenantDomainName);

        if (fIdP == null) {
            // This is an exceptional situation. If service provider has connected to an
            // identity provider, that identity provider must be present in the system.
            // If not its an exception.
            throw new IdentityProvisioningException(
                    "Provisioning identity provider not available in the system. Idp Name : "
                    + idpName);
        }

        // get a list of provisioning connectors associated with the provisioning
        // identity provider.
        ProvisioningConnectorConfig[] provisioningConfigs = fIdP.getProvisioningConnectorConfigs();

        if (provisioningConfigs != null && provisioningConfigs.length > 0) {

            for (ProvisioningConnectorConfig defaultProvisioningConfig : provisioningConfigs) {

                if (!connectorType.equals(defaultProvisioningConfig.getName())
                    || !defaultProvisioningConfig.isEnabled()) {
                    // we need to find the provisioning connector selected by the service provider.
                    continue;
                }

                // this is how we match the configuration to the runtime. the provisioning
                // connector factory should be registered with the system, with the exact
                // name available in the corresponding configuration.
                AbstractProvisioningConnectorFactory factory = registeredConnectorFactories
                        .get(connectorType);

                // get the provisioning properties associated with a given provisioning
                // connector.
                Property[] provisioningProperties = defaultProvisioningConfig
                        .getProvisioningProperties();

                if (enableJitProvisioning) {
                    Property jitEnabled = new Property();
                    jitEnabled.setName(IdentityProvisioningConstants.JIT_PROVISIONING_ENABLED);
                    jitEnabled.setValue("1");
                    provisioningProperties = IdentityApplicationManagementUtil.concatArrays(
                            provisioningProperties, new Property[]{jitEnabled});
                }

                Property userIdClaimURL = new Property();
                userIdClaimURL.setName("userIdClaimUri");

                if (fIdP.getClaimConfig() != null && fIdP.getClaimConfig().getUserClaimURI() != null) {
                    userIdClaimURL.setValue(fIdP.getClaimConfig().getUserClaimURI());
                } else {
                    userIdClaimURL.setValue("");
                }

                List<Property> provisioningPropertiesList = new ArrayList<>(Arrays.asList(provisioningProperties));

                provisioningPropertiesList.add(userIdClaimURL);

                provisioningProperties = new Property[provisioningPropertiesList.size()];
                provisioningProperties = provisioningPropertiesList.toArray(provisioningProperties);

                // get the runtime provisioning connector associate the provisioning
                // identity provider. any given time, a given provisioning identity provider
                // can only be associated with a single provisioning connector.
                return factory.getConnector(idpName, provisioningProperties, tenantDomainName);
            }
        }

        return null;
    }

    /**
     * Outbound provisioning method.
     *
     * @param provisioningEntity        Provisioning entity.
     * @param serviceProviderIdentifier Identifier of the service provider.
     * @param inboundClaimDialect       Inbound claim dialect.
     * @param spTenantDomainName        Tenant domain of the service provider.
     * @param jitProvisioning           Is JIT provisioning enabled.
     * @throws IdentityProvisioningException if error occurred while user provisioning.
     */
    public void provision(ProvisioningEntity provisioningEntity, String serviceProviderIdentifier,
                          String inboundClaimDialect, String spTenantDomainName, boolean jitProvisioning)
            throws IdentityProvisioningException {

        try {
            if (provisioningEntity.getEntityName() == null) {
                setProvisioningEntityName(provisioningEntity);
            }
            // get details about the service provider.any in-bound provisioning request via
            // the SOAP based API (or the management console) - or SCIM API with HTTP Basic
            // Authentication is considered as coming from the local service provider.
            ServiceProvider serviceProvider = ApplicationManagementService.getInstance()
                    .getServiceProvider(serviceProviderIdentifier, spTenantDomainName);

            if (serviceProvider == null) {
                throw new IdentityProvisioningException("Invalid service provider name : "
                        + serviceProviderIdentifier);
            }

            String provisioningEntityTenantDomainName = spTenantDomainName;
            if (serviceProvider.isSaasApp() && isUserTenantBasedOutboundProvisioningEnabled()) {
                provisioningEntityTenantDomainName = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            }

            ClaimMapping[] spClaimMappings = null;

            // if we know the serviceProviderClaimDialect - we do not need to find it again.
            if (inboundClaimDialect == null && serviceProvider.getClaimConfig() != null) {
                spClaimMappings = serviceProvider.getClaimConfig().getClaimMappings();
            }

            // get all the provisioning connectors associated with local service provider for
            // out-bound provisioning.
            // TODO: stop loading connectors all the time.
            Map<String, RuntimeProvisioningConfig> connectors =
                    getOutboundProvisioningConnectors(serviceProvider, spTenantDomainName);

            ProvisioningEntity outboundProEntity;

            ExecutorService executors = null;

            if (MapUtils.isNotEmpty(connectors)) {
                executors = Executors.newFixedThreadPool(connectors.size());
            }

            for (Iterator<Entry<String, RuntimeProvisioningConfig>> iterator = connectors
                    .entrySet().iterator(); iterator.hasNext(); ) {

                Entry<String, RuntimeProvisioningConfig> entry = iterator.next();

                Entry<String, AbstractOutboundProvisioningConnector> connectorEntry = entry
                        .getValue().getProvisioningConnectorEntry();

                AbstractOutboundProvisioningConnector connector = connectorEntry.getValue();
                String connectorType = connectorEntry.getKey();
                String idPName = entry.getKey();

                IdentityProvider provisioningIdp =
                        IdentityProviderManager.getInstance().getIdPByName(idPName, spTenantDomainName);

                if (provisioningIdp == null) {
                    // this is an exception if we cannot find the provisioning identity provider
                    // by its name.
                    throw new IdentityProvisioningException("Invalid identity provider name : "
                                                            + idPName);
                }

                String[] outboundClaimDialects = connector.getClaimDialectUris();

                if (outboundClaimDialects == null) {
                    String outboundClaimDialect = connector.getClaimDialectUri();
                    if (outboundClaimDialect == null) {
                        if (log.isDebugEnabled()) {
                            log.debug("No outbound claim dialects available for the connector: " + connectorType);
                        }
                        if (provisioningIdp.getClaimConfig() == null || provisioningIdp
                                .getClaimConfig().isLocalClaimDialect()) {
                            outboundClaimDialect = IdentityProvisioningConstants.WSO2_CARBON_DIALECT;
                        }
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug(String.format("Outbound claim dialect: %s available for the connector: %s",
                                    outboundClaimDialect, connectorType));
                        }
                    }
                    outboundClaimDialects = new String[] {outboundClaimDialect};
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Multiple outbound claim dialects: %s available for the connector: %s",
                                Arrays.toString(Arrays.stream(outboundClaimDialects).toArray()), connectorType));
                    }
                }

                ClaimMapping[] idpClaimMappings = null;

                if (provisioningIdp.getClaimConfig() != null) {
                    idpClaimMappings = provisioningIdp.getClaimConfig().getClaimMappings();
                }

                // TODO: this should happen asynchronously in a different thread.
                // create a new provisioning entity object for each provisioning identity
                // provider.

                Map<ClaimMapping, List<String>> mapppedClaims;

                // get mapped claims.
                mapppedClaims =
                        getMappedClaims(inboundClaimDialect, outboundClaimDialects, provisioningEntity, spClaimMappings,
                                idpClaimMappings, spTenantDomainName);

                if (provisioningIdp.getPermissionAndRoleConfig() != null) {
                    // update with mapped user groups.
                    updateProvisioningUserWithMappedRoles(provisioningEntity, provisioningIdp
                            .getPermissionAndRoleConfig().getRoleMappings());
                }

                // check whether we already have the provisioned identifier - if
                // so set it.
                ProvisionedIdentifier provisionedIdentifier;

                provisionedIdentifier =
                        getProvisionedEntityIdentifier(idPName, connectorType, provisioningEntity, spTenantDomainName);

                ProvisioningOperation provisioningOp = provisioningEntity.getOperation();

                if (ProvisioningOperation.DELETE.equals(provisioningOp) &&
                    (provisionedIdentifier == null || provisionedIdentifier.getIdentifier() == null)) {
                    //No provisioning identifier found. User has not outbound provisioned to this idp. So no need to
                    // send outbound delete request. Skip the flow
                    return;
                }
                if (provisionedIdentifier == null || provisionedIdentifier.getIdentifier() == null) {
                    provisioningOp = ProvisioningOperation.POST;
                }

                String[] provisionByRoleList = new String[0];

                if (provisioningIdp.getProvisioningRole() != null) {
                    provisionByRoleList = provisioningIdp.getProvisioningRole().trim().split("\\s*,[,\\s]*");
                }

                if (provisioningEntity.getEntityType() == ProvisioningEntityType.GROUP && Arrays.asList
                        (provisionByRoleList).contains(provisioningEntity.getEntityName())) {
                    Map<ClaimMapping, List<String>> attributes = provisioningEntity.getAttributes();
                    List<String> newUsersList = attributes.get(ClaimMapping.build(
                            IdentityProvisioningConstants.NEW_USER_CLAIM_URI, null, null, false));

                    List<String> deletedUsersList = attributes.get(ClaimMapping.build(
                            IdentityProvisioningConstants.DELETED_USER_CLAIM_URI, null, null, false));

                    Map<ClaimMapping, List<String>> mappedUserClaims;
                    ProvisionedIdentifier provisionedUserIdentifier;

                    for (String user : newUsersList) {
                        ProvisioningEntity inboundProvisioningEntity =
                                getInboundProvisioningEntity(provisioningEntity, provisioningEntityTenantDomainName,
                                        ProvisioningOperation.POST, user);

                        provisionedUserIdentifier =
                                getProvisionedEntityIdentifier(idPName, connectorType, inboundProvisioningEntity,
                                        spTenantDomainName);

                        if (provisionedUserIdentifier != null && provisionedUserIdentifier.getIdentifier() != null) {
                            continue;
                        }

                        mappedUserClaims =
                                getMappedClaims(inboundClaimDialect, outboundClaimDialects, inboundProvisioningEntity,
                                        spClaimMappings, idpClaimMappings, spTenantDomainName);

                        outboundProEntity = new ProvisioningEntity(ProvisioningEntityType.USER,
                                                                   user, ProvisioningOperation.POST, mappedUserClaims);
                        Callable<Boolean> proThread = new ProvisioningThread(outboundProEntity, spTenantDomainName,
                                provisioningEntityTenantDomainName, connector, connectorType, idPName, dao);
                        outboundProEntity.setIdentifier(provisionedIdentifier);
                        outboundProEntity.setJitProvisioning(jitProvisioning);
                        boolean isBlocking = entry.getValue().isBlocking();
                        executeOutboundProvisioning(provisioningEntity, executors, connectorType, idPName, proThread, isBlocking);

                    }

                    for (String user : deletedUsersList) {

                        ProvisioningEntity inboundProvisioningEntity =
                                getInboundProvisioningEntity(provisioningEntity, provisioningEntityTenantDomainName,
                                        ProvisioningOperation.DELETE, user);

                        provisionedUserIdentifier =
                                getProvisionedEntityIdentifier(idPName, connectorType, inboundProvisioningEntity,
                                        spTenantDomainName);

                        if (provisionedUserIdentifier != null && provisionedUserIdentifier.getIdentifier() != null) {
                            mappedUserClaims = getMappedClaims(inboundClaimDialect, outboundClaimDialects,
                                    inboundProvisioningEntity, spClaimMappings, idpClaimMappings, spTenantDomainName);

                            outboundProEntity = new ProvisioningEntity(ProvisioningEntityType.USER,
                                                                       user, ProvisioningOperation.DELETE, mappedUserClaims);
                            Callable<Boolean> proThread = new ProvisioningThread(outboundProEntity, spTenantDomainName,
                                    provisioningEntityTenantDomainName, connector, connectorType, idPName, dao);
                            outboundProEntity.setIdentifier(provisionedUserIdentifier);
                            outboundProEntity.setJitProvisioning(jitProvisioning);
                            boolean isBlocking = entry.getValue().isBlocking();
                            executeOutboundProvisioning(provisioningEntity, executors, connectorType, idPName, proThread, isBlocking);
                        }
                    }

                } else {
                    // see whether the given provisioning entity satisfies the conditions to be
                    // provisioned.

                    if (!canUserBeProvisioned(provisioningEntity, provisionByRoleList,
                            provisioningEntityTenantDomainName)) {
                        if (!canUserBeDeProvisioned(provisionedIdentifier)) {
                            continue;
                        } else {
                            // This is used when user removed from the provisioning role
                            provisioningOp = ProvisioningOperation.DELETE;
                        }
                    }
                    if (!skipOutBoundProvisioning(provisioningOp, provisioningEntity, inboundClaimDialect)) {
                        outboundProEntity = new ProvisioningEntity(provisioningEntity.getEntityType(),
                                provisioningEntity.getEntityName(), provisioningOp, mapppedClaims);

                        Callable<Boolean> proThread = new ProvisioningThread(outboundProEntity, spTenantDomainName,
                                provisioningEntityTenantDomainName, connector, connectorType, idPName, dao);
                        outboundProEntity.setIdentifier(provisionedIdentifier);
                        outboundProEntity.setJitProvisioning(jitProvisioning);
                        boolean isAllowed = true;
                        boolean isBlocking = entry.getValue().isBlocking();
                        boolean isPolicyEnabled = entry.getValue().isPolicyEnabled();
                        if (isPolicyEnabled) {
                            isAllowed = XACMLBasedRuleHandler.getInstance().isAllowedToProvision(spTenantDomainName,
                                    provisioningEntity,
                                    serviceProvider,
                                    idPName,
                                    connectorType);
                        }
                        if (isAllowed) {
                            executeOutboundProvisioning(provisioningEntity, executors, connectorType, idPName, proThread, isBlocking);
                        }
                    }
                }
            }

            if (executors != null) {
                executors.shutdown();
            }

        } catch (CarbonException | IdentityApplicationManagementException | IdentityProviderManagementException | UserStoreException e) {
            throw new IdentityProvisioningException("Error occurred while checking for user " +
                                                    "provisioning", e);
        }
    }

    /**
     * Skip outbound provisioning if user entity provisioning operation is PUT, inboundClaimDialect is local dialect and
     * updating attributes are username and last modified time.
     * This condition is occurred when the updated attribute doesn't have an outbound claim mapping.
     *
     * @param provisioningOp      The provisioning operation.
     * @param provisioningEntity  Provisioning entity.
     * @param inboundClaimDialect Inbound claim dialect.
     * @return Whether outbound provisioning is skipped for the attribute update.
     */
    private boolean skipOutBoundProvisioning(ProvisioningOperation provisioningOp,
                                             ProvisioningEntity provisioningEntity, String inboundClaimDialect) {

        if (!ProvisioningOperation.PUT.equals(provisioningOp)) {
            return false;
        }
        if (!IdentityProvisioningConstants.WSO2_CARBON_DIALECT.equals(inboundClaimDialect)) {
            return false;
        }
        if (provisioningEntity != null && provisioningEntity.getAttributes() != null) {
            for (ClaimMapping claimMapping : provisioningEntity.getAttributes().keySet()) {
                if (!IdentityProvisioningConstants.USERNAME_CLAIM_URI.equalsIgnoreCase(
                        claimMapping.getLocalClaim().getClaimUri()) &&
                        !IdentityProvisioningConstants.LAST_MODIFIED_CLAIM.equalsIgnoreCase(
                                claimMapping.getLocalClaim().getClaimUri())) {
                    return false;
                }
            }
        }
        return true;
    }

    private void executeOutboundProvisioning(ProvisioningEntity provisioningEntity, ExecutorService executors,
                                             String connectorType,
                                             String idPName, Callable<Boolean> proThread, boolean isBlocking)
            throws IdentityProvisioningException {
        if (!isBlocking) {
            executors.submit(proThread);
        } else {
            try {

                boolean success = proThread.call();
                if (!success) {
                    if (executors != null) {
                        executors.shutdown();
                    }
                    throw new IdentityProvisioningException
                            (generateMessageOnFailureProvisioningOperation(idPName,
                                                                           connectorType, provisioningEntity));
                    //DO Rollback
                }
            } catch (Exception e) { //call() of Callable interface throws this exception
                handleException(idPName, connectorType, provisioningEntity, executors, e);
            }
        }
    }

    private ProvisioningEntity getInboundProvisioningEntity(ProvisioningEntity provisioningEntity,
                                                            String tenantDomain, ProvisioningOperation operation,
                                                            String userName) throws CarbonException,
                                                                                    UserStoreException {
        Map<ClaimMapping, List<String>> outboundAttributes = new HashMap<>();

        if (userName != null) {
            outboundAttributes.put(ClaimMapping.build(
                    IdentityProvisioningConstants.USERNAME_CLAIM_URI, null, null, false),
                                   Arrays.asList(new String[]{userName}));
        }
        List<String> roleListOfUser = getUserRoles(userName, tenantDomain);
        if (roleListOfUser != null) {
            outboundAttributes.put(ClaimMapping.build(
                    IdentityProvisioningConstants.GROUP_CLAIM_URI, null, null, false), roleListOfUser);
        }

        String domainAwareName = userName;

        String domainName = getDomainFromName(provisioningEntity.getEntityName());
        if (domainName != null && !domainName.equals(UserCoreConstants.INTERNAL_DOMAIN)) {
            if (log.isDebugEnabled()) {
                log.debug("Adding domain name : " + domainName + " to user : " + userName);
            }
            domainAwareName = UserCoreUtil.addDomainToName(userName, domainName);
        }
        ProvisioningEntity inboundProvisioningEntity = new ProvisioningEntity(
                ProvisioningEntityType.USER, domainAwareName, operation, outboundAttributes);
        inboundProvisioningEntity.setInboundAttributes(getUserClaims(userName, tenantDomain));
        return inboundProvisioningEntity;
    }

    private String generateMessageOnFailureProvisioningOperation(String idPName,
                                                                 String connectorType,
                                                                 ProvisioningEntity provisioningEntity) {
        if (log.isDebugEnabled()) {
            String errMsg = "Provisioning failed for IDP = " + idPName + " " +
                            "Connector Type =" + connectorType + " ";

            errMsg += " Provisioned entity name = " +
                      provisioningEntity.getEntityName() +
                      " For operation = " + provisioningEntity.getOperation() + " " +
                      "failed  ";

            log.error(errMsg);
        }
        return "Provisioning failed for IDP = " + idPName + " " +
               "with Entity name=" + provisioningEntity.getEntityName();
    }

    /**
     * @param provisioningEntity
     * @param idPRoleMapping
     */
    private void updateProvisioningUserWithMappedRoles(ProvisioningEntity provisioningEntity,
                                                       RoleMapping[] idPRoleMapping) {

        if (ArrayUtils.isEmpty(idPRoleMapping)) {
            return;
        }

        updateMappedGroupForAttribute(provisioningEntity, idPRoleMapping,
                IdentityProvisioningConstants.GROUP_CLAIM_URI);
        updateMappedGroupForAttribute(provisioningEntity, idPRoleMapping,
                IdentityProvisioningConstants.NEW_GROUP_CLAIM_URI);
        updateMappedGroupForAttribute(provisioningEntity, idPRoleMapping,
                IdentityProvisioningConstants.DELETED_GROUP_CLAIM_URI);

    }

    /**
     * Get mapped idp roles for given role list
     *
     * @param groupList
     * @param idPRoleMapping
     * @return
     */
    private List<String> getMappedGroups(List<String> groupList, RoleMapping[] idPRoleMapping) {

        if (CollectionUtils.isEmpty(groupList)) {
            return new ArrayList<>();
        }
        Map<String, String> mappedRoles = new HashMap<>();
        for (RoleMapping mapping : idPRoleMapping) {
            mappedRoles.put(mapping.getLocalRole().getLocalRoleName(), mapping.getRemoteRole());
        }
        List<String> mappedUserGroups = new ArrayList<>();
        for (Iterator<String> iterator = groupList.iterator(); iterator.hasNext(); ) {
            String userGroup = iterator.next();
            String mappedGroup = null;
            if ((mappedGroup = mappedRoles.get(userGroup)) != null) {
                mappedUserGroups.add(mappedGroup);
            }
        }
        return mappedUserGroups;
    }

    /**
     * @param inboundClaimDialect
     * @param outboundClaimDialects
     * @param provisioningEntity
     * @param spClaimMappings
     * @param idpClaimMappings
     * @return
     * @throws IdentityApplicationManagementException
     */
    private Map<ClaimMapping, List<String>> getMappedClaims(String inboundClaimDialect,
                                                            String[] outboundClaimDialects,
                                                            ProvisioningEntity provisioningEntity,
                                                            ClaimMapping[] spClaimMappings,
                                                            ClaimMapping[] idpClaimMappings, String tenantDomainName)
            throws IdentityApplicationManagementException {

        /* If we have any in-bound attributes - need to convert those into out-bound
         attributes in a form understood by the external provisioning providers. */
        Map<String, String> inboundAttributes = provisioningEntity.getInboundAttributes();
        Map<ClaimMapping, List<String>> mapppedClaims = new HashMap<>();

        for (String outboundClaimDialect: outboundClaimDialects) {
            if (outboundClaimDialect != null) {
                /* Out-bound claim dialects are not provisioning provider specific. They are
                 specific to the connector. */

                if (inboundClaimDialect == null) {
                    /* In-bound claim dialect is service provider specific. We have read the claim mapping from
                     service provider claim configuration. */
                    mapppedClaims.putAll(ProvisioningUtil.getMappedClaims(outboundClaimDialect,
                            inboundAttributes, spClaimMappings, provisioningEntity.getAttributes(),
                            tenantDomainName));
                } else {
                    /* In-bound claim dialect is not service provider specific. Its been supplied by the corresponding
                     in-bound provisioning servlet or listener. */
                    mapppedClaims.putAll(ProvisioningUtil.getMappedClaims(outboundClaimDialect,
                            inboundAttributes, inboundClaimDialect, provisioningEntity.getAttributes(),
                            tenantDomainName));
                }
            } else {
                /* Out-bound claim dialects are provisioning provider specific. We have read the claim mapping from
                 identity provider claim configuration. */

                if (inboundClaimDialect == null) {
                    /* In-bound claim dialect is service provider specific. We have read the claim mapping from
                     service provider claim configuration. */
                    return ProvisioningUtil.getMappedClaims(idpClaimMappings,
                            inboundAttributes, spClaimMappings, provisioningEntity.getAttributes());
                } else {
                    /* In-bound claim dialect is not service provider specific. Its been supplied by the corresponding
                     in-bound provisioning servlet or listener. */
                    return ProvisioningUtil.getMappedClaims(idpClaimMappings,
                            inboundAttributes, inboundClaimDialect, provisioningEntity.getAttributes(),
                            tenantDomainName);
                }
            }
        }
        return mapppedClaims;
    }

    /**
     * @param attributeMap
     * @return
     */
    protected List<String> getGroupNames(Map<ClaimMapping, List<String>> attributeMap) {
        return ProvisioningUtil.getClaimValues(attributeMap,
                                               IdentityProvisioningConstants.GROUP_CLAIM_URI, null);
    }

    /**
     * @param attributeMap
     * @return
     */
    private String getUserName(Map<ClaimMapping, List<String>> attributeMap) {
        List<String> userList = ProvisioningUtil.getClaimValues(attributeMap,
                IdentityProvisioningConstants.USERNAME_CLAIM_URI, null);

        if (CollectionUtils.isNotEmpty(userList)) {
            return userList.get(0);
        }

        return null;
    }

    /**
     * @param provisioningEntity
     * @param provisionByRoleList
     * @param tenantDomain
     * @return
     * @throws CarbonException
     * @throws UserStoreException
     */
    protected boolean canUserBeProvisioned(ProvisioningEntity provisioningEntity,
                                           String[] provisionByRoleList, String tenantDomain) throws UserStoreException,
                                                                                                     CarbonException {

        if (provisioningEntity.getEntityType() != ProvisioningEntityType.USER
            || provisionByRoleList == null || provisionByRoleList.length == 0) {
            // we apply restrictions only for users.
            // if service provider's out-bound provisioning configuration does not define any roles
            // to be provisioned then we apply no restrictions.
            return true;
        }

        if (provisioningEntity.getAttributes() != null &&
                StringUtils.isNotBlank(provisioningEntity.getEntityName())) {
            String userName = provisioningEntity.getEntityName();
            List<String> provisioningRoleList = Arrays.asList(provisionByRoleList);

            List<String> roleListOfUser = getUserRoles(userName, tenantDomain);
            if (userHasProvisioningRoles(roleListOfUser, provisioningRoleList, userName)) {
                return true;
            }
            List<String> newRoleListOfUser = provisioningEntity.getAttributes().get(ClaimMapping.build
                        (IdentityProvisioningConstants.GROUP_CLAIM_URI, null, null, false));

            if (userHasProvisioningRoles(newRoleListOfUser, provisioningRoleList, userName)) {
                return true;
            }
        }

        return false;
    }

    /**
     * @param provisionedIdentifier
     * @return
     * @throws CarbonException
     * @throws UserStoreException
     */
    protected boolean canUserBeDeProvisioned(ProvisionedIdentifier provisionedIdentifier)
            throws UserStoreException, CarbonException, IdentityApplicationManagementException {

        // check whether we already have the provisioned identifier.current idp is not eligible to
        // provisioning.
        if (provisionedIdentifier != null && provisionedIdentifier.getIdentifier() != null) {
            return true;
        }

        return false;
    }

    /**
     * @param userName
     * @param tenantDomain
     * @return
     * @throws CarbonException
     * @throws UserStoreException
     */
    private List<String> getUserRoles(String userName, String tenantDomain) throws CarbonException,
                                                                                   UserStoreException {

        RegistryService registryService = IdentityProvisionServiceComponent.getRegistryService();
        RealmService realmService = IdentityProvisionServiceComponent.getRealmService();

        UserRealm realm = AnonymousSessionUtil.getRealmByTenantDomain(registryService,
                realmService, tenantDomain);

        UserStoreManager userstore = null;
        userstore = realm.getUserStoreManager();
        String[] newRoles = userstore.getRoleListOfUser(userName);
        return Arrays.asList(newRoles);
    }

    /**
     * @param userName
     * @param tenantDomain
     * @return
     * @throws CarbonException
     * @throws UserStoreException
     */
    private Map<String, String> getUserClaims(String userName, String tenantDomain) throws CarbonException,
                                                                                           UserStoreException {

        Map<String, String> inboundAttributes = new HashMap<>();

        RegistryService registryService = IdentityProvisionServiceComponent.getRegistryService();
        RealmService realmService = IdentityProvisionServiceComponent.getRealmService();

        UserRealm realm = AnonymousSessionUtil.getRealmByTenantDomain(registryService,
                                                                      realmService, tenantDomain);

        UserStoreManager userstore = null;
        userstore = realm.getUserStoreManager();
        Claim[] claimArray = null;
        try {
            claimArray = userstore.getUserClaimValues(userName, null);
        } catch (UserStoreException e) {
            if (e.getMessage().contains("UserNotFound")) {
                if (log.isDebugEnabled()) {
                    log.debug("User " + userName + " not found in user store");
                }
            } else {
                throw e;
            }
        }
        if (claimArray != null) {
            for (Claim claim : claimArray) {
                inboundAttributes.put(claim.getClaimUri(), claim.getValue());
            }
        }

        return inboundAttributes;
    }

    private String getUserIdClaimValue(String userIdClaimURI, String tenantDomainName) {
        return null;
    }

    /**
     * @param idpName
     * @param connectorType
     * @param provisioningEntity
     * @param tenantDomain
     * @return
     * @throws IdentityApplicationManagementException
     */
    private ProvisionedIdentifier getProvisionedEntityIdentifier(String idpName,
                                                                 String connectorType,
                                                                 ProvisioningEntity provisioningEntity,
                                                                 String tenantDomain)
            throws IdentityApplicationManagementException {
        int tenantId = getTenantIdOfDomain(tenantDomain);
        return dao.getProvisionedIdentifier(idpName, connectorType, provisioningEntity, tenantId, tenantDomain);
    }

    private String getDomainFromName(String name) {
        int index;
        if ((index = name.indexOf("/")) > 0) {
            String domain = name.substring(0, index);
            return domain;
        }
        return UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME;
    }

    /**
     * introduce extendability for handling provisioning exceptions
     *
     * @param idPName
     * @param connectorType
     * @param provisioningEntity
     * @param executors
     * @param e
     */
    protected void handleException(String idPName, String connectorType, ProvisioningEntity provisioningEntity,
                                   ExecutorService executors, Exception e) {

        if (log.isDebugEnabled()) {
            log.debug(generateMessageOnFailureProvisioningOperation(idPName, connectorType, provisioningEntity), e);
        }
    }

    /**
     * If ProvisioningEntity does not contains entity name, load it from from IDP_PROVISIONING_ENTITY table
     *
     * @param provisioningEntity
     * @return
     * @throws IdentityApplicationManagementException
     */
    private ProvisioningEntity setProvisioningEntityName(ProvisioningEntity provisioningEntity)
            throws IdentityApplicationManagementException {
        String provisionedEntityName = dao.getProvisionedEntityNameByLocalId(
                ProvisioningUtil.getAttributeValue(provisioningEntity, IdentityProvisioningConstants.ID_CLAIM_URI));

        Map<org.wso2.carbon.identity.application.common.model.ClaimMapping, List<String>> attributeList =
                provisioningEntity.getAttributes();

        ProvisioningEntityType provisioningEntityType = provisioningEntity.getEntityType();
        ProvisioningOperation provisioningOperation = provisioningEntity.getOperation();

        if (ProvisioningEntityType.USER.equals(provisioningEntityType)) {

            attributeList.put(org.wso2.carbon.identity.application.common.model.ClaimMapping
                                      .build(IdentityProvisioningConstants.USERNAME_CLAIM_URI, null, null,
                                             false), Arrays.asList(new String[]{provisionedEntityName}));

        } else if (ProvisioningEntityType.GROUP.equals(provisioningEntityType)) {

            if (ProvisioningOperation.PUT.equals(provisioningOperation)) {
                String oldGroupName = provisionedEntityName;
                String currentGroupName = ProvisioningUtil
                        .getAttributeValue(provisioningEntity, IdentityProvisioningConstants.GROUP_CLAIM_URI);
                if (!oldGroupName.equals(currentGroupName)) {
                    attributeList.put(org.wso2.carbon.identity.application.common.model.ClaimMapping
                                              .build(IdentityProvisioningConstants.OLD_GROUP_NAME_CLAIM_URI,
                                                     null, null, false),
                                      Arrays.asList(new String[]{oldGroupName}));
                    attributeList.put(org.wso2.carbon.identity.application.common.model.ClaimMapping
                                              .build(IdentityProvisioningConstants.NEW_GROUP_NAME_CLAIM_URI,
                                                     null, null, false),
                                      Arrays.asList(new String[]{currentGroupName}));
                }
            } else if (ProvisioningOperation.PATCH.equals(provisioningOperation)) {
                String oldGroupName = provisionedEntityName;
                String currentGroupName = ProvisioningUtil
                        .getAttributeValue(provisioningEntity, IdentityProvisioningConstants.GROUP_CLAIM_URI);
                if (currentGroupName == null) {
                    currentGroupName = oldGroupName;
                }
                if (!oldGroupName.equals(currentGroupName)) {
                    attributeList.put(org.wso2.carbon.identity.application.common.model.ClaimMapping
                                              .build(IdentityProvisioningConstants.OLD_GROUP_NAME_CLAIM_URI,
                                                     null, null, false),
                                      Arrays.asList(new String[]{oldGroupName}));
                    attributeList.put(org.wso2.carbon.identity.application.common.model.ClaimMapping
                                              .build(IdentityProvisioningConstants.NEW_GROUP_NAME_CLAIM_URI,
                                                     null, null, false),
                                      Arrays.asList(new String[]{currentGroupName}));
                }
            }
        }
        String userStoreDomain = ProvisioningUtil.getAttributeValue(provisioningEntity,
                                                                    IdentityProvisioningConstants.USER_STORE_DOMAIN_CLAIM_URI);
        if (log.isDebugEnabled()) {
            log.debug("Adding domain name : " + userStoreDomain + " to name : " + provisionedEntityName);
        }
        provisioningEntity
                .setEntityName(UserCoreUtil.addDomainToName(provisionedEntityName, userStoreDomain));
        return provisioningEntity;
    }


    /**
     * Update the value of given group attribute with mapped roles
     *
     * @param provisioningEntity
     * @param idPRoleMapping
     * @param groupAttributeName
     */
    private void updateMappedGroupForAttribute(ProvisioningEntity provisioningEntity, RoleMapping[] idPRoleMapping,
                                               String groupAttributeName) {

        List<String> groupList = ProvisioningUtil.getClaimValues(provisioningEntity.getAttributes(),
                groupAttributeName, null);
        List<String> mappedGroups = getMappedGroups(groupList, idPRoleMapping);

        if (mappedGroups != null && !mappedGroups.isEmpty()) {
            ProvisioningUtil.setClaimValue(groupAttributeName, provisioningEntity.getAttributes(), mappedGroups);
        }
    }

    /**
     * Check if user roles has common elements with the provisioning role list
     *
     * @param userRoles
     * @param provisioningRoles
     * @param userName
     */
    private boolean userHasProvisioningRoles(List<String> userRoles, List<String> provisioningRoles, String userName) {

        if (CollectionUtils.isNotEmpty(userRoles) && CollectionUtils.isNotEmpty(provisioningRoles)) {
            for(String provisioningRole : provisioningRoles) {
                if (userRoles.contains(provisioningRole)) {
                    if (log.isDebugEnabled()) {
                        log.debug("User with userName : " + userName + " has provisioning role(s) assigned.");
                    }
                    return true;
                }
            }
        }

        return false;
    }
}
