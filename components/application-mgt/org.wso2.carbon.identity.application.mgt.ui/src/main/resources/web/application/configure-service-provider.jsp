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

<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.apache.commons.collections.CollectionUtils" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.consent.mgt.core.model.Purpose" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.CertData" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.xsd.IdentityProvider" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationRequestConfig" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.xsd.LocalAuthenticatorConfig" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.xsd.Property" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.xsd.ServiceProviderProperty" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.xsd.ProvisioningConnectorConfig" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.xsd.RequestPathAuthenticatorConfig" %>
<%@ page import="org.wso2.carbon.identity.application.common.util.IdentityApplicationManagementUtil" %>
<%@ page import="org.wso2.carbon.identity.application.mgt.ui.ApplicationBean" %>
<%@ page import="org.wso2.carbon.identity.application.mgt.ui.client.ApplicationManagementServiceClient" %>
<%@ page import="org.wso2.carbon.identity.application.mgt.ui.util.ApplicationMgtUIUtil" %>
<%@ page import="org.wso2.carbon.identity.application.mgt.ui.util.ApplicationMgtUIConstants" %>
<%@ page import="org.wso2.carbon.identity.application.mgt.ui.ApplicationPurpose" %>
<%@ page import="org.wso2.carbon.identity.application.mgt.ui.ApplicationPurposes" %>
<%@ page import="org.wso2.carbon.identity.core.util.IdentityUtil" %>
<%@ page import="org.wso2.carbon.security.sts.service.util.STSServiceValidationUtil" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.security.cert.CertificateException" %>
<%@ page import="java.text.MessageFormat" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.Comparator" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.xsd.SpTemplate" %>
<link href="css/idpmgt.css" rel="stylesheet" type="text/css" media="all"/>
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
<script type="text/javascript" src="../identity/encode/js/identity-encode.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<script type="text/javascript" src="../identity/validation/js/identity-validate.js"></script>
<jsp:include page="../dialog/display_messages.jsp" />

<fmt:bundle basename="org.wso2.carbon.identity.application.mgt.ui.i18n.Resources">
<carbon:breadcrumb label="breadcrumb.service.provider"
                   resourceBundle="org.wso2.carbon.identity.application.mgt.ui.i18n.Resources"
                   topPage="true" request="<%=request%>"/>
<%!public static final String IS_HANDLER = "IS_HANDLER";%>


<%
    String[] createTemplateError = (String[]) request.getSession().getAttribute("createTemplateError");
    if (createTemplateError == null) {
        createTemplateError = new String[0];
    }

    String BUNDLE = "org.wso2.carbon.identity.application.mgt.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

    ApplicationBean appBean = ApplicationMgtUIUtil.getApplicationBeanFromSession(session, request.getParameter("spName"));
    if (appBean.getServiceProvider() == null || appBean.getServiceProvider().getApplicationName() == null) {
// if appbean is not set properly redirect the user to list-service-provider.jsp.
%>
<script>
    location.href = "list-service-providers.jsp";
</script>
<%
        return;
    }
    String spName = appBean.getServiceProvider().getApplicationName();
    String accessURL = appBean.getServiceProvider().getAccessUrl();
    String imageURL = appBean.getServiceProvider().getImageUrl();

    List<String> permissions = null;
    permissions = appBean.getPermissions();

    String[] allClaimUris = appBean.getClaimUris();
    Map<String, String> claimMapping = appBean.getClaimMapping();
    Map<String, String> roleMapping = appBean.getRoleMapping();
    boolean isLocalClaimsSelected = appBean.isLocalClaimsSelected();
    List<String> spClaimDialects = appBean.getSPClaimDialects();
    List<String> claimDialectUris = appBean.getClaimDialectUris();
    String isHashDisabled = request.getParameter("isHashDisabled");
    String idPName = request.getParameter("idPName");
    String action = request.getParameter("action");
    String operation = request.getParameter("operation");
    String[] userStoreDomains = null;
    StringBuilder spTemplateNames = new StringBuilder();
    boolean isNeedToUpdate = false;
    boolean isAdvanceConsentManagementEnabled = false;

    //adding code to support jwks URI
    String jwksUri = appBean.getServiceProvider().getJwksUri();
    boolean hasJWKSUri = StringUtils.isNotEmpty(jwksUri);

    String authTypeReq = request.getParameter("authType");
    if (authTypeReq != null && authTypeReq.trim().length() > 0) {
        appBean.setAuthenticationType(authTypeReq);
    }

    String samlIssuerName = request.getParameter("samlIssuer");

    // Will be supported with 'Advance Consent Management Feature'.
    /*
    if ("updateSPPurposes".equals(action)) {
        appBean.setApplicationPurposes(ApplicationMgtUIUtil.getApplicationSpecificPurposes(appBean.getServiceProvider()));
    }
    if ("updateSharedPurposes".equals(action)) {
        appBean.setSharedPurposes(ApplicationMgtUIUtil.getSharedPurposes());
    }
    ApplicationPurposes applicationPurposes = appBean.getApplicationPurposes();
    List<ApplicationPurpose> appPurposes = applicationPurposes.getAppPurposes();
    List<ApplicationPurpose> appSharedPurposes = applicationPurposes.getAppSharedPurposes();
    Purpose[] sharedPurposes = appBean.getSharedPurposes();
    boolean isConsentManagementEnabled = false;

    if (appBean.getServiceProvider().getConsentConfig() != null) {
        isConsentManagementEnabled = appBean.getServiceProvider().getConsentConfig().getEnabled();
    }
    */


    if (samlIssuerName != null && "update".equals(action)) {
        appBean.setSAMLIssuer(samlIssuerName);

        // Inbound authentication components might have set an application certificate in the session.
        // One usage in this scenario is, using the certificate inside SAML SP metadata.
        String applicationCertificate = (String) session.getAttribute("applicationCertificate");

        if (applicationCertificate != null) {
            appBean.getServiceProvider().setCertificateContent(applicationCertificate);
            session.removeAttribute("applicationCertificate");
        }

        isNeedToUpdate = true;
    }

    if (samlIssuerName != null && "delete".equals(action)) {
        appBean.deleteSAMLIssuer();
        isNeedToUpdate = true;
    }

    samlIssuerName = appBean.getSAMLIssuer();

    String kerberosServicePrinciple = request.getParameter("kerberos");

    if (kerberosServicePrinciple != null && "update".equals(action)) {
        appBean.setKerberosServiceName(kerberosServicePrinciple);
        isNeedToUpdate = true;
    }

    if (kerberosServicePrinciple != null && "delete".equals(action)) {
        appBean.deleteKerberosApp();
        isNeedToUpdate = true;
    }

    String attributeConsumingServiceIndex = request.getParameter("attrConServIndex");
    if (attributeConsumingServiceIndex != null) {
        appBean.setAttributeConsumingServiceIndex(attributeConsumingServiceIndex);
    }

    String oauthapp = request.getParameter("oauthapp");

    if (oauthapp != null && "update".equals(action)) {
        appBean.setOIDCAppName(oauthapp);
        isNeedToUpdate = true;
    }

    if (oauthapp != null && "delete".equals(action)) {
        appBean.deleteOauthApp();
        isNeedToUpdate = true;
    }

    String oauthConsumerSecret = null;

    if (session.getAttribute("oauth-consum-secret") != null && ("update".equals(action) || "regenerate".equals(action))) {
        oauthConsumerSecret = (String) session.getAttribute("oauth-consum-secret");
        appBean.setOauthConsumerSecret(oauthConsumerSecret);
        session.removeAttribute("oauth-consum-secret");
    }

    oauthapp = appBean.getOIDCClientId();

    String wsTrustEndpoint = request.getParameter("serviceName");
    String wsTrustObsoleteEndpoint = request.getParameter("obsoleteServiceName");

    if (wsTrustEndpoint != null && !wsTrustEndpoint.isEmpty() && "update".equals(action)) {
        if (wsTrustObsoleteEndpoint != null && !wsTrustObsoleteEndpoint.isEmpty()) {
            appBean.removeWstrustEp(wsTrustObsoleteEndpoint);
        }
        appBean.addWstrustEp(wsTrustEndpoint);
        isNeedToUpdate = true;
    }

    if (wsTrustEndpoint != null && !wsTrustEndpoint.isEmpty() && "delete".equals(action)) {
        appBean.removeWstrustEp(wsTrustEndpoint);
        isNeedToUpdate = true;
    }

    List<String> wsTrust = appBean.getAllWsTrustSPs();

    String display = request.getParameter("display");

    if (idPName != null && idPName.equals("")) {
        idPName = null;
    }

    if ((ApplicationBean.AUTH_TYPE_FLOW.equals(authTypeReq) || "graph".equals(authTypeReq)) && "update".equals(action)) {
        isNeedToUpdate = true;
    }

    String authType = appBean.getAuthenticationType();

    StringBuffer localAuthTypes = new StringBuffer();
    String startOption = "<option value=\"";
    String middleOption = "\">";
    String endOPtion = "</option>";
    StringBuffer requestPathAuthTypes = new StringBuffer();
    RequestPathAuthenticatorConfig[] requestPathAuthenticators = appBean.getRequestPathAuthenticators();

    if (requestPathAuthenticators != null && requestPathAuthenticators.length > 0) {
        for (RequestPathAuthenticatorConfig reqAuth : requestPathAuthenticators) {
            requestPathAuthTypes.append(startOption + Encode.forHtmlAttribute(reqAuth.getName()) + middleOption + Encode.forHtmlContent(reqAuth.getDisplayName()) + endOPtion);
        }
    }

    Map<String, String> idpAuthenticators = new HashMap<String, String>();
    IdentityProvider[] federatedIdPs = appBean.getFederatedIdentityProviders();
    Map<String, String> proIdpConnector = new HashMap<String, String>();
    Map<String, String> enabledProIdpConnector = new HashMap<String, String>();
    Map<String, String> selectedProIdpConnectors = new HashMap<String, String>();
    Map<String, Boolean> idpStatus = new HashMap<String, Boolean>();
    Map<String, Boolean> IdpProConnectorsStatus = new HashMap<String, Boolean>();

    StringBuffer idpType = null;
    StringBuffer connType = null;
    StringBuffer enabledConnType = null;

    if (federatedIdPs != null && federatedIdPs.length > 0) {
        idpType = new StringBuffer();
        StringBuffer provisioningConnectors = null;
        for (IdentityProvider idp : federatedIdPs) {
            idpStatus.put(idp.getIdentityProviderName(), idp.getEnable());
            if (idp.getProvisioningConnectorConfigs() != null && idp.getProvisioningConnectorConfigs().length > 0) {
                ProvisioningConnectorConfig[] connectors = idp.getProvisioningConnectorConfigs();
                int i = 1;
                connType = new StringBuffer();
                enabledConnType = new StringBuffer();
                provisioningConnectors = new StringBuffer();
                for (ProvisioningConnectorConfig proConnector : connectors) {
                    if (i == connectors.length) {
                        provisioningConnectors.append(proConnector.getEnabled() ? proConnector.getName() : "");
                    } else {
                        provisioningConnectors.append(proConnector.getEnabled() ? proConnector.getName() + "," : "");
                    }
                    connType.append(startOption + Encode.forHtmlAttribute(proConnector.getName()) + middleOption + Encode.forHtmlContent(proConnector.getName()) + endOPtion);
                    if (proConnector.getEnabled()) {
                        enabledConnType.append(startOption + Encode.forHtmlAttribute(proConnector.getName()) + middleOption + Encode.forHtmlContent(proConnector.getName()) + endOPtion);
                    }
                    IdpProConnectorsStatus.put(idp.getIdentityProviderName() + "_" + proConnector.getName(), proConnector.getEnabled());
                    i++;
                }
                proIdpConnector.put(idp.getIdentityProviderName(), connType.toString());
                if (idp.getEnable()) {
                    enabledProIdpConnector.put(idp.getIdentityProviderName(), enabledConnType.toString());
                    idpType.append(startOption + Encode.forHtmlAttribute(idp.getIdentityProviderName()) + "\" data=\"" + Encode.forHtmlAttribute(provisioningConnectors.toString()) + "\" >" + Encode.forHtmlContent(idp.getIdentityProviderName()) + endOPtion);
                }
            }
        }

        if (appBean.getServiceProvider().getOutboundProvisioningConfig() != null
            && appBean.getServiceProvider().getOutboundProvisioningConfig().getProvisioningIdentityProviders() != null
            && appBean.getServiceProvider().getOutboundProvisioningConfig().getProvisioningIdentityProviders().length > 0) {

            IdentityProvider[] proIdps = appBean.getServiceProvider().getOutboundProvisioningConfig().getProvisioningIdentityProviders();
            for (IdentityProvider idp : proIdps) {
                ProvisioningConnectorConfig proIdp = idp.getDefaultProvisioningConnectorConfig();
                String options = proIdpConnector.get(idp.getIdentityProviderName());
                if (proIdp != null && options != null) {
                    String oldOption = startOption + Encode.forHtmlAttribute(proIdp.getName()) + middleOption + Encode.forHtmlContent(proIdp.getName()) + endOPtion;
                    String newOption = startOption + Encode.forHtmlAttribute(proIdp.getName()) + "\" selected=\"selected" + middleOption + Encode.forHtmlContent(proIdp.getName()) + endOPtion;
                    if (options.contains(oldOption)) {
                        options = options.replace(oldOption, newOption);
                    } else {
                        options = options + newOption;
                    }
                    selectedProIdpConnectors.put(idp.getIdentityProviderName(), options);
                } else {
                    options = enabledProIdpConnector.get(idp.getIdentityProviderName());
                    selectedProIdpConnectors.put(idp.getIdentityProviderName(), options);
                }

            }
        }

    }
    try {
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
            .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        ApplicationManagementServiceClient serviceClient = new ApplicationManagementServiceClient(cookie, backendServerURL, configContext);
        userStoreDomains = serviceClient.getUserStoreDomains();
        SpTemplate[] spTemplates = serviceClient.getAllApplicationTemplateInfo();
        if (spTemplates != null) {
            for (SpTemplate spTemplate : spTemplates) {
                spTemplateNames.append(spTemplate.getName()).append(",");
            }
        }
    } catch (Exception e) {
        CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
    }

    String certString = appBean.getServiceProvider().getCertificateContent();
    CertData certData = null;
    if (StringUtils.isNotBlank(certString)) {
        try {
            certData = IdentityApplicationManagementUtil.getCertData(IdentityUtil.getCertificateString(certString));
        } catch (CertificateException e) {
            //Invalid cert data, ignore showing cert information in the UI
        }
    }

%>

<script>


    <% if(claimMapping != null) {%>
    var claimMappinRowID = <%=claimMapping.size() -1 %>;
    <%} else {%>
    var claimMappinRowID = -1;
    <%}%>

    var reqPathAuth = 0;

    <%if(appBean.getServiceProvider().getRequestPathAuthenticatorConfigs() != null){%>
    var reqPathAuth = <%=appBean.getServiceProvider().getRequestPathAuthenticatorConfigs().length%>;
    <%} else {%>
    var reqPathAuth = 0;
    <%}%>

    <% if(roleMapping != null) {%>
    var roleMappinRowID = <%=roleMapping.size() -1 %>;
    <% } else { %>
    var roleMappinRowID = -1;
    <% } %>

    function saveAsTemplate() {
        showPopupConfirm($(".editor-error-warn-container").html(), "Save Service Provider Template", 250, 550, "Save", "Cancel",
            saveTemplate, null);
    }

    function validateTextForIllegal(fld) {
        var isValid = doValidateInput(fld, '<%=resourceBundle.getString("alert.error.sp.template.not.available")%>');
        if (isValid) {
            return true;
        }
        return false;
    }

    function validateSPConfigurations() {
        if (document.getElementById("isDiscoverableApp").checked) {
            var accessUrl = document.getElementById("accessURL").value;
            if (accessUrl == '') {
                CARBON.showWarningDialog('<%=resourceBundle.getString("alert.please.provide.access.url.value")%>');
                return false;
            }
        }
        if ($('input:radio[name=claim_dialect]:checked').val() == "custom") {
            var isValied = true;
            $.each($('.spClaimVal'), function () {
                if ($(this).val().length == 0) {
                    isValied = false;
                    CARBON.showWarningDialog('<%=resourceBundle.getString("alert.error.sp.template.claim.config")%>');
                    return false;
                }
            });
            if (!isValied) {
                return false;
            }
        }
        // number_of_claim_mappings
        var numberOfClaimMappings = document.getElementById("claimMappingAddTable").rows.length;
        document.getElementById('number_of_claim_mappings').value = numberOfClaimMappings;

        if ($('[name=app_permission]').length > 0) {
            var isValied = true;
            $.each($('[name=app_permission]'), function () {
                if ($(this).val().length == 0) {
                    isValied = false;
                    CARBON.showWarningDialog('<%=resourceBundle.getString("alert.error.sp.template.permission.config")%>');
                    return false;
                }
            });
            if (!isValied) {
                return false;
            }
        }
        if ($('.roleMapIdp').length > 0) {
            var isValied = true;
            $.each($('.roleMapIdp'), function () {
                if ($(this).val().length == 0) {
                    isValied = false;
                    CARBON.showWarningDialog('<%=resourceBundle.getString("alert.error.sp.template.role.config")%>');
                    return false;
                }
            });
            if (isValied) {
                if ($('.roleMapSp').length > 0) {
                    $.each($('.roleMapSp'), function () {
                        if ($(this).val().length == 0) {
                            isValied = false;
                            CARBON.showWarningDialog('<%=resourceBundle.getString("alert.error.sp.template.role.config")%>');
                            return false;
                        }
                    });
                }
            }
            if (!isValied) {
                return false;
            }
        }
        var numberOfPermissions = document.getElementById("permissionAddTable").rows.length;
        document.getElementById('number_of_permissions').value = numberOfPermissions;

        var numberOfRoleMappings = document.getElementById("roleMappingAddTable").rows.length;
        document.getElementById('number_of_rolemappings').value = numberOfRoleMappings;
        return true;
    }

    function saveTemplate() {
        var templateName = "";
        var templateDesc = "";
        var templateNames = "";
        $(".template-name").each(function() {
            if(this.value != "") {
                if (!validateTextForIllegal(this)) {
                    return false;
                }
                templateName  = $.trim(this.value);
            }
        });
        $(".template-description").each(function() {
            if(this.value != "") {
                templateDesc  = $.trim(this.value);
            }
        });
        if (templateName === null || 0 === templateName.length) {
            CARBON.showWarningDialog('<%=resourceBundle.getString("alert.error.sp.template.not.available")%>');
            return;
        }

        templateNames = document.getElementById('templateNames').value.split(",");
        for (var i = 0; i < templateNames.length; i++) {
            if (templateNames[i] == templateName) {
                CARBON.showWarningDialog('<%=resourceBundle.getString("alert.error.sp.template.name.taken")%>');
                return;
            }
        }

        document.getElementById('templateName').value = templateName;
        document.getElementById('templateDesc').value = templateDesc;

        validateSPConfigurations();
        $.ajax({
            type: "POST",
            url: 'add-service-provider-as-template.jsp',
            data: $("#configure-sp-form").serialize(),
            success: function (responseText, status) {
                if (status == "success") {
                    CARBON.showInfoDialog('<%=resourceBundle.getString("alert.success.add.sp.template")%>');
                    return;
                } else {
                    CARBON.showErrorDialog('<%=resourceBundle.getString("alert.error.sp.template.add")%>');
                    return;
                }
            },
            error: function(e) {
                CARBON.showErrorDialog('<%=resourceBundle.getString("alert.error.sp.template.add")%>');
                return;
            },
            async: false
        });
    }

    function createAppOnclick() {
        var spName = document.getElementById("spName").value;
        if (spName == '') {
            CARBON.showWarningDialog('<fmt:message key="alert.please.provide.service.provider.name"/>');
            location.href = '#';
        } else if (!validateTextForIllegal(document.getElementById("spName"))) {
            return false;
        } else {
            if (!validateSPConfigurations()) {
                return false;
            }
            if (jQuery('#deletePublicCert').val() == 'true') {
                var confirmationMessage = 'Are you sure you want to delete the public certificate of ' +
                    spName + '?';
                if (jQuery('#certFile').val() != '') {
                    confirmationMessage = confirmationMessage.replace("delete", "re-upload");
                }
                CARBON.showConfirmationDialog(confirmationMessage,
                    function () {
                        document.getElementById("configure-sp-form").submit();
                    },
                    function () {
                        return false;
                    });
            } else {
                document.getElementById("configure-sp-form").submit();
            }
        }
    }

    function updateBeanAndRedirect(redirectURL) {
        var numberOfClaimMappings = document.getElementById("claimMappingAddTable").rows.length;
        document.getElementById('number_of_claim_mappings').value = numberOfClaimMappings;

        var numberOfPermissions = document.getElementById("permissionAddTable").rows.length;
        document.getElementById('number_of_permissions').value = numberOfPermissions;

        var numberOfRoleMappings = document.getElementById("roleMappingAddTable").rows.length;
        document.getElementById('number_of_rolemappings').value = numberOfRoleMappings;

        $.ajax({
            type: "POST",
            url: 'update-application-bean.jsp?spName=<%=Encode.forUriComponent(spName)%>',
            data: $("#configure-sp-form").serialize(),
            success: function () {
                location.href = redirectURL;
            }
        });
    }

    function getConfigurationType(postURL) {

        var configType;

        if(postURL.includes("sso-saml")) {
            configType = "SAML2 Web SSO Configuration";
        } else if(postURL.includes("oauth")) {
            configType = "OAuth/OpenID Connect Configuration";
        } else if(postURL.includes("generic-sts")) {
            configType = "WS-Trust Security Token Service Configuration";
        } else {
            configType = "Kerberos KDC";
        }

        return configType;
    }

    function updateBeanAndPostWithConfirmation(postURL, data, redirectURLOnSuccess) {

        var serviceProvider = document.getElementById("spName").value;
        var configurationType = getConfigurationType(postURL);

        function doDelete() {
            updateBeanAndPost(postURL, data, redirectURLOnSuccess)
        }

        CARBON.showConfirmationDialog("Are you sure that you want to remove " +
         configurationType + " from the service provider " + serviceProvider + "?",
                               doDelete, null);
    }

    function updateBeanAndPost(postURL, data, redirectURLOnSuccess) {
        var numberOfClaimMappings = document.getElementById("claimMappingAddTable").rows.length;
        document.getElementById('number_of_claim_mappings').value = numberOfClaimMappings;

        var numberOfPermissions = document.getElementById("permissionAddTable").rows.length;
        document.getElementById('number_of_permissions').value = numberOfPermissions;

        var numberOfRoleMappings = document.getElementById("roleMappingAddTable").rows.length;
        document.getElementById('number_of_rolemappings').value = numberOfRoleMappings;

        $.ajax({
            type: "POST",
            url: 'update-application-bean.jsp?spName=<%=Encode.forUriComponent(spName)%>',
            data: $("#configure-sp-form").serialize(),
            success: function () {
                $.ajax({
                    type: 'POST',
                    url: postURL,
                    headers: {
                        Accept: "text/html"
                    },
                    data: data,
                    async: false,
                    success: function (responseText, status) {
                        if (status == "success") {
                            location.assign(redirectURLOnSuccess);
                        }
                    }
                });
            }
        });
    }

    function updateBeanAndPostToWithConfirmation(postURL, data) {

        var serviceProvider = document.getElementById("spName").value;
        var action = data.includes("revoke")? "Revoke Secret" : "Regenerate Secret";

        function doAction() {
              updateBeanAndPostTo(postURL, data);
        }

        CARBON.showConfirmationDialog("Are you sure that you want to " +
          action + " for OAuth client?",
          doAction, null);
    }

    function updateBeanAndPostTo(postURL, data) {
        $.ajax({
            type: "POST",
            url: 'update-application-bean.jsp?spName=<%=Encode.forUriComponent(spName)%>',
            data: $("#configure-sp-form").serialize(),
            success: function () {

                $.ajax({
                    type: 'POST',
                    url: postURL,
                    data: data,
                    success: function (data, textStatus, request) {
                        window.location = request.getResponseHeader('redirectUrl');
                    }

                });
            }
        });
    }

    function onSamlSsoClick() {
        var spName = document.getElementById("oldSPName").value;
        if (spName != '') {
            updateBeanAndRedirect("../sso-saml/add_service_provider.jsp?spName=" + spName);
        } else {
            CARBON.showWarningDialog('<fmt:message key="alert.please.provide.service.provider.name"/>');
            document.getElementById("saml_link").href = "#"
        }
    }

    function onKerberosClick() {
        var spName = document.getElementById("oldSPName").value;
        if (spName != '') {
            updateBeanAndRedirect("../servicestore/add-step1.jsp?spName=" + spName);
        } else {
            CARBON.showWarningDialog('<fmt:message key="alert.please.provide.service.provider.name"/>');
            document.getElementById("kerberos_link").href = "#"
        }
    }

    function onOauthClick() {
        var spName = document.getElementById("oldSPName").value;
        if (spName != '') {
            updateBeanAndRedirect("../oauth/add.jsp?spName=" + spName);
        } else {
            CARBON.showWarningDialog('<fmt:message key="alert.please.provide.service.provider.name"/>');
            document.getElementById("oauth_link").href = "#"
        }
    }

    function onSTSClick() {
        var spName = document.getElementById("oldSPName").value;
        if (spName != '') {
            updateBeanAndRedirect("../generic-sts/sts.jsp?spName=" + spName);
        } else {
            CARBON.showWarningDialog('<fmt:message key="alert.please.provide.service.provider.name"/>');
            document.getElementById("sts_link").href = "#"
        }
    }

    function deleteReqPathRow(obj) {
        reqPathAuth--;
        jQuery(obj).parent().parent().remove();
        if ($(jQuery('#permissionAddTable tr')).length == 1) {
            $(jQuery('#permissionAddTable')).toggle();
        }
    }

    function onAdvanceAuthClick() {
        location.href = 'configure-authentication-flow.jsp?spName=<%=Encode.forUriComponent(spName)%>';
    }

    // Will be supported with 'Advance Consent Management Feature'.
    <%--
    <%if (isAdvanceConsentManagementEnabled) {%>
    function onAppPurposesManageClick() {
        var spName = document.getElementById("oldSPName").value;
        if (spName != '') {
            updateBeanAndRedirect("/carbon/consent/list-purposes.jsp?purposeGroup=" + spName + "&purposeGroupType=SP&callback=" + encodeURIComponent("/carbon/application/configure-service-provider.jsp?spName=" + spName + "&display=consent&action=updateSPPurposes"));
        } else {
            CARBON.showWarningDialog('<fmt:message key="alert.please.provide.service.provider.name"/>');
        }
    }

    function onGenralPurposesManageClick() {
        var spName = document.getElementById("oldSPName").value;
        if (spName != '') {
            updateBeanAndRedirect("/carbon/consent/list-purposes.jsp?purposeGroup=SHARED&purposeGroupType=SYSTEM&callback=" + encodeURIComponent("/carbon/application/configure-service-provider.jsp?spName=" + spName + "&display=consent&action=updateSharedPurposes"));
        } else {
            CARBON.showWarningDialog('<fmt:message key="alert.please.provide.service.provider.name"/>');
        }
    }

    function onSharedPurposesClick() {

        var sharedPurposesSelect = $("#shared_purposes").val();
        if (sharedPurposesSelect === null || sharedPurposesSelect.trim().length == 0) {
            CARBON.showWarningDialog('<fmt:message key="provide.valid.purpose"/>');
            return false;
        } else if (sharedPurposesSelect === "not_selected") {
            CARBON.showWarningDialog("<fmt:message key="select.a.consent.purpose"/>");
            return false;
        }

        var sharedPurposeIdSplit = sharedPurposesSelect.split("row_shared_purpose_id_", 2);
        if (sharedPurposeIdSplit.length !== 2) {
            CARBON.showWarningDialog('<fmt:message key="invalid.purpose.selection"/>', null, null);
            return false;
        }

        var sharedPurposeId = sharedPurposeIdSplit[1];
        var isPurposeExist = false;
        $('#shared_purposes_tbl tr').each(function() {
            if ($(this).attr("id") === "row_shared_purpose_id_" + sharedPurposeId) {
                isPurposeExist = true;
            }
        })

        if (isPurposeExist) {
            CARBON.showWarningDialog('<fmt:message key="purpose.already.added"/>');
            return false;
        }
        $('#shared_purposes_tbl').show();
        <%for (Purpose sharedPurpose : sharedPurposes) {%>
            if (sharedPurposeId === "<%=sharedPurpose.getId()%>") {
                var sharedPurposeName = "<%=Encode.forJavaScriptBlock(sharedPurpose.getName())%>";
                var sharedPurposeDesc = "<%=Encode.forJavaScriptBlock(sharedPurpose.getDescription())%>";
            }
        <%}%>
        var row = '<tr id="row_shared_purpose_id_' + sharedPurposeId + '">' +
                '    <td>'+ sharedPurposeName +
                '    <input type="hidden" id="shared_purpose_id" name="shared_purpose_id" value="' + sharedPurposeId + '"</td>' +
                '    <td>'+ sharedPurposeDesc +'</td>' +
                '    <td><input style="width:30px" name="display_order_shared_purpose_id_' + sharedPurposeId + '" type="number" min="0"' +
                '            value="0" autofocus="">' +
                '    </td>' +
                '    <td><a class="icon-link" style="background-image: url(../admin/images/delete.gif)"' +
                '        onclick="removeSharedPurposeRow(\'row_shared_purpose_id_' + sharedPurposeId + '\')">Delete</a>' +
                '    </td>' +
                '</tr>';
        $('#shared_purposes_tbl tbody').append(row);
        $('#shared_purposes').prop("selectedIndex", 0);
    }

    function removeSharedPurposeRow(rowId) {

        $('#shared_purposes_tbl tr#' + rowId).remove();
        var rows = $('#shared_purposes_tbl tr').length;
        if (rows < 2) {
            $('#shared_purposes_tbl').hide();
        }
    }
    --%>

    function onClickAddSpClaimDialectUri() {

        var spClaimDialect = $("#standard_dialect").val();
        if (spClaimDialect == null || spClaimDialect.trim().length == 0) {
            CARBON.showWarningDialog("<fmt:message key='config.application.claim.dialect.sp.not.valid'/>",
                null, null);
            return false;
        }
        spClaimDialect = spClaimDialect.trim();
        if (!$("#spClaimDialectsTblRow").length) {
            var row = '<tr id="spClaimDialectsTblRow">' +
                '    <td></td>' +
                '    <td>' +
                '        <table id="spClaimDialectsTable" style="width: 40%; margin-bottom: 3px;" class="styledInner">' +
                '            <tbody id="spClaimDialectsTableBody">' +
                '            </tbody>' +
                '        </table>' +
                '        <input type="hidden" id="spClaimDialects" name="spClaimDialects" value="">' +
                '        <input type="hidden" id="currentColumnId" value="0">' +
                '    </td>' +
                '</tr>';
            $('#spClaimDialectInputRow').after(row);
        }
        var spClaimDialects = $("#spClaimDialects").val();
        var currentColumnId = $("#currentColumnId").val();
        if (spClaimDialects == null || spClaimDialects.trim().length == 0) {
            $("#spClaimDialects").val(spClaimDialect);
            var row =
                '<tr id="spClaimDialectUri_' + parseInt(currentColumnId) + '">' +
                '</td><td style="padding-left: 30px !important; color: rgb(119, 119, 119);font-style: italic;">' +
                encodeForHTML(spClaimDialect) +
                '</td><td><a onclick="removeSpClaimDialect(\'' + encodeForHTML(encodeQuotesForJavascript(spClaimDialect)) +
                '\', \'spClaimDialectUri_' +
                parseInt(currentColumnId) + '\');return false;"' +
                ' href="#" class="icon-link" style="background-image: url(../admin/images/delete.gif)">Delete</a></td></tr>';
            $('#spClaimDialectsTable tbody').append(row);
        } else {
            var isExist = false;
            $.each(spClaimDialects.split(","), function (index, value) {
                if (value === spClaimDialect) {
                    isExist = true;
                    CARBON.showWarningDialog("<fmt:message key='config.application.claim.dialect.sp.already.exists'/>",
                        null, null);
                    return false;
                }
            });
            if (isExist) {
                return false;
            }
            $("#spClaimDialects").val(spClaimDialects + "," + spClaimDialect);
            var row =
                '<tr id="spClaimDialectUri_' + parseInt(currentColumnId) + '">' +
                '</td><td style="padding-left: 30px !important; color: rgb(119, 119, 119);font-style: italic;">' +
                encodeForHTML(spClaimDialect) +
                '</td><td><a onclick="removeSpClaimDialect(\'' +
                encodeForHTML(encodeQuotesForJavascript(spClaimDialect)) +
                '\', \'spClaimDialectUri_' +
                parseInt(currentColumnId) + '\');return false;"' +
                ' href="#" class="icon-link" style="background-image: url(../admin/images/delete.gif)">Delete</a></td></tr>';
            $('#spClaimDialectsTable tr:last').after(row);
        }
        $("#standard_dialect").val("");
        $("#currentColumnId").val(parseInt(currentColumnId) + 1);
    }

    function removeSpClaimDialect(spClaimDialect, columnId) {

        var spClaimDialects = $("#spClaimDialects").val();
        var newSpClaimDialects = "";
        if (spClaimDialects != null && spClaimDialects.trim().length > 0) {
            $.each(spClaimDialects.split(","), function (index, value) {
                if (value.trim() === spClaimDialect.trim()) {
                    return true;
                }
                if (newSpClaimDialects.length > 0) {
                    newSpClaimDialects = newSpClaimDialects + "," + value.trim();
                } else {
                    newSpClaimDialects = value.trim();
                }
            });
        }
        $('#' + columnId).remove();
        $("#spClaimDialects").val(newSpClaimDialects);
        if (newSpClaimDialects.length == 0) {
            $('#spClaimDialectsTblRow').remove();
        }
    }

    var openFile = function (event) {
        var input = event.target;

        var reader = new FileReader();
        reader.onload = function () {
            var data = reader.result;
            document.getElementById('sp-certificate').value = data;
        };
        reader.readAsText(input.files[0]);
    };

    var resetCertFile = function (event) {
        event.preventDefault();
        document.getElementById('sp-certificate').value = document.getElementById('sp-old-certificate').value;
    };

    function copyTextClick(value) {
        var copyText = value;
        copyText.select();
        document.execCommand("Copy");

        return false;
    }

    $(function () {
        $("#showDialog").dialog({
            autoOpen: false,
            modal: true,
            buttons: {
                OK: function () {
                    $(this).dialog("close");
                }
            },
            height: 300,
            width: 590,
            modal: true
        });
    });

    window.onload = function (e) {
        showManual();
        <% if(isHashDisabled != null && "false".equals(isHashDisabled) && appBean.getOIDCClientId() != null &&
           appBean.getOauthConsumerSecret() != null && ((operation != null && "add".equals(operation)) || "regenerate".equals(action))) { %>
        $("#showDialog").dialog("open");
        <% } %>
        <% if (createTemplateError.length > 0) { %>
        $( "#createTemplateErrorMsgDialog" ).dialog( "open" );
        <% } %>
    }

    jQuery(document).ready(function () {
        jQuery('#authenticationConfRow').hide();
        jQuery('#outboundProvisioning').hide();
        jQuery('#inboundProvisioning').hide();
        jQuery('#ReqPathAuth').hide();
        jQuery('#permissionConfRow').hide();
        jQuery('#claimsConfRow').hide();
        jQuery('h2.trigger').click(function () {
            if (jQuery(this).next().is(":visible")) {
                this.className = "active trigger";
            } else {
                this.className = "trigger";
            }
            jQuery(this).next().slideToggle("fast");
            return false; //Prevent the browser jump to the link anchor
        });
        jQuery('#permissionAddLink').click(function () {
            jQuery('#permissionAddTable').append(jQuery('<tr><td class="leftCol-big"><input style="width: 98%;" type="text" id="app_permission" name="app_permission"/></td>' +
                '<td><a onclick="deletePermissionRow(this)" class="icon-link" ' +
                'style="background-image: url(images/delete.gif)">' +
                'Delete' +
                '</a></td></tr>'));
        });
        jQuery('#claimMappingAddLink').click(function () {
            $('#claimMappingAddTable').show();
            var selectedIDPClaimName = $('select[name=idpClaimsList]').val();
            if (!validaForDuplications('.idpClaim', selectedIDPClaimName, 'Local Claim')) {
                return false;
            }
            claimMappinRowID++;
            var idpClaimListDiv = $('#localClaimsList').clone();
            if (idpClaimListDiv.length > 0) {
                $(idpClaimListDiv.find('select')).attr('id', 'idpClaim_' + claimMappinRowID);
                $(idpClaimListDiv.find('select')).attr('name', 'idpClaim_' + claimMappinRowID);
                $(idpClaimListDiv.find('select')).addClass("idpClaim");
            }
            if ($('input:radio[name=claim_dialect]:checked').val() == "local") {
                $('.spClaimHeaders').hide();
                $('#roleMappingSelection').hide();
                jQuery('#claimMappingAddTable').append(jQuery('<tr>' +
                    '<td style="display:none;"><input type="text" style="width: 98%;" id="spClaim_' + claimMappinRowID + '" name="spClaim_' + claimMappinRowID + '"/></td> ' +
                    '<td>' + idpClaimListDiv.html() + '</td>' +
                    '<td style="display:none;"><input type="checkbox"  name="spClaim_req_' + claimMappinRowID + '"  id="spClaim_req_' + claimMappinRowID + '" checked/></td>' +
                    '<td><input type="checkbox"  name="spClaim_mand_' + claimMappinRowID + '"  id="spClaim_mand_' + claimMappinRowID + '"/></td>' +
                    '<td><a onclick="deleteClaimRow(this);return false;" href="#" class="icon-link" style="background-image: url(images/delete.gif)"> Delete</a></td>' +
                    '</tr>'));
            }
            else {
                $('.spClaimHeaders').show();
                $('#roleMappingSelection').show();
                jQuery('#claimMappingAddTable').append(jQuery('<tr>' +
                    '<td><input type="text" class="spClaimVal" style="width: 98%;" id="spClaim_' + claimMappinRowID + '" name="spClaim_' + claimMappinRowID + '"/></td> ' +
                    '<td>' + idpClaimListDiv.html() + '</td>' +
                    '<td><input type="checkbox"  name="spClaim_req_' + claimMappinRowID + '"  id="spClaim_req_' + claimMappinRowID + '"/></td>' +
                    '<td><input type="checkbox"  name="spClaim_mand_' + claimMappinRowID + '"  id="spClaim_mand_' + claimMappinRowID + '"/></td>' +
                    '<td><a onclick="deleteClaimRow(this);return false;" href="#" class="icon-link" style="background-image: url(images/delete.gif)"> Delete</a></td>' +
                    '</tr>'));
                $('#spClaim_' + claimMappinRowID).change(function () {
                    resetRoleClaims();
                });
            }

        });
        jQuery('#roleMappingAddLink').click(function () {
            roleMappinRowID++;
            $('#roleMappingAddTable').show();
            jQuery('#roleMappingAddTable').append(jQuery('<tr><td><input style="width: 98%;" class="roleMapIdp" type="text" id="idpRole_' + roleMappinRowID + '" name="idpRole_' + roleMappinRowID + '"/></td>' +
                '<td><input style="width: 98%;" class="roleMapSp" type="text" id="spRole_' + roleMappinRowID + '" name="spRole_' + roleMappinRowID + '"/></td> ' +
                '<td><a onclick="deleteRoleMappingRow(this);return false;" href="#" class="icon-link" style="background-image: url(images/delete.gif)"> Delete</a>' +
                '</td></tr>'));
        })
        jQuery('#reqPathAuthenticatorAddLink').click(function () {
            reqPathAuth++;
            var selectedRePathAuthenticator = jQuery(this).parent().children()[0].value;
            if (!validaForDuplications('[name=req_path_auth]', selectedRePathAuthenticator, "Configuration")) {
                return false;
            }

            jQuery(this)
                .parent()
                .parent()
                .parent()
                .parent()
                .append(
                    jQuery('<tr><td><input name="req_path_auth' + '" id="req_path_auth" type="hidden" value="' + selectedRePathAuthenticator + '" />' + selectedRePathAuthenticator + '</td><td class="leftCol-small" ><a onclick="deleteReqPathRow(this);return false;" href="#" class="icon-link" style="background-image: url(images/delete.gif)"> Delete </a></td></tr>'));

        });

        $("[name=claim_dialect]").click(function () {
            var element = $(this);
            var currentId = element.attr('id');

            claimMappinRowID = -1;

            if ($('.idpClaim').length > 0) {
                CARBON.showConfirmationDialog('Changing dialect will delete all claim mappings. Do you want to proceed?',
                    function () {
                        $.each($('.idpClaim'), function () {
                            $(this).parent().parent().remove();
                        });
                        $('#claimMappingAddTable').hide();
                        changeDialectUIs(element);
                    },
                    function () {
                        //Reset checkboxes
                        if (currentId === "claim_dialect_custom") {
                            $('#claim_dialect_wso2').attr('checked', (element.val() == 'custom'));
                        } else {
                            $('#claim_dialect_custom').attr('checked', (element.val() == 'local'));
                        }
                    });
                jQuery('.ui-dialog-titlebar-close').click(function () {
                    //Reset checkboxes
                    if (currentId === "claim_dialect_custom") {
                        $('#claim_dialect_wso2').attr('checked', (element.val() == 'custom'));
                    } else {
                        $('#claim_dialect_custom').attr('checked', (element.val() == 'local'));
                    }
                })
            } else {
                $('#claimMappingAddTable').hide();
                changeDialectUIs(element);
            }
        });

        var authenticationType = $('input:radio[name=auth_type]:checked').val();
        var isAuthTypeClicked = false;
        var lastSelectedAuthType;
        var changeAuthTypeMsg = "Changing the Authentication Type from Advanced Configuration to another will remove the " +
            "advanced configurations when updating the Service Provider. Do you want to proceed?";

        $('#advanceAuthnConfRow input[name=auth_type]').mouseup(function(){
            lastSelectedAuthType = $('#advanceAuthnConfRow input[name=auth_type]:checked').val();
        }).change(function(){
            if(!isAuthTypeClicked && authenticationType == "flow" && $(this).val() !== "flow") {
                isAuthTypeClicked = true;
                CARBON.showConfirmationDialog(changeAuthTypeMsg,
                    function () {
                        return false;
                    },
                    function () {
                        $('#advanceAuthnConfRow input[name=auth_type][value=' + lastSelectedAuthType + ']').prop('checked', true);
                        isAuthTypeClicked = false;

                    });
            }
        });


        if ($('#isNeedToUpdate').val() == 'true') {
            $('#isNeedToUpdate').val('false');
            var numberOfClaimMappings = document.getElementById("claimMappingAddTable").rows.length;
            document.getElementById('number_of_claim_mappings').value = numberOfClaimMappings;

            var numberOfPermissions = document.getElementById("permissionAddTable").rows.length;
            document.getElementById('number_of_permissions').value = numberOfPermissions;

            var numberOfRoleMappings = document.getElementById("roleMappingAddTable").rows.length;
            document.getElementById('number_of_rolemappings').value = numberOfRoleMappings;

            $.ajax({
                type: "POST",
                url: 'configure-service-provider-update-ajaxprocessor.jsp?spName=<%=Encode.forUriComponent(spName)%>',
                data: $("#configure-sp-form").serialize()
            });
        }

        jQuery('#publicCertDeleteLink').click(function () {
            $(jQuery('#publicCertDiv')).toggle();
            var input = document.createElement('input');
            input.type = "hidden";
            input.name = "deletePublicCert";
            input.id = "deletePublicCert";
            input.value = "true";
            document.forms['configure-sp-form'].appendChild(input);
            document.getElementById('sp-certificate').value = "";
        });

    });

    function resetRoleClaims() {
        $("#roleClaim option").filter(function () {
            return $(this).val().length > 0;
        }).remove();
        $("#subject_claim_uri option").filter(function () {
            return $(this).val().length > 0;
        }).remove();
        $.each($('.spClaimVal'), function () {
            if ($(this).val().length > 0) {
                $("#roleClaim").append('<option value="' + encodeForHTML($(this).val()) + '">' + encodeForHTML($(this).val()) + '</option>');
                $('#subject_claim_uri').append('<option value="' + encodeForHTML($(this).val()) + '">' + encodeForHTML($(this).val()) + '</option>');
            }
        });
    }

    function changeDialectUIs(element) {
        $("#roleClaim option").filter(function () {
            return $(this).val().length > 0;
        }).remove();

        $("#subject_claim_uri option").filter(function () {
            return $(this).val().length > 0;
        }).remove();

        if (element.val() == 'local') {
            $('#addClaimUrisLbl').text('Requested Claims:');
            $('#roleMappingSelection').hide();
            if ($('#local_calim_uris').length > 0 && $('#local_calim_uris').val().length > 0) {
                var dataArray = $('#local_calim_uris').val().split(',');
                if (dataArray.length > 0) {
                    var optionsList = "";
                    $.each(dataArray, function () {
                        if (this.length > 0) {
                            optionsList += '<option value=' + this + '>' + this + '</option>'
                        }
                    });
                    if (optionsList.length > 0) {
                        $('#subject_claim_uri').append(optionsList);
                    }
                }
            }
        } else {
            $('#addClaimUrisLbl').text('Service Provider Claim URIs:');
            $('#roleMappingSelection').show();
        }
    }

    function deleteClaimRow(obj) {
        if ($('input:radio[name=claim_dialect]:checked').val() == "custom") {
            if ($(obj).parent().parent().find('input.spClaimVal').val().length > 0) {
                $('#roleClaim option[value="' + $(obj).parent().parent().find('input.spClaimVal').val() + '"]').remove();
                $('#subject_claim_uri option[value="' + $(obj).parent().parent().find('input.spClaimVal').val() + '"]').remove();
            }
        }

        jQuery(obj).parent().parent().remove();
        if ($('.idpClaim').length == 0) {
            $('#claimMappingAddTable').hide();
        }
    }

    function deleteRoleMappingRow(obj) {
        jQuery(obj).parent().parent().remove();
        if ($('.roleMapIdp').length == 0) {
            $('#roleMappingAddTable').hide();
        }
    }

    function deletePermissionRow(obj) {
        jQuery(obj).parent().parent().remove();
    }

    var deletePermissionRows = [];

    function deletePermissionRowOld(obj) {
        if (jQuery(obj).parent().prev().children()[0].value != '') {
            deletePermissionRows.push(jQuery(obj).parent().prev().children()[0].value);
        }
        jQuery(obj).parent().parent().remove();
        if ($(jQuery('#permissionAddTable tr')).length == 1) {
            $(jQuery('#permissionAddTable')).toggle();
        }
    }

    function addIDPRow(obj) {
        var selectedObj = jQuery(obj).prev().find(":selected");

        var selectedIDPName = selectedObj.val();
        if (!validaForDuplications('[name=provisioning_idp]', selectedIDPName, 'Configuration')) {
            return false;
        }

        //var stepID = jQuery(obj).parent().children()[1].value;
        var dataArray = selectedObj.attr('data').split(',');
        var newRow = '<tr><td><input name="provisioning_idp" id="" type="hidden" value="' + selectedIDPName + '" />' + selectedIDPName + ' </td><td> <select name="provisioning_con_idp_' + selectedIDPName + '" style="float: left; min-width: 150px;font-size:13px;">';
        for (var i = 0; i < dataArray.length; i++) {
            if (dataArray[i].length > 0) {
                newRow += '<option>' + dataArray[i] + '</option>';
            }
        }
        newRow += '</select></td><td><input type="checkbox" name="blocking_prov_' + selectedIDPName +
            '"  />Blocking</td><td><input type="checkbox" name="rules_enabled_' + selectedIDPName +
            '"  />Enable Rules</td><td><input type="checkbox" name="provisioning_jit_' + selectedIDPName +
            '"  />JIT Outbound</td><td class="leftCol-small" ><a onclick="deleteIDPRow(this);return false;" href="#" class="icon-link" style="background-image: url(images/delete.gif)"> Delete </a></td></tr>';
        jQuery(obj)
            .parent()
            .parent()
            .parent()
            .parent()
            .append(
                jQuery(newRow));
    }

    function deleteIDPRow(obj) {
        jQuery(obj).parent().parent().remove();
    }

    function validaForDuplications(selector, authenticatorName, type) {
        if ($(selector).length > 0) {
            var isNew = true;
            $.each($(selector), function () {
                if ($(this).val() == authenticatorName) {
                    CARBON.showWarningDialog(type + ' "' + authenticatorName + '" is already added');
                    isNew = false;
                    return false;
                }
            });
            if (!isNew) {
                return false;
            }
        }
        return true;
    }

    function showHidePassword(element, inputId) {
        if ($(element).text() == 'Show') {
            document.getElementById(inputId).type = 'text';
            $(element).text('Hide');
        } else {
            document.getElementById(inputId).type = 'password';
            $(element).text('Show');
        }
    }

    function disable() {
        document.getElementById("scim-inbound-userstore").disabled = !document.getElementById("scim-inbound-userstore").disabled;
        document.getElementById("dumb").value = document.getElementById("scim-inbound-userstore").disabled;
    }

    function validateTextForIllegal(fld) {
        var isValid = doValidateInput(fld, "Provided Service Provider name is invalid.");
        if (isValid) {
            return true;
        } else {
            return false;
        }
    }

    function selectJWKS(certDataNotNull) {
        var useJWKSUriStype = document.getElementById('use_jwks_uri').style;
        useJWKSUriStype.display = 'table-row';
        var uploadCertType = document.getElementById('upload_certificate').style;
        uploadCertType.display = 'none';
        // delete certificates if jwks_uri is selected.
        if (jQuery('#sp-certificate').val() != '') {
            jQuery('#sp-certificate').val('');
        } else if (certDataNotNull == 'true' && jQuery('#deletePublicCert').length) {
            jQuery('#deletePublicCert').val('true');
        } else if (certDataNotNull == 'true' && !jQuery('#deletePublicCert').length) {
            $(jQuery('#publicCertDiv')).toggle();
            var publicCertDiv = document.getElementById('publicCertDiv').style;
            publicCertDiv.display = 'none';
            jQuery( '#publicCertDiv').empty();
            var input = document.createElement('input');
            input.type = "hidden";
            input.name = "deletePublicCert";
            input.id = "deletePublicCert";
            input.value = "true";
            document.forms['configure-sp-form'].appendChild(input);
        }
    }

    function selectCertificate() {
         var useJWKSUriStype = document.getElementById('use_jwks_uri').style;
         useJWKSUriStype.display = 'none';
         var uploadCertType = document.getElementById('upload_certificate').style;
         uploadCertType.display = 'table-row';
         if (jQuery('#deletePublicCert').length) {
            jQuery('#deletePublicCert').val('false');
         }
         $('#jwksUri').val("");
    }

    function showManual() {
        $("#configure-sp-form").show();
    }

    $(function() {
        $( "#createTemplateErrorMsgDialog" ).dialog({
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

</script>

    <div id="middle">
        <h2>
            <fmt:message key='title.service.providers'/>
        </h2>
        <div id="workArea">
            <form id="configure-sp-form" method="post" name="configure-sp-form" method="post"
                  action="configure-service-provider-finish-ajaxprocessor.jsp">
                <input type="hidden" name="oldSPName" id="oldSPName" value="<%=Encode.forHtmlAttribute(spName)%>"/>
                <input type="hidden" id="isNeedToUpdate" value="<%=isNeedToUpdate%>"/>
                <div class="sectionSeperator togglebleTitle"><fmt:message key='title.config.app.basic.config'/></div>
                <div class="sectionSub">
                    <table class="carbonFormTable">
                        <tr>
                            <td style="width:15%" class="leftCol-med labelField"><fmt:message
                                key='config.application.info.basic.name'/>:<span class="required">*</span></td>
                            <td>
                                <input style="width:50%" id="spName" name="spName" type="text"
                                       value="<%=Encode.forHtmlAttribute(spName)%>"
                                       white-list-patterns="<%=Encode.forHtmlContent(ApplicationMgtUIUtil.getSPValidatorJavascriptRegex())%>" autofocus/>
                                <div class="sectionHelp">
                                    <fmt:message key='help.name'/>
                                </div>
                            </td>
                        </tr>
                        <tr>
                            <td style="width:15%" class="leftCol-med labelField">Description:</td>
                            <td>
                                <textarea maxlength="1023" style="width:50%" type="text" name="sp-description" id="sp-description"
                                          class="text-box-big"><%=appBean.getServiceProvider().getDescription() != null ? Encode.forHtmlContent(appBean.getServiceProvider().getDescription()) : "" %></textarea>
                                <div class="sectionHelp">
                                    <fmt:message key='help.desc'/>
                                </div>
                            </td>
                        </tr>

                        <!-- Add radio button to select certificate or jwks end point fro sp -->
                        <tr>
                            <td class="leftCol-med labelField"> Select SP Certificate Type </td>
                                <td>
                                    <label style="display:block">
                                    <input type="radio" id="choose_jwks_uri" name="choose_certificate_type"
                                     value="choose_jwks_uri" <% if (hasJWKSUri || (!hasJWKSUri && appBean.getServiceProvider().getCertificateContent() == null)) { %>
                                     checked="checked" <% } %> onclick="selectJWKS('<%=(appBean.getServiceProvider().getCertificateContent() != null)%>');" />
                                    Use SP JWKS endpoint
                                    </label>
                                    <label style="display:block">
                                    <input type="radio" id="choose_upload_certificate" name="choose_certificate_type"
                                     <% if (appBean.getServiceProvider().getCertificateContent() != null) { %> checked="checked" <% } %>
                                     value="choose_upload_certificate" onclick="selectCertificate()" />
                                    Upload SP certificate
                                    </label>
                                </td>
                        </tr>
                        <tr id ="upload_certificate" <% if (appBean.getServiceProvider().getCertificateContent() == null) { %> style="display:none" <% } %>>
                            <td style="width:15%" class="leftCol-med labelField">Application Certificate:</td>
                            <td>
                            <textarea style="width:100%;height: 100px;" type="text" name="sp-certificate"
                                      id="sp-certificate"
                                      class="text-box-big"><%=appBean.getServiceProvider().getCertificateContent() != null ? Encode.forHtmlContent(appBean.getServiceProvider().getCertificateContent()) : "" %></textarea>
                                <input type="hidden" name="sp-old-certificate"
                                       id="sp-old-certificate"
                                       value=<%=appBean.getServiceProvider().getCertificateContent() != null ? Encode.forHtmlContent(appBean.getServiceProvider().getCertificateContent()) : "" %>/>
                                <input id="certFile" name="certFile" type="file" onchange='openFile(event)'/>
                                <div class="sectionHelp">
                                    <fmt:message key='help.certificate'/>
                                </div>
                                <div id="publicCertDiv">
                                    <% if (certData != null) { %>
                                    <a id="publicCertDeleteLink" class="icon-link"
                                       style="margin-left:0;background-image:url(images/delete.gif);"><fmt:message
                                        key='public.cert.delete'/></a>
                                    <div style="clear:both"></div>
                                    <table class="styledLeft">
                                        <thead>
                                        <tr>
                                            <th><fmt:message key='issuerdn'/></th>
                                            <th><fmt:message key='subjectdn'/></th>
                                            <th><fmt:message key='notafter'/></th>
                                            <th><fmt:message key='notbefore'/></th>
                                            <th><fmt:message key='serialno'/></th>
                                            <th><fmt:message key='version'/></th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        <tr>
                                            <td><%
                                                String issuerDN = "";
                                                if (certData.getIssuerDN() != null) {
                                                    issuerDN = certData.getIssuerDN();
                                                }
                                            %><%=Encode.forHtmlContent(issuerDN)%>
                                            </td>
                                            <td><%
                                                String subjectDN = "";
                                                if (certData.getSubjectDN() != null) {
                                                    subjectDN = certData.getSubjectDN();
                                                }
                                            %><%=Encode.forHtmlContent(subjectDN)%>
                                            </td>
                                            <td><%
                                                String notAfter = "";
                                                if (certData.getNotAfter() != null) {
                                                    notAfter = certData.getNotAfter();
                                                }
                                            %><%=Encode.forHtmlContent(notAfter)%>
                                            </td>
                                            <td><%
                                                String notBefore = "";
                                                if (certData.getNotBefore() != null) {
                                                    notBefore = certData.getNotBefore();
                                                }
                                            %><%=Encode.forHtmlContent(notBefore)%>
                                            </td>
                                            <td><%
                                                String serialNo = "";
                                                if (certData.getSerialNumber() != null) {
                                                    serialNo = certData.getSerialNumber().toString();
                                                }
                                            %><%=Encode.forHtmlContent(serialNo)%>
                                            </td>
                                            <td><%=certData.getVersion()%>
                                            </td>
                                        </tr>
                                        </tbody>
                                    </table>
                                    <% } %>
                                </div>
                            </td>
                        </tr>
                        <!--JWKS TEXT BOX-->
                        <tr id="use_jwks_uri" <% if (appBean.getServiceProvider().getCertificateContent() != null) { %>
                            style="display:none" <% } %>>
                            <td style="width:15%" class="leftCol-med labelField">
                            <fmt:message key="config.application.JWKS"/>
                            </td>
                            <td style="width:50%" class="leftCol-med labelField">
                               <input style="width:50%" id="jwksUri" name="jwksUri" type="text" value="<%=jwksUri != null ? Encode.forHtmlAttribute(jwksUri) : "" %>"
                                autofocus required/>
                            </td>
                        </tr>
                        <tr>
                            <td class="leftCol-med">
                                <label for="isSaasApp"><fmt:message key="config.application.isSaasApp"/></label>
                            </td>
                            <td>
                                <div class="sectionCheckbox">
                                    <input type="checkbox" id="isSaasApp"
                                           name="isSaasApp" <%=appBean.getServiceProvider().getSaasApp() ? "checked" : "" %>/>
                                    <span style="display:inline-block" class="sectionHelp">
                                    <fmt:message key='help.saas'/>
                                </span>
                                </div>
                            </td>
                        </tr>
                        <tr>
                            <td class="leftCol-med">
                                <label for="isDiscoverableApp"><fmt:message key="config.application.isDiscoverableApp"/></label>
                            </td>
                            <td>
                                <div class="sectionCheckbox">
                                    <input type="checkbox" id="isDiscoverableApp"
                                           name="isDiscoverableApp" <%=appBean.getServiceProvider().getDiscoverable() ? "checked" :
                                            "" %>/>
                                    <span style="display:inline-block" class="sectionHelp">
                                    <fmt:message key='help.discoverable'/>
                                </span>
                                </div>
                            </td>
                        </tr>
                        <!--Access URL TEXT BOX-->
                        <tr id="use_access_url">
                            <td style="width:15%" class="leftCol-med labelField">
                                <fmt:message key="config.application.access.url"/>
                            </td>
                            <td style="width:50%" class="leftCol-med labelField">
                                <input style="width:50%" id="accessURL" name="accessURL" type="text"
                                       value="<%=accessURL != null ? Encode.forHtmlAttribute(accessURL) : "" %>" autofocus />
                                <div class="sectionHelp">
                                    <fmt:message key='help.access.url'/>
                                </div>
                            </td>
                        </tr>
                        <!--Image URL TEXT BOX-->
                        <tr id="use_image_url">
                            <td style="width:15%" class="leftCol-med labelField">
                                <fmt:message key="config.application.image.url"/>
                            </td>
                            <td style="width:50%" class="leftCol-med labelField">
                                <input style="width:50%" id="imageURL" name="imageURL" type="text"
                                       value="<%=imageURL != null ? Encode.forHtmlAttribute(imageURL) : "" %>"
                                       autofocus />
                                <div class="sectionHelp">
                                    <fmt:message key='help.image.url'/>
                                </div>
                            </td>
                        </tr>
                        <tr>
                            <td style="width:15%" class="leftCol-med labelField">
                                <fmt:message key='config.application.logout.return.url'/>
                            </td>
                            <td>
                                <% boolean logoutReturnUrlDefined = false;
                                    if (appBean.getServiceProvider().getSpProperties() != null) {
                                        for (ServiceProviderProperty property : appBean.getServiceProvider().getSpProperties()) {
                                            if (property.getName() != null && "logoutReturnUrl".equals(property.getName())) {
                                                logoutReturnUrlDefined = true; %>
                                <input style="width:50%" type="text" name="logoutReturnUrl" id="logoutReturnUrl"
                                       value="<%=property.getValue() != null ? Encode.forHtmlContent(property.getValue()) : "" %>"/>
                                <% }
                                }
                                }
                                    if (!logoutReturnUrlDefined) { %>
                                <input style="width:50%" type="text" name="logoutReturnUrl" id="logoutReturnUrl" value=".*"/>
                                <% } %>
                                <div class="sectionHelp">
                                    <fmt:message key='help.logout.return.url'/>
                                </div>
                            </td>
                        </tr>
                        <tr>
                            <td class="leftCol-med">
                                <label for="isManagementApp"><fmt:message key="config.application.isManagementApp"/></label>
                            </td>
                            <td>
                                <div class="sectionCheckbox">
                                    <input type="checkbox" id="isManagementApp"
                                           name="isManagementApp" <%=appBean.getServiceProvider().getManagementApp() ? "checked" : "" %> disabled/>
                                    <span style="display:inline-block" class="sectionHelp">
                                    <fmt:message key='help.management.app'/>
                                </span>
                                </div>
                            </td>
                        </tr>
                    </table>
                </div>

                <h2 id="claims_head" class="sectionSeperator trigger active">
                    <a href="#"><fmt:message key="title.config.app.claim"/></a>
                </h2>
                <div class="toggle_container sectionSub" style="margin-bottom:10px;" id="claimsConfRow">
                    <table style="padding-top: 5px; padding-bottom: 10px;" class="carbonFormTable">
                        <tr>
                            <td class="leftCol-med labelField">
                                <fmt:message key="config.application.claim.dialect.select"/>:
                            </td>
                            <td class="leftCol-med">
                                <input type="radio" id="claim_dialect_wso2" name="claim_dialect"
                                       value="local" <%=isLocalClaimsSelected ? "checked" : ""%>><label
                                for="claim_dialect_wso2" style="cursor: pointer;"><fmt:message
                                key="config.application.claim.dialect.local"/></label>
                            </td>
                        </tr>
                        <tr>
                            <td style="width:15%" class="leftCol-med labelField">
                            </td>
                            <td class="leftCol-med">
                                <input type="radio" id="claim_dialect_custom" name="claim_dialect"
                                       value="custom" <%=!isLocalClaimsSelected ? "checked" : ""%>><label
                                for="claim_dialect_custom" style="cursor: pointer;"><fmt:message
                                key="config.application.claim.dialect.custom"/></label>
                            </td>
                        </tr>
                    </table>
                    <table class="carbonFormTable">
                        <tr>
                            <td class="leftCol-med labelField" style="width:15%">
                                <label
                                    id="addClaimUrisLbl"><%=isLocalClaimsSelected ? "Requested Claims:" : "Identity Provider Claim URIs:"%>
                                </label>
                            </td>
                            <td class="leftCol-med">
                                <a id="claimMappingAddLink" class="icon-link"
                                   style="background-image: url(images/add.gif); margin-top: 0px !important; margin-bottom: 5px !important; margin-left: 5px;"><fmt:message
                                    key='button.add.claim.mapping'/></a>
                                <table class="styledLeft" id="claimMappingAddTable"
                                       style="<%= claimMapping == null || claimMapping.isEmpty() ? "display:none" : "" %>">
                                    <thead>
                                    <tr>
                                        <th class="leftCol-big spClaimHeaders"
                                            style="<%=isLocalClaimsSelected ? "display:none;" : ""%>"><fmt:message
                                            key='title.table.claim.sp.claim'/></th>
                                        <th class="leftCol-big"><fmt:message key='title.table.claim.idp.claim'/></th>
                                        <th class="leftCol-mid spClaimHeaders"
                                            style="<%=isLocalClaimsSelected ? "display:none;" : ""%>"><fmt:message
                                            key='config.application.req.claim'/></th>

                                        <th><fmt:message key='config.application.mand.claim'/></th>
                                        <th><fmt:message key='config.application.authz.permissions.action'/></th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <% if (claimMapping != null && !claimMapping.isEmpty()) { %>

                                    <%
                                        int i = -1;
                                        for (Map.Entry<String, String> entry : claimMapping.entrySet()) {
                                            i++;
                                    %>
                                    <tr>
                                        <td style="<%=isLocalClaimsSelected ? "display:none;" : ""%>"><input type="text"
                                                                                                             class="spClaimVal"
                                                                                                             style="width: 98%;"
                                                                                                             value="<%=Encode.forHtmlAttribute(entry.getValue())%>"
                                                                                                             id="spClaim_<%=i%>"
                                                                                                             name="spClaim_<%=i%>"
                                                                                                             readonly="readonly"/>
                                        </td>
                                        <td>
                                            <select id="idpClaim_<%=i%>" name="idpClaim_<%=i%>" class="idpClaim"
                                                    style="float:left; width: 100%">
                                                <% String[] localClaims = appBean.getClaimUris();
                                                    for (String localClaimName : localClaims) {
                                                        if (localClaimName.equals(entry.getKey())) {%>
                                                <option value="<%=Encode.forHtmlAttribute(localClaimName)%>"
                                                        selected><%=Encode.forHtmlContent(localClaimName)%>
                                                </option>
                                                <%} else {%>
                                                <option
                                                    value="<%=Encode.forHtmlAttribute(localClaimName)%>"><%=Encode.forHtmlContent(localClaimName)%>
                                                </option>
                                                <% }
                                                }%>
                                            </select>
                                        </td>
                                        <td style="<%=isLocalClaimsSelected ? "display:none;" : ""%>">
                                            <% if ("true".equals(appBean.getRequestedClaims().get(entry.getValue()))) {%>
                                            <input type="checkbox" id="spClaim_req_<%=i%>" name="spClaim_req_<%=i%>"
                                                   checked/>
                                            <%} else { %>
                                            <input type="checkbox" id="spClaim_req_<%=i%>" name="spClaim_req_<%=i%>"/>
                                            <%}%>
                                        </td>
                                        <td>
                                            <% if ("true".equals(appBean.getMandatoryClaims().get(entry.getValue()))) {%>
                                            <input type="checkbox" id="spClaim_mand_<%=i%>" name="spClaim_mand_<%=i%>"
                                                   checked/>
                                            <%} else { %>
                                            <input type="checkbox" id="spClaim_mand_<%=i%>" name="spClaim_mand_<%=i%>"/>
                                            <%}%>
                                        </td>

                                        <td>
                                            <a title="<fmt:message key='alert.info.delete.permission'/>"
                                               onclick="deleteClaimRow(this);return false;"
                                               href="#"
                                               class="icon-link"
                                               style="background-image: url(images/delete.gif)">
                                                <fmt:message key='link.delete'/>
                                            </a>
                                        </td>
                                    </tr>
                                    <% } %>
                                    <% } %>
                                    </tbody>
                                </table>
                            </td>
                        </tr>

                        <tr>
                            <td class="leftCol-med labelField"><fmt:message
                                key='config.application.info.subject.claim.uri'/>:
                            <td>
                                <select class="leftCol-med" id="subject_claim_uri" name="subject_claim_uri"
                                        style=" margin-left: 5px; ">
                                    <option value="">---Select---</option>
                                    <% if (isLocalClaimsSelected) {
                                        String[] localClaimUris = appBean.getClaimUris();
                                        for (String localClaimName : localClaimUris) {
                                            if (appBean.getSubjectClaimUri() != null && localClaimName.equals(appBean.getSubjectClaimUri())) {%>
                                    <option value="<%=Encode.forHtmlAttribute(localClaimName)%>"
                                            selected><%=Encode.forHtmlContent(localClaimName)%>
                                    </option>
                                    <%} else {%>
                                    <option
                                        value="<%=Encode.forHtmlAttribute(localClaimName)%>"><%=Encode.forHtmlContent(localClaimName)%>
                                    </option>
                                    <% }
                                    }
                                    } else {
                                        for (Map.Entry<String, String> entry : claimMapping.entrySet()) { %>
                                    <% if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                                        if (appBean.getSubjectClaimUri() != null && appBean.getSubjectClaimUri().equals(entry.getValue())) { %>
                                    <option value="<%=Encode.forHtmlAttribute(entry.getValue())%>"
                                            selected><%=Encode.forHtmlContent(entry.getValue())%>
                                    </option>
                                    <% } else { %>
                                    <option
                                        value="<%=Encode.forHtmlAttribute(entry.getValue())%>"><%=Encode.forHtmlContent(entry.getValue())%>
                                    </option>
                                    <%
                                                    }
                                                }
                                            }
                                        }
                                    %>
                                </select>
                            </td>
                        </tr>
                    </table>

                    <input type="hidden" name="number_of_claim_mappings" id="number_of_claim_mappings" value="1">
                    <div id="localClaimsList" style="display: none;">
                        <select style="float:left; width: 100%">
                            <% String[] localClaims = appBean.getClaimUris();
                                StringBuffer allLocalClaims = new StringBuffer();
                                for (String localClaimName : localClaims) { %>
                            <option
                                value="<%=Encode.forHtmlAttribute(localClaimName)%>"><%=Encode.forHtmlContent(localClaimName)%>
                            </option>
                            <%
                                    allLocalClaims.append(localClaimName + ",");
                                } %>
                        </select>
                    </div>
                    <input type="hidden" id="local_calim_uris"
                           value="<%=Encode.forHtmlAttribute(allLocalClaims.toString())%>">
                    <div id="roleMappingSelection" style="<%=isLocalClaimsSelected ? "display:none" : ""%>">
                        <table class="carbonFormTable" style="padding-top: 10px">
                            <tr>
                                <td class="leftCol-med labelField" style="width:15%">
                                    <label id="addClaimUrisLbl"><fmt:message
                                        key='config.application.role.claim.uri'/>:</label>
                                </td>
                                <td>
                                    <select id="roleClaim" name="roleClaim" style="float:left;min-width: 250px;">
                                        <option value="">---Select---</option>
                                        <% if (!isLocalClaimsSelected) {
                                            for (Map.Entry<String, String> entry : claimMapping.entrySet()) { %>
                                        <% if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                                            if (appBean.getRoleClaimUri() != null && appBean.getRoleClaimUri().equals(entry.getValue())) { %>
                                        <option value="<%=Encode.forHtmlAttribute(entry.getValue())%>"
                                                selected><%=Encode.forHtmlContent(entry.getValue())%>
                                        </option>
                                        <% } else { %>
                                        <option
                                            value="<%=Encode.forHtmlAttribute(entry.getValue())%>"><%=Encode.forHtmlContent(entry.getValue())%>
                                        </option>
                                        <% }
                                        }%>
                                        <%} %>
                                        <% } %>
                                    </select>
                                </td>
                            </tr>
                            <tr>
                                <td class="leftCol-med" style="width:15%"></td>
                                <td>
                                    <div class="sectionHelp">
                                        <fmt:message key='help.role.claim'/>
                                    </div>
                                </td>
                            </tr>
                        </table>
                    </div>
                    <div id="spClaimDialectSelection">
                        <table class="carbonFormTable">
                            <tr id="spClaimDialectInputRow">
                                <td class="leftCol-med labelField" style="width:15%">
                                    <label id="addSpClaimDialectUrisLbl"><fmt:message
                                        key='config.application.claim.dialect.sp'/>:</label>
                                </td>
                                <td>
                                    <select class="leftCol-med" id="standard_dialect" name="standard_dialect"
                                            style=" margin-left: 5px; ">
                                        <option value="">---Select---</option>
                                        <%
                                            for (String dialectURI : claimDialectUris) {%>
                                        <option
                                            value="<%=Encode.forHtmlAttribute(dialectURI)%>"><%=Encode.forHtmlContent(dialectURI)%>
                                        </option>
                                        <%
                                            } %>
                                    </select>
                                    <input id="addSpClaimDialectUriBtn" type="button"
                                           value="<fmt:message key="config.application.claim.dialect.sp.add"/>"
                                           onclick="onClickAddSpClaimDialectUri()"/>
                                </td>
                            </tr>

                            <%if (spClaimDialects != null) {%>
                            <tr id="spClaimDialectsTblRow">
                                <td></td>
                                <td>
                                    <table id="spClaimDialectsTable" style="width: 40%; margin-bottom: 3px;"
                                           class="styledInner">
                                        <tbody id="spClaimDialectsTableBody">
                                        <%
                                            StringBuilder spClaimDialectsBuilder = new StringBuilder();
                                            int spClaimDialectColumnId = 0;
                                            for (String spClaimDialect : spClaimDialects) {
                                                if (spClaimDialect != null) {
                                                    if (spClaimDialectsBuilder.length() > 0) {
                                                        spClaimDialectsBuilder.append(",").append(spClaimDialect);
                                                    } else {
                                                        spClaimDialectsBuilder.append(spClaimDialect);
                                                    }
                                        %>
                                        <tr id="spClaimDialectUri_<%=spClaimDialectColumnId%>">
                                            <td style="padding-left: 30px !important; color: rgb(119, 119, 119);font-style: italic;">
                                                <%=Encode.forHtml(spClaimDialect)%>
                                            </td>
                                            <td>
                                                <a onclick="removeSpClaimDialect('<%=Encode.forJavaScriptAttribute(spClaimDialect)%>',
                                                    'spClaimDialectUri_<%=spClaimDialectColumnId%>');return false;"
                                                   href="#" class="icon-link"
                                                   style="background-image: url(../admin/images/delete.gif)">
                                                    Delete
                                                </a>
                                            </td>
                                        </tr>
                                        <%
                                                    spClaimDialectColumnId++;
                                                }
                                            }
                                        %>
                                        </tbody>
                                    </table>
                                    <input type="hidden" id="spClaimDialects" name="spClaimDialects"
                                           value="<%=spClaimDialectsBuilder.length() > 0 ?
         Encode.forHtmlAttribute(spClaimDialectsBuilder.toString()) : ""%>">
                                    <input type="hidden" id="currentColumnId" value="<%=spClaimDialectColumnId%>">
                                </td>
                            </tr>
                            <%
                                }
                            %>

                        </table>
                    </div>
                </div>
                <%-- <h2 id="consent_head" class="sectionSeperator trigger active">
                    <a href="#"><fmt:message key="user.consent.configuration"/></a>
                </h2>
                <%if (display!=null && display.equals("consent")) {%>
                <div class="toggle_container sectionSub" style="margin-bottom: 10px;display:block;" id="consentRow">
                <%} else {%>
                <div class="toggle_container sectionSub" style="margin-bottom: 10px;display:none;" id="consentRow">
                <%}%>
                    <table style="padding-top: 5px; padding-bottom: 10px;" class="carbonFormTable">
                        <tr>
                            <td class="leftCol-med labelField"><fmt:message key="enable.consent.collection"/></td>
                            <td class="leftCol-med">
                                <div class="sectionCheckbox">
                                    <%if (isConsentManagementEnabled) {%>
                                    <input type="checkbox" id="is_consent_enabled" name="is_consent_enabled" checked/>
                                    <%} else {%>
                                    <input type="checkbox" id="is_consent_enabled" name="is_consent_enabled"/>
                                    <%}%>
                                    <div class="sectionHelp">
                                        <fmt:message key="enable.consent.collection.help"/>
                                    </div>
                                </div>
                            </td>
                        </tr>
                        <tr>
                            <td class="leftCol-med labelField"><fmt:message key="consent.collection.message"/></td>
                            <td class="leftCol-med">
                                <textarea style="width:500px; margin: 0px; height: 100px;" type="text" name="consent-description" id="consent-description"
                                            class="text-box-big"></textarea>
                                <div class="sectionHelp">
                                    <fmt:message key="consent.collection.message.help"/>
                                </div>
                            </td>
                        </tr>
                    </table>
                    <h2 id="advance_consent_head" class="sectionSeperator trigger active"
                        style="background-color: beige;">
                        <a href="#"><fmt:message key="advance.consent.config"/></a>
                    </h2>
                    <%if (display!=null && display.equals("consent")) {%>
                    <div class="toggle_container sectionSub" style="margin-bottom:10px;display:block;" id="advance_consent">
                    <%} else {%>
                    <div class="toggle_container sectionSub" style="margin-bottom: 10px;display:none;" id="advance_consent">
                    <%}%>
                        <table style="padding-top: 5px; padding-bottom: 10px;" class="carbonFormTable">
                            <tr>
                                <td class="leftCol-med labelField"><fmt:message key="application.specific.consent.purpose"/></td>
                                <td class="leftCol-med"></td>
                            </tr>
                        </table>
                        <table class="styledLeft" id="app_purposes_tbl">
                            <thead>
                                <tr>
                                    <th class="leftCol-big"><fmt:message key="purpose"/></th>
                                    <th class="leftCol-big"><fmt:message key="description"/></th>
                                    <th class="leftCol-mid"><fmt:message key="selected"/></th>
                                    <th class="leftCol-mid"><fmt:message key="display.order"/></th>
                                </tr>
                            </thead>
                            <%if (appPurpoappPurposes == null || appPurposes.isEmpty()) {%>
                            <script>
                                $(jQuery('#app_purposes_tbl')).hide();
                            </script>
                            <%}%>
                            <%for (ApplicationPurpose applicationPurpose : appPurposes) {%>
                            <tr>
                                <td>
                                    <%=Encode.forHtmlContent(applicationPurpose.getName())%>
                                    <input type="hidden" id="app_purpose_id" name="app_purpose_id" value="<%=applicationPurpose.getId()%>">
                                </td>
                                <td><%=Encode.forHtmlContent(applicationPurpose.getDescription())%></td>
                                <td>
                                    <%if (applicationPurpose.isSelected()) {%>
                                        <input type="checkbox" name="selected_purpose_id_<%=applicationPurpose.getId()%>" checked="checked" style="margin:0px;" />
                                    <%} else {%>
                                        <input type="checkbox" name="selected_purpose_id_<%=applicationPurpose.getId()%>" style="margin:0px;" />
                                    <%}%>
                                </td>
                                <td><input style="width:30px" name="display_order_purpose_id_<%=applicationPurpose.getId()%>"
                                        type="number" min="0" value="<%=applicationPurpose.getDisplayOrder()%>" autofocus=""></td>
                            </tr>
                            <%}%>
                        </table>
                        <table style="padding-top: 5px; padding-bottom: 10px;" class="carbonFormTable">
                            <tr>
                                <td class="leftCol-med labelField"><a id="saml_link" class="icon-link"
                                    onclick="onAppPurposesManageClick()"><fmt:message key="manage.app.consent.purposes"/></a></td>
                            </tr>
                        </table>
                        <table style="padding-top: 15px; padding-bottom: 10px;" class="carbonFormTable" id="shared_purposes_combo">
                                <tr>
                                    <td class="leftCol-med labelField"><fmt:message key="shared.consent.purposes"/></td>
                                </tr>
                                <tr>
                                    <td class="leftCol-med">
                                            <select class="leftCol-med" id="shared_purposes" name="shared_purposes"
                                            style=" margin-left: 5px; ">
                                                <option value="not_selected">---<fmt:message key="select"/>---</option>
                                                <%for (Purpose sharedPurpose : sharedPurposes) {%>
                                                <option
                                                    value="<%="row_shared_purpose_id_" + sharedPurpose.getId()%>"><%=Encode.forHtmlContent(sharedPurpose.getName())%>
                                                </option>
                                                <%}%>
                                            </select>
                                            <input id="addSharedConsentBtn" type="button" value="Add" onclick="onSharedPurposesClick()"/>
                                    </td>
                                </tr>
                        </table>
                        <table class="styledLeft" id="shared_purposes_tbl">
                            <thead>
                                <tr>
                                    <th class="leftCol-big"><fmt:message key="purpose"/></th>
                                    <th class="leftCol-big"><fmt:message key="description"/></th>
                                    <th class="leftCol-mid"><fmt:message key="display.order"/></th>
                                    <th class="leftCol-mid"><fmt:message key="remove"/></th>
                                </tr>
                            </thead>
                            <tbody>
                                <%if (appSharedPurposes == null || appSharedPurposes.isEmpty()) {%>
                                <script>
                                    $('#shared_purposes_tbl').hide();
                                </script>
                                <%}%>
                                <%for (ApplicationPurpose sharedPurpose : appSharedPurposes) {%>
                                <tr id="row_shared_purpose_id_<%=sharedPurpose.getId()%>">
                                    <td>
                                        <%=Encode.forHtmlContent(sharedPurpose.getName())%>
                                        <input type="hidden" id="shared_purpose_id" name="shared_purpose_id" value="<%=sharedPurpose.getId()%>">
                                    </td>
                                    <td><%=Encode.forHtmlContent(sharedPurpose.getDescription())%></td>
                                    <td><input style="width:30px" name="display_order_shared_purpose_id_<%=sharedPurpose.getId()%>"
                                            type="number" min="0" type="text" value="<%=sharedPurpose.getDisplayOrder()%>" autofocus=""></td>
                                    <td><a class="icon-link" style="background-image: url(../admin/images/delete.gif)"
                                        onclick="removeSharedPurposeRow('row_shared_purpose_id_<%=sharedPurpose.getId()%>')"><fmt:message key="link.delete"/></a></td>
                                </tr>
                                <%}%>
                            </tbody>
                        </table>
                        <table style="padding-top: 5px; padding-bottom: 10px;" class="carbonFormTable">
                            <tr>
                                <td class="leftCol-med labelField"><a id="manage_shared_purposes" class="icon-link"
                                    onclick="onGenralPurposesManageClick()"><fmt:message key="manage.shared.consent.purposes"/></a></td>
                            </tr>
                        </table>
                    </div>
                </div> --%>
                <h2 id="authorization_permission_head" class="sectionSeperator trigger active">
                    <a href="#"><fmt:message key="title.config.app.authorization.permission"/></a>
                </h2>
                <div class="toggle_container sectionSub" style="margin-bottom:10px;" id="permissionConfRow">
                    <h2 id="permission_mapping_head" class="sectionSeperator trigger active"
                        style="background-color: beige;">
                        <a href="#">Permissions</a>
                    </h2>
                    <div class="toggle_container sectionSub" style="margin-bottom:10px;display: none;"
                         id="appPermissionRow">
                        <table class="carbonFormTable">
                            <tr>
                                <td>
                                    <a id="permissionAddLink" class="icon-link"
                                       style="background-image:url(images/add.gif);margin-left:0;"><fmt:message
                                        key='button.add.permission'/></a>
                                    <div style="clear:both"></div>
                                    <div class="sectionHelp">
                                        <fmt:message key='help.permission.add'/>
                                    </div>
                                    <table class="styledLeft" id="permissionAddTable">
                                        <thead>
                                        </thead>
                                        <tbody>
                                        <% if (permissions != null && !permissions.isEmpty()) { %>

                                        <% for (int i = 0; i < permissions.size(); i++) {
                                            if (permissions.get(i) != null) {
                                        %>

                                        <tr>
                                            <td class="leftCol-big"><input style="width: 98%;" type="text"
                                                                           value="<%=Encode.forHtmlAttribute(permissions.get(i))%>"
                                                                           id="app_permission" name="app_permission"
                                                                           readonly="readonly"/></td>
                                            <td>
                                                <a title="<fmt:message key='alert.info.delete.permission'/>"
                                                   onclick="deletePermissionRow(this);return false;"
                                                   href="#"
                                                   class="icon-link"
                                                   style="background-image: url(images/delete.gif)">
                                                    <fmt:message key='link.delete'/>
                                                </a>
                                            </td>
                                        </tr>
                                        <% }
                                        } %>
                                        <% } %>
                                        </tbody>
                                    </table>
                                    <div style="clear:both"/>
                                    <input type="hidden" name="number_of_permissions" id="number_of_permissions"
                                           value="1">
                                </td>
                            </tr>

                        </table>
                    </div>
                    <h2 id="role_mapping_head" class="sectionSeperator trigger active" style="background-color: beige;">
                        <a href="#">Role Mapping</a>
                    </h2>
                    <div class="toggle_container sectionSub" style="margin-bottom:10px;display: none;"
                         id="roleMappingRowRow">
                        <table>
                            <tr>
                                <td>
                                    <a id="roleMappingAddLink" class="icon-link"
                                       style="background-image: url(images/add.gif);margin-left:0;"><fmt:message
                                        key='button.add.role.mapping'/></a>
                                    <div style="clear:both"/>
                                    <div class="sectionHelp">
                                        <fmt:message key='help.role.mapping'/>
                                    </div>
                                </td>
                            </tr>
                        </table>
                        <table class="styledLeft" id="roleMappingAddTable" style="display:none">
                            <thead>
                            <tr>
                                <th class="leftCol-big"><fmt:message key='title.table.role.idp.role'/></th>
                                <th class="leftCol-big"><fmt:message key='title.table.role.sp.role'/></th>
                                <th><fmt:message key='config.application.authz.permissions.action'/></th>
                            </tr>
                            </thead>
                            <tbody>
                            <% if (roleMapping != null && !roleMapping.isEmpty()) { %>
                            <script>
                                $(jQuery('#roleMappingAddTable')).toggle();
                            </script>
                            <%
                                int i = -1;
                                for (Map.Entry<String, String> entry : roleMapping.entrySet()) {
                                    i++;
                            %>
                            <tr>
                                <td>
                                    <input style="width: 98%;" class="roleMapIdp" type="text"
                                           value="<%=Encode.forHtmlAttribute(entry.getKey())%>" id="idpRole_<%=i%>"
                                           name="idpRole_<%=i%>" readonly="readonly"/>
                                </td>
                                <td><input style="width: 98%;" class="roleMapSp" type="text"
                                           value="<%=Encode.forHtmlAttribute(entry.getValue())%>" id="spRole_<%=i%>"
                                           name="spRole_<%=i%>" readonly="readonly"/></td>
                                <td>
                                    <a title="<fmt:message key='alert.info.delete.rolemap'/>"
                                       onclick="deleteRoleMappingRow(this);return false;"
                                       href="#"
                                       class="icon-link"
                                       style="background-image: url(images/delete.gif)">
                                        <fmt:message key='link.delete'/>
                                    </a>
                                </td>
                            </tr>
                            <% } %>
                            <% } %>
                            </tbody>
                        </table>
                        <input type="hidden" name="number_of_rolemappings" id="number_of_rolemappings" value="1">
                    </div>
                </div>

                <h2 id="app_authentication_head" class="sectionSeperator trigger active">
                    <a href="#"><fmt:message key="title.config.app.authentication"/></a>
                </h2>

                <%
                    if (display != null && (display.equals("oauthapp") || display.equals("samlIssuer") ||
                        display.equals("serviceName") || display.equals("kerberos"))) {
                %>
                <div class="toggle_container sectionSub" style="margin-bottom:10px;" id="inbound_auth_request_div">
                            <%} else { %>
                    <div class="toggle_container sectionSub" style="margin-bottom:10px;display:none;"
                         id="inbound_auth_request_div">
                                <%} %>
                        <h2 id="saml.config.head" class="sectionSeperator trigger active"
                            style="background-color: beige;">
                            <a href="#"><fmt:message key="title.config.saml2.web.sso.config"/></a>
                            <% if (appBean.getSAMLIssuer() != null) { %>
                            <div class="enablelogo"><img src="images/ok.png" width="16" height="16"></div>
                            <%} %>
                        </h2>

                                <%if (display!=null && display.equals("samlIssuer")) { %>
                        <div class="toggle_container sectionSub" style="margin-bottom:10px;" id="saml.config.div">
                                    <% } else { %>
                            <div class="toggle_container sectionSub" style="margin-bottom:10px;display:none;"
                                 id="saml.config.div">
                                <% } %>
                                <table class="carbonFormTable">
                                    <tr>
                                        <td class="leftCol-med labelField">
                                            <%
                                                if (appBean.getSAMLIssuer() == null) {
                                            %>
                                            <a id="saml_link" class="icon-link" onclick="onSamlSsoClick()"><fmt:message
                                                key='auth.configure'/></a>
                                            <%
                                            } else {
                                            %>
                                            <div style="clear:both"></div>
                                            <table class="styledLeft" id="samlTable">
                                                <thead>
                                                <tr>
                                                    <th class="leftCol-big"><fmt:message
                                                        key='title.table.saml.config.issuer'/></th>
                                                    <th class="leftCol-big"><fmt:message
                                                        key='application.info.saml2sso.acsi'/></th>
                                                    <th><fmt:message key='application.info.saml2sso.action'/></th>
                                                </tr>
                                                </thead>
                                                <tbody>
                                                <tr>
                                                    <td><%=Encode.forHtmlContent(appBean.getSAMLIssuer())%>
                                                    </td>
                                                    <td>
                                                        <% if (attributeConsumingServiceIndex == null || attributeConsumingServiceIndex.isEmpty()) {
                                                            attributeConsumingServiceIndex = appBean.getAttributeConsumingServiceIndex();
                                                        }

                                                            if (attributeConsumingServiceIndex != null) {%>
                                                        <%=Encode.forHtmlContent(attributeConsumingServiceIndex)%>
                                                        <% } %>
                                                    </td>
                                                    <td style="white-space: nowrap;">
                                                        <a title="Edit Service Providers"
                                                           onclick="updateBeanAndRedirect('../sso-saml/add_service_provider.jsp?SPAction=editServiceProvider&issuer=<%=Encode.forUriComponent(appBean.getSAMLIssuer())%>&spName=<%=Encode.forUriComponent(spName)%>');"
                                                           class="icon-link"
                                                           style="background-image: url(../admin/images/edit.gif)">Edit</a>
                                                        <a title="Delete Service Providers"
                                                           onclick="updateBeanAndPostWithConfirmation('../sso-saml/remove_service_provider-finish-ajaxprocessor.jsp',
                                                               'issuer=<%=Encode.forUriComponent(appBean.getSAMLIssuer())%>&spName=<%=Encode.forUriComponent(spName)%>',
                                                               'configure-service-provider.jsp?action=delete&samlIssuer=<%=Encode.forUriComponent(appBean.getSAMLIssuer())%>&spName=<%=Encode.forUriComponent(spName)%>');"
                                                           class="icon-link"
                                                           style="background-image: url(images/delete.gif)">
                                                            Delete </a>
                                                    </td>
                                                </tr>
                                                </tbody>
                                            </table>
                                            <%
                                                }
                                            %>
                                            <div style="clear:both"></div>
                                        </td>
                                    </tr>
                                </table>

                            </div>
                            <h2 id="oauth.config.head" class="sectionSeperator trigger active"
                                style="background-color: beige;">
                                <a href="#"><fmt:message key="title.config.oauth2.oidc.config"/></a>
                                <% if (appBean.getOIDCClientId() != null) { %>
                                <div class="enablelogo"><img src="images/ok.png" width="16" height="16"></div>
                                <%} %>
                            </h2>
                                    <%if (display!=null && display.equals("oauthapp")) { %>
                            <div class="toggle_container sectionSub" style="margin-bottom:10px;" id="oauth.config.div">
                                        <%} else { %>
                                <div class="toggle_container sectionSub" style="margin-bottom:10px;display:none;"
                                     id="oauth.config.div">
                                    <%} %>
                                    <table class="carbonFormTable">
                                        <tr>
                                            <td>
                                                <%
                                                    if (appBean.getOIDCClientId() == null) {
                                                %>
                                                <a id="oauth_link" class="icon-link" onclick="onOauthClick()">
                                                    <fmt:message key='auth.configure'/></a>
                                                <%
                                                } else {
                                                %>
                                                <div style="clear:both"></div>
                                                <table class="styledLeft" id="samlTable">
                                                    <thead>
                                                    <tr>
                                                        <th class="leftCol-big">OAuth Client Key</th>
                                                        <th class="leftCol-big">OAuth Client Secret</th>
                                                        <th><fmt:message key='application.info.oauthoidc.action'/></th>
                                                    </tr>
                                                    </thead>
                                                    <tbody>
                                                    <tr>
                                                        <td><%=Encode.forHtmlContent(appBean.getOIDCClientId())%>
                                                        </td>
                                                        <td>
                                                            <%
                                                                if (oauthConsumerSecret == null || oauthConsumerSecret.isEmpty()) {
                                                                    oauthConsumerSecret = appBean.getOauthConsumerSecret();
                                                                }
                                                                if (oauthConsumerSecret != null) {
                                                            %>
                                                            <% if (!(appBean.getOauthConsumerSecret() == null || "false".equals(isHashDisabled))) { %>
                                                            <div>
                                                                <input style="border: none; background: white;"
                                                                       type="password" autocomplete="off"
                                                                       id="oauthConsumerSecret"
                                                                       name="oauthConsumerSecret"
                                                                       value="<%=Encode.forHtmlAttribute(oauthConsumerSecret)%>"
                                                                       readonly="readonly">
                                                                <span style="float: right;">
                                                        <a style="margin-top: 5px;" class="showHideBtn"
                                                           onclick="showHidePassword(this, 'oauthConsumerSecret')">Show</a>
                                                    </span>
                                                            </div>
                                                            <% } %>
                                                            <%} %>
                                                        </td>
                                                        <td style="white-space: nowrap;">
                                                            <a title="Edit Service Providers"
                                                               onclick="updateBeanAndRedirect('../oauth/edit.jsp?appName=<%=Encode.forUriComponent(spName)%>&consumerkey=<%=Encode.forUriComponent(appBean.getOIDCClientId())%>');"
                                                               class="icon-link"
                                                               style="background-image: url(../admin/images/edit.gif)">Edit</a>


                                                            <a title="Revoke Service Providers"
                                                               onclick="updateBeanAndPostToWithConfirmation('../oauth/edit-app-ajaxprocessor.jsp','appName=<%=Encode.forUriComponent(spName)%>&consumerkey=<%=Encode.forUriComponent(appBean.getOIDCClientId())%>&action=revoke');"
                                                               class="icon-link"
                                                               style="background-image: url(images/disabled.png)">Revoke</a>


                                                            <a title="Regenerate Secret Key"
                                                               onclick="updateBeanAndPostToWithConfirmation('../oauth/edit-app-ajaxprocessor.jsp','appName=<%=Encode.forUriComponent(spName)%>&consumerkey=<%=Encode.forUriComponent(appBean.getOIDCClientId())%>&action=regenerate');"
                                                               class="icon-link"
                                                               style="background-image: url(images/enabled.png)">Regenerate
                                                                Secret</a>


                                                            <a title="Delete Service Providers"
                                                               onclick="updateBeanAndPostWithConfirmation('../oauth/remove-app-ajaxprocessor.jsp',
                                                                   'consumerkey=<%=Encode.forUriComponent(appBean.getOIDCClientId())%>&appName=<%=Encode.forUriComponent(spName)%>&spName=<%=Encode.forUriComponent(spName)%>',
                                                                   'configure-service-provider.jsp?action=delete&spName=<%=Encode.forUriComponent(spName)%>&oauthapp=<%=Encode.forUriComponent(appBean.getOIDCClientId())%>');"
                                                               class="icon-link"
                                                               style="background-image: url(images/delete.gif)">
                                                                Delete </a>
                                                        </td>
                                                    </tr>
                                                    </tbody>
                                                </table>
                                                <%
                                                    }
                                                %>
                                                <div style="clear:both"></div>
                                            </td>
                                        </tr>
                                    </table>
                                </div>


                                <h2 id="openid.config.head" class="sectionSeperator trigger active"
                                    style="background-color: beige;">
                                    <a href="#">OpenID Configuration</a>
                                    <div class="enablelogo"><img src="images/ok.png" width="16" height="16"></div>
                                </h2>
                                <div class="toggle_container sectionSub" style="margin-bottom:10px;display:none;"
                                     id="openid.config.div">
                                    <table class="carbonFormTable">

                                        <tr>
                                            <td style="width:15%" class="leftCol-med labelField">
                                                <fmt:message key='application.openid.realm'/>:
                                            </td>
                                            <td>
                                                <%
                                                    if (appBean.getOpenIDRealm() != null) {
                                                %>
                                                <input style="width:50%" id="openidRealm" name="openidRealm" type="text"
                                                       value="<%=Encode.forHtmlAttribute(appBean.getOpenIDRealm())%>"
                                                       autofocus/>
                                                <% } else { %>
                                                <input style="width:50%" id="openidRealm" name="openidRealm" type="text"
                                                       value="" autofocus/>
                                                <% } %>
                                                <div class="sectionHelp">
                                                    <fmt:message key='help.openid'/>
                                                </div>
                                            </td>

                                        </tr>

                                    </table>
                                </div>


                                <h2 id="passive.sts.config.head" class="sectionSeperator trigger active"
                                    style="background-color: beige;">
                                    <a href="#">WS-Federation (Passive) Configuration</a>
                                    <div class="enablelogo"><img src="images/ok.png" width="16" height="16"></div>
                                </h2>
                                <div class="toggle_container sectionSub" style="margin-bottom:10px;display:none;"
                                     id="passive.config.div">
                                    <table class="carbonFormTable">

                                        <tr>
                                            <td style="width:15%" class="leftCol-med labelField">
                                                <fmt:message key='application.passive.sts.realm'/>:
                                            </td>
                                            <td>
                                                <%
                                                    if (appBean.getPassiveSTSRealm() != null) {
                                                %>
                                                <input style="width:50%" id="passiveSTSRealm" name="passiveSTSRealm"
                                                       type="text"
                                                       value="<%=Encode.forHtmlAttribute(appBean.getPassiveSTSRealm())%>"
                                                       autofocus/>
                                                <% } else { %>
                                                <input style="width:50%" id="passiveSTSRealm" name="passiveSTSRealm"
                                                       type="text" value="" autofocus/>
                                                <% } %>
                                                <div class="sectionHelp">
                                                    <fmt:message key='help.passive.sts'/>
                                                </div>
                                            </td>

                                        </tr>
                                        <tr>
                                            <td style="width:15%" class="leftCol-med labelField">
                                                <fmt:message key='application.passive.sts.wreply'/>:
                                            </td>
                                            <td>
                                                <%
                                                    if (appBean.getPassiveSTSWReply() != null) {
                                                %>
                                                <input style="width:50%" id="passiveSTSWReply" name="passiveSTSWReply"
                                                       type="text"
                                                       value="<%=Encode.forHtmlAttribute(appBean.getPassiveSTSWReply())%>"
                                                       autofocus/>
                                                <% } else { %>
                                                <input style="width:50%" id="passiveSTSWReply" name="passiveSTSWReply"
                                                       type="text" value="" autofocus/>
                                                <% } %>
                                                <div class="sectionHelp">
                                                    <fmt:message key='help.passive.sts.wreply'/>
                                                </div>
                                            </td>

                                        </tr>

                                    </table>
                                </div>

                                <% if (STSServiceValidationUtil.isWSTrustAvailable()) { %>
                                <h2 id="wst.config.head" class="sectionSeperator trigger active"
                                    style="background-color: beige;">
                                    <a href="#"><fmt:message key="title.config.sts.config"/></a>
                                    <% if (appBean.getAllWsTrustSPs() != null && !appBean.getAllWsTrustSPs().isEmpty()) { %>
                                    <div class="enablelogo"><img src="images/ok.png" width="16" height="16"></div>
                                    <%} %>
                                </h2>
                                        <%if (display!=null && display.equals("serviceName")) { %>
                                <div class="toggle_container sectionSub" style="margin-bottom:10px;"
                                     id="wst.config.div">
                                            <% } else { %>
                                    <div class="toggle_container sectionSub" style="margin-bottom:10px;display:none;"
                                         id="wst.config.div">
                                        <%} %>
                                        <table class="carbonFormTable">

                                            <tr>
                                                <td>
                                                    <%
                                                        if (appBean.getAllWsTrustSPs() == null || appBean.getAllWsTrustSPs().isEmpty()) {
                                                    %>
                                                    <a id="sts_link" class="icon-link" onclick="onSTSClick()">
                                                        <fmt:message key='auth.configure'/></a>
                                                    <%
                                                    } else {
                                                    %>
                                                    <div style="clear:both"></div>
                                                    <table class="styledLeft" id="samlTable">
                                                        <thead>
                                                        <tr>
                                                            <th class="leftCol-med">Audience</th>
                                                            <th><fmt:message
                                                                key='application.info.oauthoidc.action'/></th>
                                                        </tr>
                                                        </thead>
                                                        <tbody>
                                                        <%
                                                            for (String wsTrustURI : appBean.getAllWsTrustSPs()) {
                                                        %>
                                                        <tr>
                                                            <td>
                                                                <%=Encode.forHtmlContent(wsTrustURI)%>
                                                            </td>
                                                            <td style="white-space: nowrap;">
                                                                <a title="Edit Audience"
                                                                   onclick="updateBeanAndRedirect('../generic-sts/sts.jsp?spName=<%=Encode.forUriComponent(spName)%>&spAudience=<%=Encode.forUriComponent(wsTrustURI)%>&spAction=spEdit');"
                                                                   class="icon-link"
                                                                   style="background-image: url(../admin/images/edit.gif)">Edit</a>
                                                                <a title="Delete Audience"
                                                                   onclick="updateBeanAndPostWithConfirmation('../generic-sts/remove-sts-trusted-service-ajaxprocessor.jsp',
                                                                       'action=delete&spName=<%=Encode.forUriComponent(spName)%>&endpointaddrs=<%=Encode.forUriComponent(wsTrustURI)%>',
                                                                       'configure-service-provider.jsp?spName=<%=Encode.forUriComponent(spName)%>&action=delete&serviceName=<%=Encode.forUriComponent(wsTrustURI)%>');"
                                                                   class="icon-link"
                                                                   style="background-image: url(images/delete.gif)">
                                                                    Delete </a>
                                                            </td>
                                                        </tr>
                                                        <% } %>
                                                        </tbody>
                                                    </table>
                                                    <a id="sts_link" class="icon-link" style="background-image:url(images/add.gif);" onclick="onSTSClick()">
                                                        <fmt:message key='auth.add.audience'/></a>
                                                    <%
                                                        }
                                                    %>
                                                    <div style="clear:both"></div>
                                                </td>
                                            </tr>

                                        </table>
                                    </div>
                                    <%} %>

                                    <h2 id="kerberos.kdc.head" class="sectionSeperator trigger active"
                                        style="background-color: beige;">
                                        <a href="#">Kerberos KDC</a>

                                        <% if (appBean.getKerberosServiceName() != null) { %>
                                        <div class="enablelogo"><img src="images/ok.png" width="16" height="16"></div>
                                        <% } %>
                                    </h2>

                                            <%if (display!=null && display.equals("kerberos")) { %>
                                    <div class="toggle_container sectionSub" style="margin-bottom:10px;"
                                         id="kerberos.config.div">
                                        <%} else { %>
                                        <div class="toggle_container sectionSub"
                                             style="margin-bottom:10px;display:none;"
                                             id="kerberos.config.div">
                                            <%} %>

                                            <table class="carbonFormTable">

                                                <tr>
                                                    <td>
                                                        <%
                                                            if (appBean.getKerberosServiceName() == null) {
                                                        %>
                                                        <a id="kerberos_link" class="icon-link"
                                                           onclick="onKerberosClick()"><fmt:message
                                                            key='auth.configure'/></a>

                                                        <% } else { %>
                                                        <div style="clear:both"></div>
                                                        <table class="styledLeft" id="kerberosTable">
                                                            <thead>
                                                            <tr>
                                                                <th class="leftCol-big"><fmt:message
                                                                    key='title.table.kerberos.config'/></th>
                                                                <th><fmt:message
                                                                    key='application.info.kerberos.action'/></th>
                                                            </tr>
                                                            </thead>

                                                            <tbody>
                                                            <tr>
                                                                <td><%=Encode.forHtmlContent(appBean.getKerberosServiceName())%>
                                                                </td>
                                                                <td style="white-space: nowrap;">
                                                                    <a title="Change Password"
                                                                       onclick="updateBeanAndRedirect('../servicestore/change-passwd.jsp?SPAction=changePWr&spnName=<%=Encode.forUriComponent(appBean.getKerberosServiceName())%>&spName=<%=Encode.forUriComponent(spName)%>');"
                                                                       class="icon-link"
                                                                       style="background-image: url(../admin/images/edit.gif)">Change
                                                                        Password</a>
                                                                    <a title="Delete"
                                                                       onclick="updateBeanAndPostWithConfirmation('../servicestore/delete-finish-ajaxprocessor.jsp',
                                                                           'SPAction=delete&spnName=<%=Encode.forUriComponent(appBean.getKerberosServiceName())%>&spName=<%=Encode.forUriComponent(spName)%>',
                                                                           'configure-service-provider.jsp?action=delete&spName=<%=Encode.forUriComponent(spName)%>&kerberos=<%=Encode.forUriComponent(appBean.getKerberosServiceName())%>');"
                                                                       class="icon-link"
                                                                       style="background-image: url(images/delete.gif)">
                                                                        Delete </a>
                                                                </td>
                                                            </tr>
                                                            </tbody>
                                                        </table>
                                                        <%
                                                            }
                                                        %>
                                                    </td>

                                                </tr>

                                            </table>
                                        </div>

                                        <%
                                            List<String> standardInboundAuthTypes = new ArrayList<String>();
                                            standardInboundAuthTypes = new ArrayList<String>();
                                            standardInboundAuthTypes.add("oauth2");
                                            standardInboundAuthTypes.add("wstrust");
                                            standardInboundAuthTypes.add("samlsso");
                                            standardInboundAuthTypes.add("openid");
                                            standardInboundAuthTypes.add("passivests");
                                            standardInboundAuthTypes.add("kerberos");

                                            if (!CollectionUtils.isEmpty(appBean.getInboundAuthenticators())) {
                                                List<InboundAuthenticationRequestConfig> customAuthenticators = appBean
                                                    .getInboundAuthenticators();
                                                for (InboundAuthenticationRequestConfig customAuthenticator : customAuthenticators) {
                                                    if (!standardInboundAuthTypes.contains(customAuthenticator.getInboundAuthType())) {
                                                        String type = customAuthenticator.getInboundAuthType();
                                                        String friendlyName = customAuthenticator.getFriendlyName();
                                        %>

                                        <h2 id="openid.config.head" class="sectionSeperator trigger active"
                                            style="background-color: beige;">
                                            <a href="#"><%=friendlyName%>
                                            </a>

                                            <div class="enablelogo"><img src="images/ok.png" width="16" height="16">
                                            </div>
                                        </h2>
                                        <div class="toggle_container sectionSub"
                                             style="margin-bottom:10px;display:none;"
                                             id="openid.config.div">
                                            <table class="carbonFormTable">
                                                <%
                                                    Property[] properties = customAuthenticator.getProperties();

                                                    if (properties != null && properties.length > 0) {
                                                        // Remove invalid properties returned from custom authenticators
                                                        List<Property> nonNullProperties = new ArrayList<Property>();
                                                        for (Property property : properties) {
                                                            if (property != null && StringUtils.isNotBlank(property.getName()) &&
                                                                StringUtils.isNotBlank(property.getDisplayName())) {
                                                                nonNullProperties.add(property);
                                                            }
                                                        }
                                                        properties = nonNullProperties.toArray(new Property[nonNullProperties.size()]);
                                                        Arrays.sort(properties, new Comparator<Property>() {
                                                            public int compare(Property obj1, Property obj2) {

                                                                return Integer.compare(obj1.getDisplayOrder(), obj2.getDisplayOrder());
                                                            }
                                                        });

                                                        for (Property prop : properties) {
                                                            String propName = "custom_auth_prop_name_" + type + "_" + prop.getName();
                                                            String hiddenProp = "";
                                                            if (StringUtils.equals(prop.getType(), "hidden")) {
                                                                hiddenProp = "hidden";
                                                            }
                                                %>
                                                <tr <%=hiddenProp%>>
                                                    <td>
                                                                <%if (prop.getRequired()) { %>
                                                    <td style="width:15%"
                                                        class="leftCol-med labelField"><%=prop.getDisplayName()%>
                                                        :<span class="required">*</span></td>
                                                            <% } else { %>
                                                    <td style="width:15%"
                                                        class="leftCol-med labelField"><%=prop.getDisplayName()%>:
                                                    </td>
                                                            <%} %>
                                                    <td>
                                                        <% if (prop.getConfidential()) { %>
                                                        <div id="showHideButtonDivId"
                                                             style="border:1px solid rgb(88, 105, 125);"
                                                             class="leftCol-med">
                                                            <input id=<%=propName%> name="<%=propName%>"
                                                                   type="password" autocomplete="off"
                                                                <% if (StringUtils.isNotBlank(prop.getValue())) { %>
                                                                   value="<%=prop.getValue()%>"
                                                                <% } else if (StringUtils.isNotBlank(prop.getDefaultValue())) { %>
                                                                   value="<%=prop.getDefaultValue()%>"
                                                                <%}%>
                                                                   style="  outline: none; border: none; min-width: 175px; max-width:
                                                    180px;"/>
                                                            <span style=" float: right; padding-right: 5px;">
                                                <a style="margin-top: 5px;" class="showHideBtn"
                                                   onclick="showHidePassword(this, '<%=propName%>')">Show</a>
                                            </span>
                                                        </div>
                                                        <% } else { %>
                                                        <input id="<%=propName%>"
                                                               name="<%=propName%>"
                                                               type="text"
                                                            <% if (StringUtils.isNotBlank(prop.getValue())) { %>
                                                               value="<%=prop.getValue()%>"
                                                            <% } else if (StringUtils.isNotBlank(prop.getDefaultValue())) { %>
                                                               value="<%=prop.getDefaultValue()%>"
                                                            <% } %>
                                                        />
                                                        <% } %>
                                                        <% if (prop.getDescription() != null) { %>
                                                        <div class="sectionHelp"><%=prop.getDescription()%>
                                                        </div>
                                                        <%} %>
                                                    </td>
                                                            <%
                                                }
                                    }
                                %>
                                            </table>
                                        </div>
                                        <%
                                                    }
                                                }
                                            }
                                        %>
                                    </div>

                                    <h2 id="app_authentication_advance_head" class="sectionSeperator trigger active">
                                        <a href="#"><fmt:message
                                            key="outbound.title.config.app.authentication.type"/></a>
                                    </h2>
                                            <%if (display!=null && "auth_config".equals(display)) {%>
                                    <div class="toggle_container sectionSub" style="margin-bottom:10px;display:block;"
                                         id="advanceAuthnConfRow">
                                                <% } else { %>
                                        <div class="toggle_container sectionSub"
                                             style="margin-bottom:10px;display:none;" id="advanceAuthnConfRow">
                                            <% } %>
                                            <table class="carbonFormTable">
                                                <tr>
                                                    <td class="leftCol-med labelField"><fmt:message
                                                        key='config.application.info.authentication.advance.type'/>:<span
                                                        class="required">*</span>
                                                    </td>
                                                    <td class="leftCol-med">
                                                        <% if (ApplicationBean.AUTH_TYPE_DEFAULT.equals(appBean.getAuthenticationType())) { %>
                                                        <input type="radio" id="default" name="auth_type"
                                                               value="default" checked><label for="default"
                                                                                              style="cursor: pointer;"><fmt:message
                                                        key="config.authentication.type.default"/></label>
                                                        <% } else { %>
                                                        <input type="radio" id="default" name="auth_type"
                                                               value="default"><label for="default"
                                                                                      style="cursor: pointer;"><fmt:message
                                                        key="config.authentication.type.default"/></label>
                                                        <% } %>
                                                    </td>
                                                    <td/>
                                                </tr>
                                                <tr>
                                                    <td style="width:15%" class="leftCol-med labelField"/>
                                                    <td>
                                                        <% if (ApplicationBean.AUTH_TYPE_LOCAL.equals(appBean.getAuthenticationType())) { %>
                                                        <input type="radio" id="local" name="auth_type" value="local"
                                                               checked><label for="local"
                                                                              style="cursor: pointer;"><fmt:message
                                                        key="config.authentication.type.local"/></label>
                                                        <% } else { %>
                                                        <input type="radio" id="local" name="auth_type"
                                                               value="local"><label for="local"
                                                                                    style="cursor: pointer;"><fmt:message
                                                        key="config.authentication.type.local"/></label>
                                                        <% } %>
                                                    </td>
                                                    <td>
                                                        <select name="local_authenticator" id="local_authenticator">
                                                            <%
                                                                if (appBean.getLocalAuthenticatorConfigs() != null) {
                                                                    LocalAuthenticatorConfig[] localAuthenticatorConfigs = appBean.getLocalAuthenticatorConfigs();
                                                                    for (LocalAuthenticatorConfig authenticator : localAuthenticatorConfigs) {
                                                                        Property[] props = authenticator.getProperties();
                                                                        if (props != null && props.length > 0) {
                                                                            boolean isHander = false;
                                                                            for (Property pro : props) {
                                                                                if ((IS_HANDLER.equals(pro.getName()) && Boolean.valueOf(pro.getValue()))) {
                                                                                    isHander = true;
                                                                                    break;
                                                                                }
                                                                            }
                                                                            if (isHander) {
                                                                                continue;
                                                                            }
                                                                        }
                                                            %>
                                                            
                                                            <% if (authenticator.getName().equals(appBean.getStepZeroAuthenticatorName(ApplicationBean.AUTH_TYPE_LOCAL))) { %>
                                                            <option
                                                                value="<%=Encode.forHtmlAttribute(authenticator.getName())%>"
                                                                selected><%=Encode.forHtmlContent(authenticator.getDisplayName())%>
                                                            </option>
                                                            <% } else { %>
                                                            <option
                                                                value="<%=Encode.forHtmlAttribute(authenticator.getName())%>"><%=Encode.forHtmlContent(authenticator.getDisplayName())%>
                                                            </option>
                                                            <% } %>
                                                            <% } %>
                                                            <% } %>
                                                        </select>
                                                    </td>
                                                </tr>
                                                <%

                                                    if (appBean.getEnabledFederatedIdentityProviders() != null && appBean.getEnabledFederatedIdentityProviders().size() > 0) {%>
                                                <tr>
                                                    <td class="leftCol-med labelField"/>
                                                    <td>
                                                        <% if (ApplicationBean.AUTH_TYPE_FEDERATED.equals(appBean.getAuthenticationType())) { %>
                                                        <input type="radio" id="federated" name="auth_type"
                                                               value="federated" checked><label for="federated"
                                                                                                style="cursor: pointer;"><fmt:message
                                                        key="config.authentication.type.federated"/></label>
                                                        <% } else { %>
                                                        <input type="radio" id="federated" name="auth_type"
                                                               value="federated"><label for="federated"
                                                                                        style="cursor: pointer;"><fmt:message
                                                        key="config.authentication.type.federated"/></label>
                                                        <% } %>
                                                    </td>
                                                    <td>
                                                        <select name="fed_idp" id="fed_idp">
                                                            <% List<IdentityProvider> idps = appBean.getEnabledFederatedIdentityProviders();
                                                                String selectedIdP = appBean.getStepZeroAuthenticatorName(ApplicationBean.AUTH_TYPE_FEDERATED);
                                                                boolean isSelectedIdPUsed = false;
                                                                for (IdentityProvider idp : idps) {
                                                                    if (selectedIdP != null && idp.getIdentityProviderName().equals(selectedIdP)) {
                                                                        isSelectedIdPUsed = true;
                                                            %>
                                                            <option
                                                                value="<%=Encode.forHtmlAttribute(idp.getIdentityProviderName())%>"
                                                                selected><%=Encode.forHtmlContent(idp.getIdentityProviderName()) %>
                                                            </option>
                                                            <% } else { %>
                                                            <option
                                                                value="<%=Encode.forHtmlAttribute(idp.getIdentityProviderName())%>"><%=Encode.forHtmlContent(idp.getIdentityProviderName())%>
                                                            </option>
                                                            <% } %>
                                                            <% } %>
                                                        </select>
                                                    </td>
                                                </tr>
                                                <% } else {%>
                                                <tr>
                                                    <td class="leftCol-med labelField"/>
                                                    <td>
                                                        <input type="radio" id="disabledFederated" name="auth_type"
                                                               value="federated" disabled><label
                                                        for="disabledFederated"><fmt:message
                                                        key="config.authentication.type.federated"/></label>
                                                    </td>
                                                    <td></td>
                                                </tr>
                                                <% } %>
                                                <tr>
                                                    <td class="leftCol-med labelField"/>
                                                    <td>
                                                        <% if (ApplicationBean.AUTH_TYPE_FLOW.equals(appBean.getAuthenticationType())) { %>
                                                        <input type="radio" id="advanced" name="auth_type" value="flow"
                                                               onclick="updateBeanAndRedirect('configure-authentication-flow.jsp?spName=<%=Encode.forUriComponent(spName)%>');"
                                                               checked><label style="cursor: pointer; color: #2F7ABD;"
                                                                              for="advanced"><fmt:message
                                                        key="config.authentication.type.flow"/></label>
                                                        <% } else { %>
                                                        <input type="radio" id="advanced" name="auth_type" value="flow"
                                                               onclick="updateBeanAndRedirect('configure-authentication-flow.jsp?spName=<%=Encode.forUriComponent(spName)%>')"><label
                                                        style="cursor: pointer; color: #2F7ABD;"
                                                        for="advanced"><fmt:message
                                                        key="config.authentication.type.flow"/></label>
                                                        <% } %>
                                                    </td>
                                                </tr>
                                            </table>
                                            <table class="carbonFormTable" style="padding-top: 5px;">
                                                <tr>
                                                    <td class="leftCol-med">
                                                        <input type="checkbox" id="always_send_local_subject_id"
                                                               name="always_send_local_subject_id" <%=appBean.isAlwaysSendMappedLocalSubjectId() ? "checked" : "" %>/><label
                                                        for="always_send_local_subject_id"><fmt:message
                                                        key="config.application.claim.assert.local.select"/></label>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td class="leftCol-med">
                                                        <input type="checkbox" id="always_send_auth_list_of_idps"
                                                               name="always_send_auth_list_of_idps" <%=appBean.isAlwaysSendBackAuthenticatedListOfIdPs() ? "checked" : "" %>/><label
                                                        for="always_send_auth_list_of_idps"><fmt:message
                                                        key="config.application.claim.always.auth.list"/></label>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td class="leftCol-med">
                                                        <input type="checkbox"
                                                               id="use_tenant_domain_in_local_subject_identifier"
                                                               name="use_tenant_domain_in_local_subject_identifier" <%=appBean.isUseTenantDomainInLocalSubjectIdentifier() ? "checked"
                                                            : "" %>/><label
                                                        for="use_tenant_domain_in_local_subject_identifier"><fmt:message
                                                        key="config.application.use.tenant.domain.in.local.subject.identifier"/></label>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td class="leftCol-med">
                                                        <input type="checkbox"
                                                               id="use_userstore_domain_in_local_subject_identifier"
                                                               name="use_userstore_domain_in_local_subject_identifier" <%=appBean.isUseUserstoreDomainInLocalSubjectIdentifier() ?
                                                            "checked" : "" %>/><label
                                                        for="use_userstore_domain_in_local_subject_identifier"><fmt:message
                                                        key="config.application.use.userstore.domain.in.local.subject.identifier"/></label>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td class="leftCol-med">
                                                        <input type="checkbox" id="use_userstore_domain_in_roles"
                                                               name="use_userstore_domain_in_roles" <%=appBean.isUseUserstoreDomainInRoles() ?
                                                                "checked" : "" %>/><label
                                                            for="use_userstore_domain_in_roles"><fmt:message
                                                            key="config.application.use.userstore.domain.in.roles"/></label>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td class="leftCol-med">
                                                        <input type="checkbox" id="enable_authorization"
                                                               name="enable_authorization" <%=appBean.isEnableAuthorization() ?
                                                            "checked" : "" %>/><label
                                                        for="enable_authorization"><fmt:message
                                                        key="config.application.enable.authorization"/></label>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <input type="checkbox" id="skipConsent" name="skipConsent" <%= appBean.isSkipConsent() ? "checked" : ""%> value="true"/>
                                                    <label for="skipConsent"><fmt:message key="config.application.skip.consent"/></label>
                                                    </br>
                                                    <div class="sectionHelp">
                                                        <fmt:message key='help.skip.consent'/>
                                                    </div>
                                                </tr>
                                                <tr>
                                                    <input type="checkbox" id="skipLogoutConsent" name="skipLogoutConsent" <%=appBean.isSkipLogoutConsent() ? "checked" : ""%> value="true"/>
                                                    <label for="skipLogoutConsent"><fmt:message key="config.application.skip.logout.consent"/></label>
                                                    </br>
                                                    <div class="sectionHelp">
                                                        <fmt:message key='help.skip.logout.consent'/>
                                                    </div>
                                                </tr>
                                            </table>


                                            <h2 id="req_path_head" class="sectionSeperator trigger active"
                                                style="background-color: beige;">
                                                <a href="#"><fmt:message
                                                    key="title.req.config.authentication.steps"/></a>
                                            </h2>
                                            <div class="toggle_container sectionSub" style="margin-bottom:10px;"
                                                 id="ReqPathAuth">
                                                <table class="styledLeft" width="100%" id="req_path_auth_table">
                                                    <thead>
                                                    <tr>
                                                        <td>
                                                            <select name="reqPathAuthType"
                                                                    style="float: left; min-width: 150px;font-size:13px;"><%=requestPathAuthTypes.toString()%>
                                                            </select>
                                                            <a id="reqPathAuthenticatorAddLink" class="icon-link"
                                                               style="background-image:url(images/add.gif);">Add</a>
                                                            <div style="clear:both"></div>
                                                            <div class="sectionHelp">
                                                                <fmt:message key='help.local.authnticators'/>
                                                            </div>
                                                        </td>
                                                    </tr>
                                                    </thead>

                                                    <%
                                                        if (appBean.getServiceProvider().getRequestPathAuthenticatorConfigs() != null && appBean.getServiceProvider().getRequestPathAuthenticatorConfigs().length > 0) {
                                                            int x = 0;
                                                            for (RequestPathAuthenticatorConfig reqAth : appBean.getServiceProvider().getRequestPathAuthenticatorConfigs()) {
                                                                if (reqAth != null) {
                                                    %>
                                                    <tr>
                                                        <td>
                                                            <input name="req_path_auth" id="req_path_auth" type="hidden"
                                                                   value="<%=Encode.forHtmlAttribute(reqAth.getName())%>"/>
                                                            <input
                                                                name="req_path_auth_<%=Encode.forHtmlAttribute(reqAth.getName())%>"
                                                                id="req_path_auth_<%=Encode.forHtmlAttribute(reqAth.getName())%>"
                                                                type="hidden"
                                                                value="<%=Encode.forHtmlAttribute(reqAth.getName())%>"/>

                                                            <%=Encode.forHtmlContent(reqAth.getName())%>
                                                        </td>
                                                        <td class="leftCol-small">
                                                            <a onclick="deleteReqPathRow(this);return false;" href="#"
                                                               class="icon-link"
                                                               style="background-image: url(images/delete.gif)">
                                                                Delete </a>
                                                        </td>
                                                    </tr>
                                                    <%
                                                                }
                                                            }
                                                        }

                                                    %>
                                                </table>
                                            </div>

                                        </div>

                                        <h2 id="inbound_provisioning_head" class="sectionSeperator trigger active">
                                            <a href="#"><fmt:message key="inbound.provisioning.head"/></a>
                                        </h2>
                                        <div class="toggle_container sectionSub" style="margin-bottom:10px;"
                                             id="inboundProvisioning">

                                            <h2 id="scim-inbound_provisioning_head"
                                                class="sectionSeperator trigger active"
                                                style="background-color: beige;">
                                                <a href="#"><fmt:message key="scim.inbound.provisioning.head"/></a>
                                            </h2>
                                            <div class="toggle_container sectionSub" style="margin-bottom:10px;"
                                                 id="scim-inbound-provisioning-div">
                                                <table class="carbonFormTable">
                                                    <tr>
                                                        <td>Service provider based SCIM provisioning is protected via
                                                            OAuth 2.0.
                                                            Your service provider must have a valid OAuth 2.0 client key
                                                            and a client secret to invoke the SCIM API.
                                                            To create OAuth 2.0 key/secret : Inbound Authentication
                                                            Configuration -> OAuth/OpenID Connect Configuration.<br/>
                                                        </td>
                                                    </tr>
                                                    <tr>
                                                        <td>
                                                            <select style="min-width: 250px;"
                                                                    id="scim-inbound-userstore"
                                                                    name="scim-inbound-userstore" <%=appBean.getServiceProvider().getInboundProvisioningConfig().getDumbMode() ? "disabled" : "" %>>
                                                                <option value="">---Select---</option>
                                                                <%
                                                                    if (userStoreDomains != null && userStoreDomains.length > 0) {
                                                                        for (String userStoreDomain : userStoreDomains) {
                                                                            if (userStoreDomain != null) {
                                                                                if (appBean.getServiceProvider().getInboundProvisioningConfig() != null
                                                                                    && appBean.getServiceProvider().getInboundProvisioningConfig().getProvisioningUserStore() != null
                                                                                    && userStoreDomain.equals(appBean.getServiceProvider().getInboundProvisioningConfig().getProvisioningUserStore())) {
                                                                %>
                                                                <option selected="selected"
                                                                        value="<%=Encode.forHtmlAttribute(userStoreDomain)%>"><%=Encode.forHtmlContent(userStoreDomain)%>
                                                                </option>
                                                                <%
                                                                } else {
                                                                %>
                                                                <option
                                                                    value="<%=Encode.forHtmlAttribute(userStoreDomain)%>"><%=Encode.forHtmlContent(userStoreDomain)%>
                                                                </option>
                                                                <%
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                %>
                                                            </select>
                                                            <div class="sectionHelp">
                                                                <fmt:message key='help.inbound.scim'/>
                                                            </div>
                                                        </td>
                                                    </tr>
                                                    <tr>
                                                        <td>
                                                            <input type="checkbox" name="dumb" id="dumb" value="false"
                                                                   onclick="disable()" <%=appBean.getServiceProvider().getInboundProvisioningConfig().getDumbMode() ? "checked" : "" %>>Enable
                                                            Dumb Mode<br>
                                                            <div class="sectionHelp">
                                                                <fmt:message key='help.inbound.scim.dumb'/>
                                                            </div>
                                                        </td>
                                                    </tr>
                                                </table>
                                            </div>


                                        </div>

                                        <h2 id="outbound_provisioning_head" class="sectionSeperator trigger active">
                                            <a href="#"><fmt:message key="outbound.provisioning.head"/></a>
                                        </h2>
                                        <div class="toggle_container sectionSub" style="margin-bottom:10px;"
                                             id="outboundProvisioning">
                                            <table class="styledLeft" width="100%" id="fed_auth_table">

                                                <% if (idpType != null && idpType.length() > 0) {%>
                                                <thead>

                                                <tr>
                                                    <td>
                                                        <select name="provisioning_idps"
                                                                style="float: left; min-width: 150px;font-size:13px;">
                                                            <%=idpType.toString()%>
                                                            s </select>
                                                        <a id="provisioningIdpAdd"
                                                           onclick="addIDPRow(this);return false;" class="icon-link"
                                                           style="background-image:url(images/add.gif);"></a>
                                                    </td>
                                                </tr>

                                                </thead>
                                                <% } else { %>
                                                <tr>
                                                    <td colspan="4" style="border: none;">There are no provisioning
                                                        enabled identity providers defined in the system.
                                                    </td>
                                                </tr>
                                                <%} %>

                                                <%
                                                    if (appBean.getServiceProvider().getOutboundProvisioningConfig() != null) {
                                                        IdentityProvider[] fedIdps = appBean.getServiceProvider().getOutboundProvisioningConfig().getProvisioningIdentityProviders();
                                                        if (fedIdps != null && fedIdps.length > 0) {
                                                            for (IdentityProvider idp : fedIdps) {
                                                                if (idp != null) {
                                                                    boolean jitEnabled = false;
                                                                    boolean blocking = false;
                                                                    boolean ruleEnabled = false;

                                                                    if (idp.getJustInTimeProvisioningConfig() != null &&
                                                                        idp.getJustInTimeProvisioningConfig().getProvisioningEnabled()) {
                                                                        jitEnabled = true;
                                                                    }
                                                                    if (idp.getDefaultProvisioningConnectorConfig() != null &&
                                                                        idp.getDefaultProvisioningConnectorConfig().getBlocking()) {
                                                                        blocking = true;
                                                                    }
                                                                    if (idp.getDefaultProvisioningConnectorConfig() != null &&
                                                                        idp.getDefaultProvisioningConnectorConfig().getRulesEnabled()) {
                                                                        ruleEnabled = true;
                                                                    }

                                                %>

                                                <tr>
                                                    <td>
                                                        <input name="provisioning_idp" id="" type="hidden"
                                                               value="<%=Encode.forHtmlAttribute(idp.getIdentityProviderName())%>"/>
                                                        <%=Encode.forHtmlContent(idp.getIdentityProviderName()) %>
                                                    </td>
                                                    <td>
                                                        <% if (selectedProIdpConnectors.get(idp.getIdentityProviderName()) != null) { %>
                                                        <select
                                                            name="provisioning_con_idp_<%=Encode.forHtmlAttribute(idp.getIdentityProviderName())%>"
                                                            style="float: left; min-width: 150px;font-size:13px;"><%=selectedProIdpConnectors.get(idp.getIdentityProviderName())%>
                                                        </select>
                                                        <% } %>
                                                    </td>
                                                    <td>
                                                        <div class="sectionCheckbox">
                                                            <input type="checkbox"
                                                                   id="blocking_prov_<%=Encode.forHtmlAttribute(idp.getIdentityProviderName())%>"
                                                                   name="blocking_prov_<%=Encode.forHtmlAttribute(idp.getIdentityProviderName())%>" <%=blocking ? "checked" : "" %>>Blocking
                                                        </div>
                                                    </td>
                                                    <td>
                                                        <div class="sectionCheckbox">
                                                            <input type="checkbox"
                                                                   id="rules_enabled_<%=Encode.forHtmlAttribute(idp.getIdentityProviderName())%>"
                                                                   name="rules_enabled_<%=Encode.forHtmlAttribute(idp.getIdentityProviderName())%>" <%=ruleEnabled ? "checked" :
                                                        "" %>>Enable Rules
                                                        </div>
                                                    </td>
                                                    <td>
                                                        <div class="sectionCheckbox">
                                                            <input type="checkbox"
                                                                   id="provisioning_jit_<%=Encode.forHtmlAttribute(idp.getIdentityProviderName())%>"
                                                                   name="provisioning_jit_<%=Encode.forHtmlAttribute(idp.getIdentityProviderName())%>" <%=jitEnabled ? "checked" : "" %>>Enable
                                                            JIT
                                                        </div>
                                                    </td>
                                                    <td class="leftCol-small">
                                                        <a onclick="deleteIDPRow(this);return false;" href="#"
                                                           class="icon-link"
                                                           style="background-image: url(images/delete.gif)"> Delete </a>
                                                    </td>
                                                </tr>
                                                <%
                                                                }
                                                            }
                                                        }
                                                    }
                                                %>
                                            </table>

                                        </div>

                                        <div style="clear:both"/>
                                        <!-- sectionSub Div -->
                                        <div class="buttonRow">
                                            <input type="button"
                                                   value="<fmt:message key='button.update.service.provider'/>"
                                                   onclick="createAppOnclick();"/>
                                                <input style="display: none" type="button"
                                                       value="<fmt:message key='button.save.service.provider.template'/>"
                                                       onclick="saveAsTemplate();"/>
                                            <input type="hidden" name="templateName" id="templateName"/>
                                            <input type="hidden" name="templateDesc" id="templateDesc"/>
                                            <input type="button" value="<fmt:message key='button.cancel'/>"
                                                   onclick="javascript:location.href='list-service-providers.jsp'"/>
                                        </div>
            </form>
        </div>
    </div>

    <div id="showDialog" title="WSO2 Carbon">
        <h2 style="margin-left:20px;margin-top:20px;">
            <fmt:message key="title.oauth.application"/>
        </h2>

        <% if ("add".equals(operation)) { %>
        <p style="font-size: 12px;margin-top:6px;margin-left:20px;"><fmt:message key="application.successfull"/></p>
        <% } %>

        <% if ("regenerate".equals(action)) { %>
        <p style="font-size: 12px;margin-top:6px;margin-left:20px;">
            <% String message = MessageFormat.format(resourceBundle.getString("application.regenerated"), Encode.forHtml(appBean.getOIDCClientId())); %>
            <%= message %>
        </p>
        <% } %>

        <div style="margin-left:20px;background-color: #f4f4f4; border-left: 6px solid #cccccc;height:50px;width:90%;">
            <p style="margin:20px;padding-top:10px;display:block;"><strong><fmt:message key="note"/><font
                color="red"><fmt:message key="note.oauth.application"/></font></strong></p>
        </div>
        <table style="margin-left:20px;margin-top:25px;">
            <tr style="height: 35px;">
                <td class="leftCol-small" style="margin-top:10px;font-size: 12px;"><b><fmt:message
                    key="config.application.consumerkey"/></b></td>
                <td>
                    <input style="border: none; background: white; font-size: 14px;" type="password" size="25"
                           autocomplete="off" id="consumerKey" name="consumerKey"
                           value=<%=Encode.forHtmlAttribute(appBean.getOIDCClientId())%> readonly="readonly">
                    <span>
                        <a style="margin-top: 5px;" class="showHideBtn"
                           onclick="showHidePassword(this, 'consumerKey')">Show</a>
                    </span>
                    <span style="margin-left: 6px;float: right;">
                        <a style="margin-top: 5px;" class="showHideBtn"
                           onclick="return copyTextClick(document.getElementById('consumerKey'))"><fmt:message
                            key="button.copy"/></a>
                    </span>
                </td>
            </tr>
            <tr style="height: 35px;">
                <td class="leftCol-small" style="margin-top:10px;font-size: 12px;"><b><fmt:message
                    key="config.application.consumersecret"/></b></td>
                <td>
                    <input style="border: none; background: white;font-size: 14px;" type="password" size="25"
                           autocomplete="off" id="consumerSecret" name="consumerSecret"
                           value="<%=Encode.forHtmlAttribute(oauthConsumerSecret)%>" readonly="readonly">
                    <span>
                        <a style="margin-top: 5px;" class="showHideBtn"
                           onclick="showHidePassword(this, 'consumerSecret')">Show</a>
                    </span>
                    <span style="margin-left: 6px;float: right;">
                        <a style="margin-top: 5px;" class="showHideBtn"
                           onclick="return copyTextClick(document.getElementById('consumerSecret'))"><fmt:message
                            key="button.copy"/></a>
                    </span>
                </td>
            </tr>
        </table>
    </div>
    <div class="editor-error-warn-container">
        <br/>
        <br/>
        <div class="sectionSub">
            <table class="carbonFormTable">
                <tr>
                    <td style="width:15%" class="leftCol-med labelField"><fmt:message key='config.application.template.name'/>:<span class="required">*</span></td>
                    <td>
                        <input type="text" class="template-name" name="template-name"  white-list-patterns="^[a-zA-Z0-9\s._-]*$" autofocus/>
                        <div class="sectionHelp">
                            <fmt:message key='help.template.name'/>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td style="width:15%" class="leftCol-med labelField"><fmt:message key='config.application.template.description'/>:</td>
                    <td>
                        <textarea style="width:50%" type="text" name="template-description" class="template-description" class="text-box-big"></textarea>
                        <div class="sectionHelp">
                            <fmt:message key='help.template.desc'/>
                        </div>
                    </td>
                </tr>
                <input type="hidden" id="templateNames" name="templateNames"
                       value="<%=spTemplateNames.length() > 0 ? Encode.forHtmlAttribute(spTemplateNames.toString()) : ""%>">
            </table>
        </div>
    </div>
    <div id="createTemplateErrorMsgDialog"  title='WSO2 Carbon'>
        <div id="messagebox-error">
            <h3>
                <fmt:message key="alert.error.add.sp.template"/>
            </h3>
            <table style="margin-top:10px;">
                <%
                    for (String error : createTemplateError){
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
<script src="js/configure-sp-template-flow.js"></script>
