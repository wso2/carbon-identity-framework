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
<script type="text/javascript" src="../identity/validation/js/identity-validate.js"></script>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" %>

<fmt:bundle basename="org.wso2.carbon.identity.functions.library.mgt.ui.i18n.Resources">
    <carbon:breadcrumb label="functionlib.mgt"
                       resourceBundle="org.wso2.carbon.identity.functions.library.mgt.ui.i18n.Resources"
                       topPage="true" request="<%=request%>"/>
    <jsp:include page="../dialog/display_messages.jsp"/>
    
    <script type="text/javascript">
        function createFunctionLibOnclick() {
            checkEmptyEditorContent();
            var functionLibName = document.getElementById("functionLibName").value.trim();
            var content = document.getElementById('scriptTextArea').value.trim();
            if (functionLibName == '') {
                CARBON.showWarningDialog('<fmt:message key="not.provide.function.library.name"/>');
                location.href = '#';
            } else if (!validateTextForIllegal(document.getElementById("functionLibName"))) {
                return false;
            } else {
                if (content == '') {
                    CARBON.showWarningDialog('<fmt:message key="not.provide.function.library.script"/>');
                    location.href = '#';
                } else {
                    try {
                        eval(prepareScript(content));
                        encodeFunctionLibScript();
                        $("#add-functionlib-form").submit();
                        return true;
                    } catch (e) {
                        CARBON.showWarningDialog('<fmt:message key="error.in.script"/>' + e.lineNumber + " : " + e.message);
                        location.href = '#';
                    }
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

        // Uncomment the following commented section when export functionality is implemented.
        // var openFile = function (event) {
        //     var input = event.target;
        //     var reader = new FileReader();
        //     reader.onload = function () {
        //         var data = reader.result;
        //         document.getElementById('functionlib-file-content').value = data;
        //     };
        //     document.getElementById('functionlib-file-name').value = input.files[0].name;
        //     reader.readAsText(input.files[0]);
        // };
        //
        // function importFunctionLibOnclick() {
        //
        // }

        function showManual() {
            $("#add-functionlib-form").show();
            // Uncomment the following commented section when export functionality is implemented.
            //$("#upload-functionlib-form").hide();
        }

        // Uncomment the following commented section when export functionality is implemented.
        // function showFile() {
        //     $("#add-functionlib-form").hide();
        //     $("#upload-functionlib-form").show();
        // }

        window.onload = function () {
            showManual();
        }
    
    </script>
    
    <div id="middle">
        <h2>Add New Function Library</h2>
        <div id="workArea">
            <table class="styledLeft" width="100%">
                <thead>
                <tr>
                    <th><fmt:message key="title.functionlib.select.mode"/></th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td><input type="radio" id="manual-option" name="upload-type-selector" checked="checked"
                               onclick="showManual();">
                        <label for="manual-option">Manual Configuration</label>
                    </td>
                </tr>
                <%--Uncomment the following commented section when import functionality is implemented.--%>
                <%--<tr>--%>
                    <%--<td>--%>
                        <%--<input type="radio" id="file-option" name="upload-type-selector" onclick="showFile();">--%>
                        <%--<label for="file-option">File Configuration</label>--%>
                    <%--</td>--%>
                <%--</tr>--%>
                </tbody>
            </table>
            <br/>
            <form id="add-functionlib-form" name="add-functionlib-form" method="post"
                  action="add-functionlib-finish-ajaxprocessor.jsp">
                <div class="sectionSeperator togglebleTitle"><fmt:message
                        key='title.config.function.basic.config'/></div>
                <div class="sectionSub">
                    <table class="carbonFormTable">
                        <tr>
                            <td style="width:15%" class="leftCol-med labelField"><fmt:message
                                    key='config.function.info.basic.name'/>:<span class="required">*</span></td>
                            <td>
                                <input id="functionLibName" name="functionLibraryName" type="text" value=""
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
                                <textarea maxlength="1020" style="width:50%" type="text" name="description"
                                          id="functionLib-description" class="text-box-big"></textarea>
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
                        <div id="codeMirror" class="sectionSub step_contents">
                            <textarea id="scriptTextArea" name="scriptContent"
                                      placeholder="Write JavaScript Function..."
                                      style="height: 500px;width: 100%; display: none">
                            </textarea>
                        </div>
                    </div>
                </div>
                
                <div style="clear:both"></div>
                <div class="buttonRow" style=" margin-top: 10px;">
                    <input id="createLib" type="button" value="<fmt:message key='button.reg.function.manager'/>"
                           onclick="createFunctionLibOnclick()"/>
                    <input type="button" onclick="location.href='functions-library-mgt-list.jsp'"
                           value="<fmt:message key='button.cancel'/>"/>
                </div>
            </form>
    
            <%--Uncomment the following commented section when export functionality is implemented.--%>
            <%--<form id="upload-functionlib-form" name="upload-functionlib-form" method="post"--%>
                  <%--action="#">--%>
                <%--<table class="styledLeft" width="100%">--%>
                    <%--<thead>--%>
                    <%--<tr>--%>
                        <%--<th><fmt:message key="upload.function.library.file"/></th>--%>
                    <%--</tr>--%>
                    <%--</thead>--%>
                    <%--<tbody>--%>
                    <%--<tr>--%>
                        <%--<td>--%>
                            <%--<span>File Location: </span><input type="file" class="button" id="functionlib_file"--%>
                                                               <%--name="functionlib_file" onchange='openFile(event)'/>--%>
                        <%--</td>--%>
                        <%--<textarea hidden="hidden" name="functionlib-file-content"--%>
                                  <%--id="functionlib-file-content"></textarea>--%>
                        <%--<textarea hidden="hidden" name="functionlib-file-name" id="functionlib-file-name"></textarea>--%>
                    <%--</tr>--%>
                    <%--<tr>--%>
                        <%--<td>--%>
                            <%--<input type="button" class="button"--%>
                                   <%--value="<fmt:message key='button.import.function.library'/>"--%>
                                   <%--onclick="importFunctionLibOnclick();"/>--%>
                            <%--<input type="button" class="button"--%>
                                   <%--onclick="location.href='functions-library-mgt-list.jsp'"--%>
                                   <%--value="<fmt:message key='button.cancel'/>"/>--%>
                        <%--</td>--%>
                    <%--</tr>--%>
                    <%--</tbody>--%>
                <%--</table>--%>
            <%--</form>--%>
        </div>
    </div>
</fmt:bundle>
<script src="./js/function-lib-mgt.js"></script>
