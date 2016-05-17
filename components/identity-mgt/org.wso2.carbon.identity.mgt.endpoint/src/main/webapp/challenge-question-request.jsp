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

<%@ page import="org.apache.commons.lang.ArrayUtils" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointConstants" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointUtil" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.serviceclient.UserInformationRecoveryClient" %>
<%@ page import="org.wso2.carbon.identity.mgt.stub.dto.ChallengeQuestionIdsDTO" %>
<%@ page import="org.wso2.carbon.identity.mgt.stub.dto.UserChallengesCollectionDTO" %>
<%@ page import="org.wso2.carbon.identity.mgt.stub.dto.UserChallengesDTO" %>

<%
    UserInformationRecoveryClient userInformationRecoveryClient = new UserInformationRecoveryClient();

    String username = IdentityManagementEndpointUtil.getStringValue(request.getAttribute("username"));
    String confirmationKey = IdentityManagementEndpointUtil.getStringValue(request.getAttribute("confirmationKey"));
    String[] questionIds;

    UserChallengesDTO[] userChallengesDTOs = null;

    if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(confirmationKey)) {

        session.setAttribute("username", username);

        if (Boolean.parseBoolean(application.getInitParameter(
                IdentityManagementEndpointConstants.ConfigConstants.PROCESS_ALL_SECURITY_QUESTIONS))) {
            UserChallengesCollectionDTO userChallengesCollectionDTO =
                    userInformationRecoveryClient.getChallengeQuestions(username, confirmationKey);
            userChallengesDTOs = userChallengesCollectionDTO.getUserChallengesDTOs();

            if (ArrayUtils.isEmpty(userChallengesDTOs)) {
                request.setAttribute("error", true);
                request.setAttribute("errorMsg",
                                     "Could not find Security Questions. Seems you have not configured them.");
                request.getRequestDispatcher("error.jsp").forward(request, response);
                return;
            }

            questionIds = new String[userChallengesDTOs.length];
            session.setAttribute("confirmationKey", userChallengesCollectionDTO.getKey());
        } else {
            ChallengeQuestionIdsDTO challengeQuestionIds =
                    userInformationRecoveryClient.getUserChallengeQuestionIds(username,
                                                                              confirmationKey);

            if (challengeQuestionIds != null) {
                questionIds = challengeQuestionIds.getIds();

                if (ArrayUtils.isEmpty(questionIds)) {
                    request.setAttribute("error", true);
                    request.setAttribute("errorMsg",
                                         "Could not find Security Questions. Seems you have not configured them.");
                    request.getRequestDispatcher("error.jsp").forward(request, response);
                    return;
                }

                session.setAttribute("confirmationKey", challengeQuestionIds.getKey());
                session.setAttribute("questionIdentifiers", questionIds);
                request.getRequestDispatcher("processsecurityquestions.do").forward(request, response);
                return;
            } else {
                request.setAttribute("error", true);
                request.setAttribute("errorMsg",
                                     "Could not find Security Questions. Seems you have not configured them.");
                request.getRequestDispatcher("error.jsp").forward(request, response);
                return;
            }
        }
    } else {
        request.setAttribute("error", true);
        request.setAttribute("errorMsg",
                             "Username or confirmation code is missing.");
        request.getRequestDispatcher("error.jsp").forward(request, response);
        return;
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
                <div class="clearfix"></div>
                <div class="boarder-all ">

                    <div class="padding-double">
                        <form method="post" action="processsecurityquestions.do" id="securityQuestionForm">
                            <%
                                int count = 0;
                                for (UserChallengesDTO userChallengesDTO : userChallengesDTOs) {
                            %>
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">
                                <label class="control-label"><%=Encode.forHtml(userChallengesDTO.getQuestion())%>
                                </label>
                            </div>
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">
                                <input name="<%=Encode.forHtmlAttribute(userChallengesDTO.getId())%>" type="text"
                                       class="form-control"
                                       tabindex="0" autocomplete="off" required/>
                            </div>
                            <%
                                    questionIds[count++] = userChallengesDTO.getId();
                                }

                                session.setAttribute("questionIdentifiers", questionIds);
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


