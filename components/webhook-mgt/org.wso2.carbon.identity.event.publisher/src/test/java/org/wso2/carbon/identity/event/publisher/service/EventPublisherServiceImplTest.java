/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
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

package org.wso2.carbon.identity.event.publisher.service;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.event.publisher.api.constant.ErrorMessage;
import org.wso2.carbon.identity.event.publisher.api.exception.EventPublisherException;
import org.wso2.carbon.identity.event.publisher.api.model.EventContext;
import org.wso2.carbon.identity.event.publisher.api.model.EventPayload;
import org.wso2.carbon.identity.event.publisher.api.model.SecurityEventTokenPayload;
import org.wso2.carbon.identity.event.publisher.api.model.common.SimpleSubject;
import org.wso2.carbon.identity.event.publisher.api.model.common.Subject;
import org.wso2.carbon.identity.event.publisher.api.service.EventPublisher;
import org.wso2.carbon.identity.event.publisher.internal.component.EventPublisherComponentServiceHolder;
import org.wso2.carbon.identity.event.publisher.internal.service.impl.EventPublisherServiceImpl;
import org.wso2.carbon.identity.webhook.metadata.api.model.Adapter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Consolidated test class for EventPublisherService, EventContext, and SecurityEventTokenPayload.
 */
public class EventPublisherServiceImplTest {

    @Mock
    private EventPublisher mockEventPublisher1;
    @Mock
    private EventPublisher mockEventPublisher2;
    @Mock
    private EventContext mockEventContext;
    @Mock
    private SecurityEventTokenPayload mockEventPayload;

    private List<EventPublisher> eventPublishers;
    private EventPublisherServiceImpl eventPublisherService;

    @BeforeClass
    public void setupClass() {

        MockitoAnnotations.openMocks(this);

        // Get the real singleton instance
        EventPublisherComponentServiceHolder serviceHolder =
                EventPublisherComponentServiceHolder.getInstance();

        // Mock Adapter and set it on the singleton
        Adapter webhookAdapterMock = Mockito.mock(Adapter.class);
        Mockito.when(webhookAdapterMock.getName()).thenReturn("webSubHubAdapter");
        serviceHolder.setWebhookAdapter(webhookAdapterMock);

        eventPublisherService = EventPublisherServiceImpl.getInstance();
    }

    @BeforeMethod
    public void setup() {

        Mockito.reset(mockEventPublisher1, mockEventPublisher2);
        eventPublishers = Arrays.asList(mockEventPublisher1, mockEventPublisher2);
        EventPublisherComponentServiceHolder.getInstance().setEventPublishers(eventPublishers);
    }

    @AfterMethod
    public void tearDown() {

        EventPublisherComponentServiceHolder.getInstance().setEventPublishers(null);
    }

    @Test(expectedExceptions = EventPublisherException.class)
    public void testPublishWithException() throws Exception {

        when(mockEventPublisher1.getAssociatedAdapter()).thenReturn("webSubHubAdapter");
        when(mockEventPublisher2.getAssociatedAdapter()).thenReturn("otherAdapter");

        doThrow(new EventPublisherException("E", "msg", "desc"))
                .when(mockEventPublisher1).publish(mockEventPayload, mockEventContext);

        eventPublisherService.publish(mockEventPayload, mockEventContext);
    }

    @Test
    public void testSuccessfulPublish() throws Exception {

        when(mockEventPublisher1.getAssociatedAdapter()).thenReturn("webSubHubAdapter");
        when(mockEventPublisher2.getAssociatedAdapter()).thenReturn("otherAdapter");

        doNothing().when(mockEventPublisher1).publish(mockEventPayload, mockEventContext);

        eventPublisherService.publish(mockEventPayload, mockEventContext);

        TimeUnit.MILLISECONDS.sleep(200);
        verify(mockEventPublisher1, times(1)).publish(mockEventPayload, mockEventContext);
        verify(mockEventPublisher2, never()).publish(any(), any());
    }

    @Test
    public void testPublishWithNoMatchingAdapter() {

        when(mockEventPublisher1.getAssociatedAdapter()).thenReturn("foo");
        when(mockEventPublisher2.getAssociatedAdapter()).thenReturn("bar");

        try {
            eventPublisherService.publish(mockEventPayload, mockEventContext);
            Assert.fail("Expected EventPublisherException");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof EventPublisherException);
            Assert.assertTrue(e.getMessage().contains(ErrorMessage.ERROR_CODE_EVENT_PUBLISHER_NOT_FOUND.getMessage()));
        }
    }

    @Test
    public void testCanHandleEventSuccess() throws Exception {

        when(mockEventPublisher1.getAssociatedAdapter()).thenReturn("webSubHubAdapter");
        when(mockEventPublisher2.getAssociatedAdapter()).thenReturn("otherAdapter");

        when(mockEventPublisher1.canHandleEvent(mockEventContext)).thenReturn(true);

        boolean result = eventPublisherService.canHandleEvent(mockEventContext);

        Assert.assertTrue(result, "Expected canHandleEvent to return true");
        verify(mockEventPublisher1, times(1)).canHandleEvent(mockEventContext);
        verify(mockEventPublisher2, never()).canHandleEvent(any());
    }

    @Test
    public void testCanHandleEventWithException() throws Exception {

        when(mockEventPublisher1.getAssociatedAdapter()).thenReturn("webSubHubAdapter");
        when(mockEventPublisher2.getAssociatedAdapter()).thenReturn("otherAdapter");

        doThrow(new EventPublisherException("E", "msg", "desc"))
                .when(mockEventPublisher1).canHandleEvent(mockEventContext);

        eventPublisherService.canHandleEvent(mockEventContext);

        verify(mockEventPublisher1, times(1)).canHandleEvent(mockEventContext);
        verify(mockEventPublisher2, never()).canHandleEvent(any());
    }

    @Test
    public void testCanHandleEventWithNoMatchingAdapter() {

        when(mockEventPublisher1.getAssociatedAdapter()).thenReturn("foo");
        when(mockEventPublisher2.getAssociatedAdapter()).thenReturn("bar");

        try {
            eventPublisherService.canHandleEvent(mockEventContext);
            Assert.fail("Expected EventPublisherException");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof EventPublisherException);
            Assert.assertTrue(e.getMessage().contains(ErrorMessage.ERROR_CODE_EVENT_PUBLISHER_NOT_FOUND.getMessage()));
        }
    }

    // --- The rest of your builder and singleton tests remain unchanged ---

    @Test
    public void testEventContextBuilder() {

        EventContext eventContext = EventContext.builder()
                .tenantDomain("example.com")
                .eventUri("http://example.com/event")
                .eventProfileVersion("1.0.0")
                .build();

        Assert.assertEquals(eventContext.getTenantDomain(), "example.com");
        Assert.assertEquals(eventContext.getEventUri(), "http://example.com/event");
        Assert.assertEquals(eventContext.getEventProfileVersion(), "1.0.0");
    }

    @Test
    public void testSecurityEventTokenPayloadBuilder() {

        Map<String, EventPayload> eventMap = new HashMap<>();
        eventMap.put("key1", new EventPayload() {
        });

        SecurityEventTokenPayload payload = SecurityEventTokenPayload.builder()
                .iss("issuer")
                .jti("jti123")
                .iat(123456789L)
                .aud("audience")
                .txn("transaction")
                .rci("rci123")
                .events(eventMap)
                .build();

        Assert.assertEquals(payload.getIss(), "issuer");
        Assert.assertEquals(payload.getJti(), "jti123");
        Assert.assertEquals(payload.getIat(), 123456789L);
        Assert.assertEquals(payload.getAud(), "audience");
        Assert.assertEquals(payload.getTxn(), "transaction");
        Assert.assertEquals(payload.getRci(), "rci123");
        Assert.assertNotNull(payload.getEvents());
        Assert.assertEquals(payload.getEvents().get("key1"), eventMap.get("key1"));
    }

    @Test
    public void testSecurityEventTokenPayloadWithNullEvent() {

        SecurityEventTokenPayload payload = SecurityEventTokenPayload.builder()
                .iss("issuer")
                .jti("jti123")
                .iat(123456789L)
                .aud("audience")
                .txn("transaction")
                .rci("rci123")
                .events(null)
                .build();

        Assert.assertEquals(payload.getIss(), "issuer");
        Assert.assertEquals(payload.getJti(), "jti123");
        Assert.assertEquals(payload.getIat(), 123456789L);
        Assert.assertEquals(payload.getAud(), "audience");
        Assert.assertEquals(payload.getTxn(), "transaction");
        Assert.assertEquals(payload.getRci(), "rci123");
        Assert.assertNull(payload.getEvents());
    }

    @Test
    public void testSecurityEventTokenPayloadWithEmptyEvent() {

        SecurityEventTokenPayload payload = SecurityEventTokenPayload.builder()
                .iss("issuer")
                .jti("jti123")
                .iat(123456789L)
                .aud("audience")
                .txn("transaction")
                .rci("rci123")
                .events(new HashMap<>())
                .build();

        Assert.assertEquals(payload.getIss(), "issuer");
        Assert.assertEquals(payload.getJti(), "jti123");
        Assert.assertEquals(payload.getIat(), 123456789L);
        Assert.assertEquals(payload.getAud(), "audience");
        Assert.assertEquals(payload.getTxn(), "transaction");
        Assert.assertEquals(payload.getRci(), "rci123");
        Assert.assertNotNull(payload.getEvents());
        Assert.assertTrue(payload.getEvents().isEmpty());
    }

    @Test
    public void testSecurityEventTokenPayloadWithSubId() {

        Map<String, EventPayload> eventMap = new HashMap<>();
        eventMap.put("key1", new EventPayload() {
        });

        Subject subId = SimpleSubject.createOpaqueSubject("subId123");

        SecurityEventTokenPayload payload = SecurityEventTokenPayload.builder()
                .iss("issuer")
                .jti("jti123")
                .iat(123456789L)
                .aud("audience")
                .txn("transaction")
                .rci("rci123")
                .subId(subId)
                .events(eventMap)
                .build();

        Assert.assertEquals(payload.getIss(), "issuer");
        Assert.assertEquals(payload.getJti(), "jti123");
        Assert.assertEquals(payload.getIat(), 123456789L);
        Assert.assertEquals(payload.getAud(), "audience");
        Assert.assertEquals(payload.getTxn(), "transaction");
        Assert.assertEquals(payload.getRci(), "rci123");
        Assert.assertEquals(payload.getSubId(), subId);
        Assert.assertNotNull(payload.getEvents());
        Assert.assertEquals(payload.getEvents().get("key1"), eventMap.get("key1"));
    }

    @Test
    public void testSecurityEventTokenPayloadWithNullSubId() {

        Map<String, EventPayload> eventMap = new HashMap<>();
        eventMap.put("key1", new EventPayload() {
        });

        SecurityEventTokenPayload payload = SecurityEventTokenPayload.builder()
                .iss("issuer")
                .jti("jti123")
                .iat(123456789L)
                .aud("audience")
                .txn("transaction")
                .rci("rci123")
                .subId(null)
                .events(eventMap)
                .build();

        Assert.assertEquals(payload.getIss(), "issuer");
        Assert.assertEquals(payload.getJti(), "jti123");
        Assert.assertEquals(payload.getIat(), 123456789L);
        Assert.assertEquals(payload.getAud(), "audience");
        Assert.assertEquals(payload.getTxn(), "transaction");
        Assert.assertEquals(payload.getRci(), "rci123");
        Assert.assertNull(payload.getSubId());
        Assert.assertNotNull(payload.getEvents());
        Assert.assertEquals(payload.getEvents().get("key1"), eventMap.get("key1"));
    }

    @Test
    public void testSecurityEventTokenPayloadWithEmptySubId() {

        Map<String, EventPayload> eventMap = new HashMap<>();
        eventMap.put("key1", new EventPayload() {
        });

        Subject subId = SimpleSubject.createOpaqueSubject("");

        SecurityEventTokenPayload payload = SecurityEventTokenPayload.builder()
                .iss("issuer")
                .jti("jti123")
                .iat(123456789L)
                .aud("audience")
                .txn("transaction")
                .rci("rci123")
                .subId(subId)
                .events(eventMap)
                .build();

        Assert.assertEquals(payload.getIss(), "issuer");
        Assert.assertEquals(payload.getJti(), "jti123");
        Assert.assertEquals(payload.getIat(), 123456789L);
        Assert.assertEquals(payload.getAud(), "audience");
        Assert.assertEquals(payload.getTxn(), "transaction");
        Assert.assertEquals(payload.getRci(), "rci123");
        Assert.assertEquals(payload.getSubId(), subId);
        Assert.assertNotNull(payload.getEvents());
        Assert.assertEquals(payload.getEvents().get("key1"), eventMap.get("key1"));
    }

    @Test
    public void testGetInstanceReturnsSingleton() {

        EventPublisherServiceImpl instance1 = EventPublisherServiceImpl.getInstance();
        EventPublisherServiceImpl instance2 = EventPublisherServiceImpl.getInstance();
        Assert.assertNotNull(instance1);
        Assert.assertSame(instance1, instance2, "getInstance() should return the same singleton instance");
    }
}
