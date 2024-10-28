package org.wso2.carbon.identity.mgt.endpoint.util;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.mgt.endpoint.util.client.ApiClient;
import org.wso2.carbon.identity.mgt.endpoint.util.client.ApiException;
import org.wso2.carbon.identity.mgt.endpoint.util.client.api.RecoveryApiV2;
import org.wso2.carbon.identity.mgt.endpoint.util.client.model.recovery.v2.AccountRecoveryType;
import org.wso2.carbon.identity.mgt.endpoint.util.client.model.recovery.v2.ConfirmRequest;
import org.wso2.carbon.identity.mgt.endpoint.util.client.model.recovery.v2.ConfirmResponse;
import org.wso2.carbon.identity.mgt.endpoint.util.client.model.recovery.v2.RecoveryInitRequest;
import org.wso2.carbon.identity.mgt.endpoint.util.client.model.recovery.v2.RecoveryRequest;
import org.wso2.carbon.identity.mgt.endpoint.util.client.model.recovery.v2.RecoveryResponse;
import org.wso2.carbon.identity.mgt.endpoint.util.client.model.recovery.v2.ResendRequest;
import org.wso2.carbon.identity.mgt.endpoint.util.client.model.recovery.v2.ResendResponse;
import org.wso2.carbon.identity.mgt.endpoint.util.client.model.recovery.v2.ResetRequest;
import org.wso2.carbon.identity.mgt.endpoint.util.client.model.recovery.v2.ResetResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class RecoveryApiV2Test {

    public static final String RECOVERY_API_V2_RELATIVE_PATH = "/api/users/v2/recovery";
    private static final String tenantDomain = "carbon.super";
    private static final String POST = "POST";
    private static final String PATH_PASSWORD_RECOVERY_INIT = "/password/init";
    private static final String PATH_PASSWORD_RECOVERY_RECOVER = "/password/recover";
    private static final String PATH_PASSWORD_RECOVERY_RESEND = "/password/resend";
    private static final String PATH_PASSWORD_RECOVERY_CONFIRM = "/password/confirm";
    private static final String PATH_PASSWORD_RECOVERY_RESET = "/password/reset";
    private static final String PATH_USERNAME_RECOVERY_INIT = "/username/init";
    private static final String PATH_USERNAME_RECOVERY_RECOVER = "/username/recover";
    // Test values.
    private static final String TEST_ACCEPT_HEADER = "testAcceptHeader";
    private static final String TEST_CONTENT_HEADER = "testContentHeader";
    private static final String TEST_PATH = "testPath";
    private static final Map<String, String> headers = new HashMap<>();

    @Mock
    ApiClient apiClient;

    RecoveryApiV2 recoveryApiV2;

    @BeforeMethod
    public void setup() {

        MockitoAnnotations.openMocks(this);

        when(apiClient.selectHeaderAccept(any())).thenReturn(TEST_ACCEPT_HEADER);
        when(apiClient.selectHeaderContentType(any())).thenReturn(TEST_CONTENT_HEADER);

        try (MockedStatic<IdentityManagementEndpointUtil> identityManagementEndpointUtilMockedStatic =
                     mockStatic(IdentityManagementEndpointUtil.class)) {
            identityManagementEndpointUtilMockedStatic.when(
                            () -> IdentityManagementEndpointUtil.buildEndpointUrl(anyString()))
                    .thenReturn(TEST_PATH);

            recoveryApiV2 = new RecoveryApiV2(apiClient);
        }

    }

    @Test
    public void testInitiateUsernameRecovery() throws ApiException {

        List<AccountRecoveryType> expected = new ArrayList<>();
        expected.add(new AccountRecoveryType());

        when(apiClient.invokeAPI(eq(PATH_USERNAME_RECOVERY_INIT), eq(POST), any(), any(), any(), any(),
                eq(TEST_ACCEPT_HEADER),
                eq(TEST_CONTENT_HEADER),
                any(), any())).thenReturn(expected);

        try (MockedStatic<IdentityManagementEndpointUtil> identityManagementEndpointUtilMockedStatic =
                     mockStatic(IdentityManagementEndpointUtil.class)) {
            identityManagementEndpointUtilMockedStatic.when(
                            () -> IdentityManagementEndpointUtil.getBasePath(tenantDomain, RECOVERY_API_V2_RELATIVE_PATH))
                    .thenReturn(TEST_PATH);

            RecoveryApiV2 recoveryApiV2 = new RecoveryApiV2(apiClient);
            List<AccountRecoveryType> result =
                    recoveryApiV2.initiateUsernameRecovery(new RecoveryInitRequest(), tenantDomain, headers);
            assertEquals(result, expected);
        }
    }

    @Test
    public void testRecoverUsername() throws ApiException {

        RecoveryResponse expected = new RecoveryResponse();
        when(apiClient.invokeAPI(eq(PATH_USERNAME_RECOVERY_RECOVER), eq(POST), any(), any(), any(), any(),
                eq(TEST_ACCEPT_HEADER),
                eq(TEST_CONTENT_HEADER),
                any(), any())).thenReturn(expected);

        try (MockedStatic<IdentityManagementEndpointUtil> identityManagementEndpointUtilMockedStatic =
                     mockStatic(IdentityManagementEndpointUtil.class)) {
            identityManagementEndpointUtilMockedStatic.when(
                            () -> IdentityManagementEndpointUtil.getBasePath(tenantDomain, RECOVERY_API_V2_RELATIVE_PATH))
                    .thenReturn(TEST_PATH);

            RecoveryApiV2 recoveryApiV2 = new RecoveryApiV2(apiClient);
            RecoveryResponse result = recoveryApiV2.recoverUsername(new RecoveryRequest(), tenantDomain, headers);
            assertEquals(result, expected);
        }
    }

    @Test
    public void testInitiatePasswordRecovery() throws ApiException {

        List<AccountRecoveryType> expected = new ArrayList<>();
        expected.add(new AccountRecoveryType());

        when(apiClient.invokeAPI(eq(PATH_PASSWORD_RECOVERY_INIT), eq(POST), any(), any(), any(), any(),
                eq(TEST_ACCEPT_HEADER),
                eq(TEST_CONTENT_HEADER),
                any(), any())).thenReturn(expected);

        try (MockedStatic<IdentityManagementEndpointUtil> identityManagementEndpointUtilMockedStatic =
                     mockStatic(IdentityManagementEndpointUtil.class)) {
            identityManagementEndpointUtilMockedStatic.when(
                            () -> IdentityManagementEndpointUtil.getBasePath(tenantDomain, RECOVERY_API_V2_RELATIVE_PATH))
                    .thenReturn(TEST_PATH);

            RecoveryApiV2 recoveryApiV2 = new RecoveryApiV2(apiClient);
            List<AccountRecoveryType> result =
                    recoveryApiV2.initiatePasswordRecovery(new RecoveryInitRequest(), tenantDomain,
                            headers);
            assertEquals(result, expected);
        }
    }

    @Test
    public void testRecoverPassword() throws ApiException {

        RecoveryResponse expected = new RecoveryResponse();
        when(apiClient.invokeAPI(eq(PATH_PASSWORD_RECOVERY_RECOVER), eq(POST), any(), any(), any(), any(),
                eq(TEST_ACCEPT_HEADER),
                eq(TEST_CONTENT_HEADER),
                any(), any())).thenReturn(expected);

        try (MockedStatic<IdentityManagementEndpointUtil> identityManagementEndpointUtilMockedStatic =
                     mockStatic(IdentityManagementEndpointUtil.class)) {
            identityManagementEndpointUtilMockedStatic.when(
                            () -> IdentityManagementEndpointUtil.getBasePath(tenantDomain, RECOVERY_API_V2_RELATIVE_PATH))
                    .thenReturn(TEST_PATH);

            RecoveryApiV2 recoveryApiV2 = new RecoveryApiV2(apiClient);
            RecoveryResponse result = recoveryApiV2.recoverPassword(new RecoveryRequest(), tenantDomain, headers);
            assertEquals(result, expected);
        }
    }

    @Test
    public void testResendPasswordNotification() throws ApiException {

        ResendResponse expected = new ResendResponse();
        when(apiClient.invokeAPI(eq(PATH_PASSWORD_RECOVERY_RESEND), eq(POST), any(), any(), any(), any(),
                eq(TEST_ACCEPT_HEADER),
                eq(TEST_CONTENT_HEADER),
                any(), any())).thenReturn(expected);

        try (MockedStatic<IdentityManagementEndpointUtil> identityManagementEndpointUtilMockedStatic =
                     mockStatic(IdentityManagementEndpointUtil.class)) {
            identityManagementEndpointUtilMockedStatic.when(
                            () -> IdentityManagementEndpointUtil.getBasePath(tenantDomain, RECOVERY_API_V2_RELATIVE_PATH))
                    .thenReturn(TEST_PATH);

            RecoveryApiV2 recoveryApiV2 = new RecoveryApiV2(apiClient);
            ResendResponse result =
                    recoveryApiV2.resendPasswordNotification(new ResendRequest(), tenantDomain, headers);
            assertEquals(result, expected);
        }
    }

    @Test
    public void testConfirmPasswordRecovery() throws ApiException {

        ConfirmResponse expected = new ConfirmResponse();
        when(apiClient.invokeAPI(eq(PATH_PASSWORD_RECOVERY_CONFIRM), eq(POST), any(), any(), any(), any(),
                eq(TEST_ACCEPT_HEADER),
                eq(TEST_CONTENT_HEADER),
                any(), any())).thenReturn(expected);

        try (MockedStatic<IdentityManagementEndpointUtil> identityManagementEndpointUtilMockedStatic =
                     mockStatic(IdentityManagementEndpointUtil.class)) {
            identityManagementEndpointUtilMockedStatic.when(
                            () -> IdentityManagementEndpointUtil.getBasePath(tenantDomain, RECOVERY_API_V2_RELATIVE_PATH))
                    .thenReturn(TEST_PATH);

            RecoveryApiV2 recoveryApiV2 = new RecoveryApiV2(apiClient);
            ConfirmResponse result = recoveryApiV2.confirmPasswordRecovery(new ConfirmRequest(), tenantDomain, headers);
            assertEquals(result, expected);
        }
    }

    @Test
    public void testResetUserPassword() throws ApiException {

        ResetResponse expected = new ResetResponse();
        when(apiClient.invokeAPI(eq(PATH_PASSWORD_RECOVERY_RESET), eq(POST), any(), any(), any(), any(),
                eq(TEST_ACCEPT_HEADER),
                eq(TEST_CONTENT_HEADER),
                any(), any())).thenReturn(expected);

        try (MockedStatic<IdentityManagementEndpointUtil> identityManagementEndpointUtilMockedStatic =
                     mockStatic(IdentityManagementEndpointUtil.class)) {
            identityManagementEndpointUtilMockedStatic.when(
                            () -> IdentityManagementEndpointUtil.getBasePath(tenantDomain, RECOVERY_API_V2_RELATIVE_PATH))
                    .thenReturn(TEST_PATH);

            recoveryApiV2.setApiClient(apiClient);
            ResetResponse result = recoveryApiV2.resetUserPassword(new ResetRequest(), tenantDomain, headers);
            assertEquals(result, expected);
        }
    }

    @Test
    public void testGetApiClient() {

        assertEquals(recoveryApiV2.getApiClient(), apiClient);

    }

    @Test
    public void testSetApiClient() {

        recoveryApiV2.setApiClient(apiClient);
        assertEquals(recoveryApiV2.getApiClient(), apiClient);
    }

}
