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

<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.balana.utils.Constants.PolicyConstants" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.identity.entitlement.common.EntitlementConstants" %>
<%@ page import="org.wso2.carbon.identity.entitlement.common.PolicyEditorEngine" %>
<%@ page import="org.wso2.carbon.identity.entitlement.common.dto.PolicyEditorDataHolder" %>
<%@ page import="org.wso2.carbon.identity.entitlement.stub.dto.PaginatedPolicySetDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyConstants" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.PolicyEditorConstants" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.client.EntitlementPolicyAdminServiceClient" %>
<%@ page
        import="org.wso2.carbon.identity.entitlement.ui.dto.ObligationDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.PolicyRefIdDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.RowDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.TargetDTO" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="java.util.Set" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<jsp:useBean id="entitlementPolicyBean"
             type="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean"
             class="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean" scope="session"/>
<jsp:setProperty name="entitlementPolicyBean" property="*"/>


<%
    String BUNDLE = "org.wso2.carbon.identity.entitlement.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
    PolicyEditorDataHolder holder = PolicyEditorEngine.getInstance().
            getPolicyEditorData(EntitlementConstants.PolicyEditor.SET);
    if(holder == null){
        //String message = MessageFormat.format(resourceBundle.getString("no.policy.editor.data"));
        String message = "Policy Editor data can not loaded. Please check with policy editor configurations";
%>
<script type="text/javascript">
    jQuery(document).ready(function () {
        CARBON.showErrorDialog('<%=message%>',  function () {
            location.href = "role-mgt.jsp";
        });
    });
</script>
<%
    }

    String currentCategory = null;
    String currentPreFunction = null;
    String currentFunction = null;
    String currentAttributeValue =  null;
    String currentAttributeId =  null;
    String currentAttributeDataType = null;
    String currentCombineFunction = null;

    String currentObligationId = null;
    String currentObligationEffect = null;
    String currentObligationType = null;
    String currentObligationAttributeValue = null;
    String currentObligationAttributeId = null;

    String selectedAttributeNames = "";

    String[] ruleEffects = PolicyConstants.RuleEffect.effect;

    String[] combineFunctions = new String[] {PolicyEditorConstants.COMBINE_FUNCTION_END,
            PolicyEditorConstants.COMBINE_FUNCTION_AND, PolicyEditorConstants.COMBINE_FUNCTION_OR};

    Set<String> policyCombingAlgorithm = holder.getPolicyCombiningAlgorithms().keySet();

    String[] obligationTypes = new String[]{"Obligation", "Advice"};

    String selectedAttributeDataType = request.getParameter("selectedAttributeDataType");
    String selectedAttributeId = request.getParameter("selectedAttributeId");

    // These are pass as hidden values. So can contain null value ...
    if ("null".equals(selectedAttributeId)) {
        selectedAttributeId = null;
    }

    if ("null".equals(selectedAttributeDataType)) {
        selectedAttributeDataType = null;
    }

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


    Set<String> categories = holder.getCategoryMap().keySet();
    String[] targetPreFunctions = new String[]{"is"};
    Set<String> targetFunctions = holder.getTargetFunctions();

    List<PolicyRefIdDTO> policyIds = entitlementPolicyBean.getPolicyRefIds();
    TargetDTO targetDTO = entitlementPolicyBean.getTargetDTO();
    List<ObligationDTO> obligationDTOs = entitlementPolicyBean.getObligationDTOs();

    int numberOfPages = 0;
    int pageNumberInt = 0;
    String pageNumber = request.getParameter("pageNumber");
    if (pageNumber == null) {
        pageNumber = "0";
    }
    try {
        pageNumberInt = Integer.parseInt(pageNumber);
    } catch (NumberFormatException ignored) {
    }

    String policyTypeFilter = request.getParameter("policyTypeFilter");
    if (policyTypeFilter == null || "".equals(policyTypeFilter)) {
        policyTypeFilter = "ALL";
    }
    String policySearchString = request.getParameter("policySearchString");
    if (policySearchString == null) {
        policySearchString = "*";
    } else {
        policySearchString = policySearchString.trim();
    }

    String paginationValue = "policyTypeFilter=" + policyTypeFilter +
            "&policySearchString=" + policySearchString;

    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    String forwardTo = null;
    PaginatedPolicySetDTO paginatedPolicySetDTO = null;
    org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO[] policies = null;
    try {
        EntitlementPolicyAdminServiceClient client =
                new EntitlementPolicyAdminServiceClient(cookie, serverURL, configContext);
        paginatedPolicySetDTO = client.
                getAllPolicies(policyTypeFilter, policySearchString, pageNumberInt, false);
        policies = paginatedPolicySetDTO.getPolicySet();
        numberOfPages = paginatedPolicySetDTO.getNumberOfPages();
    } catch (Exception e){
        //ignore
    }
%>

<script type="text/javascript">


    function removeRow(link){
        link.parentNode.parentNode.parentNode.removeChild(link.parentNode.parentNode);

        jQuery(jQuery('.newTargetRow')[parseInt(jQuery('.newTargetRow').length, 10) - 1 ]).removeAttr('disabled');
        jQuery(jQuery('.newTargetRow')[parseInt(jQuery('.newTargetRow').length, 10) - 1 ]).val('END');

    }

    function createNewTargetRow(value) {
        if (value == "AND" || value == "OR"){
            jQuery('.newTargetRow').attr('disabled','disabled');
            var rowIndex =  jQuery('#multipleTargetTable tr').last().attr('data-value');
            var index = parseInt(rowIndex, 10) + 1;
            jQuery('#multipleTargetTable > tbody:last').append('<tr data-value="'+ index +'"><td style="padding-left:0px !important;padding-right:0px !important"><select id="targetCategory_'  + index + '" name="targetCategory_'  + index + '" > <%for (String category : categories) { if(currentCategory != null && category.equals(currentCategory)){%> <option value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(category))%>" selected="selected"><%=Encode.forJavaScript(Encode.forHtmlContent(currentCategory))%> </option> <%} else {%> <option value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(category))%>"><%=Encode.forJavaScript(Encode.forHtmlContent(category))%> </option> <%} }%></select></td>' +
                    '<td style="padding-left:0px !important;padding-right:0px !important"><select id="targetPreFunction_'  + index + '" name="targetPreFunction_'  + index + '"><%for (String targetPreFunction : targetPreFunctions) {if (currentPreFunction != null && targetPreFunction.equals(currentPreFunction)) {%><option value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(targetPreFunction))%>" selected="selected"><%=Encode.forJavaScript(Encode.forHtmlContent(targetPreFunction))%></option><%} else {%><option value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(targetPreFunction))%>"><%=Encode.forJavaScript(Encode.forHtmlContent(targetPreFunction))%></option><%}}%></select></td>' +
                    '<td style="padding-left:0px !important;padding-right:0px !important"><select id="targetFunction_'  + index + '" name="targetFunction_'  + index + '" class="leftCol-small"><%for (String targetFunction : targetFunctions) {if (currentFunction != null && targetFunction.equals(currentFunction)) {%><option value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(targetFunction))%>" selected="selected"><%=Encode.forJavaScript(Encode.forHtmlContent(targetFunction))%></option><%} else {%><option value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(targetFunction))%>"><%=Encode.forJavaScript(Encode.forHtmlContent(targetFunction))%></option><%}}%></select></td>' +
                    '<td style="padding-left:0px !important;padding-right:0px !important"><%if (currentAttributeValue != null && !"".equals(currentAttributeValue)) {%><input type="text" size="60" name="targetAttributeValue_'  + index + '" id="targetAttributeValue_'  + index + '" value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(currentAttributeValue))%>" class="text-box-big"/><%} else {%><input type="text" size="60" name="targetAttributeValue_'  + index + '" id="targetAttributeValue_'  + index + '" class="text-box-big"/><%}%></td>' +
                    '<td><a title="Select Resources Names" class="icon-link" onclick="selectAttributesForTarget(' + index + ');" style="background-image:url(images/registry.gif);"></a></td>' +
                    '<td style="padding-left:0px !important;padding-right:0px !important"><select id="targetCombineFunctions_'  + index + '" name="targetCombineFunctions_'  + index + '" class="newTargetRow" onchange="createNewTargetRow(this.options[this.selectedIndex].value)"><%for (String combineFunction : combineFunctions) {if (currentCombineFunction != null && combineFunction.equals(currentCombineFunction)) {%><option value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(combineFunction))%>" selected="selected"><%=Encode.forJavaScript(Encode.forHtmlContent(combineFunction))%></option><%} else {%><option value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(combineFunction))%>"><%=Encode.forJavaScript(Encode.forHtmlContent(combineFunction))%></option><%}}%></select></td>' +
                    '<td><a onclick="removeRow(this)" style="background-image:url(images/delete.gif);" type="button" class="icon-link"></a></td>' +
                    '<td><input type="hidden" name="targetAttributeId_'  + index +  '" id="targetAttributeId_'  + index + '" value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(currentAttributeId))%>"/></td>' +
                    '<td><input type="hidden" name="targetAttributeTypes_'  + index + '" id="targetAttributeTypes_'  + index +  '" value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(currentAttributeDataType))%>"/></td>' +
                    '</tr>');
        }
    }

    function createNewObligationRow() {
        var rowIndex =  jQuery('#obligationTable tr').last().attr('data-value');
        var index = parseInt(rowIndex, 10) + 1;
        jQuery('#obligationTable > tbody:last').append('<tr data-value="'+ index +'">' +
                '<td style="padding-left:0px !important;padding-right:0px !important"><select id="obligationType_' + index +'" name="obligationType_' + index +'"><%for (String type : obligationTypes) {if (currentObligationType != null && type.equals(currentObligationType)) {%><option value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(type))%>" selected="selected"><%=Encode.forJavaScript(Encode.forHtmlContent(type))%></option><%} else {%><option value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(type))%>"><%=Encode.forJavaScript(Encode.forHtmlContent(type))%></option><%}}%></select></td>' +
                '<td style="padding-left:0px !important;padding-right:0px !important"><%if (currentObligationId != null && currentObligationId.trim().length() > 0) {%><input type="text" size="20" name="obligationId_' + index +'" id="obligationId_' + index +'" value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(currentObligationId))%>" /><%} else {%><input type="text" size="20" name="obligationId_' + index +'" id="obligationId_' + index +'" /><%}%></td>'+
                '<td style="padding-left:0px !important;padding-right:0px !important"><select id="obligationEffect_' + index +'" name="obligationEffect_' + index +'"><%if (ruleEffects != null) {for (String effect : ruleEffects) {if (effect.equals(currentObligationEffect)) {%><option value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(effect))%>" selected="selected"><%=Encode.forJavaScript(Encode.forHtmlContent(effect))%></option><%} else {%><option value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(effect))%>"><%=Encode.forJavaScript(Encode.forHtmlContent(effect))%></option><%}}}%></select></td>' +
                '<td style="padding-left:0px !important;padding-right:0px !important"><%if (currentObligationAttributeValue != null && currentObligationAttributeValue.trim().length() > 0) {%><input type="text" size="40" name="obligationAttributeValue_" '+ index +' id="obligationAttributeValue_" '+ index +' value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(currentObligationAttributeValue))%>" /> <%} else {%><input type="text" size="40" name="obligationAttributeValue_' + index +'" id="obligationAttributeValue_' + index +'" /><%}%></td>'+
                <%--'<td style="padding-left:0px !important;padding-right:0px !important"><%if (currentObligationAttributeId != null && currentObligationAttributeId.trim().length() > 0) {%><input type="text" size="40" name="obligationAttributeId_' + index +'" id="obligationAttributeId_' + index +'" value="<%=currentObligationAttributeId%>" /><%} else {%><input type="text" size="40" name="obligationAttributeId_' + index +'" id="obligationAttributeId_' + index +'"/><%}%></td>' +--%>
                '<td><a onclick="removeRow(this)" style="background-image:url(images/delete.gif);" type="button" class="icon-link"></a></td>' +
                '</tr>');
    }

</script>

<%
    if(targetDTO != null){
        List<RowDTO> rowDTOs = targetDTO.getRowDTOList();
        if(rowDTOs != null  && rowDTOs.size() > 0){
            RowDTO rowDTO = rowDTOs.get(0);
            currentCategory = rowDTO.getCategory();
            currentPreFunction = rowDTO.getPreFunction();
            currentFunction = rowDTO.getFunction();
            if(rowDTO.isNotCompleted()){
                if(rowDTO.getAttributeValue() != null && rowDTO.getAttributeValue().trim().length() > 0){
                    if(selectedAttributeNames != null && selectedAttributeNames.trim().length() > 0){
                        currentAttributeValue = rowDTO.getAttributeValue() + "," + selectedAttributeNames;
                    } else {
                        currentAttributeValue = rowDTO.getAttributeValue();
                    }
                } else {
                    currentAttributeValue = selectedAttributeNames;
                }
                currentAttributeId = selectedAttributeId;
                currentAttributeDataType = selectedAttributeDataType;
            } else {
                currentAttributeValue =  rowDTO.getAttributeValue();
                currentAttributeId =  rowDTO.getAttributeId();
                currentAttributeDataType = rowDTO.getAttributeDataType();
            }
            currentCombineFunction =  rowDTO.getCombineFunction();
        }
    }


    if(obligationDTOs != null && obligationDTOs.size() > 0){
        ObligationDTO dto = obligationDTOs.get(0);
        currentObligationType = dto.getType();
        currentObligationId = dto.getObligationId();
        currentObligationEffect = dto.getEffect();
        currentObligationAttributeValue = dto.getAttributeValue();
        currentObligationAttributeId = dto.getResultAttributeId();
    } else {
        obligationDTOs = null;
    }

%>

<fmt:bundle basename="org.wso2.carbon.identity.entitlement.ui.i18n.Resources">
<% if(entitlementPolicyBean.isEditPolicy()){%>
<carbon:breadcrumb
        label="edit.xacml.policy.set"
        resourceBundle="org.wso2.carbon.identity.entitlement.ui.i18n.Resources"
        topPage="true"
        request="<%=request%>"/>
<% } else { %>
<carbon:breadcrumb
        label="create.policy.set"
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

    function submitForm() {
        preSubmit();
        if (doValidationPolicyNameOnly()) {
            document.dataForm.action = "update-policy-set.jsp?nextPage=finish-policy-set";
            document.dataForm.submit();
        }
    }

    function doCancel() {
        location.href = 'index.jsp';
    }

    function doValidation() {

        var value = document.getElementsByName("policyName")[0].value;
        if (value == '') {
            CARBON.showWarningDialog('<fmt:message key="policy.name.is.required"/>');
            return false;
        }

        if(!value.match(new RegExp(regString))) {
            CARBON.showWarningDialog('<fmt:message key="policy.name.is.conformance"/>');
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

        if(!value.match(new RegExp(regString))) {
            CARBON.showWarningDialog('<fmt:message key="policy.name.is.conformance"/>');
            return false;
        }
        return true;
    }

    function deletePolicyRef(policyId) {
        preSubmit();
        document.dataForm.action = "update-policy-set.jsp?delete=true&policyRefId=" + policyId;
        document.dataForm.submit();
    }


    function preSubmit(){

        var  multipleTargetTable = jQuery('#multipleTargetTable tr').last().attr('data-value');
        var  obligationTable = jQuery('#obligationTable tr').last().attr('data-value');

        var array =  jQuery('.newTargetRow');
        for(var i = 0; i < array.length; i++){
            jQuery(array[i]).removeAttr('disabled');
        }

        var policyReferenceOrder = new Array();
        var tmp = jQuery("#dataTable tbody tr input");
        for (var j = 0; j < tmp.length; j++) {
            policyReferenceOrder.push(tmp[j].value);
        }

        jQuery('#mainTable > tbody:last').append('<tr><td><input type="hidden" name="maxTargetRows" id="maxTargetRows" value="' + multipleTargetTable +'"/></td></tr>');
        jQuery('#mainTable > tbody:last').append('<tr><td><input type="hidden" name="maxObligationRows" id="maxObligationRows" value="' + obligationTable +'"/></td></tr>') ;
        jQuery('#mainTable > tbody:last').append('<tr><td><input type="hidden" name="policyReferenceOrder" id="policyReferenceOrder" value="' + policyReferenceOrder +'"/></td></tr>') ;
    }

    function selectAttributesForTarget(index) {
        preSubmit();
        if (doValidationPolicyNameOnly()) {
            document.dataForm.action = "update-policy-set.jsp?nextPage=select-attribute&targetRowIndex="
                    + index;
            document.dataForm.submit();
        }
    }

    function updownthis(thislink, updown) {
        var sampleTable = document.getElementById('dataTable');
        var clickedRow = thislink.parentNode.parentNode;
        var addition = -1;
        if (updown == "down") {
            addition = 1;
        }
        var otherRow = sampleTable.rows[clickedRow.rowIndex + addition];
        var numrows = jQuery("#dataTable tbody tr").length;
        if (numrows <= 1) {
            return;
        }
        if (clickedRow.rowIndex == 1 && updown == "up") {
            return;
        } else if (clickedRow.rowIndex == numrows && updown == "down") {
            return;
        }
        var rowdata_clicked = new Array();
        for (var i = 0; i < clickedRow.cells.length; i++) {
            rowdata_clicked.push(clickedRow.cells[i].innerHTML);
            clickedRow.cells[i].innerHTML = otherRow.cells[i].innerHTML;
        }
        for (i = 0; i < otherRow.cells.length; i++) {
            otherRow.cells[i].innerHTML = rowdata_clicked[i];
        }
    }

    function searchServices() {
        preSubmit();
        if (doValidationPolicyNameOnly()) {
            document.dataForm.action = "update-policy-set.jsp?nextPage=create-policy-set";
            document.dataForm.submit();
        }
    }


    function doPaginate(page, pageNumberParameterName, pageNumber){
        preSubmit();
        if (doValidationPolicyNameOnly()) {
            document.dataForm.action =  "update-policy-set.jsp?nextPage=create-policy-set&" +
                    pageNumberParameterName + "=" + pageNumber + "&";
            document.dataForm.submit();
        }
    }

    function add(policyId, policyType){
        preSubmit();
//        jQuery('#policyTable > tbody:last').
//                append('<tr><td><input type="hidden" name="policyRefId" id="policyRefId" value="' + policyId +'"/></td></tr>');
//        jQuery('#policyTable > tbody:last').
//                append('<tr><td><input type="hidden" name="policyType" id="policyType" value="' + policyType +'"/></td></tr>');


        var tmp = jQuery("#dataTable tbody tr input");
        for (var j = 0; j < tmp.length; j++) {
            if((tmp[j].value == policyId)){
                CARBON.showWarningDialog('<fmt:message key="policy.id.is.existing"/>');
                return;
            }
        }

        if (doValidationPolicyNameOnly()) {
            document.dataForm.action = "update-policy-set.jsp?policyRefId=" + policyId +
                    "&policyType=" + policyType + "&nextPage=create-policy-set";
            document.dataForm.submit();
        }
    }

</script>
<div id="middle">
<%if(entitlementPolicyBean.isEditPolicy()){%>
<h2><fmt:message key="edit.xacml.policy.set"/></h2>
<%} else {%><h2><fmt:message key="create.policy.set"/></h2><%}%>
<div id="workArea">
<form id="dataForm" name="dataForm" method="post" action="">
<table id="mainTable" class="styledLeft noBorders">
<tr>
    <td class="leftCol-med"><fmt:message key='policy.set.name'/><span class="required">*</span></td>
    <%
        if (entitlementPolicyBean.getPolicyName() != null) {
    %>
    <td><input type="text" name="policyName" id="policyName"
               value="<%=Encode.forHtmlAttribute(entitlementPolicyBean.getPolicyName())%>" class="text-box-big"/></td>
    <%
    } else {
    %>
    <td><input type="text" name="policyName" id="policyName" class="text-box-big"/></td>
    <%
        }
    %>
</tr>

<tr>
    <td><fmt:message key="policy.combining.algorithm"/></td>
    <td>
        <select id="algorithmName" name="algorithmName" class="text-box-big">
            <%
                if (policyCombingAlgorithm != null && policyCombingAlgorithm.size() > 0) {
                    for (String algorithmName : policyCombingAlgorithm) {
                        if (algorithmName.equals(entitlementPolicyBean.getAlgorithmName())) {
            %>
            <option value="<%=Encode.forHtmlAttribute(algorithmName)%>"
                    selected="selected"><%=Encode.forHtmlContent(entitlementPolicyBean.getAlgorithmName())%>
            </option>
            <%
            } else {
            %>
            <option value="<%=Encode.forHtmlAttribute(algorithmName)%>"><%=Encode.forHtmlContent(algorithmName)%>
            </option>
            <%
                        }
                    }
                }
            %>
        </select>
    </td>
</tr>

<tr>
    <td class="leftCol-small" style="vertical-align:top !important"><fmt:message
            key='policy.set.description'/></td>
    <%
        if (entitlementPolicyBean.getPolicyDescription() != null) {
    %>
    <td><textarea name="policyDescription" id="policyDescription"
                  value="<%=Encode.forHtmlAttribute(entitlementPolicyBean.getPolicyDescription())%>"
                  class="text-box-big"><%=Encode.forHtmlContent(entitlementPolicyBean.getPolicyDescription())%>
    </textarea></td>
    <%
    } else {
    %>
    <td><textarea type="text" name="policyDescription" id="policyDescription"
                  class="text-box-big"></textarea></td>
    <%
        }
    %>
</tr>


<tr>
<td colspan="2">
<script type="text/javascript">
    jQuery(document).ready(function() {
        <%if(targetDTO == null){%>
        jQuery("#newTargetLinkRow").hide();
        <%}else{ %>
        jQuery("#newTargetLinkRow").show();
        <% } %>

        <%if(policySearchString == null || policySearchString.trim().length() == 0){%>
        jQuery("#newPolicyRefLinkRow").hide();
        <%}else{ %>
        jQuery("#newPolicyRefLinkRow").show();
        <% } %>


        <%if(obligationDTOs == null || obligationDTOs.size() == 0){%>
        jQuery("#newObligationLinkRow").hide();
        <%}else{ %>
        jQuery("#newObligationLinkRow").show();
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
<h2 class="trigger  <%if(targetDTO == null){%>active<%} %>"><a
        href="#"><fmt:message key="policy.apply.to"/></a></h2>

<div class="toggle_container" style="padding:0;margin-bottom:10px;" id="newTargetLinkRow">

    <table class="normal" style="padding-left:0px !important">
        <tr>
            <td>
                <table id="multipleTargetTable" name="multipleTargetTable" class="normal"
                       style="padding-left:0px !important">
                    <tr data-value="0">

                        <td style="padding-left:0px !important;padding-right:0px !important">
                            <select id="targetCategory_0" name="targetCategory_0">
                                <%
                                    for (String category : categories) {
                                        if (currentCategory != null && category.equals(currentCategory)) {
                                %>
                                <option value="<%=Encode.forHtmlAttribute(category)%>" selected="selected"><%=Encode.forHtmlContent(category)%></option>
                                <%
                                } else {
                                %>
                                <option value="<%=Encode.forHtmlAttribute(category)%>"><%=Encode.forHtmlContent(category)%></option>
                                <%
                                        }
                                    }
                                %>
                            </select>
                        </td>


                        <td style="padding-left:0px !important;padding-right:0px !important">
                            <select id="targetPreFunction_0" name="targetPreFunction_0">
                                <%
                                    for (String targetPreFunction : targetPreFunctions) {
                                        if (currentPreFunction != null && targetPreFunction.equals(currentPreFunction)) {
                                %>
                                <option value="<%=Encode.forHtmlAttribute(targetPreFunction)%>"
                                        selected="selected"><%=Encode.forHtmlContent(targetPreFunction)%>
                                </option>
                                <%
                                } else {
                                %>
                                <option value="<%=Encode.forHtmlAttribute(targetPreFunction)%>"><%=Encode.forHtmlContent(targetPreFunction)%>
                                </option>
                                <%
                                        }
                                    }
                                %>
                            </select>
                        </td>


                        <td style="padding-left:0px !important;padding-right:0px !important">
                            <select id="targetFunction_0" name="targetFunction_0"
                                    class="leftCol-small">
                                <%
                                    for (String targetFunction : targetFunctions) {
                                        if (currentFunction != null && targetFunction.equals(currentFunction)) {
                                %>
                                <option value="<%=Encode.forHtmlAttribute(targetFunction)%>"
                                        selected="selected"><%=Encode.forHtmlContent(targetFunction)%>
                                </option>
                                <%
                                } else {
                                %>
                                <option value="<%=Encode.forHtmlAttribute(targetFunction)%>"><%=Encode.forHtmlContent(targetFunction)%>
                                </option>
                                <%
                                        }
                                    }
                                %>
                            </select>
                        </td>


                        <td style="padding-left:0px !important;padding-right:0px !important">
                            <%
                                if (currentAttributeValue != null && !"".equals(currentAttributeValue)) {

                            %>
                            <input type="text" size="60" name="targetAttributeValue_0"
                                   id="targetAttributeValue_0"
                                   value="<%=Encode.forHtmlAttribute(currentAttributeValue)%>" class="text-box-big"/>
                            <%
                            } else {
                            %>
                            <input type="text" size="60" name="targetAttributeValue_0"
                                   id="targetAttributeValue_0" class="text-box-big" />

                            <%
                                }
                            %>
                        </td>
                        <td>
                            <a title="Select Resources Names" class='icon-link'
                               onclick='selectAttributesForTarget(0);'
                               style='background-image:url(images/registry.gif);'></a>
                        </td>

                        <td style="padding-left:0px !important;padding-right:0px !important">
                            <select id="targetCombineFunctions_0" name="targetCombineFunctions_0"
                                    class="newTargetRow" onchange="createNewTargetRow(this.options[this.selectedIndex].value)">
                                <%
                                    for (String combineFunction : combineFunctions) {
                                        if (currentCombineFunction != null && combineFunction.equals(currentCombineFunction)) {
                                %>
                                <option value="<%=Encode.forHtmlAttribute(combineFunction)%>"
                                        selected="selected"><%=Encode.forHtmlContent(combineFunction)%>
                                </option>
                                <%
                                } else {
                                %>
                                <option value="<%=Encode.forHtmlAttribute(combineFunction)%>"><%=Encode.forHtmlContent(combineFunction)%>
                                </option>
                                <%
                                        }
                                    }
                                %>
                            </select>
                        </td>
                        <td>
                            <input type="hidden" name="targetAttributeId_0"
                                   id="targetAttributeId_0" value="<%=Encode.forHtmlAttribute(currentAttributeId)%>"/>
                        </td>

                        <td>
                            <input type="hidden" name="targetAttributeTypes_0"
                                   id="targetAttributeTypes_0"
                                   value="<%=Encode.forHtmlAttribute(currentAttributeDataType)%>"/>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
    </table>
    <%
        if(targetDTO != null){
            List<RowDTO> rowDTOs = targetDTO.getRowDTOList();
            if(rowDTOs != null && rowDTOs.size() > 0){
                //rowDTOs.remove(0);
                for(int i = 1; i < rowDTOs.size(); i ++){
                    RowDTO rowDTO = rowDTOs.get(i);
                    currentCategory = rowDTO.getCategory();
                    currentPreFunction = rowDTO.getPreFunction();
                    currentFunction = rowDTO.getFunction();
                    if(rowDTO.isNotCompleted()){
                        if(rowDTO.getAttributeValue() != null && rowDTO.getAttributeValue().trim().length() > 0){
                            if(selectedAttributeNames != null && selectedAttributeNames.trim().length() > 0){
                                currentAttributeValue = rowDTO.getAttributeValue() + "," + selectedAttributeNames;
                            } else {
                                currentAttributeValue = rowDTO.getAttributeValue();
                            }
                        } else {
                            currentAttributeValue = selectedAttributeNames;
                        }
                        currentAttributeId = selectedAttributeId;
                        currentAttributeDataType = selectedAttributeDataType;
                    } else {
                        currentAttributeValue =  rowDTO.getAttributeValue();
                        currentAttributeId =  rowDTO.getAttributeId();
                        currentAttributeDataType = rowDTO.getAttributeDataType();
                    }
                    currentCombineFunction =  rowDTO.getCombineFunction();

    %>
    <script type="text/javascript">
        function createNextTargetRow() {
            jQuery('.newTargetRow').attr('disabled','disabled');
            var rowIndex =  jQuery('#multipleTargetTable tr').last().attr('data-value');
            var index = parseInt(rowIndex, 10) + 1;
            jQuery('#multipleTargetTable > tbody:last').append('<tr  data-value="' + index +'"><td style="padding-left:0px !important;padding-right:0px !important"><select id="targetCategory_'  + index + '" name="targetCategory_'  + index + '" > <%for (String category : categories) { if(currentCategory != null && category.equals(currentCategory)){%> <option value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(category))%>" selected="selected"><%=Encode.forJavaScript(Encode.forHtmlContent(currentCategory))%> </option> <%} else {%> <option value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(category))%>"><%=Encode.forJavaScript(Encode.forHtmlContent(category))%> </option> <%} }%></select></td>' +
                    '<td style="padding-left:0px !important;padding-right:0px !important"><select id="targetPreFunction_'  + index + '" name="targetPreFunction_'  + index + '" ><%for (String targetPreFunction : targetPreFunctions) {if (currentPreFunction != null && targetPreFunction.equals(currentPreFunction)) {%><option value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(targetPreFunction))%>" selected="selected"><%=Encode.forJavaScript(Encode.forHtmlContent(targetPreFunction))%></option><%} else {%><option value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(targetPreFunction))%>"><%=Encode.forJavaScript(Encode.forHtmlContent(targetPreFunction))%></option><%}}%></select></td>' +
                    '<td style="padding-left:0px !important;padding-right:0px !important"><select id="targetFunction_'  + index + '" name="targetFunction_'  + index + '" class="leftCol-small"><%for (String targetFunction : targetFunctions) {if (currentFunction != null && targetFunction.equals(currentFunction)) {%><option value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(targetFunction))%>" selected="selected"><%=Encode.forJavaScript(Encode.forHtmlContent(targetFunction))%></option><%} else {%><option value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(targetFunction))%>"><%=Encode.forJavaScript(Encode.forHtmlContent(targetFunction))%></option><%}}%></select></td>' +
                    '<td style="padding-left:0px !important;padding-right:0px !important"><%if (currentAttributeValue != null && !"".equals(currentAttributeValue)) {%><input type="text" size="60" name="targetAttributeValue_'  + index + '" id="targetAttributeValue_'  + index + '" value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(currentAttributeValue))%>" class="text-box-big"/><%} else {%><input type="text" size="60" name="targetAttributeValue_'  + index + '" id="targetAttributeValue_'  + index + '" class="text-box-big"/><%}%></td>' +
                    '<td><a title="Select Resources Names" class="icon-link" onclick="selectAttributesForTarget(' + index + ');" style="background-image:url(images/registry.gif);"></a></td>' +
                    '<td style="padding-left:0px !important;padding-right:0px !important"><select id="targetCombineFunctions_'  + index + '" name="targetCombineFunctions_'  + index + '" class="newTargetRow " onchange="createNewTargetRow(this.options[this.selectedIndex].value)"><%for (String combineFunction : combineFunctions) {if (currentCombineFunction != null && combineFunction.equals(currentCombineFunction)) {%><option value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(combineFunction))%>" selected="selected"><%=Encode.forJavaScript(Encode.forHtmlContent(combineFunction))%></option><%} else {%><option value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(combineFunction))%>"><%=Encode.forJavaScript(Encode.forHtmlContent(combineFunction))%></option><%}}%></select></td>' +
                    '<td><a onclick="removeRow(this)" style="background-image:url(images/delete.gif);" type="button" class="icon-link"></a></td>' +
                    '<td><input type="hidden" name="targetAttributeId_'  + index +  '" id="targetAttributeId_'  + index + '" value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(currentAttributeId))%>"/></td>' +
                    '<td><input type="hidden" name="targetAttributeTypes_'  + index + '" id="targetAttributeTypes_'  + index +  '" value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(currentAttributeDataType))%>"/></td>' +
                    '</tr>');
        }
        createNextTargetRow();
    </script>
    <%
                }
            }
        }
    %>
</div>
</td>
</tr>

<tr>
    <td colspan="2" style="margin-top:10px;">
        <h2 class="trigger  <%if(obligationDTOs == null || obligationDTOs.size() == 0){%>active<%} %>"><a href="#"><fmt:message
                key="add.new.obligations"/></a></h2>
        <div class="toggle_container" id="newObligationLinkRow">

            <table class="ob-table">
                <td style="width:152px;">Obligation Type</td>
                <td style="width:120px;">Id</td>
                <td style="width:100px;">Effect </td>
                <td style="width:270px;">Attribute Value </td>
            </table>
            <table id="obligationTable" name="obligationTable" class="normal"
                   style="padding-left:0px !important">
                <tr data-value="0">
                    <td style="padding-left:0px !important;padding-right:0px !important">
                        <select id="obligationType_0" name="obligationType_0">
                            <%
                                for (String type : obligationTypes) {
                                    if (currentObligationType != null && type.equals(currentObligationType)) {
                            %>
                            <option value="<%=Encode.forHtmlAttribute(type)%>"
                                    selected="selected"><%=Encode.forHtmlContent(type)%>
                            </option>
                            <%
                            } else {
                            %>
                            <option value="<%=Encode.forHtmlAttribute(type)%>"><%=Encode.forHtmlContent(type)%></option>
                            <%
                                    }
                                }
                            %>
                        </select>
                    </td>

                    <td style="padding-left:0px !important;padding-right:0px !important">
                        <%
                            if (currentObligationId != null && currentObligationId.trim().length() > 0) {
                        %>
                        <input type="text" size="20" name="obligationId_0" id="obligationId_0"
                               value="<%=Encode.forHtmlAttribute(currentObligationId)%>" />
                        <%
                        } else {
                        %>
                        <input type="text" size="20" name="obligationId_0" id="obligationId_0" />
                        <%
                            }
                        %>
                    </td>

                    <td style="padding-left:0px !important;padding-right:0px !important">
                        <select id="obligationEffect_0" name="obligationEffect_0">
                            <%
                                if (ruleEffects != null) {
                                    for (String effect : ruleEffects) {
                                        if (effect.equals(currentObligationEffect)) {
                            %>
                            <option value="<%=Encode.forHtmlAttribute(effect)%>" selected="selected"><%=Encode.forHtmlContent(effect)%></option>
                            <%
                            } else {
                            %>
                            <option value="<%=Encode.forHtmlAttribute(effect)%>"><%=Encode.forHtmlContent(effect)%></option>
                            <%
                                        }
                                    }
                                }
                            %>
                        </select>
                    </td>

                    <td style="padding-left:0px !important;padding-right:0px !important">
                        <%
                            if (currentObligationAttributeValue != null && currentObligationAttributeValue.trim().length() > 0) {
                        %>
                        <input type="text" size="40" name="obligationAttributeValue_0" id="obligationAttributeValue_0"
                               value="<%=Encode.forHtmlAttribute(currentObligationAttributeValue)%>" />
                        <%
                        } else {
                        %>
                        <input type="text" size="40" name="obligationAttributeValue_0" id="obligationAttributeValue_0" />
                        <%
                            }
                        %>
                        <a onclick="createNewObligationRow();" style="background-image:url(images/add.gif);float:none" type="button"
                           class="icon-link"></a>
                    </td>
                </tr>
                <%
                    if(obligationDTOs != null && obligationDTOs.size() > 0){
                        //obligationDTOs.remove(0);
                        for(int i = 1; i < obligationDTOs.size(); i++){
                            ObligationDTO dto = obligationDTOs.get(i);
                            currentObligationType = dto.getType();
                            currentObligationId = dto.getObligationId();
                            currentObligationEffect = dto.getEffect();
                            currentObligationAttributeValue = dto.getAttributeValue();
                            currentObligationAttributeId = dto.getResultAttributeId();
                %>
                <script type="text/javascript">
                    function createObligationRow() {
                        var rowIndex =  jQuery('#obligationTable tr').last().attr('data-value');
                        var index = parseInt(rowIndex, 10) + 1;
                        jQuery('#obligationTable > tbody:last').append('<tr data-value="'+ index +'">'   +
                                '<td style="padding-left:0px !important;padding-right:0px !important"><select id="obligationType_' + index +'" name="obligationType_' + index +'" ><%for (String type : obligationTypes) {if (currentObligationType != null && type.equals(currentObligationType)) {%><option value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(type))%>" selected="selected"><%=Encode.forJavaScript(Encode.forHtmlContent(type))%></option><%} else {%><option value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(type))%>"><%=Encode.forJavaScript(Encode.forHtmlContent(type))%></option><%}}%></select></td>' +
                                '<td style="padding-left:0px !important;padding-right:0px !important"><%if (currentObligationId != null && currentObligationId.trim().length() > 0) {%><input type="text" size="20" name="obligationId_' + index +'" id="obligationId_' + index +'" value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(currentObligationId))%>" /><%} else {%><input type="text" size="20" name="obligationId_' + index +'" id="obligationId_' + index +'" /><%}%></td>'+
                                '<td style="padding-left:0px !important;padding-right:0px !important"><select id="obligationEffect_' + index +'" name="obligationEffect_' + index +'" ><%if (ruleEffects != null) {for (String effect : ruleEffects) {if (effect.equals(currentObligationEffect)) {%><option value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(effect))%>" selected="selected"><%=Encode.forJavaScript(Encode.forHtmlContent(effect))%></option><%} else {%><option value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(effect))%>"><%=Encode.forJavaScript(Encode.forHtmlContent(effect))%></option><%}}}%></select></td>'+
                                '<td style="padding-left:0px !important;padding-right:0px !important"><%if (currentObligationAttributeValue != null && currentObligationAttributeValue.trim().length() > 0) {%><input type="text" size="40" name="obligationAttributeValue_' + index +'" id="obligationAttributeValue_' + index +'" value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(currentObligationAttributeValue))%>" /> <%} else {%><input type="text" size="40" name="obligationAttributeValue_' + index +'" id="obligationAttributeValue_' + index +'"/><%}%></td>'+
                                <%--'<td style="padding-left:0px !important;padding-right:0px !important"><%if (currentObligationAttributeId != null && currentObligationAttributeId.trim().length() > 0) {%><input type="text" size="40" name="obligationAttributeId_' + index +'" id="obligationAttributeId_' + index +'" value="<%=currentObligationAttributeId%>" /><%} else {%><input type="text" size="40" name="obligationAttributeId_' + index +'" id="obligationAttributeId_' + index +'"/><%}%></td>' +--%>
                                '<td><a onclick="removeRow(this)" style="background-image:url(images/delete.gif);" type="button" class="icon-link"></a></td>' +
                                '</tr>');
                    }
                    createObligationRow();
                </script>
                <%
                        }
                    }
                %>
            </table>
        </div>
    </td>
</tr>

<tr>
    <td colspan="2" style="margin-top:10px;">
        <h2 class="trigger  <%if(policySearchString == null ||
                policySearchString.trim().length() == 0){%>active<%} %>"><a href="#"><fmt:message
                key="add.new.policy.references"/></a></h2>
        <div class="toggle_container" id="newPolicyRefLinkRow">
        <table id="searchTable" name="searchTable" class="styledLeft" style="border:0;
                                            !important margin-top:10px;margin-bottom:10px;">
            <tr>
                <td>
                    <table style="border:0; !important">
                        <tbody>
                        <tr style="border:0; !important">
                            <td style="border:0; !important">
                                <nobr>
                                    <fmt:message key="search.policy"/>
                                    <input type="text" name="policySearchString"
                                           value="<%= policySearchString != null? Encode.forHtmlAttribute(policySearchString) :""%>"/>&nbsp;
                                </nobr>
                            </td>
                            <td style="border:0; !important">
                                <a class="icon-link" href="#" style="background-image: url(images/search.gif);"
                                   onclick="searchServices(); return false;"
                                   alt="<fmt:message key="search"/>"></a>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </td>
            </tr>
        </table>

        <table style="width: 100%" id="policyTable" class="styledLeft">
            <tbody>
            <%
                if (policies != null) {
                    for (int i = 0; i < policies.length; i++) {
                        if(policies[i] != null){
            %>
            <tr>
                <td>
                    <%=Encode.forHtmlContent(policies[i].getPolicyId())%>
                </td>
                <td width="50%">
                    <a onclick="add('<%=Encode.forJavaScriptAttribute(policies[i].getPolicyId())%>', '<%=Encode.forJavaScriptAttribute(policies[i].getPolicyType())%>');return false;"
                       href="#" style="background-image: url(images/add.gif);" class="icon-link">
                        <fmt:message key='add'/></a>
                </td>
            </tr>
            <%          }
                    }
                } else { %>
            <tr>
                <td colspan="2"><fmt:message key='no.policies.reference.defined'/></td>
            </tr>
                <%}%>
            </tbody>
        </table>
        <carbon:paginator pageNumber="<%=pageNumberInt%>" numberOfPages="<%=numberOfPages%>"
                          action="post"
                          page="" pageNumberParameterName="pageNumber" parameters="<%=paginationValue%>"
                          resourceBundle="org.wso2.carbon.identity.entitlement.ui.i18n.Resources"
                          prevKey="prev" nextKey="next"/>
    </div>
    </td>
</tr>


<tr>
    <td colspan="5">
        <table id="dataTable" style="width: 100%;margin-top:10px;">
            <thead>
            <tr>
                <th><fmt:message key="policy.id"/></th>
                <th><fmt:message key="policy.reference"/></th>
                <th><fmt:message key="action"/></th>
            </tr>
            </thead>
            <%
                if (policyIds != null && policyIds.size() > 0) {
                    List<PolicyRefIdDTO> orderedPolicyDTOs = new ArrayList<PolicyRefIdDTO>();
                    String policyReferenceOrder = entitlementPolicyBean.getPolicyReferenceOrder();
                    if (policyReferenceOrder != null) {
                        String[] orderedRuleIds = policyReferenceOrder.split(EntitlementPolicyConstants.ATTRIBUTE_SEPARATOR);
                        for (String orderedRuleId : orderedRuleIds) {
                            for (PolicyRefIdDTO dto : policyIds) {
                                if (orderedRuleId.trim().equals(dto.getId())) {
                                    orderedPolicyDTOs.add(dto);
                                }
                            }
                        }
                    }

                    if (orderedPolicyDTOs.size() < 1) {
                        orderedPolicyDTOs = policyIds;
                    }
                    for (PolicyRefIdDTO orderedRuleDTO : orderedPolicyDTOs) {
            %>
            <tr>

                <td>
                    <a class="icon-link" onclick="updownthis(this,'up')"
                       style="background-image:url(../admin/images/up-arrow.gif)"></a>
                    <a class="icon-link" onclick="updownthis(this,'down')"
                       style="background-image:url(../admin/images/down-arrow.gif)"></a>
                    <input type="hidden" value="<%=orderedRuleDTO.getId()%>"/>
                    <%=orderedRuleDTO.getId()%>
                </td>
                <td><%=orderedRuleDTO.isReferenceOnly()%>
                </td>
                <td>
                    <a href="#" onclick="deletePolicyRef('<%=Encode.forJavaScriptAttribute(orderedRuleDTO.getId())%>')"
                       class="icon-link" style="background-image:url(images/delete.gif);"><fmt:message
                            key="delete"/></a>
                </td>
            </tr>
            <%
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
        <input type="button" onclick="submitForm();" value="<fmt:message key="finish"/>"
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