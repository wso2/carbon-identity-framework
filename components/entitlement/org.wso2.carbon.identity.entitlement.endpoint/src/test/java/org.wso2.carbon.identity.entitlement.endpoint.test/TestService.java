/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.entitlement.endpoint.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.client.WebClient;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;

public class TestService extends Assert {
    private final static String ENDPOINT_ADDRESS = "https://localhost:9443/api/identity/entitlement/decision";
    private final static String WADL_ADDRESS = ENDPOINT_ADDRESS + "?_wadl";
    private static Log log = LogFactory.getLog(TestService.class);

    private static boolean waitForWADL() {
        WebClient client = WebClient.create(WADL_ADDRESS);
        // wait for 20 secs or so
        for (int i = 0; i < 20; i++) {
            try {
                Thread.currentThread().sleep(100);
                Response response = client.get();
                if (response.getStatus() == 200) {
                    return true;
                }
            } catch (Exception e) {
                return false;
            }
        }
        // no WADL is available yet - throw an exception or give tests a chance to run anyway
        log.error("Service offline");

        return false;
    }

    private String readReource(String path) {
        StringBuilder result = new StringBuilder("");
        try {
            //Get file from resources folder
            ClassLoader classLoader = getClass().getClassLoader();
            URI filepath = new URI(classLoader.getResource(path).toString());

            File file = new File(filepath);

            Scanner scanner = new Scanner(file);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                result.append(line).append("\n");
            }

            scanner.close();

        } catch (IOException e) {
            log.error("IO Exception in reading test case");
        } catch (URISyntaxException e) {
            log.error("IO Exception in reading test case");
        }

        return result.toString().replaceAll("\\n\\r|\\n|\\r|\\t|\\s{2,}", "").replaceAll(": ", ":");
    }

    @Test
    public void testHomeXML() {
        if (!waitForWADL()) {
            return;
        }

        WebClient client = WebClient.create(ENDPOINT_ADDRESS);

        client.header("Authorization", "Basic YWRtaW46YWRtaW4=");
        client.type("application/xml");
        client.accept("application/xml");

        client.path("home");

        String response = readReource("xml/response-home.xml");

        String webRespose = client.get(String.class);
        assertEquals(response, webRespose);
    }

    @Test
    public void testHomeJSON() {
        if (!waitForWADL()) {
            return;
        }

        WebClient client = WebClient.create(ENDPOINT_ADDRESS);

        client.header("Authorization", "Basic YWRtaW46YWRtaW4=");
        client.type("application/json");
        client.accept("application/json");

        client.path("home");

        String response = readReource("json/response-home.json");

        String webRespose = client.get(String.class);
        assertEquals(response, webRespose);
    }

    @Test
    public void testPdpXML() {
        if (!waitForWADL()) {
            return;
        }

        WebClient client = WebClient.create(ENDPOINT_ADDRESS);

        client.header("Authorization", "Basic YWRtaW46YWRtaW4=");
        client.type("application/xml");
        client.accept("application/xml");

        client.path("pdp");

        String request = readReource("xml/request-pdp-1.xml");
        String response = readReource("xml/response-pdp-1.xml");

        String webRespose = client.post(request, String.class);
        assertEquals(response, webRespose);
    }

    @Test
    public void testPdpJSON() {
        if (!waitForWADL()) {
            return;
        }

        WebClient client = WebClient.create(ENDPOINT_ADDRESS);

        client.header("Authorization", "Basic YWRtaW46YWRtaW4=");
        client.type("application/json");
        client.accept("application/json");

        client.path("pdp");

        String request = readReource("json/request-pdp-1.json");
        String response = readReource("json/response-pdp-1.json");

        String webRespose = client.post(request, String.class);
        assertEquals(response, webRespose);
    }

    @Test
    public void testGetDecisionByAttributesXML() {
        if (!waitForWADL()) {
            return;
        }

        WebClient client = WebClient.create(ENDPOINT_ADDRESS);

        client.header("Authorization", "Basic YWRtaW46YWRtaW4=");
        client.type("application/xml");
        client.accept("application/xml");

        client.path("by-attrib");

        String request = readReource("xml/request-by-attrib-1.xml");
        String response = readReource("xml/response-by-attrib-1.xml");

        String webRespose = client.post(request, String.class);

        assertEquals(response, webRespose);
    }

    @Test
    public void testGetDecisionByAttributesJSON() {
        if (!waitForWADL()) {
            return;
        }

        WebClient client = WebClient.create(ENDPOINT_ADDRESS);

        client.header("Authorization", "Basic YWRtaW46YWRtaW4=");
        client.type("application/json");
        client.accept("application/xml");

        client.path("by-attrib");

        String request = readReource("json/request-by-attrib-1.json");
        String response = readReource("json/response-by-attrib-1.xml");

        String webRespose = client.post(request, String.class);

        assertEquals(response, webRespose);
    }

    @Test
    public void testGetDecisionByAttributesBooleanXML() {
        if (!waitForWADL()) {
            return;
        }

        WebClient client = WebClient.create(ENDPOINT_ADDRESS);

        client.header("Authorization", "Basic YWRtaW46YWRtaW4=");
        client.type("application/xml");
        client.accept("application/xml");

        client.path("by-attrib-boolean");

        String request = readReource("xml/request-by-attrib-bool-1.xml");
        String response = readReource("xml/response-by-attrib-bool-1.xml");

        String webRespose = client.post(request, String.class);

        assertEquals(response, webRespose);
    }

    @Test
    public void testGetDecisionByAttributesBooleanJSON() {
        if (!waitForWADL()) {
            return;
        }

        WebClient client = WebClient.create(ENDPOINT_ADDRESS);

        client.header("Authorization", "Basic YWRtaW46YWRtaW4=");
        client.type("application/json");
        client.accept("application/json");

        client.path("by-attrib-boolean");

        String request = readReource("json/request-by-attrib-bool-1.json");
        String response = readReource("json/response-by-attrib-bool-1.json");

        String webRespose = client.post(request, String.class);
        assertEquals(response, webRespose);
    }

    @Test
    public void testEntitledAttributesXML() {
        if (!waitForWADL()) {
            return;
        }

        WebClient client = WebClient.create(ENDPOINT_ADDRESS);

        client.header("Authorization", "Basic YWRtaW46YWRtaW4=");
        client.type("application/xml");
        client.accept("application/xml");

        client.path("entitled-attribs");

        String request = readReource("xml/request-entitled-attribs-1.xml");
        String response = readReource("xml/response-entitled-attribs-1.xml");

        String webRespose = client.post(request, String.class);
        assertEquals(response, webRespose);
    }

    @Test
    public void testEntitledAttributesJSON() {
        if (!waitForWADL()) {
            return;
        }

        WebClient client = WebClient.create(ENDPOINT_ADDRESS);

        client.header("Authorization", "Basic YWRtaW46YWRtaW4=");
        client.type("application/json");
        client.accept("application/json");

        client.path("entitled-attribs");

        String request = readReource("json/request-entitled-attribs-1.json");
        String response = readReource("json/response-entitled-attribs-1.json");

        String webRespose = client.post(request, String.class);
        assertEquals(response, webRespose);
    }

    @Test
    public void testAllEntitlementsXML() {
        if (!waitForWADL()) {
            return;
        }

        WebClient client = WebClient.create(ENDPOINT_ADDRESS);

        client.header("Authorization", "Basic YWRtaW46YWRtaW4=");
        client.type("application/xml");
        client.accept("application/xml");

        client.path("entitlements-all");

        String request = readReource("xml/request-all-entitlements-1.xml");
        String response = readReource("xml/response-all-entitlements-1.xml");

        String webRespose = client.post(request, String.class);
        assertEquals(response, webRespose);
    }

    @Test
    public void testAllEntitlementsJSON() {
        if (!waitForWADL()) {
            return;
        }

        WebClient client = WebClient.create(ENDPOINT_ADDRESS);

        client.header("Authorization", "Basic YWRtaW46YWRtaW4=");
        client.type("application/json");
        client.accept("application/json");

        client.path("entitlements-all");

        String request = readReource("json/request-all-entitlements-1.json");
        String response = readReource("json/response-all-entitlements-1.json");

        String webRespose = client.post(request, String.class);
        assertEquals(response, webRespose);
    }


}
