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

<%@page import="org.wso2.carbon.identity.application.authentication.endpoint.util.Constants" %>
<%@ page import="java.net.URLDecoder" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@include file="localize.jsp" %>

<html>
<head></head>
<body>
<%
    String assertionConsumerURL = (String) request.getAttribute(Constants.SAML2SSO.ASSERTION_CONSUMER_URL);
    String samlResp = (String) request.getAttribute(Constants.SAML2SSO.SAML_RESP);
    String relayState = (String) request.getAttribute(Constants.SAML2SSO.RELAY_STATE);

    if (relayState != null) {
        relayState = URLDecoder.decode(relayState, "UTF-8");
        relayState = relayState.replaceAll("&", "&amp;").replaceAll("\"", "&quot;").replaceAll("'", "&apos;").
                replaceAll("<", "&lt;").replaceAll(">", "&gt;").replace("\n", "");
    }
%>
<p><%=AuthenticationEndpointUtil.i18n(resourceBundle, "you.are.redirected.back.to")%>
    <%=Encode.forHtmlContent(assertionConsumerURL)%>.
    <%=AuthenticationEndpointUtil.i18n(resourceBundle, "if.the.redirection.fails.please.click")%>.</p>

<form method="post" action="<%=assertionConsumerURL%>">
    <p><input type="hidden" name="SAMLResponse" value="<%=Encode.forHtmlAttribute(samlResp)%>"/>
        <input type="hidden" name="RelayState" value="<%=Encode.forHtmlAttribute(relayState)%>"/>
        <button type="submit"><%=AuthenticationEndpointUtil.i18n(resourceBundle, "post")%></button>
    </p>
</form>

<script type="text/javascript">
    document.forms[0].submit();
</script>

</body>
</html>
