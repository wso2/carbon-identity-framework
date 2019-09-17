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

<html>
<head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <!--title-->
    <jsp:directive.include file="title.jsp"/>

    <link rel="icon" href="images/favicon.png" type="image/x-icon"/>
    <link href="libs/bootstrap_3.4.1/css/bootstrap.min.css" rel="stylesheet">
    <link href="css/Roboto.css" rel="stylesheet">
    <link href="css/custom-common.css" rel="stylesheet">
</head>

<body>

<!--header-->
<jsp:directive.include file="header.jsp"/>

<script src="libs/jquery_3.4.1/jquery-3.4.1.js"></script>
<script src="libs/bootstrap_3.4.1/js/bootstrap.min.js"></script>
<script>
    function getParameterByName(name, url) {
        if (!url) {
            url = window.location.href;
        }
        name = name.replace(/[\[\]]/g, '\\$&');
        var regex = new RegExp('[?&]' + name + '(=([^&#]*)|&|#|$)'),
            results = regex.exec(url);
        if (!results) return null;
        if (!results[2]) return "";
        return decodeURIComponent(results[2].replace(/\+/g, ' '));
    }

    function submitIdentifier() {
        var username = document.getElementById("username");
        username.value = username.value.trim();
        if(username.value){
            $.ajax({
                type: "GET",
                url: "/api/users/v1/me/webauthn/start-authentication?username=admin&tenantDomain=carbon.super&storeDomain=PRIMARY&appId=https://localhost:9443&sessionDataKey="+getParameterByName("sessionDataKey"),
                success: function (data) {
                    if (data) {
                        console.log(data);
                    }
                },
                cache: false
            });
        }
    }
</script>

<!-- page content -->
<body>
<div class="col-md-4 col-md-offset-4 panel panel-default" style="margin-top: 10%">
    <form onsubmit="event.preventDefault()" id="identifierForm">
        <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">

            <label for="username"><%=AuthenticationEndpointUtil.i18n(resourceBundle, "username")%></label>
            <input id="username" name="username" type="text" class="form-control" tabindex="0" placeholder="" required>
            <input id="authType" name="authType" type="hidden" value="idf">
        </div>
        <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
            <input type="hidden" name="sessionDataKey" value='<%=Encode.forHtmlAttribute
                (request.getParameter("sessionDataKey"))%>'/>
        </div>
        <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">
            <div class="form-actions">
                <button
                        class="wr-btn grey-bg col-xs-12 col-md-12 col-lg-12 uppercase font-extra-large margin-bottom-double"
                        onclick="submitIdentifier()">
                    <%=AuthenticationEndpointUtil.i18n(resourceBundle, "next")%>
                </button>
            </div>
        </div>
        <div class="clearfix"></div>
    </form>
</div>
<body>

<!--footer-->
<jsp:directive.include file="footer.jsp"/>

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

        return extend(
            request, {
                allowCredentials,
                challenge: base64url.toByteArray(request.challenge),
            });
    }

    function talkToDevice(authenticatorResponse){

        var authRequest = authenticatorResponse;
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
