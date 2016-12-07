/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.gateway.util;

import org.opensaml.DefaultBootstrap;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameIDType;
import org.opensaml.saml2.core.impl.IssuerBuilder;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.io.MarshallingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SAMLUtils {

    private static boolean isBootStrapped = false;
    private static final Logger log = LoggerFactory.getLogger(SAMLUtils.class);


    public static void doBootstrap() {
        if (!isBootStrapped) {
            try {
                DefaultBootstrap.bootstrap();
                isBootStrapped = true;
            } catch (ConfigurationException e) {
                log.error("Error in bootstrapping the OpenSAML2 library", e);
            }
        }
    }

    public static String getHTMLResponseBody(String samlResponse) {
        return "<html>\n" +
                "\t<body>\n" +
                "        \t<p>You are now redirected to $url \n" +
                "        \tIf the redirection fails, please click the post button.</p>\n" +
                "        \t<form method='post' action='http://localhost:8080/travelocity.com/home.jsp'>\n" +
                "       \t\t\t<p>\n" +
                "<input type='hidden' name='SAMLResponse' value='" + samlResponse + "'>" +
                "        \t\t\t<button type='submit'>POST</button>\n" +
                "       \t\t\t</p>\n" +
                "       \t\t</form>\n" +
                "       \t\t<script type='text/javascript'>\n" +
                "        \t\tdocument.forms[0].submit();\n" +
                "        \t</script>\n" +
                "        </body>\n" +
                "</html>";
    }

    public static Issuer getIssuer(String issuerName, String format) {
        Issuer issuer = new IssuerBuilder().buildObject();
        issuer.setValue(issuerName);
        issuer.setFormat(format);
        return issuer;
    }


    public static String marshall(XMLObject xmlObject) throws javax.naming.ConfigurationException {

        doBootstrap();

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "org.apache.xerces.jaxp" +
                    ".DocumentBuilderFactoryImpl");
            MarshallerFactory marshallerFactory = org.opensaml.xml.Configuration.getMarshallerFactory();
            Marshaller marshaller = marshallerFactory.getMarshaller(xmlObject);
            Element element = marshaller.marshall(xmlObject);

            DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
            DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");

            LSSerializer serializer = impl.createLSSerializer();
            LSOutput output = impl.createLSOutput();

            output.setByteStream(byteArrayOutputStream);
            serializer.write(element, output);

            return byteArrayOutputStream.toString(UTF_8.name());

        } catch (MarshallingException | IOException | ClassNotFoundException | InstantiationException |
                IllegalAccessException e) {
            //TODO Build SAML Error Resp and do proper logging
            log.error("Error while marshalling the SAML response", e);
            return null;
        }
    }
}
