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
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
           prefix="carbon" %>
<%@ page import="org.apache.axis2.AxisFault" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.identity.workflow.mgt.ui.WorkflowAdminServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>

<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.identity.workflow.mgt.ui.WorkflowUIConstants" %>
<%@ page import="org.wso2.carbon.identity.workflow.mgt.stub.metadata.WorkflowWizard" %>
<%@ page import="org.wso2.carbon.identity.workflow.mgt.stub.bean.Parameter" %>
<%@ page import="org.wso2.carbon.identity.workflow.mgt.stub.metadata.bean.ParametersMetaData" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Set" %>
<%@ page import="org.owasp.encoder.Encode" %>

<%

    WorkflowAdminServiceClient client;
    String bundle = "org.wso2.carbon.identity.workflow.mgt.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(bundle, request.getLocale());
    String forwardTo = null;


    String workflowId = request.getParameter(WorkflowUIConstants.PARAM_WORKFLOW_ID);


    Map<String,Map<String,Parameter>> templateParameterValues = new HashMap<String,Map<String,Parameter>>();
    Map<String,Map<String,Parameter>> workflowParameterValues = new HashMap<String,Map<String,Parameter>>();

    WorkflowWizard workflowWizard = null ;

    try {
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext()
                        .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        client = new WorkflowAdminServiceClient(cookie, backendServerURL, configContext);
        workflowWizard = client.getWorkflow(workflowId);

        Parameter[] templateParameters = workflowWizard.getTemplateParameters();
        Parameter[] workflowImplParameters = workflowWizard.getWorkflowImplParameters();


        

        if(templateParameters != null && templateParameters.length > 0){
            for (Parameter parameter: templateParameters){
                Map<String, Parameter> stringParameterMap = templateParameterValues.get(parameter.getParamName());
                if(stringParameterMap == null){
                    stringParameterMap = new HashMap<String, Parameter>();
                    templateParameterValues.put(parameter.getParamName(), stringParameterMap);
                }
                stringParameterMap.put(parameter.getQName(),parameter);

            }
        }

        

        if(workflowImplParameters != null && workflowImplParameters.length > 0){
            for (Parameter parameter: workflowImplParameters){
                Map<String, Parameter> stringParameterMap = workflowParameterValues.get(parameter.getParamName());
                if(stringParameterMap == null){
                    stringParameterMap = new HashMap<String, Parameter>();
                    workflowParameterValues.put(parameter.getParamName(), stringParameterMap);
                }
                stringParameterMap.put(parameter.getQName(),parameter);

            }
        }


    } catch (Exception e) {
        String message = resourceBundle.getString("workflow.error.when.initiating.service.client");
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
        forwardTo = "../admin/error.jsp";
    }
%>


<script>
    function editWorkflow(workflowId){
        location.href = 'add-wf-wizard.jsp?<%=WorkflowUIConstants.PARAM_WORKFLOW_ID%>='+ workflowId ;
    }
    function doCancel() {
        location.href = 'list-workflows.jsp?wizard=finish';
    }

</script>


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
    <carbon:breadcrumb label="view"
                       resourceBundle="org.wso2.carbon.identity.workflow.mgt.ui.i18n.Resources"
                       topPage="false" request="<%=request%>"/>

    <script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../admin/js/cookies.js"></script>
    <script type="text/javascript" src="../admin/js/main.js"></script>
    <script type="text/javascript">
        function doCancel() {
            location.href = 'list-workflows.jsp';
        }
    </script>

    <div id="middle">
        <h2><fmt:message key='workflow.list'/></h2>

        <div id="workArea">
            <a title="<fmt:message key='workflow.service.workflow.edit.title'/>"
               onclick="editWorkflow('<%=Encode.forJavaScriptAttribute(workflowWizard.getWorkflowId())%>');return false;"
               href="#" style="background-image: url(images/edit.gif);" class="icon-link">
                <fmt:message key='workflow.service.workflow.edit.title'/></a>



                    <table class="styledLeft noBorders">
                        <thead>
                        <tr>
                            <th colspan="2"><fmt:message key="workflow.details"/></th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr>
                            <td width="30%"><fmt:message key='workflow.name'/></td>
                            <td><%= Encode.forHtmlContent(workflowWizard.getWorkflowName())%></td>
                        </tr>
                        <tr>
                            <td width="30%"><fmt:message key='workflow.description'/></td>
                            <td><%= Encode.forHtmlContent(workflowWizard.getWorkflowDescription())%></td>
                        </tr>
                        </tbody>
                    </table>
                    <table class="styledLeft noBorders" style="margin-top: 10px">
                        <thead>
                        <tr>
                            <th colspan="2"><fmt:message key="workflow.template"/></th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr>
                            <td width="30%"><fmt:message key='workflow.template.name'/></td>
                            <td><%=Encode.forHtmlContent(workflowWizard.getTemplate().getName())%></td>
                        </tr>
                        <tr>
                            <td width="30%"><fmt:message key='workflow.template.desc'/></td>
                            <td><%=Encode.forHtmlContent(workflowWizard.getTemplate().getDescription())%></td>
                        </tr>
                        <tr>
                            <td colspan="2" width="100%">

                                <table class="styledLeft noBorders" style="margin-top: 10px">
                                    <thead>
                                    <tr>
                                        <th colspan="2"><fmt:message key="workflow.template.parameters"/></th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    
                                    <%
                                        Set<String> keys =  templateParameterValues.keySet();

                                        for (String key:keys){
                                            Map<String, Parameter> stringParameterMapTmp =
                                                    templateParameterValues.get(key);
                                            Set<String> keysForParam = stringParameterMapTmp.keySet();
                                            if(keysForParam.size() > 1){
                                    %>
                                    <tr>
                                        <td colspan="2">
                                        <table class="styledLeft noBorders" style="margin-top: 10px">
                                            <thead>
                                            <tr>
                                                <th colspan="2"><%=Encode.forHtmlContent(key)%></th>
                                            </tr>
                                            </thead>
                                            <tbody>
                                    <%
                                        }

                                            for(String keyForParam: keysForParam){
                                                Parameter parameter = stringParameterMapTmp.get(keyForParam);


                                    %>
                                                <tr>
                                                    <td width="30%"><%=Encode.forHtmlContent(parameter.getQName())%></td>
                                                    <td><%=Encode.forHtmlContent(parameter.getParamValue())%></td>
                                                </tr>
                                    <%

                                            }
                                        if(keysForParam.size() > 1){
                                    %>
                                            </tbody>
                                        </table>
                                        </td>
                                    </tr>
                                    <%
                                            }
                                        }
                                    %>
                                    </tbody>
                                </table>

                            </td>
                        </tr>
                        
                        
                        </tbody>
                    </table>






                    <table class="styledLeft noBorders" style="margin-top: 10px">
                        <thead>
                        <tr>
                            <th colspan="2"><fmt:message key="workflow.impl"/></th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr>
                            <td width="30%"><fmt:message key='workflow.impl.name'/></td>
                            <td><%=Encode.forHtmlContent(workflowWizard.getWorkflowImpl().getWorkflowImplName())%></td>
                        </tr>
                        <tr>
                            <td colspan="2" width="100%">

                                <table class="styledLeft noBorders" style="margin-top: 10px">
                                    <thead>
                                    <tr>
                                        <th colspan="2"><fmt:message key="workflow.impl.parameters"/></th>
                                    </tr>
                                    </thead>
                                    <tbody>

                                    <%
                                        keys =  workflowParameterValues.keySet();

                                        for (String key:keys){
                                            Map<String, Parameter> stringParameterMapTmp =
                                                    workflowParameterValues.get(key);
                                            Set<String> keysForParam = stringParameterMapTmp.keySet();
                                            if(keysForParam.size() > 1){
                                    %>
                                    <tr>
                                        <td colspan="2">
                                            <table class="styledLeft noBorders" style="margin-top: 10px">
                                                <thead>
                                                <tr>
                                                    <th colspan="2"><%=Encode.forHtmlContent(key)%></th>
                                                </tr>
                                                </thead>
                                                <tbody>
                                                <%
                                                    }

                                                    for(String keyForParam: keysForParam){
                                                        Parameter parameter = stringParameterMapTmp.get(keyForParam);


                                                %>
                                                <tr>
                                                    <td width="30%"><%=Encode.forHtmlContent(parameter.getQName())%></td>
                                                    <td><%=Encode.forHtmlContent(parameter.getParamValue())%></td>
                                                </tr>
                                                <%

                                                    }
                                                    if(keysForParam.size() > 1){
                                                %>
                                                </tbody>
                                            </table>
                                        </td>
                                    </tr>
                                    <%
                                            }
                                        }
                                    %>
                                    </tbody>
                                </table>

                            </td>
                        </tr>
                        </tbody>
                    </table>





                    <table style="margin-top: 10px">
                        <tr>
                            <td class="buttonRow">
                                <input class="button" value="<fmt:message key="cancel"/>" type="button"
                                       onclick="doCancel();"/>
                            </td>
                        </tr>
                    </table>
                    <br/>
        </div>
    </div>
</fmt:bundle>