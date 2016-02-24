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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.identity.entitlement.stub.dto.PaginatedPolicySetDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.client.EntitlementPolicyAdminServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil"%><jsp:include page="../dialog/display_messages.jsp"/>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.owasp.encoder.Encode" %>
<jsp:useBean id="entitlementPolicyBean" type="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean"
             class="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean" scope="session"/>
<jsp:setProperty name="entitlementPolicyBean" property="*" />

<%
    entitlementPolicyBean.cleanEntitlementPolicyBean();
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    String forwardTo = null;
    PaginatedPolicySetDTO paginatedPolicySetDTO = null;
    PolicyDTO[] policies = null;
    String[] policyTypes = new String[] {"Policy", "PolicySet", "Active" , "Promoted"};
    String BUNDLE = "org.wso2.carbon.identity.entitlement.ui.i18n.Resources";
	ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

    String type = "role";
    String userName = request.getParameter("userName");
    String value = request.getParameter("roleName");
    if(userName != null && userName.trim().length() > 0 ) {
        type = "user";
        value = userName;
    }

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
        EntitlementPolicyAdminServiceClient client = new EntitlementPolicyAdminServiceClient(cookie, serverURL, configContext);
        paginatedPolicySetDTO = client.getAllPolicies(policyTypeFilter, policySearchString, pageNumberInt, false);
        policies = paginatedPolicySetDTO.getPolicySet();
        numberOfPages = paginatedPolicySetDTO.getNumberOfPages();

    } catch (Exception e) {
    	String message = resourceBundle.getString("error.while.loading.policy");
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
            label="ent.policies"
            resourceBundle="org.wso2.carbon.identity.entitlement.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>"/>

<script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
<script type="text/javascript" src="../carbon/admin/js/main.js"></script>
<script type="text/javascript">

    function edit(policy) {
        location.href = "edit-policy.jsp?policyid=" + policy;
    }

    function authorized(policy) {
        location.href = "authorize-policy.jsp?policyid=" + policy + "&type=" + type + "&value=" + value + "&rule=permit";
    }

    function setPolicyCombineAlgorithm() {
        var comboBox = document.getElementById("globalAlgorithmName");
        var globalAlgorithmName = comboBox[comboBox.selectedIndex].value;
        location.href = 'index.jsp?globalAlgorithmName=' + globalAlgorithmName;
    }


    function searchServices() {

        jQuery('#searchTable > tbody:last').append('<tr><td><input type="hidden" name="policyOrder" id="policyOrder" value="' + orderRuleElement() +'"/></td></tr>') ;
        document.searchForm.submit();
    }

    function addPermission(){
         location.href = 'premission-select.jsp";    
    }

</script>

<div id="middle">
    <h2><fmt:message key='configure.authorization'/></h2>
    <div id="workArea">

    <table style="border:none; margin-bottom:10px">
        <tr>
            <td>
                <div style="height:30px;">
                    <a href="javascript:document.location.href='simple-policy-editor.jsp'" class="icon-link"
                       style="background-image:url(../admin/images/add.gif);"><fmt:message key='add.new.ent.policy'/></a>
                </div>
            </td>
        </tr>
    </table>


    <form action="index.jsp" name="searchForm">
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
                                    <option value="<%= policyType%>" selected="selected"><%= policyType%>
                                    </option>
                                    <%
                                    } else {
                                    %>
                                    <option value="<%= policyType%>"><%= policyType%>
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
                    <tr>
                             <a class="icon-link" href="#" style="background-image: url(images/add.gif);"
                                   onclick="addPermission(); return false;"
                                   alt="<fmt:message key="add"/>"></a>
                    </tr>
                    </tbody>
                </table>
            </td>
            </tr>
        </table>
    </form>


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
                        if(!"Policy".equals(policies[i].getPolicyType())){
                            continue;
                        }

                    boolean edit = policies[i].getPolicyEditable();
                    boolean delete = policies[i].getPolicyCanDelete();
            %>
            <tr>
                <td style="width:100px;">
                    <a class="icon-link" onclick="updownthis(this,'up',<%=Encode.forJavaScriptAttribute(request.getParameter("pageNumber"))%>,
                            <%=numberOfPages%>)" style="background-image:url(../admin/images/up-arrow.gif)"></a>
                    <a class="icon-link" onclick="updownthis(this,'down',<%=Encode.forJavaScriptAttribute(request.getParameter("pageNumber"))%>,
                            <%=numberOfPages%>)" style="background-image:url(../admin/images/down-arrow.gif)"></a>
                    <input type="hidden" value="<%=policies[i].getPolicyId()%>"/>                    
                </td>

                <td width="10px" style="text-align:center; !important">
                    <input type="checkbox" name="policies"
                           value="<%=Encode.forHtmlAttribute(policies[i].getPolicyId())%>"
                           onclick="resetVars()" class="chkBox" <% if(!delete){%>disabled="disabled"<% } %>/>
                </td>

                <td>
                    <a <% if(edit) { %>href="policy-view.jsp?policyid=<%=Encode.forUriComponent(policies[i].getPolicyId())%>" <% } %>>
                        <%=Encode.forHtmlContent(policies[i].getPolicyId())%></a>
                </td>

                <td width="40%">
                    <a title="<fmt:message key='edit.policy'/>" onclick="edit('<%=Encode.forJavaScriptAttribute(Encode.forUriComponent(policies[i].getPolicyId()))%>');return false;"
                     href="#" style="background-image: url(images/edit.gif);" class="icon-link"> <fmt:message key='edit'/></a>
                </td>
            </tr>
            <%} }
            } else { %>
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
