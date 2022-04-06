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
  ~ KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  --%>

<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.slf4j.Logger"%>
<%@ page import="org.slf4j.LoggerFactory"%>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.xsd.SpTemplate" %>
<%@ page import="org.wso2.carbon.identity.application.mgt.ui.client.ApplicationManagementServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
           prefix="carbon" %>

<script type="text/javascript" src="../admin/js/main.js"></script>
<script type="text/javascript" src="../identity/validation/js/identity-validate.js"></script>
<jsp:include page="../dialog/display_messages.jsp"/>
<fmt:bundle
        basename="org.wso2.carbon.identity.application.mgt.ui.i18n.Resources">
    <carbon:breadcrumb label="application.mgt"
                       resourceBundle="org.wso2.carbon.identity.application.mgt.ui.i18n.Resources"
                       topPage="true" request="<%=request%>"/>
    <script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../admin/js/cookies.js"></script>
    <script type="text/javascript" src="../admin/js/main.js"></script>

    <%
        String[] retrieveTemplateError = (String[]) request.getSession().getAttribute("retrieveTemplateError");
        if (retrieveTemplateError == null) {
            retrieveTemplateError = new String[0];
        }

        String BUNDLE = "org.wso2.carbon.identity.application.mgt.ui.i18n.Resources";
        ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
    %>

    <script>
        function exportSPTemplateClick() {
            jQuery('#templateExportData').submit();
            jQuery(this).dialog("close");
        }

        function closeSP() {
            jQuery(this).dialog("close");
        }

        $(function () {
            $("#exportSPTemplateMsgDialog").dialog({
                autoOpen: false,
                buttons: {
                    OK: exportSPTemplateClick,
                    Cancel: closeSP
                },
                height: 160,
                width: 450,
                minHeight: 160,
                minWidth: 330,
                modal: true
            });
        });

        $(function () {
            $("#retrieveTemplateErrorMsgDialog").dialog({
                autoOpen: false,
                modal: true,
                buttons: {
                    OK: closeRetrieveTemplateErrorDialog
                },
                width: "fit-content"
            });
        });

        function closeRetrieveTemplateErrorDialog() {
            $(this).dialog("close");
            <%
             request.getSession().removeAttribute("retrieveTemplateError");
            %>
        }

        window.onload = function () {
            <% if (retrieveTemplateError.length > 0) { %>
            $("#retrieveTemplateErrorMsgDialog").dialog("open");
            <% } %>
        };
    </script>
    <div id="middle">
        <h2>
            <fmt:message key='title.list.service.provider.templates'/>
        </h2>
        <div id="workArea">
            <script type="text/javascript">
                function removeSPTemplate(templateName) {
                    function doDelete() {
                        $.ajax({
                            type: 'POST',
                            url: 'remove-sp-template-finish-ajaxprocessor.jsp',
                            headers: {
                                Accept: "text/html"
                            },
                            data: 'templateName=' + templateName,
                            async: false,
                            success: function (responseText, status) {
                                if (status == "success") {
                                    location.assign("list-sp-templates.jsp");
                                }
                            }
                        });
                    }

                    CARBON.showConfirmationDialog('<%=resourceBundle.getString("alert.confirm.sp.template.delete")%>' + templateName + ' ?',
                        doDelete, null);
                }

                function exportSPTemplate(templateName) {
                    document.getElementById('exportTemplateName').value = templateName;
                    $('#exportSPTemplateMsgDialog').dialog("open");
                }
            </script>
            <%
                Logger logger = LoggerFactory.getLogger(this.getClass());
                SpTemplate[] spTemplates = null;
                SpTemplate[] templatesToDisplay = new SpTemplate[0];
                String paginationValue = "region=region1&item=sp_template_list";
                String pageNumber = request.getParameter("pageNumber");
                int pageNumberInt = 0;
                int numberOfPages = 0;
                int resultsPerPage = 10;
                if (pageNumber != null) {
                    try {
                        pageNumberInt = Integer.parseInt(pageNumber);
                    } catch (NumberFormatException e) {
                        logger.error("Error while paginating SP templates.", e);
                    }
                }
                try {
                    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
                    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
                    ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
                            .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
                    ApplicationManagementServiceClient serviceClient = new
                            ApplicationManagementServiceClient(cookie, backendServerURL, configContext);
                    spTemplates = serviceClient.getAllApplicationTemplateInfo();
                    if (spTemplates != null && spTemplates.length > 0) {
                        numberOfPages = (int) Math.ceil((double) spTemplates.length / resultsPerPage);
                        int startIndex = pageNumberInt * resultsPerPage;
                        int endIndex = (pageNumberInt + 1) * resultsPerPage;
                        templatesToDisplay = new SpTemplate[resultsPerPage];
                        for (int i = startIndex, j = 0; i < endIndex && i < spTemplates.length; i++, j++) {
                            templatesToDisplay[j] = spTemplates[i];
                        }
                    }
                } catch (Exception e) {
                    String message = resourceBundle.getString("alert.error.read.sp.template.info");
                    CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request, e);
                }
            %>
            <br/>
            <table style="width: 100%" class="styledLeft">
                <div style="height:30px;">
                    <a href="javascript:document.location.href='add-sp-template.jsp'" class="icon-link"
                       style="background-image:url(../admin/images/add.gif);"><fmt:message
                            key="sp.template.add.link"/></a>
                </div>
                <tbody>
                <tr>
                    <td style="border:none !important">
                        <table class="styledLeft" width="100%" id="ServiceProviderTemplates">
                            <thead>
                            <tr style="white-space: nowrap">
                                <th class="leftCol-small"><fmt:message
                                        key="field.sp.template.name"/></th>
                                <th class="leftCol-big"><fmt:message
                                        key="field.sp.template.desc"/></th>
                                <th style="width: 30%"><fmt:message
                                        key="field.sp.template.action"/></th>
                            </tr>
                            </thead>
                            <%
                                boolean canView = CarbonUIUtil.isUserAuthorized(request,
                                        "/permission/admin/manage/identity/apptemplatemgt/view");
                                boolean canEdit = CarbonUIUtil.isUserAuthorized(request,
                                        "/permission/admin/manage/identity/apptemplatemgt/update");
                                boolean canDelete = CarbonUIUtil.isUserAuthorized(request,
                                        "/permission/admin/manage/identity/apptemplatemgt/delete");
                                if (spTemplates != null && spTemplates.length > 0) {
                            %>
                            <tbody>
                            <%
                                for (SpTemplate template : templatesToDisplay) {
                                    if (template != null) {
                            %>
                            <tr>
                                <td><%=Encode.forHtml(template.getName())%>
                                </td>
                                <td><%=template.getDescription() != null ? Encode.forHtml(template.getDescription()) : ""%>
                                </td>
                                <td style="width: 100px; white-space: nowrap;">
                                    <%
                                        if (canEdit) {
                                    %>
                                    <a title="Edit Service Provider Template"
                                       onclick="javascript:location.href=
                                               'edit-sp-template.jsp?templateName=<%=Encode.forUriComponent(template.getName())%>'"
                                       class="icon-link"
                                       style="background-image: url(../application/images/edit.gif)"><fmt:message
                                            key="sp.template.edit"/>
                                    </a>
                                    <%
                                        }
                                        if (canView) {
                                    %>
                                    <a title="Export Service Provider Template"
                                       onclick="exportSPTemplate('<%=Encode.forJavaScriptAttribute(template.getName())%>');"
                                       class="icon-link"
                                       style="background-image: url(../application/images/publish.gif)">
                                        <fmt:message key="sp.template.export"/>
                                    </a>
                                    <%
                                        }
                                        if (canDelete) {
                                    %>
                                    <a title="Remove Service Provider Template"
                                       onclick="removeSPTemplate('<%=Encode.forJavaScriptAttribute(template.getName())%>');"
                                       class="icon-link"
                                       style="background-image: url(../application/images/delete.gif)"><fmt:message
                                            key="sp.template.delete"/>
                                    </a>
                                </td>
                            </tr>
                            <%
                                        }
                                    }
                                }
                            %>
                            </tbody>
                            <% } else { %>
                            <tbody>
                            <tr>
                                <td colspan="3"><i><fmt:message key="sp.template.not.registered"/></i></td>
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
                              page="list-sp-templates.jsp"
                              pageNumberParameterName="pageNumber"
                              resourceBundle="org.wso2.carbon.identity.application.mgt.ui.i18n.Resources"
                              parameters="<%=paginationValue%>"
                              prevKey="prev" nextKey="next"/>
        </div>
    </div>
    </div>
    <div id='exportSPTemplateMsgDialog' title='WSO2 Carbon'>
        <div id='messagebox-confirm'>
            <p><fmt:message key="sp.template.export.para"/></p><br>
            <form id="templateExportData" name="template-export-data" method="post"
                  action="export-sp-template-finish-ajaxprocessor.jsp">
                <input hidden id="exportTemplateName" name="exportTemplateName"/>
            </form>
        </div>
    </div>
    <div id="retrieveTemplateErrorMsgDialog" title='WSO2 Carbon'>
        <div id="messagebox-error">
            <h3>
                <fmt:message key="alert.error.load.sp.template"/>
            </h3>
            <table style="margin-top:10px;">
                <%
                    for (String error : retrieveTemplateError) {
                %>
                <tr>
                    <td><%=error%>
                    </td>
                </tr>
                <%
                    }
                %>
            </table>
        </div>
    </div>
</fmt:bundle>

