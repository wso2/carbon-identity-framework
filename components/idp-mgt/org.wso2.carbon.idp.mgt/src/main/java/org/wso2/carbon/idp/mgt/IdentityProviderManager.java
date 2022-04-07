/*
 * Copyright (c) 2014 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.idp.mgt;

import org.apache.axiom.om.util.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.identity.application.common.ApplicationAuthenticatorService;
import org.wso2.carbon.identity.application.common.ProvisioningConnectorService;
import org.wso2.carbon.identity.application.common.model.ClaimConfig;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.IdentityProviderProperty;
import org.wso2.carbon.identity.application.common.model.LocalRole;
import org.wso2.carbon.identity.application.common.model.PermissionsAndRoleConfig;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.ProvisioningConnectorConfig;
import org.wso2.carbon.identity.application.common.model.RoleMapping;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationManagementUtil;
import org.wso2.carbon.identity.base.IdentityConstants;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.ServiceURLBuilder;
import org.wso2.carbon.identity.core.URLBuilderException;
import org.wso2.carbon.identity.core.model.ExpressionNode;
import org.wso2.carbon.identity.core.model.FilterTreeBuilder;
import org.wso2.carbon.identity.core.model.Node;
import org.wso2.carbon.identity.core.model.OperationNode;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.role.mgt.core.IdentityRoleManagementException;
import org.wso2.carbon.identity.role.mgt.core.RoleManagementService;
import org.wso2.carbon.idp.mgt.dao.CacheBackedIdPMgtDAO;
import org.wso2.carbon.idp.mgt.dao.FileBasedIdPMgtDAO;
import org.wso2.carbon.idp.mgt.dao.IdPManagementDAO;
import org.wso2.carbon.idp.mgt.internal.IdPManagementServiceComponent;
import org.wso2.carbon.idp.mgt.internal.IdpMgtServiceComponentHolder;
import org.wso2.carbon.idp.mgt.listener.IdentityProviderMgtListener;
import org.wso2.carbon.idp.mgt.model.ConnectedAppsResult;
import org.wso2.carbon.idp.mgt.model.IdpSearchResult;
import org.wso2.carbon.idp.mgt.util.IdPManagementConstants;
import org.wso2.carbon.idp.mgt.util.IdPManagementUtil;
import org.wso2.carbon.idp.mgt.util.MetadataConverter;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.stream.XMLStreamException;

import static org.wso2.carbon.user.core.UserCoreConstants.INTERNAL_DOMAIN;
import static org.wso2.carbon.user.core.UserCoreConstants.WORKFLOW_DOMAIN;
import static org.wso2.carbon.user.mgt.UserMgtConstants.APPLICATION_DOMAIN;

public class IdentityProviderManager implements IdpManager {

    private static final Log log = LogFactory.getLog(IdentityProviderManager.class);
    private static final String OPENID_IDP_ENTITY_ID = "IdPEntityId";
    private static CacheBackedIdPMgtDAO dao = new CacheBackedIdPMgtDAO(new IdPManagementDAO());
    private static volatile IdentityProviderManager instance = new IdentityProviderManager();

    private IdentityProviderManager() {

    }

    /**
     * @return
     */
    public static IdentityProviderManager getInstance() {

        return instance;
    }

    /**
     * Retrieves resident Identity provider for a given tenant.
     *
     * @param tenantDomain Tenant domain whose resident IdP is requested
     * @return <code>LocalIdentityProvider</code>
     * @throws IdentityProviderManagementException Error when getting Resident Identity Providers
     */
    @Override
    public IdentityProvider getResidentIdP(String tenantDomain)
            throws IdentityProviderManagementException {

        IdPManagementUtil.setTenantSpecifiers(tenantDomain);

        String openIdUrl;
        String oauth1RequestTokenUrl;
        String oauth1AuthorizeUrl;
        String oauth1AccessTokenUrl;
        String oauth2AuthzEPUrl;
        String oauth2TokenEPUrl;
        String oauth2RevokeEPUrl;
        String oauth2IntrospectEpUrl;
        String oauth2UserInfoEPUrl;
        String oidcCheckSessionEPUrl;
        String oidcLogoutEPUrl;
        String oIDCWebFingerEPUrl;
        String oAuth2DCREPUrl;
        String oAuth2JWKSPage;
        String oIDCDiscoveryEPUrl;
        String passiveStsUrl;
        String stsUrl;
        String scimUsersEndpoint;
        String scimGroupsEndpoint;
        String scim2UsersEndpoint;
        String scim2GroupsEndpoint;

        openIdUrl = IdentityUtil.getProperty(IdentityConstants.ServerConfig.OPENID_SERVER_URL);
        oauth1RequestTokenUrl = IdentityUtil.getProperty(IdentityConstants.OAuth.OAUTH1_REQUEST_TOKEN_URL);
        oauth1AuthorizeUrl = IdentityUtil.getProperty(IdentityConstants.OAuth.OAUTH1_AUTHORIZE_URL);
        oauth1AccessTokenUrl = IdentityUtil.getProperty(IdentityConstants.OAuth.OAUTH1_ACCESSTOKEN_URL);
        oauth2AuthzEPUrl = IdentityUtil.getProperty(IdentityConstants.OAuth.OAUTH2_AUTHZ_EP_URL);
        oauth2TokenEPUrl = IdentityUtil.getProperty(IdentityConstants.OAuth.OAUTH2_TOKEN_EP_URL);
        oauth2UserInfoEPUrl = IdentityUtil.getProperty(IdentityConstants.OAuth.OAUTH2_USERINFO_EP_URL);
        oidcCheckSessionEPUrl = IdentityUtil.getProperty(IdentityConstants.OAuth.OIDC_CHECK_SESSION_EP_URL);
        oidcLogoutEPUrl = IdentityUtil.getProperty(IdentityConstants.OAuth.OIDC_LOGOUT_EP_URL);
        passiveStsUrl = IdentityUtil.getProperty(IdentityConstants.STS.PSTS_IDENTITY_PROVIDER_URL);
        stsUrl = IdentityUtil.getProperty(IdentityConstants.STS.STS_IDENTITY_PROVIDER_URL);
        scimUsersEndpoint = IdentityUtil.getProperty(IdentityConstants.SCIM.USER_EP_URL);
        scimGroupsEndpoint = IdentityUtil.getProperty(IdentityConstants.SCIM.GROUP_EP_URL);
        scim2UsersEndpoint = IdentityUtil.getProperty(IdentityConstants.SCIM2.USER_EP_URL);
        scim2GroupsEndpoint = IdentityUtil.getProperty(IdentityConstants.SCIM2.GROUP_EP_URL);
        oauth2RevokeEPUrl = IdentityUtil.getProperty(IdentityConstants.OAuth.OAUTH2_REVOKE_EP_URL);
        oauth2IntrospectEpUrl = IdentityUtil.getProperty(IdentityConstants.OAuth.OAUTH2_INTROSPECT_EP_URL);
        oIDCWebFingerEPUrl = IdentityUtil.getProperty(IdentityConstants.OAuth.OIDC_WEB_FINGER_EP_URL);
        oAuth2DCREPUrl = IdentityUtil.getProperty(IdentityConstants.OAuth.OAUTH2_DCR_EP_URL);
        oAuth2JWKSPage = IdentityUtil.getProperty(IdentityConstants.OAuth.OAUTH2_JWKS_EP_URL);
        oIDCDiscoveryEPUrl = IdentityUtil.getProperty(IdentityConstants.OAuth.OIDC_DISCOVERY_EP_URL);

        if (StringUtils.isBlank(openIdUrl)) {
            openIdUrl = IdentityUtil.getServerURL(IdentityConstants.OpenId.OPENID, true, true);
        }

        if (StringUtils.isBlank(oauth1RequestTokenUrl)) {
            oauth1RequestTokenUrl = IdentityUtil.getServerURL(IdentityConstants.OAuth.REQUEST_TOKEN, true, true);
        }

        if (StringUtils.isBlank(oauth1AuthorizeUrl)) {
            oauth1AuthorizeUrl = IdentityUtil.getServerURL(IdentityConstants.OAuth.AUTHORIZE_URL, true, true);
        }

        if (StringUtils.isBlank(oauth1AccessTokenUrl)) {
            oauth1AccessTokenUrl = IdentityUtil.getServerURL(IdentityConstants.OAuth.ACCESS_TOKEN, true, true);
        }

        oauth2AuthzEPUrl = resolveAbsoluteURL(IdentityConstants.OAuth.AUTHORIZE, oauth2AuthzEPUrl, tenantDomain);
        oauth2TokenEPUrl = resolveAbsoluteURL(IdentityConstants.OAuth.TOKEN, oauth2TokenEPUrl, tenantDomain);
        oauth2RevokeEPUrl = resolveAbsoluteURL(IdentityConstants.OAuth.REVOKE, oauth2RevokeEPUrl, tenantDomain);
        oauth2IntrospectEpUrl = resolveAbsoluteURL(IdentityConstants.OAuth.INTROSPECT, oauth2IntrospectEpUrl,
                tenantDomain);
        oauth2IntrospectEpUrl = addTenantPathParamInLegacyMode(oauth2IntrospectEpUrl, tenantDomain);
        oauth2UserInfoEPUrl = resolveAbsoluteURL(IdentityConstants.OAuth.USERINFO, oauth2UserInfoEPUrl, tenantDomain);
        oidcCheckSessionEPUrl = resolveAbsoluteURL(IdentityConstants.OAuth.CHECK_SESSION, oidcCheckSessionEPUrl,
                tenantDomain);
        oidcLogoutEPUrl = resolveAbsoluteURL(IdentityConstants.OAuth.LOGOUT, oidcLogoutEPUrl,tenantDomain);
        oAuth2DCREPUrl = resolveAbsoluteURL(IdentityConstants.OAuth.DCR, oAuth2DCREPUrl, tenantDomain);
        oAuth2DCREPUrl = addTenantPathParamInLegacyMode(oAuth2DCREPUrl, tenantDomain);
        oAuth2JWKSPage = resolveAbsoluteURL(IdentityConstants.OAuth.JWKS, oAuth2JWKSPage, tenantDomain);
        oAuth2JWKSPage = addTenantPathParamInLegacyMode(oAuth2JWKSPage, tenantDomain);
        oIDCDiscoveryEPUrl = resolveAbsoluteURL(IdentityConstants.OAuth.DISCOVERY, oIDCDiscoveryEPUrl, tenantDomain);
        oIDCDiscoveryEPUrl = addTenantPathParamInLegacyMode(oIDCDiscoveryEPUrl, tenantDomain);
        passiveStsUrl = resolveAbsoluteURL(IdentityConstants.STS.PASSIVE_STS, passiveStsUrl, tenantDomain);

        // If sts url is configured in file, change it according to tenant domain. If not configured, add a default url
        if (StringUtils.isNotBlank(stsUrl)) {
            stsUrl = stsUrl.replace(IdentityConstants.STS.WSO2_CARBON_STS, getTenantContextFromTenantDomain(tenantDomain) +
                    IdentityConstants.STS.WSO2_CARBON_STS);
        } else {
            stsUrl = IdentityUtil.getServerURL("services/" + getTenantContextFromTenantDomain(tenantDomain) +
                    IdentityConstants.STS.WSO2_CARBON_STS, true, true);
        }

        if (StringUtils.isBlank(scimUsersEndpoint)) {
            scimUsersEndpoint = IdentityUtil.getServerURL(IdentityConstants.SCIM.USER_EP, true, false);
        }

        if (StringUtils.isBlank(scimGroupsEndpoint)) {
            scimGroupsEndpoint = IdentityUtil.getServerURL(IdentityConstants.SCIM.GROUP_EP, true, false);
        }

        if (StringUtils.isBlank(scim2UsersEndpoint)) {
            scim2UsersEndpoint = IdentityUtil.getServerURL(IdentityConstants.SCIM2.USER_EP, true, false);
        }
        try {
            if (StringUtils.isNotBlank(tenantDomain) && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals
                    (tenantDomain)) {
                scim2UsersEndpoint = getTenantUrl(scim2UsersEndpoint, tenantDomain);
            }
        } catch (URISyntaxException e) {
            log.error("SCIM 2.0 Users endpoint is malformed");
        }

        if (StringUtils.isBlank(scim2GroupsEndpoint)) {
            scim2GroupsEndpoint = IdentityUtil.getServerURL(IdentityConstants.SCIM2.GROUP_EP, true, false);
        }
        try {
            if (StringUtils.isNotBlank(tenantDomain) && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals
                    (tenantDomain)) {
                scim2GroupsEndpoint = getTenantUrl(scim2GroupsEndpoint, tenantDomain);
            }
        } catch (URISyntaxException e) {
            log.error("SCIM 2.0 Groups endpoint is malformed");
        }

        IdentityProvider identityProvider = dao.getIdPByName(null,
                IdentityApplicationConstants.RESIDENT_IDP_RESERVED_NAME,
                IdentityTenantUtil.getTenantId(tenantDomain), tenantDomain);

        if (identityProvider == null) {
            String message = "Could not find Resident Identity Provider for tenant " + tenantDomain;
            throw new IdentityProviderManagementException(message);
        }

        int tenantId = -1;
        try {
            tenantId = IdPManagementServiceComponent.getRealmService().getTenantManager().getTenantId(tenantDomain);
        } catch (UserStoreException e) {
            throw new IdentityProviderManagementException(
                    "Exception occurred while retrieving Tenant ID from Tenant Domain " + tenantDomain, e);
        }
        X509Certificate cert = null;
        try {
            IdentityTenantUtil.initializeRegistry(tenantId);
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setTenantDomain(tenantDomain, true);
            KeyStoreManager keyStoreManager = KeyStoreManager.getInstance(tenantId);
            if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                // derive key store name
                String ksName = tenantDomain.trim().replace(".", "-");
                // derive JKS name
                String jksName = ksName + ".jks";
                KeyStore keyStore = keyStoreManager.getKeyStore(jksName);
                cert = (X509Certificate) keyStore.getCertificate(tenantDomain);
            } else {
                cert = keyStoreManager.getDefaultPrimaryCertificate();
            }
        } catch (Exception e) {
            String msg = "Error retrieving primary certificate for tenant : " + tenantDomain;
            throw new IdentityProviderManagementException(msg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }

        if (cert == null) {
            throw new IdentityProviderManagementException(
                    "Cannot find the primary certificate for tenant " + tenantDomain);
        }
        try {
            identityProvider.setCertificate(Base64.encode(cert.getEncoded()));
        } catch (CertificateEncodingException e) {
            String msg = "Error occurred while encoding primary certificate for tenant domain " + tenantDomain;
            throw new IdentityProviderManagementException(msg, e);
        }

        List<FederatedAuthenticatorConfig> fedAuthnCofigs = new ArrayList<FederatedAuthenticatorConfig>();
        List<Property> propertiesList = null;

        FederatedAuthenticatorConfig openIdFedAuthn = IdentityApplicationManagementUtil
                .getFederatedAuthenticator(identityProvider.getFederatedAuthenticatorConfigs(),
                        IdentityApplicationConstants.Authenticator.OpenID.NAME);
        if (openIdFedAuthn == null) {
            openIdFedAuthn = new FederatedAuthenticatorConfig();
            openIdFedAuthn.setName(IdentityApplicationConstants.Authenticator.OpenID.NAME);
        }
        propertiesList = new ArrayList<Property>(Arrays.asList(openIdFedAuthn.getProperties()));
        if (IdentityApplicationManagementUtil.getProperty(openIdFedAuthn.getProperties(),
                IdentityApplicationConstants.Authenticator.OpenID.OPEN_ID_URL) == null) {
            Property openIdUrlProp = new Property();
            openIdUrlProp.setName(IdentityApplicationConstants.Authenticator.OpenID.OPEN_ID_URL);
            openIdUrlProp.setValue(openIdUrl);
            propertiesList.add(openIdUrlProp);
        }
        openIdFedAuthn.setProperties(propertiesList.toArray(new Property[propertiesList.size()]));
        fedAuthnCofigs.add(openIdFedAuthn);

        // SAML2 related endpoints.
        FederatedAuthenticatorConfig saml2SSOFedAuthn = buildSAMLProperties(identityProvider, tenantDomain);
        fedAuthnCofigs.add(saml2SSOFedAuthn);

        FederatedAuthenticatorConfig oauth1FedAuthn = IdentityApplicationManagementUtil
                .getFederatedAuthenticator(identityProvider.getFederatedAuthenticatorConfigs(),
                        IdentityApplicationConstants.OAuth10A.NAME);
        if (oauth1FedAuthn == null) {
            oauth1FedAuthn = new FederatedAuthenticatorConfig();
            oauth1FedAuthn.setName(IdentityApplicationConstants.OAuth10A.NAME);
        }
        propertiesList = new ArrayList<Property>(Arrays.asList(oauth1FedAuthn.getProperties()));
        if (IdentityApplicationManagementUtil.getProperty(oauth1FedAuthn.getProperties(),
                IdentityApplicationConstants.OAuth10A.OAUTH1_REQUEST_TOKEN_URL) == null) {
            Property oauth1ReqTokUrlProp = new Property();
            oauth1ReqTokUrlProp.setName(IdentityApplicationConstants.OAuth10A.OAUTH1_REQUEST_TOKEN_URL);
            oauth1ReqTokUrlProp.setValue(oauth1RequestTokenUrl);
            propertiesList.add(oauth1ReqTokUrlProp);
        }
        if (IdentityApplicationManagementUtil.getProperty(oauth1FedAuthn.getProperties(),
                IdentityApplicationConstants.OAuth10A.OAUTH1_AUTHORIZE_URL) == null) {
            Property oauth1AuthzUrlProp = new Property();
            oauth1AuthzUrlProp.setName(IdentityApplicationConstants.OAuth10A.OAUTH1_AUTHORIZE_URL);
            oauth1AuthzUrlProp.setValue(oauth1AuthorizeUrl);
            propertiesList.add(oauth1AuthzUrlProp);
        }
        if (IdentityApplicationManagementUtil.getProperty(oauth1FedAuthn.getProperties(),
                IdentityApplicationConstants.OAuth10A.OAUTH1_ACCESS_TOKEN_URL) == null) {
            Property oauth1AccessTokUrlProp = new Property();
            oauth1AccessTokUrlProp.setName(IdentityApplicationConstants.OAuth10A.OAUTH1_ACCESS_TOKEN_URL);
            oauth1AccessTokUrlProp.setValue(oauth1AccessTokenUrl);
            propertiesList.add(oauth1AccessTokUrlProp);
        }
        oauth1FedAuthn.setProperties(propertiesList.toArray(new Property[propertiesList.size()]));
        fedAuthnCofigs.add(oauth1FedAuthn);

        FederatedAuthenticatorConfig oidcFedAuthn = IdentityApplicationManagementUtil
                .getFederatedAuthenticator(identityProvider.getFederatedAuthenticatorConfigs(),
                        IdentityApplicationConstants.Authenticator.OIDC.NAME);
        if (oidcFedAuthn == null) {
            oidcFedAuthn = new FederatedAuthenticatorConfig();
            oidcFedAuthn.setName(IdentityApplicationConstants.Authenticator.OIDC.NAME);
        }
        propertiesList = new ArrayList<Property>();

        Property idPEntityIdProp;
        // When the tenant qualified urls are enabled, we need to see the oauth2 token endpoint.
        if (IdentityTenantUtil.isTenantQualifiedUrlsEnabled()) {
            idPEntityIdProp = resolveFedAuthnProperty(oauth2TokenEPUrl, oidcFedAuthn,
                    OPENID_IDP_ENTITY_ID);
        } else {
            idPEntityIdProp = resolveFedAuthnProperty(getOIDCResidentIdPEntityId(), oidcFedAuthn,
                    OPENID_IDP_ENTITY_ID);
        }
        propertiesList.add(idPEntityIdProp);

        Property authzUrlProp = resolveFedAuthnProperty(oauth2AuthzEPUrl, oidcFedAuthn,
                IdentityApplicationConstants.Authenticator.OIDC.OAUTH2_AUTHZ_URL);
        propertiesList.add(authzUrlProp);

        Property tokenUrlProp = resolveFedAuthnProperty(oauth2TokenEPUrl, oidcFedAuthn,
                IdentityApplicationConstants.Authenticator.OIDC.OAUTH2_TOKEN_URL);
        propertiesList.add(tokenUrlProp);

        Property revokeUrlProp = resolveFedAuthnProperty(oauth2RevokeEPUrl, oidcFedAuthn,
                IdentityApplicationConstants.Authenticator.OIDC.OAUTH2_REVOKE_URL);
        propertiesList.add(revokeUrlProp);

        Property instropsectUrlProp = resolveFedAuthnProperty(oauth2IntrospectEpUrl, oidcFedAuthn,
                IdentityApplicationConstants.Authenticator.OIDC.OAUTH2_INTROSPECT_URL);
        propertiesList.add(instropsectUrlProp);

        Property userInfoUrlProp = resolveFedAuthnProperty(oauth2UserInfoEPUrl, oidcFedAuthn,
                IdentityApplicationConstants.Authenticator.OIDC.OAUTH2_USER_INFO_EP_URL);
        propertiesList.add(userInfoUrlProp);

        Property checkSessionUrlProp = resolveFedAuthnProperty(oidcCheckSessionEPUrl, oidcFedAuthn,
                IdentityApplicationConstants.Authenticator.OIDC.OIDC_CHECK_SESSION_URL);
        propertiesList.add(checkSessionUrlProp);

        Property logoutUrlProp = resolveFedAuthnProperty(oidcLogoutEPUrl, oidcFedAuthn,
                IdentityApplicationConstants.Authenticator.OIDC.OIDC_LOGOUT_URL);
        propertiesList.add(logoutUrlProp);

        Property dcrUrlProp = resolveFedAuthnProperty(oAuth2DCREPUrl, oidcFedAuthn,
                IdentityApplicationConstants.Authenticator.OIDC.OAUTH2_DCR_EP_URL);
        propertiesList.add(dcrUrlProp);

        Property webFingerUrlProp = resolveFedAuthnProperty(oIDCWebFingerEPUrl, oidcFedAuthn,
                IdentityApplicationConstants.Authenticator.OIDC.OIDC_WEB_FINGER_EP_URL);
        propertiesList.add(webFingerUrlProp);

        Property jwksUrlProp = resolveFedAuthnProperty(oAuth2JWKSPage, oidcFedAuthn,
                IdentityApplicationConstants.Authenticator.OIDC.OAUTH2_JWKS_EP_URL);
        propertiesList.add(jwksUrlProp);

        Property discoveryUrlProp = resolveFedAuthnProperty(oIDCDiscoveryEPUrl, oidcFedAuthn,
                IdentityApplicationConstants.Authenticator.OIDC.OIDC_DISCOVERY_EP_URL);
        propertiesList.add(discoveryUrlProp);

        oidcFedAuthn.setProperties(propertiesList.toArray(new Property[propertiesList.size()]));
        fedAuthnCofigs.add(oidcFedAuthn);

        FederatedAuthenticatorConfig passiveSTSFedAuthn = IdentityApplicationManagementUtil
                .getFederatedAuthenticator(identityProvider.getFederatedAuthenticatorConfigs(),
                        IdentityApplicationConstants.Authenticator.PassiveSTS.NAME);
        if (passiveSTSFedAuthn == null) {
            passiveSTSFedAuthn = new FederatedAuthenticatorConfig();
            passiveSTSFedAuthn.setName(IdentityApplicationConstants.Authenticator.PassiveSTS.NAME);
        }

        propertiesList = new ArrayList<>();
        Property passiveSTSUrlProperty = IdentityApplicationManagementUtil.getProperty(passiveSTSFedAuthn
                .getProperties(), IdentityApplicationConstants.Authenticator.PassiveSTS.IDENTITY_PROVIDER_URL);
        if (passiveSTSUrlProperty == null) {
            passiveSTSUrlProperty = new Property();
            passiveSTSUrlProperty.setName(IdentityApplicationConstants.Authenticator.PassiveSTS.IDENTITY_PROVIDER_URL);
        }
        passiveSTSUrlProperty.setValue(passiveStsUrl);
        propertiesList.add(passiveSTSUrlProperty);

        Property stsIdPEntityIdProperty = IdentityApplicationManagementUtil.getProperty(passiveSTSFedAuthn
                .getProperties(), IdentityApplicationConstants.Authenticator.PassiveSTS.IDENTITY_PROVIDER_ENTITY_ID);
        if (stsIdPEntityIdProperty == null) {
            stsIdPEntityIdProperty = new Property();
            stsIdPEntityIdProperty.setName(IdentityApplicationConstants.Authenticator.PassiveSTS
                    .IDENTITY_PROVIDER_ENTITY_ID);
            stsIdPEntityIdProperty.setValue(IdPManagementUtil.getResidentIdPEntityId());
        }
        propertiesList.add(stsIdPEntityIdProperty);

        for (Property property : passiveSTSFedAuthn.getProperties()) {
            if (property != null && !IdentityApplicationConstants.Authenticator.PassiveSTS.IDENTITY_PROVIDER_URL
                    .equals(property.getName()) && !IdentityApplicationConstants.Authenticator.PassiveSTS
                    .IDENTITY_PROVIDER_ENTITY_ID.equals(property.getName())) {
                propertiesList.add(property);
            }
        }

        passiveSTSFedAuthn
                .setProperties(propertiesList.toArray(new Property[propertiesList.size()]));
        fedAuthnCofigs.add(passiveSTSFedAuthn);

        FederatedAuthenticatorConfig stsFedAuthn = IdentityApplicationManagementUtil
                .getFederatedAuthenticator(identityProvider.getFederatedAuthenticatorConfigs(),
                        IdentityApplicationConstants.Authenticator.WSTrust.NAME);
        if (stsFedAuthn == null) {
            stsFedAuthn = new FederatedAuthenticatorConfig();
            stsFedAuthn.setName(IdentityApplicationConstants.Authenticator.WSTrust.NAME);
        }
        propertiesList = new ArrayList<Property>(Arrays.asList(stsFedAuthn.getProperties()));
        if (IdentityApplicationManagementUtil.getProperty(stsFedAuthn.getProperties(),
                IdentityApplicationConstants.Authenticator.WSTrust.IDENTITY_PROVIDER_URL) == null) {
            Property stsUrlProp = new Property();
            stsUrlProp.setName(IdentityApplicationConstants.Authenticator.WSTrust.IDENTITY_PROVIDER_URL);
            stsUrlProp.setValue(stsUrl);
            propertiesList.add(stsUrlProp);
        }
        stsFedAuthn
                .setProperties(propertiesList.toArray(new Property[propertiesList.size()]));
        fedAuthnCofigs.add(stsFedAuthn);

        List<IdentityProviderProperty> identityProviderProperties = new ArrayList<IdentityProviderProperty>();

        FederatedAuthenticatorConfig sessionTimeoutConfig = new FederatedAuthenticatorConfig();
        sessionTimeoutConfig.setName(IdentityApplicationConstants.NAME);

        propertiesList = new ArrayList<Property>(Arrays.asList(sessionTimeoutConfig.getProperties()));

        Property cleanUpPeriodProp = new Property();
        cleanUpPeriodProp.setName(IdentityApplicationConstants.CLEAN_UP_PERIOD);
        String cleanUpPeriod = IdentityUtil.getProperty(IdentityConstants.ServerConfig.CLEAN_UP_PERIOD);
        if (StringUtils.isBlank(cleanUpPeriod)) {
            cleanUpPeriod = IdentityApplicationConstants.CLEAN_UP_PERIOD_DEFAULT;
        } else if (!StringUtils.isNumeric(cleanUpPeriod)) {
            log.warn("PersistanceCleanUpPeriod in identity.xml should be a numeric value");
            cleanUpPeriod = IdentityApplicationConstants.CLEAN_UP_PERIOD_DEFAULT;
        }
        cleanUpPeriodProp.setValue(cleanUpPeriod);
        propertiesList.add(cleanUpPeriodProp);

        sessionTimeoutConfig.setProperties(propertiesList.toArray(new Property[propertiesList.size()]));
        fedAuthnCofigs.add(sessionTimeoutConfig);

        identityProvider.setFederatedAuthenticatorConfigs(fedAuthnCofigs
                .toArray(new FederatedAuthenticatorConfig[fedAuthnCofigs.size()]));

        ProvisioningConnectorConfig scimProvConn = IdentityApplicationManagementUtil
                .getProvisioningConnector(identityProvider.getProvisioningConnectorConfigs(),
                        "scim");
        if (scimProvConn == null) {
            scimProvConn = new ProvisioningConnectorConfig();
            scimProvConn.setName("scim");
        }
        propertiesList = new ArrayList<>(Arrays.asList(scimProvConn.getProvisioningProperties()));
        Property scimUserEndpointProperty = IdentityApplicationManagementUtil.getProperty(scimProvConn
                .getProvisioningProperties(), IdentityApplicationConstants.SCIM.USERS_EP_URL);
        if (scimUserEndpointProperty == null) {
            Property property = new Property();
            property.setName(IdentityApplicationConstants.SCIM.USERS_EP_URL);
            property.setValue(scimUsersEndpoint);
            propertiesList.add(property);
        } else if (!scimUsersEndpoint.equalsIgnoreCase(scimUserEndpointProperty.getValue())) {
            scimUserEndpointProperty.setValue(scimUsersEndpoint);
        }
        Property scimGroupEndpointProperty = IdentityApplicationManagementUtil.getProperty(scimProvConn
                .getProvisioningProperties(), IdentityApplicationConstants.SCIM.GROUPS_EP_URL);
        if (scimGroupEndpointProperty == null) {
            Property property = new Property();
            property.setName(IdentityApplicationConstants.SCIM.GROUPS_EP_URL);
            property.setValue(scimGroupsEndpoint);
            propertiesList.add(property);
        } else if (!scimGroupsEndpoint.equalsIgnoreCase(scimGroupEndpointProperty.getValue())) {
            scimGroupEndpointProperty.setValue(scimGroupsEndpoint);
        }

        Property scim2UserEndpointProperty = IdentityApplicationManagementUtil.getProperty(scimProvConn
                .getProvisioningProperties(), IdentityApplicationConstants.SCIM2.USERS_EP_URL);
        if (scim2UserEndpointProperty == null) {
            Property property = new Property();
            property.setName(IdentityApplicationConstants.SCIM2.USERS_EP_URL);
            property.setValue(scim2UsersEndpoint);
            propertiesList.add(property);
        } else if (!scim2UsersEndpoint.equalsIgnoreCase(scim2UserEndpointProperty.getValue())) {
            scim2UserEndpointProperty.setValue(scim2UsersEndpoint);
        }
        Property scim2GroupEndpointProperty = IdentityApplicationManagementUtil.getProperty(scimProvConn
                .getProvisioningProperties(), IdentityApplicationConstants.SCIM2.GROUPS_EP_URL);
        if (scim2GroupEndpointProperty == null) {
            Property property = new Property();
            property.setName(IdentityApplicationConstants.SCIM2.GROUPS_EP_URL);
            property.setValue(scim2GroupsEndpoint);
            propertiesList.add(property);
        } else if (!scim2GroupsEndpoint.equalsIgnoreCase(scim2GroupEndpointProperty.getValue())) {
            scim2GroupEndpointProperty.setValue(scim2GroupsEndpoint);
        }
        scimProvConn.setProvisioningProperties(propertiesList.toArray(new Property[propertiesList.size()]));
        identityProvider.setProvisioningConnectorConfigs(new ProvisioningConnectorConfig[]{scimProvConn});

        return identityProvider;
    }

    private String buildSAMLUrl(String urlFromConfigFile, String tenantDomain, String defaultContext,
                                boolean appendTenantDomainInLegacyMode) throws IdentityProviderManagementException {

        String url = urlFromConfigFile;
        if (StringUtils.isBlank(url)) {
            // Now we need to build the URL based on the default context.
            try {
                url = ServiceURLBuilder.create().addPath(defaultContext).build().getAbsolutePublicURL();
            } catch (URLBuilderException ex) {
                throw new IdentityProviderManagementException("Error while building URL for context: "
                        + defaultContext + " for tenantDomain: " + tenantDomain, ex);
            }
        }
        //Should not append the tenant domain as a query parameter if it is super tenant.
        if (appendTenantDomainInLegacyMode && isNotSuperTenant(tenantDomain)) {
            Map<String, String[]> queryParams = new HashMap<>();
            queryParams.put(MultitenantConstants.TENANT_DOMAIN, new String[] {tenantDomain});

            try {
                url = IdentityUtil.buildQueryUrl(url, queryParams);
            } catch (UnsupportedEncodingException e) {
                throw new IdentityProviderManagementException("Error while building URL for context: "
                        + defaultContext + " for tenantDomain: " + tenantDomain, e);
            }
        }

        return resolveAbsoluteURL(defaultContext, url, tenantDomain);
    }

    private boolean isNotSuperTenant(String tenantDomain) {

        return !StringUtils.equals(tenantDomain, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
    }

    private FederatedAuthenticatorConfig buildSAMLProperties(IdentityProvider identityProvider, String tenantDomain)
            throws IdentityProviderManagementException {

        String samlSSOUrl = buildSAMLUrl(IdentityUtil.getProperty(IdentityConstants.ServerConfig.SSO_IDP_URL),
                tenantDomain, IdPManagementConstants.SAMLSSO, true);

        String samlLogoutUrl = buildSAMLUrl(IdentityUtil.getProperty(IdentityConstants.ServerConfig.SSO_IDP_URL),
                tenantDomain, IdPManagementConstants.SAMLSSO, true);

        String samlECPUrl = buildSAMLUrl(IdentityUtil.getProperty(IdentityConstants.ServerConfig.SAML_ECP_URL),
                tenantDomain, IdPManagementConstants.SAML_ECP_URL, true);

        String samlArtifactUrl = buildSAMLUrl(IdentityUtil.getProperty(IdentityConstants.ServerConfig.SSO_ARTIFACT_URL),
                tenantDomain, IdPManagementConstants.SSO_ARTIFACT_URL, false);

        FederatedAuthenticatorConfig samlFederatedAuthConfig = IdentityApplicationManagementUtil
                .getFederatedAuthenticator(identityProvider.getFederatedAuthenticatorConfigs(),
                        IdentityApplicationConstants.Authenticator.SAML2SSO.NAME);
        if (samlFederatedAuthConfig == null) {
            samlFederatedAuthConfig = new FederatedAuthenticatorConfig();
            samlFederatedAuthConfig.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.NAME);
        }

        List<Property> propertiesList = new ArrayList<>();

        Property samlSSOUrlProperty = resolveFedAuthnProperty(samlSSOUrl, samlFederatedAuthConfig,
                IdentityApplicationConstants.Authenticator.SAML2SSO.SSO_URL);
        propertiesList.add(samlSSOUrlProperty);

        Property samlLogoutUrlProperty = resolveFedAuthnProperty(samlLogoutUrl, samlFederatedAuthConfig,
                IdentityApplicationConstants.Authenticator.SAML2SSO.LOGOUT_REQ_URL);
        propertiesList.add(samlLogoutUrlProperty);

        Property samlECPUrlProperty = resolveFedAuthnProperty(samlECPUrl, samlFederatedAuthConfig,
                IdentityApplicationConstants.Authenticator.SAML2SSO.ECP_URL);
        propertiesList.add(samlECPUrlProperty);

        Property samlArtifactUrlProperty = resolveFedAuthnProperty(samlArtifactUrl, samlFederatedAuthConfig,
                IdentityApplicationConstants.Authenticator.SAML2SSO.ARTIFACT_RESOLVE_URL);
        propertiesList.add(samlArtifactUrlProperty);

        Property idPEntityIdProperty =
                IdentityApplicationManagementUtil.getProperty(samlFederatedAuthConfig.getProperties(),
                        IdentityApplicationConstants.Authenticator.SAML2SSO.IDP_ENTITY_ID);
        if (idPEntityIdProperty == null) {
            idPEntityIdProperty = new Property();
            idPEntityIdProperty.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.IDP_ENTITY_ID);
            idPEntityIdProperty.setValue(IdPManagementUtil.getResidentIdPEntityId());
        }
        propertiesList.add(idPEntityIdProperty);

        // Add SSO URL as a destination URL if not already available.
        addSSOUrlAsDestinationUrl(samlFederatedAuthConfig, samlSSOUrl, propertiesList);

        for (Property property : samlFederatedAuthConfig.getProperties()) {
            if (property != null &&
                    !IdentityApplicationConstants.Authenticator.SAML2SSO.SSO_URL.equals(property.getName()) &&
                    !IdentityApplicationConstants.Authenticator.SAML2SSO.LOGOUT_REQ_URL.equals(property.getName()) &&
                    !IdentityApplicationConstants.Authenticator.SAML2SSO.ECP_URL.equals(property.getName()) &&
                    !IdentityApplicationConstants.Authenticator.SAML2SSO.IDP_ENTITY_ID.equals(property.getName())) {
                propertiesList.add(property);
            }
        }

        Property samlMetadataValidityPeriodProperty =
                IdentityApplicationManagementUtil.getProperty(samlFederatedAuthConfig.
                        getProperties(), IdentityApplicationConstants.Authenticator.SAML2SSO.
                        SAML_METADATA_VALIDITY_PERIOD);
        if (samlMetadataValidityPeriodProperty == null) {
            samlMetadataValidityPeriodProperty = new Property();
            samlMetadataValidityPeriodProperty.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.
                    SAML_METADATA_VALIDITY_PERIOD);
            samlMetadataValidityPeriodProperty.setValue(IdentityApplicationConstants.Authenticator.SAML2SSO.
                    SAML_METADATA_VALIDITY_PERIOD_DEFAULT);
        }
        propertiesList.add(samlMetadataValidityPeriodProperty);
        Property samlMetadataSigningEnabledProperty =
                IdentityApplicationManagementUtil.getProperty(samlFederatedAuthConfig.
                        getProperties(), IdentityApplicationConstants.Authenticator.SAML2SSO.
                        SAML_METADATA_SIGNING_ENABLED);
        if (samlMetadataSigningEnabledProperty == null) {
            samlMetadataSigningEnabledProperty = new Property();
            samlMetadataSigningEnabledProperty.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.
                    SAML_METADATA_SIGNING_ENABLED);
            samlMetadataSigningEnabledProperty.setValue(IdentityApplicationConstants.Authenticator.SAML2SSO.
                    SAML_METADATA_SIGNING_ENABLED_DEFAULT);
        }
        propertiesList.add(samlMetadataSigningEnabledProperty);
        Property samlAuthnRequestSigningProperty =
                IdentityApplicationManagementUtil.getProperty(samlFederatedAuthConfig.
                        getProperties(), IdentityApplicationConstants.Authenticator.SAML2SSO.
                        SAML_METADATA_AUTHN_REQUESTS_SIGNING_ENABLED);
        if (samlAuthnRequestSigningProperty == null) {
            samlAuthnRequestSigningProperty = new Property();
            samlAuthnRequestSigningProperty.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.
                    SAML_METADATA_AUTHN_REQUESTS_SIGNING_ENABLED);
            samlAuthnRequestSigningProperty.setValue(IdentityApplicationConstants.Authenticator.SAML2SSO.
                    SAML_METADATA_AUTHN_REQUESTS_SIGNING_DEFAULT);
        }
        propertiesList.add(samlAuthnRequestSigningProperty);
        samlFederatedAuthConfig.setProperties(propertiesList.toArray(new Property[propertiesList.size()]));
        return samlFederatedAuthConfig;
    }

    private void addSSOUrlAsDestinationUrl(FederatedAuthenticatorConfig federatedAuthenticatorConfig,
                                           String ssoUrl,
                                           List<Property> propertiesList) {

        // First find the available configured destination URLs.
        List<Property> destinationURLs = Arrays.stream(federatedAuthenticatorConfig.getProperties())
                .filter(property -> property.getName()
                        .startsWith(IdentityApplicationConstants.Authenticator.SAML2SSO.DESTINATION_URL_PREFIX))
                .collect(Collectors.toList());

        // Check whether the SSO URL is already available as a destination URL
        boolean isSAMLSSOUrlNotPresentAsDestination = destinationURLs.stream()
                .noneMatch(x -> StringUtils.equals(ssoUrl, x.getValue()));

        if (isSAMLSSOUrlNotPresentAsDestination) {
            // There are no destination properties matching the default SSO URL.
            int propertyNameIndex = destinationURLs.size() + 1;
            Property destinationURLProperty = buildDestinationURLProperty(ssoUrl, propertyNameIndex);
            propertiesList.add(destinationURLProperty);
        }
    }

    private Property buildDestinationURLProperty(String destinationURL, int index) {

        Property destinationURLProperty = new Property();
        destinationURLProperty.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.DESTINATION_URL_PREFIX +
                IdentityApplicationConstants.MULTIVALUED_PROPERTY_CHARACTER + index);
        destinationURLProperty.setValue(destinationURL);
        return destinationURLProperty;
    }

    private Property resolveFedAuthnProperty(String epUrl, FederatedAuthenticatorConfig fedAuthnConfig,
                                             String propertyName) {

        Property property =
                IdentityApplicationManagementUtil.getProperty(fedAuthnConfig.getProperties(), propertyName);

        if (property == null || IdentityTenantUtil.isTenantQualifiedUrlsEnabled()) {
            // In tenant qualified mode we have to always give send the calculated URL and not the value stored in DB.
            property = new Property();
            property.setName(propertyName);
            // Set the calculated SAML endpoint URL.
            property.setValue(epUrl);
        }
        return property;
    }

    /**
     * Add Resident Identity provider for a given tenant.
     *
     * @param identityProvider <code>IdentityProvider</code>
     * @param tenantDomain     Tenant domain whose resident IdP is requested
     * @throws IdentityProviderManagementException Error when adding Resident Identity Provider
     */
    @Override
    public void addResidentIdP(IdentityProvider identityProvider, String tenantDomain)
            throws IdentityProviderManagementException {

        // invoking the pre listeners
        Collection<IdentityProviderMgtListener> listeners = IdPManagementServiceComponent.getIdpMgtListeners();
        for (IdentityProviderMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPreAddResidentIdP(identityProvider, tenantDomain)) {
                return;
            }
        }

        if (identityProvider.getFederatedAuthenticatorConfigs() == null) {
            identityProvider.setFederatedAuthenticatorConfigs(new FederatedAuthenticatorConfig[0]);
        }
        FederatedAuthenticatorConfig saml2SSOResidentAuthenticatorConfig = IdentityApplicationManagementUtil
                .getFederatedAuthenticator(identityProvider.getFederatedAuthenticatorConfigs(),
                        IdentityApplicationConstants.Authenticator.SAML2SSO.NAME);
        if (saml2SSOResidentAuthenticatorConfig == null) {
            saml2SSOResidentAuthenticatorConfig = new FederatedAuthenticatorConfig();
            saml2SSOResidentAuthenticatorConfig.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.NAME);
        }
        if (saml2SSOResidentAuthenticatorConfig.getProperties() == null) {
            saml2SSOResidentAuthenticatorConfig.setProperties(new Property[0]);
        }

        boolean idPEntityIdAvailable = false;
        for (Property property : saml2SSOResidentAuthenticatorConfig.getProperties()) {
            if (IdentityApplicationConstants.Authenticator.SAML2SSO.IDP_ENTITY_ID.equals(property.getName())) {
                idPEntityIdAvailable = true;
            }
        }
        if (!idPEntityIdAvailable) {
            Property property = new Property();
            property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.IDP_ENTITY_ID);
            int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
            if (tenantId != MultitenantConstants.SUPER_TENANT_ID && IdentityTenantUtil.isTenantQualifiedUrlsEnabled()) {
                property.setValue(IdPManagementUtil.getResidentIdPEntityId() + "/t/" + tenantDomain);
            } else {
                property.setValue(IdPManagementUtil.getResidentIdPEntityId());
            }

            if (saml2SSOResidentAuthenticatorConfig.getProperties().length > 0) {
                List<Property> properties = Arrays.asList(saml2SSOResidentAuthenticatorConfig.getProperties());
                properties.add(property);
                saml2SSOResidentAuthenticatorConfig.setProperties((Property[]) properties.toArray());
            } else {
                saml2SSOResidentAuthenticatorConfig.setProperties(new Property[]{property});
            }
        }
        Property samlMetadataValidityPeriodProperty = new Property();
        String samlMetadataValidityPeriod = IdentityUtil.getProperty(IdentityConstants.ServerConfig.
                SAML_METADATA_VALIDITY_PERIOD);
        if (StringUtils.isBlank(samlMetadataValidityPeriod)) {
            samlMetadataValidityPeriod = IdentityApplicationConstants.Authenticator.SAML2SSO.
                    SAML_METADATA_VALIDITY_PERIOD_DEFAULT;
        } else if (!StringUtils.isNumeric(samlMetadataValidityPeriod) ||
                Integer.parseInt(samlMetadataValidityPeriod) <= 0) {
            log.warn("SAMLMetadataValidityPeriod in identity.xml should be a numeric value " +
                    "hence defaulting to value: " + IdentityApplicationConstants.Authenticator.SAML2SSO.
                    SAML_METADATA_VALIDITY_PERIOD_DEFAULT + "m");
            samlMetadataValidityPeriod = IdentityApplicationConstants.Authenticator.SAML2SSO.
                    SAML_METADATA_VALIDITY_PERIOD_DEFAULT;
        }
        samlMetadataValidityPeriodProperty.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.
                SAML_METADATA_VALIDITY_PERIOD);
        samlMetadataValidityPeriodProperty.setValue(samlMetadataValidityPeriod);
        Property samlMetadataSigningEnabledProperty = new Property();
        String samlMetadataSigningEnabled = IdentityUtil.getProperty(IdentityConstants.ServerConfig.
                SAML_METADATA_SIGNING_ENABLED);
        if (StringUtils.isBlank(samlMetadataSigningEnabled)) {
            log.warn("SAMLMetadataSigningEnabled in identity.xml should be a boolean value");
            samlMetadataSigningEnabled = IdentityApplicationConstants.Authenticator.SAML2SSO.
                    SAML_METADATA_SIGNING_ENABLED_DEFAULT;
        }
        samlMetadataSigningEnabledProperty.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.
                SAML_METADATA_SIGNING_ENABLED);
        samlMetadataSigningEnabledProperty.setValue(samlMetadataSigningEnabled);
        Property samlAuthnRequestSigningProperty = new Property();
        String samlAuthnRequestSigningEnabled = IdentityUtil.getProperty(IdentityConstants.ServerConfig.
                SAML_METADATA_AUTHN_REQUESTS_SIGNING_ENABLED);
        if (StringUtils.isBlank(samlAuthnRequestSigningEnabled)) {
            log.warn("samlAuthnRequestSigningEnabled in identity.xml should be a boolean value");
            samlAuthnRequestSigningEnabled = IdentityApplicationConstants.Authenticator.SAML2SSO.
                    SAML_METADATA_AUTHN_REQUESTS_SIGNING_DEFAULT;
        }
        samlAuthnRequestSigningProperty.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.
                SAML_METADATA_AUTHN_REQUESTS_SIGNING_ENABLED);
        samlAuthnRequestSigningProperty.setValue(samlAuthnRequestSigningEnabled);
        List<Property> propertyList =
                new ArrayList<>(Arrays.asList(saml2SSOResidentAuthenticatorConfig.getProperties()));
        propertyList.add(samlMetadataValidityPeriodProperty);
        propertyList.add(samlMetadataSigningEnabledProperty);
        propertyList.add(samlAuthnRequestSigningProperty);
        Property[] properties = new Property[propertyList.size()];
        properties = propertyList.toArray(properties);
        saml2SSOResidentAuthenticatorConfig.setProperties(properties);

        Property oidcProperty = new Property();
        oidcProperty.setName(OPENID_IDP_ENTITY_ID);
        oidcProperty.setValue(getOIDCResidentIdPEntityId());

        FederatedAuthenticatorConfig oidcAuthenticationConfig = new FederatedAuthenticatorConfig();
        oidcAuthenticationConfig.setProperties(new Property[]{oidcProperty});
        oidcAuthenticationConfig.setName(IdentityApplicationConstants.Authenticator.OIDC.NAME);

        Property passiveStsProperty = new Property();
        passiveStsProperty.setName(IdentityApplicationConstants.Authenticator.PassiveSTS.IDENTITY_PROVIDER_ENTITY_ID);
        passiveStsProperty.setValue(IdPManagementUtil.getResidentIdPEntityId());

        FederatedAuthenticatorConfig passiveStsAuthenticationConfig = new FederatedAuthenticatorConfig();
        passiveStsAuthenticationConfig.setProperties(new Property[]{passiveStsProperty});
        passiveStsAuthenticationConfig.setName(IdentityApplicationConstants.Authenticator.PassiveSTS.NAME);

        FederatedAuthenticatorConfig[] federatedAuthenticatorConfigs = {saml2SSOResidentAuthenticatorConfig,
                passiveStsAuthenticationConfig, oidcAuthenticationConfig};
        identityProvider.setFederatedAuthenticatorConfigs(IdentityApplicationManagementUtil
                .concatArrays(identityProvider.getFederatedAuthenticatorConfigs(), federatedAuthenticatorConfigs));

        IdentityProviderProperty[] idpProperties = new IdentityProviderProperty[2];

        IdentityProviderProperty rememberMeTimeoutProperty = new IdentityProviderProperty();
        String rememberMeTimeout = IdentityUtil.getProperty(IdentityConstants.ServerConfig.REMEMBER_ME_TIME_OUT);
        if (StringUtils.isBlank(rememberMeTimeout) || !StringUtils.isNumeric(rememberMeTimeout) ||
                Integer.parseInt(rememberMeTimeout) <= 0) {
            log.warn("RememberMeTimeout in identity.xml should be a numeric value");
            rememberMeTimeout = IdentityApplicationConstants.REMEMBER_ME_TIME_OUT_DEFAULT;
        }
        rememberMeTimeoutProperty.setName(IdentityApplicationConstants.REMEMBER_ME_TIME_OUT);
        rememberMeTimeoutProperty.setValue(rememberMeTimeout);

        IdentityProviderProperty sessionIdletimeOutProperty = new IdentityProviderProperty();
        String idleTimeout = IdentityUtil.getProperty(IdentityConstants.ServerConfig.SESSION_IDLE_TIMEOUT);
        if (StringUtils.isBlank(idleTimeout) || !StringUtils.isNumeric(idleTimeout) ||
                Integer.parseInt(idleTimeout) <= 0) {
            log.warn("SessionIdleTimeout in identity.xml should be a numeric value");
            idleTimeout = IdentityApplicationConstants.SESSION_IDLE_TIME_OUT_DEFAULT;
        }
        sessionIdletimeOutProperty.setName(IdentityApplicationConstants.SESSION_IDLE_TIME_OUT);
        sessionIdletimeOutProperty.setValue(idleTimeout);

        idpProperties[0] = rememberMeTimeoutProperty;
        idpProperties[1] = sessionIdletimeOutProperty;
        identityProvider.setIdpProperties(idpProperties);

        dao.addIdP(identityProvider, IdentityTenantUtil.getTenantId(tenantDomain), tenantDomain);

        // invoking the post listeners
        for (IdentityProviderMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPostAddResidentIdP(identityProvider, tenantDomain)) {
                return;
            }
        }
    }

    /**
     * Update Resident Identity provider for a given tenant.
     *
     * @param identityProvider <code>IdentityProvider</code>
     * @param tenantDomain     Tenant domain whose resident IdP is requested
     * @throws IdentityProviderManagementException Error when updating Resident Identity Provider
     */
    @Override
    public void updateResidentIdP(IdentityProvider identityProvider, String tenantDomain)
            throws IdentityProviderManagementException {

        IdentityProvider residentIdp = dao.getIdPByName(null, IdentityApplicationConstants.RESIDENT_IDP_RESERVED_NAME,
                IdentityTenantUtil.getTenantId(tenantDomain), tenantDomain);
        Map<String, String> configurationDetails = new HashMap<>();

        for (IdentityProviderProperty property : identityProvider.getIdpProperties()) {
            configurationDetails.put(property.getName(), property.getValue());
        }

        IdentityProviderProperty[] identityMgtProperties = residentIdp.getIdpProperties();
        List<IdentityProviderProperty> newProperties = new ArrayList<>();

        for (IdentityProviderProperty identityMgtProperty : identityMgtProperties) {
            IdentityProviderProperty prop = new IdentityProviderProperty();
            String key = identityMgtProperty.getName();
            prop.setName(key);

            if (configurationDetails.containsKey(key)) {
                prop.setValue(configurationDetails.get(key));
            } else {
                prop.setValue(identityMgtProperty.getValue());
            }

            newProperties.add(prop);
            configurationDetails.remove(key);
        }

        for (Map.Entry<String, String> entry : configurationDetails.entrySet()) {
            IdentityProviderProperty prop = new IdentityProviderProperty();
            prop.setName(entry.getKey());
            prop.setValue(entry.getValue());
            newProperties.add(prop);
        }

        identityProvider.setIdpProperties(newProperties.toArray(new IdentityProviderProperty[newProperties.size()]));

        for (IdentityProviderProperty idpProp : identityProvider.getIdpProperties()) {
            if (StringUtils.equals(idpProp.getName(), IdentityApplicationConstants.SESSION_IDLE_TIME_OUT)) {
                if (StringUtils.isBlank(idpProp.getValue()) || !StringUtils.isNumeric(idpProp.getValue()) ||
                        Integer.parseInt(idpProp.getValue().trim()) <= 0) {
                    throw new IdentityProviderManagementException(IdentityApplicationConstants.SESSION_IDLE_TIME_OUT
                            + " of ResidentIdP should be a numeric value greater than 0 ");
                }
            } else if (StringUtils.equals(idpProp.getName(), IdentityApplicationConstants.REMEMBER_ME_TIME_OUT)) {
                if (StringUtils.isBlank(idpProp.getValue()) || !StringUtils.isNumeric(idpProp.getValue()) ||
                        Integer.parseInt(idpProp.getValue().trim()) <= 0) {
                    throw new IdentityProviderManagementException(IdentityApplicationConstants.REMEMBER_ME_TIME_OUT
                            + " of ResidentIdP should be a numeric value greater than 0 ");
                }
            } else if (StringUtils.equals(idpProp.getName(), IdentityApplicationConstants.Authenticator.SAML2SSO.
                    SAML_METADATA_VALIDITY_PERIOD)) {
                if (StringUtils.isBlank(idpProp.getValue()) || !StringUtils.isNumeric(idpProp.getValue()) ||
                        Integer.parseInt(idpProp.getValue().trim()) <= 0) {
                    throw new IdentityProviderManagementException(IdentityApplicationConstants.Authenticator.SAML2SSO.
                            SAML_METADATA_VALIDITY_PERIOD +
                            " of ResidentIdP should be a numeric value greater than 0 ");
                }
            } else if (StringUtils.equals(idpProp.getName(), IdentityApplicationConstants.Authenticator.SAML2SSO.
                    SAML_METADATA_SIGNING_ENABLED)) {
                if (StringUtils.isBlank(idpProp.getValue())) {
                    throw new IdentityProviderManagementException(IdentityApplicationConstants.Authenticator.SAML2SSO.
                            SAML_METADATA_SIGNING_ENABLED + " of ResidentIdP should be a boolean value ");
                } else if (StringUtils.equals(idpProp.getName(), IdentityApplicationConstants.Authenticator.SAML2SSO.
                        SAML_METADATA_AUTHN_REQUESTS_SIGNING_ENABLED)) {
                    if (StringUtils.isBlank(idpProp.getValue())) {
                        throw new IdentityProviderManagementException(IdentityApplicationConstants.Authenticator.SAML2SSO.
                                SAML_METADATA_AUTHN_REQUESTS_SIGNING_ENABLED + " of ResidentIdP should be a boolean value ");
                    }
                }

            }
        }
        // invoking the pre listeners
        Collection<IdentityProviderMgtListener> listeners = IdPManagementServiceComponent.getIdpMgtListeners();
        for (IdentityProviderMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPreUpdateResidentIdP(identityProvider, tenantDomain)) {
                return;
            }
        }

        if (identityProvider.getFederatedAuthenticatorConfigs() == null) {
            identityProvider.setFederatedAuthenticatorConfigs(new FederatedAuthenticatorConfig[0]);
        }

        IdentityProvider currentIdP = IdentityProviderManager.getInstance().getIdPByName(
                IdentityApplicationConstants.RESIDENT_IDP_RESERVED_NAME, tenantDomain, true);

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        validateUpdateOfIdPEntityId(currentIdP.getFederatedAuthenticatorConfigs(),
                identityProvider.getFederatedAuthenticatorConfigs(), tenantId, tenantDomain);

        dao.updateIdP(identityProvider, currentIdP, tenantId, tenantDomain);

        // invoking the post listeners
        for (IdentityProviderMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPostUpdateResidentIdP(identityProvider, tenantDomain)) {
                return;
            }
        }
    }

    /**
     * Retrieves registered Identity finally {
     * break;
     * }providers for a given tenant
     *
     * @param tenantDomain Tenant domain whose IdP names are requested
     * @return Set of <code>IdentityProvider</code>. IdP names, primary IdP and home realm
     * identifiers of each IdP
     * @throws IdentityProviderManagementException Error when getting list of Identity Providers
     */
    @Override
    public List<IdentityProvider> getIdPs(String tenantDomain)
            throws IdentityProviderManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        return dao.getIdPs(null, tenantId, tenantDomain);

    }

    /**
     * Get all basic identity provider information.
     *
     * @param limit        limit per page.
     * @param offset       offset value.
     * @param filter       filter value for IdP search.
     * @param sortOrder    order of IdP ASC/DESC.
     * @param sortBy       the column value need to sort.
     * @param tenantDomain tenant domain whose IdP names are requested.
     * @return Identity Provider's Basic Information array {@link IdpSearchResult}.
     * @throws IdentityProviderManagementException Server/client related error when getting list of Identity Providers.
     * @deprecated use {@link #getIdPs(Integer, Integer, String, String, String, String, List)}
     */
    @Override
    public IdpSearchResult getIdPs(Integer limit, Integer offset, String filter, String sortOrder, String sortBy,
                                   String tenantDomain)
            throws IdentityProviderManagementException {

        return getIdPs(limit, offset, filter, sortOrder, sortBy, tenantDomain, new ArrayList<>());
    }

    /**
     * Get all identity provider's Basic information along with additionally requested information depending on the
     * requiredAttributes.
     *
     * @param limit              Limit per page.
     * @param offset             Offset value.
     * @param filter             Filter value for IdP search.
     * @param sortOrder          Order of IdP ASC/DESC.
     * @param sortBy             The column value need to sort.
     * @param tenantDomain       TenantDomain of the user.
     * @param requiredAttributes Required attributes which needs to be return.
     * @return Identity Provider's Basic Information array along with requested attribute
     * information{@link IdpSearchResult}.
     * @throws IdentityProviderManagementException Server/client related error when getting list of Identity Providers.
     */
    @Override
    public IdpSearchResult getIdPs(Integer limit, Integer offset, String filter, String sortOrder, String sortBy,
                                   String tenantDomain, List<String> requiredAttributes)
            throws IdentityProviderManagementException {

        IdpSearchResult result = new IdpSearchResult();
        List<ExpressionNode> expressionNodes = getExpressionNodes(filter);
        setParameters(limit, offset, sortOrder, sortBy, filter, result);
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        result.setTotalIDPCount(dao.getTotalIdPCount(tenantId, expressionNodes));
        result.setIdpList(dao.getPaginatedIdPsSearch(tenantId, expressionNodes, result.getLimit(), result.getOffSet(),
                result.getSortOrder(), result.getSortBy(), requiredAttributes));
        return result;
    }

    /**
     * Get basic information of identity providers along with additionally requested information.
     *
     * @param limit              The limit per page.
     * @param offset             The offset value.
     * @param sortOrder          The order of IdP ASC/DESC.
     * @param sortBy             The column value need to sort.
     * @param tenantDomain       The tenant domain of the user.
     * @param requiredAttributes The required attributes which needs to be returned.
     * @param expressionNodes    The list of filters.
     * @return The basic information of identity providers along with requested attributes.
     * @throws IdentityProviderManagementException Server/client related errors when getting list of identity providers.
     */
    @Override
    public IdpSearchResult getIdPs(Integer limit, Integer offset, String sortOrder, String sortBy, String tenantDomain,
                                   List<String> requiredAttributes, List<ExpressionNode> expressionNodes)
            throws IdentityProviderManagementException {

        IdpSearchResult result = new IdpSearchResult();
        setParameters(limit, offset, null, sortBy, sortBy, result);
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        result.setTotalIDPCount(dao.getTotalIdPCount(tenantId, expressionNodes));
        result.setIdpList(dao.getPaginatedIdPsSearch(tenantId, expressionNodes, result.getLimit(), result.getOffSet(),
                result.getSortOrder(), result.getSortBy(), requiredAttributes));
        return result;
    }

    /**
     * Get all basic identity provider information.
     *
     * @param filter       filter value for IdP search.
     * @param tenantDomain tenant domain whose IdP names are requested.
     * @return filtered idp count.
     * @throws IdentityProviderManagementException Error while getting Identity  Providers count.IdentityProviderManagementService.java
     */
    @Override
    public int getTotalIdPCount(String filter, String tenantDomain) throws IdentityProviderManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        List<ExpressionNode> expressionNodes = getExpressionNodes(filter);
        return dao.getTotalIdPCount(tenantId, expressionNodes);
    }

    /**
     * Get the filter node as a list.
     *
     * @param filter value of the filter.
     * @return node tree.
     * @throws IdentityProviderManagementClientException Error when validate filters.
     */
    private List<ExpressionNode> getExpressionNodes(String filter) throws IdentityProviderManagementClientException {

        // Filter example : name sw "te" and name ew "st" and isEnabled eq "true".
        List<ExpressionNode> expressionNodes = new ArrayList<>();
        FilterTreeBuilder filterTreeBuilder;
        try {
            if (StringUtils.isNotBlank(filter)) {
                filterTreeBuilder = new FilterTreeBuilder(filter);
                Node rootNode = filterTreeBuilder.buildTree();
                setExpressionNodeList(rootNode, expressionNodes);
            }
        } catch (IOException | IdentityException e) {
            String message = "Error occurred while validate filter, filter: " + filter;
            throw IdPManagementUtil
                    .handleClientException(IdPManagementConstants.ErrorMessage.ERROR_CODE_RETRIEVE_IDP, message, e);
        }
        return expressionNodes;
    }

    /**
     * Set the node values as list of expression.
     *
     * @param node       filter node.
     * @param expression list of expression.
     * @throws IdentityProviderManagementClientException Error when passing invalid filter.
     */
    private void setExpressionNodeList(Node node, List<ExpressionNode> expression)
            throws IdentityProviderManagementClientException {

        if (node instanceof ExpressionNode) {
            if (StringUtils.isNotBlank(((ExpressionNode) node).getAttributeValue())) {
                if (((ExpressionNode) node).getAttributeValue().contains(IdPManagementConstants.IDP_IS_ENABLED)) {
                    if ("true".contains(((ExpressionNode) node).getValue())) {
                        ((ExpressionNode) node).setValue(IdPManagementConstants.IS_TRUE_VALUE);
                    } else if ("false".contains(((ExpressionNode) node).getValue())) {
                        ((ExpressionNode) node).setValue(IdPManagementConstants.IS_FALSE_VALUE);
                    } else {
                        String message = "Invalid value: " + ((ExpressionNode) node).getValue() + "is passed for " +
                                "'isEnabled' attribute in the filter. It should be 'true' or 'false'";
                        throw IdPManagementUtil
                                .handleClientException(IdPManagementConstants.ErrorMessage.ERROR_CODE_RETRIEVE_IDP,
                                        message);
                    }
                }
            }
            expression.add((ExpressionNode) node);
        } else if (node instanceof OperationNode) {
            setExpressionNodeList(node.getLeftNode(), expression);
            setExpressionNodeList(node.getRightNode(), expression);
        }
    }

    /**
     * Set the passing parameters as result.
     *
     * @param limit     page limit.
     * @param offset    offset value.
     * @param filter    filter value for IdP search.
     * @param sortOrder order of IdP(ASC/DESC).
     * @param sortBy    the column value need to sort.
     * @param result    result object.
     * @throws IdentityProviderManagementClientException Error while set offset.
     */
    private void setParameters(Integer limit, Integer offset, String filter, String sortOrder, String sortBy,
                               IdpSearchResult result) throws IdentityProviderManagementClientException {

        result.setLimit(validateLimit(limit));
        result.setOffSet(validateOffset(offset));
        result.setSortBy(validateSortBy(sortBy));
        result.setSortOrder(validateSortOrder(sortOrder));
        result.setFilter(filter);
    }

    /**
     * Validate sortBy.
     *
     * @param sortBy sortBy attribute.
     * @return Validated sortOrder and sortBy.
     */
    private String validateSortBy(String sortBy) {

        if (StringUtils.isBlank(sortBy)) {
            if (log.isDebugEnabled()) {
                log.debug("sortBy attribute is empty. Therefore we set the default sortBy attribute. sortBy" +
                        IdPManagementConstants.DEFAULT_SORT_BY);
            }
            return IdPManagementConstants.DEFAULT_SORT_BY;
        }
        switch (sortBy) {
            case IdPManagementConstants.IDP_NAME:
                sortBy = IdPManagementConstants.NAME;
                break;
            case IdPManagementConstants.IDP_HOME_REALM_ID:
                sortBy = IdPManagementConstants.HOME_REALM_ID;
                break;
            default:
                sortBy = IdPManagementConstants.DEFAULT_SORT_BY;
                if (log.isDebugEnabled()) {
                    log.debug("sortBy attribute is incorrect. Therefore we set the default sortBy attribute. " +
                            "sortBy: " + IdPManagementConstants.DEFAULT_SORT_BY);
                }
                break;
        }
        return sortBy;
    }

    /**
     * Validate sortOrder.
     *
     * @param sortOrder sortOrder ASC/DESC.
     * @return Validated sortOrder and sortBy.
     */
    private String validateSortOrder(String sortOrder) {

        if (StringUtils.isBlank(sortOrder)) {
            sortOrder = IdPManagementConstants.DEFAULT_SORT_ORDER;
            if (log.isDebugEnabled()) {
                log.debug("sortOrder is empty. Therefore we set the default sortOrder value as ASC. SortOrder: " +
                        sortOrder);
            }
        } else if (sortOrder.equals(IdPManagementConstants.DESC_SORT_ORDER)) {
            sortOrder = IdPManagementConstants.DESC_SORT_ORDER;
        } else if (sortOrder.equals(IdPManagementConstants.ASC_SORT_ORDER)) {
            sortOrder = IdPManagementConstants.ASC_SORT_ORDER;
        } else {
            sortOrder = IdPManagementConstants.DEFAULT_SORT_ORDER;
            if (log.isDebugEnabled()) {
                log.debug("sortOrder is incorrect. Therefore we set the default sortOrder value as ASC. SortOrder: "
                        + sortOrder);
            }
        }
        return sortOrder;
    }

    /**
     * Validate limit.
     *
     * @param limit given limit value.
     * @return validated limit and offset value.
     */
    private int validateLimit(Integer limit) throws IdentityProviderManagementClientException {

        if (limit == null) {
            if (log.isDebugEnabled()) {
                log.debug("Given limit is null. Therefore we get the default limit from " +
                        "identity.xml.");
            }
            limit = IdentityUtil.getDefaultItemsPerPage();
        }
        if (limit < 0) {
            String message = "Given limit: " + limit + " is a negative value.";
            throw IdPManagementUtil.handleClientException(IdPManagementConstants.ErrorMessage.ERROR_CODE_RETRIEVE_IDP,
                    message);
        }

        int maximumItemsPerPage = IdentityUtil.getMaximumItemPerPage();
        if (limit > maximumItemsPerPage) {
            if (log.isDebugEnabled()) {
                log.debug("Given limit exceed the maximum limit. Therefore we get the default limit from " +
                        "identity.xml. limit: " + maximumItemsPerPage);
            }
            limit = maximumItemsPerPage;
        }
        return limit;
    }

    /**
     * Validate offset.
     *
     * @param offset given offset value.
     * @return validated limit and offset value.
     * @throws IdentityProviderManagementClientException Error while set offset
     */
    private int validateOffset(Integer offset) throws IdentityProviderManagementClientException {

        if (offset == null) {
            // Return first page offset.
            offset = 0;
        }

        if (offset < 0) {
            String message = "Invalid offset applied. Offset should not negative. offSet: " +
                    offset;
            throw IdPManagementUtil.handleClientException(IdPManagementConstants.ErrorMessage.ERROR_CODE_RETRIEVE_IDP,
                    message);
        }
        return offset;
    }

    /**
     * Retrieves registered Identity finally {
     * break;
     * }providers for a given tenant
     *
     * @param tenantDomain Tenant domain whose IdP names are requested
     * @return Set of <code>IdentityProvider</code>. IdP names, primary IdP and home realm
     * identifiers of each IdP
     * @throws IdentityProviderManagementException Error when getting list of Identity Providers
     */
    @Override
    public List<IdentityProvider> getIdPsSearch(String tenantDomain, String filter)
            throws IdentityProviderManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        return dao.getIdPsSearch(null, tenantId, tenantDomain, filter);
    }

    /**
     * Retrieves registered Enabled Identity providers for a given tenant
     *
     * @param tenantDomain Tenant domain whose IdP names are requested
     * @return Set of <code>IdentityProvider</code>. IdP names, primary IdP and home realm
     * identifiers of each IdP
     * @throws IdentityProviderManagementException Error when getting list of Identity Providers
     */
    @Override
    public List<IdentityProvider> getEnabledIdPs(String tenantDomain)
            throws IdentityProviderManagementException {

        List<IdentityProvider> enabledIdentityProviders = new ArrayList<IdentityProvider>();
        List<IdentityProvider> identityProviers = getIdPs(tenantDomain);

        for (IdentityProvider idp : identityProviers) {
            if (idp.isEnable()) {
                enabledIdentityProviders.add(idp);
            }
        }
        return enabledIdentityProviders;

    }

    /**
     * @param idPName
     * @param tenantDomain
     * @param ignoreFileBasedIdps
     * @return
     * @throws IdentityProviderManagementException
     */
    @Override
    public IdentityProvider getIdPByName(String idPName, String tenantDomain,
                                         boolean ignoreFileBasedIdps) throws IdentityProviderManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        if (StringUtils.isEmpty(idPName)) {
            String msg = "Invalid argument: Identity Provider Name value is empty";
            throw new IdentityProviderManagementException(msg);
        }

        IdentityProvider identityProvider = dao.getIdPByName(null, idPName, tenantId, tenantDomain);

        if (!ignoreFileBasedIdps) {

            if (identityProvider == null) {
                identityProvider = new FileBasedIdPMgtDAO().getIdPByName(idPName, tenantDomain);
            }

            if (identityProvider == null) {
                identityProvider = IdPManagementServiceComponent.getFileBasedIdPs().get(
                        IdentityApplicationConstants.DEFAULT_IDP_CONFIG);
            }
        }

        return identityProvider;
    }

    @Override
    public IdentityProvider getIdPById(String id, String tenantDomain,
                                       boolean ignoreFileBasedIdps) throws IdentityProviderManagementException {

        if (StringUtils.isEmpty(id)) {
            String msg = "Invalid argument: Identity Provider ID value is empty";
            throw new IdentityProviderManagementException(msg);
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        Integer intId;
        IdentityProvider identityProvider = null;

        try {
            intId = Integer.parseInt(id);
            identityProvider = dao.getIdPById(null, intId, tenantId, tenantDomain);
        } catch (NumberFormatException e) {
            // Ignore this.
        }
        if (!ignoreFileBasedIdps) {

            if (identityProvider == null) {
                identityProvider = new FileBasedIdPMgtDAO().getIdPByName(id, tenantDomain);
            }

            if (identityProvider == null) {
                identityProvider = IdPManagementServiceComponent.getFileBasedIdPs().get(
                        IdentityApplicationConstants.DEFAULT_IDP_CONFIG);
            }
        }

        return identityProvider;
    }

    @Override
    public IdentityProvider getIdPByResourceId(String resourceId, String tenantDomain, boolean
            ignoreFileBasedIdps) throws IdentityProviderManagementException {

        validateGetIdPInputValues(resourceId);
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        return dao.getIdPByResourceId(resourceId, tenantId, tenantDomain);
    }

    @Override
    public String getIdPNameByResourceId(String resourceId) throws IdentityProviderManagementException {

        validateGetIdPInputValues(resourceId);
        return dao.getIdPNameByResourceId(resourceId);
    }

    /**
     * @param idPName
     * @param tenantDomain
     * @param ignoreFileBasedIdps
     * @return
     * @throws IdentityProviderManagementException
     */
    @Override
    public IdentityProvider getEnabledIdPByName(String idPName, String tenantDomain,
                                                boolean ignoreFileBasedIdps)
            throws IdentityProviderManagementException {

        IdentityProvider idp = getIdPByName(idPName, tenantDomain, ignoreFileBasedIdps);
        if (idp != null && idp.isEnable()) {
            return idp;
        }
        return null;
    }

    /**
     * Retrieves Identity provider information about a given tenant by Identity Provider name
     *
     * @param idPName      Unique name of the Identity provider of whose information is requested
     * @param tenantDomain Tenant domain whose information is requested
     * @return <code>IdentityProvider</code> Identity Provider information
     * @throws IdentityProviderManagementException Error when getting Identity Provider
     *                                             information by IdP name
     */
    @Override
    public IdentityProvider getIdPByName(String idPName, String tenantDomain)
            throws IdentityProviderManagementException {

        return getIdPByName(idPName, tenantDomain, false);
    }

    @Override
    public IdentityProvider getIdPById(String id, String tenantDomain) throws IdentityProviderManagementException {

        return getIdPById(id, tenantDomain, false);
    }

    /**
     * @param property     IDP authenticator property (E.g.: IdPEntityId)
     * @param value        Value associated with given Property
     * @param tenantDomain
     * @return <code>IdentityProvider</code> Identity Provider information
     * @throws IdentityProviderManagementException Error when getting Identity Provider
     *                                             information by authenticator property value
     */
    @Override
    public IdentityProvider getIdPByAuthenticatorPropertyValue(String property, String value, String tenantDomain,
                                                               boolean ignoreFileBasedIdps)
            throws IdentityProviderManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        if (StringUtils.isEmpty(property) || StringUtils.isEmpty(value)) {
            String msg = "Invalid argument: Authenticator property or property value is empty";
            throw new IdentityProviderManagementException(msg);
        }

        IdentityProvider identityProvider = dao.getIdPByAuthenticatorPropertyValue(
                null, property, value, tenantId, tenantDomain);

        if (identityProvider == null && !ignoreFileBasedIdps) {
            identityProvider = new FileBasedIdPMgtDAO()
                    .getIdPByAuthenticatorPropertyValue(property, value, tenantDomain);
        }

        return identityProvider;
    }

    /**
     * @param property      IDP authenticator property (E.g.: IdPEntityId)
     * @param value         Value associated with given Property
     * @param tenantDomain
     * @param authenticator
     * @return <code>IdentityProvider</code> Identity Provider information
     * @throws IdentityProviderManagementException Error when getting Identity Provider
     *                                             information by authenticator property value
     */
    public IdentityProvider getIdPByAuthenticatorPropertyValue(String property, String value, String tenantDomain,
                                                               String authenticator, boolean ignoreFileBasedIdps)
            throws IdentityProviderManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        if (StringUtils.isEmpty(property) || StringUtils.isEmpty(value) || StringUtils.isEmpty(authenticator)) {
            String msg = "Invalid argument: Authenticator property, property value or authenticator name is empty";
            throw new IdentityProviderManagementException(msg);
        }

        IdentityProvider identityProvider = dao.getIdPByAuthenticatorPropertyValue(
                null, property, value, authenticator, tenantId, tenantDomain);

        if (identityProvider == null && !ignoreFileBasedIdps) {
            identityProvider = new FileBasedIdPMgtDAO()
                    .getIdPByAuthenticatorPropertyValue(property, value, tenantDomain, authenticator);
        }

        return identityProvider;
    }

    /**
     * Retrieves Enabled Identity provider information about a given tenant by Identity Provider name
     *
     * @param idPName      Unique name of the Identity provider of whose information is requested
     * @param tenantDomain Tenant domain whose information is requested
     * @return <code>IdentityProvider</code> Identity Provider information
     * @throws IdentityProviderManagementException Error when getting Identity Provider
     *                                             information by IdP name
     */
    @Override
    public IdentityProvider getEnabledIdPByName(String idPName, String tenantDomain)
            throws IdentityProviderManagementException {

        IdentityProvider idp = getIdPByName(idPName, tenantDomain);
        if (idp != null && idp.isEnable()) {
            return idp;
        }
        return null;
    }

    /**
     * Retrieves Identity provider information about a given tenant by realm identifier
     *
     * @param realmId      Unique realm identifier of the Identity provider of whose information is
     *                     requested
     * @param tenantDomain Tenant domain whose information is requested
     * @throws IdentityProviderManagementException Error when getting Identity Provider
     *                                             information by IdP home realm identifier
     */
    @Override
    public IdentityProvider getIdPByRealmId(String realmId, String tenantDomain)
            throws IdentityProviderManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        if (StringUtils.isEmpty(realmId)) {
            String msg = "Invalid argument: Identity Provider Home Realm Identifier value is empty";
            throw new IdentityProviderManagementException(msg);
        }
        IdentityProvider identityProvider = dao.getIdPByRealmId(realmId, tenantId, tenantDomain);

        if (identityProvider == null) {
            identityProvider = new FileBasedIdPMgtDAO().getIdPByRealmId(realmId, tenantDomain);
        }

        return identityProvider;
    }

    /**
     * Retrieves Enabled Identity provider information about a given tenant by realm identifier
     *
     * @param realmId      Unique realm identifier of the Identity provider of whose information is
     *                     requested
     * @param tenantDomain Tenant domain whose information is requested
     * @throws IdentityProviderManagementException Error when getting Identity Provider
     *                                             information by IdP home realm identifier
     */
    @Override
    public IdentityProvider getEnabledIdPByRealmId(String realmId, String tenantDomain)
            throws IdentityProviderManagementException {

        IdentityProvider idp = getIdPByRealmId(realmId, tenantDomain);
        if (idp != null && idp.isEnable()) {
            return idp;
        }
        return null;
    }

    /**
     * Retrieves Identity provider information about a given tenant
     *
     * @param idPName      Unique Name of the IdP to which the given IdP claim URIs need to be mapped
     * @param tenantDomain The tenant domain of whose local claim URIs to be mapped
     * @param idPClaimURIs IdP claim URIs which need to be mapped to tenant's local claim URIs
     * @throws IdentityProviderManagementException Error when getting claim mappings
     */
    @Override
    public Set<ClaimMapping> getMappedLocalClaims(String idPName, String tenantDomain,
                                                  List<String> idPClaimURIs) throws
            IdentityProviderManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        if (StringUtils.isEmpty(idPName)) {
            String msg = "Invalid argument: Identity Provider Name value is empty";
            throw new IdentityProviderManagementException(msg);
        }

        IdentityProvider identityProvider = dao.getIdPByName(null, idPName, tenantId, tenantDomain);

        if (identityProvider == null) {
            identityProvider = new FileBasedIdPMgtDAO().getIdPByName(idPName, tenantDomain);
        }

        if (identityProvider == null) {
            identityProvider = IdPManagementServiceComponent.getFileBasedIdPs().get(
                    IdentityApplicationConstants.DEFAULT_IDP_CONFIG);
        }

        ClaimConfig claimConfiguration = identityProvider.getClaimConfig();

        if (claimConfiguration != null) {

            ClaimMapping[] claimMappings = claimConfiguration.getClaimMappings();

            if (claimMappings != null && claimMappings.length > 0 && idPClaimURIs != null) {
                Set<ClaimMapping> returnSet = new HashSet<ClaimMapping>();
                for (String idpClaim : idPClaimURIs) {
                    for (ClaimMapping claimMapping : claimMappings) {
                        if (claimMapping.getRemoteClaim().getClaimUri().equals(idpClaim)) {
                            returnSet.add(claimMapping);
                            break;
                        }
                    }
                }
                return returnSet;
            }
        }

        return new HashSet<ClaimMapping>();
    }

    /**
     * Retrieves Identity provider information about a given tenant
     *
     * @param idPName      Unique Name of the IdP to which the given IdP claim URIs need to be mapped
     * @param tenantDomain The tenant domain of whose local claim URIs to be mapped
     * @param idPClaimURIs IdP claim URIs which need to be mapped to tenant's local claim URIs
     * @throws IdentityProviderManagementException Error when getting claim mappings
     */
    @Override
    public Map<String, String> getMappedLocalClaimsMap(String idPName, String tenantDomain,
                                                       List<String> idPClaimURIs) throws
            IdentityProviderManagementException {

        Set<ClaimMapping> claimMappings = getMappedLocalClaims(idPName, tenantDomain, idPClaimURIs);
        Map<String, String> returnMap = new HashMap<String, String>();
        for (ClaimMapping claimMapping : claimMappings) {
            returnMap.put(claimMapping.getRemoteClaim().getClaimUri(), claimMapping.getLocalClaim()
                    .getClaimUri());
        }
        return returnMap;
    }

    /**
     * Retrieves Identity provider information about a given tenant
     *
     * @param idPName        Unique Name of the IdP to which the given local claim URIs need to be mapped
     * @param tenantDomain   The tenant domain of whose local claim URIs to be mapped
     * @param localClaimURIs Local claim URIs which need to be mapped to IdP's claim URIs
     * @throws IdentityProviderManagementException Error when getting claim mappings
     */
    @Override
    public Set<ClaimMapping> getMappedIdPClaims(String idPName, String tenantDomain,
                                                List<String> localClaimURIs) throws
            IdentityProviderManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        if (StringUtils.isEmpty(idPName)) {
            String msg = "Invalid argument: Identity Provider Name value is empty";
            throw new IdentityProviderManagementException(msg);
        }

        IdentityProvider identityProvider = dao.getIdPByName(null, idPName, tenantId, tenantDomain);

        if (identityProvider == null) {
            identityProvider = new FileBasedIdPMgtDAO().getIdPByName(idPName, tenantDomain);
        }

        if (identityProvider == null) {
            identityProvider = IdPManagementServiceComponent.getFileBasedIdPs().get(
                    IdentityApplicationConstants.DEFAULT_IDP_CONFIG);
        }

        ClaimConfig claimConfiguration = identityProvider.getClaimConfig();

        if (claimConfiguration != null) {

            ClaimMapping[] claimMappings = claimConfiguration.getClaimMappings();

            if (claimMappings != null && claimMappings.length > 0 && localClaimURIs != null) {
                Set<ClaimMapping> returnSet = new HashSet<ClaimMapping>();
                for (String localClaimURI : localClaimURIs) {
                    for (ClaimMapping claimMapping : claimMappings) {
                        if (claimMapping.getLocalClaim().getClaimUri().equals(localClaimURI)) {
                            returnSet.add(claimMapping);
                            break;
                        }
                    }
                }
                return returnSet;
            }
        }
        return new HashSet<ClaimMapping>();
    }

    /**
     * Retrieves Identity provider information about a given tenant
     *
     * @param idPName        Unique Name of the IdP to which the given local claim URIs need to be mapped
     * @param tenantDomain   The tenant domain of whose local claim URIs to be mapped
     * @param localClaimURIs Local claim URIs which need to be mapped to IdP's claim URIs
     * @throws IdentityProviderManagementException Error when getting claim mappings
     */
    @Override
    public Map<String, String> getMappedIdPClaimsMap(String idPName, String tenantDomain,
                                                     List<String> localClaimURIs) throws
            IdentityProviderManagementException {

        Set<ClaimMapping> claimMappings = getMappedIdPClaims(idPName, tenantDomain, localClaimURIs);
        Map<String, String> returnMap = new HashMap<String, String>();
        for (ClaimMapping claimMapping : claimMappings) {
            returnMap.put(claimMapping.getLocalClaim().getClaimUri(), claimMapping.getRemoteClaim()
                    .getClaimUri());
        }
        return returnMap;
    }

    /**
     * Retrieves Identity provider information about a given tenant
     *
     * @param idPName      Unique name of the IdP to which the given IdP roles need to be mapped
     * @param tenantDomain The tenant domain of whose local roles to be mapped
     * @param idPRoles     IdP roles which need to be mapped to local roles
     * @throws IdentityProviderManagementException Error when getting role mappings
     */
    @Override
    public Set<RoleMapping> getMappedLocalRoles(String idPName, String tenantDomain,
                                                String[] idPRoles) throws IdentityProviderManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        if (StringUtils.isEmpty(idPName)) {
            String msg = "Invalid argument: Identity Provider Name value is empty";
            throw new IdentityProviderManagementException(msg);
        }

        IdentityProvider identityProvider = dao.getIdPByName(null, idPName, tenantId, tenantDomain);

        if (identityProvider == null) {
            identityProvider = new FileBasedIdPMgtDAO().getIdPByName(idPName, tenantDomain);
        }

        if (identityProvider == null) {
            identityProvider = IdPManagementServiceComponent.getFileBasedIdPs().get(
                    IdentityApplicationConstants.DEFAULT_IDP_CONFIG);
        }

        PermissionsAndRoleConfig roleConfiguration = identityProvider.getPermissionAndRoleConfig();

        if (roleConfiguration != null) {
            RoleMapping[] roleMappings = roleConfiguration.getRoleMappings();

            if (roleMappings != null && roleMappings.length > 0 && idPRoles != null) {
                Set<RoleMapping> returnSet = new HashSet<RoleMapping>();
                for (String idPRole : idPRoles) {
                    for (RoleMapping roleMapping : roleMappings) {
                        if (roleMapping.getRemoteRole().equals(idPRole)) {
                            returnSet.add(roleMapping);
                            break;
                        }
                    }
                }
                return returnSet;
            }
        }
        return new HashSet<RoleMapping>();
    }

    /**
     * Retrieves Identity provider information about a given tenant
     *
     * @param idPName      Unique name of the IdP to which the given IdP roles need to be mapped
     * @param tenantDomain The tenant domain of whose local roles to be mapped
     * @param idPRoles     IdP roles which need to be mapped to local roles
     * @throws IdentityProviderManagementException Error when getting role mappings
     */
    @Override
    public Map<String, LocalRole> getMappedLocalRolesMap(String idPName, String tenantDomain,
                                                         String[] idPRoles) throws IdentityProviderManagementException {

        Set<RoleMapping> roleMappings = getMappedLocalRoles(idPName, tenantDomain, idPRoles);
        Map<String, LocalRole> returnMap = new HashMap<String, LocalRole>();
        for (RoleMapping roleMapping : roleMappings) {
            returnMap.put(roleMapping.getRemoteRole(), roleMapping.getLocalRole());
        }
        return returnMap;
    }

    /**
     * Retrieves Identity provider information about a given tenant
     *
     * @param idPName      Unique name of the IdP to which the given local roles need to be mapped
     * @param tenantDomain The tenant domain of whose local roles need to be mapped
     * @param localRoles   Local roles which need to be mapped to IdP roles
     * @throws IdentityProviderManagementException Error when getting role mappings
     */
    @Override
    public Set<RoleMapping> getMappedIdPRoles(String idPName, String tenantDomain,
                                              LocalRole[] localRoles) throws IdentityProviderManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        if (StringUtils.isEmpty(idPName)) {
            String msg = "Invalid argument: Identity Provider Name value is empty";
            throw new IdentityProviderManagementException(msg);
        }
        IdentityProvider identityProvider = dao.getIdPByName(null, idPName, tenantId, tenantDomain);

        if (identityProvider == null) {
            identityProvider = new FileBasedIdPMgtDAO().getIdPByName(idPName, tenantDomain);
        }

        if (identityProvider == null) {
            identityProvider = IdPManagementServiceComponent.getFileBasedIdPs().get(
                    IdentityApplicationConstants.DEFAULT_IDP_CONFIG);
        }

        PermissionsAndRoleConfig roleConfiguration = identityProvider.getPermissionAndRoleConfig();

        if (roleConfiguration != null) {
            RoleMapping[] roleMappings = roleConfiguration.getRoleMappings();

            if (roleMappings != null && roleMappings.length > 0 && localRoles != null) {
                Set<RoleMapping> returnSet = new HashSet<RoleMapping>();
                for (LocalRole localRole : localRoles) {
                    for (RoleMapping roleMapping : roleMappings) {
                        if (roleMapping.getLocalRole().equals(localRole)) {
                            returnSet.add(roleMapping);
                            break;
                        }
                    }
                }
                return returnSet;
            }
        }
        return new HashSet<RoleMapping>();
    }

    /**
     * Retrieves Identity provider information about a given tenant
     *
     * @param idPName      Unique name of the IdP to which the given local roles need to be mapped
     * @param tenantDomain The tenant domain of whose local roles need to be mapped
     * @param localRoles   Local roles which need to be mapped to IdP roles
     * @throws IdentityProviderManagementException Error when getting role mappings
     */
    @Override
    public Map<LocalRole, String> getMappedIdPRolesMap(String idPName, String tenantDomain,
                                                       LocalRole[] localRoles) throws
            IdentityProviderManagementException {

        Set<RoleMapping> roleMappings = getMappedIdPRoles(idPName, tenantDomain, localRoles);
        Map<LocalRole, String> returnMap = new HashMap<LocalRole, String>();
        for (RoleMapping roleMapping : roleMappings) {
            returnMap.put(roleMapping.getLocalRole(), roleMapping.getRemoteRole());
        }
        return returnMap;
    }

    /**
     * If metadata file is available, creates a new FederatedAuthenticatorConfig from that
     *
     * @param identityProvider
     * @throws IdentityProviderManagementException
     */
    private void handleMetadata(int tenantId, IdentityProvider identityProvider)
            throws IdentityProviderManagementException {

        if (IdpMgtServiceComponentHolder.getInstance().getMetadataConverters().isEmpty()) {
            throw new IdentityProviderManagementException("Metadata Converter is not set");
        }

        FederatedAuthenticatorConfig[] federatedAuthenticatorConfigs =
                identityProvider.getFederatedAuthenticatorConfigs();

        for (FederatedAuthenticatorConfig federatedAuthenticatorConfig : federatedAuthenticatorConfigs) {
            Property[] properties = federatedAuthenticatorConfig.getProperties();
            if (ArrayUtils.isNotEmpty(properties)) {

                for (Property property : properties) {
                    if (property != null) {
                        if (StringUtils.isNotBlank(property.getName()) &&
                                property.getName().contains(IdPManagementConstants.META_DATA)) {
                            for (MetadataConverter metadataConverter : IdpMgtServiceComponentHolder.getInstance()
                                    .getMetadataConverters()) {

                                if (metadataConverter.canHandle(property)) {

                                    try {
                                        if (isMetadataFileExist(identityProvider.getIdentityProviderName(),
                                                property.getValue())) {
                                            try {
                                                metadataConverter.saveMetadataString(tenantId,
                                                        identityProvider.getIdentityProviderName(),
                                                        federatedAuthenticatorConfig.getName(), property.getValue());
                                            } catch (IdentityProviderManagementException e) {
                                                String data =
                                                        "Couldn't save metadata in registry.SAML2SSOMetadataConverter" +
                                                                " is not set. ";
                                                throw IdPManagementUtil.handleServerException(
                                                        IdPManagementConstants.ErrorMessage.ERROR_CODE_ADD_IDP, data);
                                            }
                                        }
                                        StringBuilder certificate = new StringBuilder();
                                        try {
                                            FederatedAuthenticatorConfig metaFederated = metadataConverter
                                                    .getFederatedAuthenticatorConfig(properties, certificate);

                                            String spName = "";

                                            for (Property value : properties) {
                                                if (value != null && IdentityApplicationConstants.Authenticator
                                                        .SAML2SSO.SP_ENTITY_ID.equals(value.getName())) {
                                                    spName = value.getValue();
                                                }
                                            }
                                            if (spName.equals("")) {
                                                throw new IdentityProviderManagementException("SP name can't be empty");
                                            }

                                            if (metaFederated != null &&
                                                    ArrayUtils.isNotEmpty(metaFederated.getProperties())) {
                                                for (int y = 0; y < metaFederated.getProperties().length; y++) {
                                                    if (metaFederated.getProperties()[y] != null &&
                                                            IdentityApplicationConstants.Authenticator.SAML2SSO
                                                                    .SP_ENTITY_ID.equals
                                                                    (metaFederated.getProperties()[y].getName())) {
                                                        metaFederated.getProperties()[y].setValue(spName);
                                                        break;
                                                    }
                                                }
                                            }

                                            if (metaFederated != null && ArrayUtils.isNotEmpty(metaFederated.getProperties())) {
                                                federatedAuthenticatorConfig
                                                        .setProperties(metaFederated.getProperties());
                                            } else {
                                                throw new IdentityProviderManagementException(
                                                        "Error setting metadata using file");
                                            }
                                        } catch (IdentityProviderManagementException ex) {
                                            throw new IdentityProviderManagementException("Error converting metadata",
                                                    ex);
                                        }
                                        if (certificate.toString().length() > 0) {
                                            identityProvider.setCertificate(certificate.toString());

                                        }
                                    } catch (XMLStreamException e) {
                                        throw new IdentityProviderManagementException(
                                                "Error while configuring metadata", e);
                                    }
                                    break;

                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Adds an Identity Provider to the given tenant
     *
     * @param identityProvider new Identity Provider information
     * @throws IdentityProviderManagementException Error when adding Identity Provider
     *                                             information
     * @deprecated use {@link IdentityProviderManager#addIdPWithResourceId(IdentityProvider, String)} instead.
     */
    @Deprecated
    @Override
    public void addIdP(IdentityProvider identityProvider, String tenantDomain)
            throws IdentityProviderManagementException {

        addIdPWithResourceId(identityProvider, tenantDomain);
    }

    /**
     * Adds an Identity Provider to the given tenant
     *
     * @param identityProvider new Identity Provider information
     * @throws IdentityProviderManagementException Error when adding Identity Provider
     *                                             information
     */
    @Override
    public IdentityProvider addIdPWithResourceId(IdentityProvider identityProvider, String tenantDomain)
            throws IdentityProviderManagementException {

        validateAddIdPInputValues(identityProvider.getIdentityProviderName(), tenantDomain);
        validateOutboundProvisioningRoles(identityProvider,tenantDomain);

        // Invoking the pre listeners.
        Collection<IdentityProviderMgtListener> listeners = IdPManagementServiceComponent.getIdpMgtListeners();
        for (IdentityProviderMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPreAddIdP(identityProvider, tenantDomain)) {
                return null;
            }
        }

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        if (isPermissionAndRoleConfigExist(identityProvider)) {
            verifyAndUpdateRoleConfiguration(tenantDomain, tenantId, identityProvider.getPermissionAndRoleConfig());
        }

        validateIdPEntityId(extractIdpEntityIdFromMetadata(identityProvider), tenantId, tenantDomain);
        validateIdPIssuerName(identityProvider, tenantId, tenantDomain);

        handleMetadata(tenantId, identityProvider);
        String resourceId = dao.addIdP(identityProvider, tenantId, tenantDomain);
        identityProvider = dao.getIdPByResourceId(resourceId, tenantId, tenantDomain);

        // invoking the post listeners
        for (IdentityProviderMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPostAddIdP(identityProvider, tenantDomain)) {
                return null;
            }
        }
        return identityProvider;
    }

    /**
     * Deletes an Identity Provider from a given tenant
     *
     * @param idPName Name of the IdP to be deleted
     * @throws IdentityProviderManagementException Error when deleting Identity Provider
     *                                             information
     * @deprecated use {@link IdentityProviderManager#deleteIdPByResourceId(String, String)} instead.
     */
    @Deprecated
    @Override
    public void deleteIdP(String idPName, String tenantDomain) throws IdentityProviderManagementException {

        // Invoking the pre listeners.
        Collection<IdentityProviderMgtListener> listeners = IdPManagementServiceComponent.getIdpMgtListeners();
        for (IdentityProviderMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPreDeleteIdP(idPName, tenantDomain)) {
                return;
            }
        }

        if (StringUtils.isEmpty(idPName)) {
            String data = "IdP name is empty.";
            throw IdPManagementUtil.handleClientException(IdPManagementConstants.ErrorMessage
                            .ERROR_CODE_IDP_NAME_INVALID, data);
        }

        IdentityProvider identityProvider = this.getIdPByName(idPName, tenantDomain, true);
        if (identityProvider == null) {
            return;
        }
        deleteIDP(identityProvider.getResourceId(), idPName, tenantDomain);

        // Invoking the post listeners.
        for (IdentityProviderMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPostDeleteIdP(idPName, tenantDomain)) {
                return;
            }
        }
    }

    /**
     * Delete all Identity Providers from a given tenant.
     *
     * @param tenantDomain Domain of the tenant
     * @throws IdentityProviderManagementException
     */
    @Override
    public void deleteIdPs(String tenantDomain) throws IdentityProviderManagementException {

        // Invoking the pre listeners.
        Collection<IdentityProviderMgtListener> listeners = IdPManagementServiceComponent.getIdpMgtListeners();
        for (IdentityProviderMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPreDeleteIdPs(tenantDomain)) {
                return;
            }
        }

        // Delete metadata strings of each IDP
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        List<IdentityProvider> identityProviders = getIdPs(tenantDomain);
        for (IdentityProvider identityProvider : identityProviders) {
            deleteMetadataStrings(identityProvider.getIdentityProviderName(), tenantId);
        }

        dao.deleteIdPs(tenantId);

        // Invoking the post listeners.
        for (IdentityProviderMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPostDeleteIdPs(tenantDomain)) {
                return;
            }
        }
    }

    /**
     * Deletes an Identity Provider from a given tenant.
     *
     * @param resourceId Resource ID of the IdP to be deleted
     * @throws IdentityProviderManagementException Error when deleting Identity Provider
     *                                             information
     */
    @Override
    public void deleteIdPByResourceId(String resourceId, String tenantDomain) throws
            IdentityProviderManagementException {

        // Invoking the pre listeners.
        Collection<IdentityProviderMgtListener> listeners = IdPManagementServiceComponent.getIdpMgtListeners();
        for (IdentityProviderMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPreDeleteIdPByResourceId(resourceId, tenantDomain)) {
                return;
            }
        }
        IdentityProvider identityProvider = getIdPByResourceId(resourceId, tenantDomain, true);
        if (identityProvider == null) {
            return;
        }
        deleteIDP(resourceId, identityProvider.getIdentityProviderName(), tenantDomain);

        // Invoking the post listeners.
        for (IdentityProviderMgtListener listener : listeners) {
            if (listener.isEnable() &&
                    !listener.doPostDeleteIdPByResourceId(resourceId, identityProvider, tenantDomain)) {
                return;
            }
        }
    }

    /**
     * Delete metadata strings of a given IDP.
     *
     * @param idpName Identity Provider name
     * @param tenantId Id of the tenant
     * @throws IdentityProviderManagementException
     */
    private void deleteMetadataStrings(String idpName, int tenantId) throws IdentityProviderManagementException {

        for (MetadataConverter metadataConverter : IdpMgtServiceComponentHolder.getInstance().getMetadataConverters()) {
            if (metadataConverter.canDelete(tenantId, idpName)) {
                metadataConverter.deleteMetadataString(tenantId, idpName);
            }
        }
    }

    /**
     * Delete an IDP.
     *
     * @param resourceId Resource Id
     * @param idpName Name of the IDP
     * @param tenantDomain Tenant Domain
     * @throws IdentityProviderManagementException
     */
    private void deleteIDP(String resourceId, String idpName, String tenantDomain) throws
            IdentityProviderManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        // Delete metadata strings of the IDP
        deleteMetadataStrings(idpName, tenantId);

        dao.deleteIdPByResourceId(resourceId, tenantId, tenantDomain);
    }

    /**
     * Force delete an Identity Provider from a given tenant. This will remove any associations this Identity
     * Provider has with any Service Providers in authentication steps or provisioning.
     *
     * @param idpName      name of IDP to be deleted
     * @param tenantDomain tenantDomain to which the IDP belongs to
     * @deprecated use {@link IdentityProviderManager#forceDeleteIdpByResourceId(String, String)} instead.
     */
    @Deprecated
    public void forceDeleteIdp(String idpName, String tenantDomain) throws IdentityProviderManagementException {

        // Invoking the pre listeners.
        Collection<IdentityProviderMgtListener> listeners = IdPManagementServiceComponent.getIdpMgtListeners();
        for (IdentityProviderMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPreDeleteIdP(idpName, tenantDomain)) {
                return;
            }
        }

        IdentityProvider identityProvider = this
                .getIdPByName(idpName, tenantDomain, true);
        if (identityProvider == null) {
            throw IdPManagementUtil.handleClientException(IdPManagementConstants.ErrorMessage
                    .ERROR_CODE_IDP_NAME_DOES_NOT_EXIST, idpName);
        }
        forceDeleteIDP(identityProvider.getResourceId(), idpName, tenantDomain);

        // Invoking the post listeners.
        for (IdentityProviderMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPostDeleteIdP(idpName, tenantDomain)) {
                return;
            }
        }
    }

    /**
     * Force delete an Identity Provider from a given tenant. This will remove any associations this Identity
     * Provider has with any Service Providers in authentication steps or provisioning.
     *
     * @param resourceId   resource ID of IDP to be deleted
     * @param tenantDomain tenantDomain to which the IDP belongs to
     */
    public void forceDeleteIdpByResourceId(String resourceId, String tenantDomain) throws
            IdentityProviderManagementException {

        // Invoking the pre listeners
        Collection<IdentityProviderMgtListener> listeners = IdPManagementServiceComponent.getIdpMgtListeners();
        for (IdentityProviderMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPreDeleteIdPByResourceId(resourceId, tenantDomain)) {
                return;
            }
        }

        IdentityProvider identityProvider = getIdPByResourceId(resourceId, tenantDomain, true);
        if (identityProvider == null) {
            throw IdPManagementUtil.handleClientException(IdPManagementConstants.ErrorMessage
                    .ERROR_CODE_IDP_DOES_NOT_EXIST, resourceId);
        }
        forceDeleteIDP(resourceId, identityProvider.getIdentityProviderName(), tenantDomain);

        // Invoking the post listeners
        for (IdentityProviderMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPostDeleteIdPByResourceId(resourceId, identityProvider,
                    tenantDomain)) {
                return;
            }
        }
    }

    private void forceDeleteIDP(String resourceId, String idpName, String tenantDomain) throws
            IdentityProviderManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        for (MetadataConverter metadataConverter : IdpMgtServiceComponentHolder.getInstance().getMetadataConverters()) {
            if (metadataConverter.canDelete(tenantId, idpName)) {
                metadataConverter.deleteMetadataString(tenantId, idpName);
            }
        }

        dao.forceDeleteIdPByResourceId(resourceId, tenantId, tenantDomain);
    }

    /**
     * Updates a given Identity Provider information
     *
     * @param oldIdPName          existing Identity Provider name
     * @param newIdentityProvider new IdP information
     * @throws IdentityProviderManagementException Error when updating Identity Provider
     *                                             information
     * @deprecated use {@link IdentityProviderManager#updateIdPByResourceId(String, IdentityProvider, String)} instead.
     */
    @Deprecated
    @Override
    public void updateIdP(String oldIdPName, IdentityProvider newIdentityProvider,
                          String tenantDomain) throws IdentityProviderManagementException {

        // Invoking the pre listeners.
        Collection<IdentityProviderMgtListener> listeners = IdPManagementServiceComponent.getIdpMgtListeners();
        for (IdentityProviderMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPreUpdateIdP(oldIdPName, newIdentityProvider,
                    tenantDomain)) {
                return;
            }
        }
        IdentityProvider currentIdentityProvider = this
                .getIdPByName(oldIdPName, tenantDomain, true);
        if (currentIdentityProvider == null) {
            throw IdPManagementUtil.handleClientException(IdPManagementConstants.ErrorMessage
                    .ERROR_CODE_IDP_NAME_DOES_NOT_EXIST, oldIdPName);
        }
        updateIDP(currentIdentityProvider, newIdentityProvider, IdentityTenantUtil.getTenantId(tenantDomain),
                tenantDomain);

        // Invoking the post listeners.
        for (IdentityProviderMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPostUpdateIdP(oldIdPName, newIdentityProvider, tenantDomain)) {
                return;
            }
        }
    }

    /**
     * Updates a given Identity Provider information
     *
     * @param resourceId          existing Identity Provider resourceId
     * @param newIdentityProvider new IdP information
     * @param tenantDomain        tenant domain of IDP.
     * @throws IdentityProviderManagementException Error when updating Identity Provider
     *                                             information
     */
    @Override
    public IdentityProvider updateIdPByResourceId(String resourceId, IdentityProvider
            newIdentityProvider, String tenantDomain) throws IdentityProviderManagementException {

        // Invoking the pre listeners.
        Collection<IdentityProviderMgtListener> listeners = IdPManagementServiceComponent.getIdpMgtListeners();
        for (IdentityProviderMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPreUpdateIdPByResourceId(resourceId, newIdentityProvider,
                    tenantDomain)) {
                return null;
            }
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        IdentityProvider currentIdentityProvider = this
                .getIdPByResourceId(resourceId, tenantDomain, true);
        validateUpdateIdPInputValues(currentIdentityProvider, resourceId, newIdentityProvider, tenantDomain);
        updateIDP(currentIdentityProvider, newIdentityProvider, tenantId, tenantDomain);

        // Invoking the post listeners.
        for (IdentityProviderMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPostUpdateIdPByResourceId(resourceId, currentIdentityProvider,
                    newIdentityProvider, tenantDomain)) {
                return null;
            }
        }
        return dao.getUpdatedIdPByResourceId(resourceId, tenantId, tenantDomain);
    }

    private void updateIDP(IdentityProvider currentIdentityProvider, IdentityProvider newIdentityProvider, int tenantId,
                           String tenantDomain) throws IdentityProviderManagementException {

        if (isPermissionAndRoleConfigExist(newIdentityProvider)) {
            verifyAndUpdateRoleConfiguration(tenantDomain, tenantId, newIdentityProvider.getPermissionAndRoleConfig());
        }

        validateUpdateOfIdPEntityId(currentIdentityProvider.getFederatedAuthenticatorConfigs(),
                newIdentityProvider.getFederatedAuthenticatorConfigs(),
                tenantId, tenantDomain);

        validateIdPIssuerName(currentIdentityProvider, newIdentityProvider, tenantId, tenantDomain);
        handleMetadata(tenantId, newIdentityProvider);
        dao.updateIdP(newIdentityProvider, currentIdentityProvider, tenantId, tenantDomain);
    }

    /**
     * Get the authenticators registered in the system.
     *
     * @return <code>FederatedAuthenticatorConfig</code> array.
     * @throws IdentityProviderManagementException Error when getting authenticators registered
     *                                             in the system
     */
    @Override
    public FederatedAuthenticatorConfig[] getAllFederatedAuthenticators()
            throws IdentityProviderManagementException {

        List<FederatedAuthenticatorConfig> appConfig = ApplicationAuthenticatorService
                .getInstance().getFederatedAuthenticators();
        if (CollectionUtils.isNotEmpty(appConfig)) {
            return appConfig.toArray(new FederatedAuthenticatorConfig[appConfig.size()]);
        }
        return new FederatedAuthenticatorConfig[0];
    }

    /**
     * Get the Provisioning Connectors registered in the system.
     *
     * @return <code>ProvisioningConnectorConfig</code> array.
     * @throws IdentityProviderManagementException
     */
    @Override
    public ProvisioningConnectorConfig[] getAllProvisioningConnectors()
            throws IdentityProviderManagementException {

        List<ProvisioningConnectorConfig> connectorConfigs = ProvisioningConnectorService
                .getInstance().getProvisioningConnectorConfigs();
        if (connectorConfigs != null && connectorConfigs.size() > 0) {
            return connectorConfigs.toArray(new ProvisioningConnectorConfig[0]);
        }
        return null;
    }

    private boolean validateIdPEntityId(FederatedAuthenticatorConfig[] federatedAuthenticatorConfigs,
                                        int tenantId, String tenantDomain) throws IdentityProviderManagementException {

        if (federatedAuthenticatorConfigs != null) {
            for (FederatedAuthenticatorConfig authConfig : federatedAuthenticatorConfigs) {
                if (IdentityApplicationConstants.Authenticator.SAML2SSO.FED_AUTH_NAME.equals(authConfig.getName()) ||
                        IdentityApplicationConstants.Authenticator.SAML2SSO.NAME.equals(authConfig.getName())) {
                    Property[] properties = authConfig.getProperties();
                    if (properties != null) {
                        for (Property property : properties) {
                            if (IdentityApplicationConstants.Authenticator.SAML2SSO.IDP_ENTITY_ID.equals(
                                    property.getName())) {
                                if (dao.isIdPAvailableForAuthenticatorProperty(authConfig.getName(),
                                        IdentityApplicationConstants.Authenticator.SAML2SSO.IDP_ENTITY_ID,
                                        property.getValue(), tenantId)) {
                                    String msg =
                                            "An Identity Provider Entity ID has already been registered with the " +
                                                    "name '" + property.getValue() + "' for tenant '" + tenantDomain +
                                                    "'";
                                    throw new IdentityProviderManagementClientException(msg);
                                }
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean validateUpdateOfIdPEntityId(FederatedAuthenticatorConfig[] currentFederatedAuthConfigs,
                                                FederatedAuthenticatorConfig[] newFederatedAuthConfigs,
                                                int tenantId, String tenantDomain)
            throws IdentityProviderManagementException {

        String currentIdentityProviderEntityId = null;
        if (currentFederatedAuthConfigs != null) {
            for (FederatedAuthenticatorConfig fedAuthnConfig : currentFederatedAuthConfigs) {
                if (IdentityApplicationConstants.Authenticator.SAML2SSO.FED_AUTH_NAME
                        .equals(fedAuthnConfig.getName()) ||
                        IdentityApplicationConstants.Authenticator.SAML2SSO.NAME.equals(fedAuthnConfig.getName())) {
                    Property[] properties = fedAuthnConfig.getProperties();
                    if (properties != null) {
                        for (Property property : properties) {
                            if (IdentityApplicationConstants.Authenticator.SAML2SSO.IDP_ENTITY_ID.equals
                                    (property.getName())) {
                                currentIdentityProviderEntityId = property.getValue();
                                break;
                            }
                        }
                    }
                    break;
                }
            }
        }

        if (newFederatedAuthConfigs != null) {
            for (FederatedAuthenticatorConfig fedAuthnConfig : newFederatedAuthConfigs) {
                if (IdentityApplicationConstants.Authenticator.SAML2SSO.FED_AUTH_NAME
                        .equals(fedAuthnConfig.getName()) ||
                        IdentityApplicationConstants.Authenticator.SAML2SSO.NAME.equals(fedAuthnConfig.getName())) {
                    Property[] properties = fedAuthnConfig.getProperties();
                    if (properties != null) {
                        for (Property property : properties) {
                            if (IdentityApplicationConstants.Authenticator.SAML2SSO.IDP_ENTITY_ID.equals(property.
                                    getName())) {
                                if (currentIdentityProviderEntityId != null && currentIdentityProviderEntityId.equals
                                        (property.getValue())) {
                                    return true;
                                } else {
                                    if (dao.isIdPAvailableForAuthenticatorProperty(fedAuthnConfig.getName(),
                                            IdentityApplicationConstants.Authenticator.SAML2SSO.IDP_ENTITY_ID,
                                            property.getValue(), tenantId)) {
                                        String msg = "An Identity Provider Entity ID has already been registered " +
                                                "with the name '" +
                                                property.getValue() + "' for tenant '" + tenantDomain + "'";
                                        throw new IdentityProviderManagementClientException(msg);
                                    }
                                    return true;
                                }
                            }
                        }
                    }
                    break;
                }
            }
        }

        return true;
    }

    private String getOIDCResidentIdPEntityId() {

        String OIDCEntityId = IdentityUtil.getProperty("OAuth.OpenIDConnect.IDTokenIssuerID");
        if (StringUtils.isBlank(OIDCEntityId)) {
            OIDCEntityId = "localhost";
        }
        return OIDCEntityId;
    }

    public String getResidentIDPMetadata(String tenantDomain) throws IdentityProviderManagementException {

        if (IdpMgtServiceComponentHolder.getInstance().getMetadataConverters().isEmpty()) {
            throw new IdentityProviderManagementException(
                    "Error receiving Metadata object for tenant: " + tenantDomain);
        }

        IdentityProvider residentIdentityProvider = this.getResidentIdP(tenantDomain);
        FederatedAuthenticatorConfig[] federatedAuthenticatorConfigs =
                residentIdentityProvider.getFederatedAuthenticatorConfigs();
        FederatedAuthenticatorConfig samlFederatedAuthenticatorConfig = null;
        for (int i = 0; i < federatedAuthenticatorConfigs.length; i++) {
            if (federatedAuthenticatorConfigs[i].getName()
                    .equals(IdentityApplicationConstants.Authenticator.SAML2SSO.NAME)) {
                samlFederatedAuthenticatorConfig = federatedAuthenticatorConfigs[i];
                break;
            }
        }
        if (samlFederatedAuthenticatorConfig != null) {
            try {
                for (int t = 0; t < IdpMgtServiceComponentHolder.getInstance().getMetadataConverters().size(); t++) {

                    MetadataConverter converter = IdpMgtServiceComponentHolder.getInstance().getMetadataConverters()
                            .get(t);
                    if (converter.canHandle(samlFederatedAuthenticatorConfig)) {

                        return converter.getMetadataString(samlFederatedAuthenticatorConfig);

                    }
                }
            } catch (IdentityProviderSAMLException e) {
                throw new IdentityProviderManagementException(
                        "Error in retrieving metadata string for tenant:" + tenantDomain, e.getMessage());
            }
        }

        return null;
    }

    @Override
    public ConnectedAppsResult getConnectedApplications(String resourceId, Integer limit, Integer offset, String
            tenantDomain) throws IdentityProviderManagementException {

        validateResourceId(resourceId, tenantDomain);
        limit = validateLimit(limit);
        offset = validateOffset(offset);
        return dao.getConnectedApplications(resourceId, limit, offset);
    }

    private void validateResourceId(String resourceId, String tenantDomain) throws IdentityProviderManagementException {

        if (StringUtils.isEmpty(resourceId)) {
            String data = "Invalid argument: Identity Provider resource ID value is empty";
            throw IdPManagementUtil.handleClientException(IdPManagementConstants.ErrorMessage
                    .ERROR_CODE_RETRIEVE_IDP_CONNECTED_APPS, data);
        }
        if (getIdPByResourceId(resourceId, tenantDomain, true) == null) {
            throw IdPManagementUtil.handleClientException(IdPManagementConstants.ErrorMessage
                    .ERROR_CODE_IDP_DOES_NOT_EXIST, resourceId);
        }
    }

    /**
     * Resolves the public service url given the default context and the url picked from the configuration based on
     * the 'tenant_context.enable_tenant_qualified_urls' mode set in deployment.toml.
     *
     * @param defaultUrlContext Default url context path.
     * @param urlFromConfig     Url picked from the file configuration.
     * @return Absolute public url of the service if 'enable_tenant_qualified_urls' is 'true', else returns the url
     * from the file config.
     * @throws IdentityProviderManagementServerException When fail to build the absolute public url.
     */
    private String resolveAbsoluteURL(String defaultUrlContext, String urlFromConfig, String tenantDomain) throws IdentityProviderManagementServerException {

        if (!IdentityTenantUtil.isTenantQualifiedUrlsEnabled() && StringUtils.isNotBlank(urlFromConfig)) {
            if (log.isDebugEnabled()) {
                log.debug("Resolved URL:" + urlFromConfig + " from file configuration for default url context: " +
                        defaultUrlContext);
            }
            return urlFromConfig;
        }

        try {
             ServiceURLBuilder serviceURLBuilder = ServiceURLBuilder.create().setTenant(tenantDomain);
            return serviceURLBuilder.addPath(defaultUrlContext).build().getAbsolutePublicURL();
        } catch (URLBuilderException e) {
            throw IdentityProviderManagementException.error(IdentityProviderManagementServerException.class,
                    "Error while building URL: " + defaultUrlContext, e);
        }
    }

    private String addTenantPathParamInLegacyMode(String resolvedUrl, String tenantDomain) {

        try {
            if (!IdentityTenantUtil.isTenantQualifiedUrlsEnabled() && StringUtils.isNotBlank(tenantDomain) &&
                    !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                resolvedUrl = getTenantUrl(resolvedUrl, tenantDomain);
            }
        } catch (URISyntaxException e) {
            log.error(String.format("%s endpoint is malformed.", resolvedUrl), e);
        }
        return resolvedUrl;
    }

    private String getTenantUrl(String url, String tenantDomain) throws URISyntaxException {

        URI uri = new URI(url);
        URI uriModified = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), ("/t/" +
                tenantDomain + uri.getPath()), uri.getQuery(), uri.getFragment());
        return uriModified.toString();
    }

    private void verifyAndUpdateRoleConfiguration(String tenantDomain, int tenantId,
                                                  PermissionsAndRoleConfig roleConfiguration)
            throws IdentityProviderManagementException {

        List<RoleMapping> validRoleMappings = new ArrayList<>();
        List<String> validIdPRoles = new ArrayList<>();

        for (RoleMapping mapping : roleConfiguration.getRoleMappings()) {
            try {
                if (mapping.getRemoteRole() == null || mapping.getLocalRole() == null || StringUtils
                        .isBlank(mapping.getLocalRole().getLocalRoleName())) {
                    continue;
                }

                UserStoreManager usm = IdPManagementServiceComponent.getRealmService().getTenantUserRealm(tenantId)
                        .getUserStoreManager();
                String role = mapping.getLocalRole().getLocalRoleName();
                if (StringUtils.isNotBlank(mapping.getLocalRole().getUserStoreId())) {
                    role = IdentityUtil.addDomainToName(role, mapping.getLocalRole().getUserStoreId());
                }

                if (IdentityUtil.isGroupsVsRolesSeparationImprovementsEnabled()) {
                    // Only roles are allowed for role mapping.
                    if (isGroup(role)) {
                        if (log.isDebugEnabled()) {
                            log.debug("Groups including: " + role + ", are not allowed for the identity " +
                                    "provider role mapping.");
                        }
                        continue;
                    }
                }

                // Remove invalid mappings if local role does not exists.
                if (usm.isExistingRole(role)) {
                    validRoleMappings.add(mapping);
                    validIdPRoles.add(mapping.getRemoteRole());
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Invalid local role name: " + role + " for the federated role: " + mapping
                                .getRemoteRole());
                    }
                }
            } catch (UserStoreException e) {
                throw new IdentityProviderManagementException(
                        "Error occurred while retrieving UserStoreManager for tenant " + tenantDomain, e);
            }
        }

        roleConfiguration.setRoleMappings(validRoleMappings.toArray(new RoleMapping[0]));
        roleConfiguration.setIdpRoles(validIdPRoles.toArray(new String[0]));
    }

    /**
     * Validate input parameters for the getIdPByResourceId function.
     *
     * @param resourceId Identity Provider resource ID.
     * @throws IdentityProviderManagementException IdentityProviderManagementException
     */
    private void validateGetIdPInputValues(String resourceId) throws IdentityProviderManagementException {

        if (StringUtils.isEmpty(resourceId)) {
            String data = "Invalid argument: Identity Provider resource ID value is empty";
            throw IdPManagementUtil.handleClientException(IdPManagementConstants.ErrorMessage
                    .ERROR_CODE_IDP_GET_REQUEST_INVALID, data);
        }
    }

    /**
     * Validate input parameters for the addIdPWithResourceId function.
     *
     * @param idpName      Identity Provider name.
     * @param tenantDomain Tenant domain of IDP.
     * @throws IdentityProviderManagementException IdentityProviderManagementException
     */
    private void validateAddIdPInputValues(String idpName, String tenantDomain) throws
            IdentityProviderManagementException {

        if (IdentityProviderManager.getInstance().getIdPByName(idpName, tenantDomain, true) != null) {
            throw IdPManagementUtil.handleClientException(IdPManagementConstants.ErrorMessage
                    .ERROR_CODE_IDP_ALREADY_EXISTS, idpName);
        }

        if (IdPManagementServiceComponent.getFileBasedIdPs().containsKey(idpName)
                && !idpName.startsWith(IdPManagementConstants.SHARED_IDP_PREFIX)) {
            //If an IDP with name starting with "SHARED_" is added from UI, It's blocked at the service class
            // before calling this method
            throw IdPManagementUtil.handleClientException(IdPManagementConstants.ErrorMessage
                    .ERROR_CODE_IDP_ALREADY_EXISTS, idpName);
        }
    }

    /**
     * Validate input parameters for the updateIdPByResourceId function.
     *
     * @param currentIdentityProvider Old Identity Provider Information.
     * @param resourceId              Identity Provider's resource ID.
     * @param newIdentityProvider
     * @param tenantDomain            Tenant domain of IDP.
     * @throws IdentityProviderManagementException IdentityProviderManagementException
     */
    private void validateUpdateIdPInputValues(IdentityProvider currentIdentityProvider, String resourceId,
                                              IdentityProvider newIdentityProvider, String tenantDomain)
            throws IdentityProviderManagementException {

        if (currentIdentityProvider == null) {
            throw IdPManagementUtil.handleClientException(IdPManagementConstants.ErrorMessage
                    .ERROR_CODE_IDP_DOES_NOT_EXIST, resourceId);
        }
        boolean isNewIdPNameExists = false;
        IdentityProvider retrievedIdentityProvider =
                getIdPByName(newIdentityProvider.getIdentityProviderName(), tenantDomain, true);
        if (retrievedIdentityProvider != null) {
            isNewIdPNameExists = !StringUtils.equals(retrievedIdentityProvider.getResourceId(), currentIdentityProvider
                    .getResourceId());
        }
        if (isNewIdPNameExists || IdPManagementServiceComponent.getFileBasedIdPs()
                .containsKey(newIdentityProvider.getIdentityProviderName())) {
            throw IdPManagementUtil.handleClientException(IdPManagementConstants.ErrorMessage
                    .ERROR_CODE_IDP_ALREADY_EXISTS, newIdentityProvider.getIdentityProviderName());
        }

        // Validate whether there are any duplicate properties in the ProvisioningConnectorConfig.
        validateOutboundProvisioningConnectorProperties(newIdentityProvider);
    }

    /**
     * Validate whether there are any duplicate properties in the ProvisioningConnectorConfig of an IdentityProvider.
     *
     * @param newIdentityProvider IdentityProvider object.
     * @throws IdentityProviderManagementException If duplicate properties found in ProvisioningConnectorConfig.
     */
    private void validateOutboundProvisioningConnectorProperties(IdentityProvider newIdentityProvider)
            throws IdentityProviderManagementException {

        ProvisioningConnectorConfig[] provisioningConnectorConfigs =
                newIdentityProvider.getProvisioningConnectorConfigs();
        if (!ArrayUtils.isEmpty(provisioningConnectorConfigs)) {
            for (ProvisioningConnectorConfig connectorConfig : provisioningConnectorConfigs) {
                Property[] properties = connectorConfig.getProvisioningProperties();

                // If no properties have specified, validation needs to stop.
                if (ArrayUtils.isEmpty(properties) || properties.length < 2) {
                    break;
                }
                Set<Property> connectorProperties = new HashSet<>();
                for (Property property : properties) {
                    if (!connectorProperties.add(property)) {
                        throw IdPManagementUtil.handleClientException(
                                IdPManagementConstants.ErrorMessage.DUPLICATE_OUTBOUND_CONNECTOR_PROPERTIES,
                                newIdentityProvider.getIdentityProviderName());
                    }
                }
            }
        }
    }

    /**
     * Check whether PermissionAndRoleConfig is configured for the IDP.
     *
     * @param identityProvider Identity Provider information.
     * @return whether config exists.
     */
    private boolean isPermissionAndRoleConfigExist(IdentityProvider identityProvider) {

        return identityProvider.getPermissionAndRoleConfig() != null
                && identityProvider.getPermissionAndRoleConfig().getRoleMappings() != null;
    }

    /**
     * Check whether metadata file is configured for the IDP.
     *
     * @param idpName  Identity Provider name.
     * @param metadata Metadata string.
     * @return whether metadata exists.
     */
    private boolean isMetadataFileExist(String idpName, String metadata) {

        return StringUtils.isNotEmpty(idpName) && StringUtils.isNotEmpty(metadata);
    }

    @Override
    public IdentityProvider getIdPByMetadataProperty(String property, String value, String tenantDomain,
                                                     boolean ignoreFileBasedIdps)
            throws IdentityProviderManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        if (StringUtils.isEmpty(property) || StringUtils.isEmpty(value)) {
            String msg = "Invalid argument: IDP metadata property or property value is empty";
            throw new IdentityProviderManagementException(msg);
        }

        String idPName = getIDPNameByMetadataProperty(null, property, value, tenantId, tenantDomain,
                ignoreFileBasedIdps);

        if (idPName == null) {
            if (log.isDebugEnabled()) {
                log.debug("IDP Name not found for metadata property name: " + property + " value: " + value +
                        ". Returning null without continuing.");
            }
            return null;
        }

        return getIdPByName(idPName, tenantDomain, ignoreFileBasedIdps);
    }

    /**
     * Method to validate the uniqueness of the IDP Issuer Name.
     * Ideally used when adding a IDP.
     *
     * @param identityProvider Identity Provider being added.
     * @param tenantId Tenant id.
     * @param tenantDomain Tenant domain.
     * @return Returns true if valid.
     * @throws IdentityProviderManagementException IdentityProviderManagementException.
     */
    private boolean validateIdPIssuerName(IdentityProvider identityProvider, int tenantId, String tenantDomain)
            throws IdentityProviderManagementException {

        IdentityProviderProperty[] identityProviderProperties = identityProvider.getIdpProperties();
        if (!ArrayUtils.isEmpty(identityProviderProperties)) {
            for (IdentityProviderProperty prop : identityProviderProperties) {
                if (prop != null && IdentityApplicationConstants.IDP_ISSUER_NAME.equals(prop.getName())
                        && StringUtils.isNotBlank(prop.getValue())) {

                    String idpWithIssuer = getIDPNameByMetadataProperty(null,
                            IdentityApplicationConstants.IDP_ISSUER_NAME, prop.getValue(), tenantId, tenantDomain,
                            false);
                    if (StringUtils.isNotEmpty(idpWithIssuer)) {
                        String msg = "The provided IDP Issuer Name '" + prop.getValue() + "' has already been " +
                                "registered with the IDP '" + idpWithIssuer + "'.";
                        throw new IdentityProviderManagementClientException(msg);
                    }
                }
            }
        }

        return true;
    }

    /**
     * Method to validate the uniqueness of the IDP Issuer Name.
     * Ideally used when updating a IDP.
     * If the provided two IDP configs have the same Issuer Name validation is passed.
     *
     * @param currentIdP Existing Identity Provider config.
     * @param newIdP Updated Identity Provider config.
     * @param tenantId Tenant id.
     * @param tenantDomain Tenant domain.
     * @return Returns true if valid.
     * @throws IdentityProviderManagementException IdentityProviderManagementException.
     */
    private boolean validateIdPIssuerName(IdentityProvider currentIdP, IdentityProvider newIdP, int tenantId,
                                          String tenantDomain)
            throws IdentityProviderManagementException {

        String newIdPIssuerName = null;
        IdentityProviderProperty[] identityProviderProperties = newIdP.getIdpProperties();
        if (!ArrayUtils.isEmpty(identityProviderProperties)) {
            for (IdentityProviderProperty prop : identityProviderProperties) {
                if (prop != null && IdentityApplicationConstants.IDP_ISSUER_NAME.equals(prop.getName())
                        && StringUtils.isNotBlank(prop.getValue())) {

                    newIdPIssuerName = prop.getValue();
                }
            }
        }

        String currentIdPIssuerName = null;
        identityProviderProperties = currentIdP.getIdpProperties();
        if (!ArrayUtils.isEmpty(identityProviderProperties)) {
            for (IdentityProviderProperty prop : identityProviderProperties) {
                if (prop != null && IdentityApplicationConstants.IDP_ISSUER_NAME.equals(prop.getName())
                        && StringUtils.isNotBlank(prop.getValue())) {

                    currentIdPIssuerName = prop.getValue();
                }
            }
        }

        if (StringUtils.isNotBlank(newIdPIssuerName) && !StringUtils.equals(newIdPIssuerName, currentIdPIssuerName)) {
            String idpWithIssuer = getIDPNameByMetadataProperty(null,
                    IdentityApplicationConstants.IDP_ISSUER_NAME, newIdPIssuerName, tenantId, tenantDomain,
                    false);
            if (StringUtils.isNotEmpty(idpWithIssuer)) {
                String msg = "The provided IDP Issuer Name '" + newIdPIssuerName + "' has already been " +
                        "registered with the IDP '" + idpWithIssuer + "'.";
                throw new IdentityProviderManagementClientException(msg);
            }
        }

        return true;
    }

    private String getIDPNameByMetadataProperty(Connection dbConnection, String property, String value, int tenantId,
                                                String tenantDomain, boolean ignoreFileBasedIdps)
            throws IdentityProviderManagementException {

        String idPName = dao.getIdPNameByMetadataProperty(null, property, value, tenantId, tenantDomain);
        if (idPName == null && !ignoreFileBasedIdps) {
            if (log.isDebugEnabled()) {
                log.debug("Attempting to retrieve IDP Name from filebased IDPs for IDP metadata " +
                        "property name: " + property + " value: " + value);
            }
            idPName = new FileBasedIdPMgtDAO().getIdPNameByMetadataProperty(property, value);
        }

        return idPName;
    }

    /**
     * Validate whether the outbound provisioning roles does exist.
     *
     * @param identityProvider IdentityProvider.
     * @param tenantDomain     Tenant Domain.
     * @throws IdentityProviderManagementException If an error occurred while checking for role existence.
     */
    private void validateOutboundProvisioningRoles(IdentityProvider identityProvider, String tenantDomain)
            throws IdentityProviderManagementException {

        String provisioningRole = identityProvider.getProvisioningRole();
        if (StringUtils.isBlank(provisioningRole)) {
            return;
        }
        String[] outboundProvisioningRoles = StringUtils.split(provisioningRole, ",");

        try {
            RoleManagementService roleManagementService =
                    IdpMgtServiceComponentHolder.getInstance().getRoleManagementService();
            for (String roleName : outboundProvisioningRoles) {
                try {
                    if (!roleManagementService.isExistingRoleName(roleName, tenantDomain)) {
                        throw IdPManagementUtil.handleClientException(
                                IdPManagementConstants.ErrorMessage.ERROR_CODE_NOT_EXISTING_OUTBOUND_PROVISIONING_ROLE,
                                null);
                    }
                } catch (NotImplementedException e) {
                    if (log.isDebugEnabled()) {
                        log.debug("isExistingRoleName is not implemented in the RoleManagementService. " +
                                "Therefore, proceeding without validating outbound provisioning role existence.");
                    }
                }
            }
        } catch (IdentityRoleManagementException e) {
            throw IdPManagementUtil.handleServerException(
                    IdPManagementConstants.ErrorMessage.ERROR_CODE_VALIDATING_OUTBOUND_PROVISIONING_ROLES, null, e);
        }
    }

    private boolean isGroup(String localRoleName) {

        return !Stream.of(INTERNAL_DOMAIN, APPLICATION_DOMAIN, WORKFLOW_DOMAIN).anyMatch(domain -> localRoleName
                .toUpperCase().startsWith((domain + UserCoreConstants.DOMAIN_SEPARATOR).toUpperCase()));
    }

    /**
     * Get tenant context using tenant domain.
     *
     * @param tenantDomain Tenant domain of the tenant.
     * @return Tenant context of the tenant.
     */
    private String getTenantContextFromTenantDomain(String tenantDomain) {

        if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(tenantDomain)) {
            return MultitenantConstants.TENANT_AWARE_URL_PREFIX + "/" + tenantDomain + "/";
        }
        return "";
    }

    /**
     * Extracts IdpEntityId property from metadata and adds it to the existing properties of the
     * federatedAuthenticatorConfigs.
     *
     * @param identityProvider IdentityProvider.
     * @return federatedAuthenticatorConfigs - FederatedAuthenticatorConfig[] of the given identityProvider with the
     * IdPEntityId property added by extracting from metadata.
     * @throws IdentityProviderManagementException If the IdpMgtServiceComponentHolder does not contain any
     *                                             metadataConverters.
     */
    private FederatedAuthenticatorConfig[] extractIdpEntityIdFromMetadata(IdentityProvider identityProvider)
            throws IdentityProviderManagementException {

        List<MetadataConverter> metadataConverters = IdpMgtServiceComponentHolder.getInstance().getMetadataConverters();
        if (metadataConverters.isEmpty()) {
            throw new IdentityProviderManagementException("Metadata Converter is not set");
        }

        FederatedAuthenticatorConfig[] federatedAuthenticatorConfigs =
                identityProvider.getFederatedAuthenticatorConfigs();

        for (FederatedAuthenticatorConfig federatedAuthenticatorConfig : federatedAuthenticatorConfigs) {
            Property[] properties = federatedAuthenticatorConfig.getProperties();
            if (ArrayUtils.isEmpty(properties)) {
                return federatedAuthenticatorConfigs;
            }
            for (Property property : properties) {
                if (property == null) {
                    continue;
                }
                // Searching for the metadata property to extract data.
                // Ignoring the properties with blank names and names not equal to meta_data.
                if (StringUtils.isBlank(property.getName()) ||
                        !property.getName().contains((IdPManagementConstants.META_DATA))) {
                    continue;
                }
                for (MetadataConverter metadataConverter : metadataConverters) {
                    if (!metadataConverter.canHandle(property)) {
                        continue;
                    }
                    // Extracting IdpEntityId property and adding to properties of federatedAuthenticatorConfig.
                    Property idpEntityIdFromMetadata = extractPropertyFromMetadata(metadataConverter, properties,
                            IdentityApplicationConstants.Authenticator.SAML2SSO.IDP_ENTITY_ID);
                    if (idpEntityIdFromMetadata != null) {
                        ArrayList<Property> propertiesList = new ArrayList<>(Arrays.asList(properties));
                        propertiesList.add(idpEntityIdFromMetadata);
                        properties = propertiesList.toArray(properties);
                        federatedAuthenticatorConfig.setProperties(properties);
                        break;
                    }
                }
            }
        }
        return federatedAuthenticatorConfigs;
    }

    /**
     * Extracts and returns the property from metadata with the given property name.
     * If the property with the given name is not included in metadata, returns null.
     *
     * @param metadataConverter MetadataConverter to convert the metadata.
     * @param properties        Property[] from which the property should be extracted.
     * @param propertyName      String which is the property name.
     * @return propertyWithName Property with the propertyName is equal to the given property name.
     * @throws IdentityProviderManagementException If an error occurs when converting or configuring metadata.
     */
    private Property extractPropertyFromMetadata(MetadataConverter metadataConverter, Property[] properties,
                                                 String propertyName) throws IdentityProviderManagementException {

        Property propertyWithName = null;
        StringBuilder certificate = new StringBuilder();
        try {
            FederatedAuthenticatorConfig metaFederated =
                    metadataConverter.getFederatedAuthenticatorConfig(properties, certificate);
            Property[] metadataProperties = metaFederated.getProperties();
            for (Property metadataProperty : metadataProperties) {
                // Searching for the property.
                if (propertyName.equals(metadataProperty.getName())) {
                    propertyWithName = metadataProperty;
                    break;
                }
            }
        } catch (IdentityProviderManagementException ex) {
            throw new IdentityProviderManagementException("Error converting metadata", ex);
        } catch (XMLStreamException e) {
            throw new IdentityProviderManagementException("Error while configuring metadata", e);
        }
        return propertyWithName;
    }
}
