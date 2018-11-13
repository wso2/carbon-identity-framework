<%--
  ~ Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<%@ page import="com.google.gson.Gson" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.identity.core.util.IdentityUtil" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="org.wso2.carbon.identity.core.util.IdentityTenantUtil" %>
<%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.util.*" %>
<%@ page import="org.wso2.carbon.identity.template.mgt.model.Template" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@include file="localize.jsp" %>
<%@taglib prefix="e" uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" %>
<jsp:directive.include file="init-url.jsp"/>
<jsp:directive.include file="template-mapper.jsp"/>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%
    String templateId = request.getParameter("templateId");
    String promptId = request.getParameter("promptId");
    String tenantDomain = request.getParameter("tenantDomain");

    String authAPIURL = application.getInitParameter(Constants.AUTHENTICATION_REST_ENDPOINT_URL);
    if (StringUtils.isBlank(authAPIURL)) {
        authAPIURL = IdentityUtil.getServerURL("/api/identity/auth/v1.1/", true, true);
    }
    if (!authAPIURL.endsWith("/")) {
        authAPIURL += "/";
    }
    authAPIURL += "context/" + request.getParameter("promptId");
    String contextProperties = AuthContextAPIClient.getContextProperties(authAPIURL);

    String templateAPIURL = IdentityUtil.getServerURL("/t/"+tenantDomain+"/api/identity/template/mgt/v1.0.0/templates/",true,true);

    if (!templateAPIURL.endsWith("/")) {
        templateAPIURL += "/";
    }
    templateAPIURL += templateId;

    String templateJSON = TemplateMgtAPIClient.getTemplateData(templateAPIURL);
    Gson gson = new Gson();
    Template templateData = gson.fromJson(templateJSON,Template.class);
%>
<html>
<head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%=AuthenticationEndpointUtil.i18n(resourceBundle, "wso2.identity.server")%>
    </title>
    <script src="js/handlebars-v4.0.11.js"></script>
    <link rel="icon" href="images/favicon.png" type="image/x-icon"/>
    <link href="libs/bootstrap_3.3.5/css/bootstrap.min.css" rel="stylesheet">
    <link href="css/Roboto.css" rel="stylesheet">
    <link href="css/custom-common.css" rel="stylesheet">
    
    <!--[if lt IE 9]>
    <script src="js/html5shiv.min.js"></script>
    <script src="js/respond.min.js"></script>
    <![endif]-->


</head>
<body>
<script type="text/javascript">
    var data = JSON.parse("<%=Encode.forJavaScript(contextProperties)%>");
    var prompt_id = "<%=promptId%>";
</script>

<header class="header header-default">
    <div class="container-fluid"><br></div>
    <div class="container-fluid">
        <div class="pull-left brand float-remove-xs text-center-xs">
            <a href="#">
                <img src="images/logo-inverse.svg" alt="wso2" title="wso2" class="logo">
                
                <h1><em><%=AuthenticationEndpointUtil.i18n(resourceBundle, "identity.server")%>
                </em></h1>
            </a>
        </div>
    </div>
</header>
<!-- page content -->
<div class="container-fluid body-wrapper">
    
    <div class="row">
        <div class="col-md-12">

            <%
                if (templateData.getTemplateScript() != null) {
            %>
            <div id="template-holder"></div>
            <script id="template-handlebars" type="text/x-handlebars-template">
            <div class="container col-xs-10 col-sm-6 col-md-6 col-lg-4 col-centered wr-content wr-login col-centered">
                <%out.write(templateData.getTemplateScript());%>
            </div>
            </script>

            <script type="text/javascript">
                var templateInfo = document.getElementById("template-handlebars").innerHTML;
                var template = Handlebars.compile(templateInfo);

                var template_data = template(data);
                document.getElementById("template-holder").innerHTML += template_data;
                document.getElementById("promptId").value= prompt_id;
                document.getElementById("template-form").action="../commonauth";

            </script>





            <%
            } else {
            %>
            <div class="container col-xs-7 col-sm-5 col-md-4 col-lg-3 col-centered wr-content wr-login col-centered">
                <div>
                    <h2 class="wr-title uppercase blue-bg padding-double white boarder-bottom-blue margin-none"><%=Encode.forHtmlContent("Incorrect Request")%> </h2>
                </div>
        
                <div class="boarder-all col-lg-12 padding-top-double padding-bottom-double error-alert  ">
                    <div class="font-medium">
                        <strong>
                            <%=AuthenticationEndpointUtil.i18n(resourceBundle, "attention")%> :
                        </strong>
                    </div>
                    <div class="padding-bottom-double">
                        <%=AuthenticationEndpointUtil.i18n(resourceBundle, "no.template.found")%>
                    </div>
                </div>
            </div>
            <%
                }
            %>
            
        </div>
    </div>
</div>

<!-- footer -->
<footer class="footer">
    <div class="container-fluid">
        <p><%=AuthenticationEndpointUtil.i18n(resourceBundle, "wso2.identity.server")%> | &copy;
            <script>document.write(new Date().getFullYear());</script>
            <a href="http://wso2.com/" target="_blank"><i class="icon fw fw-wso2"></i>
                <%=AuthenticationEndpointUtil.i18n(resourceBundle, "inc")%>
            </a>. <%=AuthenticationEndpointUtil.i18n(resourceBundle, "all.rights.reserved")%>
        </p>
    </div>
</footer>

<script src="libs/jquery_1.11.3/jquery-1.11.3.js"></script>
<script src="libs/bootstrap_3.3.5/js/bootstrap.min.js"></script>

</body>
</html>
