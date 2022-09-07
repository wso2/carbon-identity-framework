<%--
  ~ Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
           prefix="carbon" %>

<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>


<%@page import="org.wso2.carbon.utils.ServerConstants" %>
<%@page import="java.text.MessageFormat" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.identity.claim.metadata.mgt.stub.dto.ExternalClaimDTO" %>
<%@ page import="org.wso2.carbon.identity.claim.metadata.mgt.ui.client.ClaimMetadataAdminClient" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.identity.claim.metadata.mgt.stub.dto.LocalClaimDTO" %>
<%@ page import="org.wso2.carbon.identity.claim.metadata.mgt.stub.dto.ClaimPropertyDTO" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.wso2.carbon.identity.claim.metadata.mgt.stub.dto.AttributeMappingDTO" %>
<%@ page import="org.wso2.carbon.identity.claim.metadata.mgt.ui.utils.ClaimConstants" %>


<%
    String httpMethod = request.getMethod();
    if (!"post".equalsIgnoreCase(httpMethod)) {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        return;
    }

    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext)
            config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    String localClaimURI = StringUtils.trim(request.getParameter("localClaimURI"));
    int numberOfAttributeMappings = Integer.parseInt(request.getParameter("number_of_AttributeMappings"));
    int numberOfClaimProperties = Integer.parseInt(request.getParameter("number_of_ClaimProperties"));
    String displayName = request.getParameter("displayName");
    String description = request.getParameter("description");
    String regex = request.getParameter("regex");
    String displayOrder = request.getParameter("displayOrder");
    String supported = request.getParameter("supportedhidden");
    String required = request.getParameter("requiredhidden");
    String readonly = request.getParameter("readonlyhidden");

    List<AttributeMappingDTO> attributeMappings = new ArrayList();

    for (int i = 0; i < numberOfAttributeMappings; i++) {

        String userStoreDomain = request.getParameter("userstore_" + i);
        String mappedAttribute = StringUtils.trim(request.getParameter("attribute_" + i));

        if (StringUtils.isNotBlank(userStoreDomain) && StringUtils.isNotBlank(mappedAttribute)) {

            AttributeMappingDTO attributeMapping = new AttributeMappingDTO();
            attributeMapping.setUserStoreDomain(userStoreDomain);
            attributeMapping.setAttributeName(mappedAttribute);

            attributeMappings.add(attributeMapping);
        }
    }

    List<ClaimPropertyDTO> claimProperties = new ArrayList();

    for (int i = 0; i < numberOfClaimProperties; i++) {

        String propertyName = request.getParameter("propertyName_" + i);
        String propertyValue = request.getParameter("propertyValue_" + i);

        if (StringUtils.isNotBlank(propertyName)) {

            ClaimPropertyDTO claimProperty = new ClaimPropertyDTO();
            claimProperty.setPropertyName(propertyName);
            claimProperty.setPropertyValue(propertyValue);

            claimProperties.add(claimProperty);
        }
    }

    if (StringUtils.isNotBlank(displayName)) {
        ClaimPropertyDTO dispalyNameProperty = new ClaimPropertyDTO();
        dispalyNameProperty.setPropertyName(ClaimConstants.DISPLAY_NAME_PROPERTY);
        dispalyNameProperty.setPropertyValue(displayName);
        claimProperties.add(dispalyNameProperty);
    }

    if (StringUtils.isNotBlank(description)) {
        ClaimPropertyDTO descriptionProperty = new ClaimPropertyDTO();
        descriptionProperty.setPropertyName(ClaimConstants.DESCRIPTION_PROPERTY);
        descriptionProperty.setPropertyValue(description);
        claimProperties.add(descriptionProperty);
    }

    if (StringUtils.isNotBlank(regex)) {
        ClaimPropertyDTO regexProperty = new ClaimPropertyDTO();
        regexProperty.setPropertyName(ClaimConstants.REGULAR_EXPRESSION_PROPERTY);
        regexProperty.setPropertyValue(regex);
        claimProperties.add(regexProperty);
    }

    if (StringUtils.isNotBlank(displayOrder)) {
        ClaimPropertyDTO dispalyOrderProperty = new ClaimPropertyDTO();
        dispalyOrderProperty.setPropertyName(ClaimConstants.DISPLAY_ORDER_PROPERTY);
        dispalyOrderProperty.setPropertyValue(displayOrder);
        claimProperties.add(dispalyOrderProperty);
    } else {
        displayOrder = "0";
    }

    if (StringUtils.isNotBlank(supported)) {
        ClaimPropertyDTO supportedProperty = new ClaimPropertyDTO();
        supportedProperty.setPropertyName(ClaimConstants.SUPPORTED_BY_DEFAULT_PROPERTY);
        supportedProperty.setPropertyValue(supported);
        claimProperties.add(supportedProperty);
    }

    if (StringUtils.isNotBlank(required)) {
        ClaimPropertyDTO requiredProperty = new ClaimPropertyDTO();
        requiredProperty.setPropertyName(ClaimConstants.REQUIRED_PROPERTY);
        requiredProperty.setPropertyValue(required);
        claimProperties.add(requiredProperty);
    }

    if (StringUtils.isNotBlank(readonly)) {
        ClaimPropertyDTO readOnlyProperty = new ClaimPropertyDTO();
        readOnlyProperty.setPropertyName(ClaimConstants.READ_ONLY_PROPERTY);
        readOnlyProperty.setPropertyValue(readonly);
        claimProperties.add(readOnlyProperty);
    }

    LocalClaimDTO localClaim = new LocalClaimDTO();
    localClaim.setLocalClaimURI(localClaimURI);

    localClaim.setAttributeMappings(attributeMappings.toArray(new AttributeMappingDTO[0]));
    localClaim.setClaimProperties(claimProperties.toArray(new ClaimPropertyDTO[0]));


    String forwardTo = null;
    try {

        ClaimMetadataAdminClient client = new ClaimMetadataAdminClient(cookie, serverURL, configContext);
        client.addLocalClaim(localClaim);
        forwardTo = "list-local-claims.jsp?ordinal=1";

    } catch (Exception e) {

        String BUNDLE = "org.wso2.carbon.identity.claim.metadata.mgt.ui.i18n.Resources";
        ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

        String unformatted = resourceBundle.getString("error.adding.local.claim");
        String message = MessageFormat.format(unformatted, new
                Object[]{Encode.forHtmlContent(localClaimURI)});

        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
        forwardTo = "add-local-claim.jsp?localClaimURI=" + Encode.forUriComponent(localClaimURI) +
                "&ordinal=2";

    }
%>
<script type="text/javascript">
    function forward() {
        location.href = "<%=forwardTo%>";
    }

    forward();
</script>