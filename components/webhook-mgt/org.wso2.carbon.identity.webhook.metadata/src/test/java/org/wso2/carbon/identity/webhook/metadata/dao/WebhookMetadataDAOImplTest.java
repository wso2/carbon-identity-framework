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

package org.wso2.carbon.identity.webhook.metadata.dao;

import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.webhook.metadata.api.exception.WebhookMetadataException;
import org.wso2.carbon.identity.webhook.metadata.api.model.BinaryObject;
import org.wso2.carbon.identity.webhook.metadata.internal.dao.impl.WebhookMetadataDAOImpl;
import org.wso2.carbon.identity.webhook.metadata.internal.model.WebhookMetadataProperty;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

@WithH2Database(files = {"dbscripts/h2.sql"})
@WithCarbonHome
public class WebhookMetadataDAOImplTest {

    private static final int TENANT_ID = 1;
    private static final String PRIMITIVE_PROPERTY_NAME = "orgPolicy";
    private static final String PRIMITIVE_PROPERTY_VALUE = "NO_SHARING";
    private static final String OBJECT_PROPERTY_NAME = "customObject";
    private static final byte[] OBJECT_PROPERTY_DATA = "test-object-data".getBytes();

    private final WebhookMetadataDAOImpl dao = new WebhookMetadataDAOImpl();

    @Test
    public void testAddAndGetPrimitiveProperty() throws WebhookMetadataException {

        Map<String, WebhookMetadataProperty> properties = new HashMap<>();
        properties.put(PRIMITIVE_PROPERTY_NAME,
                new WebhookMetadataProperty.Builder(PRIMITIVE_PROPERTY_VALUE).build());

        dao.addWebhookMetadataProperties(properties, TENANT_ID);

        Map<String, WebhookMetadataProperty> retrieved = dao.getWebhookMetadataProperties(TENANT_ID);
        assertNotNull(retrieved);
        assertTrue(retrieved.containsKey(PRIMITIVE_PROPERTY_NAME));
        assertEquals(retrieved.get(PRIMITIVE_PROPERTY_NAME).getValue(), PRIMITIVE_PROPERTY_VALUE);
        assertTrue(retrieved.get(PRIMITIVE_PROPERTY_NAME).isPrimitive());
    }

    @Test(dependsOnMethods = "testAddAndGetPrimitiveProperty")
    public void testUpdatePrimitiveProperty() throws WebhookMetadataException {

        Map<String, WebhookMetadataProperty> properties = new HashMap<>();
        String updatedValue = "SHARING_ALLOWED";
        properties.put(PRIMITIVE_PROPERTY_NAME,
                new WebhookMetadataProperty.Builder(updatedValue).build());

        dao.updateWebhookMetadataProperties(properties, TENANT_ID);

        Map<String, WebhookMetadataProperty> retrieved = dao.getWebhookMetadataProperties(TENANT_ID);
        assertNotNull(retrieved);
        assertEquals(retrieved.get(PRIMITIVE_PROPERTY_NAME).getValue(), updatedValue);
    }

    @Test(dependsOnMethods = "testUpdatePrimitiveProperty")
    public void testAddAndGetObjectProperty() throws WebhookMetadataException {

        Map<String, WebhookMetadataProperty> properties = new HashMap<>();
        BinaryObject binaryObject = BinaryObject.fromInputStream(new ByteArrayInputStream(OBJECT_PROPERTY_DATA));
        properties.put(OBJECT_PROPERTY_NAME, new WebhookMetadataProperty.Builder(binaryObject).build());

        dao.addWebhookMetadataProperties(properties, TENANT_ID);

        Map<String, WebhookMetadataProperty> retrieved = dao.getWebhookMetadataProperties(TENANT_ID);
        assertNotNull(retrieved);
        assertTrue(retrieved.containsKey(OBJECT_PROPERTY_NAME));
        assertFalse(retrieved.get(OBJECT_PROPERTY_NAME).isPrimitive());
        BinaryObject retrievedObject = (BinaryObject) retrieved.get(OBJECT_PROPERTY_NAME).getValue();
        assertEquals(retrievedObject.getLength(), OBJECT_PROPERTY_DATA.length);
    }

    @Test(dependsOnMethods = "testAddAndGetObjectProperty")
    public void testUpdateObjectProperty() throws WebhookMetadataException {

        Map<String, WebhookMetadataProperty> properties = new HashMap<>();
        byte[] updatedData = "updated-object-data".getBytes();
        BinaryObject updatedObject = BinaryObject.fromInputStream(new ByteArrayInputStream(updatedData));
        properties.put(OBJECT_PROPERTY_NAME, new WebhookMetadataProperty.Builder(updatedObject).build());

        dao.updateWebhookMetadataProperties(properties, TENANT_ID);

        Map<String, WebhookMetadataProperty> retrieved = dao.getWebhookMetadataProperties(TENANT_ID);
        assertNotNull(retrieved);
        BinaryObject retrievedObject = (BinaryObject) retrieved.get(OBJECT_PROPERTY_NAME).getValue();
        assertEquals(retrievedObject.getLength(), updatedData.length);
    }
}
