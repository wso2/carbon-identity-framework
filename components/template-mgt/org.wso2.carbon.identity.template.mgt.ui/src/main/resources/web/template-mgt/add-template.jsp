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

<carbon:breadcrumb label="breadcrumb.template.mgt"
                   resourceBundle="org.wso2.carbon.identity.template.mgt.ui.i18n.Resources" topPage="true"
                   request="<%=request%>"/>
<jsp:include page="../dialog/display_messages.jsp"/>

<script type="text/javascript">
    function createTemplateOnclick() {
        checkEmptyEditorContent();
        var templateName = document.getElementById("templateName").value.trim();
        var content = document.getElementById('scriptTextArea').value;
        if (templateName == '') {
            CARBON.showWarningDialog('Template name cannot be empty.');
            location.href = '#';
        } else if (!validateTextForIllegal(document.getElementById("templateName"))) {
            return false;
        } else {
            if (content == '') {
                CARBON.showWarningDialog('Template script cannot be empty.');
                location.href = '#';
            }
            $("#add-template-form").submit();
            return true;
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

    window.onload = function () {
        showManual();
    }

</script>

<fmt:bundle basename="org.wso2.carbon.identity.template.mgt.ui.i18n.Resources">
    <%
        String script = "<!-- You can customize the template here... -->\n" +
                "<!-- A sample template is provided below -->\n" +
                "\t\n" +
                "<div class=\"uppercase\">\n" +
                "    <h3>Welcome {{name}}</h3>\n" +
                "</div>\n" +
                "\n" +
                "<div class=\"boarder-all \">\n" +
                "    <div class=\"clearfix\"></div>\n" +
                "    <div class=\"padding-double login-form\">\n" +
                "\n" +
                "        <form id=\"template-form\" method=\"POST\"> <!-- *DO NOT CHANGE THIS* -->\n" +
                "            <div class=\"col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group required\">\n" +
                "\n" +
                
                "                <label for=\"sampleInput\" class=\"control-label\">sample input</label>\n" +
                "                <input type=\"text\" id=\"sampleInput\" name=\"sample_input\" class=\"form-control\" " +
                "placeholder=\"sample input placeholder\" />\n" +
                "\n" +
                "            </div>\n" +
                "\n" +
                "            <input type=\"hidden\" id=\"promptResp\" name=\"promptResp\" value=\"true\"> " +
                "<!-- *DO NOT CHANGE THIS* -->\n" +
                "            <input type=\"hidden\" id=\"promptId\" name=\"promptId\"> <!-- *DO NOT CHANGE THIS* -->\n" +
                "\n" +
                "            <div class=\"col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group required\">\n" +
                "                <input type=\"submit\" class=\"wr-btn grey-bg col-xs-12 col-md-12 col-lg-12" +
                " uppercase font-extra-large\" value=\"Submit\">\n" +
                "            </div>\n" +
                "        </form>\n" +
                "        <div class=\"clearfix\"></div>\n" +
                "    </div>\n" +
                "</div>";
    %>
    
    <div id="middle">
        <h2>Add New Template</h2>
        <div id="workArea">
            <form id="add-template-form" name="add-template-form" method="post"
                  action="add-template-finish.jsp">
                <div class="sectionSeperator togglebleTitle"><fmt:message
                        key='title.config.template.basic.config'/></div>
                <div class="sectionSub">
                    <table class="carbonFormTable">
                        <tr>
                            <td style="width:15%" class="leftCol-med labelField"><fmt:message
                                    key='config.template.info.basic.name'/>:<span class="required">*</span></td>
                            <td>
                                <input id="templateName" name="templateName" type="text" value=""
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
                                          id="template-description" class="text-box-big"></textarea>
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
                        <div id="codeMirror" class="sectionSub step_contents">
            <textarea id="scriptTextArea" name="scriptTextArea"
                      style="height: 500px;width: 100%; display: none;"><%=script%></textarea>
                        </div>
                    </div>
                </div>
                
                <div style="clear:both"></div>
                <div class="buttonRow" style=" margin-top: 10px;">
                    <input id="createLib" type="button" value="<fmt:message key='button.reg.template.manager'/>"
                           onclick="createTemplateOnclick()"/>
                    <input type="button" onclick="javascript:location.href='list-templates.jsp'"
                           value="<fmt:message key='button.cancel'/>"/>
                </div>
            </form>
        </div>
    </div>
</fmt:bundle>

<script src="./js/template-mgt.js"></script>
