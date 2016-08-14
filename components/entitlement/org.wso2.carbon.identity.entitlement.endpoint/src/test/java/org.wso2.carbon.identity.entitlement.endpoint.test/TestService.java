package org.wso2.carbon.identity.entitlement.endpoint.test;

import org.apache.cxf.jaxrs.client.WebClient;
import org.testng.Assert;
import org.testng.annotations.Test;


import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;

public class TestService extends Assert{
    private final static String ENDPOINT_ADDRESS = "https://localhost:9443/wso2-entitlement/entitlement/Decision";
    private final static String WADL_ADDRESS = ENDPOINT_ADDRESS + "?_wadl";

    TestService(){
        System.setProperty("javax.net.ssl.trustStore", "/home/manujith/Apps/wso2is-5.1.0/repository/resources/security/client-truststore.jks");
    }

    private static void waitForWADL() throws Exception {
        WebClient client = WebClient.create(WADL_ADDRESS);
        // wait for 20 secs or so
        for (int i = 0; i < 20; i++) {
            Thread.currentThread().sleep(1000);
            Response response = client.get();
            if (response.getStatus() == 200) {
                break;
            }
        }
        // no WADL is available yet - throw an exception or give tests a chance to run anyway
    }

    private String readReource(String path){
        StringBuilder result = new StringBuilder("");
        try{
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
            e.printStackTrace();
        } catch(URISyntaxException e){
            e.printStackTrace();
        }

        return result.toString().replaceAll("\\n\\r|\\n|\\r|\\t|\\s{2,}","");
    }

    @Test
    public void testGetDecisionByAttributesXML(){

        WebClient client = WebClient.create(ENDPOINT_ADDRESS);

        client.header("Authorization","Basic YWRtaW46YWRtaW4=");
        client.type("application/xml");
        client.accept("application/xml");

        client.path("by-attrib");



        String request = readReource("xml/request-by-attrib-1.xml");
        String response = readReource("xml/response-by-attrib-1.xml");

        String webRespose = client.post(request,String.class);

        assertEquals(response,webRespose);
    }

    @Test
    public void testGetDecisionByAttributesJSON(){

        WebClient client = WebClient.create(ENDPOINT_ADDRESS);

        client.header("Authorization","Basic YWRtaW46YWRtaW4=");
        client.type("application/json");
        client.accept("application/xml");

        client.path("by-attrib");



        String request = readReource("json/request-by-attrib-1.json");
        String response = readReource("json/response-by-attrib-1.xml");

        String webRespose = client.post(request,String.class);

        assertEquals(response,webRespose);
    }




}