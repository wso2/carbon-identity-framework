<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>

<%@ page import="java.util.ArrayList" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.identity.user.store.configuration.stub.dto.PropertyDTO" %>
<%@ page import="org.wso2.carbon.identity.user.store.configuration.stub.dto.UserStoreDTO" %>
<%@ page import="org.wso2.carbon.identity.user.store.configuration.ui.client.UserStoreConfigAdminServiceClient" %>
<%@ page import="org.wso2.carbon.identity.user.store.configuration.utils.SecondaryUserStoreConfigurationUtil" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>

<%

    String httpMethod = request.getMethod();
    if (!"post".equalsIgnoreCase(httpMethod)) {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        return;
    }

    String className = request.getParameterValues("classApplied")[0];
    String domain = request.getParameterValues("domainId")[0];
    String previousDomain = request.getParameterValues("previousDomainId")[0];
    String description = request.getParameterValues("description")[0];
    String repositoryClass = null;
    if (SecondaryUserStoreConfigurationUtil.isUserStoreRepositorySeparationEnabled()) {
        repositoryClass = request.getParameterValues("repositoryName")[0];
    }
    int defaultProperties = Integer.parseInt(request.getParameter("defaultProperties").replaceAll("[\\D]", ""));

    UserStoreDTO userStoreDTO = new UserStoreDTO();
    ArrayList<PropertyDTO> propertyList = new ArrayList<PropertyDTO>();
    String value = null;
    for (int i = 0; i < defaultProperties; i++) {
        PropertyDTO propertyDTO = new PropertyDTO();

        if (request.getParameter("propertyName_" + i) != null) {

            if (request.getParameter("propertyValue_" + i) == null) {
                value = "false";
            } else {
                value = request.getParameter("propertyValue_" + i);
                if (value.equals("null")) {
                    value = "false";
                } else if (value.equals("on")) {
                    value = "true";
                }
            }
            propertyDTO.setName(request.getParameter("propertyName_" + i));
            propertyDTO.setValue(value);
            propertyList.add(propertyDTO);
        }
    }
            userStoreDTO.setDomainId(domain);
            userStoreDTO.setDescription(description);
            userStoreDTO.setClassName(className);
            if (SecondaryUserStoreConfigurationUtil.isUserStoreRepositorySeparationEnabled()) {
                userStoreDTO.setRepositoryClass(repositoryClass);
            }
            userStoreDTO.setProperties(propertyList.toArray(new PropertyDTO[propertyList.size()]));

    boolean canAdd;

    if (domain != null && !"".equals(domain)) {
    	String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    	UserStoreConfigAdminServiceClient client;
        try {
        	client = new UserStoreConfigAdminServiceClient(cookie, backendServerURL, configContext);;

            canAdd = client.testLDAPConnection(userStoreDTO);
    %>
        <%=canAdd%>
    <%
        } catch (Throwable e) {
    %><%=e.getMessage()%><%
        }
    } else {
    	String errmsg = "Please specify a domain name";
    	%><%=errmsg%><%

    }
%>