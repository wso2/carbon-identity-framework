<%--
  ~ Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="e" uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<jsp:directive.include file="../init-url.jsp"/>

<script>
    function toggleSessionCheckboxes() {
        var masterCheckbox = document.getElementById("masterCheckbox").checked;
        var checkboxes = document.sessionsForm.sessionsToKill;

        if (checkboxes instanceof RadioNodeList) {
            for (i = 0; i < checkboxes.length; i++) {
                checkboxes[i].checked = masterCheckbox ? true : false;
            }
        } else {
            checkboxes.checked = masterCheckbox ? true : false;
        }
    }

    function toggleMasterCheckbox() {
        var masterCheckbox = document.getElementById("masterCheckbox");
        var checkboxes = document.sessionsForm.sessionsToKill;

        if (checkboxes instanceof RadioNodeList) {
            for (i = 0; i < checkboxes.length; i++) {
                if (!checkboxes[i].checked) {
                    masterCheckbox.checked = false;
                    return;
                }
            }
            masterCheckbox.checked = true;
        } else {
            masterCheckbox.checked = checkboxes.checked;
        }
    }

    function validateForm(submittedAction) {
        if (submittedAction === "killAction") {
            var checkboxes = document.sessionsForm.sessionsToKill;

            if (checkboxes instanceof RadioNodeList) {
                for (i = 0; i < checkboxes.length; i++) {
                    if (checkboxes[i].checked) {
                        return true;
                    }
                }
            } else if (checkboxes.checked) {
                return true;
            }
        } else if (submittedAction === "abortAction") {
            return true;
        }
        return false;
    }
</script>

<div>
    <h2 class="wr-title uppercase blue-bg padding-double white boarder-bottom-blue margin-none">
        Multiple Active Session(s) Found
    </h2>
</div>

<div class="boarder-all ">
    <div class="clearfix"></div>
    <div class="padding-double login-form">

        <form name="sessionsForm" action="<%=commonauthURL%>" method="POST"
              onsubmit="return validateForm(this.submitted)">
            <h4 class="text-center padding-double">
                You currently have <fmt:formatNumber value='${requestScope.data["maxSessionCount"]}'/> active sessions.
                You are not allowed to have more than <fmt:formatNumber
                    value='${requestScope.data["maxSessionCount"]}'/> active sessions.
            </h4>
            <table class="table table-striped table-bordered">
                <thead>
                <tr>
                    <th>#</th>
                    <th>Browser</th>
                    <th>Platform</th>
                    <th>Last Accessed</th>
                    <th><input type="checkbox" onchange="toggleSessionCheckboxes()" id="masterCheckbox" checked></th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items='${requestScope.data["sessions"]}' var="session" varStatus="loop">
                    <tr>
                        <td>${loop.index + 1}</td>
                        <td>${session[2]}</td>
                        <td>${session[3]}</td>
                        <jsp:useBean id="dateValue" class="java.util.Date"/>
                        <jsp:setProperty name="dateValue" property="time" value="${session[1]}"/>
                        <td><fmt:formatDate value="${dateValue}" pattern="MM-dd-yyyy 'at' hh:mm a"/></td>
                        <td><input type="checkbox" onchange="toggleMasterCheckbox()" value="${session[0]}"
                                   name="sessionsToKill" checked></td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
            <h4 class="text-center padding-double">
                You need to either kill unwanted active sessions & proceed, or abort the login.
                <br>
                Please select your option.
            </h4>
            <input type="hidden" id="promptResp" name="promptResp" value="true">
            <input type="hidden" id="promptId" name="promptId" value="${requestScope.promptId}">
            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group required">
                <input name="action.killActiveSessions" type="submit" onclick="this.form.submitted='killAction';"
                       class="wr-btn grey-bg col-xs-12 col-md-12 col-lg-12 uppercase font-extra-large"
                       value="Kill Active Sessions & Proceed">
            </div>

            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group required">
                <input name="action.abortLogin" type="submit" onclick="this.form.submitted='abortAction';"
                       class="wr-btn grey-bg col-xs-12 col-md-12 col-lg-12 uppercase font-extra-large"
                       value="Abort Login">
            </div>
        </form>

        <div class="clearfix"></div>
    </div>
</div>


<!-- /content -->
