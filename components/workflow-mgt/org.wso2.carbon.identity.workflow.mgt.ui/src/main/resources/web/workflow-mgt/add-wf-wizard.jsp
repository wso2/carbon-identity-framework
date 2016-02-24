<%--
  ~ Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

<%@ page import="org.wso2.carbon.identity.workflow.mgt.ui.WorkflowUIConstants" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.wso2.carbon.identity.workflow.mgt.ui.WorkflowAdminServiceClient" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.util.UUID" %>
<%@ page import="org.wso2.carbon.identity.workflow.mgt.ui.util.WorkflowUIUtil" %>
<%@ page import="org.wso2.carbon.identity.workflow.mgt.stub.metadata.WorkflowWizard" %>
<%@ page import="org.owasp.encoder.Encode" %>


<script type="text/javascript" src="extensions/js/vui.js"></script>
<script type="text/javascript" src="../extensions/core/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>

<%

    WorkflowAdminServiceClient client;
    String bundle = "org.wso2.carbon.identity.workflow.mgt.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(bundle, request.getLocale());
    String forwardTo = null;


    String requestPath = "list-workflows";
    //'path' parameter to use to track parent wizard path if this wizard trigger by another wizard
    if(request.getParameter(WorkflowUIConstants.PARAM_REQUEST_PATH) != null &&
       !request.getParameter(WorkflowUIConstants.PARAM_REQUEST_PATH).isEmpty()){
        requestPath = request.getParameter(WorkflowUIConstants.PARAM_REQUEST_PATH);
    }

    boolean isBack = false ;
    if(StringUtils.isNotBlank(request.getParameter(WorkflowUIConstants.PARAM_BACK)) && request
            .getParameter(WorkflowUIConstants.PARAM_BACK).equals("true")){
        isBack =  true ;
    }



    String workflowId = request.getParameter(WorkflowUIConstants.PARAM_WORKFLOW_ID);
    String requestToken = request.getParameter(WorkflowUIConstants.PARAM_PAGE_REQUEST_TOKEN);




    WorkflowWizard workflowWizard = null ;

    try {
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext()
                        .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        client = new WorkflowAdminServiceClient(cookie, backendServerURL, configContext);

        if(StringUtils.isNotBlank(requestToken)){
            workflowWizard = (WorkflowWizard)session.getAttribute(requestToken);
        }else{
            requestToken = UUID.randomUUID().toString();
            if(StringUtils.isNotBlank(workflowId)){
                workflowWizard = client.getWorkflow(workflowId);
            }else{
                workflowWizard = new WorkflowWizard();
            }
            session.setAttribute(requestToken,workflowWizard);
        }
    } catch (Exception e) {
        String message = resourceBundle.getString("workflow.error.when.initiating.service.client");
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
        forwardTo = "../admin/error.jsp";
    }
%>


<%
    if (forwardTo != null) {
%>
<script type="text/javascript">
    function forward() {
        location.href = "<%=Encode.forJavaScriptBlock(forwardTo)%>";
    }
</script>

<script type="text/javascript">
    forward();
</script>
<%
        return;
    }
%>





<fmt:bundle basename="org.wso2.carbon.identity.workflow.mgt.ui.i18n.Resources">
    <carbon:breadcrumb
            label="workflow.add"
            resourceBundle="org.wso2.carbon.identity.workflow.mgt.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>"/>

    <script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/main.js"></script>
    <script type="text/javascript">

        function doCancel() {
            function cancel() {
                location.href = '<%=requestPath%>.jsp?wizard=finish';
            }
            CARBON.showConfirmationDialog('<fmt:message key="confirmation.workflow.add.abort"/> ?',
                    cancel, null);
        }

        function submitPage(){
            var workflowForm = document.getElementById("id_workflow");
            if($('#id_workflow_name').val().length > 0){
                workflowForm.submit();
            }else{
                CARBON.showWarningDialog("<fmt:message key="workflow.error.empty.workflow.name"/>" , null ,null) ;
            }
        }

    </script>

    <div id="middle">
        <h2><fmt:message key='workflow.add'/></h2>

        <div id="workArea">
            <form id="id_workflow" method="post" name="serviceAdd" action="template-wf-wizard.jsp">
                <input type="hidden" name="<%=WorkflowUIConstants.PARAM_PAGE_REQUEST_TOKEN%>" value="<%=Encode.forHtmlAttribute(requestToken)%>"/>
                <input type="hidden" name="<%=WorkflowUIConstants.PARAM_REQUEST_PATH%>" value="<%=Encode.forHtmlAttribute(requestPath)%>"/>
                <table class="styledLeft">
                    <thead>
                    <tr>
                        <th><fmt:message key="workflow.details"/></th>
                    </tr>
                    </thead>
                    <tr>
                        <td class="formRow">
                            <table class="normal">
                                <tr>
                                    <td width="130px"><fmt:message key='workflow.name'/> <span style="color:red">*</span></td>
                                    <td>
                                        <input size="30" id="id_workflow_name" type="text" name="<%=WorkflowUIConstants.PARAM_WORKFLOW_NAME%>" value="<%=(workflowWizard != null && workflowWizard.getWorkflowName() != null) ? Encode.forHtmlAttribute(workflowWizard.getWorkflowName()) : ""%>" style="min-width: 30%"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td><fmt:message key='workflow.description'/></td>
                                    <td>
                                        <textarea name="<%=WorkflowUIConstants.PARAM_WORKFLOW_DESCRIPTION%>" cols="60" rows="4"><%=(workflowWizard != null && workflowWizard.getWorkflowDescription() != null) ? Encode.forHtmlAttribute(workflowWizard.getWorkflowDescription()) : ""%></textarea>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    <tr>
                        <td class="buttonRow">
                            <input onclick="submitPage();" class="button" value="<fmt:message key="next"/>" type="button"/>
                            <input class="button" value="<fmt:message key="cancel"/>" type="button" onclick="doCancel();"/>
                        </td>
                    </tr>
                </table>
                <br/>
            </form>
        </div>
    </div>
</fmt:bundle>
