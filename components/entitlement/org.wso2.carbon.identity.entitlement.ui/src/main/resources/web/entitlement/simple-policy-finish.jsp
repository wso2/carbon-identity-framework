<!--
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
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.identity.entitlement.common.EntitlementConstants" %>
<%@ page import="org.wso2.carbon.identity.entitlement.common.PolicyEditorException" %>
<%@ page import="org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO" %>
<%@ page
        import="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyCreator" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.PolicyEditorConstants" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.client.EntitlementPolicyAdminServiceClient" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.SimplePolicyEditorDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.SimplePolicyEditorElementDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.util.PolicyEditorUtil" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ResourceBundle" %>
<jsp:useBean id="entitlementPolicyBean"
             type="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean"
             class="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean" scope="session"/>
<jsp:setProperty name="entitlementPolicyBean" property="*"/>

<%

    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.
                    CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    String BUNDLE = "org.wso2.carbon.identity.entitlement.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

    int maxUserRow = 0;
    int maxResourceRow = 0;
    int maxActionRow = 0;
    int maxEnvironmentRow = 0;
    int maxRows = 0;
    String dynamicCategory = request.getParameter("policyApplied");
    String policyId = request.getParameter("policyId");
    String policyDescription = request.getParameter("policyDescription");
    String maxUserRowString = request.getParameter("maxUserRow");
    String maxResourceRowString = request.getParameter("maxResourceRow");
    String maxActionRowString = request.getParameter("maxActionRow");
    String maxEnvironmentRowString = request.getParameter("maxEnvironmentRow");
    if(maxResourceRowString != null){
        try{
            maxResourceRow = Integer.parseInt(maxResourceRowString);
        } catch(Exception e ){
            //ignore
        }
    }

    if(maxUserRowString != null){
        try{
            maxUserRow = Integer.parseInt(maxUserRowString);
        } catch(Exception e ){
            //ignore
        }
    }

    if(maxActionRowString != null){
        try{
            maxActionRow = Integer.parseInt(maxActionRowString);
        } catch(Exception e ){
            //ignore
        }
    }

    if(maxEnvironmentRowString != null){
        try{
            maxEnvironmentRow = Integer.parseInt(maxEnvironmentRowString);
        } catch(Exception e ){
            //ignore
        }
    }

    SimplePolicyEditorDTO editorDTO = null;
    String forwardTo = null;

    if(policyId != null && policyId.trim().length() > 0){

        editorDTO = new SimplePolicyEditorDTO();
        editorDTO.setAppliedCategory(dynamicCategory);
        editorDTO.setPolicyId(policyId);
        editorDTO.setDescription(policyDescription);

        List<SimplePolicyEditorElementDTO>  elementDTOs = new ArrayList<SimplePolicyEditorElementDTO>();

        if(PolicyEditorConstants.SOA_CATEGORY_RESOURCE.equals(dynamicCategory)){
            String resourceValue = request.getParameter("resourceValue");
            String function = request.getParameter("function");
            editorDTO.setResourceValue(resourceValue);
            editorDTO.setFunction(function);
            maxRows = maxResourceRow;
        } else if(PolicyEditorConstants.SOA_CATEGORY_USER.equals(dynamicCategory)){
            String userAttributeValue = request.getParameter("userAttributeValue");
            String userAttributeId = request.getParameter("userAttributeId");
            String function = request.getParameter("function");
            editorDTO.setUserAttributeId(userAttributeId);
            editorDTO.setUserAttributeValue(userAttributeValue);
            editorDTO.setFunction(function);
            maxRows = maxUserRow;
        } else if(PolicyEditorConstants.SOA_CATEGORY_ACTION.equals(dynamicCategory)){
            String actionValue = request.getParameter("actionValue");
            String function = request.getParameter("function");
            editorDTO.setActionValue(actionValue);
            editorDTO.setFunction(function);
            maxRows = maxActionRow;
        } else if(PolicyEditorConstants.SOA_CATEGORY_ENVIRONMENT.equals(dynamicCategory)){
            String actionValue = request.getParameter("environmentValue");
            String environmentId = request.getParameter("environmentId");
            editorDTO.setEnvironmentValue(actionValue);
            editorDTO.setEnvironmentId(environmentId);
            maxRows = maxEnvironmentRow;
        }

        for(int rowNumber = 0; rowNumber < maxRows + 1; rowNumber++){

            SimplePolicyEditorElementDTO elementDTO = new SimplePolicyEditorElementDTO();

            String userAttributeId = request.getParameter("userRuleAttributeId_" + rowNumber);
            if(userAttributeId != null && userAttributeId.trim().length() > 0){
                elementDTO.setUserAttributeId(userAttributeId);
            }

            String userAttributeValue = request.getParameter("userRuleAttributeValue_" + rowNumber);
            if(userAttributeValue != null && userAttributeValue.trim().length() > 0){
                elementDTO.setUserAttributeValue(userAttributeValue);
            } else {
                if(PolicyEditorConstants.SOA_CATEGORY_RESOURCE.equals(dynamicCategory)
                            || PolicyEditorConstants.SOA_CATEGORY_ACTION.equals(dynamicCategory)){
                    continue;    
                }
            }
            
            String actionValue = request.getParameter("actionRuleValue_" + rowNumber);
            if(actionValue != null && actionValue.trim().length() > 0){
                elementDTO.setActionValue(actionValue);
            }

            String resourceValue = request.getParameter("resourceRuleValue_" + rowNumber);
            if(resourceValue != null && resourceValue.trim().length() > 0){
                elementDTO.setResourceValue(resourceValue);
            } else {
                if(PolicyEditorConstants.SOA_CATEGORY_USER.equals(dynamicCategory)){
                    continue;
                }
            }

            String environmentId = request.getParameter("environmentRuleId_" + rowNumber);
            if(environmentId != null && environmentId.trim().length() > 0){
                elementDTO.setEnvironmentId(environmentId);
            }

            String environmentValue = request.getParameter("environmentRuleValue_" + rowNumber);
            if(environmentValue != null && environmentValue.trim().length() > 0){
                elementDTO.setEnvironmentValue(environmentValue);
            }

            String operationType = request.getParameter("operationRuleType_" + rowNumber);
            if(operationType != null && operationType.trim().length() > 0){
                elementDTO.setOperationType(operationType);
            }

            String resourceFunction = request.getParameter("resourceRuleFunction_" + rowNumber);
            if(resourceFunction != null && resourceFunction.trim().length() > 0){
                elementDTO.setFunctionOnResources(resourceFunction);
            }

            String userFunction = request.getParameter("userRuleFunction_" + rowNumber);
            if(userFunction != null && userFunction.trim().length() > 0){
                elementDTO.setFunctionOnUsers(userFunction);
            }

            String actionFunction = request.getParameter("actionRuleFunction_" + rowNumber);
            if(actionFunction != null && actionFunction.trim().length() > 0){
                elementDTO.setFunctionOnActions(actionFunction);
            }
            
            elementDTOs.add(elementDTO);
        }
        editorDTO.setSimplePolicyEditorElementDTOs(elementDTOs);
    }

    try {
        String message;
        EntitlementPolicyCreator creator = new EntitlementPolicyCreator();
        EntitlementPolicyAdminServiceClient client = new EntitlementPolicyAdminServiceClient(cookie,
                serverURL, configContext);
        PolicyDTO policyDTO = null;
        if(editorDTO != null){
            String[] policyEditorData = PolicyEditorUtil.createBasicPolicyData(editorDTO);
            String policy = creator.createSOAPolicy(editorDTO);
            if(entitlementPolicyBean.isEditPolicy()){
                try{
                    policyDTO = client.getPolicy(policyId, false);
                } catch (Exception e){
                    //ignore
                }

                if(policyDTO == null){
                    policyDTO = new PolicyDTO();
                }

                policyDTO.setPolicy(policy);
                policyDTO.setPolicyEditor(EntitlementConstants.PolicyEditor.RBAC);
                if(policyEditorData != null){
                    policyDTO.setPolicyEditorData(policyEditorData);
                }
                client.updatePolicy(policyDTO);
                message = resourceBundle.getString("updated.successfully");
            } else {
                policyDTO = new PolicyDTO();
                policyDTO.setPolicyId(policyId);
                policyDTO.setPolicy(policy);
                policyDTO.setPolicyEditor(EntitlementConstants.PolicyEditor.RBAC);
                if(policyEditorData != null){
                    policyDTO.setPolicyEditorData(policyEditorData);
                }
                client.addPolicy(policyDTO);
                message = resourceBundle.getString("ent.policy.added.successfully");
            }
            CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.INFO, request);
            forwardTo = "index.jsp?";
        } else {
            message = resourceBundle.getString("error.while.creating.policy");
            CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
            forwardTo = "index.jsp?";
        }
    } catch (PolicyEditorException e) {
        String message = resourceBundle.getString("error.while.creating.policy");
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
        forwardTo = "index.jsp?";
    } catch (Exception e) {
        String message = resourceBundle.getString("error.while.adding.policy") + " " + e.getMessage();
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
        forwardTo = "index.jsp?";
    }


%>

<script type="text/javascript">
    function forward() {
        location.href = "<%=forwardTo%>";
    }
</script>

<script type="text/javascript">
    forward();
</script>