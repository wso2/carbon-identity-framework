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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
	prefix="carbon"%>
<%@ page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ page import="org.wso2.carbon.CarbonConstants"%>
<%@ page import="org.wso2.carbon.identity.entitlement.stub.dto.PDPDataHolder"%>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.client.EntitlementAdminServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@page import="org.wso2.carbon.utils.ServerConstants"%>
<%@ page import="java.util.ResourceBundle" %>
<%

    PDPDataHolder pdpDataHolder = null;
    String[] pipAttributeFinders = null;
    String[] pipResourceFinders = null;
    String[] policyFinders = null;
    String forwardTo;


    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.
                    CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    String BUNDLE = "org.wso2.carbon.identity.entitlement.ui.i18n.Resources";
	ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
    try {
        EntitlementAdminServiceClient client = new EntitlementAdminServiceClient(cookie,
                serverURL, configContext);

        pdpDataHolder = client.getPDPData();
        if(pdpDataHolder != null){
            pipAttributeFinders = pdpDataHolder.getPipAttributeFinders();
            pipResourceFinders = pdpDataHolder.getPipResourceFinders();
            policyFinders = pdpDataHolder.getPolicyFinders();
        }
    } catch (Exception e) {
%>
<script type="text/javascript">
    CARBON.showErrorDialog('<%=e.getMessage()%>', function () {
        location.href = "index.jsp";
    });
</script>
<%
    }
%>
<fmt:bundle basename="org.wso2.carbon.identity.entitlement.ui.i18n.Resources">
<carbon:breadcrumb
		label="pdp.configuration"
		resourceBundle="org.wso2.carbon.identity.entitlement.ui.i18n.Resources"
		topPage="true"
		request="<%=request%>" />

    <script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
    <script type="text/javascript" src="resources/js/main.js"></script>
    <!--Yahoo includes for dom event handling-->
    <script src="../yui/build/yahoo-dom-event/yahoo-dom-event.js" type="text/javascript"></script>
    <script src="../entitlement/js/create-basic-policy.js" type="text/javascript"></script>
    <link href="../entitlement/css/entitlement.css" rel="stylesheet" type="text/css" media="all"/>

<script type="text/javascript">

    function doCancel(){
        location.href = 'index.jsp?';
    }

    function viewFinder(finder, type) {
        location.href = "view-finder.jsp?finderId=" + finder + "&type=" + type;
    }

    function refreshFinder(finder, type){
        CARBON.showConfirmationDialog("<fmt:message key='refresh.finder'/>",
                function() {
                    location.href = "refresh-finder.jsp?finderId=" + finder  + "&type=" + type;
                }, null);
    }


    function clearCache() {
        CARBON.showConfirmationDialog("<fmt:message key='cache.clear.message'/>",
                function() {
                    location.href = "clear-cache.jsp";
                }, null);
    }

    function clearAttributeCache() {
        CARBON.showConfirmationDialog("<fmt:message key='attribute.cache.clear.message'/>",
                function() {
                    location.href = "clear-attribute-cache.jsp";
                }, null);
    }
    
</script>

<div id="middle">
    <h2><fmt:message key="pdp.configuration"/></h2>
    <div id="workArea">
        <%
            if (CarbonUIUtil.isUserAuthorized(request, "/permission/admin/configure/entitlement/pdp/manage")) {
        %>
        <table style="border:none; margin-bottom:10px">
            <tr>
                <td>
                    <div style="height:30px;">
                        <a href="#" class="icon-link" onclick="clearCache();return false;"
                           style="background-image:url(images/cleanCache.png);"><fmt:message key='ent.clear.cache'/></a>
                    </div>
                </td>
                <td>
                    <div style="height:30px;">
                        <a href="#" class="icon-link" onclick="clearAttributeCache();return false;"
                           style="background-image:url(images/cleanCache.png);"><fmt:message key='ent.clear.attribute.cache'/></a>
                    </div>
                </td>
            </tr>
        </table>
        <%
            }
        %>

        <table  class="styledLeft"  style="width: 100%;margin-top:10px;">
            <thead>
                <tr>
                    <th colspan='2'><fmt:message key='policy.finder'/></th>
                </tr>
            </thead>
            <tbody>
            <%
                if(policyFinders != null){
                    for(String policyFinder : policyFinders){
            %>
                <tr>
                    <td class="leftCol-med"><%=policyFinder%></td>
                    <td>
                        <a onclick="viewFinder('<%=policyFinder%>', 'policy');return false;"
                        href="#" style="background-image: url(images/view.png);" class="icon-link">
                        <fmt:message key='view'/></a>
                        <%
                            if (CarbonUIUtil.isUserAuthorized(request, "/permission/admin/configure/entitlement/pdp/manage")) {
                        %>
                        <a onclick="refreshFinder('<%=policyFinder%>', 'policy');return false;"
                        href="#" style="background-image: url(images/icon-refresh.gif);" class="icon-link">
                        <fmt:message key='refresh'/></a>
                        <%
                            }
                        %>
                    </td>
                </tr>
                <%
                        }
                    }
                %>
            </tbody>
        </table>

        <table  class="styledLeft"  style="width: 100%;margin-top:10px;">
            <thead>
                <tr>
                    <th colspan='2'><fmt:message key='attribute.finer'/></th>
                </tr>
            </thead>
            <tbody>
            <%
                if(pipAttributeFinders != null){
                    for(String pipAttributeFinder : pipAttributeFinders){
            %>
                <tr>
                <td class="leftCol-med"><%=pipAttributeFinder%></td>
                    <td>
                        <a onclick="viewFinder('<%=pipAttributeFinder%>', 'attribute');return false;"
                        href="#" style="background-image: url(images/view.png);" class="icon-link">
                        <fmt:message key='view'/></a>
                        <%
                            if (CarbonUIUtil.isUserAuthorized(request, "/permission/admin/configure/entitlement/pdp/manage")) {
                        %>
                        <a onclick="refreshFinder('<%=pipAttributeFinder%>', 'attribute');return false;"
                        href="#" style="background-image: url(images/icon-refresh.gif);" class="icon-link">
                        <fmt:message key='refresh'/></a>
                        <%
                            }
                        %>
                    </td>
                </tr>
                <%
                        }
                    }
                %>
            </tbody>
        </table>

        <table  class="styledLeft"  style="width: 100%;margin-top:10px;">
            <thead>
                <tr>
                    <th colspan='2'><fmt:message key='resource.finder'/></th>
                </tr>
            </thead>
            <tbody>
            <%
                if(pipResourceFinders != null){
                    for(String pipResourceFinder : pipResourceFinders){
            %>
                <tr>
                    <td class="leftCol-med"><%=pipResourceFinder%></td>
                    <td>
                        <a onclick="viewFinder('<%=pipResourceFinder%>', 'resource');return false;"
                        href="#" style="background-image: url(images/view.png);" class="icon-link">
                        <fmt:message key='view'/></a>
                        <%
                            if (CarbonUIUtil.isUserAuthorized(request, "/permission/admin/configure/entitlement/pdp/manage")) {
                        %>
                        <a onclick="refreshFinder('<%=pipResourceFinder%>', 'resource');return false;"
                        href="#" style="background-image: url(images/icon-refresh.gif);" class="icon-link">
                        <fmt:message key='refresh'/></a>
                        <%
                            }
                        %>
                    </td>
                </tr>
                <%
                        }
                    }
                %>
            </tbody>
        </table>

    </div>
</div>
</fmt:bundle>
