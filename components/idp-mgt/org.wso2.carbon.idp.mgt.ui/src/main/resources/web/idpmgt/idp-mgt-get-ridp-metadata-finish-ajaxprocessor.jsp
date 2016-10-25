<%@page import="org.apache.axis2.context.ConfigurationContext" %>
<%@page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.idp.mgt.ui.client.IdentityProviderMgtServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page trimDirectiveWhitespaces="true" %>
<%
    String httpMethod = request.getMethod();
    if (!"post".equalsIgnoreCase(httpMethod)) {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        return;
    }
    String BUNDLE = "org.wso2.carbon.idp.mgt.ui.i18n.Resources";
    try {
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        IdentityProviderMgtServiceClient client = new IdentityProviderMgtServiceClient(cookie, backendServerURL, configContext);
        String metadata = client.getResidentIDPMetadata();

        out.clearBuffer();
        byte donneeFichier[] = metadata.getBytes();
        response.setHeader("Content-Disposition", "attachment;filename=\"" + "metadata.xml" + "\"");
        response.setHeader("Content-Type", "application/octet-stream;");
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("Content-Length", String.valueOf(donneeFichier.length));

        for(int i = 0; i < donneeFichier.length; i++){
            out.write(donneeFichier[i]);
        }


    } catch (Exception e) {
        CarbonUIMessage.sendCarbonUIMessage("Error downloading metadata file", CarbonUIMessage.INFO, request);
    } finally {
    }
%>
<script type="text/javascript">
    location.href = "idp-mgt-edit-local.jsp";
</script>
