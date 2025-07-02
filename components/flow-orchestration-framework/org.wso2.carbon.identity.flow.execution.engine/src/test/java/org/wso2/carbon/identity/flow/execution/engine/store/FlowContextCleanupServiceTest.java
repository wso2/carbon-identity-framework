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

package org.wso2.carbon.identity.flow.execution.engine.store;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.flow.execution.engine.Constants.FlowExecutionConfigs;

import java.lang.reflect.Field;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Unit tests for FlowContextCleanupService.
 */
public class FlowContextCleanupServiceTest {

    private static final String CUSTOM_INITIAL_DELAY = "10";
    private static final String CUSTOM_INTERVAL = "120";
    private static final String INVALID_DELAY_VALUE = "invalid";

    private FlowContextCleanupService cleanupService;

    @Mock
    private ScheduledExecutorService mockScheduler;

    private FlowContextStore mockFlowContextStore;

    private MockedStatic<IdentityUtil> identityUtilMock;
    private MockedStatic<FlowContextStore> flowContextStoreMock;
    private MockedStatic<IdentityConfigParser> identityConfigParserMock;

    @BeforeMethod
    public void setup() throws Exception {

        MockitoAnnotations.openMocks(this);

        identityConfigParserMock = mockStatic(IdentityConfigParser.class);

        IdentityConfigParser identityConfigParser = mock(IdentityConfigParser.class);
        identityConfigParserMock.when(IdentityConfigParser::getInstance)
                .thenReturn(identityConfigParser);
        mockFlowContextStore = FlowContextStore.getInstance();

        identityUtilMock = mockStatic(IdentityUtil.class);
        flowContextStoreMock = mockStatic(FlowContextStore.class);

        identityUtilMock.when(() -> IdentityUtil.getProperty(FlowExecutionConfigs.CLEANUP_ENABLED_PROPERTY))
                .thenReturn("true");
        identityUtilMock.when(() -> IdentityUtil.getProperty(FlowExecutionConfigs.CLEANUP_INITIAL_DELAY_PROPERTY))
                .thenReturn(null);
        identityUtilMock.when(() -> IdentityUtil.getProperty(FlowExecutionConfigs.CLEANUP_INTERVAL_PROPERTY))
                .thenReturn(null);

        flowContextStoreMock.when(FlowContextStore::getInstance).thenReturn(mockFlowContextStore);
    }

    @AfterMethod
    public void tearDown() {

        if (identityUtilMock != null) {
            identityUtilMock.close();
        }
        if (flowContextStoreMock != null) {
            flowContextStoreMock.close();
        }

        if (identityConfigParserMock != null) {
            identityConfigParserMock.close();
        }
    }

    @Test
    public void testConstructorWithDefaultValues() throws Exception {

        cleanupService = new FlowContextCleanupService();
        assertNotNull(cleanupService);
        assertEquals(getPrivateField(cleanupService, "initialDelay"), 30L);
        assertEquals(getPrivateField(cleanupService, "interval"), 60L);
        assertNotNull(getPrivateField(cleanupService, "scheduler"));
    }

    @Test
    public void testConstructorWithCustomValues() throws Exception {

        identityUtilMock.when(() -> IdentityUtil.getProperty(FlowExecutionConfigs.CLEANUP_INITIAL_DELAY_PROPERTY))
                .thenReturn(CUSTOM_INITIAL_DELAY);
        identityUtilMock.when(() -> IdentityUtil.getProperty(FlowExecutionConfigs.CLEANUP_INTERVAL_PROPERTY))
                .thenReturn(CUSTOM_INTERVAL);
        cleanupService = new FlowContextCleanupService();

        assertEquals(getPrivateField(cleanupService, "initialDelay"), 10L);
        assertEquals(getPrivateField(cleanupService, "interval"), 120L);
    }

    @Test
    public void testConstructorWithInvalidValues() throws Exception {

        identityUtilMock.when(() -> IdentityUtil.getProperty(FlowExecutionConfigs.CLEANUP_INITIAL_DELAY_PROPERTY))
                .thenReturn(INVALID_DELAY_VALUE);
        identityUtilMock.when(() -> IdentityUtil.getProperty(FlowExecutionConfigs.CLEANUP_INTERVAL_PROPERTY))
                .thenReturn(INVALID_DELAY_VALUE);
        cleanupService = new FlowContextCleanupService();

        assertEquals(getPrivateField(cleanupService, "initialDelay"), 30L);
        assertEquals(getPrivateField(cleanupService, "interval"), 60L);
    }

    @Test
    public void testActivateCleanUpWhenEnabled() throws Exception {

        cleanupService = createCleanupServiceWithMockScheduler();
        identityUtilMock.when(() -> IdentityUtil.getProperty(FlowExecutionConfigs.CLEANUP_ENABLED_PROPERTY))
                .thenReturn("true");
        cleanupService.activateCleanUp();

        verify(mockScheduler).scheduleWithFixedDelay(any(Runnable.class), eq(30L), eq(60L), eq(TimeUnit.MINUTES));
    }

    @Test
    public void testActivateCleanUpWhenDisabled() throws Exception {

        cleanupService = createCleanupServiceWithMockScheduler();
        identityUtilMock.when(() -> IdentityUtil.getProperty(FlowExecutionConfigs.CLEANUP_ENABLED_PROPERTY))
                .thenReturn("false");
        cleanupService.activateCleanUp();

        verify(mockScheduler, never()).scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(),
                any(TimeUnit.class));
    }

    @Test
    public void testActivateCleanUpWhenEnabledPropertyIsNull() throws Exception {

        cleanupService = createCleanupServiceWithMockScheduler();
        identityUtilMock.when(() -> IdentityUtil.getProperty(FlowExecutionConfigs.CLEANUP_ENABLED_PROPERTY))
                .thenReturn(null);

        cleanupService.activateCleanUp();
        verify(mockScheduler).scheduleWithFixedDelay(any(Runnable.class), eq(30L), eq(60L), eq(TimeUnit.MINUTES));
    }

    @Test
    public void testActivateCleanUpWhenEnabledPropertyIsEmpty() throws Exception {

        cleanupService = createCleanupServiceWithMockScheduler();
        identityUtilMock.when(() -> IdentityUtil.getProperty(FlowExecutionConfigs.CLEANUP_ENABLED_PROPERTY))
                .thenReturn("");
        cleanupService.activateCleanUp();
        verify(mockScheduler).scheduleWithFixedDelay(any(Runnable.class), eq(30L), eq(60L), eq(TimeUnit.MINUTES));
    }

    @Test
    public void testParseConfigPropertyWithValidValue() throws Exception {

        identityUtilMock.when(() -> IdentityUtil.getProperty("test.property")).thenReturn("100");
        cleanupService = new FlowContextCleanupService();
        long result = invokeParseConfigProperty(cleanupService, "test.property", 50L, "test property");
        assertEquals(result, 100L);
    }

    @Test
    public void testParseConfigPropertyWithInvalidValue() throws Exception {

        identityUtilMock.when(() -> IdentityUtil.getProperty("test.property")).thenReturn("invalid");
        cleanupService = new FlowContextCleanupService();
        long result = invokeParseConfigProperty(cleanupService, "test.property", 50L, "test property");
        assertEquals(result, 50L);
    }

    /**
     * Helper method to create a FlowContextCleanupService with a mocked scheduler.
     */
    private FlowContextCleanupService createCleanupServiceWithMockScheduler() throws Exception {

        FlowContextCleanupService service = new FlowContextCleanupService();
        setPrivateField(service, "scheduler", mockScheduler);
        return service;
    }

    /**
     * Helper method to get private field value using reflection.
     */
    private Object getPrivateField(Object object, String fieldName) throws Exception {

        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(object);
    }

    /**
     * Helper method to set private field value using reflection.
     */
    private void setPrivateField(Object object, String fieldName, Object value) throws Exception {

        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }

    /**
     * Helper method to invoke private parseConfigProperty method using reflection.
     */
    private long invokeParseConfigProperty(Object object, String property, long defaultValue, String label)
            throws Exception {

        java.lang.reflect.Method method = object.getClass().getDeclaredMethod("parseConfigProperty",
                String.class, long.class, String.class);
        method.setAccessible(true);
        return (Long) method.invoke(object, property, defaultValue, label);
    }
}
