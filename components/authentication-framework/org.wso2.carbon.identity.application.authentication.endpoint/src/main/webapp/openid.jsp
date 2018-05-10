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

<%@ page import="org.owasp.encoder.Encode" %>

<form action="<%=commonauthURL%>" method="post" id="loginForm" class="form-horizontal">
    <%
        loginFailed = request.getParameter("loginFailed");
        if (loginFailed != null) {

    %>
    <div class="alert alert-danger">
         <%=AuthenticationEndpointUtil.i18nBase64(resourceBundle,request.getParameter("errorMessage"))%>
    </div>
    <% } %>

    <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">
        <input class="input-large" type="text" id="claimed_id" name="claimed_id" size='30'
               placeholder="<%=AuthenticationEndpointUtil.i18n(resourceBundle, "openid")%>"/>
        <input type="hidden" name="sessionDataKey"
               value='<%=Encode.forHtmlAttribute(request.getParameter("sessionDataKey"))%>'/>
    </div>

    <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">
        <div class="checkbox">
            <label>
                <input type="checkbox" id="chkRemember" name="chkRemember">
                <%=AuthenticationEndpointUtil.i18n(resourceBundle, "remember.me")%>
            </label>
        </div>
        <br>

        <div class="form-actions">
            <button
                    class="wr-btn grey-bg col-xs-12 col-md-12 col-lg-12 uppercase font-extra-large"
                    type="submit">
                    <%=AuthenticationEndpointUtil.i18n(resourceBundle, "login")%>
            </button>
        </div>
    </div>


    <div class="clearfix"></div>
</form>
