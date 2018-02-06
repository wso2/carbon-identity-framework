<%--
  ~ Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

<%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.util.Constants" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@include file="localize.jsp" %>

<%
    String stat = request.getParameter(Constants.STATUS);
    String statusMessage = request.getParameter(Constants.STATUS_MSG);

    String errorStat = stat;
    String errorMsg = statusMessage;

    boolean unrecognizedStatus = true;
    if (stat.equals("Error when processing the authentication request!") ||
            stat.equals("Not a valid SAML 2.0 Request Message!")) {
        errorStat = "error.when.processing.authentication.request";
        unrecognizedStatus = false;
    }

    boolean unrecognizedStatusMsg = true;
    if (statusMessage.equals("Please try login again.") ||
            statusMessage.equals("The message was not recognized by the SAML 2.0 SSO Provider. Please check the logs for more details")) {
        errorMsg = "please.try.login.again";
        unrecognizedStatusMsg = false;
    }

    if (stat == null || statusMessage == null || unrecognizedStatus || unrecognizedStatusMsg) {
        stat = "Authentication Error !";
        statusMessage = "Something went wrong during the authentication process. Please try signing in again.";
        errorStat = "authentication.error";
        errorMsg = "something.went.wrong.during.authentication";
    }
    session.invalidate();
%>
<style>
    .info-box {
        background-color: #EEF3F6;
        border: 1px solid #ABA7A7;
        font-size: 13px;
        font-weight: bold;
        margin-bottom: 10px;
        padding: 10px;
    }
</style>

    <div id="middle">
        <h2><%=AuthenticationEndpointUtil.i18n(resourceBundle, "saml.sso")%></h2>

        <div id="workArea">
            <div class="info-box">
                <%=AuthenticationEndpointUtil.i18n(resourceBundle, errorStat)%>
            </div>
            <table class="styledLeft">
                <tbody>
                <tr>
                    <td><%=AuthenticationEndpointUtil.i18n(resourceBundle, errorMsg)%>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
