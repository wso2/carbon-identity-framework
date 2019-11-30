/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.carbon.identity.user.profile.mgt.listener;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.base.IdentityValidationUtil;
import org.wso2.carbon.identity.core.AbstractIdentityUserOperationEventListener;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.user.profile.mgt.util.ServiceHodler;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

public class ProfileMgtEventListener extends AbstractIdentityUserOperationEventListener {

    private static final String ALPHANUMERICS_ONLY = "ALPHANUMERICS_ONLY";
    private static final String DIGITS_ONLY = "DIGITS_ONLY";
    private static final String WHITESPACE_EXISTS = "WHITESPACE_EXISTS";
    private static final String URI_RESERVED_EXISTS = "URI_RESERVED_EXISTS";
    private static final String HTML_META_EXISTS = "HTML_META_EXISTS";
    private static final String XML_META_EXISTS = "XML_META_EXISTS";
    private static final String REGEX_META_EXISTS = "REGEX_META_EXISTS";
    private static final String URL = "URL";

    private static final Log log = LogFactory.getLog(ProfileMgtEventListener.class);

    @Override
    public int getExecutionOrderId() {
        return 110;
    }

    @Override
    public boolean doPreSetUserClaimValues(String userName, Map<String, String> claims, String profileName,
            UserStoreManager userStoreManager) throws UserStoreException {
        if (!isEnable()) {
            return true;
        }

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

