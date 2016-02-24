<!--
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
<%@ page import="org.wso2.carbon.identity.mgt.stub.beans.VerificationBean" %>
<%@ page import="org.wso2.carbon.identity.mgt.ui.IdentityManagementClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<carbon:jsi18n
        resourceBundle="org.wso2.carbon.identity.mgt.ui.i18n.Resources"
        request="<%=request%>"/>
<fmt:bundle basename="org.wso2.carbon.identity.mgt.ui.i18n.Resources">

<script type="text/javascript">

    function updateCredentials() {
        document.updateCredentialsForm.action = "update_credential_final.jsp";
        document.updateCredentialsForm.submit();
    }

    function cancel(){
        location.href = "../admin/login.jsp";
    }

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

</script>
<%
    String userName = null;
    String secretKey = null;
    
    VerificationBean bean = (VerificationBean) session.getAttribute("confirmationBean");
    if(bean != null){
        userName = bean.getUserId();
        secretKey = bean.getKey();
    }
    session.removeAttribute("confirmationBean");

    if (userName != null && secretKey != null) {
        CaptchaInfoBean captchaInfoBean;
        try {
            String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(),
                    session);
            ConfigurationContext configContext = (ConfigurationContext) config
                    .getServletContext()
                    .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            IdentityManagementClient client =
                    new IdentityManagementClient(backendServerURL, configContext) ;
            captchaInfoBean = client.generateRandomCaptcha();
        } catch (Exception e) {
    %>
    <div>
        <p><fmt:message key="error.captcha.generate"/></p>    
    </div>
    <%
            return;
        }

        String captchaImagePath = captchaInfoBean.getImagePath();
        String captchaImageUrl = "../../" + captchaImagePath;
        String captchaSecretKey = captchaInfoBean.getSecretKey();

        if ("failed".equals(session.getAttribute("captcha-status"))) {
            session.setAttribute("captcha-status", null);
    %>
    <script type="text/javascript">
        jQuery(document).ready(function() {
            CARBON.showWarningDialog('Please enter the letters shown as in the image to change your password.');
        });
    </script>
    <%
        }

    %>

    <%
        if ("true".equals(session.getAttribute("update-credentials-failed"))) {
            session.removeAttribute("update-credentials-failed");
    %>

    <script type="text/javascript">
        jQuery(document).ready(function() {
            CARBON.showWarningDialog('Credentials Update Failed. Pls retry updating the credentials.');
        });
    </script>
    <%
        }
    %>

    <div id="middle">
        <h2>
            <fmt:message key="password.reset"/>
        </h2>
    </div>
    <div id="workarea">
        <form id="updateCredentialsForm" name="updateCredentialsForm" action="update_credential_final.jsp"
              method="post">

            <table class="styledLeft">
                <tbody>
                <tr>
                    <td class="nopadding">
                        <table class="normal-nopadding" cellspacing="0">
                            <tbody>
                            <tr>
                                <td><fmt:message key="user.id"/></td>
                                <td colspan="2"><input readonly="true" type="text" name="userName"
                                                       id="userName"
                                                       style="width:400px" value="<%=Encode.forHtmlAttribute(userName)%>"/>
                            </tr>
                            <tr>
                                <td><input type="hidden"  name="secretKey" id="secretKey"
                                           value="<%=Encode.forHtmlAttribute(secretKey)%>" /></td>
                            </tr>
                            <tr>
                                <td><fmt:message key="new.password"/>
                                    <span class="required">*</span></td>
                                <td colspan="2"><input type="password" name="userPassword"
                                                       id="userPassword"
                                                       style="width:400px"/></td>
                            </tr>
                            <tr>
                                <td colspan="2">(Minimum of 6 Characters in length)</td>
                            </tr>
                            <tr>
                                <td><fmt:message key="new.password.repeat"/>
                                    <span class="required">*</span></td>
                                <td colspan="2"><input type="password" name="userPasswordRepeat"
                                                       id="userPasswordRepeat"
                                                       style="width:400px"/></td>
                            </tr>
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
                            </tr>
                            <tr>
                                <td><input type="hidden" name="captcha-secret-key"
                                            value="<%=Encode.forHtmlAttribute(captchaSecretKey)%>"/></td>
                                <td colspan="2" height="100"><input type="text"
                                                                    id="captcha-user-answer"
                                                                    name="captcha-user-answer"
                                                                    style="width:400px"
                                                                    value=""/></td>
                            </tr>
                            </tbody>
                        </table>
                    </td>
                </tr>

                <tr id="buttonRow">
                    <td>
                        <input class="button" type="button"
                               value="Cancel" onclick="cancel()"/>                        
                        <input class="button" type="button"
                               value="Update" onclick="updateCredentials()"/>
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
        </form>
    </div>
    <script type="text/javascript">
        showCaptcha('<%=Encode.forJavaScriptBlock(captchaImageUrl)%>');
    </script>

    <% } else { %>
    <div id="middle">
        <h2>
            <fmt:message key="password.reset.failed"/>
        </h2>

        <p><fmt:message key="request.verification.failed"/></p>

        <p>
            <fmt:message key="try.again"/><a href="forgot_root.jsp"><fmt:message key="try.again.here"/></a> .
        </p>
    </div>
    <%
        }
    %>

</fmt:bundle>
