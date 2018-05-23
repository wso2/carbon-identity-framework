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

    /**
     * If Artifact Binding is enabled
     */
    private boolean isArtifactBindingEnabled;

    /**
     * If Artifact Resolve Request should be signed
     */
    private boolean isArtifactResolveReqSigned;

    /**
     * If Artifact Response will be signed
     */
    private boolean isArtifactResponseSigned;

    /**
     * End url to send artifact resolve soap request
     */
    private String artifactResolveUrl;

    public SAML2SSOFederatedAuthenticatorConfig(FederatedAuthenticatorConfig federatedAuthenticatorConfig) {
        for (Property property : federatedAuthenticatorConfig.getProperties()) {
            switch (property.getName()) {
                case IdentityApplicationConstants.Authenticator.SAML2SSO.IDP_ENTITY_ID:
                    idpEntityId = property.getValue();
                    break;
                case IdentityApplicationConstants.Authenticator.SAML2SSO.SP_ENTITY_ID:
                    spEntityId = property.getValue();
                    break;
                case IdentityApplicationConstants.Authenticator.SAML2SSO.SSO_URL:
                    saml2SSOUrl = property.getValue();
                    break;
                case IdentityApplicationConstants.Authenticator.SAML2SSO.IS_AUTHN_REQ_SIGNED:
                    isAuthnRequestSigned = Boolean.parseBoolean(property.getValue());
                    break;
                case IdentityApplicationConstants.Authenticator.SAML2SSO.IS_LOGOUT_ENABLED:
                    isLogoutEnabled = Boolean.parseBoolean(property.getValue());
                    break;
                case IdentityApplicationConstants.Authenticator.SAML2SSO.IS_LOGOUT_REQ_SIGNED:
                    isLogoutRequestSigned = Boolean.parseBoolean(property.getValue());
                    break;
                case IdentityApplicationConstants.Authenticator.SAML2SSO.LOGOUT_REQ_URL:
                    logoutRequestUrl = property.getValue();
                    break;
                case IdentityApplicationConstants.Authenticator.SAML2SSO.IS_AUTHN_RESP_SIGNED:
                    isAuthnResponseSigned = Boolean.parseBoolean(property.getValue());
                    break;
                case IdentityApplicationConstants.Authenticator.SAML2SSO.IS_ENABLE_ASSERTION_ENCRYPTION:
                    isAuthnResponseEncrypted = Boolean.parseBoolean(property.getValue());
                    break;
                case IdentityApplicationConstants.Authenticator.SAML2SSO.IS_ARTIFACT_BINDING_ENABLED:
                    isArtifactBindingEnabled = Boolean.parseBoolean(property.getValue());
                    break;
                case IdentityApplicationConstants.Authenticator.SAML2SSO.IS_ARTIFACT_RESOLVE_REQ_SIGNED:
                    isArtifactResolveReqSigned = Boolean.parseBoolean(property.getValue());
                    break;
                case IdentityApplicationConstants.Authenticator.SAML2SSO.IS_ARTIFACT_RESPONSE_SIGNED:
                    isArtifactResponseSigned = Boolean.parseBoolean(property.getValue());
                    break;
                case IdentityApplicationConstants.Authenticator.SAML2SSO.ARTIFACT_RESOLVE_URL:
                    artifactResolveUrl = property.getValue();
                    break;
            }
        }
    }

    @Override
    public boolean isValid() {
        return isValidPropertyValue(IdentityApplicationManagementUtil.getProperty(properties,
                IdentityApplicationConstants.Authenticator.SAML2SSO.IDP_ENTITY_ID))
                && isValidPropertyValue(IdentityApplicationManagementUtil.getProperty(properties,
                IdentityApplicationConstants.Authenticator.SAML2SSO.SP_ENTITY_ID))
                && isValidPropertyValue(IdentityApplicationManagementUtil.getProperty(properties,
                IdentityApplicationConstants.Authenticator.SAML2SSO.SSO_URL));
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

    public boolean isArtifactBindingEnabled() {
        return isArtifactBindingEnabled;
    }

    public boolean isArtifactResolveReqSigned() {
        return isArtifactResolveReqSigned;
    }

    public boolean isArtifactResponseSigned() {
        return isArtifactResponseSigned;
    }

    public String getArtifactResolveUrl() { return artifactResolveUrl; }
}
