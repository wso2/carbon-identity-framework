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

package org.wso2.carbon.identity.topic.management.service;

import org.mockito.MockedStatic;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.topic.management.api.exception.TopicManagementException;
import org.wso2.carbon.identity.topic.management.api.service.TopicManager;
import org.wso2.carbon.identity.topic.management.internal.component.TopicManagementComponentServiceHolder;
import org.wso2.carbon.identity.topic.management.internal.dao.TopicManagementDAO;
import org.wso2.carbon.identity.topic.management.internal.service.impl.TopicManagementServiceImpl;
import org.wso2.carbon.identity.topic.management.internal.util.TopicManagementExceptionHandler;
import org.wso2.carbon.identity.webhook.metadata.api.model.Adapter;

import java.lang.reflect.Field;
import java.util.Collections;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.identity.topic.management.internal.constant.ErrorMessage.ERROR_CODE_TOPIC_CONSTRUCT_ERROR;
import static org.wso2.carbon.identity.topic.management.internal.constant.ErrorMessage.ERROR_CODE_TOPIC_DEREGISTRATION_ERROR;
import static org.wso2.carbon.identity.topic.management.internal.constant.ErrorMessage.ERROR_CODE_TOPIC_PERSISTENCE_ERROR;
import static org.wso2.carbon.identity.topic.management.internal.constant.ErrorMessage.ERROR_CODE_TOPIC_REGISTRATION_ERROR;

@WithCarbonHome
public class TopicManagementServiceImplTest {

    private TopicManagementServiceImpl topicManagementService;
    private TopicManagementDAO topicManagementDAO;
    private TopicManager topicManager;
    private TopicManagementComponentServiceHolder componentServiceHolderMock;
    private MockedStatic<IdentityTenantUtil> identityTenantUtilMockedStatic;

    private static final String TENANT_DOMAIN = "carbon.super";
    private static final int TENANT_ID = 1;
    private static final String CHANNEL_URI = "example.com/events";
    private static final String EVENT_PROFILE_VERSION = "v1";
    private static final String EVENT_PROFILE_NAME = "exampleEventProfile";
    private static final String TOPIC = "https://example.com/events/carbon.super";
    private static final String WEBSUBHUBADAPTER = "webSubHubAdapter";

    @BeforeClass
    public void setUpClass() throws Exception {

        identityTenantUtilMockedStatic = mockStatic(IdentityTenantUtil.class);
        identityTenantUtilMockedStatic.when(() -> IdentityTenantUtil.getTenantId(TENANT_DOMAIN)).thenReturn(TENANT_ID);

        // Mock and inject the singleton holder
        componentServiceHolderMock = mock(TopicManagementComponentServiceHolder.class);
        Field instanceField = TopicManagementComponentServiceHolder.class.getDeclaredField("INSTANCE");
        instanceField.setAccessible(true);

        // Use Unsafe to modify static final fields in Java 12+
        Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        sun.misc.Unsafe unsafe = (sun.misc.Unsafe) unsafeField.get(null);

        Object fieldBase = unsafe.staticFieldBase(instanceField);
        long fieldOffset = unsafe.staticFieldOffset(instanceField);
        unsafe.putObject(fieldBase, fieldOffset, componentServiceHolderMock);
    }

    @AfterClass
    public void tearDownClass() {

        identityTenantUtilMockedStatic.close();
    }

    @BeforeMethod
    public void setUp() throws Exception {

        Adapter webhookAdapterMock = mock(Adapter.class);
        when(componentServiceHolderMock.getWebhookAdapter()).thenReturn(webhookAdapterMock);
        when(webhookAdapterMock.getName()).thenReturn(WEBSUBHUBADAPTER);

        topicManagementDAO = mock(TopicManagementDAO.class);
        topicManager = mock(TopicManager.class);

        when(componentServiceHolderMock.getTopicManagers())
                .thenReturn(Collections.singletonList(topicManager));
        when(topicManager.getAssociatedAdapter()).thenReturn(WEBSUBHUBADAPTER);
        when(topicManager.constructTopic(CHANNEL_URI, EVENT_PROFILE_NAME, EVENT_PROFILE_VERSION,
                TENANT_DOMAIN)).thenReturn(TOPIC);

        topicManagementService = TopicManagementServiceImpl.getInstance();

        Field daoField = TopicManagementServiceImpl.class.getDeclaredField("topicManagementDAO");
        daoField.setAccessible(true);
        daoField.set(topicManagementService, topicManagementDAO);
    }

    @Test
    public void testRegisterTopic() throws TopicManagementException {

        when(topicManagementDAO.isTopicExists(TOPIC, TENANT_ID)).thenReturn(false);
        doNothing().when(topicManager).registerTopic(TOPIC, TENANT_DOMAIN);
        doNothing().when(topicManagementDAO).addTopic(TOPIC, CHANNEL_URI, EVENT_PROFILE_VERSION, TENANT_ID);

        topicManagementService.registerTopic(CHANNEL_URI, EVENT_PROFILE_NAME, EVENT_PROFILE_VERSION, TENANT_DOMAIN);

        verify(topicManagementDAO).addTopic(TOPIC, CHANNEL_URI, EVENT_PROFILE_VERSION, TENANT_ID);
    }

    @Test(expectedExceptions = TopicManagementException.class)
    public void testRegisterTopicWithNullUri() throws TopicManagementException {

        when(topicManager.constructTopic(null, EVENT_PROFILE_NAME, EVENT_PROFILE_VERSION, TENANT_DOMAIN)).thenThrow(
                TopicManagementExceptionHandler.handleServerException(ERROR_CODE_TOPIC_REGISTRATION_ERROR,
                        "INVALID_URI"));

        topicManagementService.registerTopic(null, EVENT_PROFILE_NAME, EVENT_PROFILE_VERSION, TENANT_DOMAIN);
    }

    @Test(expectedExceptions = TopicManagementException.class)
    public void testRegisterTopicAlreadyExists() throws TopicManagementException {

        when(topicManagementDAO.isTopicExists(TOPIC, TENANT_ID)).thenReturn(true);

        topicManagementService.registerTopic(CHANNEL_URI, EVENT_PROFILE_NAME, EVENT_PROFILE_VERSION, TENANT_DOMAIN);

        // Verify that addTopic was never called since the topic already exists
        verify(topicManagementDAO, never()).addTopic(TOPIC, CHANNEL_URI, EVENT_PROFILE_VERSION, TENANT_ID);
    }

    @Test
    public void testDeregisterTopic() throws TopicManagementException {

        doNothing().when(topicManager).deregisterTopic(TOPIC, TENANT_DOMAIN);
        doNothing().when(topicManagementDAO).deleteTopic(TOPIC, TENANT_ID);

        topicManagementService.deregisterTopic(CHANNEL_URI, EVENT_PROFILE_NAME, EVENT_PROFILE_VERSION, TENANT_DOMAIN);

        verify(topicManagementDAO).deleteTopic(TOPIC, TENANT_ID);
    }

    @Test(expectedExceptions = TopicManagementException.class)
    public void testDeregisterTopicWithNullUri() throws TopicManagementException {

        when(topicManager.constructTopic(null, EVENT_PROFILE_NAME, EVENT_PROFILE_VERSION, TENANT_DOMAIN)).thenThrow(
                TopicManagementExceptionHandler.handleServerException(ERROR_CODE_TOPIC_DEREGISTRATION_ERROR));

        topicManagementService.deregisterTopic(null, EVENT_PROFILE_NAME, EVENT_PROFILE_VERSION, TENANT_DOMAIN);
    }

    @Test
    public void testIsTopicExists() throws TopicManagementException {

        when(topicManagementDAO.isTopicExists(TOPIC, TENANT_ID)).thenReturn(true);

        boolean result = topicManagementService.isTopicExists(CHANNEL_URI, EVENT_PROFILE_NAME, EVENT_PROFILE_VERSION,
                TENANT_DOMAIN);
        assertTrue(result);
    }

    @Test
    public void testIsTopicNotExists() throws TopicManagementException {

        when(topicManagementDAO.isTopicExists(TOPIC, TENANT_ID)).thenReturn(false);

        boolean result = topicManagementService.isTopicExists(CHANNEL_URI, EVENT_PROFILE_NAME, EVENT_PROFILE_VERSION,
                TENANT_DOMAIN);
        assertFalse(result);
    }

    @Test(expectedExceptions = TopicManagementException.class)
    public void testIsTopicExistsWithNullUri() throws TopicManagementException {

        when(topicManager.constructTopic(null, EVENT_PROFILE_NAME, EVENT_PROFILE_VERSION, TENANT_DOMAIN)).thenThrow(
                TopicManagementExceptionHandler.handleServerException(ERROR_CODE_TOPIC_CONSTRUCT_ERROR));

        topicManagementService.isTopicExists(null, EVENT_PROFILE_NAME, EVENT_PROFILE_VERSION, TENANT_DOMAIN);
    }

    @Test(expectedExceptions = TopicManagementException.class)
    public void testWithNoManagers() throws TopicManagementException {

        when(componentServiceHolderMock.getTopicManagers()).thenReturn(Collections.emptyList());

        topicManagementService.registerTopic(CHANNEL_URI, EVENT_PROFILE_NAME, EVENT_PROFILE_VERSION, TENANT_DOMAIN);
    }

    @Test(expectedExceptions = TopicManagementException.class)
    public void testIsTopicExistsWithNullTopic() throws TopicManagementException {

        when(topicManager.constructTopic(CHANNEL_URI, EVENT_PROFILE_NAME, EVENT_PROFILE_VERSION, TENANT_DOMAIN))
                .thenReturn(null);

        topicManagementService.isTopicExists(CHANNEL_URI, EVENT_PROFILE_NAME, EVENT_PROFILE_VERSION, TENANT_DOMAIN);
    }

    @Test(expectedExceptions = TopicManagementException.class)
    public void testRegisterTopicThrowsServerExceptionOnAdapterError() throws TopicManagementException {

        when(topicManagementDAO.isTopicExists(TOPIC, TENANT_ID)).thenReturn(false);
        doThrow(TopicManagementExceptionHandler.handleServerException(ERROR_CODE_TOPIC_PERSISTENCE_ERROR)).when(
                topicManager).registerTopic(TOPIC, TENANT_DOMAIN);

        topicManagementService.registerTopic(CHANNEL_URI, EVENT_PROFILE_NAME, EVENT_PROFILE_VERSION, TENANT_DOMAIN);
    }
}
