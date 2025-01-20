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
import org.wso2.carbon.identity.application.common.model.ServiceProvider;

import java.io.StringReader;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * Unit tests for the ServiceProvider model.
 */
@Test
public class ServiceProviderTest {

    @Test(description = "Test the construction of the DiscoverableGroups list from the service provider as XML" +
            " content")
    public void testDiscoverableGroupsList() {

        final String serviceProviderXML = "<ServiceProvider>\n" +
                "    <ApplicationName>TestApp</ApplicationName>\n" +
                "    <DiscoverableGroups>\n" +
                "        <DiscoverableGroup>\n" +
                "            <UserStore>test-domain-1</UserStore>\n" +
                "            <Groups>\n" +
                "               <Group>\n" +
                "                   <ID>test-id-1</ID>\n" +
                "                   <Name>test-name-1</Name>\n" +
                "               </Group>\n" +
                "               <Group>\n" +
                "                   <ID>test-id-2</ID>\n" +
                "                   <Name>test-name-2</Name>\n" +
                "               </Group>\n" +
                "            </Groups>\n" +
                "        </DiscoverableGroup>\n" +
                "        <DiscoverableGroup>\n" +
                "            <UserStore>test-domain-2</UserStore>\n" +
                "            <Groups>\n" +
                "               <Group>\n" +
                "                   <ID>test-id-3</ID>\n" +
                "                   <Name>test-name-3</Name>\n" +
                "               </Group>\n" +
                "               <Group>\n" +
                "                   <ID>test-id-4</ID>\n" +
                "                   <Name>test-name-4</Name>\n" +
                "               </Group>\n" +
                "            </Groups>\n" +
                "        </DiscoverableGroup>\n" +
                "    </DiscoverableGroups>\n" +
                "</ServiceProvider>";

        OMElement rootElement =
                OMXMLBuilderFactory.createOMBuilder(new StringReader(serviceProviderXML))
                        .getDocumentElement();
        ServiceProvider serviceProvider = ServiceProvider.build(rootElement);
        DiscoverableGroup[] discoverableGroups = serviceProvider.getDiscoverableGroups();
        assertEquals(discoverableGroups.length, 2,
                "The discoverable group list does not match the discoverable group list specified in the provided" +
                        " XML content");
        assertDiscoverableGroup(discoverableGroups[0], "test-domain-1", new String[]{"test-id-1", "test-id-2"},
                new String[]{"test-name-1", "test-name-2"});
        assertDiscoverableGroup(discoverableGroups[1], "test-domain-2", new String[]{"test-id-3", "test-id-4"},
                new String[]{"test-name-3", "test-name-4"});
    }

    /**
     * Assert the discoverable group.
     *
     * @param discoverableGroup Discoverable group instance for assertion.
     * @param userStore         Expected user store name.
     * @param groupIDs          Expected group IDs list.
     * @param groupName         Expected group names list.
     */
    private void assertDiscoverableGroup(DiscoverableGroup discoverableGroup, String userStore, String[] groupIDs,
                                         String[] groupName) {

        assertEquals(discoverableGroup.getUserStore(), userStore,
                "The user store name does not match the user store name specified in the provided XML content");
        GroupBasicInfo[] groups = discoverableGroup.getGroups();
        for (int i = 0; i < groups.length; i++) {
            GroupBasicInfo groupBasicInfo = groups[i];
            assertEquals(groupBasicInfo.getId(), groupIDs[i],
                    "The group ID does not match the group ID specified in the provided XML content");
            assertEquals(groupBasicInfo.getName(), groupName[i],
                    "The group name does not match the group name specified in the provided XML content");
        }
    }

    @Test(description = "Test the construction of the empty DiscoverableGroups list from the service provider" +
            " as XML content")
    public void testEmptyDiscoverableGroupsList() {

        final String serviceProviderXML = "<ServiceProvider>\n" +
                "    <ApplicationName>TestApp</ApplicationName>\n" +
                "    <DiscoverableGroups>\n" +
                "       <DiscoverableGroup></DiscoverableGroup>\n" +
                "    </DiscoverableGroups>\n" +
                "</ServiceProvider>";

        OMElement rootElement =
                OMXMLBuilderFactory.createOMBuilder(new StringReader(serviceProviderXML)).getDocumentElement();
        ServiceProvider serviceProvider = ServiceProvider.build(rootElement);
        assertNull(serviceProvider.getDiscoverableGroups());
    }

    @Test(description = "Test the construction of the service provider without DiscoverableGroups from XML content")
    public void testWithoutDiscoverableGroupsList() {

        final String serviceProviderXML = "<ServiceProvider>\n" +
                "    <ApplicationName>TestApp</ApplicationName>\n" +
                "</ServiceProvider>";

        OMElement rootElement =
                OMXMLBuilderFactory.createOMBuilder(new StringReader(serviceProviderXML)).getDocumentElement();
        ServiceProvider serviceProvider = ServiceProvider.build(rootElement);
        assertNull(serviceProvider.getDiscoverableGroups());
    }
}
