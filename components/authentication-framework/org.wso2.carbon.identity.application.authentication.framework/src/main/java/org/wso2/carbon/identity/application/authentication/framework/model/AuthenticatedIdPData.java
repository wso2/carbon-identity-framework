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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Authenticated IDP data.
 */
public class AuthenticatedIdPData implements Serializable, Cloneable {

    private static final long serialVersionUID = 5576595024956777804L;

    private static final Log log = LogFactory.getLog(AuthenticatedIdPData.class);

    private String idpName;

    @Deprecated
    /**
     * @deprecated use {@link #authenticators} instead.
     */
    private AuthenticatorConfig authenticator;

    private List<AuthenticatorConfig> authenticators;
    private AuthenticatedUser user;

    public AuthenticatedIdPData() {
        authenticators = new ArrayList<>();
    }

    public String getIdpName() {
        return idpName;
    }

    public void setIdpName(String idpName) {
        this.idpName = idpName;
    }

    public AuthenticatedUser getUser() {
        return user;
    }

    public void setUser(AuthenticatedUser user) {
        this.user = user;
    }

    /**
     * @deprecated use {@link #getAuthenticators()} instead.
     * @return
     */
    @Deprecated
    public AuthenticatorConfig getAuthenticator() {

        // If the serialized authenticated data has been stored with the old class definition,
        // 'authenticator' field may be available. So that variable takes the priority over 'authenticators' list.
        if (authenticator != null) {
            if (log.isDebugEnabled()) {
                log.debug("Serialized and stored AuthenticatedIdPData object was initially serialized using the " +
                        "old class definition. Handling it in a backward compatible manner");
            }
            return authenticator;
        } else if (CollectionUtils.isNotEmpty(authenticators)) {
            // NOTE : In order to make introducing 'authenticators' field, backward compatible
            // setAuthenticator() and getAuthenticator() methods should remain working as before.
            return authenticators.get(authenticators.size() - 1);
        } else {
            return null;
        }

    }

    /**
     * @deprecated use {@link #addAuthenticator(AuthenticatorConfig)} instead.
     * @param authenticator
     */
    @Deprecated
    public void setAuthenticator(AuthenticatorConfig authenticator) {
        // NOTE : In order to make introducing 'authenticators' backward compatible.
        // setAuthenticator() and getAuthenticator() methods should remain working as before.
        addAuthenticator(authenticator);
    }

    /**
     * Adds the given authenticator as an authenticator which the user has authenticated with.
     *
     * @param authenticator Authenticator config to be added.
     */
    public void addAuthenticator(AuthenticatorConfig authenticator) {
        // If the serialized authenticated data has been stored with the old class definition,
        // authenticators' field will be null, when loading this object from the data store.
        if (this.authenticators == null) {
            this.authenticators = new ArrayList<AuthenticatorConfig>();
        }
        this.authenticators.add(authenticator);
    }

    /**
     * Returns the authenticators which were used to authenticated the user.
     *
     * @return the authenticators which were used to authenticated the user
     */
    public List<AuthenticatorConfig> getAuthenticators() {

        // If the serialized authenticated data has been stored with the old class definition,
        // 'authenticator' field might be not null and 'authenticators' field will be null.
        // So merge 'authenticator' object with the 'authenticators' list accordingly before returning.

        List<AuthenticatorConfig> authenticatorsToBeReturned = null;

        if (this.authenticators != null) {
            authenticatorsToBeReturned = new ArrayList(this.authenticators);
        }

        if (this.authenticator != null) {
            if (log.isDebugEnabled()) {
                log.debug("Serialized and stored AuthenticatedIdPData object was initially serialized using the " +
                        "old class definition. Handling it in a backward compatible manner");
            }

            if (authenticatorsToBeReturned == null) {
                authenticatorsToBeReturned = new ArrayList<AuthenticatorConfig>(1);
            }

            authenticatorsToBeReturned.add(this.authenticator);
        }

        return authenticatorsToBeReturned;
    }

    /**
     * Checks whether the the given authenticator is an authenticator which the user has already authenticated with.
     * @deprecated use {@link #isAlreadyAuthenticatedUsing(String, String)} instead.
     *
     * @param authenticatorName Name of the authenticator to be verified.
     * @return true if the user has been authenticated with the given authenticator.
     */
    @Deprecated
    public boolean isAlreadyAuthenticatedUsing(String authenticatorName) {

        for (AuthenticatorConfig authenticator : getAuthenticators()) {
            if (authenticator.getName().equals(authenticatorName)) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("User '%s' is already authenticated using the " +
                                    "IDP : '%s'and the authenticator : '%s'.",
                            user.getLoggableUserId(), idpName, authenticator.getName()));
                }
                return true;
            }
        }

        if (log.isDebugEnabled()) {
            log.debug(String.format("User '%s' was not authenticated using the " +
                            "IDP : '%s'and the authenticator : '%s' before.",
                    user.getLoggableUserId(), idpName, authenticatorName));
        }

        return false;
    }

    /**
     * Checks whether user has already authenticated with the give authenticator or the authentication mechanism.
     *
     * @param authenticatorName Name of the authenticator to be verified.
     * @param authMechanism     Authentication mechanism.
     * @return true if the user has been authenticated with the given authenticator or the given authentication
     * mechanism.
     */
    public boolean isAlreadyAuthenticatedUsing(String authenticatorName, String authMechanism) {

        String loggableUserId = null;
        if (user != null) {
            loggableUserId = user.getLoggableUserId();
        }
        for (AuthenticatorConfig authenticator : getAuthenticators()) {
            if (authenticator.getName().equals(authenticatorName)
                    || (authenticator.getApplicationAuthenticator() != null
                            && authenticator.getApplicationAuthenticator().getAuthMechanism().equals(authMechanism))) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("User '%s' is already authenticated using the " +
                                    "IDP : '%s' and the authenticator : '%s'.",
                            loggableUserId, idpName, authenticator.getName()));
                }
                return true;
            }
        }

        if (log.isDebugEnabled()) {
            log.debug(String.format("User '%s' was not authenticated using the " +
                            "IDP : '%s' and the authenticator : '%s' before.",
                    loggableUserId, idpName, authenticatorName));
        }

        return false;
    }

    public Object clone() throws CloneNotSupportedException {

        AuthenticatedIdPData authenticatedIdPData = (AuthenticatedIdPData) super.clone();
        authenticatedIdPData.setUser(new AuthenticatedUser(this.user));
        authenticatedIdPData.setIdpName(this.idpName);
        authenticatedIdPData.authenticators = new ArrayList<>(this.authenticators);
        return authenticatedIdPData;
    }

    public void setAuthenticators(List<AuthenticatorConfig> authenticators) {

        this.authenticators = authenticators;
    }
}
