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
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>

<%@ page import="org.owasp.encoder.Encode" %>

<%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.util.AuthenticationEndpointUtil" %>
<%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.util.EncodedControl" %>
<%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.util.Constants" %>
<%@ page import="org.wso2.carbon.identity.recovery.ui.IdentityManagementAdminClient" %>
<%@ page import="org.wso2.carbon.identity.recovery.model.ChallengeQuestion" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>

<%@ page import="java.util.ResourceBundle" %>
<%@ page import="java.nio.charset.StandardCharsets" %>

<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>

<%
    String BUNDLE = "org.wso2.carbon.identity.application.authentication.endpoint.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale(), new
            EncodedControl(StandardCharsets.UTF_8.toString()));
    String urlData = request.getParameter("data");
    // Extract the challege questions from the request and add them into an array
    String[] questionSets = urlData.split("&");
    // Hashmap to hold user's challenge questions
    Map<String, List<ChallengeQuestion>> challengeQuestionMap = new HashMap<>();

    for (String question : questionSets) {
        String[] questionProperties = question.split("\\|");
        // Construct a new ChallengeQuestion for each challenge question received from the request
        ChallengeQuestion tempChallengeQuesion = new ChallengeQuestion();
        tempChallengeQuesion.setQuestionSetId(questionProperties[0]);
        tempChallengeQuesion.setQuestionId(questionProperties[1]);
        tempChallengeQuesion.setQuestion(questionProperties[2]);
        // Add the challenge question to the Hashmap
        List<ChallengeQuestion> challengeQuestionList = challengeQuestionMap.get(questionProperties[0]);
        if (challengeQuestionList == null) {
            List<ChallengeQuestion> tempList = new ArrayList<>();
            tempList.add(tempChallengeQuesion);
            challengeQuestionMap.put(questionProperties[0], tempList);
        } else {
            challengeQuestionList.add(tempChallengeQuesion);
        }
    }
%>

<script type="text/javascript">
    function validate() {
        var isValid = new Boolean(true);
        $("#profile input").each(function () {
            if ($(this).val() == "") {
                isValid = false;
            }
        })
        if (isValid) {
            document.getElementById("profile").submit();
        }
        else {
            alert("Please enter the answers for challenge questions");
        }
    }
</script>

<!DOCTYPE html>
<html lang="en">
<html>
<head>
    <title><%=AuthenticationEndpointUtil.i18n(resourceBundle, "add.challenge.answers")%>
    </title>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
</head>

<body>
<div class="container">
    <h1><%=AuthenticationEndpointUtil.i18n(resourceBundle, "answer.following.questions")%>
    </h1>
    <%
        for (String list1 : challengeQuestionMap.keySet()) {
    %>
    <br><br>
    <form action="../commonauth" method="post" id="profile" name="">
        <dev class="form-horizontal">
            <legend><%=AuthenticationEndpointUtil.i18n(resourceBundle, "challenge.question.set")%>
            </legend>
            <div class="form-group">
                <label class="control-label col-sm-4"><%=AuthenticationEndpointUtil.i18n(resourceBundle,
                 "select.challenge.question")%>
                </label>
                <div class="col-sm-8">
                    <select class="form-control" id="qchallengeQuestion1"
                            name=<%="Q-" + Encode.forHtmlAttribute(list1)%>>
                        <%
                            for (ChallengeQuestion q1 : challengeQuestionMap.get(list1)) {
                        %>
                        <option name="q" selected="selected" value="<%=Encode.forHtmlAttribute(q1.getQuestion())%>">
                            <%=Encode.forHtmlContent(q1.getQuestion())%>
                        </option>
                        <%
                            }
                        %>
                    </select>
                </div>
            </div>
            <div class="form-group">
                <label class="control-label col-sm-4"><%=AuthenticationEndpointUtil.i18n(resourceBundle,
                 "answers.security.question")%>
                </label>
                <div class="col-sm-8">
                    <input type="text" class="form-control" id="answer_to_questions"
                           placeholder="<%=AuthenticationEndpointUtil.i18n(resourceBundle, "type.your.security.answer")%>"
                           name=<%="A-" + Encode.forHtmlAttribute(list1)%>>
                </div>
            </div>
        </dev>
        <%
            }
        %>
        <br>
        <label class="control-label col-sm-4"></label>
        <div class="col-sm-8">
            <input type="button" class="btn btn-primary"
                   value="<%=AuthenticationEndpointUtil.i18n(resourceBundle, "update")%>"
                   onclick="javascript: validate(); return false;">
        </div>
        <input type="hidden" name="<%="sessionDataKey"%>"
               value="<%=Encode.forHtmlAttribute(request.getParameter("sessionDataKey"))%>"/>

    </form>
</div>
</body>
</html>
