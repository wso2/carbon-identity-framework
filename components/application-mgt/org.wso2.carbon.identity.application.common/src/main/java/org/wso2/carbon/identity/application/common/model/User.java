/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.common.model;

import org.apache.axiom.om.OMElement;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Locale;

/**
 * Representation of a user.
 */
public class User implements Serializable {

    private static final long serialVersionUID = 928301275168169633L;

    protected String tenantDomain;
    protected String userStoreDomain;
    protected String userName;
    protected boolean isUsernameCaseSensitive = true;

    public User() {

    }

    public User(org.wso2.carbon.user.core.common.User commonUser) {

        this.setUserName(commonUser.getUsername());
        this.setUserStoreDomain(commonUser.getUserStoreDomain());
        this.setTenantDomain(commonUser.getTenantDomain());
    }

    /**
     * Returns a User instance populated from the given OMElement
     * The OMElement is of the form below
     * <User>
     * <TenantDomain></TenantDomain>
     * <UserStoreDomain></UserStoreDomain>
     * <UserName></UserName>
     * </User>
     *
     * @param userOM OMElement to populate user
     * @return populated User instance
     */
    public static User build(OMElement userOM) {
        User user = new User();

        if (userOM == null) {
            return user;
        }

        Iterator<?> iter = userOM.getChildElements();
        while (iter.hasNext()) {
            OMElement member = (OMElement) iter.next();
            if ("TenantDomain".equals(member.getLocalName())) {
                if (member.getText() != null) {
                    user.setTenantDomain(member.getText());
                }
            } else if ("UserStoreDomain".equalsIgnoreCase(member.getLocalName())) {
                user.setUserStoreDomain(member.getText());
            } else if ("UserName".equalsIgnoreCase(member.getLocalName())) {
                user.setUserName(member.getText());
            }
        }
        return user;

    }

    /**
     * Returns the tenant domain of the user
     *
     * @return tenant domain
     */
    public String getTenantDomain() {
        return tenantDomain;
    }

    /**
     * Sets the tenant domain of the user
     *
     * @param tenantDomain tenant domain of the user
     */
    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
        updateCaseSensitivity();
    }

    /**
     * Returns the user store domain of the user
     *
     * @return user store domain
     */
    public String getUserStoreDomain() {
        return userStoreDomain;
    }

    /**
     * Sets the user store domain of the user
     *
     * @param userStoreDomain user store domain of the user
     */
    public void setUserStoreDomain(String userStoreDomain) {

        if (StringUtils.isNotEmpty(userStoreDomain)) {
            this.userStoreDomain = userStoreDomain.toUpperCase(Locale.ENGLISH);
        }
        updateCaseSensitivity();
    }

    /**
     * Returns the username of the user
     *
     * @return username
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets the username of the user
     *
     * @param userName
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof User)) {
            return false;
        }

        User user = (User) o;

        if (!tenantDomain.equals(user.tenantDomain)) {
            return false;
        }

        boolean isUsernameCaseSensitive = IdentityUtil.isUserStoreCaseSensitive(userStoreDomain,
                IdentityTenantUtil.getTenantId(tenantDomain));

        if (isUsernameCaseSensitive) {
            if (!userName.equals(user.userName)) {
                return false;
            }
        } else {
            if (!userName.equalsIgnoreCase(user.userName)) {
                return false;
            }
        }

        if (!userStoreDomain.equals(user.userStoreDomain)) {
            return false;
        }

        return true;
    }

    /**
     * Returns a User object constructed from fully qualified username
     *
     * @param username Fully qualified username
     * @return User object
     * @throws IllegalArgumentException
     */
    public static User getUserFromUserName(String username) {

        User user = new User();
        if (StringUtils.isNotBlank(username)) {
            String tenantDomain = MultitenantUtils.getTenantDomain(username);
            String tenantAwareUsername = MultitenantUtils.getTenantAwareUsername(username);
            String tenantAwareUsernameWithNoUserDomain = UserCoreUtil.removeDomainFromName(tenantAwareUsername);
            String userStoreDomain = IdentityUtil.extractDomainFromName(username).toUpperCase(Locale.ENGLISH);
            user.setUserName(tenantAwareUsernameWithNoUserDomain);
            if (StringUtils.isNotEmpty(tenantDomain)) {
                user.setTenantDomain(tenantDomain);
            } else {
                user.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            }
            if (StringUtils.isNotEmpty(userStoreDomain)) {
                user.setUserStoreDomain(userStoreDomain);
            } else {
                user.setUserStoreDomain(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME);
            }
        }
        return user;
    }

    /**
     * Returns full qualified username of the {@link User} object.
     * ie. We append the tenantDomain and userStoreDomain to the username.
     * <p>
     * Note that the PRIMARY domain will not be appended to username when building the full qualified username.
     * Therefore a full qualified name without the userStoreDomain indicates the user belongs to the PRIMARY
     * userStoreDomain.
     *
     * @return full qualified username
     */
    public String toFullQualifiedUsername() {
        String username = null;
        if (StringUtils.isNotBlank(this.userName)) {
            username = this.userName;

            if (StringUtils.isNotBlank(this.tenantDomain)) {
                username = UserCoreUtil.addTenantDomainToEntry(username, tenantDomain);
            }

            if (StringUtils.isNotBlank(this.userStoreDomain)) {
                username = IdentityUtil.addDomainToName(username, userStoreDomain);
            }
        }
        return username;
    }

    public String getLoggableUserId() {

        return toFullQualifiedUsername();
    }

    /**
     * This method will retrieve the 'CaseInsensitiveUsername' property from the respective userstore and set that
     * value.
     */
    protected void updateCaseSensitivity() {

        if (StringUtils.isNotEmpty(tenantDomain) && StringUtils.isNotEmpty(userStoreDomain)
                && IdentityTenantUtil.getRealmService() != null) {
            this.isUsernameCaseSensitive = IdentityUtil
                    .isUserStoreCaseSensitive(userStoreDomain, IdentityTenantUtil.getTenantId(tenantDomain));
        }
    }


    @Override
    public int hashCode() {
        int result = tenantDomain.hashCode();
        result = 31 * result + userStoreDomain.hashCode();
        if (isUsernameCaseSensitive) {
            result = 31 * result + userName.hashCode();
        } else {
            result = 31 * result + userName.toLowerCase().hashCode();
        }
        return result;
    }


    @Override
    public String toString() {

        String username = null;
        if (StringUtils.isNotBlank(this.userName)) {
            username = this.userName;

            if (StringUtils.isNotBlank(this.userStoreDomain)) {
                username = UserCoreUtil.addDomainToName(username, userStoreDomain);
            }
            if (StringUtils.isNotBlank(this.tenantDomain)) {
                username = UserCoreUtil.addTenantDomainToEntry(username, tenantDomain);
            }
        }
        return username;
    }
}
