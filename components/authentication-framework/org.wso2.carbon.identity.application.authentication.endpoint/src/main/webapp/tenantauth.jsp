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

<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.util.TenantDataManager" %>
<%@ page import="java.util.List" %>
<%@ page import="org.owasp.encoder.Encode" %>

<form action="<%=commonauthURL%>" method="post" id="loginForm">
    <% if (Boolean.parseBoolean(loginFailed)) { %>
    <div class="alert alert-danger" id="error-msg">
        <%=AuthenticationEndpointUtil.i18n(resourceBundle, "username.or.password.invalid")%>
    </div>
    <%}%>


    <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">
        <select class="form-control" id='tenantList' name="tenantList" size='1'>
            <option value="<%=AuthenticationEndpointUtil.i18n(resourceBundle, "select.tenant.dropdown.display.name")%>">
                <%=AuthenticationEndpointUtil.i18n(resourceBundle, "select.tenant.dropdown.display.name")%>
            </option>
            <option value="<%=AuthenticationEndpointUtil.i18n(resourceBundle, "super.tenant")%>">
                <%=AuthenticationEndpointUtil.i18n(resourceBundle, "super.tenant.display.name")%>
            </option>

            <%
                List<String> tenantDomainsList = TenantDataManager.getAllActiveTenantDomains();
                if (!tenantDomainsList.isEmpty()) {
                    for (String tenant : tenantDomainsList) {
            %>
            <option value="<%=Encode.forHtmlAttribute(tenant)%>"><%=Encode.forHtmlContent(tenant)%>
            </option>
            <%
                    }
                }
            %>
        </select>
    </div>

    <input type="hidden" id='username' name='username'/>

    <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">
        <input id='username_tmp' name="username_tmp" type="text" class="form-control" tabindex="0"
               placeholder="<%=AuthenticationEndpointUtil.i18n(resourceBundle, "username")%>">
    </div>
    <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">
        <input id="password" name="password" type="password" class="form-control"
               placeholder="<%=AuthenticationEndpointUtil.i18n(resourceBundle, "password")%>">
    </div>
    <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">
        <input type="hidden" name="sessionDataKey" value='<%=Encode.forHtmlAttribute
            (request.getParameter("sessionDataKey"))%>'/>
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
                    type="submit" onclick="appendTenantDomain();">
                    <%=AuthenticationEndpointUtil.i18n(resourceBundle, "login")%>
            </button>
        </div>
    </div>

    <%if(request.getParameter("relyingParty").equals("wso2.my.dashboard")) { %>
    <a id="registerLink" href="create-account.jsp?sessionDataKey=<%=Encode.forHtmlAttribute
            (request.getParameter("sessionDataKey"))%>" class="font-large">
        <%=AuthenticationEndpointUtil.i18n(resourceBundle, "create.an.account")%>
    </a>
    <%} %>

    <script>

        /**
         * Append the tenant domain to the username
         */
        function appendTenantDomain() {
            var element = document.getElementById("tenantList");
            var tenantDomain = element.options[element.selectedIndex].value;

            setSelectedTenantCookie(tenantDomain, 30);

            if (tenantDomain != "<%=AuthenticationEndpointUtil.i18n(resourceBundle,"select.tenant.dropdown.display.name")%>") {

                var username = document.getElementsByName("username_tmp")[0].value;
                var userWithDomain = username + "@" + tenantDomain;

                document.getElementsByName("username")[0].value = userWithDomain;
            }
        }

        /**
         * Write the selected tenant domain to the cookie
         */
        function setSelectedTenantCookie(cvalue, exdays) {
            var date = new Date();
            date.setTime(date.getTime() + (exdays * 24 * 60 * 60 * 1000));
            var expires = "expires=" + date.toUTCString();
            document.cookie = "selectedTenantDomain=" + cvalue + "; " + expires + "; secure";
        }

        /**
         * Get the previously selected tenant domain from the cookie
         */
        function getSelectedTenantCookie() {
            var selectedTenantDomain = "";
            var name = "selectedTenantDomain=";
            var cookieItems = document.cookie.split(';');

            for (var i = 0; i < cookieItems.length; i++) {
                var item = cookieItems[i];
                item = item.trim();

                if (item.indexOf(name) != -1) {
                    selectedTenantDomain = item.substring(name.length, item.length);
                    break;
                }
            }
            return selectedTenantDomain;
        }

        /**
         * Select the tenant domain based on the previously selected tenant domain in cookie
         */
        function selectTenantFromCookie() {
            var tenant = getSelectedTenantCookie();
            var element = document.getElementById("tenantList");

            for (var i = 0; i < element.options.length; i++) {
                if (element.options[i].value == tenant) {
                    element.value = tenant;
                    break;
                }
            }

            //remove super tenant from dropdown based on the properties
            var superTenant = "<%=AuthenticationEndpointUtil.i18n(resourceBundle,"super.tenant")%>";
            if (superTenant == null || superTenant == "") {
                for (i = 0; i < element.options.length; i++) {
                    if (element.options[i].value == superTenant) {
                        element.remove(i);
                        break;
                    }
                }
            }
        }
    </script>
    <div class="clearfix"></div>
</form>
