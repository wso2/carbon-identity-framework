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

package org.wso2.carbon.identity.user.pre.update.profile.action.model;

import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.action.management.api.model.Authentication;
import org.wso2.carbon.identity.action.management.api.model.EndpointConfig;
import org.wso2.carbon.identity.user.pre.update.profile.action.api.model.PreUpdateProfileAction;

import java.sql.Timestamp;
import java.util.Date;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.TEST_ACTION;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.TEST_ATTRIBUTES;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.TEST_DESCRIPTION;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.TEST_ID;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.TEST_PASSWORD;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.TEST_URL;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.TEST_USERNAME;

public class PreUpdateProfileActionBuilderTest {

    @Test
    public void testBuildActionRequest() {

        Action action = new Action.ActionRequestBuilder()
                .name(TEST_ACTION)
                .description(TEST_DESCRIPTION)
                .endpoint(new EndpointConfig.EndpointConfigBuilder()
                        .uri(TEST_URL)
                        .authentication(new Authentication.BasicAuthBuilder(TEST_USERNAME, TEST_PASSWORD).build())
                        .build())
                .build();

        PreUpdateProfileAction.RequestBuilder requestBuilder = new PreUpdateProfileAction.RequestBuilder(action)
                .attributes(TEST_ATTRIBUTES);

        PreUpdateProfileAction preUpdateProfileAction = requestBuilder.build();

        assertNotNull(preUpdateProfileAction);
        assertNull(preUpdateProfileAction.getId());
        assertNull(preUpdateProfileAction.getStatus());
        assertEquals(preUpdateProfileAction.getName(), TEST_ACTION);
        assertEquals(preUpdateProfileAction.getDescription(), TEST_DESCRIPTION);
        assertEquals(preUpdateProfileAction.getEndpoint().getUri(), TEST_URL);
        assertEquals(preUpdateProfileAction.getEndpoint().getAuthentication().getType(), Authentication.Type.BASIC);
        assertEquals(preUpdateProfileAction.getEndpoint().getAuthentication()
                .getProperty(Authentication.Property.USERNAME).getValue(), TEST_USERNAME);
        assertEquals(preUpdateProfileAction.getEndpoint().getAuthentication()
                .getProperty(Authentication.Property.PASSWORD).getValue(), TEST_PASSWORD);
        assertEquals(preUpdateProfileAction.getAttributes(), TEST_ATTRIBUTES);
    }

    @Test
    public void testBuildActionResponse() {

        Timestamp createdAt = new Timestamp(new Date().getTime());
        Timestamp updatedAt = new Timestamp(new Date().getTime() + 5000);
        PreUpdateProfileAction.ResponseBuilder responseBuilder = new PreUpdateProfileAction.ResponseBuilder()
                .id(TEST_ID)
                .name(TEST_ACTION)
                .description(TEST_DESCRIPTION)
                .status(Action.Status.ACTIVE)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .endpoint(new EndpointConfig.EndpointConfigBuilder()
                        .uri(TEST_URL)
                        .authentication(new Authentication.BasicAuthBuilder(TEST_USERNAME, TEST_PASSWORD).build())
                        .build())
                .attributes(TEST_ATTRIBUTES);

        PreUpdateProfileAction preUpdateProfileAction = responseBuilder.build();

        assertNotNull(preUpdateProfileAction);
        assertEquals(preUpdateProfileAction.getId(), TEST_ID);
        assertEquals(preUpdateProfileAction.getName(), TEST_ACTION);
        assertEquals(preUpdateProfileAction.getDescription(), TEST_DESCRIPTION);
        assertEquals(preUpdateProfileAction.getStatus(), Action.Status.ACTIVE);
        assertEquals(preUpdateProfileAction.getCreatedAt().getTime(), createdAt.getTime());
        assertEquals(preUpdateProfileAction.getUpdatedAt().getTime(), updatedAt.getTime());
        assertEquals(preUpdateProfileAction.getEndpoint().getUri(), TEST_URL);
        assertEquals(preUpdateProfileAction.getEndpoint().getAuthentication().getType(), Authentication.Type.BASIC);
        assertEquals(preUpdateProfileAction.getEndpoint().getAuthentication()
                .getProperty(Authentication.Property.USERNAME).getValue(), TEST_USERNAME);
        assertEquals(preUpdateProfileAction.getEndpoint().getAuthentication()
                .getProperty(Authentication.Property.PASSWORD).getValue(), TEST_PASSWORD);
        assertEquals(preUpdateProfileAction.getAttributes(), TEST_ATTRIBUTES);
    }
}
