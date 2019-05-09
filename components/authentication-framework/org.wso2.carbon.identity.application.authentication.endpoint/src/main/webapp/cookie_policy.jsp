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

<%@include file="localize.jsp" %>

<html>
<head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%=AuthenticationEndpointUtil.i18n(resourceBundle, "wso2.identity.server")%></title>

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

<!-- header -->
<header class="header header-default">
    <div class="container-fluid"><br></div>
    <div class="container-fluid">
        <div class="pull-left brand float-remove-xs text-center-xs">
            <a href="#">
                <img src="images/logo-inverse.svg" alt="<%=AuthenticationEndpointUtil.i18n(resourceBundle, "business.name")%>" title="<%=AuthenticationEndpointUtil.i18n(resourceBundle, "business.name")%>" class="logo">
                <h1><em><%=AuthenticationEndpointUtil.i18n(resourceBundle, "identity.server")%>
                </em></h1>
            </a>
        </div>
    </div>
</header>

<!-- page content -->
<div class="row">
    <div class="col-xs-12 col-sm-3 col-md-3 col-lg-3 col-sm-offset-1 col-md-offset-1 col-lg-offset-1">
        <div id="toc"></div>
    </div>
    <div class="col-xs-12 col-sm-7 col-md-7 col-lg-7">
        <!-- content -->
        <div class="container col-xs-12 col-sm-12 col-md-12 col-lg-12 col-centered wr-content wr-login col-centered padding-bottom-100">
            <div>
                <h2 class="wr-title uppercase blue-bg padding-double white boarder-bottom-blue margin-none">
                    <%=AuthenticationEndpointUtil.i18n(resourceBundle, "wso2.identity.server")%> - <%=AuthenticationEndpointUtil.i18n(resourceBundle, "privacy.policy.cookies")%>
                </h2>
            </div>
            <div class="boarder-all ">
                <div class="clearfix"></div>
                <%-- Customizable content. Due to this nature, i18n is not implemented for this section --%>
                <div id="cookiePolicy" class="padding-double">
                    <h4><a href="http://wso2.org/library/identity-server"><strong>About WSO2 Identity Server</strong></a></h4>
                    <p>WSO2 Identity Server (referred to as &ldquo;WSO2 IS&rdquo; within this policy) is an open source Identity Management and Entitlement Server that is based on open standards and specifications.</p>
                    <div class="margin-bottom-double"></div>
                    <h2 id="cookie-policy"><strong>Cookie Policy</strong></h2>
                    <p>WSO2 IS uses cookies so that it can provide the best user experience for you and identify you for security purposes. If you disable cookies, some of the services will (most probably) be inaccessible to you. </p>
                    <div class="margin-bottom-double"></div>
                    <h2 id="how-wso2-is-5.8.0-processes-cookies">How does WSO2 IS process cookies?</h2>
                    <p>WSO2 IS stores and retrieves information on your browser using cookies. This information is used to provide a better experience. Some cookies serve the primary purposes of allowing a user to log in to the system, maintaining sessions, and keeping track of activities you do within the login session.</p>
                    <p>The primary purpose of some cookies used in WSO2 IS is to personally identify you as this is the main function of the WSO2 Identity Server. However the cookie lifetime ends once your session ends i.e., after you log-out, or after the session expiry time has elapsed.</p>
                    <p>Some cookies are simply used to give you a more personalised web experience and these cookies can not be used to personally identify you or your activities.</p>
                    <p>This cookie policy is part of the <a href="privacy_policy.do">WSO2 IS Privacy Policy.</a></p>
                    <div class="margin-bottom-double"></div>
                    <h2 id="what-is-a-cookie">What is a cookie?</h2>
                    <p>A browser cookie is a small piece of data that is stored on your device to help websites and mobile apps remember things about you. Other technologies, including web storage and identifiers associated with your device, may be used for similar purposes. In this policy, we use the term &ldquo;cookies&rdquo; to discuss all of these technologies.</p>
                    <div class="margin-bottom-double"></div>
                    <h2 id="what-does-wso2-is-5.8.0-use-cookies-for">What does WSO2 IS use cookies for?</h2>
                    <p>Cookies are used for two purposes in WSO2 IS.</p>
                    <ol>
                        <li>To identify you and provide security (as this is the main function of WSO2 IS).</li>
                        <li>To provide a satisfying user experience.</li>
                    </ol>
                    <div class="margin-bottom"></div>
                    <p>WSO2 IS uses cookies for the following purposes listed below.</p>
                    <h3>Preferences</h3>
                    <p>WSO2 IS uses these cookies to remember your settings and preferences, and to auto-fill the form fields to make your interactions with the site easier. </p>
                    <p>These cookies can not be used to personally identify you.</p>
                    <h3>Security</h3>
              <ul>
                    <li>WSO2 IS uses selected cookies to identify and prevent security risks.
                     For example, WSO2 IS may use these cookies to store your session information in order to prevent others from changing your password without your username and password.</li>
                </br><li>WSO2 IS uses session cookies to maintain your active session.</li>
                  </br>   <li>WSO2 IS may use temporary cookies when performing multi-factor authentication and federated authentication.</li>
              </br>   <li>WSO2 IS may use permanent cookies to detect that you have previously used the same device to log in. This is to to calculate the &ldquo;risk level&rdquo; associated with your current login attempt. This is primarily to protect you and your account from possible attack.</li>
              </ul>   
                    <h3>Performance</h3>
                    <p>WSO2 IS may use cookies to allow &ldquo;Remember Me&rdquo; functionalities.</p>
                    <div class="margin-bottom"></div>
                    <h3 id="analytics">Analytics</h3>
                    <p>WSO2 IS as a product does not use cookies for analytical purposes.</p>
                    <div class="margin-bottom"></div>
                    <h3 id="third-party-cookies">Third party cookies</h3>
                    <p>Using WSO2 IS may cause some third-party cookies to be set in your browser. WSO2 IS has no control over how any of them operate. The third-party cookies that may be set include:</p>
                    <ul>
                        <ul>
                            <li>Any social login sites. For example, third-party cookies may be set when WSO2 IS is configured to use &ldquo;social&rdquo; or &ldquo;federated&rdquo; login, and you opt to login with your &ldquo;Social Account&rdquo;.</li>
                            <li>Any third party federated login.</li>
                        </ul>
                    </ul>
                    <p>WSO2 strongly advises you to refer the respective cookie policy of such sites carefully as WSO2 has no knowledge or use on these cookies.</p>
                    <div class="margin-bottom-double"></div>
                    <h2 id="what-type-of-cookies-does-5.8.0-use">What type of cookies does WSO2 IS use?</h2>
                    <p>WSO2 IS uses persistent cookies and session cookies. A persistent cookie helps WSO2 IS to recognize you as an existing user so that it is easier to return to WSO2 or interact with WSO2 IS without signing in again. After you sign in, a persistent cookie stays in your browser and will be read by WSO2 IS when you return to WSO2 IS.</p>
                    <p>A session cookie is a cookie that is erased when the user closes the web browser. The session cookie is stored in temporary memory and is not retained after the browser is closed. Session cookies do not collect information from the user's computer.</p>
                    <div class="margin-bottom-double"></div>
                    <h2 id="how-do-i-control-my-cookies">How do I control my cookies?</h2>
                    <p>Most browsers allow you to control cookies through their settings preferences. However, if you limit the given ability for websites to set cookies, you may worsen your overall user experience since it will no longer be personalized to you. It may also stop you from saving customized settings like login information.</p>
                    <p>Most likely, disabling cookies will make you unable to use authentication and authorization functionalities offered by WSO2 IS.</p>
                    <p>If you have any questions or concerns regarding the use of cookies, please contact the entity or individuals (or their data protection officer, if applicable) of the organization running this WSO2 IS instance.</p>
                    <div class="margin-bottom-double"></div>
                    <h2 id="what-are-the-cookies-used">What are the cookies used?</h2>
                    <table class="table table-bordered">
                        <tbody>
                        <tr>
                            <td>
                                <p><strong>Cookie Name</strong></p>
                            </td>
                            <td>
                                <p><strong>Purpose</strong></p>
                            </td>
                            <td>
                                <p><strong>Retention</strong></p>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <p>JSESSIONID</p>
                            </td>
                            <td>
                                <p>To keep your session data in order to give you a good user experience.</p>
                            </td>
                            <td>
                                <p>Session</p>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <p>MSGnnnnnnnnnn</p>
                            </td>
                            <td>
                                <p>To keep some messages that are shown to you in order to give you a good user experience.</p>
                                <p>The &ldquo;nnnnnnnnnn&rdquo; reference in this coookie represents a random number e.g., MSG324935932.</p>
                            </td>
                            <td>
                                <p>Session</p>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <p>requestedURI</p>
                            </td>
                            <td>
                                <p>The URI you are accessing.</p>
                            </td>
                            <td>
                                <p>Session</p>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <p>current-breadcrumb</p>
                            </td>
                            <td>
                                <p>To keep your active page in session in order to give you a good user experience.</p>
                            </td>
                            <td>
                                <p>Session</p>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                    <div class="margin-bottom-double"></div>
                    <h2 id="disclaimer">Disclaimer</h2>
                    <p>This cookie policy is only for the illustrative purposes of the product WSO2 IS. The content in the policy is technically correct at the time of the product shipment. The organization which runs this WSO2 IS instance has full authority and responsibility with regard to the effective Cookie Policy. </p>
                    <p>WSO2, its employees, partners, and affiliates do not have access to and do not require, store, process or control any of the data, including personal data contained in WSO2 IS. All data, including personal data is controlled and processed by the entity or individual running WSO2 IS.  WSO2, its employees partners and affiliates are not a data processor or a data controller within the meaning of any data privacy regulations.  WSO2 does not provide any warranties or undertake any responsibility or liability in connection with the lawfulness or the manner and purposes for which WSO2 IS is used by such entities or persons. </p>
                </div>
                <%-- /Costomizable content --%>
                <div class="clearfix"></div>
            </div>
        </div>
        <!-- /content -->

    </div>
</div>

<!-- footer -->
<footer class="footer">
    <div class="container-fluid">
        <p><%=AuthenticationEndpointUtil.i18n(resourceBundle, "wso2.identity.server")%> | &copy;
            <script>document.write(new Date().getFullYear());</script>
            <a href="<%=AuthenticationEndpointUtil.i18n(resourceBundle, "business.homepage")%>" target="_blank"><i class="icon fw fw-wso2"></i>
                <%=AuthenticationEndpointUtil.i18n(resourceBundle, "inc")%>
            </a>. <%=AuthenticationEndpointUtil.i18n(resourceBundle, "all.rights.reserved")%>
        </p>
    </div>
</footer>

<script src="libs/jquery_1.11.3/jquery-1.11.3.js"></script>
<script src="libs/bootstrap_3.3.5/js/bootstrap.min.js"></script>

<script type="text/javascript" src="js/u2f-api.js"></script>
<script type="text/javascript">
    var ToC = "<nav role='navigation' class='table-of-contents'>" + "<h4>On this page:</h4>" + "<ul>";
    var newLine, el, title, link;

    $("#cookiePolicy h2,#cookiePolicy h3").each(function() {
        el = $(this);
        title = el.text();
        link = "#" + el.attr("id");
        if(el.is("h3")){
            newLine = "<li class='sub'>" + "<a href='" + link + "'>" + title + "</a>" + "</li>";
        }else{
            newLine = "<li >" + "<a href='" + link + "'>" + title + "</a>" + "</li>";
        }

        ToC += newLine;
    });

    ToC += "</ul>" + "</nav>";

    $("#toc").append(ToC);
</script>

</body>
</html>
