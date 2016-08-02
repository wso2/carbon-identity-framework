package org.wso2.carbon.identity.entitlement.endpoint.util;

import com.google.gson.*;
import org.w3c.dom.Attr;
import org.wso2.balana.Balana;
import org.wso2.balana.ObligationResult;
import org.wso2.balana.attr.AttributeFactory;
import org.wso2.balana.attr.AttributeValue;
import org.wso2.balana.ctx.AbstractResult;
import org.wso2.balana.ctx.AttributeAssignment;
import org.wso2.balana.ctx.ResponseCtx;
import org.wso2.balana.ctx.Status;
import org.wso2.balana.xacml3.Advice;
import org.wso2.balana.xacml3.Obligation;
import org.wso2.carbon.identity.entitlement.endpoint.exception.ResponseWriteException;

/**
 * Created by manujith on 8/1/16.
 * Converts ReponseCtx to JSON object
 * according to the XACML JSON Profile
 */
public class JSONResponseWriter {
    private static Gson gson = new Gson();
    public static JsonObject write(ResponseCtx response) throws ResponseWriteException{
        JsonObject responseWrap = new JsonObject();

        //JsonObject jsonResponse = new JsonObject();
        JsonArray results = new JsonArray();

        //Loop all AbstractResult objects in ResponseCtx and add them as
        //Requests to JSON Response
        //There should be at least 1 request
        if(response.getResults().size() < 1){
            throw new ResponseWriteException(40032,"XACML response should contain at least 1 Result");
        }

        for(AbstractResult result: response.getResults()){
            results.add(abstractResultToJSONObject(result));
        }


        responseWrap.add(EntitlementEndpointConstants.RESPONSE,results);

        return responseWrap;
    }

    private static JsonObject abstractResultToJSONObject(AbstractResult result) throws ResponseWriteException{
        JsonObject jsonResult = new JsonObject();

        //Decision property is mandatory, if not set throw error
        if(result.getDecision() == -1){
            throw new ResponseWriteException(40031,"XACML Result should contain the Decision");
        }
        jsonResult.addProperty(EntitlementEndpointConstants.DECISION,
                               AbstractResult.DECISIONS[result.getDecision()]);

        //If Status object is present, convert it
        if(result.getStatus() != null){
            jsonResult.add(EntitlementEndpointConstants.STATUS,statusToJSONObject(result.getStatus()));
        }

        //If Obligations are present
        if(result.getObligations() != null && !result.getObligations().isEmpty()){
            //can only get ObligationResult objects from balana
            JsonArray obligations = new JsonArray();
            for(ObligationResult obligation : result.getObligations()){
                if(obligation instanceof Obligation){
                    obligations.add(obligationToJsonObject((Obligation)obligation));
                }else {
                    obligations.add(new JsonPrimitive(obligation.encode()));
                }
            }

            jsonResult.add(EntitlementEndpointConstants.OBLIGATIONS,obligations);
        }

        //Do the same with attributes
        if(result.getAdvices() != null && !result.getAdvices().isEmpty()){
            //can only get ObligationResult objects from balana
            JsonArray advices = new JsonArray();
            for(Advice advice : result.getAdvices()){
                advices.add(adviceToJsonObject(advice));
            }

            jsonResult.add(EntitlementEndpointConstants.ASSOCIATED_ADVICE,advices);
        }

        /**
         * Todo: Category, PolicyIdentifierList
         */

        return jsonResult;
    }

    private static JsonObject statusToJSONObject(Status status){
        JsonObject jsonStatus = new JsonObject();

        jsonStatus.addProperty(EntitlementEndpointConstants.STATUS_MESSAGE, status.getMessage());

        if(status.getCode().size() > 0) {
            JsonObject statusCode = new JsonObject();
            statusCode.addProperty(EntitlementEndpointConstants.STATUS_CODE_VALUE, status.getCode().get(0));

            jsonStatus.add(EntitlementEndpointConstants.STATUS_CODE, statusCode);
        }

        if(status.getDetail() != null){
            jsonStatus.addProperty(EntitlementEndpointConstants.STATUS_DETAIL,status.getDetail().getEncoded());
        }

        return jsonStatus;
    }

    private static JsonObject obligationToJsonObject(Obligation obligation){
        JsonObject jsonObligation = new JsonObject();

        /**
         * Todo: Add obligation id
         */
        //jsonObligation.addProperty(EntitlementEndpointConstants.OBLIGATION_OR_ADVICE_ID,obligation);
        JsonArray attributeAssignments = new JsonArray();
        for(AttributeAssignment aa : obligation.getAssignments()){
            attributeAssignments.add(attributeAssignmentToJsonObject(aa));
        }
        jsonObligation.add(EntitlementEndpointConstants.ATTRIBUTE_ASSIGNMENTS, attributeAssignments);

        return jsonObligation;
    }

    private static JsonObject adviceToJsonObject(Advice advice){
        JsonObject jsonAdvice = new JsonObject();

        jsonAdvice.addProperty(EntitlementEndpointConstants.OBLIGATION_OR_ADVICE_ID,advice.getAdviceId().toString());
        JsonArray attributeAssignments = new JsonArray();
        for(AttributeAssignment aa : advice.getAssignments()){
            attributeAssignments.add(attributeAssignmentToJsonObject(aa));
        }
        jsonAdvice.add(EntitlementEndpointConstants.ATTRIBUTE_ASSIGNMENTS, attributeAssignments);

        return jsonAdvice;
    }

    private static JsonObject attributeAssignmentToJsonObject(AttributeAssignment attributeAssignment){
        JsonObject jsonAa = new JsonObject();
        jsonAa.addProperty(EntitlementEndpointConstants.ATTRIBUTE_ID,attributeAssignment.getAttributeId()
                                                                                        .toString());
        jsonAa.addProperty(EntitlementEndpointConstants.ATTRIBUTE_ISSUER,attributeAssignment.getIssuer());
        jsonAa.addProperty(EntitlementEndpointConstants.CATEGORY_DEFAULT, attributeAssignment.getCategory()
                                                                                              .toString());

        //try to get the attribute value and type by using json
        try {
            JsonObject attributeValue = gson.fromJson(gson.toJson(attributeAssignment), JsonObject.class);
            jsonAa.addProperty(EntitlementEndpointConstants.ATTRIBUTE_DATA_TYPE,
                               attributeValue.get("value").getAsString());
            jsonAa.addProperty(EntitlementEndpointConstants.ATTRIBUTE_DATA_TYPE,
                                attributeValue.get("identifier").getAsString());

        }catch(Exception e){
            return null;
        }


        return jsonAa;
    }
}
