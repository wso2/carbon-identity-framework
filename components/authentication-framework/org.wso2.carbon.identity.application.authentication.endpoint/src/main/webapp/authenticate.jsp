<%--
  ~ Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~ WSO2 Inc. licenses this file to you under the Apache License,
  ~ Version 2.0 (the "License"); you may not use this file except
  ~ in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~  http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  --%>
<%@ page import="org.apache.commons.collections.MapUtils" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page
        import="org.wso2.carbon.identity.application.authentication.endpoint.client.AuthAPIServiceClient" %>
<%@ page
        import="org.wso2.carbon.identity.application.authentication.endpoint.client.model.AuthenticationErrorResponse" %>
<%@ page
        import="org.wso2.carbon.identity.application.authentication.endpoint.client.model.AuthenticationResponse" %>
<%@ page
        import="org.wso2.carbon.identity.application.authentication.endpoint.client.model.AuthenticationSuccessResponse" %>
<%@ page import="java.util.HashMap" %>
<%@ page
        import="java.util.Map" %>
<%@ page import="java.util.StringTokenizer" %>
<%@ page import="static org.wso2.carbon.identity.application.authentication.endpoint.util.Constants.STATUS" %>
<%@ page
        import="static org.wso2.carbon.identity.application.authentication.endpoint.util.Constants.AUTHENTICATION_MECHANISM_NOT_CONFIGURED" %>
<%@ page import="static org.wso2.carbon.identity.application.authentication.endpoint.util.Constants.STATUS_MSG" %>
<%@ page
        import="static org.wso2.carbon.identity.application.authentication.endpoint.util.Constants.CONFIGURATION_ERROR" %>
<%@ page import="static org.wso2.carbon.identity.application.authentication.endpoint.util.Constants.ERROR_CODE" %>
<%@ page import="static org.wso2.carbon.identity.application.authentication.endpoint.util.Constants.ERROR_MSG" %>
<%@ page import="static org.wso2.carbon.identity.application.authentication.endpoint.util.Constants.SESSION_DATA_KEY" %>
<%@ page
        import="static org.wso2.carbon.identity.application.authentication.endpoint.util.Constants.AUTHENTICATION_REST_ENDPOINT_URL" %>
<jsp:directive.include file="init-url.jsp"/>

<%
    String sessionDataKey = request.getParameter(SESSION_DATA_KEY);
    String token = "";
    String authAPIURL = application.getInitParameter(AUTHENTICATION_REST_ENDPOINT_URL);
    if (StringUtils.isNotBlank(authAPIURL)) {

        AuthAPIServiceClient authAPIServiceClient = new AuthAPIServiceClient(authAPIURL);
        AuthenticationResponse authenticationResponse = authAPIServiceClient.authenticate(request.getParameter("username"), request.getParameter("password"));
        if (authenticationResponse instanceof AuthenticationSuccessResponse) {

            AuthenticationSuccessResponse successResponse = (AuthenticationSuccessResponse) authenticationResponse;
            token = successResponse.getToken();
        } else {

            // Populate a key value map from the query string received.
            Map<String, String> queryParamMap = new HashMap<String, String>();
            String queryString = request.getQueryString();
            if (StringUtils.isNotBlank(queryString)) {
                StringTokenizer stringTokenizer = new StringTokenizer(queryString, "&");
                while (stringTokenizer.hasMoreTokens()) {
                    String queryParam = stringTokenizer.nextToken();
                    String[] queryParamKeyValueArray = queryParam.split("=", 2);
                    queryParamMap.put(queryParamKeyValueArray[0], queryParamKeyValueArray[1]);
                }
            }

            AuthenticationErrorResponse errorResponse = (AuthenticationErrorResponse) authenticationResponse;
            // Update the query parameter map with the parameters received in error response.
            StringBuilder queryStringBuilder = new StringBuilder();
            if (MapUtils.isNotEmpty(errorResponse.getProperties())) {
                Map<String, String> propertyMap = errorResponse.getProperties();
                for (Map.Entry<String, String> entry : propertyMap.entrySet()) {
                    queryParamMap.put(Encode.forUriComponent(entry.getKey()), Encode.forUriComponent(entry.getValue()));
                }
            }

            queryParamMap.put(ERROR_CODE, Encode.forUriComponent(errorResponse.getCode()));
            queryParamMap.put(ERROR_MSG, Encode.forUriComponent(errorResponse.getMessage()));

            // Re-build query string
            int count = 0;
            for (Map.Entry<String, String> entry : queryParamMap.entrySet()) {
                queryStringBuilder.append(entry.getKey()).append("=").append(entry.getValue());
                count++;
                if (count < queryParamMap.size()) {
                    queryStringBuilder.append("&");
                }
            }
            String newQueryString = queryStringBuilder.toString();

            String redirectURL = "login.do";
            if (StringUtils.isNotBlank(newQueryString)) {
                redirectURL = redirectURL + "?" + newQueryString;
            }
            response.sendRedirect(redirectURL);
            return;
        }
    } else {
        String redirectURL = "error.do?" + STATUS + "=" + CONFIGURATION_ERROR + "&" + STATUS_MSG + "=" + AUTHENTICATION_MECHANISM_NOT_CONFIGURED;
        response.sendRedirect(redirectURL);
        return;
    }
%>

<html>
<body>
<p>You are now redirected to <%=commonauthURL%>
   If the redirection fails, please click the post button.</p>

<form method='post' action='<%=commonauthURL%>'>
    <p>
        <input id="token" name="token" type="hidden" value="<%=Encode.forHtmlAttribute(token)%>">
        <input id="sessionDataKey" name="sessionDataKey" type="hidden"
               value="<%=Encode.forHtmlAttribute(sessionDataKey)%>">
        <button type='submit'>POST</button>
    </p>
</form>
<script type='text/javascript'>
    document.forms[0].submit();
</script>
</body>
</html>
