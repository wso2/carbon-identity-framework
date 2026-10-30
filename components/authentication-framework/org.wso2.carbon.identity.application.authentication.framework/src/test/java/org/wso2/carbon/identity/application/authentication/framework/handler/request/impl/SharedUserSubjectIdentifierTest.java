/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ApplicationConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.PostAuthnHandlerFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.ServiceProviderProperty;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Unit tests for the shared user subject identifier handling in PostAuthenticatedSubjectIdentifierHandler.
 */
public class SharedUserSubjectIdentifierTest {

    private static final String RESIDENT_USER_ID = "2a160d20-9bb2-41ed-8ecb-44e99d581963";
    private static final String SHARED_USER_ID = "f411c2c0-5437-4088-bec2-adf562ebe26a";
    private static final String RESIDENT_ORG_ID = "10084a8d-113f-4211-a0d5-efe36b082211";
    private static final String ACCESSING_ORG_ID = "2d4e28f5-9bbc-4e2d-b880-426b272708c7";
    private static final String RESIDENT_TENANT_DOMAIN = "carbon.super";
    private static final String ACCESSING_TENANT_DOMAIN = "suborga";
    private static final String USERNAME_CLAIM = "http://wso2.org/claims/username";

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private AuthenticationContext context;
    @Mock
    private SequenceConfig sequenceConfig;
    @Mock
    private ApplicationConfig applicationConfig;

    private AutoCloseable closeable;
    private MockedStatic<FrameworkUtils> frameworkUtils;
    private MockedStatic<IdentityUtil> identityUtil;
    private AuthenticatedUser authenticatedUser;

    @BeforeMethod
    public void setUp() {

        closeable = MockitoAnnotations.openMocks(this);
        authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setUserId(RESIDENT_USER_ID);
        authenticatedUser.setUserName("alice");
        authenticatedUser.setTenantDomain(RESIDENT_TENANT_DOMAIN);
        authenticatedUser.setUserStoreDomain("PRIMARY");
        authenticatedUser.setSharedUser(true);
        authenticatedUser.setUserResidentOrganization(RESIDENT_ORG_ID);
        authenticatedUser.setAccessingOrganization(ACCESSING_ORG_ID);

        when(context.getSequenceConfig()).thenReturn(sequenceConfig);
        when(context.getTenantDomain()).thenReturn(ACCESSING_TENANT_DOMAIN);
        when(sequenceConfig.getApplicationConfig()).thenReturn(applicationConfig);
        when(sequenceConfig.getAuthenticatedUser()).thenReturn(authenticatedUser);

        frameworkUtils = mockStatic(FrameworkUtils.class);
        frameworkUtils.when(() -> FrameworkUtils.isStepBasedSequenceHandlerExecuted(context)).thenReturn(true);
        frameworkUtils.when(() -> FrameworkUtils.resolveSharedUserIdOfAccessingOrganization(any()))
                .thenReturn(SHARED_USER_ID);
        identityUtil = mockStatic(IdentityUtil.class);
    }

    @AfterMethod
    public void tearDown() throws Exception {

        frameworkUtils.close();
        identityUtil.close();
        closeable.close();
    }

    @DataProvider(name = "subjectConfigProvider")
    public Object[][] subjectConfigProvider() {

        // subjectClaimUri, subjectClaimValue, useUserIdForDefaultSubject, includeTenantDomain, settingEnabled,
        // expected subject.
        return new Object[][]{
                {FrameworkConstants.USER_ID_CLAIM, RESIDENT_USER_ID, false, false, true, SHARED_USER_ID},
                {FrameworkConstants.USER_ID_CLAIM, RESIDENT_USER_ID, false, false, false, RESIDENT_USER_ID},
                {null, null, true, false, true, SHARED_USER_ID},
                {null, null, true, false, false, RESIDENT_USER_ID},
                {FrameworkConstants.USER_ID_CLAIM, RESIDENT_USER_ID, false, true, true,
                        SHARED_USER_ID + "@" + RESIDENT_TENANT_DOMAIN},
                {null, null, true, true, true, SHARED_USER_ID + "@" + RESIDENT_TENANT_DOMAIN},
        };
    }

    @Test(dataProvider = "subjectConfigProvider")
    public void testSubjectIdentifierOfSharedUser(String subjectClaimUri, String subjectClaimValue,
                                                  boolean useUserIdForDefaultSubject, boolean includeTenantDomain,
                                                  boolean settingEnabled, String expectedSubject) throws Exception {

        when(applicationConfig.getSubjectClaimUri()).thenReturn(subjectClaimUri);
        when(context.getProperty(FrameworkConstants.SERVICE_PROVIDER_SUBJECT_CLAIM_VALUE))
                .thenReturn(subjectClaimValue);
        when(applicationConfig.isUseUserIdForDefaultSubject()).thenReturn(useUserIdForDefaultSubject);
        when(applicationConfig.isUseTenantDomainInLocalSubjectIdentifier()).thenReturn(includeTenantDomain);
        frameworkUtils.when(() -> FrameworkUtils.isSharedUserIdAsSubjectEnabled(ACCESSING_TENANT_DOMAIN))
                .thenReturn(settingEnabled);

        PostAuthnHandlerFlowStatus status =
                PostAuthenticatedSubjectIdentifierHandler.getInstance().handle(request, response, context);

        assertEquals(status, PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED);
        assertEquals(authenticatedUser.getAuthenticatedSubjectIdentifier(), expectedSubject);
        // The authenticated user keeps the resident user details.
        assertEquals(authenticatedUser.getUserId(), RESIDENT_USER_ID);
        assertEquals(authenticatedUser.getUserResidentOrganization(), RESIDENT_ORG_ID);
    }

    @Test
    public void testSubjectIdentifierWhenUserIsNotShared() {

        authenticatedUser.setSharedUser(false);
        mockUserIdSubjectClaim();

        PostAuthenticatedSubjectIdentifierHandler.getInstance().handle(request, response, context);

        assertEquals(authenticatedUser.getAuthenticatedSubjectIdentifier(), RESIDENT_USER_ID);
        frameworkUtils.verify(() -> FrameworkUtils.isSharedUserIdAsSubjectEnabled(any()), never());
    }

    @Test
    public void testSubjectIdentifierWhenUserIsFederated() {

        authenticatedUser.setFederatedUser(true);
        mockUserIdSubjectClaim();

        PostAuthenticatedSubjectIdentifierHandler.getInstance().handle(request, response, context);

        assertEquals(authenticatedUser.getAuthenticatedSubjectIdentifier(), RESIDENT_USER_ID);
    }

    @Test
    public void testSubjectIdentifierWhenUserAccessesResidentOrganization() {

        authenticatedUser.setAccessingOrganization(RESIDENT_ORG_ID);
        mockUserIdSubjectClaim();

        PostAuthenticatedSubjectIdentifierHandler.getInstance().handle(request, response, context);

        assertEquals(authenticatedUser.getAuthenticatedSubjectIdentifier(), RESIDENT_USER_ID);
    }

    @Test
    public void testSubjectIdentifierWhenSharedUserIdIsNotResolved() {

        mockUserIdSubjectClaim();
        frameworkUtils.when(() -> FrameworkUtils.resolveSharedUserIdOfAccessingOrganization(any()))
                .thenReturn(null);

        PostAuthenticatedSubjectIdentifierHandler.getInstance().handle(request, response, context);

        assertEquals(authenticatedUser.getAuthenticatedSubjectIdentifier(), RESIDENT_USER_ID);
    }

    @Test
    public void testSubjectIdentifierWhenSubjectClaimIsNotUserId() {

        when(applicationConfig.getSubjectClaimUri()).thenReturn(USERNAME_CLAIM);
        when(context.getProperty(FrameworkConstants.SERVICE_PROVIDER_SUBJECT_CLAIM_VALUE)).thenReturn("alice");
        frameworkUtils.when(() -> FrameworkUtils.isSharedUserIdAsSubjectEnabled(ACCESSING_TENANT_DOMAIN))
                .thenReturn(true);

        PostAuthenticatedSubjectIdentifierHandler.getInstance().handle(request, response, context);

        assertEquals(authenticatedUser.getAuthenticatedSubjectIdentifier(), "alice");
    }

    @Test
    public void testSubjectIdentifierForFragmentApplication() {

        ServiceProviderProperty fragmentAppProperty = new ServiceProviderProperty();
        fragmentAppProperty.setName("isFragmentApp");
        fragmentAppProperty.setValue("true");
        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setSpProperties(new ServiceProviderProperty[]{fragmentAppProperty});
        when(applicationConfig.getServiceProvider()).thenReturn(serviceProvider);
        mockUserIdSubjectClaim();

        PostAuthenticatedSubjectIdentifierHandler.getInstance().handle(request, response, context);

        assertEquals(authenticatedUser.getAuthenticatedSubjectIdentifier(), RESIDENT_USER_ID);
        frameworkUtils.verify(() -> FrameworkUtils.isSharedUserIdAsSubjectEnabled(any()), never());
    }

    private void mockUserIdSubjectClaim() {

        when(applicationConfig.getSubjectClaimUri()).thenReturn(FrameworkConstants.USER_ID_CLAIM);
        when(context.getProperty(FrameworkConstants.SERVICE_PROVIDER_SUBJECT_CLAIM_VALUE))
                .thenReturn(RESIDENT_USER_ID);
        frameworkUtils.when(() -> FrameworkUtils.isSharedUserIdAsSubjectEnabled(ACCESSING_TENANT_DOMAIN))
                .thenReturn(true);
    }
}
