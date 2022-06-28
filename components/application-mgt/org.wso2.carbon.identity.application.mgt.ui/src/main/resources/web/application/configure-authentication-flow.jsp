<!--
~ Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
-->
<link rel="stylesheet" href="codemirror/lib/codemirror.css">
<link rel="stylesheet" href="codemirror/theme/mdn-like.css">
<link rel="stylesheet" href="codemirror/addon/dialog/dialog.css">
<link rel="stylesheet" href="codemirror/addon/display/fullscreen.css">
<link rel="stylesheet" href="codemirror/addon/fold/foldgutter.css">
<link rel="stylesheet" href="codemirror/addon/hint/show-hint.css">
<link rel="stylesheet" href="codemirror/addon/lint/lint.css">

<link rel="stylesheet" href="css/idpmgt.css">
<link rel="stylesheet" href="css/conditional-authentication.css">

<script src="codemirror/lib/codemirror.js"></script>
<script src="codemirror/keymap/sublime.js"></script>
<script src="codemirror/mode/javascript/javascript.js"></script>

<script src="codemirror/addon/lint/jshint.min.js"></script>
<script src="codemirror/addon/lint/lint.js"></script>
<script src="codemirror/addon/lint/javascript-lint.js"></script>
<script src="codemirror/addon/hint/anyword-hint.js"></script>
<script src="codemirror/addon/hint/show-hint.js"></script>
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
<script src="js/handlebars.min-v4.7.7.js"></script>

<script src="../admin/js/main.js" type="text/javascript"></script>
<script type="text/javascript" src="../identity/validation/js/identity-validate.js"></script>

<%@ page import="com.google.gson.JsonArray" %>

<%@ page import="com.google.gson.JsonPrimitive" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.xsd.AuthenticationStep" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.xsd.FederatedAuthenticatorConfig" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.xsd.IdentityProvider" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.xsd.LocalAuthenticatorConfig" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.xsd.Property" %>
<%@ page import="org.wso2.carbon.identity.application.mgt.ui.ApplicationBean" %>
<%@ page import="org.wso2.carbon.identity.application.mgt.ui.client.ApplicationManagementServiceClient" %>
<%@ page import="org.wso2.carbon.identity.application.mgt.ui.client.ConditionalAuthMgtClient" %>
<%@ page import="org.wso2.carbon.identity.application.mgt.ui.util.ApplicationMgtUIUtil" %>
<%@ page import="org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.ResourceBundle" %>

<%!public static final String IS_HANDLER = "IS_HANDLER";%>
<%!public static final String BACKUP_CODE_AUTHENTICATOR = "backup-code-authenticator";%>
<carbon:breadcrumb label="breadcrumb.advanced.auth.step.config"
                   resourceBundle="org.wso2.carbon.identity.application.mgt.ui.i18n.Resources"
                   topPage="true" request="<%=request%>"/>
<jsp:include page="../dialog/display_messages.jsp"/>

<% String BUNDLE = "org.wso2.carbon.identity.application.mgt.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

    ApplicationBean appBean = ApplicationMgtUIUtil.getApplicationBeanFromSession(session, request.getParameter("spName"));
    String spName = appBean.getServiceProvider().getApplicationName();
    Map<String, String> claimMapping = appBean.getClaimMapping();

    
    LocalAuthenticatorConfig[] localAuthenticatorConfigs = appBean.getLocalAuthenticatorConfigs();
    IdentityProvider[] federatedIdPs = appBean.getFederatedIdentityProviders();
    String templatesJson = null;
    String availableJsFunctionsJson = null;
    
    StringBuilder localAuthTypes = new StringBuilder();
    String startOption = "<option value=\"";
    String middleOption = "\">";
    String endOption = "</option>";
    
    if (localAuthenticatorConfigs != null && localAuthenticatorConfigs.length > 0) {
        for (LocalAuthenticatorConfig auth : localAuthenticatorConfigs) {
            if (auth.getName().equals(BACKUP_CODE_AUTHENTICATOR)) {
                continue;
            }
            localAuthTypes.append(startOption).append(Encode.forHtmlAttribute(auth.getName())).append(middleOption)
                .append(Encode.forHtmlContent(auth.getDisplayName())).append(endOption);
        }
    }
    
    try {
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        ApplicationManagementServiceClient serviceClient = new ApplicationManagementServiceClient(cookie, backendServerURL, configContext);
        templatesJson = serviceClient.getAuthenticationTemplatesJson();
        ConditionalAuthMgtClient conditionalAuthMgtClient = new
            ConditionalAuthMgtClient(cookie, backendServerURL, configContext);
        String[] functionList = conditionalAuthMgtClient.listAvailableFunctions();
        JsonArray jsonArray = new JsonArray();
        for (String function : functionList) {
            jsonArray.add(new JsonPrimitive(function));
        }
        availableJsFunctionsJson = jsonArray.toString();
    } catch (Exception e) {
        CarbonUIMessage.sendCarbonUIMessage("Error occurred while loading SP advanced outbound authentication " +
            "configuration", CarbonUIMessage.ERROR, request, e);
    }
    if (templatesJson == null) {
        templatesJson = "";
    }
    templatesJson = StringEscapeUtils.escapeJavaScript(templatesJson);

%>

<%
    
    StringBuilder idpType = new StringBuilder();
    StringBuilder enabledIdpType = new StringBuilder();
    Map<String, String> idpAuthenticators = new HashMap<String, String>();
    Map<String, String> enabledIdpAuthenticators = new HashMap<String, String>();
    Map<String, Boolean> idpEnableStatus = new HashMap<String, Boolean>();
    Map<String, Boolean> idpAuthenticatorsStatus = new HashMap<String, Boolean>();
    
    if (federatedIdPs != null && federatedIdPs.length > 0) {
        for (IdentityProvider idp : federatedIdPs) {
            idpEnableStatus.put(idp.getIdentityProviderName(), idp.getEnable());
            if (idp.getFederatedAuthenticatorConfigs() != null && idp.getFederatedAuthenticatorConfigs().length > 0) {
                StringBuffer fedAuthenticatorDisplayType = new StringBuffer();
                StringBuffer fedAuthenticatorType = new StringBuffer();
                StringBuffer fedAuthType = new StringBuffer();
                StringBuffer enabledfedAuthType = new StringBuffer();
                
                int i = 1;
                for (FederatedAuthenticatorConfig fedAuth : idp.getFederatedAuthenticatorConfigs()) {
                    if (i == idp.getFederatedAuthenticatorConfigs().length) {
                        fedAuthenticatorDisplayType.append(fedAuth.getDisplayName());
                        fedAuthenticatorType.append(fedAuth.getName());
                    } else {
                        fedAuthenticatorDisplayType.append(fedAuth.getDisplayName()).append("%fed_auth_sep_%");
                        fedAuthenticatorType.append(fedAuth.getName()).append("%fed_auth_sep_%");
                    }
                    
                    fedAuthType.append(startOption).append(Encode.forHtmlAttribute(fedAuth.getName()))
                        .append(middleOption).append(Encode.forHtmlContent(fedAuth.getDisplayName())).append(endOption);
                    if (fedAuth.getEnabled()) {
                        enabledfedAuthType.append(startOption).append(Encode.forHtmlAttribute(fedAuth.getName()))
                            .append(middleOption).append(Encode.forHtmlContent(fedAuth.getDisplayName()))
                            .append(endOption);
                    }
                    idpAuthenticatorsStatus.put(idp.getIdentityProviderName() + "_" + fedAuth.getName(),
                        fedAuth.getEnabled());
                    i++;
                }
                
                idpAuthenticators.put(idp.getIdentityProviderName(), fedAuthType.toString());
                enabledIdpAuthenticators.put(idp.getIdentityProviderName(), enabledfedAuthType.toString());
                
                idpType.append(startOption).append(Encode.forHtmlAttribute(idp.getIdentityProviderName()))
                    .append("\" data=\"").append(Encode.forHtmlAttribute(fedAuthenticatorDisplayType.toString()))
                    .append("\"").append(" data-values=\"")
                    .append(Encode.forHtmlAttribute(fedAuthenticatorType.toString())).append("\" >")
                    .append(Encode.forHtmlContent(idp.getIdentityProviderName())).append(endOption);
                if (idp.getEnable() && enabledfedAuthType.length() > 0) {
                    enabledIdpType.append(startOption).append(Encode.forHtmlAttribute(idp.getIdentityProviderName()))
                        .append("\" data=\"").append(Encode.forHtmlAttribute(fedAuthenticatorDisplayType.toString()))
                        .append("\"").append(" data-values=\"")
                        .append(Encode.forHtmlAttribute(fedAuthenticatorType.toString())).append("\" >")
                        .append(Encode.forHtmlContent(idp.getIdentityProviderName())).append(endOption);
                }
            }
        }
    }
    
    AuthenticationStep[] steps =
        appBean.getServiceProvider().getLocalAndOutBoundAuthenticationConfig().getAuthenticationSteps();
    Map<String, String> stepIdpAuthenticators = new HashMap<String, String>();
    
    if (steps != null && steps.length > 0) {
        for (AuthenticationStep step : steps) {
            IdentityProvider[] stepFedIdps = step.getFederatedIdentityProviders();
            if (stepFedIdps != null && stepFedIdps.length > 0) {
                for (IdentityProvider idp : stepFedIdps) {
                    if (idp == null) continue;
                    FederatedAuthenticatorConfig fedAuth = idp.getDefaultAuthenticatorConfig();
                    String options = idpAuthenticators.get(idp.getIdentityProviderName());
                    if (fedAuth != null && options != null) {
                        String oldOption = startOption + Encode.forHtmlAttribute(fedAuth.getName()) + middleOption + Encode.forHtmlContent(fedAuth.getDisplayName()) + endOption;
                        String newOption = startOption + Encode.forHtmlAttribute(fedAuth.getName()) + "\" selected=\"selected" + middleOption + Encode.forHtmlContent(fedAuth.getDisplayName()) + endOption;
                        if (options.contains(oldOption)) {
                            options = options.replace(oldOption, newOption);
                        } else {
                            options = options + newOption;
                        }
                        stepIdpAuthenticators.put(step.getStepOrder() + "_" + idp.getIdentityProviderName(), options);
                    } else {
                        // No saved Federated Authenticators
                        options = enabledIdpAuthenticators.get(idp.getIdentityProviderName());
                        stepIdpAuthenticators.put(step.getStepOrder() + "_" + idp.getIdentityProviderName(), options);
                    }
                }
            }
        }
    }

%>

<script>
    function saveAsDefaultAuthSeq() {
        checkEmptyEditorContentForDefaultSeq();
        if (checkEmptyStep()) {
            CARBON.showErrorDialog('Some authentication steps do not have authenticators. Add missing authenticators ' +
                'or delete the empty step.',
                null, null);
            return false;
        }

        if (!checkAuthenticators()) {
            CARBON.showErrorDialog('You cannot add identifier as the only authenticator. Add more authenticators or' +
                ' add more authentication steps.',
                null, null);
            return false;
        }

        var showErr = false;
        var showWarn = false;

        getStepErrorsWarnings($(".stepWarningListContainer"), $(".stepErrorListContainer"));
        getEditorErrorsWarnings($(".warningListContainer"), $(".errorListContainer"));

        if ($(".messagebox-error-custom li").length > 0) {
            $(".editor-error-content").show();
            showErr = true;
        }

        if ($(".messagebox-warning-custom li").length > 0) {
            $(".editor-warning-content").show();
            showWarn = true;
        }

        if (showErr) {
            $(".err_warn_text").text('Update script with errors?');
            showPopupConfirm($(".editor-error-warn-container").html(), "WSO2 Carbon", 250, 550, "OK", "Cancel",
                showAddDefaultSeqConfirm, removeHtmlContent);
        } else if (showWarn) {
            $(".err_warn_text").text('Update script with warnings?');
            showPopupConfirm($(".editor-error-warn-container").html(), "WSO2 Carbon", 250, 550, "OK", "Cancel",
                showAddDefaultSeqConfirm, removeHtmlContent);
        } else {
            showAddDefaultSeqConfirm();
        }
    }

    function showAddDefaultSeqConfirm() {
        showPopupConfirmForDefaultAuthSeq($("#add_default_AuthSeq").html(), "<%=resourceBundle.getString("save.default.seq")%>",
            250, 550, "<%=resourceBundle.getString("button.save.default.seq")%>",
            "<%=resourceBundle.getString("button.cancel.default.seq")%>", saveSequence, null);
    }

    function saveSequence() {
        var seqDesc = "";
        $(".sequence-desc").each(function () {
            if (this.value != "") {
                seqDesc = $.trim(this.value);
            }
        });
        document.getElementById('seqDesc').value = seqDesc;

        <%
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
                .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
         boolean isSeqExists = false;
         if (isSeqExists) { %>
        CARBON.showConfirmationDialog('<%=resourceBundle.getString("alert.confirm.override.default.seq")%>', saveSeq,
            null);
        <% } else {
        %>
        saveSeq();
        <%
        }
        %>
    }

    function saveSeq() {
        $.ajax({
            type: "POST",
            url: 'add-as-default-authSeq.jsp?isSeqExists=<%=isSeqExists%>',
            data: $("#configure-auth-flow-form").serialize(),
            success: function (data, response, status) {
                if (data.match("createError") != null) {
                    CARBON.showErrorDialog('<%=resourceBundle.getString("alert.error.add.default.seq")%>');
                    return;
                } else {
                    CARBON.showInfoDialog('<%=resourceBundle.getString("alert.info.add.default.seq")%>');
                    return;
                }
            },
            error: function (xhr, ajaxOptions, thrownError) {
                CARBON.showErrorDialog('<%=resourceBundle.getString("alert.error.add.default.seq")%>' + xhr.status +
                    thrownError);
            },
            async: false
        });
    }
</script>

<fmt:bundle basename="org.wso2.carbon.identity.application.mgt.ui.i18n.Resources">
    <div id="middle">
        <h2>
            <fmt:message key='breadcrumb.advanced.auth.step.config.for'/><%=Encode.forHtmlContent(spName)%>
        </h2>
        <div id="workArea">
            <form id="configure-auth-flow-form" name="configure-auth-flow-form" method="post"
                  action="configure-authentication-flow-finish-ajaxprocessor.jsp">
                <input type=hidden name=spName value='<%=Encode.forHtmlAttribute(spName)%>'/>


                <h2 id="authentication_step_config_head" class="sectionSeperator trigger">
                    <a href="#"><fmt:message key="title.config.authentication.steps"/></a>
                </h2>

                <div class="toggle_container sectionSub" style="margin-bottom:10px;" id="stepsConfRow">
                    <table>
                        <tr>
                            <td><a id="stepsAddLink" class="icon-link"
                                   style="background-image:url(images/add.gif);margin-left:0" href="#"><fmt:message
                                    key='button.add.step'/></a></td>
                        </tr>
                    </table>
                    <div class="steps">

                        <%
                            if (steps != null && steps.length > 0) {
                                for (AuthenticationStep step : steps) {
                        %>

                        <h2 id="step_head_<%=step.getStepOrder()%>" class="sectionSeperator trigger active step_heads"
                            style="background-color: beige; clear: both;">
                            <input type="hidden" value="<%=step.getStepOrder()%>" name="auth_step" id="auth_step"/>
                            <a class="step_order_header" href="#">Step <%=step.getStepOrder()%>
                            </a>
                            <a href="#" class="delete_step icon-link"
                               style="background-image: url(images/delete.gif);float:right;width: 9px;" data-step-no="<%=step.getStepOrder()%>"></a>
                        </h2>
                        <div class="toggle_container sectionSub step_contents step_body" style="margin-bottom:10px;display: none;"
                             id="step_dev_<%=step.getStepOrder()%>">
                            <div style="padding-bottom: 5px">
                                <table class="carbonFormTable">
                                    <tr>
                                        <td><input type="checkbox" style="vertical-align: middle;"
                                                   id="subject_step_<%=step.getStepOrder()%>"
                                                   name="subject_step_<%=step.getStepOrder()%>" class="subject_steps"
                                                   onclick="setSubjectStep(this)" <%=step.getSubjectStep() ? "checked" : "" %>><label
                                                for="subject_step_<%=step.getStepOrder()%>" style="cursor: pointer;">Use
                                            subject
                                            identifier from this step</label></td>
                                    </tr>
                                    <tr>
                                        <td><input type="checkbox" style="vertical-align: middle;"
                                                   id="attribute_step_<%=step.getStepOrder()%>"
                                                   name="attribute_step_<%=step.getStepOrder()%>"
                                                   class="attribute_steps"
                                                   onclick="setAttributeStep(this)" <%=step.getAttributeStep() ? "checked" : "" %>><label
                                                for="attribute_step_<%=step.getStepOrder()%>" style="cursor: pointer;">Use
                                            attributes from this step</label></td>
                                    </tr>
                                </table>
                            </div>
                            <h2 id="local_auth_head_<%=step.getStepOrder()%>" class="sectionSeperator trigger active"
                                style="background-color: floralwhite;">
                                <a href="#">Local Authenticators</a>
                            </h2>
                            <div class="toggle_container sectionSub" style="margin-bottom:10px;"
                                 id="local_auth_head_dev_<%=step.getStepOrder()%>">
                                <table class="styledLeft auth_table" width="100%" id="local_auth_table_<%=step.getStepOrder()%>">
                                    <thead>
                                    <tr>
                                        <td>
                                            <select name="step_<%=step.getStepOrder()%>_local_oauth_select"
                                                    style="float: left; min-width: 150px;font-size:13px;">
                                                <%=localAuthTypes.toString()%>
                                            </select>
                                            <a id="localOptionAddLinkStep_<%=step.getStepOrder()%>"
                                               onclick="addLocalRow(this,'<%=step.getStepOrder()%>');return false;"
                                               class="icon-link claimmappingAddLinkss claimMappingAddLinkssLocal"
                                               style="background-image:url(images/add.gif);">Add Authenticator
                                            </a>
                                        </td>
                                    </tr>
                                    </thead>
                                    <%
                                        LocalAuthenticatorConfig[] lclAuthenticators = step.getLocalAuthenticatorConfigs();

                                        if (lclAuthenticators != null && lclAuthenticators.length > 0) {
                                            int i = 0;
                                            for (LocalAuthenticatorConfig lclAuthenticator : lclAuthenticators) {
                                                if (lclAuthenticator != null) {
                                    %>
                                    <tr>
                                        <td>
                                            <input name="step_<%=step.getStepOrder()%>_local_auth" id="" type="hidden"
                                                   value="<%=Encode.forHtmlAttribute(lclAuthenticator.getName())%>"/>
                                            <%=Encode.forHtmlContent(lclAuthenticator.getDisplayName())%>
                                        </td>
                                        <td class="leftCol-small">
                                            <a onclick="deleteLocalAuthRow(this);return false;" href="#"
                                               class="icon-link"
                                               style="background-image: url(images/delete.gif)"> Delete </a>
                                        </td>
                                    </tr>
                                    <%
                                                }
                                            }
                                        }
                                    %>
                                </table>
                            </div>

                            <%if (federatedIdPs != null && federatedIdPs.length > 0 && (enabledIdpType.length() > 0 || (step.getFederatedIdentityProviders() != null && step.getFederatedIdentityProviders().length > 0))) { %>
                            <h2 id="fed_auth_head_<%=step.getStepOrder()%>" class="sectionSeperator trigger active"
                                style="background-color: floralwhite;">
                                <a href="#">Federated Authenticators</a>
                            </h2>

                            <div class="toggle_container sectionSub" style="margin-bottom:10px;"
                                 id="fed_auth_head_dev_<%=step.getStepOrder()%>">
                                <table class="styledLeft auth_table" width="100%" id="fed_auth_table_<%=step.getStepOrder()%>">
                                    <thead>
                                    <tr style="<%=enabledIdpType.length() > 0 ? "" : "display:none"%>">
                                        <td>
                                            <select name="idpAuthType_<%=step.getStepOrder()%>"
                                                    style="float: left; min-width: 150px;font-size:13px;">
                                                <%=enabledIdpType.toString()%>
                                            </select>
                                            <a id="claimMappingAddLinkss"
                                               onclick="addIDPRow(this,'<%=step.getStepOrder()%>');return false;"
                                               class="icon-link claimMappingAddLinkssIdp"
                                               style="background-image:url(images/add.gif);">Add Authenticator</a>
                                        </td>
                                    </tr>
                                    </thead>
                                    <%

                                        IdentityProvider[] fedIdps = step.getFederatedIdentityProviders();
                                        if (fedIdps != null && fedIdps.length > 0) {
                                            int j = 0;
                                            for (IdentityProvider idp : fedIdps) {
                                                if (idp != null) {
                                    %>

                                    <tr>
                                        <td>
                                            <input name="step_<%=step.getStepOrder()%>_fed_auth" id="" type="hidden"
                                                   value="<%=Encode.forHtmlAttribute(idp.getIdentityProviderName())%>"/>
                                            <%=Encode.forHtmlContent(idp.getIdentityProviderName()) %>
                                        </td>
                                        <td>
                                            <select
                                                    name="step_<%=step.getStepOrder()%>_idp_<%=Encode.forHtmlAttribute(idp.getIdentityProviderName())%>_fed_authenticator"
                                                    style="float: left; min-width: 150px;font-size:13px;"><%=stepIdpAuthenticators.get(step.getStepOrder() + "_" + idp.getIdentityProviderName())%>
                                            </select>
                                        </td>
                                        <td class="leftCol-small">
                                            <a onclick="deleteIDPRow(this);return false;" href="#" class="icon-link"
                                               style="background-image: url(images/delete.gif)"> Delete </a>
                                        </td>
                                    </tr>
                                    <%
                                                }
                                            }
                                        }
                                    %>
                                </table>
                            </div>
                            <% } %>

                        </div>

                        <% }
                        } %>
                    </div>
                    <div class="script-select-container" style="display: none;">
                        <label class="noselect">
                            <input id="enableScript" name="enableScript" type="checkbox" value="true" <%
                                if (appBean.getServiceProvider().getLocalAndOutBoundAuthenticationConfig() != null) {
                                    if (appBean.getServiceProvider().getLocalAndOutBoundAuthenticationConfig().getAuthenticationScriptConfig() != null) {
                                        if (appBean.getServiceProvider().getLocalAndOutBoundAuthenticationConfig()
                                                .getAuthenticationScriptConfig().getEnabled()) { %>
                                   checked="checked"  <% }
                            }
                            }%>/> Enable Script Based Adaptive Authentication
                        </label>
                    </div>
                </div>
                <div style="clear:both"></div>
                <!-- sectionSub Div -->
                <br/>
                <%
                    if(FrameworkUtils.isAdaptiveAuthenticationAvailable()) {
                %>
                <h2 id="authentication_step_config_head" class="sectionSeperator trigger active">
                    <a href="#">Script Based Adaptive Authentication</a>
                </h2>
                <% } %>
                <div class="toggle_container sectionSub" id="editorRow">
                    <div class="err_warn_container">
                        <div class="disable_status">
                            <img src="images/disabled.png"><span class="disable_text">Disabled</span>
                            <span class="show_errors_toggle_buttons">
                                <a href="#">[+] Show Errors</a>
                                <a href="#" style="display: none;">[-] Hide Errors</a>
                            </span>
                        </div>
                        <div class="err_warn_content">
                            <div class="err_container">
                                <img src="images/error.gif" class="editor_err_img"/> <span class="err_head">Errors</span>
                                <ul class="err_list"></ul>
                            </div>
                            <div class="warn_container">
                                <img src="images/warning.gif" class="editor_warn_img"/><span class="err_head">Warnings</span>
                                <ul class="warn_list"></ul>
                            </div>
                        </div>
                        <div class="instruction">Correct errors and update to enable the script.</div>
                    </div>
                    <div class="warning_container">
                        <span class="show_errors_toggle_buttons">
                            <a href="#">[+] Show Warnings</a>
                            <a href="#" style="display: none;">[-] Hide Warnings</a>
                        </span>
                    </div>
                    <div class="warning_content">
                        <div class="warning_container">
                            <img src="images/warning.gif" class="editor_warn_img"/><span class="err_head">Warnings</span>
                            <ul class="warning_list"></ul>
                        </div>
                    </div>
                    <div style="position: relative;">
                        <div class="sectionSub step_contents" id="codeMirror">
<textarea id="scriptTextArea" name="scriptTextArea"
          placeholder="Write custom JavaScript or select from templates that match a scenario..."
          style="height: 500px;width: 100%; display: none;"><%
    if (appBean.getServiceProvider().getLocalAndOutBoundAuthenticationConfig() != null) {
        if (appBean.getServiceProvider().getLocalAndOutBoundAuthenticationConfig().getAuthenticationScriptConfig() != null) {
            out.print(appBean.getServiceProvider().getLocalAndOutBoundAuthenticationConfig().getAuthenticationScriptConfig().getContent());
        }
    }
%></textarea>
                        </div>
                        <div id="codeMirrorTemplate" class="step_contents">
                            <div class="add-template-container vertical-text">
                                <a id="addTemplate" class="icon-link noselect">Templates</a>
                            </div>
                            <div class="template-list-container">
                                <ul id="template_list"></ul>
                            </div>
                        </div>
                    </div>
                </div>
                <div style="clear:both"></div>
                <div class="buttonRow" style=" margin-top: 10px;">
                    <input id="createApp" type="button" value="<fmt:message key='button.update.service.provider'/>"/>
                    <input style="display: none"type="button"
                                               value="<fmt:message key='button.save.as.default.seq'/>"
                                               onclick="saveAsDefaultAuthSeq();"/>
                                        <input type="hidden" name="seqDesc" id="seqDesc"/>
                    <input type="button" value="<fmt:message key='button.cancel'/>"
                           onclick="javascript:location.href='configure-service-provider.jsp?display=auth_config&spName=<%=Encode.forUriComponent(spName)%>'"/>
                </div>
            </form>
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
        <div class="editor-warning-content">
            <div class="messagebox-warning-custom">
                <ul class="warningListContainer"></ul>
                <ul class="stepWarningListContainer"></ul>
            </div>
        </div>
    </div>
    <div id="add_default_AuthSeq" class="editor-error-warn-container" style="display: none">
            <br/>
            <br/>
            <div class="sectionSub">
                <table class="carbonFormTable">
                    <tr>
                        <td style="width:15%" class="leftCol-med labelField"><fmt:message key='config.default.seq.desc'/>:
                        </td>
                        <td>
                            <textarea style="width:50%" type="text" class="sequence-desc" name="sequence-desc"
                                      id="sequence-desc" class="text-box-big"></textarea>
                            <div class="sectionHelp"><fmt:message key='help.default.seq.desc'/></div>
                        </td>
                    </tr>
                </table>
            </div>
        </div>
</fmt:bundle>

<script id="template-info" type="text/x-handlebars-template">
    <div id='messagebox-template-summary' class="messagebox-info-custom">
        <h2>{{title}}</h2>
        <br/>
        {{#if summary}}
        <p>{{summary}}</p>
        <br/>
        {{/if}}
        {{#if preRequisites}}
        <h3>Prerequisites</h3>
        <ul>
            {{#each preRequisites}}
            <li>{{this}}</li>
            {{/each}}
        </ul>
        <br/>
        {{/if}}
        {{#if parametersDescription}}
        <h3>Parameters</h3>
        <table>
            <tbody>
            {{#each parametersDescription}}
            <tr>
                <td><i>{{@key}}</i></td>
                <td>{{this}}</td>
            </tr>
            {{/each}}
            </tbody>
        </table>
        <br/>
        {{/if}}
        {{#if defaultStepsDescription}}
        <h3>Default Steps</h3>
        <ul>
            {{#each defaultStepsDescription}}
            <li>{{@key}} : {{this}}</li>
            {{/each}}
        </ul>
        <br/>
        {{/if}}
        {{#if helpLink}}
        <h3>Help/Reference</h3>
        <a href="{{helpLink}}" target="_blank">{{helpLink}}</a>
        <br/>
        {{/if}}
        {{#if code}}
        <br/>
        <h3>Code</h3>
        <br/>
        <textarea  id="codesnippet_readonly" name="codesnippet_readonly"></textarea>
        <br/>
        {{/if}}
    </div>
</script>
<script>
    var authMap = {};
    var conditionalAuthFunctions = $.parseJSON('<%=availableJsFunctionsJson%>');
    var localAuthenticators = [];
    var localHandlers = [];

    <%
    if (localAuthenticatorConfigs != null && localAuthenticatorConfigs.length > 0) {
        for (LocalAuthenticatorConfig auth : localAuthenticatorConfigs) {
        %>
            localAuthenticators.push('<%=auth.getName()%>');
        <%
            Property[] props = auth.getProperties();
             if (props != null && props.length > 0) {
                for (Property pro : props) {
                    if ((IS_HANDLER.equals(pro.getName()) && Boolean.valueOf(pro.getValue()))) {
                        %>
                            localHandlers.push('<%=auth.getName()%>');
                        <%
                    }
                }
            }
        }
    }
    %>
    var stepOrder = 0;
    <%if(steps != null){%>
    var stepOrder = <%=steps.length%>;
    <%} else {%>
    var stepOrder = 0;
    var img = "";
    <%}%>
    var templates = $.parseJSON('<%=templatesJson%>');

    function addNewUIStep(){
        stepOrder++;
        jQuery('#stepsConfRow .steps').append(jQuery('<h2 id="step_head_' + stepOrder +
            '" class="sectionSeperator trigger active step_heads" style="background-color: beige; clear: both;"><input type="hidden" value="' + stepOrder + '" name="auth_step" id="auth_step"><a class="step_order_header" href="#">Step ' + stepOrder + '</a><a href="#" class="delete_step icon-link" data-step-no="' + stepOrder + '" style="background-image: url(images/delete.gif);float:right;width: 9px;"></a></h2><div class="toggle_container sectionSub step_contents step_body" style="margin-bottom:10px;" id="step_dev_' + stepOrder + '"> <div style="padding-bottom: 5px"><table class="carbonFormTable"><tr><td><input type="checkbox" style="vertical-align: middle;" id="subject_step_' + stepOrder + '" name="subject_step_' + stepOrder + '" class="subject_steps" onclick="setSubjectStep(this)"><label for="subject_step_' + stepOrder + '" style="cursor: pointer;">Use subject identifier from this step</label></td></tr><tr><td><input type="checkbox" style="vertical-align: middle;" id="attribute_step_' + stepOrder + '" name="attribute_step_' + stepOrder + '" class="attribute_steps" onclick="setAttributeStep(this)" ><label for="attribute_step_' + stepOrder + '" style="cursor: pointer;">Use attributes from this step</label></td></tr></table></div><h2 id="local_auth_head_' + stepOrder + '" class="sectionSeperator trigger active" style="background-color: floralwhite;"><a href="#">Local Authenticators</a></h2><div class="toggle_container sectionSub" style="margin-bottom:10px;" id="local_auth_head_dev_' + stepOrder + '"><table class="styledLeft auth_table" width="100%" id="local_auth_table_' + stepOrder + '"><thead><tr><td><select name="step_' + stepOrder + '_local_oauth_select" style="float: left; min-width: 150px;font-size:13px;"><%=localAuthTypes.toString()%></select><a id="localOptionAddLinkStep_' + stepOrder + '" onclick="addLocalRow(this,' + stepOrder + ');return false;" class="icon-link claimMappingAddLinkss claimMappingAddLinkssLocal" style="background-image:url(images/add.gif);">Add Authenticator</a></td></tr></thead></table> </div><%if (enabledIdpType.length() > 0) { %> <h2 id="fed_auth_head_' + stepOrder + '" class="sectionSeperator trigger active" style="background-color: floralwhite;"><a href="#">Federated Authenticators</a></h2><div class="toggle_container sectionSub" style="margin-bottom:10px;" id="fed_auth_head_dev_' + stepOrder + '"><table class="styledLeft auth_table" width="100%" id="fed_auth_table_' + stepOrder + '"><thead> <tr><td><select name="idpAuthType_' + stepOrder + '" style="float: left; min-width: 150px;font-size:13px;"><%=enabledIdpType.toString()%></select><a id="claimMappingAddLinkss" onclick="addIDPRow(this,' + stepOrder + ');return false;" class="icon-link claimMappingAddLinkssIdp" style="background-image:url(images/add.gif);">Add Authenticator</a></td></tr></thead></table></div><%}%></div>'));
        if (!$('#stepsConfRow').is(":visible")) {
            $(jQuery('#stepsConfRow')).toggle();
        }
        if (stepOrder == 1) {
            $('#subject_step_' + stepOrder).attr('checked', true);
            $('#attribute_step_' + stepOrder).attr('checked', true);
        }
    }
</script>
<script src="./js/configure-authentication-flow.js"></script>
