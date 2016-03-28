<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.captcha.mgt.beans.xsd.CaptchaInfoBean" %>
<%@ page import="org.wso2.carbon.identity.core.util.IdentityUtil" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.UserInformationRecoveryClient" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
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

<%
    String errorCode = request.getParameter("errorCode");
    String failedPrevious = request.getParameter("failedPrevious");

    UserInformationRecoveryClient userInformationRecoveryClient = new UserInformationRecoveryClient();

    String captchaImagePath = null;
    String captchaImageUrl = null;
    String captchaSecretKey = null;

    try {

        CaptchaInfoBean captchaInfoBean = userInformationRecoveryClient.generateCaptcha();

        captchaImagePath = captchaInfoBean.getImagePath();
        captchaImageUrl = IdentityUtil.getServerURL(null, false, false) + "/" + captchaImagePath;
        captchaSecretKey = captchaInfoBean.getSecretKey();

        System.out.println(captchaImageUrl);

    } catch (Exception e) {
        failedPrevious = "true";
        errorCode = e.getMessage();
    }
%>
<fmt:bundle basename="org.wso2.carbon.identity.application.authentication.endpoint.i18n.Resources">
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

                    <% if (failedPrevious != null && failedPrevious.equals("true")) { %>
                    <div class="alert alert-danger" id="server-error-msg">
                        <%= Encode.forHtmlContent(errorCode) %>
                    </div>
                    <% } %>

                    <form method="post" action="captcha-verify.jsp" id="recoverDetailsForm">
                        <div class="padding-double font-large">Enter below details to recover your password</div>
                        <div class="padding-double">
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group required">
                                <label class="control-label">Username</label>
                                <input id="username" name="username" type="text" class="form-control required usrName usrNameLength" required>
                            </div>
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group required">
                                <label class="control-label">Captcha</label>
                                <img src="<%=captchaImageUrl%>" alt='If you can not see the captcha " +
                        "image please refresh the page or click the link again.'/>
                            </div>
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group required">
                                <label class="control-label">Enter Captcha Text</label>
                                <input id="captchaAnswer" name="captchaAnswer" type="text" class="form-control required" required>
                            </div>
                        </div>
                        <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">
                            <input type="hidden" name="captchaSecretKey" value="<%=captchaSecretKey%>"/>
                            <input type="hidden" name="captchaImagePath" value="<%=captchaImagePath%>"/>
                            <input type="hidden" name="sessionDataKey" value='<%=Encode.forHtmlAttribute(request.getParameter("sessionDataKey"))%>'/>
                        </div>

                        <div class="form-actions">
                            <table width="100%" class="styledLeft">
                                <tbody>
                                <tr class="buttonRow">
                                    <td><input class="btn btn-primary" type="submit" value="Submit"/>
                                        <input type="button" class="btn btn-primary" value="Cancel"
                                               onclick="javascript:location.href='../dashboard/index.jag'"/></td>
                                </tr>
                                </tbody>
                            </table>
                        </div>
                    </form>
                </div>
            </div>
            <!-- /content/body -->

        </div>

        <!-- footer -->
        <footer class="footer" style="position: relative">
            <div class="container-fluid">
                <p>WSO2 Identity Server | &copy;
                    <script>document.write(new Date().getFullYear());</script>
                    <a href="http://wso2.com/" target="_blank"><i class="icon fw fw-wso2"></i> Inc</a>. All Rights
                    Reserved.
                </p>
            </div>
        </footer>

        <script src="libs/jquery_1.11.3/jquery-1.11.3.js"></script>
        <script src="libs/bootstrap_3.3.5/js/bootstrap.min.js"></script>

        <script type="text/javascript">

        </script>
    </body>
    </html>
</fmt:bundle>
