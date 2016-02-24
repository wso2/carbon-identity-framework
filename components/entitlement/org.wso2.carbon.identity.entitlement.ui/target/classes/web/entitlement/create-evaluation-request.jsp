<%@ page import="org.owasp.encoder.Encode" %>
<!--
/*
* Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
-->

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<jsp:useBean id="entitlementPolicyBean" type="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean"
             class="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean" scope="session"/>
<jsp:setProperty name="entitlementPolicyBean" property="*" />
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
	prefix="carbon"%>
<jsp:include page="../dialog/display_messages.jsp"/>
<jsp:include page="../highlighter/header.jsp"/>
<%
    String resourceNames;
    String subjectNames;
    String actionNames;
    String environmentNames;
    String multipleRequest;
    String returnPolicyList;
    String resourceNamesInclude;
    String subjectNamesInclude;
    String actionNamesInclude;
    String environmentNamesInclude;

    String clearAttributes = request.getParameter("clearAttributes");
    if("true".equals(clearAttributes)){
        session.removeAttribute("resourceNames");
        session.removeAttribute("subjectNames");
        session.removeAttribute("attributeId");
        session.removeAttribute("environmentNames");
        session.removeAttribute("actionNames");
        session.removeAttribute("resourceNamesInclude");
        session.removeAttribute("subjectNamesInclude");
        session.removeAttribute("actionNamesInclude");
        session.removeAttribute("environmentNamesInclude");
        session.removeAttribute("multipleRequest");
        session.removeAttribute("returnPolicyList");
    }

    // remove request and response from session
    session.removeAttribute("txtRequest");
    session.removeAttribute("txtResponse");


    String policyId = request.getParameter("policyId");
    if(policyId != null && policyId.trim().length() > 0){
        session.setAttribute("policyId", policyId);
    } else {
        policyId = (String)session.getAttribute("policyId");
    }

    resourceNames = (String)session.getAttribute("resourceNames");
    subjectNames = (String)session.getAttribute("subjectNames");
    actionNames = (String)session.getAttribute("actionNames");
    environmentNames = (String)session.getAttribute("environmentNames");

    multipleRequest = (String)session.getAttribute("multipleRequest");
    returnPolicyList = (String)session.getAttribute("returnPolicyList");

    resourceNamesInclude = (String)session.getAttribute("resourceNamesInclude");
    subjectNamesInclude = (String)session.getAttribute("subjectNamesInclude");
    actionNamesInclude = (String)session.getAttribute("actionNamesInclude");
    environmentNamesInclude = (String)session.getAttribute("environmentNamesInclude");
%>


<fmt:bundle basename="org.wso2.carbon.identity.entitlement.ui.i18n.Resources">
<carbon:breadcrumb
		label="create.evaluation.request"
		resourceBundle="org.wso2.carbon.identity.entitlement.ui.i18n.Resources"
		topPage="true"
		request="<%=request%>" />

    <script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
    <script type="text/javascript" src="resources/js/main.js"></script>
    <!--Yahoo includes for dom event handling-->
    <script src="../yui/build/yahoo-dom-event/yahoo-dom-event.js" type="text/javascript"></script>
    <script src="../entitlement/js/create-basic-policy.js" type="text/javascript"></script>
    <link href="../entitlement/css/entitlement.css" rel="stylesheet" type="text/css" media="all"/>

<script type="text/javascript">

    function submitForm(withPDP){
        if(validateForm()){
            if(withPDP){
                document.requestForm.action = "eval-policy-submit.jsp?withPDP=true";
                document.requestForm.submit();
            } else {
                document.requestForm.action = "eval-policy-submit.jsp?";
                document.requestForm.submit();
            }
        }
    }

    function validateForm(){
        if(document.requestForm.subjectNames.value != '' || document.forms[0].resourceNames.value != '' ||
           document.requestForm.actionNames.value != '' || document.forms[0].environmentNames.value != ''){
            return true;
        }
        CARBON.showWarningDialog("<fmt:message key='empty.form'/>");
        return false;
    }

    function clearForm(){
        location.href = 'create-evaluation-request.jsp?clearAttributes=true';        
    }

    function doCancel(){
        location.href = 'index.jsp';
    }

    function createRequest(){
        document.requestForm.action = "eval-policy.jsp?";
        document.requestForm.submit();
    }

</script>

<div id="middle">
    <h2><fmt:message key="identity.policy.tryit"/></h2>
    <div id="workArea">
        <div class="goToAdvance">
            <a class='icon-link' href="../entitlement/eval-policy.jsp"
                       style='background-image:url(images/advanceview.png);float:none'><fmt:message key="create.request.using.editor"/></a>
        </div>
        <form id="requestForm" name="requestForm" method="post" action="">
        <table class="styledLeft noBorders">

        <%
            if(policyId != null){
        %>
        <tr>
            <td colspan="3"><fmt:message key="eval.ent.policy.for.policyId"/> <b><%=Encode.forHtmlContent(policyId)%></b></td>
        </tr>
        <%
            }
        %>
        <tr>
            <td>
                <label><input type="checkbox" id="multipleRequest"
                              name="multipleRequest" value="true"
                              <%if(multipleRequest != null){%>checked="checked" <%}%> >Multiple Request</label>
            </td>
            <td>
                <label><input type="checkbox" id="returnPolicyList"
                              name="returnPolicyList" value="true"
                              <%if(returnPolicyList != null){%>checked="checked" <%}%> >Return Policy List</label>
            </td>
        </tr>
        <tr>
            <td style="width: 20%;margin-top:10px;"><fmt:message key='resource.names.are'/></td>
            <td style="width: 40%;margin-top:10px;">
            <%
                if (resourceNames != null && resourceNames.trim().length() > 0) {
            %>
            <input type="text" size="60" name="resourceNames" id="resourceNames"
                       value="<%=Encode.forHtmlAttribute(resourceNames)%>" class="text-box-big"/>
            <%
                } else {
            %>
            <input type="text" size="60" name="resourceNames" id="resourceNames"
                       class="text-box-big"/>
            <%
                }
            %>
            </td>
            <td style="width: 40%;margin-top:10px;">
                <label><input type="checkbox" id="resourceNamesInclude"
                              name="resourceNamesInclude" value="true"
                              <%if(resourceNamesInclude != null){%>checked="checked" <%}%> >Include In Result</label>
            </td>
        </tr>

        <tr>
            <td style="width: 20%;margin-top:10px;"><fmt:message key='subject.name'/></td>
            <td  style="width: 40%;margin-top:10px;">
            <%
                if (subjectNames != null && subjectNames.trim().length() > 0) {
            %>
            <input type="text" name="subjectNames" id="subjectNames"
                       value="<%=Encode.forHtmlAttribute(subjectNames)%>" class="text-box-big"/>
            <%
                } else {
            %>
            <input type="text" name="subjectNames" id="subjectNames" class="text-box-big"/>
            <%
                }
            %>
            </td>
            <td  style="width: 40%;margin-top:10px;">
                <label><input type="checkbox" id="subjectNamesInclude"
                              name="subjectNamesInclude" value="true"
                              <%if(subjectNamesInclude != null){%>checked="checked" <%}%> >Include In Result</label>
            </td>
        </tr>

        <tr>
            <td  style="width: 20%;margin-top:10px;"><fmt:message key='action.names'/></td>
            <td  style="width: 40%;margin-top:10px;">
            <%
                if (actionNames != null && actionNames.trim().length() > 0) {
            %>
            <input type="text" name="actionNames" id="actionNames" value="<%=Encode.forHtmlAttribute(actionNames)%>"
                       class="text-box-big"/>
            <%
                } else {
            %>
            <input type="text" name="actionNames" id="actionNames" class="text-box-big"/>
            <%
                }
            %>
            </td>
            <td  style="width: 40%;margin-top:10px;">
                <label><input type="checkbox" id="actionNamesInclude"
                              name="actionNamesInclude" value="true"
                              <%if(actionNamesInclude != null){%>checked="checked" <%}%> >Include In Result</label>
            </td>
        </tr>            

        <tr>
            <td style="width: 20%;margin-top:10px;"><fmt:message key='environment.names'/></td>
            <td style="width: 40%;margin-top:10px;">
            <%
                if (environmentNames != null && environmentNames.trim().length() > 0) {
            %>
            <input type="text" name="environmentNames" id="environmentNames" value="<%=Encode.forHtmlAttribute(environmentNames)%>"
                       class="text-box-big"/>
            <%
                } else {
            %>
            <input type="text" name="environmentNames" id="environmentNames" class="text-box-big"/>
            <%
                }
            %>
            </td>
            <td style="width: 40%;margin-top:10px;">
                <label><input type="checkbox" id="environmentNamesInclude"
                              name="environmentNamesInclude" value="true"
                              <%if(environmentNamesInclude != null){%>checked="checked" <%}%> >Include In Result</label>
            </td>
        </tr>


        <tr>
            <td class="buttonRow" colspan="3">
                <%
                    if(policyId != null){
                %>
                <input type="button" onclick="submitForm(false);" value="<fmt:message key="test.evaluate"/>"  class="button"/>
                <%
                    } else {
                %>
                <input type="button" onclick="submitForm(true);" value="<fmt:message key="pdp.evaluate"/>"  class="button"/>
                <%
                    }
                %>
                <input type="button" onclick="createRequest();" value="<fmt:message key="create.request"/>"  class="button"/>
                <input type="button" onclick="clearForm();" value="<fmt:message key="clear"/>"  class="button"/>

                <%
                    if(policyId != null){
                %>
                <input type="button" onclick="doCancel()" value="<fmt:message key="cancel"/>"  class="button"/>
                <%
                }
                %>

            </td>
        </tr>
        </table>
        </form>
    </div>
</div>
</fmt:bundle>
