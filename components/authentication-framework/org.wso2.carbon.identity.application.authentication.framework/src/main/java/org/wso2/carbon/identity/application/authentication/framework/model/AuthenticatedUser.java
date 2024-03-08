/*
 * Copyright (c) 2013-2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.authentication.framework.model;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.identity.application.authentication.framework.exception.DuplicatedAuthUserException;
import org.wso2.carbon.identity.application.authentication.framework.exception.UserIdNotFoundException;
import org.wso2.carbon.identity.application.authentication.framework.exception.UserSessionException;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.store.UserSessionStore;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.User;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

    private static final Log log = LogFactory.getLog(AuthenticatedUser.class);

    protected String userId;
    private String authenticatedSubjectIdentifier;
    private String federatedIdPName;
    private boolean isFederatedUser;
    private String accessingOrganization;
    private String userResidentOrganization;
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
        try {
            this.userId = authenticatedUser.getUserId();
        } catch (UserIdNotFoundException e) {
            // Since the authenticated user is used for unauthenticated users as well, we cannot do anything here.
            if (log.isDebugEnabled()) {
                log.debug("Null user id is found while copying the AuthenticateUser instance.");
            }
        }
        this.userName = authenticatedUser.getUserName();
        this.userStoreDomain = authenticatedUser.getUserStoreDomain();
        if (authenticatedUser.getUserAttributes() != null) {
            this.userAttributes.putAll(authenticatedUser.getUserAttributes());
        }
        this.isFederatedUser = authenticatedUser.isFederatedUser();
        this.federatedIdPName = authenticatedUser.getFederatedIdPName();
        if (!isFederatedUser && StringUtils.isNotEmpty(userStoreDomain) && StringUtils.isNotEmpty(tenantDomain)) {
            updateCaseSensitivity();
        }
        this.accessingOrganization = authenticatedUser.getAccessingOrganization();
        this.userResidentOrganization = authenticatedUser.getUserResidentOrganization();
    }

    public AuthenticatedUser(org.wso2.carbon.user.core.common.User user) {

        this.userId = user.getUserID();
        this.userName = user.getUsername();
        this.tenantDomain = user.getTenantDomain();
        this.userStoreDomain = user.getUserStoreDomain();
        this.isFederatedUser = false;
        if (user.getAttributes() != null) {
            for (Map.Entry<String, String> entry : user.getAttributes().entrySet()) {
                userAttributes.put(ClaimMapping.build(entry.getKey(), entry.getKey(), null, true), entry.getValue());
            }
        }

    }

    public AuthenticatedUser(User user) {

        this.userName = user.getUserName();
        this.tenantDomain = user.getTenantDomain();
        this.userStoreDomain = user.getUserStoreDomain();
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
        authenticatedUser.setUserId(authenticatedUser.getLocalUserIdInternal());

        return authenticatedUser;
    }

    /**
     * Internal method to get the user id of a local user with the parameters available in the authenticated user
     * object.
     */
    private String getLocalUserIdInternal() {

        String userId = null;
        if (userName != null && userStoreDomain != null && tenantDomain != null) {
            try {
                String tenantDomain = this.getTenantDomain();
                /* When the user resident organization is set in the authenticated user, use that to resolve the user's
                tenant domain. The below check should be removed once console app is registered per each tenant. */
                if (StringUtils.isNotEmpty(this.userResidentOrganization)) {
                    tenantDomain = FrameworkServiceDataHolder.getInstance().getOrganizationManager()
                            .resolveTenantDomain(this.userResidentOrganization);
                }
                int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
                userId = FrameworkUtils.resolveUserIdFromUsername(tenantId,
                        this.getUserStoreDomain(), this.getUserName());
            } catch (UserSessionException e) {
                log.error("Error while resolving the user id from username for local user.");
            } catch (OrganizationManagementException e) {
                log.error("Error while resolving the tenant domain by organization id: " +
                        this.userResidentOrganization);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("User id could not be resolved for local user: " + toFullQualifiedUsername());
            }
        }
        return userId;
    }

    /**
     * Internal method to get the user id of a federated user with the parameters available in the authenticated user
     * object.
     */
    private String getFederatedUserIdInternal() {

        String userId = null;
        if (federatedIdPName != null && tenantDomain != null && authenticatedSubjectIdentifier != null) {
            try {
                int tenantId = IdentityTenantUtil.getTenantId(this.getTenantDomain());
                int idpId = UserSessionStore.getInstance().getIdPId(this.getFederatedIdPName(), tenantId);
                String subjectIdentifier = this.getAuthenticatedSubjectIdentifier();
                /* The federated user from another organization is happening via organization SSO login flow. In that
                case the subject identifier is set in the authenticated username */
                if (StringUtils.isNotEmpty(this.userResidentOrganization)) {
                    subjectIdentifier = this.userName;
                }
                userId = UserSessionStore.getInstance().getFederatedUserId(subjectIdentifier, tenantId, idpId);
                try {
                    if (userId == null) {
                        userId = UUID.randomUUID().toString();
                        UserSessionStore.getInstance()
                                .storeUserData(userId, this.getAuthenticatedSubjectIdentifier(), tenantId, idpId);
                    }
                } catch (DuplicatedAuthUserException e) {
                    if (log.isDebugEnabled()) {
                        log.debug("User authenticated is already persisted. Username: "
                                + this.getAuthenticatedSubjectIdentifier() + " Tenant Domain :"
                                + this.getTenantDomain() + " IdP: " + this.getFederatedIdPName(), e);
                    }
                    // Since duplicate entry was found, let's try to get the ID again.
                    userId = UserSessionStore.getInstance()
                            .getFederatedUserId(this.getAuthenticatedSubjectIdentifier(), tenantId, idpId);
                }
            } catch (UserSessionException e) {
                log.error("Error while resolving the user id from username for federated user.");
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("User id could not be resolved for federated user: " + toFullQualifiedUsername());
            }
        }
        return userId;
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
            throw new IllegalArgumentException("Failed to create Federated Authenticated User from the given subject "
                    + "identifier. Null or empty value provided for authenticatedSubjectIdentifier");
        }

        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setAuthenticatedSubjectIdentifier(authenticatedSubjectIdentifier);
        authenticatedUser.setFederatedUser(true);

        return authenticatedUser;
    }

    /**
     * Returns an AuthenticatedUser instance populated from the given subject identifier string.
     * It is assumed that this user is authenticated from a federated authenticator.
     *
     * @param authenticatedSubjectIdentifier a string that represents authenticated subject identifier
     * @param federatedIdPName               federated IDP name
     * @return populated AuthenticatedUser instance
     */
    public static AuthenticatedUser createFederateAuthenticatedUserFromSubjectIdentifier(
            String authenticatedSubjectIdentifier, String federatedIdPName) {

        if (authenticatedSubjectIdentifier == null || authenticatedSubjectIdentifier.trim().isEmpty()) {
            throw new IllegalArgumentException("Failed to create Federated Authenticated User from the given subject "
                    + "identifier. Null or empty value provided for authenticatedSubjectIdentifier");
        }

        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setAuthenticatedSubjectIdentifier(authenticatedSubjectIdentifier);
        authenticatedUser.setFederatedUser(true);
        authenticatedUser.setFederatedIdPName(federatedIdPName);
        authenticatedUser.setUserId(authenticatedUser.getFederatedUserIdInternal());

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

    public String getUserId() throws UserIdNotFoundException {

        if (this.userId != null) {
            return this.userId;
        }
        // User id can be null sometimes in some flows. Hence trying to resolve it here.
        this.userId = resolveUserIdInternal();
        if (this.userId == null) {
            throw new UserIdNotFoundException("User id is not available for user.");
        }
        return this.userId;
    }

    public boolean isUserIdExists() {

        return this.userId != null;
    }

    public void setUserId(String userId) {

        this.userId = userId;
    }

    private String resolveUserIdInternal() {

        String userId;
        if (!isFederatedUser()) {
            if (log.isDebugEnabled()) {
                log.debug("Trying to resolve the user id for the local user: " + toFullQualifiedUsername());
            }
            userId = this.getLocalUserIdInternal();
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Trying to resolve the user id for the federated user: " + toFullQualifiedUsername());
            }
            userId = this.getFederatedUserIdInternal();
        }

        return userId;
    }

    /**
     * Returns the user id of the authenticated user.
     * If the user id is not available, it will return the fully qualified username.
     *
     * @return the user id of the authenticated user.
     * @deprecated use {@link #getLoggableMaskedUserId()}
     */
    @Deprecated
    @Override
    public String getLoggableUserId() {

        if (userId != null) {
            return userId;
        }

        // User id can be null sometimes in some flows. Hence, trying to resolve it here.
        String loggableUserId = resolveUserIdInternal();
        if (loggableUserId == null) {
            // If the user id is still null, lets get the fully qualified username as the user id for logging purposes.
            loggableUserId = toFullQualifiedUsername();
        }
        return loggableUserId;
    }

    /**
     * Returns the user id of the authenticated user.
     * If the user id is not available, it will return the fully qualified username. Masked username is returned if
     * masking is enabled.
     *
     * @return the user id of the authenticated user.
     */
    @Override
    public String getLoggableMaskedUserId() {

        if (userId != null) {
            return userId;
        }

        // User id can be null sometimes in some flows. Hence, trying to resolve it here.
        String loggableUserId = resolveUserIdInternal();
        if (loggableUserId == null) {
            // If the user id is still null, lets get the fully qualified username as the user id for logging purposes.
            loggableUserId = toFullQualifiedUsername();
            if (LoggerUtils.isLogMaskingEnable) {
                loggableUserId = LoggerUtils.getMaskedContent(loggableUserId);
            }
        }
        return loggableUserId;
    }

    /**
     * Sets authenticated subject identifier according to the useTenantDomainInLocalSubjectIdentifier and
     * useUserstoreDomainInLocalSubjectIdentifier properties.
     *
     * @param authenticatedSubjectIdentifier authenticated subject identifier
     * @param serviceProvider service provider
     */

    public void setAuthenticatedSubjectIdentifier(String authenticatedSubjectIdentifier,
                                                  ServiceProvider serviceProvider) {

        setAuthenticatedSubjectIdentifier(authenticatedSubjectIdentifier);
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


    public String getAccessingOrganization() {

        return accessingOrganization;
    }

    public void setAccessingOrganization(String accessingOrganization) {

        this.accessingOrganization = accessingOrganization;
    }

    public String getUserResidentOrganization() {

        return userResidentOrganization;
    }

    public void setUserResidentOrganization(String userResidentOrganization) {

        this.userResidentOrganization = userResidentOrganization;
    }

    /**
     * Returns whether this user's identity is managed by an organization or not. A user who has been federated login
     * from an internal organization is considered as an organization user.
     *
     * @return isOrganizationUser
     */
    public boolean isOrganizationUser() {

        return this.isFederatedUser && StringUtils.isNotBlank(this.getUserResidentOrganization());
    }

    @Override
    public boolean equals(Object o) {

        if (!isFederatedUser) {
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
            return federatedIdPName != null ?
                    federatedIdPName.equals(that.federatedIdPName) : that.federatedIdPName == null;
        }
    }

    @Override
    public int hashCode() {

        if (!isFederatedUser) {
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
