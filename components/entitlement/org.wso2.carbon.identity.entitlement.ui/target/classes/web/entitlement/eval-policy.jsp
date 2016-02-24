<!--
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 -->

<%@ page import="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyConstants" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyCreator" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.RequestDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.RowDTO" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="org.owasp.encoder.Encode" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
	prefix="carbon"%>
<jsp:include page="../dialog/display_messages.jsp"/>
<jsp:include page="../highlighter/header.jsp"/>

<%
    String forwardTo = null;
    boolean showResponse = false;
    String requestString = (String)session.getAttribute("txtRequest");
    String responseString = (String)session.getAttribute("txtResponse");
    String policyId = (String)session.getAttribute("policyId");
    String isResponse = request.getParameter("isResponse");
    if(isResponse != null && isResponse.trim().length() > 0){
        showResponse = true;
    }
    if(responseString != null){
        responseString = responseString.trim().replaceAll("><", ">\n<");
    } else {
        responseString = "";
    }
    if(!showResponse){
        List<RowDTO> rowDTOs = new ArrayList<RowDTO>();
        String multipleRequest = request.getParameter("multipleRequest");
        String returnPolicyList = request.getParameter("returnPolicyList");
        String resourceNames = request.getParameter("resourceNames");
        String subjectNames = request.getParameter("subjectNames");
        String actionNames = request.getParameter("actionNames");
        String environmentNames = request.getParameter("environmentNames");

        if (resourceNames != null  && !resourceNames.trim().equals("")){
            RowDTO rowDTO = new RowDTO();
            rowDTO.setAttributeValue(resourceNames);
            rowDTO.setAttributeDataType(EntitlementPolicyConstants.STRING_DATA_TYPE);
            rowDTO.setAttributeId("urn:oasis:names:tc:xacml:1.0:resource:resource-id");
            rowDTO.setCategory("urn:oasis:names:tc:xacml:3.0:attribute-category:resource");
            String resourceNamesInclude = request.getParameter("resourceNamesInclude");
            if(resourceNamesInclude != null){
                rowDTO.setNotCompleted(Boolean.parseBoolean(resourceNamesInclude));
                session.setAttribute("resourceNamesInclude",resourceNamesInclude);
            }
            rowDTOs.add(rowDTO);
            session.setAttribute("resourceNames",resourceNames);
        }
        if (subjectNames != null  && !subjectNames.trim().equals("")){
            RowDTO rowDTO = new RowDTO();
            rowDTO.setAttributeValue(subjectNames);
            rowDTO.setAttributeDataType(EntitlementPolicyConstants.STRING_DATA_TYPE);
            rowDTO.setAttributeId("urn:oasis:names:tc:xacml:1.0:subject:subject-id");
            rowDTO.setCategory("urn:oasis:names:tc:xacml:1.0:subject-category:access-subject");
            String subjectNamesInclude = request.getParameter("subjectNamesInclude");
            if(subjectNamesInclude != null){
                rowDTO.setNotCompleted(Boolean.parseBoolean(subjectNamesInclude));
                session.setAttribute("subjectNamesInclude",subjectNamesInclude);
            }
            rowDTOs.add(rowDTO);
            session.setAttribute("subjectNames",subjectNames);
        }
        if (actionNames != null  && !actionNames.trim().equals("")){
            RowDTO rowDTO = new RowDTO();
            rowDTO.setAttributeValue(actionNames);
            rowDTO.setAttributeDataType(EntitlementPolicyConstants.STRING_DATA_TYPE);
            rowDTO.setAttributeId("urn:oasis:names:tc:xacml:1.0:action:action-id");
            rowDTO.setCategory("urn:oasis:names:tc:xacml:3.0:attribute-category:action");
            String actionNamesInclude = request.getParameter("actionNamesInclude");
            if(actionNamesInclude != null){
                rowDTO.setNotCompleted(Boolean.parseBoolean(actionNamesInclude));
                session.setAttribute("actionNamesInclude",actionNamesInclude);
            }
            rowDTOs.add(rowDTO);
            session.setAttribute("actionNames",actionNames);
        }
        if (environmentNames != null  && !environmentNames.trim().equals("")){
            RowDTO rowDTO = new RowDTO();
            rowDTO.setAttributeValue(environmentNames);
            rowDTO.setAttributeDataType(EntitlementPolicyConstants.STRING_DATA_TYPE);
            rowDTO.setAttributeId("urn:oasis:names:tc:xacml:1.0:environment:environment-id");
            rowDTO.setCategory("urn:oasis:names:tc:xacml:3.0:attribute-category:environment");
            String environmentNamesInclude = request.getParameter("environmentNamesInclude");
            if(environmentNamesInclude != null){
                rowDTO.setNotCompleted(Boolean.parseBoolean(environmentNamesInclude));
                session.setAttribute("actionNamesInclude",environmentNamesInclude);
            }
            rowDTOs.add(rowDTO);
            session.setAttribute("environmentNames",environmentNames);
        }

        RequestDTO requestDTO = new RequestDTO();
        if(multipleRequest != null){
            requestDTO.setMultipleRequest(Boolean.parseBoolean(multipleRequest));
            session.setAttribute("multipleRequest", multipleRequest);
        }
        if(returnPolicyList != null){
            requestDTO.setReturnPolicyIdList(Boolean.parseBoolean(returnPolicyList));
            session.setAttribute("returnPolicyList", returnPolicyList);
        }
        requestDTO.setRowDTOs(rowDTOs);

        EntitlementPolicyCreator entitlementPolicyCreator = new EntitlementPolicyCreator();
        try {
            if(requestString != null && requestString.trim().length() > 0){
                requestString = requestString.trim().replaceAll("><", ">\n<");
            } else if(!requestDTO.getRowDTOs().isEmpty()){
                String createdRequest = entitlementPolicyCreator.createBasicRequest(requestDTO);
                if(createdRequest != null && createdRequest.trim().length() > 0){
                    requestString = createdRequest.trim().replaceAll("><", ">\n<");
                }
            } else {
                requestString = "";
            }
        } catch (Exception e) {
            CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request);
            forwardTo = "../admin/error.jsp";
    %>
    <script type="text/javascript">
        forward();
    </script>
    <%
        }
    }
%>
<fmt:bundle basename="org.wso2.carbon.identity.entitlement.ui.i18n.Resources">
	<carbon:breadcrumb label="eval.policy"
		resourceBundle="org.wso2.carbon.identity.entitlement.ui.i18n.Resources"
		topPage="true" request="<%=request%>" />

	<script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
	<script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
	<script type="text/javascript" src="../carbon/admin/js/main.js"></script>
    <script src="../editarea/edit_area_full.js" type="text/javascript"></script>
    <script type="text/javascript">
        jQuery(document).ready(function(){
            editAreaLoader.init({
                id : "txtRequestTemp"
                ,syntax: "xml"
                ,start_highlight: true
            });
        });

    </script>

    
    <script type="text/javascript">

        function validateRequest() {
           var value = document.getElementById("txtRequestTemp").value;
           if (value == '') {
               CARBON.showWarningDialog("<fmt:message key='empty.request'/>");
               return false;
           }
           return true;
        }

        function forward() {
           location.href = "<%=forwardTo%>";
        }

        function evaluateXACMLRequest(withPDP){
            if(validateRequest()){
                if(withPDP){
                    document.evaluateRequest.action = "eval-policy-submit.jsp?withPDP=true"; 
                } else {
                    document.evaluateRequest.action = "eval-policy-submit.jsp";     
                }
                document.getElementById("txtRequest").value = editAreaLoader.getValue("txtRequestTemp");
                document.evaluateRequest.submit();
            }
        }

        function backToEvaluation(){
            location.href = "eval-policy.jsp";
        }
    </script>


	<div id="middle">
	<h2><fmt:message key='eval.ent.policy'/></h2>
	<div id="workArea">
    <form name="evaluateRequest" id="evaluateRequest" action="eval-policy-submit.jsp" method="post">
	<table style="width: 100%" class="styledLeft">
	
		<thead>
			<tr>
				<th>
                    <%
                        if(showResponse){
                    %>
                        <fmt:message key='ent.eval.policy.response'/>
                    <%
                        } else {
                    %>
                        <fmt:message key='ent.eval.policy.request'/>
                    <%
                        }
                    %>
                </th>
			</tr>
		</thead>
		<tbody>
        <tr>
            <td>
                <div>
                <textarea id="txtRequestTemp" name="txtRequestTemp" rows="30" cols="120">
                    <%if(showResponse){%> <%=Encode.forHtmlContent(responseString)%> <% } else {%> <%=Encode.forHtmlContent(requestString)%> <%}%>
                </textarea>
                <textarea name="txtRequest" id="txtRequest" style="display:none">
                    <%if(showResponse){%> <%=Encode.forHtmlContent(responseString)%> <% } else {%> <%=Encode.forHtmlContent(requestString)%> <%}%>
                </textarea>
                <input type="hidden" id="forwardTo" name="forwardTo" value="eval-policy.jsp" />
                </div>
            </td>
        </tr>
        <tr>
            <td class="buttonRow">
                <%
                    if(showResponse){
                %>
                <input type="button" value="<fmt:message key='back.evaluate'/>" class="button" onclick="backToEvaluation();"/>
                <%
                    } else {
                %>
                    <%
                        if(policyId != null){
                    %>
                <input type="button" value="<fmt:message key='test.evaluate'/>" class="button" onclick="evaluateXACMLRequest(false);"/>
                <%
                    }else {
                %>
                <input type="button" value="<fmt:message key='pdp.evaluate'/>" class="button" onclick="evaluateXACMLRequest(true);"/>
                <%
                    }
                %>
                <input class="button" type="reset" value="<fmt:message key='cancel'/>"
                       onclick="javascript:document.location.href='create-evaluation-request.jsp'"/>

                <%
                    }
                %>
            </td>
        </tr>
		</tbody>	
	</table>
	</form>
	</div>	
	</div>
</fmt:bundle>
