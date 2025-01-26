/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.user.pre.update.password.action.execution;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.execution.exception.ActionExecutionRequestBuilderException;
import org.wso2.carbon.identity.action.execution.model.ActionExecutionRequest;
import org.wso2.carbon.identity.action.execution.model.ActionType;
import org.wso2.carbon.identity.action.management.model.Authentication;
import org.wso2.carbon.identity.action.management.model.EndpointConfig;
import org.wso2.carbon.identity.certificate.management.model.Certificate;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.context.IdentityContext;
import org.wso2.carbon.identity.core.context.model.Flow;
import org.wso2.carbon.identity.user.action.service.model.UserActionContext;
import org.wso2.carbon.identity.user.pre.update.password.action.core.constant.PreUpdatePasswordActionConstants;
import org.wso2.carbon.identity.user.pre.update.password.action.core.execution.PreUpdatePasswordActionRequestBuilder;
import org.wso2.carbon.identity.user.pre.update.password.action.service.model.Credential;
import org.wso2.carbon.identity.user.pre.update.password.action.service.model.PasswordSharing;
import org.wso2.carbon.identity.user.pre.update.password.action.service.model.PasswordUpdatingUser;
import org.wso2.carbon.identity.user.pre.update.password.action.service.model.PreUpdatePasswordAction;
import org.wso2.carbon.identity.user.pre.update.password.action.service.model.PreUpdatePasswordEvent;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_ACTION;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_CERTIFICATE_ID;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_CERTIFICATE_NAME;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_DESCRIPTION;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_ID;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_PASSWORD;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_SAMPLE_CERTIFICATE;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_URL;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_USERNAME;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_USER_STORE_DOMAIN_ID;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_USER_STORE_DOMAIN_NAME;

/**
 * Represents an unencrypted credential.
 */
@WithCarbonHome
public class PreUpdatePasswordActionRequestBuilderTest {

    private PreUpdatePasswordAction preUpdatePasswordAction;
    private PreUpdatePasswordAction preUpdatePasswordActionWithoutCert;
    private UserActionContext userActionContext;
    private final Map<String, Object> eventContext = new HashMap<>();
    private PreUpdatePasswordActionRequestBuilder preUpdatePasswordActionRequestBuilder;

    @BeforeClass
    public void init() {

        userActionContext = new UserActionContext.Builder()
                .userId(TEST_ID)
                .password(TEST_PASSWORD.toCharArray())
                .userStoreDomain(TEST_USER_STORE_DOMAIN_NAME)
                .build();

        preUpdatePasswordAction = new PreUpdatePasswordAction.ResponseBuilder()
                .id(TEST_ID)
                .name(TEST_ACTION)
                .description(TEST_DESCRIPTION)
                .endpoint(new EndpointConfig.EndpointConfigBuilder()
                        .uri(TEST_URL)
                        .authentication(new Authentication.BasicAuthBuilder(TEST_USERNAME, TEST_PASSWORD).build())
                        .build())
                .passwordSharing(new PasswordSharing.Builder()
                        .format(PasswordSharing.Format.SHA256_HASHED)
                        .certificate(new Certificate.Builder()
                                .id(TEST_CERTIFICATE_ID)
                                .name(TEST_CERTIFICATE_NAME)
                                .certificateContent(TEST_SAMPLE_CERTIFICATE)
                                .build())
                        .build())
                .build();

        preUpdatePasswordActionWithoutCert = new PreUpdatePasswordAction.ResponseBuilder()
                .id(TEST_ID)
                .name(TEST_ACTION)
                .description(TEST_DESCRIPTION)
                .endpoint(new EndpointConfig.EndpointConfigBuilder()
                        .uri(TEST_URL)
                        .authentication(new Authentication.BasicAuthBuilder(TEST_USERNAME, TEST_PASSWORD).build())
                        .build())
                .passwordSharing(new PasswordSharing.Builder()
                        .format(PasswordSharing.Format.PLAIN_TEXT)
                        .build())
                .build();
    }

    @BeforeMethod
    public void setUp() {

        preUpdatePasswordActionRequestBuilder = new PreUpdatePasswordActionRequestBuilder();
        eventContext.put(PreUpdatePasswordActionConstants.USER_ACTION_CONTEXT, userActionContext);
    }

    @AfterMethod
    public void tearDown() {

        IdentityContext.destroyCurrentContext();
        eventContext.clear();
    }

    @Test
    public void getSupportedActionType() {

        assertEquals(preUpdatePasswordActionRequestBuilder.getSupportedActionType(), ActionType.PRE_UPDATE_PASSWORD);
    }

    @Test
    public void testRequestBuilder() throws ActionExecutionRequestBuilderException {

        IdentityContext.getThreadLocalIdentityContext().setFlow(new Flow.Builder()
                .name(Flow.Name.PASSWORD_UPDATE)
                .initiatingPersona(Flow.InitiatingPersona.USER)
                .build());
        eventContext.put("action", preUpdatePasswordAction);

        ActionExecutionRequest actionExecutionRequest = preUpdatePasswordActionRequestBuilder
                .buildActionExecutionRequest(eventContext);

        assertNotNull(actionExecutionRequest);
        assertEquals(actionExecutionRequest.getActionType(), ActionType.PRE_UPDATE_PASSWORD);
        assertTrue(actionExecutionRequest.getEvent() instanceof PreUpdatePasswordEvent);

        PreUpdatePasswordEvent preUpdatePasswordEvent = (PreUpdatePasswordEvent) actionExecutionRequest.getEvent();
        assertEquals(preUpdatePasswordEvent.getInitiator(), PreUpdatePasswordEvent.FlowInitiator.USER);
        assertEquals(preUpdatePasswordEvent.getAction(), PreUpdatePasswordEvent.Action.UPDATE);

        assertEquals(preUpdatePasswordEvent.getUserStore().getName(), TEST_USER_STORE_DOMAIN_NAME);
        assertEquals(preUpdatePasswordEvent.getUserStore().getId(), TEST_USER_STORE_DOMAIN_ID);

        assertTrue(preUpdatePasswordEvent.getUser() instanceof PasswordUpdatingUser);
        PasswordUpdatingUser passwordUpdatingUser = (PasswordUpdatingUser) preUpdatePasswordEvent.getUser();
        assertEquals(passwordUpdatingUser.getId(), TEST_ID);
        assertNotNull(passwordUpdatingUser.getUpdatingCredential());
        assertTrue(passwordUpdatingUser.getUpdatingCredential() instanceof String);
    }

    @Test
    public void testRequestBuilderWithUnEncryptedCredential() throws ActionExecutionRequestBuilderException {

        IdentityContext.getThreadLocalIdentityContext().setFlow(new Flow.Builder()
                .name(Flow.Name.PASSWORD_RESET)
                .initiatingPersona(Flow.InitiatingPersona.APPLICATION)
                .build());
        eventContext.put("action", preUpdatePasswordActionWithoutCert);

        ActionExecutionRequest actionExecutionRequest = preUpdatePasswordActionRequestBuilder
                .buildActionExecutionRequest(eventContext);

        assertNotNull(actionExecutionRequest);
        assertEquals(actionExecutionRequest.getActionType(), ActionType.PRE_UPDATE_PASSWORD);
        assertTrue(actionExecutionRequest.getEvent() instanceof PreUpdatePasswordEvent);

        PreUpdatePasswordEvent preUpdatePasswordEvent = (PreUpdatePasswordEvent) actionExecutionRequest.getEvent();
        assertEquals(preUpdatePasswordEvent.getInitiator(), PreUpdatePasswordEvent.FlowInitiator.APPLICATION);
        assertEquals(preUpdatePasswordEvent.getAction(), PreUpdatePasswordEvent.Action.RESET);

        assertEquals(preUpdatePasswordEvent.getUserStore().getName(), TEST_USER_STORE_DOMAIN_NAME);
        assertEquals(preUpdatePasswordEvent.getUserStore().getId(), TEST_USER_STORE_DOMAIN_ID);

        assertTrue(preUpdatePasswordEvent.getUser() instanceof PasswordUpdatingUser);
        PasswordUpdatingUser passwordUpdatingUser = (PasswordUpdatingUser) preUpdatePasswordEvent.getUser();
        assertEquals(passwordUpdatingUser.getId(), TEST_ID);
        assertNotNull(passwordUpdatingUser.getUpdatingCredential());
        assertTrue(passwordUpdatingUser.getUpdatingCredential() instanceof Credential);
    }
}
