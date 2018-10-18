<%--
  ~ Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~  Licensed under the Apache License, Version 2.0 (the "License");
  ~  you may not use this file except in compliance with the License.
  ~  You may obtain a copy of the License at
  ~
  ~  http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~  Unless required by applicable law or agreed to in writing, software
  ~  distributed under the License is distributed on an "AS IS" BASIS,
  ~  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~  See the License for the specific language governing permissions and
  ~  limitations under the License.
  --%>

<link rel="stylesheet" href="codemirror/lib/codemirror.css">
<link rel="stylesheet" href="codemirror/theme/mdn-like.css">
<link rel="stylesheet" href="codemirror/addon/dialog/dialog.css">
<link rel="stylesheet" href="codemirror/addon/display/fullscreen.css">
<link rel="stylesheet" href="codemirror/addon/fold/foldgutter.css">
<link rel="stylesheet" href="codemirror/addon/hint/show-hint.css">
<link rel="stylesheet" href="codemirror/addon/lint/lint.css">
<link rel="stylesheet" href="css/default-auth-seq-mgt.css">
<link rel="stylesheet" href="css/edit-default-authSeq.css">
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

<script type="text/javascript" src="extensions/js/vui.js"></script>
<script type="text/javascript" src="../extensions/core/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<script type="text/javascript" src="../identity/validation/js/identity-validate.js"></script>
<jsp:include page="../dialog/display_messages.jsp"/>

<%@ page import="java.util.ResourceBundle" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" %>

<carbon:breadcrumb label="breadcrumb.default.seq.mgt"
                   resourceBundle="org.wso2.carbon.idp.mgt.ui.i18n.Resources"
                   topPage="true"
                   request="<%=request%>"/>
<script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
<script type="text/javascript" src="../carbon/admin/js/main.js"></script>
<script type="text/javascript" src="../identity/validation/js/identity-validate.js"></script>
<jsp:include page="../dialog/display_messages.jsp"/>

<%
    String[] createError = (String[]) request.getSession().getAttribute("createError");
    if (createError == null) {
        createError = new String[0];
    }
    String mode = request.getParameter("mode");

    String BUNDLE = "org.wso2.carbon.idp.mgt.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
%>
<script type="text/javascript">
    var openFile = function (event) {
        var input = event.target;
        var reader = new FileReader();
        reader.onload = function () {
            document.getElementById('sequence-file-content').value = reader.result;
        };
        document.getElementById('sequence-file-name').value = input.files[0].name;
        reader.readAsText(input.files[0]);
    };

    function addDefaultAuthnSeq() {
        if (document.getElementById('file-option').checked) {
            var seqContent = $.trim(document.getElementById('sequence-file-content').value);
            if (seqContent === null || 0 === seqContent.length) {
                CARBON.showWarningDialog('<%=resourceBundle.getString("alert.error.default.seq.file.available")%>');
                location.href = '#';
                return false;
            } else {
                document.getElementById('selected-mode').value = "file";
                var adaptiveScript = extractAdaptiveAuthScript(seqContent, $(".warningListContainer"),
                    $(".errorListContainer"));
                if (typeof adaptiveScript !== 'undefined' && adaptiveScript !== null) {
                    checkJSSyntaxErrorOfFile(adaptiveScript, $(".warningListContainer"), $(".errorListContainer"));
                }
                var withoutAdaptiveScript = extractWithoutAuthScript(seqContent);
                validateStepConfigOfFile(adaptiveScript, withoutAdaptiveScript, $(".warningListContainer"),
                    $(".errorListContainer"));
                addSeqAndSendError();
            }
        } else if (document.getElementById('manual-option').checked) {
            if (checkEmptyEditorContent()) {
                CARBON.showWarningDialog('<%=resourceBundle.getString("alert.error.default.seq.content.available")%>');
                location.href = '#';
                return false;
            } else {
                document.getElementById('selected-mode').value = "manual";
                var adaptiveScript = extractAdaptiveAuthScript(doc.getValue(), $(".warningListContainer"),
                    $(".errorListContainer"));
                if (typeof adaptiveScript !== 'undefined' && adaptiveScript !== null) {
                    checkJSSyntaxError(adaptiveScript, $(".warningListContainer"), $(".errorListContainer"));
                }
                var withoutAdaptiveScript = extractWithoutAuthScript(doc.getValue());
                validateStepConfig(adaptiveScript, withoutAdaptiveScript, $(".warningListContainer"),
                    $(".errorListContainer"));
                addSeqAndSendError();
            }
        }
    }

    function addSeqAndSendError() {
        var showErr = false;
        var showWarn = false;
        if ($(".messagebox-error-custom li").length > 0) {
            $(".editor-error-content").show();
            showErr = true;
        }

        if ($(".messagebox-warning-custom li").length > 0) {
            $(".editor-warning-content").show();
            showWarn = true;
        }

        if (showErr) {
            $(".err_warn_text").text('Add default authentication sequence with errors in adaptive authentication script?');
            showPopupConfirm($(".editor-error-warn-container").html(), "WSO2 Carbon", 250, 550, "OK", "Cancel",
                addDefaultSeq, removeHtmlContent);
        } else if (showWarn) {
            $(".err_warn_text").text('Add default authentication sequence with warnings in adaptive authentication script?');
            showPopupConfirm($(".editor-error-warn-container").html(), "WSO2 Carbon", 250, 550, "OK", "Cancel",
                addDefaultSeq, removeHtmlContent);
        } else {
            addDefaultSeq();
        }
    }

    $(function () {
        $("#createErrorMsgDialog").dialog({
            autoOpen: false,
            modal: true,
            buttons: {
                OK: closeCreateErrorDialog
            },
            width: "fit-content"
        });
    });

    function closeCreateErrorDialog() {
        $(this).dialog("close");
        <%
         request.getSession().removeAttribute("createError");
        %>
    }

    window.onload = function () {
        initialLoad();
        <% if (createError.length > 0) { %>
        $("#createErrorMsgDialog").show();
        $("#createErrorMsgDialog").dialog("open");
        <% } %>
    };

    function showManual() {
        $("#file-config").hide();
        $("#xml-config").show();
        $("#createErrorMsgDialog").hide();
    }

    function showFile() {
        $("#file-config").show();
        $("#xml-config").hide();
        $("#createErrorMsgDialog").hide();
    }

    function initialLoad() {
        <%
        if (mode != null && mode.equals("inline")) { %>
        $("#file-config").hide();
        $("#file-option").prop("checked", false);
        $("#xml-config").show();
        $("#manual-option").prop("checked", true);
        <%
        } else { %>
        $("#file-config").show();
        $("#xml-config").hide();
        <% } %>
        $("#createErrorMsgDialog").hide();
    }
</script>
<fmt:bundle basename="org.wso2.carbon.idp.mgt.ui.i18n.Resources">
    <div id="middle">
        <h2>
            <fmt:message key='title.default.seq.add'/>
        </h2>
        <div id="workArea">
            <form id="add-default-auth-seq-form" name="add-default-auth-seq-form" method="post"
                  action="add-default-authSeq-finish-ajaxprocessor.jsp">
                <table class="carbonFormTable">
                    <tbody>
                    <tr>
                        <td class="leftCol-med labelField" style="width:20%"><fmt:message
                                key='config.default.seq.desc'/>:
                        </td>
                        <td>
                        <textarea style="width:50%; margin-bottom:3px" type="text" name="sequence-description"
                                  id="sequence-description"
                                  class="text-box-big"></textarea>
                            <div class="sectionHelp">
                                <fmt:message key='help.default.seq.desc'/>
                            </div>
                        </td>
                    </tr>
                    </tbody>
                </table>
                <table class="carbonFormTable">
                    <tr>
                        <td class="leftCol-med labelField" style="width:20%"><fmt:message
                                key='config.default.seq.config'/>:<span class="required">*</span>
                        </td>
                        <td style="width:15%">
                            <input type="radio" id="file-option" name="upload-type-selector" checked="checked"
                                   onclick="showFile();">
                            <label for="file-option"><fmt:message key="field.default.seq.add.file"/></label>
                        </td>
                        <td>
                            <input type="radio" id="manual-option" name="upload-type-selector"
                                   onclick="showManual();">
                            <label for="manual-option"><fmt:message key="field.default.seq.add.manual"/></label>
                        </td>
                    </tr>
                </table>
                <table class="carbonFormTable">
                    <tr id="file-config">
                        <td class="leftCol-med labelField" style="width:20%"></td>
                        <td>
                            <input type="file" class="button" id="sp_file" name="sp_file"
                                   onchange='openFile(event)' style="margin-bottom:3px"/>
                            <textarea hidden="hidden" name="sequence-file-content"
                                      id="sequence-file-content"></textarea>
                            <textarea hidden="hidden" name="sequence-file-name" id="sequence-file-name"></textarea>
                            <textarea hidden="hidden" name="sequence-desc" id="sequence-desc-upload"></textarea>
                            <div class="sectionHelp">
                                <fmt:message key='help.default.seq.file'/>
                            </div>
                        </td>
                    </tr>
                </table>
                <table class="carbonFormTable">
                    <tr id="xml-config" style="display: none">
                        <td class="leftCol-med labelField" style="width:20%"></td>
                        <td>
                            <div style="position: relative;">
                                <div class="sectionSub step_contents" id="codeMirror">
                                    <textarea id="seqContent" name="seqContent" readonly="false"
                                              style="height: 500px; width: 100%; display: none; margin-left: 10%"></textarea>
                                </div>
                            </div>
                        </td>
                    </tr>
                    </tbody>
                </table>
                <textarea hidden="hidden" name="selected-mode" id="selected-mode"></textarea>
                <textarea hidden="hidden" name="seqContentXml" id="seqContentXml"></textarea>
            </form>
        </div>
        <div class="buttonRow">
            <input type="button" class="button" value="<fmt:message key='button.import.default.seq'/>"
                   onclick="addDefaultAuthnSeq();"/>
            <input type="button" class="button"
                   onclick="javascript:location.href='idp-mgt-edit-local.jsp?selectDefaultSeq=true'"
                   value="<fmt:message key='button.cancel'/>"/>
        </div>
    </div>
    <div class="editor-error-warn-container">
        <div class="err_warn_text"></div>
        <div class="editor-error-content">
            <div class="messagebox-error-custom">
                <ul class="errorListContainer"></ul>
                <ul class="stepErrorListContainer"></ul>
            </div>
        </div>
        <div class="editor-warning-content" style="padding-bottom: 15px">
            <div class="messagebox-warning-custom">
                <ul class="warningListContainer"></ul>
                <ul class="stepWarningListContainer"></ul>
            </div>
        </div>
    </div>
    <div id="createErrorMsgDialog" title='WSO2 Carbon' style="display: none">
        <div id="messagebox-error">
            <h3>
                <fmt:message key="alert.error.add.default.seq"/>
            </h3>
            <table style="margin-top:10px;">
                <%
                    for (String error : createError) {
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
<script src="js/default_auth_seq.js"></script>
