/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.flow.extension.management;

import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.management.api.model.ActionDTO;
import org.wso2.carbon.identity.action.management.api.model.EndpointConfig;
import org.wso2.carbon.identity.flow.extension.model.AccessConfig;
import org.wso2.carbon.identity.flow.extension.model.ContextPath;
import org.wso2.carbon.identity.flow.extension.model.FlowExtensionAction;

import java.util.Arrays;
import java.util.Collections;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for {@link FlowExtensionActionConverter}, covering the allowed headers of the endpoint, which are the
 * headers the access config exposes.
 */
public class FlowExtensionActionConverterTest {

    private static final String URI = "https://extension.example.com";
    private static final String REGISTRATION = "REGISTRATION";
    private static final String PASSWORD_RECOVERY = "PASSWORD_RECOVERY";

    private final FlowExtensionActionConverter converter = new FlowExtensionActionConverter();

    @Test
    public void testExposedHeadersBecomeAllowedHeaders() {

        AccessConfig accessConfig = new AccessConfig(Arrays.asList(
                new ContextPath("/user/id", false),
                new ContextPath("/request/additionalHeaders[name=X-Tenant]", false, REGISTRATION),
                new ContextPath("/request/additionalHeaders[name=x-tenant]", true, PASSWORD_RECOVERY),
                new ContextPath("/request/additionalHeaders[name=x-device]", false, REGISTRATION),
                new ContextPath("/runtimeValues[name=plan]", false, REGISTRATION)), null);
        EndpointConfig endpoint = new EndpointConfig.EndpointConfigBuilder()
                .uri(URI)
                .allowedHeaders(Collections.singletonList("x-other"))
                .build();

        ActionDTO actionDTO = converter.buildActionDTO(buildAction(endpoint, accessConfig));

        assertEquals(actionDTO.getEndpoint().getAllowedHeaders(), Arrays.asList("x-tenant", "x-device"));
        assertEquals(actionDTO.getEndpoint().getUri(), URI);
    }

    @Test
    public void testExposeWithoutHeadersClearsAllowedHeaders() {

        AccessConfig accessConfig = new AccessConfig(
                Collections.singletonList(new ContextPath("/user/id", false)), null);

        ActionDTO actionDTO = converter.buildActionDTO(buildAction(null, accessConfig));

        // An update with only the allowed headers merges them with the existing endpoint.
        assertTrue(actionDTO.getEndpoint().getAllowedHeaders().isEmpty());
        assertNull(actionDTO.getEndpoint().getUri());
    }

    @Test
    public void testAllowedHeadersAreNotKeptWithoutExpose() {

        EndpointConfig endpoint = new EndpointConfig.EndpointConfigBuilder()
                .uri(URI)
                .allowedHeaders(Collections.singletonList("x-other"))
                .build();
        AccessConfig accessConfig = new AccessConfig(null,
                Collections.singletonList(new ContextPath("/user/claims[uri=http://wso2.org/claims/email]", false)));

        ActionDTO actionDTO = converter.buildActionDTO(buildAction(endpoint, accessConfig));

        assertNull(actionDTO.getEndpoint().getAllowedHeaders());
        assertEquals(actionDTO.getEndpoint().getUri(), URI);
    }

    @Test
    public void testEndpointIsLeftOutWhenNeitherIsGiven() {

        ActionDTO actionDTO = converter.buildActionDTO(buildAction(null, null));

        assertNull(actionDTO.getEndpoint());
    }

    private FlowExtensionAction buildAction(EndpointConfig endpoint, AccessConfig accessConfig) {

        return new FlowExtensionAction.ResponseBuilder()
                .name("Flow Extension")
                .endpoint(endpoint)
                .accessConfig(accessConfig)
                .build();
    }
}
