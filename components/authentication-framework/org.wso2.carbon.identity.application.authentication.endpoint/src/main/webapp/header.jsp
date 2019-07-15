<!-- localize.jsp MUST already be included in the calling script -->
<%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.util.AuthenticationEndpointUtil" %>

<header class="header header-default">
    <div class="container-fluid"><br></div>
    <div class="container-fluid">
        <div class="pull-left brand float-remove-xs text-center-xs">
            <a href="#">
                <img src="images/logo-inverse.svg"
                     alt="<%=AuthenticationEndpointUtil.i18n(resourceBundle, "business.name")%>"
                     title="<%=AuthenticationEndpointUtil.i18n(resourceBundle, "business.name")%>"
                     class="logo">
                <h1><em><%=AuthenticationEndpointUtil.i18n(resourceBundle, "identity.server")%>
                </em></h1>
            </a>
        </div>
    </div>
</header>