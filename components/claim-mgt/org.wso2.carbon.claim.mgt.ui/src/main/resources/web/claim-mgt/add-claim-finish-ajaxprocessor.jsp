<%--
  ~ Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

<%@page import="org.apache.axis2.context.ConfigurationContext" %>
<%@page import="org.wso2.carbon.CarbonConstants" %>
<%@page import="org.wso2.carbon.claim.mgt.stub.dto.ClaimAttributeDTO" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
           prefix="carbon" %>
<%@ page import="org.wso2.carbon.claim.mgt.stub.dto.ClaimDTO" %>
<%@ page import="org.wso2.carbon.claim.mgt.stub.dto.ClaimMappingDTO" %>
<%@ page import="org.wso2.carbon.claim.mgt.ui.client.ClaimAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>


<%@page import="org.wso2.carbon.utils.ServerConstants" %>
<%@page import="java.text.MessageFormat" %>
<%@page import="java.util.ArrayList" %>
<%@page import="java.util.List" %>
<%@ page import="org.owasp.encoder.Encode" %>

<%
    String httpMethod = request.getMethod();
    if (!"post".equalsIgnoreCase(httpMethod)) {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        return;
    }

    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().getAttribute(
            CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    String forwardTo = null;
    ClaimMappingDTO mapping = null;
    ClaimDTO claim = null;
    String displayName = request.getParameter("displayName");
    String description = request.getParameter("description");
    String claimUri = request.getParameter("claimUri");
    String dialect = request.getParameter("dialect");
    String attribute = request.getParameter("attribute");
    String regex = request.getParameter("regex");
    String supported = request.getParameter("supportedhidden");
    String required = request.getParameter("requiredhidden");
    String readonly = request.getParameter("readonlyhidden");
    String store = request.getParameter("store");
    String displayOrder = request.getParameter("displayOrder");
    String BUNDLE = "org.wso2.carbon.claim.mgt.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

    try {
        ClaimAdminClient client = new ClaimAdminClient(cookie, serverURL, configContext);
        mapping = new ClaimMappingDTO();
        claim = new ClaimDTO();
        claim.setClaimUri(claimUri);
        claim.setDisplayTag(displayName);
        claim.setDescription(description);
        claim.setDialectURI(dialect);
        claim.setRegEx(regex);
        if (displayOrder.trim().length() == 0) {
            displayOrder = "0";
        }
        claim.setDisplayOrder(Integer.parseInt(displayOrder));

        if ("true".equals(required)) {
            claim.setRequired(true);
        }

        if ("true".equals(supported)) {
            claim.setSupportedByDefault(true);
        }

        if ("true".equals(readonly)) {
            claim.setReadOnly(true);
        }

        mapping.setClaim(claim);

        if (attribute != null) {
            String[] attributes = attribute.split(";");
            List<ClaimAttributeDTO> attrList = new ArrayList<ClaimAttributeDTO>();

            for (int i = 0; i < attributes.length; i++) {
                int index = 0;
                if ((index = attributes[i].indexOf("/")) > 1) {
                    String domain = attributes[i].substring(0, index);
                    String attrName = attributes[i].substring(index + 1);
                    if (domain != null) {
                        ClaimAttributeDTO attr = new ClaimAttributeDTO();
                        attr.setAttributeName(attrName);
                        attr.setDomainName(domain);
                        attrList.add(attr);
                    } else {
                        mapping.setMappedAttribute(attributes[i]);
                    }
                } else {
                    mapping.setMappedAttribute(attributes[i]);
                }
            }

            if (attrList.size() > 0) {
                mapping.setMappedAttributes(attrList.toArray(new ClaimAttributeDTO[attrList.size()]));
            }

        }

        client.addNewClaimMappping(mapping);
        forwardTo = "claim-view.jsp?store=" + Encode.forUriComponent(store) + "&dialect=" + Encode.forUriComponent(dialect) + "&ordinal=1";
    } catch (Exception e) {
        String unformatted = resourceBundle.getString("error.adding.claim.mapping");
        String message = MessageFormat.format(unformatted, new Object[]{e.getMessage()});
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
        forwardTo = "add-claim.jsp?dialect=" + Encode.forUriComponent(dialect) + "&ordinal=2";
    }
%>

<%@page import="java.util.ResourceBundle" %>
<script
        type="text/javascript">
    function forward() {
        location.href = "<%=forwardTo%>";
    }
</script>

<script type="text/javascript">
    forward();
</script>
