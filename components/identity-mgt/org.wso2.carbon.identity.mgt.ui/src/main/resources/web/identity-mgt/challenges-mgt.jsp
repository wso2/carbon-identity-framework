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

<%@page import="org.wso2.carbon.identity.mgt.stub.dto.ChallengeQuestionDTO"%>
<jsp:include page="../dialog/display_messages.jsp"/>

<%@ page import="org.wso2.carbon.identity.mgt.ui.IdentityManagementAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.List" %>
<%@ page import="org.owasp.encoder.Encode" %>
<script type="text/javascript" src="extensions/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>

<%
    String selectedChallenge = null;
    List<ChallengeQuestionDTO> challenges = null;
    String deleteRowId = request.getParameter("deleteRowId");
    String editRowId = request.getParameter("editRowId");
    String addRowId = request.getParameter("addRowId");
    String setName = request.getParameter("setName");
    challenges = (List<ChallengeQuestionDTO>) session.
                                    getAttribute(IdentityManagementAdminClient.CHALLENGE_QUESTION);
    
    if(challenges == null){
        try {
            String cookie = (String) session
                    .getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
            String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(),
                    session);
            ConfigurationContext configContext = (ConfigurationContext) config
                    .getServletContext()
                    .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            IdentityManagementAdminClient client =
                    new IdentityManagementAdminClient(cookie, backendServerURL, configContext) ;

            ChallengeQuestionDTO[] questionDTOs = client.getChallengeQuestions();

            if(questionDTOs != null && questionDTOs.length > 0){
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
    }



    if(challenges != null ){
        if(deleteRowId != null){
            int rowNo = Integer.parseInt(deleteRowId);
            challenges.remove(rowNo);
        }

        if(editRowId != null){
            int rowNo = Integer.parseInt(editRowId);
            selectedChallenge = challenges.get(rowNo).getQuestion();
            challenges.remove(rowNo);
        }


    } else {
        challenges = new ArrayList<ChallengeQuestionDTO>();
    }

    if(addRowId != null){
        ChallengeQuestionDTO dto = new ChallengeQuestionDTO();
        dto.setQuestion(addRowId);
        dto.setQuestionSetId(setName);
        challenges.add(dto);

    }

    session.setAttribute(IdentityManagementAdminClient.CHALLENGE_QUESTION, challenges);
%>
        
<fmt:bundle basename="org.wso2.carbon.identity.mgt.ui.i18n.Resources">
<carbon:breadcrumb label="challenge.add"
		resourceBundle="org.wso2.carbon.identity.mgt.ui.i18n.Resources"
		topPage="true" request="<%=request%>" />

    <script type="text/javascript">


        function removeRow(row){
        	function doDelete() {
	            var setName = document.getElementsByName("setName")[0].value;
	            location.href= 'challenges-mgt.jsp?deleteRowId=' + encodeURIComponent(row) + '&setName=' +
                        encodeURIComponent(setName);
        	}
        	
            CARBON.showConfirmationDialog("<fmt:message key="confirm.delete.challenge.question"/> ?", doDelete, null);
        }

        function  editRow(row){
            var setName = document.getElementsByName("setName")[0].value;
            location.href= 'challenges-mgt.jsp?editRowId=' + encodeURIComponent(row) + '&setName=' +
                    encodeURIComponent(setName);
        }

        function addRow(){
            var setName = document.getElementsByName("setName")[0].value;
            var question = document.getElementsByName("question0")[0].value;
            if (setName == ""){
                CARBON. showErrorDialog('Please enter a non empty Question Set Id', null, null);
            } else if (question == null || question == ""){
                CARBON. showErrorDialog('Please enter a valid security question', null, null);
            } else {
                location.href= 'challenges-mgt.jsp?addRowId=' + encodeURIComponent(question) + '&setName=' +
                        encodeURIComponent(setName);
            }
            
        }

        function cancelForm(){
            location.href = 'challenges-set-mgt.jsp';    
        }

    </script>

    <div id="middle">
        <h2>Add Challenge Questions</h2>

            <form id="questionForm" name="questionForm" method="post" action="challenges-mgt-finish.jsp">
            <div id="workArea">
            <table class="normal">

                <tr>
                       <%
                           if(setName != null && setName.trim().length() > 0){
                       %>
                            <td  style="padding-top:8px;"> Challenge Question Set Id :</td>
                            <td><input name="setName" id="setName"  value="<%=Encode.forHtmlAttribute(setName)%>" readonly="readonly" size="35" /></td>
                       <%
                           } else {
                       %>
                            <td style="padding-top:8px;"> Enter Challenge Question Set Id :</td>
                            <td><input name="setName" id="setName" /></td>
                       <%
                           }
                       %>
                </tr>
                <tr>
                       <%
                           if(selectedChallenge != null && selectedChallenge.trim().length() > 0){
                       %>
                            <td style="padding-top:8px;"> Edit Challenge Question :</td>
                            <td><input size="70" name="question0" id="question0"  value="<%=Encode.forHtmlAttribute(selectedChallenge)%>"  /></td>
<!--                             <td>
                                <a onclick="addRow()" style='background-image:url(images/add.gif);' type="button" class="icon-link">Update</a>
                            </td> -->


                       <%
                           } else {
                       %>
                            <td style="padding-top:8px;"> Enter New Challenge Question :</td>
                            <td><input size="70" name="question0" id="question0" /></td>
<!--                             <td class="buttonRow">
                                <a onclick="addRow()" style='background-image:url(images/add.gif);' type="button" class="icon-link">Add</a>
                            </td> -->
                       <%
                           }
                       %>
                </tr>
 				<tr>
						<td>
							<%
	                           if(selectedChallenge != null && selectedChallenge.trim().length() > 0){
	                       %>
	                    
	                                <button onclick="addRow()"  type="button" class="button">Update</button>
	                           
	
	                       <%
	                           } else {
	                       %>
	                          
	                                <button onclick="addRow()" type="button" class="button">Add</button>
	                            
	                       <%
	                           }
	                       %>
							
						</td>
 						<td></td>
						<td></td>
				</tr> 
            </table>
            <p>&nbsp;</p>    
            <table class="styledLeft">
                <tr>
                    <td class="nopadding">
                        <table cellspacing="0" id="mainTable" style="width:100%;border:none !important">
                            <thead>
                                <th class="leftCol-small">Challenge Questions</th>
                                <th>Actions</th>
                            </thead>
                            <tbody>

                            <%
                                if(challenges.size() > 0){
                                    for(int i = 0; i < challenges.size(); i++){
                                        if(setName != null &&  setName.equals(challenges.get(i).getQuestionSetId())){
                             %>
                            <tr>
                                <td width="60%">
                                	<%=Encode.forHtmlContent(challenges.get(i).getQuestion())%>
                                </td>
                                <td width="40%">
                                    <a onclick="removeRow('<%=i%>')" style='background-image:url(images/delete.gif);' type="button" class="icon-link">Delete</a>
                                    <a onclick="editRow('<%=i%>')" style='background-image:url(images/edit.gif);' type="button" class="icon-link">Edit</a>
                                </td>
                            </tr>

                            <%
                                        } else {
                                         %>

                                        <input type="hidden" name="question<%=i+1%>" id="question<%=i+1%>"
                                               size="60" value="<%=Encode.forHtmlAttribute(challenges.get(i).getQuestion())%>"/>

                                         <%
                                        }
%>
                                        <input type="hidden" name="setId<%=i+1%>" id="setId<%=i+1%>"
                                               size="60" value="<%=Encode.forHtmlAttribute(challenges.get(i).getQuestionSetId())%>"/>
                                        <input type="hidden" name="question<%=i+1%>" id="question<%=i+1%>"
                                               size="60" value="<%=Encode.forHtmlAttribute(challenges.get(i).getQuestion())%>"/>
<%
                                    }
                                }
                            %>
                            
                            </tbody>
                        </table>
                    </td>

                </tr>
					<tr>
						<td class="buttonRow">
						<input type="submit" value="Finish" class="button" />
						<input type="button" value="Cancel" onclick="cancelForm();" class="button" />
						</td>
					</tr>
				</table>

            
        </div>
        </form>
    </div>
</fmt:bundle>

