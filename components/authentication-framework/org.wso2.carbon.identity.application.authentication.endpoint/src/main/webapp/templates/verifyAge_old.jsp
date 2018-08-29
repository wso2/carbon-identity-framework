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

<div>
    <h2 class="wr-title uppercase blue-bg padding-double white boarder-bottom-blue margin-none">Content Warning</h2>
</div>

<div class="boarder-all col-lg-12 padding-top-double padding-bottom-double error-alert  ">
    <div class="font-medium">
        <strong>
            Attention :
        </strong>
    </div>
    <div class="padding-bottom-double">
        You need to be over 18 years to login to this application.
    </div>
</div>

<div >
    <div class="clearfix"></div>
</div>

<div class = "margin-none wr-login">
    <div class="font-large info text-left padding-top-double" >
        <strong>Verify your age</strong>
    </div>
</div>

<div class="boarder-all ">
    <div class="clearfix"></div>
    <div class="padding-double login-form">

        <form action="<%=commonauthURL%>" method="POST">

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
<!-- /content -->
