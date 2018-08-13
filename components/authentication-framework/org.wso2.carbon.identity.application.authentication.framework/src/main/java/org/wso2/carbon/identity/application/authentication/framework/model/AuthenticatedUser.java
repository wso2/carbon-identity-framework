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

package org.wso2.carbon.identity.application.authentication.framework.model;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.User;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * AuthenticatedUser is the class that represents the authenticated subject.
 * This class keeps information relevant to the authenticated subject.
 * This state of information includes:
 * <ul>
 * <li>Tenant Domain
 * <li>User store Domain
 * <li>username
 * <li>user attributes
 * </ul>
 */
public class AuthenticatedUser extends User {

    private static final long serialVersionUID = -6919627053686253276L;

    private String authenticatedSubjectIdentifier;
    private String federatedIdPName;
    private boolean isFederatedUser;
    private Map<ClaimMapping, String> userAttributes = new HashMap<>();

    /**
     * Instantiates an AuthenticatedUser
     */
    public AuthenticatedUser() {

    }

    /**
     * Copy the given AuthenticatedUser instance
     *
     * @param authenticatedUser authenticated user instance to copy
     */
    public AuthenticatedUser(AuthenticatedUser authenticatedUser) {

        this.authenticatedSubjectIdentifier = authenticatedUser.getAuthenticatedSubjectIdentifier();
        this.tenantDomain = authenticatedUser.getTenantDomain();
        this.userName = authenticatedUser.getUserName();
        this.userStoreDomain = authenticatedUser.getUserStoreDomain();
        if (authenticatedUser.getUserAttributes() != null) {
            this.userAttributes.putAll(authenticatedUser.getUserAttributes());
        }
        this.isFederatedUser = authenticatedUser.isFederatedUser();
        if (!isFederatedUser && StringUtils.isNotEmpty(userStoreDomain) && StringUtils.isNotEmpty(tenantDomain)) {
            updateCaseSensitivity();
        }
    }

    /**
     * Returns an AuthenticatedUser instance populated from the given subject identifier string.
     * It is assumed that this user is authenticated from a local authenticator thus extract user
     * store domain and tenant domain from the given string.
     *
     * @param authenticatedSubjectIdentifier a string in
     *                                       <userstore_domain>/<username>@<tenant_domain> format
     * @return populated AuthenticatedUser instance
     */
    public static AuthenticatedUser createLocalAuthenticatedUserFromSubjectIdentifier(
            String authenticatedSubjectIdentifier) {

        if (authenticatedSubjectIdentifier == null || authenticatedSubjectIdentifier.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Failed to create Local Authenticated User from the given subject identifier." +
                    " Invalid argument. authenticatedSubjectIdentifier : " + authenticatedSubjectIdentifier);
        }

        AuthenticatedUser authenticatedUser = new AuthenticatedUser();

        if (StringUtils.isNotEmpty(UserCoreUtil.getDomainFromThreadLocal())) {
            if (authenticatedSubjectIdentifier.indexOf(CarbonConstants.DOMAIN_SEPARATOR) > 0) {
                String[] subjectIdentifierSplits =
                        authenticatedSubjectIdentifier.split(CarbonConstants.DOMAIN_SEPARATOR, 2);
                if (UserCoreUtil.getDomainFromThreadLocal().equalsIgnoreCase(subjectIdentifierSplits[0])) {
                    authenticatedUser.setUserStoreDomain(subjectIdentifierSplits[0]);
                    authenticatedUser.setUserName(MultitenantUtils.getTenantAwareUsername(subjectIdentifierSplits[1]));
                }
            } else {
                authenticatedUser.setUserStoreDomain(UserCoreUtil.getDomainFromThreadLocal());
                authenticatedUser.setUserName(MultitenantUtils.getTenantAwareUsername(authenticatedSubjectIdentifier));
            }
        } else {
            authenticatedUser.setUserStoreDomain(IdentityUtil.getPrimaryDomainName());
            authenticatedUser.setUserName(MultitenantUtils.getTenantAwareUsername(authenticatedSubjectIdentifier));
        }

        authenticatedUser.setTenantDomain(MultitenantUtils.getTenantDomain(authenticatedSubjectIdentifier));
        authenticatedUser.setAuthenticatedSubjectIdentifier(authenticatedSubjectIdentifier);

        return authenticatedUser;
    }

    /**
     * Returns an AuthenticatedUser instance populated from the given subject identifier string.
     * It is assumed that this user is authenticated from a federated authenticator.
     *
     * @param authenticatedSubjectIdentifier a string that represents authenticated subject
     *                                       identifier
     * @return populated AuthenticatedUser instance
     */
    public static AuthenticatedUser createFederateAuthenticatedUserFromSubjectIdentifier(
            String authenticatedSubjectIdentifier) {

        if (authenticatedSubjectIdentifier == null || authenticatedSubjectIdentifier.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Failed to create Federated Authenticated User from the given subject " +
                    "identifier. Invalid argument. authenticatedSubjectIdentifier : " + authenticatedSubjectIdentifier);
        }

        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setAuthenticatedSubjectIdentifier(authenticatedSubjectIdentifier);
        authenticatedUser.setFederatedUser(true);

        return authenticatedUser;
    }

    /**
     * Returns the authenticated subject identifier.
     * For a locally authenticated user, subject
     * identifier is as below.
     * <userstore_domain>/<username>@<tenant_domain>.
     *
     * @return the authenticated subject identifier
     */
    public String getAuthenticatedSubjectIdentifier() {
        return authenticatedSubjectIdentifier;
    }

    /**
     * Sets the authenticated subject identifier.
     *
     * @param authenticatedSubjectIdentifier the authenticated subject identifier
     */
    public void setAuthenticatedSubjectIdentifier(String authenticatedSubjectIdentifier) {
        this.authenticatedSubjectIdentifier = authenticatedSubjectIdentifier;
    }

    /**
     * Sets authenticated subject identifier according to the useTenantDomainInLocalSubjectIdentifier and
     * useUserstoreDomainInLocalSubjectIdentifier properties.
     *
     * @param authenticatedSubjectIdentifier authenticated subject identifier
     * @param serviceProvider service provider
     */

    public void setAuthenticatedSubjectIdentifier(String authenticatedSubjectIdentifier, ServiceProvider serviceProvider) {

        if (!isFederatedUser() && serviceProvider != null) {
            boolean useUserstoreDomainInLocalSubjectIdentifier = serviceProvider.getLocalAndOutBoundAuthenticationConfig()
                    .isUseUserstoreDomainInLocalSubjectIdentifier();
            boolean useTenantDomainInLocalSubjectIdentifier = serviceProvider.getLocalAndOutBoundAuthenticationConfig()
                    .isUseTenantDomainInLocalSubjectIdentifier();
            if (useUserstoreDomainInLocalSubjectIdentifier && StringUtils.isNotEmpty(userStoreDomain)) {
                authenticatedSubjectIdentifier = IdentityUtil.addDomainToName(userName, userStoreDomain);
            }
            if (useTenantDomainInLocalSubjectIdentifier && StringUtils.isNotEmpty(tenantDomain)) {
                authenticatedSubjectIdentifier = UserCoreUtil.addTenantDomainToEntry(authenticatedSubjectIdentifier,
                        tenantDomain);
            }
        }
        this.authenticatedSubjectIdentifier = authenticatedSubjectIdentifier;
    }

    /**
     * Returns the user attributes of the authenticated user as a map.
     * The map holds the respective ClaimMapping object as the key and the attribute as the value.
     *
     * @return a map of ClaimMapping to attribute value
     */
    public Map<ClaimMapping, String> getUserAttributes() {
        return userAttributes;
    }

    /**
     * Sets the user attributes of the authenticated user.
     *
     * @param userAttributes a map of ClaimMapping to attribute value
     */
    public void setUserAttributes(Map<ClaimMapping, String> userAttributes) {
        this.userAttributes = userAttributes;
    }

    public String getUsernameAsSubjectIdentifier(boolean useUserstoreDomainInLocalSubjectIdentifier, boolean
            useTenantDomainInLocalSubjectIdentifier) {
        String userName = this.userName;
        if (useUserstoreDomainInLocalSubjectIdentifier && userStoreDomain != null) {
            userName = UserCoreUtil.addDomainToName(userName, userStoreDomain);
        }
        if (useTenantDomainInLocalSubjectIdentifier && tenantDomain != null) {
            userName = UserCoreUtil.addTenantDomainToEntry(userName, tenantDomain);
        }
        return userName;
    }

    /**
     * Returns whether this user federated user or not.
     *
     * @return isFederatedUser
     */
    public boolean isFederatedUser() {
        return isFederatedUser;
    }

    /**
     * Sets the flag to indicate whether this is a federated user or not.
     *
     * @param isFederatedUser
     */
    public void setFederatedUser(boolean isFederatedUser) {
        this.isFederatedUser = isFederatedUser;
    }

    /**
     *
     * @return
     */
    public String getFederatedIdPName() {
        return federatedIdPName;
    }

    /**
     * Sets the flag to indicate whether this is a federated user or not.
     * @param federatedIdPName
     */
    public void setFederatedIdPName(String federatedIdPName) {
        this.federatedIdPName = federatedIdPName;
    }

    @Override
    public boolean equals(Object o) {

        if(!isFederatedUser) {
            return super.equals(o);
        } else {
            if (this == o) {
                return true;
            }
            if (!(o instanceof AuthenticatedUser)) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }

            AuthenticatedUser that = (AuthenticatedUser) o;

            if (!authenticatedSubjectIdentifier.equals(that.authenticatedSubjectIdentifier)) {
                return false;
            }
            // checking for null because we can't be 100% sure that federatedIdPName is set to a non-null value in all
            // places that use AuthenticatedUser
            return federatedIdPName != null ? federatedIdPName.equals(that.federatedIdPName) : that.federatedIdPName == null;
        }
    }

    @Override
    public int hashCode() {

        if(!isFederatedUser) {
            return super.hashCode();
        } else {
            int result = authenticatedSubjectIdentifier.hashCode();
            // checking for null because we can't be 100% sure that federatedIdPName is set to a non-null value in all
            // places that use AuthenticatedUser
            result = 31 * result + (federatedIdPName != null ? federatedIdPName.hashCode() : 0);
            return result;
        }
    }

    @Override
    public String toFullQualifiedUsername() {
        if (isFederatedUser && StringUtils.isBlank(userName)) {
            //username,userstore domain may be null for federated users
            return authenticatedSubjectIdentifier;
        }
        return super.toFullQualifiedUsername();
    }

    @Override
    public String toString() {

        if (isFederatedUser && StringUtils.isBlank(userName)) {
            //username,userstore domain may be null for federated users
            return authenticatedSubjectIdentifier;
        }
        return super.toString();
    }
}
