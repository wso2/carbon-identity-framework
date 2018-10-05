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

<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.xsd.DefaultAuthenticationSequence" %>
<%@ page import="org.wso2.carbon.idp.mgt.ui.client.DefaultAuthenticationSeqMgtServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page
        import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ResourceBundle" %>

<fmt:bundle
        basename="org.wso2.carbon.idp.mgt.ui.i18n.Resources">
    <carbon:breadcrumb label="breadcrumb.default.seq.mgt"
                       resourceBundle="org.wso2.carbon.idp.mgt.ui.i18n.Resources"
                       topPage="true" request="<%=request%>"/>
    <script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/main.js"></script>
    <script type="text/javascript" src="../identity/validation/js/identity-validate.js"></script>
    <%
        String seqContent = "";
        String seqDesc = "";

        String BUNDLE = "org.wso2.carbon.idp.mgt.ui.i18n.Resources";
        ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

        String[] updateError = (String[]) request.getSession().getAttribute("updateError");
        if (updateError == null) {
            updateError = new String[0];
        }

        try {
            String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
            String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
            ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
                    .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            DefaultAuthenticationSeqMgtServiceClient serviceClient = new
                    DefaultAuthenticationSeqMgtServiceClient(cookie, backendServerURL, configContext);
            DefaultAuthenticationSequence sequence = serviceClient.getDefaultAuthenticationSeq();
            if (sequence == null || StringUtils.isBlank(sequence.getContentXml())) {
                String message = resourceBundle.getString("alert.error.load.default.seq");
                CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
                return;
            }
            String receivedSeqDesc = sequence.getDescription();
            seqDesc = receivedSeqDesc != null ? receivedSeqDesc.trim() : "";
            String receivedSeqContent = sequence.getContentXml();
            seqContent = receivedSeqContent != null ? receivedSeqContent.trim() : "";
        } catch (Exception e) {
            String message = resourceBundle.getString("alert.error.load.default.seq");
            CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request, e);
        }
    %>

    <script>
        function updateDefaultAuthSeq() {
            if (checkEmptyEditorContent()) {
                CARBON.showWarningDialog('<%=resourceBundle.getString("alert.error.default.seq.content.available")%>');
                location.href = '#';
                return false;
            }

            var showErr = false;
            var showWarn = false;
            var adaptiveScript = extractAdaptiveAuthScript(doc.getValue(), $(".warningListContainer"),
                $(".errorListContainer"));
            if (typeof adaptiveScript !== 'undefined' && adaptiveScript !== null) {
                checkJSSyntaxError(adaptiveScript, $(".warningListContainer"), $(".errorListContainer"));
            }

            var withoutAdaptiveScript = extractWithoutAuthScript(doc.getValue());
            validateStepConfig(adaptiveScript, withoutAdaptiveScript, $(".warningListContainer"),
                $(".errorListContainer"));

            if ($(".messagebox-error-custom li").length > 0) {
                $(".editor-error-content").show();
                showErr = true;
            }

            if ($(".messagebox-warning-custom li").length > 0) {
                $(".editor-warning-content").show();
                showWarn = true;
            }

            if (showErr) {
                $(".err_warn_text").text('Update default authentication sequence with errors in adaptive authentication script?');
                showPopupConfirm($(".editor-error-warn-container").html(), "WSO2 Carbon", 250, 550, "OK", "Cancel",
                    updateDefaultSeq, removeHtmlContent);
            } else if (showWarn) {
                $(".err_warn_text").text('Update default authentication sequence with warnings in adaptive authentication script?');
                showPopupConfirm($(".editor-error-warn-container").html(), "WSO2 Carbon", 250, 550, "OK", "Cancel",
                    updateDefaultSeq, removeHtmlContent);
            } else {
                updateDefaultSeq();
            }
        }

        $(function () {
            $("#updateErrorMsgDialog").dialog({
                autoOpen: false,
                modal: true,
                buttons: {
                    OK: closeUpdateErrorDialog
                },
                width: "fit-content"
            });
        });

        function closeUpdateErrorDialog() {
            $(this).dialog("close");
            <%
             request.getSession().removeAttribute("updateError");
            %>
        }

        window.onload = function () {
            showManual();
            <% if (updateError.length > 0) { %>
            $("#updateErrorMsgDialog").show();
            $("#updateErrorMsgDialog").dialog("open");
            <% } %>
        };

        function showManual() {
            $("#update-default-authSeq-form").show();
            $("#updateErrorMsgDialog").hide();
        }
    </script>

    <div id="middle">
    <h2>
        <fmt:message key='title.default.seq.update'/>
    </h2>
    <div id="workArea">
        <form id="update-default-authSeq-form" name="update-default-authSeq-form" method="post"
              action="edit-default-authSeq-finish-ajaxprocessor.jsp">
            <table class="carbonFormTable">
                <tr>
                    <td style="width:15%" class="leftCol-med labelField"><fmt:message
                            key='config.default.seq.desc'/>:
                    </td>
                    <td>
                        <textarea style="width:50%" type="text" name="sequence-desc" id="sequence-desc"
                                  class="text-box-big"><%=Encode.forHtmlContent(seqDesc)%></textarea>
                        <div class="sectionHelp">
                            <fmt:message key='help.default.seq.desc'/>
                        </div>
                    </td>
                </tr>
            </table>
            <div class="toggle_container sectionSub" id="editorRow">
                <div style="position: relative;">
                    <div class="sectionSub step_contents" id="codeMirror">
                        <textarea id="seqContent" name="seqContent" readonly="false"
                                  style="height: 500px;width: 100%; display: none;"><%out.print(Encode.forHtmlContent(seqContent));%></textarea>
                    </div>
                </div>
            </div>
            <br/>
            <div class="buttonRow">
                <input id="updateSeq" type="button" class="button"
                       value="<fmt:message key='button.update.default.seq'/>"
                       onclick="updateDefaultAuthSeq();"/>
                <input type="button" class="button"
                       onclick="javascript:location.href='idp-mgt-edit-local.jsp?selectDefaultSeq=true'"
                       value="<fmt:message key='button.cancel'/>"/>
            </div>
        </form>
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
    <div id="updateErrorMsgDialog" title='WSO2 Carbon' style="display: none">
        <div id="messagebox-error">
            <h3>
                <fmt:message key="alert.error.update.default.seq"/>
            </h3>
            <table style="margin-top:10px;">
                <%
                    for (String error : updateError) {
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

