<%--
  ~ Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~  WSO2 Inc. licenses this file to you under the Apache License,
  ~  Version 2.0 (the "License"); you may not use this file except
  ~  in compliance with the License.
  ~  You may obtain a copy of the License at
  ~
  ~    http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.captcha.mgt.beans.xsd.CaptchaInfoBean" %>
<%@ page import="org.wso2.carbon.identity.core.util.IdentityUtil" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointConstants" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointUtil" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.serviceclient.UserInformationRecoveryClient" %>

<%
    boolean error = IdentityManagementEndpointUtil.getBooleanValue(request.getAttribute("error"));
    String errorMsg = IdentityManagementEndpointUtil.getStringValue(request.getAttribute("errorMsg"));

    boolean isEmailNotificationEnabled = false;
    boolean isPasswordRecoveryEmailConfirmation = false;

    String username = IdentityManagementEndpointUtil.getStringValue(request.getAttribute("username"));
    String confirmationKey = IdentityManagementEndpointUtil.getStringValue(request.getAttribute("confirmationKey"));

    if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(confirmationKey)) {
        isPasswordRecoveryEmailConfirmation = true;
    } else {
        isEmailNotificationEnabled = Boolean.parseBoolean(application.getInitParameter(
                IdentityManagementEndpointConstants.ConfigConstants.ENABLE_EMAIL_NOTIFICATION));
    }

%>
<fmt:bundle basename="org.wso2.carbon.identity.mgt.endpoint.i18n.Resources">
    <html>
    <head>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>WSO2 Identity Server</title>

        <link rel="icon" href="images/favicon.png" type="image/x-icon"/>
        <link href="libs/bootstrap_3.3.5/css/bootstrap.min.css" rel="stylesheet">
        <link href="css/Roboto.css" rel="stylesheet">
        <link href="css/custom-common.css" rel="stylesheet">

        <!--[if lt IE 9]>
        <script src="js/html5shiv.min.js"></script>
        <script src="js/respond.min.js"></script>
        <![endif]-->
    </head>

    <body>

    <!-- header -->
    <header class="header header-default">
        <div class="container-fluid"><br></div>
        <div class="container-fluid">
            <div class="pull-left brand float-remove-xs text-center-xs">
                <a href="#">
                    <img src="images/logo-inverse.svg" alt="wso2" title="wso2" class="logo">

                    <h1><em>Identity Server</em></h1>
                </a>
            </div>
        </div>
    </header>

    <!-- page content -->
    <div class="container-fluid body-wrapper">

        <div class="row">
            <!-- content -->
            <div class="col-xs-12 col-sm-10 col-md-8 col-lg-5 col-centered wr-login">
                <h2 class="wr-title uppercase blue-bg padding-double white boarder-bottom-blue margin-none">Recover
                    Password
                </h2>

                <div class="clearfix"></div>
                <div class="boarder-all ">

                    <% if (error) { %>
                    <div class="alert alert-danger" id="server-error-msg">
                        <%= Encode.forHtmlContent(errorMsg) %>
                    </div>
                    <% } %>
                    <div class="alert alert-danger" id="error-msg" hidden="hidden"></div>

                    <div class="padding-double font-large">Enter below details to recover your password</div>
                    <div class="padding-double">
                        <form method="post" action="processsecurityquestions.do" id="recoverDetailsForm">
                            <%
                                if (isPasswordRecoveryEmailConfirmation) {
                            %>
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group required">
                                <input id="hidden-username" name="username" type="hidden" class="form-control"
                                       value="<%=Encode.forHtmlAttribute(username)%>">
                            </div>
                            <%

                            } else {
                            %>
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group required">
                                <input id="username" name="username" type="text" class="form-control" tabindex="0"
                                       placeholder="Username" required>
                            </div>
                            <%
                                }
                            %>
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">
                                <input id="confirmationKey" type="hidden" name="confirmationKey"
                                       value="<%=Encode.forHtmlAttribute(confirmationKey)%>"/>
                                <input id="isPasswordRecoveryEmailConfirmation" type="hidden"
                                       name="isPasswordRecoveryEmailConfirmation"
                                       value="<%=isPasswordRecoveryEmailConfirmation%>"/>
                            </div>
                            <%
                                if (isEmailNotificationEnabled) {
                            %>
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">
                                <input type="radio" name="recoveryOption" value="EMAIL" checked/>
                                Recover with Email
                            </div>
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">
                                <input type="radio" name="recoveryOption" value="SECURITY_QUESTIONS"/>
                                Recover with Security Questions
                            </div>
                            <%
                            } else {
                            %>
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">
                                <input type="hidden" name="recoveryOption" value="SECURITY_QUESTIONS"/>
                            </div>
                            <%
                                }
                            %>

                            <div class="form-actions">
                                <table width="100%" class="styledLeft">
                                    <tbody>
                                    <tr class="buttonRow">
                                        <td>
                                            <button id="recoverySubmit"
                                                    class="wr-btn grey-bg col-xs-12 col-md-12 col-lg-12 uppercase font-extra-large"
                                                    type="submit">Submit
                                            </button>
                                        </td>
                                        <td>&nbsp;&nbsp;</td>
                                        <td>
                                            <button id="recoveryCancel"
                                                    class="wr-btn grey-bg col-xs-12 col-md-12 col-lg-12 uppercase font-extra-large"
                                                    onclick="location.href='<%=Encode.forJavaScript(IdentityManagementEndpointUtil.getUserPortalUrl(
                                                        application.getInitParameter(IdentityManagementEndpointConstants.ConfigConstants.USER_PORTAL_URL)))%>';">
                                                Cancel
                                            </button>
                                        </td>
                                    </tr>
                                    </tbody>
                                </table>
                            </div>
                        </form>
                    </div>
                </div>
                <div class="clearfix"></div>
            </div>
            <!-- /content/body -->

        </div>
    </div>

    <!-- footer -->
    <footer class="footer">
        <div class="container-fluid">
            <p>WSO2 Identity Server | &copy;
                <script>document.write(new Date().getFullYear());</script>
                <a href="http://wso2.com/" target="_blank"><i class="icon fw fw-wso2"></i> Inc</a>. All Rights Reserved.
            </p>
        </div>
    </footer>

    <script src="libs/jquery_1.11.3/jquery-1.11.3.js"></script>
    <script src="libs/bootstrap_3.3.5/js/bootstrap.min.js"></script>
    </body>
    </html>
</fmt:bundle>
