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

package org.wso2.carbon.identity.gateway.handler.validation.saml;

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
import org.wso2.carbon.identity.framework.context.IdentityMessageContext;
import org.wso2.carbon.identity.framework.handler.GatewayEventHandler;
import org.wso2.carbon.identity.framework.handler.GatewayInvocationResponse;
import org.wso2.carbon.identity.framework.message.IdentityRequest;
import org.wso2.carbon.identity.framework.util.FrameworkUtil;
import org.wso2.carbon.identity.gateway.util.SAMLUtils;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING;
import static org.apache.xerces.impl.Constants.SECURITY_MANAGER_PROPERTY;
import static org.apache.xerces.impl.Constants.XERCES_PROPERTY_PREFIX;
import static org.wso2.carbon.identity.gateway.handler.validation.saml.SAMLConstants.SAML_AUTH_REQUEST;

public class SAMLValidationHandler extends GatewayEventHandler {

    private Logger logger = LoggerFactory.getLogger(SAMLValidationHandler.class);
    private static final String XERCES_SECURITY_MANAGER_PROPERTY = XERCES_PROPERTY_PREFIX + SECURITY_MANAGER_PROPERTY;


    @Override
    public GatewayInvocationResponse handle(IdentityMessageContext context) {

        IdentityRequest identityRequest = context.getIdentityRequest();

        String encodedRequest = identityRequest.getBody();
        String method = identityRequest.getMethod();

        if ("POST".equalsIgnoreCase(method)) {

            String urlDecodedRequest = null;
            try {
                urlDecodedRequest = URLDecoder.decode(encodedRequest.split("=", 2)[1], UTF_8.name());
                String decodedRequest = new String(Base64.getDecoder().decode(urlDecodedRequest), UTF_8);
                if (logger.isDebugEnabled()) {
                    logger.debug("Decoded SAML request: " + decodedRequest);
                }
                AuthnRequest samlAuthenticationRequest = buildSAMLRequest(decodedRequest);

                // add the built request to the authentication context map
                Map<String, Object> authContextMap = new HashMap<>();
                authContextMap.put(SAML_AUTH_REQUEST, samlAuthenticationRequest);

                // add the authentication context map to message context passed across handlers
                String sessionId = FrameworkUtil.generateSessionIdentifier();
                context.addParameter(sessionId, authContextMap);

                // add the sessionIdentifier to the message context.
                FrameworkUtil.addSessionIdentifierToContext(context, sessionId);

            } catch (IOException |
                    UnmarshallingException |
                    ConfigurationException |
                    ParserConfigurationException |
                    SAXException e) {
                logger.error("Error building the SAML Authentication Request.", e);
                return GatewayInvocationResponse.ERROR;
            }

        }

        return GatewayInvocationResponse.CONTINUE;
    }

    @Override
    public boolean canHandle(IdentityMessageContext identityMessageContext) {
        return true;
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
