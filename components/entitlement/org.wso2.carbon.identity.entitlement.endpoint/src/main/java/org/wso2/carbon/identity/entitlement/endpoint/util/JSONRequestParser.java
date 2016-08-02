package org.wso2.carbon.identity.entitlement.endpoint.util;

import com.google.gson.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.wso2.balana.Balana;
import org.wso2.balana.UnknownIdentifierException;
import org.wso2.balana.XACMLConstants;
import org.wso2.balana.attr.AttributeValue;
import org.wso2.balana.ctx.Attribute;
import org.wso2.balana.ctx.xacml3.RequestCtx;
import org.wso2.balana.xacml3.*;
import org.wso2.carbon.identity.entitlement.endpoint.exception.RequestParseException;

import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.*;

/**
 * Created by manujith on 7/20/16.
 * This class will deal with parsing a given JSON String to a
 * RequestCtx object, so that it can be evaluated by the engine.
 */
public class JSONRequestParser {
    private static Gson gson = new Gson();

    public static RequestCtx parse(String jsonRequest) throws JsonParseException, RequestParseException,
                                                              UnknownIdentifierException{
        JsonObject requestObject = null;
        Set<Attributes> categories = new HashSet<>();
        boolean returnPolicyIdList = false;
        boolean combinedDecision = false;
        MultiRequests multiRequests = null;
        RequestDefaults requestDefaults = null;

        try {
            requestObject = gson.fromJson(jsonRequest, JsonObject.class);
            requestObject = requestObject.get("Request").getAsJsonObject();
        }catch(Exception e){
            throw new JsonParseException("Error in JSON Request String");
        }


        Set<Map.Entry<String,JsonElement>> jsonAttributes = requestObject.entrySet();

        for(Map.Entry<String, JsonElement> jsonAttribute : jsonAttributes){


            if(jsonAttribute.getValue().isJsonPrimitive()){
                switch(jsonAttribute.getKey()) {
                    case XACMLConstants.RETURN_POLICY_LIST:
                        if (jsonAttribute.getValue().getAsBoolean() == true) {
                            returnPolicyIdList = true;
                        }
                        break;

                    case XACMLConstants.COMBINE_DECISION:
                        if (jsonAttribute.getValue().getAsBoolean() == true) {
                            combinedDecision = true;
                        }
                        break;

                    case EntitlementEndpointConstants.XPATH_VERSION:
                        String xPathVersion = jsonAttribute.getValue().getAsString();
                        requestDefaults = new RequestDefaults(xPathVersion);
                        break;
                }
            }else if(jsonAttribute.getValue().isJsonObject()){
                URI category = null;
                Node content = null;
                Set<Attribute> attributes = null;
                String id = null;

                JsonObject jsonCategory = jsonAttribute.getValue().getAsJsonObject();

                switch(jsonAttribute.getKey()){

                    //For a custom Category
                    //Or a Category with long identifier
                    case EntitlementEndpointConstants.CATEGORY_DEFAULT:

                        if(jsonCategory.has(EntitlementEndpointConstants.CATEGORY_ID)){
                            category = stringCateogryToURI(jsonCategory
                                                            .get(EntitlementEndpointConstants.CATEGORY_ID)
                                                            .getAsString());
                        }

                    case EntitlementEndpointConstants.CATEGORY_RESOURCE:
                    case EntitlementEndpointConstants.CATEGORY_ACTION:
                    case EntitlementEndpointConstants.CATEGORY_ENVIRONMENT:
                    case EntitlementEndpointConstants.CATEGORY_ACCESS_SUBJECT:
                    case EntitlementEndpointConstants.CATEGORY_RECIPIENT_SUBJECT:
                    case EntitlementEndpointConstants.CATEGORY_INTERMEDIARY_SUBJECT:
                    case EntitlementEndpointConstants.CATEGORY_CODEBASE:
                    case EntitlementEndpointConstants.CATEGORY_REQUESTING_MACHINE:

                        if(category == null){
                            category = stringCateogryToURI(jsonAttribute.getKey());
                        }

                        if(jsonCategory.has(EntitlementEndpointConstants.ID)){
                            id = jsonCategory.get(EntitlementEndpointConstants.ID).getAsString();
                        }

                        if(jsonCategory.has(EntitlementEndpointConstants.CONTENT)){

                            ByteArrayInputStream inputStream;
                            DocumentBuilderFactory dbf;
                            Document doc = null;

                            String xmlContent = stringContentToXMLContent(jsonCategory
                                                                    .get(EntitlementEndpointConstants.CONTENT)
                                                                    .getAsString());
                            inputStream = new ByteArrayInputStream(xmlContent.getBytes());
                            dbf = DocumentBuilderFactory.newInstance();
                            dbf.setNamespaceAware(true);

                            try {
                                doc = dbf.newDocumentBuilder().parse(inputStream);
                            } catch (Exception e) {
                                System.err.println("DOM of request element can not be created from String");

                            } finally {
                                try {
                                    inputStream.close();
                                } catch (IOException e) {
                                    System.err.println("Error in closing input stream of XACML response");
                                }
                            }

                            if(doc != null){
                                content = doc;
                            }

                        }

                        //add all category attributes
                        if(jsonCategory.has(EntitlementEndpointConstants.ATTRIBUTE)){
                            if(jsonCategory.get(EntitlementEndpointConstants.ATTRIBUTE).isJsonArray()){
                                attributes = new HashSet<>();
                                for(JsonElement jsonElement : jsonCategory.get(EntitlementEndpointConstants.ATTRIBUTE)
                                                                         .getAsJsonArray()){
                                    attributes.add(jsonObjectToAttribute(jsonElement.getAsJsonObject()));

                                }
                            }
                        }

                        break;

                    case EntitlementEndpointConstants.MULTI_REQUESTS:
                        Set<Map.Entry<String,JsonElement>> jsonRequestReferences = jsonCategory.entrySet();
                        Set<RequestReference> requestReferences = new HashSet<>();

                        if(jsonRequestReferences.isEmpty()){
                            throw new RequestParseException("MultiRequest should contain at least one "+
                                                                    "Reference Request");
                        }

                        for(Map.Entry<String,JsonElement> jsonRequstReference : jsonRequestReferences){
                            requestReferences.add(jsonObjectToRequestReference(jsonRequstReference.getValue()
                                                                                            .getAsJsonObject()));
                        }

                        //multiRequests = new MultiRequests(requestReferences);
                        /*
                        Todo: Ask for public constructor
                         */

                        break;

                }

                //Build the Attributes object using above values
                Attributes attributesObj = new Attributes(category,content,attributes,id);
                categories.add(attributesObj);
            }



        }


        RequestCtx requestCtx = new RequestCtx(null,categories,returnPolicyIdList,combinedDecision,
                                                    multiRequests, requestDefaults);
        return requestCtx;
    }

    public static Attribute jsonObjectToAttribute(JsonObject jsonObject) throws RequestParseException,
                                                                                UnknownIdentifierException{
        URI id = null;
        URI type = stringAttributeToURI(EntitlementEndpointConstants.ATTRIBUTE_DATA_TYPE_STRING);
        boolean includeInResult = false;
        String issuer = null;
        List<AttributeValue> attributeValues = new ArrayList<>();

        Set<Map.Entry<String,JsonElement>> properties = jsonObject.entrySet();
        for(Map.Entry<String,JsonElement> property : properties){
            if(property.getValue().isJsonPrimitive()){
                switch(property.getKey()){
                    case EntitlementEndpointConstants.ATTRIBUTE_ID:
                        id = stringAttributeToURI(property.getValue().getAsString());
                        break;

                    case EntitlementEndpointConstants.ATTRIBUTE_ISSUER:
                        issuer = property.getValue().getAsString();
                        break;

                    case EntitlementEndpointConstants.ATTRIBUTE_INCLUDE_IN_RESULT:
                        includeInResult = property.getValue().getAsBoolean();
                        break;

                    case EntitlementEndpointConstants.ATTRIBUTE_DATA_TYPE:
                        type = stringAttributeToURI(property.getValue().getAsString());
                        break;

                    case EntitlementEndpointConstants.ATTRIBUTE_VALUE:
                        URI dataType = stringAttributeToURI(
                                          jsonElementToDataType(property.getValue().getAsJsonPrimitive()));

                        //If a recognizable data type is given, it should replace the above
                        if(type.equals(EntitlementEndpointConstants.ATTRIBUTE_DATA_TYPE_STRING)
                                    && dataType != null){
                            type = dataType;
                        }

                        attributeValues.add(getAttributeValue(property.getValue().getAsString(),dataType,type));
                }
            }else if(property.getValue().isJsonArray()){
                if(property.getKey().equals(EntitlementEndpointConstants.ATTRIBUTE_VALUE)){
                    JsonArray valueArray = property.getValue().getAsJsonArray();
                    for(JsonElement value : valueArray){
                        if(value.isJsonPrimitive()){
                            //check if each value's data type can be determined
                            URI dataType = stringAttributeToURI(
                                             jsonElementToDataType(property.getValue().getAsJsonPrimitive()));
                            attributeValues.add(getAttributeValue(property.getValue().getAsString(),dataType,type));
                        }
                    }
                }

                /*
                Todo: Spec mentions resolve the type by checking all elements at the end
                 */
            }
        }

        if(id == null){
            throw new RequestParseException("Attribute Id should be set");
        }

        if(attributeValues.isEmpty()){
            throw new RequestParseException("Attribute should have at least one value");
        }

        return new Attribute(id, type,issuer,null,attributeValues,includeInResult,
                                                            XACMLConstants.XACML_VERSION_3_0);
    }

    public static AttributeValue getAttributeValue(String value,URI dataType,URI parentDataType)
                                throws UnknownIdentifierException{
        URI type = dataType;
        AttributeValue attributeValue = null;

        //check if dataType attribute is set, if not use the parent data type
        if(dataType == null){
            type = parentDataType;
        }

        try{
            attributeValue  = Balana.getInstance().getAttributeFactory().createValue(type,value);
        }catch(Exception e){
            throw new UnknownIdentifierException();
        }
        return attributeValue;
    }

    public static RequestReference jsonObjectToRequestReference(JsonObject jsonRequestReference){
        RequestReference requestReference = new RequestReference();
        Set<AttributesReference> attributesReferences = new HashSet<>();

        if(jsonRequestReference.has(EntitlementEndpointConstants.REFERENCE_ID)){
            JsonArray referenceIds = jsonRequestReference.get(EntitlementEndpointConstants.REFERENCE_ID).getAsJsonArray();
            for(JsonElement reference : referenceIds){
                AttributesReference attributesReference = new AttributesReference();
                attributesReference.setId(reference.getAsString());
                attributesReferences.add(attributesReference);
            }
            requestReference.setReferences(attributesReferences);
        }
        return requestReference;
    }

    public static URI stringCateogryToURI(String category){
        URI uri = null;
        String uriName = category;
        switch(category){
            case EntitlementEndpointConstants.CATEGORY_RESOURCE:
                uriName = EntitlementEndpointConstants.CATEGORY_RESOURCE_URI;
                break;
            case EntitlementEndpointConstants.CATEGORY_ACTION:
                uriName = EntitlementEndpointConstants.CATEGORY_ACTION_URI;
                break;
            case EntitlementEndpointConstants.CATEGORY_ENVIRONMENT:
                uriName = EntitlementEndpointConstants.CATEGORY_ENVIRONMENT_URI;
                break;
            case EntitlementEndpointConstants.CATEGORY_ACCESS_SUBJECT:
                uriName = EntitlementEndpointConstants.CATEGORY_ACCESS_SUBJECT_URI;
                break;
            case EntitlementEndpointConstants.CATEGORY_RECIPIENT_SUBJECT:
                uriName = EntitlementEndpointConstants.CATEGORY_RECIPIENT_SUBJECT_URI;
                break;
            case EntitlementEndpointConstants.CATEGORY_INTERMEDIARY_SUBJECT:
                uriName = EntitlementEndpointConstants.CATEGORY_INTERMEDIARY_SUBJECT_URI;
                break;
            case EntitlementEndpointConstants.CATEGORY_CODEBASE:
                uriName = EntitlementEndpointConstants.CATEGORY_CODEBASE_URI;
                break;
            case EntitlementEndpointConstants.CATEGORY_REQUESTING_MACHINE:
                uriName = EntitlementEndpointConstants.CATEGORY_REQUESTING_MACHINE_URI;
                break;
        }

        uri = URI.create(uriName);
        return uri;
    }

    public static String jsonElementToDataType(JsonPrimitive element){
        if(element.isString()){
            return EntitlementEndpointConstants.ATTRIBUTE_DATA_TYPE_STRING;
        }else if(element.isBoolean()){
            return EntitlementEndpointConstants.ATTRIBUTE_DATA_TYPE_BOOLEAN;
        }else if(element.isNumber()){
            double n1 = element.getAsDouble();
            int n2 = element.getAsInt();
            if(Math.ceil(n1) == n2){
                return EntitlementEndpointConstants.ATTRIBUTE_DATA_TYPE_INTEGER;
            }else{
                return EntitlementEndpointConstants.ATTRIBUTE_DATA_TYPE_DOUBLE;
            }
        }

        return null;
    }


    public static URI stringAttributeToURI(String attribute){
        String uriName = attribute;
        switch(attribute){
            case EntitlementEndpointConstants.ATTRIBUTE_DATA_TYPE_STRING_SHORT:
                uriName = EntitlementEndpointConstants.ATTRIBUTE_DATA_TYPE_STRING;
                break;

            case EntitlementEndpointConstants.ATTRIBUTE_DATA_TYPE_BOOLEAN_SHORT:
                uriName = EntitlementEndpointConstants.ATTRIBUTE_DATA_TYPE_BOOLEAN;
                break;

            case EntitlementEndpointConstants.ATTRIBUTE_DATA_TYPE_INTEGER_SHORT:
                uriName = EntitlementEndpointConstants.ATTRIBUTE_DATA_TYPE_INTEGER;
                break;

            case EntitlementEndpointConstants.ATTRIBUTE_DATA_TYPE_DOUBLE_SHORT:
                uriName = EntitlementEndpointConstants.ATTRIBUTE_DATA_TYPE_DOUBLE;
                break;

            case EntitlementEndpointConstants.ATTRIBUTE_DATA_TYPE_TIME_SHORT:
                uriName = EntitlementEndpointConstants.ATTRIBUTE_DATA_TYPE_TIME;
                break;

            case EntitlementEndpointConstants.ATTRIBUTE_DATA_TYPE_DATE_SHORT:
                uriName = EntitlementEndpointConstants.ATTRIBUTE_DATA_TYPE_DATE;
                break;

            case EntitlementEndpointConstants.ATTRIBUTE_DATA_TYPE_DATE_TIME_SHORT:
                uriName = EntitlementEndpointConstants.ATTRIBUTE_DATA_TYPE_DATE_TIME;
                break;

            case EntitlementEndpointConstants.ATTRIBUTE_DATA_TYPE_DATE_TIME_DURATION_SHORT:
                uriName = EntitlementEndpointConstants.ATTRIBUTE_DATA_TYPE_DATE_TIME_DURATION;
                break;

            case EntitlementEndpointConstants.ATTRIBUTE_DATA_TYPE_YEAR_MONTH_DURATION_SHORT:
                uriName = EntitlementEndpointConstants.ATTRIBUTE_DATA_TYPE_YEAR_MONTH_DURATION;
                break;

            case EntitlementEndpointConstants.ATTRIBUTE_DATA_TYPE_ANY_URI_SHORT:
                uriName = EntitlementEndpointConstants.ATTRIBUTE_DATA_TYPE_ANY_URI;
                break;

            case EntitlementEndpointConstants.ATTRIBUTE_DATA_TYPE_HEX_BINARY_SHORT:
                uriName = EntitlementEndpointConstants.ATTRIBUTE_DATA_TYPE_HEX_BINARY;
                break;

            case EntitlementEndpointConstants.ATTRIBUTE_DATA_TYPE_BASE64_BINARY_SHORT:
                uriName = EntitlementEndpointConstants.ATTRIBUTE_DATA_TYPE_BASE64_BINARY;
                break;

            case EntitlementEndpointConstants.ATTRIBUTE_DATA_TYPE_RFC_822_NAME_SHORT:
                uriName = EntitlementEndpointConstants.ATTRIBUTE_DATA_TYPE_RFC_822_NAME;
                break;

            case EntitlementEndpointConstants.ATTRIBUTE_DATA_TYPE_X_500_NAME_SHORT:
                uriName = EntitlementEndpointConstants.ATTRIBUTE_DATA_TYPE_X_500_NAME;
                break;

            case EntitlementEndpointConstants.ATTRIBUTE_DATA_TYPE_IP_ADDRESS_SHORT:
                uriName = EntitlementEndpointConstants.ATTRIBUTE_DATA_TYPE_IP_ADDRESS;
                break;

            case EntitlementEndpointConstants.ATTRIBUTE_DATA_TYPE_DNS_NAME_SHORT:
                uriName = EntitlementEndpointConstants.ATTRIBUTE_DATA_TYPE_DNS_NAME;
                break;

            case EntitlementEndpointConstants.ATTRIBUTE_DATA_TYPE_XPATH_EXPRESSION_SHORT:
                uriName = EntitlementEndpointConstants.ATTRIBUTE_DATA_TYPE_XPATH_EXPRESSION;
                break;
        }
        return URI.create(uriName);
    }

    public static String stringContentToXMLContent(String content) throws RequestParseException {
        if(content.startsWith("<")){
            //todo : check if GSON automatically unescape the string
            return content;
        }else{
            //do base64 decoding
            return new String(DatatypeConverter.parseBase64Binary(content));
        }
    }
}
