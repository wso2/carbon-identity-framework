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
<%@ page import="org.wso2.balana.utils.policy.dto.BasicRuleDTO" %>
<%@ page import="org.wso2.balana.utils.policy.dto.BasicTargetDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.common.EntitlementConstants" %>
<%@ page import="org.wso2.carbon.identity.entitlement.common.PolicyEditorEngine" %>
<%@ page import="org.wso2.carbon.identity.entitlement.common.dto.PolicyEditorDataHolder" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyConstants" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.PolicyEditorConstants" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Set" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<jsp:useBean id="entitlementPolicyBean" type="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean"
             class="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean" scope="session"/>
<jsp:setProperty name="entitlementPolicyBean" property="*" />

<%
    BasicRuleDTO basicRuleDTO = null;
    PolicyEditorDataHolder holder = PolicyEditorEngine.getInstance().
                                    getPolicyEditorData(EntitlementConstants.PolicyEditor.BASIC);
    Set<String> functionIds = holder.getRuleFunctions();
    Set<String> preFunctionIds = holder.getPreFunctionMap().keySet();
    Set<String> targetFunctionIds = holder.getTargetFunctions();
    Set<String> ruleEffects = holder.getRuleEffectMap().keySet();
    Set<String> subjectIds =  holder.getCategoryAttributeIdMap().get(PolicyEditorConstants.SOA_CATEGORY_SUBJECT);
    Set<String> environmentIds = holder.getCategoryAttributeIdMap().get(PolicyEditorConstants.SOA_CATEGORY_ENVIRONMENT);
    Set<String> algorithmNames = holder.getRuleCombiningAlgorithms().keySet();
    Set<String> availableCategories = holder.getCategoryMap().keySet();

    List<BasicRuleDTO> basicRuleDTOs = entitlementPolicyBean.getBasicRuleDTOs();
    BasicTargetDTO basicTargetDTO = entitlementPolicyBean.getBasicTargetDTO();

    String selectedAttributeDataType = request.getParameter("selectedAttributeDataType");
    String selectedAttributeId = request.getParameter("selectedAttributeId");
    String category = request.getParameter("category");

    String ruleId = request.getParameter("ruleId");
    if(ruleId != null && ruleId.trim().length() > 0 && !ruleId.trim().equals("null") ) {
        basicRuleDTO = entitlementPolicyBean.getBasicRuleElement(ruleId);
    }

    // Why null  TODO
    if("null".equals(selectedAttributeId)){
        selectedAttributeId = null;
    }

    if("null".equals(selectedAttributeDataType)){
        selectedAttributeDataType = null;
    }

    String selectedAttributeNames = "";

    String selectedSubjectNames = "";
    String selectedResourceNames = "";
    String selectedActionNames = "";
    String selectedEnvironmentNames = "";
    String selectedResourceId="";
    String selectedResourceDataType="";
    String selectedSubjectId="";
    String selectedSubjectDataType="";
    String selectedActionId="";
    String selectedActionDataType="";
    String selectedEnvironmentId="";
    String selectedEnvironmentDataType="";

    String resourceNames = "";
    String environmentNames = "";
    String subjectNames = "";
    String actionNames = "";
    String functionOnResources = "";
    String functionOnSubjects = "";
    String functionOnActions = "";
    String functionOnEnvironment = "";
    String preFunctionOnResources = "";
    String preFunctionOnSubjects = "";
    String preFunctionOnActions = "";
    String preFunctionOnEnvironment = "";
    String resourceDataType = "";
    String subjectDataType = "";
    String actionDataType = "";
    String environmentDataType = "";
    String resourceId= "";
    String subjectId = "";
    String actionId = "";
    String environmentId = "";
    String ruleDescription = "";
    String ruleEffect = "";

    String resourceNamesTarget = "";
    String environmentNamesTarget = "";
    String subjectNamesTarget = "";
    String actionNamesTarget = "";

    String functionOnResourcesTarget = "";
    String functionOnSubjectsTarget = "";
    String functionOnActionsTarget = "";
    String functionOnEnvironmentTarget = "";

    String preFunctionOnSubjectsTarget = "";
    String preFunctionOnActionsTarget = "";
    String preFunctionOnEnvironmentTarget = "";
    String preFunctionOnResourcesTarget = "";

    String resourceDataTypeTarget = "";
    String subjectDataTypeTarget = "";
    String actionDataTypeTarget = "";
    String environmentDataTypeTarget = "";

    String resourceIdTarget = "";
    String subjectIdTarget = "";
    String actionIdTarget = "";
    String environmentIdTarget = "";

    int noOfSelectedAttributes = 1;
    /**
     *  Get posted resources from jsp pages and put then in to a String object
     */
    while(true) {
        String attributeName  = request.getParameter("attributeValue" + noOfSelectedAttributes);
        if (attributeName == null || attributeName.trim().length() < 1) {
            break;
        }
        if(selectedAttributeNames.equals("")) {
            selectedAttributeNames = attributeName.trim();
        } else {
            selectedAttributeNames = selectedAttributeNames + "," + attributeName.trim();
        }
        noOfSelectedAttributes ++;
    }


    if(category != null){
        if (EntitlementPolicyConstants.RESOURCE_ELEMENT.equals(category)){
            selectedResourceNames = selectedAttributeNames;
            selectedResourceId = selectedAttributeId;
            selectedResourceDataType = selectedAttributeDataType;
        } else if (EntitlementPolicyConstants.SUBJECT_ELEMENT.equals(category)){
            selectedSubjectNames = selectedAttributeNames;
            selectedSubjectId = selectedAttributeId;
            selectedSubjectDataType = selectedAttributeDataType;
        } else if (EntitlementPolicyConstants.ACTION_ELEMENT.equals(category)){
            selectedActionNames = selectedAttributeNames;
            selectedActionId = selectedAttributeId;
            selectedActionDataType = selectedAttributeDataType;
        } else if (EntitlementPolicyConstants.ENVIRONMENT_ELEMENT.equals(category)){
            selectedEnvironmentNames = selectedAttributeNames;
            selectedEnvironmentId = selectedAttributeId;
            selectedEnvironmentDataType = selectedAttributeDataType;
        }
    }
    /**
     * Assign current BasicRule Object Values to variables to show on UI
     */
    if(basicRuleDTO != null){

        ruleEffect = basicRuleDTO.getRuleEffect();
        ruleId = basicRuleDTO.getRuleId();
        ruleDescription = basicRuleDTO.getRuleDescription();

        resourceNames =  basicRuleDTO.getResourceList();
        subjectNames = basicRuleDTO.getSubjectList();
        actionNames = basicRuleDTO.getActionList();
        environmentNames = basicRuleDTO.getEnvironmentList();

        functionOnActions = basicRuleDTO.getFunctionOnActions();
        functionOnResources = basicRuleDTO.getFunctionOnResources();
        functionOnSubjects = basicRuleDTO.getFunctionOnSubjects();
        functionOnEnvironment = basicRuleDTO.getFunctionOnEnvironment();

        preFunctionOnActions = basicRuleDTO.getPreFunctionOnActions();
        preFunctionOnResources = basicRuleDTO.getPreFunctionOnResources();
        preFunctionOnSubjects = basicRuleDTO.getPreFunctionOnSubjects();
        preFunctionOnEnvironment = basicRuleDTO.getPreFunctionOnEnvironment();

        if(selectedResourceDataType != null && selectedResourceDataType.trim().length() > 0){
            resourceDataType = selectedResourceDataType;
        } else {
            resourceDataType = basicRuleDTO.getResourceDataType();
        }

        if(selectedSubjectDataType != null && selectedSubjectDataType.trim().length() > 0){
            subjectDataType = selectedSubjectDataType;
        } else {
            subjectDataType = basicRuleDTO.getSubjectDataType();
        }

        if(selectedActionDataType != null && selectedActionDataType.trim().length() > 0){
            actionDataType = selectedActionDataType;
        } else {
            actionDataType = basicRuleDTO.getActionDataType();
        }

        if(selectedEnvironmentDataType != null && selectedEnvironmentDataType.trim().length() > 0){
            environmentDataType = selectedEnvironmentDataType;
        } else {
            environmentDataType = basicRuleDTO.getEnvironmentDataType();
        }

        if(selectedResourceId != null && selectedResourceId.trim().length() > 0){
            resourceId = selectedResourceId;
        } else {
            resourceId = basicRuleDTO.getResourceId();
        }

        if(selectedSubjectId != null && selectedSubjectId.trim().length() > 0){
            subjectId = selectedSubjectId;
        } else {
            subjectId = basicRuleDTO.getSubjectId();
        }

        if(selectedActionId != null && selectedActionId.trim().length() > 0){
            actionId = selectedActionId;
        } else {
            actionId = basicRuleDTO.getActionId();
        }

        if(selectedEnvironmentId != null && selectedEnvironmentId.trim().length() > 0){
            environmentId = selectedEnvironmentId;
        } else {
            environmentId = basicRuleDTO.getEnvironmentId();
        }

        if(selectedResourceNames != null && selectedResourceNames.trim().length() > 0){
            if(resourceNames != null && resourceNames.trim().length() > 0){
                resourceNames = resourceNames + ","  + selectedResourceNames;
            } else {
                resourceNames = selectedResourceNames;
            }
        }

        if(selectedSubjectNames != null && selectedSubjectNames.trim().length() > 0){
            if(subjectNames != null && subjectNames.trim().length() > 0){
                subjectNames = subjectNames + ","  + selectedSubjectNames;
            } else {
                subjectNames = selectedSubjectNames;
            }
        }

        if(selectedActionNames != null && selectedActionNames.trim().length() > 0){
            if(actionNames != null && actionNames.trim().length() > 0){
                actionNames = actionNames + ","  + selectedActionNames;
            } else {
                actionNames = selectedActionNames;
            }
        }

        if(selectedEnvironmentNames != null && selectedEnvironmentNames.trim().length() > 0){
            if(environmentNames != null && environmentNames.trim().length() > 0){
                environmentNames = environmentNames + ","  + selectedEnvironmentNames;
            } else {
                environmentNames = selectedEnvironmentNames;
            }
        }

    }

    /**
     * Assign current BasicTarget Object Values to variables to show on UI.
     */
    if(basicTargetDTO != null){

        resourceNamesTarget =  basicTargetDTO.getResourceList();
        subjectNamesTarget = basicTargetDTO.getSubjectList();
        actionNamesTarget = basicTargetDTO.getActionList();
        environmentNamesTarget = basicTargetDTO.getEnvironmentList();

        functionOnActionsTarget = basicTargetDTO.getFunctionOnActions();
        functionOnResourcesTarget = basicTargetDTO.getFunctionOnResources();
        functionOnSubjectsTarget = basicTargetDTO.getFunctionOnSubjects();
        functionOnEnvironmentTarget = basicTargetDTO.getFunctionOnEnvironment();

        resourceDataTypeTarget  = basicTargetDTO.getResourceDataType();
        subjectDataTypeTarget  = basicTargetDTO.getSubjectDataType();
        actionDataTypeTarget  = basicTargetDTO.getActionDataType();
        environmentDataTypeTarget  = basicTargetDTO.getEnvironmentDataType();

        resourceIdTarget = basicTargetDTO.getResourceId();
        subjectIdTarget = basicTargetDTO.getSubjectId();
        actionIdTarget = basicTargetDTO.getActionId();
        environmentIdTarget = basicTargetDTO.getEnvironmentId();

        if(basicRuleDTO == null) {
            if(selectedResourceNames != null && selectedResourceNames.trim().length() > 0){
                if(resourceNamesTarget != null && resourceNamesTarget.trim().length() > 0){
                    resourceNamesTarget = resourceNamesTarget + ","  + selectedResourceNames;
                } else {
                    resourceNamesTarget = selectedResourceNames;
                }
            }

            if(selectedSubjectNames != null && selectedSubjectNames.trim().length() > 0){
                if(subjectNamesTarget != null && subjectNamesTarget.trim().length() > 0){
                    subjectNamesTarget = subjectNamesTarget + ","  + selectedSubjectNames;
                } else {
                    subjectNamesTarget = selectedSubjectNames;
                }
            }

            if(selectedActionNames != null && selectedActionNames.trim().length() > 0){
                if(actionNamesTarget != null && actionNamesTarget.trim().length() > 0){
                    actionNamesTarget = actionNamesTarget + ","  + selectedActionNames;
                } else {
                    actionNamesTarget = selectedActionNames;
                }
            }

            if(selectedEnvironmentNames != null && selectedEnvironmentNames.trim().length() > 0){
                if(environmentNamesTarget != null && environmentNamesTarget.trim().length() > 0){
                    environmentNamesTarget = environmentNamesTarget + ","  + selectedEnvironmentNames;
                } else {
                    environmentNamesTarget = selectedEnvironmentNames;
                }
            }

            if(selectedResourceDataType != null && selectedResourceDataType.trim().length() > 0){
                resourceDataTypeTarget = selectedResourceDataType;
            }

            if(selectedSubjectDataType != null && selectedSubjectDataType.trim().length() > 0){
                subjectDataTypeTarget  = selectedSubjectDataType;
            }

            if(selectedActionDataType != null && selectedActionDataType.trim().length() > 0){
                actionDataTypeTarget  = selectedActionDataType;
            }

            if(selectedEnvironmentDataType != null && selectedEnvironmentDataType.trim().length() > 0){
                environmentDataTypeTarget  = selectedEnvironmentDataType;
            }

            if(selectedResourceId != null && selectedResourceId.trim().length() > 0){
                resourceIdTarget = selectedResourceId;
            }

            if(selectedSubjectId != null && selectedSubjectId.trim().length() > 0){
                subjectIdTarget = selectedSubjectId;
            }

            if(selectedActionId != null && selectedActionId.trim().length() > 0){
                actionIdTarget = selectedActionId;
            }

            if(selectedEnvironmentId != null && selectedEnvironmentId.trim().length() > 0){
                environmentIdTarget = selectedEnvironmentId;
            }
        }
    }

%>



<fmt:bundle basename="org.wso2.carbon.identity.entitlement.ui.i18n.Resources">
<% if(entitlementPolicyBean.isEditPolicy()){%>
<carbon:breadcrumb
        label="edit.xacml.policy"
        resourceBundle="org.wso2.carbon.identity.entitlement.ui.i18n.Resources"
        topPage="true"
        request="<%=request%>"/>
<% } else { %>
<carbon:breadcrumb
        label="create.policy"
        resourceBundle="org.wso2.carbon.identity.entitlement.ui.i18n.Resources"
        topPage="true"
        request="<%=request%>"/>
<%}%>

<script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
<script type="text/javascript" src="resources/js/main.js"></script>
<!--Yahoo includes for dom event handling-->
<script src="../yui/build/yahoo-dom-event/yahoo-dom-event.js" type="text/javascript"></script>
<script src="../entitlement/js/create-basic-policy.js" type="text/javascript"></script>
<link href="../entitlement/css/entitlement.css" rel="stylesheet" type="text/css" media="all"/>
<style>
.leftCol-vsmall{
	width:90px;
}
.text-box-mid-big{
	width:220px;
}
</style>
<script type="text/javascript">

    var regString = /^[a-zA-Z0-9._:-]{3,50}$/;    // TODO make this configurable

    function preSubmit(){

        var ruleElementOrder = new Array();
        var tmp = jQuery("#dataTable tbody tr input");
        for (var j = 0; j < tmp.length; j++) {
            ruleElementOrder.push(tmp[j].value);
        }
        jQuery('#mainTable > tbody:last').append('<tr><td><input type="hidden" name="ruleElementOrder" id="ruleElementOrder" value="' + ruleElementOrder +'"/></td></tr>') ;
    }

    function submitForm() {
        if(doValidationPolicyNameOnly()){
            document.getElementsByName("ruleId")[0].value = "";
            preSubmit();
            document.dataForm.action = "basic-policy-update.jsp?action=completePolicy";
            document.dataForm.submit();
        }
    }

    function doCancel() {
        location.href  = 'index.jsp';
    }

    function doValidation() {

        var value = document.getElementsByName("policyName")[0].value;
        if (value == '') {
            CARBON.showWarningDialog('<fmt:message key="policy.name.is.required"/>');
            return false;
        }

        var ruleValue = document.getElementsByName("ruleId")[0].value;
        if (ruleValue == '') {
            CARBON.showWarningDialog('<fmt:message key="rule.id.is.required"/>');
            return false;
        }

        var tmp = jQuery("#dataTable tbody tr input");
        for (var j = 0; j < tmp.length; j++) {
            if((tmp[j].value == ruleValue) && (ruleValue != "<%=ruleId%>")){
                CARBON.showWarningDialog('<fmt:message key="rule.id.is.existing"/>');
                return false;
            }
        }

        if(!ruleValue.match(new RegExp(regString))) {
            CARBON.showWarningDialog('<fmt:message key="rule.id.is.not.conformance"/>');
            return false;
        }

        return true;
    }

    function doValidationPolicyNameOnly() {

        var value = document.getElementsByName("policyName")[0].value;
        if (value == '') {
            CARBON.showWarningDialog('<fmt:message key="policy.name.is.required"/>');
            return false;
        }

        if (!value.match(new RegExp(regString))) {
            if (value.match(new RegExp("\\s", "g"))) {
                CARBON.showWarningDialog('<fmt:message key="policy.name.with.space"/>');
                return false;
            }
            else if (value.match(new RegExp("\\W", "g"))) {
                CARBON.showWarningDialog('<fmt:message key="policy.name.with.special-character"/>');
                return false;
            }
            else {
                CARBON.showWarningDialog('<fmt:message key="policy.name.is.conformance"/>');
                return false;
            }
        }
        return true;
    }

    function doUpdate(){
        if(doValidation()){
            preSubmit();
            document.dataForm.action = "basic-policy-update.jsp?action=updateRule&completedRule=true&updateRule=true";
            document.dataForm.submit();
        }
    }

    function doCancelRule(){
        preSubmit();
        document.dataForm.action = "basic-policy-update.jsp?action=cancelRule&ruleId=";
        document.dataForm.submit();
    }

    function deleteRule(ruleId) {
        preSubmit();
        document.dataForm.action = "basic-policy-update.jsp?action=deleteRule&ruleId=" + ruleId;
        document.dataForm.submit();
    }

    function editRule(ruleId){
        preSubmit();
        document.dataForm.action = "basic-policy-update.jsp?action=editRule&editRule=true&ruleId=" + ruleId;
        document.dataForm.submit();
    }

    function doAdd() {
        if(doValidation()){
            preSubmit();
            document.dataForm.action = "basic-policy-update.jsp?action=addRule&completedRule=true";
            document.dataForm.submit();
        }
    }

    function selectAttributes(attributeType){
        if(doValidationPolicyNameOnly()){
            preSubmit();
            document.dataForm.action =
                    "basic-policy-update.jsp?action=selectAttributes&updateRule=true&category=" + attributeType;
            document.dataForm.submit();
        }
    }


    function selectAttributesForTarget(attributeType){
        if(doValidationPolicyNameOnly()){
            preSubmit();
            document.dataForm.action = "basic-policy-update.jsp?action=selectAttributes&ruleId=&attributeType=" + attributeType;
            document.dataForm.submit();
        }
    }


    function updownthis(thislink,updown){
        var sampleTable = document.getElementById('dataTable');
        var clickedRow = thislink.parentNode.parentNode;
        var addition = -1;
        if(updown == "down"){
            addition = 1;
        }
        var otherRow = sampleTable.rows[clickedRow.rowIndex + addition];
        var numrows = jQuery("#dataTable tbody tr").length;
        if(numrows <= 1){
            return;
        }
        if(clickedRow.rowIndex == 1 && updown == "up"){
            return;
        } else if(clickedRow.rowIndex == numrows && updown == "down"){
            return;
        }
        var rowdata_clicked = new Array();
        for(var i=0;i<clickedRow.cells.length;i++){
            rowdata_clicked.push(clickedRow.cells[i].innerHTML);
            clickedRow.cells[i].innerHTML = otherRow.cells[i].innerHTML;
        }
        for(i=0;i<otherRow.cells.length;i++){
            otherRow.cells[i].innerHTML =rowdata_clicked[i];
        }
    }

    function getCategoryType() {
        document.dataForm.submit();
    }

    jQuery(document).ready(function() {

        <%if(basicRuleDTO == null){%>
        jQuery("#newRuleLinkRow").hide();
        <%}else{ %>
        jQuery("#newRuleLinkRow").show();
        <% } %>
        <%if(basicTargetDTO == null){%>
        jQuery("#newTargetLinkRow").hide();
        <%}else{ %>
        jQuery("#newTargetLinkRow").show();
        <% } %>

        jQuery("h2.trigger").click(function() {
            if (jQuery(this).next().is(":visible")) {
                this.className = "active trigger";
            } else {
                this.className = "trigger";
            }

            jQuery(this).next().slideToggle("fast");
            return false; //Prevent the browser jump to the link anchor
        });
    });

</script>



<div id="middle">
<%if(entitlementPolicyBean.isEditPolicy()){%>
<h2><fmt:message key="edit.xacml.policy"/></h2>
<%} else {%><h2><fmt:message key="create.policy"/></h2><%}%>
<div id="workArea">
<form id="dataForm" name="dataForm" method="post" action="">
<table  id="mainTable"  class="styledLeft noBorders">

    <tr>
        <td class="leftCol-med"><fmt:message key='policy.name'/><span class="required">*</span></td>
        <%
            if(entitlementPolicyBean.getPolicyName() != null) {
        %>
        <td><input type="text" name="policyName" id="policyName" value="<%=Encode.forHtmlAttribute(entitlementPolicyBean.getPolicyName())%>" class="text-box-big"/></td>
        <%
        } else {
        %>
        <td><input type="text" name="policyName" id="policyName" class="text-box-big"/></td>
        <%
            }
        %>
    </tr>

    <%
        if(holder.isShowRuleAlgorithms() && algorithmNames != null){
    %>
    <tr>
        <td><fmt:message key="rule.combining.algorithm"/></td>
        <td>
            <select id="algorithmName" name="algorithmName" class="text-box-big">
                <%
                    for (String algorithmName : algorithmNames) {
                        if(algorithmName.equals(entitlementPolicyBean.getAlgorithmName())){
                %>
                <option value="<%=Encode.forHtmlAttribute(algorithmName)%>" selected="selected"><%=Encode.forHtmlContent(entitlementPolicyBean.getAlgorithmName())%></option>
                <%
                } else {
                %>
                <option value="<%=Encode.forHtmlAttribute(algorithmName)%>"><%=Encode.forHtmlContent(algorithmName)%></option>
                <%
                        }
                    }
                %>
            </select>
        </td>
    </tr>
    <%
        }
    %>
    <%
        if(holder.isShowPolicyDescription()){
    %>
    <tr>
        <td class="leftCol-small" style="vertical-align:top !important"><fmt:message key='policy.description'/></td>
        <%
            if(entitlementPolicyBean.getPolicyDescription() != null) {
        %>
        <td><textarea name="policyDescription" id="policyDescription" value="<%=Encode.forHtmlAttribute(entitlementPolicyBean.getPolicyDescription())%>" class="text-box-big">
            <%=Encode.forHtmlContent(entitlementPolicyBean.getPolicyDescription())%></textarea></td>
        <%
        } else {
        %>
        <td><textarea type="text" name="policyDescription" id="policyDescription" class="text-box-big"></textarea></td>
        <%
            }
        %>
    </tr>
    <%
        }
    %>


    <tr>
    <td colspan="2" style="margin-top:10px;">
    <h2 class="trigger  <%if(basicTargetDTO == null){%>active<%} %>"><a href="#"><fmt:message
            key="policy.apply.to"/></a></h2>
    <div class="toggle_container" id="newTargetLinkRow">
    <table id="targetTable" class="normal" style="padding-left:0px !important">
    <tr>
        <td><fmt:message key='resource.names'/></td>
        <td>
        <table class="normal" style="padding-left:0px !important">
        <tr>
        <td style="padding-left:0px !important;padding-right:0px !important">
            <select id="functionOnResourcesTarget" name="functionOnResourcesTarget" class="leftCol-small">
                <%
                    if(targetFunctionIds != null){
                        for (String functionId : targetFunctionIds) {
                            if (functionId.equals(functionOnResourcesTarget)) {
                %>
                <option value="<%=Encode.forHtmlAttribute(functionId)%>" selected="selected"><%=Encode.forHtmlContent(functionId)%>
                </option>
                <%
                } else {
                %>
                <option value="<%=Encode.forHtmlAttribute(functionId)%>"><%=Encode.forHtmlContent(functionId)%>
                </option>
                <%
                            }
                        }
                    }
                %>
            </select>
        </td>
        <td style="padding-left:0px !important;padding-right:0px !important">
            <%
                if (resourceNamesTarget != null && resourceNamesTarget.trim().length() > 0) {

            %>
            <input type="text" size="60" name="resourceNamesTarget" id="resourceNamesTarget"
                   value="<%=Encode.forHtmlAttribute(resourceNamesTarget)%>" class="text-box-big"/>
            <%
            } else {
            %>
            <input type="text" size="60" name="resourceNamesTarget" id="resourceNamesTarget"
                   class="text-box-big"/>

            <%
                }
            %>
        </td>
        <td>
            <a title="Select Resources Names" class='icon-link' onclick='selectAttributes("Resource");'
               style='background-image:url(images/registry.gif); float:right;'></a>
        </td>
        <td>
            <input type="hidden" name="resourceIdTarget" id="resourceIdTarget" value="<%=Encode.forHtmlContent(resourceIdTarget)%>" />
        </td>
        </tr>
        </table>
        </td>
    </tr>
    <tr>
        <td><fmt:message key='roles.users'/></td>
        <td>
        <table class="normal" style="padding-left:0px !important">
        <tr>
        <td style="padding-left:0px !important;padding-right:0px !important">
            <select id="subjectIdTarget" name="subjectIdTarget" class="leftCol-small">
                <%
                    if(subjectIds != null){
                        for (String id : subjectIds) {
                            if (id.equals(subjectIdTarget)) {
                %>
                <option value="<%=Encode.forHtmlAttribute(id)%>" selected="selected"><%=Encode.forHtmlContent(id)%></option>
                <%
                        } else {
                %>
                <option value="<%=Encode.forHtmlAttribute(id)%>"><%=Encode.forHtmlContent(id)%></option>
                <%
                            }
                        }
                    }
                %>
            </select>
        </td>
        <td style="padding-left:0px !important;padding-right:0px !important">
            <select id="functionOnSubjectsTarget" name="functionOnSubjectsTarget" class="leftCol-small">
                <%
                    if(targetFunctionIds != null){
                    for (String functionId : targetFunctionIds) {
                        if (functionId.equals(functionOnSubjectsTarget)) {
                %>
                <option value="<%=Encode.forHtmlAttribute(functionId)%>" selected="selected"><%=Encode.forHtmlContent(functionId)%>
                </option>
                <%
                } else {
                %>
                <option value="<%=Encode.forHtmlAttribute(functionId)%>"><%=Encode.forHtmlContent(functionId)%>
                </option>
                <%
                        }
                    }
                    }
                %>
            </select>
        </td>
        <td style="padding-left:0px !important;padding-right:0px !important">
            <%
                if (subjectNamesTarget != null && subjectNamesTarget.trim().length() > 0) {
            %>
            <input type="text" name="subjectNamesTarget" id="subjectNamesTarget"
                   value="<%=Encode.forHtmlAttribute(subjectNamesTarget)%>" class="text-box-mid-big"/>
            <%
                } else {
            %>
            <input type="text" name="subjectNamesTarget" id="subjectNamesTarget" class="text-box-mid-big"/>
            <%
                }
            %>
        </td>
        <td>
            <a title="Select Subject Names" class='icon-link' onclick='selectAttributes("Subject");'
               style='background-image:url(images/user-store.gif); float:right;'></a>
        </td>
        </tr>
        </table>
        </td>
    </tr>
    <tr>
        <td><fmt:message key='action.names'/></td>
        <td>
        <table class="normal" style="padding-left:0px !important">
        <tr>
        <td style="padding-left:0px !important;padding-right:0px !important">
            <select id="functionOnActionsTarget" name="functionOnActionsTarget" class="leftCol-small">
                <%
                    if(targetFunctionIds != null){
                    for (String functionId : targetFunctionIds) {
                        if (functionId.equals(functionOnActionsTarget)) {
                %>
                <option value="<%=Encode.forHtmlAttribute(functionId)%>" selected="selected"><%=Encode.forHtmlContent(functionId)%>
                </option>
                <%
                } else {
                %>
                <option value="<%=Encode.forHtmlAttribute(functionId)%>"><%=Encode.forHtmlContent(functionId)%>
                </option>
                <%
                        }
                    }
                    }
                %>
            </select>
        </td>
        <td style="padding-left:0px !important;padding-right:0px !important">
            <%
                if (actionNamesTarget != null && actionNamesTarget.trim().length() > 0) {

            %>
            <input type="text" name="actionNamesTarget" id="actionNamesTarget" value="<%=Encode.forHtmlAttribute(actionNamesTarget)%>"
                   class="text-box-big"/>
            <%
            } else {
            %>
            <input type="text" name="actionNamesTarget" id="actionNamesTarget" class="text-box-big"/>

            <%
                }
            %>
        </td>
        <td>
            <a title="Select Action Names" class='icon-link' onclick='selectAttributes("Action");'
               style='background-image:url(images/actions.png); float:right;'></a>
        </td>

        <td>
            <input type="hidden" name="actionIdTarget" id="actionIdTarget"
                   value="<%=Encode.forHtmlAttribute(actionIdTarget)%>"/>
        </td>
        </tr>
        </table>
        </td>
    </tr>

    <tr>
        <td><fmt:message key='environment.names'/></td>
        <td>
        <table class="normal" style="padding-left:0px !important">
        <tr>
        <td style="padding-left:0px !important;padding-right:0px !important">
            <select id="environmentIdTarget" name="environmentIdTarget" class="leftCol-small">
                <%
                    if(environmentIds != null){
                    for (String id : environmentIds) {
                        if (id.equals(environmentIdTarget)) {
                %>
                <option value="<%=Encode.forHtmlAttribute(id)%>" selected="selected"><%=Encode.forHtmlContent(id)%></option>
                <%
                } else {
                %>
                <option value="<%=Encode.forHtmlAttribute(id)%>"><%=Encode.forHtmlContent(id)%></option>
                <%
                        }
                    }
                    }
                %>
            </select>
        </td>
        <td style="padding-left:0px !important;padding-right:0px !important">
            <select id="functionOnEnvironmentTarget" name="functionOnEnvironmentTarget" class="leftCol-small">
                <%
                    if(targetFunctionIds != null){
                    for (String functionId : targetFunctionIds) {
                        if (functionId.equals(functionOnEnvironmentTarget)) {
                %>
                <option value="<%=Encode.forHtmlAttribute(functionId)%>" selected="selected"><%=Encode.forHtmlContent(functionId)%>
                </option>
                <%
                } else {
                %>
                <option value="<%=Encode.forHtmlAttribute(functionId)%>"><%=Encode.forHtmlContent(functionId)%>
                </option>
                <%
                        }
                    }
                    }
                %>
            </select>
        </td>
        <td style="padding-left:0px !important;padding-right:0px !important">
            <%
                if (environmentNamesTarget != null && environmentNamesTarget.trim().length() > 0) {

            %>
            <input type="text" name="environmentNamesTarget" id="environmentNamesTarget"
                   value="<%=Encode.forHtmlAttribute(environmentNamesTarget)%>" class="text-box-mid-big"/>
            <%
            } else {
            %>
            <input type="text" name="environmentNamesTarget" id="environmentNamesTarget" class="text-box-mid-big"/>

            <%
                }
            %>
        </td>
        <td>
            <a title="Select Environment Names" class='icon-link' onclick='selectAttributes("Environment");'
               style='background-image:url(images/calendar.jpg); float:right;'></a>
        </td>
        </tr>
        </table>
        </td>
    </tr>
    </table>
    </div>
    </td>
    </tr>

    <tr>
    <td colspan="2" style="margin-top:10px;">
    <h2 class="trigger  <%if(basicRuleDTO == null){%>active<%} %>"><a href="#"><fmt:message
            key="add.new.entitlement.rule"/></a></h2>
    <div class="toggle_container" id="newRuleLinkRow">

    <table id="ruleTable" class="normal" style="padding-left:0px !important">
        <tr>
        <td>
        <table class="normal" cellspacing="0">
        <%
            if(holder.isShowRuleId()){
        %>
            <tr>
                <td class="leftCol-small"><fmt:message key='rule.name'/><span class="required">*</span>
                </td>
                <td>
                    <%
                        if (ruleId != null && ruleId.trim().length() > 0 && !ruleId.trim().equals("null")) {
                    %>
                    <input type="text" name="ruleId" id="ruleId" class="text-box-big"
                           value="<%=Encode.forHtmlAttribute(basicRuleDTO.getRuleId())%>"/>
                    <%
                    } else {
                    %>
                    <input type="text" name="ruleId" id="ruleId" class="text-box-big"/>
                    <%
                        }
                    %>
                </td>
            </tr>
        <%
            }
        %>

        <%
            if(holder.isShowRuleEffect()){
        %>
            <tr>
                <td><fmt:message key="rule.effect"/></td>
                <td>
                    <select id="ruleEffect" name="ruleEffect" class="leftCol-vsmall">
                        <%
                            if (ruleEffects != null) {
                                for (String effect : ruleEffects) {
                                    if (effect.equals(ruleEffect)) {

                        %>
                        <option value="<%=Encode.forHtmlAttribute(effect)%>" selected="selected"><%=Encode.forHtmlContent(ruleEffect)%>
                        </option>
                        <%
                        } else {

                        %>
                        <option value="<%=Encode.forHtmlAttribute(effect)%>"><%=Encode.forHtmlContent(effect)%>
                        </option>
                        <%
                                    }
                                }
                            }
                        %>
                    </select>
                </td>
            </tr>
        <%
            }
        %>

        <%
            if(holder.isShowRuleDescription()){
        %>
        <tr>
            <td class="leftCol-vsmall" style="vertical-align:top !important"><fmt:message key='policy.description'/></td>
            <%
                if(ruleDescription != null) {
            %>
            <td><input name="ruleDescription" id="ruleDescription" value="<%=Encode.forHtmlAttribute(ruleDescription)%>" class="text-box-big"/></td>
            <%
            } else {
            %>
            <td><input type="text" name="ruleDescription" id="ruleDescription" class="text-box-big" /></td>
            <%
                }
            %>
        </tr>
        <%
            }
        %>

            <tr>
            <td><fmt:message key='resource.names'/></td>
            <td>
            <table class="normal" style="padding-left:0px !important">
                <tr>
                    <td style="padding-left:0px !important;padding-right:0px !important">
                        <select id="preFunctionOnResources" name="preFunctionOnResources" class="leftCol-vsmall">
                            <%
                                if(preFunctionIds != null){
                                for (String preFunctionId : preFunctionIds) {
                                    if (preFunctionId.equals(preFunctionOnResources)) {
                            %>
                            <option value="<%=Encode.forHtmlAttribute(preFunctionId)%>" selected="selected"><%=Encode.forHtmlContent(preFunctionId)%>
                            </option>
                            <%
                                    } else {
                            %>
                            <option value="<%=Encode.forHtmlAttribute(preFunctionId)%>"><%=Encode.forHtmlContent(preFunctionId)%>
                            </option>
                            <%
                                    }
                                }
                                }
                            %>
                        </select>
                    </td>
                    <td style="padding-left:0px !important;padding-right:0px !important">
                        <select id="functionOnResources" name="functionOnResources" class="leftCol-small">
                            <%
                                if(functionIds != null){
                                for (String functionId : functionIds) {
                                    if (functionId.equals(functionOnResources)) {
                            %>
                            <option value="<%=Encode.forHtmlAttribute(functionId)%>" selected="selected"><%=Encode.forHtmlContent(functionOnResources)%>
                            </option>
                            <%
                            } else {
                            %>
                            <option value="<%=Encode.forHtmlAttribute(functionId)%>"><%=Encode.forHtmlContent(functionId)%>
                            </option>
                            <%
                                    }
                                }
                                }
                            %>
                        </select>
                    </td>
                    <td style="padding-left:0px !important;padding-right:0px !important">
                        <%
                            if (resourceNames != null && !resourceNames.equals("")) {

                        %>
                        <input type="text" size="60" name="resourceNames" id="resourceNames"
                               value="<%=Encode.forHtmlAttribute(resourceNames)%>" class="text-box-big"/>
                        <%
                        } else {
                        %>
                        <input type="text" size="60" name="resourceNames" id="resourceNames"
                               class="text-box-big"/>

                        <%
                            }
                        %>
                    </td>
                    <td>
                        <a title="Select Resources Names" class='icon-link' onclick='selectAttributes("Resource");'
                           style='background-image:url(images/registry.gif); float:right;'></a>
                    </td>
                    <td>
                        <input type="hidden" name="resourceId" id="resourceId" value="<%=Encode.forHtmlAttribute(resourceId)%>" />
                    </td>

                    <td>
                        <input type="hidden" name="resourceDataType" id="resourceDataType" value="<%=Encode.forHtmlAttribute(resourceDataType)%>" />
                    </td>
                </tr>
            </table>
            </td>
            </tr>

            <tr>
            <td class="leftCol-small"><fmt:message key='roles.users'/></td>
            <td>
            <table class="normal" style="padding-left:0px !important">
                <tr>
                    <td style="padding-left:0px !important;padding-right:0px !important">
                        <select id="subjectId" name="subjectId" class="leftCol-vsmall">
                            <%
                                if(subjectIds != null){
                                    for (String id : subjectIds) {
                                        if (id.equals(subjectId)) {
                            %>
                            <option value="<%=Encode.forHtmlAttribute(id)%>" selected="selected"><%=Encode.forHtmlContent(id)%></option>
                            <%
                                        } else {
                            %>
                            <option value="<%=Encode.forHtmlAttribute(id)%>"><%=Encode.forHtmlContent(id)%></option>
                            <%
                                        }
                                    }
                                }
                            %>
                        </select>
                    </td>
                    <td style="padding-left:0px !important;padding-right:0px !important">
                        <select id="preFunctionOnSubjects" name="preFunctionOnSubjects" class="leftCol-small">
                            <%
                                if(preFunctionIds != null){
                                for (String preFunctionId : preFunctionIds) {
                                    if (preFunctionId.equals(preFunctionOnSubjects)) {
                            %>
                            <option value="<%=Encode.forHtmlAttribute(preFunctionId)%>" selected="selected"><%=Encode.forHtmlContent(preFunctionId)%>
                            </option>
                            <%
                            } else {
                            %>
                            <option value="<%=Encode.forHtmlAttribute(preFunctionId)%>"><%=Encode.forHtmlContent(preFunctionId)%>
                            </option>
                            <%
                                    }
                                }
                                }
                            %>
                        </select>
                    </td>
                    <td style="padding-left:0px !important;padding-right:0px !important">
                        <select id="functionOnSubjects" name="functionOnSubjects" class="leftCol-small">
                            <%
                                if(functionIds != null){
                                for (String functionId : functionIds) {
                                    if (functionId.equals(functionOnSubjects)) {
                            %>
                            <option value="<%=Encode.forHtmlAttribute(functionId)%>" selected="selected"><%=Encode.forHtmlContent(functionOnSubjects)%>
                            </option>
                            <%
                            } else {
                            %>
                            <option value="<%=Encode.forHtmlAttribute(functionId)%>"><%=Encode.forHtmlContent(functionId)%>
                            </option>
                            <%
                                    }
                                }
                                }
                            %>
                        </select>
                    </td>
                    <td style="padding-left:0px !important;padding-right:0px !important">
                        <%
                            if (subjectNames != null && !subjectNames.equals("")) {

                        %>
                        <input type="text" name="subjectNames" id="subjectNames"
                               value="<%=Encode.forHtmlAttribute(subjectNames)%>" class="text-box-mid-big"/>
                        <%
                        } else {
                        %>
                        <input type="text" name="subjectNames" id="subjectNames" class="text-box-mid-big"/>

                        <%
                            }
                        %>
                    </td>
                    <td>
                        <a title="Select Subject Names" class='icon-link' onclick='selectAttributes("Subject");'
                           style='background-image:url(images/user-store.gif); float:right;'></a>
                    </td>

                    <td>
                        <input type="hidden" name="subjectDataType" id="subjectDataType" value="<%=Encode.forHtmlAttribute(subjectDataType)%>" />
                    </td>
                </tr>
            </table>
            </td>
            </tr>


            <tr>
            <td class="leftCol-small"><fmt:message key='action.names'/></td>
            <td>
            <table class="normal" style="padding-left:0px !important">
                <tr>
                    <td style="padding-left:0px !important;padding-right:0px !important">
                        <select id="preFunctionOnActions" name="preFunctionOnActions" class="leftCol-vsmall">
                            <%
                                if(preFunctionIds != null){
                                    for (String preFunctionId : preFunctionIds) {
                                        if (preFunctionId.equals(preFunctionOnActions)) {
                            %>
                            <option value="<%=Encode.forHtmlAttribute(preFunctionId)%>" selected="selected"><%=Encode.forHtmlContent(preFunctionId)%></option>
                            <%
                                    } else {
                            %>
                            <option value="<%=Encode.forHtmlAttribute(preFunctionId)%>"><%=Encode.forHtmlContent(preFunctionId)%></option>
                            <%
                                        }
                                    }
                                }
                            %>
                        </select>
                    </td>
                    <td style="padding-left:0px !important;padding-right:0px !important">
                        <select id="functionOnActions" name="functionOnActions" class="leftCol-small">
                            <%
                                if(functionIds != null){
                                for (String functionId : functionIds) {
                                    if (functionId.equals(functionOnActions)) {
                            %>
                            <option value="<%=Encode.forHtmlAttribute(functionId)%>" selected="selected"><%=Encode.forHtmlContent(functionOnActions)%>
                            </option>
                            <%
                            } else {
                            %>
                            <option value="<%=Encode.forHtmlAttribute(functionId)%>"><%=Encode.forHtmlContent(functionId)%>
                            </option>
                            <%
                                    }
                                }
                                }
                            %>
                        </select>
                    </td>
                    <td style="padding-left:0px !important;padding-right:0px !important">
                        <%
                            if (actionNames != null && !actionNames.equals("")) {

                        %>
                        <input type="text" name="actionNames" id="actionNames" value="<%=Encode.forHtmlAttribute(actionNames)%>"
                               class="text-box-big"/>
                        <%
                        } else {
                        %>
                        <input type="text" name="actionNames" id="actionNames" class="text-box-big"/>

                        <%
                            }
                        %>
                    </td>
                    <td>
                        <a title="Select Action Names" class='icon-link' onclick='selectAttributes("Action");'
                           style='background-image:url(images/actions.png); float:right;'></a>
                    </td>

                    <td>
                        <input type="hidden" name="actionId" id="actionId" value="<%=Encode.forHtmlAttribute(actionId)%>" />
                    </td>

                    <td>
                        <input type="hidden" name="actionDataType" id="actionDataType" value="<%=Encode.forHtmlAttribute(actionDataType)%>" />
                    </td>
                </tr>
            </table>
            </td>
            </tr>

            <tr>
            <td class="leftCol-small"><fmt:message key='environment.names'/></td>
            <td>
            <table class="normal" style="padding-left:0px !important">
                <tr>
                    <td style="padding-left:0px !important;padding-right:0px !important">
                        <select id="environmentId" name="environmentId" class="leftCol-vsmall">
                            <%
                                if(environmentIds != null){
                                for (String id : environmentIds) {
                                    if (id.equals(environmentId)) {
                            %>
                            <option value="<%=Encode.forHtmlAttribute(id)%>" selected="selected"><%=Encode.forHtmlContent(id)%></option>
                            <%
                            } else {
                            %>
                            <option value="<%=Encode.forHtmlAttribute(id)%>"><%=Encode.forHtmlContent(id)%></option>
                            <%
                                    }
                                }
                                }
                            %>
                        </select>
                    </td>
                    <td style="padding-left:0px !important;padding-right:0px !important">
                        <select id="preFunctionOnEnvironment" name="preFunctionOnEnvironment" class="leftCol-small">
                            <%
                                if(preFunctionIds != null){
                                for (String preFunctionId : preFunctionIds) {
                                    if (preFunctionId.equals(preFunctionOnEnvironment)) {
                            %>
                            <option value="<%=Encode.forHtmlAttribute(preFunctionId)%>" selected="selected"><%=Encode.forHtmlContent(preFunctionId)%>
                            </option>
                            <%
                            } else {
                            %>
                            <option value="<%=Encode.forHtmlAttribute(preFunctionId)%>"><%=Encode.forHtmlContent(preFunctionId)%>
                            </option>
                            <%
                                    }
                                }
                                }
                            %>
                        </select>
                    </td>
                    <td style="padding-left:0px !important;padding-right:0px !important">
                        <select id="functionOnEnvironment" name="functionOnEnvironment" class="leftCol-small">
                            <%
                                if(functionIds != null){
                                for (String functionId : functionIds) {
                                    if (functionId.equals(functionOnEnvironment)) {
                            %>
                            <option value="<%=Encode.forHtmlAttribute(functionId)%>" selected="selected"><%=Encode.forHtmlContent(functionOnEnvironment)%>
                            </option>
                            <%
                            } else {
                            %>
                            <option value="<%=Encode.forHtmlAttribute(functionId)%>"><%=Encode.forHtmlContent(functionId)%>
                            </option>
                            <%
                                    }
                                }
                                }
                            %>
                        </select>
                    </td>
                    <td style="padding-left:0px !important;padding-right:0px !important">
                        <%
                            if (environmentNames != null && !environmentNames.equals("")) {

                        %>
                        <input type="text" name="environmentNames" id="environmentNames" value="<%=Encode.forHtmlAttribute(environmentNames)%>"
                               class="text-box-mid-big"/>
                        <%
                        } else {
                        %>
                        <input type="text" name="environmentNames" id="environmentNames" class="text-box-mid-big"/>

                        <%
                            }
                        %>
                    </td>
                    <td>
                        <a title="Select Environment Names" class='icon-link' onclick='selectAttributes("Environment");'
                           style='background-image:url(images/calendar.jpg); float:right;'></a>
                    </td>

                    <td>
                        <input type="hidden" name="environmentDataType" id="environmentDataType" value="<%=Encode.forHtmlAttribute(environmentDataType)%>" />
                    </td>
                </tr>
            </table>
            </td>
            </tr>
        </table>
        </td>
        </tr>

        <tr>
        <td colspan="2" class="buttonRow">
            <%
                if (basicRuleDTO != null && basicRuleDTO.isCompletedRule()) {
            %>
            <input class="button" type="button" value="<fmt:message key='update'/>"
                   onclick="doUpdate();"/>

            <input class="button" type="button" value="<fmt:message key='cancel'/>"
                   onclick="doCancelRule();"/>

            <%
            } else {
            %>

            <input class="button" type="button" value="<fmt:message key='add'/>"
                   onclick="doAdd();"/>
            <%
                }
            %>
        </td>
        </tr>

    </table>
    </div>
    </td>
    </tr>

    <tr>
    <td colspan="2">
        <table id="dataTable" style="width: 100%;margin-top:10px;">
            <thead>
            <tr>
                <th><fmt:message key="rule.id"/></th>
                <th><fmt:message key="rule.effect"/></th>
                <th><fmt:message key="action"/></th>
            </tr>
            </thead>
            <%
                if (basicRuleDTOs != null && basicRuleDTOs.size() > 0) {
                    List<BasicRuleDTO> orderedBasicRuleDTOs = new ArrayList<BasicRuleDTO>();
                    String ruleElementOrder = entitlementPolicyBean.getRuleElementOrder();
                    if(ruleElementOrder != null){
                        String[] orderedRuleIds = ruleElementOrder.split(EntitlementPolicyConstants.ATTRIBUTE_SEPARATOR);
                        for(String orderedRuleId : orderedRuleIds){
                            for(BasicRuleDTO orderedBasicRuleElementDTO : basicRuleDTOs) {
                                if(orderedRuleId.trim().equals(orderedBasicRuleElementDTO.getRuleId())){
                                    orderedBasicRuleDTOs.add(orderedBasicRuleElementDTO);
                                }
                            }
                        }
                    }

                    if(orderedBasicRuleDTOs.size() < 1){
                        orderedBasicRuleDTOs = basicRuleDTOs;
                    }
                    for (BasicRuleDTO ruleElementDTO : orderedBasicRuleDTOs) {
                        if(ruleElementDTO.isCompletedRule()){
            %>
            <tr>

                <td>
                    <a class="icon-link" onclick="updownthis(this,'up')" style="background-image:url(../admin/images/up-arrow.gif)"></a>
                    <a class="icon-link" onclick="updownthis(this,'down')" style="background-image:url(../admin/images/down-arrow.gif)"></a>
                    <input type="hidden" value="<%=ruleElementDTO.getRuleId()%>"/>
                    <%=ruleElementDTO.getRuleId()%>
                </td>
                <td><%=ruleElementDTO.getRuleEffect()%></td>
                <td>
                    <a href="#" onclick="editRule('<%=Encode.forJavaScriptAttribute(ruleElementDTO.getRuleId())%>')"
                       class="icon-link" style="background-image:url(images/edit.gif);"><fmt:message
                            key="edit"/></a>
                    <a href="#" onclick="deleteRule('<%=Encode.forJavaScriptAttribute(ruleElementDTO.getRuleId())%>')"
                       class="icon-link" style="background-image:url(images/delete.gif);"><fmt:message
                            key="delete"/></a>
                </td>
            </tr>
            <%
                    }
                }
            } else {
            %>
            <tr class="noRuleBox">
                <td colspan="3"><fmt:message key="no.rule.defined"/><br/></td>
            </tr>
            <%
                }
            %>
        </table>
    </td>
    </tr>
    <tr>
        <td class="buttonRow" colspan="2">
            <input type="button" onclick="submitForm();" value="<fmt:message key="finish"/>"  class="button"/>
            <input type="button" onclick="doCancel();" value="<fmt:message key="cancel" />" class="button"/>
        </td>
    </tr>
</table>
</form>
</div>
</div>
</fmt:bundle>
