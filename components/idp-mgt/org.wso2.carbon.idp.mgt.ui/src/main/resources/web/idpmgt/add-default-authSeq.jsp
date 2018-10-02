<%--
  ~ Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~  Licensed under the Apache License, Version 2.0 (the "License");
  ~  you may not use this file except in compliance with the License.
  ~  You may obtain a copy of the License at
  ~
  ~  http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~  Unless required by applicable law or agreed to in writing, software
  ~  distributed under the License is distributed on an "AS IS" BASIS,
  ~  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~  See the License for the specific language governing permissions and
  ~  limitations under the License.
  --%>

<%@ page import="java.util.ResourceBundle" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" %>

<carbon:breadcrumb label="breadcrumb.default.seq.mgt"
                   resourceBundle="org.wso2.carbon.idp.mgt.ui.i18n.Resources"
                   topPage="true"
                   request="<%=request%>"/>
<script type="text/javascript" src="extensions/js/vui.js"></script>
<script type="text/javascript" src="../extensions/core/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<script type="text/javascript" src="../identity/validation/js/identity-validate.js"></script>
<jsp:include page="../dialog/display_messages.jsp"/>

<%
    String[] createError = (String[]) request.getSession().getAttribute("createError");
    if (createError == null) {
        createError = new String[0];
    }

    String BUNDLE = "org.wso2.carbon.idp.mgt.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
%>
<script type="text/javascript">
    var openFile = function (event) {
        var input = event.target;
        var reader = new FileReader();
        reader.onload = function () {
            document.getElementById('sequence-file-content').value = reader.result;
        };
        document.getElementById('sequence-file-name').value = input.files[0].name;
        reader.readAsText(input.files[0]);
    };

    function importDefaultAuthnSeq() {
        var seqContent = $.trim(document.getElementById('sequence-file-content').value);
        if (seqContent === null || 0 === seqContent.length) {
            CARBON.showWarningDialog('<%=resourceBundle.getString("alert.error.default.seq.file.available")%>');
            location.href = '#';
            return false;
        } else {
            $("#add-default-auth-seq-form").submit();
            return true;
        }
    }

    $(function () {
        $("#createErrorMsgDialog").dialog({
            autoOpen: false,
            modal: true,
            buttons: {
                OK: closeCreateErrorDialog
            },
            width: "fit-content"
        });
    });

    function closeCreateErrorDialog() {
        $(this).dialog("close");
        <%
         request.getSession().removeAttribute("createError");
        %>
    }

    window.onload = function () {
        showManual();
        <% if (createError.length > 0) { %>
        $("#createErrorMsgDialog").show();
        $("#createErrorMsgDialog").dialog("open");
        <% } %>
    };

    function showManual() {
        $("#add-default-auth-seq-form").show();
        $("#createErrorMsgDialog").hide();
    }
</script>
<fmt:bundle basename="org.wso2.carbon.idp.mgt.ui.i18n.Resources">
    <div id="middle">
        <h2>
            <fmt:message key='title.default.seq.add'/>
        </h2>
        <div id="workArea">
            <form id="add-default-auth-seq-form" name="add-default-auth-seq-form" method="post"
                  action="add-default-authSeq-finish-ajaxprocessor.jsp">
                <div class="sectionSeperator togglebleTitle"><fmt:message key='upload.default.seq.file'/></div>
                <div class="sectionSub">
                    <table class="carbonFormTable">
                        <tr>
                            <td style="width:15%" class="leftCol-med labelField"><fmt:message
                                    key='config.default.seq.name'/>:
                            </td>
                            <td>
                                <textarea style="width:50%" name="sequence-name" id="sequence-name"
                                          class="text-box-big"></textarea>
                                <div class="sectionHelp">
                                    <fmt:message key='help.default.seq.name'/>
                                </div>
                            </td>
                        </tr>
                        <tr>
                            <td style="width:15%" class="leftCol-med labelField"><fmt:message
                                    key='config.default.seq.file.location'/>:<span class="required">*</span>
                            </td>
                            <td>
                                <input type="file" class="button" id="sp_file" name="sp_file"
                                       onchange='openFile(event)'/>
                                <textarea hidden="hidden" name="sequence-file-content"
                                          id="sequence-file-content"></textarea>
                                <textarea hidden="hidden" name="sequence-file-name" id="sequence-file-name"></textarea>
                                <div class="sectionHelp">
                                    <fmt:message key='help.default.seq.content'/>
                                </div>
                            </td>
                        </tr>
                    </table>
                </div>
                <div class="buttonRow">
                    <input type="button" class="button" value="<fmt:message key='button.import.default.seq'/>"
                           onclick="importDefaultAuthnSeq();"/>
                    <input type="button" class="button"
                           onclick="javascript:location.href='idp-mgt-edit-local.jsp?selectDefaultSeq=true'"
                           value="<fmt:message key='button.cancel'/>"/>
                </div>
            </form>
        </div>
    </div>
    <div id="createErrorMsgDialog" title='WSO2 Carbon'>
        <div id="messagebox-error">
            <h3>
                <fmt:message key="alert.error.add.default.seq"/>
            </h3>
            <table style="margin-top:10px;">
                <%
                    for (String error : createError) {
                %>
                <tr>
                    <td><%=error%>
                    </td>
                </tr>
                <%
                    }
                %>
            </table>
        </div>
    </div>
</fmt:bundle>
