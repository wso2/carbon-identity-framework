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
                    <%=AuthenticationEndpointUtil.i18n(resourceBundle, "wso2.identity.server")%> - <%=AuthenticationEndpointUtil.i18n(resourceBundle, "privacy.policy.general")%>
                </h2>
            </div>
            <div class="boarder-all ">
                <div class="clearfix"></div>
                <div id="privacyPolicy" class="padding-double">
                    <h4><a href="http://wso2.org/library/identity-server"><strong>About WSO2 Identity Server</strong></a></h4>
                    <p>WSO2 Identity Server (refereed hereafter as &ldquo;IS 5.5&rdquo;) is an open source Identity Management and Entitlement Server which is based on open standards and specifications.</p>
                    <div class="margin-bottom-double"></div>
                    <h2 id="privacy-policy"><strong>Privacy Policy</strong></h2>
                    <p>This describes how IS 5.5 capture your personal information, purpose and the retention of your personal information.</p>
                    <p>Please note that this policy is for reference only, and applicable for the Software as a product. WSO2 Inc., or its developers has no access to the information held within the IS 5.5. Please refer &ldquo;Disclaimers&rdquo; for more information</p>
                    <div class="margin-bottom-double"></div>
                    <h2 id="what-are-the-personal-information">What are the personal information</h2>
                    <p>IS 5.5 considers anything related to you as your personal information. This includes, but not limited to,</p>
                    <ul>
                        <li>Your user name (except the case where the user name created by your employer under contract)</li>
                        <li>Date of Birth/Age</li>
                        <li>Your IP address you would use to login</li>
                        <li>Your device ID if you use to login with a device (Phone, Tablet).</li>
                    </ul>
                    <p>However IS 5.5 consider following are not your personal information, and used only for <strong>statistical</strong> purposes. Reason for this is that these information can not be used to track you.</p>
                    <ul>
                        <li>City/Country which you originate the TCP/IP connection.</li>
                        <li>Time of the day you login, Year, Month, Week, Hour or Minute</li>
                        <li>Type of the device you use to login (Phone, Tablet, etc)</li>
                        <li>Operating system and Generic browser information</li>
                    </ul>
                    <div class="margin-bottom-double"></div>
                    <h2 id="collection-of-your-information">Collection of your information</h2>
                    <p>IS 5.5 collect your information only to serve your access requirements.</p>
                    <p>For example</p>
                    <ul>
                        <li>IS 5.5 uses your IP address to detect any suspicious login attempt to your account.</li>
                        <li>IS 5.5 uses your First Name, Last Name, etc to provide a rich and personalized information</li>
                        <li>IS 5.5 use your security question and answers only to allow account recovery.</li>
                    </ul>
                    <div class="margin-bottom"></div>
                    <h3 id="tracking-technologies">Tracking Technologies</h3>
                    <p>IS 5.5 collect your information with</p>
                    <ul>
                        <li>User profile page where you enter your personal data</li>
                        <li>Tracking your IP address with HTTP request, HTTP headers, and TCP/IP.</li>
                        <li>Tracking your geographic information with the IP address.</li>
                        <li>Your login history with browser cookies. Please refer our cookie policy for more information.</li>
                    </ul>
                    <div class="margin-bottom-double"></div>
                    <h2 id="user-of-your-personal-information">Use of your personal information</h2>
                    <p>IS 5.5 will only use your personal information for the purposes for which it was collected (or for a use identified as consistent with that purpose).</p>
                    <p>IS 5.5 use your personal information only for following purposes.</p>
                    <ul>
                        <li>To provide you with personalized user experience. IS 5.5 use your Name, profile pictures you upload for this purpose.</li>
                        <li>To protect your account from unauthorized access or potential hacking attempt. IS 5.5 use HTTP or TCP/IP Headers for this purpose</li>
                        <ul>
                            <li>This includes,</li>
                            <ul>
                                <li>IP address,</li>
                                <li>Browser fingerprinting,</li>
                                <li>Cookies</li>
                            </ul>
                        </ul>
                        <li>Derive statistical data for analytical purposes on system performance improvements. IS 5.5 will not keep any personal information after statistical calculations. Thus statistical report has no means to identify an individual person.</li>
                        <ul>
                            <li>IS 5.5 may use</li>
                            <ul>
                                <li>IP Address to derive Geographic information</li>
                                <li>Browser fingerprinting to determine the Browser technology, version.</li>
                            </ul>
                        </ul>
                    </ul>
                    <div class="margin-bottom-double"></div>
                    <h2 id="disclosure-of-your-personal-information">Disclosure of your personal information</h2>
                    <p>IS 5.5 will only disclose personal information for the purposes for which it was collected (or for a use identified as consistent with that purpose), unless you have otherwise consented or where it is required by law.</p>
                    <div class="margin-bottom"></div>
                    <h3 id="legal-process">Legal process</h3>
                    <p>IS 5.5 may disclose your personal information with or without your consent where it is required by law following due land lawful process.</p>
                    <div class="margin-bottom-double"></div>
                    <h2 id="how-is-5.5-store-or-keep-your-personal-information">How IS 5.5 store or keep your personal information</h2>
                    <div class="margin-bottom"></div>
                    <h3 id="where-your-personal-information-stored">Where your personal information stored</h3>
                    <p>IS 5.5 store your personal information in secured Databases. IS 5.5 exercise proper industry accepted security measures to protect the database where your personal information is held.</p>
                    <p>IS 5.5 may use encryption to keep your personal data with added level of security.</p>
                    <div class="margin-bottom"></div>
                    <h3 id="how-long-does-is-5.5-keep-your-personal-information">How long IS 5.5 keep your personal information</h3>
                    <p>IS 5.5 keep your personal data as long as you are an active user of our system. You can update your personal data at any time with the given self-care user portals.</p>
                    <p>IS 5.5 may keep hashed secrets to provide you with added level of security. This includes,</p>
                    <ul>
                        <ul>
                            <li>Password</li>
                            <li>Previously used passwords.</li>
                        </ul>
                    </ul>
                    <div class="margin-bottom"></div>
                    <h3 id="how-can-you-request-a-removal-of-your-personal-information">How can you request a removal of your personal information</h3>
                    <p>You can request deletion of your account with the administrator. The administrator will be the administrator of the tenant you are registered or the super-administrator if you do not use the tenant feature.</p>
                    <p>You can additionally request to anonymize all traces of your activities IS 5.5 may have retained in Logs, Databases or Analytical storage.</p>
                    <div class="margin-bottom-double"></div>
                    <h2 id="about">About</h2>
                    <div class="margin-bottom"></div>
                    <h3 id="changes-to-this-policy">Changes to this policy</h3>
                    <p>The organization running IS 5.5 may revise the Privacy Policy from time to time. You can find the most recent governing policy with the respective link provided by the organization running IS 5.5. The organization will notify any changes to the privacy policy over our oficial public channels.</p>
                    <div class="margin-bottom"></div>
                    <h3 id="your-choices">Your choices</h3>
                    <p>If you are already have an account with Identity server; you have right to deactivate your account if you find that this privacy policy is unacceptable for you.</p>
                    <p>If you do not have an account, you can chose not to subscribe, if you do not agree with our privacy policy.</p>
                    <div class="margin-bottom"></div>
                    <h3 id="contact-us">Contact us</h3>
                    <p>Please contact us if you have any question or concerns of this privacy policy.</p>
                    <p><a href="https://wso2.com/contact/">https://wso2.com/contact/</a></p>
                    <div class="margin-bottom-double"></div>
                    <h2 id="disclaimer">Disclaimer</h2>
                    <ol>
                        <li>This privacy policy statement serves as a template for the organization running WOS2 IS 5.5. The organizational policies will govern the real privacy policy applicable for its business purposes.</li>
                        <li>WSO2 or its employees, partners, affiliates does not have access to the any data, including privacy data held at the organization running IS 5.5.</li>
                        <li>This policy should be modified according to the organizational requirements.</li>
                    </ol>
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

    $("#privacyPolicy h2,#privacyPolicy h3").each(function() {
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
