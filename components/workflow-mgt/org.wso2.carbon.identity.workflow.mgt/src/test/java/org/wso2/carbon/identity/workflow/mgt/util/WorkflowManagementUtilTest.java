/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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

package org.wso2.carbon.identity.workflow.mgt.util;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.workflow.mgt.bean.Parameter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for {@link WorkflowManagementUtil}.
 */
public class WorkflowManagementUtilTest {

    // ---- isUUID ----

    @DataProvider(name = "validUUIDs")
    public Object[][] validUUIDs() {

        return new Object[][]{
                {UUID.randomUUID().toString()},
                {"550e8400-e29b-41d4-a716-446655440000"},
                {"00000000-0000-0000-0000-000000000000"}
        };
    }

    /**
     * Test that isUUID returns true for well-formed UUID strings.
     *
     * @param uuid Valid UUID string.
     */
    @Test(dataProvider = "validUUIDs")
    public void testIsUUID_withValidUUID_returnsTrue(String uuid) {

        assertTrue(WorkflowManagementUtil.isUUID(uuid));
    }

    /**
     * Test that isUUID returns false when the input is null.
     */
    @Test
    public void testIsUUID_withNull_returnsFalse() {

        assertFalse(WorkflowManagementUtil.isUUID(null));
    }

    @DataProvider(name = "nonUUIDStrings")
    public Object[][] nonUUIDStrings() {

        return new Object[][]{
                {"not-a-uuid"},
                {"//User[@name='john']"},
                {""},
                {"false()"},
                {"!@#$invalid"},
                {"550e8400-e29b-41d4-a716"}  // Truncated UUID.
        };
    }

    /**
     * Test that isUUID returns false for strings that are not valid UUIDs.
     *
     * @param value Non-UUID string.
     */
    @Test(dataProvider = "nonUUIDStrings")
    public void testIsUUID_withNonUUIDString_returnsFalse(String value) {

        assertFalse(WorkflowManagementUtil.isUUID(value));
    }

    // ---- createWorkflowRoleName ----

    /**
     * Test that createWorkflowRoleName returns a role name with the Internal domain prefix.
     */
    @Test
    public void testCreateWorkflowRoleName_returnsInternalPrefixedName() {

        String roleName = WorkflowManagementUtil.createWorkflowRoleName("ApprovalWorkflow");
        assertEquals(roleName, "Internal/ApprovalWorkflow");
    }

    // ---- getParameter ----

    /**
     * Test that getParameter returns the matching parameter when it exists in the list.
     */
    @Test
    public void testGetParameter_whenMatch_returnsParameter() {

        Parameter param = new Parameter();
        param.setParamName("stepCount");
        param.setqName("stepCount");
        param.setHolder("Template");
        param.setParamValue("2");

        List<Parameter> paramList = Collections.singletonList(param);
        Parameter result = WorkflowManagementUtil.getParameter(paramList, "stepCount", "Template");
        assertEquals(result, param);
    }

    /**
     * Test that getParameter returns null when no parameter matches the given name and holder.
     */
    @Test
    public void testGetParameter_whenNoMatch_returnsNull() {

        Parameter param = new Parameter();
        param.setParamName("stepCount");
        param.setqName("stepCount");
        param.setHolder("Template");

        List<Parameter> paramList = Collections.singletonList(param);
        assertNull(WorkflowManagementUtil.getParameter(paramList, "missing", "Template"));
        assertNull(WorkflowManagementUtil.getParameter(paramList, "stepCount", "WrongHolder"));
    }

    /**
     * Test that getParameter returns null on an empty list.
     */
    @Test
    public void testGetParameter_withEmptyList_returnsNull() {

        assertNull(WorkflowManagementUtil.getParameter(Collections.emptyList(), "any", "any"));
    }

    // ---- readFileFromResource ----

    /**
     * Test that readFileFromResource returns the full content of the provided stream.
     */
    @Test
    public void testReadFileFromResource_returnsStreamContent() throws Exception {

        String expected = "hello workflow content";
        InputStream stream = new ByteArrayInputStream(expected.getBytes(StandardCharsets.UTF_8));
        String result = WorkflowManagementUtil.readFileFromResource(stream);
        assertEquals(result, expected);
    }

    /**
     * Test that readFileFromResource returns an empty string for an empty stream.
     */
    @Test
    public void testReadFileFromResource_withEmptyStream_returnsEmptyString() throws Exception {

        InputStream stream = new ByteArrayInputStream(new byte[0]);
        String result = WorkflowManagementUtil.readFileFromResource(stream);
        assertEquals(result, "");
    }
}
