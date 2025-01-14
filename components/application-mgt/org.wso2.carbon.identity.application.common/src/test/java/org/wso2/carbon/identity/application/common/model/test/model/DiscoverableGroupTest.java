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

package org.wso2.carbon.identity.application.common.model.test.model;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.model.DiscoverableGroup;

import java.io.StringReader;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * Unit tests for the DiscoverableGroup.
 */
@Test
public class DiscoverableGroupTest {

    @Test(description = "Test the creation of the DiscoverableGroup model using XML content")
    public void testBuildDiscoverableGroupInstanceFromXML() {

        final String discoverableGroupsXML = "<DiscoverableGroup>\n" +
                "    <UserStore>test-domain</UserStore>\n" +
                "    <Group>test-group-id-1</Group>\n" +
                "    <Group>test-group-id-2</Group>\n" +
                "    <Group>test-group-id-3</Group>\n" +
                "</DiscoverableGroup>";

        OMElement rootElement =
                OMXMLBuilderFactory.createOMBuilder(new StringReader(discoverableGroupsXML)).getDocumentElement();
        DiscoverableGroup discoverableGroup = DiscoverableGroup.build(rootElement);
        assertEquals(discoverableGroup.getUserStore(), "test-domain",
                "The user store name does not match the user store name specified in the provided XML content");
        assertEquals(discoverableGroup.getGroups().length, 3,
                "The group list does not match the group list specified in the provided XML content");
        assertEquals(discoverableGroup.getGroups()[0], "test-group-id-1",
                "The group id does not match the group id specified in the provided XML content");
        assertEquals(discoverableGroup.getGroups()[1], "test-group-id-2",
                "The group id does not match the group id specified in the provided XML content");
        assertEquals(discoverableGroup.getGroups()[2], "test-group-id-3",
                "The group id does not match the group id specified in the provided XML content");
    }

    @Test(description = "Test the creation of the DiscoverableGroup model using XML content with empty user store")
    public void testBuildDiscoverableGroupInstanceFromXMLWithEmptyUserStore() {

        final String discoverableGroupsXML = "<DiscoverableGroup>\n" +
                "    <UserStore></UserStore>\n" +
                "    <Group>test-group-id-1</Group>\n" +
                "    <Group>test-group-id-2</Group>\n" +
                "    <Group>test-group-id-3</Group>\n" +
                "</DiscoverableGroup>";

        OMElement rootElement =
                OMXMLBuilderFactory.createOMBuilder(new StringReader(discoverableGroupsXML)).getDocumentElement();
        DiscoverableGroup discoverableGroup = DiscoverableGroup.build(rootElement);
        assertNull(discoverableGroup);
    }

    @Test(description = "Test the creation of the DiscoverableGroup model using XML content with whitespace" +
            " user store")
    public void testBuildDiscoverableGroupInstanceFromXMLWithWhitespaceUserStore() {

        final String discoverableGroupsXML = "<DiscoverableGroup>\n" +
                "    <UserStore>   </UserStore>\n" +
                "    <Group>test-group-id-1</Group>\n" +
                "    <Group>test-group-id-2</Group>\n" +
                "    <Group>test-group-id-3</Group>\n" +
                "</DiscoverableGroup>";

        OMElement rootElement =
                OMXMLBuilderFactory.createOMBuilder(new StringReader(discoverableGroupsXML)).getDocumentElement();
        DiscoverableGroup discoverableGroup = DiscoverableGroup.build(rootElement);
        assertNull(discoverableGroup);
    }

    @Test(description = "Test the creation of the DiscoverableGroup model using XML content with empty group")
    public void testBuildDiscoverableGroupInstanceFromXMLWithEmptyGroup() {

        final String discoverableGroupsXML = "<DiscoverableGroup>\n" +
                "    <UserStore>test-domain</UserStore>\n" +
                "    <Group>test-group-id-1</Group>\n" +
                "    <Group></Group>\n" +
                "    <Group>test-group-id-3</Group>\n" +
                "</DiscoverableGroup>";

        OMElement rootElement =
                OMXMLBuilderFactory.createOMBuilder(new StringReader(discoverableGroupsXML)).getDocumentElement();
        DiscoverableGroup discoverableGroup = DiscoverableGroup.build(rootElement);
        assertEquals(discoverableGroup.getUserStore(), "test-domain",
                "The user store name does not match the user store name specified in the provided XML content");
        assertEquals(discoverableGroup.getGroups().length, 2,
                "The group list does not match the valid group list specified in the provided XML content");
        assertEquals(discoverableGroup.getGroups()[0], "test-group-id-1",
                "The group id does not match the group id specified in the provided XML content");
        assertEquals(discoverableGroup.getGroups()[1], "test-group-id-3",
                "The group id does not match the group id specified in the provided XML content");
    }

    @Test(description = "Test the creation of the DiscoverableGroup model using XML content with whitespace group")
    public void testBuildDiscoverableGroupInstanceFromXMLWithWhitespaceGroup() {

        final String discoverableGroupsXML = "<DiscoverableGroup>\n" +
                "    <UserStore>test-domain</UserStore>\n" +
                "    <Group>test-group-id-1</Group>\n" +
                "    <Group>   </Group>\n" +
                "    <Group>test-group-id-3</Group>\n" +
                "</DiscoverableGroup>";

        OMElement rootElement =
                OMXMLBuilderFactory.createOMBuilder(new StringReader(discoverableGroupsXML)).getDocumentElement();
        DiscoverableGroup discoverableGroup = DiscoverableGroup.build(rootElement);
        assertEquals(discoverableGroup.getUserStore(), "test-domain",
                "The user store name does not match the user store name specified in the provided XML content");
        assertEquals(discoverableGroup.getGroups().length, 2,
                "The group list does not match the valid group list specified in the provided XML content");
        assertEquals(discoverableGroup.getGroups()[0], "test-group-id-1",
                "The group id does not match the group id specified in the provided XML content");
        assertEquals(discoverableGroup.getGroups()[1], "test-group-id-3",
                "The group id does not match the group id specified in the provided XML content");
    }

    @Test(description = "Test the creation of the DiscoverableGroup model using XML content without user store")
    public void testBuildDiscoverableGroupInstanceFromXMLWithoutUserStore() {

        final String discoverableGroupsXML = "<DiscoverableGroup>\n" +
                "    <Group>test-group-id-1</Group>\n" +
                "    <Group>test-group-id-2</Group>\n" +
                "    <Group>test-group-id-3</Group>\n" +
                "</DiscoverableGroup>";

        OMElement rootElement =
                OMXMLBuilderFactory.createOMBuilder(new StringReader(discoverableGroupsXML)).getDocumentElement();
        DiscoverableGroup discoverableGroup = DiscoverableGroup.build(rootElement);
        assertNull(discoverableGroup);
    }

    @Test(description = "Test the creation of the DiscoverableGroup model using XML content without groups")
    public void testBuildDiscoverableGroupInstanceFromXMLWithoutGroups() {

        final String discoverableGroupsXML = "<DiscoverableGroup>\n" +
                "    <UserStore>test-domain</UserStore>\n" +
                "</DiscoverableGroup>";

        OMElement rootElement =
                OMXMLBuilderFactory.createOMBuilder(new StringReader(discoverableGroupsXML)).getDocumentElement();
        DiscoverableGroup discoverableGroup = DiscoverableGroup.build(rootElement);
        assertNull(discoverableGroup);
    }
}
