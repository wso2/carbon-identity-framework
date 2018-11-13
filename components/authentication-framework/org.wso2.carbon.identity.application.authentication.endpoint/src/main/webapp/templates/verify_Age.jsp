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

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="e" uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" %>
<jsp:directive.include file="../init-url.jsp"/>


<div id="quoteData"></div>


<script id="quote-template" type="text/x-handlebars-template">

    <div class="uppercase">
        <h3>Hello {{name}} !!! </h3>
    </div>

    <div class="margin-none wr-login">
        <div class="font-large info text-left padding-top-double" >
            <strong>{{promptLabel}}</strong>
        </div>
    </div>

    <div class="boarder-all ">
        <div class="clearfix"></div>
        <div class="padding-double login-form">

            <form action="<%=commonauthURL%>" method="POST">
            <form action="../commonauth" method="POST">

                <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group required">
                    <label for="dob" class="control-label"/>birth date</label>
                    <input type="text" id="dob" name="dob" class="form-control" placeholder="yyyy-mm-dd" />
                </div>

                <input type="hidden" id="promptResp" name="promptResp" value="true">
                <input type="hidden" id="promptId" name="promptId" value="${requestScope.promptId}">
                <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group required">
                    <input type="submit" class="wr-btn grey-bg col-xs-12 col-md-12 col-lg-12 uppercase font-extra-large" value="Submit">
                </div>
            </form>

            <div class="clearfix"></div>
        </div>
    </div>

    <br/>

    {{makeLink "Home" "http://localhost.com:8080/saml2-web-app-dispatch.com/"}}<br/><br/>

</script>

<script type="text/javascript">

    Handlebars.registerHelper("makeLink",function (text, url) {
        text = Handlebars.Utils.escapeExpression(text);
        url = Handlebars.Utils.escapeExpression(url);

        var theLink = '<a href="' + url + '">'+ text+'</a>';
        return new Handlebars.SafeString(theLink);

    });

    var userName = '${requestScope.data["username"]}';
    var quoteInfo = document.getElementById("quote-template").innerHTML;
    var template = Handlebars.compile(quoteInfo);

    var quoteData = template({
        name :userName,
        promptLabel: "Verify the following information to continue"
    });

    document.getElementById("quoteData").innerHTML += quoteData;

</script>

