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

package org.wso2.carbon.identity.common.util.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.wso2.carbon.identity.common.base.exception.IdentityException;

import javax.xml.parsers.DocumentBuilderFactory;

/**
 * XML Utils.
 */
public class XMLUtils {

    private static final Logger logger = LoggerFactory.getLogger(XMLUtils.class);
    private static final int ENTITY_EXPANSION_LIMIT = 0;
    private static volatile XMLUtils instance = new XMLUtils();

    private XMLUtils() {

    }

    public static XMLUtils getInstance() {

        return instance;
    }

    public static Element getDocumentElement(String xmlString) throws IdentityException {
//
//        try {
//            DocumentBuilderFactory documentBuilderFactory = getSecuredDocumentBuilderFactory();
//            DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();
//            Document document = docBuilder.parse(new ByteArrayInputStream(xmlString.trim().getBytes(StandardCharsets
//                    .UTF_8)));
//            Element element = document.getDocumentElement();
//            return element;
//        } catch (ParserConfigurationException | SAXException | IOException e) {
//            String message = "Error in constructing element from the encoded XML string.";
//            throw IdentityException.error(message, e);
//        }

        return null;
    }

    /**
     * Create DocumentBuilderFactory with the XXE and XEE prevention measurements.
     *
     * @return DocumentBuilderFactory instance
     */
    public static DocumentBuilderFactory getSecuredDocumentBuilderFactory() {

//        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//        dbf.setNamespaceAware(true);
//        dbf.setXIncludeAware(false);
//        dbf.setExpandEntityReferences(false);
//        try {
//            dbf.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE, false);
//            dbf.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE, false);
//            dbf.setFeature(Constants.XERCES_FEATURE_PREFIX + Constants.LOAD_EXTERNAL_DTD_FEATURE, false);
//            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
//
//        } catch (ParserConfigurationException e) {
//            logger.error("Failed to load XML Processor Feature " + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE+" or" +
//                    " " +
//                    Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE + " or " + Constants.LOAD_EXTERNAL_DTD_FEATURE +
//                    " or secure-processing.");
//        }
//
//        SecurityManager securityManager = new SecurityManager();
//        securityManager.setEntityExpansionLimit(ENTITY_EXPANSION_LIMIT);
//        dbf.setAttribute(Constants.XERCES_PROPERTY_PREFIX + Constants.SECURITY_MANAGER_PROPERTY, securityManager);

        return null;

    }
}
