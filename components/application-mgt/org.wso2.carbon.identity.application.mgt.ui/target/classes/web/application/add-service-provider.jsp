<!--
~ Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"%>

<carbon:breadcrumb label="breadcrumb.service.provider" resourceBundle="org.wso2.carbon.identity.application.mgt.ui.i18n.Resources" topPage="true" request="<%=request%>" />
<jsp:include page="../dialog/display_messages.jsp"/>

<script type="text/javascript" src="extensions/js/vui.js"></script>
<script type="text/javascript" src="../extensions/core/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<script type="text/javascript" src="../identity/validation/js/identity-validate.js"></script>
<jsp:include page="../dialog/display_messages.jsp" />

<script type="text/javascript">
function createAppOnclick() {
    var spName = document.getElementById("spName").value;
    var description = document.getElementById("sp-description").value;
    if( spName == '') {
        CARBON.showWarningDialog('Please provide Service Provider ID');
        location.href = '#';
    } else if (!validateTextForIllegal(document.getElementById("spName"))) {
        return false;
    }else {
        location.href='add-service-provider-finish.jsp?spName=' + spName+'&sp-description='+description;
    }
}

function validateTextForIllegal(fld) {
    var isValid = doValidateInput(fld, "Provided Service Provider name is invalid.");
    if (isValid) {
        return true;
    } else {
        return false;
    }
}

</script>

<fmt:bundle basename="org.wso2.carbon.identity.application.mgt.ui.i18n.Resources">
    <div id="middle">
        <h2>
            <fmt:message key='title.service.providers.add'/>
        </h2>
        <div id="workArea">
            <form id="idp-mgt-edit-form" name="add-service-provider-form" method="post" action="add-service-provider-finish.jsp" enctype="multipart/form-data" >
            <div class="sectionSeperator togglebleTitle"><fmt:message key='title.config.app.basic.config'/></div>
            <div class="sectionSub">
                <table class="carbonFormTable">
                    <tr>
                        <td style="width:15%" class="leftCol-med labelField"><fmt:message key='config.application.info.basic.name'/>:<span class="required">*</span></td>
                        <td>
                            <input id="spName" name="spName" type="text" value="" white-list-patterns="^[a-zA-Z0-9._|-]*$" autofocus/>
                            <div class="sectionHelp">
                                <fmt:message key='help.name'/>
                            </div>
                        </td>
                    </tr>
                    <tr>
                       <td class="leftCol-med labelField">Description:</td>
                     <td>
                        <textarea style="width:50%" type="text" name="sp-description" id="sp-description" class="text-box-big"></textarea>
                        <div class="sectionHelp">
                                <fmt:message key='help.desc'/>
                            </div>
                        </td>
                    </tr>
                </table>
            </div>
            <div class="buttonRow">
                <input type="button" class="button"  value="<fmt:message key='button.add.service.providers'/>" onclick="createAppOnclick();"/>
                <input type="button" class="button" onclick="javascript:location.href='list-service-providers.jsp'" value="<fmt:message key='button.cancel'/>" />
            </div>
            </form>
        </div>
    </div>
</fmt:bundle>