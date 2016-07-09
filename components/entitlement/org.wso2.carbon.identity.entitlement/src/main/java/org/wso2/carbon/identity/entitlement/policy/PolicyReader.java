/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.identity.entitlement.policy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.balana.AbstractPolicy;
import org.wso2.balana.ParsingException;
import org.wso2.balana.Policy;
import org.wso2.balana.PolicySet;
import org.wso2.balana.finder.PolicyFinder;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class PolicyReader implements ErrorHandler {

    // the standard attribute for specifying the XML schema language
    private static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

    // the standard identifier for the XML schema specification
    private static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";

    // the standard attribute for specifying schema source
    private static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
    // To enable attempted thread-safety using double-check locking
    private static final Object lock = new Object();
    private static Log log = LogFactory.getLog(PolicyReader.class);
    private static volatile PolicyReader reader;
    // the builder used to create DOM documents
    private DocumentBuilder builder;

    // policy finder module to find  policies
    private PolicyFinder policyFinder;

    private PolicyReader(PolicyFinder policyFinder) {

        this.policyFinder = policyFinder;
        // create the factory
        DocumentBuilderFactory factory = IdentityUtil.getSecuredDocumentBuilderFactory();
        factory.setIgnoringComments(true);
        // now use the factory to create the document builder
        try {
            builder = factory.newDocumentBuilder();
            builder.setErrorHandler(this);
        } catch (ParserConfigurationException pce) {
            throw new IllegalArgumentException("Filed to setup repository: ");
        }
    }

    /**
     * @param policyFinder
     * @return
     */
    public static PolicyReader getInstance(PolicyFinder policyFinder) {
        if (reader == null) {
            synchronized (lock) {
                if (reader == null) {
                    reader = new PolicyReader(policyFinder);
                }
            }
        }
        return reader;
    }

    /**
     * @param policy
     * @return
     */
    public boolean isValidPolicy(String policy) {
        InputStream stream = null;
        try {
            stream = new ByteArrayInputStream(policy.getBytes("UTF-8"));
            handleDocument(builder.parse(stream));
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * @param policy
     * @return
     */
    public synchronized AbstractPolicy getPolicy(String policy) {
        InputStream stream = null;
        try {
            stream = new ByteArrayInputStream(policy.getBytes("UTF-8"));
            return handleDocument(builder.parse(stream));
        } catch (Exception e) {
            log.error("Error while parsing the policy", e);
            return null;
        }
    }

    /**
     * Reads policy target from the policy
     *
     * @param policy policy as a String
     * @return target as PolicyTarget object
     */
    public PolicyTarget getTarget(String policy) {
        InputStream stream = null;
        PolicyTarget policyTarget = new PolicyTarget();
        try {
            stream = new ByteArrayInputStream(policy.getBytes("UTF-8"));
            AbstractPolicy abstractPolicy = handleDocument(builder.parse(stream));
            policyTarget.setTarget(abstractPolicy.getTarget());
            policyTarget.setPolicyId(abstractPolicy.getId().toString());
            return policyTarget;
        } catch (Exception e) {
            log.error("Error while parsing the policy", e);
            return null;
        }
    }

    /**
     * @param doc
     * @return
     * @throws ParsingException
     */
    private AbstractPolicy handleDocument(Document doc) throws ParsingException {
        // handle the policy, if it's a known type
        Element root = doc.getDocumentElement();
        String name = root.getLocalName();
        // see what type of policy this is
        if (name.equals("Policy")) {
            return Policy.getInstance(root);
        } else if (name.equals("PolicySet")) {
            return PolicySet.getInstance(root, policyFinder);
        } else {
            // this isn't a root type that we know how to handle
            throw new ParsingException("Unknown root document type: " + name);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void warning(SAXParseException exception) throws SAXException {
        if (log.isWarnEnabled()) {
            String message = null;
            message = "Warning on line " + exception.getLineNumber() + ": "
                    + exception.getMessage();
            log.warn(message);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void error(SAXParseException exception) throws SAXException {
        if (log.isWarnEnabled()) {
            log.warn("Error on line " + exception.getLineNumber() + ": " + exception.getMessage()
                    + " ... " + "Policy will not be available");
        }

        throw new SAXException("error parsing policy");
    }

    /**
     * {@inheritDoc}
     */
    public void fatalError(SAXParseException exception) throws SAXException {
        if (log.isWarnEnabled()) {
            log.warn("Fatal error on line " + exception.getLineNumber() + ": "
                    + exception.getMessage() + " ... " + "Policy will not be available");
        }

        throw new SAXException("fatal error parsing policy");
    }

    public PolicyFinder getPolicyFinder() {
        return policyFinder;
    }
}
