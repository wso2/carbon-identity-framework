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

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.apache.commons.fileupload.FileItem" %>
<%@ page import="org.apache.commons.fileupload.FileItemFactory" %>
<%@ page import="org.apache.commons.fileupload.disk.DiskFileItem" %>
<%@page import="org.apache.commons.fileupload.disk.DiskFileItemFactory" %>
<%@page import="org.apache.commons.fileupload.servlet.ServletFileUpload" %>
<%@page import="org.apache.commons.fileupload.servlet.ServletRequestContext" %>
<%@page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@page import="org.wso2.carbon.ui.CarbonUIUtil" %>

<%@page import="org.wso2.carbon.user.mgt.ui.UserAdminClient"%>
<%@page import="org.wso2.carbon.user.mgt.ui.Util"%>
<%@page import="org.wso2.carbon.utils.ServerConstants"%>
<%@page import="java.util.List"%>

<script type="text/javascript" src="../admin/js/main.js"></script>

<%
    String httpMethod = request.getMethod();
    if (!"post".equalsIgnoreCase(httpMethod)) {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        return;
    }

        String forwardTo = null;
        try {

            if (ServletFileUpload.isMultipartContent(request)) {
                FileItemFactory factory = new DiskFileItemFactory();
                ServletFileUpload upload = new ServletFileUpload(factory);
                List items = upload.parseRequest(new ServletRequestContext(request));
                
                byte[] content = null;
                String fileName = null;
                String password = null;
                String userStoreDomain = null;
                for (Object item : items) {
                    DiskFileItem diskFileItem = (DiskFileItem) item;
                    String name = diskFileItem.getFieldName();
                    if (name.equals("usersFile")) {
                        FileItem fileItem = diskFileItem;
                        fileName = fileItem.getName();
                        int index = fileName.lastIndexOf("\\");
                        fileName = fileName.substring(index+1);
                        content = fileItem.get();
                    } else if (name.equals("password")) {
                        password = new String(diskFileItem.get());
                    } else if (name.equals("userStore")){
                        userStoreDomain = new String (diskFileItem.get());
                    }
                }

                String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
                String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
                ConfigurationContext configContext =
                        (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);                
                UserAdminClient client = new UserAdminClient(cookie, backendServerURL, configContext);
                client.bulkImportUsers(userStoreDomain, fileName, Util.buildDataHandler(content), password);
                forwardTo = "user-mgt.jsp?ordinal=1";
            } else {
                throw new Exception("unexpected.data");
            }
        } catch (Exception e) {
            forwardTo = "user-mgt.jsp?ordinal=1";;
            CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request);
        }
    %>

<script type="text/javascript">
    function forward() {
        location.href = "<%=forwardTo%>";
    }
</script>

<script type="text/javascript">
    forward();
</script>
