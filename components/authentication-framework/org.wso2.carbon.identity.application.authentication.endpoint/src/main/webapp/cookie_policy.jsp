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
                <img src="images/logo-inverse.svg" alt="wso2" title="wso2" class="logo">
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
                <div id="cookiePolicy" class="padding-double">
                    <h4><a href="http://wso2.org/library/identity-server"><strong>About WSO2 Identity Server</strong></a></h4>
                    <p>WSO2 Identity Server (refereed hereafter as &ldquo;IS 5.5.0&rdquo;) is an open source Identity Management and Entitlement Server which is based on open standards and specifications.</p>
                    <div class="margin-bottom-double"></div>
                    <h2 id="cookie-policy"><strong>Cookie Policy</strong></h2>
                    <p>IS 5.5 uses cookies so that it can provide you with the best user experience, and identify you for security purposes. Disabling cookies will most probably cause you unable to access some of the services.</p>
                    <div class="margin-bottom-double"></div>
                    <h2 id="how-os-5.5-process-cookies">How IS 5.5 process cookies</h2>
                    <p>IS 5.5 stores and retrieves information on your browser using cookies. This information is used to provide better experience. Some cookies serves the primary purpose of allowing login to the system, maintain session and keep track of activities you do within the login session.</p>
                    <p>Primary purpose of some cookies used in IS5.5 is to personally identify you, as this is the primary function of the Identity Server. However the cookie lifetime will be ended once your session ended with you log-out or session expiry.</p>
                    <p>Some cookies are simply used to give you a more personalised web experience, and these can not be used to not personally identify you or your activities.</p>
                    <p>This Cookie Policy is part of IS 5.5 Privacy Policy.</p>
                    <div class="margin-bottom-double"></div>
                    <h2 id="what-is-a-cookie">What is a cookie ?</h2>
                    <p>A browser cookie is a small piece of data that is stored on your device to help websites and mobile apps remember things about you. Other technologies, including Web storage and identifiers associated with your device, may be used for similar purposes. In this policy, we use the term &ldquo;cookies&rdquo; to discuss all of these technologies.</p>
                    <div class="margin-bottom-double"></div>
                    <h2 id="what-is-5.5-use-cookies-for">What IS 5.5 use cookies for?</h2>
                    <p>Cookies are used for two purpose in IS 5.5.</p>
                    <ol>
                        <li>To identify you to provide the security as this is the primary function of IS</li>
                        <li>To provide satisfying user experience.</li>
                    </ol>
                    <div class="margin-bottom"></div>
                    <h3 id="is-5.5-uses-cookies-for-the-following-purposes">IS 5.5 uses cookies for the following purposes.</h3>
                    <h4>Preferences</h4>
                    <p>IS 5.5 uses these cookies to remember your settings and preferences and to pre-fill the form fields to make your interactions with the site easier for you.</p>
                    <ul>
                        <li>These can not be used to personally identify you.</li>
                    </ul>
                    <h4>Security</h4>
                    <p>IS 5.5 use selected cookies to identify and prevent security risks.</p>
                    <p>For example, IS 5.5 may use these cookies to store your session information to prevent others from changing your password without your username and password.</p>
                    <p>IS 5.5 uses session cookie to maintain your active session.</p>
                    <p>IS 5.5 may use temporary cookie when performing multi-factor authentication and federated authentication.</p>
                    <p>IS 5.5 may use permanent cookies to detect that you have previously used the same device to log-in. This is to to calculate the &ldquo;risk level&rdquo; associated with your current login attempt. This is primarily to protect you and your account from possible attack.</p>
                    <h4>Performance</h4>
                    <p>IS 5.5 may use cookies to allow &ldquo;Remember Me&rdquo; functionalities.</p>
                    <div class="margin-bottom"></div>
                    <h3 id="analytics">Analytics</h3>
                    <p>IS 5.5 as a product does not use cookies for analytical purposes</p>
                    <div class="margin-bottom"></div>
                    <h3 id="third-party-cookies">Third party cookies</h3>
                    <p>Using IS 5.5 may cause some third-party cookie being set to your browser. IS 5.5 has no control of the operation of any of them. The third-party cookies which maybe set include,</p>
                    <ul>
                        <ul>
                            <li>Any of the social login sites, when IS 5.5 configured to use &ldquo;Social&rdquo; or &ldquo;Federated&rdquo; login, and you opt to do login with your &ldquo;Social Account&rdquo;</li>
                            <li>Any third party Federated login.</li>
                        </ul>
                    </ul>
                    <p>We strongly advise you to refer the respective cookie policy carefully for such sites, as IS has no knowledge or use on these cookies.</p>
                    <div class="margin-bottom-double"></div>
                    <h2 id="what-type-of-cookies-is-5.5-use">What type of cookies IS 5.5 use?</h2>
                    <p>IS 5.5 use persistent cookies and session cookies. A persistent cookie helps IS 5.5 recognize you as an existing user, so it's easier to return to WSO2 or interact with IS 5.5 without signing in again. After you sign in, a persistent cookie stays in your browser and will be read by IS 5.5 when you return to IS 5.5.</p>
                    <p>A session cookie is a cookie that is erased when the user closes the Web browser. The session cookie is stored in temporary memory and is not retained after the browser is closed. Session cookies do not collect information from the users computer.</p>
                    <div class="margin-bottom-double"></div>
                    <h2 id="how-do-i-control-my-cookies">How do I control my cookies?</h2>
                    <p>Most browsers allow you to control cookies through their settings preferences. However, if you limit the ability of websites to set cookies, you may worsen your overall user experience, since it will no longer be personalized to you. It may also stop you from saving customized settings like login information.</p>
                    <p>Most likely disabling cookies will make you unable to use Authentication and Authorization functionalities offered by IS 5.5.</p>
                    <p>If you have any questions or concerns regarding the use of cookies, please contact the organization running this IS 5.5 instance - Data Protection Officer.</p>
                    <div class="margin-bottom-double"></div>
                    <h2 id="what-are-the-cookies-used">What are the cookies used.</h2>
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
                                <p>To keep your session data, to give you a good user experience</p>
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
                                <p>To keep some messages shown to you, to give you a good user experience.</p>
                                <p>The &ldquo;nnnnnnnnnn&rdquo; here represent a random number. E.g MSG324935932</p>
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
                                <p>The URI you are accessing</p>
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
                                <p>To keep your active page in session, to give you a good user experience</p>
                            </td>
                            <td>
                                <p>Session</p>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                    <div class="margin-bottom-double"></div>
                    <h2 id="disclaimer">Disclaimer.</h2>
                    <p>This cookie policy is only for illustrative purpose of the product IS 5.5. The content in the policy is technically correct at the time of the product is shipment. The organization which runs this IS 5.5 instance has full authority and responsibility of the effective Cookie Policy.</p>
                </div>
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
            <a href="http://wso2.com/" target="_blank"><i class="icon fw fw-wso2"></i>
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
