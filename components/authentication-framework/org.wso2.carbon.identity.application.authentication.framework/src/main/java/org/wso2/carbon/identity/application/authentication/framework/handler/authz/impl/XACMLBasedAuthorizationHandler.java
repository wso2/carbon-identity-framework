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

package org.wso2.carbon.identity.application.authentication.framework.handler.authz.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.wso2.balana.utils.exception.PolicyBuilderException;
import org.wso2.balana.utils.policy.PolicyBuilder;
import org.wso2.balana.utils.policy.dto.RequestElementDTO;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.handler.authz.AuthorizationHandler;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyConstants;
import org.wso2.carbon.identity.entitlement.ui.dto.RequestDTO;
import org.wso2.carbon.identity.entitlement.ui.dto.RowDTO;
import org.wso2.carbon.identity.entitlement.ui.util.PolicyCreatorUtil;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class XACMLBasedAuthorizationHandler implements AuthorizationHandler {

    private static final Log log = LogFactory.getLog(XACMLBasedAuthorizationHandler.class);
    private static volatile XACMLBasedAuthorizationHandler instance;

    public static XACMLBasedAuthorizationHandler getInstance() {

        if (instance == null) {
            synchronized (XACMLBasedAuthorizationHandler.class) {
                if (instance == null) {
                    instance = new XACMLBasedAuthorizationHandler();
                }
            }
        }
        return instance;
    }

    /**
     * Executes the authorization flow
     *
     * @param request  request
     * @param response response
     * @param context  context
     */
    @Override
    public boolean isAuthorized(HttpServletRequest request, HttpServletResponse response,
                                AuthenticationContext context) {

        if (log.isDebugEnabled()) {
            log.debug("In policy authorization flow...");
        }

        if (context != null) {
            try {
                //TODO: "RequestDTO" and "PolicyCreatorUtil" is taken from entitlement.ui. Need to reconsider of
                // using the ui bundle
                RequestDTO requestDTO = createRequestDTO(context);
                RequestElementDTO requestElementDTO = PolicyCreatorUtil.createRequestElementDTO(requestDTO);

                String requestString = PolicyBuilder.getInstance().buildRequest(requestElementDTO);
                if (log.isDebugEnabled()) {
                    log.debug("XACML Authorization request :\n" + requestString);
                }
                String responseString =
                        FrameworkServiceDataHolder.getInstance().getEntitlementService().getDecision(requestString);
                if (log.isDebugEnabled()) {
                    log.debug("XACML Authorization response :\n" + responseString);
                }
                Boolean isAuthorized = evaluateXACMLResponse(responseString);
                if (isAuthorized) {
                    return true;
                }
                //todo: audit log if not authorized
            } catch (PolicyBuilderException e) {
                log.error("Policy Builder Exception occurred", e);
            } catch (EntitlementException e) {
                log.error("Entitlement Exception occurred", e);
            } catch (FrameworkException e) {
                log.error("Error when evaluating the XACML response", e);
            }
        }
        return false;
    }

    private RequestDTO createRequestDTO(AuthenticationContext context) {

        List<RowDTO> rowDTOs = new ArrayList<>();
        RowDTO contextIdentifierDTO =
                createRowDTO(context.getContextIdentifier(), "urn:oasis:names:tc:xacml:1" +
                        ".0:resource:authn-context-id", "authn-context");
        rowDTOs.add(contextIdentifierDTO);
        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setRowDTOs(rowDTOs);
        return requestDTO;
    }

    private RowDTO createRowDTO(String resourceName, String attributeId, String categoryValue) {

        RowDTO rowDTOTenant = new RowDTO();
        rowDTOTenant.setAttributeValue(resourceName);
        rowDTOTenant.setAttributeDataType(EntitlementPolicyConstants.STRING_DATA_TYPE);
        rowDTOTenant.setAttributeId(attributeId);
        rowDTOTenant.setCategory("urn:oasis:names:tc:xacml:3.0:attribute-category:".concat(categoryValue));
        return rowDTOTenant;

    }

    private boolean evaluateXACMLResponse(String xacmlResponse) throws FrameworkException {

        try {
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(xacmlResponse));
            Document doc = db.parse(is);

            XPath xpath = XPathFactory.newInstance().newXPath();
            XPathExpression expr = xpath.compile("/Response/Result/Decision/text()");
            String decision = (String) expr.evaluate(doc, XPathConstants.STRING);
            if (decision.equalsIgnoreCase(EntitlementPolicyConstants.RULE_EFFECT_PERMIT)
                    || decision.equalsIgnoreCase(EntitlementPolicyConstants.RULE_EFFECT_NOT_APPLICABLE)) {
                return true;
            }

        } catch (ParserConfigurationException | SAXException | XPathExpressionException | IOException e) {
            throw new FrameworkException("Exception occurred while xacmlResponse processing", e);
        }
        return false;
    }
}
