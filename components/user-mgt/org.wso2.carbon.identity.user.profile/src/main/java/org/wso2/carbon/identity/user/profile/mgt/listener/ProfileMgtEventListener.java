/*
 * Copyright (c) 2010-2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.user.profile.mgt.listener;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.base.IdentityValidationUtil;
import org.wso2.carbon.identity.core.AbstractIdentityUserOperationEventListener;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.user.profile.mgt.util.ServiceHodler;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreClientException;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.constants.UserCoreClaimConstants;
import org.wso2.carbon.user.core.jdbc.JDBCUserStoreManager;
import org.wso2.carbon.user.core.jdbc.UniqueIDJDBCUserStoreManager;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ProfileMgtEventListener extends AbstractIdentityUserOperationEventListener {

    private static final String ALPHANUMERICS_ONLY = "ALPHANUMERICS_ONLY";
    private static final String DIGITS_ONLY = "DIGITS_ONLY";
    private static final String WHITESPACE_EXISTS = "WHITESPACE_EXISTS";
    private static final String URI_RESERVED_EXISTS = "URI_RESERVED_EXISTS";
    private static final String HTML_META_EXISTS = "HTML_META_EXISTS";
    private static final String XML_META_EXISTS = "XML_META_EXISTS";
    private static final String REGEX_META_EXISTS = "REGEX_META_EXISTS";
    private static final String URL = "URL";
    private static final Set<String> IMMUTABLE_CLAIM_URIS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            UserCoreClaimConstants.USER_ID_CLAIM_URI,
            UserCoreClaimConstants.USER_ID_CLAIM_URI,
            "http://wso2.org/claims/created"
    )));

    private static final Log log = LogFactory.getLog(ProfileMgtEventListener.class);

    @Override
    public int getExecutionOrderId() {
        return 110;
    }

    @Override
    public boolean doPreSetUserClaimValue(String userName, String claimURI, String claimValue, String profileName,
            UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }
        validateImmutableLocalClaimUpdate(userName, Collections.singletonMap(claimURI, claimValue), profileName,
                userStoreManager);
        return true;
    }

    @Override
    public boolean doPreSetUserClaimValues(String userName, Map<String, String> claims, String profileName,
            UserStoreManager userStoreManager) throws UserStoreException {
        if (!isEnable()) {
            return true;
        }

        validateImmutableLocalClaimUpdate(userName, claims, profileName, userStoreManager);
        if (log.isDebugEnabled()) {
            String userStoreDomain = UserCoreUtil.getDomainName(userStoreManager.getRealmConfiguration());
            if (StringUtils.isBlank(userStoreDomain)) {
                userStoreDomain = UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME;
            }
            String tenantDomain = IdentityTenantUtil.getTenantDomain(userStoreManager.getTenantId());
            log.debug("doPreSetUserClaimValues method executed in ProfileMgtEventListener for user: " +
                    getFullQualifiedUsername(userName, userStoreDomain, tenantDomain));
        }

        //The following black listed patterns contain possible invalid inputs for profile which could be used for a
        // stored XSS attack.
        String[] whiteListPatternKeys = {ALPHANUMERICS_ONLY, DIGITS_ONLY};
        String[] blackListPatternKeys = {WHITESPACE_EXISTS, URI_RESERVED_EXISTS, HTML_META_EXISTS, XML_META_EXISTS,
                                         REGEX_META_EXISTS, URL};

        if (!IdentityValidationUtil.isValid(profileName, whiteListPatternKeys, blackListPatternKeys)) {
            throw new UserStoreException("profile name contains invalid characters!");
        }
        return true;
    }

    @Override
    public boolean doPreDeleteUserClaimValue(String userName, String claimURI, String profileName,
            UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }
        validateImmutableLocalClaimDelete(Collections.singleton(claimURI), profileName);
        return true;
    }

    @Override
    public boolean doPreDeleteUserClaimValues(String userName, String[] claims, String profileName,
            UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable() || claims == null) {
            return true;
        }
        validateImmutableLocalClaimDelete(new HashSet<>(Arrays.asList(claims)), profileName);
        return true;
    }

    /**
     * Delete federated user account associations a user has upon deleting the local user account.
     *
     * @param userName
     * @param userStoreManager
     * @return
     * @throws UserStoreException
     */
    @Override
    public boolean doPreDeleteUser(String userName,
            UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String userStoreDomain = UserCoreUtil.getDomainName(userStoreManager.getRealmConfiguration());
        if (StringUtils.isBlank(userStoreDomain)) {
            userStoreDomain = UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME;
        }
        int tenantId = userStoreManager.getTenantId();

        if (log.isDebugEnabled()) {
            log.debug("doPreDeleteUser method executed in ProfileMgtEventListener for user:" +
                    getFullQualifiedUsername(userName, userStoreDomain, IdentityTenantUtil.getTenantDomain(tenantId)));
        }

        deleteFederatedIdpAccountAssociations(userName, userStoreDomain, tenantId);
        return true;
    }

    private void validateImmutableLocalClaimUpdate(String userName, Map<String, String> claims, String profileName,
                                                   UserStoreManager userStoreManager)
            throws UserStoreException {

        if (!isRestrictImmutableLocalClaimsUpdateEnabled() || claims == null || claims.isEmpty() ||
                !UserCoreConstants.DEFAULT_PROFILE.equals(profileName)) {
            return;
        }

        Set<String> immutableClaimsInRequest = IMMUTABLE_CLAIM_URIS.stream().filter(claims::containsKey)
                .collect(Collectors.toSet());
        if (immutableClaimsInRequest.isEmpty()) {
            return;
        }

        Map<String, String> existingClaimValues = userStoreManager.getUserClaimValues(userName,
                immutableClaimsInRequest.toArray(new String[0]), profileName);
        for (String claimURI : immutableClaimsInRequest) {
            String existingValue = existingClaimValues != null ? existingClaimValues.get(claimURI) : null;
            if (Objects.equals(existingValue, claims.get(claimURI))) {
                continue;
            }
            // For JDBC user stores, allow the initial population of the userid claim when no value is stored yet.
            if (UserCoreClaimConstants.USER_ID_CLAIM_URI.equals(claimURI) && StringUtils.isEmpty(existingValue)
                    && isLegacyJdbcUserStoreManager(userStoreManager)) {
                continue;
            }

            throw new UserStoreClientException("Invalid operation. " + claimURI +
                    " is an immutable claim and cannot be modified");
        }
    }

    private void validateImmutableLocalClaimDelete(Set<String> claimURIs, String profileName)
            throws UserStoreException {

        if (!isRestrictImmutableLocalClaimsUpdateEnabled() || CollectionUtils.isEmpty(claimURIs) ||
            !UserCoreConstants.DEFAULT_PROFILE.equals(profileName)) {
            return;
        }

        for (String immutableClaim : IMMUTABLE_CLAIM_URIS) {
            if (claimURIs.contains(immutableClaim)) {
                throw new UserStoreClientException("Invalid operation. " + immutableClaim +
                        " is an immutable claim and cannot be modified");
            }
        }
    }

    private boolean isLegacyJdbcUserStoreManager(UserStoreManager userStoreManager) {

        return userStoreManager instanceof JDBCUserStoreManager &&
                !(userStoreManager instanceof UniqueIDJDBCUserStoreManager);
    }

    private boolean isRestrictImmutableLocalClaimsUpdateEnabled() {

        String value = IdentityUtil.getProperty("UserClaimUpdate.RestrictImmutableLocalClaimUpdate");
        if (value == null) {
            if (log.isDebugEnabled()) {
                log.debug("UserClaimUpdate.RestrictImmutableLocalClaimUpdate property is not set. " +
                        "Defaulting to true.");
            }
            return true;
        }
        if (log.isDebugEnabled()) {
            log.debug("UserClaimUpdate.RestrictImmutableLocalClaimUpdate property is set to " + value);
        }
        return Boolean.parseBoolean(value);
    }

    /**
     * Delete federated idp account associations from IDN_ASSOCIATED_ID table
     *
     * @param tenantAwareUsername
     * @param userStoreDomain
     * @param tenantId
     * @throws UserStoreException
     */
    private void deleteFederatedIdpAccountAssociations(String tenantAwareUsername,
            String userStoreDomain,
            int tenantId) throws UserStoreException {

        // Run this code only if IDN_ASSOCIATED_ID table presents. We are doing this because of this feature can be used
        // by products which does not have the IDN tables.
        if (!ServiceHodler.isIDNTableExist()) {
            return;
        }

        String sql = "DELETE FROM IDN_ASSOCIATED_ID WHERE USER_NAME=? AND DOMAIN_NAME=? AND TENANT_ID=?";

        String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
        // get tenant domain and user store domain appended username for logging
        String fullyQualifiedUsername = getFullQualifiedUsername(tenantAwareUsername, userStoreDomain, tenantDomain);
        if (log.isDebugEnabled()) {
            log.debug("Deleting federated IDP user account associations of user:" + fullyQualifiedUsername);
        }

        try (Connection connection = IdentityDatabaseUtil.getDBConnection()) {
            try (PreparedStatement prepStmt = connection.prepareStatement(sql)) {
                prepStmt.setString(1, tenantAwareUsername);
                prepStmt.setString(2, userStoreDomain);
                prepStmt.setInt(3, tenantId);
                prepStmt.executeUpdate();
                IdentityDatabaseUtil.commitTransaction(connection);
            } catch (SQLException e1) {
                IdentityDatabaseUtil.rollbackTransaction(connection);
                throw new UserStoreException(String.format("Error when trying to delete the federated IDP user "
                        + "account associations of user:%s", fullyQualifiedUsername), e1);
            }
        } catch (SQLException e) {
            String msg = "Error when trying to delete the federated IDP user account associations of user:%s";
            throw new UserStoreException(String.format(msg, fullyQualifiedUsername), e);
        }
    }

    private String getFullQualifiedUsername(String tenantAwareUsername,
            String userStoreDomain,
            String tenantDomain) {

        String fullyQualifiedUsername = UserCoreUtil.addDomainToName(tenantAwareUsername, userStoreDomain);
        fullyQualifiedUsername = UserCoreUtil.addTenantDomainToEntry(fullyQualifiedUsername, tenantDomain);
        return fullyQualifiedUsername;
    }
}

