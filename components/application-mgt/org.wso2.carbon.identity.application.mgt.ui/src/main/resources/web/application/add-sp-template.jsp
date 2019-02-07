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

<%@ page import="java.util.ResourceBundle" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" %>
<carbon:breadcrumb label="breadcrumb.service.provider"
                   resourceBundle="org.wso2.carbon.identity.application.mgt.ui.i18n.Resources"
                   topPage="true"
                   request="<%=request%>"/>
<script type="text/javascript" src="extensions/js/vui.js"></script>
<script type="text/javascript" src="../extensions/core/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<script type="text/javascript" src="../identity/validation/js/identity-validate.js"></script>
<jsp:include page="../dialog/display_messages.jsp"/>
<%
    String[] createTemplateError = (String[]) request.getSession().getAttribute("createTemplateError");
    if (createTemplateError == null) {
        createTemplateError = new String[0];
    }

    String BUNDLE = "org.wso2.carbon.identity.application.mgt.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
%>
<script type="text/javascript">
    function validateTextForIllegal(fld) {
        var isValid = doValidateInput(fld, '<%=resourceBundle.getString("alert.error.sp.template.invalid")%>');
        if (isValid) {
            return true;
        }
        return false;
    }

    var openFile = function (event) {
        var input = event.target;
        var reader = new FileReader();
        reader.onload = function () {
            var data = reader.result;
            document.getElementById('sp-template-file-content').value = data;
        };
        document.getElementById('sp-template-file-name').value = input.files[0].name;
        reader.readAsText(input.files[0]);
    };

    function addSPTemplate() {
        var templateName = $.trim(document.getElementById('template-name').value);
        var templateContent = $.trim(document.getElementById('sp-template-file-content').value);
        if (templateName === null || 0 === templateName.length) {
            CARBON.showWarningDialog('<%=resourceBundle.getString("alert.error.sp.template.not.available")%>');
            location.href = '#';
            return false;
        } else if (templateContent === null || 0 === templateContent.length) {
            CARBON.showWarningDialog('<%=resourceBundle.getString("alert.error.sp.template.file.not.available")%>');
            location.href = '#';
            return false;
        } else if (!validateTextForIllegal(document.getElementById("template-name"))) {
            return false;
        } else {
            $("#upload-sp-template-form").submit();
            return true;
        }
    }

    function showManual() {
        $("#upload-sp-template-form").show();
    }

    $(function () {
        $("#createTemplateErrorMsgDialog").dialog({
            autoOpen: false,
            modal: true,
            buttons: {
                OK: closeCreateTemplateErrorDialog
            },
            width: "fit-content"
        });
    });

    function closeCreateTemplateErrorDialog() {
        $(this).dialog("close");
        <%
         request.getSession().removeAttribute("createTemplateError");
        %>
    }

    window.onload = function () {
        showManual();
        <% if (createTemplateError.length > 0) { %>
        $("#createTemplateErrorMsgDialog").dialog("open");
        <% } %>
    };
</script>
<fmt:bundle basename="org.wso2.carbon.identity.application.mgt.ui.i18n.Resources">
    <div id="middle">
        <h2>
            <fmt:message key='title.service.provider.template.add'/>
        </h2>
        <div id="workArea">
            <form id="upload-sp-template-form" name="upload-sp-template-form" method="post"
                  action="add-sp-template-finish-ajaxprocessor.jsp">
                <div class="sectionSeperator togglebleTitle"><fmt:message
                        key='upload.service.provider.template.file'/></div>
                <div class="sectionSub">
                    <table class="carbonFormTable">
                        <tr>
                            <td style="width:15%" class="leftCol-med labelField"><fmt:message
                                    key='config.application.template.name'/>:<span class="required">*</span></td>
                            <td>
                                <input id="template-name" name="template-name" type="text" value=""
                                       white-list-patterns="^[a-zA-Z0-9\s._-]*$" autofocus/>
                                <div class="sectionHelp">
                                    <fmt:message key='help.template.name'/>
                                </div>
                            </td>
                        </tr>
                        <tr>
                            <td style="width:15%" class="leftCol-med labelField"><fmt:message
                                    key='config.application.template.description'/>:
                            </td>
                            <td>
                                <textarea style="width:50%" type="text" name="template-description"
                                          id="template-description" class="text-box-big"></textarea>
                                <div class="sectionHelp">
                                    <fmt:message key='help.template.desc'/>
                                </div>
                            </td>
                        </tr>
                        <tr>
                            <td style="width:15%" class="leftCol-med labelField"><fmt:message
                                    key='config.application.template.file.location'/>:
                            </td>
                            <td>
                                <input type="file" class="button" id="sp_file" name="sp_file"
                                       onchange='openFile(event)'/>
                                <textarea hidden="hidden" name="sp-template-file-content"
                                          id="sp-template-file-content"></textarea>
                                <textarea hidden="hidden" name="sp-template-file-name"
                                          id="sp-template-file-name"></textarea>
                                <div class="sectionHelp">
                                    <fmt:message key='help.template'/>
                                </div>
                            </td>
                        </tr>
                    </table>
                </div>
                <div class="buttonRow">
                    <input type="button" class="button"
                           value="<fmt:message key='button.add.service.provider.template'/>"
                           onclick="addSPTemplate();"/>
                    <input type="button" class="button" onclick="javascript:location.href='list-sp-templates.jsp'"
                           value="<fmt:message key='button.cancel'/>"/>
                </div>
            </form>
        </div>
    </div>
    <div id="createTemplateErrorMsgDialog" title='WSO2 Carbon'>
        <div id="messagebox-error">
            <h3>
                <fmt:message key="alert.error.add.sp.template"/>
            </h3>
            <table style="margin-top:10px;">
                <%
                    for (String error : createTemplateError) {
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

