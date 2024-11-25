/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.action.management.util;

import org.json.JSONObject;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.action.management.model.Action;
import org.wso2.carbon.identity.action.management.model.Authentication;
import org.wso2.carbon.identity.action.management.model.EndpointConfig;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.utils.AuditLog;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Unit test class for ActionManagementAuditLogger class.
 */
@WithCarbonHome
public class ActionManagementAuditLoggerTest {

    private ActionManagementAuditLogger auditLogger;
    private Action action;
    private CarbonContext carbonContext;
    private MockedStatic<CarbonContext> carbonContextMockedStatic;
    private MockedStatic<IdentityUtil> identityUtil;
    private MockedStatic<IdentityTenantUtil> identityTenantUtil;
    private MockedStatic<LoggerUtils> loggerUtilsMockedStatic;

    private static final String ADD_ACTION = "add-action";
    private static final String UPDATE_ACTION = "update-action";
    private static final String DELETE_ACTION = "delete-action";

    @BeforeMethod
    public void setUp() throws NoSuchFieldException, IllegalAccessException {

        MockitoAnnotations.openMocks(this);
        auditLogger = new ActionManagementAuditLogger();
        identityUtil = mockStatic(IdentityUtil.class);
        identityTenantUtil = mockStatic(IdentityTenantUtil.class);

        carbonContextMockedStatic = mockStatic(CarbonContext.class);
        carbonContext = mock(CarbonContext.class);
        carbonContextMockedStatic.when(CarbonContext::getThreadLocalCarbonContext).thenReturn(carbonContext);
        when(carbonContext.getUsername()).thenReturn("testUser");
        when(carbonContext.getTenantDomain()).thenReturn("carbon.super");
        identityUtil.when(() -> IdentityUtil.getInitiatorId("testUser", "carbon.super")).
                thenReturn("initiator-id-test");

        loggerUtilsMockedStatic = mockStatic(LoggerUtils.class);
        loggerUtilsMockedStatic.when(LoggerUtils::isEnableV2AuditLogs).thenReturn(true);
        loggerUtilsMockedStatic.when(() -> LoggerUtils.jsonObjectToMap(any(JSONObject.class))).thenCallRealMethod();

        // Mock Action
        action = mock(Action.class);
        when(action.getId()).thenReturn("action-test-id");
        when(action.getName()).thenReturn("Test Action");
        when(action.getDescription()).thenReturn("This is a test action.");
        when(action.getType()).thenReturn(Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN);
        Map<String, String> authProperties = new HashMap<>();
        authProperties.put("accessToken", "W*********t");
        Authentication auth = new Authentication.AuthenticationBuilder().type(
                Authentication.Type.BEARER).properties(authProperties).build();
        when(action.getEndpoint()).thenReturn(new EndpointConfig.EndpointConfigBuilder().
                uri("https://test.com").
                authentication(auth).build());
        when(action.getStatus()).thenReturn(Action.Status.ACTIVE);
    }

    @AfterMethod
    public void tearDown() {

        auditLogger = null;
        action = null;
        carbonContextMockedStatic.close();
        identityUtil.close();
        identityTenantUtil.close();
        loggerUtilsMockedStatic.close();
    }

    @Test
    public void testPrintAuditLogWithAction() throws NoSuchFieldException, IllegalAccessException {

        ActionManagementAuditLogger.Operation operation = ActionManagementAuditLogger.Operation.ADD;
        auditLogger.printAuditLog(operation, action);
        AuditLog.AuditLogBuilder capturedArg = captureTriggerAuditLogEventArgs();

        Assert.assertNotNull(capturedArg);
        assertActionData(capturedArg);
        assertAuditLoggerData(capturedArg, ADD_ACTION);
    }

    @Test
    public void testPrintAuditLogWithActionId() throws NoSuchFieldException, IllegalAccessException {

        ActionManagementAuditLogger.Operation operation = ActionManagementAuditLogger.Operation.UPDATE;
        auditLogger.printAuditLog(operation, action.getId(), action);
        AuditLog.AuditLogBuilder capturedArg = captureTriggerAuditLogEventArgs();

        Assert.assertNotNull(capturedArg);
        assertActionData(capturedArg);
        assertAuditLoggerData(capturedArg, UPDATE_ACTION);
    }

    @Test
    public void testPrintAuditLogWithActionTypeAndId() throws NoSuchFieldException, IllegalAccessException {

        ActionManagementAuditLogger.Operation operation = ActionManagementAuditLogger.Operation.DELETE;
        auditLogger.printAuditLog(operation, action.getType().name(), action.getId());
        AuditLog.AuditLogBuilder capturedArg = captureTriggerAuditLogEventArgs();

        Assert.assertNotNull(capturedArg);
        Assert.assertEquals(extractMapByField("ActionId", capturedArg), "action-test-id");
        Assert.assertEquals(extractMapByField("ActionType", capturedArg),
                Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN.getActionType());
        assertAuditLoggerData(capturedArg, DELETE_ACTION);

    }

    /**
     * Capture the arguments passed to the triggerAuditLogEvent method in the {@link LoggerUtils} class.
     * The captured {@code AuditLogBuilder} contains all the necessary
     * information that will be logged, allowing verification of audit log data.
     *
     * @return The captured {@link AuditLog.AuditLogBuilder} instance containing the data to be logged.
     */
    private AuditLog.AuditLogBuilder captureTriggerAuditLogEventArgs() {

        ArgumentCaptor<AuditLog.AuditLogBuilder> auditLogBuilderCaptor = ArgumentCaptor.
                forClass(AuditLog.AuditLogBuilder.class);
        loggerUtilsMockedStatic.verify(() -> LoggerUtils.triggerAuditLogEvent(auditLogBuilderCaptor.capture()));
        return auditLogBuilderCaptor.getValue();
    }

    /**
     * Extract the specific field name from the provided {@link AuditLog.AuditLogBuilder} instance.
     *
     * @param fieldName       Name of the field to be extracted.
     * @param auditLogBuilder {@link AuditLog.AuditLogBuilder} instance.
     * @return Value of the extracted field.
     * @throws NoSuchFieldException   if the provided field does not exist.
     * @throws IllegalAccessException if the provided field is not accessible.
     */
    private String extractMapByField(String fieldName, AuditLog.AuditLogBuilder auditLogBuilder)
            throws NoSuchFieldException, IllegalAccessException {

        Field dataField = AuditLog.AuditLogBuilder.class.getDeclaredField("data");
        dataField.setAccessible(true);
        Map<String, Object> dataMap = (Map<String, Object>) dataField.get(auditLogBuilder);
        return (String) dataMap.get(fieldName);
    }

    /**
     * Extract the specific field name from the provided map.
     *
     * @param fieldName       Name of the field to be extracted.
     * @param auditLogBuilder {@link AuditLog.AuditLogBuilder} instance.
     * @return Value of the extracted field.
     * @throws NoSuchFieldException   if the provided field does not exist.
     * @throws IllegalAccessException if the provided field is not accessible.
     */
    private String extractEndpointMapByField(String fieldName, AuditLog.AuditLogBuilder auditLogBuilder)
            throws NoSuchFieldException, IllegalAccessException {

        Field dataField = AuditLog.AuditLogBuilder.class.getDeclaredField("data");
        dataField.setAccessible(true);
        Map<String, Object> dataMap = (Map<String, Object>) dataField.get(auditLogBuilder);
        Map<String, Object> endpointConfigMap = (Map<String, Object>) dataMap.get("EndpointConfiguration");
        return (String) endpointConfigMap.get(fieldName);
    }

    /**
     * Extract field.
     *
     * @param fieldName       Name of the field to be extracted.
     * @param auditLogBuilder {@link AuditLog.AuditLogBuilder} instance.
     * @return Value of the extracted field.
     * @throws NoSuchFieldException   if the provided field does not exist.
     * @throws IllegalAccessException if the provided field is not accessible.
     */
    private String extractField(String fieldName, AuditLog.AuditLogBuilder auditLogBuilder)
            throws NoSuchFieldException, IllegalAccessException {

        Field dataField = AuditLog.AuditLogBuilder.class.getDeclaredField(fieldName);
        dataField.setAccessible(true);
        return (String) dataField.get(auditLogBuilder);
    }

    /**
     * Assert data fields related to the Action object of the captured audit logger.
     *
     * @param auditLogBuilder {@link AuditLog.AuditLogBuilder} instance.
     * @throws NoSuchFieldException   if the provided field does not exist.
     * @throws IllegalAccessException if the provided field is not accessible.
     */
    private void assertActionData(AuditLog.AuditLogBuilder auditLogBuilder)
            throws NoSuchFieldException, IllegalAccessException {

        Assert.assertEquals(extractMapByField("ActionId", auditLogBuilder), "action-test-id");
        Assert.assertEquals(extractMapByField("ActionName", auditLogBuilder), "Test Action");
        Assert.assertEquals(extractMapByField("ActionType", auditLogBuilder),
                Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN.getActionType());
        Assert.assertEquals(extractMapByField("ActionStatus", auditLogBuilder), Action.Status.ACTIVE.name());
        Assert.assertEquals(extractMapByField("ActionDescription", auditLogBuilder),
                "This is a test action.");
        Assert.assertEquals(extractEndpointMapByField("AuthenticationScheme", auditLogBuilder),
                Authentication.Type.BEARER.getName());
        Assert.assertEquals(extractEndpointMapByField("EndpointUri", auditLogBuilder),
                "https://test.com");
    }

    /**
     * Assert generic data fields in audit logger.
     *
     * @param auditLogBuilder {@link AuditLog.AuditLogBuilder} instance.
     * @param operation       Operation to be logged.
     * @throws NoSuchFieldException   if the provided field does not exist.
     * @throws IllegalAccessException if the provided field is not accessible.
     */
    private void assertAuditLoggerData(AuditLog.AuditLogBuilder auditLogBuilder,
                                       String operation)
            throws NoSuchFieldException, IllegalAccessException {

        Assert.assertEquals(extractField("initiatorId", auditLogBuilder), "initiator-id-test");
        Assert.assertEquals(extractField("targetId", auditLogBuilder), "System");
        Assert.assertEquals(extractField("targetType", auditLogBuilder), "Action");
        switch (operation) {
            case ADD_ACTION:
                Assert.assertEquals(extractField("action", auditLogBuilder), "add-action");
                break;
            case UPDATE_ACTION:
                Assert.assertEquals(extractField("action", auditLogBuilder), "update-action");
                break;
            case DELETE_ACTION:
                Assert.assertEquals(extractField("action", auditLogBuilder), "delete-action");
                break;
        }
    }
}

