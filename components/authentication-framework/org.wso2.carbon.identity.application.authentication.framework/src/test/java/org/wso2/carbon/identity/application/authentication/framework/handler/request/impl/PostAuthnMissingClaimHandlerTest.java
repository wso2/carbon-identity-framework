/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.wso2.carbon.identity.application.authentication.framework.handler.request.impl;

import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ApplicationConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.PostAuthnHandlerFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.claim.metadata.mgt.model.AttributeMapping;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.core.ServiceURL;
import org.wso2.carbon.identity.core.ServiceURLBuilder;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.POST_AUTHENTICATION_REDIRECTION_TRIGGERED;

/**
 * This is a test class for {@link PostAuthnMissingClaimHandler}.
 */
public class PostAuthnMissingClaimHandlerTest extends PostAuthnMissingClaimHandler {

    private static final AuthenticationContext context = new AuthenticationContext();
    private static final SequenceConfig sequenceConfig = mock(SequenceConfig.class);
    private static final ApplicationConfig applicationConfig = mock(ApplicationConfig.class);
    private static final HttpServletRequest request = mock(HttpServletRequest.class);
    private static final HttpServletResponse response = mock(HttpServletResponse.class);
    private static final RealmService realmService = mock(RealmService.class);
    private static final TenantManager tenantManager = mock(TenantManager.class);
    private static final UserRealm userRealm = mock(UserRealm.class);
    private static final AbstractUserStoreManager userStoreManager = mock(AbstractUserStoreManager.class);

    private static final String MOBILE_CLAIM = "http://wso2.org/claims/mobile";
    private static final String MOBILE_NUMBER = "+123456789";
    private static final String REDIRECT_URL = "https://localhost:9443/dummmy";

    @SuppressWarnings("checkstyle:LocalVariableName")
    @Test(description = "This test case tests the related display names for mandatory missing claims are derived")
    public void testCorrectDisplayNamesDeriveForMissingClaims()
            throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {

        List<AttributeMapping> mappedAttributes = new ArrayList<>();

        Map<String, String> localClaimProperties = new HashMap<>();
        localClaimProperties.put("Description", "Local");
        localClaimProperties.put("DisplayName", "Local");

        Map<String, String> localityClaimProperties = new HashMap<>();
        localityClaimProperties.put("Description", "Locality");
        localityClaimProperties.put("DisplayName", "Locality");

        Map<String, String> secretKeyClaimProperties = new HashMap<>();
        secretKeyClaimProperties.put("Description", "Claim to store the secret key");
        secretKeyClaimProperties.put("DisplayName", "Secret Key");

        Map<String, String> countryClaimProperties = new HashMap<>();
        countryClaimProperties.put("Description", "Country");
        countryClaimProperties.put("DisplayName", "Country");

        Map<String, String> verifyEmailClaimProperties = new HashMap<>();
        verifyEmailClaimProperties.put("Description", "Temporary claim to invoke email verified feature");
        verifyEmailClaimProperties.put("DisplayName", "Verify Email");

        List<LocalClaim> localClaims = new ArrayList<>();
        LocalClaim localClaim = new LocalClaim("http://wso2.org/claims/local", mappedAttributes, localClaimProperties);
        LocalClaim localClaim2 = new
                LocalClaim("http://wso2.org/claims/locality", mappedAttributes, localityClaimProperties);
        LocalClaim localClaim3 = new
                LocalClaim("http://wso2.org/claims/identity/secretkey", mappedAttributes, secretKeyClaimProperties);
        LocalClaim localClaim4 = new
                LocalClaim("http://wso2.org/claims/country", mappedAttributes, countryClaimProperties);
        LocalClaim localClaim5 = new
                LocalClaim("http://wso2.org/claims/identity/verifyEmail", mappedAttributes, verifyEmailClaimProperties);
        localClaims.add(localClaim);
        localClaims.add(localClaim2);
        localClaims.add(localClaim3);
        localClaims.add(localClaim4);
        localClaims.add(localClaim5);

        Map<String, String> missingClaimMap = new HashMap<>();
        missingClaimMap.put("http://wso2.org/claims/local", "http://wso2.org/claims/local");
        missingClaimMap.put("http://wso2.org/claims/country", "http://wso2.org/claims/country");
        missingClaimMap.put("http://wso2.org/claims/locality", "http://wso2.org/claims/locality");

        String relatedDisplayNames = "http://wso2.org/claims/local|Local,http://wso2.org/claims/country|Country," +
                "http://wso2.org/claims/locality|Locality";

        Class<PostAuthnMissingClaimHandler> claimDisplay = PostAuthnMissingClaimHandler.class;
        Object obj = claimDisplay.newInstance();
        Method displayName = claimDisplay.
                getDeclaredMethod("getMissingClaimsDisplayNames", Map.class, List.class);
        displayName.setAccessible(true);
        String returnedDisplayNames = (String) displayName.invoke(obj, missingClaimMap, localClaims);
        assertEquals(returnedDisplayNames, relatedDisplayNames);
    }

    @Test(description = "Handle missing mobile claim response when verification on update is enabled.")
    public void handleMissingMobileClaimResponseWithVerificationOnUpdateEnabled() throws Exception {

        buildAuthenticationContext();
        MockedStatic<ServiceURLBuilder> serviceUrlBuilder = mockServiceUrlBuilder();

        Map<String, String[]> requestParameters = new HashMap<>();
        requestParameters.put("claim_mand_" + MOBILE_CLAIM, new String[]{MOBILE_NUMBER});
        requestParameters.put("sessionDataKey", new String[]{"76049b83-cf23-4aa7-b88b-75d3b0374e1e"});
        when(request.getParameterMap()).thenReturn(requestParameters);

        MockedStatic<LoggerUtils> loggerUtils = mockStatic(LoggerUtils.class);
        loggerUtils.when(LoggerUtils::isDiagnosticLogsEnabled).thenReturn(false);

        when(userRealm.getUserStoreManager()).thenReturn(userStoreManager);
        when(realmService.getTenantManager()).thenReturn(tenantManager);
        when(tenantManager.getTenantId("carbon.super")).thenReturn(1);
        when(realmService.getTenantUserRealm(1)).thenReturn(userRealm);
        FrameworkServiceDataHolder.getInstance().setRealmService(realmService);
        doNothing().when(userStoreManager).setUserClaimValuesWithID(any(), any(), any());
        IdentityUtil.threadLocalProperties.get().put(
                FrameworkConstants.CLAIM_FOR_PENDING_OTP_VERIFICATION, MOBILE_CLAIM);

        PostAuthnMissingClaimHandler.getInstance().handlePostAuthenticationForMissingClaimsResponse(
                request, response, context);

        Assert.assertNull(IdentityUtil.threadLocalProperties.get().get(
                FrameworkConstants.CLAIM_FOR_PENDING_OTP_VERIFICATION));
        Assert.assertEquals(context.getProperty(FrameworkConstants.OTP_VERIFICATION_PENDING_CLAIM).toString(),
                String.format("{uri=%s, value=%s}", MOBILE_CLAIM, MOBILE_NUMBER));
        verify(response).sendRedirect(REDIRECT_URL);

        loggerUtils.close();
        serviceUrlBuilder.close();
    }

    @Test(description = "Handle missing mobile claim response when after successful verification.",
            dependsOnMethods = {"handleMissingMobileClaimResponseWithVerificationOnUpdateEnabled"})
    public void handleMissingMobileClaimResponseAfterVerification() throws Exception {

        context.setProperty(POST_AUTHENTICATION_REDIRECTION_TRIGGERED, true);
        when(userStoreManager.getUserClaimValueWithID(any(), any(), any())).thenReturn(MOBILE_NUMBER);

        PostAuthnHandlerFlowStatus status = PostAuthnMissingClaimHandler.getInstance().handle(
                request, response, context);

        Assert.assertEquals(status, PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED);
    }

    private AuthenticatedUser buildAuthenticatedUser() throws Exception {

        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setUserStoreDomain("PRIMARY");
        authenticatedUser.setTenantDomain("carbon.super");
        authenticatedUser.setUserName("testuser");
        authenticatedUser.setUserId("f9fef46c-2fad-499b-bd9b-f31323f16767");
        authenticatedUser.setAuthenticatedSubjectIdentifier(authenticatedUser.getUserId());
        return authenticatedUser;
    }

    private void buildAuthenticationContext() throws Exception {

        AuthenticatedUser authenticatedUser = buildAuthenticatedUser();

        Map<Integer, StepConfig> stepMap = new HashMap<>();
        StepConfig stepConfig = new StepConfig();
        stepConfig.setAuthenticatedUser(authenticatedUser);
        stepConfig.setSubjectAttributeStep(true);
        stepMap.put(1, stepConfig);

        context.setSequenceConfig(sequenceConfig);
        when(sequenceConfig.getAuthenticatedUser()).thenReturn(authenticatedUser);
        when(sequenceConfig.getStepMap()).thenReturn(stepMap);
        when(sequenceConfig.getApplicationConfig()).thenReturn(applicationConfig);
        when(applicationConfig.getClaimMappings()).thenReturn(new HashMap<>());
    }

    private MockedStatic<ServiceURLBuilder> mockServiceUrlBuilder() throws Exception {

        ServiceURL serviceURL = mock(ServiceURL.class);
        when(serviceURL.getAbsolutePublicURL()).thenReturn(REDIRECT_URL);
        MockedStatic<ServiceURLBuilder> mockedServiceURLBuilder = mockStatic(ServiceURLBuilder.class);
        ServiceURLBuilder mockBuilder = mock(ServiceURLBuilder.class);
        mockedServiceURLBuilder.when(ServiceURLBuilder::create).thenReturn(mockBuilder);
        when(mockBuilder.addPath(any())).thenReturn(mockBuilder);
        when(mockBuilder.addParameter(any(), any())).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(serviceURL);

        return mockedServiceURLBuilder;
    }
}
