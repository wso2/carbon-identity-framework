,<!--
/*
* Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
-->
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.identity.entitlement.stub.dto.AttributeDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyConstants" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.client.EntitlementPolicyAdminServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page
        import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ResourceBundle" %>
<jsp:useBean id="entitlementPolicyBean" type="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean"
             class="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean" scope="session"/>
<jsp:setProperty name="entitlementPolicyBean" property="*" />

<%

    String resourceNames = "";
     String resourceId = "";
     String resourceDataType = "";
     String subjectNames = "";
     String subjectId = "";
     String subjectDataType = "";
     String actionNames = "";
     String actionId = "";
     String actionDataType = "";
     String environmentNames = "";
     String environmentId = "";
     String environmentDataType = "";
     String [] results = null;
     String forwardTo;

     String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
     ConfigurationContext configContext =
             (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.
                     CONFIGURATION_CONTEXT);
     String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
     String BUNDLE = "org.wso2.carbon.identity.entitlement.ui.i18n.Resources";
     ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

     resourceNames = (String)session.getAttribute("resourceNames");
     resourceId = (String)session.getAttribute("resourceId");
     resourceDataType = (String)session.getAttribute("resourceDataType");

     subjectNames = (String)session.getAttribute("subjectNames");
     subjectId = (String)session.getAttribute("subjectId");
     subjectDataType = (String)session.getAttribute("subjectDataType");

     actionNames = (String)session.getAttribute("actionNames");
     actionId = (String)session.getAttribute("actionId");
     actionDataType = (String)session.getAttribute("actionDataType");

     environmentNames = (String)session.getAttribute("environmentNames");
     environmentId = (String)session.getAttribute("environmentId");
     environmentDataType = (String)session.getAttribute("environmentDataType");

     List<AttributeDTO> attributeValueDTOs = new ArrayList<AttributeDTO>();

     if(resourceNames != null && !"".equals(resourceNames)){
         AttributeDTO attributeValueDTO = new AttributeDTO();
         attributeValueDTO.setAttributeValue(resourceNames);
         attributeValueDTO.setAttributeType(EntitlementPolicyConstants.RESOURCE_ELEMENT);
         attributeValueDTO.setAttributeDataType(resourceDataType);
         attributeValueDTO.setAttributeId(resourceId);
         attributeValueDTOs.add(attributeValueDTO);
     }

     if(subjectNames != null && !"".equals(subjectNames)){
         AttributeDTO attributeValueDTO = new AttributeDTO();
         attributeValueDTO.setAttributeValue(subjectNames);
         attributeValueDTO.setAttributeType(EntitlementPolicyConstants.SUBJECT_ELEMENT);
         attributeValueDTO.setAttributeId(subjectId);
         attributeValueDTO.setAttributeDataType(subjectDataType);
         attributeValueDTOs.add(attributeValueDTO);
     }

     if(actionNames != null && !"".equals(actionNames)){
         AttributeDTO attributeValueDTO = new AttributeDTO();
         attributeValueDTO.setAttributeValue(actionNames);
         attributeValueDTO.setAttributeType(EntitlementPolicyConstants.ACTION_ELEMENT);
         attributeValueDTO.setAttributeId(actionId);
         attributeValueDTO.setAttributeDataType(actionDataType);
         attributeValueDTOs.add(attributeValueDTO);
     }

     if(environmentNames != null && !"".equals(environmentNames)){
         AttributeDTO attributeValueDTO = new AttributeDTO();
         attributeValueDTO.setAttributeValue(environmentNames);
         attributeValueDTO.setAttributeType(EntitlementPolicyConstants.ENVIRONMENT_ELEMENT);
         attributeValueDTO.setAttributeId(environmentId);
         attributeValueDTO.setAttributeDataType(environmentDataType);
         attributeValueDTOs.add(attributeValueDTO);
     }

    try {

        if(attributeValueDTOs.size() > 0){
            EntitlementPolicyAdminServiceClient client = new EntitlementPolicyAdminServiceClient(cookie,
                    serverURL, configContext);
            results = client.getAdvanceSearchResult(attributeValueDTOs.toArray(new AttributeDTO[attributeValueDTOs.size()]));
        }

    } catch (Exception e) {
    	String message = resourceBundle.getString("error.while.loading.policy.resource");
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
        forwardTo = "../admin/error.jsp";
    }

    forwardTo = "advance-search.jsp";

%>

<script type="text/javascript">
    function forward() {
        location.href = "<%=forwardTo%>";
    }
</script>

<script type="text/javascript">
    forward();
</script>
