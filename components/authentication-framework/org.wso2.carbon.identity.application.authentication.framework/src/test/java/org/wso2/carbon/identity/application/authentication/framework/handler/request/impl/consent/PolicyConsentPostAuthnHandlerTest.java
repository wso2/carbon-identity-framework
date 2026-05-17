/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.consent;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.consent.mgt.core.ConsentManager;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementClientException;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementException;
import org.wso2.carbon.consent.mgt.core.model.PIICategory;
import org.wso2.carbon.consent.mgt.core.model.Purpose;
import org.wso2.carbon.consent.mgt.core.model.PurposeCategory;
import org.wso2.carbon.consent.mgt.core.model.PurposePIICategory;
import org.wso2.carbon.consent.mgt.core.model.PurposeVersion;
import org.wso2.carbon.consent.mgt.core.model.Receipt;
import org.wso2.carbon.consent.mgt.core.model.ReceiptInput;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ApplicationConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.PostAuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.PostAuthnHandlerFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;

import java.util.Arrays;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.testng.Assert.assertEquals;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ACTIVE_STATE;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PII_CAT_NAME_INVALID;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.REJECTED_STATE;

/**
 * Unit tests for {@link PolicyConsentPostAuthnHandler}.
 */
public class PolicyConsentPostAuthnHandlerTest {

    private static final String SUBJECT_ID = "testUser";
    private static final String TENANT_DOMAIN = "carbon.super";
    private static final String SESSION_DATA_KEY = "session-key-123";
    private static final String PURPOSE_UUID_1 = "purpose-uuid-1";
    private static final String PURPOSE_UUID_2 = "purpose-uuid-2";
    private static final String VERSION_UUID_1 = "version-uuid-1";
    private static final String VERSION_UUID_2 = "version-uuid-2";
    private static final String AUTH_ENDPOINT_URL = "https://localhost:9443/authenticationendpoint/login.do";
    private static final String CONSOLE_APPLICATION_NAME = FrameworkConstants.Application.CONSOLE_APP;
    private static final String MY_ACCOUNT_APPLICATION_NAME = FrameworkConstants.Application.MY_ACCOUNT_APP;

    @Mock
    private ConsentManager consentManager;

    private PolicyConsentPostAuthnHandler handler;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private AuthenticationContext context;
    private AuthenticatedUser authenticatedUser;
    private SequenceConfig sequenceConfig;

    private MockedStatic<FrameworkServiceDataHolder> frameworkServiceDataHolderMock;
    private MockedStatic<FrameworkUtils> frameworkUtilsMock;
    private MockedStatic<ConfigurationFacade> configurationFacadeMock;
    private MockedStatic<LoggerUtils> loggerUtilsMock;

    @BeforeMethod
    public void setUp() throws ConsentManagementException {

        openMocks(this);
        handler = new PolicyConsentPostAuthnHandler();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        context = new AuthenticationContext();
        authenticatedUser = mock(AuthenticatedUser.class);
        sequenceConfig = mock(SequenceConfig.class);

        when(authenticatedUser.getAuthenticatedSubjectIdentifier()).thenReturn(SUBJECT_ID);
        when(authenticatedUser.getUserName()).thenReturn(SUBJECT_ID);
        when(authenticatedUser.getTenantDomain()).thenReturn(TENANT_DOMAIN);

        ApplicationConfig appConfig = mock(ApplicationConfig.class);
        when(appConfig.getServiceProvider()).thenReturn(mock(ServiceProvider.class));
        when(sequenceConfig.getApplicationConfig()).thenReturn(appConfig);

        context.setSequenceConfig(sequenceConfig);
        context.setContextIdentifier(SESSION_DATA_KEY);
        when(sequenceConfig.getAuthenticatedUser()).thenReturn(authenticatedUser);

        FrameworkServiceDataHolder dataHolder = mock(FrameworkServiceDataHolder.class);
        frameworkServiceDataHolderMock = mockStatic(FrameworkServiceDataHolder.class);
        frameworkServiceDataHolderMock.when(FrameworkServiceDataHolder::getInstance).thenReturn(dataHolder);
        when(dataHolder.getConsentManager()).thenReturn(consentManager);

        frameworkUtilsMock = mockStatic(FrameworkUtils.class);
        frameworkUtilsMock.when(() -> FrameworkUtils.isAPIBasedAuthenticationFlow(request)).thenReturn(false);

        ConfigurationFacade configurationFacadeInstance = mock(ConfigurationFacade.class);
        configurationFacadeMock = mockStatic(ConfigurationFacade.class);
        configurationFacadeMock.when(ConfigurationFacade::getInstance).thenReturn(configurationFacadeInstance);
        when(configurationFacadeInstance.getAuthenticationEndpointURL()).thenReturn(AUTH_ENDPOINT_URL);

        // Prevent LoggerUtils.isDiagnosticLogsEnabled() from calling IdentityTenantUtil.getTenantId()
        // with a null tenant domain (no CarbonContext in unit tests).
        loggerUtilsMock = mockStatic(LoggerUtils.class);
        loggerUtilsMock.when(LoggerUtils::isDiagnosticLogsEnabled).thenReturn(false);

        // Default stubs so ConsentReceiptUtils.buildReceiptInput() can complete without NPE.
        // Tests that need specific Purpose behaviour override these with their own stubs.
        Purpose defaultPurpose = mock(Purpose.class);
        when(defaultPurpose.getId()).thenReturn(1);
        when(consentManager.getPurposeByUuid(anyString())).thenReturn(defaultPurpose);
        PurposeCategory defaultPurposeCategory = mock(PurposeCategory.class);
        when(consentManager.getPurposeCategoryByName("DEFAULT")).thenReturn(defaultPurposeCategory);
    }

    @AfterMethod
    public void tearDown() {

        frameworkServiceDataHolderMock.close();
        frameworkUtilsMock.close();
        configurationFacadeMock.close();
        loggerUtilsMock.close();
    }

    /**
     * handle() — entry point routing
     */
    @Test(description = "Returns SUCCESS_COMPLETED when no authenticated user is present in context.")
    public void testHandleReturnsSuccessWhenNoAuthenticatedUser() throws Exception {

        when(sequenceConfig.getAuthenticatedUser()).thenReturn(null);

        PostAuthnHandlerFlowStatus status = handler.handle(request, response, context);

        assertEquals(status, PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED);
    }

    @Test(description = "Returns SUCCESS_COMPLETED immediately for API-based authentication flows.")
    public void testHandleSkipsForAPIBasedAuthFlow() throws Exception {

        frameworkUtilsMock.when(() -> FrameworkUtils.isAPIBasedAuthenticationFlow(request)).thenReturn(true);

        PostAuthnHandlerFlowStatus status = handler.handle(request, response, context);

        assertEquals(status, PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED);
    }

    @Test(description = "Returns SUCCESS_COMPLETED immediately for Console application.")
    public void testHandleSkipsForConsoleApplication() throws Exception {

        context.setServiceProviderName(CONSOLE_APPLICATION_NAME);

        PostAuthnHandlerFlowStatus status = handler.handle(request, response, context);

        assertEquals(status, PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED);
        verify(response, never()).sendRedirect(anyString());
        verify(consentManager, never()).listPurposes(anyList(), anyInt());
    }

    @Test(description = "Returns SUCCESS_COMPLETED immediately for My Account application.")
    public void testHandleSkipsForMyAccountApplication() throws Exception {

        context.setServiceProviderName(MY_ACCOUNT_APPLICATION_NAME);

        PostAuthnHandlerFlowStatus status = handler.handle(request, response, context);

        assertEquals(status, PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED);
        verify(response, never()).sendRedirect(anyString());
        verify(consentManager, never()).listPurposes(anyList(), anyInt());
    }

    @Test(description = "getName returns the expected handler name.")
    public void testGetName() {

        assertEquals(handler.getName(), "PolicyConsentPostAuthenticationHandler");
    }

    /**
     * handlePrePolicyConsent — mandatory purposes
     */
    @Test(description = "Returns SUCCESS_COMPLETED when there are no policy purposes at all.")
    public void testPreConsentSuccessWhenNoPolicyPurposesExist() throws Exception {

        when(consentManager.listPurposes(anyList(), anyInt())).thenReturn(Collections.emptyList());

        PostAuthnHandlerFlowStatus status = handler.handle(request, response, context);

        assertEquals(status, PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED);
        verify(response, never()).sendRedirect(anyString());
    }

    @Test(description = "Returns INCOMPLETE and redirects for mandatory purpose without receipt (promptOnLogin).")
    public void testPreConsentRedirectsForMandatoryPurposeWithNoActiveReceipt() throws Exception {

        Purpose mandatory = buildMandatoryPurpose(PURPOSE_UUID_1, VERSION_UUID_1);
        when(consentManager.listPurposes(anyList(), anyInt()))
                .thenReturn(Collections.singletonList(mandatory));
        when(consentManager.listReceipts(anyString(), eq("SYSTEM"), isNull(),
                eq(PURPOSE_UUID_1), eq(VERSION_UUID_1), isNull(), isNull(), eq(1)))
                .thenReturn(Collections.emptyList());

        PostAuthnHandlerFlowStatus status = handler.handle(request, response, context);

        assertEquals(status, PostAuthnHandlerFlowStatus.INCOMPLETE);
        verify(response).sendRedirect(anyString());
    }

    @Test(description = "Returns SUCCESS_COMPLETED for mandatory purpose with receipt (promptOnLogin).")
    public void testPreConsentSkipsMandatoryPurposeWithActiveReceipt() throws Exception {

        Purpose mandatory = buildMandatoryPurpose(PURPOSE_UUID_1, VERSION_UUID_1);
        when(consentManager.listPurposes(anyList(), anyInt()))
                .thenReturn(Collections.singletonList(mandatory));
        when(consentManager.listReceipts(anyString(), eq("SYSTEM"), isNull(),
                eq(PURPOSE_UUID_1), eq(VERSION_UUID_1), isNull(), isNull(), eq(1)))
                .thenReturn(Collections.singletonList(mock(Receipt.class)));

        PostAuthnHandlerFlowStatus status = handler.handle(request, response, context);

        assertEquals(status, PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED);
        verify(response, never()).sendRedirect(anyString());
    }

    @Test(description = "Redirect URL includes mandatoryPurposeIds param for mandatory unconsented purposes.")
    public void testRedirectUrlContainsMandatoryPurposeIdsParam() throws Exception {

        Purpose mandatory = buildMandatoryPurpose(PURPOSE_UUID_1, VERSION_UUID_1);
        when(consentManager.listPurposes(anyList(), anyInt()))
                .thenReturn(Collections.singletonList(mandatory));
        when(consentManager.listReceipts(anyString(), eq("SYSTEM"), eq(ACTIVE_STATE),
                eq(PURPOSE_UUID_1), eq(VERSION_UUID_1), isNull(), isNull(), eq(1)))
                .thenReturn(Collections.emptyList());

        handler.handle(request, response, context);

        verify(response).sendRedirect(
                org.mockito.ArgumentMatchers.argThat(url ->
                        url.contains("policy_consent.do") &&
                        url.contains(SESSION_DATA_KEY) &&
                        url.contains("mandatoryPurposeIds") &&
                        url.contains(PURPOSE_UUID_1) &&
                        !url.contains("optionalPurposeIds")));
    }

    /**
     * handlePrePolicyConsent — optional purposes
     */
    @Test(description = "Returns INCOMPLETE and redirects for optional purpose without receipt (promptOnLogin).")
    public void testPreConsentRedirectsForOptionalPurposeWithNoReceipt() throws Exception {

        Purpose optional = buildOptionalPurpose(PURPOSE_UUID_1, VERSION_UUID_1);
        when(consentManager.listPurposes(anyList(), anyInt()))
                .thenReturn(Collections.singletonList(optional));
        when(consentManager.listReceipts(anyString(), eq("SYSTEM"), isNull(),
                eq(PURPOSE_UUID_1), eq(VERSION_UUID_1), isNull(), isNull(), eq(1)))
                .thenReturn(Collections.emptyList());

        PostAuthnHandlerFlowStatus status = handler.handle(request, response, context);

        assertEquals(status, PostAuthnHandlerFlowStatus.INCOMPLETE);
        verify(response).sendRedirect(anyString());
    }

    @Test(description = "Returns SUCCESS_COMPLETED when an optional purpose already has a REJECTED receipt.")
    public void testPreConsentSkipsOptionalPurposeWithRejectedReceipt() throws Exception {

        Purpose optional = buildOptionalPurpose(PURPOSE_UUID_1, VERSION_UUID_1);
        when(consentManager.listPurposes(anyList(), anyInt()))
                .thenReturn(Collections.singletonList(optional));
        when(consentManager.listReceipts(anyString(), eq("SYSTEM"), isNull(),
                eq(PURPOSE_UUID_1), eq(VERSION_UUID_1), isNull(), isNull(), eq(1)))
                .thenReturn(Collections.singletonList(mock(Receipt.class)));

        PostAuthnHandlerFlowStatus status = handler.handle(request, response, context);

        assertEquals(status, PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED);
        verify(response, never()).sendRedirect(anyString());
    }

    @Test(description = "Redirect URL includes optionalPurposeIds param for optional unseen purposes.")
    public void testRedirectUrlContainsOptionalPurposeIdsParam() throws Exception {

        Purpose optional = buildOptionalPurpose(PURPOSE_UUID_1, VERSION_UUID_1);
        when(consentManager.listPurposes(anyList(), anyInt()))
                .thenReturn(Collections.singletonList(optional));
        when(consentManager.listReceipts(anyString(), eq("SYSTEM"), isNull(),
                eq(PURPOSE_UUID_1), eq(VERSION_UUID_1), isNull(), isNull(), eq(1)))
                .thenReturn(Collections.emptyList());

        handler.handle(request, response, context);

        verify(response).sendRedirect(
                org.mockito.ArgumentMatchers.argThat(url ->
                        url.contains("policy_consent.do") &&
                        url.contains(SESSION_DATA_KEY) &&
                        url.contains("optionalPurposeIds") &&
                        url.contains(PURPOSE_UUID_1) &&
                        !url.contains("mandatoryPurposeIds")));
    }

    @Test(description = "Redirect URL contains both mandatory and optional params when both types are pending.")
    public void testRedirectUrlContainsBothParamsWhenMixedPurposesPending() throws Exception {

        Purpose mandatory = buildMandatoryPurpose(PURPOSE_UUID_1, VERSION_UUID_1);
        Purpose optional = buildOptionalPurpose(PURPOSE_UUID_2, VERSION_UUID_2);
        when(consentManager.listPurposes(anyList(), anyInt()))
                .thenReturn(Arrays.asList(mandatory, optional));
        when(consentManager.listReceipts(anyString(), eq("SYSTEM"), isNull(),
                eq(PURPOSE_UUID_1), eq(VERSION_UUID_1), isNull(), isNull(), eq(1)))
                .thenReturn(Collections.emptyList());
        when(consentManager.listReceipts(anyString(), eq("SYSTEM"), isNull(),
                eq(PURPOSE_UUID_2), eq(VERSION_UUID_2), isNull(), isNull(), eq(1)))
                .thenReturn(Collections.emptyList());

        handler.handle(request, response, context);

        verify(response).sendRedirect(
                org.mockito.ArgumentMatchers.argThat(url ->
                        url.contains("mandatoryPurposeIds") && url.contains(PURPOSE_UUID_1) &&
                        url.contains("optionalPurposeIds") && url.contains(PURPOSE_UUID_2)));
    }

    @Test(description = "Throws PostAuthenticationFailedException when ConsentManager throws during pre-consent.")
    public void testPreConsentWrapsConsentManagerException() throws Exception {

        when(consentManager.listPurposes(anyList(), anyInt()))
                .thenThrow(new ConsentManagementException("DB error", "CM_00001"));

        try {
            handler.handle(request, response, context);
            throw new AssertionError("Expected PostAuthenticationFailedException was not thrown.");
        } catch (PostAuthenticationFailedException e) {
            // Expected.
        }
    }

    /**
     * handlePostPolicyConsent — mandatory purposes
     */
    @Test(description = "Throws PostAuthenticationFailedException when user denies consent (mandatory declined).")
    public void testPostConsentThrowsWhenUserDenies() {

        context.addParameter("policyConsentPrompted", true);
        context.addParameter("policyMandatoryUnconsentedIds", Collections.singletonList(PURPOSE_UUID_1));
        context.addParameter("policyOptionalUnconsentedIds", Collections.emptyList());
        when(request.getParameter("consent")).thenReturn("deny");

        try {
            handler.handle(request, response, context);
            throw new AssertionError("Expected PostAuthenticationFailedException was not thrown.");
        } catch (PostAuthenticationFailedException e) {
            // Expected.
        }
    }

    @Test(description = "Records ACTIVE consent for each mandatory purpose when user approves.")
    @SuppressWarnings("unchecked")
    public void testPostConsentRecordsActiveReceiptForMandatoryPurposes() throws Exception {

        PIICategory piiCategory = mock(PIICategory.class);
        when(piiCategory.getId()).thenReturn(1);
        when(consentManager.getPIICategoryByName("Policy")).thenReturn(piiCategory);

        context.addParameter("policyConsentPrompted", true);
        context.addParameter("policyMandatoryUnconsentedIds",
                Arrays.asList(PURPOSE_UUID_1, PURPOSE_UUID_2));
        context.addParameter("policyOptionalUnconsentedIds", Collections.emptyList());
        when(request.getParameter("consent")).thenReturn("approve");

        PostAuthnHandlerFlowStatus status = handler.handle(request, response, context);

        assertEquals(status, PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED);
        verify(consentManager, times(2)).addConsent(any(ReceiptInput.class));
    }

    @Test(description = "Returns SUCCESS_COMPLETED with no consent recording when both purpose lists are empty.")
    @SuppressWarnings("unchecked")
    public void testPostConsentReturnsSuccessWithEmptyPurposeLists() throws Exception {

        context.addParameter("policyConsentPrompted", true);
        context.addParameter("policyMandatoryUnconsentedIds", Collections.emptyList());
        context.addParameter("policyOptionalUnconsentedIds", Collections.emptyList());
        when(request.getParameter("consent")).thenReturn("approve");

        PostAuthnHandlerFlowStatus status = handler.handle(request, response, context);

        assertEquals(status, PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED);
        verify(consentManager, never()).addConsent(any(ReceiptInput.class));
    }

    /**
     * handlePostPolicyConsent — optional purposes
     */
    @Test(description = "Records ACTIVE consent for accepted optional purposes when not in skipped list.")
    @SuppressWarnings("unchecked")
    public void testPostConsentRecordsActiveReceiptForAcceptedOptionalPurpose() throws Exception {

        PIICategory piiCategory = mock(PIICategory.class);
        when(piiCategory.getId()).thenReturn(1);
        when(consentManager.getPIICategoryByName("Policy")).thenReturn(piiCategory);

        context.addParameter("policyConsentPrompted", true);
        context.addParameter("policyMandatoryUnconsentedIds", Collections.emptyList());
        context.addParameter("policyOptionalUnconsentedIds",
                Collections.singletonList(PURPOSE_UUID_1));
        when(request.getParameter("consent")).thenReturn("approve");
        // PURPOSE_UUID_1 is in the approved set — user checked the checkbox.
        when(request.getParameterValues("optionalPurposeId")).thenReturn(new String[]{PURPOSE_UUID_1});

        PostAuthnHandlerFlowStatus status = handler.handle(request, response, context);

        assertEquals(status, PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED);
        // buildReceiptInput only sets state for the rejected path; ACTIVE leaves state as null.
        verify(consentManager, times(1)).addConsent(any(ReceiptInput.class));
    }

    @Test(description = "Records REJECTED receipt via ReceiptInput for skipped optional purposes.")
    @SuppressWarnings("unchecked")
    public void testPostConsentRecordsRejectedReceiptForSkippedOptionalPurpose() throws Exception {

        PIICategory piiCategory = mock(PIICategory.class);
        when(piiCategory.getId()).thenReturn(1);
        when(consentManager.getPIICategoryByName("Policy")).thenReturn(piiCategory);

        Purpose purpose = buildOptionalPurpose(PURPOSE_UUID_1, VERSION_UUID_1);
        when(consentManager.getPurposeByUuid(PURPOSE_UUID_1)).thenReturn(purpose);
        PurposeCategory defaultCategory = mock(PurposeCategory.class);
        when(defaultCategory.getId()).thenReturn(10);
        when(consentManager.getPurposeCategoryByName("DEFAULT")).thenReturn(defaultCategory);

        context.addParameter("policyConsentPrompted", true);
        context.addParameter("policyMandatoryUnconsentedIds", Collections.emptyList());
        context.addParameter("policyOptionalUnconsentedIds",
                Collections.singletonList(PURPOSE_UUID_1));
        when(request.getParameter("consent")).thenReturn("approve");
        // No optionalPurposeId submitted — PURPOSE_UUID_1 is skipped/rejected.
        when(request.getParameterValues("optionalPurposeId")).thenReturn(null);

        PostAuthnHandlerFlowStatus status = handler.handle(request, response, context);

        assertEquals(status, PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED);
        ArgumentCaptor<ReceiptInput> captor = ArgumentCaptor.forClass(ReceiptInput.class);
        verify(consentManager, times(1)).addConsent(captor.capture());
        assertEquals(captor.getValue().getState(), REJECTED_STATE);
        assertEquals(captor.getValue().getPiiPrincipalId(), SUBJECT_ID);
        assertEquals(captor.getValue().getTenantDomain(), TENANT_DOMAIN);
    }

    @Test(description = "Records ACTIVE for accepted and REJECTED for skipped when both optional purposes are present.")
    @SuppressWarnings("unchecked")
    public void testPostConsentHandlesMixedOptionalAcceptAndSkip() throws Exception {

        PIICategory piiCategory = mock(PIICategory.class);
        when(piiCategory.getId()).thenReturn(1);
        when(consentManager.getPIICategoryByName("Policy")).thenReturn(piiCategory);

        Purpose skippedPurpose = buildOptionalPurpose(PURPOSE_UUID_1, VERSION_UUID_1);
        when(consentManager.getPurposeByUuid(PURPOSE_UUID_1)).thenReturn(skippedPurpose);
        PurposeCategory defaultCategory = mock(PurposeCategory.class);
        when(defaultCategory.getId()).thenReturn(10);
        when(consentManager.getPurposeCategoryByName("DEFAULT")).thenReturn(defaultCategory);

        context.addParameter("policyConsentPrompted", true);
        context.addParameter("policyMandatoryUnconsentedIds", Collections.emptyList());
        context.addParameter("policyOptionalUnconsentedIds",
                Arrays.asList(PURPOSE_UUID_1, PURPOSE_UUID_2));
        when(request.getParameter("consent")).thenReturn("approve");
        // UUID_1 skipped (not submitted), UUID_2 accepted.
        when(request.getParameterValues("optionalPurposeId")).thenReturn(new String[]{PURPOSE_UUID_2});

        PostAuthnHandlerFlowStatus status = handler.handle(request, response, context);

        assertEquals(status, PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED);
        ArgumentCaptor<ReceiptInput> captor = ArgumentCaptor.forClass(ReceiptInput.class);
        verify(consentManager, times(2)).addConsent(captor.capture());
        // buildReceiptInput sets REJECTED_STATE explicitly; ACTIVE leaves state as null.
        assertEquals(captor.getAllValues().stream().filter(r -> REJECTED_STATE.equals(r.getState())).count(), 1L);
        assertEquals(captor.getAllValues().stream().filter(r -> r.getState() == null).count(), 1L);
    }

    /**
     * handlePostPolicyConsent — PII category creation
     */
    @Test(description = "Creates a new PII category when one does not already exist.")
    @SuppressWarnings("unchecked")
    public void testPostConsentCreatesNewPIICategoryWhenAbsent() throws Exception {

        ConsentManagementClientException notFoundException = mock(ConsentManagementClientException.class);
        when(notFoundException.getErrorCode()).thenReturn(ERROR_CODE_PII_CAT_NAME_INVALID.getCode());
        when(consentManager.getPIICategoryByName("Policy")).thenThrow(notFoundException);
        PIICategory newCategory = mock(PIICategory.class);
        when(newCategory.getId()).thenReturn(2);
        when(consentManager.addPIICategoryWithUuid(any())).thenReturn(newCategory);

        context.addParameter("policyConsentPrompted", true);
        context.addParameter("policyMandatoryUnconsentedIds",
                Collections.singletonList(PURPOSE_UUID_1));
        context.addParameter("policyOptionalUnconsentedIds", Collections.emptyList());
        when(request.getParameter("consent")).thenReturn("approve");

        PostAuthnHandlerFlowStatus status = handler.handle(request, response, context);

        assertEquals(status, PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED);
        verify(consentManager).addPIICategoryWithUuid(any());
    }

    /**
     * helpers
     */
    private Purpose buildMandatoryPurpose(String purposeUuid, String versionUuid) throws ConsentManagementException {

        PurposePIICategory mandatoryElement = mock(PurposePIICategory.class);
        when(mandatoryElement.getMandatory()).thenReturn(true);

        PurposeVersion version = mock(PurposeVersion.class);
        when(version.getUuid()).thenReturn(versionUuid);
        when(version.getPurposePIICategories()).thenReturn(Collections.singletonList(mandatoryElement));
        when(version.getProperties()).thenReturn(Collections.singletonMap("promptOnLogin", "true"));

        Purpose purpose = mock(Purpose.class);
        when(purpose.getUuid()).thenReturn(purposeUuid);
        when(purpose.getLatestVersion()).thenReturn(version);
        when(consentManager.listPurposeVersions(purposeUuid))
                .thenReturn(Collections.singletonList(version));
        return purpose;
    }

    private Purpose buildOptionalPurpose(String purposeUuid, String versionUuid) throws ConsentManagementException {

        PurposeVersion version = mock(PurposeVersion.class);
        when(version.getUuid()).thenReturn(versionUuid);
        when(version.getPurposePIICategories()).thenReturn(Collections.emptyList());
        when(version.getProperties()).thenReturn(Collections.singletonMap("promptOnLogin", "true"));

        Purpose purpose = mock(Purpose.class);
        when(purpose.getUuid()).thenReturn(purposeUuid);
        when(purpose.getId()).thenReturn(42);
        when(purpose.getLatestVersion()).thenReturn(version);
        when(consentManager.listPurposeVersions(purposeUuid))
                .thenReturn(Collections.singletonList(version));
        return purpose;
    }

    /**
     * promptOnLogin feature tests
     */
    @Test(description = "Policy with promptOnLogin should prompt if user has no consent for that version or later.")
    public void testPromptOnLoginPolicyWithNoConsent() throws Exception {

        frameworkUtilsMock.when(() -> FrameworkUtils.isConsentV2APIEnabled()).thenReturn(true);
        frameworkUtilsMock.when(() -> FrameworkUtils.isConsentPageSkippedForSP(any())).thenReturn(false);

        String versionUuidWithPrompt = "version-uuid-with-prompt";
        String versionUuidLatest = "version-uuid-latest";

        Purpose purpose = buildOptionalPurpose(PURPOSE_UUID_1, versionUuidLatest);

        PurposeVersion versionWithPrompt = mock(PurposeVersion.class);
        when(versionWithPrompt.getUuid()).thenReturn(versionUuidWithPrompt);
        when(versionWithPrompt.getProperties()).thenReturn(Collections.singletonMap("promptOnLogin", "true"));
        when(versionWithPrompt.getPurposePIICategories()).thenReturn(Collections.emptyList());

        PurposeVersion latestVersion = mock(PurposeVersion.class);
        when(latestVersion.getUuid()).thenReturn(versionUuidLatest);
        when(latestVersion.getProperties()).thenReturn(Collections.emptyMap());
        when(latestVersion.getPurposePIICategories()).thenReturn(Collections.emptyList());

        when(purpose.getLatestVersion()).thenReturn(latestVersion);
        when(consentManager.listPurposes(anyList(), anyInt()))
                .thenReturn(Collections.singletonList(purpose));
        when(consentManager.listPurposeVersions(PURPOSE_UUID_1))
                .thenReturn(Arrays.asList(versionWithPrompt, latestVersion));
        when(consentManager.listReceipts(eq(SUBJECT_ID), anyString(), isNull(), eq(PURPOSE_UUID_1),
                anyString(), isNull(), isNull(), anyInt()))
                .thenReturn(Collections.emptyList());

        PostAuthnHandlerFlowStatus status = handler.handle(request, response, context);

        assertEquals(status, PostAuthnHandlerFlowStatus.INCOMPLETE);
        ArgumentCaptor<String> redirectUrlCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(redirectUrlCaptor.capture());
        String redirectUrl = redirectUrlCaptor.getValue();

        // Should contain the optional purpose ID since it has promptOnLogin
        assertEquals(context.getParameter("policyOptionalUnconsentedIds"),
                Collections.singletonList(PURPOSE_UUID_1));
    }

    @Test(description = "Policy with promptOnLogin should not prompt if user has consent for that version.")
    public void testPromptOnLoginPolicyWithConsent() throws Exception {

        frameworkUtilsMock.when(() -> FrameworkUtils.isConsentV2APIEnabled()).thenReturn(true);
        frameworkUtilsMock.when(() -> FrameworkUtils.isConsentPageSkippedForSP(any())).thenReturn(false);

        String versionUuidWithPrompt = "version-uuid-with-prompt";
        String versionUuidLatest = "version-uuid-latest";

        Purpose purpose = buildOptionalPurpose(PURPOSE_UUID_1, versionUuidLatest);

        PurposeVersion versionWithPrompt = mock(PurposeVersion.class);
        when(versionWithPrompt.getUuid()).thenReturn(versionUuidWithPrompt);
        when(versionWithPrompt.getProperties()).thenReturn(Collections.singletonMap("promptOnLogin", "true"));
        when(versionWithPrompt.getPurposePIICategories()).thenReturn(Collections.emptyList());

        PurposeVersion latestVersion = mock(PurposeVersion.class);
        when(latestVersion.getUuid()).thenReturn(versionUuidLatest);
        when(latestVersion.getProperties()).thenReturn(Collections.emptyMap());
        when(latestVersion.getPurposePIICategories()).thenReturn(Collections.emptyList());

        when(purpose.getLatestVersion()).thenReturn(latestVersion);
        when(consentManager.listPurposes(anyList(), anyInt()))
                .thenReturn(Collections.singletonList(purpose));
        when(consentManager.listPurposeVersions(PURPOSE_UUID_1))
                .thenReturn(Arrays.asList(versionWithPrompt, latestVersion));

        Receipt mockReceipt = mock(Receipt.class);
        when(consentManager.listReceipts(eq(SUBJECT_ID), anyString(), isNull(), eq(PURPOSE_UUID_1),
                eq(versionUuidWithPrompt), isNull(), isNull(), anyInt()))
                .thenReturn(Collections.singletonList(mockReceipt));

        PostAuthnHandlerFlowStatus status = handler.handle(request, response, context);

        assertEquals(status, PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED);
        verify(response, never()).sendRedirect(anyString());
    }

    @Test(description = "Policy with promptOnLogin should not prompt if user has consent for a later version.")
    public void testPromptOnLoginPolicyWithConsentForLaterVersion() throws Exception {

        frameworkUtilsMock.when(() -> FrameworkUtils.isConsentV2APIEnabled()).thenReturn(true);
        frameworkUtilsMock.when(() -> FrameworkUtils.isConsentPageSkippedForSP(any())).thenReturn(false);

        String versionUuidWithPrompt = "version-uuid-with-prompt";
        String versionUuidLatest = "version-uuid-latest";

        Purpose purpose = buildOptionalPurpose(PURPOSE_UUID_1, versionUuidLatest);

        PurposeVersion versionWithPrompt = mock(PurposeVersion.class);
        when(versionWithPrompt.getUuid()).thenReturn(versionUuidWithPrompt);
        when(versionWithPrompt.getProperties()).thenReturn(Collections.singletonMap("promptOnLogin", "true"));
        when(versionWithPrompt.getPurposePIICategories()).thenReturn(Collections.emptyList());

        PurposeVersion latestVersion = mock(PurposeVersion.class);
        when(latestVersion.getUuid()).thenReturn(versionUuidLatest);
        when(latestVersion.getProperties()).thenReturn(Collections.emptyMap());
        when(latestVersion.getPurposePIICategories()).thenReturn(Collections.emptyList());

        when(purpose.getLatestVersion()).thenReturn(latestVersion);
        when(consentManager.listPurposes(anyList(), anyInt()))
                .thenReturn(Collections.singletonList(purpose));
        when(consentManager.listPurposeVersions(PURPOSE_UUID_1))
                .thenReturn(Arrays.asList(versionWithPrompt, latestVersion));

        Receipt mockReceipt = mock(Receipt.class);
        // User has consent for the LATEST version (which is after the promptOnLogin version)
        when(consentManager.listReceipts(eq(SUBJECT_ID), anyString(), isNull(), eq(PURPOSE_UUID_1),
                eq(versionUuidWithPrompt), isNull(), isNull(), anyInt()))
                .thenReturn(Collections.emptyList());
        when(consentManager.listReceipts(eq(SUBJECT_ID), anyString(), isNull(), eq(PURPOSE_UUID_1),
                eq(versionUuidLatest), isNull(), isNull(), anyInt()))
                .thenReturn(Collections.singletonList(mockReceipt));

        PostAuthnHandlerFlowStatus status = handler.handle(request, response, context);

        assertEquals(status, PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED);
        verify(response, never()).sendRedirect(anyString());
    }

    @Test(description = "Policy without promptOnLogin should be skipped regardless of consent status.")
    public void testPolicyWithoutPromptOnLoginIsSkipped() throws Exception {

        frameworkUtilsMock.when(() -> FrameworkUtils.isConsentV2APIEnabled()).thenReturn(true);
        frameworkUtilsMock.when(() -> FrameworkUtils.isConsentPageSkippedForSP(any())).thenReturn(false);

        Purpose purpose = buildOptionalPurpose(PURPOSE_UUID_1, VERSION_UUID_1);

        PurposeVersion version = mock(PurposeVersion.class);
        when(version.getUuid()).thenReturn(VERSION_UUID_1);
        when(version.getProperties()).thenReturn(Collections.emptyMap());
        when(version.getPurposePIICategories()).thenReturn(Collections.emptyList());

        when(purpose.getLatestVersion()).thenReturn(version);
        when(consentManager.listPurposes(anyList(), anyInt()))
                .thenReturn(Collections.singletonList(purpose));
        when(consentManager.listPurposeVersions(PURPOSE_UUID_1))
                .thenReturn(Collections.singletonList(version));

        PostAuthnHandlerFlowStatus status = handler.handle(request, response, context);

        assertEquals(status, PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED);
        verify(response, never()).sendRedirect(anyString());
    }

    @Test(description = "Mandatory policy with promptOnLogin should block login if user declines.")
    public void testMandatoryPromptOnLoginPolicyBlocksLogin() throws Exception {

        frameworkUtilsMock.when(() -> FrameworkUtils.isConsentV2APIEnabled()).thenReturn(true);
        frameworkUtilsMock.when(() -> FrameworkUtils.isConsentPageSkippedForSP(any())).thenReturn(false);

        String versionUuidWithPrompt = "version-uuid-with-prompt";

        Purpose purpose = buildMandatoryPurpose(PURPOSE_UUID_1, versionUuidWithPrompt);

        PurposeVersion versionWithPrompt = mock(PurposeVersion.class);
        when(versionWithPrompt.getUuid()).thenReturn(versionUuidWithPrompt);
        when(versionWithPrompt.getProperties()).thenReturn(Collections.singletonMap("promptOnLogin", "true"));
        PurposePIICategory mandatoryElement = mock(PurposePIICategory.class);
        when(mandatoryElement.getMandatory()).thenReturn(true);
        when(versionWithPrompt.getPurposePIICategories()).thenReturn(Collections.singletonList(mandatoryElement));

        when(purpose.getLatestVersion()).thenReturn(versionWithPrompt);
        when(consentManager.listPurposes(anyList(), anyInt()))
                .thenReturn(Collections.singletonList(purpose));
        when(consentManager.listPurposeVersions(PURPOSE_UUID_1))
                .thenReturn(Collections.singletonList(versionWithPrompt));
        when(consentManager.listReceipts(eq(SUBJECT_ID), anyString(), isNull(), eq(PURPOSE_UUID_1),
                anyString(), isNull(), isNull(), anyInt()))
                .thenReturn(Collections.emptyList());

        PostAuthnHandlerFlowStatus status = handler.handle(request, response, context);

        assertEquals(status, PostAuthnHandlerFlowStatus.INCOMPLETE);
        assertEquals(context.getParameter("policyMandatoryUnconsentedIds"),
                Collections.singletonList(PURPOSE_UUID_1));
    }
}
