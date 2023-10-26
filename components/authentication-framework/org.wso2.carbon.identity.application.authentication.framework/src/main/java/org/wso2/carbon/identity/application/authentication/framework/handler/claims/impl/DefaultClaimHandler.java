/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.application.authentication.framework.handler.claims.impl;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.config.builder.FileBasedConfigurationBuilder;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ApplicationConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.exception.UserIdNotFoundException;
import org.wso2.carbon.identity.application.authentication.framework.handler.approles.ApplicationRolesResolver;
import org.wso2.carbon.identity.application.authentication.framework.handler.approles.exception.ApplicationRolesException;
import org.wso2.carbon.identity.application.authentication.framework.handler.claims.ClaimHandler;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.model.ClaimConfig;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.IdPGroup;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ApplicationConstants;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataHandler;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.user.api.ClaimManager;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.DiagnosticLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.wso2.carbon.identity.core.util.IdentityUtil.getLocalGroupsClaimURI;

/**
 * Default claim handler implementation.
 */
public class DefaultClaimHandler implements ClaimHandler {

    public static final String SERVICE_PROVIDER_SUBJECT_CLAIM_VALUE =
            FrameworkConstants.SERVICE_PROVIDER_SUBJECT_CLAIM_VALUE;
    private static final Log log = LogFactory.getLog(DefaultClaimHandler.class);
    private static volatile DefaultClaimHandler instance;

    public static DefaultClaimHandler getInstance() {
        if (instance == null) {
            synchronized (DefaultClaimHandler.class) {
                if (instance == null) {
                    instance = new DefaultClaimHandler();
                }
            }
        }
        return instance;
    }

    @Override
    public Map<String, String> handleClaimMappings(StepConfig stepConfig,
                                                   AuthenticationContext context, Map<String, String> remoteClaims,
                                                   boolean isFederatedClaims) throws FrameworkException {

        if (log.isDebugEnabled()) {
            logInput(remoteClaims, isFederatedClaims);
        }

        ApplicationConfig appConfig = context.getSequenceConfig().getApplicationConfig();
        String spStandardDialect = getStandardDialect(context.getRequestType(), appConfig);
        context.setProperty(FrameworkConstants.SP_STANDARD_DIALECT, spStandardDialect);
        List<ClaimMapping> selectedRequestedClaims = FrameworkServiceDataHolder.getInstance()
                .getHighestPriorityClaimFilter().getFilteredClaims(context, appConfig);
        setMandatoryAndRequestedClaims(appConfig, selectedRequestedClaims);
        context.getSequenceConfig().setApplicationConfig(appConfig);

        Map<String, String> returningClaims;
        if (isFederatedClaims) {
            returningClaims = handleFederatedClaims(remoteClaims, spStandardDialect, stepConfig, context);
        } else {
            returningClaims = handleLocalClaims(spStandardDialect, stepConfig, context);
        }
        if (log.isDebugEnabled()) {
            logOutput(returningClaims, context);
        }
        return returningClaims;
    }

    /**
     * @param spStandardDialect
     * @param remoteClaims
     * @param stepConfig
     * @param context
     * @return
     * @throws FrameworkException
     */
    protected Map<String, String> handleFederatedClaims(Map<String, String> remoteClaims, String spStandardDialect,
                                                        StepConfig stepConfig, AuthenticationContext context)
            throws FrameworkException {

        ClaimMapping[] idPClaimMappings = context.getExternalIdP().getClaimMappings();

        if (idPClaimMappings == null) {
            idPClaimMappings = new ClaimMapping[0];
        }

        String applicationRoles =
                getApplicationRolesForFederatedUser(stepConfig, context, idPClaimMappings);

        Map<String, String> spClaimMappings = context.getSequenceConfig().getApplicationConfig().
                getClaimMappings();

        if (spClaimMappings == null) {
            spClaimMappings = new HashMap<>();
        }

        Map<String, String> carbonToStandardClaimMapping;
        Map<String, String> spRequestedClaimMappings = context.getSequenceConfig().getApplicationConfig().
                getRequestedClaimMappings();
        if (StringUtils.isNotBlank(spStandardDialect) && !StringUtils.equals(spStandardDialect, ApplicationConstants
                .LOCAL_IDP_DEFAULT_CLAIM_DIALECT)) {
            carbonToStandardClaimMapping = getCarbonToStandardDialectMapping(spStandardDialect, context,
                    spRequestedClaimMappings, context.getTenantDomain());
            spRequestedClaimMappings = mapRequestClaimsInStandardDialect(spRequestedClaimMappings,
                    carbonToStandardClaimMapping);
            context.setProperty(FrameworkConstants.SP_TO_CARBON_CLAIM_MAPPING, spRequestedClaimMappings);
        }

        ApplicationAuthenticator authenticator = stepConfig.
                getAuthenticatedAutenticator().getApplicationAuthenticator();

        boolean useDefaultIdpDialect = context.getExternalIdP().useDefaultLocalIdpDialect()
                || idPClaimMappings.length == 0;

        // When null the local claim dialect will be used.
        String idPStandardDialect = null;
        if (useDefaultIdpDialect || !useLocalClaimDialectForClaimMappings()) {
            idPStandardDialect = authenticator.getClaimDialectURI();
        }

        // Insert the runtime claims from the context. The priority is for runtime claims.
        remoteClaims.putAll(context.getRuntimeClaims());

        Map<String, String> localUnfilteredClaims = new HashMap<>();
        Map<String, String> spUnfilteredClaims = new HashMap<>();
        Map<String, String> spFilteredClaims = new HashMap<>();
        Map<String, String> localUnfilteredClaimsForNullValues = new HashMap<>();


        // claim mapping from local IDP to remote IDP : local-claim-uri / idp-claim-uri

        Map<String, String> localToIdPClaimMap = null;
        Map<String, String> defaultValuesForClaims = new HashMap<>();

        loadDefaultValuesForClaims(idPClaimMappings, defaultValuesForClaims);

        if (idPStandardDialect != null || useDefaultIdpDialect) {
            localToIdPClaimMap = getLocalToIdpClaimMappingWithStandardDialect(remoteClaims, idPClaimMappings, context,
                    idPStandardDialect);
        } else if (idPClaimMappings.length > 0) {
            localToIdPClaimMap = FrameworkUtils.getClaimMappings(idPClaimMappings, true);
            if (useLocalClaimDialectForClaimMappings() && enableMergingCustomClaimMappingsWithDefaultMappings()) {
                localToIdPClaimMap = filterLocaltoIdPClaimMap(localToIdPClaimMap, remoteClaims.keySet());
                getMergedLocalIdpClaimMappings(authenticator.getClaimDialectURI(),
                        context.getTenantDomain(), localToIdPClaimMap, remoteClaims);
            }
        } else {
            log.warn("Authenticator : " + authenticator.getFriendlyName() + " does not have " +
                     "a standard dialect and IdP : " + context.getExternalIdP().getIdPName() +
                     " does not have custom claim mappings. Cannot proceed with claim mappings");
            return spFilteredClaims;
        }

        // Loop remote claims and map to local claims
        mapRemoteClaimsToLocalClaims(remoteClaims, localUnfilteredClaims, localToIdPClaimMap, defaultValuesForClaims,
                localUnfilteredClaimsForNullValues);

        // Insert the runtime claims from the context. The priority is for runtime claims.
        localUnfilteredClaims.putAll(context.getRuntimeClaims());

        if (StringUtils.isNotBlank(applicationRoles)) {
            localUnfilteredClaims.put(FrameworkConstants.APP_ROLES_CLAIM, applicationRoles);
            if (!CarbonConstants.ENABLE_LEGACY_AUTHZ_RUNTIME) {
                // Add app associated roles to roles claim in Role V2 runtime.
                localUnfilteredClaims.put(getLocalGroupsClaimURI(), applicationRoles);
            }
        }

        // claim mapping from local service provider to remote service provider.
        Map<String, String> localToSPClaimMappings = mapLocalSpClaimsToRemoteSPClaims(spStandardDialect, context,
                                                                                      spClaimMappings);

        // Loop through <code>localToSPClaimMappings</code> and filter
        // <code>spUnfilteredClaims</code> and <code>spFilteredClaims</code>
        filterSPClaims(spRequestedClaimMappings, localUnfilteredClaims, spUnfilteredClaims, spFilteredClaims,
                       localToSPClaimMappings);

        if (stepConfig.isSubjectAttributeStep()) {
            if (MapUtils.isNotEmpty(localUnfilteredClaimsForNullValues)) {
            /*
             Set all locally mapped unfiltered null remote claims as a property.
             This property will used to retrieve unfiltered null value claims.
             */
                context.setProperty(FrameworkConstants.UNFILTERED_LOCAL_CLAIMS_FOR_NULL_VALUES,
                        localUnfilteredClaimsForNullValues);
            }

            // set unfiltered remote claims as a property
            context.setProperty(FrameworkConstants.UNFILTERED_IDP_CLAIM_VALUES, remoteClaims);
            // set all locally mapped unfiltered remote claims as a property
            context.setProperty(FrameworkConstants.UNFILTERED_LOCAL_CLAIM_VALUES, localUnfilteredClaims);
            // set all service provider mapped unfiltered remote claims as a property
            context.setProperty(FrameworkConstants.UNFILTERED_SP_CLAIM_VALUES, spUnfilteredClaims);
        }

        if (FrameworkConstants.RequestType.CLAIM_TYPE_OPENID.equals(context.getRequestType())) {
            spFilteredClaims = spUnfilteredClaims;
        }

        // set the subject claim URI as a property
        if (stepConfig.isSubjectIdentifierStep()) {
            if (spStandardDialect != null) {
                setSubjectClaimForFederatedClaims(localUnfilteredClaims, spStandardDialect, context);
            } else {
                setSubjectClaimForFederatedClaims(spUnfilteredClaims, null, context);
            }
        }


        //Add multi Attributes separator with claims.it can be defined in user-mgt.xml file
        UserRealm realm = getUserRealm(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        UserStoreManager userStore = getUserStoreManager(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, realm);
        addMultiAttributeSeparatorToRequestedClaims(null, userStore, spFilteredClaims, realm);

        return spFilteredClaims;

    }

    /**
     * Filter local claim mapping only if the claim value is there in the remote claim set.
     *
     * @param localToIdPClaimMap    Local to IdP claim mapping.
     * @param keySet                Claim keys of remote claim set.
     * @return  the local to idp claim mappings which comes in the remote claims.
     */
    private Map<String, String> filterLocaltoIdPClaimMap(Map<String, String> localToIdPClaimMap, Set<String> keySet) {

        return new HashMap<>(localToIdPClaimMap.entrySet().stream()
                .filter(claimMap -> keySet.contains(claimMap.getValue()))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue)));
    }

    private void setMandatoryAndRequestedClaims(ApplicationConfig appConfig,
                                                             List<ClaimMapping> selectedRequestedClaims) {


        Map<String, String> claimMappings = new HashMap<>();
        Map<String, String> requestedClaims = new HashMap<>();
        Map<String, String> mandatoryClaims = new HashMap<>();

        if (isNotEmpty(selectedRequestedClaims)) {
            selectedRequestedClaims.stream().filter(claim -> claim.getRemoteClaim() != null
                    && claim.getRemoteClaim().getClaimUri() != null).forEach(claim -> {
                if (claim.getLocalClaim() != null) {
                    setClaimsWhenLocalClaimExists(claimMappings, requestedClaims, mandatoryClaims, claim);
                } else {
                    setClaimsWhenLocalClaimNotExists(claimMappings, requestedClaims, mandatoryClaims, claim);
                }
            });
        }
        appConfig.setClaimMappings(claimMappings);
        appConfig.setRequestedClaims(requestedClaims);
        appConfig.setMandatoryClaims(mandatoryClaims);

        if (LoggerUtils.isDiagnosticLogsEnabled()) {
            Map<String, Object> params = new HashMap<>();
            params.put(FrameworkConstants.LogConstants.SERVICE_PROVIDER, appConfig.getApplicationName());
            Optional.ofNullable(requestedClaims.entrySet()).ifPresent(entries -> {
                List<String> claimsList = entries.stream().map(Entry::getKey).collect(Collectors.toList());
                params.put(FrameworkConstants.LogConstants.REQUESTED_CLAIMS, claimsList);
            });
            Optional.ofNullable(mandatoryClaims.entrySet()).ifPresent(entries -> {
                List<String> claimsList = entries.stream().map(Entry::getKey).collect(Collectors.toList());
                params.put(FrameworkConstants.LogConstants.MANDATORY_CLAIMS, claimsList);
            });
            if (LoggerUtils.isDiagnosticLogsEnabled()) {
                LoggerUtils.triggerDiagnosticLogEvent(new DiagnosticLog.DiagnosticLogBuilder(
                        FrameworkConstants.LogConstants.AUTHENTICATION_FRAMEWORK,
                        FrameworkConstants.LogConstants.ActionIDs.HANDLE_CLAIM_MAPPING)
                        .resultMessage("Handling service provider requested claims.")
                        .logDetailLevel(DiagnosticLog.LogDetailLevel.APPLICATION)
                        .resultStatus(DiagnosticLog.ResultStatus.SUCCESS));
            }
        }
    }

    private void setClaimsWhenLocalClaimNotExists(Map<String, String> claimMappings,
                                                  Map<String, String> requestedClaims,
                                                  Map<String, String> mandatoryClaims, ClaimMapping claim) {

        claimMappings.put(claim.getRemoteClaim().getClaimUri(), null);
        if (claim.isRequested()) {
            requestedClaims.put(claim.getRemoteClaim().getClaimUri(), null);
        }
        if (claim.isMandatory()) {
            mandatoryClaims.put(claim.getRemoteClaim().getClaimUri(), null);
        }
    }

    private void setClaimsWhenLocalClaimExists(Map<String, String> claimMappings, Map<String, String> requestedClaims,
                                               Map<String, String> mandatoryClaims, ClaimMapping claim) {

        claimMappings.put(claim.getRemoteClaim().getClaimUri(), claim
                .getLocalClaim().getClaimUri());
        if (claim.isRequested()) {
            requestedClaims.put(claim.getRemoteClaim().getClaimUri(), claim
                    .getLocalClaim().getClaimUri());
        }
        if (claim.isMandatory()) {
            mandatoryClaims.put(claim.getRemoteClaim().getClaimUri(), claim
                    .getLocalClaim().getClaimUri());
        }
    }

    private void filterSPClaims(Map<String, String> spRequestedClaimMappings, Map<String, String> localUnfilteredClaims,
                                Map<String, String> spUnfilteredClaims, Map<String, String> spFilteredClaims,
                                Map<String, String> localToSPClaimMappings) {

        localToSPClaimMappings.entrySet().stream().filter(entry -> StringUtils.isNotBlank(localUnfilteredClaims.
                get(entry.getKey()))).forEach(entry -> {
                    spUnfilteredClaims.put(entry.getValue(), localUnfilteredClaims.get(entry.getKey()));
                    if (StringUtils.isNotBlank(spRequestedClaimMappings.get(entry.getValue())) ||
                            FrameworkConstants.APP_ROLES_CLAIM.equals(entry.getKey())) {
                        spFilteredClaims.put(entry.getValue(), localUnfilteredClaims.get(entry.getKey()));
                    }
                }
                                             );

    }

    private Map<String, String> mapLocalSpClaimsToRemoteSPClaims(String spStandardDialect,
                                                                 AuthenticationContext context,
                                                                 Map<String, String> spClaimMappings)
            throws FrameworkException {
        Map<String, String> localToSPClaimMappings = null;

        if (spStandardDialect != null) {
            // passing null for keySet argument to get all claim mappings,
            // since we don't know required claim mappings in advance
            // Key:value -> carbon_dialect:standard_dialect
            try {
                localToSPClaimMappings = getClaimMappings(spStandardDialect, null,
                                                          context.getTenantDomain(), true);
            } catch (Exception e) {
                throw new FrameworkException("Error occurred while getting all claim mappings from " +
                                             spStandardDialect + " dialect to " +
                                             ApplicationConstants.LOCAL_IDP_DEFAULT_CLAIM_DIALECT + " dialect for " +
                                             context.getTenantDomain() + " to handle federated claims", e);
            }
        } else if (!spClaimMappings.isEmpty()) {
            localToSPClaimMappings = FrameworkUtils.getLocalToSPClaimMappings(spClaimMappings);
        } else { // no standard dialect and no custom claim mappings
            throw new AssertionError("Authenticator Error! Authenticator does not have a " +
                                     "standard dialect and no custom claim mappings defined for IdP");
        }
        return localToSPClaimMappings;
    }

    private void mapRemoteClaimsToLocalClaims(Map<String, String> remoteClaims,
                                              Map<String, String> localUnfilteredClaims,
                                              Map<String, String> localToIdPClaimMap,
                                              Map<String, String> defaultValuesForClaims,
                                              Map<String, String> localUnfilteredClaimsForNullValues) {
        for (Entry<String, String> entry : localToIdPClaimMap.entrySet()) {
            String localClaimURI = entry.getKey();
            String claimValue = remoteClaims.get(localToIdPClaimMap.get(localClaimURI));
            if (StringUtils.isEmpty(claimValue)) {
                claimValue = defaultValuesForClaims.get(localClaimURI);
            }
            if (!StringUtils.isEmpty(claimValue)) {
                localUnfilteredClaims.put(localClaimURI, claimValue);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Claim " + localClaimURI + " has null value or blank.");
                }
                localUnfilteredClaimsForNullValues.put(localClaimURI, claimValue);
            }
        }
    }

    /**
     * Combine the Idp claim mapping with the default mapping.
     *
     * @param idPStandardDialect    Standard Idp dialect URI.
     * @param tenantDomain          tenant domain.
     * @param localToIdPClaimMap    default local to idp claim mapping.
     * @param remoteClaims          Claims from idp.
     * @return combined claim mappings.
     * @throws FrameworkException   If an exception occurred in combining the idp claims with default claims.
     */
    private Map<String, String> getMergedLocalIdpClaimMappings(String idPStandardDialect, String tenantDomain,
                                                                Map<String, String> localToIdPClaimMap,
                                                                Map<String, String> remoteClaims) throws
            FrameworkException {

        if (idPStandardDialect == null) {
            idPStandardDialect = ApplicationConstants.LOCAL_IDP_DEFAULT_CLAIM_DIALECT;
        }
        try {
            Map<String, String> localToIdpClaimMappingWithStandardDialect =
                    getClaimMappings(idPStandardDialect, remoteClaims.keySet(),
                            tenantDomain, true);
            localToIdPClaimMap.putAll(localToIdpClaimMappingWithStandardDialect.entrySet().stream()
                    .filter(x -> !localToIdPClaimMap.containsKey(x.getKey()))
                    .collect(Collectors.toMap(Entry::getKey, Entry::getValue)));

            return localToIdPClaimMap;
        } catch (FrameworkException e) {
            throw new FrameworkException("Error occurred while getting all claim mappings from " +
                    idPStandardDialect + " dialect for " +
                    tenantDomain + " to handle federated claims", e);
        }
    }

    private Map<String, String> getLocalToIdpClaimMappingWithStandardDialect(Map<String, String> remoteClaims,
                                                                             ClaimMapping[] idPClaimMappings,
                                                                             AuthenticationContext context,
                                                                             String idPStandardDialect)
            throws FrameworkException {
        Map<String, String> localToIdPClaimMap;
        if (idPStandardDialect == null) {
            idPStandardDialect = ApplicationConstants.LOCAL_IDP_DEFAULT_CLAIM_DIALECT;
        }

        try {
            localToIdPClaimMap = getClaimMappings(idPStandardDialect,
                                                  remoteClaims.keySet(), context.getTenantDomain(), true);
        } catch (Exception e) {
            throw new FrameworkException("Error occurred while getting claim mappings for " +
                                         "received remote claims from " +
                                         idPStandardDialect + " dialect to " +
                                         ApplicationConstants.LOCAL_IDP_DEFAULT_CLAIM_DIALECT + " dialect for " +
                                         context.getTenantDomain() + " to handle federated claims", e);
        }
        // adding remote claims with default values also to the key set because they may not come from the federated IdP
        localToIdPClaimMap.putAll(Arrays.stream(idPClaimMappings).filter(claimMapping -> StringUtils.
                isNotBlank(claimMapping.getDefaultValue()) && !localToIdPClaimMap.containsKey(claimMapping.
                getLocalClaim().getClaimUri())).collect(Collectors.toMap(claimMapping -> claimMapping.getLocalClaim().
                getClaimUri(), ClaimMapping::getDefaultValue)));

        return localToIdPClaimMap;
    }

    private void loadDefaultValuesForClaims(ClaimMapping[] idPClaimMappings,
                                            Map<String, String> defaultValuesForClaims) {

        defaultValuesForClaims.putAll(Arrays.asList(idPClaimMappings).stream().filter(claimMapping -> StringUtils.
                isNotBlank(claimMapping.getDefaultValue())).collect(Collectors.toMap(claimMapping -> claimMapping.
                getLocalClaim().getClaimUri(), claimMapping -> claimMapping.getDefaultValue())));
    }

    /**
     * @param applicationConfig
     * @param locallyMappedUserRoles
     * @return
     */
    private static String getServiceProviderMappedUserRoles(ApplicationConfig applicationConfig,
                                                            List<String> locallyMappedUserRoles, String claimSeparator)
            throws FrameworkException {

        Map<String, String> localToSpRoleMapping = applicationConfig.getRoleMappings();

        if (MapUtils.isNotEmpty(localToSpRoleMapping)) {

            localToSpRoleMapping.entrySet().stream().filter(roleMapping -> locallyMappedUserRoles.contains(roleMapping.
                    getKey())).forEach(roleMapping -> {
                        locallyMappedUserRoles.remove(roleMapping.getKey());
                        locallyMappedUserRoles.add(roleMapping.getValue());

                    }
            );
        }

        return StringUtils.join(locallyMappedUserRoles, claimSeparator);
    }

    /**
     * @param context
     * @return
     * @throws FrameworkException
     */
    protected Map<String, String> handleLocalClaims(String spStandardDialect,
                                                    StepConfig stepConfig,
                                                    AuthenticationContext context)
            throws FrameworkException {

        ApplicationConfig appConfig = context.getSequenceConfig().getApplicationConfig();

        Map<String, String> spToLocalClaimMappings = appConfig.getClaimMappings();
        if (spToLocalClaimMappings == null) {
            spToLocalClaimMappings = new HashMap<>();
        }

        Map<String, String> carbonToStandardClaimMapping;
        Map<String, String> requestedClaimMappings = appConfig.getRequestedClaimMappings();
        if (requestedClaimMappings == null) {
            requestedClaimMappings = new HashMap<>();
        }

        AuthenticatedUser authenticatedUser = getAuthenticatedUser(stepConfig, context);

        String tenantDomain = authenticatedUser.getTenantDomain();

        UserRealm realm = getUserRealm(tenantDomain);

        if (realm == null) {
            log.warn("No valid tenant domain provider. No claims returned back");
            return new HashMap<>();
        }

        ClaimManager claimManager = getClaimManager(tenantDomain, realm);

        AbstractUserStoreManager userStore = getUserStoreManager(tenantDomain, realm);

        // key:value -> carbon_dialect:claim_value
        Map<String, String> allLocalClaims;

        // If default dialect -> all non-null user claims
        // If custom dialect -> all non-null user claims that have been mapped to custom claims
        // key:value -> sp_dialect:claim_value
        Map<String, String> allSPMappedClaims = new HashMap<>();

        // Requested claims only
        // key:value -> sp_dialect:claim_value
        Map<String, String> spRequestedClaims = new HashMap<>();

        // Retrieve all non-null user claim values against local claim uris.
        allLocalClaims = retrieveAllNunNullUserClaimValues(authenticatedUser, claimManager, appConfig, userStore);

        String applicationRoles = getApplicationRoles(authenticatedUser, context);

        handleApplicationRolesForLocalUser(stepConfig, context, allLocalClaims, applicationRoles);

        if (!CarbonConstants.ENABLE_LEGACY_AUTHZ_RUNTIME) {
            // Handle app associated roles in roles claim in Role V2 runtime.
            handleRoleAppAssoication(allLocalClaims, applicationRoles);
        }

        // Insert the runtime claims from the context. The priority is for runtime claims.
        allLocalClaims.putAll(context.getRuntimeClaims());

        handleRoleClaim(context, allLocalClaims);

        // if standard dialect get all claim mappings from standard dialect to carbon dialect
        spToLocalClaimMappings = getStandardDialectToCarbonMapping(spStandardDialect, context, spToLocalClaimMappings,
                tenantDomain);
        if (StringUtils.isNotBlank(spStandardDialect) && (!StringUtils.equals(spStandardDialect, ApplicationConstants
                .LOCAL_IDP_DEFAULT_CLAIM_DIALECT))) {
            carbonToStandardClaimMapping = getCarbonToStandardDialectMapping(spStandardDialect, context,
                    spToLocalClaimMappings, tenantDomain);
            requestedClaimMappings = mapRequestClaimsInStandardDialect(requestedClaimMappings,
                    carbonToStandardClaimMapping);
            context.setProperty(FrameworkConstants.SP_TO_CARBON_CLAIM_MAPPING, requestedClaimMappings);
        }

        mapSPClaimsAndFilterRequestedClaims(spToLocalClaimMappings, requestedClaimMappings, allLocalClaims,
                                            allSPMappedClaims, spRequestedClaims);
        if (stepConfig == null || stepConfig.isSubjectAttributeStep()) {
            context.setProperty(FrameworkConstants.UNFILTERED_LOCAL_CLAIM_VALUES, allLocalClaims);
            context.setProperty(FrameworkConstants.UNFILTERED_SP_CLAIM_VALUES, allSPMappedClaims);
        }

        if (stepConfig == null || stepConfig.isSubjectIdentifierStep()) {
            if (spStandardDialect != null) {
                setSubjectClaimForLocalClaims(authenticatedUser, userStore, allLocalClaims, spStandardDialect, context);
            } else {
                setSubjectClaimForLocalClaims(authenticatedUser, userStore, allSPMappedClaims, null, context);
            }
        }


        if (FrameworkConstants.RequestType.CLAIM_TYPE_OPENID.equals(context.getRequestType())) {
            spRequestedClaims = allSPMappedClaims;
        }

        /*
        * This is a custom change added to pass 'MultipleAttributeSeparator' attribute value to other components,
        * since we can't get the logged in user in some situations.
        *
        * Following components affected from this change -
        * org.wso2.carbon.identity.application.authentication.endpoint
        * org.wso2.carbon.identity.provider
        * org.wso2.carbon.identity.oauth
        * org.wso2.carbon.identity.oauth.endpoint
        * org.wso2.carbon.identity.sso.saml
        *
        * TODO: Should use Map<String, List<String>> in future for claim mapping
        * */
        addMultiAttributeSeparatorToRequestedClaims(authenticatedUser, userStore, spRequestedClaims, realm);

        return spRequestedClaims;
    }

    private Map<String, String> mapRequestClaimsInStandardDialect(Map<String, String> requestedClaimMappings,
                                                                  Map<String, String> carbonToStandardClaimMapping) {

        if (MapUtils.isEmpty(requestedClaimMappings)) {
            return new HashMap<>();
        }

        return requestedClaimMappings.values().stream()
                .distinct()
                .filter(mapping -> StringUtils.
                isNotBlank(carbonToStandardClaimMapping.get(mapping)))
                .collect(Collectors.toMap(carbonToStandardClaimMapping::get, Function.identity()));
    }

    private void addMultiAttributeSeparatorToRequestedClaims(AuthenticatedUser authenticatedUser,
                                                             org.wso2.carbon.user.core.UserStoreManager userStore,
                                                             Map<String, String> spRequestedClaims, UserRealm realm)
            throws FrameworkException {

        if (!spRequestedClaims.isEmpty()) {
            if (authenticatedUser != null && StringUtils.isNotBlank(authenticatedUser.getUserStoreDomain())) {
                try {
                    userStore = realm.getUserStoreManager()
                            .getSecondaryUserStoreManager(authenticatedUser.getUserStoreDomain());
                } catch (org.wso2.carbon.user.core.UserStoreException e) {
                    throw new FrameworkException("Error while retrieving the user store manager", e);
                }
            }
            RealmConfiguration realmConfiguration = userStore.getRealmConfiguration();

            String claimSeparator = realmConfiguration.getUserStoreProperty(IdentityCoreConstants
                    .MULTI_ATTRIBUTE_SEPARATOR);
            if (StringUtils.isNotBlank(claimSeparator)) {
                spRequestedClaims.putIfAbsent(IdentityCoreConstants.MULTI_ATTRIBUTE_SEPARATOR, claimSeparator);
            }
        }
    }

    private void mapSPClaimsAndFilterRequestedClaims(Map<String, String> spToLocalClaimMappings,
                                                     Map<String, String> requestedClaimMappings,
                                                     Map<String, String> allLocalClaims,
                                                     Map<String, String> allSPMappedClaims,
                                                     Map<String, String> spRequestedClaims) {

        spToLocalClaimMappings.entrySet().stream().filter(entry -> StringUtils.isNotBlank(allLocalClaims.get(entry.
                getValue()))).forEach(entry -> {
                    allSPMappedClaims.put(entry.getKey(), allLocalClaims.get(entry.getValue()));
                    if (requestedClaimMappings.get(entry.getKey()) != null) {
                        spRequestedClaims.put(entry.getKey(), allLocalClaims.get(entry.getValue()));
                    }
                }
        );

    }

    private Map<String, String> getStandardDialectToCarbonMapping(String spStandardDialect,
                                                                 AuthenticationContext context,
                                                                 Map<String, String> spToLocalClaimMappings,
                                                                 String tenantDomain) throws FrameworkException {
        if (spStandardDialect != null) {
            try {
                spToLocalClaimMappings = getClaimMappings(spStandardDialect, null,
                                                          context.getTenantDomain(), false);
            } catch (Exception e) {
                throw new FrameworkException("Error occurred while getting all claim mappings from " +
                                             spStandardDialect + " dialect to " +
                                             ApplicationConstants.LOCAL_IDP_DEFAULT_CLAIM_DIALECT + " dialect for " +
                                             tenantDomain + " to handle local claims", e);
            }
        }
        return spToLocalClaimMappings;
    }

    private Map<String, String> getCarbonToStandardDialectMapping(String spStandardDialect,
                                                                  AuthenticationContext context,
                                                                  Map<String, String> spToLocalClaimMappings,
                                                                  String tenantDomain) throws FrameworkException {
        if (spStandardDialect != null) {
            try {
                spToLocalClaimMappings = getClaimMappings(spStandardDialect, null,
                        context.getTenantDomain(), true);
            } catch (Exception e) {
                throw new FrameworkException("Error occurred while getting all claim mappings from " +
                        ApplicationConstants.LOCAL_IDP_DEFAULT_CLAIM_DIALECT + " dialect to " +
                         spStandardDialect + " dialect for " + tenantDomain + " to handle local claims", e);
            }
        }
        return spToLocalClaimMappings;
    }

    private Map<String, String> retrieveAllNunNullUserClaimValues(AuthenticatedUser authenticatedUser,
            ClaimManager claimManager, ApplicationConfig appConfig,
            AbstractUserStoreManager userStore) throws FrameworkException {

        String tenantDomain = authenticatedUser.getTenantDomain();

        Map<String, String> allLocalClaims = new HashMap<>();
        try {

            org.wso2.carbon.user.api.ClaimMapping[] claimMappings = claimManager
                    .getAllClaimMappings(ApplicationConstants.LOCAL_IDP_DEFAULT_CLAIM_DIALECT);
            List<String> localClaimURIs = new ArrayList<>();
            for (org.wso2.carbon.user.api.ClaimMapping mapping : claimMappings) {
                String claimURI = mapping.getClaim().getClaimUri();
                localClaimURIs.add(claimURI);
            }
            allLocalClaims = userStore.getUserClaimValuesWithID(authenticatedUser.getUserId(),
                    localClaimURIs.toArray(new String[0]), null);

            if (allLocalClaims == null) {
                return new HashMap<>();
            }
        } catch (UserStoreException e) {
            if (e.getMessage().contains("UserNotFound")) {
                if (log.isDebugEnabled()) {
                    log.debug("User " + authenticatedUser.getLoggableUserId() + " not found in user store");
                }
            } else {
                throw new FrameworkException("Error occurred while getting all user claims for " +
                        authenticatedUser.getLoggableUserId() + " in " + tenantDomain, e);
            }
        } catch (UserIdNotFoundException e) {
            throw new FrameworkException("User id is not available for user: " + authenticatedUser.getLoggableUserId(),
                    e);
        }
        return allLocalClaims;
    }

    private AbstractUserStoreManager getUserStoreManager(String tenantDomain, UserRealm realm) throws
            FrameworkException {
        AbstractUserStoreManager userStore;
        try {
            userStore = (AbstractUserStoreManager) realm.getUserStoreManager();
        } catch (UserStoreException e) {
            throw new FrameworkException("Error occurred while retrieving the UserStoreManager " +
                    "from Realm for " + tenantDomain + " to handle local claims", e);
        }
        return userStore;
    }

    private ClaimManager getClaimManager(String tenantDomain, UserRealm realm) throws FrameworkException {
        ClaimManager claimManager = null;
        try {
            claimManager = realm.getClaimManager();
        } catch (UserStoreException e) {
            throw new FrameworkException("Error occurred while retrieving the ClaimManager " +
                                         "from Realm for " + tenantDomain + " to handle local claims", e);
        }
        return claimManager;
    }

    private UserRealm getUserRealm(String tenantDomain) throws FrameworkException {
        UserRealm realm;
        try {
            RealmService realmService = FrameworkServiceDataHolder.getInstance().getRealmService();
            int tenantId = realmService.getTenantManager().getTenantId(tenantDomain);

            realm = (UserRealm) realmService.getTenantUserRealm(tenantId);
        } catch (UserStoreException e) {
            throw new FrameworkException("Error occurred while retrieving the Realm for " +
                                         tenantDomain + " to handle local claims", e);
        }
        return realm;
    }

    private AuthenticatedUser getAuthenticatedUser(StepConfig stepConfig, AuthenticationContext context) {

        AuthenticatedUser authenticatedUser;
        if (stepConfig != null) {
            // Calling from StepBasedSequenceHandler.
            authenticatedUser = stepConfig.getAuthenticatedUser();
            if (log.isDebugEnabled()) {
                log.debug("Authenticated user found from step config.");
            }
        } else {
            // Calling from RequestPathBasedSequenceHandler.
            authenticatedUser = context.getSequenceConfig().getAuthenticatedUser();
            if (log.isDebugEnabled()) {
                log.debug("Authenticated user found from authentication context.");
            }
        }
        return authenticatedUser;
    }

    /**
     * Set federated subject's SP Subject Claim URI as a property
     */
    private void setSubjectClaimForFederatedClaims(Map<String, String> attributesMap,
                                                   String spStandardDialect,
                                                   AuthenticationContext context) {

        String subjectURI = context.getSequenceConfig().getApplicationConfig().getSubjectClaimUri();
        if (subjectURI != null && !subjectURI.isEmpty()) {
            if (spStandardDialect != null) {
                setSubjectClaim(null, null, attributesMap, spStandardDialect, context);
                if (context.getProperty(SERVICE_PROVIDER_SUBJECT_CLAIM_VALUE) == null) {
                    log.warn("Subject claim could not be found amongst locally mapped " +
                             "unfiltered remote claims");
                }
            } else {
                setSubjectClaim(null, null, attributesMap, null, context);
                if (context.getProperty(SERVICE_PROVIDER_SUBJECT_CLAIM_VALUE) == null) {
                    log.warn("Subject claim could not be found amongst service provider mapped " +
                             "unfiltered remote claims");
                }
            }
        }
    }

    /**
     * Set federated subject's SP Subject Claim URI as a property
     */
    private void setSubjectClaimForLocalClaims(AuthenticatedUser authenticatedUser,
                                               AbstractUserStoreManager userStore,
                                               Map<String, String> attributesMap,
                                               String spStandardDialect,
                                               AuthenticationContext context) {

        String subjectURI = context.getSequenceConfig().getApplicationConfig().getSubjectClaimUri();
        if (subjectURI != null && !subjectURI.isEmpty()) {
            if (spStandardDialect != null) {
                setSubjectClaim(authenticatedUser, userStore, attributesMap, spStandardDialect, context);
                if (context.getProperty(SERVICE_PROVIDER_SUBJECT_CLAIM_VALUE) == null) {
                    log.warn("Subject claim could not be found amongst unfiltered local claims");
                }
            } else {
                setSubjectClaim(authenticatedUser, userStore, attributesMap, null, context);
                if (context.getProperty(SERVICE_PROVIDER_SUBJECT_CLAIM_VALUE) == null) {
                    log.warn("Subject claim could not be found amongst service provider mapped " +
                             "unfiltered local claims");
                }
            }
        }
    }

    /**
     * Set authenticated user's SP Subject Claim URI as a property
     */
    private void setSubjectClaim(AuthenticatedUser authenticatedUser, AbstractUserStoreManager userStore,
                                 Map<String, String> attributesMap, String spStandardDialect,
                                 AuthenticationContext context) {

        String subjectURI = context.getSequenceConfig().getApplicationConfig().getSubjectClaimUri();
        ApplicationConfig applicationConfig = context.getSequenceConfig().getApplicationConfig();
        ServiceProvider serviceProvider = applicationConfig.getServiceProvider();
        ClaimConfig claimConfig = serviceProvider.getClaimConfig();
        boolean isLocalClaimDialect = claimConfig.isLocalClaimDialect();
        Map<String, String> spToLocalClaimMappings = applicationConfig.getClaimMappings();
        if (subjectURI != null) {

            if (!isLocalClaimDialect && spStandardDialect != null) {
                if (spToLocalClaimMappings != null) {
                    subjectURI = spToLocalClaimMappings.get(subjectURI);
                }
            }

            if (attributesMap.get(subjectURI) != null) {
                context.setProperty(SERVICE_PROVIDER_SUBJECT_CLAIM_VALUE, attributesMap.get(subjectURI));
                if (log.isDebugEnabled()) {
                    log.debug("Setting \'ServiceProviderSubjectClaimValue\' property value from " +
                            "attribute map " + attributesMap.get(subjectURI));
                }
            } else {
                log.debug("Subject claim not found among attributes");
            }

            // if federated case return
            if (authenticatedUser == null || userStore == null || authenticatedUser.isFederatedUser()) {
                if (log.isDebugEnabled()) {
                    log.debug("User id or user store \'NULL\'. Possibly federated case");
                }
                return;
            }

            // standard dialect
            if (spStandardDialect != null) {
                setSubjectClaimForStandardDialect(authenticatedUser, userStore, context, subjectURI);
            }
        }
    }

    private void setSubjectClaimForStandardDialect(AuthenticatedUser authenticatedUser,
                                                   AbstractUserStoreManager userStore,
                                                   AuthenticationContext context, String subjectURI) {
        try {
            String value = userStore.getUserClaimValueWithID(authenticatedUser.getUserId(), subjectURI, null);
            if (value != null) {
                context.setProperty(SERVICE_PROVIDER_SUBJECT_CLAIM_VALUE, value);
                if (log.isDebugEnabled()) {
                    log.debug("Setting \'ServiceProviderSubjectClaimValue\' property value " +
                              "from user store " + value);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Subject claim for " + authenticatedUser.getLoggableUserId()
                            + " not found in user store");
                }
            }
        } catch (UserStoreException e) {
            log.error("Error occurred while retrieving " + subjectURI + " claim value for user "
                            + authenticatedUser.getLoggableUserId(), e);
        } catch (UserIdNotFoundException e) {
            log.error("User id is not available for user: " + authenticatedUser.getLoggableUserId(), e);
        }
    }

    /**
     * @param otherDialect
     * @param keySet
     * @param tenantDomain
     * @param useLocalDialectAsKey
     * @return
     * @throws FrameworkException
     */
    private Map<String, String> getClaimMappings(String otherDialect, Set<String> keySet,
                                                 String tenantDomain, boolean useLocalDialectAsKey)
            throws FrameworkException {

        Map<String, String> claimMapping = null;
        try {
            claimMapping = ClaimMetadataHandler.getInstance()
                    .getMappingsMapFromOtherDialectToCarbon(otherDialect, keySet, tenantDomain,
                                                            useLocalDialectAsKey);
        } catch (ClaimMetadataException e) {
            throw new FrameworkException("Error while loading mappings.", e);
        }

        if (claimMapping == null) {
            claimMapping = new HashMap<>();
        }

        return claimMapping;
    }

    /**
     * Returns the claim dialect URI based on the client type
     *
     * @param clientType
     * @param appConfig
     * @return standard dialect -> SP uses standard dialect; carbon or other
     * null -> SP uses custom dialect
     */
    protected String getStandardDialect(String clientType, ApplicationConfig appConfig) {

        Map<String, String> claimMappings = appConfig.getClaimMappings();
        if (FrameworkConstants.RequestType.CLAIM_TYPE_OIDC.equals(clientType)) {
            return "http://wso2.org/oidc/claim";
        } else if (FrameworkConstants.RequestType.CLAIM_TYPE_STS.equals(clientType)) {
            return "http://schemas.xmlsoap.org/ws/2005/05/identity";
        } else if (FrameworkConstants.RequestType.CLAIM_TYPE_OPENID.equals(clientType)) {
            return "http://axschema.org";
        } else if (FrameworkConstants.RequestType.CLAIM_TYPE_SCIM.equals(clientType)) {
            return "urn:scim:schemas:core:1.0";
        } else if (FrameworkConstants.RequestType.CLAIM_TYPE_WSO2.equals(clientType)) {
            return ApplicationConstants.LOCAL_IDP_DEFAULT_CLAIM_DIALECT;
        } else if (claimMappings == null || claimMappings.isEmpty()) {
            return ApplicationConstants.LOCAL_IDP_DEFAULT_CLAIM_DIALECT;
        } else {
            boolean isAtLeastOneNotEqual = false;
            for (Map.Entry<String, String> entry : claimMappings.entrySet()) {
                if (!entry.getKey().equals(entry.getValue())) {
                    isAtLeastOneNotEqual = true;
                    break;
                }
            }
            if (!isAtLeastOneNotEqual) {
                return ApplicationConstants.LOCAL_IDP_DEFAULT_CLAIM_DIALECT;
            }
        }
        return null;
    }

    private void logInput(Map<String, String> remoteClaims, boolean isFederatedClaims) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        if (remoteClaims != null) {
            for (Map.Entry<String, String> entry : remoteClaims.entrySet()) {
                sb.append(entry.getKey());
                sb.append(":");
                sb.append(entry.getValue());
                sb.append(",");
            }
        }
        sb.append("]");
        log.debug("Executing claim handler. isFederatedClaims = " + isFederatedClaims +
                  " and remote claims = " + sb.toString());
    }

    private void logOutput(Map<String, String> returningClaims, AuthenticationContext context) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (Map.Entry<String, String> entry : returningClaims.entrySet()) {
            sb.append(entry.getKey());
            sb.append(":");
            sb.append(entry.getValue());
            sb.append(",");
        }
        sb.append("]");
        log.debug("Returning claims from claim handler = " + sb.toString());
        Map<String, String> claimsProperty = (Map<String, String>)
                context.getProperty(FrameworkConstants.UNFILTERED_IDP_CLAIM_VALUES);
        if (claimsProperty != null) {
            sb = new StringBuilder();
            sb.append("[");
            for (Map.Entry<String, String> entry : claimsProperty.entrySet()) {
                sb.append(entry.getKey());
                sb.append(":");
                sb.append(entry.getValue());
                sb.append(",");
            }
            sb.append("]");
        }
        log.debug(FrameworkConstants.UNFILTERED_IDP_CLAIM_VALUES +
                  " map property set to " + sb.toString());
        claimsProperty = (Map<String, String>)
                context.getProperty(FrameworkConstants.UNFILTERED_LOCAL_CLAIM_VALUES);
        if (claimsProperty != null) {
            sb = new StringBuilder();
            sb.append("[");
            for (Map.Entry<String, String> entry : claimsProperty.entrySet()) {
                sb.append(entry.getKey());
                sb.append(":");
                sb.append(entry.getValue());
                sb.append(",");
            }
            sb.append("]");
        }
        log.debug(FrameworkConstants.UNFILTERED_LOCAL_CLAIM_VALUES +
                  " map property set to " + sb.toString());
        claimsProperty = (Map<String, String>)
                context.getProperty(FrameworkConstants.UNFILTERED_SP_CLAIM_VALUES);
        if (claimsProperty != null) {
            sb = new StringBuilder();
            sb.append("[");
            for (Map.Entry<String, String> entry : claimsProperty.entrySet()) {
                sb.append(entry.getKey());
                sb.append(":");
                sb.append(entry.getValue());
                sb.append(",");
            }
            sb.append("]");
        }
        log.debug(FrameworkConstants.UNFILTERED_SP_CLAIM_VALUES +
                  " map property set to " + sb.toString());
    }

    /**
     * Checks if a configuration is available indicating to use the local claim
     * dialect instead of the federated authenticator's dialect when a custom dialect
     * claim mapping is used.
     *
     * @return True if local claim dialect should be used.
     */
    private boolean useLocalClaimDialectForClaimMappings() {

        return FileBasedConfigurationBuilder.getInstance().isCustomClaimMappingsForAuthenticatorsAllowed();
    }

    /**
     * Checks if a configuration is available indicating to combine the custom claim
     * dialect with the federated authenticator's dialect when a custom dialect
     * claim mapping is used.
     *
     * @return True if both need to be combined.
     */
    private boolean enableMergingCustomClaimMappingsWithDefaultMappings() {

        return FileBasedConfigurationBuilder.getInstance()
                .isMergingCustomClaimMappingsWithDefaultClaimMappingsAllowed();
    }

    /**
     * Handle role app association in roles claim.
     *
     * @param appAssociatedRoles App associated roles.
     * @param mappedAttrs Mapped claim attributes.
     */
    private void handleRoleAppAssoication(Map<String, String> mappedAttrs, String appAssociatedRoles) {

        if (mappedAttrs.containsKey(getLocalGroupsClaimURI())) {
            mappedAttrs.put(getLocalGroupsClaimURI(),
                    StringUtils.isEmpty(appAssociatedRoles) ? "" : appAssociatedRoles);
        }
    }

    /**
     * Specially handle role claim values.
     *
     * @param context Authentication context.
     * @param mappedAttrs Mapped claim attributes.
     */
    private void handleRoleClaim(AuthenticationContext context, Map<String, String> mappedAttrs) {

        if (mappedAttrs.containsKey(getLocalGroupsClaimURI())) {
            String[] groups = mappedAttrs.get(getLocalGroupsClaimURI()).split(Pattern
                    .quote(FrameworkUtils.getMultiAttributeSeparator()));
            SequenceConfig sequenceConfig = context.getSequenceConfig();
            // Execute only if it has allowed removing userstore domain from the sp level configurations.
            if (isRemoveUserDomainInRole(sequenceConfig)) {
                mappedAttrs.put(getLocalGroupsClaimURI(), FrameworkUtils
                        .removeDomainFromNamesExcludeHybrid(Arrays.asList(groups)));
            }
        }
    }

    /**
     * Add the application roles of federated user to remote claims.
     *
     * @param stepConfig       StepConfig of current step.
     * @param context          AuthenticationContext of current authentication flow.
     * @param idPClaimMappings Claim mappings of IdP of the current step.
     * @return Application roles of federated user.
     * @throws FrameworkException Exception on handling application roles for federated user.
     */
    protected String getApplicationRolesForFederatedUser(StepConfig stepConfig, AuthenticationContext context,
                                                         ClaimMapping[] idPClaimMappings) throws FrameworkException {

        // IdP claim mappings should be available and the current step should be a subject attribute step.
        if (idPClaimMappings == null || !stepConfig.isSubjectAttributeStep()) {
            return StringUtils.EMPTY;
        }
        // Get the remote claim URI of the groups claim.
        String remoteClaimURIOfGroupsClaim = Arrays.stream(idPClaimMappings)
                .filter(claimMapping -> claimMapping.getLocalClaim().getClaimUri()
                        .equals(FrameworkConstants.GROUPS_CLAIM))
                .map(claimMapping -> claimMapping.getRemoteClaim().getClaimUri())
                .findFirst()
                .orElse(null);
        // If there is no groups claim mapping, no need to proceed.
        if (StringUtils.isBlank(remoteClaimURIOfGroupsClaim)) {
            return StringUtils.EMPTY;
        }
        // Regardless of whether the application role claim is requested from the SP, we need to add it to the remote
        // claims since otherwise we wouldn't know if application roles are resolved or not at a later stage.
        IdentityProvider identityProvider = context.getExternalIdP().getIdentityProvider();
        if (identityProvider == null) {
            return StringUtils.EMPTY;
        }
        IdPGroup[] possibleIdPGroups = identityProvider.getIdPGroupConfig();
        boolean useAppRoleMapping = ArrayUtils.isNotEmpty(possibleIdPGroups);
        if (useAppRoleMapping) {
            String appRoles = getApplicationRoles(stepConfig.getAuthenticatedUser(), context);
            // Checking if the appRoles string is null but can be an empty string.
            if (appRoles != null) {
                return appRoles;
            }
        }
        return StringUtils.EMPTY;
    }

    /**
     * Add the application roles of local user to local claims.
     *
     * @param stepConfig StepConfig of current step.
     * @param context AuthenticationContext of current authentication flow.
     * @param allLocalClaims All local claims of the current authenticated user.
     * @throws FrameworkException Exception on handling application roles for local user.
     */
    protected void handleApplicationRolesForLocalUser(StepConfig stepConfig, AuthenticationContext context,
                                                    Map<String, String> allLocalClaims, String appAssociatedRoles)
            throws FrameworkException {

        AuthenticatedUser authenticatedUser = getAuthenticatedUser(stepConfig, context);
        if (authenticatedUser == null) {
            return;
        }
        if (stepConfig == null || stepConfig.isSubjectAttributeStep()) {
            String requestedAppRoleClaim = context.getSequenceConfig().getApplicationConfig()
                    .getRequestedClaimMappings().get(FrameworkConstants.APP_ROLES_CLAIM);
            if (requestedAppRoleClaim != null) {
                if (appAssociatedRoles != null) {
                    allLocalClaims.put(FrameworkConstants.APP_ROLES_CLAIM, appAssociatedRoles);
                }
            }
        }
    }

    /**
     * Resolve if the user is JIT provisioned based on the IdP type claim.
     *
     * @param allLocalClaims All local claims of the current authenticated user.
     * @return True if the user is JIT provisioned.
     */
    private boolean isUserJITProvisioned(Map<String, String> allLocalClaims) {

        if (allLocalClaims == null || allLocalClaims.isEmpty()) {
            return false;
        }
        return allLocalClaims.entrySet().stream()
                .anyMatch(entry -> entry.getKey().equals(FrameworkConstants.IDP_TYPE_CLAIM)
                        && !FrameworkConstants.JSAttributes.JS_LOCAL_IDP.equalsIgnoreCase(entry.getValue()));
    }

    /**
     * Get the application roles of the authenticated user for application roles resolver if available.
     *
     * @param authenticatedUser Authenticated user to get the application roles for.
     * @param context           Authentication context.
     * @return Application roles of the authenticated user.
     * @throws FrameworkException Exception on getting application roles.
     */
    private String getApplicationRoles(AuthenticatedUser authenticatedUser, AuthenticationContext context)
            throws FrameworkException {

        ServiceProvider serviceProvider = context.getSequenceConfig().getApplicationConfig().getServiceProvider();
        if (serviceProvider == null) {
            return null;
        }
        String applicationId = serviceProvider.getApplicationResourceId();
        ApplicationRolesResolver appRolesResolver = FrameworkServiceDataHolder.getInstance()
                .getHighestPriorityApplicationRolesResolver();
        if (appRolesResolver == null) {
            log.debug("No application roles resolver found.");
            // Return empty string if no application roles resolver is available.
            return StringUtils.EMPTY;
        }
        String[] appRoles;
        try {
            appRoles = appRolesResolver.getRoles(authenticatedUser, applicationId);
        } catch (ApplicationRolesException e) {
            throw new FrameworkException("Error while retrieving application roles for user: " +
                    authenticatedUser.getLoggableUserId() + " and application: " + applicationId, e);
        }
        if (appRoles != null) {
            return String.join(FrameworkUtils.getMultiAttributeSeparator(), appRoles);
        }
        return null;
    }

    private static boolean isRemoveUserDomainInRole(SequenceConfig sequenceConfig) {

        return !sequenceConfig.getApplicationConfig().getServiceProvider().getLocalAndOutBoundAuthenticationConfig().
                isUseUserstoreDomainInRoles();
    }
}
