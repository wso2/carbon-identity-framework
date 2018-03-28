/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.testng.annotations.Test;
import org.wso2.balana.ObligationResult;
import org.wso2.balana.ctx.AbstractResult;
import org.wso2.balana.ctx.AttributeAssignment;
import org.wso2.balana.ctx.ResponseCtx;
import org.wso2.balana.ctx.Status;
import org.wso2.balana.ctx.xacml3.Result;
import org.wso2.balana.xacml3.Advice;
import org.wso2.balana.xacml3.Obligation;
import org.wso2.carbon.identity.entitlement.endpoint.exception.ResponseWriteException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Unit test class for JSON response writer
 */
public class TestJSONResponseWriter extends Assert {
    private static Log log = LogFactory.getLog(TestJSONResponseWriter.class);

    @Test
    public void testWriteWithObligations() throws URISyntaxException {

        List<AttributeAssignment> assignments = new ArrayList<>();
        String content = "Error: Channel request is not WEB.";
        URI type = new URI("http://www.w3.org/2001/XMLSchema#string");
        URI attributeId = new URI("urn:oasis:names:tc:xacml:3.0:example:attribute:text");
        AttributeAssignment attributeAssignment = new AttributeAssignment(attributeId, type, null, content, null);
        assignments.add(attributeAssignment);

        List<ObligationResult> obligationResults = new ArrayList<>();
        ObligationResult obligationResult = new Obligation(assignments, new URI("channel_ko"));
        obligationResults.add(obligationResult);

        List<String> codes = new ArrayList<>();
        codes.add("urn:oasis:names:tc:xacml:1.0:status:ok");
        AbstractResult abstractResult = new Result(1, new Status(codes), obligationResults, null, null);

        ResponseCtx responseCtx = new ResponseCtx(abstractResult);

        JSONResponseWriter jsonResponseWriter = new JSONResponseWriter();
        try {
            JsonObject jsonObject = jsonResponseWriter.write(responseCtx);
            assertNotNull("Failed to build the XACML json response", jsonObject.toString());
            assertFalse("Failed to build the XACML json response", jsonObject.entrySet().isEmpty());
            for(Map.Entry<String, JsonElement> jsonElementEntry: jsonObject.entrySet()) {
                if (jsonElementEntry.getKey().equals("Response")) {
                    JsonArray jsonArray = (JsonArray) jsonElementEntry.getValue();
                    assertEquals("Failed to build the XACML json response with correct evaluation",
                            jsonArray.get(0).getAsJsonObject().get("Decision").getAsString(), "Deny");
                }
            }
        } catch (ResponseWriteException e) {
            assertNull("Failed to build the XACML response", e);
        }

    }

    @Test
    public void testWriteWithAdvices() throws URISyntaxException {

        List<AttributeAssignment> assignments = new ArrayList<>();
        String content = "Error: Channel request is not WEB.";
        URI type = new URI("http://www.w3.org/2001/XMLSchema#string");
        URI attributeId = new URI("urn:oasis:names:tc:xacml:3.0:example:attribute:text");
        AttributeAssignment attributeAssignment = new AttributeAssignment(attributeId, type, null, content, null);
        assignments.add(attributeAssignment);

        List<Advice> adviceResults = new ArrayList<>();
        Advice adviceResult = new Advice(new URI("channel_ko"), assignments);
        adviceResults.add(adviceResult);

        List<String> codes = new ArrayList<>();
        codes.add("urn:oasis:names:tc:xacml:1.0:status:ok");
        AbstractResult abstractResult = new Result(1, new Status(codes), null, adviceResults, null);

        ResponseCtx responseCtx = new ResponseCtx(abstractResult);

        JSONResponseWriter jsonResponseWriter = new JSONResponseWriter();
        try {
            JsonObject jsonObject = jsonResponseWriter.write(responseCtx);
            assertNotNull("Failed to build the XACML json response", jsonObject.toString());
            assertFalse("Failed to build the XACML json response", jsonObject.entrySet().isEmpty());
            for(Map.Entry<String, JsonElement> jsonElementEntry: jsonObject.entrySet()) {
                if (jsonElementEntry.getKey().equals("Response")) {
                    JsonArray jsonArray = (JsonArray) jsonElementEntry.getValue();
                    assertEquals("Failed to build the XACML json response with correct evaluation",
                            jsonArray.get(0).getAsJsonObject().get("Decision").getAsString(), "Deny");
                }
            }
        } catch (ResponseWriteException e) {
            assertNull("Failed to build the XACML json response", e);
        }

    }
}
