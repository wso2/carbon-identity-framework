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

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="e" uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" %>
<jsp:directive.include file="../init-url.jsp"/>
<%@include file="../localize.jsp" %>
<%@ page import="static org.wso2.carbon.identity.core.util.IdentityUtil.isSelfSignUpEPAvailable" %>
<%@ page import="static org.wso2.carbon.identity.core.util.IdentityUtil.isRecoveryEPAvailable" %>
<%@ page import="static org.wso2.carbon.identity.core.util.IdentityUtil.getServerURL" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="org.owasp.encoder.Encode" %>

<%!
    private static final String UTF_8 = "UTF-8";
%>
<div>
    <h2 class="wr-title uppercase blue-bg padding-double white boarder-bottom-blue margin-none">
        <%=AuthenticationEndpointUtil.i18n(resourceBundle, "login")%>
    </h2>
</div>

<div class="boarder-all ">
    <div class="clearfix"></div>
    <div class="padding-double login-form">
        
        <form action="<%=commonauthURL%>" method="POST">
    
            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group required">
                <label for="username" class="control-label"><%=AuthenticationEndpointUtil.i18n(resourceBundle, "username")%></label>
                <input type="text" id="username" name="username" class="form-control" tabindex="0" placeholder="" required>
            </div>
            <input type="hidden" id="promptResp" name="promptResp" value="true">
            <input type="hidden" id="promptId" name="promptId" value="${requestScope.promptId}">

            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">
                <div class="form-actions">
                    <button
                            class="wr-btn grey-bg col-xs-12 col-md-12 col-lg-12 uppercase font-extra-large margin-bottom-double"
                            type="submit">
                        <%=AuthenticationEndpointUtil.i18n(resourceBundle, "next")%>
                    </button>
                </div>
            </div>
            <%
                String recoveryEPAvailable = application.getInitParameter("EnableRecoveryEndpoint");
                String enableSelfSignUpEndpoint = application.getInitParameter("EnableSelfSignUpEndpoint");
                Boolean isRecoveryEPAvailable;
                Boolean isSelfSignUpEPAvailable;
        
                if (StringUtils.isNotBlank(recoveryEPAvailable)) {
                    isRecoveryEPAvailable = Boolean.valueOf(recoveryEPAvailable);
                } else {
                    isRecoveryEPAvailable = isRecoveryEPAvailable();
                }
        
                if (StringUtils.isNotBlank(enableSelfSignUpEndpoint)) {
                    isSelfSignUpEPAvailable = Boolean.valueOf(enableSelfSignUpEndpoint);
                } else {
                    isSelfSignUpEPAvailable = isSelfSignUpEPAvailable();
                }
        
                if (isRecoveryEPAvailable || isSelfSignUpEPAvailable) {
                    String scheme = request.getScheme();
                    String serverName = request.getServerName();
                    int serverPort = request.getServerPort();
                    String urlWithoutEncoding = request.getRequestURL().append("?").append(request.getQueryString()).toString();
                    String urlEncodedURL = URLEncoder.encode(urlWithoutEncoding, UTF_8);
            
                    String identityMgtEndpointContext =
                            application.getInitParameter("IdentityManagementEndpointContextURL");
                    if (StringUtils.isBlank(identityMgtEndpointContext)) {
                        identityMgtEndpointContext = getServerURL("/accountrecoveryendpoint", true, true);
                    }
            
                    if (isRecoveryEPAvailable) {
            %>
            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">
                <div class="form-actions">
                    <%=AuthenticationEndpointUtil.i18n(resourceBundle, "forgot.username.password")%>
                    <a id="usernameRecoverLink" href="<%=getRecoverUsernameUrl(identityMgtEndpointContext, urlEncodedURL)%>">
                        <%=AuthenticationEndpointUtil.i18n(resourceBundle, "forgot.username")%>
                    </a>
                    ?
                </div>
            </div>
            <%
                }
                if (isSelfSignUpEPAvailable) {
            %>
            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">
                <div class="form-actions">
                    <%=AuthenticationEndpointUtil.i18n(resourceBundle, "no.account")%>
                    <a id="registerLink" href="<%=getRegistrationUrl(identityMgtEndpointContext, urlEncodedURL)%>">
                        <%=AuthenticationEndpointUtil.i18n(resourceBundle, "register.now")%>
                    </a>
                </div>
            </div>
            <%
                    }
                }
            %>
        </form>
        
        <div class="clearfix"></div>
    </div>
</div>

<%!
    private String getRecoverUsernameUrl(String identityMgtEndpointContext, String urlEncodedURL) {
        return identityMgtEndpointContext + "/recoverusername.do?callback=" + Encode.forHtmlAttribute(urlEncodedURL);
    }
    private String getRegistrationUrl(String identityMgtEndpointContext, String urlEncodedURL) {
        return identityMgtEndpointContext + "/register.do?callback=" + Encode.forHtmlAttribute(urlEncodedURL);
    }
%>
<!-- /content -->
