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

<%@page import="org.owasp.encoder.Encode" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@include file="localize.jsp" %>
<%@include file="init-url.jsp" %>
<%@ page import="java.io.File"%>

<%
    String authRequest = request.getParameter("data");
%>

<html>
<head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <!-- title -->
    <%
        File titleFile = new File(getServletContext().getRealPath("extensions/title.jsp"));
        if (titleFile.exists()) {
    %>
            <jsp:include page="extensions/title.jsp"/>
    <%} else {%>
            <jsp:directive.include file="includes/title.jsp"/>
    <%}%>

    <link rel="icon" href="images/favicon.png" type="image/x-icon"/>
    <link href="libs/bootstrap_3.4.1/css/bootstrap.min.css" rel="stylesheet">
    <link href="css/Roboto.css" rel="stylesheet">
    <link href="css/custom-common.css" rel="stylesheet">
    <!--[if lt IE 9]>
    <script src="js/html5shiv.min.js"></script>
    <script src="js/respond.min.js"></script>
    <![endif]-->
</head>

<body onload='talkToDevice();'>

<!-- header -->
<%
    File headerFile = new File(getServletContext().getRealPath("extensions/header.jsp"));
    if (headerFile.exists()) {
%>
        <jsp:include page="extensions/header.jsp"/>
<%} else {%>
        <jsp:directive.include file="includes/header.jsp"/>
<%}%>

<!-- page content -->
<div class="container-fluid body-wrapper">

    <div class="row">
        <div class="col-md-12">
            <!-- content -->
            <div class="container col-xs-7 col-sm-5 col-md-4 col-lg-3 col-centered wr-content wr-login col-centered">
                <div>
                    <h2 class="wr-title uppercase blue-bg padding-double white boarder-bottom-blue margin-none">
                        <%=AuthenticationEndpointUtil.i18n(resourceBundle, "verification")%>
                    </h2>
                </div>

                <div class="boarder-all col-lg-12 padding-top-double padding-bottom-double">
                    <div class="padding-bottom-double font-large">
                        <%=AuthenticationEndpointUtil.i18n(resourceBundle, "touch.your.u2f.device")%>
                    </div>
                    <div> <img class="img-responsive" src="images/U2F.png"> </div>
                </div>
            </div>
            <!-- /content -->

        </div>
    </div>
</div>

<!-- footer -->
<%
    File footerFile = new File(getServletContext().getRealPath("extensions/footer.jsp"));
    if (footerFile.exists()) {
%>
        <jsp:include page="extensions/footer.jsp"/>
<%} else {%>
        <jsp:directive.include file="includes/footer.jsp"/>
<%}%>

<script src="libs/jquery_3.4.1/jquery-3.4.1.js"></script>
<script src="libs/bootstrap_3.4.1/js/bootstrap.min.js"></script>

<script>


    $('#popover').popover({
        html: true,
        title: function () {
            return $("#popover-head").html();
        },
        content: function () {
            return $("#popover-content").html();
        }
    });

</script>

<script type="text/javascript" src="js/u2f-api.js"></script>
<script type="text/javascript" src="libs/base64js/base64js-1.3.0.min.js"></script>
<script type="text/javascript" src="libs/base64url.js"></script>


<script type="text/javascript">

    function responseToObject(response) {

        if (response.u2fResponse) {
            return response;
        } else {
            var clientExtensionResults = {};

            try {
                clientExtensionResults = response.getClientExtensionResults();
            } catch (e) {
                console.error('getClientExtensionResults failed', e);
            }

            if (response.response.attestationObject) {
                return {
                    id: response.id,
                    response: {
                        attestationObject: base64url.fromByteArray(response.response.attestationObject),
                        clientDataJSON: base64url.fromByteArray(response.response.clientDataJSON)
                    },
                    clientExtensionResults,
                    type: response.type
                };
            } else {
                return {
                    id: response.id,
                    response: {
                        authenticatorData: base64url.fromByteArray(response.response.authenticatorData),
                        clientDataJSON: base64url.fromByteArray(response.response.clientDataJSON),
                        signature: base64url.fromByteArray(response.response.signature),
                        userHandle: response.response.userHandle && base64url.fromByteArray(response.response.userHandle)
                    },
                    clientExtensionResults,
                    type: response.type
                };
            }
        }
    }


    function extend(obj, more) {

        return Object.assign({}, obj, more);
    }

    function decodePublicKeyCredentialRequestOptions(request) {

        const allowCredentials = request.allowCredentials && request.allowCredentials.map(credential => extend(
            credential, {
                id: base64url.toByteArray(credential.id),
            }));

        const publicKeyCredentialRequestOptions = extend(
            request, {
                allowCredentials,
                challenge: base64url.toByteArray(request.challenge),
            });

        return publicKeyCredentialRequestOptions;
    }

    function talkToDevice(){

        var authRequest = '<%=Encode.forJavaScriptBlock(authRequest)%>';
        var jsonAuthRequest = JSON.parse(authRequest);
        console.log(jsonAuthRequest);
        navigator.credentials.get({
          publicKey: decodePublicKeyCredentialRequestOptions(jsonAuthRequest.publicKeyCredentialRequestOptions),
      })
        .then(function(data) {
            payload = {};
            payload.requestId = jsonAuthRequest.requestId;
            payload.credential = responseToObject(data);
            var form = document.getElementById('form');
            var reg = document.getElementById('tokenResponse');
            reg.value = JSON.stringify(payload);
            form.submit();
        })
        .catch(function(err) {
            var form = document.getElementById('form');
            var reg = document.getElementById('tokenResponse');
            reg.value = JSON.stringify({errorCode : 400, message : err});
            form.submit();
        });
    }

</script>

<form method="POST" action="<%=commonauthURL%>" id="form" onsubmit="return false;">
    <input type="hidden" name="sessionDataKey" value='<%=Encode.forHtmlAttribute(request.getParameter("sessionDataKey"))%>'/>
    <input type="hidden" name="tokenResponse" id="tokenResponse" value="tmp val"/>
</form>

</body>
</html>
