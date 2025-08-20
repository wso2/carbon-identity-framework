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

package org.wso2.carbon.identity.webhook.metadata.service;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.webhook.metadata.api.exception.WebhookMetadataException;
import org.wso2.carbon.identity.webhook.metadata.api.model.Adapter;
import org.wso2.carbon.identity.webhook.metadata.api.model.AdapterType;
import org.wso2.carbon.identity.webhook.metadata.internal.dao.impl.FileBasedEventAdapterMetadataDAOImpl;
import org.wso2.carbon.identity.webhook.metadata.internal.service.impl.EventAdapterMetadataServiceImpl;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;

/**
 * Unit tests for EventAdapterMetadataServiceImpl.
 */
public class EventAdapterMetadataServiceImplTest {

    @Mock
    private FileBasedEventAdapterMetadataDAOImpl mockDAO;

    private EventAdapterMetadataServiceImpl service;

    @BeforeMethod
    public void setUp() throws Exception {

        MockitoAnnotations.openMocks(this);
        service = EventAdapterMetadataServiceImpl.getInstance();

        Field daoField = EventAdapterMetadataServiceImpl.class.getDeclaredField("adapterMetadataDAO");
        daoField.setAccessible(true);
        daoField.set(service, mockDAO);

        Field initializedField = EventAdapterMetadataServiceImpl.class.getDeclaredField("initialized");
        initializedField.setAccessible(true);
        initializedField.set(service, true);

        Field adaptersField = EventAdapterMetadataServiceImpl.class.getDeclaredField("adapters");
        adaptersField.setAccessible(true);
        Adapter enabledAdapter = new Adapter.Builder()
                .name("websubhub")
                .type(AdapterType.PublisherSubscriber)
                .enabled(true)
                .properties(Collections.singletonMap("baseUrl", "http://localhost:9000/hub"))
                .build();
        Adapter disabledAdapter = new Adapter.Builder()
                .name("httppublisher")
                .type(AdapterType.Publisher)
                .enabled(false)
                .properties(Collections.emptyMap())
                .build();
        adaptersField.set(service, Arrays.asList(enabledAdapter, disabledAdapter));
    }

    @AfterMethod
    public void tearDown() {
        // No cleanup needed
    }

    @Test
    public void testInitSuccess() throws Exception {
        // Prepare
        Field initializedField = EventAdapterMetadataServiceImpl.class.getDeclaredField("initialized");
        initializedField.setAccessible(true);
        initializedField.set(service, false);

        List<Adapter> adapters = Arrays.asList(
                new Adapter.Builder().name("A").type(AdapterType.Publisher).enabled(true).build()
        );
        Mockito.doNothing().when(mockDAO).init();
        when(mockDAO.getAdapters()).thenReturn(adapters);

        // Act
        service.init();

        // Assert
        Assert.assertTrue((Boolean) initializedField.get(service));
        Assert.assertEquals(service.getAdapters().size(), 1);
    }

    @Test
    public void testInitAlreadyInitialized() throws Exception {

        Field initializedField = EventAdapterMetadataServiceImpl.class.getDeclaredField("initialized");
        initializedField.setAccessible(true);
        initializedField.set(service, true);

        service.init(); // Should not call DAO again, no exception
        Assert.assertTrue((Boolean) initializedField.get(service));
    }

    @Test
    public void testGetAdapters() throws Exception {

        when(mockDAO.getAdapters()).thenReturn(service.getAdapters());
        List<Adapter> result = service.getAdapters();
        Assert.assertNotNull(result);
        Assert.assertEquals(result.size(), 2);
        Assert.assertEquals(result.get(0).getName(), "websubhub");
        Assert.assertEquals(result.get(1).getName(), "httppublisher");
    }

    @Test
    public void testGetCurrentActiveAdapter() throws Exception {

        Adapter active = service.getCurrentActiveAdapter();
        Assert.assertNotNull(active);
        Assert.assertEquals(active.getName(), "websubhub");
        Assert.assertTrue(active.isEnabled());
        Assert.assertEquals(active.getType(), AdapterType.PublisherSubscriber);
    }

    @Test
    public void testGetAdapterByName() throws Exception {

        Adapter adapter = service.getAdapterByName("httppublisher");
        Assert.assertNotNull(adapter);
        Assert.assertEquals(adapter.getName(), "httppublisher");
        Assert.assertFalse(adapter.isEnabled());
        Assert.assertEquals(adapter.getType(), AdapterType.Publisher);
    }

    @Test(expectedExceptions = WebhookMetadataException.class)
    public void testGetAdaptersNotInitialized() throws Exception {

        Field initializedField = EventAdapterMetadataServiceImpl.class.getDeclaredField("initialized");
        initializedField.setAccessible(true);
        initializedField.set(service, false);
        service.getAdapters();
    }

    @Test(expectedExceptions = WebhookMetadataException.class)
    public void testGetCurrentActiveAdapterNotInitialized() throws Exception {

        Field initializedField = EventAdapterMetadataServiceImpl.class.getDeclaredField("initialized");
        initializedField.setAccessible(true);
        initializedField.set(service, false);
        service.getCurrentActiveAdapter();
    }

    @Test(expectedExceptions = WebhookMetadataException.class)
    public void testGetAdapterByNameNotFound() throws Exception {

        service.getAdapterByName("nonexistent");
    }
}
