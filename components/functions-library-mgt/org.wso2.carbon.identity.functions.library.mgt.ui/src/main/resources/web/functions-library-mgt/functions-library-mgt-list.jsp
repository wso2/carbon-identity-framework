<%--
  ~ Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
  ~ KIND, either express or implied. See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  --%>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.identity.functions.library.mgt.model.xsd.FunctionLibrary" %>
<%@ page import="org.wso2.carbon.identity.functions.library.mgt.ui.client.FunctionLibraryManagementServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ResourceBundle" %>


<fmt:bundle basename="org.wso2.carbon.identity.functions.library.mgt.ui.i18n.Resources">
    <carbon:breadcrumb label="functionlib.mgt"
                       resourceBundle="org.wso2.carbon.identity.functions.library.mgt.ui.i18n.Resources"
                       topPage="true" request="<%=request%>"/>
    <jsp:include page="../dialog/display_messages.jsp"/>
    
    <script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/main.js"></script>
    <div id="middle">
        
        <h2>
            Function Library Management
        </h2>
        
        <div id="workArea">
            
            <script type="text/javascript">
                function removeItem(functionLibraryName) {
                    function doDelete() {

                        var functionLibName = functionLibraryName;
                        $.ajax({
                            type: 'POST',
                            url: 'remove-function-library-finish-ajaxprocessor.jsp',
                            headers: {
                                Accept: "text/html"
                            },
                            data: 'functionLibraryName=' + functionLibName,
                            async: false,
                            success: function (responseText, status) {
                                if (status == "success") {
                                    location.assign("functions-library-mgt-list.jsp");
                                }
                            }
                        });
                    }

                    CARBON.showConfirmationDialog('<fmt:message key="remove.function.library.warn"/>', doDelete, null);
                }
            </script>
            
            <%
                FunctionLibrary[] functionLibraries = null;
                String BUNDLE = "org.wso2.carbon.identity.functions.library.mgt.ui.i18n.Resources";
                ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
                FunctionLibrary[] functionLibrariesToDisplay = new FunctionLibrary[0];
                String paginationValue = "region=region1&item=function_libraries_list";
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
                    
                    FunctionLibraryManagementServiceClient serviceClient = new
                            FunctionLibraryManagementServiceClient(cookie, backendServerURL, configContext);
                    functionLibraries = serviceClient.listFunctionLibraries();
                    
                    if (functionLibraries != null) {
                        numberOfPages = (int) Math.ceil((double) functionLibraries.length / resultsPerPage);
                        int startIndex = pageNumberInt * resultsPerPage;
                        int endIndex = (pageNumberInt + 1) * resultsPerPage;
                        functionLibrariesToDisplay = new FunctionLibrary[resultsPerPage];
                        for (int i = startIndex, j = 0; i < endIndex && i < functionLibraries.length; i++, j++) {
                            functionLibrariesToDisplay[j] = (FunctionLibrary) functionLibraries[i];
                        }
                    }
                } catch (Exception e) {
                    String message = resourceBundle.getString("error.while.reading.function.libraries") + " : " +
                            e.getMessage();
                    CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request, e);
                }
            %>
            
            <br/>
            <table style="width: 100%" class="styledLeft">
                <tbody>
                <tr>
                    <td style="border:none !important">
                        <table class="styledLeft" width="100%" id="FunctionLibraries">
                            <thead>
                            <tr style="white-space: nowrap">
                                <th class="leftCol-med"><fmt:message
                                        key="function.library.name"/></th>
                                <th class="leftCol-big"><fmt:message
                                        key="function.library.desc"/></th>
                                <th style="width: 30%"><fmt:message
                                        key="functionlib.list.action"/></th>
                            </tr>
                            </thead>
                            
                            <%
                                if (functionLibraries != null && functionLibraries.length > 0) {
                            
                            %>
                            <tbody>
                            <%
                                for (FunctionLibrary functionLib : functionLibrariesToDisplay) {
                                    if (functionLib != null) {
                            
                            %>
                            <tr>
                                <td><%=Encode.forHtml(functionLib.getFunctionLibraryName())%>
                                </td>
                                <td><%=functionLib.getDescription() != null ? Encode.forHtml(functionLib.getDescription()) : ""%>
                                </td>
                                <td>
                                    <a title="<fmt:message key='edit.functionlib.info'/>"
                                       onclick=""
                                       href="functions-library-mgt-edit.jsp?functionLibraryName=<%=Encode.forUriComponent(functionLib.getFunctionLibraryName())%>"
                                       class="icon-link"
                                       style="background-image: url(images/edit.gif)">
                                        <fmt:message key='edit'/>
                                    </a>
                                        <%--Uncomment the following commented section when export functionality is implemented.--%>
                                        <%--<a title="<fmt:message key='export.functionlib.info'/>"--%>
                                        <%--onclick=""--%>
                                        <%--href="#"--%>
                                        <%--class="icon-link"--%>
                                        <%--style="background-image: url(images/export.gif)">--%>
                                        <%--<fmt:message key='export'/>--%>
                                        <%--</a>--%>
                                    <a title="<fmt:message key='delete.functionlib.info'/>"
                                       onclick="removeItem('<%=Encode.forJavaScriptAttribute(functionLib.getFunctionLibraryName())%>');return false;"
                                       href=""
                                       class="icon-link"
                                       style="background-image: url(images/delete.gif)">
                                        <fmt:message key='delete'/>
                                    </a>
                                </td>
                            </tr>
                            <%
                                    }
                                }
                            %>
                            </tbody>
                            <%} else { %>
                            <tbody>
                            <tr>
                                <td colspan="3"><i>No Function Library created</i></td>
                            </tr>
                            </tbody>
                            <%}%>
                        </table>
                    </td>
                </tr>
                </tbody>
            </table>
            <carbon:paginator pageNumber="<%=pageNumberInt%>"
                              numberOfPages="<%=numberOfPages%>"
                              page="functions-library-mgt-list.jsp"
                              pageNumberParameterName="pageNumber"
                              resourceBundle="org.wso2.carbon.identity.functions.library.mgt.ui.i18n.Resources"
                              parameters="<%=paginationValue%>"
                              prevKey="prev" nextKey="next"/>
            <br/>
        </div>
    </div>
</fmt:bundle>
