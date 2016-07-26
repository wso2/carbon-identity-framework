package org.wso2.carbon.identity.entitlement.endpoint.test;

import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.balana.Balana;
import org.wso2.balana.XACMLConstants;
import org.wso2.balana.attr.AttributeValue;
import org.wso2.balana.attr.StringAttribute;
import org.wso2.balana.ctx.Attribute;
import org.wso2.balana.ctx.xacml3.RequestCtx;
import org.wso2.balana.xacml3.Attributes;
import org.wso2.carbon.identity.entitlement.endpoint.util.EntitlementEndpointConstants;
import org.wso2.carbon.identity.entitlement.endpoint.util.JSONRequestParser;
import org.wso2.carbon.identity.entitlement.pdp.EntitlementEngine;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by manujith on 7/21/16.
 */


public class TestJSONRequestParser {
    @Test
    public void testParse(){
        AttributeValue attributeValue = new StringAttribute("http://127.0.0.1");
        List<AttributeValue> attributeValues = new ArrayList<>();
        attributeValues.add(attributeValue);

        Attribute attribute  = new Attribute(URI.create("urn:oasis:names:tc:xacml:1.0:resource:resource-id"),
                                    null,null,null,attributeValues,false, XACMLConstants.XACML_VERSION_3_0);
        Set<Attribute> attributeSet = new HashSet<>();
        attributeSet.add(attribute);

        Attributes category = new Attributes(URI.create(EntitlementEndpointConstants.CATEGORY_RESOURCE_URI),
                                             attributeSet);
        Set<Attributes> categories = new HashSet<>();
        categories.add(category);

        RequestCtx requestCtx = new RequestCtx(categories,null);


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
            System.out.println("test");
        }catch(Exception e){
            e.printStackTrace();
        }


    }
}
