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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.action.management.api.exception.ActionMgtException;
import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.action.management.api.model.ActionDTO;
import org.wso2.carbon.identity.action.management.api.model.ActionProperty;
import org.wso2.carbon.identity.action.management.api.model.ActionRule;
import org.wso2.carbon.identity.action.management.api.model.Authentication;
import org.wso2.carbon.identity.action.management.api.model.EndpointConfig;
import org.wso2.carbon.identity.action.management.internal.util.ActionDTOBuilder;
import org.wso2.carbon.identity.action.management.internal.util.ActionManagementAuditLogger;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.certificate.management.model.Certificate;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.rule.management.api.model.ANDCombinedRule;
import org.wso2.carbon.identity.rule.management.api.model.Expression;
import org.wso2.carbon.identity.rule.management.api.model.ORCombinedRule;
import org.wso2.carbon.identity.rule.management.api.model.Value;
import org.wso2.carbon.identity.rule.management.api.util.AuditLogBuilderForRule;
import org.wso2.carbon.utils.AuditLog;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.identity.action.management.util.TestUtil.CERTIFICATE_ID;
import static org.wso2.carbon.identity.action.management.util.TestUtil.CERTIFICATE_NAME;
import static org.wso2.carbon.identity.action.management.util.TestUtil.CERTIFICATE_PROPERTY_NAME;
import static org.wso2.carbon.identity.action.management.util.TestUtil.PASSWORD_SHARING_TYPE_PROPERTY_NAME;
import static org.wso2.carbon.identity.action.management.util.TestUtil.PRE_ISSUE_ACCESS_TOKEN_ACTION_ID;
import static org.wso2.carbon.identity.action.management.util.TestUtil.PRE_UPDATE_PASSWORD_ACTION_ID;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_ACCESS_TOKEN;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_ACTION_ALLOWED_HEADER_1;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_ACTION_ALLOWED_PARAMETER_1;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_ACTION_DESCRIPTION;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_ACTION_DESCRIPTION_UPDATED;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_ACTION_NAME;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_ACTION_NAME_UPDATED;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_ACTION_URI;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_ACTION_URI_UPDATED;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_API_KEY_HEADER;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_API_KEY_VALUE;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_CERTIFICATE;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_CERTIFICATE_UPDATED;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_CREATED_AT;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_DELETED_PROPERTY;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_PASSWORD;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_PASSWORD_SHARING_TYPE;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_PASSWORD_SHARING_TYPE_UPDATED;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_UPDATED_AT;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_USERNAME;

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
    private static final String ACTIVATE_ACTION = "activate-action";
    private static final String DEACTIVATE_ACTION = "deactivate-action";

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

        Map<String, ActionProperty> actionProperties = new HashMap<>();
        actionProperties.put(PASSWORD_SHARING_TYPE_PROPERTY_NAME,
                new ActionProperty.BuilderForService(TEST_PASSWORD_SHARING_TYPE).build());
        actionProperties.put(CERTIFICATE_PROPERTY_NAME, new ActionProperty.BuilderForService(new Certificate.Builder()
                .id(CERTIFICATE_ID).name(CERTIFICATE_NAME)
                .certificateContent(TEST_CERTIFICATE).build()).build());

        actionDTO = new ActionDTOBuilder()
                .id(PRE_UPDATE_PASSWORD_ACTION_ID)
                .name(TEST_ACTION_NAME)
                .description(TEST_ACTION_DESCRIPTION)
                .type(Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN)
                .status(Action.Status.ACTIVE)
                .endpoint(new EndpointConfig.EndpointConfigBuilder()
                        .uri(TEST_ACTION_URI)
                        .allowedHeaders(Collections.singletonList(TEST_ACTION_ALLOWED_HEADER_1))
                        .allowedParameters(Collections.singletonList(TEST_ACTION_ALLOWED_PARAMETER_1))
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

    @DataProvider
    public Object[][] addActionDataProvider() {

        Map<String, ActionProperty> actionProperties = new HashMap<>();
        actionProperties.put(PASSWORD_SHARING_TYPE_PROPERTY_NAME,
                new ActionProperty.BuilderForService(TEST_PASSWORD_SHARING_TYPE).build());
        actionProperties.put(CERTIFICATE_PROPERTY_NAME, new ActionProperty.BuilderForService(new Certificate.Builder()
                .id(CERTIFICATE_ID).name(CERTIFICATE_NAME)
                .certificateContent(TEST_CERTIFICATE).build()).build());

        return new Object[][]{
                // CREATE/ADD Operations - Test cases for ADD operation
                {ActionManagementAuditLogger.Operation.ADD,
                        new ActionDTOBuilder()
                                .id(PRE_UPDATE_PASSWORD_ACTION_ID)
                                .name(TEST_ACTION_NAME)
                                .description(TEST_ACTION_DESCRIPTION)
                                .type(Action.ActionTypes.PRE_UPDATE_PASSWORD)
                                .status(Action.Status.ACTIVE)
                                .createdAt(Timestamp.valueOf(TEST_CREATED_AT))
                                .updatedAt(Timestamp.valueOf(TEST_UPDATED_AT))
                                .endpoint(new EndpointConfig.EndpointConfigBuilder()
                                        .uri(TEST_ACTION_URI)
                                        .allowedHeaders(List.of(TEST_ACTION_ALLOWED_HEADER_1))
                                        .allowedParameters(List.of(TEST_ACTION_ALLOWED_PARAMETER_1))
                                        .authentication(new Authentication.BearerAuthBuilder(TEST_ACCESS_TOKEN).build())
                                        .build())
                                .properties(actionProperties)
                                .rule(ActionRule.create(buildMockORCombinedRule()))
                                .build()
                },
                // ADD operation without properties
                {ActionManagementAuditLogger.Operation.ADD,
                        new ActionDTOBuilder()
                                .id(PRE_ISSUE_ACCESS_TOKEN_ACTION_ID)
                                .name(TEST_ACTION_NAME)
                                .description(TEST_ACTION_DESCRIPTION)
                                .type(Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN)
                                .status(Action.Status.ACTIVE)
                                .createdAt(Timestamp.valueOf(TEST_CREATED_AT))
                                .updatedAt(Timestamp.valueOf(TEST_UPDATED_AT))
                                .endpoint(new EndpointConfig.EndpointConfigBuilder()
                                        .uri(TEST_ACTION_URI)
                                        .allowedHeaders(new ArrayList<>())
                                        .allowedParameters(new ArrayList<>())
                                        .authentication(new Authentication.BasicAuthBuilder(TEST_USERNAME,
                                                TEST_PASSWORD).build())
                                        .build())
                                .build()
                },
                // Add operation without allowed headers and parameters
                {ActionManagementAuditLogger.Operation.ADD,
                        new ActionDTOBuilder()
                                .id(PRE_ISSUE_ACCESS_TOKEN_ACTION_ID)
                                .name(TEST_ACTION_NAME)
                                .description(TEST_ACTION_DESCRIPTION)
                                .type(Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN)
                                .status(Action.Status.ACTIVE)
                                .createdAt(Timestamp.valueOf(TEST_CREATED_AT))
                                .updatedAt(Timestamp.valueOf(TEST_UPDATED_AT))
                                .endpoint(new EndpointConfig.EndpointConfigBuilder()
                                        .uri(TEST_ACTION_URI)
                                        .authentication(new Authentication.NoneAuthBuilder().build())
                                        .build())
                                .build()
                }
        };
    }

    @DataProvider
    public Object[][] updateActionDataProvider() {

        Map<String, ActionProperty> updatedActionProperties = new HashMap<>();
        Map<String, ActionProperty> deleteActionProperties = new HashMap<>();
        updatedActionProperties.put(PASSWORD_SHARING_TYPE_PROPERTY_NAME,
                new ActionProperty.BuilderForService(TEST_PASSWORD_SHARING_TYPE_UPDATED).build());
        updatedActionProperties.put(CERTIFICATE_PROPERTY_NAME, new ActionProperty.BuilderForService(new Certificate
                .Builder().id(CERTIFICATE_ID).name(CERTIFICATE_NAME)
                .certificateContent(TEST_CERTIFICATE_UPDATED).build()).build());

        deleteActionProperties.put(PASSWORD_SHARING_TYPE_PROPERTY_NAME,
                new ActionProperty.BuilderForService(TEST_DELETED_PROPERTY).build());
        deleteActionProperties.put(CERTIFICATE_PROPERTY_NAME,
                new ActionProperty.BuilderForService(TEST_DELETED_PROPERTY).build());

        return new Object[][]{
                // UPDATE Operations - Test cases for UPDATE operation
                // full action update
                {ActionManagementAuditLogger.Operation.UPDATE,
                        new ActionDTOBuilder()
                                .id(PRE_UPDATE_PASSWORD_ACTION_ID)
                                .name(TEST_ACTION_NAME_UPDATED)
                                .description(TEST_ACTION_DESCRIPTION_UPDATED)
                                .type(Action.ActionTypes.PRE_UPDATE_PASSWORD)
                                .status(Action.Status.ACTIVE)
                                .endpoint(new EndpointConfig.EndpointConfigBuilder()
                                        .uri(TEST_ACTION_URI_UPDATED)
                                        .allowedHeaders(List.of(TEST_ACTION_ALLOWED_HEADER_1))
                                        .allowedParameters(List.of(TEST_ACTION_ALLOWED_PARAMETER_1))
                                        .authentication(new Authentication.APIKeyAuthBuilder(TEST_API_KEY_HEADER,
                                                TEST_API_KEY_VALUE).build())
                                        .build())
                                .properties(updatedActionProperties)
                                .rule(ActionRule.create(buildMockORCombinedRule()))
                                .build()
                },
                // Update action name
                {ActionManagementAuditLogger.Operation.UPDATE,
                        new ActionDTOBuilder()
                                .id(PRE_ISSUE_ACCESS_TOKEN_ACTION_ID)
                                .type(Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN)
                                .name(TEST_ACTION_NAME_UPDATED)
                                .build()
                },
                // Update action description
                {ActionManagementAuditLogger.Operation.UPDATE,
                        new ActionDTOBuilder()
                                .id(PRE_ISSUE_ACCESS_TOKEN_ACTION_ID)
                                .type(Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN)
                                .description(TEST_ACTION_DESCRIPTION_UPDATED)
                                .build()
                },
                // Update action endpoint
                {ActionManagementAuditLogger.Operation.UPDATE,
                        new ActionDTOBuilder()
                                .id(PRE_ISSUE_ACCESS_TOKEN_ACTION_ID)
                                .type(Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN)
                                .endpoint(new EndpointConfig.EndpointConfigBuilder()
                                        .uri(TEST_ACTION_URI)
                                        .authentication(new Authentication.NoneAuthBuilder().build())
                                        .build())
                                .build()
                },
                // Update action endpoint with allowed headers and parameters
                {ActionManagementAuditLogger.Operation.UPDATE,
                        new ActionDTOBuilder()
                                .id(PRE_ISSUE_ACCESS_TOKEN_ACTION_ID)
                                .type(Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN)
                                .endpoint(new EndpointConfig.EndpointConfigBuilder()
                                        .uri(TEST_ACTION_URI_UPDATED)
                                        .allowedHeaders(List.of(TEST_ACTION_ALLOWED_HEADER_1))
                                        .allowedParameters(List.of(TEST_ACTION_ALLOWED_PARAMETER_1))
                                        .authentication(new Authentication.BasicAuthBuilder(TEST_USERNAME,
                                                TEST_PASSWORD).build())
                                        .build())
                                .build()
                },
                // Update action endpoint uri
                {ActionManagementAuditLogger.Operation.UPDATE,
                        new ActionDTOBuilder()
                                .id(PRE_ISSUE_ACCESS_TOKEN_ACTION_ID)
                                .type(Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN)
                                .endpoint(new EndpointConfig.EndpointConfigBuilder()
                                        .uri(TEST_ACTION_URI)
                                        .build())
                                .build()
                },
                // Update action endpoint authentication
                {ActionManagementAuditLogger.Operation.UPDATE,
                        new ActionDTOBuilder()
                                .id(PRE_ISSUE_ACCESS_TOKEN_ACTION_ID)
                                .type(Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN)
                                .endpoint(new EndpointConfig.EndpointConfigBuilder()
                                        .authentication(new Authentication.BearerAuthBuilder(TEST_ACCESS_TOKEN).build())
                                        .build())
                                .build()
                },
                // Update allowed headers and parameters
                {ActionManagementAuditLogger.Operation.UPDATE,
                        new ActionDTOBuilder()
                                .id(PRE_ISSUE_ACCESS_TOKEN_ACTION_ID)
                                .type(Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN)
                                .endpoint(new EndpointConfig.EndpointConfigBuilder()
                                        .allowedHeaders(List.of(TEST_ACTION_ALLOWED_HEADER_1))
                                        .allowedParameters(List.of(TEST_ACTION_ALLOWED_PARAMETER_1))
                                        .build())
                                .build()
                },
                // Deleted allowed headers and parameters
                {ActionManagementAuditLogger.Operation.UPDATE,
                        new ActionDTOBuilder()
                                .id(PRE_ISSUE_ACCESS_TOKEN_ACTION_ID)
                                .type(Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN)
                                .endpoint(new EndpointConfig.EndpointConfigBuilder()
                                        .allowedHeaders(new ArrayList<>())
                                        .allowedParameters(new ArrayList<>())
                                        .build())
                                .build()
                },
                // Update action properties
                {ActionManagementAuditLogger.Operation.UPDATE,
                        new ActionDTOBuilder()
                                .id(PRE_UPDATE_PASSWORD_ACTION_ID)
                                .type(Action.ActionTypes.PRE_UPDATE_PASSWORD)
                                .properties(updatedActionProperties)
                                .build()
                },
                // Delete action properties
                {ActionManagementAuditLogger.Operation.UPDATE,
                        new ActionDTOBuilder()
                                .id(PRE_UPDATE_PASSWORD_ACTION_ID)
                                .type(Action.ActionTypes.PRE_UPDATE_PASSWORD)
                                .properties(deleteActionProperties)
                                .build()
                }
        };
    }

    @DataProvider
    public Object[][] activateAndDeactivateActionDataProvider() {

        return new Object[][]{

                {ActionManagementAuditLogger.Operation.ACTIVATE,
                        Timestamp.valueOf(TEST_UPDATED_AT)
                },
                {ActionManagementAuditLogger.Operation.DEACTIVATE,
                        Timestamp.valueOf(TEST_UPDATED_AT)
                }
        };
    }

    @Test(dataProvider = "addActionDataProvider")
    public void testPrintAddActionAuditLog(ActionManagementAuditLogger.Operation operation, ActionDTO creatingActionDTO)
            throws NoSuchFieldException, IllegalAccessException, ActionMgtException {

        Timestamp createdAt = Timestamp.valueOf(TestUtil.TEST_CREATED_AT);
        Timestamp updatedAt = Timestamp.valueOf(TestUtil.TEST_UPDATED_AT);
        auditLogger.printAuditLog(operation, creatingActionDTO, createdAt, updatedAt);

        AuditLog.AuditLogBuilder capturedArg = captureTriggerAuditLogEventArgs();
        Assert.assertEquals(extractMapByField("CreatedAt", capturedArg), String.valueOf(createdAt));
        Assert.assertEquals(extractMapByField("UpdatedAt", capturedArg), String.valueOf(updatedAt));
        assertActionData(capturedArg, creatingActionDTO);
        assertAuditLoggerData(capturedArg, ADD_ACTION);
    }

    @Test(dataProvider = "updateActionDataProvider")
    public void testPrintUpdateActionAuditLog(ActionManagementAuditLogger.Operation operation,
                                              ActionDTO updatingActionDTO)
            throws NoSuchFieldException, IllegalAccessException, ActionMgtException {

        Timestamp updatedAt = Timestamp.valueOf(TEST_UPDATED_AT);
        auditLogger.printAuditLog(operation, updatingActionDTO, null, updatedAt);

        AuditLog.AuditLogBuilder capturedArg = captureTriggerAuditLogEventArgs();
        Assert.assertNotNull(capturedArg);
        Assert.assertEquals(extractMapByField("UpdatedAt", capturedArg), String.valueOf(updatedAt));
        assertActionData(capturedArg, updatingActionDTO);
        assertAuditLoggerData(capturedArg, UPDATE_ACTION);
    }

    @Test
    public void testPrintDeleteActionAuditLog() throws NoSuchFieldException, IllegalAccessException {

        ActionManagementAuditLogger.Operation operation = ActionManagementAuditLogger.Operation.DELETE;
        auditLogger.printAuditLog(operation, actionDTO.getType().name(), actionDTO.getId());
        AuditLog.AuditLogBuilder capturedArg = captureTriggerAuditLogEventArgs();

        Assert.assertNotNull(capturedArg);
        Assert.assertEquals(extractMapByField("ActionId", capturedArg), actionDTO.getId());
        Assert.assertEquals(extractMapByField("ActionType", capturedArg),
                Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN.getActionType());
        assertAuditLoggerData(capturedArg, DELETE_ACTION);
    }

    @Test(dataProvider = "activateAndDeactivateActionDataProvider")
    public void testPrintActivateAndDeactivateActionAuditLog(ActionManagementAuditLogger.Operation operation,
                                                             Timestamp updatedAt) throws
            NoSuchFieldException, IllegalAccessException {

        auditLogger.printAuditLog(operation, actionDTO.getType().name(), actionDTO.getId(), updatedAt);
        AuditLog.AuditLogBuilder capturedArg = captureTriggerAuditLogEventArgs();

        Assert.assertNotNull(capturedArg);
        Assert.assertEquals(extractMapByField("ActionId", capturedArg), actionDTO.getId());
        Assert.assertEquals(extractMapByField("ActionType", capturedArg), actionDTO.getType().name());
        Assert.assertEquals(extractMapByField("UpdatedAt", capturedArg), String.valueOf(updatedAt));

        if (operation.equals(ActionManagementAuditLogger.Operation.ACTIVATE)) {
            assertAuditLoggerData(capturedArg, ACTIVATE_ACTION);
        } else if (operation.equals(ActionManagementAuditLogger.Operation.DEACTIVATE)) {
            assertAuditLoggerData(capturedArg, DEACTIVATE_ACTION);
        }
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
     * @param actionDTO       {@link ActionDTO} instance.
     * @throws NoSuchFieldException   if the provided field does not exist.
     * @throws IllegalAccessException if the provided field is not accessible.
     */
    private void assertActionData(AuditLog.AuditLogBuilder auditLogBuilder, ActionDTO actionDTO)
            throws NoSuchFieldException, IllegalAccessException, ActionMgtException {

        Field dataField = AuditLog.AuditLogBuilder.class.getDeclaredField("data");
        dataField.setAccessible(true);
        Map<String, Object> dataMap = (Map<String, Object>) dataField.get(auditLogBuilder);
        Map<String, Object> endpointConfigMap = (Map<String, Object>) dataMap.get("EndpointConfiguration");
        Map<String, Object> propertiesMap = (Map<String, Object>) dataMap.get("Properties");

        String id = actionDTO.getId();
        String name = actionDTO.getName();
        String description = actionDTO.getDescription();
        String type = actionDTO.getType() != null ? actionDTO.getType().name() : null;
        String status = actionDTO.getStatus() != null ? actionDTO.getStatus().name() : null;

        String uri = actionDTO.getEndpoint() != null && actionDTO.getEndpoint().getUri() != null ?
                actionDTO.getEndpoint().getUri() : null;
        String allowedHeaders = actionDTO.getEndpoint() != null && actionDTO.getEndpoint().getAllowedHeaders() != null ?
                actionDTO.getEndpoint().getAllowedHeaders().toString() : null;
        String allowedParameters = actionDTO.getEndpoint() != null &&
                actionDTO.getEndpoint().getAllowedParameters() != null ?
                actionDTO.getEndpoint().getAllowedParameters().toString() : null;
        String authenticationScheme = actionDTO.getEndpoint() != null &&
                actionDTO.getEndpoint().getAuthentication() != null &&
                actionDTO.getEndpoint().getAuthentication().getType() != null ?
                actionDTO.getEndpoint().getAuthentication().getType().getName() : null;
        String rule = actionDTO.getActionRule() != null ?
                AuditLogBuilderForRule.buildRuleValue(actionDTO.getActionRule().getRule()) : null;

        assertField(id != null, dataMap, "ActionId", id);
        assertField(name != null, dataMap, "ActionName", name);
        assertField(description != null, dataMap, "ActionDescription", description);
        assertField(type != null, dataMap, "ActionType", type);
        assertField(status != null, dataMap, "ActionStatus", status);
        assertField(uri != null, endpointConfigMap, "EndpointUri", uri);
        assertField(allowedHeaders != null, endpointConfigMap, "AllowedHeaders", allowedHeaders);
        assertField(allowedParameters != null, endpointConfigMap, "AllowedParameters", allowedParameters);
        assertField(authenticationScheme != null, endpointConfigMap, "AuthenticationScheme",
                authenticationScheme);
        assertField(rule != null, dataMap, "Rule", rule);

        if (authenticationScheme != null) {
            switch (actionDTO.getEndpoint().getAuthentication().getType()) {
                case BASIC:
                    assertField(true, endpointConfigMap, "Username",
                            LoggerUtils.getMaskedContent(actionDTO.getEndpoint().getAuthentication()
                                    .getProperty(Authentication.Property.USERNAME).getValue()));
                    assertField(true, endpointConfigMap, "Password",
                            LoggerUtils.getMaskedContent(actionDTO.getEndpoint().getAuthentication()
                                    .getProperty(Authentication.Property.PASSWORD).getValue()));
                    break;
                case BEARER:
                    assertField(true, endpointConfigMap, "AccessToken",
                            LoggerUtils.getMaskedContent(actionDTO.getEndpoint().getAuthentication()
                                    .getProperty(Authentication.Property.ACCESS_TOKEN).getValue()));
                    break;
                case API_KEY:
                    assertField(true, endpointConfigMap, "ApiKeyHeader",
                            LoggerUtils.getMaskedContent(actionDTO.getEndpoint().getAuthentication()
                                    .getProperty(Authentication.Property.HEADER).getValue()));
                    assertField(true, endpointConfigMap, "ApiKeyValue",
                            LoggerUtils.getMaskedContent(actionDTO.getEndpoint().getAuthentication()
                                    .getProperty(Authentication.Property.VALUE).getValue()));
                    break;
                case NONE:
                default:
                    break;
            }
        }

        if (actionDTO.getProperties() != null &&
                actionDTO.getPropertyValue(PASSWORD_SHARING_TYPE_PROPERTY_NAME) != null) {
            assertField(propertiesMap.get(PASSWORD_SHARING_TYPE_PROPERTY_NAME) != null, propertiesMap,
                    PASSWORD_SHARING_TYPE_PROPERTY_NAME, LoggerUtils.getMaskedContent(actionDTO.getProperties()
                            .get(PASSWORD_SHARING_TYPE_PROPERTY_NAME).toString()));
        }

        if (actionDTO.getProperties() != null && actionDTO.getPropertyValue(CERTIFICATE_PROPERTY_NAME) != null) {
            assertField(propertiesMap.get(CERTIFICATE_PROPERTY_NAME) != null, propertiesMap,
                    CERTIFICATE_PROPERTY_NAME, LoggerUtils.getMaskedContent(actionDTO.getProperties()
                            .get(CERTIFICATE_PROPERTY_NAME).toString()));
        }
    }

    /**
     * Assert field.
     *
     * @param isFieldExist Field existence.
     * @param dataMap      Data map.
     * @param fieldName    Field name.
     * @param value        Value to be asserted.
     */
    private void assertField(boolean isFieldExist, Map<String, Object> dataMap, String fieldName, String value) {

        if (isFieldExist) {
            Assert.assertEquals(dataMap.get(fieldName).toString(), value);
        } else {
            Assert.assertTrue(dataMap == null || dataMap.get(fieldName) == null);
        }
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
            case ACTIVATE_ACTION:
                Assert.assertEquals(extractField("action", auditLogBuilder), "activate-action");
                break;
            case DEACTIVATE_ACTION:
                Assert.assertEquals(extractField("action", auditLogBuilder), "deactivate-action");
                break;
        }
    }

    /**
     * Builds a mock {@link ORCombinedRule} with predefined expressions.
     * The rule consists of:
     * - An AND rule matching "application = testapp1" and "grantType = authorization_code".
     * - An AND rule matching "application = testapp2".
     * These are combined with OR logic.
     *
     * @return A mock {@link ORCombinedRule} instance.
     */
    private ORCombinedRule buildMockORCombinedRule() {
        Expression expression1 = new Expression.Builder().field("application").operator("equals")
                .value(new Value(Value.Type.REFERENCE, "testapp1")).build();

        Expression expression2 = new Expression.Builder().field("grantType").operator("equals")
                .value(new Value(Value.Type.STRING, "authorization_code")).build();
        ANDCombinedRule andCombinedRule1 =
                new ANDCombinedRule.Builder().addExpression(expression1).addExpression(expression2).build();

        Expression expression3 = new Expression.Builder().field("application").operator("equals")
                .value(new Value(Value.Type.REFERENCE, "testapp2")).build();
        ANDCombinedRule andCombinedRule2 =
                new ANDCombinedRule.Builder().addExpression(expression3).build();

        return new ORCombinedRule.Builder().addRule(andCombinedRule1).addRule(andCombinedRule2).build();
    }
}
