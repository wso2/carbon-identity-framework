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
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@page import="org.wso2.carbon.CarbonConstants" %>
<%@page import="org.wso2.carbon.identity.mgt.stub.dto.ChallengeQuestionDTO" %>
<%@page import="org.wso2.carbon.identity.mgt.ui.IdentityManagementAdminClient" %>

<jsp:include page="../dialog/display_messages.jsp"/>

<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.util.List" %>
<%@ page import="org.owasp.encoder.Encode" %>
<script type="text/javascript" src="extensions/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>

<%
    session.removeAttribute(IdentityManagementAdminClient.CHALLENGE_QUESTION);
    List<ChallengeQuestionDTO> challenges = null;
    HashSet<String> questionSetNames = new HashSet<String>();

    try {
        String cookie = (String) session
                .getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(),
                session);
        ConfigurationContext configContext = (ConfigurationContext) config
                .getServletContext()
                .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        IdentityManagementAdminClient client =
                new IdentityManagementAdminClient(cookie, backendServerURL, configContext);

        ChallengeQuestionDTO[] questionDTOs = client.getChallengeQuestions();

        if(questionDTOs != null && questionDTOs.length > 0){
            for(ChallengeQuestionDTO questionDTO : questionDTOs){
                if(questionDTO.getQuestionSetId() != null){
                    questionSetNames.add(questionDTO.getQuestionSetId());
                }
            }
            challenges = new ArrayList<ChallengeQuestionDTO>(Arrays.asList(questionDTOs));
            session.setAttribute(IdentityManagementAdminClient.CHALLENGE_QUESTION, challenges);
        }
    } catch (Exception e) {
        CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR,
                request);
%>
        <script type="text/javascript">
            location.href = "index.jsp";
        </script>
<%
        return;
    }



%>
        
<fmt:bundle basename="org.wso2.carbon.identity.mgt.ui.i18n.Resources">
<carbon:breadcrumb label="challenge.mgt"
		resourceBundle="org.wso2.carbon.identity.mgt.ui.i18n.Resources"
		topPage="false" request="<%=request%>" />

    <script type="text/javascript">

        function removeSet(row){
        	function doDelete() {
            	location.href= 'challenges-mgt-finish.jsp?removeSetId=' + encodeURIComponent(row);
        	}
            
            CARBON.showConfirmationDialog("<fmt:message key="confirm.delete.challenge.set"/> " + row + " ?", doDelete, null);
        }

        function cancelForm(){
            location.href = '../userstore/index.jsp';
        }

    </script>

    <div id="middle">
        <h2>Challenge Questions Management</h2>
            <div id="workArea">
            <table class="normal">
            <tr>
               <td ><a href="challenges-mgt.jsp" style="background-image: url(images/add.gif);"
                       class="icon-link">Add new challenge questions set</a></td>
            </tr>
            </table>

            <p>&nbsp;</p>

            <table  class="styledLeft" style="width: 100%;">
                <thead>
                    <th colspan="2" class="leftCol-small">Challenge Questions Sets</th>
                </thead>
                <tbody>

                <% if (questionSetNames.size() > 0) {
                    for (String questionSetName : questionSetNames) {
                %>
                <tr>
                    <td width="60%">
                        <a href="challenges-mgt.jsp?setName=<%=Encode.forUriComponent(questionSetName)%>" ><%=Encode.forHtmlContent(questionSetName)%></a>
                    </td>
                    <td width="40%">
                        <a onclick="removeSet('<%=Encode.forJavaScriptAttribute(questionSetName)%>')" style='background-image:url(images/delete.gif);'
                           type="button" class="icon-link">Delete</a>
                    </td>
                </tr>

                <%
                    }
                } else {
                %>
                <tr>
                    <td colspan="2"><i>No challenges questions set registered</i></td>
                </tr>
                <%
                    }
                %>
                </tbody>
            </table>
        </div>
    </div>
</fmt:bundle>
