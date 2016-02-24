<!--
  ~
  ~ Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~ WSO2 Inc. licenses this file to you under the Apache License,
  ~  Version 2.0 (the "License"); you may not use this file except
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
  ~
  -->


<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.captcha.mgt.beans.xsd.CaptchaInfoBean" %>
<%@ page import="org.wso2.carbon.identity.mgt.ui.IdentityManagementClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<script type="text/javascript">

    var captchaImgUrl;
    
    function showCaptcha(captchaImgUrlArg) {
        captchaImgUrl = captchaImgUrlArg;
        var captchaImgDiv = document.getElementById("captchaImgDiv");
        captchaImgDiv.innerHTML = "<img src='../identity-mgt/images/ajax-loader.gif' alt='busy'/>";
        setTimeout("showCaptchaTimely()", 4000);
    }

    function showCaptchaTimely() {
        var captchaImgDiv = document.getElementById("captchaImgDiv");
        captchaImgDiv.innerHTML = "<img src='" + captchaImgUrl + "' alt='If you can not see the captcha " +
                        "image please refresh the page or click the link again.'/>";
    }

    function cancel(){
        location.href = "../admin/login.jsp";
    }

</script>

<fmt:bundle basename="org.wso2.carbon.identity.mgt.ui.i18n.Resources">
    <link href="css/forgot-password.css" rel="stylesheet" type="text/css" media="all"/>    
    <%
        CaptchaInfoBean captchaInfoBean;
        String backendServerURL;
        try {

            backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(),
                    session);
            ConfigurationContext configContext = (ConfigurationContext) config
                    .getServletContext()
                    .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            IdentityManagementClient client =
                    new IdentityManagementClient(backendServerURL, configContext);                        
            captchaInfoBean = client.generateRandomCaptcha();

        } catch (Exception e) {
            CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR,
                request);
    %>
            <script type="text/javascript">
                location.href = "index.jsp";
            </script>
    <%
            return;
        }
    %>
    <div id="middle">
        <h2>
            <fmt:message key="header.forget.password.1"/>
        </h2>
    </div>

    <%
        if(captchaInfoBean == null){
    %>
        <div>
            <p><fmt:message key="error.captcha.generate"/></p>.
        </div>
    <%
            return;
        }
        
        String captchaImagePath = captchaInfoBean.getImagePath();
        String captchaImageUrl = "../../" + captchaImagePath;
        String captchaSecretKey = captchaInfoBean.getSecretKey();
    %>

    <form id="userIdValidation" action="forgot_password_step2.jsp" method="post">
        <table>
            <tbody>
            <tr>
                <td><fmt:message key="enter.user.id"/><span class="required">*</span></td>
                <td colspan="2"><input type="text" tabindex="1" name="userName" id="userName"
                                       style="width:400px" /></td>
            </tr>
            <tr></tr>
            <tr>
                <td colspan="2"><fmt:message key="captcha.message"/></td>
            </tr>
            <tr>
                <td></td>
                <td colspan="2">
                    <div id="captchaImgDiv"></div>
                </td>
            </tr>

            <tr>
                <td><fmt:message key="word.verification"/><span
                        class="required">*</span></td>
                <td colspan="2" height="100"><input type="text"
                                                    id="captcha-user-answer"
                                                    name="captcha-user-answer"
                                                    style="width:400px"
                                                    value=""/></td>
                 <td>
                    <input type="hidden" name="captcha-secret-key"
                               value="<%=Encode.forHtmlAttribute(captchaSecretKey)%>"/>
                 </td>
            </tr>

            <tr id="buttonRow">
                <td>
                    <input type="button" tabindex="2" value="Cancel"  onclick="cancel()"/>
                    <input type="submit" tabindex="2" value="Next"/>
                </td>
            </tr>
            <tr id="waitMessage" style="display:none">
                <td>
                    <div style="font-size:13px !important;margin-top:10px;margin-bottom:10px;">
                        <img
                                src="images/ajax-loader.gif" align="left" hspace="20"/>Please
                        wait until the Service is imported to the Registry.
                    </div>
                </td>
            </tr>
            </tbody>
        </table>

        <script type="text/javascript">
              showCaptcha('<%=Encode.forJavaScriptBlock(captchaImageUrl)%>');
        </script>
    </form>
</fmt:bundle>

