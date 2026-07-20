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

package org.wso2.carbon.identity.flow.extension.model;

import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.action.management.api.model.EndpointConfig;
import org.wso2.carbon.identity.certificate.management.model.Certificate;

import java.sql.Timestamp;
import java.util.Collections;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;

/**
 * Unit tests for {@link FlowExtensionAction} and its {@code ResponseBuilder} / {@code RequestBuilder},
 * verifying the extra Flow Extension fields (access config, certificate, icon URL) are carried on
 * top of the inherited {@link Action} fields.
 */
public class FlowExtensionActionTest {

    private AccessConfig sampleAccessConfig() {

        return new AccessConfig(
                Collections.singletonList(new ContextPath("/user/id", false)),
                Collections.singletonList(new ContextPath("/user/credentials/password", true)));
    }

    private Certificate sampleCertificate() {

        return new Certificate.Builder()
                .id("cert-1")
                .name("cert")
                .certificateContent("pem-content")
                .build();
    }

    @Test
    public void testResponseBuilderCarriesAllFields() {

        AccessConfig accessConfig = sampleAccessConfig();
        Certificate certificate = sampleCertificate();
        EndpointConfig endpoint = new EndpointConfig();
        Timestamp now = new Timestamp(1_700_000_000_000L);

        FlowExtensionAction action = new FlowExtensionAction.ResponseBuilder()
                .id("action-1")
                .type(Action.ActionTypes.FLOW_EXTENSION)
                .name("my-extension")
                .description("desc")
                .status(Action.Status.ACTIVE)
                .actionVersion("v1")
                .createdAt(now)
                .updatedAt(now)
                .endpoint(endpoint)
                .accessConfig(accessConfig)
                .certificate(certificate)
                .iconUrl("https://icon")
                .build();

        assertEquals(action.getId(), "action-1");
        assertEquals(action.getType(), Action.ActionTypes.FLOW_EXTENSION);
        assertEquals(action.getName(), "my-extension");
        assertEquals(action.getDescription(), "desc");
        assertEquals(action.getStatus(), Action.Status.ACTIVE);
        assertEquals(action.getActionVersion(), "v1");
        assertEquals(action.getCreatedAt(), now);
        assertEquals(action.getUpdatedAt(), now);
        assertSame(action.getEndpoint(), endpoint);
        assertSame(action.getAccessConfig(), accessConfig);
        assertSame(action.getCertificate(), certificate);
        assertEquals(action.getIconUrl(), "https://icon");
    }

    @Test
    public void testRequestBuilderCopiesBaseActionAndAddsExtensionFields() {

        EndpointConfig endpoint = new EndpointConfig();
        Action baseAction = new Action.ActionResponseBuilder()
                .name("base-name")
                .description("base-desc")
                .actionVersion("v2")
                .endpoint(endpoint)
                .build();

        AccessConfig accessConfig = sampleAccessConfig();
        Certificate certificate = sampleCertificate();

        FlowExtensionAction action = new FlowExtensionAction.RequestBuilder(baseAction)
                .accessConfig(accessConfig)
                .certificate(certificate)
                .iconUrl("https://icon")
                .build();

        assertEquals(action.getName(), "base-name");
        assertEquals(action.getDescription(), "base-desc");
        assertEquals(action.getActionVersion(), "v2");
        assertSame(action.getEndpoint(), endpoint);
        assertSame(action.getAccessConfig(), accessConfig);
        assertSame(action.getCertificate(), certificate);
        assertEquals(action.getIconUrl(), "https://icon");
    }

    @Test
    public void testUnsetExtensionFieldsAreNull() {

        FlowExtensionAction action = new FlowExtensionAction.ResponseBuilder()
                .name("no-extras")
                .build();

        assertNull(action.getAccessConfig());
        assertNull(action.getCertificate());
        assertNull(action.getIconUrl());
    }
}
