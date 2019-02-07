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

<link rel="stylesheet" href="codemirror/lib/codemirror.css">
<link rel="stylesheet" href="codemirror/theme/mdn-like.css">
<link rel="stylesheet" href="codemirror/addon/dialog/dialog.css">
<link rel="stylesheet" href="codemirror/addon/display/fullscreen.css">
<link rel="stylesheet" href="codemirror/addon/fold/foldgutter.css">
<link rel="stylesheet" href="codemirror/addon/hint/show-hint.css">
<link rel="stylesheet" href="codemirror/addon/lint/lint.css">
<link rel="stylesheet" href="css/idpmgt.css">
<link rel="stylesheet" href="css/list-sp-templates-flow.css">
<script src="codemirror/lib/codemirror.js"></script>
<script src="codemirror/keymap/sublime.js"></script>
<script src="codemirror/mode/xml/xml.js"></script>
<script src="codemirror/mode/xml/test.js"></script>
<script src="codemirror/addon/lint/jshint.min.js"></script>
<script src="codemirror/addon/lint/lint.js"></script>
<script src="codemirror/addon/lint/javascript-lint.js"></script>
<script src="codemirror/addon/hint/anyword-hint.js"></script>
<script src="codemirror/addon/hint/show-hint.js"></script>
<script src="codemirror/addon/hint/xml-hint.js"></script>
<script src="codemirror/addon/hint/javascript-hint.js"></script>
<script src="codemirror/addon/hint/wso2-hints.js"></script>
<script src="codemirror/addon/edit/closebrackets.js"></script>
<script src="codemirror/addon/edit/matchbrackets.js"></script>
<script src="codemirror/addon/fold/brace-fold.js"></script>
<script src="codemirror/addon/fold/foldcode.js"></script>
<script src="codemirror/addon/fold/foldgutter.js"></script>
<script src="codemirror/addon/display/fullscreen.js"></script>
<script src="codemirror/addon/display/placeholder.js"></script>
<script src="codemirror/addon/comment/comment.js"></script>
<script src="codemirror/addon/selection/active-line.js"></script>
<script src="codemirror/addon/dialog/dialog.js"></script>
<script src="codemirror/addon/display/panel.js"></script>
<script src="codemirror/util/formatting.js"></script>
<script src="js/handlebars.min-v4.0.11.js"></script>
<script src="../admin/js/main.js" type="text/javascript"></script>
<script type="text/javascript" src="extensions/js/vui.js"></script>
<script type="text/javascript" src="../extensions/core/js/vui.js"></script>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.xsd.SpTemplate" %>
<%@ page
        import="org.wso2.carbon.identity.application.mgt.stub.IdentityApplicationManagementServiceIdentityApplicationManagementClientException" %>
<%@ page import="org.wso2.carbon.identity.application.mgt.ui.client.ApplicationManagementServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page
        import="java.util.ResourceBundle" %>

<script type="text/javascript" src="extensions/js/vui.js"></script>
<script type="text/javascript" src="../extensions/core/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<script type="text/javascript" src="../identity/validation/js/identity-validate.js"></script>
<jsp:include page="../dialog/display_messages.jsp"/>
<fmt:bundle
        basename="org.wso2.carbon.identity.application.mgt.ui.i18n.Resources">
    <carbon:breadcrumb label="application.mgt"
                       resourceBundle="org.wso2.carbon.identity.application.mgt.ui.i18n.Resources"
                       topPage="true" request="<%=request%>"/>
    <script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/main.js"></script>
    <script type="text/javascript" src="../identity/validation/js/identity-validate.js"></script>
    <%
        String[] updateTemplateError = (String[]) request.getSession().getAttribute("updateTemplateError");
        if (updateTemplateError == null) {
            updateTemplateError = new String[0];
        }

        String templateContent = "";
        String templateName = "";
        String templateDesc = "";

        String BUNDLE = "org.wso2.carbon.identity.application.mgt.ui.i18n.Resources";
        ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

        try {
            templateName = request.getParameter("templateName") != null ? request.getParameter("templateName").trim() : "";
            String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
            String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
            ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
                    .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            ApplicationManagementServiceClient serviceClient = new
                    ApplicationManagementServiceClient(cookie, backendServerURL, configContext);
            SpTemplate spTemplate = serviceClient.getApplicationTemplate(templateName);
            if (spTemplate == null) {
                String message = resourceBundle.getString("alert.error.load.sp.template");
                CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
            }
            templateDesc = spTemplate.getDescription();
            templateContent = spTemplate.getContent();
        } catch (IdentityApplicationManagementServiceIdentityApplicationManagementClientException e) {
            String message = resourceBundle.getString("alert.error.load.sp.template");
            if (e.getFaultMessage() != null && e.getFaultMessage().getIdentityApplicationManagementClientException() != null
                    && e.getFaultMessage().getIdentityApplicationManagementClientException().getMessages() != null) {
                String[] errorMessages = e.getFaultMessage().getIdentityApplicationManagementClientException().getMessages();
                session.setAttribute("retrieveTemplateError", errorMessages);
            } else {
                CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request, e);
            }
    %>
    <script>
        location.href = 'list-sp-templates.jsp';
    </script>
    <%
        }
    %>
    <script type="text/javascript">
        function validateTextForIllegal(fld) {
            var isValid = doValidateInput(fld, '<%=resourceBundle.getString("alert.error.sp.template.not.available")%>');
            if (isValid) {
                return true;
            }
            return false;
        }

        function updateTemplateOnclick() {
            var templateName = $.trim(document.getElementById('template-name').value);
            var templateContent = $.trim(document.getElementById('templateContent').value);
            if (templateName === null || 0 === templateName.length) {
                CARBON.showWarningDialog('<%=resourceBundle.getString("alert.error.sp.template.not.available")%>');
                location.href = '#';
                return false;
            } else if (templateContent === null || 0 === templateContent.length) {
                CARBON.showWarningDialog('<%=resourceBundle.getString("alert.error.sp.template.content.not.available")%>');
                location.href = '#';
                return false;
            } else if (!validateTextForIllegal(document.getElementById("template-name"))) {
                return false;
            } else {
                $("#update-sp-template-form").submit();
                return true;
            }
        }

        $(function () {
            $("#updateTemplateErrorMsgDialog").dialog({
                autoOpen: false,
                modal: true,
                buttons: {
                    OK: closeUpdateTemplateErrorDialog
                },
                width: "fit-content"
            });
        });

        function closeUpdateTemplateErrorDialog() {
            $(this).dialog("close");
            <%
             request.getSession().removeAttribute("updateTemplateError");
            %>
        }

        window.onload = function () {
            showManual();
            <% if (updateTemplateError.length > 0) { %>
            $("#updateTemplateErrorMsgDialog").dialog("open");
            <% } %>
        };

        function showManual() {
            $("#update-sp-template-form").show();
        }
    </script>
    <div id="middle">
    <h2>
        <fmt:message key='title.service.provider.template.update'/>
    </h2>
    <div id="workArea">
        <form id="update-sp-template-form" name="update-sp-template-form" method="post"
              action="edit-sp-template-finish-ajaxprocessor.jsp">
            <table class="carbonFormTable">
                <tr>
                    <td style="width:15%" class="leftCol-med labelField"><fmt:message
                            key='config.application.template.name'/>:<span class="required">*</span></td>
                    <td>
                        <input style="width:50%" id="template-name" name="template-name" type="text"
                               value="<%=Encode.forHtmlAttribute(templateName)%>"
                               white-list-patterns="^[a-zA-Z0-9\s._-]*$" autofocus/>
                        <div class="sectionHelp"><fmt:message key='help.template.name'/></div>
                    </td>
                </tr>
                <tr>
                    <td style="width:15%" class="leftCol-med labelField"><fmt:message
                            key='config.application.template.description'/>:
                    </td>
                    <td>
                        <textarea style="width:50%" type="text" name="template-description" id="template-description"
                                  class="text-box-big"><%=templateDesc != null ? Encode.forHtmlContent(templateDesc) : ""%></textarea>
                        <div class="sectionHelp"><fmt:message key='help.template.desc'/></div>
                    </td>
                </tr>
            </table>
            <div class="toggle_container sectionSub" id="editorRow">
                <div style="position: relative;">
                    <div class="sectionSub step_contents" id="codeMirror">
                        <textarea id="templateContent" name="templateContent"
                                  placeholder="Configure service provider template.."
                                  style="height: 500px;width: 100%; display: none;"><%out.print(Encode.forHtmlContent(templateContent));%></textarea>
                    </div>
                </div>
            </div>
            </br>
            <textarea hidden="hidden" name="sp-template-name"
                      id="sp-template-name"><%=Encode.forHtmlContent(templateName)%></textarea>
            <div class="buttonRow">
                <input id="updateTemplate" type="button" class="button"
                       value="<fmt:message key='button.update.service.provider.template'/>"
                       onclick="updateTemplateOnclick();"/>
                <input type="button" class="button" onclick="javascript:location.href='list-sp-templates.jsp'"
                       value="<fmt:message key='button.cancel'/>"/>
            </div>
        </form>
    </div>
    <div id="updateTemplateErrorMsgDialog" title='WSO2 Carbon'>
        <div id="messagebox-error">
            <h3>
                <fmt:message key="alert.error.update.sp.template"/>
            </h3>
            <table style="margin-top:10px;">
                <%
                    for (String error : updateTemplateError) {
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
<script src="js/list-sp-templates-flow.js"></script>

