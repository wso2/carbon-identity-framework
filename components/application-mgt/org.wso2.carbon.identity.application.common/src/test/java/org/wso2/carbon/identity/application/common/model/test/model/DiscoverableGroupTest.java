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
import org.wso2.carbon.identity.application.common.model.GroupBasicInfo;

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
                "    <Groups>\n" +
                "       <Group>\n" +
                "           <ID>test-group-id-1</ID>\n" +
                "           <Name>test-group-name-1</Name>\n" +
                "       </Group>\n" +
                "       <Group>\n" +
                "           <ID>test-group-id-2</ID>\n" +
                "           <Name>test-group-name-2</Name>\n" +
                "       </Group>\n" +
                "       <Group>\n" +
                "           <ID>test-group-id-3</ID>\n" +
                "           <Name>test-group-name-3</Name>\n" +
                "       </Group>\n" +
                "    </Groups>\n" +
                "</DiscoverableGroup>";

        OMElement rootElement =
                OMXMLBuilderFactory.createOMBuilder(new StringReader(discoverableGroupsXML)).getDocumentElement();
        DiscoverableGroup discoverableGroup = DiscoverableGroup.build(rootElement);
        assertEquals(discoverableGroup.getUserStore(), "test-domain",
                "The user store name does not match the user store name specified in the provided XML content");
        assertEquals(discoverableGroup.getGroups().length, 3,
                "The group list does not match the group list specified in the provided XML content");
        assertGroup(discoverableGroup.getGroups()[0], "test-group-id-1", "test-group-name-1");
        assertGroup(discoverableGroup.getGroups()[1], "test-group-id-2", "test-group-name-2");
        assertGroup(discoverableGroup.getGroups()[2], "test-group-id-3", "test-group-name-3");
    }

    /**
     * Assert the group basic info.
     *
     * @param groupBasicInfo GroupBasicInfo object for assertion.
     * @param id Expected group id.
     * @param name Expected group name.
     */
    private void assertGroup(GroupBasicInfo groupBasicInfo, String id, String name) {

        assertEquals(groupBasicInfo.getId(), id,
                "The group id does not match the group id specified in the provided XML content");
        assertEquals(groupBasicInfo.getName(), name,
                "The group name does not match the group name specified in the provided XML content");
    }

    @Test(description = "Test the creation of the DiscoverableGroup model using XML content with empty user store")
    public void testBuildDiscoverableGroupInstanceFromXMLWithEmptyUserStore() {

        final String discoverableGroupsXML = "<DiscoverableGroup>\n" +
                "    <UserStore></UserStore>\n" +
                "    <Groups>\n" +
                "       <Group>\n" +
                "           <ID>test-group-id-1</ID>\n" +
                "           <Name>test-group-name-1</Name>\n" +
                "       </Group>\n" +
                "       <Group>\n" +
                "           <ID>test-group-id-2</ID>\n" +
                "           <Name>test-group-name-2</Name>\n" +
                "       </Group>\n" +
                "       <Group>\n" +
                "           <ID>test-group-id-3</ID>\n" +
                "           <Name>test-group-name-3</Name>\n" +
                "       </Group>\n" +
                "    </Groups>\n" +
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
                "    <Groups>\n" +
                "       <Group>\n" +
                "           <ID>test-group-id-1</ID>\n" +
                "           <Name>test-group-name-1</Name>\n" +
                "       </Group>\n" +
                "       <Group>\n" +
                "           <ID>test-group-id-2</ID>\n" +
                "           <Name>test-group-name-2</Name>\n" +
                "       </Group>\n" +
                "       <Group>\n" +
                "           <ID>test-group-id-3</ID>\n" +
                "           <Name>test-group-name-3</Name>\n" +
                "       </Group>\n" +
                "    </Groups>\n" +
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
                "    <Groups>\n" +
                "       <Group></Group>\n" +
                "    </Groups>\n" +
                "</DiscoverableGroup>";

        OMElement rootElement =
                OMXMLBuilderFactory.createOMBuilder(new StringReader(discoverableGroupsXML)).getDocumentElement();
        DiscoverableGroup discoverableGroup = DiscoverableGroup.build(rootElement);
        assertNull(discoverableGroup);
    }

    @Test(description = "Test the creation of the DiscoverableGroup model using XML content without user store " +
            "and groups")
    public void testBuildDiscoverableGroupInstanceFromXMLWithoutUserStoreAndGroups() {

        final String discoverableGroupsXML = "<DiscoverableGroup></DiscoverableGroup>";

        OMElement rootElement =
                OMXMLBuilderFactory.createOMBuilder(new StringReader(discoverableGroupsXML)).getDocumentElement();
        DiscoverableGroup discoverableGroup = DiscoverableGroup.build(rootElement);
        assertNull(discoverableGroup);
    }
}
