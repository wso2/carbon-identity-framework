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
import org.wso2.carbon.identity.action.management.dao.model.ActionDTO;
import org.wso2.carbon.identity.action.management.model.Action;
import org.wso2.carbon.identity.action.management.model.Authentication;
import org.wso2.carbon.identity.action.management.model.EndpointConfig;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.certificate.management.model.Certificate;
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
import static org.wso2.carbon.identity.action.management.util.TestUtil.CERTIFICATE_ID;
import static org.wso2.carbon.identity.action.management.util.TestUtil.CERTIFICATE_NAME;
import static org.wso2.carbon.identity.action.management.util.TestUtil.CERTIFICATE_PROPERTY_NAME;
import static org.wso2.carbon.identity.action.management.util.TestUtil.PASSWORD_SHARING_TYPE_PROPERTY_NAME;
import static org.wso2.carbon.identity.action.management.util.TestUtil.PRE_UPDATE_PASSWORD_ACTION_ID;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_ACCESS_TOKEN;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_ACTION_DESCRIPTION;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_ACTION_NAME;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_ACTION_URI;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_CERTIFICATE;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_PASSWORD_SHARING_TYPE;

/**
 * Unit test class for ActionManagementAuditLogger class.
 */
@WithCarbonHome
public class ActionManagementAuditLoggerTest {

    private ActionManagementAuditLogger auditLogger;
    private ActionDTO actionDTO;
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
        loggerUtilsMockedStatic.when(() -> LoggerUtils.getMaskedContent(any(String.class))).thenCallRealMethod();

        Map<String, Object> actionProperties = new HashMap<>();
        actionProperties.put(PASSWORD_SHARING_TYPE_PROPERTY_NAME, TEST_PASSWORD_SHARING_TYPE);
        actionProperties.put(CERTIFICATE_PROPERTY_NAME, new Certificate.Builder()
                .id(CERTIFICATE_ID).name(CERTIFICATE_NAME)
                .certificateContent(TEST_CERTIFICATE).build());

        actionDTO = new ActionDTO.Builder()
                .id(PRE_UPDATE_PASSWORD_ACTION_ID)
                .name(TEST_ACTION_NAME)
                .description(TEST_ACTION_DESCRIPTION)
                .type(Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN)
                .status(Action.Status.ACTIVE)
                .endpoint(new EndpointConfig.EndpointConfigBuilder()
                        .uri(TEST_ACTION_URI)
                        .authentication(new Authentication.BearerAuthBuilder(TEST_ACCESS_TOKEN).build())
                        .build())
                .properties(actionProperties)
                .build();
    }

    @AfterMethod
    public void tearDown() {

        auditLogger = null;
        actionDTO = null;
        carbonContextMockedStatic.close();
        identityUtil.close();
        identityTenantUtil.close();
        loggerUtilsMockedStatic.close();
    }

    @Test
    public void testPrintAuditLogWithAction() throws NoSuchFieldException, IllegalAccessException {

        ActionManagementAuditLogger.Operation operation = ActionManagementAuditLogger.Operation.ADD;
        auditLogger.printAuditLog(operation, actionDTO);
        AuditLog.AuditLogBuilder capturedArg = captureTriggerAuditLogEventArgs();

        Assert.assertNotNull(capturedArg);
        assertActionData(capturedArg);
        assertAuditLoggerData(capturedArg, ADD_ACTION);
    }

    @Test
    public void testPrintAuditLogWithActionId() throws NoSuchFieldException, IllegalAccessException {

        ActionManagementAuditLogger.Operation operation = ActionManagementAuditLogger.Operation.UPDATE;
        auditLogger.printAuditLog(operation, actionDTO.getId(), actionDTO);
        AuditLog.AuditLogBuilder capturedArg = captureTriggerAuditLogEventArgs();

        Assert.assertNotNull(capturedArg);
        assertActionData(capturedArg);
        assertAuditLoggerData(capturedArg, UPDATE_ACTION);
    }

    @Test
    public void testPrintAuditLogWithActionTypeAndId() throws NoSuchFieldException, IllegalAccessException {

        ActionManagementAuditLogger.Operation operation = ActionManagementAuditLogger.Operation.DELETE;
        auditLogger.printAuditLog(operation, actionDTO.getType().name(), actionDTO.getId());
        AuditLog.AuditLogBuilder capturedArg = captureTriggerAuditLogEventArgs();

        Assert.assertNotNull(capturedArg);
        Assert.assertEquals(extractMapByField("ActionId", capturedArg), actionDTO.getId());
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
    private void assertActionData(AuditLog.AuditLogBuilder auditLogBuilder) throws NoSuchFieldException,
            IllegalAccessException {

        Field dataField = AuditLog.AuditLogBuilder.class.getDeclaredField("data");
        dataField.setAccessible(true);
        Map<String, Object> dataMap = (Map<String, Object>) dataField.get(auditLogBuilder);
        Map<String, Object> endpointConfigMap = (Map<String, Object>) dataMap.get("EndpointConfiguration");
        Map<String, Object> propertiesMap = (Map<String, Object>) dataMap.get("Properties");

        Assert.assertEquals(dataMap.get("ActionId").toString(), PRE_UPDATE_PASSWORD_ACTION_ID);
        Assert.assertEquals(dataMap.get("ActionName").toString(), TEST_ACTION_NAME);
        Assert.assertEquals(dataMap.get("ActionType").toString(),
                Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN.getActionType());
        Assert.assertEquals(dataMap.get("ActionStatus").toString(), Action.Status.ACTIVE.name());
        Assert.assertEquals(dataMap.get("ActionDescription").toString(), TEST_ACTION_DESCRIPTION);

        Assert.assertEquals(endpointConfigMap.get("EndpointUri").toString(), TEST_ACTION_URI);
        Assert.assertEquals(endpointConfigMap.get("AuthenticationScheme").toString(),
                Authentication.Type.BEARER.getName());
        assertMasked(endpointConfigMap.get("AccessToken").toString());

        assertMasked(propertiesMap.get(PASSWORD_SHARING_TYPE_PROPERTY_NAME).toString());
        assertMasked(propertiesMap.get(CERTIFICATE_PROPERTY_NAME).toString());
    }

    /**
     * Assert masked data fields.
     *
     * @param value Value to be asserted.
     */
    private void assertMasked(String value) {

        Assert.assertTrue(value.contains("*"));
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

