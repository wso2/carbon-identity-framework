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
                <%-- Customizable content. Due to this nature, i18n is not implemented for this section --%>
                <div id="privacyPolicy" class="padding-double">
                    <h4><a href="http://wso2.org/library/identity-server"><strong>About WSO2 Identity Server</strong></a></h4>
                    <p>WSO2 Identity Server (referred to as &ldquo;WSO2 IS&rdquo; within this policy) is an open source Identity Management and Entitlement Server that is based on open standards and specifications.</p>
                    <div class="margin-bottom-double"></div>
                    <h2 id="privacy-policy"><strong>Privacy Policy</strong></h2>
                    <p>This policy describes how WSO2 IS captures your personal information, the purposes of collection, and information about the retention of your personal information.</p>
                    <p>Please note that this policy is for reference only, and is applicable for the software as a product. WSO2 Inc. and its developers have no access to the information held within WSO2 IS. Please see the <a href="privacy_policy.do#disclaimer">Disclaimer</a> section for more information</p>
                    <p>Entities, organisations or individuals controlling the use and administration of WSO2 IS should create their own privacy policies setting out the manner in which data is controlled or processed by the respective entity, organisation or individual.</p>
                    <div class="margin-bottom-double"></div>
                    <h2 id="what-is-personal-information">What is personal information?</h2>
                    <p>WSO2 IS considers anything related to you, and by which you may be identified, as your personal information. This includes, but is not limited to:</p>
                    <ul>
                        <li>Your user name (except in cases where the user name created by your employer is under contract)</li>
                        <li>Your date of birth/age</li>
                        <li>IP address used to log in</li>
                        <li>Your device ID if you use a device (e.g., phone or tablet) to log in</li>
                    </ul>
                    <p>However, WSO2 IS also collects the following information that is not considered personal information, but is used only for <strong>statistical</strong> purposes. The reason for this is that this information can not be used to track you.</p>
                    <ul>
                        <li>City/Country from which you originated the TCP/IP connection</li>
                        <li>Time of the day that you logged in (year, month, week, hour or minute)</li>
                        <li>Type of device that you used to log in (e.g., phone or tablet)</li>
                        <li>Operating system and generic browser information</li>
                    </ul>
                    <div class="margin-bottom-double"></div>
                    <h2 id="collection-of-personal-information">Collection of personal information</h2>
                    <p>WSO2 IS collects your information only to serve your access requirements. For example:
                    <ul>
                        <li>WSO2 IS uses your IP address to detect any suspicious login attempts to your account.</li>
                        <li>WSO2 IS uses attributes like your first name, last name, etc., to provide a rich and personalized user experience.</li>
                        <li>WSO2 IS uses your security questions and answers only to allow account recovery.</li>
                    </ul>
                    <div class="margin-bottom"></div>
                    <h3 id="tracking-technologies">Tracking Technologies</h3>
                    <p>WSO2 IS collects your information by:</p>
                    <ul>
                        <li>Collecting information from the user profile page where you enter your personal data.</li>
                        <li>Tracking your IP address with HTTP request, HTTP headers, and TCP/IP.</li>
                        <li>Tracking your geographic information with the IP address.</li>
                        <li>Tracking your login history with browser cookies. Please see our <a href="cookie_policy.do">cookie policy</a> for more information.</li>
                    </ul>
                    <div class="margin-bottom-double"></div>
                    <h2 id="user-of-personal-information">Use of personal information</h2>
                    <p>WSO2 IS will only use your personal information for the purposes for which it was collected (or for a use identified as consistent with that purpose).</p>
                    <p>WSO2 IS uses your personal information only for the following purposes.</p>
                    <ul>
                        <li>To provide you with a personalized user experience. WSO2 IS uses your name and uploaded profile pictures for this purpose.</li>
                        <li>To protect your account from unauthorized access or potential hacking attempts. WSO2 IS uses HTTP or TCP/IP Headers for this purpose.</li>
                        <ul>
                            <li>This includes:</li>
                            <ul>
                                <li>IP address</li>
                                <li>Browser fingerprinting</li>
                                <li>Cookies</li>
                            </ul>
                        </ul>
                        <li>Derive statistical data for analytical purposes on system performance improvements. WSO2 IS will not keep any personal information after statistical calculations. Therefore, the statistical report has no means of identifying an individual person.</li>
                        <ul>
                            <li>WSO2 IS may use:</li>
                            <ul>
                                <li>IP Address to derive geographic information</li>
                                <li>Browser fingerprinting to determine the browser technology or/and version</li>
                            </ul>
                        </ul>
                    </ul>
                    <div class="margin-bottom-double"></div>
                    <h2 id="disclosure-of-personal-information">Disclosure of personal information</h2>
                    <p>WSO2 IS only discloses personal information to the relevant applications (also known as “Service Providers”) that are registered with WSO2 IS. These applications are registered by the identity administrator of your entity or organization. Personal information is disclosed only for the purposes for which it was collected (or for a use identified as consistent with that purpose), as controlled by such Service Providers, unless you have consented otherwise or where it is required by law.</p>
                    <div class="margin-bottom"></div>
                    <h3 id="legal-process">Legal process</h3>
                    <p>Please note that the organisation, entity or individual running WSO2 IS may be compelled to disclose your personal information with or without your consent when it is required by law following due and lawful process.</p>
                    <div class="margin-bottom-double"></div>
                    <h2 id="storage-of-personal-information">Storage of personal information</h2>
                    <div class="margin-bottom"></div>
                    <h3 id="where-your-personal-information-stored">Where your personal information is stored</h3>
                    <p>WSO2 IS stores your personal information in secured databases. WSO2 IS exercises proper industry accepted security measures to protect the database where your personal information is held. WSO2 IS as a product does not transfer or share your data with any third parties or locations. </p>
                    <p>WSO2 IS may use encryption to keep your personal data with an added level of security.</p>
                    <div class="margin-bottom"></div>
                    <h3 id="how-long-does-is-5.5-keep-your-personal-information">How long your personal information is retained</h3>
                    <p>WSO2 IS retains your personal data as long as you are an active user of our system. You can update your personal data at any time using the given self-care user portals.</p>
                    <p>WSO2 IS may keep hashed secrets to provide you with an added level of security. This includes:</p>
                    <ul>
                            <li>Current password</li>
                            <li>Previously used passwords</li>
                    </ul>
                    <div class="margin-bottom"></div>
                    <h3 id="how-to-request-removal-of-your-personal-information">How to request removal of your personal information</h3>
                    <p>You can request the administrator to delete your account. The administrator is the administrator of the tenant you are registered under, or the super-administrator if you do not use the tenant feature.</p>
                    <p>Additionally, you can request to anonymize all traces of your activities that WSO2 IS may have retained in logs, databases or analytical storage.</p>
                    <div class="margin-bottom-double"></div>
                    <h2 id="more-information">More information</h2>
                    <div class="margin-bottom"></div>
                    <h3 id="changes-to-this-policy">Changes to this policy</h3>
                    <p>Upgraded versions of WSO2 IS may contain changes to this policy and revisions to this policy will be packaged within such upgrades. Such changes would only apply to users who choose to use upgraded versions.</p>
                    <p>The organization running WSO2 IS may revise the Privacy Policy from time to time. You can find the most recent governing policy with the respective link provided by the organization running WSO2 IS. The organization will notify any changes to the privacy policy over our official public channels.</p>
                    <div class="margin-bottom"></div>
                    <h3 id="your-choices">Your choices</h3>
                    <p>If you are already have a user account within WSO2 IS, you have the right to deactivate your account if you find that this privacy policy is unacceptable to you.</p>
                    <p>If you do not have an account and you do not agree with our privacy policy, you can chose not to create one.</p>
                    <div class="margin-bottom"></div>
                    <h3 id="contact-us">Contact us</h3>
                    <p>Please contact WSO2 if you have any question or concerns regarding this privacy policy.</p>
                    <p><a href="https://wso2.com/contact/">https://wso2.com/contact/</a></p>
                    <div class="margin-bottom-double"></div>
                    <h2 id="disclaimer">Disclaimer</h2>
                    <ol>
                      <li>WSO2, its employees, partners, and affiliates do not have access to and do not require, store, process or control any of the data, including personal data contained in WSO2 IS. All data, including personal data is controlled and processed by the entity or individual running WSO2 IS.  WSO2, its employees partners and affiliates are not a data processor or a data controller within the meaning of any data privacy regulations.  WSO2 does not provide any warranties or undertake any responsibility or liability in connection with the lawfulness or the manner and purposes for which WSO2 IS is used by such entities or persons. </li>
                      </br>
                      <li>This privacy policy is for the informational purposes of the entity or persons  running WSO2 IS and sets out the processes and functionality contained within WSO2 IS regarding personal data protection. It is the responsibility of entities and persons running WSO2 IS to create and administer its own rules and processes governing users’ personal data, and such  rules and processes may change the use, storage and disclosure policies contained herein. Therefore users should consult the entity or persons running WSO2 IS for its own privacy policy for details governing users’ personal data. </li>
                    </ol>
                </div>
                <%-- /Costomizable content --%>
                <div class="clearfix"></div>
            </div>
        </div>
        <!-- /content -->
    </div>
</div>
