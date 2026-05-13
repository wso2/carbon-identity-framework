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
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.PostAuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.PostAuthnHandlerFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;

import java.util.Arrays;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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

    @BeforeMethod
    public void setUp() {

        openMocks(this);
        handler = new PolicyConsentPostAuthnHandler();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        context = new AuthenticationContext();
        authenticatedUser = mock(AuthenticatedUser.class);
        sequenceConfig = mock(SequenceConfig.class);

        when(authenticatedUser.getAuthenticatedSubjectIdentifier()).thenReturn(SUBJECT_ID);
        when(authenticatedUser.getTenantDomain()).thenReturn(TENANT_DOMAIN);

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
    }

    @AfterMethod
    public void tearDown() {

        frameworkServiceDataHolderMock.close();
        frameworkUtilsMock.close();
        configurationFacadeMock.close();
    }

    // -------------------------------------------------------------------------
    // handle() — entry point routing
    // -------------------------------------------------------------------------

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
                verify(consentManager, never()).listPurposes(any(), anyInt(), anyInt());
        }

        @Test(description = "Returns SUCCESS_COMPLETED immediately for My Account application.")
        public void testHandleSkipsForMyAccountApplication() throws Exception {

                context.setServiceProviderName(MY_ACCOUNT_APPLICATION_NAME);

                PostAuthnHandlerFlowStatus status = handler.handle(request, response, context);

                assertEquals(status, PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED);
                verify(response, never()).sendRedirect(anyString());
                verify(consentManager, never()).listPurposes(any(), anyInt(), anyInt());
        }

    @Test(description = "getName returns the expected handler name.")
    public void testGetName() {

        assertEquals(handler.getName(), "PolicyConsentPostAuthenticationHandler");
    }

    // -------------------------------------------------------------------------
    // handlePrePolicyConsent — mandatory purposes
    // -------------------------------------------------------------------------

    @Test(description = "Returns SUCCESS_COMPLETED when there are no policy purposes at all.")
    public void testPreConsentSuccessWhenNoPolicyPurposesExist() throws Exception {

        when(consentManager.listPurposes(any(), anyInt(), anyInt())).thenReturn(Collections.emptyList());

        PostAuthnHandlerFlowStatus status = handler.handle(request, response, context);

        assertEquals(status, PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED);
        verify(response, never()).sendRedirect(anyString());
    }

    @Test(description = "Returns INCOMPLETE and redirects when a mandatory purpose has no ACTIVE receipt.")
    public void testPreConsentRedirectsForMandatoryPurposeWithNoActiveReceipt() throws Exception {

        Purpose mandatory = buildMandatoryPurpose(PURPOSE_UUID_1, VERSION_UUID_1);
        when(consentManager.listPurposes(any(), anyInt(), anyInt()))
                .thenReturn(Collections.singletonList(mandatory));
        // No ACTIVE receipt for this version.
        when(consentManager.listReceipts(eq(SUBJECT_ID), isNull(), eq(ACTIVE_STATE),
                eq(PURPOSE_UUID_1), eq(VERSION_UUID_1), eq(1), eq(0)))
                .thenReturn(Collections.emptyList());

        PostAuthnHandlerFlowStatus status = handler.handle(request, response, context);

        assertEquals(status, PostAuthnHandlerFlowStatus.INCOMPLETE);
        verify(response).sendRedirect(anyString());
    }

    @Test(description = "Returns SUCCESS_COMPLETED when a mandatory purpose already has an ACTIVE receipt.")
    public void testPreConsentSkipsMandatoryPurposeWithActiveReceipt() throws Exception {

        Purpose mandatory = buildMandatoryPurpose(PURPOSE_UUID_1, VERSION_UUID_1);
        when(consentManager.listPurposes(any(), anyInt(), anyInt()))
                .thenReturn(Collections.singletonList(mandatory));
        when(consentManager.listReceipts(eq(SUBJECT_ID), isNull(), eq(ACTIVE_STATE),
                eq(PURPOSE_UUID_1), eq(VERSION_UUID_1), eq(1), eq(0)))
                .thenReturn(Collections.singletonList(mock(Receipt.class)));

        PostAuthnHandlerFlowStatus status = handler.handle(request, response, context);

        assertEquals(status, PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED);
        verify(response, never()).sendRedirect(anyString());
    }

    @Test(description = "Redirect URL includes mandatoryPurposeIds param for mandatory unconsented purposes.")
    public void testRedirectUrlContainsMandatoryPurposeIdsParam() throws Exception {

        Purpose mandatory = buildMandatoryPurpose(PURPOSE_UUID_1, VERSION_UUID_1);
        when(consentManager.listPurposes(any(), anyInt(), anyInt()))
                .thenReturn(Collections.singletonList(mandatory));
        when(consentManager.listReceipts(eq(SUBJECT_ID), isNull(), eq(ACTIVE_STATE),
                eq(PURPOSE_UUID_1), eq(VERSION_UUID_1), eq(1), eq(0)))
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

    // -------------------------------------------------------------------------
    // handlePrePolicyConsent — optional purposes
    // -------------------------------------------------------------------------

    @Test(description = "Returns INCOMPLETE and redirects when an optional purpose has no receipt of any state.")
    public void testPreConsentRedirectsForOptionalPurposeWithNoReceipt() throws Exception {

        Purpose optional = buildOptionalPurpose(PURPOSE_UUID_1, VERSION_UUID_1);
        when(consentManager.listPurposes(any(), anyInt(), anyInt()))
                .thenReturn(Collections.singletonList(optional));
        // No receipt of any state.
        when(consentManager.listReceipts(eq(SUBJECT_ID), isNull(), isNull(),
                eq(PURPOSE_UUID_1), eq(VERSION_UUID_1), eq(1), eq(0)))
                .thenReturn(Collections.emptyList());

        PostAuthnHandlerFlowStatus status = handler.handle(request, response, context);

        assertEquals(status, PostAuthnHandlerFlowStatus.INCOMPLETE);
        verify(response).sendRedirect(anyString());
    }

        @Test(description = "Returns SUCCESS_COMPLETED when an optional purpose already has a REJECTED receipt.")
    public void testPreConsentSkipsOptionalPurposeWithRejectedReceipt() throws Exception {

        Purpose optional = buildOptionalPurpose(PURPOSE_UUID_1, VERSION_UUID_1);
        when(consentManager.listPurposes(any(), anyInt(), anyInt()))
                .thenReturn(Collections.singletonList(optional));
        // A REJECTED receipt already exists — user saw and skipped this version.
        when(consentManager.listReceipts(eq(SUBJECT_ID), isNull(), isNull(),
                eq(PURPOSE_UUID_1), eq(VERSION_UUID_1), eq(1), eq(0)))
                .thenReturn(Collections.singletonList(mock(Receipt.class)));

        PostAuthnHandlerFlowStatus status = handler.handle(request, response, context);

        assertEquals(status, PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED);
        verify(response, never()).sendRedirect(anyString());
    }

    @Test(description = "Redirect URL includes optionalPurposeIds param for optional unseen purposes.")
    public void testRedirectUrlContainsOptionalPurposeIdsParam() throws Exception {

        Purpose optional = buildOptionalPurpose(PURPOSE_UUID_1, VERSION_UUID_1);
        when(consentManager.listPurposes(any(), anyInt(), anyInt()))
                .thenReturn(Collections.singletonList(optional));
        when(consentManager.listReceipts(eq(SUBJECT_ID), isNull(), isNull(),
                eq(PURPOSE_UUID_1), eq(VERSION_UUID_1), eq(1), eq(0)))
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
        when(consentManager.listPurposes(any(), anyInt(), anyInt()))
                .thenReturn(Arrays.asList(mandatory, optional));
        when(consentManager.listReceipts(eq(SUBJECT_ID), isNull(), eq(ACTIVE_STATE),
                eq(PURPOSE_UUID_1), eq(VERSION_UUID_1), eq(1), eq(0)))
                .thenReturn(Collections.emptyList());
        when(consentManager.listReceipts(eq(SUBJECT_ID), isNull(), isNull(),
                eq(PURPOSE_UUID_2), eq(VERSION_UUID_2), eq(1), eq(0)))
                .thenReturn(Collections.emptyList());

        handler.handle(request, response, context);

        verify(response).sendRedirect(
                org.mockito.ArgumentMatchers.argThat(url ->
                        url.contains("mandatoryPurposeIds") && url.contains(PURPOSE_UUID_1) &&
                        url.contains("optionalPurposeIds") && url.contains(PURPOSE_UUID_2)));
    }

    @Test(description = "Throws PostAuthenticationFailedException when ConsentManager throws during pre-consent.")
    public void testPreConsentWrapsConsentManagerException() throws Exception {

        when(consentManager.listPurposes(any(), anyInt(), anyInt()))
                .thenThrow(new ConsentManagementException("DB error", "CM_00001"));

        try {
            handler.handle(request, response, context);
            throw new AssertionError("Expected PostAuthenticationFailedException was not thrown.");
        } catch (PostAuthenticationFailedException e) {
            // Expected.
        }
    }

    // -------------------------------------------------------------------------
    // handlePostPolicyConsent — mandatory purposes
    // -------------------------------------------------------------------------

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
        when(request.getParameter("skippedOptionalIds")).thenReturn(null);

        PostAuthnHandlerFlowStatus status = handler.handle(request, response, context);

        assertEquals(status, PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED);
        verify(consentManager, times(2)).addConsent(eq(SUBJECT_ID), eq("SYSTEM"), eq(TENANT_DOMAIN),
                anyString(), any(), eq(true));
    }

    @Test(description = "Returns SUCCESS_COMPLETED with no consent recording when both purpose lists are empty.")
    @SuppressWarnings("unchecked")
    public void testPostConsentReturnsSuccessWithEmptyPurposeLists() throws Exception {

        context.addParameter("policyConsentPrompted", true);
        context.addParameter("policyMandatoryUnconsentedIds", Collections.emptyList());
        context.addParameter("policyOptionalUnconsentedIds", Collections.emptyList());
        when(request.getParameter("consent")).thenReturn("approve");
        when(request.getParameter("skippedOptionalIds")).thenReturn(null);

        PostAuthnHandlerFlowStatus status = handler.handle(request, response, context);

        assertEquals(status, PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED);
        verify(consentManager, never()).addConsent(anyString(), anyString(), anyString(), anyString(), any(), eq(true));
        verify(consentManager, never()).addConsent(any(ReceiptInput.class));
    }

    // -------------------------------------------------------------------------
    // handlePostPolicyConsent — optional purposes
    // -------------------------------------------------------------------------

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
        when(request.getParameter("skippedOptionalIds")).thenReturn(null);

        PostAuthnHandlerFlowStatus status = handler.handle(request, response, context);

        assertEquals(status, PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED);
        verify(consentManager, times(1)).addConsent(eq(SUBJECT_ID), eq("SYSTEM"), eq(TENANT_DOMAIN),
                anyString(), any(), eq(true));
        verify(consentManager, never()).addConsent(any(ReceiptInput.class));
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
        when(request.getParameter("skippedOptionalIds")).thenReturn(PURPOSE_UUID_1);

        PostAuthnHandlerFlowStatus status = handler.handle(request, response, context);

        assertEquals(status, PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED);
        // REJECTED path calls addConsent(ReceiptInput), not the String-based helper.
        verify(consentManager, never()).addConsent(eq(SUBJECT_ID), eq("SYSTEM"), eq(TENANT_DOMAIN),
                anyString(), any(), eq(true));
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
        // UUID_1 skipped, UUID_2 accepted.
        when(request.getParameter("skippedOptionalIds")).thenReturn(PURPOSE_UUID_1);

        PostAuthnHandlerFlowStatus status = handler.handle(request, response, context);

        assertEquals(status, PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED);
        // UUID_2 accepted → String-based helper (ACTIVE).
        verify(consentManager, times(1)).addConsent(eq(SUBJECT_ID), eq("SYSTEM"), eq(TENANT_DOMAIN),
                anyString(), any(), eq(true));
        // UUID_1 skipped → ReceiptInput with REJECTED.
        ArgumentCaptor<ReceiptInput> captor = ArgumentCaptor.forClass(ReceiptInput.class);
        verify(consentManager, times(1)).addConsent(captor.capture());
        assertEquals(captor.getValue().getState(), REJECTED_STATE);
    }

    // -------------------------------------------------------------------------
    // handlePostPolicyConsent — PII category creation
    // -------------------------------------------------------------------------

    @Test(description = "Creates a new PII category when one does not already exist.")
    @SuppressWarnings("unchecked")
    public void testPostConsentCreatesNewPIICategoryWhenAbsent() throws Exception {

        ConsentManagementClientException notFoundException = mock(ConsentManagementClientException.class);
        when(notFoundException.getErrorCode()).thenReturn(ERROR_CODE_PII_CAT_NAME_INVALID.getCode());
        when(consentManager.getPIICategoryByName("Policy")).thenThrow(notFoundException);
        PIICategory newCategory = mock(PIICategory.class);
        when(newCategory.getId()).thenReturn(2);
        when(consentManager.addPIICategory(any())).thenReturn(newCategory);

        context.addParameter("policyConsentPrompted", true);
        context.addParameter("policyMandatoryUnconsentedIds",
                Collections.singletonList(PURPOSE_UUID_1));
        context.addParameter("policyOptionalUnconsentedIds", Collections.emptyList());
        when(request.getParameter("consent")).thenReturn("approve");
        when(request.getParameter("skippedOptionalIds")).thenReturn(null);

        PostAuthnHandlerFlowStatus status = handler.handle(request, response, context);

        assertEquals(status, PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED);
        verify(consentManager).addPIICategory(any());
    }

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

    private Purpose buildMandatoryPurpose(String purposeUuid, String versionUuid) {

        PurposePIICategory mandatoryElement = mock(PurposePIICategory.class);
        when(mandatoryElement.getMandatory()).thenReturn(true);

        PurposeVersion version = mock(PurposeVersion.class);
        when(version.getUuid()).thenReturn(versionUuid);
        when(version.getPurposePIICategories()).thenReturn(Collections.singletonList(mandatoryElement));

        Purpose purpose = mock(Purpose.class);
        when(purpose.getUuid()).thenReturn(purposeUuid);
        when(purpose.getLatestVersion()).thenReturn(version);
        return purpose;
    }

    private Purpose buildOptionalPurpose(String purposeUuid, String versionUuid) {

        PurposeVersion version = mock(PurposeVersion.class);
        when(version.getUuid()).thenReturn(versionUuid);
        when(version.getPurposePIICategories()).thenReturn(Collections.emptyList());

        Purpose purpose = mock(Purpose.class);
        when(purpose.getUuid()).thenReturn(purposeUuid);
        when(purpose.getId()).thenReturn(42);
        when(purpose.getLatestVersion()).thenReturn(version);
        return purpose;
    }
}
