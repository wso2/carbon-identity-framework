<%--
  ~ Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>


<%@page import="org.apache.axis2.context.ConfigurationContext"%>
<%@page import="org.wso2.carbon.CarbonConstants"%>
<%@page import="org.wso2.carbon.directory.server.manager.ui.DirectoryServerManagerClient"%>
<%@page import="org.wso2.carbon.ui.CarbonUIMessage"%>
<%@page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<%@page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.owasp.encoder.Encode" %>

<script type="text/javascript" src="../userstore/extensions/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<jsp:include page="../dialog/display_messages.jsp"/>

 <%
     String applicationSPName = request.getParameter("spName");
     session.setAttribute("application-sp-name", applicationSPName);

        DirectoryServerManagerClient serverManager = null;
        try{

            serverManager = (DirectoryServerManagerClient)session.getAttribute(DirectoryServerManagerClient.
                    SERVER_MANAGER_CLIENT);

            if(serverManager == null){
                String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
                String backEndServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
                ConfigurationContext configContext =
                    (ConfigurationContext) config.getServletContext().
                            getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

                serverManager = new DirectoryServerManagerClient(cookie, backEndServerURL, configContext);
                session.setAttribute(DirectoryServerManagerClient.SERVER_MANAGER_CLIENT, serverManager);
            }
        }catch(Exception e){
            CarbonUIMessage uiMsg = new CarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, e);
            session.setAttribute(CarbonUIMessage.ID, uiMsg);
   %>
            <jsp:include page="../admin/error.jsp"/>
   <%
            return;
        }
   %>


<fmt:bundle basename="org.wso2.carbon.directory.server.manager.ui.i18n.Resources">
<carbon:breadcrumb label="add.service"
                   resourceBundle="org.wso2.carbon.directory.server.manager.ui.i18n.Resources"
                   topPage="false" request="<%=request%>"/>

<script type="text/javascript">

    document.onload=clearFields();

    function clearFields() {
        document.dataForm.serviceName.reset();
        document.dataForm.serviceDescription.reset();
        document.dataForm.password.reset();
        document.dataForm.retype.reset();
    }

    function validateString(fieldName, regString) {
        var stringValue = document.getElementsByName(fieldName)[0].value;
        var errorMessage = "";
        if(regString != "null" && !stringValue.match(new RegExp(regString))){
            errorMessage = "No conformance";
            return errorMessage;
        }else if(regString != "null" && stringValue == ''){
            return errorMessage;
        }

        if (stringValue == '') {
            errorMessage = "Empty string";
            return errorMessage;
        }

        return errorMessage;
    }

    function doValidation() {
        var reason = "";

        reason = validateString("serviceName", "<%=Encode.forJavaScriptBlock(serverManager.getServiceNameConformanceRegularExpression())%>");
        if (reason != "") {
            if (reason == "No conformance") {
                CARBON.showWarningDialog("<fmt:message key="enter.service.name.not.conforming"/>");
            } else if (reason == "Empty string") {
            	CARBON.showWarningDialog("<fmt:message key="enter.user.name.empty"/>");
            }
            return false;
        }

        reason = validatePasswordOnCreation("password", "retype", "<%=Encode.forJavaScriptBlock(serverManager.getPasswordConformanceRegularExpression())%>");
        if (reason != "") {
            if (reason == "Empty Password") {
                CARBON.showWarningDialog("<fmt:message key="enter.the.same.password.twice"/>");
            } else if (reason == "Min Length") {
                CARBON.showWarningDialog("<fmt:message key="password.mimimum.characters"/>");
            } else if (reason == "Invalid Character") {
                CARBON.showWarningDialog("<fmt:message key="invalid.character.in.password"/>");
            } else if (reason == "Password Mismatch") {
                CARBON.showWarningDialog("<fmt:message key="password.mismatch"/>");
            } else if (reason == "No conformance") {
            	CARBON.showWarningDialog("<fmt:message key="password.conformance"/>");
            }
            return false;
        }
        return true;
    }


    function doCancel() {
        location.href = '../application/configure-service-provider.jsp?action=cancel&display=kerberos&spName=<%=Encode.forJavaScriptBlock(Encode.forUriComponent(applicationSPName))%>';
    }



    function doFinish() {
        document.dataForm.action = "add-finish.jsp";
        if (doValidation() == true) {
            document.dataForm.submit();
        }
    }

</script>
<div id="middle">
    <h2><fmt:message key="add.service.principle"/></h2>

    <div id="workArea">

        <form method="post" action="add-finish.jsp" name="dataForm">
            <table class="styledLeft" id="userAdd" width="60%">
                <thead>
                    <tr>
                        <th><fmt:message key="enter.service.details"/></th>
                    </tr>
                </thead>
                <tr>
                    <td class="formRaw">
                        <table class="normal">
                            <tr>
                                <td><fmt:message key="service.name"/><font color="red">*</font>
                                </td>
                                <td><input type="text" name="serviceName" value=""
                                           style="width:150px"/></td>
                            </tr>
			                <tr>
                                <td><fmt:message key="service.description"/>
                                </td>
                                <td><input type="text" name="serviceDescription" value=""
                                           style="width:150px"/></td>
                            </tr>
                            <tr>
                                <td><fmt:message key="password"/><font color="red">*</font></td>
                                <td><input type="password" name="password" value="" style="width:150px"/></td>
                            </tr>
                            <tr>
                                <td><fmt:message key="password.repeat"/><font
                                        color="red">*</font></td>
                                <td><input type="password" name="retype" value="" style="width:150px"/></td>
                            </tr>
                        </table>
                    </td>
                </tr>
                <tr>
                    <td class="buttonRow">
                        <input type="button" class="button" value="<fmt:message key="finish"/>" onclick="doFinish();"/>
                        <input type="button" class="button" value="<fmt:message key="cancel"/>" onclick="doCancel();"/>
                    </td>
                </tr>
            </table>
        </form>
    </div>
    <p>&nbsp;</p>
</div>
</fmt:bundle>