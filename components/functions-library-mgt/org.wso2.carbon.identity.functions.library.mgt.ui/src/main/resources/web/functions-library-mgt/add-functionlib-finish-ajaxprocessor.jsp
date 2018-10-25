<%@ page import="org.wso2.carbon.identity.functions.library.mgt.model.xsd.FunctionLibrary" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.identity.functions.library.mgt.ui.client.*" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>


<%

    String httpMethod = request.getMethod();
    if (!"post".equalsIgnoreCase(httpMethod)) {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        return;
    }

    String functionLibName = request.getParameter("functionLibName");
    String description = request.getParameter("functionLib-description");
    String content = request.getParameter("scriptTextArea");

    if (functionLibName != null && !"".equals(functionLibName)) {

        FunctionLibrary functionLibrary = new FunctionLibrary();
        functionLibrary.setFunctionLibraryName(functionLibName+".js");
        functionLibrary.setDescription(description);
        functionLibrary.setFunctionLibraryScript(content);

        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
try{
    FunctionLibraryManagementServiceClient serviceClient = new FunctionLibraryManagementServiceClient(cookie,backendServerURL,configContext);
    serviceClient.createFunctionLibrary(functionLibrary);
%>
<script>
    location.href = 'functions-library-mgt-list.jsp';
</script>
<%
} catch (Exception e) {
    CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
%>
<script>
    location.href = 'functions-library-mgt-add.jsp';
</script>
<%
    }
    }else{
%>
<script>
    location.href = 'functions-library-mgt-add.jsp';
</script>
<%
    }%>


