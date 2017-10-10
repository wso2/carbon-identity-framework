/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl;

import org.apache.commons.collections.CollectionUtils;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.IObjectFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorStateInfo;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ApplicationConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ExternalIdPConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.handler.claims.ClaimHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.provisioning.ProvisioningHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.step.StepHandler;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.authentication.framwork.test.utils.CommonTestUtils;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.ThreadLocalProvisioningServiceProvider;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationManagementUtil;
import org.wso2.carbon.identity.application.mgt.ApplicationConstants;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.doAnswer;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.doThrow;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

@PrepareForTest({FrameworkUtils.class, IdentityApplicationManagementUtil.class})
public class DefaultStepBasedSequenceHandlerTest {

    @Spy
    private DefaultStepBasedSequenceHandler stepBasedSequenceHandler;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private AuthenticationContext context;

    private ThreadLocalProvisioningServiceProvider threadLocalProvisioningSp;

    @ObjectFactory
    public IObjectFactory getObjectFactory() {
        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    @BeforeMethod
    public void setUp() throws Exception {
        initMocks(this);
        context = spy(new AuthenticationContext());
    }

    @AfterMethod
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetInstance() throws Exception {
        CommonTestUtils.testSingleton(
                DefaultStepBasedSequenceHandler.getInstance(),
                DefaultStepBasedSequenceHandler.getInstance()
        );
    }

    @DataProvider(name = "spRoleMappingDataProvider")
    public Object[][] provideSpRoleMappingData() {
        return Util.getSpRoleMappingData();
    }

    @Test(dataProvider = "spRoleMappingDataProvider")
    public void testGetServiceProviderMappedUserRoles(Map<String, String> spRoleMappings,
                                                      List<String> localUserRoles,
                                                      String multiAttributeSeparator,
                                                      String expectedRoles) throws Exception {
        Util.mockMultiAttributeSeparator(multiAttributeSeparator);
        SequenceConfig sequenceConfig = Util.mockSequenceConfig(spRoleMappings);
        String mappedRoles = stepBasedSequenceHandler.getServiceProviderMappedUserRoles(sequenceConfig, localUserRoles);
        assertEquals(mappedRoles, expectedRoles, "Service Provider Mapped Role do not have the expect value.");
    }

    @DataProvider(name = "spRoleClaimUriProvider")
    private Object[][] getSpRoleClaimUriData() {
        return new Object[][]{
                {"SP_ROLE_CLAIM", "SP_ROLE_CLAIM"},
                {null, FrameworkConstants.LOCAL_ROLE_CLAIM_URI},
                {"", FrameworkConstants.LOCAL_ROLE_CLAIM_URI}
        };
    }

    /*
        Find SP mapped role claim URI among mapped claims
     */
    @Test(dataProvider = "spRoleClaimUriProvider")
    public void testGetSpRoleClaimUri(String spRoleClaimUri,
                                      String expectedRoleClaimUri) throws Exception {
        ApplicationConfig appConfig = mock(ApplicationConfig.class);
        when(appConfig.getRoleClaim()).thenReturn(spRoleClaimUri);
        assertEquals(stepBasedSequenceHandler.getSpRoleClaimUri(appConfig), expectedRoleClaimUri);
    }

    @DataProvider(name = "spClaimMappingProvider")
    public Object[][] getSpClaimMappingProvider() {
        return new Object[][]{
                {       // SP mapped role claim
                        new HashMap<String, String>() {{
                            put("SP_ROLE_CLAIM", FrameworkConstants.LOCAL_ROLE_CLAIM_URI);
                        }},
                        "SP_ROLE_CLAIM"
                },
                {       // Role claim not among SP mapped claims
                        new HashMap<String, String>() {{
                            put("SP_CLAIM", "LOCAL_CLAIM");
                        }},
                        FrameworkConstants.LOCAL_ROLE_CLAIM_URI
                },
                {      // No SP mapped claims
                        new HashMap<>(), FrameworkConstants.LOCAL_ROLE_CLAIM_URI
                },
                {
                        null, FrameworkConstants.LOCAL_ROLE_CLAIM_URI
                }
        };
    }

    /*
        Get role claim URI from SP mapped claims
     */
    @Test(dataProvider = "spClaimMappingProvider")
    public void testGetSpRoleClaimUriSpMappedClaim(Map<String, String> claimMappings,
                                                   String expectedRoleClaim) throws Exception {
        ApplicationConfig appConfig = mock(ApplicationConfig.class);
        when(appConfig.getClaimMappings()).thenReturn(claimMappings);
        String roleClaim = stepBasedSequenceHandler.getSpRoleClaimUri(appConfig);
        assertEquals(roleClaim, expectedRoleClaim);
    }

    @DataProvider(name = "idpRoleClaimUriProvider")
    public Object[][] getIdpRoleClaimUriData() {
        return new Object[][]{
                {"IDP_ROLE_CLAIM", "IDP_ROLE_CLAIM"},
                {"", ""},
                {null, null}
        };
    }

    /*
        Get User Role Claim URI from IDP Mapped Role Claim URI
     */
    @Test(dataProvider = "idpRoleClaimUriProvider")
    public void testGetIdpRoleClaimUri(String idpRoleClaimUri,
                                       String expectedRoleClaimUri) throws Exception {

        ExternalIdPConfig externalIdPConfig = mock(ExternalIdPConfig.class);
        when(externalIdPConfig.getRoleClaimUri()).thenReturn(idpRoleClaimUri);
        assertEquals(stepBasedSequenceHandler.getIdpRoleClaimUri(externalIdPConfig), expectedRoleClaimUri);
    }

    @DataProvider(name = "idpClaimMappingProvider")
    public Object[][] getIdpClaimMappingsProvider() {
        return new Object[][]{
                {       // SP mapped role claim
                        new ClaimMapping[]{
                                ClaimMapping.build(FrameworkConstants.LOCAL_ROLE_CLAIM_URI, "IDP_ROLE_CLAIM", "", true)
                        },
                        "IDP_ROLE_CLAIM"
                },
                {       // Role claim not among SP mapped claims
                        new ClaimMapping[]{
                                ClaimMapping.build("LOCAL_CLAIM", "IDP_CLAIM", "", true)
                        },
                        null
                },
                {       // Role claim among claim mappings but remote claim is null
                        new ClaimMapping[]{
                                ClaimMapping.build(FrameworkConstants.LOCAL_ROLE_CLAIM_URI, null, null, true)
                        },
                        null
                },
                {      // No IDP mapped claims
                        new ClaimMapping[0], null
                },
                {
                        null, null
                }
        };
    }

    @Test(dataProvider = "idpClaimMappingProvider")
    public void testGetIdpRoleClaimUriFromClaimMappings(Object claimMappings,
                                                        String expectedRoleClaimUri) throws Exception {

        ExternalIdPConfig externalIdPConfig = mock(ExternalIdPConfig.class);
        when(externalIdPConfig.getClaimMappings()).thenReturn((ClaimMapping[]) claimMappings);

        String roleClaim = stepBasedSequenceHandler.getIdpRoleClaimUri(externalIdPConfig);
        assertEquals(roleClaim, expectedRoleClaimUri);

    }

    @Test
    public void testHandleClaimMappings() throws Exception {
        ClaimHandler claimHandler = Util.mockClaimHandler();
        mockStatic(FrameworkUtils.class);
        when(FrameworkUtils.getClaimHandler()).thenReturn(claimHandler);

        Map<String, String> claims = stepBasedSequenceHandler.handleClaimMappings(
                null,
                new AuthenticationContext(),
                new HashMap<String, String>(),
                false);
        assertNotNull(claims);
    }

    @Test
    public void testHandleClaimMappingsFailed() throws Exception {

        ClaimHandler claimHandler = mock(ClaimHandler.class);
        doThrow(new FrameworkException("Claim Handling failed"))
                .when(claimHandler)
                .handleClaimMappings(any(StepConfig.class), any(AuthenticationContext.class), any(Map.class), anyBoolean());

        mockStatic(FrameworkUtils.class);
        when(FrameworkUtils.getClaimHandler()).thenReturn(claimHandler);

        Map<String, String> claims = stepBasedSequenceHandler.handleClaimMappings(
                null,
                new AuthenticationContext(),
                new HashMap<String, String>(),
                false);

        assertNotNull(claims);
        assertEquals(claims.size(), 0);
    }

    @DataProvider(name = "idpMappedUserRoleDataProvider")
    public Object[][] getIdpMappedUserRolesData() {
        return new Object[][]{
                // IDP mapped user role is null
                {null, null, true, null}
        };
    }

    @Test(dataProvider = "idpMappedUserRoleDataProvider")
    public void testGetIdentityProviderMappedUserRoles(Map<String, String> attributeValueMap,
                                                       String idpRoleClaimUri,
                                                       boolean excludeUnmapped,
                                                       List<String> expected) throws Exception {

        ExternalIdPConfig externalIdPConfig = mock(ExternalIdPConfig.class);

        List<String> mappedUserRoles = stepBasedSequenceHandler.getIdentityProvideMappedUserRoles(externalIdPConfig,
                attributeValueMap, idpRoleClaimUri, excludeUnmapped);

        if (CollectionUtils.isEmpty(mappedUserRoles)) {
            mappedUserRoles = Collections.emptyList();
        }

        if (CollectionUtils.isEmpty(expected)) {
            expected = Collections.emptyList();
        }

        Collections.sort(mappedUserRoles);
        Collections.sort(expected);
        assertEquals(mappedUserRoles, expected);
    }

    @Test
    public void testResetAuthenticationContext() throws Exception {

        AuthenticationContext context = new AuthenticationContext();
        context.setSubject(new AuthenticatedUser());
        context.setStateInfo(mock(AuthenticatorStateInfo.class));
        context.setExternalIdP(mock(ExternalIdPConfig.class));

        Map<String, String> authenticatorProperties = new HashMap<>();
        authenticatorProperties.put("Prop1", "Value1");

        context.setAuthenticatorProperties(authenticatorProperties);
        context.setRetryCount(3);
        context.setRetrying(true);
        context.setCurrentAuthenticator("OIDCAuthenticator");

        stepBasedSequenceHandler.resetAuthenticationContext(context);

        assertResetContext(context);
    }

    private void assertResetContext(AuthenticationContext context) {
        assertNull(context.getSubject());
        assertNull(context.getStateInfo());
        assertNull(context.getExternalIdP());
        assertEquals(context.getAuthenticatorProperties().size(), 0);
        assertEquals(context.getRetryCount(), 0);
        assertFalse(context.isRetrying());
        assertNull(context.getCurrentAuthenticator());
    }

    @DataProvider(name = "jitProvisioningDataProvider")
    public Object[][] getJitProvisioningData() {

        final String PROVISIONING_USERSTORE_CLAIM_URI = "provisioning_user_store_claimUri";
        final String PROVISONING_USERSTORE_BY_ID = "PROVISIONING_USER_STORE1";
        final String PROVISONING_USERSTORE_BY_CLAIM = "PROVISIONING_USER_STORE2";

        return new Object[][]{
                // Provisioning User Store ID , Provisioning User Store Claim URI, External Attribute Map, expected
                // userstore to which user should be provisioned to
                {
                        // Provisioning user store picked from provisioningUserStoreId
                        PROVISONING_USERSTORE_BY_ID,
                        PROVISIONING_USERSTORE_CLAIM_URI,
                        new HashMap<>(),
                        PROVISONING_USERSTORE_BY_ID
                },
                {
                        // Provisioning user store picked from external claims
                        null,
                        PROVISIONING_USERSTORE_CLAIM_URI,
                        new HashMap<String, String>() {{
                            put(PROVISIONING_USERSTORE_CLAIM_URI, PROVISONING_USERSTORE_BY_CLAIM);
                        }},
                        PROVISONING_USERSTORE_BY_CLAIM
                },
                {
                        // Provisioning user store not picked from provisioningUserStoreId or external claims
                        null,
                        PROVISIONING_USERSTORE_CLAIM_URI,
                        new HashMap<>(),
                        null
                }
        };
    }


    @Test(dataProvider = "jitProvisioningDataProvider")
    public void testHandleJitProvisioning(String provisioningUserStoreId,
                                          String provisioningUserStoreClaimUri,
                                          Map<String, String> externalAttributeValues,
                                          String expectedUserStoreToBeProvisioned) throws Exception {

        final String subjectIdentifier = "subjectID";
        final List<String> mappedRoles = Collections.emptyList();
        final String TENANT_DOMAIN = "foo.com";

        context = getMockedContextForJitProvisioning(provisioningUserStoreId, provisioningUserStoreClaimUri, TENANT_DOMAIN);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        // Mock the provisioning handler
        ProvisioningHandler provisioningHandler = mock(ProvisioningHandler.class);
        doNothing().when(provisioningHandler).handle(anyList(), anyString(), anyMap(), captor.capture(), anyString());

        // Mock framework util to returned mocked provisoning handler
        returnMockedProvisoningHandlerFromFramework(provisioningHandler);
        mockHandlerThreadLocalProvisioningServiceProvider();

        stepBasedSequenceHandler.handleJitProvisioning(subjectIdentifier, context, mappedRoles, externalAttributeValues);
        verify(provisioningHandler).handle(anyList(), anyString(), anyMap(), captor.capture(), anyString());

        // check whether the user is provisioned to correct user store
        assertEquals(captor.getValue(), expectedUserStoreToBeProvisioned);
        assertNotNull(threadLocalProvisioningSp);
        assertTrue(threadLocalProvisioningSp.isJustInTimeProvisioning());
        assertEquals(threadLocalProvisioningSp.getTenantDomain(), TENANT_DOMAIN);
        assertEquals(threadLocalProvisioningSp.getClaimDialect(), ApplicationConstants.LOCAL_IDP_DEFAULT_CLAIM_DIALECT);
    }

    private void mockHandlerThreadLocalProvisioningServiceProvider() throws Exception {

        mockStatic(IdentityApplicationManagementUtil.class);

        doAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) throws Throwable {
                threadLocalProvisioningSp = (ThreadLocalProvisioningServiceProvider) invocation.getArguments()[0];
                return null;
            }
        }).when(IdentityApplicationManagementUtil.class, "setThreadLocalProvisioningServiceProvider",
                any(ThreadLocalProvisioningServiceProvider.class));
    }

    private AuthenticationContext getMockedContextForJitProvisioning(String provisioningUserStoreId,
                                                                     String provisioningUserStoreClaimUri,
                                                                     String tenantDomain) {

        ExternalIdPConfig externalIdPConfig = spy(new ExternalIdPConfig());
        when(externalIdPConfig.getProvisioningUserStoreId()).thenReturn(provisioningUserStoreId);
        when(externalIdPConfig.getProvisioningUserStoreClaimURI()).thenReturn(provisioningUserStoreClaimUri);

        ApplicationConfig applicationConfig = new ApplicationConfig(mock(ServiceProvider.class));
        applicationConfig.setApplicationName("DUMMY_NAME");

        SequenceConfig sequenceConfig = new SequenceConfig();
        sequenceConfig.setApplicationConfig(applicationConfig);

        context.setTenantDomain(tenantDomain);
        context.setSequenceConfig(sequenceConfig);
        context.setExternalIdP(externalIdPConfig);

        return context;
    }

    @Test
    public void testHandleJitProvisioningFailure() throws Exception {

        final String subjectIdentifier = "subjectID";
        final List<String> mappedRoles = Collections.emptyList();
        Map<String, String> externalAttributeValues = Collections.emptyMap();

        context = getMockedContextForJitProvisioning(null, null, null);
        // Mock the provisioning handler
        ProvisioningHandler provisioningHandler = mock(ProvisioningHandler.class);
        doThrow(new FrameworkException("Provisioning Failed"))
                .when(provisioningHandler).handle(anyList(), anyString(), anyMap(), anyString(), anyString());

        returnMockedProvisoningHandlerFromFramework(provisioningHandler);

        try {
            stepBasedSequenceHandler
                    .handleJitProvisioning(subjectIdentifier, context, mappedRoles, externalAttributeValues);
        } catch (FrameworkException ex) {
            fail("Possible API change. This method did not throw any exception to outside before.");
        }
    }

    private void returnMockedProvisoningHandlerFromFramework(ProvisioningHandler provisioningHandler) throws FrameworkException {
        // Mock framework util to returned mocked provisioning handler
        mockStatic(FrameworkUtils.class);
        when(FrameworkUtils.getProvisioningHandler()).thenReturn(provisioningHandler);
    }

    /**
     * First step of the sequence is handled
     */
    @Test
    public void testHandleSingleStep() throws Exception {
        // mock the step handler
        StepHandler stepHandler = getMockedStepHandlerForIncompleteStep(true);

        mockStatic(FrameworkUtils.class);
        when(FrameworkUtils.getStepHandler()).thenReturn(stepHandler);

        StepConfig stepConfig = new StepConfig();
        SequenceConfig sequenceConfig = new SequenceConfig();
        sequenceConfig.getStepMap().put(1, stepConfig);
        context.setSequenceConfig(sequenceConfig);

        stepBasedSequenceHandler.handle(request, response, context);
        assertFalse(context.getSequenceConfig().isCompleted());
        assertTrue(context.isRequestAuthenticated());
    }

    @Test
    public void testHandleSingleStepFinish() throws Exception {
        // mock the step handler
        StepHandler stepHandler = getMockedStepHandlerForSuccessfulRequestAuthentication();
        mockStatic(FrameworkUtils.class);
        when(FrameworkUtils.getStepHandler()).thenReturn(stepHandler);

        StepConfig stepConfig = new StepConfig();
        SequenceConfig sequenceConfig = new SequenceConfig();
        sequenceConfig.getStepMap().put(1, stepConfig);
        context.setSequenceConfig(sequenceConfig);

        doNothing().when(stepBasedSequenceHandler).handlePostAuthentication(any(HttpServletRequest.class), any
                (HttpServletResponse.class), any(AuthenticationContext.class));
        stepBasedSequenceHandler.handle(request, response, context);

        assertTrue(context.getSequenceConfig().isCompleted());
        assertTrue(context.isRequestAuthenticated());
        assertResetContext(context);
    }

    private StepHandler getMockedStepHandlerForSuccessfulRequestAuthentication() throws Exception {
        // mock the step handler
        StepHandler stepHandler = mock(StepHandler.class);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                AuthenticationContext context = invocationOnMock.getArgumentAt(2, AuthenticationContext.class);
                StepConfig stepConfig = context.getSequenceConfig().getStepMap().get(context.getCurrentStep());
                stepConfig.setCompleted(true);
                context.setRequestAuthenticated(true);
                return null;
            }
        }).when(stepHandler).handle(any(HttpServletRequest.class), any(HttpServletResponse.class),
                any(AuthenticationContext.class));

        return stepHandler;
    }

    private StepHandler getMockedStepHandlerForIncompleteStep(final boolean isRequestAuthenticated) throws Exception {
        // mock the step handler
        StepHandler stepHandler = mock(StepHandler.class);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                AuthenticationContext context = invocationOnMock.getArgumentAt(2, AuthenticationContext.class);
                StepConfig stepConfig = context.getSequenceConfig().getStepMap().get(context.getCurrentStep());
                stepConfig.setCompleted(false);
                context.setRequestAuthenticated(isRequestAuthenticated);
                return null;
            }
        }).when(stepHandler).handle(any(HttpServletRequest.class), any(HttpServletResponse.class),
                any(AuthenticationContext.class));
        return stepHandler;
    }

    @DataProvider(name = "stepData")
    public Object[][] provideStepData() {
        return new Object[][]{
                {
                        // Intermediate step is authenticated
                        true
                },
                {
                        // Intermediate step failed to authenticate
                        false
                }
        };
    }

    @Test(dataProvider = "stepData")
    public void testHandleIntermediateStep(boolean secondStepAuthenticated) throws Exception {
        StepHandler stepHandler = getMockedStepHandlerForSuccessfulRequestAuthentication();
        mockStatic(FrameworkUtils.class);
        when(FrameworkUtils.getStepHandler()).thenReturn(stepHandler);

        StepConfig firstStep = new StepConfig();
        firstStep.setOrder(1);

        // Second step is completed.
        StepConfig lastStep = new StepConfig();
        lastStep.setOrder(2);
        lastStep.setCompleted(true);

        SequenceConfig sequenceConfig = new SequenceConfig();
        sequenceConfig.getStepMap().put(1, firstStep);
        sequenceConfig.getStepMap().put(2, lastStep);

        doNothing().when(stepBasedSequenceHandler).handlePostAuthentication(any(HttpServletRequest.class), any
                (HttpServletResponse.class), any(AuthenticationContext.class));

        // currently we have completed second step
        context.setCurrentStep(2);
        context.setSequenceConfig(sequenceConfig);
        context.setRequestAuthenticated(secondStepAuthenticated);

        stepBasedSequenceHandler.handle(request, response, context);
    }

    @Test
    public void testHandleMultiOptionStep() throws Exception {

        StepHandler stepHandler = getMockedStepHandlerForIncompleteStep(true);
        mockStatic(FrameworkUtils.class);
        when(FrameworkUtils.getStepHandler()).thenReturn(stepHandler);

        StepConfig firstStep = new StepConfig();
        firstStep.setOrder(1);

        // Second step is completed.
        StepConfig lastStep = new StepConfig();
        lastStep.setMultiOption(true);
        lastStep.setOrder(2);
        lastStep.setCompleted(true);

        SequenceConfig sequenceConfig = new SequenceConfig();
        sequenceConfig.getStepMap().put(1, firstStep);
        sequenceConfig.getStepMap().put(2, lastStep);

        doNothing().when(stepBasedSequenceHandler).handlePostAuthentication(any(HttpServletRequest.class), any
                (HttpServletResponse.class), any(AuthenticationContext.class));

        // currently we have completed second step
        context.setCurrentStep(2);
        context.setSequenceConfig(sequenceConfig);
        context.setRequestAuthenticated(false);

        stepBasedSequenceHandler.handle(request, response, context);
        assertResetContext(context);
        // Assert whether the sequence is retrying the step
        assertTrue(context.getSequenceConfig().getStepMap().get(context.getCurrentStep()).isRetrying());
        // Assert whether before retrying the context request authentication status was set to true.
        assertTrue(context.isRequestAuthenticated());

        // step handler completes the step successfully
        stepHandler = getMockedStepHandlerForSuccessfulRequestAuthentication();
        when(FrameworkUtils.getStepHandler()).thenReturn(stepHandler);

        stepBasedSequenceHandler.handle(request, response, context);
        assertTrue(context.getSequenceConfig().isCompleted());
        assertTrue(context.isRequestAuthenticated());
    }

    /*
        Even though the failed step has multi option, if the request is passive then we should not allow to retry
     */
    @Test
    public void testHandlePassiveAuthenticateWhenMultiOptionStep() throws Exception {

        StepHandler stepHandler = getMockedStepHandlerForSuccessfulRequestAuthentication();
        mockStatic(FrameworkUtils.class);
        when(FrameworkUtils.getStepHandler()).thenReturn(stepHandler);

        StepConfig firstStep = new StepConfig();
        firstStep.setOrder(1);

        // Second step is completed.
        StepConfig lastStep = new StepConfig();
        lastStep.setMultiOption(true);
        lastStep.setOrder(2);
        lastStep.setCompleted(true);

        SequenceConfig sequenceConfig = new SequenceConfig();
        sequenceConfig.getStepMap().put(1, firstStep);
        sequenceConfig.getStepMap().put(2, lastStep);

        doNothing().when(stepBasedSequenceHandler).handlePostAuthentication(any(HttpServletRequest.class), any
                (HttpServletResponse.class), any(AuthenticationContext.class));

        // currently we have completed second step
        context.setCurrentStep(2);
        context.setSequenceConfig(sequenceConfig);
        context.setPassiveAuthenticate(true);
        context.setRequestAuthenticated(false);

        stepBasedSequenceHandler.handle(request, response, context);
        assertResetContext(context);
        assertTrue(context.getSequenceConfig().isCompleted());
        assertFalse(context.getSequenceConfig().getStepMap().get(context.getCurrentStep()).isRetrying());
    }
}
