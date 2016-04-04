/*
 *
 * Copyright (c) 2007, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
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

package org.wso2.carbon.policyeditor;

import org.apache.axis2.AxisFault;
import org.apache.commons.io.Charsets;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.util.SecurityManager;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.wso2.carbon.identity.core.util.IdentityIOStreamUtils;
import org.wso2.carbon.policyeditor.util.CarbonEntityResolver;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;


public class PolicyEditorService {

    private static final Log log = LogFactory.getLog(PolicyEditorService.class);
    private static final String SECURITY_MANAGER_PROPERTY = Constants.XERCES_PROPERTY_PREFIX + Constants.SECURITY_MANAGER_PROPERTY;
    private static final int ENTITY_EXPANSION_LIMIT = 0;
    public static final String EXTERNAL_GENERAL_ENTITIES_URI = "http://xml.org/sax/features/external-general-entities";
    // The location of the XSD file resources
    private static final String ORG_WSO2_CARBON_POLICYEDITOR_XSD =
            "/org/wso2/carbon/policyeditor/xsd/";


    /**
     * Retrieves a Policy document from a given URL
     *
     * @param policyURL
     * @return A CDATA Wrapped Policy document if found
     * @throws AxisFault
     */
    public String getPolicyDoc(String policyURL) throws AxisFault {
        String policy;

        // Open a stream to the policy file using the URL.
        try {
            URL url = new URL(policyURL);

            InputStream in = url.openStream();
            BufferedReader dis =
                    new BufferedReader(new InputStreamReader(in, Charsets.UTF_8));
            StringBuilder fBuf = new StringBuilder();

            String line;
            while ((line = dis.readLine()) != null) {
                fBuf.append(line).append("\n");
            }
            in.close();

            policy = fBuf.toString();
            dis.close();
        } catch (IOException e) {
            throw new AxisFault("Axis Error while getting policy docs." ,e);
        }

        return "<![CDATA[" + policy + "]]>";
    }


    /**
     * Retrieves content from a named schema file bundled as a resource.
     *
     * @param fileName
     * @return
     * @throws AxisFault
     */
    public String getSchema(String fileName) throws AxisFault {
        String schema;

        StringBuilder fBuf;
        try {
            InputStream in = PolicyEditorService.class.getResourceAsStream(
                    ORG_WSO2_CARBON_POLICYEDITOR_XSD + fileName);

            BufferedReader dis =
                    new BufferedReader(new InputStreamReader(in, Charsets.UTF_8));
            fBuf = new StringBuilder();

            String line;
            while ((line = dis.readLine()) != null) {
                fBuf.append(line).append("\n");
            }
            in.close();

            schema = fBuf.toString();
            dis.close();
        } catch (IOException e) {
            throw new AxisFault("Axis error while getting schemas.", e);
        }

        return "<![CDATA[" + schema + "]]>";
    }

    /**
     * Returns a list of bundled shema (XSD) file names
     *
     * @return A file name list
     * @throws AxisFault
     */
    public String getAvailableSchemas() throws AxisFault {
        String fileList = "";

        StringBuilder fBuf;
        BufferedReader dis = null;
        try {
            InputStream in = PolicyEditorService.class.getResourceAsStream(
                    ORG_WSO2_CARBON_POLICYEDITOR_XSD + "policies.xml");

            dis = new BufferedReader(new InputStreamReader(in, Charsets.UTF_8));
            fBuf = new StringBuilder();

            String line;
            while ((line = dis.readLine()) != null) {
                fBuf.append(line).append("\n");
            }
            fileList = fBuf.toString();
        } catch (IOException e) {
            throw new AxisFault("Axis fault while getting schemas.", e);
        } finally {
            IdentityIOStreamUtils.closeReader(dis);
        }

        return "<![CDATA[" + fileList + "]]>";
    }


    /**
     * Formats a given unformatted XML string
     *
     * @param xml
     * @return A CDATA wrapped, formatted XML String
     */
    public String formatXML(String xml) {

        try {
            // create the factory
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            docFactory.setIgnoringComments(true);
            docFactory.setNamespaceAware(true);
            docFactory.setExpandEntityReferences(false);
            SecurityManager securityManager = new SecurityManager();
            securityManager.setEntityExpansionLimit(ENTITY_EXPANSION_LIMIT);
            docFactory.setAttribute(SECURITY_MANAGER_PROPERTY, securityManager);
            DocumentBuilder docBuilder;
            Document xmlDoc;

            // now use the factory to create the document builder
            docFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            docFactory.setFeature(EXTERNAL_GENERAL_ENTITIES_URI, false);
            docBuilder = docFactory.newDocumentBuilder();
            docBuilder.setEntityResolver(new CarbonEntityResolver());
            xmlDoc = docBuilder.parse(new ByteArrayInputStream(xml.getBytes(Charsets.UTF_8)));


            OutputFormat format = new OutputFormat(xmlDoc);
            format.setLineWidth(0);
            format.setIndenting(true);
            format.setIndent(2);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLSerializer serializer = new XMLSerializer(baos, format);
            serializer.serialize(xmlDoc);

            xml = baos.toString("UTF-8");

        } catch (ParserConfigurationException pce) {
            throw new IllegalArgumentException("Failed to parse the unformatted XML String. ", pce);
        } catch (Exception e) {
            log.error("Error occured while formtting the unformatted XML String. ", e);
        }

        return "<![CDATA[" + xml + "]]>";
    }

}
