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
<%@ page import="org.wso2.carbon.identity.mgt.beans.User" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointConstants" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointUtil" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.IdentityManagementServiceUtil" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.serviceclient.PasswordRecoverySecurityQuestionClient" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.serviceclient.beans.ChallengeQuestionsResponse" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.serviceclient.beans.ErrorResponse" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.serviceclient.model.ChallengeQuestion" %>
<%@ page import="javax.ws.rs.core.Response" %>
<%@ page import="org.apache.cxf.jaxrs.impl.ResponseImpl" %>

<%
    String username = IdentityManagementEndpointUtil.getStringValue(request.getAttribute("username"));
    PasswordRecoverySecurityQuestionClient pwRecoverySecurityQuestionClient = new PasswordRecoverySecurityQuestionClient();
    ErrorResponse errorResponse = (ErrorResponse)request.getAttribute("errorResponse");
    ChallengeQuestion[] challengeQuestions = null;

    if(errorResponse != null) {
        username = ((User)session.getAttribute("user")).getUserName();
    }
    if (StringUtils.isNotBlank(username)) {
        if (Boolean.parseBoolean(application.getInitParameter(
                IdentityManagementEndpointConstants.ConfigConstants.PROCESS_ALL_SECURITY_QUESTIONS))) {
            User user = IdentityManagementServiceUtil.getInstance().getUser(username);
            session.setAttribute("user", user);
            Response responseJAXRS = pwRecoverySecurityQuestionClient.initiateUserChallengeQuestionAtOnce(user);

            int statusCode = responseJAXRS.getStatus();
            if(Response.Status.OK.getStatusCode() == statusCode) {
                ChallengeQuestionsResponse challengeQuestionsResponse = responseJAXRS.readEntity(ChallengeQuestionsResponse.class);
                session.setAttribute("challengeQuestionsResponse", challengeQuestionsResponse);
                challengeQuestions = challengeQuestionsResponse.getQuestion();
                if(((ResponseImpl)responseJAXRS).getHeaders().containsKey("reCaptcha") &&
                        Boolean.parseBoolean((String) ((ResponseImpl)responseJAXRS).getHeaders().get("reCaptcha").get(0))) {
                    request.setAttribute("reCaptcha", "true");
                    request.setAttribute("reCaptchaKey", ((ResponseImpl)responseJAXRS).getHeaders().get("reCaptchaKey").get(0));
                    request.setAttribute("reCaptchaAPI", ((ResponseImpl)responseJAXRS).getHeaders().get("reCaptchaAPI").get(0));
                }
            } else if (Response.Status.BAD_REQUEST.getStatusCode() == statusCode || Response.Status.INTERNAL_SERVER_ERROR.getStatusCode() == statusCode) {
                ErrorResponse errorResponseFetchChallengeQuestions = responseJAXRS.readEntity(ErrorResponse.class);
                request.setAttribute("error", true);
                request.setAttribute("errorMsg", errorResponseFetchChallengeQuestions.getMessage());
                request.getRequestDispatcher("error.jsp").forward(request, response);
                return;
            }
        } else {
            request.getRequestDispatcher("challenge-question-process.jsp?username=" + username).forward(request,
                            response);
        }
    } else {
        request.setAttribute("error", true);
        request.setAttribute("errorMsg", "Username is missing.");
        request.getRequestDispatcher("error.jsp").forward(request, response);
        return;
    }

    boolean reCpatchaEnabled = false;
    if (request.getAttribute("reCaptcha") != null && "TRUE".equalsIgnoreCase((String) request.getAttribute("reCaptcha"))) {
        reCpatchaEnabled = true;
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

        <%
            if (reCpatchaEnabled) {
        %>
        <script src='<%=(request.getAttribute("reCaptchaAPI"))%>'></script>
        <%
            }
        %>
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
             <%
                if(errorResponse != null) {
             %>
                <div class="alert alert-danger" id="server-error-msg">
                    <%=errorResponse.getMessage()%>
                </div>
             <%
                }
             %>
                <div class="clearfix"></div>
                <div class="boarder-all ">

                    <div class="padding-double">
                        <form method="post" action="processsecurityquestions.do" id="securityQuestionForm">
                            <%
                                int count = 0;
                                if (challengeQuestions != null) {
                                    for (ChallengeQuestion challengeQuestion : challengeQuestions) {
                            %>
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">
                                <label class="control-label"><%=Encode.forHtml(challengeQuestion.getQuestion())%>
                                </label>
                            </div>
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">
                                <input name="<%=Encode.forHtmlAttribute(challengeQuestion.getQuestionSetId())%>" type="text"
                                       class="form-control"
                                       tabindex="0" autocomplete="off" required/>
                            </div>
                            <%
                                    }
                                }
                            %>
                            <%
                                if (reCpatchaEnabled) {
                            %>
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">
                                <div class="g-recaptcha"
                                     data-sitekey="<%=Encode.forHtmlContent((String)request.getAttribute("reCaptchaKey"))%>">
                                </div>
                            </div>
                            <%
                                }
                            %>
                            <div class="form-actions">
                                <button id="answerSubmit"
                                        class="wr-btn grey-bg col-xs-12 col-md-12 col-lg-12 uppercase font-extra-large"
                                        type="submit">Submit
                                </button>
                            </div>
                            <div class="clearfix"></div>
                        </form>
                    </div>
                </div>
            </div>
            <!-- /content/body -->

        </div>
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
    </body>
    </html>
</fmt:bundle>


