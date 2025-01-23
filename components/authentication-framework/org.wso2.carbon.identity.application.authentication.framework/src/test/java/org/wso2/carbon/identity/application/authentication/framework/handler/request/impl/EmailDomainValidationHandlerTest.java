/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.authentication.framework.handler.request.impl;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.AbstractFrameworkTest;
import org.wso2.carbon.identity.application.authentication.framework.FederatedApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.config.loader.UIBasedConfigurationLoader;
import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ExternalIdPConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.PostAuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.PostAuthnHandlerFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataHandler;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.organization.config.service.OrganizationConfigManager;
import org.wso2.carbon.identity.organization.config.service.exception.OrganizationConfigClientException;
import org.wso2.carbon.identity.organization.config.service.model.ConfigProperty;
import org.wso2.carbon.identity.organization.config.service.model.DiscoveryConfig;
import org.wso2.carbon.identity.organization.discovery.service.OrganizationDiscoveryManager;
import org.wso2.carbon.identity.organization.discovery.service.model.OrgDiscoveryAttribute;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link EmailDomainValidationHandler}
 */
@WithCarbonHome
public class EmailDomainValidationHandlerTest extends AbstractFrameworkTest {

    private static final String VALID_EMAIL = "user@test.com";
    private static final String INVALID_EMAIL = "user@testInvalid.com";
    private static final String SUPER_ORG_ID = "10084a8d-113f-4211-a0d5-efe36b082211";
    private static final String SUB_ORG_ID = "93d996f9-a5ba-4275-a52b-adaad9eba869";
    public static final String SUPER_ORG_TENANT_DOMAIN = "carbon.super";
    public static final String SUB_ORG_TENANT_DOMAIN = "test";
    public static final String EMAIL_ADDRESS_CLAIM_URI = "http://wso2.org/claims/emailaddress";
    public static final String EMAIL = "email";

    private MockedStatic<CarbonContext> carbonContextMockedStatic;
    private MockedStatic<IdentityTenantUtil> identityTenantUtil;
    private MockedStatic<ClaimMetadataHandler> claimMetadataHandler;
    @Mock
    private OrganizationDiscoveryManager organizationDiscoveryManager;
    @Mock
    private OrganizationConfigManager organizationConfigManager;
    @Mock
    private OrganizationManager organizationManager;
    private CarbonContext carbonContext;
    private EmailDomainValidationHandler emailDomainValidationHandler;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private UIBasedConfigurationLoader configurationLoader;
    private ServiceProvider sp;
    private AutoCloseable mocks;

    @BeforeClass
    public void setUp() throws Exception {

        mocks = MockitoAnnotations.openMocks(this);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        configurationLoader = new UIBasedConfigurationLoader();

        identityTenantUtil = mockStatic(IdentityTenantUtil.class);
        claimMetadataHandler = mockStatic(ClaimMetadataHandler.class);

        emailDomainValidationHandler = EmailDomainValidationHandler.getInstance();
        sp = getTestServiceProvider("email-domain-validation-sp.xml");

        FrameworkServiceDataHolder.getInstance().setOrganizationDiscoveryManager(organizationDiscoveryManager);
        FrameworkServiceDataHolder.getInstance().setOrganizationConfigManager(organizationConfigManager);
        FrameworkServiceDataHolder.getInstance().setOrganizationManager(organizationManager);

        List<OrgDiscoveryAttribute> orgDiscoveryAttributes = new ArrayList<>();
        OrgDiscoveryAttribute orgDiscoveryAttribute = new OrgDiscoveryAttribute();
        orgDiscoveryAttribute.setType("emailDomain");
        orgDiscoveryAttribute.setValues(Collections.singletonList("test.com"));
        orgDiscoveryAttributes.add(orgDiscoveryAttribute);
        when(organizationDiscoveryManager.getOrganizationDiscoveryAttributes(anyString(), anyBoolean())).thenReturn(
                orgDiscoveryAttributes);

        carbonContextMockedStatic = mockStatic(CarbonContext.class);
        carbonContext = mock(CarbonContext.class);
        carbonContextMockedStatic.when(CarbonContext::getThreadLocalCarbonContext).thenReturn(carbonContext);
        when(carbonContext.getTenantDomain()).thenReturn(SUPER_ORG_TENANT_DOMAIN);
    }

    @AfterClass
    public void tearDown() throws Exception {

        carbonContextMockedStatic.close();
        identityTenantUtil.close();
        claimMetadataHandler.close();
        mocks.close();
    }

    @Test(description = "Test whether the email domain validation handler is disabled for primary organizations.")
    public void testIsDisabledForPrimaryOrganizations() throws Exception {

        when(carbonContext.getTenantDomain()).thenReturn(SUPER_ORG_TENANT_DOMAIN);
        when(organizationManager.resolveOrganizationId(SUPER_ORG_TENANT_DOMAIN)).thenReturn(SUPER_ORG_ID);
        when(organizationManager.isPrimaryOrganization(SUPER_ORG_ID)).thenReturn(true);
        Assert.assertFalse(emailDomainValidationHandler.isEnabled(),
                "Email domain validation handler should be disabled for primary organizations.");
    }

    @Test(description = "Test whether the email domain validation handler is enabled for sub organizations when " +
            "email domain discovery is enabled.")
    public void testIsEnabledForSubOrganizationsWhenEmailDomainDiscoveryEnabled() throws Exception {

        reset(organizationConfigManager);
        when(carbonContext.getTenantDomain()).thenReturn(SUB_ORG_TENANT_DOMAIN);
        when(organizationManager.resolveOrganizationId(SUB_ORG_TENANT_DOMAIN)).thenReturn(SUB_ORG_ID);
        when(organizationManager.isPrimaryOrganization(SUB_ORG_ID)).thenReturn(false);
        when(organizationManager.getPrimaryOrganizationId(SUB_ORG_ID)).thenReturn(SUPER_ORG_ID);
        when(organizationManager.resolveTenantDomain(SUPER_ORG_ID)).thenReturn(SUPER_ORG_TENANT_DOMAIN);

        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(SUPER_ORG_TENANT_DOMAIN)).thenReturn(-1234);

        List<ConfigProperty> configProperties = new ArrayList<>();
        ConfigProperty configProperty = new ConfigProperty("emailDomain.enable", "true");
        configProperties.add(configProperty);
        DiscoveryConfig discoveryConfig = new DiscoveryConfig(configProperties);
        when(organizationConfigManager.getDiscoveryConfigurationByTenantId(-1234)).thenReturn(discoveryConfig);

        Assert.assertTrue(emailDomainValidationHandler.isEnabled(),
                "Email domain validation handler should be enabled for" +
                        "sub organizations when email domain discovery is enabled.");
    }

    @Test(description = "Test whether the email domain validation handler is disabled for sub organizations when " +
            "email domain discovery is disabled.")
    public void testIsDisabledWhenNoDiscoveryConfigsForOrganization() throws Exception {

        when(carbonContext.getTenantDomain()).thenReturn(SUB_ORG_TENANT_DOMAIN);
        when(organizationManager.resolveOrganizationId(SUB_ORG_TENANT_DOMAIN)).thenReturn(SUB_ORG_ID);
        when(organizationManager.isPrimaryOrganization(SUB_ORG_ID)).thenReturn(false);
        when(organizationManager.getPrimaryOrganizationId(SUB_ORG_ID)).thenReturn(SUPER_ORG_ID);
        when(organizationManager.resolveTenantDomain(SUPER_ORG_ID)).thenReturn(SUPER_ORG_TENANT_DOMAIN);

        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(SUPER_ORG_TENANT_DOMAIN)).thenReturn(-1234);

        when(organizationConfigManager.getDiscoveryConfigurationByTenantId(-1234)).thenThrow(
                new OrganizationConfigClientException("No organization configs found."));

        Assert.assertFalse(emailDomainValidationHandler.isEnabled(),
                "Email domain validation handler should be disabled when there are no discovery" +
                        " configurations for the organization.");
    }

    @Test(description = "Test if the validation pass with a valid email domain for the authenticated user.")
    public void testAuthenticatedUserWithValidEmailDomain() throws Exception {

        AuthenticationContext context = buildAuthenticationContext(sp, VALID_EMAIL, false);
        PostAuthnHandlerFlowStatus status = emailDomainValidationHandler.handle(request, response, context);
        Assert.assertEquals(status, PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED,
                "Expected the email domain validation handler to succeed with a valid email domain.");
    }

    @Test(description = "Test if the validation fails with an invalid email domain for the authenticated user.",
            expectedExceptions = PostAuthenticationFailedException.class)
    public void testAuthenticatedUserWithInvalidEmailDomain() throws Exception {

        AuthenticationContext context = buildAuthenticationContext(sp, INVALID_EMAIL, false);
        emailDomainValidationHandler.handle(request, response, context);
    }

    @Test(description = "Test if the validation pass with a valid email domain when the user authenticates in a" +
            " non-subject attribute step.")
    public void testAuthenticatedUserWithValidEmailDomainAndNotSubjectAttributeStep() throws Exception {

        AuthenticationContext context = buildAuthenticationContext(sp, VALID_EMAIL, true);

        Map<String, String> mockedMappings = new HashMap<>();
        mockedMappings.put(EMAIL_ADDRESS_CLAIM_URI, EMAIL);

        ClaimMetadataHandler mockClaimMetadataHandler = mock(ClaimMetadataHandler.class);
        when(mockClaimMetadataHandler.getMappingsMapFromOtherDialectToCarbon(
                anyString(), anySet(), anyString(), anyBoolean())).thenReturn(mockedMappings);
        claimMetadataHandler.when(ClaimMetadataHandler::getInstance).thenReturn(mockClaimMetadataHandler);

        PostAuthnHandlerFlowStatus status = emailDomainValidationHandler.handle(request, response, context);
        Assert.assertEquals(status, PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED,
                "Expected the email domain validation handler to succeed with a valid email domain when the user " +
                        "authenticates in a non-subject attribute step.");
    }

    private AuthenticationContext buildAuthenticationContext(ServiceProvider sp, String userEmail,
                                                             boolean notSubjectAttributeStep) throws Exception {

        AuthenticationContext authenticationContext = getAuthenticationContext(sp);
        authenticationContext.setProperty(FrameworkConstants.STEP_BASED_SEQUENCE_HANDLER_TRIGGERED, true);
        SequenceConfig sequenceConfig =
                configurationLoader.getSequenceConfig(authenticationContext, Collections.emptyMap(), sp);
        authenticationContext.setSequenceConfig(sequenceConfig);

        FederatedApplicationAuthenticator authenticator = mock(FederatedApplicationAuthenticator.class);
        AuthenticatorConfig authenticatorConfig = new AuthenticatorConfig();
        when(authenticator.getClaimDialectURI()).thenReturn("http://wso2.org/oidc/claim");
        authenticatorConfig.setApplicationAuthenticator(authenticator);

        AuthenticatedUser user = new AuthenticatedUser();
        user.setUserName(userEmail);
        user.setAuthenticatedSubjectIdentifier(userEmail);

        Map<ClaimMapping, String> userAttributes = new HashMap<>();
        userAttributes.put(ClaimMapping.build(
                EMAIL_ADDRESS_CLAIM_URI,
                EMAIL,
                null, false), userEmail);

        user.setUserAttributes(userAttributes);

        for (Map.Entry<Integer, StepConfig> entry : sequenceConfig.getStepMap().entrySet()) {
            StepConfig stepConfig = entry.getValue();
            stepConfig.setAuthenticatedUser(user);
            stepConfig.setAuthenticatedAutenticator(authenticatorConfig);
            if (notSubjectAttributeStep) {
                stepConfig.setSubjectAttributeStep(false);
            }
        }

        if (notSubjectAttributeStep) {
            ExternalIdPConfig externalIdPConfig = mock(ExternalIdPConfig.class);
            when(externalIdPConfig.useDefaultLocalIdpDialect()).thenReturn(true);
            authenticationContext.setExternalIdP(externalIdPConfig);
        }

        authenticationContext.setSequenceConfig(sequenceConfig);

        Map<String, String> unfilteredLocalClaimValues = new HashMap<>();
        unfilteredLocalClaimValues.put(EMAIL_ADDRESS_CLAIM_URI, userEmail);
        authenticationContext.setProperty(FrameworkConstants.UNFILTERED_LOCAL_CLAIM_VALUES, unfilteredLocalClaimValues);

        return authenticationContext;
    }
}
