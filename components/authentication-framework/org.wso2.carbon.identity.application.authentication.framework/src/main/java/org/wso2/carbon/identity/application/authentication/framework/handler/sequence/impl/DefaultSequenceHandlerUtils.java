/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ApplicationConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.model.ClaimConfig;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataHandler;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.InternalRoleDomains.APPLICATION_DOMAIN;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.InternalRoleDomains.WORKFLOW_DOMAIN;


/**
 * Common utility used by Default Sequence Handlers.
 */
public class DefaultSequenceHandlerUtils {

    private static Log log = LogFactory.getLog(DefaultSequenceHandlerUtils.class);
    private static final String SEND_ONLY_SP_MAPPED_ROLES = "SPRoleManagement.ReturnOnlyMappedLocalRoles";
    private DefaultSequenceHandlerUtils() {
    }

    public static String getServiceProviderMappedUserRoles(SequenceConfig sequenceConfig,
                                                           List<String> locallyMappedUserRoles) {
        if (log.isDebugEnabled()) {
            AuthenticatedUser authenticatedUser = sequenceConfig.getAuthenticatedUser();
            String serviceProvider = sequenceConfig.getApplicationConfig().getApplicationName();
            log.debug("Getting Service Provider mapped roles of application: " + serviceProvider +
                    " of user: " + authenticatedUser);
        }

        // SP role mapped role values joined by Multi Attribute Separator.
        boolean returnOnlyMappedLocalRoles = Boolean.parseBoolean(IdentityUtil.getProperty(SEND_ONLY_SP_MAPPED_ROLES));

        String spMappedRoles = null;
        if (CollectionUtils.isNotEmpty(locallyMappedUserRoles)) {
            // Get SP Role mappings
            Map<String, String> localToSpRoleMapping = sequenceConfig.getApplicationConfig().getRoleMappings();
            List<String> spMappedRoleList = new ArrayList<>();
            List<String> domainRemovedRoleList = new ArrayList<>();
            // Check whether there are any SpRoleMappings
            if (localToSpRoleMapping != null && !localToSpRoleMapping.isEmpty()) {
                for (String locallyMappedRole : locallyMappedUserRoles) {
                    if (localToSpRoleMapping.containsKey(locallyMappedRole)) {
                        // add the SP mapped role
                        String spMappedRole = localToSpRoleMapping.get(locallyMappedRole);
                        spMappedRoleList.add(spMappedRole);
                        if (log.isDebugEnabled()) {
                            log.debug("Mapping local role: " + locallyMappedRole + " to service provider role: "
                                    + spMappedRole);
                        }
                    } else {
                        //  If ReturnOnlyMappedLocalRoles is false, add local role to the list.
                        if (!returnOnlyMappedLocalRoles) {
                            if (isRemoveUserDomainInRole(sequenceConfig)) {
                                //if 'Use user store domain in roles' is false add the list to remove domain name.
                                domainRemovedRoleList.add(locallyMappedRole);
                            } else {
                                spMappedRoleList.add(locallyMappedRole);
                            }
                        }
                    }
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("No local roles to map to Service Provider role mappings. Sending back all local roles " +
                            "as service provider mapped roles.");
                }
                // We don't have any sp role mappings
                if (isRemoveUserDomainInRole(sequenceConfig)) {
                    domainRemovedRoleList = locallyMappedUserRoles;
                } else {
                    spMappedRoleList = locallyMappedUserRoles;
                }
            }

            //if 'Use user store domain in roles' is false remove the domain from roles.
            if (isRemoveUserDomainInRole(sequenceConfig)) {
                List<String> domainRemovedRoles = removeDomainFromNamesExcludeHybrid(domainRemovedRoleList);
                if (!domainRemovedRoles.isEmpty()) {
                    spMappedRoleList.addAll(domainRemovedRoles);
                }
            }
            spMappedRoles = StringUtils.join(spMappedRoleList.toArray(), FrameworkUtils.getMultiAttributeSeparator());
        }

        if (log.isDebugEnabled()) {
            log.debug("Service Provider Mapped Roles: " + spMappedRoles);
        }
        return spMappedRoles;
    }

    /**
     * Remove domain name from roles except the hybrid roles (Internal,Application & Workflow)
     *
     * @param names list of roles assigned to a user
     * @return list of roles assigned to a user with domain name removed from roles
     */
    private static List<String> removeDomainFromNamesExcludeHybrid(List<String> names) {

        List<String> nameList = new ArrayList<String>();
        for (String name : names) {
            String userStoreDomain = IdentityUtil.extractDomainFromName(name);
            if (UserCoreConstants.INTERNAL_DOMAIN.equalsIgnoreCase(userStoreDomain) || APPLICATION_DOMAIN
                    .equalsIgnoreCase(userStoreDomain) || WORKFLOW_DOMAIN.equalsIgnoreCase(userStoreDomain)) {
                nameList.add(name);
            } else {
                nameList.add(UserCoreUtil.removeDomainFromName(name));
            }
        }
        return nameList;
    }

    // Execute only if it has allowed removing userstore domain from the sp level configurations.
    private static boolean isRemoveUserDomainInRole(SequenceConfig sequenceConfig) {

        return !sequenceConfig.getApplicationConfig().getServiceProvider().getLocalAndOutBoundAuthenticationConfig().
                isUseUserstoreDomainInRoles();
    }

    /**
     * Get the roles from service provider requested claims.
     * After the service provider claims mappings the role claim URI could
     * change from the local role claim uri. This method will find the
     * roles based on the given role claim URI or the proper role claim URI.
     *
     * @param context        AuthenticationContext.
     * @param sequenceConfig SequenceConfig.
     * @param mappedAttrs    Service Provider mapped claims.
     * @param spRoleUri      Service Provider role claim URiI.
     * @return Roles.
     * @throws FrameworkException
     */
    public static String[] getRolesFromSPMappedClaims(AuthenticationContext context, SequenceConfig sequenceConfig,
                                                      Map<String, String> mappedAttrs, String spRoleUri)
            throws FrameworkException {

        String spStandardDialect = DefaultSequenceHandlerUtils.getSPStandardDialect(context);
        String roleAttr = null;

        if (spStandardDialect != null && DefaultSequenceHandlerUtils.isLocalClaimDialect(context)) {
            spRoleUri = DefaultSequenceHandlerUtils.getStandardRoleClaimURI(spStandardDialect,
                    context.getTenantDomain());
            roleAttr = mappedAttrs.get(spRoleUri);
        } else if (spStandardDialect != null && !DefaultSequenceHandlerUtils.isLocalClaimDialect(context)) {
            String localClaim =
                    DefaultSequenceHandlerUtils.getSPMappedLocalRoleClaimURI(sequenceConfig.getApplicationConfig());
            spRoleUri = DefaultSequenceHandlerUtils.getStandardClaimURIFromLocal(spStandardDialect,
                    context.getTenantDomain(), localClaim);
            roleAttr = mappedAttrs.get(spRoleUri);
        } else if (spStandardDialect == null && DefaultSequenceHandlerUtils.isLocalClaimDialect(context)) {
            roleAttr = mappedAttrs.get(spRoleUri);
        } else if (spStandardDialect == null && !DefaultSequenceHandlerUtils.isLocalClaimDialect(context)) {
            roleAttr = mappedAttrs.get(spRoleUri);
        }

        if (StringUtils.isNotBlank(roleAttr)) {
            // Need to convert multiAttributeSeparator value into a regex literal before calling
            // split function. Otherwise split can produce misleading results in case
            // multiAttributeSeparator contains regex special meaning characters like .*
            return roleAttr.split(Pattern.quote(FrameworkUtils.getMultiAttributeSeparator()));
        }

        return null;
    }

    /**
     * Get the standard claim dialect of the service provider in the
     * authentication context.
     *
     * @param context AuthenticationContext.
     * @return The claim dialect of the service provider.
     */
    private static String getSPStandardDialect(AuthenticationContext context) {

        ApplicationConfig appConfig = context.getSequenceConfig().getApplicationConfig();
        String spStandardDialect;
        if (context.getProperties().containsKey(FrameworkConstants.SP_STANDARD_DIALECT)) {
            spStandardDialect = (String) context.getProperty(FrameworkConstants.SP_STANDARD_DIALECT);
        } else {
            spStandardDialect = FrameworkUtils.getStandardDialect(context.getRequestType(), appConfig);
        }
        return spStandardDialect;
    }

    /**
     * Checks if the service provider is using the local claim dialect.
     * Extracts the service provider details from the authentication context.
     *
     * @param context AuthenticationContext.
     * @return True if the used dialect is the local dialect.
     */
    private static boolean isLocalClaimDialect(AuthenticationContext context) {

        ApplicationConfig appConfig = context.getSequenceConfig().getApplicationConfig();
        ClaimConfig claimConfig = appConfig.getServiceProvider().getClaimConfig();
        return claimConfig.isLocalClaimDialect();
    }

    /**
     * Get the standard role claim URI used for the given dialect.
     *
     * @param standardDialect Dialect URI.
     * @param tenantDomain    Tenant domain.
     * @return Matching role claim uri of the given dialect.
     * @throws FrameworkException
     */
    private static String getStandardRoleClaimURI(String standardDialect, String tenantDomain)
            throws FrameworkException {

        String roleClaim = getStandardClaimURIFromLocal(standardDialect, tenantDomain, FrameworkConstants
                .LOCAL_ROLE_CLAIM_URI);
        if (StringUtils.isBlank(roleClaim)) {
            return FrameworkConstants.LOCAL_ROLE_CLAIM_URI;
        }
        return roleClaim;
    }

    /**
     * Get the standard claim URI used for the given dialect.
     *
     * @param standardDialect Dialect URI.
     * @param tenantDomain    Tenant domain.
     * @param claimURI        Local claim URI.
     * @return Matching claim uri of the given dialect.
     * @throws FrameworkException
     */
    private static String getStandardClaimURIFromLocal(String standardDialect, String tenantDomain, String claimURI)
            throws FrameworkException {

        try {
            Map<String, String> claimMapping = ClaimMetadataHandler.getInstance()
                    .getMappingsMapFromOtherDialectToCarbon(standardDialect, null, tenantDomain, true);
            if (claimMapping.containsKey(claimURI)) {
                return claimMapping.get(claimURI);
            }
        } catch (ClaimMetadataException e) {
            throw new FrameworkException("Error while loading mappings.", e);
        }
        return null;
    }

    /**
     * Used to get the service provider mapped local role claim URI.
     *
     * @param appConfig ApplicationConfig.
     * @return Service Provider mapped role claim URI.
     */
    private static String getSPMappedLocalRoleClaimURI(ApplicationConfig appConfig) {

        String spRoleClaimUri = appConfig.getRoleClaim();
        if (StringUtils.isNotBlank(spRoleClaimUri)) {

            Map<String, String> spToLocalClaimMapping = appConfig.getClaimMappings();
            if (MapUtils.isNotEmpty(spToLocalClaimMapping)) {

                for (Map.Entry<String, String> entry : spToLocalClaimMapping.entrySet()) {
                    if (spRoleClaimUri.equals(entry.getKey())) {
                        return entry.getValue();
                    }
                }
            }
        }
        return FrameworkConstants.LOCAL_ROLE_CLAIM_URI;
    }

    /**
     * Get the role claim URI of the service provider form application config.
     *
     * @param appConfig ApplicationConfig.
     * @return Role claim URI of the service provider.
     */
    public static String getSpRoleClaimUri(ApplicationConfig appConfig) {

        // Get external identity provider role claim uri.
        String spRoleClaimUri = appConfig.getRoleClaim();

        if (StringUtils.isEmpty(spRoleClaimUri)) {
            // No role claim uri defined
            // we can still try to find it out - lets have a look at the claim
            // mapping.
            Map<String, String> spToLocalClaimMapping = appConfig.getClaimMappings();

            if (MapUtils.isNotEmpty(spToLocalClaimMapping)) {
                for (Map.Entry<String, String> entry : spToLocalClaimMapping.entrySet()) {
                    if (FrameworkConstants.LOCAL_ROLE_CLAIM_URI.equals(entry.getValue())) {
                        spRoleClaimUri = entry.getKey();
                        break;
                    }
                }
            }
        }

        if (StringUtils.isEmpty(spRoleClaimUri)) {
            spRoleClaimUri = FrameworkConstants.LOCAL_ROLE_CLAIM_URI;
            if (log.isDebugEnabled()) {
                String serviceProvider = appConfig.getApplicationName();
                log.debug("Service Provider Role Claim URI not configured for SP: " + serviceProvider +
                        ". Defaulting to " + spRoleClaimUri);
            }
        }

        return spRoleClaimUri;
    }
}
