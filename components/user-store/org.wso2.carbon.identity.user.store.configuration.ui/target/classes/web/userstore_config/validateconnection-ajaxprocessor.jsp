<%@ page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.identity.user.store.configuration.ui.client.UserStoreConfigAdminServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%
    String domainName = request.getParameter("domainName");
    String driverName = request.getParameter("driverName");
    String connectionURL = request.getParameter("connectionURL");
    String username = request.getParameter("username");
    String connectionPassword = request.getParameter("connectionPassword");
    String messageID = request.getParameter("messageID");

    boolean canAdd ;
    if (domainName != null && !"".equals(domainName)) {
    	String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    	UserStoreConfigAdminServiceClient client;
        try {
        	client = new UserStoreConfigAdminServiceClient(cookie, backendServerURL, configContext);;
        	
            canAdd = client.testRDBMSConnection(domainName, driverName, connectionURL, username, connectionPassword,
                    messageID);
%>
<%=canAdd%>
<%
} catch (Throwable e) {
%><%=e.getMessage()%><%
        }
    }else{
    	String errmsg = "Please specify a domain name";
    	%><%=errmsg%><%
    	
    }
%>