/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.flow.execution.engine.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.reflect.Modifier;
import java.util.UUID;

/**
 * Tests organization ID initialization and preservation in the flow organization model.
 */
public class FlowOrganizationTest {

    @Test
    public void testOrganizationIdIsInitializedAtConstruction() {

        FlowOrganization organization = new FlowOrganization();
        String organizationId = organization.getOrganizationId();

        Assert.assertNotNull(organizationId);
        Assert.assertEquals(UUID.fromString(organizationId).toString(), organizationId);
        Assert.assertEquals(organization.getOrganizationId(), organizationId);
    }

    @Test
    public void testOrganizationIdFieldIsFinal() throws NoSuchFieldException {

        Assert.assertTrue(Modifier.isFinal(FlowOrganization.class.getDeclaredField("organizationId").getModifiers()),
                "The organization ID must not be reassigned during flow execution.");
    }

    @Test
    public void testOrganizationIdsAreUniqueAcrossInstances() {

        Assert.assertNotEquals(new FlowOrganization().getOrganizationId(),
                new FlowOrganization().getOrganizationId());
    }

    @Test
    public void testFlowContextInitializesOrganizationId() {

        FlowExecutionContext context = new FlowExecutionContext();

        Assert.assertNotNull(context.getFlowOrganization().getOrganizationId());
    }

    @Test
    public void testOrganizationIdIsPreservedDuringJsonRoundTrip() throws Exception {

        FlowOrganization organization = new FlowOrganization();
        ObjectMapper objectMapper = new ObjectMapper();

        String json = objectMapper.writeValueAsString(organization);
        FlowOrganization restored = objectMapper.readValue(json, FlowOrganization.class);

        Assert.assertNotSame(restored, organization);
        Assert.assertEquals(restored.getOrganizationId(), organization.getOrganizationId());
    }
}
