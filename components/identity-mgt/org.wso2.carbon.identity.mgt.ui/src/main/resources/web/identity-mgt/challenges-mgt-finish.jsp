<!--
  ~
  ~ Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@page import="org.apache.axis2.context.ConfigurationContext"%>
<jsp:include page="../dialog/display_messages.jsp"/>
<%@page import="org.wso2.carbon.CarbonConstants" %>
<%@page import="org.wso2.carbon.identity.mgt.stub.dto.ChallengeQuestionDTO" %>
<%@ page import="org.wso2.carbon.identity.mgt.ui.IdentityManagementAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>
<script type="text/javascript" src="extensions/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>

<%
    List<ChallengeQuestionDTO> challenges =  new ArrayList<ChallengeQuestionDTO>();
    String  removeSetId = request.getParameter("removeSetId");

    if(removeSetId != null && removeSetId.trim().length() > 0){
        List<ChallengeQuestionDTO> retrievedChallenges = (List<ChallengeQuestionDTO>) session.
                getAttribute(IdentityManagementAdminClient.CHALLENGE_QUESTION);

        if(retrievedChallenges != null){
            Iterator<ChallengeQuestionDTO> iterator = retrievedChallenges.iterator();
            while(iterator.hasNext()){
                ChallengeQuestionDTO dto = iterator.next();
                if(removeSetId.trim().equals(dto.getQuestionSetId())){
                    iterator.remove();
                }
            }
            challenges = retrievedChallenges;
        }
    }

    int i = 1;
    while(true){
        String question = request.getParameter("question" + i);
        String setId = request.getParameter("setId" + i);
        if(question == null || question.trim().length() == 0 || question.trim().equals("null")){
            if(setId == null || setId.trim().length() < 1 || setId.trim().equals("null")){
                break;
            }
        } else {
            ChallengeQuestionDTO dto = new ChallengeQuestionDTO();
            dto.setQuestion(question.trim());
            dto.setPromoteQuestion(true);
            dto.setQuestionSetId(setId.trim());
            challenges.add(dto);
        }
        i++;
    }

    try {
        String cookie = (String) session
                .getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(),
                session);
        ConfigurationContext configContext = (ConfigurationContext) config
                .getServletContext()
                .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        IdentityManagementAdminClient proxy =
                            new IdentityManagementAdminClient(cookie, backendServerURL, configContext);
        if(challenges.size() >= 0){
            proxy.setChallengeQuestions(challenges.toArray(new ChallengeQuestionDTO[challenges.size()]));
        }

%>
    <script type="text/javascript">
        location.href = "challenges-set-mgt.jsp";
    </script>
<%
    } catch (Exception e) {
        CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR,
                request);
%>
    <script type="text/javascript">
        location.href = "challenges-set-mgt.jsp";
    </script>
<%
    return;
    }
%>
        

