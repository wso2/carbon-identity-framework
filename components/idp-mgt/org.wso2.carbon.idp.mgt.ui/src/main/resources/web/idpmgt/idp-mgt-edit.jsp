<!--
~ Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->

<%@page import="org.apache.axis2.context.ConfigurationContext" %>
<%@page import="org.apache.commons.lang.StringUtils" %>
<%@page import="org.owasp.encoder.Encode" %>
<%@page import="org.wso2.carbon.CarbonConstants" %>
<%@page import="org.wso2.carbon.identity.application.common.model.CertData" %>
<%@page import="org.wso2.carbon.identity.application.common.model.idp.xsd.Claim" %>

<%@page import="org.wso2.carbon.identity.application.common.model.idp.xsd.ClaimMapping" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" %>
<%@ taglib uri="http://www.owasp.org/index.php/Category:OWASP_CSRFGuard_Project/Owasp.CsrfGuard.tld" prefix="csrf" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.idp.xsd.FederatedAuthenticatorConfig" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.idp.xsd.Property" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.idp.xsd.ProvisioningConnectorConfig" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.idp.xsd.RoleMapping" %>
<%@ page import="org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants" %>
<%@ page import="org.wso2.carbon.identity.application.common.util.IdentityApplicationManagementUtil" %>
<%@ page import="org.wso2.carbon.identity.core.util.IdentityUtil" %>
<%@ page import="org.wso2.carbon.idp.mgt.ui.client.IdentityProviderMgtServiceClient" %>
<%@ page import="org.wso2.carbon.idp.mgt.ui.util.IdPManagementUIUtil" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.user.core.util.UserCoreUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.Comparator" %>
<%@page import="java.util.HashMap" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.UUID" %>
<%@ page
        import="org.wso2.carbon.identity.application.authentication.framework.config.builder.FileBasedConfigurationBuilder" %>
<%@ page import="org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig" %>
<link href="css/idpmgt.css" rel="stylesheet" type="text/css" media="all"/>

<carbon:breadcrumb label="identity.providers" resourceBundle="org.wso2.carbon.idp.mgt.ui.i18n.Resources"
                   topPage="true" request="<%=request%>"/>
<jsp:include page="../dialog/display_messages.jsp"/>

<%
    String idPName = request.getParameter("idPName");
    if (idPName != null && idPName.equals("")) {
        idPName = null;
    }
    String realmId = null;
    String idpDisplayName = null;
    String description = null;
    boolean federationHubIdp = false;
    CertData certData = null;
    Claim[] identityProviderClaims = null;
    String userIdClaimURI = null;
    String roleClaimURI = null;
    ClaimMapping[] claimMappings = null;
    String[] roles = null;
    RoleMapping[] roleMappings = null;
    String idPAlias = null;
    boolean isProvisioningEnabled = false;
    boolean isCustomClaimEnabled = false;
    boolean isPasswordProvisioningEnabled = false;
    boolean isUserNameModificationAllowed = false;

    String provisioningUserStoreId = null;
    boolean isOpenIdEnabled = false;
    boolean isOpenIdDefault = false;
    String openIdUrl = null;
    boolean isOpenIdUserIdInClaims = false;
    boolean isSAML2SSOEnabled = false;
    boolean isSAMLSSODefault = false;
    String idPEntityId = null;
    String spEntityId = null;
    String nameIdFormat = null;
    String ssoUrl = null;
    boolean isAuthnRequestSigned = false;
    boolean isEnableAssertionEncription = false;
    boolean isEnableAssertionSigning = false;

    String signatureAlgorithm = IdentityApplicationConstants.XML.SignatureAlgorithm.RSA_SHA1;
    String digestAlgorithm = IdentityApplicationConstants.XML.DigestAlgorithm.SHA1;
    String authenticationContextClass = IdentityApplicationConstants.SAML2.AuthnContextClass.PASSWORD_PROTECTED_TRANSPORT;
    String authenticationContextComparisonLevel = IdentityApplicationConstants.SAML2.AuthnContextComparison.EXACT;
    String forceAuthentication = "as_request";
    String attributeConsumingServiceIndex = null;
    String includeAuthenticationContext = "yes";
    boolean includeNameIdPolicy = false;
    boolean includeProtocolBinding = false;
    boolean includeCert = false;

    String requestMethod = "redirect";
    boolean isSLOEnabled = false;
    boolean isLogoutRequestSigned = false;
    String logoutUrl = null;
    boolean isAuthnResponseSigned = false;
    boolean isSAMLSSOUserIdInClaims = false;
    boolean isOIDCEnabled = false;
    boolean isOIDCDefault = false;
    String clientId = null;
    String clientSecret = null;
    String authzUrl = null;
    String tokenUrl = null;
    String callBackUrl = null;
    String userInfoEndpoint = null;
    boolean isOIDCUserIdInClaims = false;
    boolean isPassiveSTSEnabled = false;
    boolean isPassiveSTSDefault = false;
    String passiveSTSRealm = null;
    String passiveSTSUrl = null;
    boolean isPassiveSTSUserIdInClaims = false;
    boolean isEnablePassiveSTSAssertionSignatureValidation = true;
    boolean isEnablePassiveSTSAssertionAudienceValidation = true;
    String[] userStoreDomains = null;
    boolean isFBAuthEnabled = false;
    boolean isFBAuthDefault = false;
    String fbClientId = null;
    String fbClientSecret = null;
    String fbScope = null;
    String fbUserInfoFields = null;
    boolean isFBUserIdInClaims = false;
    String fbAuthnEndpoint = null;
    String fbOauth2TokenEndpoint = null;
    String fbUserInfoEndpoint = null;
    String fbCallBackUrl = null;
    String responseAuthnContextClassRef = "default";

    // To check for existence of authenticator bundles
    boolean isOpenidAuthenticatorActive = false;
    boolean isSamlssoAuthenticatorActive = false;
    boolean isOpenidconnectAuthenticatorActive = false;
    boolean isPassivestsAuthenticatorActive = false;
    boolean isFacebookAuthenticatorActive = false;

    // Claims
    String[] claimUris = new String[0];

    // Provisioning
    boolean isGoogleProvEnabled = false;
    boolean isGoogleProvDefault = false;
    String googleDomainName = null;
    String googleFamilyNameClaim = null;
    String googleFamilyNameDefaultValue = null;
    String googleGivenNameClaim = null;
    String googleGivenNameDefaultValue = null;
    String googlePrimaryEmailClaim = null;
    String googleProvServiceAccEmail = null;
    String googleProvAdminEmail = null;
    String googleProvApplicationName = null;
    String googleProvPattern = null;
    String googleProvisioningSeparator = null;
    String googleProvPrivateKeyData = null;

    boolean isSfProvEnabled = false;

    boolean isScimProvEnabled = false;
    boolean isScimProvDefault = false;
    String scimUserName = null;
    String scimPassword = null;
    String scimGroupEp = null;
    String scimUserEp = null;
    String scimUserStoreDomain = null;
    boolean isSCIMPwdProvEnabled = false;
    String scimDefaultPwd = null;
    String disableDefaultPwd = "";
    String scimUniqueID = null;

    boolean isSpmlProvEnabled = false;
    boolean isSpmlProvDefault = false;
    String spmlUserName = null;
    String spmlPassword = null;
    String spmlEndpoint = null;
    String spmlObjectClass = null;
    String spmlUniqueID = null;

    String oidcQueryParam = "";
    String samlQueryParam = "";
    String passiveSTSQueryParam = "";
    String openidQueryParam = "";

    String provisioningRole = null;
    Map<String, ProvisioningConnectorConfig> customProvisioningConnectors = null;

    Set<String> signatureAlgorithms = IdentityApplicationManagementUtil.getXMLSignatureAlgorithmNames();
    Set<String> digestAlgorithms = IdentityApplicationManagementUtil.getXMLDigestAlgorithmNames();
    Set<String> authenticationContextClasses = IdentityApplicationManagementUtil.getSAMLAuthnContextClassNames();
    List<String> authenticationContextComparisonLevels = IdentityApplicationManagementUtil
            .getSAMLAuthnContextComparisonLevels();

    String[] idpClaims = new String[]{"admin", "Internal/everyone"};//appBean.getSystemClaims();

    Map<String, UUID> idpUniqueIdMap = (Map<String, UUID>) session.getAttribute("idpUniqueIdMap");

    if (idpUniqueIdMap == null) {
        idpUniqueIdMap = new HashMap<String, UUID>();
    }

    IdentityProvider identityProvider = null;

    if (idPName != null && idpUniqueIdMap.get(idPName) != null) {
        identityProvider = (IdentityProvider) session.getAttribute(idpUniqueIdMap.get(idPName).toString());
    }

    List<IdentityProvider> identityProvidersList =
            (List<IdentityProvider>) session.getAttribute("identityProviderList");

    Map<String, FederatedAuthenticatorConfig> allFedAuthConfigs = new HashMap<String, FederatedAuthenticatorConfig>();

    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    IdentityProviderMgtServiceClient client =
            new IdentityProviderMgtServiceClient(cookie, backendServerURL, configContext);

    allFedAuthConfigs = client.getAllFederatedAuthenticators();
    customProvisioningConnectors = client.getCustomProvisioningConnectors();

    if (identityProvidersList == null) {
%>
<script type="text/javascript">
    location.href = "idp-mgt-list-load.jsp?callback=idp-mgt-edit.jsp";
</script>
<%
        return;
    }
    if (idPName != null && identityProvider != null) {
        idPName = identityProvider.getIdentityProviderName();
        federationHubIdp = identityProvider.getFederationHub();
        realmId = identityProvider.getHomeRealmId();
        idpDisplayName = identityProvider.getDisplayName();
        description = identityProvider.getIdentityProviderDescription();
        provisioningRole = identityProvider.getProvisioningRole();
        if (StringUtils.isNotBlank(identityProvider.getCertificate())) {
            certData = IdentityApplicationManagementUtil.getCertData(identityProvider.getCertificate());
        }

        identityProviderClaims = identityProvider.getClaimConfig().getIdpClaims();

        userIdClaimURI = identityProvider.getClaimConfig().getUserClaimURI();
        roleClaimURI = identityProvider.getClaimConfig().getRoleClaimURI();

        claimMappings = identityProvider.getClaimConfig().getClaimMappings();

        if (identityProviderClaims != null && identityProviderClaims.length != 0) {
            isCustomClaimEnabled = true;
        } else {
            isCustomClaimEnabled = false;
        }


        roles = identityProvider.getPermissionAndRoleConfig().getIdpRoles();
        roleMappings = identityProvider.getPermissionAndRoleConfig().getRoleMappings();

        FederatedAuthenticatorConfig[] fedAuthnConfigs = identityProvider.getFederatedAuthenticatorConfigs();

        if (fedAuthnConfigs != null && fedAuthnConfigs.length > 0) {
            for (FederatedAuthenticatorConfig fedAuthnConfig : fedAuthnConfigs) {
                if (fedAuthnConfig.getProperties() == null) {
                    fedAuthnConfig.setProperties(new Property[0]);
                }
                if (fedAuthnConfig.getDisplayName().equals(IdentityApplicationConstants.Authenticator.OpenID.NAME)) {
                    isOpenidAuthenticatorActive = true;
                    allFedAuthConfigs.remove(fedAuthnConfig.getDisplayName());
                    isOpenIdEnabled = fedAuthnConfig.getEnabled();

                    Property openIdUrlProp = IdPManagementUIUtil.getProperty(fedAuthnConfig.getProperties(),
                            IdentityApplicationConstants.Authenticator.OpenID.OPEN_ID_URL);
                    if (openIdUrlProp != null) {
                        openIdUrl = openIdUrlProp.getValue();
                    }

                    Property queryParamProp = IdPManagementUIUtil.getProperty(fedAuthnConfig.getProperties(), "commonAuthQueryParams");
                    if (queryParamProp != null) {
                        openidQueryParam = queryParamProp.getValue();
                    }

                    Property isOpenIdUserIdInClaimsProp = IdPManagementUIUtil.getProperty(fedAuthnConfig.getProperties(),
                            IdentityApplicationConstants.Authenticator.OpenID.IS_USER_ID_IN_CLAIMS);
                    if (isOpenIdUserIdInClaimsProp != null) {
                        isOpenIdUserIdInClaims = Boolean.parseBoolean(isOpenIdUserIdInClaimsProp.getValue());
                    }
                } else if (fedAuthnConfig.getDisplayName().equals(IdentityApplicationConstants.Authenticator.Facebook.NAME)) {
                    isFacebookAuthenticatorActive = true;
                    allFedAuthConfigs.remove(fedAuthnConfig.getDisplayName());
                    isFBAuthEnabled = fedAuthnConfig.getEnabled();
                    Property fbClientIdProp = IdPManagementUIUtil.getProperty(fedAuthnConfig.getProperties(),
                            IdentityApplicationConstants.Authenticator.Facebook.CLIENT_ID);
                    if (fbClientIdProp != null) {
                        fbClientId = fbClientIdProp.getValue();
                    }
                    Property fbClientSecretProp = IdPManagementUIUtil.getProperty(fedAuthnConfig.getProperties(),
                            IdentityApplicationConstants.Authenticator.Facebook.CLIENT_SECRET);
                    if (fbClientSecretProp != null) {
                        fbClientSecret = fbClientSecretProp.getValue();
                    }
                    Property fbScopeProp = IdPManagementUIUtil
                            .getProperty(fedAuthnConfig.getProperties(),
                                    IdentityApplicationConstants.Authenticator.Facebook.SCOPE);
                    if (fbScopeProp != null) {
                        fbScope = fbScopeProp.getValue();
                    }
                    Property fbUserInfoFieldsProp = IdPManagementUIUtil.getProperty(fedAuthnConfig.getProperties(),
                            IdentityApplicationConstants.Authenticator.Facebook.USER_INFO_FIELDS);
                    if (fbUserInfoFieldsProp != null) {
                        fbUserInfoFields = fbUserInfoFieldsProp.getValue();
                    }
                    Property fbAuthnEndpointProp = IdPManagementUIUtil
                            .getProperty(fedAuthnConfig.getProperties(),
                                    IdentityApplicationConstants.Authenticator.Facebook.AUTH_ENDPOINT);
                    if (fbAuthnEndpointProp != null) {
                        fbAuthnEndpoint = fbAuthnEndpointProp.getValue();
                    }
                    Property fbOauth2TokenEndpointProp = IdPManagementUIUtil
                            .getProperty(fedAuthnConfig.getProperties(),
                                    IdentityApplicationConstants.Authenticator.Facebook.AUTH_TOKEN_ENDPOINT);
                    if (fbOauth2TokenEndpointProp != null) {
                        fbOauth2TokenEndpoint = fbOauth2TokenEndpointProp.getValue();
                    }
                    Property fbUserInfoEndpointProp = IdPManagementUIUtil
                            .getProperty(fedAuthnConfig.getProperties(),
                                    IdentityApplicationConstants.Authenticator.Facebook.USER_INFO_ENDPOINT);
                    if (fbUserInfoEndpointProp != null) {
                        fbUserInfoEndpoint = fbUserInfoEndpointProp.getValue();
                    }
                    Property fbCallBackUrlProp = IdPManagementUIUtil
                            .getProperty(fedAuthnConfig.getProperties(),
                                    IdentityApplicationConstants.Authenticator.Facebook.CALLBACK_URL);
                    if (fbCallBackUrlProp != null) {
                        fbCallBackUrl = fbCallBackUrlProp.getValue();
                    }
                } else if (fedAuthnConfig.getDisplayName().equals(IdentityApplicationConstants.Authenticator.PassiveSTS.NAME)) {
                    isPassivestsAuthenticatorActive = true;
                    allFedAuthConfigs.remove(fedAuthnConfig.getDisplayName());
                    isPassiveSTSEnabled = fedAuthnConfig.getEnabled();
                    Property passiveSTSRealmProp = IdPManagementUIUtil.getProperty(fedAuthnConfig.getProperties(),
                            IdentityApplicationConstants.Authenticator.PassiveSTS.REALM_ID);
                    if (passiveSTSRealmProp != null) {
                        passiveSTSRealm = passiveSTSRealmProp.getValue();
                    }
                    Property passiveSTSUrlProp = IdPManagementUIUtil.getProperty(fedAuthnConfig.getProperties(),
                            IdentityApplicationConstants.Authenticator.PassiveSTS.IDENTITY_PROVIDER_URL);
                    if (passiveSTSUrlProp != null) {
                        passiveSTSUrl = passiveSTSUrlProp.getValue();
                    }
                    Property isPassiveSTSUserIdInClaimsProp = IdPManagementUIUtil.getProperty(fedAuthnConfig.getProperties(),
                            IdentityApplicationConstants.Authenticator.PassiveSTS.IS_USER_ID_IN_CLAIMS);
                    if (isPassiveSTSUserIdInClaimsProp != null) {
                        isPassiveSTSUserIdInClaims = Boolean.parseBoolean(isPassiveSTSUserIdInClaimsProp.getValue());
                    }
                    Property isEnableAssertionSignatureValidationProp =
                            IdPManagementUIUtil.getProperty(fedAuthnConfig.getProperties(),
                                IdentityApplicationConstants.Authenticator.PassiveSTS.IS_ENABLE_ASSERTION_SIGNATURE_VALIDATION);
                    if (isEnableAssertionSignatureValidationProp != null) {
                        isEnablePassiveSTSAssertionSignatureValidation =
                                Boolean.parseBoolean(isEnableAssertionSignatureValidationProp.getValue());
                    }
                    Property isEnableAssertionAudienceValidationProp =
                            IdPManagementUIUtil.getProperty(fedAuthnConfig.getProperties(),
                                    IdentityApplicationConstants.Authenticator.PassiveSTS.IS_ENABLE_ASSERTION_AUDIENCE_VALIDATION);
                    if (isEnableAssertionAudienceValidationProp != null) {
                        isEnablePassiveSTSAssertionAudienceValidation =
                                Boolean.parseBoolean(isEnableAssertionAudienceValidationProp.getValue());
                    }

                    Property queryParamProp = IdPManagementUIUtil.getProperty(fedAuthnConfig.getProperties(), "commonAuthQueryParams");
                    if (queryParamProp != null) {
                        passiveSTSQueryParam = queryParamProp.getValue();
                    }

                } else if (fedAuthnConfig.getDisplayName().equals(IdentityApplicationConstants.Authenticator.OIDC.NAME)) {
                    isOpenidconnectAuthenticatorActive = true;
                    allFedAuthConfigs.remove(fedAuthnConfig.getDisplayName());
                    isOIDCEnabled = fedAuthnConfig.getEnabled();
                    Property authzUrlProp = IdPManagementUIUtil.getProperty(fedAuthnConfig.getProperties(),
                            IdentityApplicationConstants.Authenticator.OIDC.OAUTH2_AUTHZ_URL);
                    if (authzUrlProp != null) {
                        authzUrl = authzUrlProp.getValue();
                    }
                    Property tokenUrlProp = IdPManagementUIUtil.getProperty(fedAuthnConfig.getProperties(),
                            IdentityApplicationConstants.Authenticator.OIDC.OAUTH2_TOKEN_URL);
                    if (tokenUrlProp != null) {
                        tokenUrl = tokenUrlProp.getValue();
                    }
                    Property callBackURLProp = IdPManagementUIUtil.getProperty(fedAuthnConfig.getProperties(),
                            IdentityApplicationConstants.Authenticator.OIDC.CALLBACK_URL);
                    if (callBackURLProp != null) {
                        callBackUrl = callBackURLProp.getValue();
                    }

                    Property userInfoEndpointProp = IdPManagementUIUtil.getProperty(fedAuthnConfig.getProperties(),
                            IdentityApplicationConstants.Authenticator.OIDC.USER_INFO_URL);
                    if (userInfoEndpointProp != null) {
                        userInfoEndpoint = userInfoEndpointProp.getValue();
                    }

                    Property clientIdProp = IdPManagementUIUtil.getProperty(fedAuthnConfig.getProperties(),
                            IdentityApplicationConstants.Authenticator.OIDC.CLIENT_ID);
                    if (clientIdProp != null) {
                        clientId = clientIdProp.getValue();
                    }
                    Property clientSecretProp = IdPManagementUIUtil.getProperty(fedAuthnConfig.getProperties(),
                            IdentityApplicationConstants.Authenticator.OIDC.CLIENT_SECRET);
                    if (clientSecretProp != null) {
                        clientSecret = clientSecretProp.getValue();
                    }
                    Property isOIDCUserIdInClaimsProp = IdPManagementUIUtil.getProperty(fedAuthnConfig.getProperties(),
                            IdentityApplicationConstants.Authenticator.OIDC.IS_USER_ID_IN_CLAIMS);
                    if (isOIDCUserIdInClaimsProp != null) {
                        isOIDCUserIdInClaims = Boolean.parseBoolean(isOIDCUserIdInClaimsProp.getValue());
                    }

                    Property queryParamProp = IdPManagementUIUtil.getProperty(fedAuthnConfig.getProperties(), "commonAuthQueryParams");
                    if (queryParamProp != null) {
                        oidcQueryParam = queryParamProp.getValue();
                    }

                } else if (fedAuthnConfig.getDisplayName().equals(IdentityApplicationConstants.Authenticator.SAML2SSO.NAME)) {
                    isSamlssoAuthenticatorActive = true;
                    allFedAuthConfigs.remove(fedAuthnConfig.getDisplayName());
                    isSAML2SSOEnabled = fedAuthnConfig.getEnabled();
                    Property idPEntityIdProp = IdPManagementUIUtil.getProperty(fedAuthnConfig.getProperties(),
                            IdentityApplicationConstants.Authenticator.SAML2SSO.IDP_ENTITY_ID);
                    if (idPEntityIdProp != null) {
                        idPEntityId = idPEntityIdProp.getValue();
                    }
                    Property spEntityIdProp = IdPManagementUIUtil.getProperty(fedAuthnConfig.getProperties(),
                            IdentityApplicationConstants.Authenticator.SAML2SSO.SP_ENTITY_ID);
                    if (spEntityIdProp != null) {
                        spEntityId = spEntityIdProp.getValue();
                    }
                    Property nameIDFormatProp = IdPManagementUIUtil.getProperty(fedAuthnConfig.getProperties(),
                            IdentityApplicationConstants.Authenticator.SAML2SSO.NAME_ID_TYPE);
                    if (nameIDFormatProp != null) {
                        nameIdFormat = nameIDFormatProp.getValue();
                    }
                    Property ssoUrlProp = IdPManagementUIUtil.getProperty(fedAuthnConfig.getProperties(),
                            IdentityApplicationConstants.Authenticator.SAML2SSO.SSO_URL);
                    if (spEntityIdProp != null) {
                        ssoUrl = ssoUrlProp.getValue();
                    }
                    Property isAuthnRequestSignedProp = IdPManagementUIUtil.getProperty(fedAuthnConfig.getProperties(),
                            IdentityApplicationConstants.Authenticator.SAML2SSO.IS_AUTHN_REQ_SIGNED);
                    if (isAuthnRequestSignedProp != null) {
                        isAuthnRequestSigned = Boolean.parseBoolean(isAuthnRequestSignedProp.getValue());
                    }

                    Property isEnableAssertionSigningProp = IdPManagementUIUtil.getProperty(fedAuthnConfig.getProperties(),
                            IdentityApplicationConstants.Authenticator.SAML2SSO.IS_ENABLE_ASSERTION_SIGNING);
                    if (isEnableAssertionSigningProp != null) {
                        isEnableAssertionSigning = Boolean.parseBoolean(isEnableAssertionSigningProp.getValue());
                    }

                    Property isEnableAssersionEncriptionProp = IdPManagementUIUtil.getProperty(fedAuthnConfig.getProperties(),
                            IdentityApplicationConstants.Authenticator.SAML2SSO.IS_ENABLE_ASSERTION_ENCRYPTION);
                    if (isEnableAssersionEncriptionProp != null) {
                        isEnableAssertionEncription = Boolean.parseBoolean(isEnableAssersionEncriptionProp.getValue());
                    }

                    Property isSLOEnabledProp = IdPManagementUIUtil.getProperty(fedAuthnConfig.getProperties(),
                            IdentityApplicationConstants.Authenticator.SAML2SSO.IS_LOGOUT_ENABLED);
                    if (isSLOEnabledProp != null) {
                        isSLOEnabled = Boolean.parseBoolean(isSLOEnabledProp.getValue());
                    }
                    Property logoutUrlProp = IdPManagementUIUtil.getProperty(fedAuthnConfig.getProperties(),
                            IdentityApplicationConstants.Authenticator.SAML2SSO.LOGOUT_REQ_URL);
                    if (logoutUrlProp != null) {
                        logoutUrl = logoutUrlProp.getValue();
                    }
                    Property isLogoutRequestSignedProp = IdPManagementUIUtil.getProperty(fedAuthnConfig.getProperties(),
                            IdentityApplicationConstants.Authenticator.SAML2SSO.IS_LOGOUT_REQ_SIGNED);
                    if (isLogoutRequestSignedProp != null) {
                        isLogoutRequestSigned = Boolean.parseBoolean(isLogoutRequestSignedProp.getValue());
                    }
                    Property isAuthnResponseSignedProp = IdPManagementUIUtil.getProperty(fedAuthnConfig.getProperties(),
                            IdentityApplicationConstants.Authenticator.SAML2SSO.IS_AUTHN_RESP_SIGNED);
                    if (isAuthnResponseSignedProp != null) {
                        isAuthnResponseSigned = Boolean.parseBoolean(isAuthnResponseSignedProp.getValue());
                    }

                    Property signatureAlgoProp = IdPManagementUIUtil.getProperty(fedAuthnConfig.getProperties(),
                            IdentityApplicationConstants.Authenticator.SAML2SSO.SIGNATURE_ALGORITHM);
                    if (signatureAlgoProp != null) {
                        signatureAlgorithm = signatureAlgoProp.getValue();
                    }

                    Property digestAlgoProp = IdPManagementUIUtil.getProperty(fedAuthnConfig.getProperties(),
                            IdentityApplicationConstants.Authenticator.SAML2SSO.DIGEST_ALGORITHM);
                    if (digestAlgoProp != null) {
                        digestAlgorithm = digestAlgoProp.getValue();
                    }

                    Property includeAuthnContextProp = IdPManagementUIUtil.getProperty(fedAuthnConfig.getProperties(),
                            IdentityApplicationConstants.Authenticator.SAML2SSO.INCLUDE_AUTHN_CONTEXT);
                    if (includeAuthnContextProp != null) {
                        includeAuthenticationContext = includeAuthnContextProp.getValue();
                    } else {
                        includeAuthenticationContext = "yes";
                    }

                    Property authnContextRefClassProp = IdPManagementUIUtil.getProperty(fedAuthnConfig.getProperties(),
                            IdentityApplicationConstants.Authenticator.SAML2SSO.AUTHENTICATION_CONTEXT_CLASS);
                    if (authnContextRefClassProp != null) {
                        authenticationContextClass = authnContextRefClassProp.getValue();
                    } else {
                        authenticationContextClass = "urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport";
                    }

                    Property authnContextCompLevelProp = IdPManagementUIUtil.getProperty(fedAuthnConfig.getProperties(),
                            IdentityApplicationConstants.Authenticator.SAML2SSO.AUTHENTICATION_CONTEXT_COMPARISON_LEVEL);
                    if (authnContextCompLevelProp != null) {
                        authenticationContextComparisonLevel = authnContextCompLevelProp.getValue();
                    } else {
                        authenticationContextComparisonLevel = "Exact";
                    }

                    Property includeNameIdPolicyProp = IdPManagementUIUtil.getProperty(fedAuthnConfig.getProperties(),
                            IdentityApplicationConstants.Authenticator.SAML2SSO.INCLUDE_NAME_ID_POLICY);
                    if (includeNameIdPolicyProp != null) {
                        includeNameIdPolicy = Boolean.parseBoolean(includeNameIdPolicyProp.getValue());
                    }

                    Property includeCertProp = IdPManagementUIUtil.getProperty(fedAuthnConfig.getProperties(),
                            IdentityApplicationConstants.Authenticator.SAML2SSO.INCLUDE_CERT);
                    if (includeCertProp != null) {
                        includeCert = Boolean.parseBoolean(includeCertProp.getValue());
                    }

                    Property includeProtocolBindingProp = IdPManagementUIUtil.getProperty(fedAuthnConfig.getProperties(),
                            IdentityApplicationConstants.Authenticator.SAML2SSO.INCLUDE_PROTOCOL_BINDING);
                    if (includeProtocolBindingProp != null) {
                        includeProtocolBinding = Boolean.parseBoolean(includeProtocolBindingProp.getValue());
                    }

                    Property forceAuthProp = IdPManagementUIUtil.getProperty(fedAuthnConfig.getProperties(),
                            IdentityApplicationConstants.Authenticator.SAML2SSO.FORCE_AUTHENTICATION);
                    if (forceAuthProp != null) {
                        forceAuthentication = forceAuthProp.getValue();
                    } else {
                        forceAuthentication = "as_request";
                    }

                    Property attributeConsumingServiceIndexProp = IdPManagementUIUtil.getProperty(fedAuthnConfig.getProperties(),
                            IdentityApplicationConstants.Authenticator.SAML2SSO.ATTRIBUTE_CONSUMING_SERVICE_INDEX);
                    if (attributeConsumingServiceIndexProp != null) {
                        attributeConsumingServiceIndex = attributeConsumingServiceIndexProp.getValue();
                    }

                    Property requestMethodProp = IdPManagementUIUtil.getProperty(fedAuthnConfig.getProperties(),
                            IdentityApplicationConstants.Authenticator.SAML2SSO.REQUEST_METHOD);
                    if (requestMethodProp != null) {
                        requestMethod = requestMethodProp.getValue();
                    } else {
                        requestMethod = "redirect";
                    }

                    Property responseAuthnContextClassRefProp = IdPManagementUIUtil.getProperty(fedAuthnConfig
                            .getProperties(), IdentityApplicationConstants.Authenticator.SAML2SSO
                            .RESPONSE_AUTHN_CONTEXT_CLASS_REF);
                    if (responseAuthnContextClassRefProp != null) {
                        responseAuthnContextClassRef = responseAuthnContextClassRefProp.getValue();
                    } else {
                        responseAuthnContextClassRef = "default";
                    }

                    Property isSAMLSSOUserIdInClaimsProp = IdPManagementUIUtil.getProperty(fedAuthnConfig.getProperties(),
                            IdentityApplicationConstants.Authenticator.SAML2SSO.IS_USER_ID_IN_CLAIMS);
                    if (isSAMLSSOUserIdInClaimsProp != null) {
                        isSAMLSSOUserIdInClaims = Boolean.parseBoolean(isSAMLSSOUserIdInClaimsProp.getValue());
                    }

                    Property queryParamProp = IdPManagementUIUtil.getProperty(fedAuthnConfig.getProperties(), "commonAuthQueryParams");
                    if (queryParamProp != null) {
                        samlQueryParam = queryParamProp.getValue();
                    }
                } else {
                    FederatedAuthenticatorConfig customConfig = allFedAuthConfigs.get(fedAuthnConfig.getName());
                    if (customConfig != null) {
                        Property[] properties = fedAuthnConfig.getProperties();
                        Property[] customProperties = customConfig.getProperties();

                        if (properties != null && properties.length > 0 && customProperties != null && customProperties.length > 0) {
                            for (Property property : properties) {
                                for (Property customProperty : customProperties) {
                                    if (property.getName().equals(customProperty.getName())) {
                                        customProperty.setValue(property.getValue());
                                        break;
                                    }
                                }
                            }
                        }

                        customConfig.setEnabled(fedAuthnConfig.getEnabled());
                        allFedAuthConfigs.put(fedAuthnConfig.getName(), customConfig);
                    }
                }
            }
        }


        idPAlias = identityProvider.getAlias();
        isProvisioningEnabled = identityProvider.getJustInTimeProvisioningConfig().getProvisioningEnabled();
        provisioningUserStoreId = identityProvider.getJustInTimeProvisioningConfig().getProvisioningUserStore();
        isPasswordProvisioningEnabled =
                identityProvider.getJustInTimeProvisioningConfig().getPasswordProvisioningEnabled();
        isUserNameModificationAllowed = identityProvider.getJustInTimeProvisioningConfig().getModifyUserNameAllowed();

        if (identityProvider.getDefaultAuthenticatorConfig() != null
                && identityProvider.getDefaultAuthenticatorConfig().getName() != null) {
            isOpenIdDefault = identityProvider.getDefaultAuthenticatorConfig().getDisplayName().equals(
                    IdentityApplicationConstants.Authenticator.OpenID.NAME);
        }

        if (identityProvider.getDefaultAuthenticatorConfig() != null
                && identityProvider.getDefaultAuthenticatorConfig().getName() != null) {
            isSAMLSSODefault = identityProvider.getDefaultAuthenticatorConfig().getDisplayName().equals(
                    IdentityApplicationConstants.Authenticator.SAML2SSO.NAME);
        }

        if (identityProvider.getDefaultAuthenticatorConfig() != null
                && identityProvider.getDefaultAuthenticatorConfig().getName() != null) {
            isOIDCDefault = identityProvider.getDefaultAuthenticatorConfig().getDisplayName().equals(
                    IdentityApplicationConstants.Authenticator.OIDC.NAME);
        }

        if (identityProvider.getDefaultAuthenticatorConfig() != null
                && identityProvider.getDefaultAuthenticatorConfig().getName() != null) {
            isPassiveSTSDefault = identityProvider.getDefaultAuthenticatorConfig().getDisplayName().equals(
                    IdentityApplicationConstants.Authenticator.PassiveSTS.NAME);
        }

        if (identityProvider.getDefaultAuthenticatorConfig() != null
                && identityProvider.getDefaultAuthenticatorConfig().getName() != null) {
            isFBAuthDefault = identityProvider.getDefaultAuthenticatorConfig().getDisplayName().equals(
                    IdentityApplicationConstants.Authenticator.Facebook.NAME);
        }

        ProvisioningConnectorConfig[] provisioningConnectors = identityProvider.getProvisioningConnectorConfigs();

        ProvisioningConnectorConfig googleApps = null;
        ProvisioningConnectorConfig salesforce = null;
        ProvisioningConnectorConfig scim = null;
        ProvisioningConnectorConfig spml = null;

        if (provisioningConnectors != null) {
            for (ProvisioningConnectorConfig provisioningConnector : provisioningConnectors) {
                if (provisioningConnector != null && "scim".equals(provisioningConnector.getName())) {
                    scim = provisioningConnector;
                } else if (provisioningConnector != null && "spml".equals(provisioningConnector.getName())) {
                    spml = provisioningConnector;
                } else if (provisioningConnector != null && "salesforce".equals(provisioningConnector.getName())) {
                    salesforce = provisioningConnector;
                } else if (provisioningConnector != null && "googleapps".equals(provisioningConnector.getName())) {
                    googleApps = provisioningConnector;
                } else {
                    if (customProvisioningConnectors.containsKey(provisioningConnector.getName())) {

                        ProvisioningConnectorConfig customConfig = customProvisioningConnectors.get(provisioningConnector.getName());
                        Property[] properties = provisioningConnector.getProvisioningProperties();
                        Property[] customProperties = customConfig.getProvisioningProperties();

                        customConfig.setEnabled(provisioningConnector.getEnabled());

                        if (properties != null && properties.length > 0 && customProperties != null && customProperties.length > 0) {
                            for (Property property : properties) {
                                for (Property customProperty : customProperties) {
                                    if (property.getName().equals(customProperty.getName())) {
                                        customProperty.setValue(property.getValue());
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (scim != null) {

            if (identityProvider.getDefaultProvisioningConnectorConfig() != null
                    && identityProvider.getDefaultProvisioningConnectorConfig().getName() != null) {
                isScimProvDefault = identityProvider.getDefaultProvisioningConnectorConfig().getName().equals(scim.getName());
            }

            Property[] scimProperties = scim.getProvisioningProperties();
            if (scimProperties != null && scimProperties.length > 0) {
                for (Property scimProperty : scimProperties) {
                    //This is a safety to check to avoid NPE
                    if (scimProperty != null) {
                        if ("scim-username".equals(scimProperty.getName())) {
                            scimUserName = scimProperty.getValue();
                        } else if ("scim-password".equals(scimProperty.getName())) {
                            scimPassword = scimProperty.getValue();
                        } else if ("scim-user-ep".equals(scimProperty.getName())) {
                            scimUserEp = scimProperty.getValue();
                        } else if ("scim-group-ep".equals(scimProperty.getName())) {
                            scimGroupEp = scimProperty.getValue();
                        } else if ("scim-user-store-domain".equals(scimProperty.getName())) {
                            scimUserStoreDomain = scimProperty.getValue();
                        } else if ("scim-enable-pwd-provisioning".equals(scimProperty.getName())) {
                            isSCIMPwdProvEnabled = Boolean.parseBoolean(scimProperty.getValue());
                        } else if ("scim-default-pwd".equals(scimProperty.getName())) {
                            scimDefaultPwd = scimProperty.getValue();
                        } else if ("UniqueID".equals(scimProperty.getName())) {
                            scimUniqueID = scimProperty.getValue();
                        }
                    }
                }
            }

            if (scim.getEnabled()) {
                isScimProvEnabled = true;
            }

        }

        // Provisioning
        isGoogleProvEnabled = false;
        isGoogleProvDefault = false;
        googleDomainName = "";
        googleFamilyNameClaim = "";
        googleFamilyNameDefaultValue = "";
        googleGivenNameClaim = "";
        googleGivenNameDefaultValue = "";
        googlePrimaryEmailClaim = "";
        googleProvServiceAccEmail = "";
        googleProvAdminEmail = "";
        googleProvApplicationName = "";
        googleProvPattern = "";
        googleProvisioningSeparator = "";

        if (googleApps != null) {

            if (identityProvider.getDefaultProvisioningConnectorConfig() != null
                    && identityProvider.getDefaultProvisioningConnectorConfig().getName() != null) {
                isGoogleProvDefault = identityProvider.getDefaultProvisioningConnectorConfig().getName().equals(googleApps.getName());
            }

            Property[] googleProperties = googleApps.getProvisioningProperties();
            if (googleProperties != null && googleProperties.length > 0) {
                for (Property googleProperty : googleProperties) {
                    if (googleProperty != null) {
                        if ("google_prov_domain_name".equals(googleProperty.getName())) {
                            googleDomainName = googleProperty.getValue();
                        } else if ("google_prov_givenname".equals(googleProperty.getName())) {
                            googleGivenNameDefaultValue = googleProperty.getValue();
                        } else if ("google_prov_familyname".equals(googleProperty.getName())) {
                            googleFamilyNameDefaultValue = googleProperty.getValue();
                        } else if ("google_prov_service_acc_email".equals(googleProperty.getName())) {
                            googleProvServiceAccEmail = googleProperty.getValue();
                        } else if ("google_prov_admin_email".equals(googleProperty.getName())) {
                            googleProvAdminEmail = googleProperty.getValue();
                        } else if ("google_prov_application_name".equals(googleProperty.getName())) {
                            googleProvApplicationName = googleProperty.getValue();
                        } else if ("google_prov_email_claim_dropdown".equals(googleProperty.getName())) {
                            googlePrimaryEmailClaim = googleProperty.getValue();
                        } else if ("google_prov_givenname_claim_dropdown".equals(googleProperty.getName())) {
                            googleGivenNameClaim = googleProperty.getValue();
                        } else if ("google_prov_familyname_claim_dropdown".equals(googleProperty.getName())) {
                            googleFamilyNameClaim = googleProperty.getValue();
                        } else if ("google_prov_private_key".equals(googleProperty.getName())) {
                            googleProvPrivateKeyData = googleProperty.getValue();
                        } else if ("google_prov_pattern".equals(googleProperty.getName())) {
                            googleProvPattern = googleProperty.getValue();
                        } else if ("google_prov_separator".equals(googleProperty.getName())) {
                            googleProvisioningSeparator = googleProperty.getValue();
                        }
                    }
                }
            }

            if (googleApps.getEnabled()) {
                isGoogleProvEnabled = true;
            }
        }

        if (spml != null) {

            if (identityProvider.getDefaultProvisioningConnectorConfig() != null
                    && identityProvider.getDefaultProvisioningConnectorConfig().getName() != null) {
                isSpmlProvDefault = identityProvider.getDefaultProvisioningConnectorConfig().getName().equals(spml.getName());
            }

            Property[] spmlProperties = spml.getProvisioningProperties();
            if (spmlProperties != null && spmlProperties.length > 0) {
                for (Property spmlProperty : spmlProperties) {
                    if (spmlProperty != null) {
                        if ("spml-username".equals(spmlProperty.getName())) {
                            spmlUserName = spmlProperty.getValue();
                        } else if ("spml-password".equals(spmlProperty.getName())) {
                            spmlPassword = spmlProperty.getValue();
                        } else if ("spml-ep".equals(spmlProperty.getName())) {
                            spmlEndpoint = spmlProperty.getValue();
                        } else if ("spml-oc".equals(spmlProperty.getName())) {
                            spmlObjectClass = spmlProperty.getValue();
                        } else if ("UniqueID".equals(spmlProperty.getName())) {
                            spmlUniqueID = spmlProperty.getValue();
                        }
                    }
                }
            }

            if (spml.getEnabled()) {
                isSpmlProvEnabled = true;
            }
        }
    }

    if (idPName == null) {
        idPName = "";
    }

    if (realmId == null) {
        realmId = "";
    }

    if (idpDisplayName == null) {
        idpDisplayName = "";
    }
    if (description == null) {
        description = "";
    }

    if (provisioningRole == null) {
        provisioningRole = "";
    }

    if (passiveSTSQueryParam == null) {
        passiveSTSQueryParam = "";
    }

    if (oidcQueryParam == null) {
        oidcQueryParam = "";
    }
    if (StringUtils.isBlank(idPAlias)) {
        idPAlias = IdentityUtil.getServerURL("/oauth2/token", true, false);
    }
    String provisionStaticDropdownDisabled = "";
    String provisionDynamicDropdownDisabled = "";

    if (!isProvisioningEnabled) {
        provisionStaticDropdownDisabled = "disabled=\'disabled\'";
        provisionDynamicDropdownDisabled = "disabled=\'disabled\'";
    } else if (isProvisioningEnabled && provisioningUserStoreId != null) {
        provisionDynamicDropdownDisabled = "disabled=\'disabled\'";
    } else if (isProvisioningEnabled && provisioningUserStoreId == null) {
        provisionStaticDropdownDisabled = "disabled=\'disabled\'";
    }

    userStoreDomains = client.getUserStoreDomains();

    claimUris = client.getAllLocalClaimUris();

    Iterator<FederatedAuthenticatorConfig> fedAuthConfigIterator = allFedAuthConfigs.values().iterator();
    while (fedAuthConfigIterator.hasNext()) {
        FederatedAuthenticatorConfig fedAuthConfig = fedAuthConfigIterator.next();
        if (fedAuthConfig.getDisplayName().equals(IdentityApplicationConstants.Authenticator.OpenID.NAME)) {
            isOpenidAuthenticatorActive = true;
            fedAuthConfigIterator.remove();
        } else if (fedAuthConfig.getDisplayName().equals(IdentityApplicationConstants.Authenticator.SAML2SSO.NAME)) {
            isSamlssoAuthenticatorActive = true;
            fedAuthConfigIterator.remove();
        } else if (fedAuthConfig.getDisplayName().equals(IdentityApplicationConstants.Authenticator.OIDC.NAME)) {
            isOpenidconnectAuthenticatorActive = true;
            fedAuthConfigIterator.remove();
        } else if (fedAuthConfig.getDisplayName().equals(IdentityApplicationConstants.Authenticator.PassiveSTS.NAME)) {
            isPassivestsAuthenticatorActive = true;
            fedAuthConfigIterator.remove();
        } else if (fedAuthConfig.getDisplayName().equals(IdentityApplicationConstants.Authenticator.Facebook.NAME)) {
            isFacebookAuthenticatorActive = true;
            fedAuthConfigIterator.remove();
        }
    }

    String openIdEnabledChecked = "";
    String openIdDefaultDisabled = "";
    if (identityProvider != null) {
        if (isOpenIdEnabled) {
            openIdEnabledChecked = "checked=\'checked\'";
        } else {
            openIdDefaultDisabled = "disabled=\'disabled\'";
        }
    }
    String openIdDefaultChecked = "";

    if (identityProvider != null) {
        if (isOpenIdDefault) {
            openIdDefaultChecked = "checked=\'checked\'";
            openIdDefaultDisabled = "disabled=\'disabled\'";
        }
    }
    if (StringUtils.isBlank(openIdUrl)) {
        openIdUrl = StringUtils.EMPTY;
    }

    String saml2SSOEnabledChecked = "";
    String saml2SSODefaultDisabled = "";
    if (identityProvider != null) {
        if (isSAML2SSOEnabled) {
            saml2SSOEnabledChecked = "checked=\'checked\'";
        } else {
            saml2SSODefaultDisabled = "disabled=\'disabled\'";
        }
    }
    String saml2SSODefaultChecked = "";
    if (identityProvider != null) {
        if (isSAMLSSODefault) {
            saml2SSODefaultChecked = "checked=\'checked\'";
            saml2SSODefaultDisabled = "disabled=\'disabled\'";
        }
    }
    if (idPEntityId == null) {
        idPEntityId = "";
    }
    if (spEntityId == null) {
        spEntityId = "";
    }

    if (StringUtils.isBlank(nameIdFormat)) {

        // check whether a global default value for NameIdType is set in the application-authentication.xml file
        AuthenticatorConfig authenticatorConfig = FileBasedConfigurationBuilder.getInstance()
                .getAuthenticatorConfigMap().get(IdentityApplicationConstants.Authenticator.SAML2SSO.NAME);

        if (authenticatorConfig != null) {
            nameIdFormat = authenticatorConfig.getParameterMap()
                    .get(IdentityApplicationConstants.Authenticator.SAML2SSO.NAME_ID_TYPE);
        }

        if (StringUtils.isBlank(nameIdFormat)) {
            // Going with the default value
            nameIdFormat = IdentityApplicationConstants.Authenticator.SAML2SSO.UNSPECIFIED_NAME_ID_FORMAT;
        }
    }

    if (StringUtils.isBlank(ssoUrl)) {
        ssoUrl = StringUtils.EMPTY;
    }
    String authnRequestSignedChecked = "";
    if (identityProvider != null) {
        if (isAuthnRequestSigned) {
            authnRequestSignedChecked = "checked=\'checked\'";
        }
    }

    String enableAssertinEncriptionChecked = "";
    if (identityProvider != null) {
        if (isEnableAssertionEncription) {
            enableAssertinEncriptionChecked = "checked=\'checked\'";
        }
    }

    String enableAssertionSigningChecked = "";
    if (identityProvider != null) {
        if (isEnableAssertionSigning) {
            enableAssertionSigningChecked = "checked=\'checked\'";
        }
    }

    String sloEnabledChecked = "";
    if (identityProvider != null) {
        if (isSLOEnabled) {
            sloEnabledChecked = "checked=\'checked\'";
        }
    }
    if (StringUtils.isBlank(logoutUrl)) {
        logoutUrl = StringUtils.EMPTY;
    }
    String logoutRequestSignedChecked = "";
    if (identityProvider != null) {
        if (isLogoutRequestSigned) {
            logoutRequestSignedChecked = "checked=\'checked\'";
        }
    }
    String authnResponseSignedChecked = "";
    if (identityProvider != null) {
        if (isAuthnResponseSigned) {
            authnResponseSignedChecked = "checked=\'checked\'";
        }
    }

    String signAlgoDropdownDisabled = "";
    if (!isAuthnRequestSigned) {
        signAlgoDropdownDisabled = "disabled=\'disabled\'";
    }

    String digestAlgoDropdownDisabled = "";
    if (!isAuthnRequestSigned) {
        digestAlgoDropdownDisabled = "disabled=\'disabled\'";
    }

    String authnContextClassRefDropdownDisabled = "";
    String authnContextComparisonDropdownDisabled = "";
    if ("no".equals(includeAuthenticationContext)) {
        authnContextClassRefDropdownDisabled = "disabled=\'disabled\'";
        authnContextComparisonDropdownDisabled = "disabled=\'disabled\'";
    }

    String includeNameIdPolicyChecked = "";
    if (identityProvider != null) {
        if (includeNameIdPolicy) {
            includeNameIdPolicyChecked = "checked=\'checked\'";
        }
    }

    String includeCertChecked = "";
    if (identityProvider != null) {
        if (includeCert) {
            includeCertChecked = "checked=\'checked\'";
        }
    }

    String includeProtocolBindingChecked = "";
    if (identityProvider != null) {
        if (includeProtocolBinding) {
            includeProtocolBindingChecked = "checked=\'checked\'";
        }
    }

    if (attributeConsumingServiceIndex == null) {
        attributeConsumingServiceIndex = "";
    }

    String oidcEnabledChecked = "";
    String oidcDefaultDisabled = "";
    if (identityProvider != null) {
        if (isOIDCEnabled) {
            oidcEnabledChecked = "checked=\'checked\'";
        } else {
            oidcDefaultDisabled = "disabled=\'disabled\'";
        }
    }
    String oidcDefaultChecked = "";

    if (identityProvider != null) {
        if (isOIDCDefault) {
            oidcDefaultChecked = "checked=\'checked\'";
            oidcDefaultDisabled = "disabled=\'disabled\'";
        }
    }
    if (clientId == null) {
        clientId = "";
    }
    if (clientSecret == null) {
        clientSecret = "";
    }
    if (StringUtils.isBlank(authzUrl)) {
        authzUrl = StringUtils.EMPTY;
    }
    if (StringUtils.isBlank(tokenUrl)) {
        tokenUrl = StringUtils.EMPTY;
    }

    if (StringUtils.isBlank(callBackUrl)) {
        callBackUrl = IdentityUtil.getServerURL(IdentityApplicationConstants.COMMONAUTH, true, true);
    }

    if (StringUtils.isBlank(userInfoEndpoint)) {
        userInfoEndpoint = StringUtils.EMPTY;
    }

    String passiveSTSEnabledChecked = "";
    String passiveSTSDefaultDisabled = "";
    if (identityProvider != null) {
        if (isPassiveSTSEnabled) {
            passiveSTSEnabledChecked = "checked=\'checked\'";
        } else {
            passiveSTSDefaultDisabled = "disabled=\'disabled\'";
        }
    }
    String passiveSTSDefaultChecked = "";
    if (identityProvider != null) {
        if (isPassiveSTSDefault) {
            passiveSTSDefaultChecked = "checked=\'checked\'";
            passiveSTSDefaultDisabled = "disabled=\'disabled\'";
        }
    }
    if (passiveSTSRealm == null) {
        passiveSTSRealm = "";
    }
    if (StringUtils.isBlank(passiveSTSUrl)) {
        passiveSTSUrl = StringUtils.EMPTY;
    }

    String enablePassiveSTSAssertionSignatureValidationChecked = "";
    if (identityProvider != null) {
        if (isEnablePassiveSTSAssertionSignatureValidation) {
            enablePassiveSTSAssertionSignatureValidationChecked = "checked=\'checked\'";
        }
    }

    String enablePassiveSTSAssertionAudienceValidationChecked = "";
    if (identityProvider != null) {
        if (isEnablePassiveSTSAssertionAudienceValidation) {
            enablePassiveSTSAssertionAudienceValidationChecked = "checked=\'checked\'";
        }
    }

    String fbAuthEnabledChecked = "";
    String fbAuthDefaultDisabled = "";

    if (identityProvider != null) {
        if (isFBAuthEnabled) {
            fbAuthEnabledChecked = "checked=\'checked\'";
        } else {
            fbAuthDefaultDisabled = "disabled=\'disabled\'";
        }
    }
    String fbAuthDefaultChecked = "";
    if (identityProvider != null) {
        if (isFBAuthDefault) {
            fbAuthDefaultChecked = "checked=\'checked\'";
            fbAuthDefaultDisabled = "disabled=\'disabled\'";
        }
    }
    if (fbClientId == null) {
        fbClientId = "";
    }
    if (fbClientSecret == null) {
        fbClientSecret = "";
    }
    if (fbScope == null) {
        fbScope = "email";
    }
    if (fbUserInfoFields == null) {
        fbUserInfoFields = "";
    }
    if (fbCallBackUrl == null) {
        fbCallBackUrl = "";
    }
    String fbUserIdInClaims = "";
    if (identityProvider != null) {
        if (isFBUserIdInClaims) {
            fbUserIdInClaims = "checked=\'checked\'";
        }
    }
    if (fbAuthnEndpoint == null) {
        fbAuthnEndpoint = IdentityApplicationConstants.FB_AUTHZ_URL;
    }
    if (fbOauth2TokenEndpoint == null) {
        fbOauth2TokenEndpoint = IdentityApplicationConstants.FB_TOKEN_URL;
    }
    if (fbUserInfoEndpoint == null) {
        fbUserInfoEndpoint = IdentityApplicationConstants.FB_USER_INFO_URL;
    }


    // Out-bound Provisioning
    String googleProvEnabledChecked = "";
    String googleProvDefaultDisabled = "";
    String googleProvDefaultChecked = "disabled=\'disabled\'";

    if (identityProvider != null) {
        if (isGoogleProvEnabled) {
            googleProvEnabledChecked = "checked=\'checked\'";
            googleProvDefaultChecked = "";
            if (isGoogleProvDefault) {
                googleProvDefaultChecked = "checked=\'checked\'";
            }
        }
    }

    if (googleDomainName == null) {
        googleDomainName = "";
    }
    if (googleGivenNameDefaultValue == null) {
        googleGivenNameDefaultValue = "";
    }
    if (googleFamilyNameClaim == null) {
        googleFamilyNameClaim = "";
    }
    if (googleFamilyNameDefaultValue == null) {
        googleFamilyNameDefaultValue = "";
    }
    if (googleProvServiceAccEmail == null) {
        googleProvServiceAccEmail = "";
    }
    if (googleProvAdminEmail == null) {
        googleProvAdminEmail = "";
    }
    if (googleProvApplicationName == null) {
        googleProvApplicationName = "";
    }

    if (googleProvPattern == null) {
        googleProvPattern = "";
    }

    if (googleProvisioningSeparator == null) {
        googleProvisioningSeparator = "";
    }

    String spmlProvEnabledChecked = "";
    String spmlProvDefaultDisabled = "";
    String spmlProvDefaultChecked = "disabled=\'disabled\'";


    if (identityProvider != null) {
        if (isSpmlProvEnabled) {
            spmlProvEnabledChecked = "checked=\'checked\'";
            spmlProvDefaultChecked = "";
            if (isSpmlProvDefault) {
                spmlProvDefaultChecked = "checked=\'checked\'";
            }
        }
    }

    if (spmlUserName == null) {
        spmlUserName = "";
    }
    if (spmlPassword == null) {
        spmlPassword = "";
    }
    if (spmlEndpoint == null) {
        spmlEndpoint = "";
    }
    if (spmlObjectClass == null) {
        spmlObjectClass = "";
    }

    String scimProvEnabledChecked = "";
    String scimProvDefaultDisabled = "";
    String scimPwdProvEnabledChecked = "";
    String scimProvDefaultChecked = "disabled=\'disabled\'";
    if (identityProvider != null) {
        if (isScimProvEnabled) {
            scimProvEnabledChecked = "checked=\'checked\'";
            scimProvDefaultChecked = "";
            if (isScimProvDefault) {
                scimProvDefaultChecked = "checked=\'checked\'";
            }
        }
        if (isSCIMPwdProvEnabled) {
            scimPwdProvEnabledChecked = "checked=\'checked\'";
            disableDefaultPwd = "disabled=\'disabled\'";
        }
    }

    // If SCIM Provisioning has not been Configured at all,
    // make password provisioning enable by default.
    // Since scimUserName is a required field,
    // it being blank means that SCIM Provisioning has not been configured at all.
    if (scimUserName == null) {
        scimUserName = "";
        scimPwdProvEnabledChecked = "checked=\'checked\'";
        disableDefaultPwd = "disabled=\'disabled\'";
    }
    if (scimPassword == null) {
        scimPassword = "";
    }
    if (scimGroupEp == null) {
        scimGroupEp = "";
    }
    if (scimUserEp == null) {
        scimUserEp = "";
    }
    if (scimUserStoreDomain == null) {
        scimUserStoreDomain = "";
    }
    if (scimDefaultPwd == null) {
        scimDefaultPwd = "";
    }

%>

<script>

    var claimMappinRowID = -1;
    var claimMappinRowIDSPML = -1;
    var advancedClaimMappinRowID = -1;
    var roleRowId = -1;
    var claimRowId = -1;

    <% if(identityProviderClaims != null){ %>
    claimRowId = <%=identityProviderClaims.length-1%>;
    <% } %>

    <% if(roles != null){ %>
    roleRowId = <%=roles.length-1%>;
    <% } %>

    <% if(claimMappings != null){ %>
    advancedClaimMappinRowID = <%=claimMappings.length-1%>;
    <% } %>


    var claimURIDropdownPopulator = function () {
        var $user_id_claim_dropdown = jQuery('#user_id_claim_dropdown');
        var $role_claim_dropdown = jQuery('#role_claim_dropdown');
        var $google_prov_email_claim_dropdown = jQuery('#google_prov_email_claim_dropdown');
        var $google_prov_familyname_claim_dropdown = jQuery('#google_prov_familyname_claim_dropdown');
        var $google_prov_givenname_claim_dropdown = jQuery('#google_prov_givenname_claim_dropdown');
        var $idpClaimsList2 = jQuery('#idpClaimsList2');


        $user_id_claim_dropdown.empty();
        $role_claim_dropdown.empty();
        $google_prov_email_claim_dropdown.empty();
        $google_prov_familyname_claim_dropdown.empty();
        $google_prov_givenname_claim_dropdown.empty();
        $idpClaimsList2.empty();


        if ('<%=userIdClaimURI%>' == '') {
            $user_id_claim_dropdown.append('<option value = "">--- Select Claim URI ---</option>');
        } else {
            $user_id_claim_dropdown.append('<option selected="selected" value = "">--- Select Claim URI ---</option>');
        }

        if ('<%=roleClaimURI%>' == '') {
            $role_claim_dropdown.append('<option value = "">--- Select Claim URI ---</option>');
        } else {
            $role_claim_dropdown.append('<option selected="selected" value = "">--- Select Claim URI ---</option>');
        }


        if ('<%=googlePrimaryEmailClaim%>' == '') {
            $google_prov_email_claim_dropdown.append('<option value = "">--- Select Claim URI ---</option>');
        } else {
            $google_prov_email_claim_dropdown.append('<option selected="selected" value = "">--- Select Claim URI ---</option>');
        }

        if ('<%=googleFamilyNameClaim%>' == '') {
            $google_prov_familyname_claim_dropdown.append('<option value = "">--- Select Claim URI ---</option>');
        } else {
            $google_prov_familyname_claim_dropdown.append('<option selected="selected" value = "">--- Select Claim URI ---</option>');
        }

        if ('<%=googleGivenNameClaim%>' == '') {
            $google_prov_givenname_claim_dropdown.append('<option value = "">--- Select Claim URI ---</option>');
        } else {
            $google_prov_givenname_claim_dropdown.append('<option selected="selected" value = "">--- Select Claim URI ---</option>');
        }

        $idpClaimsList2.append('<option value = "" >--- Select Claim URI ---</option>');

        jQuery('#claimAddTable .claimrow').each(function () {
            if ($(this).val().trim() != "") {
                var val = htmlEncode($(this).val());
                if (val == '<%=userIdClaimURI%>') {
                    $user_id_claim_dropdown.append('<option selected="selected">' + val + '</option>');
                } else {
                    $user_id_claim_dropdown.append('<option>' + val + '</option>');
                }
                if (val == '<%=roleClaimURI%>') {
                    $role_claim_dropdown.append('<option selected="selected">' + val + '</option>');
                } else {
                    $role_claim_dropdown.append('<option>' + val + '</option>');
                }

                if (val == '<%=googlePrimaryEmailClaim%>') {
                    $google_prov_email_claim_dropdown.append('<option selected="selected">' + val + '</option>');
                } else {
                    $google_prov_email_claim_dropdown.append('<option>' + val + '</option>');
                }

                if (val == '<%=googleFamilyNameClaim%>') {
                    $google_prov_familyname_claim_dropdown.append('<option selected="selected">' + val + '</option>');
                } else {
                    $google_prov_familyname_claim_dropdown.append('<option>' + val + '</option>');
                }

                if (val == '<%=googleGivenNameClaim%>') {
                    $google_prov_givenname_claim_dropdown.append('<option selected="selected">' + val + '</option>');
                } else {
                    $google_prov_givenname_claim_dropdown.append('<option>' + val + '</option>');
                }

                $idpClaimsList2.append('<option>' + val + '</option>');

            }
        })

        var selectedVal = "";
        var selected = $("input[type='radio'][name='choose_dialet_type_group']:checked");
        if (selected.length > 0) {
            selectedVal = selected.val();
        }

        if (selectedVal == "choose_dialet_type1") {
            $(".customClaim").hide();
            var option = '<option value="">---Select Claim URI ---</option>';
            $user_id_claim_dropdown.empty();
            $role_claim_dropdown.empty();
            $google_prov_email_claim_dropdown.empty();
            $google_prov_familyname_claim_dropdown.empty();
            $google_prov_givenname_claim_dropdown.empty();
            $idpClaimsList2.empty();


            var user_id_option = '<option value="">---Select Claim URI ---</option>';

            <% for(int i =0 ; i< claimUris.length ; i++){

           		 if(claimUris[i].equals(userIdClaimURI)){  %>
            user_id_option += '<option  selected="selected" value="' + "<%=claimUris[i]%>" + '">' + "<%=claimUris[i]%>" + '</option>';
            <% 	 } else {  %>
            user_id_option += '<option value="' + "<%=claimUris[i]%>" + '">' + "<%=claimUris[i]%>" + '</option>';
            <%	 }
            }%>


            var google_prov_email_option = '<option value="">---Select Claim URI ---</option>';

            <% for(int i =0 ; i< claimUris.length ; i++){

           		 if(claimUris[i].equals(googlePrimaryEmailClaim)){  %>
            google_prov_email_option += '<option  selected="selected" value="' + "<%=claimUris[i]%>" + '">' + "<%=claimUris[i]%>" + '</option>';
            <% 	 } else {  %>
            google_prov_email_option += '<option value="' + "<%=claimUris[i]%>" + '">' + "<%=claimUris[i]%>" + '</option>';
            <%	 }
            }%>


            var google_prov_family_email_option = '<option value="">---Select Claim URI ---</option>';

            <% for(int i =0 ; i< claimUris.length ; i++){

           		 if(claimUris[i].equals(googleFamilyNameClaim)){  %>
            google_prov_family_email_option += '<option  selected="selected" value="' + "<%=claimUris[i]%>" + '">' + "<%=claimUris[i]%>" + '</option>';
            <% 	 } else {  %>
            google_prov_family_email_option += '<option value="' + "<%=claimUris[i]%>" + '">' + "<%=claimUris[i]%>" + '</option>';
            <%	 }
            }%>


            var google_prov_givenname_option = '<option value="">---Select Claim URI ---</option>';

            <% for(int i =0 ; i< claimUris.length ; i++){

           		 if(claimUris[i].equals(googleGivenNameClaim)){  %>
            google_prov_givenname_option += '<option  selected="selected" value="' + "<%=claimUris[i]%>" + '">' + "<%=claimUris[i]%>" + '</option>';
            <% 	 } else {  %>
            google_prov_givenname_option += '<option value="' + "<%=claimUris[i]%>" + '">' + "<%=claimUris[i]%>" + '</option>';
            <%	 }
            }%>


            <% for(int i =0 ; i< claimUris.length ; i++){%>
            option += '<option value="' + "<%=claimUris[i]%>" + '">' + "<%=claimUris[i]%>" + '</option>';

            <%}%>


            $user_id_claim_dropdown.append(user_id_option);
            $role_claim_dropdown.append('<option value="http://wso2.org/claims/role">http://wso2.org/claims/role</option>');
            $google_prov_email_claim_dropdown.append(google_prov_email_option);
            $google_prov_familyname_claim_dropdown.append(google_prov_family_email_option);
            $google_prov_givenname_claim_dropdown.append(google_prov_givenname_option);
            $idpClaimsList2.append(option);


            $(".role_claim").hide();
            $(jQuery('#claimAddTable')).hide();

            if ($(jQuery('#advancedClaimMappingAddTable tr')).length > 1) {
                $(jQuery('#advancedClaimMappingAddTable')).show();
            }
        }

        if (selectedVal == "choose_dialet_type2") {
            var option = '';

            <% for(int i =0 ; i< claimUris.length ; i++){%>
            option += '<option value="' + "<%=claimUris[i]%>" + '">' + "<%=claimUris[i]%>" + '</option>';

            <%}%>

            $user_id_claim_dropdown.replace($option, "");
            $role_claim_dropdown.replace('<option value="http://wso2.org/claims/role">http://wso2.org/claims/role</option>', "");
            $google_prov_email_claim_dropdown.replace($option, "");
            $google_prov_familyname_claim_dropdown.replace($option, "");
            $google_prov_givenname_claim_dropdown.replace($option, "");
            $idpClaimsList2.replace($option, "");


            $(".role_claim").show();

            if ($(jQuery('#claimAddTable tr')).length == 2) {
                $(jQuery('#claimAddTable')).toggle();
            }

            if ($(jQuery('#advancedClaimMappingAddTable tr')).length > 1) {
                $(jQuery('#advancedClaimMappingAddTable')).show();
            }

        }
    };


    jQuery(document).ready(function () {
        jQuery('#outBoundAuth').hide();
        jQuery('#inBoundProvisioning').hide();
        jQuery('#outBoundProvisioning').hide();
        jQuery('#roleConfig').hide();
        jQuery('#claimConfig').hide();
        jQuery('#openIdLinkRow').hide();
        jQuery('#saml2SSOLinkRow').hide();
        jQuery('#oauth2LinkRow').hide();
        jQuery('#passiveSTSLinkRow').hide();
        jQuery('#fbAuthLinkRow').hide();
        jQuery('#baisClaimLinkRow').hide();
        jQuery('#advancedClaimLinkRow').hide();
        jQuery('#openIdDefault').attr('disabled', 'disabled');
        jQuery('#saml2SSODefault').attr('disabled', 'disabled');
        jQuery('#oidcDefault').attr('disabled', 'disabled');
        jQuery('#passiveSTSDefault').attr('disabled', 'disabled');
        jQuery('#fbAuthDefault').attr('disabled', 'disabled');
        jQuery('#googleProvDefault').attr('disabled', 'disabled');
        jQuery('#sfProvDefault').attr('disabled', 'disabled');
        jQuery('#scimProvDefault').attr('disabled', 'disabled');
        jQuery('#spmlProvDefault').attr('disabled', 'disabled');
        jQuery('#openIdDefault').attr('disabled', 'disabled');
        jQuery('#saml2SSODefault').attr('disabled', 'disabled');
        jQuery('#oidcDefault').attr('disabled', 'disabled');
        jQuery('#passiveSTSDefault').attr('disabled', 'disabled');
        jQuery('#fbAuthDefault').attr('disabled', 'disabled');

        if ($(jQuery('#claimMappingAddTable tr')).length < 2) {
            $(jQuery('#claimMappingAddTable')).hide();
        }

        if ($(jQuery('#claimMappingAddTableSPML tr')).length < 2) {
            $(jQuery('#claimMappingAddTableSPML')).hide();
        }


        if (<%=isOpenIdEnabled%>) {
            jQuery('#openid_enable_logo').show();
        } else {
            jQuery('#openid_enable_logo').hide();
        }

        if (<%=isSAML2SSOEnabled%>) {
            jQuery('#sampl2sso_enable_logo').show();
        } else {
            jQuery('#sampl2sso_enable_logo').hide();
        }

        if (<%=isOIDCEnabled%>) {
            jQuery('#oAuth2_enable_logo').show();
        } else {
            jQuery('#oAuth2_enable_logo').hide();
        }

        if (<%=isPassiveSTSEnabled%>) {
            jQuery('#wsfederation_enable_logo').show();
        } else {
            jQuery('#wsfederation_enable_logo').hide();
        }

        if (<%=isFBAuthEnabled%>) {
            jQuery('#fecebook_enable_logo').show();
        } else {
            jQuery('#fecebook_enable_logo').hide();
        }

        if (<%=isGoogleProvEnabled%>) {
            jQuery('#google_enable_logo').show();
        } else {
            jQuery('#google_enable_logo').hide();
        }

        if (<%=isScimProvEnabled%>) {
            jQuery('#scim_enable_logo').show();
        } else {
            jQuery('#scim_enable_logo').hide();
        }

        if (<%=isSpmlProvEnabled%>) {
            jQuery('#spml_enable_logo').show();
        } else {
            jQuery('#spml_enable_logo').hide();
        }

        jQuery('h2.trigger').click(function () {
            if (jQuery(this).next().is(":visible")) {
                this.className = "active trigger";
            } else {
                this.className = "trigger";
            }
            jQuery(this).next().slideToggle("fast");
            return false; //Prevent the browser jump to the link anchor
        })
        jQuery('#publicCertDeleteLink').click(function () {
            $(jQuery('#publicCertDiv')).toggle();
            var input = document.createElement('input');
            input.type = "hidden";
            input.name = "deletePublicCert";
            input.id = "deletePublicCert";
            input.value = "true";
            document.forms['idp-mgt-edit-form'].appendChild(input);
        })
        jQuery('#claimAddLink').click(function () {

            claimRowId++;
            var option = '<option value="">---Select Claim URI ---</option>';

            <% for(int i =0 ; i< claimUris.length ; i++){%>
            option += '<option value="' + "<%=claimUris[i]%>" + '">' + "<%=claimUris[i]%>" + '</option>';

            <%}%>

            $("#claimrow_id_count").val(claimRowId + 1);


            var newrow = jQuery('<tr><td><input class="claimrow" style=" width: 90%; " type="text" id="claimrowid_' + claimRowId + '" name="claimrowname_' + claimRowId + '"/></td>' +
                    '<td><select class="claimrow_wso2" name="claimrow_name_wso2_' + claimRowId + '">' + option + '</select></td> ' +
                    '<td><a onclick="deleteClaimRow(this)" class="icon-link" ' +
                    'style="background-image: url(images/delete.gif)">' +
                    'Delete' +
                    '</a></td></tr>');
            jQuery('.claimrow', newrow).blur(function () {
                claimURIDropdownPopulator();
            });
            jQuery('#claimAddTable').append(newrow);
            if ($(jQuery('#claimAddTable tr')).length == 2) {
                $(jQuery('#claimAddTable')).toggle();
            }

        })

        claimURIDropdownPopulator();

        var $signature_algorithem_dropdown = jQuery('#signature_algorithem_dropdown');
        var $digest_algorithem_dropdown = jQuery('#digest_algorithem_dropdown');
        var $authentication_context_class_dropdown = jQuery('#authentication_context_class_dropdown');
        var $auth_context_comparison_level_dropdown = jQuery('#auth_context_comparison_level_dropdown');

        jQuery('#authentication_context_class_dropdown').change(function () {
            var selectedClass = $("#authentication_context_class_dropdown").val();
            if (selectedClass == '<%=IdentityApplicationConstants.Authenticator.SAML2SSO.CUSTOM_AUTHENTICATION_CONTEXT_CLASS_OPTION%>') {
                jQuery('#custom_authentication_context_class').removeAttr('disabled');
            } else {
                jQuery('#custom_authentication_context_class').val("");
                jQuery('#custom_authentication_context_class').attr('disabled', true);
            }
        });
    })


    function idpMgtUpdate() {
        document.getElementById("meta_data_saml").value = "";
        if (doValidation()) {
            var allDeletedClaimStr = "";
            for (var i = 0; i < deleteClaimRows.length; i++) {
                if (i < deleteClaimRows.length - 1) {
                    allDeletedClaimStr += deleteClaimRows[i] + ", ";
                } else {
                    allDeletedClaimStr += deleteClaimRows[i] + "?";
                }
            }
            var allDeletedRoleStr = "";
            for (var i = 0; i < deletedRoleRows.length; i++) {
                if (i < deletedRoleRows.length - 1) {
                    allDeletedRoleStr += deletedRoleRows[i] + ", ";
                } else {
                    allDeletedRoleStr += deletedRoleRows[i] + "?";
                }
            }

            if (jQuery('#deletePublicCert').val() == 'true') {
                var confirmationMessage = 'Are you sure you want to delete the public certificate of ' +
                        jQuery('#idPName').val() + '?';
                if (jQuery('#certFile').val() != '') {
                    confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                }
                CARBON.showConfirmationDialog(confirmationMessage,
                        function () {
                            if (allDeletedClaimStr != "") {
                                CARBON.showConfirmationDialog('Are you sure you want to delete the claim URI(s) ' +
                                        allDeletedClaimStr,
                                        function () {
                                            if (allDeletedRoleStr != "") {
                                                CARBON.showConfirmationDialog('Are you sure you want to delete the ' +
                                                        'role(s) ' + allDeletedRoleStr,
                                                        function () {
                                                            if (jQuery('#deleteClaimMappings').val() == 'true') {
                                                                var confirmationMessage = 'Are you sure you want to ' +
                                                                        'delete the Claim URI Mappings of ' +
                                                                        jQuery('#idPName').val() + '?';
                                                                if (jQuery('#claimMappingFile').val() != '') {
                                                                    confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                                }
                                                                CARBON.showConfirmationDialog(confirmationMessage,
                                                                        function () {
                                                                            if (jQuery('#deleteRoleMappings').val() == 'true') {
                                                                                var confirmationMessage = 'Are you sure you want to ' +
                                                                                        'delete the Role Mappings of ' +
                                                                                        jQuery('#idPName').val() + '?';
                                                                                if (jQuery('#roleMappingFile').val() != '') {
                                                                                    confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                                                }
                                                                                CARBON.showConfirmationDialog(confirmationMessage,
                                                                                        function () {
                                                                                            doEditFinish();
                                                                                        },
                                                                                        function () {
                                                                                            location.href =
                                                                                                    "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                                                        });
                                                                            } else {
                                                                                doEditFinish();
                                                                            }
                                                                        },
                                                                        function () {
                                                                            location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                                        });
                                                            } else {
                                                                if (jQuery('#deleteRoleMappings').val() == 'true') {
                                                                    var confirmationMessage = 'Are you sure you want to ' +
                                                                            'delete the Role Mappings of ' +
                                                                            jQuery('#idPName').val() + '?';
                                                                    if (jQuery('#roleMappingFile').val() != '') {
                                                                        confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                                    }
                                                                    CARBON.showConfirmationDialog(confirmationMessage,
                                                                            function () {
                                                                                doEditFinish();
                                                                            },
                                                                            function () {
                                                                                location.href =
                                                                                        "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                                            });
                                                                } else {
                                                                    doEditFinish();
                                                                }
                                                            }
                                                        },
                                                        function () {
                                                            location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                        });
                                            } else {
                                                if (jQuery('#deleteClaimMappings').val() == 'true') {
                                                    var confirmationMessage = 'Are you sure you want to ' +
                                                            'delete the Claim URI mappings of ' +
                                                            jQuery('#idPName').val() + '?';
                                                    if (jQuery('#claimMappingFile').val() != '') {
                                                        confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                    }
                                                    CARBON.showConfirmationDialog(confirmationMessage,
                                                            function () {

                                                            },
                                                            function () {
                                                                location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                            });
                                                } else {
                                                    if (jQuery('#deleteRoleMappings').val() == 'true') {
                                                        var confirmationMessage = 'Are you sure you want to ' +
                                                                'delete the Role Mappings of ' +
                                                                jQuery('#idPName').val() + '?';
                                                        if (jQuery('#roleMappingFile').val() != '') {
                                                            confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                        }
                                                        CARBON.showConfirmationDialog(confirmationMessage,
                                                                function () {
                                                                    doEditFinish();
                                                                },
                                                                function () {
                                                                    location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                                });
                                                    } else {
                                                        doEditFinish();
                                                    }
                                                }
                                            }
                                        },
                                        function () {
                                            location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                        });
                            } else {
                                if (allDeletedRoleStr != "") {
                                    CARBON.showConfirmationDialog('Are you sure you want to delete the ' +
                                            'role(s) ' + allDeletedRoleStr,
                                            function () {
                                                if (jQuery('#deleteClaimMappings').val() == 'true') {
                                                    var confirmationMessage = 'Are you sure you want to ' +
                                                            'delete the Claim URI mappings of ' +
                                                            jQuery('#idPName').val() + '?';
                                                    if (jQuery('#claimMappingFile').val() != '') {
                                                        confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                    }
                                                    CARBON.showConfirmationDialog(confirmationMessage,
                                                            function () {

                                                            },
                                                            function () {
                                                                location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                            });
                                                } else {
                                                    if (jQuery('#deleteRoleMappings').val() == 'true') {
                                                        var confirmationMessage = 'Are you sure you want to ' +
                                                                'delete the Role Mappings of ' +
                                                                jQuery('#idPName').val() + '?';
                                                        if (jQuery('#roleMappingFile').val() != '') {
                                                            confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                        }
                                                        CARBON.showConfirmationDialog(confirmationMessage,
                                                                function () {
                                                                    doEditFinish();
                                                                },
                                                                function () {
                                                                    location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                                });
                                                    } else {
                                                        doEditFinish();
                                                    }
                                                }
                                            },
                                            function () {
                                                location.href = "idp-mgt-edit.jsp?idPName=Encode.forUriComponent(idPName)%>";
                                            });
                                } else {
                                    if (jQuery('#deleteClaimMappings').val() == 'true') {
                                        var confirmationMessage = 'Are you sure you want to ' +
                                                'delete the Claim URI mappings of ' +
                                                jQuery('#idPName').val() + '?';
                                        if (jQuery('#claimMappingFile').val() != '') {
                                            confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                        }
                                        CARBON.showConfirmationDialog(confirmationMessage,
                                                function () {
                                                    if (jQuery('#deleteRoleMappings').val() == 'true') {
                                                        var confirmationMessage = 'Are you sure you want to ' +
                                                                'delete the Role Mappings of ' +
                                                                jQuery('#idPName').val() + '?';
                                                        if (jQuery('#roleMappingFile').val() != '') {
                                                            confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                        }
                                                        CARBON.showConfirmationDialog(confirmationMessage,
                                                                function () {
                                                                    doEditFinish();
                                                                },
                                                                function () {
                                                                    location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                                });
                                                    } else {
                                                        doEditFinish();
                                                    }
                                                },
                                                function () {
                                                    location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                });
                                    } else {
                                        if (jQuery('#deleteRoleMappings').val() == 'true') {
                                            var confirmationMessage = 'Are you sure you want to ' +
                                                    'delete the Role Mappings of ' +
                                                    jQuery('#idPName').val() + '?';
                                            if (jQuery('#roleMappingFile').val() != '') {
                                                confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                            }
                                            CARBON.showConfirmationDialog(confirmationMessage,
                                                    function () {
                                                        doEditFinish();
                                                    },
                                                    function () {
                                                        location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                    });
                                        } else {
                                            doEditFinish();
                                        }
                                    }
                                }
                            }
                        },
                        function () {
                            location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                        });
            } else {
                if (allDeletedClaimStr != "") {
                    CARBON.showConfirmationDialog('Are you sure you want to delete the claim URI(s) ' +
                            allDeletedClaimStr,
                            function () {
                                if (allDeletedRoleStr != "") {
                                    CARBON.showConfirmationDialog('Are you sure you want to delete the ' +
                                            'role(s) ' + allDeletedRoleStr,
                                            function () {
                                                if (jQuery('#deleteClaimMappings').val() == 'true') {
                                                    var confirmationMessage = 'Are you sure you want to ' +
                                                            'delete the Claim URI mappings of ' +
                                                            jQuery('#idPName').val() + '?';
                                                    if (jQuery('#claimMappingFile').val() != '') {
                                                        confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                    }
                                                    CARBON.showConfirmationDialog(confirmationMessage,
                                                            function () {

                                                            },
                                                            function () {
                                                                location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                            });
                                                } else {
                                                    if (jQuery('#deleteRoleMappings').val() == 'true') {
                                                        var confirmationMessage = 'Are you sure you want to ' +
                                                                'delete the Role Mappings of ' +
                                                                jQuery('#idPName').val() + '?';
                                                        if (jQuery('#roleMappingFile').val() != '') {
                                                            confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                        }
                                                        CARBON.showConfirmationDialog(confirmationMessage,
                                                                function () {
                                                                    doEditFinish();
                                                                },
                                                                function () {
                                                                    location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                                });
                                                    } else {
                                                        doEditFinish();
                                                    }
                                                }
                                            },
                                            function () {
                                                location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                            });
                                } else {
                                    if (jQuery('#deleteClaimMappings').val() == 'true') {
                                        var confirmationMessage = 'Are you sure you want to ' +
                                                'delete the Claim URI mappings of ' +
                                                jQuery('#idPName').val() + '?';
                                        if (jQuery('#claimMappingFile').val() != '') {
                                            confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                        }
                                        CARBON.showConfirmationDialog(confirmationMessage,
                                                function () {
                                                    if (jQuery('#deleteRoleMappings').val() == 'true') {
                                                        var confirmationMessage = 'Are you sure you want to ' +
                                                                'delete the Role Mappings of ' +
                                                                jQuery('#idPName').val() + '?';
                                                        if (jQuery('#roleMappingFile').val() != '') {
                                                            confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                        }
                                                        CARBON.showConfirmationDialog(confirmationMessage,
                                                                function () {
                                                                    doEditFinish();
                                                                },
                                                                function () {
                                                                    location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                                });
                                                    } else {
                                                        doEditFinish();
                                                    }
                                                },
                                                function () {
                                                    location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                });
                                    } else {
                                        if (jQuery('#deleteRoleMappings').val() == 'true') {
                                            var confirmationMessage = 'Are you sure you want to ' +
                                                    'delete the Role Mappings of ' +
                                                    jQuery('#idPName').val() + '?';
                                            if (jQuery('#roleMappingFile').val() != '') {
                                                confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                            }
                                            CARBON.showConfirmationDialog(confirmationMessage,
                                                    function () {
                                                        doEditFinish();
                                                    },
                                                    function () {
                                                        location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                    });
                                        } else {
                                            doEditFinish();
                                        }
                                    }
                                }
                            },
                            function () {
                                location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                            });
                } else {
                    if (allDeletedRoleStr != "") {
                        CARBON.showConfirmationDialog('Are you sure you want to delete the ' +
                                'role(s) ' + allDeletedRoleStr,
                                function () {
                                    if (jQuery('#deleteClaimMappings').val() == 'true') {
                                        var confirmationMessage = 'Are you sure you want to ' +
                                                'delete the Claim URI mappings of ' +
                                                jQuery('#idPName').val() + '?';
                                        if (jQuery('#claimMappingFile').val() != '') {
                                            confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                        }
                                        CARBON.showConfirmationDialog(confirmationMessage,
                                                function () {
                                                    if (jQuery('#deleteRoleMappings').val() == 'true') {
                                                        var confirmationMessage = 'Are you sure you want to ' +
                                                                'delete the Role Mappings of ' +
                                                                jQuery('#idPName').val() + '?';
                                                        if (jQuery('#roleMappingFile').val() != '') {
                                                            confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                        }
                                                        CARBON.showConfirmationDialog(confirmationMessage,
                                                                function () {
                                                                    doEditFinish();
                                                                },
                                                                function () {
                                                                    location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                                });
                                                    } else {
                                                        doEditFinish();
                                                    }
                                                },
                                                function () {
                                                    location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                });
                                    } else {
                                        if (jQuery('#deleteRoleMappings').val() == 'true') {
                                            var confirmationMessage = 'Are you sure you want to ' +
                                                    'delete the Role Mappings of ' +
                                                    jQuery('#idPName').val() + '?';
                                            if (jQuery('#roleMappingFile').val() != '') {
                                                confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                            }
                                            CARBON.showConfirmationDialog(confirmationMessage,
                                                    function () {
                                                        doEditFinish();
                                                    },
                                                    function () {
                                                        location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                    });
                                        } else {
                                            doEditFinish();
                                        }
                                    }
                                },
                                function () {
                                    location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                });
                    } else {
                        if (jQuery('#deleteClaimMappings').val() == 'true') {
                            var confirmationMessage = 'Are you sure you want to ' +
                                    'delete the Claim URI mappings of ' +
                                    jQuery('#idPName').val() + '?';
                            if (jQuery('#claimMappingFile').val() != '') {
                                confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                            }
                            CARBON.showConfirmationDialog(confirmationMessage,
                                    function () {
                                        if (jQuery('#deleteRoleMappings').val() == 'true') {
                                            var confirmationMessage = 'Are you sure you want to ' +
                                                    'delete the Role Mappings of ' +
                                                    jQuery('#idPName').val() + '?';
                                            if (jQuery('#roleMappingFile').val() != '') {
                                                confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                            }
                                            CARBON.showConfirmationDialog(confirmationMessage,
                                                    function () {
                                                        doEditFinish();
                                                    },
                                                    function () {
                                                        location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                    });
                                        } else {
                                            doEditFinish();
                                        }
                                    },
                                    function () {
                                        location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                    });
                        } else {
                            if (jQuery('#deleteRoleMappings').val() == 'true') {
                                var confirmationMessage = 'Are you sure you want to ' +
                                        'delete the Role Mappings of ' +
                                        jQuery('#idPName').val() + '?';
                                if (jQuery('#roleMappingFile').val() != '') {
                                    confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                }
                                CARBON.showConfirmationDialog(confirmationMessage,
                                        function () {
                                            doEditFinish();
                                        },
                                        function () {
                                            location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                        });
                            } else {
                                doEditFinish();
                            }
                        }
                    }
                }
            }
        }
    }
    function idpMgtUpdateMetadata() {
        if (document.getElementById("meta_data_saml").value != "") {
            <%
                if(idPName != null && !(idPName.equals(""))){
                %>
            CARBON.showConfirmationDialog("This will delete your public certificate and SAML SSO configuration, Do you want to proceed?",
                    function () {
                        if (doValidation()) {
                            var allDeletedClaimStr = "";
                            for (var i = 0; i < deleteClaimRows.length; i++) {
                                if (i < deleteClaimRows.length - 1) {
                                    allDeletedClaimStr += deleteClaimRows[i] + ", ";
                                } else {
                                    allDeletedClaimStr += deleteClaimRows[i] + "?";
                                }
                            }
                            var allDeletedRoleStr = "";
                            for (var i = 0; i < deletedRoleRows.length; i++) {
                                if (i < deletedRoleRows.length - 1) {
                                    allDeletedRoleStr += deletedRoleRows[i] + ", ";
                                } else {
                                    allDeletedRoleStr += deletedRoleRows[i] + "?";
                                }
                            }

                            if (jQuery('#deletePublicCert').val() == 'true') {
                                var confirmationMessage = 'Are you sure you want to delete the public certificate of ' +
                                        jQuery('#idPName').val() + '?';
                                if (jQuery('#certFile').val() != '') {
                                    confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                }
                                CARBON.showConfirmationDialog(confirmationMessage,
                                        function () {
                                            if (allDeletedClaimStr != "") {
                                                CARBON.showConfirmationDialog('Are you sure you want to delete the claim URI(s) ' +
                                                        allDeletedClaimStr,
                                                        function () {
                                                            if (allDeletedRoleStr != "") {
                                                                CARBON.showConfirmationDialog('Are you sure you want to delete the ' +
                                                                        'role(s) ' + allDeletedRoleStr,
                                                                        function () {
                                                                            if (jQuery('#deleteClaimMappings').val() == 'true') {
                                                                                var confirmationMessage = 'Are you sure you want to ' +
                                                                                        'delete the Claim URI Mappings of ' +
                                                                                        jQuery('#idPName').val() + '?';
                                                                                if (jQuery('#claimMappingFile').val() != '') {
                                                                                    confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                                                }
                                                                                CARBON.showConfirmationDialog(confirmationMessage,
                                                                                        function () {
                                                                                            if (jQuery('#deleteRoleMappings').val() == 'true') {
                                                                                                var confirmationMessage = 'Are you sure you want to ' +
                                                                                                        'delete the Role Mappings of ' +
                                                                                                        jQuery('#idPName').val() + '?';
                                                                                                if (jQuery('#roleMappingFile').val() != '') {
                                                                                                    confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                                                                }
                                                                                                CARBON.showConfirmationDialog(confirmationMessage,
                                                                                                        function () {
                                                                                                            doEditFinish();
                                                                                                        },
                                                                                                        function () {
                                                                                                            location.href =
                                                                                                                    "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                                                                        });
                                                                                            } else {
                                                                                                doEditFinish();
                                                                                            }
                                                                                        },
                                                                                        function () {
                                                                                            location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                                                        });
                                                                            } else {
                                                                                if (jQuery('#deleteRoleMappings').val() == 'true') {
                                                                                    var confirmationMessage = 'Are you sure you want to ' +
                                                                                            'delete the Role Mappings of ' +
                                                                                            jQuery('#idPName').val() + '?';
                                                                                    if (jQuery('#roleMappingFile').val() != '') {
                                                                                        confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                                                    }
                                                                                    CARBON.showConfirmationDialog(confirmationMessage,
                                                                                            function () {
                                                                                                doEditFinish();
                                                                                            },
                                                                                            function () {
                                                                                                location.href =
                                                                                                        "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                                                            });
                                                                                } else {
                                                                                    doEditFinish();
                                                                                }
                                                                            }
                                                                        },
                                                                        function () {
                                                                            location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                                        });
                                                            } else {
                                                                if (jQuery('#deleteClaimMappings').val() == 'true') {
                                                                    var confirmationMessage = 'Are you sure you want to ' +
                                                                            'delete the Claim URI mappings of ' +
                                                                            jQuery('#idPName').val() + '?';
                                                                    if (jQuery('#claimMappingFile').val() != '') {
                                                                        confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                                    }
                                                                    CARBON.showConfirmationDialog(confirmationMessage,
                                                                            function () {

                                                                            },
                                                                            function () {
                                                                                location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                                            });
                                                                } else {
                                                                    if (jQuery('#deleteRoleMappings').val() == 'true') {
                                                                        var confirmationMessage = 'Are you sure you want to ' +
                                                                                'delete the Role Mappings of ' +
                                                                                jQuery('#idPName').val() + '?';
                                                                        if (jQuery('#roleMappingFile').val() != '') {
                                                                            confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                                        }
                                                                        CARBON.showConfirmationDialog(confirmationMessage,
                                                                                function () {
                                                                                    doEditFinish();
                                                                                },
                                                                                function () {
                                                                                    location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                                                });
                                                                    } else {
                                                                        doEditFinish();
                                                                    }
                                                                }
                                                            }
                                                        },
                                                        function () {
                                                            location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                        });
                                            } else {
                                                if (allDeletedRoleStr != "") {
                                                    CARBON.showConfirmationDialog('Are you sure you want to delete the ' +
                                                            'role(s) ' + allDeletedRoleStr,
                                                            function () {
                                                                if (jQuery('#deleteClaimMappings').val() == 'true') {
                                                                    var confirmationMessage = 'Are you sure you want to ' +
                                                                            'delete the Claim URI mappings of ' +
                                                                            jQuery('#idPName').val() + '?';
                                                                    if (jQuery('#claimMappingFile').val() != '') {
                                                                        confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                                    }
                                                                    CARBON.showConfirmationDialog(confirmationMessage,
                                                                            function () {

                                                                            },
                                                                            function () {
                                                                                location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                                            });
                                                                } else {
                                                                    if (jQuery('#deleteRoleMappings').val() == 'true') {
                                                                        var confirmationMessage = 'Are you sure you want to ' +
                                                                                'delete the Role Mappings of ' +
                                                                                jQuery('#idPName').val() + '?';
                                                                        if (jQuery('#roleMappingFile').val() != '') {
                                                                            confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                                        }
                                                                        CARBON.showConfirmationDialog(confirmationMessage,
                                                                                function () {
                                                                                    doEditFinish();
                                                                                },
                                                                                function () {
                                                                                    location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                                                });
                                                                    } else {
                                                                        doEditFinish();
                                                                    }
                                                                }
                                                            },
                                                            function () {
                                                                location.href = "idp-mgt-edit.jsp?idPName=Encode.forUriComponent(idPName)%>";
                                                            });
                                                } else {
                                                    if (jQuery('#deleteClaimMappings').val() == 'true') {
                                                        var confirmationMessage = 'Are you sure you want to ' +
                                                                'delete the Claim URI mappings of ' +
                                                                jQuery('#idPName').val() + '?';
                                                        if (jQuery('#claimMappingFile').val() != '') {
                                                            confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                        }
                                                        CARBON.showConfirmationDialog(confirmationMessage,
                                                                function () {
                                                                    if (jQuery('#deleteRoleMappings').val() == 'true') {
                                                                        var confirmationMessage = 'Are you sure you want to ' +
                                                                                'delete the Role Mappings of ' +
                                                                                jQuery('#idPName').val() + '?';
                                                                        if (jQuery('#roleMappingFile').val() != '') {
                                                                            confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                                        }
                                                                        CARBON.showConfirmationDialog(confirmationMessage,
                                                                                function () {
                                                                                    doEditFinish();
                                                                                },
                                                                                function () {
                                                                                    location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                                                });
                                                                    } else {
                                                                        doEditFinish();
                                                                    }
                                                                },
                                                                function () {
                                                                    location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                                });
                                                    } else {
                                                        if (jQuery('#deleteRoleMappings').val() == 'true') {
                                                            var confirmationMessage = 'Are you sure you want to ' +
                                                                    'delete the Role Mappings of ' +
                                                                    jQuery('#idPName').val() + '?';
                                                            if (jQuery('#roleMappingFile').val() != '') {
                                                                confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                            }
                                                            CARBON.showConfirmationDialog(confirmationMessage,
                                                                    function () {
                                                                        doEditFinish();
                                                                    },
                                                                    function () {
                                                                        location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                                    });
                                                        } else {
                                                            doEditFinish();
                                                        }
                                                    }
                                                }
                                            }
                                        },
                                        function () {
                                            location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                        });
                            } else {
                                if (allDeletedClaimStr != "") {
                                    CARBON.showConfirmationDialog('Are you sure you want to delete the claim URI(s) ' +
                                            allDeletedClaimStr,
                                            function () {
                                                if (allDeletedRoleStr != "") {
                                                    CARBON.showConfirmationDialog('Are you sure you want to delete the ' +
                                                            'role(s) ' + allDeletedRoleStr,
                                                            function () {
                                                                if (jQuery('#deleteClaimMappings').val() == 'true') {
                                                                    var confirmationMessage = 'Are you sure you want to ' +
                                                                            'delete the Claim URI mappings of ' +
                                                                            jQuery('#idPName').val() + '?';
                                                                    if (jQuery('#claimMappingFile').val() != '') {
                                                                        confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                                    }
                                                                    CARBON.showConfirmationDialog(confirmationMessage,
                                                                            function () {

                                                                            },
                                                                            function () {
                                                                                location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                                            });
                                                                } else {
                                                                    if (jQuery('#deleteRoleMappings').val() == 'true') {
                                                                        var confirmationMessage = 'Are you sure you want to ' +
                                                                                'delete the Role Mappings of ' +
                                                                                jQuery('#idPName').val() + '?';
                                                                        if (jQuery('#roleMappingFile').val() != '') {
                                                                            confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                                        }
                                                                        CARBON.showConfirmationDialog(confirmationMessage,
                                                                                function () {
                                                                                    doEditFinish();
                                                                                },
                                                                                function () {
                                                                                    location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                                                });
                                                                    } else {
                                                                        doEditFinish();
                                                                    }
                                                                }
                                                            },
                                                            function () {
                                                                location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                            });
                                                } else {
                                                    if (jQuery('#deleteClaimMappings').val() == 'true') {
                                                        var confirmationMessage = 'Are you sure you want to ' +
                                                                'delete the Claim URI mappings of ' +
                                                                jQuery('#idPName').val() + '?';
                                                        if (jQuery('#claimMappingFile').val() != '') {
                                                            confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                        }
                                                        CARBON.showConfirmationDialog(confirmationMessage,
                                                                function () {
                                                                    if (jQuery('#deleteRoleMappings').val() == 'true') {
                                                                        var confirmationMessage = 'Are you sure you want to ' +
                                                                                'delete the Role Mappings of ' +
                                                                                jQuery('#idPName').val() + '?';
                                                                        if (jQuery('#roleMappingFile').val() != '') {
                                                                            confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                                        }
                                                                        CARBON.showConfirmationDialog(confirmationMessage,
                                                                                function () {
                                                                                    doEditFinish();
                                                                                },
                                                                                function () {
                                                                                    location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                                                });
                                                                    } else {
                                                                        doEditFinish();
                                                                    }
                                                                },
                                                                function () {
                                                                    location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                                });
                                                    } else {
                                                        if (jQuery('#deleteRoleMappings').val() == 'true') {
                                                            var confirmationMessage = 'Are you sure you want to ' +
                                                                    'delete the Role Mappings of ' +
                                                                    jQuery('#idPName').val() + '?';
                                                            if (jQuery('#roleMappingFile').val() != '') {
                                                                confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                            }
                                                            CARBON.showConfirmationDialog(confirmationMessage,
                                                                    function () {
                                                                        doEditFinish();
                                                                    },
                                                                    function () {
                                                                        location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                                    });
                                                        } else {
                                                            doEditFinish();
                                                        }
                                                    }
                                                }
                                            },
                                            function () {
                                                location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                            });
                                } else {
                                    if (allDeletedRoleStr != "") {
                                        CARBON.showConfirmationDialog('Are you sure you want to delete the ' +
                                                'role(s) ' + allDeletedRoleStr,
                                                function () {
                                                    if (jQuery('#deleteClaimMappings').val() == 'true') {
                                                        var confirmationMessage = 'Are you sure you want to ' +
                                                                'delete the Claim URI mappings of ' +
                                                                jQuery('#idPName').val() + '?';
                                                        if (jQuery('#claimMappingFile').val() != '') {
                                                            confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                        }
                                                        CARBON.showConfirmationDialog(confirmationMessage,
                                                                function () {
                                                                    if (jQuery('#deleteRoleMappings').val() == 'true') {
                                                                        var confirmationMessage = 'Are you sure you want to ' +
                                                                                'delete the Role Mappings of ' +
                                                                                jQuery('#idPName').val() + '?';
                                                                        if (jQuery('#roleMappingFile').val() != '') {
                                                                            confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                                        }
                                                                        CARBON.showConfirmationDialog(confirmationMessage,
                                                                                function () {
                                                                                    doEditFinish();
                                                                                },
                                                                                function () {
                                                                                    location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                                                });
                                                                    } else {
                                                                        doEditFinish();
                                                                    }
                                                                },
                                                                function () {
                                                                    location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                                });
                                                    } else {
                                                        if (jQuery('#deleteRoleMappings').val() == 'true') {
                                                            var confirmationMessage = 'Are you sure you want to ' +
                                                                    'delete the Role Mappings of ' +
                                                                    jQuery('#idPName').val() + '?';
                                                            if (jQuery('#roleMappingFile').val() != '') {
                                                                confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                            }
                                                            CARBON.showConfirmationDialog(confirmationMessage,
                                                                    function () {
                                                                        doEditFinish();
                                                                    },
                                                                    function () {
                                                                        location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                                    });
                                                        } else {
                                                            doEditFinish();
                                                        }
                                                    }
                                                },
                                                function () {
                                                    location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                });
                                    } else {
                                        if (jQuery('#deleteClaimMappings').val() == 'true') {
                                            var confirmationMessage = 'Are you sure you want to ' +
                                                    'delete the Claim URI mappings of ' +
                                                    jQuery('#idPName').val() + '?';
                                            if (jQuery('#claimMappingFile').val() != '') {
                                                confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                            }
                                            CARBON.showConfirmationDialog(confirmationMessage,
                                                    function () {
                                                        if (jQuery('#deleteRoleMappings').val() == 'true') {
                                                            var confirmationMessage = 'Are you sure you want to ' +
                                                                    'delete the Role Mappings of ' +
                                                                    jQuery('#idPName').val() + '?';
                                                            if (jQuery('#roleMappingFile').val() != '') {
                                                                confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                            }
                                                            CARBON.showConfirmationDialog(confirmationMessage,
                                                                    function () {
                                                                        doEditFinish();
                                                                    },
                                                                    function () {
                                                                        location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                                    });
                                                        } else {
                                                            doEditFinish();
                                                        }
                                                    },
                                                    function () {
                                                        location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                    });
                                        } else {
                                            if (jQuery('#deleteRoleMappings').val() == 'true') {
                                                var confirmationMessage = 'Are you sure you want to ' +
                                                        'delete the Role Mappings of ' +
                                                        jQuery('#idPName').val() + '?';
                                                if (jQuery('#roleMappingFile').val() != '') {
                                                    confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                }
                                                CARBON.showConfirmationDialog(confirmationMessage,
                                                        function () {
                                                            doEditFinish();
                                                        },
                                                        function () {
                                                            location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                        });
                                            } else {
                                                doEditFinish();
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    function () {
                        location.href =
                                "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                    });
            <%

            }else{



        %>
            if (doValidation()) {
                var allDeletedClaimStr = "";
                for (var i = 0; i < deleteClaimRows.length; i++) {
                    if (i < deleteClaimRows.length - 1) {
                        allDeletedClaimStr += deleteClaimRows[i] + ", ";
                    } else {
                        allDeletedClaimStr += deleteClaimRows[i] + "?";
                    }
                }
                var allDeletedRoleStr = "";
                for (var i = 0; i < deletedRoleRows.length; i++) {
                    if (i < deletedRoleRows.length - 1) {
                        allDeletedRoleStr += deletedRoleRows[i] + ", ";
                    } else {
                        allDeletedRoleStr += deletedRoleRows[i] + "?";
                    }
                }

                if (jQuery('#deletePublicCert').val() == 'true') {
                    var confirmationMessage = 'Are you sure you want to delete the public certificate of ' +
                            jQuery('#idPName').val() + '?';
                    if (jQuery('#certFile').val() != '') {
                        confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                    }
                    CARBON.showConfirmationDialog(confirmationMessage,
                            function () {
                                if (allDeletedClaimStr != "") {
                                    CARBON.showConfirmationDialog('Are you sure you want to delete the claim URI(s) ' +
                                            allDeletedClaimStr,
                                            function () {
                                                if (allDeletedRoleStr != "") {
                                                    CARBON.showConfirmationDialog('Are you sure you want to delete the ' +
                                                            'role(s) ' + allDeletedRoleStr,
                                                            function () {
                                                                if (jQuery('#deleteClaimMappings').val() == 'true') {
                                                                    var confirmationMessage = 'Are you sure you want to ' +
                                                                            'delete the Claim URI Mappings of ' +
                                                                            jQuery('#idPName').val() + '?';
                                                                    if (jQuery('#claimMappingFile').val() != '') {
                                                                        confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                                    }
                                                                    CARBON.showConfirmationDialog(confirmationMessage,
                                                                            function () {
                                                                                if (jQuery('#deleteRoleMappings').val() == 'true') {
                                                                                    var confirmationMessage = 'Are you sure you want to ' +
                                                                                            'delete the Role Mappings of ' +
                                                                                            jQuery('#idPName').val() + '?';
                                                                                    if (jQuery('#roleMappingFile').val() != '') {
                                                                                        confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                                                    }
                                                                                    CARBON.showConfirmationDialog(confirmationMessage,
                                                                                            function () {
                                                                                                doEditFinish();
                                                                                            },
                                                                                            function () {
                                                                                                location.href =
                                                                                                        "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                                                            });
                                                                                } else {
                                                                                    doEditFinish();
                                                                                }
                                                                            },
                                                                            function () {
                                                                                location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                                            });
                                                                } else {
                                                                    if (jQuery('#deleteRoleMappings').val() == 'true') {
                                                                        var confirmationMessage = 'Are you sure you want to ' +
                                                                                'delete the Role Mappings of ' +
                                                                                jQuery('#idPName').val() + '?';
                                                                        if (jQuery('#roleMappingFile').val() != '') {
                                                                            confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                                        }
                                                                        CARBON.showConfirmationDialog(confirmationMessage,
                                                                                function () {
                                                                                    doEditFinish();
                                                                                },
                                                                                function () {
                                                                                    location.href =
                                                                                            "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                                                });
                                                                    } else {
                                                                        doEditFinish();
                                                                    }
                                                                }
                                                            },
                                                            function () {
                                                                location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                            });
                                                } else {
                                                    if (jQuery('#deleteClaimMappings').val() == 'true') {
                                                        var confirmationMessage = 'Are you sure you want to ' +
                                                                'delete the Claim URI mappings of ' +
                                                                jQuery('#idPName').val() + '?';
                                                        if (jQuery('#claimMappingFile').val() != '') {
                                                            confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                        }
                                                        CARBON.showConfirmationDialog(confirmationMessage,
                                                                function () {

                                                                },
                                                                function () {
                                                                    location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                                });
                                                    } else {
                                                        if (jQuery('#deleteRoleMappings').val() == 'true') {
                                                            var confirmationMessage = 'Are you sure you want to ' +
                                                                    'delete the Role Mappings of ' +
                                                                    jQuery('#idPName').val() + '?';
                                                            if (jQuery('#roleMappingFile').val() != '') {
                                                                confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                            }
                                                            CARBON.showConfirmationDialog(confirmationMessage,
                                                                    function () {
                                                                        doEditFinish();
                                                                    },
                                                                    function () {
                                                                        location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                                    });
                                                        } else {
                                                            doEditFinish();
                                                        }
                                                    }
                                                }
                                            },
                                            function () {
                                                location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                            });
                                } else {
                                    if (allDeletedRoleStr != "") {
                                        CARBON.showConfirmationDialog('Are you sure you want to delete the ' +
                                                'role(s) ' + allDeletedRoleStr,
                                                function () {
                                                    if (jQuery('#deleteClaimMappings').val() == 'true') {
                                                        var confirmationMessage = 'Are you sure you want to ' +
                                                                'delete the Claim URI mappings of ' +
                                                                jQuery('#idPName').val() + '?';
                                                        if (jQuery('#claimMappingFile').val() != '') {
                                                            confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                        }
                                                        CARBON.showConfirmationDialog(confirmationMessage,
                                                                function () {

                                                                },
                                                                function () {
                                                                    location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                                });
                                                    } else {
                                                        if (jQuery('#deleteRoleMappings').val() == 'true') {
                                                            var confirmationMessage = 'Are you sure you want to ' +
                                                                    'delete the Role Mappings of ' +
                                                                    jQuery('#idPName').val() + '?';
                                                            if (jQuery('#roleMappingFile').val() != '') {
                                                                confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                            }
                                                            CARBON.showConfirmationDialog(confirmationMessage,
                                                                    function () {
                                                                        doEditFinish();
                                                                    },
                                                                    function () {
                                                                        location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                                    });
                                                        } else {
                                                            doEditFinish();
                                                        }
                                                    }
                                                },
                                                function () {
                                                    location.href = "idp-mgt-edit.jsp?idPName=Encode.forUriComponent(idPName)%>";
                                                });
                                    } else {
                                        if (jQuery('#deleteClaimMappings').val() == 'true') {
                                            var confirmationMessage = 'Are you sure you want to ' +
                                                    'delete the Claim URI mappings of ' +
                                                    jQuery('#idPName').val() + '?';
                                            if (jQuery('#claimMappingFile').val() != '') {
                                                confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                            }
                                            CARBON.showConfirmationDialog(confirmationMessage,
                                                    function () {
                                                        if (jQuery('#deleteRoleMappings').val() == 'true') {
                                                            var confirmationMessage = 'Are you sure you want to ' +
                                                                    'delete the Role Mappings of ' +
                                                                    jQuery('#idPName').val() + '?';
                                                            if (jQuery('#roleMappingFile').val() != '') {
                                                                confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                            }
                                                            CARBON.showConfirmationDialog(confirmationMessage,
                                                                    function () {
                                                                        doEditFinish();
                                                                    },
                                                                    function () {
                                                                        location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                                    });
                                                        } else {
                                                            doEditFinish();
                                                        }
                                                    },
                                                    function () {
                                                        location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                    });
                                        } else {
                                            if (jQuery('#deleteRoleMappings').val() == 'true') {
                                                var confirmationMessage = 'Are you sure you want to ' +
                                                        'delete the Role Mappings of ' +
                                                        jQuery('#idPName').val() + '?';
                                                if (jQuery('#roleMappingFile').val() != '') {
                                                    confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                }
                                                CARBON.showConfirmationDialog(confirmationMessage,
                                                        function () {
                                                            doEditFinish();
                                                        },
                                                        function () {
                                                            location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                        });
                                            } else {
                                                doEditFinish();
                                            }
                                        }
                                    }
                                }
                            },
                            function () {
                                location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                            });
                } else {
                    if (allDeletedClaimStr != "") {
                        CARBON.showConfirmationDialog('Are you sure you want to delete the claim URI(s) ' +
                                allDeletedClaimStr,
                                function () {
                                    if (allDeletedRoleStr != "") {
                                        CARBON.showConfirmationDialog('Are you sure you want to delete the ' +
                                                'role(s) ' + allDeletedRoleStr,
                                                function () {
                                                    if (jQuery('#deleteClaimMappings').val() == 'true') {
                                                        var confirmationMessage = 'Are you sure you want to ' +
                                                                'delete the Claim URI mappings of ' +
                                                                jQuery('#idPName').val() + '?';
                                                        if (jQuery('#claimMappingFile').val() != '') {
                                                            confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                        }
                                                        CARBON.showConfirmationDialog(confirmationMessage,
                                                                function () {

                                                                },
                                                                function () {
                                                                    location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                                });
                                                    } else {
                                                        if (jQuery('#deleteRoleMappings').val() == 'true') {
                                                            var confirmationMessage = 'Are you sure you want to ' +
                                                                    'delete the Role Mappings of ' +
                                                                    jQuery('#idPName').val() + '?';
                                                            if (jQuery('#roleMappingFile').val() != '') {
                                                                confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                            }
                                                            CARBON.showConfirmationDialog(confirmationMessage,
                                                                    function () {
                                                                        doEditFinish();
                                                                    },
                                                                    function () {
                                                                        location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                                    });
                                                        } else {
                                                            doEditFinish();
                                                        }
                                                    }
                                                },
                                                function () {
                                                    location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                });
                                    } else {
                                        if (jQuery('#deleteClaimMappings').val() == 'true') {
                                            var confirmationMessage = 'Are you sure you want to ' +
                                                    'delete the Claim URI mappings of ' +
                                                    jQuery('#idPName').val() + '?';
                                            if (jQuery('#claimMappingFile').val() != '') {
                                                confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                            }
                                            CARBON.showConfirmationDialog(confirmationMessage,
                                                    function () {
                                                        if (jQuery('#deleteRoleMappings').val() == 'true') {
                                                            var confirmationMessage = 'Are you sure you want to ' +
                                                                    'delete the Role Mappings of ' +
                                                                    jQuery('#idPName').val() + '?';
                                                            if (jQuery('#roleMappingFile').val() != '') {
                                                                confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                            }
                                                            CARBON.showConfirmationDialog(confirmationMessage,
                                                                    function () {
                                                                        doEditFinish();
                                                                    },
                                                                    function () {
                                                                        location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                                    });
                                                        } else {
                                                            doEditFinish();
                                                        }
                                                    },
                                                    function () {
                                                        location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                    });
                                        } else {
                                            if (jQuery('#deleteRoleMappings').val() == 'true') {
                                                var confirmationMessage = 'Are you sure you want to ' +
                                                        'delete the Role Mappings of ' +
                                                        jQuery('#idPName').val() + '?';
                                                if (jQuery('#roleMappingFile').val() != '') {
                                                    confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                }
                                                CARBON.showConfirmationDialog(confirmationMessage,
                                                        function () {
                                                            doEditFinish();
                                                        },
                                                        function () {
                                                            location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                        });
                                            } else {
                                                doEditFinish();
                                            }
                                        }
                                    }
                                },
                                function () {
                                    location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                });
                    } else {
                        if (allDeletedRoleStr != "") {
                            CARBON.showConfirmationDialog('Are you sure you want to delete the ' +
                                    'role(s) ' + allDeletedRoleStr,
                                    function () {
                                        if (jQuery('#deleteClaimMappings').val() == 'true') {
                                            var confirmationMessage = 'Are you sure you want to ' +
                                                    'delete the Claim URI mappings of ' +
                                                    jQuery('#idPName').val() + '?';
                                            if (jQuery('#claimMappingFile').val() != '') {
                                                confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                            }
                                            CARBON.showConfirmationDialog(confirmationMessage,
                                                    function () {
                                                        if (jQuery('#deleteRoleMappings').val() == 'true') {
                                                            var confirmationMessage = 'Are you sure you want to ' +
                                                                    'delete the Role Mappings of ' +
                                                                    jQuery('#idPName').val() + '?';
                                                            if (jQuery('#roleMappingFile').val() != '') {
                                                                confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                            }
                                                            CARBON.showConfirmationDialog(confirmationMessage,
                                                                    function () {
                                                                        doEditFinish();
                                                                    },
                                                                    function () {
                                                                        location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                                    });
                                                        } else {
                                                            doEditFinish();
                                                        }
                                                    },
                                                    function () {
                                                        location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                    });
                                        } else {
                                            if (jQuery('#deleteRoleMappings').val() == 'true') {
                                                var confirmationMessage = 'Are you sure you want to ' +
                                                        'delete the Role Mappings of ' +
                                                        jQuery('#idPName').val() + '?';
                                                if (jQuery('#roleMappingFile').val() != '') {
                                                    confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                }
                                                CARBON.showConfirmationDialog(confirmationMessage,
                                                        function () {
                                                            doEditFinish();
                                                        },
                                                        function () {
                                                            location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                        });
                                            } else {
                                                doEditFinish();
                                            }
                                        }
                                    },
                                    function () {
                                        location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                    });
                        } else {
                            if (jQuery('#deleteClaimMappings').val() == 'true') {
                                var confirmationMessage = 'Are you sure you want to ' +
                                        'delete the Claim URI mappings of ' +
                                        jQuery('#idPName').val() + '?';
                                if (jQuery('#claimMappingFile').val() != '') {
                                    confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                }
                                CARBON.showConfirmationDialog(confirmationMessage,
                                        function () {
                                            if (jQuery('#deleteRoleMappings').val() == 'true') {
                                                var confirmationMessage = 'Are you sure you want to ' +
                                                        'delete the Role Mappings of ' +
                                                        jQuery('#idPName').val() + '?';
                                                if (jQuery('#roleMappingFile').val() != '') {
                                                    confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                                }
                                                CARBON.showConfirmationDialog(confirmationMessage,
                                                        function () {
                                                            doEditFinish();
                                                        },
                                                        function () {
                                                            location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                                        });
                                            } else {
                                                doEditFinish();
                                            }
                                        },
                                        function () {
                                            location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                        });
                            } else {
                                if (jQuery('#deleteRoleMappings').val() == 'true') {
                                    var confirmationMessage = 'Are you sure you want to ' +
                                            'delete the Role Mappings of ' +
                                            jQuery('#idPName').val() + '?';
                                    if (jQuery('#roleMappingFile').val() != '') {
                                        confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                                    }
                                    CARBON.showConfirmationDialog(confirmationMessage,
                                            function () {
                                                doEditFinish();
                                            },
                                            function () {
                                                location.href = "idp-mgt-edit.jsp?idPName=<%=Encode.forUriComponent(idPName)%>";
                                            });
                                } else {
                                    doEditFinish();
                                }
                            }
                        }
                    }
                }
            }

            <%}%>
        } else {
            CARBON.showWarningDialog('Select a valid IDP metadata file');
            return false;
        }
    }

    function doEditFinish() {
        jQuery('#primary').removeAttr('disabled');
        jQuery('#openIdEnabled').removeAttr('disabled');
        jQuery('#saml2SSOEnabled').removeAttr('disabled');
        jQuery('#oidcEnabled').removeAttr('disabled');
        jQuery('#passiveSTSEnabled').removeAttr('disabled');
        jQuery('#fbAuthEnabled').removeAttr('disabled');
        jQuery('#openIdDefault').removeAttr('disabled');
        jQuery('#saml2SSODefault').removeAttr('disabled');
        jQuery('#oidcDefault').removeAttr('disabled');
        jQuery('#passiveSTSDefault').removeAttr('disabled');
        jQuery('#fbAuthDefault').removeAttr('disabled');
        jQuery('#googleProvDefault').removeAttr('disabled');
        jQuery('#spmlProvDefault').removeAttr('disabled');
        jQuery('#sfProvDefault').removeAttr('disabled');
        jQuery('#scimProvDefault').removeAttr('disabled');

        for (id in getEnabledCustomAuth()) {
            var defId = '#' + id.replace("_Enabled", "_Default");
            jQuery(defId).removeAttr('disabled');
        }
        <% if(idPName == null || idPName.equals("")){ %>
        jQuery('#idp-mgt-edit-form').attr('action', 'idp-mgt-add-finish-ajaxprocessor.jsp?<csrf:tokenname/>=<csrf:tokenvalue/>');
        <% } %>
        jQuery('#idp-mgt-edit-form').submit();
    }

</script>

<fmt:bundle basename="org.wso2.carbon.idp.mgt.ui.i18n.Resources">
    <div id="middle">
        <h2>
            <fmt:message key='add.identity.provider'/>
        </h2>

        <div id="workArea">
            <form id="idp-mgt-edit-form" name="idp-mgt-edit-form" method="post"
                  action="idp-mgt-edit-finish-ajaxprocessor.jsp?<csrf:tokenname/>=<csrf:tokenvalue/>"
                  enctype="multipart/form-data">
                        <% if(idPName != null && idpUniqueIdMap.get(idPName) != null) { %>
                <input type="hidden" name="idpUUID"
                       value="<%= Encode.forHtmlAttribute(idpUniqueIdMap.get(idPName).toString()) %>"/>
                        <% } %>
                <div class="sectionSeperator togglebleTitle"><fmt:message key='identity.provider.info'/></div>
                <div class="sectionSub">
                    <table class="carbonFormTable">
                        <tr>
                            <td class="leftCol-med labelField"><fmt:message key='name'/>:<span class="required">*</span>
                            </td>
                            <td>
                                <input id="idPName" name="idPName" type="text"
                                       value="<%=Encode.forHtmlAttribute(idPName)%>" autofocus/>
                                <%if (identityProvider != null && identityProvider.getEnable()) { %>
                                <input id="enable" name="enable" type="hidden" value="1">
                                <%} %>
                                <div class="sectionHelp">
                                    <fmt:message key='name.help'/>
                                </div>
                            </td>
                        </tr>

                        <tr>
                            <td class="leftCol-med labelField"><fmt:message key='idp.display.name'/>:</td>
                            <td>
                                <input id="idpDisplayName" name="idpDisplayName" type="text"
                                       value="<%=Encode.forHtmlAttribute(idpDisplayName)%>" autofocus/>

                                <div class="sectionHelp">
                                    <fmt:message key='idp.display.name.help'/>
                                </div>
                            </td>
                        </tr>

                        <tr>
                            <td class="leftCol-med labelField"><fmt:message key='description'/></td>
                            <td>
                                <input id="idPDescription" name="idPDescription" type="text"
                                       value="<%=Encode.forHtmlAttribute(description)%>" autofocus/>

                                <div class="sectionHelp">
                                    <fmt:message key='description.help'/>
                                </div>
                            </td>
                        </tr>

                        <tr>
                            <td class="leftCol-med labelField">
                                <label for="federationHub"><fmt:message key='federation.hub.identity.proider'/></label>
                            </td>
                            <td>
                                <div class="sectionCheckbox">
                                    <input type="checkbox" id="federation_hub_idp"
                                           name="federation_hub_idp" <%=federationHubIdp ? "checked" : "" %>>
                                    <span style="display:inline-block" class="sectionHelp">
                                    <fmt:message key='federation.hub.identity.proider.help'/>
                                </span>
                                </div>
                            </td>
                        </tr>


                        <tr>
                            <td class="leftCol-med labelField"><fmt:message key='home.realm.id'/>:</td>
                            <td>
                                <input id="realmId" name="realmId" type="text"
                                       value="<%=Encode.forHtmlAttribute(realmId)%>" autofocus/>

                                <div class="sectionHelp">
                                    <fmt:message key='home.realm.id.help'/>
                                </div>
                            </td>
                        </tr>
                        <tr>
                            <td class="leftCol-med labelField"><fmt:message key='certificate'/>:</td>
                            <td>
                                <input id="certFile" name="certFile" type="file"/>

                                <div class="sectionHelp">
                                    <fmt:message key='certificate.help'/>
                                </div>
                                <div id="publicCertDiv">
                                    <% if (certData != null) { %>
                                    <a id="publicCertDeleteLink" class="icon-link"
                                       style="margin-left:0;background-image:url(images/delete.gif);"><fmt:message
                                            key='public.cert.delete'/></a>

                                    <div style="clear:both"></div>
                                    <table class="styledLeft">
                                        <thead>
                                        <tr>
                                            <th><fmt:message key='issuerdn'/></th>
                                            <th><fmt:message key='subjectdn'/></th>
                                            <th><fmt:message key='notafter'/></th>
                                            <th><fmt:message key='notbefore'/></th>
                                            <th><fmt:message key='serialno'/></th>
                                            <th><fmt:message key='version'/></th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        <tr>
                                            <td><%
                                                String issuerDN = "";
                                                if (certData.getIssuerDN() != null) {
                                                    issuerDN = certData.getIssuerDN();
                                                }
                                            %><%=Encode.forHtmlContent(issuerDN)%>
                                            </td>
                                            <td><%
                                                String subjectDN = "";
                                                if (certData.getSubjectDN() != null) {
                                                    subjectDN = certData.getSubjectDN();
                                                }
                                            %><%=Encode.forHtmlContent(subjectDN)%>
                                            </td>
                                            <td><%
                                                String notAfter = "";
                                                if (certData.getNotAfter() != null) {
                                                    notAfter = certData.getNotAfter();
                                                }
                                            %><%=Encode.forHtmlContent(notAfter)%>
                                            </td>
                                            <td><%
                                                String notBefore = "";
                                                if (certData.getNotBefore() != null) {
                                                    notBefore = certData.getNotBefore();
                                                }
                                            %><%=Encode.forHtmlContent(notBefore)%>
                                            </td>
                                            <td><%
                                                String serialNo = "";
                                                if (certData.getSerialNumber() != null) {
                                                    serialNo = certData.getSerialNumber().toString();
                                                }
                                            %><%=Encode.forHtmlContent(serialNo)%>
                                            </td>
                                            <td><%=certData.getVersion()%>
                                            </td>
                                        </tr>
                                        </tbody>
                                    </table>
                                    <% } %>
                                </div>
                            </td>
                        </tr>


                        <tr>
                            <td class="leftCol-med labelField"><fmt:message key='resident.idp.alias'/>:</td>
                            <td>
                                <input id="tokenEndpointAlias" name="tokenEndpointAlias" type="text"
                                       value="<%=Encode.forHtmlAttribute(idPAlias)%>" autofocus/>

                                <div class="sectionHelp">
                                    <fmt:message key='resident.idp.alias.help'/>
                                </div>
                            </td>
                        </tr>

                    </table>
                </div>


                <h2 id="claim_config_head" class="sectionSeperator trigger active"><a href="#"><fmt:message
                        key="claim.config.head"/></a>
                </h2>

                <div class="toggle_container sectionSub" style="margin-bottom:10px;" id="claimConfig">

                    <h2 id="basic_claim_config_head" class="sectionSeperator trigger active"
                        style="background-color: beige;">
                        <a href="#"><fmt:message key="basic.cliam.config"/></a>
                    </h2>

                    <div class="toggle_container sectionSub" style="margin-bottom:10px;" id="baisClaimLinkRow">

                        <table>


                            <tr>
                                <td class="leftCol-med labelField"><fmt:message key='select_dialet_type'/>:</td>
                                <td>
                                    <label style="display:block">
                                        <input type="radio" id="choose_dialet_type1" name="choose_dialet_type_group"
                                               value="choose_dialet_type1" <% if (!isCustomClaimEnabled) { %>
                                               checked="checked" <% } %> />
                                        Use Local Claim Dialect
                                    </label>
                                    <label style="display:block">
                                        <input type="radio" id="choose_dialet_type2" name="choose_dialet_type_group"
                                               value="choose_dialet_type2"  <% if (isCustomClaimEnabled) { %>
                                               checked="checked" <% } %> />
                                        Define Custom Claim Dialect
                                    </label>
                                </td>
                            </tr>
                            <tr>


                                <td class="leftCol-med labelField customClaim"><fmt:message key='claimURIs'/>:</td>

                                <td class="customClaim">
                                    <a id="claimAddLink" class="icon-link"
                                       style="margin-left:0;background-image:url(images/add.gif);"><fmt:message
                                            key='add.claim'/></a>

                                    <div style="clear:both"></div>
                                    <div class="sectionHelp">
                                        <fmt:message key='claimURIs.help'/>
                                    </div>
                                    <table class="styledLeft" id="claimAddTable" style="display:none">
                                        <thead>
                                        <tr>
                                            <th class="leftCol-big"><fmt:message key='idp.claim'/></th>
                                            <th><fmt:message key='wso2.claim'/></th>
                                            <th><fmt:message key='actions'/></th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        <%
                                            if (claimMappings != null && claimMappings.length > 0) {
                                        %>
                                        <script>
                                            $(
                                                    jQuery('#claimAddTable'))
                                                    .toggle();
                                        </script>
                                        <% for (int i = 0; i < claimMappings.length; i++) { %>
                                        <tr>
                                            <td><input type="text" style=" width: 90%; " class="claimrow"
                                                       value="<%=Encode.forHtmlAttribute(claimMappings[i].getRemoteClaim().getClaimUri())%>"
                                                       id="claimrowid_<%=i%>"
                                                       name="claimrowname_<%=i%>"/></td>
                                            <td>
                                                <select id="claimrow_id_wso2_<%=i%>" class="claimrow_wso2"
                                                        name="claimrow_name_wso2_<%=i%>">
                                                    <option value="">--- Select Claim URI ---</option>
                                                            <% for(String wso2ClaimName : claimUris) {
													if(claimMappings[i].getLocalClaim().getClaimUri() != null && claimMappings[i].getLocalClaim().getClaimUri().equals(wso2ClaimName)){	%>
                                                    <option selected="selected"
                                                            value="<%=Encode.forHtmlAttribute(wso2ClaimName)%>"><%=Encode.forHtmlContent(wso2ClaimName)%>
                                                    </option>
                                                            <%
													} else{ %>
                                                    <option value="<%=Encode.forHtmlAttribute(wso2ClaimName)%>"><%=Encode.forHtmlContent(wso2ClaimName)%>
                                                    </option>
                                                            <%}
												}%>


                                            </td>

                                            <td>
                                                <a title="<fmt:message key='delete.claim'/>"
                                                   onclick="deleteClaimRow(this);return false;"
                                                   href="#"
                                                   class="icon-link"
                                                   style="background-image: url(images/delete.gif)">
                                                    <fmt:message key='delete'/>
                                                </a>
                                            </td>
                                        </tr>

                                        <% } %>
                                        <% } %>

                                        </tbody>
                                    </table>
                                </td>
                            </tr>

                            <tr>
                                <td>

                                    <% if (claimMappings != null) { %>
                                    <input type="hidden" id="claimrow_id_count" name="claimrow_name_count"
                                           value="<%=claimMappings.length%>">
                                    <% } else { %>
                                    <input type="hidden" id="claimrow_id_count" name="claimrow_name_count" value="0">
                                    <% } %>

                                </td>
                            </tr>

                            <tr>
                                <td class="leftCol-med labelField"><fmt:message key='user.id.claim.uri'/>:</td>
                                <td>
                                    <select id="user_id_claim_dropdown" name="user_id_claim_dropdown"></select>

                                    <div class="sectionHelp">
                                        <fmt:message key='user.id.claim.uri.help'/>
                                    </div>
                                </td>
                            </tr>
                            <tr class="role_claim">
                                <td class="leftCol-med labelField"><fmt:message key='role.claim.uri'/>:</td>
                                <td>
                                    <select id="role_claim_dropdown" name="role_claim_dropdown"></select>

                                    <div class="sectionHelp">
                                        <fmt:message key='role.claim.uri.help'/>
                                    </div>
                                </td>
                            </tr>
                        </table>
                    </div>

                    <h2 id="advanced_claim_config_head" class="sectionSeperator trigger active"
                        style="background-color: beige;">
                        <a href="#"><fmt:message key="advanced.cliam.config"/></a>
                    </h2>

                    <div class="toggle_container sectionSub" style="margin-bottom:10px;" id="advancedClaimLinkRow">
                        <table style="width: 100%">
                            <tr>

                                <td colspan="2">
                                    <table>
                                        <tr>
                                            <td class="leftCol-med labelField"><fmt:message key='role.claim.filter'/>:
                                            </td>
                                            <td>
                                                <select id="idpClaimsList2" name="idpClaimsList2"
                                                        style="float: left;"></select>
                                                <a id="advancedClaimMappingAddLink" class="icon-link"
                                                   style="background-image: url(images/add.gif);"><fmt:message
                                                        key='button.add.advanced.claim'/></a>

                                                <div style="clear: both"/>
                                                <div class="sectionHelp">
                                                    <fmt:message key='help.advanced.claim.mapping'/>
                                                </div>
                                            </td>
                                        </tr>
                                    </table>
                                    <table class="styledLeft" id="advancedClaimMappingAddTable" style="display:none">
                                        <thead>
                                        <tr>
                                            <th class="leftCol-big">Claim URI</th>
                                            <th class="leftCol-big">Default Value</th>
                                            <th>Actions</th>
                                        </tr>
                                        </thead>
                                        <tbody>

                                        <%
                                            if (claimMappings != null && claimMappings.length > 0) {
                                        %>
                                        <script>
                                            $(
                                                    jQuery('#advancedClaimMappingAddTable'))
                                                    .show();
                                        </script>
                                        <% for (int i = 0; i < claimMappings.length; i++) {
                                            if (!isCustomClaimEnabled) {
                                        %>
                                        <tr>
                                            <td><input type="text" style="width: 99%;" class="claimrow"
                                                       value="<%=claimMappings[i].getLocalClaim().getClaimUri()%>"
                                                       id="advancnedIdpClaim_<%=i%>" name="advancnedIdpClaim_<%=i%>"/>
                                            </td>
                                            <td><input type="text" style="width: 99%;" class="claimrow"
                                                       value="<%=claimMappings[i].getDefaultValue() != null ? claimMappings[i].getDefaultValue() : "" %>"
                                                       id="advancedDefault_<%=i%>" name="advancedDefault_<%=i%>"/></td>
                                            <td>
                                                <a title="<fmt:message key='delete.claim'/>"
                                                   onclick="deleteClaimRow(this);return false;"
                                                   href="#"
                                                   class="icon-link"
                                                   style="background-image: url(images/delete.gif)">
                                                    <fmt:message key='delete'/>
                                                </a>
                                            </td>
                                        </tr>

                                        <% } else {

                                            if (claimMappings[i].getRequested()) {
                                        %>
                                        <tr>
                                            <td><input type="text" style="width: 99%;" class="claimrow"
                                                       value="<%=Encode.forHtmlAttribute(claimMappings[i].getRemoteClaim().getClaimUri())%>"
                                                       id="advancnedIdpClaim_<%=i%>" name="advancnedIdpClaim_<%=i%>"/>
                                            </td>
                                            <td><input type="text" style="width: 99%;" class="claimrow"
                                                       value="<%=claimMappings[i].getDefaultValue() != null ?
                                   Encode.forHtmlAttribute(claimMappings[i].getDefaultValue()) : "" %>"
                                                       id="advancedDefault_<%=i%>" name="advancedDefault_<%=i%>"/></td>
                                            <td>
                                                <a title="<fmt:message key='delete.claim'/>"
                                                   onclick="deleteClaimRow(this);return false;"
                                                   href="#"
                                                   class="icon-link"
                                                   style="background-image: url(images/delete.gif)">
                                                    <fmt:message key='delete'/>
                                                </a>
                                            </td>
                                        </tr>

                                        <%
                                                    }

                                                }

                                            }%>
                                        <% } %>

                                        </tbody>
                                    </table>
                                </td>
                            </tr>

                            <tr>
                                <td>
                                    <%
                                        if (claimMappings != null) {
                                    %> <input type="hidden" id="advanced_claim_id_count" name="advanced_claim_id_count"
                                              value="<%=claimMappings.length%>"> <% } else { %> <input
                                        type="hidden" id="advanced_claim_id_count" name="advanced_claim_id_count"
                                        value="0">
                                    <% } %>

                                </td>
                            </tr>
                        </table>
                    </div>


                </div>


                <h2 id="role_permission_config_head" class="sectionSeperator trigger active">
                    <a href="#"><fmt:message key="role.config.head"/></a>
                </h2>

                <div class="toggle_container sectionSub" style="margin-bottom:10px;" id="roleConfig">
                    <table>
                        <tr>
                            <td class="leftCol-med labelField"><fmt:message key='roles'/>:</td>
                            <td>
                                <a id="roleAddLink" class="icon-link"
                                   style="margin-left:0;background-image:url(images/add.gif);"><fmt:message
                                        key='add.role.mapping'/></a>

                                <div style="clear:both"/>
                                <div class="sectionHelp">
                                    <fmt:message key='roles.mapping.help'/>
                                </div>
                                <table class="styledLeft" id="roleAddTable" style="display:none">
                                    <thead>
                                    <tr>
                                        <th class="leftCol-big"><fmt:message key='idp.role'/></th>
                                        <th class="leftCol-big"><fmt:message key='local.role'/></th>
                                        <th><fmt:message key='actions'/></th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <%
                                        if (roleMappings != null && roleMappings.length > 0) {
                                    %>
                                    <script>
                                        $(
                                                jQuery('#roleAddTable'))
                                                .toggle();
                                    </script>
                                    <%
                                        for (int i = 0; i < roleMappings.length; i++) {
                                    %>
                                    <tr>
                                        <td><input type="text"
                                                   value="<%=Encode.forHtmlAttribute(roleMappings[i].getRemoteRole())%>"
                                                   id="rolerowname_<%=i%>"
                                                   name="rolerowname_<%=i%>"/></td>
                                        <td><input type="text"
                                                   value="<%=UserCoreUtil.addDomainToName(roleMappings[i].getLocalRole().getLocalRoleName(), roleMappings[i].getLocalRole().getUserStoreId())%>"
                                                   id="localrowname_<%=i%>" name="localrowname_<%=i%>"/></td>
                                        <td>
                                            <a title="<fmt:message key='delete.role'/>"
                                               onclick="deleteRoleRow(this);return false;"
                                               href="#"
                                               class="icon-link"
                                               style="background-image: url(images/delete.gif)">
                                                <fmt:message key='delete'/>
                                            </a>
                                        </td>
                                    </tr>
                                    <%
                                        }
                                    %>
                                    <%
                                        }
                                    %>


                                    </tbody>
                                </table>
                            </td>
                        </tr>

                        <tr>
                            <td>
                                <%
                                    if (roleMappings != null) {
                                %> <input type="hidden" id="rolemappingrow_id_count" name="rolemappingrow_name_count"
                                          value="<%=roleMappings.length%>"> <% } else { %> <input
                                    type="hidden" id="rolemappingrow_id_count"
                                    name="rolemappingrow_name_count" value="0"> <% } %>

                            </td>
                        </tr>

                        <tr>
                            <td class="leftCol-med labelField"><fmt:message key='provisioning.role'/>:</td>
                            <td>
                                <input id="idpProvisioningRole" class="leftCol-med" name="idpProvisioningRole"
                                       type="text"
                                       value="<%=Encode.forHtmlAttribute(provisioningRole)%>"/>

                                <div class="sectionHelp">
                                    <fmt:message key='provisioning.role.help'/>
                                </div>
                            </td>
                        </tr>

                    </table>
                </div>


                <h2 id="out_bound_auth_head" class="sectionSeperator trigger active">
                    <a href="#"><fmt:message key="out.bound.auth.config"/></a>
                </h2>

                <div class="toggle_container sectionSub" style="margin-bottom:10px;" id="outBoundAuth">

                    <% if (isOpenidAuthenticatorActive) { %>
                    <h2 id="openid_head" class="sectionSeperator trigger active" style="background-color: beige;">
                        <a href="#"><fmt:message key="openid.config"/></a>

                        <div id="openid_enable_logo" class="enablelogo"
                             style="float:right;padding-right: 5px;padding-top: 5px;"><img
                                src="images/ok.png" alt="enable" width="16" height="16"></div>
                    </h2>
                    <div class="toggle_container sectionSub" style="margin-bottom:10px;" id="openIdLinkRow">
                        <table class="carbonFormTable">
                            <tr>
                                <td class="leftCol-med labelField">
                                    <label for="openIdEnabled"><fmt:message key='openid.enabled'/></label>
                                </td>
                                <td>
                                    <div class="sectionCheckbox">
                                        <input id="openIdEnabled" name="openIdEnabled"
                                               type="checkbox" <%=openIdEnabledChecked%>
                                               onclick="checkEnabled(this);"/>
                                        <span style="display:inline-block" class="sectionHelp">
                                    <fmt:message key='openid.enabled.help'/>
                                </span>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td class="leftCol-med labelField">
                                    <label for="openIdDefault"><fmt:message key='openid.default'/></label>
                                </td>
                                <td>
                                    <div class="sectionCheckbox">
                                        <input id="openIdDefault" name="openIdDefault"
                                               type="checkbox" <%=openIdDefaultChecked%> <%=openIdDefaultDisabled%>
                                               onclick="checkDefault(this);"/>
                                        <span style="display:inline-block" class="sectionHelp">
                                    <fmt:message key='openid.default.help'/>
                                </span>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td class="leftCol-med labelField"><fmt:message key='openid.url'/>:<span
                                        class="required">*</span></td>
                                <td>
                                    <input id="openIdUrl" name="openIdUrl" type="text"
                                           value="<%=Encode.forHtmlAttribute(openIdUrl)%>"/>

                                    <div class="sectionHelp">
                                        <fmt:message key='openid.url.help'/>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td class="leftCol-med labelField"><fmt:message key='openid.user.id.location'/>:</td>
                                <td>
                                    <label>
                                        <input type="radio" value="0"
                                               name="open_id_user_id_location" <% if (!isOpenIdUserIdInClaims) { %>
                                               checked="checked" <%}%> />
                                        User ID found in 'claimed_id'
                                    </label>
                                    <label>
                                        <input type="radio" value="1"
                                               name="open_id_user_id_location" <% if (isOpenIdUserIdInClaims) { %>
                                               checked="checked" <%}%> />
                                        User ID found among claims
                                    </label>

                                    <div class="sectionHelp">
                                        <fmt:message key='openid.user.id.location.help'/>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td class="leftCol-med labelField"><fmt:message key='query.param'/>:</td>
                                <td>
                                    <%if (openidQueryParam != null) { %>
                                    <input id="openidQueryParam" name="openidQueryParam" type="text"
                                           value=<%=Encode.forHtmlAttribute(openidQueryParam)%>>
                                    <% } else { %>
                                    <input id="openidQueryParam" name="openidQueryParam" type="text"/>
                                    <% } %>
                                    <div class="sectionHelp">
                                        <fmt:message key='query.param.help'/>
                                    </div>
                                </td>
                            </tr>
                        </table>
                    </div>
                    <% } %>

                    <% if (isSamlssoAuthenticatorActive) { %>

                    <h2 id="saml2_sso_head" class="sectionSeperator trigger active" style="background-color: beige;">
                        <a href="#"><fmt:message key="saml2.web.sso.config"/></a>

                        <div id="sampl2sso_enable_logo" class="enablelogo"
                             style="float:right;padding-right: 5px;padding-top: 5px;"><img
                                src="images/ok.png" alt="enable" width="16" height="16"></div>
                    </h2>
                    <div class="toggle_container sectionSub" style="margin-bottom:10px;" id="saml2SSOLinkRow">
                        <div id="saml_commons_section">

                            <table class="carbonFormTable">
                                <tr>
                                    <td class="leftCol-med labelField">
                                        <label for="saml2SSOEnabled"><fmt:message key='saml2.sso.enabled'/></label>
                                    </td>
                                    <td>
                                        <div class="sectionCheckbox">
                                            <input id="saml2SSOEnabled" name="saml2SSOEnabled"
                                                   type="checkbox" <%=saml2SSOEnabledChecked%>
                                                   onclick="checkEnabled(this);"/>
                                            <span style="display:inline-block" class="sectionHelp">
                                    <fmt:message key='saml2.sso.enabled.help'/>
                                </span>
                                        </div>
                                    </td>
                                </tr>
                                <tr>
                                    <td class="leftCol-med labelField">
                                        <label for="saml2SSODefault"><fmt:message key='saml2.sso.default'/></label>
                                    </td>
                                    <td>
                                        <div class="sectionCheckbox">
                                            <input id="saml2SSODefault" name="saml2SSODefault"
                                                   type="checkbox" <%=saml2SSODefaultChecked%> <%=saml2SSODefaultDisabled%>
                                                   onclick="checkDefault(this);"/>
                                            <span style="display:inline-block" class="sectionHelp">
                                    <fmt:message key='saml2.sso.default.help'/>
                                </span>
                                        </div>
                                    </td>
                                </tr>

                                <tr>
                                    <td class="leftCol-med labelField"><fmt:message key='sp.entity.id'/>:<span
                                            class="required">*</span></td>
                                    <td>
                                        <input id="spEntityId" name="spEntityId" type="text"
                                               value=<%=Encode.forHtmlAttribute(spEntityId)%>>

                                        <div class="sectionHelp">
                                            <fmt:message key='sp.entity.id.help'/>
                                        </div>
                                    </td>
                                </tr>
                                <tr>
                                    <td class="leftCol-med labelField"><fmt:message key='name.id.format'/>:<span
                                            class="required">*</span></td>
                                    <td>
                                        <input id="NameIDType" name="NameIDType" type="text" size="70"
                                               value=<%=Encode.forHtmlAttribute(nameIdFormat)%>>

                                        <div class="sectionHelp">
                                            <fmt:message key='name.id.format.help'/>
                                        </div>
                                    </td>
                                </tr>
                            </table>
                        </div>
                        <div id="saml_mode_selction_section">
                            <table class="carbonFormTable" width="100%">

                                <tbody>
                                <tr>
                                    <td class="leftCol-med labelField"><fmt:message key='saml.sso.select.mode'/><span
                                            ></span></td>
                                    <td>
                                        <input type="radio" checked="checked" name="saml_ui_mode"  value="manual"
                                               onclick="
                                        $('#manual_section').show(); $('#metadata_section').hide();">
                                        <fmt:message key='saml.mode.manual'/>


                                        <input type="radio" name="saml_ui_mode" value="file" onclick="
                                    $('#manual_section').hide(); $('#metadata_section').show();">
                                        <fmt:message key='saml.mode.file'/>

                                        <div class="sectionHelp">
                                            <fmt:message key='help.metadata.select.mode'/>
                                        </div>
                                    </td>
                                </tr>
                                </tbody>
                            </table>
                        </div>



                        <div id="manual_section">
                            <table class="carbonFormTable" width="100%">

                                <tbody>
                                <br>
                                <tr>
                                    <td class="leftCol-med labelField"><fmt:message key='idp.entity.id'/>:<span
                                            class="required">*</span></td>
                                    <td>
                                        <input id="idPEntityId" name="idPEntityId" type="text"
                                               value=<%=Encode.forHtmlAttribute(idPEntityId)%>>

                                        <div class="sectionHelp">
                                            <fmt:message key='idp.entity.id.help'/>
                                        </div>
                                    </td>
                                </tr>

                                <tr>
                                    <td class="leftCol-med labelField"><fmt:message key='sso.url'/>:<span
                                            class="required">*</span></td>
                                    <td>
                                        <input id="ssoUrl" name="ssoUrl" type="text"
                                               value=<%=Encode.forHtmlAttribute(ssoUrl)%>>

                                        <div class="sectionHelp">
                                            <fmt:message key='sso.url.help'/>
                                        </div>
                                    </td>
                                </tr>
                                <tr>
                                    <td class="leftCol-med labelField">
                                        <label for="authnRequestSigned"><fmt:message
                                                key='authn.request.signed'/></label>
                                    </td>
                                    <td>
                                        <div class="sectionCheckbox">
                                            <input id="authnRequestSigned" name="authnRequestSigned"
                                                   type="checkbox" <%=Encode.forHtmlAttribute(authnRequestSignedChecked)%>/>
                                            <span style="display:inline-block" class="sectionHelp">
                                    <fmt:message key='authn.request.signed.help'/>
                                </span>
                                        </div>
                                    </td>
                                </tr>

                                <tr>
                                    <td class="leftCol-med labelField">
                                        <label for="enableAssersionEncryption"><fmt:message
                                                key='authn.enable.assertion.encryption'/></label>
                                    </td>
                                    <td>
                                        <div class="sectionCheckbox">
                                            <input id="IsEnableAssetionEncription" name="IsEnableAssetionEncription"
                                                   type="checkbox" <%=enableAssertinEncriptionChecked%>/>
                                            <span style="display:inline-block" class="sectionHelp">
                                    <fmt:message key='authn.enable.assertion.encryption.help'/>
                                </span>
                                        </div>
                                    </td>
                                </tr>


                                <tr>
                                    <td class="leftCol-med labelField">
                                        <label for="enableAssersionSigning"><fmt:message
                                                key='authn.enable.assertion.signing'/></label>
                                    </td>
                                    <td>
                                        <div class="sectionCheckbox">
                                            <input id="isEnableAssertionSigning" name="isEnableAssertionSigning"
                                                   type="checkbox" <%=enableAssertionSigningChecked%>/>
                                            <span style="display:inline-block" class="sectionHelp">
                                    <fmt:message key='authn.enable.assertion.signing.help'/>
                                </span>
                                        </div>
                                    </td>
                                </tr>

                                <tr>
                                    <td class="leftCol-med labelField">
                                        <label for="sloEnabled"><fmt:message key='logout.enabled'/></label>
                                    </td>
                                    <td>
                                        <div class="sectionCheckbox">
                                            <input id="sloEnabled" name="sloEnabled"
                                                   type="checkbox" <%=sloEnabledChecked%>/>
                                            <span style="display:inline-block" class="sectionHelp">
                                    <fmt:message key='logout.enabled.help'/>
                                </span>
                                        </div>
                                    </td>
                                </tr>
                                <tr>
                                    <td class="leftCol-med labelField"><fmt:message key='logout.url'/>:</td>
                                    <td>
                                        <input id="logoutUrl" name="logoutUrl" type="text" value=<%=logoutUrl%>>

                                        <div class="sectionHelp">
                                            <fmt:message key='logout.url.help'/>
                                        </div>
                                    </td>
                                </tr>
                                <tr>
                                    <td class="leftCol-med labelField">
                                        <label for="logoutRequestSigned"><fmt:message
                                                key='logout.request.signed'/></label>
                                    </td>
                                    <td>
                                        <div class="sectionCheckbox">
                                            <input id="logoutRequestSigned" name="logoutRequestSigned"
                                                   type="checkbox" <%=logoutRequestSignedChecked%>/>
                                            <span style="display:inline-block" class="sectionHelp">
                                    <fmt:message key='logout.request.signed.help'/>
                                </span>
                                        </div>
                                    </td>
                                </tr>
                                <tr>
                                    <td class="leftCol-med labelField">
                                        <label for="authnResponseSigned"><fmt:message
                                                key='authn.response.signed'/></label>
                                    </td>
                                    <td>
                                        <div class="sectionCheckbox">
                                            <input id="authnResponseSigned" name="authnResponseSigned"
                                                   type="checkbox" <%=authnResponseSignedChecked%>/>
                                            <span style="display:inline-block" class="sectionHelp">
                                    <fmt:message key='authn.response.signed.help'/>
                                </span>
                                        </div>
                                    </td>
                                </tr>

                                <!-- Signature Algorithm -->

                                <tr>
                                    <td class="leftCol-med labelField"><fmt:message key='signature.algorithm'/>:</td>
                                    <td>

                                        <select id="signature_algorithem_dropdown"
                                                name="SignatureAlgorithm" <%=signAlgoDropdownDisabled%>>
                                            <%
                                                for (String algorithm : signatureAlgorithms) {
                                                    if (signatureAlgorithm != null && algorithm.equalsIgnoreCase(signatureAlgorithm)) {
                                            %>
                                            <option selected="selected"><%=Encode.forHtmlContent(signatureAlgorithm)%>
                                            </option>
                                            <%
                                            } else {
                                            %>
                                            <option><%=Encode.forHtmlContent(algorithm)%>
                                            </option>
                                            <%
                                                    }
                                                }
                                            %>
                                        </select>
                                        <div class="sectionHelp" style="margin-top: 5px">
                                            <fmt:message key='signature.algorithm.help'/>
                                        </div>
                                    </td>
                                </tr>

                                <!-- Digest Algorithm -->

                                <tr>
                                    <td class="leftCol-med labelField"><fmt:message key='digest.algorithm'/>:</td>
                                    <td>

                                        <select id="digest_algorithem_dropdown"
                                                name="DigestAlgorithm" <%=digestAlgoDropdownDisabled%>>
                                            <%
                                                for (String algorithm : digestAlgorithms) {
                                                    if (digestAlgorithm != null && algorithm.equalsIgnoreCase(digestAlgorithm)) {
                                            %>
                                            <option selected="selected"><%=Encode.forHtmlContent(digestAlgorithm)%>
                                            </option>
                                            <%
                                            } else {
                                            %>
                                            <option><%=Encode.forHtmlContent(algorithm)%>
                                            </option>
                                            <%
                                                    }
                                                }
                                            %>
                                        </select>
                                        <div class="sectionHelp" style="margin-top: 5px">
                                            <fmt:message key='digest.algorithm.help'/>
                                        </div>
                                    </td>
                                </tr>

                                <!-- Attribute Consuming Service Index -->

                                <tr>
                                    <td class="leftCol-med labelField"><fmt:message key='attr.consuming.service.index'/>:
                                    </td>
                                    <td>
                                        <input id="attrConsumingServiceIndex" name="AttributeConsumingServiceIndex"
                                               type="text"
                                               value=<%=Encode.forHtmlAttribute(attributeConsumingServiceIndex)%>>

                                        <div class="sectionHelp">
                                            <fmt:message key='attr.consuming.service.index.help'/>
                                        </div>
                                    </td>
                                </tr>

                                <!-- Force Authentication -->

                                <tr>
                                    <td class="leftCol-med labelField">
                                        <label for="forceAuthentication"><fmt:message
                                                key='enable.force.authentication'/></label>
                                    </td>
                                    <td>
                                        <div class="sectionCheckbox">

                                            <label><input type="radio" value="yes" <%
	              if(forceAuthentication !=null && forceAuthentication.equals("yes")){%>checked="checked"<%
                                                }%> name="ForceAuthentication"/> Yes </label>
                                            <label><input type="radio" value="no" <%
	              if(forceAuthentication !=null && forceAuthentication.equals("no")){%>checked
                                                    ="checked"<%}%> name="ForceAuthentication"/>No </label>
                                            <label><input type="radio" value="as_request" <%
	              if(forceAuthentication!=null&&forceAuthentication.equals("as_request")){%>checked="checked"<%}%>
                                                          name="ForceAuthentication"/>As Per Request</label>

                                        </div>
                                        <div class="sectionHelp" style="margin-top: 5px">
                                            <fmt:message key='enable.force.authentication.help'/>
                                        </div>
                                    </td>
                                </tr>

                                <!-- Include Public Cert -->

                                <tr>
                                    <td class="leftCol-med labelField">
                                        <label for="includeCert"><fmt:message key='include.cert'/></label>
                                    </td>
                                    <td>
                                        <div class="sectionCheckbox">
                                            <input id="includeCert" name="IncludeCert"
                                                   type="checkbox" <%=includeCertChecked%>/>
                                            <span style="display:inline-block" class="sectionHelp">
	                      <fmt:message key='include.cert.help'/>
	                  </span>
                                        </div>
                                    </td>
                                </tr>

                                <!-- Include Protocol Binding -->

                                <tr>
                                    <td class="leftCol-med labelField">
                                        <label for="includeProtocolBinding"><fmt:message
                                                key='include.protocol.binding'/></label>
                                    </td>
                                    <td>
                                        <div class="sectionCheckbox">
                                            <input id="includeProtocolBinding" name="IncludeProtocolBinding"
                                                   type="checkbox" <%=includeProtocolBindingChecked%>/>
                                            <span style="display:inline-block" class="sectionHelp">
	                      <fmt:message key='include.protocol.binding.help'/>
	                  </span>
                                        </div>
                                    </td>
                                </tr>

                                <!-- Include NameID Policy -->

                                <tr>
                                    <td class="leftCol-med labelField">
                                        <label for="includeNameIDPolicy"><fmt:message
                                                key='include.name.id.policy'/></label>
                                    </td>
                                    <td>
                                        <div class="sectionCheckbox">
                                            <input id="includeNameIDPolicy" name="IncludeNameIDPolicy"
                                                   type="checkbox" <%=includeNameIdPolicyChecked%>/>
                                            <span style="display:inline-block" class="sectionHelp">
	                      <fmt:message key='include.name.id.policy.help'/>
	                  </span>
                                        </div>
                                    </td>
                                </tr>

                                <!-- Include Authentication Context -->
                                <tr>
                                    <td class="leftCol-med labelField">
                                        <label for="includeAuthnContext"><fmt:message
                                                key='include.authentication.context'/></label>
                                    </td>
                                    <td>
                                        <div class="sectionCheckbox">
                                            <label><input type="radio" id="includeAuthnCtxYes" value="yes" <%
	              if(includeAuthenticationContext != null && includeAuthenticationContext.equals("yes")){%>checked="checked"<%
                                                }%> name="IncludeAuthnContext"/>Yes </label>
                                            <label><input type="radio" id="includeAuthnCtxNo" value="no" <%
	              if(includeAuthenticationContext != null && includeAuthenticationContext.equals("no")){%>checked="checked"<%
                                                }%> name="IncludeAuthnContext"/>No </label>
                                            <label><input type="radio" id="includeAuthnCtxReq" value="as_request" <%
	              if(includeAuthenticationContext !=null && includeAuthenticationContext.equals("as_request")){%>checked="checked"
                                                          <%
	              		}%>name="IncludeAuthnContext"/>As Per Request</label>
                                        </div>
                                        <div class="sectionHelp" style="margin-top: 5px">
                                            <fmt:message key='include.authentication.context.help'/>
                                        </div>
                                    </td>
                                </tr>

                                <!-- Authentication Context Class -->

                                <tr>
                                    <td class="leftCol-med labelField"><fmt:message key='authentication.context.class'/>:
                                    </td>
                                    <td>
                                        <%
                                            boolean isNotCustom = false;
                                        %>
                                        <select id="authentication_context_class_dropdown"
                                                name="AuthnContextClassRef" <%=authnContextClassRefDropdownDisabled%>>
                                            <%
                                                for (String authnContextClass : authenticationContextClasses) {
                                                    if (authnContextClass != null && authnContextClass.equalsIgnoreCase(authenticationContextClass)) {
                                                        isNotCustom = true;
                                            %>
                                            <option selected="selected"><%=Encode.forHtmlContent(authenticationContextClass)%>
                                            </option>
                                            <%
                                            } else {
                                            %>
                                            <option><%=Encode.forHtmlContent(authnContextClass)%>
                                            </option>
                                            <%
                                                    }
                                                }
                                            %>

                                            <%
                                                if (isNotCustom) {
                                            %>
                                            <option><%=IdentityApplicationConstants.Authenticator.SAML2SSO.CUSTOM_AUTHENTICATION_CONTEXT_CLASS_OPTION %>
                                            </option>
                                            <%
                                            } else {
                                            %>
                                            <option selected="selected"><%=IdentityApplicationConstants.Authenticator.SAML2SSO.CUSTOM_AUTHENTICATION_CONTEXT_CLASS_OPTION %>
                                            </option>
                                            <%
                                                }
                                            %>
                                        </select>
                                        <div class="sectionHelp" style="margin-top: 5px">
                                            <fmt:message key='authentication.context.class.help'/>
                                        </div>
                                        <%
                                            if (isNotCustom) {
                                        %>
                                        <input id="custom_authentication_context_class"
                                               name="CustomAuthnContextClassRef"
                                               type="text" value="" disabled="true">
                                        <%
                                        } else {
                                        %>
                                        <input id="custom_authentication_context_class"
                                               name="CustomAuthnContextClassRef"
                                               type="text"
                                               value="<%=Encode.forHtmlContent(authenticationContextClass)%>">
                                        <%
                                            }
                                        %>
                                        <div class="sectionHelp" style="margin-top: 5px">
                                            <fmt:message key='authentication.context.class.custom.help'/>
                                        </div>
                                    </td>
                                </tr>

                                <!-- Authenticatin Context Comparison Level -->

                                <tr>
                                    <td class="leftCol-med labelField"><fmt:message
                                            key='authentication.context.comparison'/>:
                                    </td>
                                    <td>

                                        <select id="auth_context_comparison_level_dropdown"
                                                name="AuthnContextComparisonLevel" <%=authnContextComparisonDropdownDisabled%>>
                                            <%
                                                for (String authnContextComparisonLevel : authenticationContextComparisonLevels) {
                                                    if (authnContextComparisonLevel != null && authnContextComparisonLevel.equals(authenticationContextComparisonLevel)) {
                                            %>
                                            <option selected="selected"><%=Encode.forHtmlContent(authenticationContextComparisonLevel)%>
                                            </option>
                                            <%
                                            } else {
                                            %>
                                            <option><%=Encode.forHtmlContent(authnContextComparisonLevel)%>
                                            </option>
                                            <%
                                                    }
                                                }
                                            %>
                                        </select>
                                        <div class="sectionHelp" style="margin-top: 5px">
                                            <fmt:message key='authentication.context.comparison.help'/>
                                        </div>
                                    </td>
                                </tr>

                                <tr>
                                    <td class="leftCol-med labelField"><fmt:message
                                            key='saml2.sso.user.id.location'/>:
                                    </td>
                                    <td>
                                        <label>
                                            <input type="radio" value="0"
                                                   name="saml2_sso_user_id_location" <% if (!isSAMLSSOUserIdInClaims) { %>
                                                   checked="checked" <%}%> />
                                            User ID found in 'Name Identifier'
                                        </label>
                                        <label>
                                            <input type="radio" value="1"
                                                   name="saml2_sso_user_id_location" <% if (isSAMLSSOUserIdInClaims) { %>
                                                   checked="checked" <%}%> />
                                            User ID found among claims
                                        </label>

                                        <div class="sectionHelp">
                                            <fmt:message key='saml2.sso.user.id.location.help'/>
                                        </div>
                                    </td>
                                </tr>

                                <tr>
                                    <td class="leftCol-med labelField"><fmt:message key='request.method'/>:</td>
                                    <td>
                                        <label>
                                            <input type="radio" name="RequestMethod" value="redirect"
                                                   <% if(requestMethod != null && requestMethod.equals("redirect")){%>checked="checked"<%}%>/>HTTP-Redirect
                                        </label>
                                        <label><input type="radio" name="RequestMethod" value="post"
                                                      <% if(requestMethod != null && requestMethod.equals("post")){%>checked="checked"<%}%>/>HTTP-POST
                                        </label>
                                        <label><input type="radio" name="RequestMethod" value="as_request"
                                                      <% if(requestMethod != null && requestMethod.equals("as_request")){%>checked="checked"<%}%>/>As
                                            Per Request
                                        </label>

                                        <div class="sectionHelp" style="margin-top: 5px">
                                            <fmt:message key='request.method.help'/>
                                        </div>
                                    </td>
                                </tr>

                                <tr>
                                    <td class="leftCol-med labelField"><fmt:message key='authn.context.class.ref'/>:</td>
                                    <td>
                                        <label>
                                            <input type="radio" name="ResponseAuthnContextClassRef" value="default"
                                                   <% if(responseAuthnContextClassRef != null && responseAuthnContextClassRef.equals("default")){%>checked="checked"<%}%>/>Default
                                        </label>
                                        <label><input type="radio" name="ResponseAuthnContextClassRef" value="as_response"
                                                      <% if(responseAuthnContextClassRef != null && responseAuthnContextClassRef.equals("as_response")){%>checked="checked"<%}%>/>As Per Response
                                        </label>

                                        <div class="sectionHelp" style="margin-top: 5px">
                                            <fmt:message key='authn.context.class.ref.help'/>
                                        </div>
                                    </td>
                                </tr>

                                <tr>
                                    <td class="leftCol-med labelField"><fmt:message key='query.param'/>:</td>
                                    <td>
                                        <%
                                            if (samlQueryParam == null) {
                                                samlQueryParam = "";
                                            }
                                        %>

                                        <input id="samlQueryParam" name="samlQueryParam" type="text"
                                               value=<%=Encode.forHtmlAttribute(samlQueryParam)%>>

                                        <div class="sectionHelp">
                                            <fmt:message key='query.param.help'/>
                                        </div>
                                    </td>
                                </tr>
                                </tbody>
                            </table>
                            <br>
                        </div>
                        <br><br>

                        <div id="metadata_section">
                            <table class="styledLeft" width="100%">
                                <thead>
                                <tr>
                                    <th><fmt:message key="saml.sso.upload.id.provider.metadata"/></th>
                                </tr>
                                </thead>
                                <tbody>

                                <tr>
                                    <td><span>File Location: </span><input type="file" id="meta_data_saml"
                                                                           name="meta_data_saml" size="50"/></td>
                                </tr>
                                <tr>
                                    <td>
                                        <input type="button" value="<fmt:message key='register'/>"
                                               onclick="idpMgtUpdateMetadata();"/>
                                        <input class="button" type="reset" value="<fmt:message key='saml.sso.cancel'/>"
                                               onclick="doCancel();"/></td>
                                </tr>
                                </tbody>
                            </table>
                        </div>
                        <script>
                            $('#manual_section').show();
                            $('#metadata_section').hide();
                        </script>


                    </div>
                    <script type="text/javascript">

                        function doCancel() {
                            document.getElementById("meta_data_saml").value = '';
                        }
                    </script>
                    <% } %>

                    <% if (isOpenidconnectAuthenticatorActive) { %>

                    <h2 id="oauth2_head" class="sectionSeperator trigger active" style="background-color: beige;">
                        <a href="#"><fmt:message key="oidc.config"/></a>

                        <div id="oAuth2_enable_logo" class="enablelogo"
                             style="float:right;padding-right: 5px;padding-top: 5px;"><img
                                src="images/ok.png" alt="enable" width="16" height="16"></div>
                    </h2>
                    <div class="toggle_container sectionSub" style="margin-bottom:10px;" id="oauth2LinkRow">
                        <table class="carbonFormTable">
                            <tr>
                                <td class="leftCol-med labelField">
                                    <label for="oidcEnabled"><fmt:message key='oidc.enabled'/></label>
                                </td>
                                <td>
                                    <div class="sectionCheckbox">
                                        <input id="oidcEnabled" name="oidcEnabled"
                                               type="checkbox" <%=oidcEnabledChecked%>
                                               onclick="checkEnabled(this);"/>
                                        <span style="display:inline-block" class="sectionHelp">
                                    <fmt:message key='oidc.enabled.help'/>
                                </span>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td class="leftCol-med labelField">
                                    <label for="oidcDefault"><fmt:message key='oidc.default'/></label>
                                </td>
                                <td>
                                    <div class="sectionCheckbox">
                                        <input id="oidcDefault" name="oidcDefault"
                                               type="checkbox" <%=oidcDefaultChecked%> <%=oidcDefaultDisabled%>
                                               onclick="checkDefault(this);"/>
                                        <span style="display:inline-block" class="sectionHelp">
                                    <fmt:message key='oidc.default.help'/>
                                </span>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td class="leftCol-med labelField"><fmt:message key='client.id'/>:<span
                                        class="required">*</span></td>
                                <td>
                                    <input id="clientId" name="clientId" type="text"
                                           value=<%=Encode.forHtmlAttribute(clientId)%>>

                                    <div class="sectionHelp">
                                        <fmt:message key='client.id.help'/>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td class="leftCol-med labelField"><fmt:message key='client.secret'/>:<span
                                        class="required">*</span></td>
                                <td>
                                    <div id="showHideButtonDivIdOauth" style="border:1px solid rgb(88, 105, 125);"
                                         class="leftCol-med">
                                        <input id="clientSecret" name="clientSecret" type="password"
                                               autocomplete="off" value="<%=Encode.forHtmlAttribute(clientSecret)%>"
                                               style="  outline: none; border: none; min-width: 175px; max-width: 180px;"/>
                                        <span id="showHideButtonIdOauth" style=" float: right; padding-right: 5px;">
	                        		<a style="margin-top: 5px;" class="showHideBtn"
                                       onclick="showHidePassword(this, 'clientSecret')">Show</a>
	                       		</span>
                                    </div>
                                    <div class="sectionHelp">
                                        <fmt:message key='client.secret.help'/>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td class="leftCol-med labelField"><fmt:message key='authz.endpoint'/>:<span
                                        class="required">*</span></td>
                                <td>
                                    <input id="authzUrl" name="authzUrl" type="text"
                                           value=<%=Encode.forHtmlAttribute(authzUrl)%>>

                                    <div class="sectionHelp">
                                        <fmt:message key='authz.endpoint.help'/>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td class="leftCol-med labelField"><fmt:message key='token.endpoint'/>:<span
                                        class="required">*</span></td>
                                <td>
                                    <input id="tokenUrl" name="tokenUrl" type="text"
                                           value=<%=Encode.forHtmlAttribute(tokenUrl)%>>

                                    <div class="sectionHelp">
                                        <fmt:message key='token.endpoint.help'/>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td class="leftCol-med labelField"><fmt:message key='callbackurl'/>
                                <td>
                                    <input id="callbackUrl" name="callbackUrl" type="text"
                                           value=<%=Encode.forHtmlAttribute(callBackUrl)%>>

                                    <div class="sectionHelp">
                                        <fmt:message key='callbackUrl.help'/>
                                    </div>
                                </td>
                            </tr>

                            <tr>
                                <td class="leftCol-med labelField"><fmt:message key='userInfoEndpoint'/>
                                <td>
                                    <input id="userInfoEndpoint" name="userInfoEndpoint" type="text"
                                           value=<%=Encode.forHtmlAttribute(userInfoEndpoint)%>>

                                    <div class="sectionHelp">
                                        <fmt:message key='userInfoEndpoint.help'/>
                                    </div>
                                </td>
                            </tr>

                            <tr>
                                <td class="leftCol-med labelField"><fmt:message key='oidc.user.id.location'/>:</td>
                                <td>
                                    <label>
                                        <input type="radio" value="0"
                                               name="oidc_user_id_location" <% if (!isOIDCUserIdInClaims) { %>
                                               checked="checked" <%}%> />
                                        User ID found in 'sub' attribute
                                    </label>
                                    <label>
                                        <input type="radio" value="1"
                                               name="oidc_user_id_location" <% if (isOIDCUserIdInClaims) { %>
                                               checked="checked" <%}%> />
                                        User ID found among claims
                                    </label>

                                    <div class="sectionHelp">
                                        <fmt:message key='oidc.user.id.location.help'/>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td class="leftCol-med labelField"><fmt:message key='query.param'/>:</td>
                                <td>
                                    <input id="oidcQueryParam" name="oidcQueryParam" type="text"
                                           value="<%=Encode.forHtmlAttribute(oidcQueryParam)%>">

                                    <div class="sectionHelp">
                                        <fmt:message key='query.param.help'/>
                                    </div>
                                </td>
                            </tr>
                        </table>
                    </div>

                    <% } %>

                    <% if (isPassivestsAuthenticatorActive) { %>

                    <h2 id="passive_sts_head" class="sectionSeperator trigger active" style="background-color: beige;">
                        <a href="#"><fmt:message key="passive.sts.config"/></a>

                        <div id="wsfederation_enable_logo" class="enablelogo"
                             style="float:right;padding-right: 5px;padding-top: 5px;"><img
                                src="images/ok.png" alt="enable" width="16" height="16"></div>
                    </h2>
                    <div class="toggle_container sectionSub" style="margin-bottom:10px;" id="passiveSTSLinkRow">
                        <table class="carbonFormTable">
                            <tr>
                                <td class="leftCol-med labelField">
                                    <label for="passiveSTSEnabled"><fmt:message key='passive.sts.enabled'/></label>
                                </td>
                                <td>
                                    <div class="sectionCheckbox">
                                        <input id="passiveSTSEnabled" name="passiveSTSEnabled"
                                               type="checkbox" <%=passiveSTSEnabledChecked%>
                                               onclick="checkEnabled(this);"/>
                                        <span style="display:inline-block" class="sectionHelp">
                                    <fmt:message key='passive.sts.enabled.help'/>
                                </span>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td class="leftCol-med labelField">
                                    <label for="passiveSTSDefault"><fmt:message key='passive.sts.default'/></label>
                                </td>
                                <td>
                                    <div class="sectionCheckbox">
                                        <input id="passiveSTSDefault" name="passiveSTSDefault"
                                               type="checkbox" <%=passiveSTSDefaultChecked%> <%=passiveSTSDefaultDisabled%>
                                               onclick="checkDefault(this);"/>
                                        <span style="display:inline-block" class="sectionHelp">
                                    <fmt:message key='passive.sts.default.help'/>
                                </span>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td class="leftCol-med labelField"><fmt:message key='passive.sts.realm'/>:<span
                                        class="required">*</span>
                                </td>
                                <td>
                                    <input id="passiveSTSRealm" name="passiveSTSRealm" type="text"
                                           value="<%=Encode.forHtmlAttribute(passiveSTSRealm)%>"/>

                                    <div class="sectionHelp">
                                        <fmt:message key='passive.sts.realm.help'/>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td class="leftCol-med labelField"><fmt:message key='passive.sts.url'/>:<span
                                        class="required">*</span></td>
                                <td>
                                    <input id="passiveSTSUrl" name="passiveSTSUrl" type="text"
                                           value="<%=Encode.forHtmlAttribute(passiveSTSUrl)%>"/>

                                    <div class="sectionHelp">
                                        <fmt:message key='passive.sts.url.help'/>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td class="leftCol-med labelField"><fmt:message key='passive.sts.user.id.location'/>:
                                </td>
                                <td>
                                    <label>
                                        <input type="radio" value="0"
                                               name="passive_sts_user_id_location" <% if (!isPassiveSTSUserIdInClaims) { %>
                                               checked="checked" <%}%>/>
                                        User ID found in 'Name Identifier'
                                    </label>
                                    <label>
                                        <input type="radio" value="1"
                                               name="passive_sts_user_id_location" <% if (isPassiveSTSUserIdInClaims) { %>
                                               checked="checked" <%}%>/>
                                        User ID found among claims
                                    </label>

                                    <div class="sectionHelp">
                                        <fmt:message key='passive.sts.user.id.location.help'/>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td class="leftCol-med labelField">
                                    <label for="enablePassiveSTSAssertionSignatureValidation">
                                        <fmt:message key='passive.sts.enable.assertion.signature.validation'/>
                                    </label>
                                </td>
                                <td>
                                    <div class="sectionCheckbox">
                                        <input id="isEnablePassiveSTSAssertionSignatureValidation"
                                               name="isEnablePassiveSTSAssertionSignatureValidation"
                                               type="checkbox" <%=enablePassiveSTSAssertionSignatureValidationChecked%>/>
                                        <span style="display:inline-block" class="sectionHelp">
                                    <fmt:message key='passive.sts.enable.assertion.signature.validation.help'/>
                                </span>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td class="leftCol-med labelField">
                                    <label for="enablePassiveSTSAssertionAudienceValidation">
                                        <fmt:message key='passive.sts.enable.assertion.audience.validation'/>
                                    </label>
                                </td>
                                <td>
                                    <div class="sectionCheckbox">
                                        <input id="isEnablePassiveSTSAssertionAudienceValidation"
                                               name="isEnablePassiveSTSAssertionAudienceValidation"
                                               type="checkbox" <%=enablePassiveSTSAssertionAudienceValidationChecked%>/>
                                        <span style="display:inline-block" class="sectionHelp">
                                    <fmt:message key='passive.sts.enable.assertion.audience.validation.help'/>
                                </span>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td class="leftCol-med labelField"><fmt:message key='query.param'/>:</td>
                                <td>
                                    <input id="passiveSTSQueryParam" name="passiveSTSQueryParam" type="text"
                                           value="<%=Encode.forHtml(passiveSTSQueryParam)%>"/>

                                    <div class="sectionHelp">
                                        <fmt:message key='query.param.help'/>
                                    </div>
                                </td>
                            </tr>
                        </table>
                    </div>

                    <% } %>


                    <% if (isFacebookAuthenticatorActive) { %>

                    <h2 id="fb_auth_head" class="sectionSeperator trigger active" style="background-color: beige;">
                        <a href="#"><fmt:message key="fbauth.config"/></a>

                        <div id="fecebook_enable_logo" class="enablelogo"
                             style="float:right;padding-right: 5px;padding-top: 5px;"><img
                                src="images/ok.png" alt="enable" width="16" height="16"></div>
                    </h2>
                    <div class="toggle_container sectionSub" style="margin-bottom:10px;" id="fbAuthLinkRow">
                        <table class="carbonFormTable">
                            <tr>
                                <td class="leftCol-med labelField">
                                    <label for="fbAuthEnabled"><fmt:message key='fbauth.enabled'/></label>
                                </td>
                                <td>
                                    <div class="sectionCheckbox">
                                        <input id="fbAuthEnabled" name="fbAuthEnabled"
                                               type="checkbox" <%=fbAuthEnabledChecked%>
                                               onclick="checkEnabled(this);"/>
                                        <span style="display:inline-block" class="sectionHelp">
                                    <fmt:message key='fbauth.enabled.help'/>
                                </span>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td class="leftCol-med labelField">
                                    <label for="fbAuthDefault"><fmt:message key='fbauth.default'/></label>
                                </td>
                                <td>
                                    <div class="sectionCheckbox">
                                        <input id="fbAuthDefault" name="fbAuthDefault"
                                               type="checkbox" <%=fbAuthDefaultChecked%> <%=fbAuthDefaultDisabled%>
                                               onclick="checkDefault(this);"/>
                                        <span style="display:inline-block" class="sectionHelp">
                                    <fmt:message key='fbauth.default.help'/>
                                </span>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td class="leftCol-med labelField"><fmt:message key='client.id'/>:<span
                                        class="required">*</span></td>
                                <td>
                                    <input id="fbClientId" name="fbClientId" type="text"
                                           value="<%=Encode.forHtmlAttribute(fbClientId)%>"/>

                                    <div class="sectionHelp">
                                        <fmt:message key='fbauth.client.id.help'/>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td class="leftCol-med labelField"><fmt:message key='client.secret'/>:<span
                                        class="required">*</span></td>
                                <td>
                                    <div id="showHideButtonDivId" style="border:1px solid rgb(88, 105, 125);"
                                         class="leftCol-med">
                                        <input id="fbClientSecret" name="fbClientSecret" type="password"
                                               autocomplete="off" value="<%=Encode.forHtmlAttribute(fbClientSecret)%>"
                                               style="  outline: none; border: none; min-width: 175px; max-width: 180px;"/>
                                        <span id="showHideButtonId" style=" float: right; padding-right: 5px;">
       								<a style="margin-top: 5px;" class="showHideBtn"
                                       onclick="showHidePassword(this, 'fbClientSecret')">Show</a>
       							</span>
                                    </div>

                                    <div class="sectionHelp"><fmt:message key='fbauth.client.secret.help'/></div>
                                </td>

                            </tr>
                            <tr>
                                <td class="leftCol-med labelField"><fmt:message key='fbauth.scope'/>:</td>
                                <td>
                                    <input id="fbScope" name="fbScope" type="text"
                                           value="<%=Encode.forHtmlAttribute(fbScope)%>"/>

                                    <div class="sectionHelp">
                                        <fmt:message key='fbauth.scope.help'/>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td class="leftCol-med labelField"><fmt:message
                                        key='fbauth.user.information.fields'/>:
                                </td>
                                <td>
                                    <input id="fbUserInfoFields" name="fbUserInfoFields" type="text"
                                           value="<%=Encode.forHtmlAttribute(fbUserInfoFields)%>"/>

                                    <div class="sectionHelp">
                                        <fmt:message key='fbauth.user.information.fields.help'/>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td class="leftCol-med labelField"><fmt:message key='fbauth.callback.url.fields'/>:</td>
                                <td>
                                    <input id="fbCallBackUrl" name="fbCallBackUrl" type="text"
                                           value="<%=Encode.forHtmlAttribute(fbCallBackUrl)%>"/>

                                    <div class="sectionHelp">
                                        <fmt:message key='fbauth.callback.url.fields.help'/>
                                    </div>
                                </td>
                            </tr>
                            <tr style="display:none;">
                                <td class="leftCol-med labelField">Facebook Authentication Endpoint:<span
                                        class="required">*</span></td>
                                <td><input class="text-box-big" id="fbAuthnEndpoint"
                                           name="fbAuthnEndpoint" type="text"
                                           value=<%=Encode.forHtmlAttribute(fbAuthnEndpoint)%>></td>
                            </tr>
                            <tr style="display:none;">
                                <td class="leftCol-med labelField">Facebook OAuth2 Token Endpoint:<span
                                        class="required">*</span></td>
                                <td><input class="text-box-big" id="fbOauth2TokenEndpoint"
                                           name="fbOauth2TokenEndpoint" type="text"
                                           value=<%=Encode.forHtmlAttribute(fbOauth2TokenEndpoint)%>></td>
                            </tr>
                            <tr style="display:none;">
                                <td class="leftCol-med labelField">Facebook User Information Endpoint:<span
                                        class="required">*</span></td>
                                <td><input class="text-box-big" id="fbUserInfoEndpoint"
                                           name="fbUserInfoEndpoint" type="text"
                                           value=<%=Encode.forHtmlAttribute(fbUserInfoEndpoint)%>></td>
                            </tr>
                        </table>
                    </div>

                    <% } %>

                    <%

                        if (allFedAuthConfigs != null && allFedAuthConfigs.size() > 0) {

                            for (Map.Entry<String, FederatedAuthenticatorConfig> entry : allFedAuthConfigs.entrySet()) {
                                FederatedAuthenticatorConfig fedConfig = entry.getValue();
                                if (fedConfig != null) {
                                    boolean isEnabled = fedConfig.getEnabled();

                                    boolean isDefault = false;

                                    if (identityProvider != null && identityProvider.getDefaultAuthenticatorConfig() != null && identityProvider.getDefaultAuthenticatorConfig().getDisplayName() != null
                                            && identityProvider.getDefaultAuthenticatorConfig().getName().equals(fedConfig.getName())) {
                                        isDefault = true;
                                    }


                                    String valueChecked = "";
                                    String valueDefaultDisabled = "";

                                    String enableChecked = "";
                                    String enableDefaultDisabled = "";

                                    if (isDefault) {
                                        valueChecked = "checked=\'checked\'";
                                        valueDefaultDisabled = "disabled=\'disabled\'";
                                    }

                                    if (isEnabled) {
                                        enableChecked = "checked=\'checked\'";
                                        enableDefaultDisabled = "disabled=\'disabled\'";
                                    }

                                    if (fedConfig.getDisplayName() != null && fedConfig.getDisplayName().trim().length() > 0) {

                    %>

                    <h2 id="custom_auth_head_"<%=fedConfig.getDisplayName() %> class="sectionSeperator trigger active"
                        style="background-color: beige;">
                        <a href="#" style="text-transform:capitalize;"><%=fedConfig.getDisplayName()%> Configuration</a>
                        <% if (isEnabled) { %>
                        <div id="custom_auth_head_enable_logo_<%=fedConfig.getName()%>" class="enablelogo"
                             style="float:right;padding-right: 5px;padding-top: 5px;"><img src="images/ok.png"
                                                                                           alt="enable" width="16"
                                                                                           height="16"></div>
                        <%} else {%>
                        <div id="custom_auth_head_enable_logo_<%=fedConfig.getName()%>" class="enablelogo"
                             style="float:right;padding-right: 5px;padding-top: 5px; display: none"><img
                                src="images/ok.png" alt="enable"
                                width="16" height="16"></div>
                        <%}%>
                    </h2>
                    <div class="toggle_container sectionSub" style="margin-bottom:10px;display: none;"
                         id="custom_auth_<%=fedConfig.getName()%>">
                        <table class="carbonFormTable">
                            <tr>
                                <td class="leftCol-med labelField">
                                    <input type="hidden" name="custom_auth_name" value=<%=fedConfig.getName()%>>
                                    <input type="hidden" name="<%=fedConfig.getName()%>_DisplayName"
                                           value=<%=fedConfig.getDisplayName()%>>

                                    <label for="<%=fedConfig.getName()%>Enabled">Enable</label>
                                </td>
                                <td>
                                    <div class="sectionCheckbox">
                                        <input id="<%=fedConfig.getName()%>_Enabled"
                                               name="<%=fedConfig.getName()%>_Enabled"
                                               type="checkbox" <%=enableChecked%>
                                               onclick="checkEnabled(this); checkEnabledLogo(this, '<%=fedConfig.getName()%>')"/>
                                        <span style="display:inline-block" class="sectionHelp">Specifies if custom authenticator is enabled for this Identity Provider
                                </span>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td class="leftCol-med labelField">
                                    <label for="<%=fedConfig.getName()%>_Default">Default</label>
                                </td>
                                <td>
                                    <div class="sectionCheckbox">
                                        <input id="<%=fedConfig.getName()%>_Default"
                                               name="<%=fedConfig.getName()%>_Default"
                                               type="checkbox" <%=valueChecked%> <%=valueDefaultDisabled%>
                                               onclick="checkDefault(this);"/>
                                        <span style="display:inline-block" class="sectionHelp">Specifies if custom authenticator is the default
                                </span>
                                    </div>
                                </td>
                            </tr>

                            <% Property[] properties = fedConfig.getProperties();

                                if (properties != null && properties.length > 0) {
                                    Arrays.sort(properties, new Comparator<Property>() {
                                        public int compare(Property obj1, Property obj2) {
                                            Property property1 = (Property) obj1;
                                            Property property2 = (Property) obj2;
                                            if (property1.getDisplayOrder() == property2.getDisplayOrder())
                                                return 0;
                                            else if (property1.getDisplayOrder() > property2.getDisplayOrder())
                                                return 1;
                                            else
                                                return -1;
                                        }
                                    });
                                    for (Property prop : properties) {
                                        if (prop != null && prop.getDisplayName() != null) {
                            %>

                            <tr>
                                <%if (prop.getRequired()) { %>
                                <td class="leftCol-med labelField"><%=prop.getDisplayName()%>:<span
                                        class="required">*</span></td>
                                <% } else { %>
                                <td class="leftCol-med labelField"><%=prop.getDisplayName()%>:</td>
                                <%} %>
                                <td>
                                    <% if (prop.getConfidential()) { %>

                                    <% if (prop.getValue() != null) { %>
                                    <div id="showHideButtonDivId" style="border:1px solid rgb(88, 105, 125);"
                                         class="leftCol-med">
                                        <input id="cust_auth_prop_<%=fedConfig.getName()%>#<%=prop.getName()%>"
                                               name="cust_auth_prop_<%=fedConfig.getName()%>#<%=prop.getName()%>"
                                               type="password" autocomplete="off"
                                               value="<%=prop.getValue()%>"
                                               style="  outline: none; border: none; min-width: 175px; max-width: 180px;"/>
                                        <span id="showHideButtonId"
                                              style=" float: right; padding-right: 5px;">
       													<a style="margin-top: 5px;" class="showHideBtn"
                                                           onclick="showHidePassword(this, 'cust_auth_prop_<%=fedConfig.getName()%>#<%=prop.getName()%>')">Show</a>
       												</span>
                                    </div>
                                    <% } else { %>

                                    <div id="showHideButtonDivId" style="border:1px solid rgb(88, 105, 125);"
                                         class="leftCol-med">
                                        <input id="cust_auth_prop_<%=fedConfig.getName()%>#<%=prop.getName()%>"
                                               name="cust_auth_prop_<%=fedConfig.getName()%>#<%=prop.getName()%>"
                                               type="password" autocomplete="off"
                                               style="  outline: none; border: none; min-width: 175px; max-width: 180px;"/>
                                        <span id="showHideButtonId"
                                              style=" float: right; padding-right: 5px;">
       													<a style="margin-top: 5px;" class="showHideBtn"
                                                           onclick="showHidePassword(this, 'cust_auth_prop_<%=fedConfig.getName()%>#<%=prop.getName()%>')">Show</a>
       												</span>
                                    </div>

                                    <% } %>

                                    <% } else { %>

                                    <% if (prop.getValue() != null) { %>
                                    <input id="cust_auth_prop_<%=fedConfig.getName()%>#<%=prop.getName()%>"
                                           name="cust_auth_prop_<%=fedConfig.getName()%>#<%=prop.getName()%>"
                                           type="text"
                                           value="<%=prop.getValue()%>"/>
                                    <% } else { %>
                                    <input id="cust_auth_prop_<%=fedConfig.getName()%>#<%=prop.getName()%>"
                                           name="cust_auth_prop_<%=fedConfig.getName()%>#<%=prop.getName()%>"
                                           type="text">
                                    <% } %>

                                    <% } %>

                                    <%
                                        if (prop.getDescription() != null) { %>
                                    <div class="sectionHelp"><%=prop.getDescription()%>
                                    </div>
                                    <%} %>
                                </td>
                            </tr>
                            <%
                                        }
                                    }
                                }
                            %>

                        </table>
                    </div>

                    <%
                                    }
                                }
                            }
                        }
                    %>

                </div>

                <h2 id="in_bound_provisioning_head" class="sectionSeperator trigger active">
                    <a href="#"><fmt:message key="in.bound.provisioning.config"/></a>
                </h2>

                <div class="toggle_container sectionSub" style="margin-bottom:10px;" id="inBoundProvisioning">
                    <table>
                        <tr>
                            <td>
                                <label style="display:block">
                                    <input type="radio" id="provision_disabled" name="provisioning"
                                           value="provision_disabled" <% if (!isProvisioningEnabled) { %>
                                           checked="checked" <% } %> />
                                    No provisioning
                                </label>

                                <div>
                                    <label>
                                        <input type="radio" id="provision_static" name="provisioning"
                                               value="provision_static" <% if (isProvisioningEnabled && provisioningUserStoreId != null) { %>
                                               checked="checked" <% } %>/>
                                        Always provision to User Store Domain
                                    </label>
                                    <select id="provision_static_dropdown"
                                            name="provision_static_dropdown" <%=provisionStaticDropdownDisabled%>>
                                        <%
                                            if (userStoreDomains != null && userStoreDomains.length > 0) {
                                                for (String userStoreDomain : userStoreDomains) {
                                                    if (provisioningUserStoreId != null && userStoreDomain.equals(provisioningUserStoreId)) {
                                        %>
                                        <option selected="selected"><%=Encode.forHtmlContent(userStoreDomain)%>
                                        </option>
                                        <%
                                        } else {
                                        %>
                                        <option><%=userStoreDomain%>
                                        </option>
                                        <%
                                                    }
                                                }
                                            }
                                        %>
                                    </select>

                                </div>

                                <div class="sectionHelp">
                                    <fmt:message key='provisioning.enabled.help'/>
                                </div>
                                <div style="padding-left: 40px; !important">
                                    <label style="display:block">
                                        <input type="radio" id="modify_username_password" name="choose_jit_type_group"
                                               value="modify_username_password" <% if (isPasswordProvisioningEnabled
                                                && isUserNameModificationAllowed) { %>
                                               checked="checked" <% } if(!isProvisioningEnabled) { %> disabled
                                                <%}%>/>
                                        Ask username and password
                                    </label>
                                </div>
                                <div style="padding-left: 40px; !important">
                                    <label style="display:block">
                                        <input type="radio" id=modify_password" name="choose_jit_type_group"
                                               value="modify_password"  <% if (isPasswordProvisioningEnabled &&
                                                !isUserNameModificationAllowed) { %>
                                               checked="checked" <% } if(!isProvisioningEnabled) { %> disabled
                                                <%}%>/>
                                        Ask password
                                    </label>
                                </div>
                                <div style="padding-left: 40px; !important">
                                    <label style="display:block">
                                        <input type="radio" id="do_not_modify" name="choose_jit_type_group"
                                               value="don_not_modify"  <% if (!isPasswordProvisioningEnabled &&
                                                !isUserNameModificationAllowed) { %>
                                               checked="checked" <% } if(!isProvisioningEnabled) { %> disabled
                                                <%}%>/>
                                        Do not ask username or password
                                    </label>
                                </div>
                            </td>
                        </tr>
                    </table>
                </div>


                <!-- Outbound Provisioning UI -->
                <h2 id="out_bound_provisioning_head" class="sectionSeperator trigger active">
                    <a href="#"><fmt:message key="out.bound.provisioning.config"/></a>
                </h2>


                <div class="toggle_container sectionSub"
                     style="margin-bottom: 10px; display: none;" id="outBoundProv">

                    <!-- Google Connector -->
                    <h2 id="google_prov_head" class="sectionSeperator trigger active"
                        style="background-color: beige;">
                        <a href="#"><fmt:message key="google.provisioning.connector"/></a>

                        <div id="google_enable_logo" class="enablelogo"
                             style="float:right;padding-right: 5px;padding-top: 5px;"><img
                                src="images/ok.png" alt="enable" width="16" height="16"></div>
                    </h2>
                    <div class="toggle_container sectionSub"
                         style="margin-bottom: 10px; display: none;" id="googleProvRow">
                        <table class="carbonFormTable">
                            <tr>
                                <td class="leftCol-med labelField"><label
                                        for="googleProvEnabled"><fmt:message
                                        key='google.provisioning.enabled'/>:</label></td>
                                <td>
                                    <div class="sectionCheckbox">
                                        <!-- -->
                                        <input id="googleProvEnabled" name="googleProvEnabled"
                                               type="checkbox" <%=googleProvEnabledChecked%>
                                               onclick="checkProvEnabled(this);"/> <span
                                            style="display: inline-block" class="sectionHelp"> <fmt:message
                                            key='google.provisioning.enabled.help'/>
										</span>
                                    </div>
                                </td>
                            </tr>
                            <tr style="display:none;">
                                <td class="leftCol-med labelField"><label
                                        for="googleProvDefault"><fmt:message
                                        key='google.provisioning.default'/>:</label></td>
                                <td>
                                    <div class="sectionCheckbox">
                                        <!-- -->
                                        <input id="googleProvDefault" name="googleProvDefault"
                                               type="checkbox" <%=googleProvDefaultChecked%>
                                                <%=googleProvDefaultDisabled%>
                                               onclick="checkProvDefault(this);"/> <span
                                            style="display: inline-block" class="sectionHelp"> <fmt:message
                                            key='google.provisioning.default.help'/>
										</span>
                                    </div>
                                </td>
                            </tr>

                            <tr>
                                <td class="leftCol-med labelField"><fmt:message
                                        key='google.provisioning.domain.name'/>:<span class="required">*</span></td>
                                <td><input id="google_prov_domain_name"
                                           name="google_prov_domain_name" type="text"
                                           value="<%=Encode.forHtmlAttribute(googleDomainName)%>"/>

                                    <div class="sectionHelp">
                                        <fmt:message key='google.provisioning.domain.name.help'/>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td class="leftCol-med labelField"><fmt:message
                                        key='google.provisioning.attribute.primary.email'/>:<span
                                        class="required">*</span></td>
                                <td>
                                    <div>
                                        <select id="google_prov_email_claim_dropdown"
                                                name="google_prov_email_claim_dropdown">
                                        </select>
                                        <!--a id="claimMappingAddLink" class="icon-link" style="background-image: url(images/add.gif);"><fmt:message key='button.add.claim.mapping' /></a-->
                                    </div>
                                    <div class="sectionHelp">
                                        <fmt:message
                                                key='google.provisioning.attribute.primary.email.help'/>
                                    </div>
                                </td>
                            </tr>

                            <tr>
                                <td class="leftCol-med labelField"><fmt:message
                                        key='google.provisioning.attribute.given.name'/>:<span class="required">*</span>
                                </td>
                                <td>
                                    <div>
                                        <label> <!-- --> Pick given name from Claim :
                                        </label> <select id="google_prov_givenname_claim_dropdown"
                                                         name="google_prov_givenname_claim_dropdown">
                                    </select>
                                    </div>
                                    <div style=" display: none; ">
                                        <label> Given name default value : </label> <input
                                            id="google_prov_givenname" name="google_prov_givenname"
                                            type="text"
                                            value="<%=Encode.forHtmlAttribute(googleGivenNameDefaultValue)%>"/>
                                    </div>
                                    <div class="sectionHelp">
                                        <fmt:message
                                                key='google.provisioning.attribute.given.name.help'/>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td class="leftCol-med labelField"><fmt:message
                                        key='google.provisioning.attribute.family.name'/>:<span
                                        class="required">*</span></td>
                                <td>
                                    <div>
                                        <label> Pick family name from Claim : </label> <select
                                            id="google_prov_familyname_claim_dropdown"
                                            name="google_prov_familyname_claim_dropdown">
                                    </select>
                                    </div>
                                    <div style=" display: none;">
                                        <label> Family name default value : </label> <input
                                            id="google_prov_familyname" name="google_prov_familyname"
                                            type="text"
                                            value="<%=Encode.forHtmlAttribute(googleFamilyNameDefaultValue)%>"/>
                                    </div>
                                    <div class="sectionHelp">
                                        <fmt:message
                                                key='google.provisioning.attribute.family.name.help'/>
                                    </div>
                                </td>
                            </tr>

                            <tr>
                                <td class="leftCol-med labelField"><fmt:message
                                        key='google.provisioning.service.accont.email'/>:<span class="required">*</span>
                                </td>
                                <td>
                                    <div>
                                        <input id="google_prov_service_acc_email"
                                               name="google_prov_service_acc_email" type="text"
                                               value="<%=Encode.forHtmlAttribute(googleProvServiceAccEmail)%>"/>
                                    </div>
                                    <div class="sectionHelp">
                                        <fmt:message
                                                key='google.provisioning.service.accont.email.help'/>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td class="leftCol-med labelField"><fmt:message
                                        key='google.provisioning.service.account.private.key'/>:
                                </td>
                                <td><span><input id="google_prov_private_key"
                                                 name="google_prov_private_key" type="file"/>
									<% if (googleProvPrivateKeyData != null) { %>
                                         <img src="images/key.png" alt="key" width="14" height="14"
                                              style=" padding-right: 5px; "><label>Private Key attached</label>
									<% } %></span>

                                    <div class="sectionHelp">
                                        <fmt:message
                                                key='google.provisioning.service.account.private.key.help'/>
                                    </div>
                                    <div id="google_prov_privatekey_div">

                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td class="leftCol-med labelField"><fmt:message
                                        key='google.provisioning.admin.email'/>:<span class="required">*</span></td>
                                <td>
                                    <div>
                                        <input id="google_prov_admin_email"
                                               name="google_prov_admin_email" type="text"
                                               value="<%=Encode.forHtmlAttribute(googleProvAdminEmail)%>"/>
                                    </div>
                                    <div class="sectionHelp">
                                        <fmt:message key='google.provisioning.admin.email.help'/>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td class="leftCol-med labelField"><fmt:message
                                        key='google.provisioning.application.name'/>:<span class="required">*</span>
                                </td>
                                <td>
                                    <div>
                                        <input id="google_prov_application_name"
                                               name="google_prov_application_name" type="text"
                                               value="<%=Encode.forHtmlAttribute(googleProvApplicationName)%>"/>
                                    </div>
                                    <div class="sectionHelp">
                                        <fmt:message key='google.provisioning.application.name.help'/>
                                    </div>
                                </td>
                            </tr>

                            <tr>
                                <td class="leftCol-med labelField"><fmt:message
                                        key='google.provisioning.pattern'/>:
                                </td>
                                <td>
                                    <div>
                                        <input id="google_prov_pattern"
                                               name="google_prov_pattern" type="text"
                                               value="<%=Encode.forHtmlAttribute(googleProvPattern)%>"/>
                                    </div>
                                    <div class="sectionHelp">
                                        <fmt:message key='google_prov_pattern.help'/>
                                    </div>
                                </td>
                            </tr>

                            <tr>
                                <td class="leftCol-med labelField"><fmt:message
                                        key='google.provisioning.separator'/>:
                                </td>
                                <td>
                                    <div>
                                        <input id="google_prov_separator"
                                               name="google_prov_separator" type="text"
                                               value="<%=Encode.forHtmlAttribute(googleProvisioningSeparator)%>"/>
                                    </div>
                                    <div class="sectionHelp">
                                        <fmt:message key='google.provisioning.separator.help'/>
                                    </div>
                                </td>
                            </tr>

                        </table>
                    </div>

                        <%--<%@ include file="salesforce.jsp"%>--%>
                    <jsp:include page="salesforce.jsp"></jsp:include>

                    <h2 id="scim_prov_head" class="sectionSeperator trigger active"
                        style="background-color: beige;">
                        <a href="#"><fmt:message key="scim.provisioning.connector"/></a>

                        <div id="scim_enable_logo" class="enablelogo"
                             style="float:right;padding-right: 5px;padding-top: 5px;"><img
                                src="images/ok.png" alt="enable" width="16" height="16"></div>

                    </h2>
                    <div class="toggle_container sectionSub"
                         style="margin-bottom: 10px; display: none;" id="scimProvRow">

                        <table class="carbonFormTable">
                            <tr>
                                <td class="leftCol-med labelField"><label
                                        for="scimProvEnabled"><fmt:message
                                        key='scim.provisioning.enabled'/>:</label></td>
                                <td>
                                    <div class="sectionCheckbox">
                                        <!-- -->
                                        <input id="scimProvEnabled" name="scimProvEnabled"
                                               type="checkbox" <%=scimProvEnabledChecked%>
                                               onclick="checkProvEnabled(this);"/> <span
                                            style="display: inline-block" class="sectionHelp"> <fmt:message
                                            key='scim.provisioning.enabled.help'/>
                                        </span>
                                    </div>
                                </td>
                            </tr>
                            <tr style="display:none;">
                                <td class="leftCol-med labelField"><label
                                        for="scimProvDefault"><fmt:message
                                        key='scim.provisioning.default'/>:</label></td>
                                <td>
                                    <div class="sectionCheckbox">
                                        <!-- -->
                                        <input id="scimProvDefault" name="scimProvDefault"
                                               type="checkbox" <%=scimProvDefaultChecked%>
                                                <%=scimProvDefaultDisabled%>
                                               onclick="checkProvDefault(this);"/> <span
                                            style="display: inline-block" class="sectionHelp"> <fmt:message
                                            key='scim.provisioning.default.help'/>
                                        </span>
                                    </div>
                                </td>
                            </tr>

                            <tr>
                                <td class="leftCol-med labelField"><fmt:message
                                        key='scim.provisioning.user.name'/>:<span
                                        class="required">*</span></td>
                                <td><input class="text-box-big" id="scim-username"
                                           name="scim-username" type="text"
                                           value=<%=Encode.forHtmlAttribute(scimUserName) %>></td>
                            </tr>
                            <tr>
                                <td class="leftCol-med labelField"><fmt:message
                                        key='scim.provisioning.user.password'/>:<span
                                        class="required">*</span></td>
                                <td><input class="text-box-big" id="scim-password"
                                           name="scim-password" type="password" autocomplete="off"
                                           value=<%=Encode.forHtmlAttribute(scimPassword) %>></td>
                            </tr>
                            <tr>
                                <td class="leftCol-med labelField"><fmt:message
                                        key='scim.provisioning.user.endpoint'/>:<span
                                        class="required">*</span></td>
                                <td><input class="text-box-big" id="scim-user-ep"
                                           name="scim-user-ep" type="text"
                                           value=<%=Encode.forHtmlAttribute(scimUserEp) %>></td>
                            </tr>
                            <tr>
                                <td class="leftCol-med labelField"><fmt:message
                                        key='scim.provisioning.group.endpoint'/>:
                                </td>
                                <td><input class="text-box-big" id="scim-group-ep"
                                           name="scim-group-ep" type="text"
                                           value=<%=Encode.forHtmlAttribute(scimGroupEp) %>></td>
                            </tr>
                            <tr>
                                <td class="leftCol-med labelField"><fmt:message
                                        key='scim.provisioning.userStore.domain'/>:
                                </td>
                                <td><input class="text-box-big" id="scim-user-store-domain"
                                           name="scim-user-store-domain" type="text"
                                           value=<%=Encode.forHtmlAttribute(scimUserStoreDomain)%>></td>
                            </tr>
                            <tr>
                                <td class="leftCol-med labelField"><label><fmt:message
                                        key='scim.password.provisioning.enabled'/>:</label></td>
                                <td>
                                    <div class="sectionCheckbox">
                                        <!-- -->
                                        <input id="scimPwdProvEnabled" name="scimPwdProvEnabled"
                                               type="checkbox" <%=scimPwdProvEnabledChecked%>
                                               onclick="disableDefaultPwd(this);"/>
                                        <span style="display: inline-block" class="sectionHelp"> <fmt:message
                                                key='scim.password.provisioning.enabled.help'/>
                                        </span>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td class="leftCol-med labelField">
                                    <fmt:message key='scim.default.password'/>:
                                </td>
                                <td><input class="text-box-big" id="scim-default-pwd" <%=disableDefaultPwd%>
                                           name="scim-default-pwd" type="text" value=<%=scimDefaultPwd%>></td>
                                <%if (scimUniqueID != null) {%>
                                <input type="hidden" id="scim-unique-id" name="scim-unique-id"
                                       value=<%=Encode.forHtmlAttribute(scimUniqueID)%>>
                                <%}%>
                            </tr>
                        </table>

                    </div>

                    <h2 id="spml_prov_head" class="sectionSeperator trigger active"
                        style="background-color: beige;">
                        <a href="#"><fmt:message key="spml.provisioning.connector"/></a>

                        <div id="spml_enable_logo" class="enablelogo"
                             style="float:right;padding-right: 5px;padding-top: 5px;"><img
                                src="images/ok.png" alt="enable" width="16" height="16"></div>

                    </h2>
                    <div class="toggle_container sectionSub"
                         style="margin-bottom: 10px; display: none;" id="spmlProvRow">

                        <table class="carbonFormTable">

                            <tr>
                                <td class="leftCol-med labelField"><label
                                        for="spmlProvEnabled"><fmt:message
                                        key='spml.provisioning.enabled'/>:</label></td>
                                <td>
                                    <div class="sectionCheckbox">
                                        <!-- -->
                                        <input id="spmlProvEnabled" name="spmlProvEnabled"
                                               type="checkbox" <%=spmlProvEnabledChecked%>
                                               onclick="checkProvEnabled(this);"/> <span
                                            style="display: inline-block" class="sectionHelp"> <fmt:message
                                            key='spml.provisioning.enabled.help'/>
                                        </span>
                                    </div>
                                </td>
                            </tr>
                            <tr style="display:none;">
                                <td class="leftCol-med labelField"><label
                                        for="spmlProvDefault"><fmt:message
                                        key='spml.provisioning.default'/>:</label></td>
                                <td>
                                    <div class="sectionCheckbox">
                                        <!-- -->
                                        <input id="spmlProvDefault" name="spmlProvDefault"
                                               type="checkbox" <%=spmlProvDefaultChecked%>
                                                <%=spmlProvDefaultDisabled%>
                                               onclick="checkProvDefault(this);"/> <span
                                            style="display: inline-block" class="sectionHelp"> <fmt:message
                                            key='spml.provisioning.default.help'/>
                                        </span>
                                    </div>
                                </td>
                            </tr>

                            <tr>
                                <td class="leftCol-med labelField"><fmt:message
                                        key='spml.provisioning.user.name'/>:
                                </td>
                                <td><input class="text-box-big" id="spml-username"
                                           name="spml-username" type="text"
                                           value=<%=Encode.forHtmlAttribute(spmlUserName) %>></td>
                            </tr>
                            <tr>
                                <td class="leftCol-med labelField"><fmt:message
                                        key='spml.provisioning.user.password'/>:
                                </td>
                                <td><input class="text-box-big" id="spml-password"
                                           name="spml-password" type="password" autocomplete="off"
                                           value=<%=Encode.forHtmlAttribute(spmlPassword) %>></td>
                            </tr>
                            <tr>
                                <td class="leftCol-med labelField"><fmt:message
                                        key='spml.provisioning.endpoint'/>:<span
                                        class="required">*</span></td>
                                <td><input class="text-box-big" id="spml-ep" name="spml-ep"
                                           type="text" value=<%=Encode.forHtmlAttribute(spmlEndpoint) %>></td>
                            </tr>

                            <tr>
                                <td class="leftCol-med labelField"><fmt:message
                                        key='spml.provisioning.objectClass'/>:<span
                                        class="required">*</span></td>
                                <td><input class="text-box-big" id="spml-oc" name="spml-oc"
                                           type="text" value=<%=Encode.forHtmlAttribute(spmlObjectClass) %>></td>
                                <%if (spmlUniqueID != null) {%>
                                <input type="hidden" id="spml-unique-id" name="spml-unique-id"
                                       value=<%=Encode.forHtmlAttribute(spmlUniqueID)%>>
                                <%}%>
                            </tr>

                        </table>
                    </div>

                    <%

                        if (customProvisioningConnectors != null && customProvisioningConnectors.size() > 0) {

                            for (Map.Entry<String, ProvisioningConnectorConfig> entry : customProvisioningConnectors.entrySet()) {
                                ProvisioningConnectorConfig fedConfig = entry.getValue();
                                if (fedConfig != null) {
                                    boolean isEnabled = fedConfig.getEnabled();


                                    String enableChecked = "";

                                    if (isEnabled) {
                                        enableChecked = "checked=\'checked\'";
                                    }

                                    if (fedConfig.getName() != null && fedConfig.getName().trim().length() > 0) {

                    %>

                    <h2 id="custom_pro_head_"<%=fedConfig.getName() %> class="sectionSeperator trigger active"
                        style="background-color: beige;">
                        <a href="#" style="text-transform:capitalize;"><%=fedConfig.getName()%> Provisioning
                            Configuration</a>
                        <% if (isEnabled) { %>
                        <div id="custom_pro_head_enable_logo_<%=fedConfig.getName()%>" class="enablelogo"
                             style="float:right;padding-right: 5px;padding-top: 5px;"><img src="images/ok.png"
                                                                                           alt="enable" width="16"
                                                                                           height="16"></div>
                        <%} else {%>
                        <div id="custom_pro_head_enable_logo_<%=fedConfig.getName()%>" class="enablelogo"
                             style="float:right;padding-right: 5px;padding-top: 5px; display: none"><img
                                src="images/ok.png" alt="enable"
                                width="16" height="16"></div>
                        <%}%>
                    </h2>
                    <div class="toggle_container sectionSub" style="margin-bottom:10px;display: none;"
                         id="custom_pro_<%=fedConfig.getName()%>">
                        <table class="carbonFormTable">
                            <tr>
                                <td class="leftCol-med labelField">
                                    <input type="hidden" name="custom_pro_name" value=<%=fedConfig.getName()%>>

                                    <label for="<%=fedConfig.getName()%>Enabled">Enable</label>
                                </td>
                                <td>
                                    <div class="sectionCheckbox">
                                        <input id="<%=fedConfig.getName()%>_PEnabled"
                                               name="<%=fedConfig.getName()%>_PEnabled"
                                               type="checkbox" <%=enableChecked%>
                                               onclick="checkEnabledLogo(this, '<%=fedConfig.getName()%>')"/>
                                        <span style="display:inline-block" class="sectionHelp">Specifies if custom provisioning connector is enabled for this Identity Provider
                                </span>
                                    </div>
                                </td>
                            </tr>


                            <%
                                Property[] properties = fedConfig.getProvisioningProperties();
                                if (properties != null && properties.length > 0) {
                                    for (Property prop : properties) {
                                        if (prop != null && prop.getDisplayName() != null) {
                            %>

                            <tr>
                                <%if (prop.getRequired()) { %>
                                <td class="leftCol-med labelField"><%=prop.getDisplayName()%>:<span
                                        class="required">*</span></td>
                                <% } else { %>
                                <td class="leftCol-med labelField"><%=prop.getDisplayName()%>:</td>
                                <%} %>
                                <td>
                                    <% if (prop.getConfidential()) { %>

                                    <% if (prop.getValue() != null) { %>
                                    <div id="showHideButtonDivId" style="border:1px solid rgb(88, 105, 125);"
                                         class="leftCol-med">
                                        <input id="cust_pro_prop_<%=fedConfig.getName()%>#<%=prop.getName()%>"
                                               name="cust_pro_prop_<%=fedConfig.getName()%>#<%=prop.getName()%>"
                                               type="password" autocomplete="off"
                                               value="<%=prop.getValue()%>"
                                               style="  outline: none; border: none; min-width: 175px; max-width: 180px;"/>
                                        <span id="showHideButtonId"
                                              style=" float: right; padding-right: 5px;">
       													<a style="margin-top: 5px;" class="showHideBtn"
                                                           onclick="showHidePassword(this, 'cust_pro_prop_<%=fedConfig.getName()%>#<%=prop.getName()%>')">Show</a>
       												</span>
                                    </div>
                                    <% } else { %>

                                    <div id="showHideButtonDivId" style="border:1px solid rgb(88, 105, 125);"
                                         class="leftCol-med">
                                        <input id="cust_pro_prop_<%=fedConfig.getName()%>#<%=prop.getName()%>"
                                               name="cust_pro_prop_<%=fedConfig.getName()%>#<%=prop.getName()%>"
                                               type="password" autocomplete="off"
                                               style="  outline: none; border: none; min-width: 175px; max-width: 180px;"/>
                                        <span id="showHideButtonId"
                                              style=" float: right; padding-right: 5px;">
       													<a style="margin-top: 5px;" class="showHideBtn"
                                                           onclick="showHidePassword(this, 'cust_pro_prop_<%=fedConfig.getName()%>#<%=prop.getName()%>')">Show</a>
       												</span>
                                    </div>

                                    <% } %>

                                    <% } else { %>

                                    <% if (prop.getValue() != null) { %>
                                    <input id="cust_pro_prop_<%=fedConfig.getName()%>#<%=prop.getName()%>"
                                           name="cust_pro_prop_<%=fedConfig.getName()%>#<%=prop.getName()%>" type="text"
                                           value="<%=prop.getValue()%>"/>
                                    <% } else { %>
                                    <input id="cust_pro_prop_<%=fedConfig.getName()%>#<%=prop.getName()%>"
                                           name="cust_pro_prop_<%=fedConfig.getName()%>#<%=prop.getName()%>"
                                           type="text">
                                    <% } %>

                                    <% } %>

                                    <%
                                        if (prop.getDescription() != null) { %>
                                    <div class="sectionHelp"><%=prop.getDescription()%>
                                    </div>
                                    <%} %>
                                </td>
                            </tr>
                            <%
                                        }
                                    }
                                }
                            %>

                        </table>
                    </div>

                    <%
                                    }
                                }
                            }
                        }
                    %>

                </div>


        </div>


        <!-- sectionSub Div -->
        <div class="buttonRow">
            <% if (identityProvider != null) { %>
            <input type="button" value="<fmt:message key='update'/>" onclick="idpMgtUpdate();"/>
            <% } else { %>
            <input type="button" value="<fmt:message key='register'/>" onclick="idpMgtUpdate();"/>
            <% } %>
            <input type="button" value="<fmt:message key='cancel'/>" onclick="idpMgtCancel();"/>
        </div>
        </form>
    </div>
    </div>


    <script type="text/javascript" src="../admin/js/main.js"></script>
    <script type="text/javascript" src="js/idp_mgt_edit.js"></script>

</fmt:bundle>
