<!--
  ~
  ~ Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~ WSO2 Inc. licenses this file to you under the Apache License,
  ~  Version 2.0 (the "License"); you may not use this file except
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
  ~
  -->

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
	prefix="carbon"%>
<%@page import="org.apache.axis2.context.ConfigurationContext"%>
<%@page import="org.wso2.carbon.CarbonConstants"%>
<%@page import="org.wso2.carbon.identity.mgt.stub.dto.ChallengeQuestionDTO"%>
<%@page import="org.wso2.carbon.identity.mgt.stub.dto.UserChallengesDTO"%>
<%@page import="org.wso2.carbon.identity.mgt.ui.IdentityManagementAdminClient"%>
<%@page import="org.wso2.carbon.identity.mgt.ui.UserInformationRecoveryClient"%>
<script type="text/javascript" src="extensions/js/vui.js"></script>
<script type="text/javascript" src="../extensions/core/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>

<jsp:include page="../dialog/display_messages.jsp" />
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.owasp.encoder.Encode" %>

<%
    String username = request.getParameter("username");
    String forwardTo = null;
    IdentityManagementAdminClient client = null;
    UserInformationRecoveryClient infoClient = null;
    ChallengeQuestionDTO[] challenges;
    UserChallengesDTO[] userChallenges = null;
    UserChallengesDTO currentUserChallenge;
    Map<String, HashSet<ChallengeQuestionDTO>> challengesMap = new HashMap<String, HashSet<ChallengeQuestionDTO>>();
    Map<String, UserChallengesDTO> userChallengesMap = new HashMap<String, UserChallengesDTO>();

    if (username == null) {
        username = (String) request.getSession().getAttribute("logged-user");
    }

    String BUNDLE = "org.wso2.carbon.identity.mgt.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

    try {
        String cookie = (String) session
		.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config
		.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config
		.getServletContext().getAttribute(
				CarbonConstants.CONFIGURATION_CONTEXT);
        client = new IdentityManagementAdminClient(cookie,
                backendServerURL, configContext);
        infoClient = new UserInformationRecoveryClient(cookie, backendServerURL, configContext);
        
     	challenges = infoClient.getChallengeQuestions();
		try {
			userChallenges = client.getChallengeQuestionsOfUser(username);
		} catch (Exception e) {

		}
		if (challenges != null) {
			for (ChallengeQuestionDTO challenge : challenges) {
				HashSet<ChallengeQuestionDTO> questionDTOs = challengesMap.get(challenge
						.getQuestionSetId());
				if (questionDTOs == null) {
					questionDTOs = new HashSet<ChallengeQuestionDTO>();
					questionDTOs.add(challenge);
					challengesMap.put(challenge.getQuestionSetId(), questionDTOs);
				}
				questionDTOs.add(challenge);
			}
		}

		if (userChallenges != null) {
			for (UserChallengesDTO userChallengesDTO : userChallenges) {
				userChallengesMap.put(userChallengesDTO.getId(), userChallengesDTO);
			}
		}
	} catch (Exception e) {
		String message = resourceBundle
				.getString("error.while.loading.account.recovery.data");
		CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
		forwardTo = "../admin/error.jsp";
	}
%>

<%
    if ( forwardTo != null) {
%>
<script type="text/javascript">
    function forward() {
        location.href = "<%=forwardTo%>";
    }
</script>

<script type="text/javascript">
    forward();
</script>
<%
    return;
	}
%>

<script type="text/javascript" src="extensions/js/vui.js"></script>
<script type="text/javascript" src="../extensions/core/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>

<fmt:bundle basename="org.wso2.carbon.identity.mgt.ui.i18n.Resources">
    <carbon:breadcrumb
            label="account.recovery"
            resourceBundle="org.wso2.carbon.identity.user.profile.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>"/>

    <div id="middle">
        <h2><fmt:message key="my.account.recovery"/></h2>
        <div id="workArea">   
        <%
        if(!client.isReadOnlyUserStore(username, null)) {
        %>
        <form action="account-recovery-finish.jsp?userName=<%=Encode.forUriComponent(username)%>">

        <%
            if(challengesMap.size() > 0){

             int challengeNumber = 1;
             for(Map.Entry<String,  HashSet<ChallengeQuestionDTO>> entry : challengesMap.entrySet()){
                currentUserChallenge = userChallengesMap.get(entry.getKey());
         %>
             <div class="sectionSeperator"><fmt:message key="challenge.question.set"/> <%=challengeNumber%></div>
             <div class=”sectionSub”>
                <table class="carbonFormTable">

                    <tr>
                        <td class="leftCol-med labelField"><fmt:message key="challenge.question"/><span class="required">*</span></td>
                        <td>
                            <select id="challengeQuestion_<%=challengeNumber%>" name="challengeQuestion_<%=challengeNumber%>" class="leftCol-big">
                                <%
                                    for (ChallengeQuestionDTO dto : entry.getValue()) {
                                        String question = dto.getQuestion();
                                        if (question != null && question.trim().length() > 0) {
                                            if(currentUserChallenge != null &&
                                                    question.equals(currentUserChallenge.getQuestion())){
                                %>
                                        <option value="<%=Encode.forHtmlAttribute(question)%>"  selected="selected"><%=Encode.forHtmlContent(question)%></option>
                                <%
                                            } else {
                                %>
                                        <option value="<%=Encode.forHtmlAttribute(question)%>"><%=Encode.forHtmlContent(question)%></option>
                                <%
                                            }
                                        }
                                    }
                                %>
                            </select>

                        </td>
                    </tr>
                    <tr>
                            <td>
                                <fmt:message key="challenge.answer"/>
                            </td>
                            <%
                                if (currentUserChallenge != null && currentUserChallenge.getAnswer() != null &&
                                            currentUserChallenge.getAnswer().trim().length() > 0) {
                            %>
                                <td><input class="leftCol-big" type="password" name="challengeAnswer_<%=challengeNumber%>"
                                           id="challengeAnswer_<%=challengeNumber%>" value="<%=Encode.forHtmlAttribute(currentUserChallenge.getAnswer())%>"/></td>
                            <%
                                } else {
                            %>
                                <td><input type="password" class="leftCol-big" name="challengeAnswer_<%=challengeNumber%>"
                                           id="challengeAnswer_<%=challengeNumber%>" /></td>
                            <%
                                }
                            %>
                     </tr>
                </table>
             </div>
             <input type="hidden" name="challengeId_<%=challengeNumber%>" id="challengeId_<%=challengeNumber%>" value="<%=Encode.forHtmlAttribute(entry.getKey())%>"/>




        <%
            challengeNumber ++;
            }

            }
        %>
            <div class="buttonRow">
                    <input type="submit" class="button" value="Update" />
                </div>
        </form>
        <%
        } else {
        %> 
        	<fmt:message key="recovery.not.supported"/>
       	<%        	
        }
        %>
        </div>
    </div>
</fmt:bundle>
