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

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<fmt:bundle basename="org.wso2.carbon.identity.mgt.ui.i18n.Resources">
    
    <script type="text/javascript">
        function cancel(){
            location.href = "../admin/login.jsp";
        }
   </script>

    <link href="css/forgot-password.css" rel="stylesheet" type="text/css" media="all"/>

    <div id="middle">
        <h2><fmt:message key="account.id.recovery"/></h2>
        <p><fmt:message key="account.id.recovery.message"/></p>
    </div>
    <div>
    <form action="forgot_accountId_finish.jsp" id="accountId"  method="post">        
        <table>
            <tbody>
            <tr>
                <td><fmt:message key="email.address"/></td>
                <td>
                    <input type="text" tabindex="2" name="emailAddress" id="emailAddress">
                </td>
            </tr>
            <br/>
            <tr>
                <td><fmt:message key="last.name"/></td>
                <td>
                    <input type="text" tabindex="2" name="lastName" id="lastName">
                </td>
            </tr>
            <tr>
                <td>
                    <input type="button" value="Cancel" onclick="cancel()"/>
                </td>
                <td>
                    <input type="submit"  value="Next"/>
                </td>
            </tr>
            </tbody>
        </table>
    </form>
    </div>
</fmt:bundle>

