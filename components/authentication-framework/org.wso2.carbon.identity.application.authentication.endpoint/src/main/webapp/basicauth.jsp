<%--
  ~ Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

<%@ page import="org.apache.cxf.jaxrs.client.JAXRSClientFactory" %>
<%@ page import="org.apache.cxf.jaxrs.provider.json.JSONProvider" %>
<%@ page import="org.apache.cxf.jaxrs.client.WebClient" %>
<%@ page import="org.apache.http.HttpStatus" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.util.client.SelfUserRegistrationResource" %>
<%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.util.AuthenticationEndpointUtil" %>
<%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.util.bean.ResendCodeRequestDTO" %>
<%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.util.bean.UserDTO" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.net.URLDecoder" %>
<%@ page import="javax.ws.rs.core.Response" %>
<%@ page import="static org.wso2.carbon.identity.core.util.IdentityUtil.isSelfSignUpEPAvailable" %>
<%@ page import="static org.wso2.carbon.identity.core.util.IdentityUtil.isRecoveryEPAvailable" %>
<%@ page import="static org.wso2.carbon.identity.core.util.IdentityUtil.getServerURL" %>
<%@ page import="org.apache.commons.codec.binary.Base64" %>
<%@ page import="java.nio.charset.Charset" %>
<%@ page import="org.wso2.carbon.base.ServerConfiguration" %>
<%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.util.EndpointConfigManager" %>

<jsp:directive.include file="init-loginform-action-url.jsp"/>

<script src="libs/jquery_3.4.1/jquery-3.4.1.js"></script>
<script>
    function goBack() {
        window.history.back();
    }
    
    // Handle form submission preventing double submission.
    $(document).ready(function(){
        $.fn.preventDoubleSubmission = function() {
            $(this).on('submit',function(e){
                var $form = $(this);
                if ($form.data('submitted') === true) {
                    // Previously submitted - don't submit again.
                    e.preventDefault();
                    console.warn("Prevented a possible double submit event");
                } else {
                    e.preventDefault();
                    var userName = document.getElementById("username");
                    userName.value = userName.value.trim();
                    if(userName.value){
                        $.ajax({
                            type: "GET",
                            url: "/logincontext?sessionDataKey=" + getParameterByName("sessionDataKey") + "&relyingParty=" + getParameterByName("relyingParty") + "&tenantDomain=" + getParameterByName("tenantDomain"),
                            success: function (data) {
                                if (data && data.status == 'redirect' && data.redirectUrl && data.redirectUrl.length > 0) {
                                    window.location.href = data.redirectUrl;
                                } else {
                                    // Mark it so that the next submit can be ignored.
                                    $form.data('submitted', true);
                                    document.getElementById("loginForm").submit();
                                }
                            },
                            cache: false
                        });
                    }
                }
            });

            return this;
        };
        $('#loginForm').preventDoubleSubmission();
    });
</script>

<%!
    private static final String JAVAX_SERVLET_FORWARD_REQUEST_URI = "javax.servlet.forward.request_uri";
    private static final String JAVAX_SERVLET_FORWARD_QUERY_STRING = "javax.servlet.forward.query_string";
    private static final String UTF_8 = "UTF-8";
    private static final String TENANT_DOMAIN = "tenant-domain";
%>
<%
    String resendUsername = request.getParameter("resend_username");
    if (StringUtils.isNotBlank(resendUsername)) {

        ResendCodeRequestDTO selfRegistrationRequest = new ResendCodeRequestDTO();
        UserDTO userDTO = AuthenticationEndpointUtil.getUser(resendUsername);
        selfRegistrationRequest.setUser(userDTO);

        String path = config.getServletContext().getInitParameter(Constants.ACCOUNT_RECOVERY_REST_ENDPOINT_URL);
        String proxyContextPath = ServerConfiguration.getInstance().getFirstProperty(IdentityCoreConstants
                .PROXY_CONTEXT_PATH);
        if (proxyContextPath == null) {
            proxyContextPath = "";
        }
        String url;
        if (StringUtils.isNotBlank(EndpointConfigManager.getServerOrigin())) {
            url = EndpointConfigManager.getServerOrigin() + proxyContextPath + path;
        } else {
            url = IdentityUtil.getServerURL(path, true, false);
        }
        url = url.replace(TENANT_DOMAIN, userDTO.getTenantDomain());

        List<JSONProvider> providers = new ArrayList<JSONProvider>();
        JSONProvider jsonProvider = new JSONProvider();
        jsonProvider.setDropRootElement(true);
        jsonProvider.setIgnoreNamespaces(true);
        jsonProvider.setValidateOutput(true);
        jsonProvider.setSupportUnwrapped(true);
        providers.add(jsonProvider);

        String toEncode = EndpointConfigManager.getAppName() + ":" + String
                .valueOf(EndpointConfigManager.getAppPassword());
        byte[] encoding = Base64.encodeBase64(toEncode.getBytes());
        String authHeader = new String(encoding, Charset.defaultCharset());
        String header = "Client " + authHeader;

        SelfUserRegistrationResource selfUserRegistrationResource = JAXRSClientFactory
                .create(url, SelfUserRegistrationResource.class, providers);
        WebClient.client(selfUserRegistrationResource).header("Authorization", header);
        Response selfRegistrationResponse = selfUserRegistrationResource.regenerateCode(selfRegistrationRequest);
        if (selfRegistrationResponse != null &&  selfRegistrationResponse.getStatus() == HttpStatus.SC_CREATED) {
%>
<div class="alert alert-info">
    <%=AuthenticationEndpointUtil.i18n(resourceBundle,Constants.ACCOUNT_RESEND_SUCCESS_RESOURCE)%>
</div>
<%
} else {
%>
<div class="alert alert-danger">
    <%=AuthenticationEndpointUtil.i18n(resourceBundle,Constants.ACCOUNT_RESEND_FAIL_RESOURCE)%>
</div>
<%
        }
    }
%>

<form action="<%=loginFormActionURL%>" method="post" id="loginForm">

    <%
        if (loginFormActionURL.equals(samlssoURL) || loginFormActionURL.equals(oauth2AuthorizeURL)) {
    %>
    <input id="tocommonauth" name="tocommonauth" type="hidden" value="true">
    <%
        }
    %>

    <% if (Boolean.parseBoolean(loginFailed)) { %>
    <div class="alert alert-danger" id="error-msg"><%= AuthenticationEndpointUtil.i18n(resourceBundle, errorMessage) %>
    </div>
    <%}else if((Boolean.TRUE.toString()).equals(request.getParameter("authz_failure"))){%>
    <div class="alert alert-danger" id="error-msg">
        <%=AuthenticationEndpointUtil.i18n(resourceBundle, "unauthorized.to.login")%>
    </div>
    <%}%>

    <% if (!isIdentifierFirstLogin(inputType)) { %>
    <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">
        <label for="username"><%=AuthenticationEndpointUtil.i18n(resourceBundle, "username")%></label>
        <input id="username" name="username" type="text" class="form-control" tabindex="0" placeholder="" required
               autofocus>
    </div>
    <% } else {%>
        <input id="username" name="username" type="hidden" value="<%=username%>">
    <% }%>
    <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">
        <label for="password"><%=AuthenticationEndpointUtil.i18n(resourceBundle, "password")%></label>
        <input id="password" name="password" type="password" class="form-control" placeholder="" autocomplete="off">
    </div>
    <%
        if (reCaptchaEnabled) {
    %>
    <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">
        <div class="g-recaptcha"
             data-sitekey="<%=Encode.forHtmlContent(request.getParameter("reCaptchaKey"))%>">
        </div>
    </div>
    <%
        }
    %>
    <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">
        <div class="checkbox">
            <label>
                <input type="checkbox" id="chkRemember" name="chkRemember">
                <%=AuthenticationEndpointUtil.i18n(resourceBundle, "remember.me")%>
            </label>
        </div>
    </div>
    <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
        <input type="hidden" name="sessionDataKey" value='<%=Encode.forHtmlAttribute
            (request.getParameter("sessionDataKey"))%>'/>
    </div>
    <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 padding-double">
        <div class="alert alert-warning margin-bottom-3 padding-10" role="alert">
            <div>
                <%=AuthenticationEndpointUtil.i18n(resourceBundle, "privacy.policy.cookies.short.description")%>
                <a href="cookie_policy.do" target="policy-pane">
                    <%=AuthenticationEndpointUtil.i18n(resourceBundle, "privacy.policy.cookies")%>
                </a>
                <%=AuthenticationEndpointUtil.i18n(resourceBundle, "privacy.policy.for.more.details")%>
            </div>
        </div>
        <div class="alert alert-warning margin-none padding-10" role="alert">
            <div>
                <%=AuthenticationEndpointUtil.i18n(resourceBundle, "privacy.policy.privacy.short.description")%>
                <a href="privacy_policy.do" target="policy-pane">
                    <%=AuthenticationEndpointUtil.i18n(resourceBundle, "privacy.policy.general")%>
                </a>
            </div>
        </div>
    </div>
    <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">
        <div class="form-actions">
            <button
                    class="wr-btn grey-bg col-xs-12 col-md-12 col-lg-12 uppercase font-extra-large margin-bottom-double"
                    type="submit">
                    <%=AuthenticationEndpointUtil.i18n(resourceBundle, "login")%>
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
                String uri = (String) request.getAttribute(JAVAX_SERVLET_FORWARD_REQUEST_URI);
                String prmstr = URLDecoder.decode(((String) request.getAttribute(JAVAX_SERVLET_FORWARD_QUERY_STRING)), UTF_8);
                String urlWithoutEncoding = scheme + "://" +serverName + ":" + serverPort + uri + "?" + prmstr;
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
                <% if (!isIdentifierFirstLogin(inputType)) { %>
                    <a id="usernameRecoverLink" href="<%=getRecoverAccountUrl(identityMgtEndpointContext, urlEncodedURL, true)%>">
                        <%=AuthenticationEndpointUtil.i18n(resourceBundle, "forgot.username")%>
                    </a>
                    <%=AuthenticationEndpointUtil.i18n(resourceBundle, "forgot.username.password.or")%>
                <% } %>
                <a id="passwordRecoverLink" href="<%=getRecoverAccountUrl(identityMgtEndpointContext, urlEncodedURL, false)%>">
                    <%=AuthenticationEndpointUtil.i18n(resourceBundle, "forgot.password")%>
                </a>
                ?
            </div>
    
            <div class="form-actions">
                <% if (isIdentifierFirstLogin(inputType)) { %>
                <a id="backLink" onclick="goBack()">
                    <%=AuthenticationEndpointUtil.i18n(resourceBundle, "sign.in.different.account")%>
                </a>
                <% } %>
            </div>
        </div>
        <%
                }
                if (isSelfSignUpEPAvailable && !isIdentifierFirstLogin(inputType)) {
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
    <% if (Boolean.parseBoolean(loginFailed) && errorCode.equals(IdentityCoreConstants.USER_ACCOUNT_NOT_CONFIRMED_ERROR_CODE) && request.getParameter("resend_username") == null) { %>
        <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">
            <div class="form-actions">
                <%=AuthenticationEndpointUtil.i18n(resourceBundle, "no.confirmation.mail")%>
                <a id="registerLink"
                   href="login.do?resend_username=<%=Encode.forHtml(request.getParameter("failedUsername"))%>&<%=AuthenticationEndpointUtil.cleanErrorMessages(Encode.forJava(request.getQueryString()))%>">
                    <%=AuthenticationEndpointUtil.i18n(resourceBundle, "resend.mail")%>
                </a>
            </div>
        </div>
        <%}%>
    </div>

    <div class="clearfix"></div>
    <%!
    
        private String getRecoverAccountUrl(String identityMgtEndpointContext, String urlEncodedURL, boolean isUsernameRecovery) {
        
            return identityMgtEndpointContext + "/recoveraccountrouter.do?callback=" +
                    Encode.forHtmlAttribute(urlEncodedURL) + "&isUsernameRecovery=" + isUsernameRecovery;
        }
    
        private String getRegistrationUrl(String identityMgtEndpointContext, String urlEncodedURL) {
        
            return identityMgtEndpointContext + "/register.do?callback=" + Encode.forHtmlAttribute(urlEncodedURL);
        }
    %>
</form>
