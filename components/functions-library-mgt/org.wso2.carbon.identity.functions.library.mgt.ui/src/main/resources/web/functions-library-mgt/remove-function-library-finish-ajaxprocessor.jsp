<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.identity.functions.library.mgt.ui.client.FunctionLibraryManagementServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>


<%
    String httpMethod = request.getMethod();
    if (!"post".equalsIgnoreCase(httpMethod)) {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        return;
    }

    String functionLibraryName = request.getParameter("functionLibraryName");
    String BUNDLE = "org.wso2.carbon.identity.functions.library.mgt.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

    if (functionLibraryName != null && !"".equals(functionLibraryName)) {
        try {

            String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
            String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
            ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
                    .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

             FunctionLibraryManagementServiceClient serviceClient =
                    new FunctionLibraryManagementServiceClient(cookie, backendServerURL, configContext);
            serviceClient.deleteFunctionLibrary(functionLibraryName);

        } catch (Exception e) {
            String message = resourceBundle.getString("functionlibrary.list.error.while.removing.functionlib") + " : " + e.getMessage();
            CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request, e);
        }
    }

%>

<script>
    location.href = 'functions-library-mgt-list.jsp';
</script>
