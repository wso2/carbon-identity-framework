<!--
~ Copyright (c) 2019, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 LLC. licenses this file to you under the Apache License, Version 2.0 (the "License");
~ you may not use this file except in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing, software
~ distributed under the License is distributed on an "AS IS" BASIS,
~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
~ See the License for the specific language governing permissions and
~ limitations under the License.
-->

<%@page import="org.apache.commons.lang.StringUtils" %>
<%@page import="org.owasp.encoder.Encode" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" %>
<%@ taglib uri="http://www.owasp.org/index.php/Category:OWASP_CSRFGuard_Project/Owasp.CsrfGuard.tld" prefix="csrf" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.idp.xsd.FederatedAuthenticatorConfig" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.idp.xsd.Property" %>
<%@ page import="org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants.Authenticator.OIDC" %>
<%@ page import="org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants" %>
<%@ page import="org.wso2.carbon.identity.core.util.IdentityUtil" %>
<%@ page import="org.wso2.carbon.idp.mgt.ui.util.IdPManagementUIUtil" %>
<%@ page import="org.wso2.carbon.identity.core.ServiceURLBuilder" %>
<%@ page import="org.wso2.carbon.identity.core.URLBuilderException" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.UUID" %>

<link href="css/idpmgt.css" rel="stylesheet" type="text/css" media="all"/>
<carbon:breadcrumb label="identity.providers" resourceBundle="org.wso2.carbon.idp.mgt.ui.i18n.Resources"
                   topPage="true" request="<%=request%>"/>
<jsp:include page="../dialog/display_messages.jsp"/>

<%
    boolean isOpenidconnectAuthenticatorActive = Boolean.parseBoolean(request.getParameter(
            "isOpenidconnectAuthenticatorActive"));
    boolean isOIDCEnabled = Boolean.parseBoolean(request.getParameter("isOIDCEnabled"));
    boolean isOIDCDefault = Boolean.parseBoolean(request.getParameter("isOIDCDefault"));
    boolean isOIDCBasicAuthEnabled = false;
    String clientId = null;
    String clientSecret = null;
    String authzUrl = null;
    String tokenUrl = null;
    String callBackUrl = null;
    String userInfoEndpoint = null;
    String logoutUrlOIDC = null;
    boolean isOIDCUserIdInClaims = false;
    String scopes = StringUtils.EMPTY;
    String oidcQueryParam = StringUtils.EMPTY;
    
    Map<String, UUID> idpUniqueIdMap = (Map<String, UUID>)session.getAttribute(
            IdPManagementUIUtil.IDP_LIST_UNIQUE_ID);
    
    String idPName = request.getParameter("idPName");
    if (idPName != null && idPName.equals("")) {
        idPName = null;
    }
    
    IdentityProvider identityProvider = null;
    
    if (idPName != null && idpUniqueIdMap.get(idPName) != null) {
        identityProvider = (IdentityProvider) session.getAttribute(idpUniqueIdMap.get(idPName).toString());
    }
    
    if (idPName != null && identityProvider != null) {
        FederatedAuthenticatorConfig[] fedAuthnConfigs = identityProvider.getFederatedAuthenticatorConfigs();
        
        if (fedAuthnConfigs != null && fedAuthnConfigs.length > 0) {
            for (FederatedAuthenticatorConfig fedAuthnConfig : fedAuthnConfigs) {
                if (fedAuthnConfig.getProperties() == null) {
                    fedAuthnConfig.setProperties(new Property[0]);
                }
                if (fedAuthnConfig.getDisplayName().equals(IdentityApplicationConstants.Authenticator.OIDC.NAME)) {
                    Property authzUrlProp = IdPManagementUIUtil.getProperty(fedAuthnConfig.getProperties(),
                            OIDC.OAUTH2_AUTHZ_URL);
                    if (authzUrlProp != null) {
                        authzUrl = authzUrlProp.getValue();
                    }
                    Property tokenUrlProp = IdPManagementUIUtil.getProperty(fedAuthnConfig.getProperties(),
                            OIDC.OAUTH2_TOKEN_URL);
                    if (tokenUrlProp != null) {
                        tokenUrl = tokenUrlProp.getValue();
                    }
                    Property callBackURLProp = IdPManagementUIUtil.getProperty(fedAuthnConfig.getProperties(),
                            OIDC.CALLBACK_URL);
                    if (callBackURLProp != null) {
                        callBackUrl = callBackURLProp.getValue();
                    }
                    Property userInfoEndpointProp = IdPManagementUIUtil.getProperty(fedAuthnConfig.getProperties(),
                            OIDC.USER_INFO_URL);
                    if (userInfoEndpointProp != null) {
                        userInfoEndpoint = userInfoEndpointProp.getValue();
                    }
                    Property logoutUrlProp = IdPManagementUIUtil.getProperty(fedAuthnConfig.getProperties(),
                            IdentityApplicationConstants.Authenticator.OIDC.OIDC_LOGOUT_URL);
                    if (logoutUrlProp != null) {
                        logoutUrlOIDC = logoutUrlProp.getValue();
                    }
                    Property clientIdProp = IdPManagementUIUtil.getProperty(fedAuthnConfig.getProperties(),
                            OIDC.CLIENT_ID);
                    if (clientIdProp != null) {
                        clientId = clientIdProp.getValue();
                    }
                    Property clientSecretProp = IdPManagementUIUtil.getProperty(fedAuthnConfig.getProperties(),
                            OIDC.CLIENT_SECRET);
                    if (clientSecretProp != null) {
                        clientSecret = clientSecretProp.getValue();
                    }
                    Property isOIDCUserIdInClaimsProp = IdPManagementUIUtil.getProperty(fedAuthnConfig.getProperties(),
                            OIDC.IS_USER_ID_IN_CLAIMS);
                    if (isOIDCUserIdInClaimsProp != null) {
                        isOIDCUserIdInClaims = Boolean.parseBoolean(isOIDCUserIdInClaimsProp.getValue());
                    }
                    Property scopesProp = IdPManagementUIUtil.getProperty(fedAuthnConfig.getProperties(),
                             OIDC.SCOPES);
                    if (scopesProp != null) {
                        scopes = scopesProp.getValue();
                    }
                    Property queryParamProp = IdPManagementUIUtil.getProperty(fedAuthnConfig.getProperties(),
                            OIDC.QUERY_PARAMS);
                    if (queryParamProp != null) {
                        oidcQueryParam = queryParamProp.getValue();
                    }
                    Property basicAuthEnabledProp = IdPManagementUIUtil.getProperty(fedAuthnConfig.getProperties(),
                            OIDC.IS_BASIC_AUTH_ENABLED);
                    if (basicAuthEnabledProp != null) {
                        isOIDCBasicAuthEnabled = Boolean.parseBoolean(basicAuthEnabledProp.getValue());
                    }
                }
            }
        }
    }
    
    String oidcEnabledChecked = StringUtils.EMPTY;
    String oidcDefaultDisabled = StringUtils.EMPTY;
    if (identityProvider != null) {
        if (isOIDCEnabled) {
            oidcEnabledChecked = "checked=\'checked\'";
        } else {
            oidcDefaultDisabled = "disabled=\'disabled\'";
        }
    }
    String oidcDefaultChecked = StringUtils.EMPTY;
    if (identityProvider != null) {
        if (isOIDCDefault) {
            oidcDefaultChecked = "checked=\'checked\'";
            oidcDefaultDisabled = "disabled=\'disabled\'";
        }
    }
    if (clientId == null) {
        clientId = StringUtils.EMPTY;
    }
    if (clientSecret == null) {
        clientSecret = StringUtils.EMPTY;
    }
    if (StringUtils.isBlank(authzUrl)) {
        authzUrl = StringUtils.EMPTY;
    }
    if (StringUtils.isBlank(tokenUrl)) {
        tokenUrl = StringUtils.EMPTY;
    }
    if (StringUtils.isBlank(callBackUrl)) {
        try {
            callBackUrl = ServiceURLBuilder.create().addPath(IdentityApplicationConstants.COMMONAUTH).build().getAbsolutePublicURL();
        } catch(URLBuilderException e) {
            throw new RuntimeException("Error occurred while building URL in tenant qualified mode.", e);
        }
    }
    if (StringUtils.isBlank(userInfoEndpoint)) {
        userInfoEndpoint = StringUtils.EMPTY;
    }
    if (StringUtils.isBlank(logoutUrlOIDC)) {
        logoutUrlOIDC = StringUtils.EMPTY;
    }
    String oidcBasicAuthEnabledChecked = StringUtils.EMPTY;
    if (isOIDCBasicAuthEnabled) {
        oidcBasicAuthEnabledChecked = "checked=\'checked\'";
    }
    if (scopes == null) {
        scopes = StringUtils.EMPTY;
    }
    if (oidcQueryParam == null) {
        oidcQueryParam = StringUtils.EMPTY;
    }
    if (StringUtils.isBlank(scopes) && !oidcQueryParam.toLowerCase().contains("scope=")) {
       scopes = "openid";
    }
    
%>
<fmt:bundle basename="org.wso2.carbon.idp.mgt.ui.i18n.Resources">

    <% if (isOpenidconnectAuthenticatorActive) { %>

    <h2 id="oauth2_head" class="sectionSeperator trigger active" style="background-color: beige;">
        <a href="#"><fmt:message key="oidc.config"/></a>

        <div id="oAuth2_enable_logo" class="enablelogo"
             style="float:right;padding-right: 5px;padding-top: 5px;"><img
                src="images/ok.png" alt="enable" width="16" height="16"></div>
    </h2>
    <div class="toggle_container sectionSub" style="margin-bottom:10px;" id="oauth2LinkRow">
        <table class="carbonFormTable">
            <tr>
                <td class="leftCol-med labelField">
                    <label for="oidcEnabled"><fmt:message key='oidc.enabled'/></label>
                </td>
                <td>
                    <div class="sectionCheckbox">
                        <input id="oidcEnabled" name="oidcEnabled"
                               type="checkbox" <%=oidcEnabledChecked%>
                               onclick="checkEnabled(this);"/>
                        <span style="display:inline-block" class="sectionHelp">
                                    <fmt:message key='oidc.enabled.help'/>
                        </span>
                    </div>
                </td>
            </tr>
            <tr>
                <td class="leftCol-med labelField">
                    <label for="oidcDefault"><fmt:message key='oidc.default'/></label>
                </td>
                <td>
                    <div class="sectionCheckbox">
                        <input id="oidcDefault" name="oidcDefault"
                               type="checkbox" <%=oidcDefaultChecked%> <%=oidcDefaultDisabled%>
                               onclick="checkDefault(this);"/>
                        <span style="display:inline-block" class="sectionHelp">
                                    <fmt:message key='oidc.default.help'/>
                        </span>
                    </div>
                </td>
            </tr>
            <tr>
                <td class="leftCol-med labelField"><fmt:message key='client.id'/>:<span
                        class="required">*</span></td>
                <td>
                    <input id="clientId" name="clientId" type="text"
                           value=<%=Encode.forHtmlAttribute(clientId)%>>

                    <div class="sectionHelp">
                        <fmt:message key='client.id.help'/>
                    </div>
                </td>
            </tr>
            <tr>
                <td class="leftCol-med labelField"><fmt:message key='client.secret'/>:<span
                        class="required">*</span></td>
                <td>
                    <div id="showHideButtonDivIdOauth" style="border:1px solid rgb(88, 105, 125);"
                         class="leftCol-med">
                        <input id="clientSecret" name="clientSecret" type="password"
                               autocomplete="new-password" value="<%=Encode.forHtmlAttribute(clientSecret)%>"
                               style="  outline: none; border: none; min-width: 175px; max-width: 180px;"/>
                        <span id="showHideButtonIdOauth" style=" float: right; padding-right: 5px;">
                            <a style="margin-top: 5px;" class="showHideBtn"
                               onclick="showHidePassword(this, 'clientSecret')">Show</a>
                        </span>
                    </div>
                    <div class="sectionHelp">
                        <fmt:message key='client.secret.help'/>
                    </div>
                </td>
            </tr>
            <tr>
                <td class="leftCol-med labelField"><fmt:message key='authz.endpoint'/>:<span
                        class="required">*</span></td>
                <td>
                    <input id="authzUrl" name="authzUrl" type="text"
                           value=<%=Encode.forHtmlAttribute(authzUrl)%>>

                    <div class="sectionHelp">
                        <fmt:message key='authz.endpoint.help'/>
                    </div>
                </td>
            </tr>
            <tr>
                <td class="leftCol-med labelField"><fmt:message key='token.endpoint'/>:<span
                        class="required">*</span></td>
                <td>
                    <input id="tokenUrl" name="tokenUrl" type="text"
                           value=<%=Encode.forHtmlAttribute(tokenUrl)%>>

                    <div class="sectionHelp">
                        <fmt:message key='token.endpoint.help'/>
                    </div>
                </td>
            </tr>
            <tr>
                <td class="leftCol-med labelField"><fmt:message key='callbackurl'/>
                <td>
                    <input id="callbackUrl" name="callbackUrl" type="text"
                           value=<%=Encode.forHtmlAttribute(callBackUrl)%>>

                    <div class="sectionHelp">
                        <fmt:message key='callbackUrl.help'/>
                    </div>
                </td>
            </tr>
            <tr>
                <td class="leftCol-med labelField"><fmt:message key='userInfoEndpoint'/>
                <td>
                    <input id="userInfoEndpoint" name="userInfoEndpoint" type="text"
                           value=<%=Encode.forHtmlAttribute(userInfoEndpoint)%>>

                    <div class="sectionHelp">
                        <fmt:message key='userInfoEndpoint.help'/>
                    </div>
                </td>
            </tr>
             <tr>
                <td class="leftCol-med labelField"><fmt:message key='logout.endpoint'/>
                <td>
                    <input id="logoutUrlOIDC" name="logoutUrlOIDC" type="text"
                           value=<%=Encode.forHtmlAttribute(logoutUrlOIDC)%>>
            
                    <div class="sectionHelp">
                        <fmt:message key='logout.endpoint.help'/>
                    </div>
                </td>
            </tr>
            <tr>
                <td class="leftCol-med labelField"><fmt:message key='oidc.user.id.location'/>:</td>
                <td>
                    <label>
                        <input type="radio" value="0"
                               name="oidc_user_id_location" <% if (!isOIDCUserIdInClaims) { %>
                               checked="checked" <%}%> />
                        User ID found in 'sub' attribute
                    </label>
                    <label>
                        <input type="radio" value="1"
                               name="oidc_user_id_location" <% if (isOIDCUserIdInClaims) { %>
                               checked="checked" <%}%> />
                        User ID found among claims
                    </label>

                    <div class="sectionHelp">
                        <fmt:message key='oidc.user.id.location.help'/>
                    </div>
                </td>
            </tr>
            <tr>
                <td class="leftCol-med labelField"><fmt:message key='scopes'/>:</td>
                <td>
                    <input id="scopes" name="scopes" type="text"
                           value="<%=Encode.forHtmlAttribute(scopes)%>">

                    <div class="sectionHelp">
                        <fmt:message key='scopes.help'/>
                    </div>
                </td>
            </tr>
            <tr>
                <td class="leftCol-med labelField"><fmt:message key='query.param'/>:</td>
                <td>
                    <input id="oidcQueryParam" name="oidcQueryParam" type="text"
                           value="<%=Encode.forHtmlAttribute(oidcQueryParam)%>">

                    <div class="sectionHelp">
                        <fmt:message key='query.param.help'/>
                    </div>
                </td>
            </tr>
            <tr>
                <td class="leftCol-med labelField"><fmt:message key='oidc.enable.basicauth'/>:</td>
                <td>
                    <div class="sectionCheckbox">
                        <input id="oidcBasicAuthEnabled" name="oidcBasicAuthEnabled"
                               type="checkbox" <%=oidcBasicAuthEnabledChecked%> />
                        <span style="display:inline-block" class="sectionHelp">
                                    <fmt:message key='oidc.enable.basicauth.help'/>
                        </span>
                    </div>
                </td>
            </tr>
        </table>
    </div>

    <% } %>
</fmt:bundle>