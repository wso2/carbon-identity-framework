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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.flow.extension.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.execution.api.model.Organization;

import java.util.Collections;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link FlowExtensionOrganization}.
 */
public class FlowExtensionOrganizationTest {

    private ObjectMapper mapper;

    @BeforeClass
    public void setUp() {

        mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }

    @Test
    public void testOrganizationFieldsAndAttributes() {

        FlowExtensionOrganization organization = new FlowExtensionOrganization.Builder()
                .id("org-1")
                .name("Acme")
                .orgHandle("acme")
                .description("Acme organization")
                .depth(2)
                .attributes(Collections.singletonMap("taxId", "123"))
                .build();

        assertEquals(organization.getId(), "org-1");
        assertEquals(organization.getName(), "Acme");
        assertEquals(organization.getOrgHandle(), "acme");
        assertEquals(organization.getDescription(), "Acme organization");
        assertEquals(organization.getDepth(), 2);
        assertEquals(organization.getAttributes().get("taxId"), "123");
    }

    @Test
    public void testOrganizationFieldsAreSerializedPolymorphically() throws Exception {

        Organization organization =
                new FlowExtensionOrganization.Builder()
                        .name("Acme")
                        .description("Acme organization")
                        .attributes(Collections.singletonMap("taxId", "123"))
                        .build();

        String json = mapper.writeValueAsString(organization);

        assertTrue(json.contains("\"name\":\"Acme\""), json);
        assertTrue(json.contains("\"description\":\"Acme organization\""), json);
        assertTrue(json.contains("\"taxId\":\"123\""), json);
    }

    @Test
    public void testEmptyOptionalFieldsAreOmitted() throws Exception {

        String json = mapper.writeValueAsString(new FlowExtensionOrganization.Builder().build());

        assertFalse(json.contains("description"), json);
        assertFalse(json.contains("attributes"), json);
    }
}
