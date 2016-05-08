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

<%@ page import="org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointUtil" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.serviceclient.UserIdentityManagementAdminServiceClient" %>
<%@ page import="org.wso2.carbon.identity.mgt.stub.dto.UserChallengesDTO" %>
<%@ page import="java.util.Set" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointConstants" %>

<%
    if (session.getAttribute("username") != null) {
        boolean skip = Boolean.parseBoolean(request.getParameter("skip"));
        if (!skip) {
            UserIdentityManagementAdminServiceClient userIdentityManagementAdminServiceClient = new
                    UserIdentityManagementAdminServiceClient();
            Set<String> challengeQuestionSet =
                    (Set<String>) session.getAttribute("challengeQuestionSet");
            if (challengeQuestionSet != null) {
                UserChallengesDTO[] userChallengesDTOs = new UserChallengesDTO[challengeQuestionSet.size()];

                int count = 0;
                for (String setId : challengeQuestionSet) {
                    String question = request.getParameter("Q-" + setId);
                    String answer = request.getParameter("A-" + setId);

                    if (StringUtils.isBlank(answer)) {
                        request.setAttribute("error", true);
                        request.setAttribute("errorMsg",
                                             "Please answer each question. None of the answers can be empty.");
                        request.getRequestDispatcher("challenge-question-add.jsp").forward(request, response);
                        return;
                    }

                    UserChallengesDTO userChallengesDTO = new UserChallengesDTO();
                    userChallengesDTO.setId(setId);
                    userChallengesDTO.setQuestion(question);
                    userChallengesDTO.setAnswer(answer);
                    userChallengesDTOs[count++] = userChallengesDTO;
                }
                userIdentityManagementAdminServiceClient.setChallengeQuestionsOfUser(
                        IdentityManagementEndpointUtil.getStringValue(session.getAttribute("username")),
                        userChallengesDTOs);
            }
        }
    } else {
        request.setAttribute("error", true);
        request.setAttribute("errorMsg", "Registered user not found in session.");
        request.getRequestDispatcher("error.jsp").forward(request, response);
    }

    session.invalidate();
%>
<html>
<head>
    <link href="libs/bootstrap_3.3.5/css/bootstrap.min.css" rel="stylesheet">
    <link href="css/Roboto.css" rel="stylesheet">
    <link href="css/custom-common.css" rel="stylesheet">
</head>
<body>
<div class="container">
    <div id="infoModel" class="modal fade" role="dialog">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal">&times;</button>
                    <h4 class="modal-title">Information</h4>
                </div>
                <div class="modal-body">
                    <p>User details successfully submitted</p>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
                </div>
            </div>
        </div>
    </div>
</div>
<script src="libs/jquery_1.11.3/jquery-1.11.3.js"></script>
<script src="libs/bootstrap_3.3.5/js/bootstrap.min.js"></script>
<script type="application/javascript">
    $(document).ready(function () {
        var infoModel = $("#infoModel");
        infoModel.modal("show");
        infoModel.on('hidden.bs.modal', function () {
            location.href = "<%=IdentityManagementEndpointUtil.getUserPortalUrl(
                application.getInitParameter(IdentityManagementEndpointConstants.ConfigConstants.USER_PORTAL_URL))%>";
        })
    });
</script>
</body>
</html>
