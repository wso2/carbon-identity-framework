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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.wso2.balana.ObligationResult;
import org.wso2.balana.ctx.AbstractResult;
import org.wso2.balana.ctx.AttributeAssignment;
import org.wso2.balana.ctx.ResponseCtx;
import org.wso2.balana.ctx.Status;
import org.wso2.balana.xacml3.Advice;
import org.wso2.balana.xacml3.Obligation;
import org.wso2.carbon.identity.entitlement.endpoint.exception.ResponseWriteException;

/**
 * Converts ReponseCtx to JSON object
 * according to the XACML JSON Profile
 */
public class JSONResponseWriter {
    private static Gson gson = new Gson();

    /**
     * Returns <code>JsonObject</code> created by parsing the contents of a given
     * Balana <code>{@link ResponseCtx}</code>
     *
     * @param response <code>{@link ResponseCtx}</code>
     * @return <code>{@link JsonObject}</code> with parsed properties
     * @throws ResponseWriteException <code>{@link ResponseWriteException}</code>
     */
    public static JsonObject write(ResponseCtx response) throws ResponseWriteException {
        JsonObject responseWrap = new JsonObject();

        //JsonObject jsonResponse = new JsonObject();
        JsonArray results = new JsonArray();

        //Loop all AbstractResult objects in ResponseCtx and add them as
        //Requests to JSON Response
        //There should be at least 1 request
        if (response.getResults().size() < 1) {
            throw new ResponseWriteException(40032, "XACML response should contain at least 1 Result");
        }

        for (AbstractResult result : response.getResults()) {
            results.add(abstractResultToJSONObject(result));
        }
        responseWrap.add(EntitlementEndpointConstants.RESPONSE, results);

        return responseWrap;
    }

    /**
     * Private method to convert a given Balana <code>{@link AbstractResult}</code> to a <code>{@link JsonObject}</code>
     *
     * @param result <code>{@link AbstractResult}</code>
     * @return <code>{@link JsonObject}</code>
     * @throws ResponseWriteException <code>{@link ResponseWriteException}</code>
     */
    private static JsonObject abstractResultToJSONObject(AbstractResult result) throws ResponseWriteException {
        JsonObject jsonResult = new JsonObject();

        //Decision property is mandatory, if not set throw error
        if (result.getDecision() == -1) {
            throw new ResponseWriteException(40031, "XACML Result should contain the Decision");
        }
        jsonResult.addProperty(EntitlementEndpointConstants.DECISION,
                AbstractResult.DECISIONS[result.getDecision()]);

        //If Status object is present, convert it
        if (result.getStatus() != null) {
            jsonResult.add(EntitlementEndpointConstants.STATUS, statusToJSONObject(result.getStatus()));
        }

        //If Obligations are present
        if (result.getObligations() != null && !result.getObligations().isEmpty()) {
            //can only get ObligationResult objects from balana
            JsonArray obligations = new JsonArray();
            for (ObligationResult obligation : result.getObligations()) {
                if (obligation instanceof Obligation) {
                    obligations.add(obligationToJsonObject((Obligation) obligation));
                } else {
                    obligations.add(new JsonPrimitive(obligation.encode()));
                }
            }

            jsonResult.add(EntitlementEndpointConstants.OBLIGATIONS, obligations);
        }

        //Do the same with attributes
        if (result.getAdvices() != null && !result.getAdvices().isEmpty()) {
            //can only get ObligationResult objects from balana
            JsonArray advices = new JsonArray();
            for (Advice advice : result.getAdvices()) {
                advices.add(adviceToJsonObject(advice));
            }

            jsonResult.add(EntitlementEndpointConstants.ASSOCIATED_ADVICE, advices);
        }

        /**
         * Todo: Category, PolicyIdentifierList
         */

        return jsonResult;
    }

    /**
     * Private method to convert Balana <code>{@link Status}</code> to <code>{@link JsonObject}</code>
     *
     * @param status <code>{@link Status}</code>
     * @return <code>{@link JsonObject}</code>
     */
    private static JsonObject statusToJSONObject(Status status) {
        JsonObject jsonStatus = new JsonObject();

        jsonStatus.addProperty(EntitlementEndpointConstants.STATUS_MESSAGE, status.getMessage());

        if (status.getCode().size() > 0) {
            JsonObject statusCode = new JsonObject();
            statusCode.addProperty(EntitlementEndpointConstants.STATUS_CODE_VALUE, status.getCode().get(0));

            jsonStatus.add(EntitlementEndpointConstants.STATUS_CODE, statusCode);
        }
        if (status.getDetail() != null) {
            jsonStatus.addProperty(EntitlementEndpointConstants.STATUS_DETAIL, status.getDetail().getEncoded());
        }
        return jsonStatus;
    }

    /**
     * Private method to convert Balana <code>{@link Obligation}</code> to <code>{@link JsonObject}</code>
     *
     * @param obligation <code>{@link Obligation}</code>
     * @return <code>{@link JsonObject}</code>
     */
    private static JsonObject obligationToJsonObject(Obligation obligation) {
        JsonObject jsonObligation = new JsonObject();

        /**
         * Todo: Add obligation id
         */
        //jsonObligation.addProperty(EntitlementEndpointConstants.OBLIGATION_OR_ADVICE_ID,obligation);
        JsonArray attributeAssignments = new JsonArray();
        for (AttributeAssignment aa : obligation.getAssignments()) {
            attributeAssignments.add(attributeAssignmentToJsonObject(aa));
        }
        jsonObligation.add(EntitlementEndpointConstants.ATTRIBUTE_ASSIGNMENTS, attributeAssignments);

        return jsonObligation;
    }

    /**
     * Private method to convert Balana <code>{@link Advice}</code> to <code>{@link JsonObject}</code>
     *
     * @param advice <code>{@link Advice}</code>
     * @return <code>{@link JsonObject}</code>
     */
    private static JsonObject adviceToJsonObject(Advice advice) {
        JsonObject jsonAdvice = new JsonObject();

        jsonAdvice.addProperty(EntitlementEndpointConstants.OBLIGATION_OR_ADVICE_ID, advice.getAdviceId().toString());
        JsonArray attributeAssignments = new JsonArray();
        for (AttributeAssignment aa : advice.getAssignments()) {
            attributeAssignments.add(attributeAssignmentToJsonObject(aa));
        }
        jsonAdvice.add(EntitlementEndpointConstants.ATTRIBUTE_ASSIGNMENTS, attributeAssignments);

        return jsonAdvice;
    }

    /**
     * Private method to convert a given Balana <code>{@link AttributeAssignment}</code> to <code>{@link JsonObject}</code>
     *
     * @param attributeAssignment <code>{@link AttributeAssignment}</code>
     * @return <code>{@link JsonObject}</code>
     */
    private static JsonObject attributeAssignmentToJsonObject(AttributeAssignment attributeAssignment) {
        JsonObject jsonAa = new JsonObject();
        jsonAa.addProperty(EntitlementEndpointConstants.ATTRIBUTE_ID, attributeAssignment.getAttributeId()
                .toString());
        /*As per the xacml 3.0 core spec(section 5.41), Category and Issuer are optional categories for
        Element <AttributeAssignmentExpression>*/
        if(attributeAssignment.getIssuer() != null) {
            jsonAa.addProperty(EntitlementEndpointConstants.ATTRIBUTE_ISSUER, attributeAssignment.getIssuer()
                    .toString());
        }
        if(attributeAssignment.getCategory() != null) {
            jsonAa.addProperty(EntitlementEndpointConstants.CATEGORY_DEFAULT, attributeAssignment.getCategory()
                    .toString());
        }

        //try to get the attribute value and type by using json
        try {
            JsonObject attributeValue = gson.fromJson(gson.toJson(attributeAssignment), JsonObject.class);
            /*As per the xacml 3.0 core spec(section 7.3.1), data-type is a required attribute and content is optional
            for Element <AttributeValue>*/
            if(attributeValue.get("content") != null) {
                jsonAa.addProperty(EntitlementEndpointConstants.ATTRIBUTE_VALUE,
                        attributeValue.get("content").getAsString());
            }
            jsonAa.addProperty(EntitlementEndpointConstants.ATTRIBUTE_DATA_TYPE,
                    attributeValue.get("type").getAsString());
        } catch (Exception e) {
            return null;
        }


        return jsonAa;
    }
}
