<%--
  Copyright (c) 2010 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.

   WSO2 Inc. licenses this file to you under the Apache License,
   Version 2.0 (the "License"); you may not use this file except
   in compliance with the License.
   You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing,
   software distributed under the License is distributed on an
   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
   KIND, either express or implied.  See the License for the
   specific language governing permissions and limitations
   under the License.
  --%>

<%@ page import="org.owasp.encoder.Encode" %>
<jsp:include page="../dialog/display_messages.jsp"/>

<script type="text/javascript">
    var msgId;
    <%
    if(request.getParameter("msgId") == null){
    %>
    msgId = '<%="MSG" + System.currentTimeMillis() + Math.random()%>';
    <%
    } else {
    %>
    msgId = '<%=Encode.forJavaScriptBlock(request.getParameter("msgId"))%>';
    <%
    }
    %>
</script>
<%

    if (request.getParameter("errorMessage") != null) {
%>
<script type="text/javascript">
    jQuery(document).ready(function () {
        if (getCookie(msgId) == null) {
            CARBON.showErrorDialog("<%=Encode.forJavaScriptBlock(Encode.forHtml(request.getParameter("errorMessage")))%>");
            setCookie(msgId, 'true');
        }
    })
</script>
<%
} else if (request.getParameter("message") != null) {
%>
<script type="text/javascript">
    jQuery(document).ready(function () {
        if (getCookie(msgId) == null) {
            CARBON.showInfoDialog("<%=Encode.forJavaScriptBlock(Encode.forHtml(request.getParameter("message")))%>");
            setCookie(msgId, 'true');
        }
    });
</script>
<%
    }
%>