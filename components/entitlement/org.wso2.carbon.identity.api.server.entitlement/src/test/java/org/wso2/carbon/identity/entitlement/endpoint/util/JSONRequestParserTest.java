/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import com.google.gson.JsonParseException;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.testutil.IdentityBaseTest;

public class JSONRequestParserTest extends IdentityBaseTest {

    @DataProvider(name = "BuildRequest")
    public Object[][] buildRequest() {

        return new Object[][] {
                {"{\n" +
                        "   \"Request\": {\n" +
                        "      \"AccessSubject\": {\n" +
                        "         \"Attribute\": [\n" +
                        "            {\n" +
                        "               \"AttributeId\": \"subject-id\",\n" +
                        "               \"Value\": \"sampleName\",\n" +
                        "               \"DataType\": \"string\",\n" +
                        "               \"IncludeInResult\": true\n" +
                        "            }\n" +
                        "         ]\n" +
                        "      },\n" +
                        "     \n" +
                        "      \"Resource\": {\n" +
                        "         \"Attribute\": [\n" +
                        "            {\n" +
                        "               \"AttributeId\": \"resource-id\",\n" +
                        "               \"Value\": \"index.jsp\",\n" +
                        "               \"DataType\": \"string\",\n" +
                        "               \"IncludeInResult\": true\n" +
                        "            }\n" +
                        "         ]\n" +
                        "      },\n" +
                        "      \"Action\": {\n" +
                        "            \"Attribute\": [{\n" +
                        "                    \"AttributeId\": \"action-id\",\n" +
                        "                    \"Value\": \"modify-welcome\",\n" +
                        "                    \"DataType\": \"string\",\n" +
                        "                    \"IncludeInResult\": true\n" +
                        "                }\n" +
                        "            ]\n" +
                        "        }\n" +
                        "        \n" +
                        "   }\n" +
                        "}"},
                {"{\"Request\":\n" +
                "{\n" +
                "\"AccessSubject\":{\n" +
                "            \"Content\": \"PD94bWwgdmVyc2lvbj0iMS4wIj8+DQo8Y2F0YWxvZz48Ym9vayBpZD0iYmsxMDEiPjxhdXRob3I+R2FtYmFyZGVsbGEsIE1hdHRoZXc8L2F1dGhvcj48dGl0bGU+WE1MIERldmVsb3BlcidzIEd1aWRlPC90aXRsZT48Z2VucmU+Q29tcHV0ZXI8L2dlbnJlPjxwcmljZT40NC45NTwvcHJpY2U+PHB1Ymxpc2hfZGF0ZT4yMDAwLTEwLTAxPC9wdWJsaXNoX2RhdGU+PGRlc2NyaXB0aW9uPkFuIGluLWRlcHRoIGxvb2sgYXQgY3JlYXRpbmcgYXBwbGljYXRpb25zIHdpdGggWE1MLjwvZGVzY3JpcHRpb24+PC9ib29rPjwvY2F0YWxvZz4=\"\n" +
                "}\n" +
                "}}"},
                {"{\n" +
                        "   \"Request\": {\n" +
                        "      \"AccessSubject\": {\n" +
                        "         \"Attribute\": [\n" +
                        "            {\n" +
                        "               \"AttributeId\": \"subject-id\",\n" +
                        "               \"Value\": [\"sampleName\"],\n" +
                        "               \"DataType\": \"string\",\n" +
                        "               \"IncludeInResult\": true\n" +
                        "            }\n" +
                        "         ]\n" +
                        "      },\n" +
                        "     \n" +
                        "      \"Resource\": {\n" +
                        "         \"Attribute\": [\n" +
                        "            {\n" +
                        "               \"AttributeId\": \"resource-id\",\n" +
                        "               \"Value\": [\"index.jsp\"],\n" +
                        "               \"DataType\": \"string\",\n" +
                        "               \"IncludeInResult\": true\n" +
                        "            }\n" +
                        "         ]\n" +
                        "      },\n" +
                        "      \"Action\": {\n" +
                        "            \"Attribute\": [{\n" +
                        "                    \"AttributeId\": \"action-id\",\n" +
                        "                    \"Value\": [\"modify-welcome\",\"view-welcome\"],\n" +
                        "                    \"DataType\": \"string\",\n" +
                        "                    \"IncludeInResult\": true\n" +
                        "                }\n" +
                        "            ]\n" +
                        "        }\n" +
                        "        \n" +
                        "   }\n" +
                        "}"}

        };
    }

    @DataProvider(name = "BuildNullRequest")
    public Object[][] buildNullRequest() {

        return new Object[][] {
                {null},
                {" "}

        };
    }

    @Test(dataProvider = "BuildRequest")
    public void testParseNonEmptyRequests(String request) throws Exception {

        Assert.assertNotNull(JSONRequestParser.parse(request), "The request passed context is null." +
                " The passed request is :" + request + " . The converted request object is :"
                + JSONRequestParser.parse(request).toString());
    }

    @Test(dataProvider = "BuildNullRequest", expectedExceptions = JsonParseException.class)
    public void testParseEmptyRequests(String request) throws Exception {

        Assert.assertNull(JSONRequestParser.parse(request), "The request passed context is not null null." +
                " The passed request is :" + request );
    }

}
