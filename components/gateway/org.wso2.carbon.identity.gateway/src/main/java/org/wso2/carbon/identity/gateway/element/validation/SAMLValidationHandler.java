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

package org.wso2.carbon.identity.gateway.element.validation;

import org.apache.xerces.util.SecurityManager;
import org.opensaml.Configuration;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.io.UnmarshallingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.carbon.identity.framework.handler.HandlerException;
import org.wso2.carbon.identity.framework.handler.HandlerResponseStatus;
import org.wso2.carbon.identity.gateway.context.GatewayMessageContext;
import org.wso2.carbon.identity.gateway.handler.AbstractGatewayHandler;
import org.wso2.carbon.identity.gateway.message.GatewayRequest;
import org.wso2.carbon.identity.gateway.util.SAMLUtils;
import org.xml.sax.SAXException;

import javax.ws.rs.HttpMethod;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING;
import static org.apache.xerces.impl.Constants.SECURITY_MANAGER_PROPERTY;
import static org.apache.xerces.impl.Constants.XERCES_PROPERTY_PREFIX;
import static org.wso2.carbon.identity.gateway.util.SAMLConstants.SAML_AUTH_REQUEST;

public class SAMLValidationHandler extends AbstractGatewayHandler {

    private static Logger logger = LoggerFactory.getLogger(SAMLValidationHandler.class);
    private static final String XERCES_SECURITY_MANAGER_PROPERTY = XERCES_PROPERTY_PREFIX + SECURITY_MANAGER_PROPERTY;
    private static final String SAML_REQUEST = "SAMLRequest";



    @Override
    public HandlerResponseStatus handle(GatewayMessageContext context) throws HandlerException {

        GatewayRequest identityRequest = context.getCurrentIdentityRequest();
        String method = identityRequest.getMethod();

        if (HttpMethod.POST.equalsIgnoreCase(method) || HttpMethod.GET.equalsIgnoreCase(method)) {

            String urlDecodedRequest = String.valueOf(identityRequest.getProperty(SAML_REQUEST));
            String base64DecodedRequest;
            try {
                base64DecodedRequest = decodeForPost(urlDecodedRequest);
                // if the method is HTTP-redirect, then we need to inflate the request too.
                if (HttpMethod.GET.equalsIgnoreCase(method)) {
                    base64DecodedRequest = inflate(urlDecodedRequest);
                }
                // validate the SAML Request now.
                validateSAMLRequest(base64DecodedRequest, context);
                return HandlerResponseStatus.CONTINUE;
            } catch (UnsupportedEncodingException e) {
                logger.error("Error decoding the SAML request.", e);
                // throw an exception here.
            } catch (IOException | UnmarshallingException | ConfigurationException | ParserConfigurationException |
                    SAXException e) {
                logger.error("Error validating the decoded SAML request.", e);
            }
        } else {
            // unsupported method
            logger.error("HTTP method " + method + " is not supported by SAML.");
            throw new HandlerException("HTTP method " + method + " is not supported by SAML.");
        }
        // if we get here, we have encountered an error.
        return HandlerResponseStatus.SUSPEND;
    }


    /**
     * Get the base64 decoded SAML Request String from the Identity Request.
     *
     * @return base64 decoded SAML Request String.
     * @throws UnsupportedEncodingException
     */
    private String decodeForPost(String urlDecodedRequest) throws UnsupportedEncodingException {

        if (urlDecodedRequest != null) {
            return new String(Base64.getDecoder().decode(urlDecodedRequest.getBytes(UTF_8)));
        }
        return null;
    }


    private String inflate(String urlDecodedRequest) {

        try {
            byte[] xmlBytes = urlDecodedRequest.getBytes(UTF_8);
            byte[] base64DecodedByteArray = Base64.getDecoder().decode(xmlBytes);
            try {
                Inflater inflater = new Inflater(true);
                inflater.setInput(base64DecodedByteArray);
                byte[] xmlMessageBytes = new byte[5000];
                int resultLength = inflater.inflate(xmlMessageBytes);

                if (!inflater.finished()) {
                    throw new RuntimeException("End of the compressed data stream has NOT been reached");
                }
                inflater.end();
                return new String(xmlMessageBytes, 0, resultLength, UTF_8);

            } catch (DataFormatException e) {
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(base64DecodedByteArray);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                InflaterInputStream iis = new InflaterInputStream(byteArrayInputStream);
                byte[] buf = new byte[1024];
                int count = iis.read(buf);
                while (count != -1) {
                    byteArrayOutputStream.write(buf, 0, count);
                    count = iis.read(buf);
                }
                iis.close();
                return new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            logger.error("Error when decoding the SAML Request.", e);
            throw new RuntimeException("Error decoding SAML request.", e);
        }
    }


    private void validateSAMLRequest(String decodedRequest, GatewayMessageContext context)
            throws ParserConfigurationException, UnmarshallingException, SAXException, ConfigurationException,
            IOException {

        AuthnRequest samlAuthenticationRequest = buildSAMLRequest(decodedRequest);

        // TODO: Have to decide on how we add things to context map
        String sessionId = context.getSessionDataKey();
        Map<String, Object> authContextMap =
                (Map<String, Object>) Optional.ofNullable(context.getParameter(sessionId)).orElseThrow(() ->
                        new RuntimeException("Unable to find the authentication context map."));

        // add the built request to the authentication context map
        authContextMap.put(SAML_AUTH_REQUEST, samlAuthenticationRequest);
    }


    private AuthnRequest buildSAMLRequest(String samlRequest)
            throws ParserConfigurationException, SAXException, ConfigurationException, IOException,
            UnmarshallingException {

        SAMLUtils.doBootstrap();
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        documentBuilderFactory.setExpandEntityReferences(false);
        documentBuilderFactory.setFeature(FEATURE_SECURE_PROCESSING, true);

        SecurityManager securityManager = new SecurityManager();
        securityManager.setEntityExpansionLimit(0);

        documentBuilderFactory.setAttribute(XERCES_SECURITY_MANAGER_PROPERTY, securityManager);
        DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();
        docBuilder.setEntityResolver((publicId, systemId) -> {
            throw new SAXException("SAML request contains invalid elements. Possible XML External Entity " +
                    "(XXE) attack.");
        });

        try (InputStream inputStream = new ByteArrayInputStream(samlRequest.trim().getBytes(UTF_8))) {
            Document document = docBuilder.parse(inputStream);
            Element element = document.getDocumentElement();
            UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
            Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(element);
            return (AuthnRequest) unmarshaller.unmarshall(element);
        }

    }
}
