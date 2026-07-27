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

package org.wso2.carbon.identity.event.publisher.internal;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.event.publisher.api.service.EventPublisher;
import org.wso2.carbon.identity.event.publisher.internal.component.EventPublisherComponentServiceHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Consolidated test class for EventPublisherService, EventContext, and SecurityEventTokenPayload.
 */
public class EventPublisherComponentServiceHolderTest {

    private EventPublisherComponentServiceHolder holder;
    private EventPublisher publisher1;
    private EventPublisher publisher2;

    @BeforeMethod
    public void setUp() {

        holder = EventPublisherComponentServiceHolder.getInstance();
        publisher1 = Mockito.mock(EventPublisher.class);
        publisher2 = Mockito.mock(EventPublisher.class);
        holder.setEventPublishers(new ArrayList<>());
    }

    @AfterMethod
    public void tearDown() {

        holder.setEventPublishers(new ArrayList<>());
    }

    @Test
    public void testAddEventPublisher() {

        holder.addEventPublisher(publisher1);
        Assert.assertTrue(holder.getEventPublishers().contains(publisher1));
    }

    @Test
    public void testRemoveEventPublisher() {

        holder.addEventPublisher(publisher1);
        holder.removeEventPublisher(publisher1);
        Assert.assertFalse(holder.getEventPublishers().contains(publisher1));
    }

    @Test
    public void testSetEventPublishers() {

        List<EventPublisher> publishers = Arrays.asList(publisher1, publisher2);
        holder.setEventPublishers(publishers);
        Assert.assertEquals(holder.getEventPublishers(), publishers);
    }

    @Test
    public void testGetEventPublishersReturnsMutableList() {

        holder.addEventPublisher(publisher1);
        List<EventPublisher> publishers = holder.getEventPublishers();
        publishers.add(publisher2);
        Assert.assertTrue(holder.getEventPublishers().contains(publisher2));
    }
}
