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
<%@page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.identity.entitlement.stub.dto.EntitlementFinderDataHolder" %>
<%@ page import="org.wso2.carbon.identity.entitlement.stub.dto.PaginatedPolicySetDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyConstants" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.client.EntitlementPolicyAdminServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage"%>
<jsp:include page="../dialog/display_messages.jsp"/>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.owasp.encoder.Encode" %>
<jsp:useBean id="entitlementPolicyBean" type="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean"
             class="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean" scope="session"/>
<jsp:setProperty name="entitlementPolicyBean" property="*" />

<%
    // remove session attributes
    entitlementPolicyBean.cleanEntitlementPolicyBean();
    session.removeAttribute("publishAction");
    session.removeAttribute("policyVersion");
    session.removeAttribute("policyOrder");
    session.removeAttribute("publishAllPolicies");
    session.removeAttribute("selectedPolicies");
    session.removeAttribute("subscriberIds");
    session.removeAttribute("policyId");

    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    String forwardTo = null;
    PaginatedPolicySetDTO paginatedPolicySetDTO = null;

    PolicyDTO[] policies = null;
    String[] policyTypes = new String[] {"Policy", "PolicySet"};
    String BUNDLE = "org.wso2.carbon.identity.entitlement.ui.i18n.Resources";
	ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
    session.removeAttribute("publishAllPolicies");
    session.removeAttribute("selectedPolicies");
    session.removeAttribute(EntitlementPolicyConstants.ENTITLEMENT_PUBLISHER_MODULE);

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

    try {
        EntitlementPolicyAdminServiceClient client =
                new EntitlementPolicyAdminServiceClient(cookie, serverURL, configContext);
        paginatedPolicySetDTO = client.
                getAllPolicies(policyTypeFilter, policySearchString, pageNumberInt, false);
        EntitlementFinderDataHolder [] entitlementFinders = client.getEntitlementDataModules();
        if(entitlementFinders != null){
            for(EntitlementFinderDataHolder holder : entitlementFinders){
                entitlementPolicyBean.getEntitlementFinders().put(holder.getName(), holder);
            }
        }
        policies = paginatedPolicySetDTO.getPolicySet();
        numberOfPages = paginatedPolicySetDTO.getNumberOfPages();

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
            label="policy.administration"
            resourceBundle="org.wso2.carbon.identity.entitlement.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

<script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
<script type="text/javascript" src="../carbon/admin/js/main.js"></script>
<script type="text/javascript">

    var allPolicesSelected = false;        

    function removePolicies() {
        location.href = "remove-policy.jsp";
    }

    function edit(policy) {
        location.href = "edit-policy.jsp?policyid=" + policy;
    }

    function showVersions(policy){
        location.href = "show-policy-version.jsp?policyId=" + policy;
    }

    function viewStatus(policy) {
        location.href = "show-policy-status.jsp?policyid=" + policy;
    }

    function setPolicyCombineAlgorithm() {
        var comboBox = document.getElementById("globalAlgorithmName");
        var globalAlgorithmName = comboBox[comboBox.selectedIndex].value;
        location.href = 'index.jsp?globalAlgorithmName=' + globalAlgorithmName;
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
                document.policyForm.action = "remove-policy.jsp";
                document.policyForm.submit();
            });
        } else {
            CARBON.showConfirmationDialog("<fmt:message key="delete.services.on.page.prompt"/>",function() {
                document.policyForm.action = "remove-policy.jsp";
                document.policyForm.submit();
            });
        }
    }

    function publishPolicies(){

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
            CARBON.showInfoDialog('<fmt:message key="select.policies.to.be.published"/>');
            return;
        }
        if (allPolicesSelected) {
            CARBON.showConfirmationDialog("<fmt:message key="publish.all.policies.prompt"/>",function() {
                document.policyForm.action = "start-publish.jsp";
                document.policyForm.submit();
            });
        } else {
            CARBON.showConfirmationDialog("<fmt:message key="publish.services.on.page.prompt"/>",function() {
                document.policyForm.action = "start-publish.jsp";
                document.policyForm.submit();
            });
        }
    }

    function publishAllPolicies() {
        CARBON.showConfirmationDialog("<fmt:message key="publish.all.policies.prompt"/>",function() {
            location.href = "start-publish.jsp?publishAllPolicies=true";
        });                
    }

    function publishPolicy(policy) {
        location.href = "start-publish.jsp?policyId=" + policy;
    }

    function publishPolicyToPDP(policy) {
        location.href = "start-publish.jsp?toPDP=true&policyId=" + policy;
    }


    function tryPolicy(policy) {
        location.href = "create-evaluation-request.jsp?policyId=" + policy;
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
        location.href = 'index.jsp?policyTypeFilter=' + policyTypeFilter ;
    }

    function orderRuleElement(){
        var ruleElementOrder = new Array();
        var tmp = jQuery("#dataTable tbody tr input.chkBox");
        for (var i = 0 ; i < tmp.length; i++){
            ruleElementOrder.push(tmp[i].value);
        }
        return ruleElementOrder;
    }

</script>

<div id="middle">
    <h2><fmt:message key='policy.administration'/></h2>
    <div id="workArea">

    <%
        if (CarbonUIUtil.isUserAuthorized(request, "/permission/admin/configure/entitlement/policy/manage/add")) {
    %>
    <table style="border:none; margin-bottom:10px">
        <tr>
            <td>
                <div style="height:30px;">
                    <a href="javascript:document.location.href='add-policy.jsp'" class="icon-link"
                       style="background-image:url(../admin/images/add.gif);"><fmt:message key='add.new.ent.policy'/></a>
                </div>
            </td>
            <%--<td>--%>
                <%--<div style="height:30px;">--%>
                    <%--<a href="javascript:document.location.href='create-policy-set.jsp'" class="icon-link"--%>
                       <%--style="background-image:url(../admin/images/add.gif);"><fmt:message key='add.new.policy.set'/></a>--%>
                <%--</div>--%>
            <%--</td>            --%>
            <%--<td>--%>
                <%--<div style="height:30px;">--%>
                    <%--<a href="javascript:document.location.href='import-policy.jsp'" class="icon-link"--%>
                       <%--style="background-image:url(images/import.gif);"><fmt:message key='import.new.ent.policy'/></a>--%>
                <%--</div>--%>
            <%--</td>--%>
        </tr>
    </table>
    <%
        }
    %>

    <form action="index.jsp" name="searchForm" method="post">
        <table id="searchTable" name="searchTable" class="styledLeft" style="border:0;
                                                !important margin-top:10px;margin-bottom:10px;">
            <tr>
            <td>
                <table style="border:0; !important">
                    <tbody>
                    <tr style="border:0; !important">
                        <td style="border:0; !important">
                            <nobr>
                                <fmt:message key="policy.type"/>
                                <select name="policyTypeFilter" id="policyTypeFilter"  onchange="getSelectedPolicyType();">
                                    <%
                                        if (policyTypeFilter.equals("ALL")) {
                                    %>
                                    <option value="ALL" selected="selected"><fmt:message key="all"/></option>
                                    <%
                                    } else {
                                    %>
                                    <option value="ALL"><fmt:message key="all"/></option>
                                    <%
                                        }
                                        for (String policyType : policyTypes) {
                                            if (policyTypeFilter.equals(policyType)) {
                                    %>
                                    <option value="<%=Encode.forHtmlAttribute(policyType)%>" selected="selected"><%=Encode.forHtmlContent(policyType)%>
                                    </option>
                                    <%
                                    } else {
                                    %>
                                    <option value="<%=Encode.forHtmlAttribute(policyType)%>"><%=Encode.forHtmlContent(policyType)%>
                                    </option>
                                    <%
                                            }
                                        }
                                    %>
                                </select>
                                &nbsp;&nbsp;&nbsp;
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

    <table style="margin-top:10px;margin-bottom:10px">
        <tbody>
        <tr>
            <td>
                <a style="cursor: pointer;" onclick="selectAllInThisPage(true);return false;" href="#"><fmt:message key="selectAllInPage"/></a>
               &nbsp; | &nbsp;</td><td><a style="cursor: pointer;" onclick="selectAllInThisPage(false);return false;" href="#"><fmt:message key="selectNone"/></a>
            </td>
            <td width="20%">&nbsp;</td>
            <%
                if (CarbonUIUtil.isUserAuthorized(request, "/permission/admin/configure/entitlement/policy/manage/delete")) {
            %>
            <td>
                <a onclick="deleteServices();return false;"  href="#"  class="icon-link"
                   style="background-image: url(images/delete.gif);" ><fmt:message key="delete"/></a>
            </td>
            <%
                }
                if (CarbonUIUtil.isUserAuthorized(request, "/permission/admin/configure/entitlement/policy/publish")) {
            %>
            <td>
                <a onclick="publishPolicies();return false;"  href="#" class="icon-link"
                   style="background-image: url(images/publish.gif);" ><fmt:message key="publish.selected"/></a>
            </td>
            <%
                }
                if (CarbonUIUtil.isUserAuthorized(request, "/permission/admin/configure/entitlement/policy/publish")) {
            %>
            <td>
                <a onclick="publishAllPolicies();return false;"  class="icon-link" href="#"
                   style="background-image: url(images/publish-all.gif);" ><fmt:message key="publish.all.policies"/></a>
            </td>
            <%
                }
            %>
            <td width="20%">&nbsp;</td>
        </tr>
        </tbody>
    </table>

    <form action="" name="policyForm" method="post">
        <table style="width: 100%" id="dataTable" class="styledLeft">
            <thead>
            <tr>
                <th colspan="5"><fmt:message key='available.ent.policies'/></th>
            </tr>
            </thead>
            <tbody>
            <%
            if (policies != null) {
                for (int i = 0; i < policies.length; i++) {
                    if(policies[i] != null){
            %>
            <tr>
                <td width="10px" style="text-align:center; !important">
                    <input type="checkbox" name="policies"
                           value="<%=Encode.forHtmlAttribute(policies[i].getPolicyId())%>"
                           onclick="resetVars()" class="chkBox" />
                </td>

                <td>
                    <a href="policy-view.jsp?policyid=<%=Encode.forUriComponent(policies[i].getPolicyId())%>"><%=Encode.forHtmlContent(policies[i].getPolicyId())%></a>
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
                    boolean canEdit = CarbonUIUtil.isUserAuthorized(request, "/permission/admin/configure/entitlement/policy/manage/edit");
                    boolean canViewVersions = CarbonUIUtil.isUserAuthorized(request, "/permission/admin/configure/entitlement/policy/view");
                    boolean canPublish = CarbonUIUtil.isUserAuthorized(request, "/permission/admin/configure/entitlement/policy/publish");
                    boolean canTryIt  = CarbonUIUtil.isUserAuthorized(request, "/permission/admin/configure/entitlement/policy/manage/test");
                    boolean canViewStatus = CarbonUIUtil.isUserAuthorized(request, "/permission/admin/configure/entitlement/policy/view");
                %>


                <td width="60%">
                    <%
                        if (canEdit) {
                    %>
                    <a title="<fmt:message key='edit.policy'/>"
                       onclick="edit('<%=Encode.forJavaScriptAttribute(policies[i].getPolicyId())%>');return false;"
                    href="#" style="background-image: url(images/edit.gif);" class="icon-link">
                    <fmt:message key='edit'/></a>
                    <%
                        }
                        if (canViewVersions) {
                    %>
                    <a title="<fmt:message key='versions'/>"
                       onclick="showVersions('<%=Encode.forJavaScriptAttribute(policies[i].getPolicyId())%>');return false;"
                       href="#" style="background-image: url(images/edit.gif);" class="icon-link">
                        <fmt:message key='versions'/></a>
                    <%
                        }
                        if (canPublish) {
                    %>
                    <a title="<fmt:message key='publish.to.pdp'/>"   id="publish"
                       onclick="publishPolicyToPDP('<%=Encode.forJavaScriptAttribute(policies[i].getPolicyId())%>');return false;"
                       href="#" style="background-image: url(images/publish.gif);" class="icon-link">
                        <fmt:message key='publish.to.pdp'/></a>
                    <%
                        }
                        if (canTryIt) {
                    %>
                    <a title="<fmt:message key='try.this'/>"
                       onclick="tryPolicy('<%=Encode.forJavaScriptAttribute(policies[i].getPolicyId())%>');return false;"
                       href="#" style="background-image: url(images/evaluate.png);" class="icon-link">
                        <fmt:message key='try.this'/></a>
                    <%
                        }
                        if (canViewStatus) {
                    %>
                    <a title="<fmt:message key='view'/>"
                       onclick="viewStatus('<%=Encode.forJavaScriptAttribute(policies[i].getPolicyId())%>');return false;"
                       href="#" style="background-image: url(images/view.gif);" class="icon-link">
                        <fmt:message key='view.status'/></a>
                    <%
                        }
                    %>
                </td>
            </tr>
            <%} } } else { %>
            <tr>
                <td colspan="2"><fmt:message key='no.policies.defined'/></td>
            </tr>
            <%}%>
            </tbody>
        </table>
    </form>
    <carbon:paginator pageNumber="<%=pageNumberInt%>" numberOfPages="<%=numberOfPages%>"
                      page="index.jsp" pageNumberParameterName="pageNumber" parameters="<%=paginationValue%>"
                      resourceBundle="org.wso2.carbon.identity.entitlement.ui.i18n.Resources"
                      prevKey="prev" nextKey="next"/>
        </div>
    </div>
</fmt:bundle>
