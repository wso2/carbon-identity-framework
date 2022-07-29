/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.entitlement.endpoint.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.Test;
import org.wso2.balana.XACMLConstants;
import org.wso2.balana.attr.AttributeValue;
import org.wso2.balana.attr.StringAttribute;
import org.wso2.balana.ctx.Attribute;
import org.wso2.balana.ctx.xacml3.RequestCtx;
import org.wso2.balana.xacml3.Attributes;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Unit test class for JSON parser / writer
 */


public class TestJSONRequestParser {
    private static Log log = LogFactory.getLog(TestJSONRequestParser.class);

    @Test
    public void testParse() {
        AttributeValue attributeValue = new StringAttribute("http://127.0.0.1");
        List<AttributeValue> attributeValues = new ArrayList<>();
        attributeValues.add(attributeValue);

        Attribute attribute = new Attribute(URI.create("urn:oasis:names:tc:xacml:1.0:resource:resource-id"),
                null, null, null, attributeValues, false, XACMLConstants.XACML_VERSION_3_0);
        Set<Attribute> attributeSet = new HashSet<>();
        attributeSet.add(attribute);

        Attributes category = new Attributes(URI.create(EntitlementEndpointConstants.CATEGORY_RESOURCE_URI),
                attributeSet);
        Set<Attributes> categories = new HashSet<>();
        categories.add(category);

        RequestCtx requestCtx = new RequestCtx(categories, null);


        String jsonRequest = "{\n" +
                "  \"Request\":{\n" +
                "    \"Action\":{\n" +
                "      \"Attribute\":[{\n" +
                "        \"AttributeId\":\"urn:oasis:names:tc:xacml:1.0:action:action-id\",\n" +
                "        \"Value\":\"read\"\n" +
                "      }]\n" +
                "    },\n" +
                "    \"Resource\":{\n" +
                "      \"Attribute\":[{\n" +
                "        \"AttributeId\":\"urn:oasis:names:tc:xacml:1.0:resource:resource-id\",\n" +
                "        \"Value\":\"http://127.0.0.1/service/very_secure/\"\n" +
                "      }]\n" +
                "    }\n" +
                "  }\n" +
                "}";

        String jsonRequest2 = "{\"Request\":\n" +
                "{\n" +
                "\"AccessSubject\":{\n" +
                "            \"Content\": \"PD94bWwgdmVyc2lvbj0iMS4wIj8+DQo8Y2F0YWxvZz48Ym9vayBpZD0iYmsxMDEiPjxhdXRob3I+R2FtYmFyZGVsbGEsIE1hdHRoZXc8L2F1dGhvcj48dGl0bGU+WE1MIERldmVsb3BlcidzIEd1aWRlPC90aXRsZT48Z2VucmU+Q29tcHV0ZXI8L2dlbnJlPjxwcmljZT40NC45NTwvcHJpY2U+PHB1Ymxpc2hfZGF0ZT4yMDAwLTEwLTAxPC9wdWJsaXNoX2RhdGU+PGRlc2NyaXB0aW9uPkFuIGluLWRlcHRoIGxvb2sgYXQgY3JlYXRpbmcgYXBwbGljYXRpb25zIHdpdGggWE1MLjwvZGVzY3JpcHRpb24+PC9ib29rPjwvY2F0YWxvZz4=\"\n" +
                "}\n" +
                "}}";

        try {
            RequestCtx requestCtx1 = JSONRequestParser.parse(jsonRequest);
        } catch (Exception e) {
            log.error("Exception in JSON Parser Test");
        }


    }
}
