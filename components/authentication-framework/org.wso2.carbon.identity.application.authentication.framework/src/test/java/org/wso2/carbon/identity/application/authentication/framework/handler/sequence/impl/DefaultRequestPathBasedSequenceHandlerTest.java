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

import edu.emory.mathcs.backport.java.util.Arrays;
import org.mockito.Mock;
import org.mockito.Spy;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.IObjectFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.RequestPathApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ApplicationConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.exception.InvalidCredentialsException;
import org.wso2.carbon.identity.application.authentication.framework.exception.LogoutFailedException;
import org.wso2.carbon.identity.application.authentication.framework.handler.claims.ClaimHandler;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedIdPData;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.doThrow;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

@PrepareForTest(FrameworkUtils.class)
public class DefaultRequestPathBasedSequenceHandlerTest {


    private DefaultRequestPathBasedSequenceHandler sequenceHandler;

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @Mock
    RequestPathApplicationAuthenticator requestPathAuthenticator;

    @Spy
    private SequenceConfig sequenceConfig;

    @Spy
    private AuthenticatorConfig authenticatorConfig;


    private AuthenticationContext context;


    @BeforeClass
    public void setUp() throws Exception {

        initMocks(this);
//        mockStatic(FrameworkUtils.class);
//        when(FrameworkUtils.getMultiAttributeSeparator()).thenReturn(",");

        sequenceHandler = new DefaultRequestPathBasedSequenceHandler();
        // Mock authentication context and sequence config for request path authentication
        context = new AuthenticationContext();

        authenticatorConfig = spy(new AuthenticatorConfig());
        doReturn(requestPathAuthenticator).when(authenticatorConfig).getApplicationAuthenticator();

        sequenceConfig = spy(new SequenceConfig());
        doReturn(Arrays.asList(new AuthenticatorConfig[]{authenticatorConfig}))
                .when(sequenceConfig).getReqPathAuthenticators();

        context.setSequenceConfig(sequenceConfig);
    }

    @AfterMethod
    public void tearDown() throws Exception {
    }

    @ObjectFactory
    public IObjectFactory getObjectFactory() {
        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    @Test
    public void testGetInstance() throws Exception {

        DefaultRequestPathBasedSequenceHandler sequenceHandler = DefaultRequestPathBasedSequenceHandler.getInstance();
        Assert.assertNotNull(sequenceHandler);

        DefaultRequestPathBasedSequenceHandler anotherSequenceHandler = DefaultRequestPathBasedSequenceHandler
                .getInstance();
        Assert.assertNotNull(anotherSequenceHandler);
        Assert.assertEquals(sequenceHandler, anotherSequenceHandler);
    }

    /*
        None of the available request path authenticators can handle the request.
     */
    @Test
    public void testHandleNoneCanHandle() throws Exception {
        // mock the behaviour of the request path authenticator
        when(requestPathAuthenticator.canHandle(any(HttpServletRequest.class))).thenReturn(false);
        sequenceHandler.handle(request, response, context);
        Assert.assertEquals(context.getSequenceConfig().isCompleted(), true);
    }

    /*
        Request path authenticator throws an InvalidCredentialsException
     */
    @Test
    public void testHandleInvalidCredentialException() throws Exception {
        // mock the behaviour of the request path authenticator
        when(requestPathAuthenticator.canHandle(any(HttpServletRequest.class))).thenReturn(true);
        doThrow(new InvalidCredentialsException("Invalid Credentials."))
                .when(requestPathAuthenticator).process(request, response, context);

        sequenceHandler.handle(request, response, context);
        Assert.assertEquals(context.isRequestAuthenticated(), false);
    }

    /*
        Request path authenticator throws an InvalidCredentialsException
    */
    @Test
    public void testHandleAuthenticatorFailedException() throws Exception {

        // mock the behaviour of the request path authenticator
        when(requestPathAuthenticator.canHandle(any(HttpServletRequest.class))).thenReturn(true);
        doThrow(new AuthenticationFailedException("Authentication Failed."))
                .when(requestPathAuthenticator).process(request, response, context);

        sequenceHandler.handle(request, response, context);
        Assert.assertEquals(context.isRequestAuthenticated(), false);
    }

    /*
        Request path authenticator throws a LogoutFailedException
    */
    @Test(expectedExceptions = FrameworkException.class)
    public void testHandleLogoutFailedException() throws Exception {

        // mock the behaviour of the request path authenticator
        when(requestPathAuthenticator.canHandle(any(HttpServletRequest.class))).thenReturn(true);
        doThrow(new LogoutFailedException("Logout Failed."))
                .when(requestPathAuthenticator).process(request, response, context);

        sequenceHandler.handle(request, response, context);
    }

    /*
        Request path authenticator can handle the request successfully and authentication succeeds
     */
    @Test
    public void testHandleAuthSuccess() throws Exception {

        // mock the behaviour of the request path authenticator
        when(requestPathAuthenticator.canHandle(any(HttpServletRequest.class))).thenReturn(true);
        doReturn(AuthenticatorFlowStatus.SUCCESS_COMPLETED).when(requestPathAuthenticator)
                .process(any(HttpServletRequest.class), any(HttpServletResponse.class), any(AuthenticationContext.class));

        String subjectIdentifier = "H2/alice@t1.com";
        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setAuthenticatedSubjectIdentifier(subjectIdentifier);
        authenticatedUser.setFederatedUser(false);

        context.setSubject(authenticatedUser);

        mockStatic(FrameworkUtils.class);
        when(FrameworkUtils.getMultiAttributeSeparator()).thenReturn(",");

        sequenceHandler = spy(new DefaultRequestPathBasedSequenceHandler());
        // mock triggering post authentication
        doNothing().when(sequenceHandler).handlePostAuthentication(any(HttpServletRequest.class), any
                (HttpServletResponse.class), any(AuthenticationContext.class), any(AuthenticatedIdPData.class));

        sequenceHandler.handle(request, response, context);

        Assert.assertEquals(context.getSequenceConfig().isCompleted(), true);
        Assert.assertNotNull(context.getCurrentAuthenticatedIdPs());
        Assert.assertEquals(context.getCurrentAuthenticatedIdPs().size(), 1);

        AuthenticatedIdPData authenticatedIdPData = context.getCurrentAuthenticatedIdPs()
                .get(FrameworkConstants.LOCAL_IDP_NAME);

        Assert.assertNotNull(authenticatedIdPData);
        Assert.assertEquals(authenticatedIdPData.getIdpName(), FrameworkConstants.LOCAL_IDP_NAME);
        Assert.assertNotNull(authenticatedIdPData.getUser());
        Assert.assertEquals(authenticatedIdPData.getUser().getAuthenticatedSubjectIdentifier(), subjectIdentifier);
        Assert.assertEquals(authenticatedIdPData.getAuthenticator(), authenticatorConfig);
    }

    @Test
    public void testHandlePostAuthentication() throws Exception {
    }

    @DataProvider(name = "spRoleMappingDataProvider")
    public Object[][] provideSpRoleMappingData() {

        Map<String, String> spRoleMappings = new HashMap<>();
        spRoleMappings.put("LOCAL_ROLE1", "SP_ROLE1");
        spRoleMappings.put("LOCAL_ROLE2", "SP_ROLE2");

        List<String> localUserRoles = Arrays.asList(new String[]{"LOCAL_ROLE1", "ADMIN", "LOCAL_ROLE2"});
        String localRoles = "LOCAL_ROLE1,ADMIN,LOCAL_ROLE2";

        return new Object[][]{
                {spRoleMappings, localUserRoles, "SP_ROLE1,ADMIN,SP_ROLE2"},
                {null, localUserRoles, localRoles},
                {new HashMap<>(), localUserRoles, localRoles},
                {spRoleMappings, new ArrayList<>(), null},
                {spRoleMappings, null, null},
                {null, null, null}
        };
    }

    @Test(dataProvider = "spRoleMappingDataProvider")
    public void testGetServiceProviderMappedUserRoles(Map<String, String> spRoleMappings,
                                                      List<String> localUserRoles,
                                                      String expectedRoles) throws Exception {

        mockStatic(FrameworkUtils.class);
        when(FrameworkUtils.getMultiAttributeSeparator()).thenReturn(",");


        SequenceConfig sequenceConfig = spy(new SequenceConfig());
        ApplicationConfig applicationConfig = mock(ApplicationConfig.class);
        when(applicationConfig.getApplicationName()).thenReturn("APP");
        when(sequenceConfig.getApplicationConfig()).thenReturn(applicationConfig);
        when(applicationConfig.getRoleMappings()).thenReturn(spRoleMappings);

        String mappedRoles = sequenceHandler.getServiceProviderMappedUserRoles(sequenceConfig, localUserRoles);
        Assert.assertEquals(mappedRoles, expectedRoles);
    }

    @Test
    public void testGetSpRoleClaimUri() throws Exception {

        String roleClaim;
        ApplicationConfig appConfig = mock(ApplicationConfig.class);

        // IDP mapped Role claim
        when(appConfig.getRoleClaim()).thenReturn("IDP_ROLE_CLAIM");
        roleClaim = sequenceHandler.getSpRoleClaimUri(appConfig);
        Assert.assertEquals(roleClaim, "IDP_ROLE_CLAIM");

        // No IDP mapped Role claim or SP mapped Role claim available
        reset(appConfig);
        when(appConfig.getRoleClaim()).thenReturn(null);
        // Get the default role claim URI
        roleClaim = sequenceHandler.getSpRoleClaimUri(appConfig);
        Assert.assertEquals(roleClaim, FrameworkConstants.LOCAL_ROLE_CLAIM_URI);
    }

    /*
        Get role claim from SP mapped roles
     */
    @Test
    public void testGetSpRoleClaimUriSpMappedClaim() throws Exception {

        String roleClaim;
        ApplicationConfig appConfig = mock(ApplicationConfig.class);

        // SP mapped role claim
        reset(appConfig);
        Map<String, String> claimMappings = new HashMap<>();
        claimMappings.put("SP_ROLE_CLAIM", FrameworkConstants.LOCAL_ROLE_CLAIM_URI);
        when(appConfig.getClaimMappings()).thenReturn(claimMappings);
        roleClaim = sequenceHandler.getSpRoleClaimUri(appConfig);
        Assert.assertEquals(roleClaim, "SP_ROLE_CLAIM");

        // Role not among SP mapped claims
        reset(appConfig);
        claimMappings = new HashMap<>();
        claimMappings.put("SP_MAPPED_CLAIM", "DUMMY");
        when(appConfig.getClaimMappings()).thenReturn(claimMappings);
        roleClaim = sequenceHandler.getSpRoleClaimUri(appConfig);
        Assert.assertEquals(roleClaim, FrameworkConstants.LOCAL_ROLE_CLAIM_URI);

        // No IDP mapped Role claim or SP mapped Role claim available
        reset(appConfig);
        when(appConfig.getClaimMappings()).thenReturn(new HashMap<String, String>());
        // Get the default role claim URI
        roleClaim = sequenceHandler.getSpRoleClaimUri(appConfig);
        Assert.assertEquals(roleClaim, FrameworkConstants.LOCAL_ROLE_CLAIM_URI);

        reset(appConfig);
        when(appConfig.getClaimMappings()).thenReturn(null);
        // Get the default role claim URI
        roleClaim = sequenceHandler.getSpRoleClaimUri(appConfig);
        Assert.assertEquals(roleClaim, FrameworkConstants.LOCAL_ROLE_CLAIM_URI);
    }


    @Test
    public void testHandleClaimMappingsErrorFlow() throws Exception {

        ClaimHandler claimHandler = mock(ClaimHandler.class);
        doThrow(new FrameworkException("ERROR")).when(claimHandler).handleClaimMappings(any(StepConfig.class),
                any(AuthenticationContext.class), any(Map.class), anyBoolean());

        mockStatic(FrameworkUtils.class);
        when(FrameworkUtils.getClaimHandler()).thenReturn(claimHandler);

        Map<String, String> claims = sequenceHandler.handleClaimMappings(new AuthenticationContext());
        Assert.assertNull(claims);
    }

    @Test
    public void testHandleClaimMappings() throws Exception {

        ClaimHandler claimHandler = mock(ClaimHandler.class);
        Map<String, String> claims = new HashMap<>();
        claims.put("claim1", "value1");

        doReturn(claims).when(claimHandler).handleClaimMappings(any(StepConfig.class), any(AuthenticationContext.class),
                any(Map.class), anyBoolean());

        mockStatic(FrameworkUtils.class);
        when(FrameworkUtils.getClaimHandler()).thenReturn(claimHandler);

        claims = sequenceHandler.handleClaimMappings(new AuthenticationContext());
        Assert.assertNotNull(claims);
    }
}
