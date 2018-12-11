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
<%@include file="localize.jsp" %>

<%! private static final String INVALID_MESSAGE_MESSAGE =
        "The message was not recognized by the SAML 2.0 SSO Provider. Please check the logs for more details";
    private static final String EXCEPTION_MESSAGE = "Please try login again.";
    private static final String INVALID_MESSAGE_STATUS = "Not a valid SAML 2.0 Request Message!";
    private static final String EXCEPTION_STATUS = "Error when processing the authentication request!";
%><%
    String stat = request.getParameter(Constants.STATUS);
    String statusMessage = request.getParameter(Constants.STATUS_MSG);
    
    String errorStat = stat;
    String errorMsg = statusMessage;
    
    boolean unrecognizedStatus = true;
    if (EXCEPTION_STATUS.equals(stat) || INVALID_MESSAGE_STATUS.equals(stat)) {
        errorStat = "error.when.processing.authentication.request";
        unrecognizedStatus = false;
    }
    
    boolean unrecognizedStatusMsg = true;
    if (EXCEPTION_MESSAGE.equals(statusMessage) || INVALID_MESSAGE_MESSAGE.equals(statusMessage)) {
        errorMsg = "please.try.login.again";
        unrecognizedStatusMsg = false;
    }
    
    if (stat == null || statusMessage == null || unrecognizedStatus || unrecognizedStatusMsg) {
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
