/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.mgt.util;

import org.apache.axiom.om.util.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.mgt.IdentityMgtConfig;
import org.wso2.carbon.identity.mgt.constants.IdentityMgtConstants;
import org.wso2.carbon.identity.mgt.dto.UserDTO;
import org.wso2.carbon.identity.mgt.internal.IdentityMgtServiceComponent;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class Utils {

    private static final Log log = LogFactory.getLog(Utils.class);

    private Utils() {
    }

    public static UserDTO processUserId(String userId) throws IdentityException {


        if (userId == null || userId.trim().length() < 1) {
            throw IdentityException.error("Can not proceed with out a user id");
        }

        UserDTO userDTO = new UserDTO(userId);
        if (!IdentityMgtConfig.getInstance().isSaasEnabled()) {
            validateTenant(userDTO);
        }
        userDTO.setTenantId(getTenantId(userDTO.getTenantDomain()));
        return userDTO;

    }

    public static void validateTenant(UserDTO user) throws IdentityException {
        if (user.getTenantDomain() != null && !user.getTenantDomain().isEmpty()) {
            if (!user.getTenantDomain().equals(
                    PrivilegedCarbonContext.getThreadLocalCarbonContext()
                            .getTenantDomain())) {
                throw IdentityException.error(
                        "Failed access to unauthorized tenant domain");
            }

            user.setTenantId(getTenantId(user.getTenantDomain()));
        }
    }

    /**
     * gets no of verified user challenges
     *
     * @param userDTO bean class that contains user and tenant Information
     * @return no of verified challenges
     * @throws IdentityException if fails
     */
    public static int getVerifiedChallenges(UserDTO userDTO) throws IdentityException {

        int noOfChallenges = 0;

        try {
            UserRegistry registry = IdentityMgtServiceComponent.getRegistryService().
                    getConfigSystemRegistry(MultitenantConstants.SUPER_TENANT_ID);
            String identityKeyMgtPath = IdentityMgtConstants.IDENTITY_MANAGEMENT_CHALLENGES +
                    RegistryConstants.PATH_SEPARATOR + userDTO.getUserId() +
                    RegistryConstants.PATH_SEPARATOR + userDTO.getUserId();

            Resource resource;
            if (registry.resourceExists(identityKeyMgtPath)) {
                resource = registry.get(identityKeyMgtPath);
                String property = resource.getProperty(IdentityMgtConstants.VERIFIED_CHALLENGES);
                if (property != null) {
                    return Integer.parseInt(property);
                }
            }
        } catch (RegistryException e) {
            log.error("Error while processing userKey", e);
        }

        return noOfChallenges;
    }

    /**
     * gets the tenant id from the tenant domain
     *
     * @param domain - tenant domain name
     * @return tenantId
     * @throws IdentityException if fails or tenant doesn't exist
     */
    public static int getTenantId(String domain) throws IdentityException {

        int tenantId;
        TenantManager tenantManager = IdentityMgtServiceComponent.getRealmService().getTenantManager();

        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(domain)) {
            tenantId = MultitenantConstants.SUPER_TENANT_ID;
            if (log.isDebugEnabled()) {
                String msg = "Domain is not defined implicitly. So it is Super Tenant domain.";
                log.debug(msg);
            }
        } else {
            try {
                tenantId = tenantManager.getTenantId(domain);
                if (tenantId < 1 && tenantId != MultitenantConstants.SUPER_TENANT_ID) {
                    String msg = "This action can not be performed by the users in non-existing domains.";
                    log.error(msg);
                    throw IdentityException.error(msg);
                }
            } catch (org.wso2.carbon.user.api.UserStoreException e) {
                String msg = "Error in retrieving tenant id of tenant domain: " + domain + ".";
                log.error(msg, e);
                throw IdentityException.error(msg, e);
            }
        }
        return tenantId;
    }

    /**
     * Get the claims from the user store manager
     *
     * @param userName user name
     * @param tenantId tenantId
     * @param claim    claim name
     * @return claim value
     * @throws IdentityException if fails
     */
    public static String getClaimFromUserStoreManager(String userName, int tenantId, String claim)
            throws IdentityException {

        org.wso2.carbon.user.core.UserStoreManager userStoreManager = null;
        RealmService realmService = IdentityMgtServiceComponent.getRealmService();
        String claimValue = "";

        try {
            if (realmService.getTenantUserRealm(tenantId) != null) {
                userStoreManager = (org.wso2.carbon.user.core.UserStoreManager) realmService.getTenantUserRealm(tenantId).
                        getUserStoreManager();
            }

        } catch (Exception e) {
            String msg = "Error retrieving the user store manager for tenant id : " + tenantId;
            log.error(msg, e);
            throw IdentityException.error(msg, e);
        }
        try {
            if (userStoreManager != null) {
                Map<String, String> claimsMap = userStoreManager
                        .getUserClaimValues(userName, new String[]{claim}, UserCoreConstants.DEFAULT_PROFILE);
                if (claimsMap != null && !claimsMap.isEmpty()) {
                    claimValue = claimsMap.get(claim);
                }
            }
            return claimValue;
        } catch (Exception e) {
            String msg = "Unable to retrieve the claim for user : " + userName;
            log.error(msg, e);
            throw IdentityException.error(msg, e);
        }
    }

    /**
     * get email address from user store
     *
     * @param userName user name
     * @param tenantId tenant id
     * @return email address
     */
    public static String getEmailAddressForUser(String userName, int tenantId) {

        String email = null;

        try {
            if (log.isDebugEnabled()) {
                log.debug("Retrieving email address from user profile.");
            }

            Tenant tenant = IdentityMgtServiceComponent.getRealmService().
                    getTenantManager().getTenant(tenantId);
            if (tenant != null && tenant.getAdminName().equals(userName)) {
                email = tenant.getEmail();
            }

            if (email == null || email.trim().length() < 1) {
                email = getClaimFromUserStoreManager(userName, tenantId,
                        UserCoreConstants.ClaimTypeURIs.EMAIL_ADDRESS);
            }

            if ((email == null || email.trim().length() < 1) && MultitenantUtils.isEmailUserName()) {
                email = UserCoreUtil.removeDomainFromName(userName);
            }
        } catch (Exception e) {
            String msg = "Unable to retrieve an email address associated with the given user : " + userName;
            log.warn(msg, e);   // It is common to have users with no email address defined.
        }

        return email;
    }

    /**
     * Update Password with the user input
     *
     * @return true - if password was successfully reset
     * @throws IdentityException
     */
    public static boolean updatePassword(String userId, int tenantId, String password) throws IdentityException {

        String tenantDomain = null;

        if (userId == null || userId.trim().length() < 1 ||
                password == null || password.trim().length() < 1) {
            String msg = "Unable to find the required information for updating password";
            log.error(msg);
            throw IdentityException.error(msg);
        }

        try {
            UserStoreManager userStoreManager = IdentityMgtServiceComponent.
                    getRealmService().getTenantUserRealm(tenantId).getUserStoreManager();

            userStoreManager.updateCredentialByAdmin(userId, password);
            if (log.isDebugEnabled()) {
                String msg = "Password is updated for  user: " + userId;
                log.debug(msg);
            }
            return true;
        } catch (UserStoreException e) {
            String msg = "Error in changing the password, user name: " + userId + "  domain: " +
                    tenantDomain + ".";
            log.error(msg, e);
            throw IdentityException.error(msg, e);
        }
    }

    /**
     * @param value
     * @return
     * @throws UserStoreException
     */
    public static String doHash(String value) throws UserStoreException {
        try {
            String digsestFunction = "SHA-256";
            MessageDigest dgst = MessageDigest.getInstance(digsestFunction);
            byte[] byteValue = dgst.digest(value.getBytes());
            return Base64.encode(byteValue);
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage(), e);
            throw new UserStoreException(e.getMessage(), e);
        }
    }

    /**
     * Set claim to user store manager
     *
     * @param userName user name
     * @param tenantId tenant id
     * @param claim    claim uri
     * @param value    claim value
     * @throws IdentityException if fails
     */
    public static void setClaimInUserStoreManager(String userName, int tenantId, String claim,
                                                  String value) throws IdentityException {
        org.wso2.carbon.user.core.UserStoreManager userStoreManager = null;
        RealmService realmService = IdentityMgtServiceComponent.getRealmService();
        try {
            if (realmService.getTenantUserRealm(tenantId) != null) {
                userStoreManager = (org.wso2.carbon.user.core.UserStoreManager) realmService.getTenantUserRealm(tenantId).
                        getUserStoreManager();
            }

        } catch (Exception e) {
            String msg = "Error retrieving the user store manager for the tenant";
            log.error(msg, e);
            throw IdentityException.error(msg, e);
        }

        try {
            if (userStoreManager != null) {
                String oldValue = userStoreManager.getUserClaimValue(userName, claim, null);
                if (oldValue == null || !oldValue.equals(value)) {
                    Map<String,String> claimMap = new HashMap<String,String>();
                    claimMap.put(claim, value);
                    userStoreManager.setUserClaimValues(userName, claimMap, UserCoreConstants.DEFAULT_PROFILE);
                }
            }
        } catch (Exception e) {
            String msg = "Unable to set the claim for user : " + userName;
            log.error(msg, e);
            throw IdentityException.error(msg, e);
        }
    }


    public static String getUserStoreDomainName(String userName) {
        int index;
        String userDomain;
        if ((index = userName.indexOf(CarbonConstants.DOMAIN_SEPARATOR)) >= 0) {
            // remove domain name if exist
            userDomain = userName.substring(0, index);
        } else {
            userDomain = UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME;
        }
        return userDomain;
    }


    public static String[] getChallengeUris() {
        //TODO
        return new String[]{IdentityMgtConstants.DEFAULT_CHALLENGE_QUESTION_URI01,
                IdentityMgtConstants.DEFAULT_CHALLENGE_QUESTION_URI02};
    }

    public static Policy getSecurityPolicy() {

        String policyString = "        <wsp:Policy wsu:Id=\"UTOverTransport\" xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\"\n" +
                "                    xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">\n" +
                "          <wsp:ExactlyOne>\n" +
                "            <wsp:All>\n" +
                "              <sp:TransportBinding xmlns:sp=\"http://schemas.xmlsoap.org/ws/2005/07/securitypolicy\">\n" +
                "                <wsp:Policy>\n" +
                "                  <sp:TransportToken>\n" +
                "                    <wsp:Policy>\n" +
                "                      <sp:HttpsToken RequireClientCertificate=\"true\"/>\n" +
                "                    </wsp:Policy>\n" +
                "                  </sp:TransportToken>\n" +
                "                  <sp:AlgorithmSuite>\n" +
                "                    <wsp:Policy>\n" +
                "                      <sp:Basic256/>\n" +
                "                    </wsp:Policy>\n" +
                "                  </sp:AlgorithmSuite>\n" +
                "                  <sp:Layout>\n" +
                "                    <wsp:Policy>\n" +
                "                      <sp:Lax/>\n" +
                "                    </wsp:Policy>\n" +
                "                  </sp:Layout>\n" +
                "                  <sp:IncludeTimestamp/>\n" +
                "                </wsp:Policy>\n" +
                "              </sp:TransportBinding>\n" +
                "            </wsp:All>\n" +
                "          </wsp:ExactlyOne>\n" +
                "        </wsp:Policy>";

        return PolicyEngine.getPolicy(new ByteArrayInputStream(policyString.getBytes()));

    }
}
