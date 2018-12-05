<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.identity.template.mgt.model.TemplateInfo" %>
<%@ page import="org.wso2.carbon.identity.template.mgt.ui.client.TemplateManagementServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ResourceBundle" %>

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

<fmt:bundle basename="org.wso2.carbon.identity.template.mgt.ui.i18n.Resources">
    <carbon:breadcrumb label="template.mgt"
                       resourceBundle="org.wso2.carbon.identity.template.mgt.ui.i18n.Resources"
                       topPage="true" request="<%=request%>"/>
    
    <script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/main.js"></script>
    <div id="middle">
        
        <h2>
            Template Management
        </h2>
        
        <div id="workArea">
            
            <script type="text/javascript">
                function removeItem(templateName) {
                    function doDelete() {

                        var template_name = templateName;
                        console.log(template_name);
                        $.ajax({
                            type: 'POST',
                            url: 'remove-template-finish.jsp',
                            headers: {
                                Accept: "text/html"
                            },
                            data: 'templateName=' + template_name,
                            async: false,
                            success: function (responseText, status) {
                                if (status == "success") {
                                    location.assign("list-templates.jsp");
                                }
                            }
                        });
                    }

                    CARBON.showConfirmationDialog('Are you sure you want to delete "'
                        + templateName + '" Template? \n WARN: If you delete this template, ' +
                        'the authentication scripts which used this will no longer function properly!',
                        doDelete, null);
                }
            </script>
            
            <%
                List<TemplateInfo> templateList = null;
                
                String BUNDLE = "org.wso2.carbon.identity.template.mgt.ui.i18n.Resources";
                ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
                TemplateInfo[] templateListToDisplay = new TemplateInfo[0];
                String paginationValue = "region=region1&item=list_templates";
                String pageNumber = request.getParameter("pageNumber");
                
                int pageNumberInt = 0;
                int numberOfPages = 0;
                int resultsPerPage = 10;
                int limit = 0;
                int offset = 0;
                
                if (pageNumber != null) {
                    try {
                        pageNumberInt = Integer.parseInt(pageNumber);
                    } catch (NumberFormatException ignored) {
                        //not needed here since it's defaulted to 0
                    }
                }
                
                try {
                    String currentUser = (String) session.getAttribute("logged-user");
                    TemplateManagementServiceClient serviceClient = new TemplateManagementServiceClient(currentUser);
                    templateList = serviceClient.listTemplates(limit, offset);
                    
                    if (templateList != null) {
                        numberOfPages = (int) Math.ceil((double) templateList.size() / resultsPerPage);
                        int startIndex = pageNumberInt * resultsPerPage;
                        int endIndex = (pageNumberInt + 1) * resultsPerPage;
                        templateListToDisplay = new TemplateInfo[resultsPerPage];
                        
                        for (int i = startIndex, j = 0; i < endIndex && i < templateList.size(); i++, j++) {
                            templateListToDisplay[j] = templateList.get(i);
                        }
                    }
                } catch (Exception e) {
                    String message = resourceBundle.getString("error.while.reading.templates") + " : " + e.getMessage();
                    CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request, e);
                }
            %>
            
            
            <br/>
            <table style="width: 100%" class="styledLeft">
                <tbody>
                <tr>
                    <td style="border:none !important">
                        <table class="styledLeft" width="100%" id="Templates">
                            <thead>
                            <tr style="white-space: nowrap">
                                <th class="leftCol-med"><fmt:message
                                        key="template.name"/></th>
                                <th class="leftCol-big"><fmt:message
                                        key="template.desc"/></th>
                                <th style="width: 30%"><fmt:message
                                        key="template.list.action"/></th>
                            </tr>
                            </thead>
                            
                            <%
                                if (templateList != null && templateList.size() > 0) {
                            
                            %>
                            <tbody>
                            <%
                                for (TemplateInfo template : templateListToDisplay) {
                                    if (template != null) {
                            
                            %>
                            <tr>
                                
                                <td><%=Encode.forHtml(template.getTemplateName())%>
                                </td>
                                <td><%=template.getDescription() != null ? Encode.forHtml(template.getDescription()) : ""%>
                                </td>
                                <td>
                                    <a title="<fmt:message key='edit.template.info'/>"
                                       onclick=""
                                       href="get-template.jsp?templateName=<%=Encode.forUriComponent(template.getTemplateName().trim())%>"
                                       class="icon-link"
                                       style="background-image: url(images/edit.gif)">
                                        <fmt:message key='edit'/>
                                    </a>
                                    <a title="<fmt:message key='delete.template.info'/>"
                                       onclick="removeItem('<%=Encode.forJavaScriptAttribute(template
                                                                .getTemplateName())%>'); return false;"
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
                                <td colspan="3"><i>No Template created</i></td>
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
                              page="list-templates.jsp"
                              pageNumberParameterName="pageNumber"
                              resourceBundle="org.wso2.carbon.identity.template.mgt.ui.i18n.Resources"
                              parameters="<%=paginationValue%>"
                              prevKey="prev" nextKey="next"/>
            <br/>
        </div>
    </div>
</fmt:bundle>
