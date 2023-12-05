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
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.identity.base.IdentityValidationUtil" %>
<%@ page import="org.wso2.carbon.identity.workflow.mgt.bean.metadata.type.InputType" %>

<%@ page import="org.wso2.carbon.identity.workflow.mgt.stub.WorkflowAdminServiceWorkflowException" %>
<%@ page import="org.wso2.carbon.identity.workflow.mgt.stub.bean.Parameter" %>
<%@ page import="org.wso2.carbon.identity.workflow.mgt.stub.metadata.Template" %>
<%@ page import="org.wso2.carbon.identity.workflow.mgt.stub.metadata.WorkflowWizard" %>
<%@ page import="org.wso2.carbon.identity.workflow.mgt.stub.metadata.bean.InputData" %>
<%@ page import="org.wso2.carbon.identity.workflow.mgt.stub.metadata.bean.Item" %>
<%@ page import="org.wso2.carbon.identity.workflow.mgt.stub.metadata.bean.MapType" %>
<%@ page import="org.wso2.carbon.identity.workflow.mgt.stub.metadata.bean.ParameterMetaData" %>
<%@ page import="org.wso2.carbon.identity.workflow.mgt.ui.WorkflowAdminServiceClient" %>
<%@ page import="org.wso2.carbon.identity.workflow.mgt.ui.WorkflowUIConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="java.util.Set" %>

<%

    WorkflowAdminServiceClient client;
    String bundle = "org.wso2.carbon.identity.workflow.mgt.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(bundle, request.getLocale());
    String forwardTo = null;


    String requestPath = "list-workflows";
    //'path' parameter to use to track parent wizard path if this wizard trigger by another wizard
    if (StringUtils.isNotBlank(request.getParameter(WorkflowUIConstants.PARAM_REQUEST_PATH)) && IdentityValidationUtil
            .isValidOverBlackListPatterns(request.getParameter(WorkflowUIConstants.PARAM_REQUEST_PATH),
                                          IdentityValidationUtil.ValidatorPattern.URI_RESERVED_EXISTS.name())) {
        requestPath = request.getParameter(WorkflowUIConstants.PARAM_REQUEST_PATH);
    }

    boolean isBack = false ;
    if(StringUtils.isNotBlank(request.getParameter(WorkflowUIConstants.PARAM_BACK)) && request
            .getParameter(WorkflowUIConstants.PARAM_BACK).equals("true")){
        isBack =  true ;
    }

    boolean isSelectTemplate = false;
    if(StringUtils.isNotBlank(request.getParameter(WorkflowUIConstants.PARAM_SELECT_ITEM)) && request
            .getParameter(WorkflowUIConstants.PARAM_SELECT_ITEM).equals("true")){
        isSelectTemplate =  true ;
    }

    String requestToken = request.getParameter(WorkflowUIConstants.PARAM_PAGE_REQUEST_TOKEN);

    String workflowName = request.getParameter(WorkflowUIConstants.PARAM_WORKFLOW_NAME);
    String description = request.getParameter(WorkflowUIConstants.PARAM_WORKFLOW_DESCRIPTION);

    String templateId = request.getParameter(WorkflowUIConstants.PARAM_TEMPLATE_ID);

    WorkflowWizard workflowWizard = null ;
    Template template = null ;

    Template[] templateList = null;
    Map<String,Map<String,Parameter>> parameterValues = new HashMap<String,Map<String,Parameter>>();

    try {


        if(StringUtils.isBlank(requestToken) || session.getAttribute(requestToken)==null){
            throw new WorkflowAdminServiceWorkflowException("This page is expired or can not access from this URL");
        }

        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext()
                        .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        client = new WorkflowAdminServiceClient(cookie, backendServerURL, configContext);

        workflowWizard = (WorkflowWizard)session.getAttribute(requestToken);

        //If there are no any template registered in OSGi, it is not possible to continue from here.
        templateList = client.listTemplates();
        if (templateList == null || templateList.length == 0) {
            throw new WorkflowAdminServiceWorkflowException("There is no any registered templates.");
        }

        if(!isBack) {

            if (workflowWizard.getTemplate() == null) {
                if (templateList.length == 1) {
                    template = templateList[0];
                } else if (StringUtils.isNotBlank(templateId)) {
                    template = client.getTemplate(templateId);
                }
                workflowWizard.setTemplate(template);
            } else {
                if (StringUtils.isNotBlank(templateId) &&
                    !workflowWizard.getTemplate().getTemplateId().equals(templateId)) {
                    template = client.getTemplate(templateId);
                    workflowWizard.setTemplate(template);
                    workflowWizard.setWorkflowImpl(null);
                }else{
                    template = workflowWizard.getTemplate();
                }
            }

            if(!isSelectTemplate) {
                workflowWizard.setWorkflowName(workflowName);
                workflowWizard.setWorkflowDescription(description);
            }
        }else{
            template = workflowWizard.getTemplate();
        }


        Parameter[] wfParameters = workflowWizard.getTemplateParameters();
        if(wfParameters != null && wfParameters.length > 0){
            for (Parameter parameter: wfParameters){
                Map<String, Parameter> stringParameterMap = parameterValues.get(parameter.getParamName());
                if(stringParameterMap == null){
                    stringParameterMap = new HashMap<String, Parameter>();
                    parameterValues.put(parameter.getParamName(),stringParameterMap);
                }
                stringParameterMap.put(parameter.getQName(),parameter);

            }
        }


    } catch (Exception e) {
        String message = resourceBundle.getString("workflow.error.when.initiating.service.client") + e.getMessage();
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
            label="workflow.template"
            resourceBundle="org.wso2.carbon.identity.workflow.mgt.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>


    <script type="text/javascript" src="js/jquery-3.6.0.js"></script>
    <script type="text/javascript" src="js/jquery-ui-1.13.2.min.js"></script>
    <script type="text/javascript" src="js/tokenizer.js"></script>
    <link rel="stylesheet" type="text/css" href="css/input_style.css">


    <script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../admin/js/cookies.js"></script>
    <script type="text/javascript" src="../admin/js/main.js"></script>

    <!-- Override carbon jquery from latest release of it, because this tokenizer support for latest one -->


    <style>

        .tknz-wrapper {
            width: 96%;
            height: 54px;
            margin: 5px;
            padding: 5px;
            overflow: auto;
            color: #fefefe;
            background: #fefefe;
            font-family: "Courier", Times, sans-serif;
            border: solid 1px #DFDFDF;
        }

    </style>

    <script type="text/javascript">

        function goBack() {
            location.href = "add-wf-wizard.jsp?<%=WorkflowUIConstants.PARAM_BACK%>=true&<%=WorkflowUIConstants.PARAM_PAGE_REQUEST_TOKEN%>=<%=Encode.forJavaScriptBlock(Encode.forUriComponent(requestToken))%>";
        }

        function doCancel() {
            function cancel() {
                location.href = '<%=requestPath%>.jsp?wizard=finish';
            }

            CARBON.showConfirmationDialog('<fmt:message key="confirmation.workflow.add.abort"/> ?',
                    cancel, null);
        }





        function selectTemplate(){
            var workflowForm = document.getElementById("id_workflow_template");
            workflowForm.submit();
        }

        var stepOrder = 0;
        function nextWizard(){
            if(!validateInputs()){
                alert("Required fields are missing");
                return ;
            }

            try {
                for (var currentStep = 1; currentStep <= stepOrder; currentStep++) {
                    var newValues = $("#p-step-" + currentStep + "-users").tokenizer('get');
                    $("#p-step-" + currentStep + "-users").val(newValues);
                    newValues = $("#p-step-" + currentStep + "-roles").tokenizer('get');
                    $("#p-step-" + currentStep + "-roles").val(newValues);
                }
            }catch(e){

            }

            var nextWizardForm = document.getElementById("id_nextwizard");
            nextWizardForm.submit();
        }



        function validateInputs(){
            cleanSteps();
            <%
            if(template !=null && template.getParametersMetaData() !=null && template.getParametersMetaData().getParameterMetaData() !=null){
                ParameterMetaData[] parameterMetaData = template.getParametersMetaData().getParameterMetaData();
                for(ParameterMetaData parameterMetaDataTmp : parameterMetaData){
                    if(parameterMetaDataTmp.getIsRequired()){
                        if(parameterMetaDataTmp.getInputType().equals(InputType.TEXT.value()) || parameterMetaDataTmp.getInputType().equals(InputType.TEXT_AREA.value())){
            %>
            if($("#<%=parameterMetaDataTmp.getName()%>").val() == ""){
                return false ;
            }
            <%
                         }else if(parameterMetaDataTmp.getInputType().equals(InputType.SELECT.value())){

            %>
            if($("#<%=parameterMetaDataTmp.getName()%>").val() == "<fmt:message key="select"/>"){
                return false ;
            }

            <%
                        }else if(parameterMetaDataTmp.getInputType().equals(InputType.MULTIPLE_STEPS_USER_ROLE.value())){

            %>
            if (stepOrder == 0 || ( $("#p-step-1-users").tokenizer('get') == "" && $("#p-step-1-roles").tokenizer('get') == "" )) {
                return false;
            }

            <%
                        }
                    }
                }
            }

            %>
            return true ;

        }

    </script>

    <div id="middle">

        <h2><fmt:message key='workflow.add'/></h2>

        <div id="workArea">

            <%
                if(templateList != null && templateList.length > 1){
            %>

            <form id="id_workflow_template" method="post" name="serviceAdd" action="template-wf-wizard.jsp">
                <input type="hidden" name="<%=WorkflowUIConstants.PARAM_PAGE_REQUEST_TOKEN%>" value="<%=Encode.forHtmlAttribute(requestToken)%>"/>
                <input type="hidden" name="<%=WorkflowUIConstants.PARAM_REQUEST_PATH%>" value="<%=Encode.forHtmlAttribute(requestPath)%>"/>
                <input type="hidden" name="<%=WorkflowUIConstants.PARAM_SELECT_ITEM%>" value="true"/>
                <table border="1">
                    <tr>
                        <td width="60px"><fmt:message key='workflow.template'/></td>
                        <td>
                            <select onchange="selectTemplate();" id="id_template" name="<%=WorkflowUIConstants.PARAM_TEMPLATE_ID%>"
                                    style="min-width: 30%">
                                    <option value="" selected><fmt:message key="select"/></option>
                                <%
                                    for (Template templateTmp : templateList) {
                                        String selected = "" ;
                                        if(template!=null && templateTmp.getTemplateId().equals(template.getTemplateId())){
                                            selected = "selected" ;
                                        }
                                %>
                                    <option value="<%=Encode.forHtmlAttribute(templateTmp.getTemplateId())%>" <%=selected%>>
                                        <%=Encode.forHtmlContent(templateTmp.getName())%>
                                    </option>
                                <%
                                    }
                                %>
                            </select>
                        </td>
                    </tr>
                </table>
            </form>

            <%
                }
            %>

            </br>

            <%
                if(template != null ){
            %>
            <form method="post" name="serviceAdd" id="id_nextwizard" action="workflowimpl-wf-wizard.jsp">
                <input type="hidden" name="<%=WorkflowUIConstants.PARAM_PAGE_REQUEST_TOKEN%>" value="<%=Encode.forHtmlAttribute(requestToken)%>"/>
                <input type="hidden" name="<%=WorkflowUIConstants.PARAM_REQUEST_PATH%>" value="<%=Encode.forHtmlAttribute(requestPath)%>"/>

                <table class="styledLeft">
                    <thead>
                    <tr>
                        <th><fmt:message key='workflow.template'/> : <%= Encode.forHtml(template.getName())%></th>
                    </tr>
                    </thead>
                    <tr>
                        <td class="formRow">
                            <table class="normal" style="width: 100%;">
                                <%
                                    ParameterMetaData[] parameterMetaData = template.getParametersMetaData().getParameterMetaData();
                                    if (parameterMetaData.length==0) {
                                %>
                                <tr>
                                    <td colspan="1"><fmt:message key="workflow.template.has.no.params"/></td>
                                </tr>
                                <%
                                } else {
                                    for (ParameterMetaData metaData : parameterMetaData) {
                                        if (metaData != null) {
                                %>
                                <tr>
                                    <td width="200px" style="vertical-align: top !important;"><%=Encode.forHtmlContent(metaData.getDisplayName())%><%=metaData.getIsRequired()?"<span style=\"color:red\">*</span>":""%>
                                    </td>
                                </tr>
                                <tr>
                                    <%
                                        if(metaData.getInputType().equals(InputType.TEXT.value())){
                                            String textTypeValue = "" ;
                                            if(parameterValues.get(metaData.getName()) != null && parameterValues.get(metaData.getName()).get(metaData.getName()) != null){
                                                textTypeValue = parameterValues.get(metaData.getName()).get(metaData.getName()).getParamValue();
                                            }
                                    %>
                                    <td>
                                        <input id="<%=Encode.forHtmlAttribute(metaData.getName())%>" name="<%=Encode.forHtmlAttribute(metaData.getName())%>"
                                                  title="<%=Encode.forHtmlAttribute(metaData.getDisplayName())%>" style="min-width: 30%" value="<%=Encode.forHtmlAttribute(textTypeValue)%>"/>
                                    </td>
                                    <%
                                    } else if(metaData.getInputType().equals(InputType.TEXT_AREA.value())){
                                        String textAreaTypeValue = "" ;
                                        if(parameterValues.get(metaData.getName()) != null && parameterValues.get(metaData.getName()).get(metaData.getName()) != null){
                                            textAreaTypeValue = parameterValues.get(metaData.getName()).get(metaData.getName()).getParamValue();
                                        }
                                    %>
                                    <td><textarea id="<%=Encode.forHtmlAttribute(metaData.getName())%>" name="<%=Encode.forHtmlAttribute(metaData.getName())%>" title="<%=Encode.forHtmlAttribute(metaData.getDisplayName())%>" style="min-width: 30%">
                                        <%= Encode.forHtmlContent(textAreaTypeValue)%></textarea>
                                    </td>
                                    <%
                                    } else if(metaData.getInputType().equals(InputType.SELECT.value())){
                                        String selectedValue = "" ;
                                        if(parameterValues.get(metaData.getName()) != null && parameterValues.get(metaData.getName()).get(metaData.getName()) != null){
                                            selectedValue = parameterValues.get(metaData.getName()).get(metaData.getName()).getParamValue();
                                        }
                                        InputData inputData = metaData.getInputData();
                                        MapType mapType = inputData.getMapType();
                                        Item[] items = mapType.getItem();
                                    %>
                                    <td>
                                        <select id="<%=Encode.forHtmlAttribute(metaData.getName())%>" name="<%=Encode.forHtmlAttribute(metaData.getName())%>" style="min-width: 30%">
                                            <option value="<fmt:message key="select"/>"><fmt:message key="select"/></option>
                                        <%
                                            for (Item item: items) {
                                                if (item != null) {
                                                    boolean select = item.getValue().equals(selectedValue);
                                        %>
                                            <option value="<%=Encode.forHtmlAttribute(item.getKey())%>" <%=select ? "selected" :""%>><%=Encode.forHtmlContent(item.getValue())%></option>
                                        <%
                                                }
                                            }
                                        %>
                                    </select>
                                    </td>
                                    <%
                                    } else if (metaData.getInputType().equals(InputType.MULTIPLE_STEPS_USER_ROLE.value())) {
                                        Map<String, Parameter> stringParameterMap =  parameterValues.get(metaData.getName());
                                    %>


                                    <script>


                                        <%

                                        Map<String,Map<String,String>> stepMap = new HashMap<String,Map<String,String>>();
                                        if(stringParameterMap !=null ) {
                                            Set<String> keys = stringParameterMap.keySet();
                                            for (String key : keys) {
                                                String[] split = key.split("-");
                                                Map<String, String> stringStringMap = stepMap.get(split[2]);
                                                if (stringStringMap == null) {
                                                    stringStringMap = new HashMap<String, String>();
                                                    stepMap.put(split[2], stringStringMap);
                                                }
                                                stringStringMap
                                                        .put(split[3], stringParameterMap.get(key).getParamValue());
                                            }
                                        }
                                        %>
                                        jQuery(document).ready(function(){

                                            jQuery('h2.trigger').click(function(){
                                                if (jQuery(this).next().is(":visible")) {
                                                    this.className = "active trigger step_heads";
                                                } else {
                                                    this.className = "trigger step_heads";
                                                }
                                                jQuery(this).next().slideToggle("fast");
                                                return false; //Prevent the browser jump to the link anchor
                                            });

                                            jQuery('#stepsAddLink').click(function(){
                                                cleanSteps();
                                                addStep("","");

                                            });

                                            <%

                                               for(int a =0 ; a<stepMap.size();a++) {
                                                   Map<String, String> stringStringMap = stepMap.get((a + 1) + "");
                                                   String users = stringStringMap.get("users");
                                                   String roles = stringStringMap.get("roles");
                                            %>

                                                try {
                                                    addStep("<%=Encode.forJavaScriptBlock(users)%>", "<%=Encode.forJavaScriptBlock(roles)%>");
                                                }catch(e){

                                                }



                                            <%


                                                }

                                            %>

                                        });


                                        function addStep(users,roles){

                                            stepOrder++;

                                            var stepHtml = '<div class="toggle_container sectionSub" id="div_step_head_'+stepOrder+'" style="border:solid 1px #ccc;padding: 10px;margin-bottom:10px;" >' +
                                                           '<h2 id="step_head_'+stepOrder+'" class="trigger active step_heads" style="background-color: beige; clear: both;">' +
                                                           '<input type="hidden" value="'+stepOrder+'" name="approve_step" id="approve_step">' +
                                                           '<a class="step_order_header" href="#">Step '+stepOrder+'</a>' +
                                                           '<a onclick="deleteStep(this);return false;" href="#" class="icon-link" style="background-image: url(images/delete.gif);float:right;width: 9px;" name="delete-obj" id="delete-obj-'+stepOrder+'"></a>' +
                                                           '</h2>' +
                                                           '<table style="width:100%;">' +
                                                           '<tr><td id="search_step_head_'+stepOrder+'"></td></tr>' +
                                                           '<tr id="id_step_roles_'+stepOrder+'" style="display:none;">' +
                                                           '<td style="width:100%;">' +
                                                           '<table  style="width:100%;">' +
                                                           '<tr><td width="40px">Roles</td><td onclick="moveSearchController(\''+stepOrder+'\',\'roles\', false);"><input readonly  name="<%=Encode.forJavaScriptBlock(Encode.forHtmlAttribute(metaData.getName()))%>-step-'+stepOrder+'-roles" id="p-step-'+stepOrder+'-roles"  type="text"  class="tokenizer_'+stepOrder+'"/></td></tr>' +
                                                           '</table>' +
                                                           '</td>' +
                                                           '</tr>' +
                                                           '<tr id="id_step_users_'+stepOrder+'" style="width:100%;display:none;">' +
                                                           '<td style="width:100%;">' +
                                                           '<table style="width:100%;">' +
                                                           '<tr><td width="40px">Users</td><td onclick="moveSearchController(\''+stepOrder+'\',\'users\', false);"><input readonly  name="<%=Encode.forJavaScriptBlock(Encode.forHtmlAttribute(metaData.getName()))%>-step-'+stepOrder+'-users" id="p-step-'+stepOrder+'-users" type="text" class="tokenizer_'+stepOrder+'"/></td></tr>' +
                                                           '</table>' +
                                                           '</td>' +
                                                           '</tr>' +
                                                           '</table>' +
                                                           '</div>' ;

                                            jQuery('#stepsConfRow').append(jQuery(stepHtml));

                                            //Move search component to selected step
                                            moveSearchController(stepOrder, "roles", true)

                                            //Init tokanizer for users and roles inputs in given step.
                                            initInputs("p-step-"+stepOrder+"-roles");
                                            initInputs("p-step-"+stepOrder+"-users");

                                            if(users !=null && users!="") {
                                                getSelectedItems(users.split(","), "users");
                                            }
                                            if(roles !=null && roles!="") {
                                                getSelectedItems(roles.split(","), "roles");
                                            }
                                        }

                                        function initInputs(id){
                                            $("#" + id).tokenizer({
                                                label: ''
                                            });
                                        }


                                        function moveSearchController(step, category, init){

                                            $("#id_search_controller").detach().appendTo("#search_step_head_"+step);
                                            $("#id_search_controller").show();
                                            $("#currentstep").val(step);

                                            loadCategory(category, init);
                                        }




                                        function deleteStep(obj){

                                            $("#id_search_controller").hide();
                                            $("#id_search_controller").detach().appendTo("#id_search_controller_base");

                                            stepOrder--;
                                            jQuery(obj).parent().next().remove();
                                            jQuery(obj).parent().parent().remove();
                                            if($('.step_heads').length > 0){
                                                var newStepOrderVal = 1;
                                                $.each($('.step_heads'), function(){
                                                    var oldApproveStepVal = parseInt($(this).find('input[name="approve_step"]').val());

                                                    //Changes in header
                                                    $(this).attr('id','step_head_'+newStepOrderVal);
                                                    $(this).find('input[name="approve_step"]').val(newStepOrderVal);
                                                    $(this).find('a[name="delete-obj"]').attr('id','delete-obj-'+newStepOrderVal);

                                                    $(this).find('.step_order_header').text('Step '+newStepOrderVal);

                                                    var textArea_Users = $('#p-step-'+oldApproveStepVal+'-users');
                                                    textArea_Users.attr('id','p-step-'+newStepOrderVal+'-users');
                                                    textArea_Users.attr('name','UserAndRole-step-'+newStepOrderVal+'-users');

                                                    var textArea_Roles = $('#p-step-'+oldApproveStepVal+'-roles');
                                                    textArea_Roles.attr('id','p-step-'+newStepOrderVal+'-roles');
                                                    textArea_Roles.attr('name','UserAndRole-step-'+newStepOrderVal+'-roles');

                                                    newStepOrderVal++;
                                                });
                                            }
                                        }


                                        function getSelectedItems(allList, category){

                                            if(allList!=null && allList.length!=0) {
                                                var currentStep = $("#currentstep").val();

                                                $("#id_step_"+category+"_" + currentStep).show();
                                                var currentValues = $("#p-step-" + currentStep + "-" + category).val();
                                                for(var i=0;i<allList.length;i++) {
                                                    var newItem = allList[i];
                                                    $("#p-step-" + currentStep + "-" + category).tokenizer('push',
                                                            newItem);
                                                }
                                                var newValues = $("#p-step-" + currentStep + "-" + category).tokenizer('get');

                                            }


                                        }

                                        function cleanSteps(){
                                            for(var x = 1 ; x<= stepOrder ;  ){
                                                if ($("#p-step-" + x + "-users").tokenizer('get') == "" &&
                                                        $("#p-step-" + x + "-roles").tokenizer('get') == "") {
                                                    deleteStep(document.getElementById("delete-obj-"+x));
                                                }else{
                                                    x++ ;
                                                }
                                            }
                                        }
                                    </script>
                                    <td>
                                        <a id="stepsAddLink" class="icon-link" style="background-image:url(images/add.png);margin-left:0"><fmt:message key='workflow.template.button.add.step'/></a>
                                       <div style="margin-bottom:10px;width: 100%" id="stepsConfRow">
                                       </div>
                                    </td>
                                    <%
                                    } else {
                                    %>
                                        <%--Appending 'p-' to differentiate dynamic params--%>
                                    <td>


                                        Test
                                    </td>
                                    <%

                                            }
                                        //todo:handle 'required' value

                                        }
                                    %>
                                </tr>
                                <%
                                        }
                                    }

                                %>
                            </table>
                        </td>
                    </tr>
                </table>
                <br/>
                <table class="styledLeft">

                    <tr>
                        <td class="buttonRow">
                            <input class="button" value="<fmt:message key="back"/>" type="button" onclick="goBack();">
                            <input class="button" value="<fmt:message key="next"/>" type="button" onclick="nextWizard();"/>
                            <input class="button" value="<fmt:message key="cancel"/>" type="button"
                                   onclick="doCancel();"/>
                        </td>
                    </tr>
                </table>
                <br/>
            </form>
            <%
                }
            %>
        </div>
    </div>


    <!-- Using general search component, we have added for user/role search -->
    <div id="id_search_controller_base">
        <div id="id_search_controller" style="display:none;">
            <input type="hidden" id="currentstep" name="currentstep" value=""/>
            <div id="id_user_search">
                <jsp:include page="../userstore/user-role-search.jsp">
                    <jsp:param name="function-get-all-items" value="getSelectedItems"/>
                </jsp:include>
            </div>
        </div>
    </div>


</fmt:bundle>