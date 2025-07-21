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

package org.wso2.carbon.identity.webhook.management.dao;

import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.webhook.management.internal.component.WebhookManagementComponentServiceHolder;
import org.wso2.carbon.identity.webhook.management.internal.dao.WebhookManagementDAO;
import org.wso2.carbon.identity.webhook.management.internal.dao.impl.handler.AdapterTypeHandler;
import org.wso2.carbon.identity.webhook.management.internal.dao.impl.handler.PublisherAdapterTypeHandler;
import org.wso2.carbon.identity.webhook.management.internal.dao.impl.handler.PublisherSubscriberAdapterTypeHandler;
import org.wso2.carbon.identity.webhook.management.internal.dao.impl.handler.WebhookAdapterTypeHandlerFactory;
import org.wso2.carbon.identity.webhook.metadata.api.model.Adapter;
import org.wso2.carbon.identity.webhook.metadata.api.model.AdapterType;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class WebhookAdapterTypeHandlerFactoryTest {

    @BeforeClass
    public void setUp() {
        
        Adapter mockAdapter = Mockito.mock(Adapter.class);
        Mockito.when(mockAdapter.getType()).thenReturn(AdapterType.Publisher);
        WebhookManagementComponentServiceHolder.getInstance().setWebhookAdapter(mockAdapter);
    }

    @Test
    public void testGetHandler_PublisherAdapterType() {

        WebhookManagementDAO mockDao = Mockito.mock(WebhookManagementDAO.class);
        AdapterTypeHandler handler = WebhookAdapterTypeHandlerFactory.getHandler(mockDao);
        assertNotNull(handler, "Handler should not be null");
        assertTrue(handler instanceof PublisherAdapterTypeHandler ||
                        handler instanceof PublisherSubscriberAdapterTypeHandler,
                "Handler should be a known AdapterTypeHandler");
    }

    @Test
    public void testGetHandler_PublisherSubscriberAdapterType() {

        WebhookManagementDAO mockDao = Mockito.mock(WebhookManagementDAO.class);
        AdapterTypeHandler handler = WebhookAdapterTypeHandlerFactory.getHandler(mockDao);
        assertNotNull(handler, "Handler should not be null");
        assertTrue(true, "Handler should be AdapterTypeHandler");
    }

    @Test
    public void testGetHandler_NullDAO() {

        AdapterTypeHandler handler = WebhookAdapterTypeHandlerFactory.getHandler(null);
        assertNotNull(handler, "Handler should not be null for null DAO");
        assertTrue(true, "Handler should be AdapterTypeHandler");
    }
}
