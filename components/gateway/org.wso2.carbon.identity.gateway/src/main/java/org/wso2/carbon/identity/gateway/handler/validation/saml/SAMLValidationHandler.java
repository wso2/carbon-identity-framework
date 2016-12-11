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

import org.apache.commons.lang.StringUtils;
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
import org.wso2.carbon.identity.gateway.util.SAMLUtils;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import javax.ws.rs.HttpMethod;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING;
import static org.apache.xerces.impl.Constants.SECURITY_MANAGER_PROPERTY;
import static org.apache.xerces.impl.Constants.XERCES_PROPERTY_PREFIX;
import static org.wso2.carbon.identity.gateway.SAMLConstants.SAML_AUTH_REQUEST;

public class SAMLValidationHandler extends GatewayEventHandler {

    private Logger logger = LoggerFactory.getLogger(SAMLValidationHandler.class);
    private static final String XERCES_SECURITY_MANAGER_PROPERTY = XERCES_PROPERTY_PREFIX + SECURITY_MANAGER_PROPERTY;


    @Override
    public GatewayInvocationResponse handle(IdentityMessageContext context) {

        IdentityRequest identityRequest = context.getCurrentIdentityRequest();

        String encodedRequest = identityRequest.getBody();
        String method = identityRequest.getMethod();

        // we are expecting to handle SAML POST binding request
        if (StringUtils.equalsIgnoreCase(HttpMethod.POST, method)) {
            try {
                String decodedRequest = getBase64DecodedSamlRequest(encodedRequest);
                if (decodedRequest == null) {
                    logger.error("Error decoding the SAML Request.");
                    return GatewayInvocationResponse.ERROR;
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("Decoded SAML request: " + decodedRequest);
                }

                AuthnRequest samlAuthenticationRequest = buildSAMLRequest(decodedRequest);

                // Review this approach
                String sessionId = context.getSessionDataKey();
                Map<String, Object> authContextMap =
                        (Map<String, Object>) Optional.ofNullable(context.getParameter(sessionId)).orElseThrow(() ->
                                new RuntimeException("Unable to find the authentication context map."));

                // add the built request to the authentication context map
                authContextMap.put(SAML_AUTH_REQUEST, samlAuthenticationRequest);

            } catch (IOException |
                    UnmarshallingException |
                    ConfigurationException |
                    ParserConfigurationException |
                    SAXException e) {
                logger.error("Error building the SAML Authentication Request.", e);
                return GatewayInvocationResponse.ERROR;
            }
        } else if (StringUtils.equalsIgnoreCase(method, HttpMethod.GET)) {
            // TODO : handle SAML HTTP-Redirect binding
            return GatewayInvocationResponse.SUSPEND;
        } else {
            return GatewayInvocationResponse.ERROR;
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


    /**
     * get the base64 decoded SAML Request String from the url encoded request body.
     *
     * @param urlEncodedRequest URL encoded request parameters.
     * @return base64 decoded SAML Request String.
     * @throws UnsupportedEncodingException
     */
    private String getBase64DecodedSamlRequest(String urlEncodedRequest) throws UnsupportedEncodingException {

        String[] encodedParams = Optional.ofNullable(urlEncodedRequest).orElse("").split("=", 2);
        if (encodedParams.length == 2) {
            String urlDecodedSAMLRequest = URLDecoder.decode(encodedParams[1], UTF_8.name());
            return new String(Base64.getDecoder().decode(urlDecodedSAMLRequest), UTF_8);
        }
        return null;
    }
}
