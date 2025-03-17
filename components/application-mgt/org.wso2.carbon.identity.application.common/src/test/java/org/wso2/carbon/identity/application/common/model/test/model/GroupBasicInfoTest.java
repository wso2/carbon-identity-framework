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
import org.wso2.carbon.identity.application.common.model.GroupBasicInfo;

import java.io.StringReader;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * Unit tests for the GroupBasicInfo.
 */
@Test
public class GroupBasicInfoTest {

    @Test(description = "Test the creation of the GroupBasicInfo model using XML content")
    public void testBuildGroupBasicInfoInstanceFromXML() {

        final String groupBasicInfoXML = "<Group>\n" +
                "    <ID>test-group-id</ID>\n" +
                "    <Name>test-group-name</Name>\n" +
                "</Group>";

        OMElement rootElement =
                OMXMLBuilderFactory.createOMBuilder(new StringReader(groupBasicInfoXML)).getDocumentElement();
        GroupBasicInfo groupBasicInfo = GroupBasicInfo.build(rootElement);

        assertEquals(groupBasicInfo.getId(), "test-group-id",
                "The group id does not match the group id specified in the provided XML content");
        assertEquals(groupBasicInfo.getName(), "test-group-name",
                "The group name does not match the group name specified in the provided XML content");
    }

    @Test(description = "Test the creation of the GroupBasicInfo model using XML content without group name")
    public void testBuildGroupBasicInfoInstanceFromXMLWithoutGroupName() {

        final String groupBasicInfoXML = "<Group>\n" +
                "    <ID>test-group-id</ID>\n" +
                "</Group>";

        OMElement rootElement =
                OMXMLBuilderFactory.createOMBuilder(new StringReader(groupBasicInfoXML)).getDocumentElement();
        GroupBasicInfo groupBasicInfo = GroupBasicInfo.build(rootElement);
        assertEquals(groupBasicInfo.getId(), "test-group-id",
                "The group id does not match the group id specified in the provided XML content");
        assertNull(groupBasicInfo.getName());
    }

    @Test(description = "Test the creation of the GroupBasicInfo model using XML content without group id")
    public void testBuildGroupBasicInfoInstanceFromXMLWithoutGroupID() {

        final String groupBasicInfoXML = "<Group>\n" +
                "    <Name>test-group-name</Name>\n" +
                "</Group>";

        OMElement rootElement =
                OMXMLBuilderFactory.createOMBuilder(new StringReader(groupBasicInfoXML)).getDocumentElement();
        GroupBasicInfo groupBasicInfo = GroupBasicInfo.build(rootElement);
        assertNull(groupBasicInfo);
    }

    @Test(description = "Test the creation of the GroupBasicInfo model using XML content with empty group id")
    public void testBuildGroupBasicInfoInstanceFromXMLWithEmptyGroupID() {

        final String groupBasicInfoXML = "<Group>\n" +
                "    <ID></ID>\n" +
                "    <Name>test-group-name</Name>\n" +
                "</Group>";

        OMElement rootElement =
                OMXMLBuilderFactory.createOMBuilder(new StringReader(groupBasicInfoXML)).getDocumentElement();
        GroupBasicInfo groupBasicInfo = GroupBasicInfo.build(rootElement);
        assertNull(groupBasicInfo);
    }

    @Test(description = "Test the creation of the GroupBasicInfo model using XML content with whitespace group id")
    public void testBuildGroupBasicInfoInstanceFromXMLWithWhitespaceGroupID() {

        final String groupBasicInfoXML = "<Group>\n" +
                "    <ID>    </ID>\n" +
                "    <Name>test-group-name</Name>\n" +
                "</Group>";

        OMElement rootElement =
                OMXMLBuilderFactory.createOMBuilder(new StringReader(groupBasicInfoXML)).getDocumentElement();
        GroupBasicInfo groupBasicInfo = GroupBasicInfo.build(rootElement);
        assertNull(groupBasicInfo);
    }
}
