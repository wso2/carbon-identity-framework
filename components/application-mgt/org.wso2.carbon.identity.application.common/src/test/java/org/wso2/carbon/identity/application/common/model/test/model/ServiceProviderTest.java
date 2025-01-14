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
                "            <Group>test-id-1</Group>\n" +
                "            <Group>test-id-2</Group>\n" +
                "        </DiscoverableGroup>\n" +
                "        <DiscoverableGroup>\n" +
                "            <UserStore>test-domain-2</UserStore>\n" +
                "            <Group>test-id-3</Group>\n" +
                "            <Group>test-id-4</Group>\n" +
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
        assertEquals(discoverableGroups[0].getUserStore(), "test-domain-1",
                "The user store name does not match the user store name specified in the provided XML content");
        assertEquals(discoverableGroups[0].getGroups(), new String[] {"test-id-1", "test-id-2"},
                "The group list does not match the group list specified in the provided XML content");
        assertEquals(discoverableGroups[1].getUserStore(), "test-domain-2",
                "The user store name does not match the user store name specified in the provided XML content");
        assertEquals(discoverableGroups[1].getGroups(), new String[] {"test-id-3", "test-id-4"},
                "The group list does not match the group list specified in the provided XML content");
    }

    @Test(description = "Test the construction of the empty DiscoverableGroups list from the service provider" +
            " as XML content")
    public void testEmptyDiscoverableGroupsList() {

        final String serviceProviderXML = "<ServiceProvider>\n" +
                "    <ApplicationName>TestApp</ApplicationName>\n" +
                "</ServiceProvider>";

        OMElement rootElement =
                OMXMLBuilderFactory.createOMBuilder(new StringReader(serviceProviderXML)).getDocumentElement();
        ServiceProvider serviceProvider = ServiceProvider.build(rootElement);
        assertNull(serviceProvider.getDiscoverableGroups());
    }
}
