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

<%@ page import="java.nio.charset.StandardCharsets" %>
<%@ page import="java.util.ResourceBundle" %>
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
    // Extract the challenge questions from the request and add them into an array
    String[] questionSets = null;
    if (urlData != null) {
        questionSets = urlData.split("&");
    }
    // Hash-map to hold available challenge questions in the system
    Map<String, List<ChallengeQuestion>> challengeQuestionMap = new HashMap<>();

    for (String question : questionSets) {
        String[] questionProperties = question.split("\\|");
        // Construct a new ChallengeQuestion for each challenge question received from the request
        ChallengeQuestion tempChallengeQuestion = new ChallengeQuestion();
        // Extract the challenge question properties
        String questionSetId = questionProperties[0];
        String questionId = questionProperties[1];
        String questionBody = questionProperties[2];
        tempChallengeQuestion.setQuestionSetId(questionSetId);
        tempChallengeQuestion.setQuestionId(questionId);
        tempChallengeQuestion.setQuestion(questionBody);
        // Add the challenge question to the Hash-map
        List<ChallengeQuestion> challengeQuestionList = challengeQuestionMap.get(questionSetId);
        if (challengeQuestionList == null) {
            challengeQuestionList = new ArrayList<>();
            challengeQuestionList.add(tempChallengeQuestion);
            challengeQuestionMap.put(questionSetId, challengeQuestionList);
        } else {
            challengeQuestionList.add(tempChallengeQuestion);
        }
    }
%>

<!DOCTYPE html>
<html lang="en">
<html>
<head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
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
        for (String challengeQuestionSet : challengeQuestionMap.keySet()) {
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
                    <select class="form-control" id="challengeQuestion1"
                            name=<%="Q-" + Encode.forHtmlAttribute(challengeQuestionSet)%>>
                        <%
                            for (ChallengeQuestion challengeQuestion : challengeQuestionMap.get(challengeQuestionSet)) {
                        %>
                        <option name="q" selected="selected" value="<%=Encode.forHtmlAttribute(challengeQuestion.getQuestion())%>">
                            <%=Encode.forHtmlContent(challengeQuestion.getQuestion())%>
                        </option>
                        <%
                            }
                        %>
                    </select>
                </div>
            </div>
            <div class="form-group">
                <label class="control-label col-sm-4"><%=AuthenticationEndpointUtil.i18n(resourceBundle,
                 "answers.challenge.question")%>
                </label>
                <div class="col-sm-8">
                    <input required type="text" class="form-control" id="answer_to_questions"
                           placeholder="<%=AuthenticationEndpointUtil.i18n(resourceBundle, "type.your.challenge.answer")%>"
                           name=<%="A-" + Encode.forHtmlAttribute(challengeQuestionSet)%>>
                </div>
            </div>
        </dev>
        <%
            }
        %>
        <br>
        <label class="control-label col-sm-4"></label>
        <div class="col-sm-8">
            <input type="submit" class="btn btn-primary"
                   value="<%=AuthenticationEndpointUtil.i18n(resourceBundle, "update")%>">
        </div>
        <input type="hidden" name="<%="sessionDataKey"%>"
               value="<%=Encode.forHtmlAttribute(request.getParameter("sessionDataKey"))%>"/>

    </form>
</div>
</body>
</html>
