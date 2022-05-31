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

import org.mockito.Mock;
import org.mockito.Spy;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.IObjectFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
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
import org.wso2.carbon.identity.application.authentication.framwork.test.utils.CommonTestUtils;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ApplicationMgtSystemConfig;
import org.wso2.carbon.identity.application.mgt.dao.ApplicationDAO;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.carbon.identity.core.util.IdentityUtil.getLocalGroupsClaimURI;

@PrepareForTest({FrameworkUtils.class, ApplicationMgtSystemConfig.class, IdentityTenantUtil.class, IdentityUtil.class})
@PowerMockIgnore("org.mockito.*")
public class DefaultRequestPathBasedSequenceHandlerTest {

    private static final String SUBJECT_CLAIM_URI = "subjectClaimUri";

    private DefaultRequestPathBasedSequenceHandler requestPathBasedSequenceHandler;

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @Mock
    ApplicationDAO applicationDAO;

    @Mock
    private ApplicationMgtSystemConfig applicationMgtSystemConfig;

    @Mock
    RequestPathApplicationAuthenticator requestPathAuthenticator;

    @Mock
    private RealmService mockRealmService;

    @Mock
    private RealmConfiguration mockRealmConfiguration;

    @Spy
    private SequenceConfig sequenceConfig;

    @Spy
    private AuthenticatorConfig authenticatorConfig;

    private AuthenticationContext context;

    @BeforeClass
    public void setUp() throws Exception {

        initMocks(this);
        requestPathBasedSequenceHandler = new DefaultRequestPathBasedSequenceHandler();
        // Mock authentication context and sequence config for request path authentication
        context = new AuthenticationContext();

        doReturn(requestPathAuthenticator).when(authenticatorConfig).getApplicationAuthenticator();

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

        CommonTestUtils.testSingleton(
                DefaultRequestPathBasedSequenceHandler.getInstance(),
                DefaultRequestPathBasedSequenceHandler.getInstance()
        );
    }

    /*
        None of the available request path authenticators can handle the request.
     */
    @Test
    public void testHandleNoneCanHandle() throws Exception {

        // mock the behaviour of the request path authenticator
        when(requestPathAuthenticator.canHandle(any(HttpServletRequest.class))).thenReturn(false);
        requestPathBasedSequenceHandler.handle(request, response, context);
        assertEquals(context.getSequenceConfig().isCompleted(), true);
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

        requestPathBasedSequenceHandler.handle(request, response, context);
        assertEquals(context.isRequestAuthenticated(), false);
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

        requestPathBasedSequenceHandler.handle(request, response, context);
        assertEquals(context.isRequestAuthenticated(), false);
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

        requestPathBasedSequenceHandler.handle(request, response, context);
    }

    /*
        Request path authenticator can handle the request successfully and authentication succeeds
     */
    @Test
    public void testHandleAuthSuccess() throws Exception {

        // mock the behaviour of the request path authenticator
        when(requestPathAuthenticator.canHandle(any(HttpServletRequest.class))).thenReturn(true);
        doReturn(AuthenticatorFlowStatus.SUCCESS_COMPLETED).when(requestPathAuthenticator)
                .process(any(HttpServletRequest.class), any(HttpServletResponse.class),
                        any(AuthenticationContext.class));

        String subjectIdentifier = "H2/alice@t1.com";
        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setAuthenticatedSubjectIdentifier(subjectIdentifier);
        authenticatedUser.setFederatedUser(false);

        context.setSubject(authenticatedUser);

        mockStatic(FrameworkUtils.class);
        when(FrameworkUtils.getMultiAttributeSeparator()).thenReturn(",");

        requestPathBasedSequenceHandler = spy(new DefaultRequestPathBasedSequenceHandler());
        // mock triggering post authentication
        doNothing().when(requestPathBasedSequenceHandler).handlePostAuthentication(any(HttpServletRequest.class), any
                (HttpServletResponse.class), any(AuthenticationContext.class), any(AuthenticatedIdPData.class));

        requestPathBasedSequenceHandler.handle(request, response, context);

        assertEquals(context.getSequenceConfig().isCompleted(), true);
        assertNotNull(context.getCurrentAuthenticatedIdPs());
        assertEquals(context.getCurrentAuthenticatedIdPs().size(), 1);

        AuthenticatedIdPData authenticatedIdPData = context.getCurrentAuthenticatedIdPs()
                .get(FrameworkConstants.LOCAL_IDP_NAME);

        assertNotNull(authenticatedIdPData);
        assertEquals(authenticatedIdPData.getIdpName(), FrameworkConstants.LOCAL_IDP_NAME);
        assertNotNull(authenticatedIdPData.getUser());
        assertEquals(authenticatedIdPData.getUser().getAuthenticatedSubjectIdentifier(), subjectIdentifier);
        assertEquals(authenticatedIdPData.getAuthenticator(), authenticatorConfig);
    }

    @DataProvider
    public Object[][] getPostAuthenticationData() {

        Util.mockIdentityUtil();

        return new Object[][]{
                // unfiltered local claims | mapped attributes | subjectClaimUri
                {
                        // No subject claim URI defined
                        null,
                        new HashMap<>(),
                        null,
                        null
                },
                {
                        // Subject claim URI defined. But we can't find the subject claim in unfiltered or mapped claims
                        null,
                        new HashMap<>(),
                        SUBJECT_CLAIM_URI,
                        null
                },
                {
                        // Subject claim found in unfiltered claims
                        new HashMap<String, String>() {{
                            put(SUBJECT_CLAIM_URI, "unfiltered_claim_subject");
                        }},
                        new HashMap<Object, Object>() {
                            {
                                put(getLocalGroupsClaimURI(), "localRole1,localRole2");
                            }
                        },
                        SUBJECT_CLAIM_URI,
                        "unfiltered_claim_subject"

                },
                {
                        // Unfiltered claims available. But subject claim is not among them
                        new HashMap<String, String>() {{
                            put("local_claim_uri", "local_claim");
                        }},
                        new HashMap<Object, Object>() {
                            {
                                put(getLocalGroupsClaimURI(), "localRole1,localRole2");
                                put(SUBJECT_CLAIM_URI, "mapped_attribute_claim_subject");
                            }
                        }, SUBJECT_CLAIM_URI,
                        null
                },
                {
                        // Unfiltered claims not available. Pick subject claim from mapped attributes.
                        null,
                        new HashMap<Object, Object>() {
                            {
                                put(getLocalGroupsClaimURI(), "localRole1,localRole2");
                                put(SUBJECT_CLAIM_URI, "mapped_attribute_claim_subject");
                            }
                        }, SUBJECT_CLAIM_URI,
                        "mapped_attribute_claim_subject"
                }

        };
    }

    @Test(dataProvider = "getPostAuthenticationData")
    public void testHandlePostAuthentication(Map<String, String> unfilteredLocalClaims,
                                             Map<String, String> mappedAttributes,
                                             String subjectClaimUri,
                                             String expectedSubjectIdentifier) throws Exception {

        Util.mockIdentityUtil();
        requestPathBasedSequenceHandler = spy(new DefaultRequestPathBasedSequenceHandler());
        doReturn(mappedAttributes)
                .when(requestPathBasedSequenceHandler)
                .handleClaimMappings(any(AuthenticationContext.class));

        doReturn("spRole1,spRole2")
                .when(requestPathBasedSequenceHandler)
                .getServiceProviderMappedUserRoles(any(SequenceConfig.class), anyList());

        ServiceProvider serviceProvider = new ServiceProvider();
        ApplicationConfig applicationConfig = spy(new ApplicationConfig(serviceProvider, SUPER_TENANT_DOMAIN_NAME));
        when(applicationConfig.getSubjectClaimUri()).thenReturn(subjectClaimUri);

        SequenceConfig sequenceConfig = new SequenceConfig();
        sequenceConfig.setApplicationConfig(applicationConfig);

        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setUserName("alice");

        sequenceConfig.setAuthenticatedUser(new AuthenticatedUser());

        AuthenticationContext context = new AuthenticationContext();
        context.setProperty(FrameworkConstants.UNFILTERED_LOCAL_CLAIM_VALUES, unfilteredLocalClaims);
        context.setSequenceConfig(sequenceConfig);

        ApplicationAuthenticator applicationAuthenticator = mock(ApplicationAuthenticator.class);
        when(applicationAuthenticator.getName()).thenReturn("Authenticator1");

        AuthenticatorConfig authenticatorConfig = new AuthenticatorConfig();
        authenticatorConfig.setApplicationAuthenticator(applicationAuthenticator);

        AuthenticatedIdPData idPData = new AuthenticatedIdPData();
        idPData.setIdpName("LOCAL");

        idPData.setAuthenticator(authenticatorConfig);

        mockStatic(FrameworkUtils.class);
        when(FrameworkUtils.getMultiAttributeSeparator()).thenReturn(",");

        requestPathBasedSequenceHandler.handlePostAuthentication(request, response, context, idPData);

        assertNotNull(context.getSequenceConfig().getAuthenticatedUser());
        assertEquals(context.getSequenceConfig().getAuthenticatedUser().getAuthenticatedSubjectIdentifier(),
                expectedSubjectIdentifier);
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
        mockStatic(ApplicationMgtSystemConfig.class);
        mockStatic(IdentityTenantUtil.class);
        when(ApplicationMgtSystemConfig.getInstance()).thenReturn(applicationMgtSystemConfig);
        when(applicationMgtSystemConfig.getApplicationDAO()).thenReturn(applicationDAO);
        when(IdentityTenantUtil.getRealmService()).thenReturn(mockRealmService);
        when(mockRealmService.getBootstrapRealmConfiguration()).thenReturn(mockRealmConfiguration);
        String mappedRoles = requestPathBasedSequenceHandler
                .getServiceProviderMappedUserRoles(sequenceConfig, localUserRoles);
        assertEquals(mappedRoles, expectedRoles);
    }

    @DataProvider(name = "spRoleClaimUriProvider")
    private Object[][] getSpRoleClaimUriData() {

        Util.mockIdentityUtil();
        return new Object[][]{
                {"SP_ROLE_CLAIM", "SP_ROLE_CLAIM"},
                {null, getLocalGroupsClaimURI()}
        };
    }

    /*
        Get Service Provider mapped role claim URI
     */
    @Test(dataProvider = "spRoleClaimUriProvider")
    public void testGetSpRoleClaimUri(String spRoleClaimUri,
                                      String expectedRoleClaimUri) throws Exception {

        Util.mockIdentityUtil();
        ApplicationConfig appConfig = mock(ApplicationConfig.class);
        when(appConfig.getRoleClaim()).thenReturn(spRoleClaimUri);
        assertEquals(requestPathBasedSequenceHandler.getSpRoleClaimUri(appConfig), expectedRoleClaimUri);
    }

    @DataProvider(name = "spClaimMappingProvider")
    public Object[][] getSpClaimMappingProvider() {

        Util.mockIdentityUtil();
        return new Object[][]{
                {       // SP mapped role claim
                        new HashMap<String, String>() {{
                            put("SP_ROLE_CLAIM", getLocalGroupsClaimURI());
                        }},
                        "SP_ROLE_CLAIM"
                },
                {       // Role claim not among SP mapped claims
                        new HashMap<String, String>() {{
                            put("SP_CLAIM", "LOCAL_CLAIM");
                        }},
                        getLocalGroupsClaimURI()
                },
                {      // No SP mapped claims
                        new HashMap<>(), getLocalGroupsClaimURI()
                },
                {
                        null, getLocalGroupsClaimURI()
                }
        };
    }

    /*
        Get role claim URI from SP mapped claims
     */
    @Test(dataProvider = "spClaimMappingProvider")
    public void testGetSpRoleClaimUriSpMappedClaim(Map<String, String> claimMappings,
                                                   String expectedRoleClaim) throws Exception {

        Util.mockIdentityUtil();
        ApplicationConfig appConfig = mock(ApplicationConfig.class);
        when(appConfig.getClaimMappings()).thenReturn(claimMappings);
        String roleClaim = requestPathBasedSequenceHandler.getSpRoleClaimUri(appConfig);
        assertEquals(roleClaim, expectedRoleClaim);
    }

    @Test
    public void testHandleClaimMappingsErrorFlow() throws Exception {

        ClaimHandler claimHandler = mock(ClaimHandler.class);
        doThrow(new FrameworkException("ERROR")).when(claimHandler).handleClaimMappings(any(StepConfig.class),
                any(AuthenticationContext.class), any(Map.class), anyBoolean());

        mockStatic(FrameworkUtils.class);
        when(FrameworkUtils.getClaimHandler()).thenReturn(claimHandler);

        Map<String, String> claims = requestPathBasedSequenceHandler.handleClaimMappings(new AuthenticationContext());
        assertNotNull(claims);
        assertEquals(claims.size(), 0);
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

        claims = requestPathBasedSequenceHandler.handleClaimMappings(new AuthenticationContext());
        assertNotNull(claims);
    }

    @Test
    public void testHandleClaimMappingsFailed() throws Exception {

        ClaimHandler claimHandler = mock(ClaimHandler.class);
        doThrow(new FrameworkException("Claim Handling failed"))
                .when(claimHandler)
                .handleClaimMappings(any(StepConfig.class), any(AuthenticationContext.class),
                        any(Map.class), anyBoolean());

        mockStatic(FrameworkUtils.class);
        when(FrameworkUtils.getClaimHandler()).thenReturn(claimHandler);

        Map<String, String> claims = requestPathBasedSequenceHandler.handleClaimMappings(new AuthenticationContext());
        assertNotNull(claims);
        assertEquals(claims.size(), 0);
    }
}
