<!--
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 -->
<%@ page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ page import="org.wso2.carbon.CarbonConstants"%>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyConstants"%>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyCreator"%>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.client.EntitlementAdminServiceClient"%>

<%
    boolean evaluatedWithPDP = false;
    String requestString = request.getParameter("txtRequest");
    String withPDP = request.getParameter("withPDP");
    if("true".equals(withPDP)){
        evaluatedWithPDP = true; 
    }
    String forwardTo = request.getParameter("forwardTo");
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().
                    getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
	String resp = null;
	String BUNDLE = "org.wso2.carbon.identity.entitlement.ui.i18n.Resources";
	ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

    List<RowDTO> rowDTOs = new ArrayList<RowDTO>();
    String resourceNames = request.getParameter("resourceNames");
    String subjectNames = request.getParameter("subjectNames");
    String actionNames = request.getParameter("actionNames");
    String environmentNames = request.getParameter("environmentNames");
    String multipleRequest = request.getParameter("multipleRequest");
    String returnPolicyList = request.getParameter("returnPolicyList");

    if (resourceNames != null  && resourceNames.trim().length() > 0){
        RowDTO rowDTO = new RowDTO();
        rowDTO.setAttributeValue(resourceNames);
        rowDTO.setAttributeDataType(EntitlementPolicyConstants.STRING_DATA_TYPE);
        rowDTO.setAttributeId("urn:oasis:names:tc:xacml:1.0:resource:resource-id");
        rowDTO.setCategory("urn:oasis:names:tc:xacml:3.0:attribute-category:resource");
        String resourceNamesInclude = request.getParameter("resourceNamesInclude");
        if(resourceNamesInclude != null){
            rowDTO.setNotCompleted(Boolean.parseBoolean(resourceNamesInclude));
            session.setAttribute("resourceNamesInclude",resourceNamesInclude);
        }
        rowDTOs.add(rowDTO);
        session.setAttribute("resourceNames",resourceNames);
    }
    if (subjectNames != null  && subjectNames.trim().length() > 0){
        RowDTO rowDTO = new RowDTO();
        rowDTO.setAttributeValue(subjectNames);
        rowDTO.setAttributeDataType(EntitlementPolicyConstants.STRING_DATA_TYPE);
        rowDTO.setAttributeId("urn:oasis:names:tc:xacml:1.0:subject:subject-id");
        rowDTO.setCategory("urn:oasis:names:tc:xacml:1.0:subject-category:access-subject");
        String subjectNamesInclude = request.getParameter("subjectNamesInclude");
        if(subjectNamesInclude != null){
            rowDTO.setNotCompleted(Boolean.parseBoolean(subjectNamesInclude));
            session.setAttribute("subjectNamesInclude",subjectNamesInclude);
        }
        rowDTOs.add(rowDTO);
        session.setAttribute("subjectNames",subjectNames);
    }
    if (actionNames != null  && actionNames.trim().length() > 0){
        RowDTO rowDTO = new RowDTO();
        rowDTO.setAttributeValue(actionNames);
        rowDTO.setAttributeDataType(EntitlementPolicyConstants.STRING_DATA_TYPE);
        rowDTO.setAttributeId("urn:oasis:names:tc:xacml:1.0:action:action-id");
        rowDTO.setCategory("urn:oasis:names:tc:xacml:3.0:attribute-category:action");
        String actionNamesInclude = request.getParameter("actionNamesInclude");
        if(actionNamesInclude != null){
            rowDTO.setNotCompleted(Boolean.parseBoolean(actionNamesInclude));
            session.setAttribute("actionNamesInclude",actionNamesInclude);
        }
        rowDTOs.add(rowDTO);
        session.setAttribute("actionNames",actionNames);
    }
    if (environmentNames != null  && environmentNames.trim().length() > 0){
        RowDTO rowDTO = new RowDTO();
        rowDTO.setAttributeValue(environmentNames);
        rowDTO.setAttributeDataType(EntitlementPolicyConstants.STRING_DATA_TYPE);
        rowDTO.setAttributeId("urn:oasis:names:tc:xacml:1.0:environment:environment-id");
        rowDTO.setCategory("urn:oasis:names:tc:xacml:3.0:attribute-category:environment");
        rowDTOs.add(rowDTO);
        String environmentNamesInclude = request.getParameter("environmentNamesInclude");
        if(environmentNamesInclude != null){
            rowDTO.setNotCompleted(Boolean.parseBoolean(environmentNamesInclude));
            session.setAttribute("actionNamesInclude",environmentNamesInclude);
        }
        session.setAttribute("environmentNames", environmentNames);
    }

    RequestDTO requestDTO = new RequestDTO();
    if(multipleRequest != null){
        requestDTO.setMultipleRequest(Boolean.parseBoolean(multipleRequest));
        session.setAttribute("multipleRequest", multipleRequest);
    }
    if(returnPolicyList != null){
        requestDTO.setReturnPolicyIdList(Boolean.parseBoolean(returnPolicyList));
        session.setAttribute("returnPolicyList", returnPolicyList);
    }
    requestDTO.setRowDTOs(rowDTOs);

    EntitlementPolicyCreator entitlementPolicyCreator = new EntitlementPolicyCreator();

    try {
    	EntitlementAdminServiceClient adminClient =
                                new EntitlementAdminServiceClient(cookie, serverURL, configContext);
    	EntitlementServiceClient client = new EntitlementServiceClient(cookie, serverURL, configContext);
        if(requestString == null || requestString.trim().length() < 1){
            String createdRequest = entitlementPolicyCreator.createBasicRequest(requestDTO);
            if(createdRequest != null && createdRequest.trim().length() > 0){
                requestString = createdRequest.trim().replaceAll("><", ">\n<");
            }
        }
        if(evaluatedWithPDP){
            resp = client.getDecision(requestString);
        } else { 
            String policyId = (String) session.getAttribute("policyId");
            if(policyId != null){
                resp = adminClient.getDecision(requestString, new String[]{policyId});
            } else {
                resp = adminClient.getDecision(requestString);
            }
        }

        String responseValue = ClientUtil.getStatus(resp);

        session.setAttribute("txtRequest", requestString);
        session.setAttribute("txtResponse", resp);
    	if (forwardTo == null) {
            CarbonUIMessage.sendCarbonUIMessage(responseValue, CarbonUIMessage.INFO, request);
            forwardTo = "create-evaluation-request.jsp";
    	} else {
            forwardTo = "eval-policy.jsp?isResponse=true";
        }
    } catch (Exception e) {
    	String message = resourceBundle.getString("invalid.request");
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
        if (forwardTo == null) {
            forwardTo = "create-evaluation-request.jsp";
     	}       
    }
%>

<%@page import="org.wso2.carbon.identity.entitlement.ui.client.EntitlementServiceClient"%>
<%@page import="org.wso2.carbon.identity.entitlement.ui.dto.RequestDTO"%>
<%@page import="org.wso2.carbon.identity.entitlement.ui.dto.RowDTO" %>
<%@page import="org.wso2.carbon.identity.entitlement.ui.util.ClientUtil" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.owasp.encoder.Encode" %>
<script
	type="text/javascript">
    function forward() {
        location.href = "<%=Encode.forJavaScriptBlock(forwardTo)%>";
	}
</script>

<script type="text/javascript">
	forward();
</script>