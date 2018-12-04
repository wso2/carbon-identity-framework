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

<link rel="stylesheet" href="css/template-mgt.css">
<link rel="stylesheet" href="css/template.css">

<script src="../application/codemirror/lib/codemirror.js"></script>
<script src="../application/codemirror/keymap/sublime.js"></script>
<script src="../application/codemirror/mode/xml/xml.js"></script>
<script src="../application/codemirror/mode/css/css.js"></script>
<script src="../application/codemirror/mode/javascript/javascript.js"></script>
<script src="../application/codemirror/mode/htmlmixed/htmlmixed.js"></script>

<script src="../application/codemirror/addon/lint/jshint.min.js"></script>
<script src="../application/codemirror/addon/lint/lint.js"></script>
<script src="../application/codemirror/addon/lint/javascript-lint.js"></script>
<script src="../application/codemirror/addon/lint/html-lint.js"></script>
<script src="../application/codemirror/addon/lint/css-lint.js"></script>

<script src="../application/codemirror/addon/hint/anyword-hint.js"></script>
<script src="../application/codemirror/addon/hint/show-hint.js"></script>
<script src="../application/codemirror/addon/hint/javascript-hint.js"></script>
<script src="../application/codemirror/addon/hint/wso2-hints.js"></script>
<script src="../application/codemirror/addon/hint/html-hint.js"></script>
<script src="../application/codemirror/addon/hint/htmlhint.js"></script>
<script src="../application/codemirror/addon/hint/css-hint.js"></script>
<script src="../application/codemirror/addon/hint/xml-hint.js"></script>

<script src="../application/codemirror/addon/mode/simple.js"></script>
<script src="../application/codemirror/addon/mode/multiplex.js"></script>

<script src="../application/codemirror/addon/edit/matchtags.js"></script>
<script src="../application/codemirror/addon/edit/closetag.js"></script>
<script src="../application/codemirror/addon/edit/closebrackets.js"></script>
<script src="../application/codemirror/addon/edit/matchbrackets.js"></script>
<script src="../application/codemirror/addon/fold/brace-fold.js"></script>
<script src="../application/codemirror/addon/fold/foldcode.js"></script>
<script src="../application/codemirror/addon/fold/foldgutter.js"></script>
<script src="../application/codemirror/addon/fold/xml-fold.js"></script>

<script src="../application/codemirror/addon/display/fullscreen.js"></script>
<script src="../application/codemirror/addon/display/placeholder.js"></script>
<script src="../application/codemirror/addon/comment/comment.js"></script>
<script src="../application/codemirror/addon/selection/active-line.js"></script>
<script src="../application/codemirror/addon/dialog/dialog.js"></script>
<script src="../application/codemirror/addon/display/panel.js"></script>
<script src="../application/codemirror/util/formatting.js"></script>

<script type="text/javascript" src="../identity/validation/js/identity-validate.js"></script>


<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.identity.template.mgt.model.Template" %>
<%@ page import="org.wso2.carbon.identity.template.mgt.ui.client.TemplateManagementServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.util.ResourceBundle" %>
<jsp:include page="../dialog/display_messages.jsp"/>

<script type="text/javascript">
    function UpdateTemplateOnclick() {
        checkEmptyEditorContent();
        var templateName = document.getElementById("templateName").value.trim();
        var oldTemplateName = document.getElementById("oldTemplateName").value.trim();
        var content = document.getElementById('scriptTextArea').value;
        if (templateName == '') {
            CARBON.showWarningDialog('Please provide template Name');
            location.href = '#';

            // CARBON.showConfirmationDialog('Are you sure you want to edit "' + oldTemplateName + '" ' +
            //     'Template name ? \n WARN: If you edit this library name, ' +
            //     'the authentication scripts which used this will no longer function properly!',
            //     doEdit, null);
        } else if (!validateTextForIllegal(document.getElementById('templateName'))) {
            return false;
        } else {
            if (content == '') {
                CARBON.showWarningDialog('Template script cannot be empty.');
                location.href = '#';
            } else {
                if(templateName != oldTemplateName){
                    CARBON.showConfirmationDialog('Are you sure you want to edit "' + oldTemplateName + '" ' +
                        'Template name ? \n WARN: If you edit this library name, ' +
                        'the authentication scripts which used this will no longer function properly!',
                        doEdit, null);
                } else {
                    doEdit();
                }
            }
            function doEdit() {
                $("#update-template-form").submit();
                return true;
            }
        }
    }

    function validateTextForIllegal(field) {
        var isValid = doValidateInput(field, "Provided template name is invalid.");
        if (isValid) {
            return true;
        } else {
            return false;
        }
    }
</script>
<fmt:bundle
        basename="org.wso2.carbon.identity.template.mgt.ui.i18n.Resources">
    <carbon:breadcrumb label="template.mgt"
                       resourceBundle="org.wso2.carbon.identity.template.mgt.ui.i18n.Resources"
                       topPage="true" request="<%=request%>"/>
    
    <%
        String templateName = request.getParameter("templateName");
        String BUNDLE = "org.wso2.carbon.identity.template.mgt.ui.i18n.Resources";
        ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
        Template template = null;
        if (templateName != null && !"".equals(templateName)) {
            
            try {
                String currentUser = (String) session.getAttribute("logged-user");
                TemplateManagementServiceClient serviceClient = new TemplateManagementServiceClient(currentUser);
                template = serviceClient.getTemplateByName(templateName);
                
            } catch (Exception e) {
                String message = resourceBundle.getString("alert.error.while.reading.template") + " : " + e.getMessage();
                CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request, e);
            }
        }
    
    %>
    <div id="workArea">
        <div id="middle">
            <h2>Edit Template</h2>
            
            <form id="update-template-form" name="update-template-form" method="post"
                  action="edit-template-finish.jsp">
                
                <input type="hidden" name="oldTemplateName" id="oldTemplateName"
                       value="<%=Encode.forHtmlAttribute(templateName)%>"/>
                
                <div class="sectionSeperator togglebleTitle"><fmt:message
                        key='title.config.template.basic.config'/></div>
                <div class="sectionSub">
                    <table class="carbonFormTable">
                        <tr>
                            <td style="width:15%" class="leftCol-med labelField"><fmt:message
                                    key='config.template.info.basic.name'/>:<span class="required">*</span></td>
                            <td>
                                <input id="templateName" name="templateName" type="text"
                                       value="<%=Encode.forHtmlAttribute(templateName)%>"
                                       white-list-patterns="^[a-zA-Z0-9\s._-]*$" autofocus/>
                                <div class="sectionHelp">
                                    <fmt:message key='help.name'/>
                                </div>
                            </td>
                        </tr>
                        <tr>
                            
                            <td class="leftCol-med labelField">Description:</td>
                            <td>
                                <textarea style="width:50%" type="text" name="template-description"
                                          id="template-description"
                                          class="text-box-big"><%=template.getDescription() != null ?
                                        Encode.forHtmlContent(template.getDescription()) : ""%>
                                </textarea>
                                <div class="sectionHelp">
                                    <fmt:message key='help.desc'/>
                                </div>
                            </td>
                        </tr>
                    </table>
                </div>
                
                <h2 id="authentication_step_config_head" class="sectionSeperator trigger active">
                    <a href="#">Template Script</a>
                </h2>
                <div class="toggle_container" id="editorRow">
                    <div style="position: relative;">
                        <div id="codeMirror">
            <textarea id="scriptTextArea" name="scriptTextArea"
                      placeholder="Write JavaScript Function..."
                      style="height: 500px;width: 100%; display: none;"><%=template.getTemplateScript() != null ?
                    Encode.forHtmlContent(template.getTemplateScript()) : "" %>
            </textarea>
                        
                        </div>
                    
                    </div>
                </div>
                <div style="clear:both"></div>
                <div class="buttonRow" style=" margin-top: 10px;">
                    <input id="update" type="button" value="<fmt:message key='button.update.template.manager'/>"
                           onclick="UpdateTemplateOnclick()"/>
                    <input type="button" onclick="javascript:location.href='list-templates.jsp'"
                           value="<fmt:message key='button.cancel'/>"/>
                </div>
            </form>
        </div>
    </div>
</fmt:bundle>
<script src="./js/template-mgt.js"></script>