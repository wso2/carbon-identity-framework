package org.wso2.carbon.identity.application.mgt.listener;

import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementClientException;
import org.wso2.carbon.identity.application.common.model.AuthorizedAPI;
import org.wso2.carbon.identity.application.mgt.ApplicationConstants;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.expectThrows;

public class ConsoleAuthorizedAPIListenerTest {

    private ConsoleAuthorizedAPIListener consoleAuthorizedAPIListener;

    MockedStatic<IdentityTenantUtil> identityTenantUtil;
    MockedStatic<ApplicationManagementService> applicationManagementService;
    MockedStatic<LoggerUtils> loggerUtils;

    @BeforeMethod
    public void setUp() throws Exception {

        identityTenantUtil = mockStatic(IdentityTenantUtil.class);
        applicationManagementService = mockStatic(ApplicationManagementService.class);
        loggerUtils = mockStatic(LoggerUtils.class);

        loggerUtils.when(() -> LoggerUtils.triggerAuditLogEvent(any(), anyBoolean()))
                .thenAnswer(inv -> null);

        consoleAuthorizedAPIListener = new ConsoleAuthorizedAPIListener();
        IdentityUtil.threadLocalProperties.remove();
    }

    @AfterMethod
    public void tearDown() throws Exception {

        IdentityUtil.threadLocalProperties.remove();

        identityTenantUtil.close();
        applicationManagementService.close();
        loggerUtils.close();
    }


    @DataProvider
    public Object[][] getPreAddAuthorizedAPITestData() {

        String appId1 = mockAppId();
        String appId2 = mockAppId();
        String appId3 = mockAppId();

        AuthorizedAPI authorizedAPI1 = getAuthorizedAPI("https://test.com/oauth2/token", appId1);
        AuthorizedAPI authorizedAPI2 = getAuthorizedAPI("https://test.com/applications", appId2);
        AuthorizedAPI authorizedAPI3 = getAuthorizedAPI("https://test.com/applications/local", appId3);

        return new Object[][]{
                // isTenantQualifiedURLsEnabled, appId, authorizedAPI, tenantDomain, consoleInboundKey, expectException
                { false, appId1, authorizedAPI1, ApplicationConstants.SUPER_TENANT,
                        ApplicationConstants.CONSOLE_APPLICATION_CLIENT_ID, true },
                { false, appId2, authorizedAPI2, "abc.com",
                        ApplicationConstants.CONSOLE_APPLICATION_CLIENT_ID + "_" + "abc.com", true },
                { true, appId3, authorizedAPI3, "abc.com", "client-id-test-1", false }
        };

    }

    @Test(dataProvider = "getPreAddAuthorizedAPITestData")
    public void testPreAddAuthorizedAPI(boolean isTenantQualifiedURLsEnabled, String appId,
                                        AuthorizedAPI authorizedAPI, String tenantDomain, String consoleInboundKey,
                                        boolean expectException) throws Exception {

        identityTenantUtil.when(IdentityTenantUtil::isTenantQualifiedUrlsEnabled)
                .thenReturn(isTenantQualifiedURLsEnabled);

        ApplicationManagementService applicationMgtSvc = mock(ApplicationManagementService.class);
        applicationManagementService.when(ApplicationManagementService::getInstance).thenReturn(applicationMgtSvc);
        when(applicationMgtSvc.getApplicationResourceIDByInboundKey(consoleInboundKey, "oauth2",
                tenantDomain)).thenReturn(appId);

        if (expectException) {
            IdentityApplicationManagementClientException e =
                    expectThrows(IdentityApplicationManagementClientException.class, () ->
                            consoleAuthorizedAPIListener.preAddAuthorizedAPI(appId, authorizedAPI, tenantDomain));

            assertEquals(e.getMessage(), "Adding authorized APIs to the console application is not allowed");
        } else {
            consoleAuthorizedAPIListener.preAddAuthorizedAPI(appId, authorizedAPI, tenantDomain);
        }
    }

    private AuthorizedAPI getAuthorizedAPI(String apiID, String appID) {

        AuthorizedAPI authorizedAPI = new AuthorizedAPI();
        authorizedAPI.setAPIId(apiID);
        authorizedAPI.setAppId(appID);

        return authorizedAPI;
    }

    private String mockAppId() {
        return UUID.randomUUID().toString();
    }
}
