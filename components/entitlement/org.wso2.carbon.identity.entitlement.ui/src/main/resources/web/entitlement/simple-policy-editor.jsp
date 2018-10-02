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
<%@ page import="org.wso2.carbon.identity.entitlement.common.EntitlementConstants" %>
<%@ page import="org.wso2.carbon.identity.entitlement.common.PolicyEditorEngine" %>
<%@ page import="org.wso2.carbon.identity.entitlement.common.dto.PolicyEditorDataHolder" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.PolicyEditorConstants" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.SimplePolicyEditorDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.SimplePolicyEditorElementDTO" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Set" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<jsp:useBean id="entitlementPolicyBean"
             type="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean"
             class="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean" scope="session"/>
<jsp:setProperty name="entitlementPolicyBean" property="*"/>

<%
    SimplePolicyEditorDTO policyEditorDTO = entitlementPolicyBean.getSimplePolicyEditorDTO();
    List<SimplePolicyEditorElementDTO> elementDTOList = null;
    String selectedPolicyApplied = request.getParameter("policyApplied");
    String policyId = request.getParameter("policyId");
    String policyDescription = request.getParameter("policyDescription");

    PolicyEditorDataHolder holder = PolicyEditorEngine.getInstance().
            getPolicyEditorData(EntitlementConstants.PolicyEditor.RBAC);
    
    String[] policyApplies  = new String[]{PolicyEditorConstants.SOA_CATEGORY_RESOURCE ,
            PolicyEditorConstants.SOA_CATEGORY_USER, PolicyEditorConstants.SOA_CATEGORY_ENVIRONMENT,
            PolicyEditorConstants.SOA_CATEGORY_ACTION};


    Set<String> userAttributeIds =  holder.getCategoryAttributeIdMap().
                                        get(PolicyEditorConstants.SOA_CATEGORY_SUBJECT);
    Set<String> envAttributeIds  =  holder.getCategoryAttributeIdMap().
                                        get(PolicyEditorConstants.SOA_CATEGORY_ENVIRONMENT);
    
    String selectedRuleUserAttributeId = null;
    String selectedRuleUserAttributeValue = null;
    String selectedRuleActionValue = null;
    String selectedRuleResourceValue = null;
    String selectedRuleEnvironmentValue= null;
    String selectedRuleEnvironmentId= null;
    String selectedRuleOperationType= null;
    String selectedRuleResourceFunction = null;
    String selectedRuleUserFunction = null;
    String selectedRuleActionFunction = null;

    String selectedUserAttributeId = null;
    String selectedUserAttributeValue = null;
    String selectedActionValue = null;
    String selectedResourceValue = null;
    String selectedEnvironmentValue= null;
    String selectedEnvironmentId= null;
    String selectedFunction = null;

    if(policyEditorDTO != null){
        policyId = policyEditorDTO.getPolicyId();
        policyDescription = policyEditorDTO.getDescription();
        selectedFunction = policyEditorDTO.getFunction();
        selectedUserAttributeId = policyEditorDTO.getUserAttributeId();
        selectedUserAttributeValue = policyEditorDTO.getUserAttributeValue();
        selectedActionValue= policyEditorDTO.getActionValue();
        selectedEnvironmentValue= policyEditorDTO.getEnvironmentValue();
        selectedResourceValue = policyEditorDTO.getResourceValue();
        if(selectedPolicyApplied == null || selectedPolicyApplied.trim().length() == 0){
            selectedPolicyApplied = policyEditorDTO.getAppliedCategory();
        }

        elementDTOList = policyEditorDTO.getSimplePolicyEditorElementDTOs();

        if(elementDTOList != null && elementDTOList.size() > 0){
            SimplePolicyEditorElementDTO elementDTO = elementDTOList.get(0);
            if(elementDTO != null){
                selectedRuleActionValue = elementDTO.getActionValue();
                selectedRuleUserAttributeId = elementDTO.getUserAttributeId();
                selectedRuleUserAttributeValue = elementDTO.getUserAttributeValue();
                selectedRuleResourceValue = elementDTO.getResourceValue();
                selectedRuleEnvironmentValue= elementDTO.getEnvironmentValue();
                selectedRuleEnvironmentId= elementDTO.getEnvironmentId();
                selectedRuleOperationType= elementDTO.getOperationType();
                selectedRuleResourceFunction = elementDTO.getFunctionOnResources();
                selectedRuleUserFunction = elementDTO.getFunctionOnUsers();
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
<script src="../entitlement/js/policy-editor.js" type="text/javascript"></script>
<link href="../entitlement/css/entitlement.css" rel="stylesheet" type="text/css" media="all"/>




<script type="text/javascript">

    var regString = /^[a-zA-Z0-9._-]{3,20}$/;    // TODO make this configurable

    jQuery(document).ready(function(){

    });

    function doValidationPolicyNameOnly() {

        var value = document.getElementsByName("policyId")[0].value;
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

    function doSubmit(){
        if(doValidationPolicyNameOnly()){
            preSubmit();
            document.dataForm.action = "simple-policy-finish-ajaxprocessor.jsp";
            document.dataForm.submit();
        }
    }

    function preSubmit(){

        var userRuleTable = "";
        var actionRuleTable = "";
        var resourceRuleTable = "";
        var environmentRuleTable = "";

        if(document.getElementById('userRuleTable') != null){
            userRuleTable =  jQuery(document.getElementById('userRuleTable').rows[document.
                        getElementById('userRuleTable').rows.length-1]).attr('data-value');
        }

        if(document.getElementById('resourceRuleTable') != null){
            resourceRuleTable = jQuery(document.getElementById('resourceRuleTable').rows[document.
                    getElementById('resourceRuleTable').rows.length-1]).attr('data-value');
        }

        if(document.getElementById('environmentRuleTable') != null){
            environmentRuleTable = jQuery(document.getElementById('environmentRuleTable').rows[document.
                    getElementById('environmentRuleTable').rows.length-1]).attr('data-value');
        }

        if(document.getElementById('actionRuleTable') != null){
            actionRuleTable = jQuery(document.getElementById('actionRuleTable').rows[document.
                    getElementById('actionRuleTable').rows.length-1]).attr('data-value');
        }

        jQuery('#mainTable > tbody:last').append('<tr><td><input type="hidden" name="maxUserRow" id="maxUserRow" value="' + userRuleTable +'"/></td></tr>') ;
        jQuery('#mainTable > tbody:last').append('<tr><td><input type="hidden" name="maxResourceRow" id="maxResourceRow" value="' + resourceRuleTable +'"/></td></tr>') ;
        jQuery('#mainTable > tbody:last').append('<tr><td><input type="hidden" name="maxEnvironmentRow" id="maxEnvironmentRow" value="' + environmentRuleTable +'"/></td></tr>') ;
        jQuery('#mainTable > tbody:last').append('<tr><td><input type="hidden" name="maxActionRow" id="maxActionRow" value="' + actionRuleTable +'"/></td></tr>') ;
    }

    function doCancel(){
        location.href = "index.jsp";
    }

    function removeRow(link){
        link.parentNode.parentNode.parentNode.parentNode.parentNode.parentNode.parentNode.
                removeChild(link.parentNode.parentNode.parentNode.parentNode.parentNode.parentNode);
    }
    
    function createNewResourceRuleRow() {
        var rowIndex =  jQuery(document.getElementById('resourceRuleTable').rows[document.
                            getElementById('resourceRuleTable').rows.length-1]).attr('data-value');
        var index = parseInt(rowIndex, 10) + 1;
        jQuery('#resourceRuleTable > tbody:last').append('<tr data-value="'+ index +'"><td><table class="oneline-listing"><tr><td style="white-space:nowrap;">Child resource</td><td>User</td><td></td><td>Action</td><td>Environment</td><td></td></tr>' +
            '<td><%if (selectedRuleResourceValue != null && selectedRuleResourceValue.trim().length() > 0) {%><input type="text" name="resourceRuleValue_'  + index + '"  id="resourceRuleValue_'  + index + '" value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(selectedRuleResourceValue))%>"/><%} else {%><input type="text" name="resourceRuleValue_'  + index + '"  id="resourceRuleValue_'  + index + '"  /><% }%> </td>' +
            '<td><select id="userRuleAttributeId_'  + index + '"  name="userRuleAttributeId_'  + index + '"  ><%for (String userAttribute : userAttributeIds) {if (selectedRuleUserAttributeId != null && userAttribute.equals(selectedRuleUserAttributeId)) {%><option value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(userAttribute))%>" selected="selected"><%=Encode.forJavaScript(Encode.forHtmlContent(userAttribute))%></option><%} else {%><option value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(userAttribute))%>"><%=Encode.forJavaScript(Encode.forHtmlContent(userAttribute))%></option><%}}%></select></td>' +
            '<td><%if (selectedRuleUserAttributeValue != null && selectedRuleUserAttributeValue.trim().length() > 0) {%><input type="text" name="userRuleAttributeValue_'  + index + '"  id="userRuleAttributeValue_'  + index + '"  value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(selectedRuleUserAttributeValue))%>"/><%} else {%><input type="text" name="userRuleAttributeValue_'  + index + '"  id="userRuleAttributeValue_'  + index + '"  /><%}%></td>' +
            '<td><%if (selectedRuleActionValue != null && selectedRuleActionValue.trim().length() > 0) {%><input type="text" name="actionRuleValue_'  + index + '"  id="actionRuleValue_'  + index + '"  value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(selectedRuleActionValue))%>"/><%} else {%><input type="text" name="actionRuleValue_'  + index + '"  id="actionRuleValue_'  + index + '"  /><%}%></td>' +
            '<td><select id="environmentRuleId_'  + index + '"  name="environmentRuleId_'  + index + '"  ><%for (String userAttribute : envAttributeIds) {if (selectedRuleEnvironmentId != null && userAttribute.equals(selectedRuleEnvironmentId)) {%><option value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(userAttribute))%>" selected="selected"><%=Encode.forJavaScript(Encode.forHtmlContent(userAttribute))%></option><%} else {%><option value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(userAttribute))%>"><%=Encode.forJavaScript(Encode.forHtmlContent(userAttribute))%></option><%}}%></select></td>' +
            '<td><%if (selectedRuleEnvironmentValue != null && selectedRuleEnvironmentValue.trim().length() > 0) {%><input type="text" name="environmentRuleValue_'  + index + '"  id="environmentRuleValue_'  + index + '"  value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(selectedRuleEnvironmentValue))%>"/><%} else {%><input type="text" name="environmentRuleValue_'  + index + '"  id="environmentRuleValue_'  + index + '"  /><%}%></td>' +
            '<td><a onclick="removeRow(this)" style="background-image:url(images/delete.gif);" type="button" class="icon-link"></a></td>' +
            '</tr></table></td></tr>');
    }
    

    function createNewUserRuleRow() {
        var rowIndex =  jQuery(document.getElementById('userRuleTable').rows[document.
                            getElementById('userRuleTable').rows.length-1]).attr('data-value');
        var index = parseInt(rowIndex, 10) + 1;
        jQuery('#userRuleTable > tbody:last').append('<tr data-value="'+ index +'"><td><table class="oneline-listing"><tr></tr><tr><td> Action </td>' +
            '<td><%if (selectedRuleActionValue != null && selectedRuleActionValue.trim().length() > 0) {%><input type="text" name="actionRuleValue_'  + index + '" id="actionRuleValue_'  + index + '" value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(selectedRuleActionValue))%>"/><%} else {%><input type="text" name="actionRuleValue_'  + index + '" id="actionRuleValue_'  + index + '" /><%}%></td>' +
            '<td> Resource </td>' +
            '<td><%if (selectedRuleResourceValue != null && selectedRuleResourceValue.trim().length() > 0) {%><input type="text" name="resourceRuleValue_'  + index + '" id="resourceRuleValue_'  + index + '" value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(selectedRuleResourceValue))%>"/><%} else {%><input type="text" name="resourceRuleValue_'  + index + '" id="resourceRuleValue_'  + index + '" /><%}%></td>' +
            '<td> Environment </td>' +
            '<td><select id="environmentRuleId_'  + index + '"  name="environmentRuleId_'  + index + '"  ><%for (String userAttribute : envAttributeIds) {if (selectedRuleEnvironmentId != null && userAttribute.equals(selectedRuleEnvironmentId)) {%><option value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(userAttribute))%>" selected="selected"><%=Encode.forJavaScript(Encode.forHtmlContent(userAttribute))%></option><%} else {%><option value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(userAttribute))%>"><%=Encode.forJavaScript(Encode.forHtmlContent(userAttribute))%></option><%}}%></select></td>' +
            '<td><%if (selectedRuleEnvironmentValue != null && selectedRuleEnvironmentValue.trim().length() > 0) {%><input type="text" name="environmentRuleValue_'  + index + '"  id="environmentRuleValue_'  + index + '"  value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(selectedRuleEnvironmentValue))%>"/><%} else {%><input type="text" name="environmentRuleValue_'  + index + '"  id="environmentRuleValue_'  + index + '"  /><%}%></td>' +
            '<td><a onclick="removeRow(this)" style="background-image:url(images/delete.gif);" type="button" class="icon-link"></a></td>' +
            '</tr><tr></tr></table></td></tr>');
    }

    function createNewActionRuleRow() {
        var rowIndex =  jQuery(document.getElementById('actionRuleTable').rows[document.
                            getElementById('actionRuleTable').rows.length-1]).attr('data-value');
        var index = parseInt(rowIndex, 10) + 1;
        jQuery('#actionRuleTable > tbody:last').append('<tr data-value="'+ index +'"><td><table class="oneline-listing"><tr></tr><tr><td>Resource</td>' +
                '<td><%if (selectedRuleResourceValue != null && selectedRuleResourceValue.trim().length() > 0) {%><input type="text" name="resourceRuleValue_'  + index + '"  id="resourceRuleValue_'  + index + '" value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(selectedRuleResourceValue))%>"/><%} else {%><input type="text" name="resourceRuleValue_'  + index + '"  id="resourceRuleValue_'  + index + '"  /><% }%> </td>' +
                '<td>User</td>' +
                '<td><select id="userRuleAttributeId_'  + index + '" name="userRuleAttributeId_'  + index + '" ><%for (String userAttribute : userAttributeIds) {if (selectedRuleUserAttributeId != null && userAttribute.equals(selectedRuleUserAttributeId)) {%><option value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(userAttribute))%>"  selected="selected"><%=Encode.forJavaScript(Encode.forHtmlContent(userAttribute))%></option><%} else {%><option value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(userAttribute))%>"><%=Encode.forJavaScript(Encode.forHtmlContent(userAttribute))%></option><%}}%></select></td>' +
                '<td><%if (selectedRuleUserAttributeValue != null && selectedRuleUserAttributeValue.trim().length() > 0) {%><input type="text" name="userRuleAttributeValue_'  + index + '" id="userRuleAttributeValue_'  + index + '" value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(selectedRuleUserAttributeValue))%>"/><% } else {%><input type="text" name="userRuleAttributeValue_'  + index + '" id="userRuleAttributeValue_'  + index + '" /><%}%></td>' +
                '<td> Environment </td>' +
                '<td><select id="environmentRuleId_'  + index + '"  name="environmentRuleId_'  + index + '"  ><%for (String userAttribute : envAttributeIds) {if (selectedRuleEnvironmentId != null && userAttribute.equals(selectedRuleEnvironmentId)) {%><option value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(userAttribute))%>" selected="selected"><%=Encode.forJavaScript(Encode.forHtmlContent(userAttribute))%></option><%} else {%><option value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(userAttribute))%>"><%=Encode.forJavaScript(Encode.forHtmlContent(userAttribute))%></option><%}}%></select></td>' +
                '<td><%if (selectedRuleEnvironmentValue != null && selectedRuleEnvironmentValue.trim().length() > 0) {%><input type="text" name="environmentRuleValue_'  + index + '"  id="environmentRuleValue_'  + index + '"  value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(selectedRuleEnvironmentValue))%>"/><%} else {%><input type="text" name="environmentRuleValue_'  + index + '"  id="environmentRuleValue_'  + index + '"  /><%}%></td>' +
                '<td><a onclick="removeRow(this)" style="background-image:url(images/delete.gif);" type="button" class="icon-link"></a></td>' +
                '</tr><tr></tr></table></td></tr>');
    }

    function createNewEnvironmentRuleRow() {
        var rowIndex =  jQuery(document.getElementById('environmentRuleTable').rows[document.
                            getElementById('environmentRuleTable').rows.length-1]).attr('data-value');
        var index = parseInt(rowIndex, 10) + 1;
        jQuery('#environmentRuleTable > tbody:last').append('<tr data-value="'+ index +'"><td><table class="oneline-listing"><tr></tr><tr><td> Resource </td>' +
            '<td><%if (selectedRuleResourceValue != null && selectedRuleResourceValue.trim().length() > 0) {%><input type="text" name="resourceRuleValue_'  + index + '"  id="resourceRuleValue_'  + index + '" value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(selectedRuleResourceValue))%>"/><%} else {%><input type="text" name="resourceRuleValue_'  + index + '"  id="resourceRuleValue_'  + index + '"  /><% }%> </td>' +
            '<td> User </td>'+
            '<td><select id="userRuleAttributeId_'  + index + '"  name="userRuleAttributeId_'  + index + '"  ><%for (String userAttribute : userAttributeIds) {if (selectedRuleUserAttributeId != null && userAttribute.equals(selectedRuleUserAttributeId)) {%><option value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(userAttribute))%>" selected="selected"><%=Encode.forJavaScript(Encode.forHtmlContent(userAttribute))%></option><%} else {%><option value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(userAttribute))%>"><%=Encode.forJavaScript(Encode.forHtmlContent(userAttribute))%></option><%}}%></select></td>' +
            '<td><%if (selectedRuleUserAttributeValue != null && selectedRuleUserAttributeValue.trim().length() > 0) {%><input type="text" name="userRuleAttributeValue_'  + index + '"  id="userRuleAttributeValue_'  + index + '"  value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(selectedRuleUserAttributeValue))%>"/><%} else {%><input type="text" name="userRuleAttributeValue_'  + index + '"  id="userRuleAttributeValue_'  + index + '"  /><%}%></td>' +
            '<td> Action </td>' +
            '<td><%if (selectedRuleActionValue != null && selectedRuleActionValue.trim().length() > 0) {%><input type="text" name="actionRuleValue_'  + index + '"  id="actionRuleValue_'  + index + '"  value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(selectedRuleActionValue))%>"/><%} else {%><input type="text" name="actionRuleValue_'  + index + '"  id="actionRuleValue_'  + index + '"  /><%}%></td>' +
            '<td><a onclick="removeRow(this)" style="background-image:url(images/delete.gif);" type="button" class="icon-link"></a></td>' +
            '</tr></table></td></tr>');
    }

    function getCategoryType() {
        document.dataForm.submit();
    }


</script>

<div id="middle">
<%if(entitlementPolicyBean.isEditPolicy()){%>
<h2><fmt:message key="edit.xacml.policy"/></h2>
<%} else {%><h2><fmt:message key="create.policy"/></h2><%}%>
<div id="workArea">
<form id="dataForm" name="dataForm" method="post" action="">
<table id="mainTable" class="styledLeft noBorders">
    <tr>
        <td class="leftCol-med"><fmt:message key="policy.name"/><span class="required">*</span></td>
        <%
            if (policyId != null && policyId.trim().length() > 0) {
        %>
            <td><input type="text" name="policyId" id="policyId" value="<%=Encode.forHtmlAttribute(policyId)%>"/></td>
        <%
            } else {
        %>
            <td><input type="text" name="policyId" id="policyId" /></td>
        <%
            }
        %>
    </tr>
    <tr>
        <td><fmt:message key="policy.description"/></td>
        <%
            if (policyDescription != null && policyDescription.trim().length() > 0) {
        %>
            <td><textarea name="policyDescription" id="policyDescription"  class="text-box-big" value="<%=Encode.forHtmlAttribute(policyDescription)%>"></textarea></td>
        <%
            } else {
        %>
            <td><textarea type="text" name="policyDescription" id="policyDescription"  class="text-box-big"></textarea></td>
        <%
            }
        %>
    </tr>
    <tr>
        <td class="leftCol-small"><fmt:message key="policy.based.on" /></td>
        <td>
            <select id="policyApplied" name="policyApplied" <%if(entitlementPolicyBean.isEditPolicy()){%> disabled="disabled" <%}%>
                    onchange="getCategoryType();">
                <%
                    for (String  policyApply : policyApplies) {
                        if(selectedPolicyApplied != null && policyApply.equals(selectedPolicyApplied)){
                %>
                    <option value="<%=Encode.forHtmlAttribute(policyApply)%>" selected="selected" ><%=Encode.forHtmlContent(policyApply)%></option>
                <%
                        } else {
                %>
                    <option value="<%=Encode.forHtmlAttribute(policyApply)%>" ><%=Encode.forHtmlContent(policyApply)%></option>
                <%
                        }
                    }
                %>
            </select>
        </td>
        <%if(entitlementPolicyBean.isEditPolicy() && selectedPolicyApplied != null){%>
            <input type="hidden" name="policyApplied" id="policyApplied" value="<%=selectedPolicyApplied%>"/>
        <%}%>
    </tr>
 <%--</table> /////////////////// TODO--%>
        <%--END Basic information section --%>

                <%--**********************--%>
                <%--**********************--%>
                <%--START user policy type--%>
                <%--**********************--%>
                <%--**********************--%>

    <%
        if(PolicyEditorConstants.SOA_CATEGORY_USER.equals(selectedPolicyApplied)) {
    %>
        <tr>
            <td colspan="2">
            <table class="normal" style="padding-left:0px !important">
            <tr>
            <td>User whose</td>
            <td>
                <select id="userAttributeId" name="userAttributeId" >
                    <%
                        for (String userAttribute : userAttributeIds) {
                            if (selectedUserAttributeId != null && userAttribute.equals(selectedUserAttributeId)) {
                    %>
                        <option value="<%=Encode.forHtmlAttribute(userAttribute)%>"  selected="selected"><%=Encode.forHtmlContent(userAttribute)%></option>
                    <%
                            } else {
                    %>
                        <option value="<%=Encode.forHtmlAttribute(userAttribute)%>"><%=Encode.forHtmlContent(userAttribute)%></option>
                    <%
                            }
                        }
                    %>
                </select>
            </td>
            <td> is equal to </td>
            <td>
                <%
                    if (selectedUserAttributeValue != null && selectedUserAttributeValue.trim().length() > 0) {
                %>
                    <input type="text" name="userAttributeValue" id="userAttributeValue" value="<%=Encode.forHtmlAttribute(selectedUserAttributeValue)%>"/>
                <%
                    } else {
                %>
                    <input type="text" name="userAttributeValue" id="userAttributeValue" />
                <%
                    }
                %>
            </td>
            </tr>
            </table>
            </td>
        </tr>

        <tr>
        <td colspan="4">
        <table  id="userRuleTable" >
            <tr data-value="0">
            <td>
            <table class="oneline-listing">
            <tr></tr>
            <tr>
                <td> Action </td>
                <td>
                <%
                    if (selectedRuleActionValue != null && selectedRuleActionValue.trim().length() > 0) {
                %>
                    <input type="text" name="actionRuleValue_0" id="actionRuleValue_0" value="<%=Encode.forHtmlAttribute(selectedRuleActionValue)%>"/>
                <%
                    } else {
                %>
                    <input type="text" name="actionRuleValue_0" id="actionRuleValue_0" />
                <%
                    }
                %>
                </td>
                <td> Resource </td>
                <td>
                <%
                    if (selectedRuleResourceValue != null && selectedRuleResourceValue.trim().length() > 0) {
                %>
                    <input type="text" name="resourceRuleValue_0" id="resourceRuleValue_0" value="<%=Encode.forHtmlAttribute(selectedRuleResourceValue)%>"/>
                <%
                    } else {
                %>
                    <input type="text" name="resourceRuleValue_0" id="resourceRuleValue_0" />
                <%
                    }
                %>
                </td>
                <td> Environment </td>
                <td>
                    <select id="environmentRuleId_0" name="environmentRuleId_0"  >
                        <%
                            for (String userAttribute : envAttributeIds) {
                                if (selectedRuleEnvironmentId != null && userAttribute.equals(selectedRuleEnvironmentId)) {
                        %>
                            <option value="<%=Encode.forHtmlAttribute(userAttribute)%>"  selected="selected"><%=Encode.forHtmlContent(selectedRuleEnvironmentId)%></option>
                        <%
                                } else {
                        %>
                            <option value="<%=Encode.forHtmlAttribute(userAttribute)%>"><%=Encode.forHtmlContent(userAttribute)%></option>
                        <%
                                }
                            }
                        %>
                    </select>
                </td>
                <td>  <%
                    if (selectedRuleEnvironmentValue != null && selectedRuleEnvironmentValue.trim().length() > 0) {
                %>
                    <input type="text" name="environmentRuleValue_0" id="environmentRuleValue_0" value="<%=Encode.forHtmlAttribute(selectedRuleEnvironmentValue)%>"/>
                <%
                    } else {
                %>
                    <input type="text" name="environmentRuleValue_0" id="environmentRuleValue_0" />
                <%
                    }
                %>
                </td>
                <td>
                    <a onclick="createNewUserRuleRow();" style="background-image:url(images/add.gif);float:none" type="button"
                       class="icon-link"></a>
                </td>
            </tr>
            <tr></tr>
            </table>
            </td>
            </tr>
<%
if(elementDTOList != null && elementDTOList.size() > 0){
    elementDTOList.remove(0);
    for(SimplePolicyEditorElementDTO elementDTO : elementDTOList){
        selectedRuleActionValue = elementDTO.getActionValue();
        selectedRuleUserAttributeId = elementDTO.getUserAttributeId();
        selectedRuleUserAttributeValue = elementDTO.getUserAttributeValue();
        selectedRuleResourceValue = elementDTO.getResourceValue();
        selectedRuleEnvironmentValue= elementDTO.getEnvironmentValue();
        selectedRuleEnvironmentId= elementDTO.getEnvironmentId();
        selectedRuleOperationType= elementDTO.getOperationType();
        selectedRuleResourceFunction = elementDTO.getFunctionOnResources();
        selectedRuleUserFunction = elementDTO.getFunctionOnUsers();
%>
    <script type="text/javascript">
        function createUserRuleRow() {
            var rowIndex =  jQuery(document.getElementById('userRuleTable').rows[document.
                        getElementById('userRuleTable').rows.length-1]).attr('data-value');
            var index = parseInt(rowIndex, 10) + 1;
            jQuery('#userRuleTable > tbody:last').append('<tr data-value="'+ index +'"><td><table class="oneline-listing"><tr></tr><tr><td> Action </td>' +
                '<td><%if (selectedRuleActionValue != null && selectedRuleActionValue.trim().length() > 0) {%><input type="text" name="actionRuleValue_'  + index + '" id="actionRuleValue_'  + index + '" value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(selectedRuleActionValue))%>"/><%} else {%><input type="text" name="actionRuleValue_'  + index + '" id="actionRuleValue_'  + index + '" /><%}%></td>' +
                '<td> Resource </td>' +
                '<td><%if (selectedRuleResourceValue != null && selectedRuleResourceValue.trim().length() > 0) {%><input type="text" name="resourceRuleValue_'  + index + '" id="resourceRuleValue_'  + index + '" value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(selectedRuleResourceValue))%>"/><%} else {%><input type="text" name="resourceRuleValue_'  + index + '" id="resourceRuleValue_'  + index + '" /><%}%></td>' +
                '<td> Environment </td>' +
                '<td><select id="environmentRuleId_'  + index + '"  name="environmentRuleId_'  + index + '"  ><%for (String userAttribute : envAttributeIds) {if (selectedRuleEnvironmentId != null && userAttribute.equals(selectedRuleEnvironmentId)) {%><option value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(userAttribute))%>" selected="selected"><%=Encode.forJavaScript(Encode.forHtmlContent(userAttribute))%></option><%} else {%><option value="Encode.forJavaScript(Encode.forHtmlAttribute(<%=userAttribute%>))"><%=Encode.forJavaScript(Encode.forHtmlContent(userAttribute))%></option><%}}%></select></td>' +
                '<td><%if (selectedRuleEnvironmentValue != null && selectedRuleEnvironmentValue.trim().length() > 0) {%><input type="text" name="environmentRuleValue_'  + index + '"  id="environmentRuleValue_'  + index + '"  value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(selectedRuleEnvironmentValue))%>"/><%} else {%><input type="text" name="environmentRuleValue_'  + index + '"  id="environmentRuleValue_'  + index + '"  /><%}%></td>' +
                '<td><a onclick="removeRow(this)" style="background-image:url(images/delete.gif);" type="button" class="icon-link"></a></td>' +
                '</tr><tr></tr></table></td></tr>');
        }
        createUserRuleRow();
    </script>
        <%
            }
        }
        %>
        </table>
        </td>
        </tr>
        <%--********************--%>
        <%--********************--%>
        <%--END user policy type--%>
        <%--********************--%>
        <%--********************--%>
        <%--********************--%>


        <%--************************--%>
        <%--************************--%>
        <%--START action policy type--%>
        <%--************************--%>
        <%--************************--%>
        <%--************************--%>

        <%
            } else if(PolicyEditorConstants.SOA_CATEGORY_ACTION.equals(selectedPolicyApplied)){
        %>

        <tr>
        <td colspan="2">
        <table class="normal" style="padding-left:0px !important">
        <tr>
            <td>Action which is equals to</td>
            <%
                if (selectedActionValue != null && selectedActionValue.trim().length() > 0) {
            %>
                <td><input type="text" name="actionValue" id="actionValue" value="<%=Encode.forHtmlAttribute(selectedActionValue)%>"/></td>
            <%
                } else {
            %>
                <td><input type="text" name="actionValue" id="actionValue" /></td>
            <%
                }
            %>
        </tr>
        </table>
        </td>
        </tr>


        <tr>
        <td colspan="4">
        <table  id="actionRuleTable" >
            <tr data-value="0">
            <td>
            <table class="oneline-listing">
            <tr></tr>
            <tr>
                <td> Resource</td>
                <td>
                <%
                    if (selectedRuleResourceValue != null && selectedRuleResourceValue.trim().length() > 0) {
                %>
                    <input type="text" name="resourceRuleValue_0" id="resourceRuleValue_0" value="<%=Encode.forHtmlAttribute(selectedRuleResourceValue)%>"/>
                <%
                    } else {
                %>
                    <input type="text" name="resourceRuleValue_0" id="resourceRuleValue_0" />
                <%
                    }
                %>
                </td>
                <td>User</td>
                <td>
                    <select id="userRuleAttributeId_0" name="userRuleAttributeId_0" >
                        <%
                            for (String userAttribute : userAttributeIds) {
                                if (selectedRuleUserAttributeId != null && userAttribute.equals(selectedRuleUserAttributeId)) {
                        %>
                            <option value="<%=Encode.forHtmlAttribute(userAttribute)%>"  selected="selected"><%=Encode.forHtmlContent(userAttribute)%></option>
                        <%
                                } else {
                        %>
                            <option value="<%=Encode.forHtmlAttribute(userAttribute)%>"><%=Encode.forHtmlContent(userAttribute)%></option>
                        <%
                                }
                            }
                        %>
                    </select>
                </td>
                <td>
                    <%
                        if (selectedRuleUserAttributeValue != null && selectedRuleUserAttributeValue.trim().length() > 0) {
                    %>
                        <input type="text" name="userRuleAttributeValue_0" id="userRuleAttributeValue_0" value="<%=Encode.forHtmlAttribute(selectedRuleUserAttributeValue)%>"/>
                    <%
                        } else {
                    %>
                        <input type="text" name="userRuleAttributeValue_0" id="userRuleAttributeValue_0" />
                    <%
                        }
                    %>
                </td>
            <td> Environment </td>
            <td>
                <select id="environmentRuleId_0" name="environmentRuleId_0"  >
                    <%
                        for (String userAttribute : envAttributeIds) {
                            if (selectedRuleEnvironmentId != null && userAttribute.equals(selectedRuleEnvironmentId)) {
                    %>
                        <option value="<%=Encode.forHtmlAttribute(userAttribute)%>"  selected="selected"><%=Encode.forHtmlContent(selectedRuleEnvironmentId)%></option>
                    <%
                            } else {
                    %>
                        <option value="<%=Encode.forHtmlAttribute(userAttribute)%>"><%=Encode.forHtmlContent(userAttribute)%></option>
                    <%
                            }
                        }
                    %>
                </select>
            </td>
            <td>  <%
                if (selectedRuleEnvironmentValue != null && selectedRuleEnvironmentValue.trim().length() > 0) {
            %>
                <input type="text" name="environmentRuleValue_0" id="environmentRuleValue_0" value="<%=Encode.forHtmlAttribute(selectedRuleEnvironmentValue)%>"/>
            <%
                } else {
            %>
                <input type="text" name="environmentRuleValue_0" id="environmentRuleValue_0" />
            <%
                }
            %>
            </td>
                <td>
                    <a onclick="createNewActionRuleRow();" style="background-image:url(images/add.gif);float:none" type="button"
                       class="icon-link"></a>
                </td>
            </tr>
            <tr></tr>
            </table>
            </td>
            </tr>
        <%
            if(elementDTOList != null && elementDTOList.size() > 0){
                elementDTOList.remove(0);
                for(SimplePolicyEditorElementDTO elementDTO : elementDTOList){
                    selectedRuleActionValue = elementDTO.getActionValue();
                    selectedRuleUserAttributeId = elementDTO.getUserAttributeId();
                    selectedRuleUserAttributeValue = elementDTO.getUserAttributeValue();
                    selectedRuleResourceValue = elementDTO.getResourceValue();
                    selectedRuleEnvironmentValue= elementDTO.getEnvironmentValue();
                    selectedRuleEnvironmentId= elementDTO.getEnvironmentId();
                    selectedRuleOperationType= elementDTO.getOperationType();
                    selectedRuleResourceFunction = elementDTO.getFunctionOnResources();
                    selectedRuleUserFunction = elementDTO.getFunctionOnUsers();
        %>
                <script type="text/javascript">
                    function createActionRuleRow() {
                        var rowIndex =  jQuery(document.getElementById('actionRuleTable').rows[document.
                                    getElementById('actionRuleTable').rows.length-1]).attr('data-value');
                        var index = parseInt(rowIndex, 10) + 1;
                        jQuery('#actionRuleTable > tbody:last').append('<tr data-value="'+ index +'"><td><table class="oneline-listing"><tr></tr><tr><td>Resource</td>' +
                                <%--'<td><select id="resourceRuleFunction_'  + index + '" name="resourceRuleFunction_'  + index + '"><%for (String function : functions) {if (selectedRuleResourceFunction != null && selectedRuleResourceFunction.equals(function)) { %><option value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(function))%>"  selected="selected"><%=Encode.forJavaScript(Encode.forHtmlContent(function))%></option><%} else {%><option value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(function))%>"><%=Encode.forJavaScript(Encode.forHtmlContent(function))%></option><%}}%></select></td><td>to</td>< +--%>
                                '<td><%if (selectedRuleResourceValue != null && selectedRuleResourceValue.trim().length() > 0) {%><input type="text" name="resourceRuleValue_'  + index + '" id="resourceRuleValue_'  + index + '" value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(selectedRuleResourceValue))%>"/><%} else {%><input type="text" name="resourceRuleValue_'  + index + '" id="resourceRuleValue_'  + index + '" /><%}%></td>' +
                                '<td>User</td>' +
                                '<td><select id="userRuleAttributeId_'  + index + '" name="userRuleAttributeId_'  + index + '" ><%for (String userAttribute : userAttributeIds) {if (selectedRuleUserAttributeId != null && userAttribute.equals(selectedRuleUserAttributeId)) {%><option value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(userAttribute))%>"  selected="selected"><%=Encode.forJavaScript(Encode.forHtmlContent(userAttribute))%></option><%} else {%><option value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(userAttribute))%>"><%=Encode.forJavaScript(Encode.forHtmlContent(userAttribute))%></option><%}}%></select></td>' +
                                '<td><%if (selectedRuleUserAttributeValue != null && selectedRuleUserAttributeValue.trim().length() > 0) {%><input type="text" name="userRuleAttributeValue_'  + index + '" id="userRuleAttributeValue_'  + index + '" value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(selectedRuleUserAttributeValue))%>"/><% } else {%><input type="text" name="userRuleAttributeValue_'  + index + '" id="userRuleAttributeValue_'  + index + '" /><%}%></td>' +
                                '<td> Environment </td>' +
                                '<td><select id="environmentRuleId_'  + index + '"  name="environmentRuleId_'  + index + '"  ><%for (String userAttribute : envAttributeIds) {if (selectedRuleEnvironmentId != null && userAttribute.equals(selectedRuleEnvironmentId)) {%><option value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(userAttribute))%>" selected="selected"><%=Encode.forJavaScript(Encode.forHtmlContent(userAttribute))%></option><%} else {%><option value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(userAttribute))%>"><%=Encode.forJavaScript(Encode.forHtmlContent(userAttribute))%></option><%}}%></select></td>' +
                                '<td><%if (selectedRuleEnvironmentValue != null && selectedRuleEnvironmentValue.trim().length() > 0) {%><input type="text" name="environmentRuleValue_'  + index + '"  id="environmentRuleValue_'  + index + '"  value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(selectedRuleEnvironmentValue))%>"/><%} else {%><input type="text" name="environmentRuleValue_'  + index + '"  id="environmentRuleValue_'  + index + '"  /><%}%></td>' +
                                '<td><a onclick="removeRow(this)" style="background-image:url(images/delete.gif);" type="button" class="icon-link"></a></td>' +
                                '</tr><tr></tr></table></td></tr>');
                    }
                    createActionRuleRow();
                </script>
        <%
                }
            }
        %>
        </table>
        </td>
        </tr>

        <%--********************--%>
        <%--********************--%>
        <%--END action policy type--%>
        <%--********************--%>
        <%--********************--%>
        <%--********************--%>


        <%--************************--%>
        <%--************************--%>
        <%--START environment policy type--%>
        <%--************************--%>
        <%--************************--%>
        <%--************************--%>

        <%
            } else if(PolicyEditorConstants.SOA_CATEGORY_ENVIRONMENT.equals(selectedPolicyApplied)){
        %>


        <tr>
        <td colspan="2">
        <table class="normal" style="padding-left:0px !important">
        <tr>
        <td>Environment which </td>
            <td>
                <select id="environmentId" name="environmentId"  >
                    <%
                    for (String userAttribute : envAttributeIds) {
                        if (selectedRuleEnvironmentId != null && userAttribute.equals(selectedRuleEnvironmentId)) {
                        %>
                            <option value="<%=Encode.forHtmlAttribute(userAttribute)%>"  selected="selected"><%=Encode.forHtmlContent(userAttribute)%></option>
                        <%
                        } else {
                        %>
                            <option value="<%=Encode.forHtmlAttribute(userAttribute)%>"><%=Encode.forHtmlContent(userAttribute)%></option>
                        <%
                        }
                    }
                    %>
                </select>
            </td>
            <td> is equals to </td>
            <%
                if (selectedEnvironmentValue != null && selectedEnvironmentValue.trim().length() > 0) {
            %>
                <td><input type="text" name="environmentValue" id="environmentValue" value="<%=Encode.forHtmlAttribute(selectedEnvironmentValue)%>"/></td>
            <%
                } else {
            %>
                <td><input type="text" name="environmentValue" id="environmentValue" /></td>
            <%
                }
            %>
        </tr>
        </table>
        </td>
        </tr>

        <tr>
        <td colspan="4">
        <table  id="environmentRuleTable" >
              <tr data-value="0">
              <td>
              <table class="oneline-listing">
              <tr></tr>
              <tr>
                  <td> Resource </td>
                  <td>
                  <%
                      if (selectedRuleResourceValue != null && selectedRuleResourceValue.trim().length() > 0) {
                  %>
                      <input type="text" name="resourceRuleValue_0" id="resourceRuleValue_0" value="<%=Encode.forHtmlAttribute(selectedRuleResourceValue)%>"/>
                  <%
                      } else {
                  %>
                      <input type="text" name="resourceRuleValue_0" id="resourceRuleValue_0" />
                  <%
                      }
                  %>
                  </td>
                  <td> User </td>
                  <td>
                      <select id="userRuleAttributeId_0" name="userRuleAttributeId_0"  >
                          <%
                              for (String userAttribute : userAttributeIds) {
                                  if (selectedRuleUserAttributeId != null && userAttribute.equals(selectedRuleUserAttributeId)) {
                          %>
                              <option value="<%=Encode.forHtmlAttribute(userAttribute)%>"  selected="selected"><%=Encode.forHtmlContent(userAttribute)%></option>
                          <%
                                  } else {
                          %>
                              <option value="<%=Encode.forHtmlAttribute(userAttribute)%>"><%=Encode.forHtmlContent(userAttribute)%></option>
                          <%
                                  }
                              }
                          %>
                      </select>
                  </td>
                  <td> <%
                      if (selectedRuleUserAttributeValue != null && selectedRuleUserAttributeValue.trim().length() > 0) {
                  %>
                      <input type="text" name="userRuleAttributeValue_0" id="userRuleAttributeValue_0" value="<%=Encode.forHtmlAttribute(selectedRuleUserAttributeValue)%>"/>
                  <%
                      } else {
                  %>
                      <input type="text" name="userRuleAttributeValue_0" id="userRuleAttributeValue_0" />
                  <%
                      }
                  %>
                  </td>
                  <td> Action </td>
                  <td>  <%
                      if (selectedRuleActionValue != null && selectedRuleActionValue.trim().length() > 0) {
                  %>
                      <input type="text" name="actionRuleValue_0" id="actionRuleValue_0" value="<%=Encode.forHtmlAttribute(selectedRuleActionValue)%>"/>
                  <%
                      } else {
                  %>
                      <input type="text" name="actionRuleValue_0" id="actionRuleValue_0" />
                  <%
                      }
                  %>
                  </td>
                  <td>
                      <a onclick="createNewEnvironmentRuleRow();" style="background-image:url(images/add.gif);float:none" type="button"
                         class="icon-link"></a>
                  </td>
              </tr>
              </table>
              </td>
              </tr>
<%
  if(elementDTOList != null && elementDTOList.size() > 0){
      elementDTOList.remove(0);
      for(SimplePolicyEditorElementDTO elementDTO : elementDTOList){
          selectedRuleActionValue = elementDTO.getActionValue();
          selectedRuleUserAttributeId = elementDTO.getUserAttributeId();
          selectedRuleUserAttributeValue = elementDTO.getUserAttributeValue();
          selectedRuleResourceValue = elementDTO.getResourceValue();
          selectedRuleEnvironmentValue= elementDTO.getEnvironmentValue();
          selectedRuleEnvironmentId= elementDTO.getEnvironmentId();
          selectedRuleOperationType= elementDTO.getOperationType();
          selectedRuleResourceFunction = elementDTO.getFunctionOnResources();
          selectedRuleUserFunction = elementDTO.getFunctionOnUsers();
%>
      <script type="text/javascript">
          function createEnvironmentRuleRow() {
              var rowIndex =  jQuery(document.getElementById('environmentRuleTable').rows[document.
                          getElementById('environmentRuleTable').rows.length-1]).attr('data-value');
              var index = parseInt(rowIndex, 10) + 1;
              jQuery('#environmentRuleTable > tbody:last').append('<tr data-value="'+ index +'"><td><table class="oneline-listing"><tr></tr><tr><td> Resource </td>' +
                  '<td><%if (selectedRuleResourceValue != null && selectedRuleResourceValue.trim().length() > 0) {%><input type="text" name="resourceRuleValue_'  + index + '"  id="resourceRuleValue_'  + index + '" value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(selectedRuleResourceValue))%>"/><%} else {%><input type="text" name="resourceRuleValue_'  + index + '"  id="resourceRuleValue_'  + index + '"  /><% }%> </td>' +
                  '<td> User </td>' +
                  '<td><select id="userRuleAttributeId_'  + index + '"  name="userRuleAttributeId_'  + index + '"  ><%for (String userAttribute : userAttributeIds) {if (selectedRuleUserAttributeId != null && userAttribute.equals(selectedRuleUserAttributeId)) {%><option value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(userAttribute))%>" selected="selected"><%=Encode.forJavaScript(Encode.forHtmlContent(userAttribute))%></option><%} else {%><option value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(userAttribute))%>"><%=Encode.forJavaScript(Encode.forHtmlContent(userAttribute))%></option><%}}%></select></td>' +
                  '<td><%if (selectedRuleUserAttributeValue != null && selectedRuleUserAttributeValue.trim().length() > 0) {%><input type="text" name="userRuleAttributeValue_'  + index + '"  id="userRuleAttributeValue_'  + index + '"  value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(selectedRuleUserAttributeValue))%>"/><%} else {%><input type="text" name="userRuleAttributeValue_'  + index + '"  id="userRuleAttributeValue_'  + index + '"  /><%}%></td>' +
                  '<td> Action </td>' +
                  '<td><%if (selectedRuleActionValue != null && selectedRuleActionValue.trim().length() > 0) {%><input type="text" name="actionRuleValue_'  + index + '"  id="actionRuleValue_'  + index + '"  value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(selectedRuleActionValue))%>"/><%} else {%><input type="text" name="actionRuleValue_'  + index + '"  id="actionRuleValue_'  + index + '"  /><%}%></td>' +
                  '<td><a onclick="removeRow(this)" style="background-image:url(images/delete.gif);" type="button" class="icon-link"></a></td>' +
                  '</tr></table></td></tr>');
          }
          createEnvironmentRuleRow();
      </script>
<%
      }
  }
%>
        </table>
        </td>
        </tr>
        <%--********************--%>
        <%--********************--%>
        <%--END environment policy type--%>
        <%--********************--%>
        <%--********************--%>
        <%--********************--%>


        <%--************************--%>
        <%--************************--%>
        <%--START Resource policy type--%>
        <%--************************--%>
        <%--************************--%>
        <%--************************--%>
        <%
        } else {
        %>

        <tr>
        <td colspan="2">
        <table class="normal" style="padding-left:0px !important">
        <tr>
            <td>Resource which is equals to</td>

            <%
                if (selectedResourceValue != null && selectedResourceValue.trim().length() > 0) {
            %>
                <td><input type="text" name="resourceValue" id="resourceValue" value="<%=Encode.forHtmlAttribute(selectedResourceValue)%>"/></td>
            <%
                } else {
            %>
                <td><input type="text" name="resourceValue" id="resourceValue" /></td>
            <%
                }
            %>
        </tr>
        </table>
        </td>
        </tr>

        <tr>
        <td colspan="4">
            <table  id="resourceRuleTable" >
                <tr data-value="0">
                    <td>
                        <table class="oneline-listing">
                            <tr>
                                <td style="white-space:nowrap;">Child resource</td>
                                <td>User</td>
                                <td></td>
                                <td>Action</td>
                                <td>Environment</td>
                                <td></td>
                            </tr>
                            <tr>
                                <td>
                                    <%
                                        if (selectedRuleResourceValue != null && selectedRuleResourceValue.trim().length() > 0) {
                                    %>
                                    <input type="text" name="resourceRuleValue_0" id="resourceRuleValue_0" value="<%=Encode.forHtmlAttribute(selectedRuleResourceValue)%>"/>
                                    <%
                                    } else {
                                    %>
                                    <input type="text" name="resourceRuleValue_0" id="resourceRuleValue_0" />
                                    <%
                                        }
                                    %>
                                </td>
                                <td>
                                    <select id="userRuleAttributeId_0" name="userRuleAttributeId_0"  >
                                        <%
                                            for (String userAttribute : userAttributeIds) {
                                                if (selectedRuleUserAttributeId != null && userAttribute.equals(selectedRuleUserAttributeId)) {
                                        %>
                                        <option value="<%=Encode.forHtmlAttribute(userAttribute)%>"  selected="selected"><%=Encode.forHtmlContent(userAttribute)%></option>
                                        <%
                                        } else {
                                        %>
                                        <option value="<%=Encode.forHtmlAttribute(userAttribute)%>"><%=Encode.forHtmlContent(userAttribute)%></option>
                                        <%
                                                }
                                            }
                                        %>
                                    </select>
                                </td>
                                <td> <%
                                    if (selectedRuleUserAttributeValue != null && selectedRuleUserAttributeValue.trim().length() > 0) {
                                %>
                                    <input type="text" name="userRuleAttributeValue_0" id="userRuleAttributeValue_0" value="<%=Encode.forHtmlAttribute(selectedRuleUserAttributeValue)%>"/>
                                    <%
                                    } else {
                                    %>
                                    <input type="text" name="userRuleAttributeValue_0" id="userRuleAttributeValue_0" />
                                    <%
                                        }
                                    %>
                                </td>
                                <td>  <%
                                    if (selectedRuleActionValue != null && selectedRuleActionValue.trim().length() > 0) {
                                %>
                                    <input type="text" name="actionRuleValue_0" id="actionRuleValue_0" value="<%=Encode.forHtmlAttribute(selectedRuleActionValue)%>"/>
                                    <%
                                    } else {
                                    %>
                                    <input type="text" name="actionRuleValue_0" id="actionRuleValue_0" />
                                    <%
                                        }
                                    %>
                                </td>
                                <td>
                                    <select id="environmentRuleId_0" name="environmentRuleId_0"  >
                                        <%
                                            for (String userAttribute : envAttributeIds) {
                                                if (selectedRuleEnvironmentId != null && userAttribute.equals(selectedRuleEnvironmentId)) {
                                        %>
                                        <option value="<%=Encode.forHtmlAttribute(userAttribute)%>"  selected="selected"><%=Encode.forHtmlContent(userAttribute)%></option>
                                        <%
                                        } else {
                                        %>
                                        <option value="<%=Encode.forHtmlAttribute(userAttribute)%>"><%=Encode.forHtmlContent(userAttribute)%></option>
                                        <%
                                                }
                                            }
                                        %>
                                    </select>
                                </td>
                                <td>  <%
                                    if (selectedRuleEnvironmentValue != null && selectedRuleEnvironmentValue.trim().length() > 0) {
                                %>
                                    <input type="text" name="environmentRuleValue_0" id="environmentRuleValue_0" value="<%=Encode.forHtmlAttribute(selectedRuleEnvironmentValue)%>"/>
                                    <%
                                    } else {
                                    %>
                                    <input type="text" name="environmentRuleValue_0" id="environmentRuleValue_0" />
                                    <%
                                        }
                                    %>
                                </td>

                                <td>
                                    <a onclick="createNewResourceRuleRow();" style="background-image:url(images/add.gif);float:none" type="button"
                                       class="icon-link"></a>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
                <%
                    if(elementDTOList != null && elementDTOList.size() > 0){
                        elementDTOList.remove(0);
                        for(SimplePolicyEditorElementDTO elementDTO : elementDTOList){
                            selectedRuleActionValue = elementDTO.getActionValue();
                            selectedRuleUserAttributeId = elementDTO.getUserAttributeId();
                            selectedRuleUserAttributeValue = elementDTO.getUserAttributeValue();
                            selectedRuleResourceValue = elementDTO.getResourceValue();
                            selectedRuleEnvironmentValue= elementDTO.getEnvironmentValue();
                            selectedRuleEnvironmentId= elementDTO.getEnvironmentId();
                            selectedRuleOperationType= elementDTO.getOperationType();
                            selectedRuleResourceFunction = elementDTO.getFunctionOnResources();
                            selectedRuleUserFunction = elementDTO.getFunctionOnUsers();
                %>
                <script type="text/javascript">
                    function createResourceRuleRow() {
                        var rowIndex =  jQuery(document.getElementById('resourceRuleTable').rows[document.
                                getElementById('resourceRuleTable').rows.length-1]).attr('data-value');
                        var index = parseInt(rowIndex, 10) + 1;

                        jQuery('#resourceRuleTable > tbody:last').append('<tr data-value="'+ index +'"><td><table class="oneline-listing"><tr><td style="white-space:nowrap;">Child resource</td><td>User</td><td></td><td>Action</td><td>Environment</td><td></td></tr>' +
                                '<td><%if (selectedRuleResourceValue != null && selectedRuleResourceValue.trim().length() > 0) {%><input type="text" name="resourceRuleValue_'  + index + '"  id="resourceRuleValue_'  + index + '" value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(selectedRuleResourceValue))%>"/><%} else {%><input type="text" name="resourceRuleValue_'  + index + '"  id="resourceRuleValue_'  + index + '"  /><% }%> </td>' +
                                '<td><select id="userRuleAttributeId_'  + index + '"  name="userRuleAttributeId_'  + index + '"  ><%for (String userAttribute : userAttributeIds) {if (selectedRuleUserAttributeId != null && userAttribute.equals(selectedRuleUserAttributeId)) {%><option value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(userAttribute))%>" selected="selected"><%=Encode.forJavaScript(Encode.forHtmlContent(userAttribute))%></option><%} else {%><option value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(userAttribute))%>"><%=Encode.forJavaScript(Encode.forHtmlContent(userAttribute))%></option><%}}%></select></td>' +
                                '<td><%if (selectedRuleUserAttributeValue != null && selectedRuleUserAttributeValue.trim().length() > 0) {%><input type="text" name="userRuleAttributeValue_'  + index + '"  id="userRuleAttributeValue_'  + index + '"  value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(selectedRuleUserAttributeValue))%>"/><%} else {%><input type="text" name="userRuleAttributeValue_'  + index + '"  id="userRuleAttributeValue_'  + index + '"  /><%}%></td>' +
                                '<td><%if (selectedRuleActionValue != null && selectedRuleActionValue.trim().length() > 0) {%><input type="text" name="actionRuleValue_'  + index + '"  id="actionRuleValue_'  + index + '"  value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(selectedRuleActionValue))%>"/><%} else {%><input type="text" name="actionRuleValue_'  + index + '"  id="actionRuleValue_'  + index + '"  /><%}%></td>' +
                                '<td><select id="environmentRuleId_'  + index + '"  name="environmentRuleId_'  + index + '"  ><%for (String userAttribute : envAttributeIds) {if (selectedRuleEnvironmentId != null && userAttribute.equals(selectedRuleEnvironmentId)) {%><option value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(userAttribute))%>" selected="selected"><%=Encode.forJavaScript(Encode.forHtmlContent(userAttribute))%></option><%} else {%><option value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(userAttribute))%>"><%=Encode.forJavaScript(Encode.forHtmlContent(userAttribute))%></option><%}}%></select></td>' +
                                '<td><%if (selectedRuleEnvironmentValue != null && selectedRuleEnvironmentValue.trim().length() > 0) {%><input type="text" name="environmentRuleValue_'  + index + '"  id="environmentRuleValue_'  + index + '"  value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(selectedRuleEnvironmentValue))%>"/><%} else {%><input type="text" name="environmentRuleValue_'  + index + '"  id="environmentRuleValue_'  + index + '"  /><%}%></td>' +
                                '<td><a onclick="removeRow(this)" style="background-image:url(images/delete.gif);" type="button" class="icon-link"></a></td>' +
                                '</tr></table></td></tr>');
                    }
                    createResourceRuleRow();
                </script>
                <%
                        }
                    }
                %>
            </table>
        </td>
        </tr>
        <%
            }
        %>
        <tr>
            <td class="buttonRow" colspan="2">
            <input type="button" onclick="doSubmit();" value="<fmt:message key="finish"/>"
                   class="button"/>
            <input type="button" onclick="doCancel();" value="<fmt:message key="cancel" />"
                   class="button"/>
            </td>
        </tr>
    </table>
    </form>
</div>
</div>
</fmt:bundle>
