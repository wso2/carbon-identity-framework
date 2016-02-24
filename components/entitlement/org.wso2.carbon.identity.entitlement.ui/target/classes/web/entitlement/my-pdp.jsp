<!--
 ~ Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.identity.entitlement.stub.dto.PaginatedPolicySetDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.PolicyEditorConstants" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.client.EntitlementAdminServiceClient" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.client.EntitlementPolicyAdminServiceClient"%>
<jsp:include page="../dialog/display_messages.jsp"/>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.owasp.encoder.Encode" %>

<%

    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    String forwardTo = null;
    PaginatedPolicySetDTO paginatedPolicySetDTO = null;
    String globalPolicyCombiningAlgorithm = null;
    String [] policyCombiningAlgorithms = new String[]{PolicyEditorConstants.CombiningAlog.DENY_OVERRIDE_ID,
                            PolicyEditorConstants.CombiningAlog.PERMIT_OVERRIDE_ID,
                            PolicyEditorConstants.CombiningAlog.FIRST_APPLICABLE_ID,
                            PolicyEditorConstants.CombiningAlog.PERMIT_UNLESS_DENY_ID,
                            PolicyEditorConstants.CombiningAlog.DENY_UNLESS_PERMIT_ID,
                            PolicyEditorConstants.CombiningAlog.ORDER_PERMIT_OVERRIDE_ID,
                            PolicyEditorConstants.CombiningAlog.ORDER_DENY_OVERRIDE_ID,
                            PolicyEditorConstants.CombiningAlog.ONLY_ONE_APPLICABLE_ID};

    PolicyDTO[] policies = null;
    String BUNDLE = "org.wso2.carbon.identity.entitlement.ui.i18n.Resources";
	ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
    globalPolicyCombiningAlgorithm = request.getParameter("globalAlgorithmName");

    int numberOfPages = 0;
    String pageNumber = request.getParameter("pageNumber");
    if (pageNumber == null) {
        pageNumber = "0";
    }
    int pageNumberInt = 0;
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
        policySearchString = "";
    } else {
        policySearchString = policySearchString.trim();
    }

    String paginationValue = "policyTypeFilter=" + policyTypeFilter +
                             "&policySearchString=" + policySearchString;

    try {
        EntitlementPolicyAdminServiceClient client =
                new EntitlementPolicyAdminServiceClient(cookie, serverURL, configContext);
        EntitlementAdminServiceClient adminClient =
                new EntitlementAdminServiceClient(cookie, serverURL, configContext);
        paginatedPolicySetDTO = client.
                getAllPolicies(policyTypeFilter, policySearchString, pageNumberInt, true);
        policies = paginatedPolicySetDTO.getPolicySet();
        numberOfPages = paginatedPolicySetDTO.getNumberOfPages();
        if(globalPolicyCombiningAlgorithm != null && globalPolicyCombiningAlgorithm.trim().length() > 0){
            adminClient.setGlobalPolicyAlgorithm(globalPolicyCombiningAlgorithm);
        } else {
            globalPolicyCombiningAlgorithm = adminClient.getGlobalPolicyAlgorithm();
        }

    } catch (Exception e) {
    	String message = resourceBundle.getString("error.while.loading.policy")+ " " + e.getMessage();
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request, e);
        forwardTo = "../admin/error.jsp";
%>

<script type="text/javascript">
    function forward() {
        location.href = "<%=forwardTo%>";
    }
</script>

<script type="text/javascript">
    forward();
</script>
<%
    }
%>

<fmt:bundle basename="org.wso2.carbon.identity.entitlement.ui.i18n.Resources">
    <carbon:breadcrumb
            label="my.pdp.policies"
            resourceBundle="org.wso2.carbon.identity.entitlement.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>"/>

<script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
<script type="text/javascript" src="../carbon/admin/js/main.js"></script>
<script type="text/javascript">

    var allPolicesSelected = false;        

    function setPolicyCombineAlgorithm() {
        var comboBox = document.getElementById("globalAlgorithmName");
        var globalAlgorithmName = comboBox[comboBox.selectedIndex].value;
        location.href = 'my-pdp.jsp?globalAlgorithmName=' + globalAlgorithmName;
    }

    function enable(policy) {
        location.href = "enable-disable-policy.jsp?policyid=" + policy +"&action=enable";
    }

    function disable(policy) {
        location.href = "enable-disable-policy.jsp?policyid=" + policy +"&action=disable";
    }


    function deletePolicy(policy) {
        CARBON.showConfirmationDialog("<fmt:message key="de.promote.policy.message"/>",function() {
            document.policyForm.action = "remove-policy.jsp?policyId=" + policy;
            document.policyForm.submit();
        });
    }

    function deleteServices() {
        var selected = false;
        if (document.policyForm.policies[0] != null) { // there is more than 1 policy
            for (var j = 0; j < document.policyForm.policies.length; j++) {
                selected = document.policyForm.policies[j].checked;
                if (selected) break;
            }
        } else if (document.policyForm.policies != null) { // only 1 policy
            selected = document.policyForm.policies.checked;
        }
        if (!selected) {
            CARBON.showInfoDialog('<fmt:message key="select.policies.to.be.deleted"/>');
            return;
        }
        if (allPolicesSelected) {
            CARBON.showConfirmationDialog("<fmt:message key="delete.all.policies.prompt"/>",function() {
                document.policyForm.action = "remove-policy.jsp?pdp=true";
                document.policyForm.submit();
            });
        } else {
            CARBON.showConfirmationDialog("<fmt:message key="delete.services.on.page.prompt"/>",function() {
                document.policyForm.action = "remove-policy.jsp?pdp=true";
                document.policyForm.submit();
            });
        }
    }

    function selectAllInThisPage(isSelected) {
        allPolicesSelected = false;
        if (document.policyForm.policies != null &&
            document.policyForm.policies[0] != null) { // there is more than 1 service
            if (isSelected) {
                for (var j = 0; j < document.policyForm.policies.length; j++) {
                    document.policyForm.policies[j].checked = true;
                }
            } else {
                for (j = 0; j < document.policyForm.policies.length; j++) {
                    document.policyForm.policies[j].checked = false;
                }
            }
        } else if (document.policyForm.policies != null) { // only 1 service
            document.policyForm.policies.checked = isSelected;
        }
        return false;
    }

    function selectAllInAllPages() {
        selectAllInThisPage(true);
        allPolicesSelected = true;
        return false;
    }

    function resetVars() {
        allPolicesSelected = false;

        var isSelected = false;
        if (document.policyForm.policies[0] != null) { // there is more than 1 service
            for (var j = 0; j < document.policyForm.policies.length; j++) {
                if (document.policyForm.policies[j].checked) {
                    isSelected = true;
                }
            }                           
        } else if (document.policyForm.policies != null) { // only 1 service
            if (document.policyForm.policies.checked) {
                isSelected = true;
            }
        }
        return false;
    }

    function searchServices() {
        document.searchForm.submit();
    }


    function getSelectedPolicyType() {
        var comboBox = document.getElementById("policyTypeFilter");
        var policyTypeFilter = comboBox[comboBox.selectedIndex].value;
        location.href = 'my-pdp.jsp?policyTypeFilter=' + policyTypeFilter ;
    }

    function saveOrder(row) {

        if (jQuery('#order_editBtn' + row).is(":visible")) {
            jQuery('#order_edit' + row).show();
            jQuery('#order_editBtn' + row).hide();
            jQuery('#order_saveBtn' + row).show();
            jQuery('#order_cancelBtn' + row).show();
        } else {
            jQuery('#order_edit' + row).hide();
            jQuery('#order_editBtn' + row).show();
            jQuery('#order_saveBtn' + row).hide();
            jQuery('#order_cancelBtn' + row).hide();
        }
    }


    function updateOrder(policyId, order) {
        saveOrder();
        location.href = 'update_order.jsp?policyId=' + Encode.forUriComponent(policyId) + "&order=" + Encode.forUriComponent(order);
    }
</script>

<div id="middle">
    <h2><fmt:message key='my.pdp.policy'/></h2>
    <div id="workArea">

    <%
        if (CarbonUIUtil.isUserAuthorized(request, "/permission/admin/configure/entitlement/policy/view")) {
    %>
    <table class="styledLeft" style="margin-top:10px;margin-bottom:10px;">
        <tr>
            <td>
            <table  style="border:0; !important" >
                <tr>
                <td style="border:0; !important"><fmt:message key="policy.combining.algorithm"/></td>
                <td style="border:0; !important">
                <select id="globalAlgorithmName" name="globalAlgorithmName" class="text-box-big">
                <%
                  if (policyCombiningAlgorithms != null && policyCombiningAlgorithms.length > 0) {
                      for (String algorithmName : policyCombiningAlgorithms) {
                          if(algorithmName.equals(globalPolicyCombiningAlgorithm)){
                %>
                      <option value="<%=Encode.forHtmlAttribute(algorithmName)%>" selected="selected"><%=Encode.forHtmlContent(globalPolicyCombiningAlgorithm)%></option>
                <%
                            } else {
                %>
                      <option value="<%=Encode.forHtmlAttribute(algorithmName)%>"><%=Encode.forHtmlContent(algorithmName)%></option>
                <%
                            }
                        }
                    }
                %>
                  </select>
                </td>
                <%
                    if (CarbonUIUtil.isUserAuthorized(request, "/permission/admin/configure/entitlement/pdp/manage")) {
                %>
                <td style="border:0; !important">
                    <input type="button" class="button"  tabindex="4" value="Update"
                           onclick="setPolicyCombineAlgorithm();"/>
                </td>
                <%
                    }
                %>
                </tr>
            </table>
            </td>
        </tr>
    </table>
    <%
        }
    %>

    <form action="my-pdp.jsp" name="searchForm" method="post">
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
    </form>

    <%--<table style="margin-top:10px;margin-bottom:10px">--%>
        <%--<tbody>--%>
        <%--<tr>--%>
            <%--<td>--%>
                <%--<a style="cursor: pointer;" onclick="selectAllInThisPage(true);return false;" href="#"><fmt:message key="selectAllInPage"/></a>--%>
               <%--&nbsp; | &nbsp;</td><td><a style="cursor: pointer;" onclick="selectAllInThisPage(false);return false;" href="#"><fmt:message key="selectNone"/></a>--%>
            <%--</td>--%>
            <%--<td width="20%">&nbsp;</td>--%>
            <%--<td>--%>
                <%--<a onclick="deleteServices();return false;"  href="#"  class="icon-link"--%>
                   <%--style="background-image: url(images/delete.gif);" ><fmt:message key="delete"/></a>--%>
            <%--</td>--%>
        <%--</tr>--%>
        <%--</tbody>--%>
    <%--</table>--%>

    <form action="" name="policyForm" method="post">
        <table style="width: 100%" id="dataTable" class="styledLeft">
            <thead>
            <tr>
                <th><fmt:message key='policy.order'/></th>
                <th><fmt:message key='id'/></th>
                <th><fmt:message key='type'/></th>
                <th><fmt:message key='actions'/></th>
            </tr>
            </thead>
            <tbody>
            <%
            if (policies != null) {
                for (int i = 0; i < policies.length; i++) {
                    if(policies[i] != null){
            %>
            <tr>
                <td width="30px" style="text-align:center; !important">
                    <%=policies[i].getPolicyOrder()%>
                </td>
                <td>
                    <a href="policy-view-pdp.jsp?policyId=<%=Encode.forUriComponent(policies[i].getPolicyId())%>">
                        <%=Encode.forUriComponent(policies[i].getPolicyId())%></a>
                </td>
                <td width="20px" style="text-align:left;">
                    <%
                        if(policies[i].getPolicyType() == null || "".equals(policies[i].getPolicyType())){
                            policies[i].setPolicyType("Policy");
                        }
                    %>
                    <nobr>
                        <img src="images/<%=Encode.forUriComponent(policies[i].getPolicyType())%>-type.gif"
                             title="<%=Encode.forHtmlAttribute(policies[i].getPolicyType())%>"
                             alt="<%=Encode.forHtmlAttribute(policies[i].getPolicyType())%>"/>
                        <%=Encode.forHtmlContent(policies[i].getPolicyType())%>
                    </nobr>
                </td>

                <%
                    boolean canEnable = CarbonUIUtil.isUserAuthorized(request, "/permission/admin/configure/entitlement/policy/manage/enable");
                    boolean canDemote = CarbonUIUtil.isUserAuthorized(request, "/permission/admin/configure/entitlement/policy/manage/demote");
                    boolean canOrder = CarbonUIUtil.isUserAuthorized(request, "/permission/admin/configure/entitlement/policy/manage/order");
                %>

                <td width="60%">
                    <%
                        if (canEnable) {
                            if (policies[i].getActive()) {
                    %>
                    <a title="<fmt:message key='disable.policy'/>"
                       onclick="disable('<%=Encode.forJavaScriptAttribute(policies[i].getPolicyId())%>');return false;"
                       href="#" style="background-image: url(images/disable.gif);" class="icon-link">
                        <fmt:message key='disable.policy'/></a>
                    <%
                            } else {
                    %>
                    <a title="<fmt:message key='enable.policy'/>"
                       onclick="enable('<%=Encode.forJavaScriptAttribute(policies[i].getPolicyId())%>');return false;"
                       href="#" style="background-image: url(images/enable.gif);" class="icon-link">
                        <fmt:message key='enable.policy'/></a>
                    <%
                            }
                        }
                        if (canDemote) {
                    %>
                    <a title="<fmt:message key='delete'/>"
                       onclick="deletePolicy('<%=Encode.forJavaScriptAttribute(policies[i].getPolicyId())%>');return false;"
                       href="#" style="background-image: url(images/delete.gif);" class="icon-link">
                        <fmt:message key='delete'/></a>
                    <%
                        }
                        if (canOrder) {
                    %>
                    <%--<div style="width:100%">--%>
                        <input style="float: left; display: none;margin-top:3px;"
                               id="order_edit<%=i%>" value="<%=policies[i].getPolicyOrder()%>">
                        &nbsp;
                        &nbsp;
                        &nbsp;
                        <a id="order_editBtn<%=i%>" class="icon-link" style="background-image: url(images/edit.gif);
                        display: block;" onclick="saveOrder('<%=i%>')"><fmt:message key="order"/></a>
                        <a class="icon-link" style="background-image: url(images/save.gif); display: none;" id="order_saveBtn<%=i%>"
                           onclick="updateOrder('<%=Encode.forJavaScriptAttribute(policies[i].getPolicyId())%>' ,document.getElementById('order_edit<%=i%>').value);">
                            <fmt:message key="save"/>
                        </a>
                        &nbsp;
                        &nbsp;
                        <a class="icon-link" style="background-image: url(images/cancel.gif);
                            display: none;" id="order_cancelBtn<%=i%>" onclick="saveOrder('<%=i%>')">
                                <fmt:message key="cancel"/>
                        </a>
                    <%--</div>--%>
                    <%
                        }
                    %>
                </td>
            </tr>
            <%} }
            } else { %>
            <tr>
                <td colspan="4"><fmt:message key='no.policies.defined'/></td>
            </tr>
            <%}%>
            </tbody>
        </table>
    </form>
    <carbon:paginator pageNumber="<%=pageNumberInt%>" numberOfPages="<%=numberOfPages%>"
                      page="my-pdp.jsp" pageNumberParameterName="pageNumber" parameters="<%=paginationValue%>"
                      resourceBundle="org.wso2.carbon.identity.entitlement.ui.i18n.Resources"
                      prevKey="prev" nextKey="next"/>
        </div>
    </div>
</fmt:bundle>
