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
<%@ page import="org.wso2.balana.utils.policy.dto.BasicRuleDTO" %>
<%@ page import="org.wso2.balana.utils.policy.dto.BasicTargetDTO" %>
<%@ page import="org.owasp.encoder.Encode" %>

<jsp:useBean id="entitlementPolicyBean" type="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean"
             class="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean" scope="session"/>
<jsp:setProperty name="entitlementPolicyBean" property="*" />

<%
    BasicRuleDTO basicRuleDTO = new BasicRuleDTO();
    BasicTargetDTO basicTargetDTO = new BasicTargetDTO();
    entitlementPolicyBean.setRuleElementOrder(null);

    String policyName = request.getParameter("policyName");
    String algorithmName = request.getParameter("algorithmName");
    String policyDescription = request.getParameter("policyDescription");

    String category = request.getParameter("category");
    String ruleElementOrder = request.getParameter("ruleElementOrder");
    String updateRule = request.getParameter("updateRule");
    String nextPage = request.getParameter("nextPage");
    String returnPage = request.getParameter("returnPage");
    // rules
    String ruleId = request.getParameter("ruleId");
    String ruleEffect = request.getParameter("ruleEffect");
    String ruleDescription = request.getParameter("ruleDescription");
    String completedRule = request.getParameter("completedRule");
    String editRule = request.getParameter("editRule");

    String resourceNames = request.getParameter("resourceNames");
    String functionOnResources = request.getParameter("functionOnResources");
    String resourceDataType = request.getParameter("resourceDataType");
    String preFunctionOnResources = request.getParameter("preFunctionOnResources");
    String resourceId = request.getParameter("resourceId");

    String subjectNames = request.getParameter("subjectNames");
    String functionOnSubjects = request.getParameter("functionOnSubjects");
    String subjectDataType = request.getParameter("subjectDataType");
    String subjectId = request.getParameter("subjectId");
    String preFunctionOnSubjects = request.getParameter("preFunctionOnSubjects");

    String actionNames = request.getParameter("actionNames");
    String functionOnActions = request.getParameter("functionOnActions");
    String actionDataType = request.getParameter("actionDataType");
    String actionId = request.getParameter("actionId");
    String preFunctionOnActions = request.getParameter("preFunctionOnActions");

    String environmentNames = request.getParameter("environmentNames");
    String functionOnEnvironment = request.getParameter("functionOnEnvironment");
    String environmentDataType = request.getParameter("environmentDataType");
    String environmentId = request.getParameter("environmentId");
    String preFunctionOnEnvironment = request.getParameter("preFunctionOnEnvironment");

    // targets
    String resourceNamesTarget = request.getParameter("resourceNamesTarget");
    String functionOnResourcesTarget = request.getParameter("functionOnResourcesTarget");
    String resourceDataTypeTarget = request.getParameter("resourceDataTypeTarget");
    String resourceIdTarget = request.getParameter("resourceIdTarget");
    String preFunctionOnResourcesTarget = request.getParameter("preFunctionOnResourcesTarget");

    String subjectNamesTarget = request.getParameter("subjectNamesTarget");
    String functionOnSubjectsTarget = request.getParameter("functionOnSubjectsTarget");
    String subjectDataTypeTarget = request.getParameter("subjectDataTypeTarget");
    String subjectIdTarget = request.getParameter("subjectIdTarget");
    String preFunctionOnSubjectsTarget = request.getParameter("preFunctionOnSubjectsTarget");

    String actionNamesTarget = request.getParameter("actionNamesTarget");
    String functionOnActionsTarget = request.getParameter("functionOnActionsTarget");
    String actionDataTypeTarget = request.getParameter("actionDataTypeTarget");
    String actionIdTarget = request.getParameter("actionIdTarget");
    String preFunctionOnActionsTarget = request.getParameter("preFunctionOnActionsTarget");

    String environmentNamesTarget = request.getParameter("environmentNamesTarget");
    String functionOnEnvironmentTarget = request.getParameter("functionOnEnvironmentTarget");
    String preFunctionOnEnvironmentTarget = request.getParameter("preFunctionOnEnvironmentTarget");
    String environmentDataTypeTarget = request.getParameter("environmentDataTypeTarget");
    String environmentIdTarget = request.getParameter("environmentIdTarget");

//    String attributeIdTarget = request.getParameter("attributeIdTarget");
//    String functionOnAttributesTarget = request.getParameter("functionOnAttributesTarget");
//    String userAttributeValueTarget = request.getParameter("userAttributeValueTarget");


    if(ruleId != null && ruleId.trim().length() > 0 && !ruleId.trim().equals("null") && editRule == null ) {

        basicRuleDTO.setRuleId(ruleId);
        basicRuleDTO.setRuleEffect(ruleEffect);

        if(ruleDescription != null && ruleDescription.trim().length() > 0 ){
            basicRuleDTO.setRuleDescription(ruleDescription);
        }

        if(resourceNames != null && !resourceNames.equals("")){
            basicRuleDTO.setResourceList(resourceNames);
        }

        if(functionOnResources != null && !functionOnResources.equals("")){
            basicRuleDTO.setFunctionOnResources(functionOnResources);
        }

        if(resourceDataType != null && resourceDataType.trim().length() > 0 &&
                                        !resourceDataType.trim().equals("null")){
            basicRuleDTO.setResourceDataType(resourceDataType);
        }

        if(resourceId != null && resourceId.trim().length() > 0 && !resourceId.trim().equals("null")){
            basicRuleDTO.setResourceId(resourceId);
        }

        if(preFunctionOnResources != null && preFunctionOnResources.trim().length() > 0){
            basicRuleDTO.setPreFunctionOnResources(preFunctionOnResources);
        }

        if(subjectNames != null && !subjectNames.equals("")){
            basicRuleDTO.setSubjectList(subjectNames);
        }

        if(subjectNames != null && !functionOnSubjects.equals("")){
            basicRuleDTO.setFunctionOnSubjects(functionOnSubjects);
        }

        if(subjectDataType != null && subjectDataType.trim().length() > 0 &&
                                                            !subjectDataType.trim().equals("null")) {
            basicRuleDTO.setSubjectDataType(subjectDataType);
        }

        if(subjectId != null && subjectId.trim().length() > 0 && !subjectId.trim().equals("null")){
            basicRuleDTO.setSubjectId(subjectId);
        }

        if(preFunctionOnSubjects != null && preFunctionOnSubjects.trim().length() > 0){
            basicRuleDTO.setPreFunctionOnSubjects(preFunctionOnSubjects);
        }

        if(actionNames != null && !actionNames.equals("")){
            basicRuleDTO.setActionList(actionNames);
        }

        if(functionOnActions != null && !functionOnActions.equals("")){
            basicRuleDTO.setFunctionOnActions(functionOnActions);
        }

        if(actionDataType != null && actionDataType.trim().length() > 0 &&
                                            !actionDataType.trim().equals("null")){
            basicRuleDTO.setActionDataType(actionDataType);
        }

        if(actionId != null && actionId.trim().length() > 0 && !actionId.trim().equals("null")){
            basicRuleDTO.setActionId(actionId);
        }

        if(preFunctionOnActions != null && preFunctionOnActions.trim().length() > 0){
            basicRuleDTO.setPreFunctionOnActions(preFunctionOnActions);
        }

        if(environmentNames != null && !environmentNames.equals("")){
            basicRuleDTO.setEnvironmentList(environmentNames);
        }

        if(functionOnEnvironment != null && !functionOnEnvironment.equals("")){
            basicRuleDTO.setFunctionOnEnvironment(functionOnEnvironment);
        }

        if(environmentDataType != null && environmentDataType.trim().length() > 0 && 
                                                !environmentDataType.trim().equals("null")){
            basicRuleDTO.setEnvironmentDataType(environmentDataType);
        }

        if(environmentId != null && environmentId.trim().length() > 0 &&
                                                !environmentId.trim().equals("null")){
            basicRuleDTO.setEnvironmentId(environmentId);
        }

        if(preFunctionOnEnvironment != null && preFunctionOnEnvironment.trim().length() > 0){
            basicRuleDTO.setPreFunctionOnEnvironment(preFunctionOnEnvironment);
        }

        if(completedRule != null && completedRule.equals("true")){
            basicRuleDTO.setCompletedRule(true);
        }

        entitlementPolicyBean.setBasicRuleElementDTOs(basicRuleDTO);
    }

    if(resourceNamesTarget != null && !resourceNamesTarget.equals("")){
        basicTargetDTO.setResourceList(resourceNamesTarget);
    }

    if(functionOnResourcesTarget != null && !functionOnResourcesTarget.equals("")){
        basicTargetDTO.setFunctionOnResources(functionOnResourcesTarget);
    }

    if(resourceDataTypeTarget != null && resourceDataTypeTarget.trim().length() > 0 &&
                                                    !resourceDataTypeTarget.trim().equals("null")){
        basicTargetDTO.setResourceDataType(resourceDataTypeTarget);
    }

    if(resourceIdTarget != null && resourceIdTarget.trim().length() > 0 &&
                                            !resourceIdTarget.trim().equals("null")){
        basicTargetDTO.setResourceId(resourceIdTarget);
    }

    if(subjectNamesTarget != null && !subjectNamesTarget.equals("")){
        basicTargetDTO.setSubjectList(subjectNamesTarget);
    }

    if(functionOnSubjectsTarget != null && !functionOnSubjectsTarget.equals("")){
        basicTargetDTO.setFunctionOnSubjects(functionOnSubjectsTarget);
    }

    if(subjectDataTypeTarget != null && subjectDataTypeTarget.trim().length() > 0 &&
                                                    !subjectDataTypeTarget.trim().equals("null")){
        basicTargetDTO.setSubjectDataType(subjectDataTypeTarget);
    }

    if(subjectIdTarget != null && subjectIdTarget.trim().length() > 0 &&
                                                    !subjectIdTarget.trim().equals("null")){
        basicTargetDTO.setSubjectId(subjectIdTarget);
    }

    if(actionNamesTarget != null && !actionNamesTarget.equals("")){
        basicTargetDTO.setActionList(actionNamesTarget);
    }

    if(functionOnActionsTarget != null && !functionOnActionsTarget.equals("")){
        basicTargetDTO.setFunctionOnActions(functionOnActionsTarget);
    }

    if(actionDataTypeTarget != null && actionDataTypeTarget.trim().length() > 0 &&
                                                !actionDataTypeTarget.trim().equals("null")){
        basicTargetDTO.setActionDataType(actionDataTypeTarget);
    }

    if(actionIdTarget != null && actionIdTarget.trim().length() > 0 &&
                                                !actionIdTarget.trim().equals("null")){
        basicTargetDTO.setActionId(actionIdTarget);
    }

    if(environmentNamesTarget != null && !environmentNamesTarget.equals("")){
        basicTargetDTO.setEnvironmentList(environmentNamesTarget);
    }

    if(functionOnEnvironmentTarget != null && !functionOnEnvironmentTarget.equals("")){
        basicTargetDTO.setFunctionOnEnvironment(functionOnEnvironmentTarget);
    }

    if(environmentDataTypeTarget != null && environmentDataTypeTarget.trim().length() > 0 &&
                                               !environmentDataTypeTarget.trim().equals("null")){
        basicTargetDTO.setEnvironmentDataType(environmentDataTypeTarget);
    }

    if(environmentIdTarget != null && environmentIdTarget.trim().length() > 0 &&
                                                !environmentIdTarget.trim().equals("null")){
        basicTargetDTO.setEnvironmentId(environmentIdTarget);
    }

    entitlementPolicyBean.setBasicTargetDTO(basicTargetDTO);
    
    String forwardTo;

    if(ruleElementOrder != null && ruleElementOrder.trim().length() > 0){
        if(basicRuleDTO.isCompletedRule() && !"true".equals(updateRule)){
            entitlementPolicyBean.setRuleElementOrder(ruleElementOrder.trim() + ", " +
                                                      basicRuleDTO.getRuleId());
        } else{
            entitlementPolicyBean.setRuleElementOrder(ruleElementOrder.trim());
        }
    }

    if(completedRule != null && completedRule.equals("true")){
        forwardTo = nextPage + ".jsp?";
    } else {
        forwardTo = nextPage + ".jsp?ruleId=" + Encode.forUriComponent(ruleId);
        if(category != null && category.trim().length() > 0){
            forwardTo = forwardTo + "&category=" + Encode.forUriComponent(category);
        }
        if(returnPage != null && returnPage.trim().length() > 0){
            forwardTo = forwardTo + "&returnPage=" + Encode.forUriComponent(returnPage);
        }
    }

%>

<script type="text/javascript">
    function forward() {
        location.href = "<%=Encode.forJavaScriptBlock(forwardTo)%>";
    }
</script>

<script type="text/javascript">
    forward();
</script>
