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

import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationManagementUtil;

public class SAML2SSOFederatedAuthenticatorConfig extends FederatedAuthenticatorConfig {
    /**
     *
     */
    private static final long serialVersionUID = -171672098979315832L;

    /**
     * The IdP's Entity Issuer value
     */
    private String idpEntityId;

    /**
     * If Single Logout is enabled
     */
    private boolean isLogoutEnabled;

    /**
     * The SAML2 Web SSO URL of the IdP
     */
    private String saml2SSOUrl;

    /**
     * If LogoutRequest should be signed
     */
    private boolean isLogoutRequestSigned;

    /**
     * If the LogoutRequestUrl is different from ACS URL
     */
    private String logoutRequestUrl;

    /*
     * The service provider's Entity Id
     */
    private String spEntityId;

    /**
     * If the AuthnRequest has to be signed
     */
    private boolean isAuthnRequestSigned;

    /**
     * If the AuthnRequest has to be signed
     */
    private boolean isAuthnResponseSigned;

    /**
     * If the AuthnResponse has to be encrypted
     */
    private boolean isAuthnResponseEncrypted;

    /**
     * If User ID is found among claims
     */
    private boolean isUserIdInClaims;

    public SAML2SSOFederatedAuthenticatorConfig(FederatedAuthenticatorConfig federatedAuthenticatorConfig) {
        for (Property property : federatedAuthenticatorConfig.getProperties()) {
            if (IdentityApplicationConstants.Authenticator.SAML2SSO.IDP_ENTITY_ID.equals(property.getName())) {
                idpEntityId = property.getValue();
            } else if (IdentityApplicationConstants.Authenticator.SAML2SSO.SP_ENTITY_ID.equals(property.getName())) {
                spEntityId = property.getValue();
            } else if (IdentityApplicationConstants.Authenticator.SAML2SSO.SSO_URL.equals(property.getName())) {
                saml2SSOUrl = property.getValue();
            } else if (IdentityApplicationConstants.Authenticator.SAML2SSO.IS_AUTHN_REQ_SIGNED.equals(
                    property.getName())) {
                isAuthnRequestSigned = Boolean.parseBoolean(property.getValue());
            } else if (IdentityApplicationConstants.Authenticator.SAML2SSO.IS_LOGOUT_ENABLED.equals(
                    property.getName())) {
                isLogoutEnabled = Boolean.parseBoolean(property.getValue());
            } else if (IdentityApplicationConstants.Authenticator.SAML2SSO.IS_LOGOUT_REQ_SIGNED.equals(
                    property.getName())) {
                isLogoutRequestSigned = Boolean.parseBoolean(property.getValue());
            } else if (IdentityApplicationConstants.Authenticator.SAML2SSO.LOGOUT_REQ_URL.equals(property.getName())) {
                logoutRequestUrl = property.getValue();
            } else if (IdentityApplicationConstants.Authenticator.SAML2SSO.IS_AUTHN_RESP_SIGNED.equals(
                    property.getName())) {
                isAuthnResponseSigned = Boolean.parseBoolean(property.getValue());
            } else if (IdentityApplicationConstants.Authenticator.SAML2SSO.IS_ENABLE_ASSERTION_ENCRYPTION.equals(
                    property.getName())) {
                isAuthnResponseEncrypted = Boolean.parseBoolean(property.getValue());
            }
        }
    }

    @Override
    public boolean isValid() {

        if (IdentityApplicationManagementUtil.getProperty(properties,
                IdentityApplicationConstants.Authenticator.SAML2SSO.IDP_ENTITY_ID) != null
                && !"".equals(IdentityApplicationManagementUtil.getProperty(properties,
                IdentityApplicationConstants.Authenticator.SAML2SSO.IDP_ENTITY_ID))
                && IdentityApplicationManagementUtil.getProperty(properties,
                IdentityApplicationConstants.Authenticator.SAML2SSO.SP_ENTITY_ID) != null
                && !"".equals(IdentityApplicationManagementUtil.getProperty(properties,
                IdentityApplicationConstants.Authenticator.SAML2SSO.SP_ENTITY_ID))
                && IdentityApplicationManagementUtil.getProperty(properties,
                IdentityApplicationConstants.Authenticator.SAML2SSO.SSO_URL) != null
                && !"".equals(IdentityApplicationManagementUtil.getProperty(properties,
                IdentityApplicationConstants.Authenticator.SAML2SSO.SSO_URL))) {
            return true;
        }
        return false;
    }

    @Override
    public String getName() {
        return IdentityApplicationConstants.Authenticator.SAML2SSO.NAME;
    }

    ////////////////////////////// Getters ///////////////////////////

    public String getIdpEntityId() {
        return idpEntityId;
    }

    public boolean isLogoutEnabled() {
        return isLogoutEnabled;
    }

    public boolean isLogoutRequestSigned() {
        return isLogoutRequestSigned;
    }

    public String getLogoutRequestUrl() {
        return logoutRequestUrl;
    }

    public String getSpEntityId() {
        return spEntityId;
    }

    public boolean isAuthnRequestSigned() {
        return isAuthnRequestSigned;
    }

    public boolean isAuthnResponseSigned() {
        return isAuthnResponseSigned;
    }

    public boolean isUserIdInClaims() {
        return isUserIdInClaims;
    }

    public String getSaml2SSOUrl() {
        return saml2SSOUrl;
    }

    public boolean isAuthnResponseEncrypted() {
        return isAuthnResponseEncrypted;
    }
}
