<!--
~ Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.identity.application.mgt.ui.client.ApplicationManagementServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="static org.wso2.carbon.identity.application.mgt.ui.util.ApplicationMgtUIConstants.*" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.xsd.SpTemplate" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"%>

<carbon:breadcrumb label="breadcrumb.service.provider" resourceBundle="org.wso2.carbon.identity.application.mgt.ui.i18n.Resources" topPage="true" request="<%=request%>" />
<jsp:include page="../dialog/display_messages.jsp"/>

<script type="text/javascript" src="extensions/js/vui.js"></script>
<script type="text/javascript" src="../extensions/core/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<script type="text/javascript" src="../identity/validation/js/identity-validate.js"></script>
<jsp:include page="../dialog/display_messages.jsp" />

<%
    String[] clientError = (String[]) request.getSession().getAttribute("clientError");
    if (clientError == null) {
        clientError = new String[0];
    }
    SpTemplate[] spTemplates = null;
    try {
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext()
                        .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        ApplicationManagementServiceClient serviceClient = new ApplicationManagementServiceClient(cookie,
                backendServerURL, configContext);
        spTemplates = serviceClient.getAllApplicationTemplateInfo();
    } catch (Exception e) {
        CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
%>
<script>
    location.href = 'add-service-provider.jsp';
</script>
<%
    }
%>
<script type="text/javascript">
function createAppOnclick() {
    var spName = document.getElementById("spName").value;
    var description = document.getElementById("sp-description").value;
    if( spName == '') {
        CARBON.showWarningDialog('Please provide Service Provider ID');
        location.href = '#';
    } else if (!validateTextForIllegal(document.getElementById("spName"))) {
        return false;
    }else {
        $("#add-sp-form").submit();
        return true;
    }
}

function validateTextForIllegal(fld) {
    var isValid = doValidateInput(fld, "Provided Service Provider name is invalid.");
    if (isValid) {
        return true;
    } else {
        return false;
    }
}

var openFile = function (event) {
    var input = event.target;
    var reader = new FileReader();
    reader.onload = function () {
        var data = reader.result;
        document.getElementById('sp-file-content').value = data;
    };
    document.getElementById('sp-file-name').value = input.files[0].name;
    reader.readAsText(input.files[0]);
};

function importAppOnclick() {
    if (document.getElementById('sp-file-content').value === null || document.getElementById('sp-file-content').value === "") {
        CARBON.showWarningDialog('Please specify service provider configuration file.');
        location.href = '#';
        return false;
    } else {
        $("#upload-sp-form").submit();
        return true;
    }
}
function showManual() {
    $("#add-sp-form").show();
    $("#upload-sp-form").hide();
}

function showFile() {
    $("#add-sp-form").hide();
    $("#upload-sp-form").show();
}
$(function() {
    $( "#clientErrorMsgDialog" ).dialog({
        autoOpen: false,
        modal: true,
        buttons: {
            OK: closeClientErrorDialog
        },
        width: "fit-content"
    });
});
function closeClientErrorDialog() {
   $(this).dialog("close");
   <%
    request.getSession().removeAttribute("clientError");
   %>
}
window.onload = function() {
    showManual();
    <% if (Boolean.valueOf(request.getParameter("clientError")) && clientError.length > 0) { %>
        $( "#clientErrorMsgDialog" ).dialog( "open" );
    <% } %>
};
</script>

<fmt:bundle basename="org.wso2.carbon.identity.application.mgt.ui.i18n.Resources">
    <div id="middle">
        <h2>
            <fmt:message key='title.service.providers.add'/>
        </h2>
        <div id="workArea">
            <table class="styledLeft" width="100%">
                <thead>
                <tr>
                    <th><fmt:message key="title.sp.select.mode"/></th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td><input type="radio" id="manual-option" name="upload-type-selector" checked="checked"
                               onclick="showManual();">
                        <label for="manual-option">Manual Configuration</label>
                    </td>
            
                </tr>
                <tr>
                    <td>
                        <input type="radio" id="file-option" name="upload-type-selector" onclick="showFile();">
                        <label for="file-option">File Configuration</label>
                    </td>
                </tr>
                
                </tbody>
            </table>
            <br/>
    
            <form id="add-sp-form" name="add-service-provider-form" method="post"
                  action="add-service-provider-finish-ajaxprocessor.jsp">
            <div class="sectionSeperator togglebleTitle"><fmt:message key='title.config.app.basic.config'/></div>
            <div class="sectionSub">
                <table class="carbonFormTable">
                    <tr>
                        <td style="width:15%" class="leftCol-med labelField"><fmt:message key='config.application.info.basic.name'/>:<span class="required">*</span></td>
                        <td>
                            <input id="spName" name="spName" type="text" value="" white-list-patterns="^[a-zA-Z0-9\s._-]*$" autofocus/>
                            <div class="sectionHelp">
                                <fmt:message key='help.name'/>
                            </div>
                        </td>
                    </tr>
                    <tr>
                       <td class="leftCol-med labelField">Description:</td>
                     <td>
                        <textarea maxlength="1023" style="width:50%" type="text" name="sp-description" id="sp-description" class="text-box-big"></textarea>
                        <div class="sectionHelp">
                                <fmt:message key='help.desc'/>
                            </div>
                        </td>
                    </tr>
                    <%
                        if (spTemplates != null && spTemplates.length > 0) {
                    %>
                    <tr>
                        <td style="width:15%" class="leftCol-med labelField"><fmt:message key='config.application.info.basic.template'/>:</td>
                        <td>
                            <select style="min-width: 250px;" id="sp-template" name="sp-template">
                                <option value="">---Select---</option>
                                <%
                                    for (SpTemplate spTemplate : spTemplates) {
                                        if (spTemplate != null) {
                                            if (spTemplate.getName().equals(
                                                    TENANT_DEFAULT_SP_TEMPLATE_NAME)) {
                                %>
                                <option
                                        value="<%=Encode.forHtmlAttribute(spTemplate.getName())%>"
                                        title="<%=Encode.forHtmlAttribute(spTemplate.getDescription())%>" selected>
                                    <%=Encode.forHtmlContent(spTemplate.getName())%>
                                </option>
                                <%              } else { %>
                                <option
                                        value="<%=Encode.forHtmlAttribute(spTemplate.getName())%>"
                                        title="<%=Encode.forHtmlAttribute(spTemplate.getDescription())%>">
                                    <%=Encode.forHtmlContent(spTemplate.getName())%>
                                </option>
                                <%
                                                }
                                            }
                                        }
                                %>
                            </select>
                            <div class="sectionHelp">
                                <fmt:message key='help.template'/>
                            </div>
                        </td>
                    </tr>
                    <%
                        }
                    %>
                </table>
            </div>
            <div class="buttonRow">
                <input type="button" class="button"  value="<fmt:message key='button.add.service.providers'/>" onclick="createAppOnclick();"/>
                <input type="button" class="button" onclick="javascript:location.href='list-service-providers.jsp'" value="<fmt:message key='button.cancel'/>" />
            </div>
            </form>
            <form id="upload-sp-form" name="upload-sp-form" method="post"
                  action="import-service-provider-finish-ajaxprocessor.jsp">
                <table class="styledLeft" width="100%">
                    <thead>
                    <tr>
                        <th><fmt:message key="upload.service.provider.file"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td>
                            <span>File Location: </span><input type="file" class="button" id="sp_file" name="sp_file" onchange='openFile(event)'/>
                        </td>
                        <textarea hidden="hidden" name="sp-file-content" id="sp-file-content"></textarea>
                        <textarea hidden="hidden" name="sp-file-name" id="sp-file-name"></textarea>
                    </tr>
                    <tr>
                        <td>
                            <input type="button" class="button"  value="<fmt:message key='button.import.service.providers'/>"
                                   onclick="importAppOnclick();"/>
                            <input type="button" class="button" onclick="javascript:location.href='list-service-providers.jsp'" value="<fmt:message key='button.cancel'/>" />
                        </td>
                    </tr>
                    </tbody>
                </table>
            </form>
        </div>
    </div>
    <div id="clientErrorMsgDialog"  title='WSO2 Carbon'>
        <div id="messagebox-error">
            <h3>
                <fmt:message key="alert.error.add.sp.template"/>
            </h3>
            <table style="margin-top:10px;">
                <%
                    for (String error : clientError){
                %>
                <tr>
                    <td><%=error%></td>
                </tr>
                <%
                    }
                %>
            </table>
        </div>
    </div>
</fmt:bundle>
