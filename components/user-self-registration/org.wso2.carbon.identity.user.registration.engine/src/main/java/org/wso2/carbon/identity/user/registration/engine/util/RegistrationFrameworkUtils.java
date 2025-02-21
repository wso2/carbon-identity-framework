/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.user.registration.engine.util;

import com.nimbusds.jwt.JWTClaimsSet;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ApplicationConstants;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataHandler;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.user.registration.mgt.exception.RegistrationClientException;
import org.wso2.carbon.identity.user.registration.engine.cache.RegistrationContextCache;
import org.wso2.carbon.identity.user.registration.engine.cache.RegistrationContextCacheEntry;
import org.wso2.carbon.identity.user.registration.engine.cache.RegistrationContextCacheKey;
import org.wso2.carbon.identity.user.registration.mgt.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.registration.engine.model.RegistrationContext;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.wso2.carbon.identity.user.registration.engine.util.Constants.ErrorMessages.ERROR_FLOW_ID_NOT_FOUND;
import static org.wso2.carbon.identity.user.registration.engine.util.Constants.ErrorMessages.ERROR_INVALID_FLOW_ID;

public class RegistrationFrameworkUtils {

    private static final Log LOG = LogFactory.getLog(RegistrationFrameworkUtils.class);

    public static void addRegContextToCache(RegistrationContext context) {

        RegistrationContextCacheEntry cacheEntry = new RegistrationContextCacheEntry(context);
        RegistrationContextCacheKey cacheKey = new RegistrationContextCacheKey(context.getContextIdentifier());
        RegistrationContextCache.getInstance().addToCache(cacheKey, cacheEntry);
    }

    public static RegistrationContext retrieveRegContextFromCache(String contextId) throws RegistrationFrameworkException {

        if (contextId == null) {
            throw new RegistrationClientException(ERROR_FLOW_ID_NOT_FOUND.getCode(),
                                                  ERROR_FLOW_ID_NOT_FOUND.getMessage(),
                                                  ERROR_FLOW_ID_NOT_FOUND.getDescription());
        }
        RegistrationContextCacheEntry entry =
                RegistrationContextCache.getInstance().getValueFromCache(new RegistrationContextCacheKey(contextId));
        if (entry == null) {
            throw new RegistrationClientException(ERROR_INVALID_FLOW_ID.getCode(),
                                                  ERROR_INVALID_FLOW_ID.getMessage(),
                                                  String.format(ERROR_INVALID_FLOW_ID.getDescription(), contextId));
        }
        return entry.getContext();
    }

    public static void removeRegContextFromCache(String contextId) {

        RegistrationContextCache.getInstance().clearCacheEntry(new RegistrationContextCacheKey(contextId));
    }

    public static ServiceProvider retrieveSpFromAppId(String appId, String tenantDomain) throws  RegistrationFrameworkException{

        ApplicationManagementService appInfo = ApplicationManagementService.getInstance();

        ServiceProvider sp;
        try {
            sp = appInfo.getApplicationByResourceId(appId, tenantDomain);
        } catch (IdentityApplicationManagementException e) {
            throw new RegistrationFrameworkException("Error occurred while retrieving service provider", e);
        }
        if (sp == null) {
            throw new RegistrationFrameworkException("Service provider not found for app id: " + appId);
        }

        return sp;
    }

    public static Map<String, String> convertClaimsFromIdpToLocalClaims(String tenantDomain,
                                                                        Map<String, String> remoteClaims,
                                                                        ClaimMapping[] idPClaimMappings,
                                                                        String idPStandardDialect)
    throws RegistrationFrameworkException {

        Map<String, String> localToIdPClaimMap;

        if (idPStandardDialect == null) {
            idPStandardDialect = ApplicationConstants.LOCAL_IDP_DEFAULT_CLAIM_DIALECT;
        }

        try {
            localToIdPClaimMap = getClaimMappings(idPStandardDialect, remoteClaims.keySet(), tenantDomain, true);
        } catch (Exception e) {
            throw new RegistrationFrameworkException("Error occurred while getting claim mappings for", e);
        }
        // Adding remote claims with default values also to the key set because they may not come from the federated IdP
        localToIdPClaimMap.putAll(Arrays.stream(idPClaimMappings)
                .filter(claimMapping -> StringUtils.isNotBlank(claimMapping.getDefaultValue())
                        && !localToIdPClaimMap.containsKey(claimMapping.getLocalClaim().getClaimUri()))
                .collect(Collectors.toMap(claimMapping -> claimMapping.getLocalClaim().getClaimUri(), ClaimMapping::getDefaultValue)));

         Map<String, String> mappedLocalClaimsForIdPClaims = new HashMap<>();

            for (Map.Entry<String, String> entry : localToIdPClaimMap.entrySet()) {
                String localClaimURI = entry.getKey();
                String claimValue = remoteClaims.get(localToIdPClaimMap.get(localClaimURI));
                if (StringUtils.isEmpty(claimValue)) {
                    LOG.debug("Claim " + localClaimURI + " has null value or blank hence not updating.");
                } else {
                    mappedLocalClaimsForIdPClaims.put(localClaimURI, claimValue);
                }
            }

        return mappedLocalClaimsForIdPClaims;
    }

    // Copied from DefaultClaimHandler
    private static Map<String, String> getClaimMappings(String otherDialect, Set<String> keySet,
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

    public static Optional<String> getSignedUserAssertion(String userId, RegistrationContext context) {

        JWTClaimsSet userAssertion = buildUserAssertionClaimSet(userId, context);
        try {
            return Optional.ofNullable(AutoLoginAssertionUtils.generateSignedUserAssertion(userAssertion,
                    context.getTenantDomain()));
        } catch (FrameworkException e) {
            LOG.error("Error while generating signed user assertion", e);
            return Optional.empty();
        }
    }

    private static JWTClaimsSet buildUserAssertionClaimSet(String userId, RegistrationContext context) {

        long curTimeInMillis = Calendar.getInstance().getTimeInMillis();
        String[] engagedAuthenticators = context.getAuthenticatedMethods().toArray(new String[0]);

        JWTClaimsSet.Builder jwtClaimsSetBuilder = new JWTClaimsSet.Builder();
        jwtClaimsSetBuilder.issuer("wso2");
        jwtClaimsSetBuilder.subject(context.getRegisteringUser().getUsername());
        jwtClaimsSetBuilder.issueTime(new Date(curTimeInMillis));
        jwtClaimsSetBuilder.jwtID(UUID.randomUUID().toString());
        jwtClaimsSetBuilder.notBeforeTime(new Date(curTimeInMillis));
        jwtClaimsSetBuilder.expirationTime(calculateUserAssertionExpiryTime(curTimeInMillis));
        jwtClaimsSetBuilder.claim("amr", engagedAuthenticators );
        jwtClaimsSetBuilder.claim("userId", userId);

        return jwtClaimsSetBuilder.build();
    }

    private static Date calculateUserAssertionExpiryTime(long curTimeInMillis) {

        Date expirationTime;
        // Default value of 5min
        long accessTokenLifeTimeInMillis = 5 * 60 * 1000;
        // When accessTokenLifeTimeInMillis is equal to Long.MAX_VALUE the curTimeInMillis +
        // accessTokenLifeTimeInMillis can be a negative value
        if (curTimeInMillis + accessTokenLifeTimeInMillis < curTimeInMillis) {
            expirationTime = new Date(Long.MAX_VALUE);
        } else {
            expirationTime = new Date(curTimeInMillis + accessTokenLifeTimeInMillis);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("User assertion expiry time : " + expirationTime + "ms.");
        }
        return expirationTime;
    }
}
