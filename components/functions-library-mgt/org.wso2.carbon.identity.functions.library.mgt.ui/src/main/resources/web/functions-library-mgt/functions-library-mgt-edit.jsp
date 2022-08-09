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

<link rel="stylesheet" href="../application/codemirror/lib/codemirror.css">
<link rel="stylesheet" href="../application/codemirror/theme/mdn-like.css">
<link rel="stylesheet" href="../application/codemirror/addon/dialog/dialog.css">
<link rel="stylesheet" href="../application/codemirror/addon/display/fullscreen.css">
<link rel="stylesheet" href="../application/codemirror/addon/fold/foldgutter.css">
<link rel="stylesheet" href="../application/codemirror/addon/hint/show-hint.css">
<link rel="stylesheet" href="../application/codemirror/addon/lint/lint.css">

<link rel="stylesheet" href="css/function-lib-mgt.css">

<script src="../application/codemirror/lib/codemirror.js"></script>
<script src="../application/codemirror/keymap/sublime.js"></script>
<script src="../application/codemirror/mode/javascript/javascript.js"></script>

<script src="../application/codemirror/addon/lint/jshint.min.js"></script>
<script src="../application/codemirror/addon/lint/lint.js"></script>
<script src="../application/codemirror/addon/lint/javascript-lint.js"></script>
<script src="../application/codemirror/addon/hint/anyword-hint.js"></script>
<script src="../application/codemirror/addon/hint/show-hint.js"></script>
<script src="../application/codemirror/addon/hint/javascript-hint.js"></script>
<script src="../application/codemirror/addon/hint/wso2-hints.js"></script>

<script src="../application/codemirror/addon/edit/closebrackets.js"></script>
<script src="../application/codemirror/addon/edit/matchbrackets.js"></script>
<script src="../application/codemirror/addon/fold/brace-fold.js"></script>
<script src="../application/codemirror/addon/fold/foldcode.js"></script>
<script src="../application/codemirror/addon/fold/foldgutter.js"></script>
<script src="../application/codemirror/addon/display/fullscreen.js"></script>
<script src="../application/codemirror/addon/display/placeholder.js"></script>
<script src="../application/codemirror/addon/comment/comment.js"></script>
<script src="../application/codemirror/addon/selection/active-line.js"></script>
<script src="../application/codemirror/addon/dialog/dialog.js"></script>
<script src="../application/codemirror/addon/display/panel.js"></script>
<script src="../application/codemirror/util/formatting.js"></script>


<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" %>

<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.identity.functions.library.mgt.model.xsd.FunctionLibrary" %>
<%@ page import="org.wso2.carbon.identity.functions.library.mgt.ui.client.FunctionLibraryManagementServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page
        import="static org.wso2.carbon.identity.functions.library.mgt.ui.util.FunctionLibraryUIConstants.FUNCTION_LIBRARY_NAME" %>
<script type="text/javascript" src="../identity/validation/js/identity-validate.js"></script>
<jsp:include page="../dialog/display_messages.jsp"/>

<fmt:bundle basename="org.wso2.carbon.identity.functions.library.mgt.ui.i18n.Resources">
    <carbon:breadcrumb label="functionlib.mgt"
                       resourceBundle="org.wso2.carbon.identity.functions.library.mgt.ui.i18n.Resources"
                       topPage="true" request="<%=request%>"/>
    <script type="text/javascript">
        function UpdateFunctionLibOnclick() {
            checkEmptyEditorContent();
            var functionLibName = document.getElementById("functionLibraryName").value.trim();
            var oldFunctionLibName = document.getElementById("oldFunctionLibraryName").value.trim();
            var content = document.getElementById("scriptTextArea").value.trim();
            if (functionLibName == '') {
                CARBON.showWarningDialog('<fmt:message key="not.provide.function.library.name"/>');
                location.href = '#';
            } else if (!validateTextForIllegal(document.getElementById("functionLibraryName"))) {
                return false;
            } else {
                if (content == '') {
                    CARBON.showWarningDialog('<fmt:message key="not.provide.function.library.script"/>');
                    location.href = '#';
                } else {
                    try {
                        eval(prepareScript(content));
                        functionLibName = functionLibName + ".js";
                        if (functionLibName != oldFunctionLibName) {
                            CARBON.showConfirmationDialog('<fmt:message key="update.function.library.name.warn"/>',
                                doEdit, null);
                        } else {
                            doEdit();
                        }
                    } catch (e) {
                        CARBON.showWarningDialog('<fmt:message key="error.in.script"/>' + e.lineNumber + " : " +
                            "" + e.message);
                        location.href = '#';
                    }
                }

                function doEdit() {
                    encodeFunctionLibScript();
                    $("#update-functionlib-form").submit();
                    return true;
                }
            }
        }

        function validateTextForIllegal(field) {
            var isValid = doValidateInput(field, '<fmt:message key="invalid.function.library.name"/>');
            return isValid;
        }

        function prepareScript(code) {
            var module = "var module = { exports:{} };";
            var exports = "var exports = {};";
            var require = "function require(name){};";
            code = module + exports + require + code;
            return code;
        }
    
    </script>
    
    <%
        String functionLibraryName = request.getParameter(FUNCTION_LIBRARY_NAME);
        String BUNDLE = "org.wso2.carbon.identity.functions.library.mgt.ui.i18n.Resources";
        ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
        FunctionLibrary functionLibrary = null;
        if (StringUtils.isNotBlank(functionLibraryName)) {
            try {
                String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
                String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
                ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().
                        getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
                
                FunctionLibraryManagementServiceClient serviceClient =
                        new FunctionLibraryManagementServiceClient(cookie, backendServerURL, configContext);
                functionLibrary = serviceClient.getFunctionLibrary(functionLibraryName);
            } catch (Exception e) {
                String message = resourceBundle.getString("alert.error.while.reading.function.libraries") + " : " +
                        e.getMessage();
                CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request, e);
            }
        }
    %>
    
    <div id="workArea">
        <div id="middle">
            <h2>Edit Function Library</h2>
            
            <form id="update-functionlib-form" name="update-functionlib-form" method="post"
                  action="edit-functionlib-finish-ajaxprocessor.jsp">
                
                <input type="hidden" name="oldFunctionLibraryName" id="oldFunctionLibraryName"
                       value="<%=Encode.forHtmlAttribute(functionLibraryName)%>"/>
                
                <div class="sectionSeperator togglebleTitle"><fmt:message
                        key='title.config.function.basic.config'/></div>
                <div class="sectionSub">
                    <table class="carbonFormTable">
                        <tr>
                            <td style="width:15%" class="leftCol-med labelField"><fmt:message
                                    key='config.function.info.basic.name'/>:<span class="required">*</span></td>
                            <td>
                                <input id="functionLibraryName" name="functionLibraryName" type="text"
                                       value="<%=Encode.forHtmlAttribute(functionLibraryName.substring(0, functionLibraryName.length()-3))%>"
                                       white-list-patterns="^[a-zA-Z0-9\s._-]*$" autofocus/>
                                <span>.js</span>
                                <div class="sectionHelp">
                                    <fmt:message key='help.name'/>
                                </div>
                            </td>
                        </tr>
                        <tr>
                            <td class="leftCol-med labelField">Description:</td>
                            <td>
                                <textarea maxlength="1020" style=" width:50%" type="text" name="description"
                                          id="functionLib-description"
                                          class="text-box-big"><%=functionLibrary.getDescription() != null ? Encode.forHtmlContent(functionLibrary.getDescription()) : ""%></textarea>
                                <div class="sectionHelp">
                                    <fmt:message key='help.desc'/>
                                </div>
                            </td>
                        </tr>
                    </table>
                </div>
                
                <h2 id="authentication_step_config_head" class="sectionSeperator trigger active">
                    <a href="#">Function Library Script</a>
                </h2>
                <div class="toggle_container" id="editorRow">
                    <div style="position: relative;">
                        <div id="codeMirror">
                            <textarea id="scriptTextArea" name="scriptContent"
                                      placeholder="Write JavaScript Function..."
                                      style="height: 500px;width: 100%; display: none;"><%=functionLibrary.getFunctionLibraryScript() != null ? Encode.forHtmlContent(functionLibrary.getFunctionLibraryScript()).trim() : "" %>
                            </textarea>
                        </div>
                    </div>
                </div>
                <div style="clear:both"></div>
                <div class="buttonRow" style=" margin-top: 10px;">
                    <input id="update" type="button" value="<fmt:message key='button.update.function.manager'/>"
                           onclick="UpdateFunctionLibOnclick()"/>
                    <input type="button" onclick="location.href='functions-library-mgt-list.jsp'"
                           value="<fmt:message key='button.cancel'/>"/>
                </div>
            </form>
        </div>
    </div>
</fmt:bundle>
<script src="./js/function-lib-mgt.js"></script>