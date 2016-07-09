<%--
  ~ Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~ WSO2 Inc. licenses this file to you under the Apache License,
  ~ Version 2.0 (the "License"); you may not use this file except
  ~ in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  --%>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.xsd.ApplicationBasicInfo" %>
<%@ page import="org.wso2.carbon.identity.application.mgt.ui.client.ApplicationManagementServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.owasp.encoder.Encode" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
           prefix="carbon" %>

<script type="text/javascript" src="extensions/js/vui.js"></script>
<script type="text/javascript" src="../extensions/core/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>

<jsp:include page="../dialog/display_messages.jsp"/>

<fmt:bundle
        basename="org.wso2.carbon.identity.application.mgt.ui.i18n.Resources">
    <carbon:breadcrumb label="application.mgt"
                       resourceBundle="org.wso2.carbon.identity.application.mgt.ui.i18n.Resources"
                       topPage="true" request="<%=request%>"/>

    <script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/main.js"></script>

    <div id="middle">

        <h2>
            <fmt:message key='title.list.service.providers'/>
        </h2>

        <div id="workArea">

            <script type="text/javascript">

                function removeItem(appid) {
                    function doDelete() {
                        var appName = appid;
                        $.ajax({
                            type: 'POST',
                            url: 'remove-service-provider-finish-ajaxprocessor.jsp',
                            headers: {
                                Accept: "text/html"
                            },
                            data: 'appid=' + appName,
                            async: false,
                            success: function (responseText, status) {
                                if (status == "success") {
                                    location.assign("list-service-providers.jsp");
                                }
                            }
                        });
                    }

                    CARBON.showConfirmationDialog('Are you sure you want to delete "' + appid + '" SP information?',
                            doDelete, null);
                }
            </script>

            <%
                ApplicationBasicInfo[] applications = null;

                String BUNDLE = "org.wso2.carbon.identity.application.mgt.ui.i18n.Resources";
                ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
                ApplicationBasicInfo[] applicationsToDisplay = new ApplicationBasicInfo[0];
                String paginationValue = "region=region1&item=service_providers_list";
                String pageNumber = request.getParameter("pageNumber");

                int pageNumberInt = 0;
                int numberOfPages = 0;
                int resultsPerPage = 10;

                if (pageNumber != null) {
                    try {
                        pageNumberInt = Integer.parseInt(pageNumber);
                    } catch (NumberFormatException ignored) {
                        //not needed here since it's defaulted to 0
                    }
                }

                try {
                    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
                    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
                    ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
                            .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

                    ApplicationManagementServiceClient serviceClient = new
                            ApplicationManagementServiceClient(cookie, backendServerURL, configContext);
                    applications = serviceClient.getAllApplicationBasicInfo();

                    if (applications != null) {
                        numberOfPages = (int) Math.ceil((double) applications.length / resultsPerPage);
                        int startIndex = pageNumberInt * resultsPerPage;
                        int endIndex = (pageNumberInt + 1) * resultsPerPage;
                        applicationsToDisplay = new ApplicationBasicInfo[resultsPerPage];

                        for (int i = startIndex, j = 0; i < endIndex && i < applications.length; i++, j++) {
                            applicationsToDisplay[j] = applications[i];
                        }
                    }
                } catch (Exception e) {
                    String message = resourceBundle.getString("error.while.reading.app.info") + " : " + e.getMessage();
                    CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request, e);
                }
            %>

            <div style="height:30px;">
                <a href="load-service-provider.jsp?spName=wso2carbon-local-sp" class="icon-link"
                   style="background-image:url(images/local-sp.png);"><fmt:message key='local.sp'/></a>
            </div>
            <br/>
            <table style="width: 100%" class="styledLeft">
                <tbody>
                <tr>
                    <td style="border:none !important">
                        <table class="styledLeft" width="100%" id="ServiceProviders">
                            <thead>
                            <tr style="white-space: nowrap">
                                <th class="leftCol-med"><fmt:message
                                        key="field.service.provider.id"/></th>
                                <th class="leftCol-big"><fmt:message
                                        key="application.list.application.desc"/></th>
                                <th style="width: 30%"><fmt:message
                                        key="application.list.application.action"/></th>
                            </tr>
                            </thead>
                            <%
                                if (applications != null && applications.length > 0) {
                            %>
                            <tbody>
                            <%
                                for (ApplicationBasicInfo app : applicationsToDisplay) {
                                    if (app != null) {
                            %>
                            <tr>
                                <td><%=Encode.forHtml(app.getApplicationName())%>
                                </td>
                                <td><%=app.getDescription() != null ? Encode.forHtml(app.getDescription()) : ""%>
                                </td>
                                <td style="width: 100px; white-space: nowrap;"><a
                                        title="Edit Service Providers"
                                        href="load-service-provider.jsp?spName=<%=Encode.forUriComponent(app.getApplicationName())%>"
                                        class="icon-link"
                                        style="background-image: url(../admin/images/edit.gif)">Edit</a>

                                    <a title="Remove Service Providers"
                                       onclick="removeItem('<%=Encode.forJavaScriptAttribute(app.getApplicationName())%>');return false;" href="#"
                                       class="icon-link"
                                       style="background-image: url(../admin/images/delete.gif)">Delete
                                    </a></td>
                            </tr>
                            <%
                                    }
                                }
                            %>
                            </tbody>
                            <% } else { %>
                            <tbody>
                            <tr>
                                <td colspan="3"><i>No Service Providers registered</i></td>
                            </tr>
                            </tbody>
                            <% } %>
                        </table>
                    </td>
                </tr>
                </tbody>
            </table>

            <carbon:paginator pageNumber="<%=pageNumberInt%>"
                              numberOfPages="<%=numberOfPages%>"
                              page="list-service-providers.jsp"
                              pageNumberParameterName="pageNumber"
                              resourceBundle="org.wso2.carbon.identity.application.mgt.ui.i18n.Resources"
                              parameters="<%=paginationValue%>"
                              prevKey="prev" nextKey="next"/>
            <br/>
        </div>
    </div>
</fmt:bundle>