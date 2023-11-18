/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.DefaultRequestCoordinator;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatorData;
import org.wso2.carbon.identity.application.authentication.framework.model.auth.service.AuthServiceErrorInfo;
import org.wso2.carbon.identity.application.authentication.framework.model.auth.service.AuthServiceRequest;
import org.wso2.carbon.identity.application.authentication.framework.model.auth.service.AuthServiceResponse;
import org.wso2.carbon.identity.application.authentication.framework.model.auth.service.AuthServiceResponseData;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.authentication.framework.util.auth.service.AuthServiceConstants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Unit tests for {@link AuthenticationService}.
 */
@PrepareForTest({FrameworkUtils.class, ConfigurationFacade.class})
@PowerMockIgnore({"org.mockito.*"})
public class AuthenticationServiceTest extends AbstractFrameworkTest {

    private static final Log log = LogFactory.getLog(AuthenticationServiceTest.class);
    private static final String MULTI_OPS_AUTHENTICATORS = "OpenIDConnectAuthenticator:google:google_myaccount;" +
            "BasicAuthenticator:LOCAL;FIDOAuthenticator:LOCAL";
    private static final String SINGLE_AUTHENTICATOR = "BasicAuthenticator:LOCAL";
    private static final String SESSION_DATA_KEY = "4458306a-5e77-497f-be67-8ccbec6ff6d0";
    private static final String FINAL_SESSION_DATA_KEY = "bcf793dc-4ea9-4324-a485-7d20999a063e";
    private static final String LOCATION_HEADER = "location";
    private static final String ERROR_MSG_LOGIN_FAIL = "login.fail.message";

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @BeforeMethod
    public void init() throws IOException {

        MockitoAnnotations.initMocks(this);
        mockStatic(ConfigurationFacade.class);
        ConfigurationFacade configurationFacade = mock(ConfigurationFacade.class);
        PowerMockito.when(ConfigurationFacade.getInstance()).thenReturn(configurationFacade);

        mockStatic(FrameworkUtils.class);
        DefaultRequestCoordinator defaultRequestCoordinator = mock(DefaultRequestCoordinator.class);
        PowerMockito.when(FrameworkUtils.getRequestCoordinator()).thenReturn(defaultRequestCoordinator);
        PowerMockito.when(FrameworkUtils.getMaxInactiveInterval()).thenReturn(-1);

        doNothing().when(defaultRequestCoordinator).handle(request, response);
    }

    @DataProvider(name = "authProvider")
    public Object[][] authProvider() {

        // boolean isMultiOpsResponse, String redirectUrl, Object authenticatorFlowStatus, Object authServiceFlowStatus,
        // String sessionDataKey, String authenticatorList
        return new Object[][]{
                {true, getIntermediateRedirectUrl(SESSION_DATA_KEY, MULTI_OPS_AUTHENTICATORS),
                        AuthenticatorFlowStatus.INCOMPLETE, AuthServiceConstants.FlowStatus.INCOMPLETE,
                        SESSION_DATA_KEY, MULTI_OPS_AUTHENTICATORS},
                {false, getIntermediateRedirectUrl(SESSION_DATA_KEY, SINGLE_AUTHENTICATOR),
                        AuthenticatorFlowStatus.INCOMPLETE, AuthServiceConstants.FlowStatus.INCOMPLETE,
                        SESSION_DATA_KEY, SINGLE_AUTHENTICATOR},
                {false, getFinalRedirectUrl(FINAL_SESSION_DATA_KEY),
                        AuthenticatorFlowStatus.SUCCESS_COMPLETED, AuthServiceConstants.FlowStatus.SUCCESS_COMPLETED,
                        FINAL_SESSION_DATA_KEY, StringUtils.EMPTY}
        };
    }

    @Test(dataProvider = "authProvider")
    public void testHandleAuthentication(boolean isMultiOpsResponse, String redirectUrl,
                                         Object authenticatorFlowStatus,
                                         Object authServiceFlowStatus, String sessionDataKey,
                                         String authenticatorList) throws Exception {

        AuthenticationService authenticationService = new AuthenticationService();
        AuthServiceRequest authServiceRequest = new AuthServiceRequest(request, response);
        when(request.getAttribute(FrameworkConstants.IS_MULTI_OPS_RESPONSE)).thenReturn(isMultiOpsResponse);
        when(request.getAttribute(FrameworkConstants.RequestParams.FLOW_STATUS)).thenReturn(authenticatorFlowStatus);
        when(request.getAttribute(FrameworkConstants.CONTEXT_IDENTIFIER)).thenReturn(sessionDataKey);
        if (isMultiOpsResponse) {
            List<AuthenticatorData> authenticatorDataMap = getMultiOpsAuthenticatorData(authenticatorList);
            for (AuthenticatorData authenticatorData : authenticatorDataMap) {
                when(FrameworkUtils.getAppAuthenticatorByName(authenticatorData.getName()))
                        .thenReturn(new MockApiBasedAuthenticator(authenticatorData.getName()));
            }
        }
        List<AuthenticatorData> expected = getAuthenticatorData(authenticatorList);
        if (AuthenticatorFlowStatus.INCOMPLETE == authenticatorFlowStatus && !isMultiOpsResponse) {
            when(request.getAttribute(AuthServiceConstants.AUTH_SERVICE_AUTH_INITIATION_DATA)).thenReturn(expected);
        }
        when(response.getHeader(LOCATION_HEADER)).thenReturn(redirectUrl);
        AuthServiceResponse authServiceResponse = authenticationService.handleAuthentication(authServiceRequest);

        Assert.assertEquals(authServiceResponse.getSessionDataKey(), sessionDataKey);
        Assert.assertEquals(authServiceResponse.getFlowStatus(), authServiceFlowStatus);
        Optional<AuthServiceResponseData> authServiceResponseData = authServiceResponse.getData();
        if (AuthServiceConstants.FlowStatus.SUCCESS_COMPLETED == authServiceFlowStatus) {
            Assert.assertFalse(authServiceResponseData.isPresent(), "Expected authServiceResponseData to be" +
                    " null as the flow is compete.");
        } else {
            if (!authServiceResponseData.isPresent()) {
                Assert.fail("Expected authServiceResponseData to be present as the flow is incomplete.");
            }
            Assert.assertEquals(authServiceResponseData.get().isAuthenticatorSelectionRequired(), isMultiOpsResponse);
            List<AuthenticatorData> actual = authServiceResponseData.get().getAuthenticatorOptions();
            validateReturnedAuthenticators(actual, expected, isMultiOpsResponse);
        }
    }

    @DataProvider(name = "authProviderForFailures")
    public Object[][] authProviderForFailures() {

        // String redirectUrl, Object authenticatorFlowStatus, Object authServiceFlowStatus,
        // String sessionDataKey, String authenticatorList, String errorCode, String errorMsg
        return new Object[][]{
                {getFailureRedirectUrl(SESSION_DATA_KEY, SINGLE_AUTHENTICATOR, ERROR_MSG_LOGIN_FAIL),
                        AuthenticatorFlowStatus.INCOMPLETE, AuthServiceConstants.FlowStatus.FAIL_INCOMPLETE,
                        SESSION_DATA_KEY, SINGLE_AUTHENTICATOR,
                        AuthServiceConstants.ErrorMessage.ERROR_AUTHENTICATION_FAILURE_RETRY_AVAILABLE.code(),
                        ERROR_MSG_LOGIN_FAIL},
                {getFinalRedirectUrl(FINAL_SESSION_DATA_KEY),
                        AuthenticatorFlowStatus.FAIL_COMPLETED, AuthServiceConstants.FlowStatus.FAIL_COMPLETED,
                        FINAL_SESSION_DATA_KEY, StringUtils.EMPTY,
                        AuthServiceConstants.ErrorMessage.ERROR_AUTHENTICATION_FAILURE.code(),
                        AuthServiceConstants.ErrorMessage.ERROR_AUTHENTICATION_FAILURE.message()}
                ,
        };
    }

    @Test(dataProvider = "authProviderForFailures")
    public void testNegativeHandleAuthentication(String redirectUrl, Object authenticatorFlowStatus,
                                                 Object authServiceFlowStatus, String sessionDataKey,
                                                 String authenticatorList, String errorCode, String errorMsg)
            throws Exception {

        AuthenticationService authenticationService = new AuthenticationService();
        AuthServiceRequest authServiceRequest = new AuthServiceRequest(request, response);
        when(request.getAttribute(FrameworkConstants.IS_MULTI_OPS_RESPONSE)).thenReturn(false);
        when(request.getAttribute(FrameworkConstants.RequestParams.FLOW_STATUS)).thenReturn(authenticatorFlowStatus);
        when(request.getAttribute(FrameworkConstants.CONTEXT_IDENTIFIER)).thenReturn(sessionDataKey);
        List<AuthenticatorData> expected = getAuthenticatorData(authenticatorList);
        if (AuthenticatorFlowStatus.INCOMPLETE == authenticatorFlowStatus) {
            when(request.getAttribute(AuthServiceConstants.AUTH_SERVICE_AUTH_INITIATION_DATA)).thenReturn(expected);
        } else {
            when(request.getAttribute(FrameworkConstants.IS_AUTH_FLOW_CONCLUDED)).thenReturn(true);
        }
        when(response.getHeader(LOCATION_HEADER)).thenReturn(redirectUrl);
        AuthServiceResponse authServiceResponse = authenticationService.handleAuthentication(authServiceRequest);

        Assert.assertEquals(authServiceResponse.getSessionDataKey(), sessionDataKey);
        Assert.assertEquals(authServiceResponse.getFlowStatus(), authServiceFlowStatus);
        Optional<AuthServiceResponseData> authServiceResponseData = authServiceResponse.getData();
        Optional<AuthServiceErrorInfo> authServiceErrorInfo = authServiceResponse.getErrorInfo();
        Assert.assertTrue(authServiceErrorInfo.isPresent(), "Expected authServiceErrorInfo to be present as the " +
                "flow is an failure flow.");
        Assert.assertEquals(authServiceErrorInfo.get().getErrorCode(), errorCode);
        Assert.assertEquals(authServiceErrorInfo.get().getErrorMessage(), errorMsg);

        if (AuthServiceConstants.FlowStatus.FAIL_COMPLETED == authServiceFlowStatus) {
            Assert.assertFalse(authServiceResponseData.isPresent(), "Expected authServiceResponseData to be" +
                    " null as the flow is compete.");
        } else {
            if (!authServiceResponseData.isPresent()) {
                Assert.fail("Expected authServiceResponseData to be present as the flow is incomplete.");
            }
            Assert.assertFalse(authServiceResponseData.get().isAuthenticatorSelectionRequired());
            List<AuthenticatorData> actual = authServiceResponseData.get().getAuthenticatorOptions();
            validateReturnedAuthenticators(actual, expected, false);
        }
    }

    private void validateReturnedAuthenticators(List<AuthenticatorData> actual, List<AuthenticatorData> expected,
                                                boolean isMultiOps) {

        if (actual.size() != expected.size()) {
            Assert.fail("Expected authenticator list size is " + expected.size() + " but actual size is "
                    + actual.size());
        }

        if (isMultiOps) {
            boolean hasAuthParams = expected.stream()
                    .anyMatch(authenticatorData -> !authenticatorData.getAuthParams().isEmpty());
            if (hasAuthParams) {
                Assert.fail("Request is multi option but authenticator data contains auth params.");
            }
        }

        for (AuthenticatorData expectedAuthenticatorData : expected) {

            boolean isNameMatch = actual.stream().anyMatch(actualAuthenticatorData ->
                    expectedAuthenticatorData.getName().equals(actualAuthenticatorData.getName()));
            boolean isDisplayNameMatch = actual.stream().anyMatch(actualAuthenticatorData ->
                    expectedAuthenticatorData.getDisplayName().equals(actualAuthenticatorData.getDisplayName()));
            boolean isi18Key = actual.stream().anyMatch(actualAuthenticatorData ->
                    expectedAuthenticatorData.getI18nKey().equals(actualAuthenticatorData.getI18nKey()));
            boolean isIdpNameMatch = actual.stream().anyMatch(actualAuthenticatorData ->
                    expectedAuthenticatorData.getIdp().equals(actualAuthenticatorData.getIdp()));

            Assert.assertTrue(isNameMatch, "Expected authenticator name is not present in the actual " +
                    "authenticator list");
            Assert.assertTrue(isDisplayNameMatch, "Expected authenticator display name is not present in the" +
                    " actual authenticator list");
            Assert.assertTrue(isIdpNameMatch, "Expected authenticator idp name is not present in the actual " +
                    "authenticator list");
            Assert.assertTrue(isi18Key, "Expected i18Key is not present in the actual " +
                    "authenticator list");
        }
    }

    private String getIntermediateRedirectUrl(String sessionDataKey, String authenticators) {

        return "/authenticationendpoint/login" +
                ".do?client_id=zhcEOyGulBfPryTXxoftrlXAq4Qa&commonAuthCallerPath=%2Foauth2%2Fauthorize&forceAuth" +
                "=false&passiveAuth=false&redirect_uri=http%3A%2F%2Fexample" +
                ".com&response_type=code&scope=openid+internal_login&state=request_0&tenantDomain=carbon" +
                ".super&sessionDataKey=" + sessionDataKey + "&relyingParty" +
                "=zhcEOyGulBfPryTXxoftrlXAq4Qa&type=oidc&sp=sample&isSaaSApp=false&authenticators" +
                "=" + authenticators;

    }

    private String getFinalRedirectUrl(String sessionDataKey) {

        return "/oauth2/authorize?sessionDataKey=" + sessionDataKey;
    }

    private String getFailureRedirectUrl(String sessionDataKey, String authenticators, String errorMsg) {

        return "/authenticationendpoint/login" +
                ".do?client_id=MY_ACCOUNT&code_challenge=zgULEfTaVz4ITrtk__ib2TN_v7yRh_zVj1hw70UzAP4" +
                "&code_challenge_method=S256&commonAuthCallerPath=%2Foauth2%2Fauthorize&forceAuth=false&passiveAuth" +
                "=false&redirect_uri=https%3A%2F%2Flocalhost%3A9443%2Fmyaccount&response_mode=form_post&response_type" +
                "=code&scope=openid+openid+SYSTEM&state=request_0&tenantDomain=carbon.super&sessionDataKey=" +
                sessionDataKey + "&relyingParty=MY_ACCOUNT&type=oidc&sp=My+Account&isSaaSApp=true&inputType=idf" +
                "&authenticators=" + authenticators + "&authFailure=true&authFailureMsg=" + errorMsg;
    }

    private List<AuthenticatorData> getMultiOpsAuthenticatorData(String authenticatorList) {

        if (StringUtils.isBlank(authenticatorList)) {
            return new ArrayList<>();
        }

        return Arrays.stream(authenticatorList.split(AuthServiceConstants.AUTHENTICATOR_SEPARATOR))
                .map(authenticator -> {
                    AuthenticatorData authenticatorData = new AuthenticatorData();
                    authenticatorData.setName(authenticator.split(AuthServiceConstants.AUTHENTICATOR_IDP_SEPARATOR)[0]);
                    authenticatorData.setDisplayName(authenticatorData.getName());
                    return authenticatorData;
                })
                .collect(Collectors.toList());
    }

    private List<AuthenticatorData> getAuthenticatorData(String authenticatorList) {

        List<AuthenticatorData> authenticatorDataList = new ArrayList<>();
        String[] authenticatorAndIdpsArr = StringUtils.split(authenticatorList,
                AuthServiceConstants.AUTHENTICATOR_SEPARATOR);
        for (String authenticatorAndIdps : authenticatorAndIdpsArr) {
            String[] authenticatorIdpSeperatedArr = StringUtils.split(authenticatorAndIdps,
                    AuthServiceConstants.AUTHENTICATOR_IDP_SEPARATOR);
            for (int i = 1; i < authenticatorIdpSeperatedArr.length; i++) {
                String name = authenticatorIdpSeperatedArr[0];
                String idp = authenticatorIdpSeperatedArr[i];
                ApplicationAuthenticator authenticator = new MockApiBasedAuthenticator(name, idp);
                AuthenticatorData authenticatorData = new AuthenticatorData();
                authenticatorData.setName(name);
                authenticatorData.setIdp(idp);
                authenticatorData.setDisplayName(authenticator.getFriendlyName());
                authenticatorData.setI18nKey(authenticator.getI18nKey());
                authenticatorDataList.add(authenticatorData);
            }
        }
        return authenticatorDataList;
    }
}
