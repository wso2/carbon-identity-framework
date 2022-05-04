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
package org.wso2.carbon.identity.application.mgt.ui;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.consent.mgt.core.model.Purpose;
import org.wso2.carbon.identity.application.common.model.script.xsd.AuthenticationScriptConfig;
import org.wso2.carbon.identity.application.common.model.xsd.ApplicationPermission;
import org.wso2.carbon.identity.application.common.model.xsd.AuthenticationStep;
import org.wso2.carbon.identity.application.common.model.xsd.Claim;
import org.wso2.carbon.identity.application.common.model.xsd.ClaimConfig;
import org.wso2.carbon.identity.application.common.model.xsd.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.xsd.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.xsd.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.xsd.InboundProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.xsd.JustInTimeProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.xsd.LocalAndOutboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.xsd.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.xsd.LocalRole;
import org.wso2.carbon.identity.application.common.model.xsd.OutboundProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.xsd.PermissionsAndRoleConfig;
import org.wso2.carbon.identity.application.common.model.xsd.Property;
import org.wso2.carbon.identity.application.common.model.xsd.ProvisioningConnectorConfig;
import org.wso2.carbon.identity.application.common.model.xsd.RequestPathAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.xsd.RoleMapping;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProviderProperty;
import org.wso2.carbon.identity.application.mgt.ui.util.ApplicationMgtUIConstants;
import org.wso2.carbon.identity.base.IdentityConstants;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * UI bean to represent an application.
 */
public class ApplicationBean {

    public static final String AUTH_TYPE_DEFAULT = "default";
    public static final String AUTH_TYPE_LOCAL = "local";
    public static final String AUTH_TYPE_FEDERATED = "federated";
    public static final String AUTH_TYPE_FLOW = "flow";
    public static final String IDP_LOCAL_NAME = "LOCAL";

    public static final String LOCAL_IDP = "wso2carbon-local-idp";
    public static final String DUMB = "dumb";

    private static final String LOGOUT_RETURN_URL = "logoutReturnUrl";

    private ServiceProvider serviceProvider;
    private IdentityProvider[] federatedIdentityProviders;
    private Map<String, IdentityProvider> federatedIdentityProvidersMap = new HashMap<>();
    private List<IdentityProvider> enabledFederatedIdentityProviders;
    private LocalAuthenticatorConfig[] localAuthenticatorConfigs;
    private RequestPathAuthenticatorConfig[] requestPathAuthenticators;
    private Map<String, String> roleMap;
    private Map<String, String> claimMap;
    private Map<String, String> requestedClaims = new HashMap<String, String>();
    private Map<String, String> mandatoryClaims = new HashMap<String, String>();
    private String samlIssuer;
    private String kerberosServiceName;
    private String oauthAppName;
    private String oauthConsumerSecret;
    private String attrConsumServiceIndex;
    private List<String> wstrustEp = new ArrayList<String>(0);
    private String passivests;
    private String passiveSTSWReply;
    private String openid;
    private String[] claimUris;
    private List<String> claimDialectUris;
    private List<InboundAuthenticationRequestConfig> inboundAuthenticationRequestConfigs;
    private List<String> standardInboundAuthTypes;
    private ApplicationPurposes applicationPurposes;
    private Purpose[] sharedPurposes;
    private Map<String, InboundAuthenticationRequestConfig> customInboundAuthenticatorConfigs;

    private static final Log log = LogFactory.getLog(ApplicationBean.class);

    public ApplicationBean() {
        standardInboundAuthTypes = new ArrayList<String>();
        standardInboundAuthTypes.add("oauth2");
        standardInboundAuthTypes.add("wstrust");
        standardInboundAuthTypes.add("samlsso");
        standardInboundAuthTypes.add("openid");
        standardInboundAuthTypes.add("passivests");
    }

    public void reset() {
        serviceProvider = null;
        federatedIdentityProviders = null;
        federatedIdentityProvidersMap.clear();
        localAuthenticatorConfigs = null;
        requestPathAuthenticators = null;
        roleMap = null;
        claimMap = null;
        requestedClaims = new HashMap<String, String>();
        mandatoryClaims = new HashMap<String, String>();
        samlIssuer = null;
        kerberosServiceName = null;
        oauthAppName = null;
        wstrustEp = new ArrayList<String>(0);
        passivests = null;
        passiveSTSWReply = null;
        openid = null;
        oauthConsumerSecret = null;
        attrConsumServiceIndex = null;
        enabledFederatedIdentityProviders = null;
        inboundAuthenticationRequestConfigs = Collections.EMPTY_LIST;
        applicationPurposes = null;
        sharedPurposes = null;
    }

    /**
     * @return
     */
    public ServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    /**
     * @param serviceProvider
     */
    public void setServiceProvider(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    /**
     * @return
     */
    public String getAuthenticationType() {
        return serviceProvider.getLocalAndOutBoundAuthenticationConfig().getAuthenticationType();
    }

    /**
     * @param type
     */
    public void setAuthenticationType(String type) {
        serviceProvider.getLocalAndOutBoundAuthenticationConfig().setAuthenticationType(type);
    }

    /**
     * @param type
     * @return
     */
    public String getStepZeroAuthenticatorName(String type) {
        if (AUTH_TYPE_LOCAL.equalsIgnoreCase(type)) {
            if (serviceProvider.getLocalAndOutBoundAuthenticationConfig().getAuthenticationSteps() != null
                    && serviceProvider.getLocalAndOutBoundAuthenticationConfig()
                    .getAuthenticationSteps().length > 0
                    && serviceProvider.getLocalAndOutBoundAuthenticationConfig()
                    .getAuthenticationSteps()[0].getLocalAuthenticatorConfigs() != null
                    && serviceProvider.getLocalAndOutBoundAuthenticationConfig()
                    .getAuthenticationSteps()[0].getLocalAuthenticatorConfigs()[0] != null) {
                return serviceProvider.getLocalAndOutBoundAuthenticationConfig()
                        .getAuthenticationSteps()[0].getLocalAuthenticatorConfigs()[0].getName();
            }
        }

        if (AUTH_TYPE_FEDERATED.equalsIgnoreCase(type)) {
            if (serviceProvider.getLocalAndOutBoundAuthenticationConfig().getAuthenticationSteps() != null
                    && serviceProvider.getLocalAndOutBoundAuthenticationConfig()
                    .getAuthenticationSteps().length > 0
                    && serviceProvider.getLocalAndOutBoundAuthenticationConfig()
                    .getAuthenticationSteps()[0].getFederatedIdentityProviders() != null
                    && serviceProvider.getLocalAndOutBoundAuthenticationConfig()
                    .getAuthenticationSteps()[0].getFederatedIdentityProviders().length > 0
                    && serviceProvider.getLocalAndOutBoundAuthenticationConfig()
                    .getAuthenticationSteps()[0].getFederatedIdentityProviders()[0] != null) {
                return serviceProvider.getLocalAndOutBoundAuthenticationConfig()
                        .getAuthenticationSteps()[0].getFederatedIdentityProviders()[0]
                        .getIdentityProviderName();
            }
        }

        return null;
    }

    public void setStepZeroAuthenticatorName(String type, String name) {
        if (AUTH_TYPE_LOCAL.equalsIgnoreCase(type)) {
            LocalAuthenticatorConfig localAuthenticator = new LocalAuthenticatorConfig();
            localAuthenticator.setName(name);
            AuthenticationStep authStep = new AuthenticationStep();
            authStep.setLocalAuthenticatorConfigs(new LocalAuthenticatorConfig[]{localAuthenticator});

        }

    }

    /**
     * @return
     */
    public IdentityProvider[] getFederatedIdentityProviders() {
        return federatedIdentityProviders;
    }

    /**
     * @param federatedIdentityProviders
     */
    public void setFederatedIdentityProviders(IdentityProvider[] federatedIdentityProviders) {
        this.federatedIdentityProviders = federatedIdentityProviders;
        if (federatedIdentityProviders != null) {
            federatedIdentityProvidersMap.clear();
            for (IdentityProvider identityProvider : federatedIdentityProviders) {
                federatedIdentityProvidersMap.put(identityProvider.getIdentityProviderName(), identityProvider);
            }
        }
    }

    public List<IdentityProvider> getEnabledFederatedIdentityProviders() {
        if (enabledFederatedIdentityProviders != null) {
            return enabledFederatedIdentityProviders;
        }
        if (federatedIdentityProviders != null && federatedIdentityProviders.length > 0) {
            enabledFederatedIdentityProviders = new ArrayList<IdentityProvider>();
            for (IdentityProvider idp : federatedIdentityProviders) {
                if (idp.getEnable()) {
                    FederatedAuthenticatorConfig[] fedAuthConfigs = idp.getFederatedAuthenticatorConfigs();
                    if (fedAuthConfigs != null && fedAuthConfigs.length > 0) {
                        for (FederatedAuthenticatorConfig config : fedAuthConfigs) {
                            if (config.getEnabled()) {
                                enabledFederatedIdentityProviders.add(idp);
                                break;
                            }
                        }
                    }
                }
            }
        }
        return enabledFederatedIdentityProviders;
    }

    /**
     * @return
     */
    public LocalAuthenticatorConfig[] getLocalAuthenticatorConfigs() {
        return localAuthenticatorConfigs;
    }

    /**
     * @param localAuthenticatorConfigs
     */
    public void setLocalAuthenticatorConfigs(LocalAuthenticatorConfig[] localAuthenticatorConfigs) {
        this.localAuthenticatorConfigs = localAuthenticatorConfigs;
    }

    /**
     * @return
     */
    public RequestPathAuthenticatorConfig[] getRequestPathAuthenticators() {
        return requestPathAuthenticators;
    }

    /**
     * @param requestPathAuthenticators
     */
    public void setRequestPathAuthenticators(
            RequestPathAuthenticatorConfig[] requestPathAuthenticators) {
        this.requestPathAuthenticators = requestPathAuthenticators;
    }

    /**
     * @return
     */
    public List<String> getPermissions() {

        List<String> permList = new ArrayList<String>();

        if (serviceProvider != null && serviceProvider.getPermissionAndRoleConfig() != null) {
            PermissionsAndRoleConfig permissionAndRoleConfig = serviceProvider
                    .getPermissionAndRoleConfig();
            if (permissionAndRoleConfig != null) {
                ApplicationPermission[] permissions = permissionAndRoleConfig.getPermissions();
                if (permissions != null && permissions.length > 0) {
                    for (int i = 0; i < permissions.length; i++) {
                        if (permissions[i] != null) {
                            permList.add(permissions[i].getValue());
                        }
                    }
                }
            }

        }

        return permList;
    }

    /**
     * @param permissions
     */
    public void setPermissions(String[] permissions) {
        ApplicationPermission[] applicationPermission = new ApplicationPermission[permissions.length];
        for (int i = 0; i < permissions.length; i++) {
            ApplicationPermission appPermission = new ApplicationPermission();
            appPermission.setValue(permissions[i]);
        }
        serviceProvider.getPermissionAndRoleConfig().setPermissions(applicationPermission);
    }

    /**
     * @return
     */
    public String getRoleClaimUri() {
        if (serviceProvider.getClaimConfig() != null) {
            return serviceProvider.getClaimConfig().getRoleClaimURI();
        } else {
            return null;
        }
    }

    /**
     * @param roleClaimUri
     */
    public void setRoleClaimUri(String roleClaimUri) {

        if (roleClaimUri != null) {
            if (serviceProvider.getClaimConfig() != null) {
                serviceProvider.getClaimConfig().setRoleClaimURI(roleClaimUri);
            } else {
                ClaimConfig claimConfig = new ClaimConfig();
                claimConfig.setRoleClaimURI(roleClaimUri);
                serviceProvider.setClaimConfig(claimConfig);
            }
        }
    }

    /**
     * @return
     */
    public String getUserClaimUri() {
        if (serviceProvider.getClaimConfig() != null) {
            return serviceProvider.getClaimConfig().getUserClaimURI();
        } else {
            return null;
        }
    }

    /**
     * @param userClaimUri
     */
    public void setUserClaimUri(String userClaimUri) {

        if (userClaimUri != null) {
            if (serviceProvider.getClaimConfig() != null) {
                serviceProvider.getClaimConfig().setUserClaimURI(userClaimUri);
            } else {
                ClaimConfig claimConfig = new ClaimConfig();
                claimConfig.setUserClaimURI(userClaimUri);
                serviceProvider.setClaimConfig(claimConfig);
            }
        }
    }

    /**
     * @return
     */
    public Map<String, String> getRoleMapping() {

        if (serviceProvider.getPermissionAndRoleConfig() == null) {
            return new HashMap<String, String>();
        }

        RoleMapping[] roleMapping = serviceProvider.getPermissionAndRoleConfig().getRoleMappings();

        if (roleMap != null && roleMapping != null && (roleMapping.length == roleMap.size())) {
            return roleMap;
        }

        roleMap = new HashMap<String, String>();

        if (roleMapping != null) {
            for (int i = 0; i < roleMapping.length; i++) {
                roleMap.put(roleMapping[i].getLocalRole().getLocalRoleName(),
                        roleMapping[i].getRemoteRole());
            }
        }

        return roleMap;
    }

    /**
     * @param spRole
     * @param localRole
     */
    public void addRoleMapping(String spRole, String localRole) {
        roleMap.put(localRole, spRole);
    }

    /**
     * @return
     */
    public Map<String, String> getClaimMapping() {

        if (serviceProvider.getClaimConfig() == null) {
            return new HashMap<String, String>();
        }

        ClaimMapping[] claimMapping = serviceProvider.getClaimConfig().getClaimMappings();

        if (claimMap != null && claimMapping != null && (claimMapping.length == claimMap.size())) {
            return claimMap;
        }

        claimMap = new HashMap<String, String>();

        if (claimMapping != null) {
            for (int i = 0; i < claimMapping.length; i++) {
                if (claimMapping[i] != null && claimMapping[i].getRemoteClaim() != null
                        && claimMapping[i].getLocalClaim() != null) {
                    claimMap.put(claimMapping[i].getLocalClaim().getClaimUri(), claimMapping[i]
                            .getRemoteClaim().getClaimUri());
                    if (claimMapping[i].getRequested()) {
                        requestedClaims.put(claimMapping[i].getRemoteClaim().getClaimUri(), "true");
                    } else {
                        requestedClaims
                                .put(claimMapping[i].getRemoteClaim().getClaimUri(), "false");
                    }

                    if (claimMapping[i].getMandatory()) {
                        mandatoryClaims.put(claimMapping[i].getRemoteClaim().getClaimUri(), "true");
                    } else {
                        mandatoryClaims
                                .put(claimMapping[i].getRemoteClaim().getClaimUri(), "false");
                    }
                }
            }
        }

        return claimMap;
    }

    /**
     * Is Local Claims Selected
     *
     * @return
     */
    public boolean isLocalClaimsSelected() {
        if (serviceProvider.getClaimConfig() != null) {
            return serviceProvider.getClaimConfig().getLocalClaimDialect();
        }
        return true;
    }

    public boolean isAlwaysSendMappedLocalSubjectId() {
        if (serviceProvider.getClaimConfig() != null) {
            return serviceProvider.getClaimConfig().getAlwaysSendMappedLocalSubjectId();
        }
        return false;
    }

    public boolean isAlwaysSendBackAuthenticatedListOfIdPs() {
        if (serviceProvider.getLocalAndOutBoundAuthenticationConfig() != null) {
            return serviceProvider.getLocalAndOutBoundAuthenticationConfig().
                    getAlwaysSendBackAuthenticatedListOfIdPs();
        }
        return false;
    }

    public boolean isUseTenantDomainInLocalSubjectIdentifier() {
        if (serviceProvider.getLocalAndOutBoundAuthenticationConfig() != null) {
            return serviceProvider.getLocalAndOutBoundAuthenticationConfig().
                    getUseTenantDomainInLocalSubjectIdentifier();
        }
        return false;
    }

    public boolean isUseUserstoreDomainInLocalSubjectIdentifier() {
        if (serviceProvider.getLocalAndOutBoundAuthenticationConfig() != null) {
            return serviceProvider.getLocalAndOutBoundAuthenticationConfig().
                    getUseUserstoreDomainInLocalSubjectIdentifier();
        }
        return false;
    }

    /**
     * Returns whether consent needs to be skipped for this service provider.
     *
     * @return true of consent is skipped, false otherwise.
     */
    public boolean isSkipConsent() {

        if (serviceProvider.getLocalAndOutBoundAuthenticationConfig() != null) {
            return serviceProvider.getLocalAndOutBoundAuthenticationConfig().getSkipConsent();
        }
        return false;
    }

    /**
     * Returns whether logout consent needs to be skipped for this service provider.
     *
     * @return true of logout consent is skipped, false otherwise.
     */
    public boolean isSkipLogoutConsent() {

        if (serviceProvider.getLocalAndOutBoundAuthenticationConfig() != null) {
            return serviceProvider.getLocalAndOutBoundAuthenticationConfig().getSkipLogoutConsent();
        }
        return false;
    }

    public boolean isEnableAuthorization() {

        return serviceProvider.getLocalAndOutBoundAuthenticationConfig() != null &&
               serviceProvider.getLocalAndOutBoundAuthenticationConfig().getEnableAuthorization();

    }

    public String getSubjectClaimUri() {
        if (serviceProvider.getLocalAndOutBoundAuthenticationConfig() != null) {
            return serviceProvider.getLocalAndOutBoundAuthenticationConfig().getSubjectClaimUri();
        }
        return null;
    }

    public String getAttributeConsumingServiceIndex() {
        if (attrConsumServiceIndex != null) {
            return attrConsumServiceIndex;
        }

        InboundAuthenticationRequestConfig[] authRequests = serviceProvider
                .getInboundAuthenticationConfig().getInboundAuthenticationRequestConfigs();
        if (authRequests != null) {
            for (InboundAuthenticationRequestConfig request : authRequests) {
                if ("samlsso".equalsIgnoreCase(request.getInboundAuthType())) {
                    if (request.getProperties() != null) {
                        for (Property property : request.getProperties()) {
                            if ("attrConsumServiceIndex".equalsIgnoreCase(property.getName())) {
                                attrConsumServiceIndex = property.getValue();
                                return attrConsumServiceIndex;
                            }
                        }
                    }
                    return null;
                }
            }
        }
        return null;
    }

    public void setAttributeConsumingServiceIndex(String attrConsumServiceIndex) {
        this.attrConsumServiceIndex = attrConsumServiceIndex;
    }

    public String getOauthConsumerSecret() {
        if (oauthConsumerSecret != null) {
            return oauthConsumerSecret;
        }

        InboundAuthenticationRequestConfig[] authRequests = serviceProvider
                .getInboundAuthenticationConfig().getInboundAuthenticationRequestConfigs();
        if (authRequests != null) {
            for (InboundAuthenticationRequestConfig request : authRequests) {
                if ("oauth2".equalsIgnoreCase(request.getInboundAuthType())) {
                    if (request.getProperties() != null) {
                        for (Property property : request.getProperties()) {
                            if ("oauthConsumerSecret".equalsIgnoreCase(property.getName())) {
                                oauthConsumerSecret = property.getValue();
                                return oauthConsumerSecret;
                            }
                        }
                    }
                    return null;
                }
            }
        }
        return null;
    }

    public void setOauthConsumerSecret(String oauthConsumerSecret) {
        this.oauthConsumerSecret = oauthConsumerSecret;
    }

    /**
     * @return
     */
    public String getSAMLIssuer() {

        if (samlIssuer != null) {
            return samlIssuer;
        }

        InboundAuthenticationRequestConfig[] authRequest = serviceProvider
                .getInboundAuthenticationConfig().getInboundAuthenticationRequestConfigs();

        if (authRequest != null) {
            for (int i = 0; i < authRequest.length; i++) {
                if ("samlsso".equalsIgnoreCase(authRequest[i].getInboundAuthType())) {
                    samlIssuer = authRequest[i].getInboundAuthKey();
                    break;
                }
            }
        }

        return samlIssuer;
    }

    public String getKerberosServiceName() {
        if (kerberosServiceName != null) {
            return kerberosServiceName;
        }
        InboundAuthenticationRequestConfig[] authRequest = serviceProvider
                .getInboundAuthenticationConfig().getInboundAuthenticationRequestConfigs();

        if (authRequest != null) {
            for (int i = 0; i < authRequest.length; i++) {
                if ("kerberos".equalsIgnoreCase(authRequest[i].getInboundAuthType())) {
                    kerberosServiceName = authRequest[i].getInboundAuthKey();
                    break;
                }
            }
        }
        return kerberosServiceName;
    }

    public void setKerberosServiceName(String kerberosServiceName) {
        this.kerberosServiceName = kerberosServiceName;
    }

    /**
     * @param issuerName
     */
    public void setSAMLIssuer(String issuerName) {
        this.samlIssuer = issuerName;
    }

    public void deleteSAMLIssuer() {
        this.samlIssuer = null;
        this.attrConsumServiceIndex = null;
        InboundAuthenticationRequestConfig[] authRequest = serviceProvider
                .getInboundAuthenticationConfig().getInboundAuthenticationRequestConfigs();

        if (authRequest != null && authRequest.length > 0) {
            List<InboundAuthenticationRequestConfig> tempAuthRequest =
                    new ArrayList<InboundAuthenticationRequestConfig>();
            for (int i = 0; i < authRequest.length; i++) {
                if ("samlsso".equalsIgnoreCase(authRequest[i].getInboundAuthType())) {
                    continue;
                }
                tempAuthRequest.add(authRequest[i]);
            }
            if (CollectionUtils.isNotEmpty(tempAuthRequest)) {
                serviceProvider
                        .getInboundAuthenticationConfig()
                        .setInboundAuthenticationRequestConfigs(
                                tempAuthRequest
                                        .toArray(new InboundAuthenticationRequestConfig[tempAuthRequest
                                                .size()]));
            } else {
                serviceProvider.getInboundAuthenticationConfig()
                        .setInboundAuthenticationRequestConfigs(null);
            }
        }
    }

    /**
     * @param oauthAppName
     */
    public void setOIDCAppName(String oauthAppName) {
        this.oauthAppName = oauthAppName;
    }

    public void deleteOauthApp() {
        this.oauthAppName = null;
        this.oauthConsumerSecret = null;
        InboundAuthenticationRequestConfig[] authRequest = serviceProvider
                .getInboundAuthenticationConfig().getInboundAuthenticationRequestConfigs();

        if (authRequest != null && authRequest.length > 0) {
            List<InboundAuthenticationRequestConfig> tempAuthRequest =
                    new ArrayList<InboundAuthenticationRequestConfig>();
            for (int i = 0; i < authRequest.length; i++) {
                if ("oauth2".equalsIgnoreCase(authRequest[i].getInboundAuthType())) {
                    continue;
                }
                tempAuthRequest.add(authRequest[i]);
            }
            if (CollectionUtils.isNotEmpty(tempAuthRequest)) {
                serviceProvider
                        .getInboundAuthenticationConfig()
                        .setInboundAuthenticationRequestConfigs(
                                tempAuthRequest
                                        .toArray(new InboundAuthenticationRequestConfig[tempAuthRequest
                                                .size()]));
            } else {
                serviceProvider.getInboundAuthenticationConfig()
                        .setInboundAuthenticationRequestConfigs(null);
            }
        }
    }

    public void deleteKerberosApp() {
        this.kerberosServiceName = null;
        InboundAuthenticationRequestConfig[] authRequest = serviceProvider
                .getInboundAuthenticationConfig().getInboundAuthenticationRequestConfigs();

        if (authRequest != null && authRequest.length > 0) {
            List<InboundAuthenticationRequestConfig> tempAuthRequest =
                    new ArrayList<InboundAuthenticationRequestConfig>();
            for (int i = 0; i < authRequest.length; i++) {
                if ("kerberos".equalsIgnoreCase(authRequest[i].getInboundAuthType())) {
                    continue;
                }
                tempAuthRequest.add(authRequest[i]);
            }
            if (CollectionUtils.isNotEmpty(tempAuthRequest)) {
                serviceProvider
                        .getInboundAuthenticationConfig()
                        .setInboundAuthenticationRequestConfigs(
                                tempAuthRequest
                                        .toArray(new InboundAuthenticationRequestConfig[tempAuthRequest
                                                .size()]));
            } else {
                serviceProvider.getInboundAuthenticationConfig()
                        .setInboundAuthenticationRequestConfigs(null);
            }
        }
    }

    public void deleteWstrustEp() {
        this.wstrustEp = null;
        InboundAuthenticationRequestConfig[] authRequest = serviceProvider
                .getInboundAuthenticationConfig().getInboundAuthenticationRequestConfigs();

        if (authRequest != null && authRequest.length > 0) {
            List<InboundAuthenticationRequestConfig> tempAuthRequest =
                    new ArrayList<InboundAuthenticationRequestConfig>();
            for (int i = 0; i < authRequest.length; i++) {
                if ("wstrust".equalsIgnoreCase(authRequest[i].getInboundAuthType())) {
                    continue;
                }
                tempAuthRequest.add(authRequest[i]);
            }
            if (CollectionUtils.isNotEmpty(tempAuthRequest)) {
                serviceProvider
                        .getInboundAuthenticationConfig()
                        .setInboundAuthenticationRequestConfigs(
                                tempAuthRequest
                                        .toArray(new InboundAuthenticationRequestConfig[tempAuthRequest
                                                .size()]));
            } else {
                serviceProvider.getInboundAuthenticationConfig()
                        .setInboundAuthenticationRequestConfigs(null);
            }
        }
    }

    /**
     * @return
     */
    public String getOIDCClientId() {

        if (oauthAppName != null) {
            return oauthAppName;
        }

        InboundAuthenticationRequestConfig[] authRequest = serviceProvider
                .getInboundAuthenticationConfig().getInboundAuthenticationRequestConfigs();

        if (authRequest != null) {
            for (int i = 0; i < authRequest.length; i++) {
                if ("oauth2".equalsIgnoreCase(authRequest[i].getInboundAuthType())) {
                    oauthAppName = authRequest[i].getInboundAuthKey();
                    break;
                }
            }
        }

        return oauthAppName;
    }

    /**
     * @return
     */
    public String getOpenIDRealm() {

        if (openid != null) {
            return openid;
        }

        InboundAuthenticationRequestConfig[] authRequest = serviceProvider
                .getInboundAuthenticationConfig().getInboundAuthenticationRequestConfigs();
        if (authRequest != null) {
            for (int i = 0; i < authRequest.length; i++) {
                if ("openid".equalsIgnoreCase(authRequest[i].getInboundAuthType())) {
                    openid = authRequest[i].getInboundAuthKey();
                    break;
                }
            }
        }
        return openid;
    }

    /**
     * @return
     */
    public String getWstrustSP() {
        List<String> wsTrustEps = getAllWsTrustSPs();
        if (CollectionUtils.isNotEmpty(wsTrustEps)) {
            return getAllWsTrustSPs().get(0);
        } else {
            return null;
        }
    }

    /**
     * @return
     */
    public List<String> getAllWsTrustSPs() {
        if (CollectionUtils.isNotEmpty(wstrustEp)) {
            return wstrustEp;
        } else {
            wstrustEp = new ArrayList<>(0);
        }

        InboundAuthenticationRequestConfig[] authRequests = serviceProvider
                .getInboundAuthenticationConfig().getInboundAuthenticationRequestConfigs();
        if (authRequests != null) {
            Arrays.stream(authRequests).filter(authRequest ->
                    (authRequest.getInboundAuthType() != null && !authRequest.getInboundAuthType().isEmpty()
                            && "wstrust".equalsIgnoreCase(authRequest.getInboundAuthType())))
                    .forEach(authRequest -> wstrustEp.add(authRequest.getInboundAuthKey()));
        }

        return wstrustEp;
    }

    /**
     * @return
     */
    public String getPassiveSTSRealm() {

        if (passivests != null) {
            return passivests;
        }

        InboundAuthenticationRequestConfig[] authRequest = serviceProvider
                .getInboundAuthenticationConfig().getInboundAuthenticationRequestConfigs();

        if (authRequest != null) {
            for (int i = 0; i < authRequest.length; i++) {
                if ("passivests".equalsIgnoreCase(authRequest[i].getInboundAuthType())) {
                    passivests = authRequest[i].getInboundAuthKey();
                    break;
                }
            }
        }

        return passivests;
    }

    /**
     * @return
     */
    public String getPassiveSTSWReply() {

        if (passiveSTSWReply != null) {
            return passiveSTSWReply;
        }

        InboundAuthenticationRequestConfig[] authRequest = serviceProvider
                .getInboundAuthenticationConfig().getInboundAuthenticationRequestConfigs();

        if (authRequest != null) {
            for (int i = 0; i < authRequest.length; i++) {
                if ("passivests".equalsIgnoreCase(authRequest[i].getInboundAuthType())) {

                    // get wreply url from properties
                    Property[] properties = authRequest[i].getProperties();
                    if (properties != null) {
                        for (int j = 0; j < properties.length; j++) {
                            if ("passiveSTSWReply".equalsIgnoreCase(properties[j].getName())) {
                                passiveSTSWReply = properties[j].getValue();
                                break;
                            }
                        }
                    }

                    break;
                }
            }
        }

        return passiveSTSWReply;
    }

    /**
     * @return
     */
    public String[] getClaimUris() {
        return claimUris;
    }

    public void setClaimUris(String[] claimUris) {
        this.claimUris = claimUris;
    }

    /**
     * Get service provider claim dialects.
     *
     * @return claim dialects of service provider
     */
    public List<String> getSPClaimDialects() {

        return serviceProvider.getClaimConfig() != null && !ArrayUtils.isEmpty(serviceProvider.getClaimConfig()
                .getSpClaimDialects()) ? Arrays.asList(serviceProvider.getClaimConfig().getSpClaimDialects()) :
                new ArrayList<>();
    }

    /**
     * Set claim dialects Uris.
     *
     * @param claimDialectUris list of claim dialect Uris
     */
    public void setClaimDialectUris(List<String> claimDialectUris) {

        this.claimDialectUris = claimDialectUris;
    }

    /**
     * Get claim dialects Uris.
     *
     * @return list of claim dialect Uris
     */
    public List<String> getClaimDialectUris() {

        return claimDialectUris;
    }


    private boolean isCustomInboundAuthType(String authType) {
        return !standardInboundAuthTypes.contains(authType);
    }

    /**
     * Get all custom authenticators.
     *
     * @return Custom authenticators
     */
    public List<InboundAuthenticationRequestConfig> getInboundAuthenticators() {

        if (CollectionUtils.isNotEmpty(Collections.singleton(customInboundAuthenticatorConfigs))) {
            if (CollectionUtils.isNotEmpty(inboundAuthenticationRequestConfigs)) {
                return inboundAuthenticationRequestConfigs;
            }
            InboundAuthenticationRequestConfig[] authRequests = serviceProvider
                    .getInboundAuthenticationConfig()
                    .getInboundAuthenticationRequestConfigs();

            inboundAuthenticationRequestConfigs = new ArrayList<>();
            if (authRequests != null) {
                for (InboundAuthenticationRequestConfig request : authRequests) {
                    if (isCustomInboundAuthType(request.getInboundAuthType()) && customInboundAuthenticatorConfigs
                            .containsKey(request.getInboundAuthType() + ":" + request.getInboundConfigType())) {
                        customInboundAuthenticatorConfigs.remove(
                                request.getInboundAuthType() + ":" + request.getInboundConfigType());
                        inboundAuthenticationRequestConfigs.add(request);
                    }
                }
            }
            inboundAuthenticationRequestConfigs.addAll(customInboundAuthenticatorConfigs.values());
        }
        return inboundAuthenticationRequestConfigs;
    }


    /**
     * @param request
     */
    public void updateOutBoundAuthenticationConfig(HttpServletRequest request) {

        String[] authSteps = request.getParameterValues("auth_step");

        if (authSteps != null && authSteps.length > 0) {
            List<AuthenticationStep> authStepList = new ArrayList<AuthenticationStep>();

            for (String authstep : authSteps) {
                AuthenticationStep authStep = new AuthenticationStep();
                authStep.setStepOrder(Integer.parseInt(authstep));

                boolean isSubjectStep = request.getParameter("subject_step_" + authstep) != null
                        && "on".equals(request.getParameter("subject_step_" + authstep)) ? true
                        : false;
                authStep.setSubjectStep(isSubjectStep);

                boolean isAttributeStep = request.getParameter("attribute_step_" + authstep) != null
                        && "on".equals(request.getParameter("attribute_step_" + authstep)) ? true
                        : false;
                authStep.setAttributeStep(isAttributeStep);

                String[] localAuthenticatorNames = request.getParameterValues("step_" + authstep
                        + "_local_auth");

                if (localAuthenticatorNames != null && localAuthenticatorNames.length > 0) {
                    List<LocalAuthenticatorConfig> localAuthList = new ArrayList<LocalAuthenticatorConfig>();
                    for (String name : localAuthenticatorNames) {
                        if (name != null) {
                            LocalAuthenticatorConfig localAuth = new LocalAuthenticatorConfig();
                            localAuth.setName(name);
                            if (localAuthenticatorConfigs != null) {
                                for (LocalAuthenticatorConfig config : localAuthenticatorConfigs) {
                                    if (config.getName().equals(name)) {
                                        localAuth.setDisplayName(config.getDisplayName());
                                        break;
                                    }
                                }
                            }
                            localAuthList.add(localAuth);
                        }
                    }

                    if (localAuthList != null && !localAuthList.isEmpty()) {
                        authStep.setLocalAuthenticatorConfigs(localAuthList
                                .toArray(new LocalAuthenticatorConfig[localAuthList.size()]));
                    }

                }

                String[] federatedIdpNames = request.getParameterValues("step_" + authstep
                        + "_fed_auth");

                if (federatedIdpNames != null && federatedIdpNames.length > 0) {
                    List<IdentityProvider> fedIdpList = new ArrayList<>();
                    for (String name : federatedIdpNames) {
                        if (StringUtils.isNotBlank(name)) {
                            IdentityProvider idp = new IdentityProvider();
                            idp.setIdentityProviderName(name);
                            IdentityProvider referringIdP = federatedIdentityProvidersMap.get(name);
                            String authenticatorName = request.getParameter("step_" + authstep + "_idp_" + name +
                                    "_fed_authenticator");
                            if (StringUtils.isNotBlank(authenticatorName)) {
                                String authenticatorDisplayName = null;

                                for (FederatedAuthenticatorConfig config : referringIdP
                                        .getFederatedAuthenticatorConfigs()) {
                                    if (authenticatorName.equals(config.getName())) {
                                        authenticatorDisplayName = config.getDisplayName();
                                        break;
                                    }
                                }

                                FederatedAuthenticatorConfig authenticator = new FederatedAuthenticatorConfig();
                                authenticator.setName(authenticatorName);
                                authenticator.setDisplayName(authenticatorDisplayName);
                                idp.setDefaultAuthenticatorConfig(authenticator);
                                idp.setFederatedAuthenticatorConfigs(new FederatedAuthenticatorConfig[]{authenticator});
                                fedIdpList.add(idp);
                            }
                        }
                    }

                    if (fedIdpList != null && !fedIdpList.isEmpty()) {
                        authStep.setFederatedIdentityProviders(fedIdpList
                                .toArray(new IdentityProvider[fedIdpList.size()]));
                    }
                }

                if ((authStep.getFederatedIdentityProviders() != null && authStep
                        .getFederatedIdentityProviders().length > 0)
                        || (authStep.getLocalAuthenticatorConfigs() != null && authStep
                        .getLocalAuthenticatorConfigs().length > 0)) {
                    authStepList.add(authStep);
                }

            }

            if (serviceProvider.getLocalAndOutBoundAuthenticationConfig() == null) {
                serviceProvider
                        .setLocalAndOutBoundAuthenticationConfig(new LocalAndOutboundAuthenticationConfig());
            }
            if (CollectionUtils.isNotEmpty(authStepList)) {

                LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig =
                        serviceProvider.getLocalAndOutBoundAuthenticationConfig();
                localAndOutboundAuthenticationConfig.setAuthenticationSteps(
                        authStepList.toArray(new AuthenticationStep[authStepList.size()]));
            }
        }
    }

    /**
     * @param request
     */
    public void conditionalAuthentication(HttpServletRequest request) {

        AuthenticationScriptConfig authenticationScriptConfig = new AuthenticationScriptConfig();
        LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig =
                serviceProvider.getLocalAndOutBoundAuthenticationConfig();
        String flawByScript = request.getParameter("scriptTextArea");
        // Decode the auth script.
        flawByScript = new String(Base64.getDecoder().decode(flawByScript), StandardCharsets.UTF_8);

        if (StringUtils.isBlank(flawByScript)) {
            authenticationScriptConfig.setEnabled(false);
        } else {
            if ("true".equalsIgnoreCase(request.getParameter("enableScript"))) {
                authenticationScriptConfig.setEnabled(true);
            } else {
                authenticationScriptConfig.setEnabled(false);
            }
        }

        authenticationScriptConfig.setContent(flawByScript);
        localAndOutboundAuthenticationConfig.setAuthenticationScriptConfig(authenticationScriptConfig);
    }

    /**
     * @param request
     */
    public void update(HttpServletRequest request) {

        // update basic info.
        serviceProvider.setApplicationName(request.getParameter("spName"));
        serviceProvider.setDescription(request.getParameter("sp-description"));
        serviceProvider.setCertificateContent(request.getParameter("sp-certificate"));

        String jwks = request.getParameter("jwksUri");
        serviceProvider.setJwksUri(jwks);

        if (Boolean.parseBoolean(request.getParameter("deletePublicCert"))) {
            serviceProvider.setCertificateContent("");
        }
        String isSasApp = request.getParameter("isSaasApp");
        serviceProvider.setSaasApp((isSasApp != null && "on".equals(isSasApp)) ? true : false);

        String isDiscoverableApp = request.getParameter("isDiscoverableApp");
        serviceProvider.setDiscoverable("on".equals(isDiscoverableApp));

        String accessUrl = request.getParameter("accessURL");
        serviceProvider.setAccessUrl(accessUrl);

        String imageUrl = request.getParameter("imageURL");
        serviceProvider.setImageUrl(imageUrl);

        String logoutReturnUrl = request.getParameter(LOGOUT_RETURN_URL);
        if (StringUtils.isNotBlank(logoutReturnUrl)) {
            boolean logoutReturnUrlDefined = false;
            if (serviceProvider.getSpProperties() != null) {
                for (ServiceProviderProperty property : serviceProvider.getSpProperties()) {
                    if (property.getName() != null && LOGOUT_RETURN_URL.equals(property.getName())) {
                        property.setValue(logoutReturnUrl);
                        logoutReturnUrlDefined = true;
                        break;
                    }
                }
            }
            if (!logoutReturnUrlDefined) {
                ServiceProviderProperty property = new ServiceProviderProperty();
                property.setName(LOGOUT_RETURN_URL);
                property.setDisplayName("Logout Return URL");
                property.setValue(logoutReturnUrl);
                serviceProvider.addSpProperties(property);
            }
        }

        if (serviceProvider.getLocalAndOutBoundAuthenticationConfig() == null) {
            // create fresh one.
            serviceProvider
                    .setLocalAndOutBoundAuthenticationConfig(new LocalAndOutboundAuthenticationConfig());
        }

        // authentication type : default, local, federated or advanced.
        serviceProvider.getLocalAndOutBoundAuthenticationConfig()
                .setAuthenticationType(request.getParameter("auth_type"));

        // update inbound provisioning data.
        String provisioningUserStore = request.getParameter("scim-inbound-userstore");
        InboundProvisioningConfig inBoundProConfig = new InboundProvisioningConfig();
        inBoundProConfig.setProvisioningUserStore(provisioningUserStore);
        inBoundProConfig.setDumbMode(Boolean.parseBoolean(request.getParameter(DUMB)));
        serviceProvider.setInboundProvisioningConfig(inBoundProConfig);

        // update outbound provisioning data.
        String[] provisioningProviders = request.getParameterValues("provisioning_idp");

        if (provisioningProviders != null && provisioningProviders.length > 0) {

            List<IdentityProvider> provisioningIdps = new ArrayList<IdentityProvider>();

            for (String proProvider : provisioningProviders) {
                String connector = request.getParameter("provisioning_con_idp_" + proProvider);
                String jitEnabled = request.getParameter("provisioning_jit_" + proProvider);
                String blocking = request.getParameter("blocking_prov_" + proProvider);
                String ruleEnabled = request.getParameter("rules_enabled_" + proProvider);
                if (connector != null) {
                    IdentityProvider proIdp = new IdentityProvider();
                    proIdp.setIdentityProviderName(proProvider);

                    JustInTimeProvisioningConfig jitpro = new JustInTimeProvisioningConfig();

                    if ("on".equals(jitEnabled)) {
                        jitpro.setProvisioningEnabled(true);
                    }

                    proIdp.setJustInTimeProvisioningConfig(jitpro);

                    ProvisioningConnectorConfig proCon = new ProvisioningConnectorConfig();
                    if ("on".equals(ruleEnabled)) {
                        proCon.setRulesEnabled(true);
                    } else {
                        proCon.setRulesEnabled(false);
                    }
                    if ("on".equals(blocking)) {
                        proCon.setBlocking(true);
                    } else {
                        proCon.setBlocking(false);
                    }
                    proCon.setName(connector);
                    proIdp.setDefaultProvisioningConnectorConfig(proCon);
                    provisioningIdps.add(proIdp);
                }
            }

            if (CollectionUtils.isNotEmpty(provisioningIdps)) {
                OutboundProvisioningConfig outboundProConfig = new OutboundProvisioningConfig();
                outboundProConfig.setProvisioningIdentityProviders(provisioningIdps
                        .toArray(new IdentityProvider[provisioningIdps.size()]));
                serviceProvider.setOutboundProvisioningConfig(outboundProConfig);
            }
        } else {
            serviceProvider.setOutboundProvisioningConfig(new OutboundProvisioningConfig());
        }

        // get all request-path authenticators.
        String[] requestPathAuthenticators = request.getParameterValues("req_path_auth");

        if (requestPathAuthenticators != null && requestPathAuthenticators.length > 0) {
            List<RequestPathAuthenticatorConfig> reqAuthList = new ArrayList<RequestPathAuthenticatorConfig>();
            for (String name : requestPathAuthenticators) {
                if (name != null) {
                    RequestPathAuthenticatorConfig reqAuth = new RequestPathAuthenticatorConfig();
                    reqAuth.setName(name);
                    reqAuth.setDisplayName(request.getParameter("req_path_auth_" + name));
                    reqAuthList.add(reqAuth);
                }
            }

            if (CollectionUtils.isNotEmpty(reqAuthList)) {
                serviceProvider.setRequestPathAuthenticatorConfigs(reqAuthList
                        .toArray(new RequestPathAuthenticatorConfig[reqAuthList.size()]));
            } else {
                serviceProvider.setRequestPathAuthenticatorConfigs(null);
            }
        } else {
            serviceProvider.setRequestPathAuthenticatorConfigs(null);
        }

        List<InboundAuthenticationRequestConfig> authRequestList = new ArrayList<InboundAuthenticationRequestConfig>();

        // update in-bound authentication configuration.

        if (samlIssuer != null) {
            InboundAuthenticationRequestConfig samlAuthenticationRequest = new InboundAuthenticationRequestConfig();
            samlAuthenticationRequest.setInboundAuthKey(samlIssuer);
            samlAuthenticationRequest.setInboundAuthType("samlsso");
            if (attrConsumServiceIndex != null && !attrConsumServiceIndex.isEmpty()) {
                Property property = new Property();
                property.setName("attrConsumServiceIndex");
                property.setValue(attrConsumServiceIndex);
                Property[] properties = {property};
                samlAuthenticationRequest.setProperties(properties);
            }
            authRequestList.add(samlAuthenticationRequest);
        }

        if (kerberosServiceName != null) {
            InboundAuthenticationRequestConfig kerberosAuthenticationRequest = new InboundAuthenticationRequestConfig();
            kerberosAuthenticationRequest.setInboundAuthKey(kerberosServiceName);
            kerberosAuthenticationRequest.setInboundAuthType("kerberos");
            authRequestList.add(kerberosAuthenticationRequest);
        }

        if (oauthAppName != null) {
            InboundAuthenticationRequestConfig opicAuthenticationRequest = new InboundAuthenticationRequestConfig();
            opicAuthenticationRequest.setInboundAuthKey(oauthAppName);
            opicAuthenticationRequest.setInboundAuthType("oauth2");
            if (oauthConsumerSecret != null && !oauthConsumerSecret.isEmpty()) {
                Property property = new Property();
                property.setName("oauthConsumerSecret");
                property.setValue(oauthConsumerSecret);
                Property[] properties = {property};
                opicAuthenticationRequest.setProperties(properties);
            }
            authRequestList.add(opicAuthenticationRequest);
        }

        if (CollectionUtils.isNotEmpty(wstrustEp)) {
            wstrustEp.forEach(entry -> {
                InboundAuthenticationRequestConfig opicAuthenticationRequest = new InboundAuthenticationRequestConfig();
                opicAuthenticationRequest.setInboundAuthKey(entry);
                opicAuthenticationRequest.setInboundAuthType("wstrust");
                authRequestList.add(opicAuthenticationRequest);
            });
        }

        String passiveSTSRealm = request.getParameter("passiveSTSRealm");
        String passiveSTSWReply = request.getParameter("passiveSTSWReply");

        if (StringUtils.isNotBlank(passiveSTSRealm)) {
            InboundAuthenticationRequestConfig opicAuthenticationRequest = new InboundAuthenticationRequestConfig();
            opicAuthenticationRequest.setInboundAuthKey(passiveSTSRealm);
            opicAuthenticationRequest.setInboundAuthType("passivests");
            if (passiveSTSWReply != null && !passiveSTSWReply.isEmpty()) {
                Property property = new Property();
                property.setName("passiveSTSWReply");
                property.setValue(passiveSTSWReply);
                Property[] properties = {property};
                opicAuthenticationRequest.setProperties(properties);
            }
            authRequestList.add(opicAuthenticationRequest);
        }

        String openidRealm = request.getParameter("openidRealm");

        if (StringUtils.isNotBlank(openidRealm)) {
            InboundAuthenticationRequestConfig opicAuthenticationRequest = new InboundAuthenticationRequestConfig();
            opicAuthenticationRequest.setInboundAuthKey(openidRealm);
            opicAuthenticationRequest.setInboundAuthType("openid");
            authRequestList.add(opicAuthenticationRequest);
        }

        if (!CollectionUtils.isEmpty(inboundAuthenticationRequestConfigs)) {
            for (InboundAuthenticationRequestConfig customAuthConfig : inboundAuthenticationRequestConfigs) {
                String type = customAuthConfig.getInboundAuthType();
                Property[] properties = customAuthConfig.getProperties();
                if (!ArrayUtils.isEmpty(properties)) {
                    for (Property prop : properties) {
                        String propVal = request.getParameter(
                                "custom_auth_prop_name_" + type + "_" + prop.getName());
                        prop.setValue(propVal);
                    }
                }
                authRequestList.add(customAuthConfig);
            }
        }

        if (serviceProvider.getInboundAuthenticationConfig() == null) {
            serviceProvider.setInboundAuthenticationConfig(new InboundAuthenticationConfig());
        }

        if (CollectionUtils.isNotEmpty(authRequestList)) {
            serviceProvider.getInboundAuthenticationConfig()
                    .setInboundAuthenticationRequestConfigs(
                            authRequestList
                                    .toArray(new InboundAuthenticationRequestConfig[authRequestList
                                            .size()]));
        }

        // update local and out-bound authentication.
        if (AUTH_TYPE_DEFAULT.equalsIgnoreCase(serviceProvider
                .getLocalAndOutBoundAuthenticationConfig().getAuthenticationType())) {
            serviceProvider.getLocalAndOutBoundAuthenticationConfig().setAuthenticationSteps(null);
            serviceProvider.getLocalAndOutBoundAuthenticationConfig().setAuthenticationScriptConfig(null);
        } else if (AUTH_TYPE_LOCAL.equalsIgnoreCase(serviceProvider
                .getLocalAndOutBoundAuthenticationConfig().getAuthenticationType())) {
            AuthenticationStep authStep = new AuthenticationStep();
            LocalAuthenticatorConfig localAuthenticator = new LocalAuthenticatorConfig();
            localAuthenticator.setName(request.getParameter("local_authenticator"));
            if (localAuthenticator.getName() != null && localAuthenticatorConfigs != null) {
                for (LocalAuthenticatorConfig config : localAuthenticatorConfigs) {
                    if (config.getName().equals(localAuthenticator.getName())) {
                        localAuthenticator.setDisplayName(config.getDisplayName());
                        break;
                    }
                }
            }
            authStep.setLocalAuthenticatorConfigs(new LocalAuthenticatorConfig[]{localAuthenticator});
            serviceProvider.getLocalAndOutBoundAuthenticationConfig().setAuthenticationSteps(
                    new AuthenticationStep[]{authStep});
            serviceProvider.getLocalAndOutBoundAuthenticationConfig().setAuthenticationScriptConfig(null);
        } else if (AUTH_TYPE_FEDERATED.equalsIgnoreCase(serviceProvider
                .getLocalAndOutBoundAuthenticationConfig().getAuthenticationType())) {
            AuthenticationStep authStep = new AuthenticationStep();
            IdentityProvider idp = new IdentityProvider();
            idp.setIdentityProviderName(request.getParameter("fed_idp"));
            authStep.setFederatedIdentityProviders(new IdentityProvider[]{idp});
            serviceProvider.getLocalAndOutBoundAuthenticationConfig().setAuthenticationSteps(
                    new AuthenticationStep[]{authStep});
            serviceProvider.getLocalAndOutBoundAuthenticationConfig().setAuthenticationScriptConfig(null);
        } else if (AUTH_TYPE_FLOW.equalsIgnoreCase(serviceProvider
                .getLocalAndOutBoundAuthenticationConfig().getAuthenticationType())) {
            // already updated.
        }

        String alwaysSendAuthListOfIdPs = request.getParameter("always_send_auth_list_of_idps");
        serviceProvider.getLocalAndOutBoundAuthenticationConfig()
                .setAlwaysSendBackAuthenticatedListOfIdPs(alwaysSendAuthListOfIdPs != null &&
                        "on".equals(alwaysSendAuthListOfIdPs) ? true : false);

        String useTenantDomainInLocalSubjectIdentifier = request.getParameter(
                "use_tenant_domain_in_local_subject_identifier");
        serviceProvider.getLocalAndOutBoundAuthenticationConfig()
                .setUseTenantDomainInLocalSubjectIdentifier(useTenantDomainInLocalSubjectIdentifier != null &&
                        "on".equals(useTenantDomainInLocalSubjectIdentifier) ? true : false);

        String useUserstoreDomainInLocalSubjectIdentifier = request.getParameter(
                "use_userstore_domain_in_local_subject_identifier");
        serviceProvider.getLocalAndOutBoundAuthenticationConfig()
                .setUseUserstoreDomainInLocalSubjectIdentifier(useUserstoreDomainInLocalSubjectIdentifier != null &&
                        "on".equals(useUserstoreDomainInLocalSubjectIdentifier) ? true : false);

        String useUserstoreDomainInRoles = request.getParameter("use_userstore_domain_in_roles");
        serviceProvider.getLocalAndOutBoundAuthenticationConfig()
                .setUseUserstoreDomainInRoles(useUserstoreDomainInRoles != null &&
                        "on".equals(useUserstoreDomainInRoles) ? true : false);

        boolean skipConsent = Boolean.parseBoolean(request.getParameter(IdentityConstants.SKIP_CONSENT));
        serviceProvider.getLocalAndOutBoundAuthenticationConfig().setSkipConsent(skipConsent);

        boolean skipLogoutConsent = Boolean.parseBoolean(request.getParameter(IdentityConstants.SKIP_LOGOUT_CONSENT));
        serviceProvider.getLocalAndOutBoundAuthenticationConfig().setSkipLogoutConsent(skipLogoutConsent);

        String enableAuthorization = request.getParameter(
                "enable_authorization");
        serviceProvider.getLocalAndOutBoundAuthenticationConfig()
                .setEnableAuthorization(enableAuthorization != null && "on".equals(enableAuthorization));


        String subjectClaimUri = request.getParameter("subject_claim_uri");
        serviceProvider.getLocalAndOutBoundAuthenticationConfig()
                .setSubjectClaimUri((subjectClaimUri != null && !subjectClaimUri.isEmpty()) ? subjectClaimUri : null);

        // update application permissions.
        PermissionsAndRoleConfig permAndRoleConfig = new PermissionsAndRoleConfig();
        String[] permissions = request.getParameterValues("app_permission");
        List<ApplicationPermission> appPermList = new ArrayList<ApplicationPermission>();

        if (permissions != null && permissions.length > 0) {
            for (String permission : permissions) {
                if (permission != null && !permission.trim().isEmpty()) {
                    ApplicationPermission appPermission = new ApplicationPermission();
                    appPermission.setValue(permission);
                    appPermList.add(appPermission);
                }
            }
        }

        if (CollectionUtils.isNotEmpty(appPermList)) {
            permAndRoleConfig.setPermissions(appPermList
                    .toArray(new ApplicationPermission[appPermList.size()]));
        }

        // update role mapping.
        int roleMappingCount = Integer.parseInt(request.getParameter("number_of_rolemappings"));
        List<RoleMapping> roleMappingList = new ArrayList<RoleMapping>();

        for (int i = 0; i < roleMappingCount; i++) {
            RoleMapping mapping = new RoleMapping();
            LocalRole localRole = new LocalRole();
            localRole.setLocalRoleName(request.getParameter("idpRole_" + i));
            mapping.setLocalRole(localRole);
            mapping.setRemoteRole(request.getParameter("spRole_" + i));
            if (mapping.getLocalRole() != null && mapping.getRemoteRole() != null) {
                roleMappingList.add(mapping);
            }
        }

        permAndRoleConfig.setRoleMappings(roleMappingList.toArray(new RoleMapping[roleMappingList
                .size()]));
        serviceProvider.setPermissionAndRoleConfig(permAndRoleConfig);

        if (serviceProvider.getClaimConfig() == null) {
            serviceProvider.setClaimConfig(new ClaimConfig());
        }

        if (request.getParameter("claim_dialect") != null
                && "custom".equals(request.getParameter("claim_dialect"))) {
            serviceProvider.getClaimConfig().setLocalClaimDialect(false);
        } else {
            serviceProvider.getClaimConfig().setLocalClaimDialect(true);
        }

        // update claim configuration.
        int claimCount = Integer.parseInt(request.getParameter("number_of_claim_mappings"));
        List<ClaimMapping> claimMappingList = new ArrayList<ClaimMapping>();

        for (int i = 0; i < claimCount; i++) {
            ClaimMapping mapping = new ClaimMapping();

            Claim localClaim = new Claim();
            localClaim.setClaimUri(request.getParameter("idpClaim_" + i));

            Claim spClaim = new Claim();
            spClaim.setClaimUri(request.getParameter("spClaim_" + i));

            String requested = request.getParameter("spClaim_req_" + i);
            if (requested != null && "on".equals(requested)) {
                mapping.setRequested(true);
            } else {
                mapping.setRequested(false);
            }

            String mandatory = request.getParameter("spClaim_mand_" + i);
            if (mandatory != null && "on".equals(mandatory)) {
                mapping.setMandatory(true);
            } else {
                mapping.setMandatory(false);
            }

            mapping.setLocalClaim(localClaim);
            mapping.setRemoteClaim(spClaim);

            if (isLocalClaimsSelected() || mapping.getRemoteClaim().getClaimUri() == null ||
                    mapping.getRemoteClaim().getClaimUri().isEmpty()) {
                mapping.getRemoteClaim().setClaimUri(mapping.getLocalClaim().getClaimUri());
            }

            if (mapping.getLocalClaim().getClaimUri() != null
                    && mapping.getRemoteClaim().getClaimUri() != null) {
                claimMappingList.add(mapping);
            }
        }

        String spClaimDialectParam = request.getParameter(ApplicationMgtUIConstants.Params.SP_CLAIM_DIALECT);
        String[] spClaimDialects = null;
        if (StringUtils.isNotBlank(spClaimDialectParam)) {
            spClaimDialects = spClaimDialectParam.split(",");

        }
        serviceProvider.getClaimConfig().setSpClaimDialects(spClaimDialects);

        serviceProvider.getClaimConfig().setClaimMappings(
                claimMappingList.toArray(new ClaimMapping[claimMappingList.size()]));

        serviceProvider.getClaimConfig().setRoleClaimURI(request.getParameter("roleClaim"));

        String alwaysSendMappedLocalSubjectId = request.getParameter("always_send_local_subject_id");
        serviceProvider.getClaimConfig().setAlwaysSendMappedLocalSubjectId(
                alwaysSendMappedLocalSubjectId != null
                        && "on".equals(alwaysSendMappedLocalSubjectId) ? true : false);

    }

    /**
     * Set the server configured custom inbound authenticator configs as map.
     *
     * @param customInboundAuthenticatorConfigs Custom inbound authenticators enabled for the server.
     */
    public void setCustomInboundAuthenticatorConfigs(
            InboundAuthenticationRequestConfig[] customInboundAuthenticatorConfigs) {

        Map<String, InboundAuthenticationRequestConfig> customInboundAuthConfigs = new HashMap<>();
        if (customInboundAuthenticatorConfigs != null && customInboundAuthenticatorConfigs.length > 0) {
            for (InboundAuthenticationRequestConfig config : customInboundAuthenticatorConfigs) {
                customInboundAuthConfigs.put(
                        config.getInboundAuthType() + ":" + config.getInboundConfigType(), config);
            }
        }
        this.customInboundAuthenticatorConfigs = customInboundAuthConfigs;
    }

    /**
     * @return
     */
    public Map<String, String> getRequestedClaims() {
        return requestedClaims;
    }

    /**
     * @return
     */
    public Map<String, String> getMandatoryClaims() {
        return mandatoryClaims;
    }

    /**
     * @param wstrustEp
     */
    public void setWstrustEp(String wstrustEp) {
        if (CollectionUtils.isEmpty(this.wstrustEp)) {
            this.wstrustEp = new ArrayList<String>(0);
        }

        this.wstrustEp.clear();
        this.wstrustEp.add(wstrustEp);
    }

    /**
     * @param wstrustEps
     */
    public void setWstrustEp(List<String> wstrustEps) {
        this.wstrustEp = wstrustEps;
    }

    /**
     * @param passivests
     */
    public void setPassivests(String passivests) {
        this.passivests = passivests;
    }

    /**
     * @param passiveSTSWReply
     */
    public void setPassiveSTSWReply(String passiveSTSWReply) {
        this.passiveSTSWReply = passiveSTSWReply;
    }

    /**
     * @param openid
     */
    public void setOpenid(String openid) {
        this.openid = openid;
    }

    /**
     * @param wstrustEp
     */
    public void addWstrustEp(String wstrustEp) {
        if (wstrustEp != null && !wstrustEp.isEmpty()) {
            if (this.wstrustEp == null) {
                this.wstrustEp = new ArrayList<String>(0);
            }
            this.wstrustEp.add(wstrustEp);
        }
    }

    /**
     * @param wstrustEp
     */
    public void removeWstrustEp(String wstrustEp) {
        if (wstrustEp != null && !wstrustEp.isEmpty()) {
            if (this.wstrustEp != null && !this.wstrustEp.isEmpty()) {
                if (this.wstrustEp.stream().anyMatch(entry -> wstrustEp.equalsIgnoreCase(entry))) {
                    this.wstrustEp.remove(wstrustEp);

                    InboundAuthenticationRequestConfig[] authRequestConfigs = serviceProvider
                            .getInboundAuthenticationConfig().getInboundAuthenticationRequestConfigs();

                    if (authRequestConfigs != null && authRequestConfigs.length > 0) {
                        List<InboundAuthenticationRequestConfig> tempAuthRequest =
                                new ArrayList<InboundAuthenticationRequestConfig>();
                        for (InboundAuthenticationRequestConfig authRequestConfig : authRequestConfigs) {
                            if ("wstrust".equalsIgnoreCase(authRequestConfig.getInboundAuthType()) &&
                                wstrustEp.equalsIgnoreCase(authRequestConfig.getInboundAuthKey())) {
                                continue;
                            }
                            tempAuthRequest.add(authRequestConfig);
                        }
                        if (CollectionUtils.isNotEmpty(tempAuthRequest)) {
                            serviceProvider
                                    .getInboundAuthenticationConfig()
                                    .setInboundAuthenticationRequestConfigs(
                                            tempAuthRequest
                                                    .toArray(new InboundAuthenticationRequestConfig[tempAuthRequest
                                                            .size()]));
                        } else {
                            serviceProvider.getInboundAuthenticationConfig()
                                    .setInboundAuthenticationRequestConfigs(null);
                        }
                    }
                }
            }
        }
    }

    /**
     * @param request
     */
    public void updateLocalSp(HttpServletRequest request) {

        // update basic info.
        serviceProvider.setApplicationName(request.getParameter("spName"));
        serviceProvider.setDescription(request.getParameter("sp-description"));

        String provisioningUserStore = request.getParameter("scim-inbound-userstore");
        InboundProvisioningConfig inBoundProConfig = new InboundProvisioningConfig();
        inBoundProConfig.setProvisioningUserStore(provisioningUserStore);
        inBoundProConfig.setDumbMode(Boolean.parseBoolean(request.getParameter(DUMB)));
        serviceProvider.setInboundProvisioningConfig(inBoundProConfig);

        String[] provisioningProviders = request.getParameterValues("provisioning_idp");
        List<IdentityProvider> provisioningIdps = new ArrayList<IdentityProvider>();

        if (serviceProvider.getOutboundProvisioningConfig() == null
                || provisioningProviders == null || provisioningProviders.length == 0) {
            serviceProvider.setOutboundProvisioningConfig(new OutboundProvisioningConfig());
        }

        if (provisioningProviders != null && provisioningProviders.length > 0) {
            for (String proProvider : provisioningProviders) {
                String connector = request.getParameter("provisioning_con_idp_" + proProvider);
                String jitEnabled = request.getParameter("provisioning_jit_" + proProvider);
                String blocking = request.getParameter("blocking_prov_" + proProvider);
                String rulesEnabled = request.getParameter("rules_enabled_" + proProvider);
                JustInTimeProvisioningConfig jitpro = new JustInTimeProvisioningConfig();

                if ("on".equals(jitEnabled)) {
                    jitpro.setProvisioningEnabled(true);
                }
                if (connector != null) {
                    IdentityProvider proIdp = new IdentityProvider();
                    proIdp.setIdentityProviderName(proProvider);
                    ProvisioningConnectorConfig proCon = new ProvisioningConnectorConfig();
                    if ("on".equals(blocking)) {
                        proCon.setBlocking(true);
                    }
                    if ("on".equals(rulesEnabled)) {
                        proCon.setRulesEnabled(true);
                    }
                    proCon.setName(connector);
                    proIdp.setJustInTimeProvisioningConfig(jitpro);
                    proIdp.setDefaultProvisioningConnectorConfig(proCon);
                    provisioningIdps.add(proIdp);
                }
            }

            if (CollectionUtils.isNotEmpty(provisioningIdps)) {
                OutboundProvisioningConfig outboundProConfig = new OutboundProvisioningConfig();
                outboundProConfig.setProvisioningIdentityProviders(provisioningIdps
                        .toArray(new IdentityProvider[provisioningIdps.size()]));
                serviceProvider.setOutboundProvisioningConfig(outboundProConfig);
            }
        }

    }

    public ApplicationPurposes getApplicationPurposes() {

        return applicationPurposes;
    }

    public void setApplicationPurposes(ApplicationPurposes applicationPurposes) {

        this.applicationPurposes = applicationPurposes;
    }

    public Purpose[] getSharedPurposes() {

        return sharedPurposes;
    }

    public void setSharedPurposes(Purpose[] sharedPurposes) {

        this.sharedPurposes = sharedPurposes;
    }

    /**
     * To check whether to append userstore domain name with role name.
     *
     * @return true, if the user store domain should be appended with the role name.
     */
    public boolean isUseUserstoreDomainInRoles() {
        if (serviceProvider.getLocalAndOutBoundAuthenticationConfig() != null) {
            return serviceProvider.getLocalAndOutBoundAuthenticationConfig().getUseUserstoreDomainInRoles();
        }
        return false;
    }

    private ArrayList<ServiceProviderProperty> getServiceProviderProperties() {
        ArrayList<ServiceProviderProperty> spPropList;
        if (serviceProvider.getSpProperties() != null) {
            spPropList = new ArrayList<>(Arrays.asList(serviceProvider.getSpProperties()));
        } else {
            spPropList = new ArrayList<>();
        }
        return spPropList;
    }

}
