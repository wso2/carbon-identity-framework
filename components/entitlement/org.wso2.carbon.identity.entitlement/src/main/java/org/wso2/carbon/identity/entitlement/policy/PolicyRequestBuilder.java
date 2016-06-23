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
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * XACML request is built
 */
public class PolicyRequestBuilder {

    private static Log log = LogFactory.getLog(PolicyRequestBuilder.class);

    /**
     * creates DOM representation of the XACML request
     *
     * @param request XACML request as a String object
     * @return XACML request as a DOM element
     * @throws EntitlementException throws, if fails
     */
    public Element getXacmlRequest(String request) throws EntitlementException {

        ByteArrayInputStream inputStream;
        DocumentBuilderFactory documentBuilderFactory;
        Document doc;
        DocumentBuilder documentBuilder;

        inputStream = new ByteArrayInputStream(request.getBytes());
        documentBuilderFactory = IdentityUtil.getSecuredDocumentBuilderFactory();

        try {

            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            doc = documentBuilder.parse(inputStream);
        } catch (SAXException e) {
            throw new EntitlementException("Error while creating DOM from XACML request");
        } catch (IOException e) {
            throw new EntitlementException("Error while creating DOM from XACML request");
        } catch (ParserConfigurationException e) {
            throw new EntitlementException("Error while creating DOM from XACML request");
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                log.error("Error in closing input stream of XACML request");
            }
        }
        return doc.getDocumentElement();
    }
}
