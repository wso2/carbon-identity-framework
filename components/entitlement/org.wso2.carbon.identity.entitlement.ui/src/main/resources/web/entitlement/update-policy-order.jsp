<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.identity.entitlement.stub.dto.PaginatedPolicySetDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyConstants" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.client.EntitlementPolicyAdminServiceClient" %>
<%@ page
        import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ResourceBundle" %>
<!--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->


<%
    String forwardTo = null;
    String policyOrder = request.getParameter("policyOrder");
    String policyTypeFilter = request.getParameter("policyTypeFilter");
    String policySearchString = request.getParameter("policySearchString");
    String pageNumber = request.getParameter("pageNumber");
    if (pageNumber == null) {
        pageNumber = "0";
    }
    int pageNumberInt = 0;
    try {
        pageNumberInt = Integer.parseInt(pageNumber);
    } catch (NumberFormatException ignored) {
    }

    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    String BUNDLE = "org.wso2.carbon.identity.entitlement.ui.i18n.Resources";
	ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

    try {
        EntitlementPolicyAdminServiceClient client =
                new EntitlementPolicyAdminServiceClient(cookie, serverURL, configContext);
            if(policyOrder != null && policyOrder.trim().length() > 0){
                String[] policyIds = policyOrder.split(EntitlementPolicyConstants.ATTRIBUTE_SEPARATOR);
                boolean authorize = true;
                PaginatedPolicySetDTO paginatedPolicySetDTO = client.getAllPolicies(policyTypeFilter,
                                                                    policySearchString, pageNumberInt);
                PolicyDTO[] policyDTOs = paginatedPolicySetDTO.getPolicySet();
                if(policyDTOs != null){
                    List<PolicyDTO>  orderedArray = new ArrayList<PolicyDTO>();
                    for(PolicyDTO dto : policyDTOs){
                        if (!dto.getPolicyEditable()) {
                            authorize = false;
                            break;
                        }
                    }

                    if(authorize){
                        for(int i = 0; i < policyIds.length; i ++){
                            PolicyDTO policyDTO = null;
                            for(PolicyDTO dto : policyDTOs){
                                if (policyIds[i].equals(dto.getPolicyId())) {
                                    policyDTO = dto;
                                    break;
                                }
                            }
                            if(policyDTO != null){
                                policyDTO.setPolicyOrder(policyIds.length - i);
                                orderedArray.add(policyDTO);
                            }
                        }
                        client.reOderPolicies(orderedArray.toArray(new PolicyDTO[orderedArray.size()]));
                    } else {
                        String message = resourceBundle.getString("cannot.order.policies");
                        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.WARNING, request);
                    }
                }
            }
            forwardTo = "index.jsp?policyTypeFilter" + policyTypeFilter +
                                                        "&policySearchString=" +policySearchString;
    } catch (Exception e) {
    	String message = resourceBundle.getString("error.while.ordering.policy");
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
        forwardTo = "index.jsp?region=region1&item=policy_menu";
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
