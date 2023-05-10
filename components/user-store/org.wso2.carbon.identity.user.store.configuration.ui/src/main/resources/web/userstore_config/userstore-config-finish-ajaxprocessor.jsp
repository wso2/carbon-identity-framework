        <!--
/*
* Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
-->
<%@ page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.identity.user.store.configuration.stub.dto.PropertyDTO" %>
<%@ page import="org.wso2.carbon.identity.user.store.configuration.stub.dto.UserStoreDTO" %>
<%@ page import="org.wso2.carbon.identity.user.store.configuration.ui.client.UserStoreConfigAdminServiceClient" %>
<%@ page import="org.wso2.carbon.identity.user.store.configuration.utils.SecondaryUserStoreConfigurationUtil" %>
<%@ page import="org.wso2.carbon.ndatasource.common.DataSourceException" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.user.mgt.ui.UserAdminUIConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ArrayList" %>
        <%@ page import="java.util.ResourceBundle" %>

        <%
            String httpMethod = request.getMethod();
            if (!"post".equalsIgnoreCase(httpMethod)) {
                response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                return;
            }

            int maxDefaultProperties = 1000;
            String forwardTo = "index.jsp";
            String BUNDLE = "org.wso2.carbon.identity.user.store.configuration.ui.i18n.Resources";
            ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
            String className = request.getParameterValues("classApplied")[0];
            String domain = request.getParameterValues("domainId")[0];
            String previousDomain = request.getParameterValues("previousDomainId")[0];
            String description = request.getParameterValues("description")[0];
            String repositoryClass = null;
            if (SecondaryUserStoreConfigurationUtil.isUserStoreRepositorySeparationEnabled()) {
                repositoryClass = request.getParameterValues("repositoryName")[0];
            }
            int defaultProperties = Integer.parseInt(request.getParameter("defaultProperties").replaceAll("[\\D]", ""));    //number of default properties

            if (maxDefaultProperties < defaultProperties) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            UserStoreConfigAdminServiceClient userStoreConfigAdminServiceClient = null;
            try{
            	
                String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
                String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
                ConfigurationContext configContext =
                        (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
                userStoreConfigAdminServiceClient = new UserStoreConfigAdminServiceClient(cookie, backendServerURL, configContext);


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

                    if(domain != null && domain != "" && !domain.equalsIgnoreCase(UserAdminUIConstants.INTERNAL_DOMAIN)
                       && !domain.equalsIgnoreCase(UserAdminUIConstants.APPLICATION_DOMAIN)) {
        	            if(previousDomain != null && previousDomain != "") {
        	                    // This is an update
        	                    if(previousDomain != domain) {
        	                            // update userstore with domain name change
        	                            userStoreConfigAdminServiceClient.updateUserStoreWithDomainName(previousDomain, userStoreDTO);
        	                    } else {
        	                            // update userstore with same domain name
        	                            userStoreConfigAdminServiceClient.updateUserStore(userStoreDTO);
        	                    }
        	            }
        	            else {
        	                    // This is an add
        	                    userStoreConfigAdminServiceClient.addUserStore(userStoreDTO);
        	            }

        	            // Session need to be update according to new user store info 
        	            session.removeAttribute(UserAdminUIConstants.USER_STORE_INFO);
        	            session.removeAttribute(UserAdminUIConstants.USER_LIST_CACHE);
        	            session.removeAttribute(UserAdminUIConstants.ROLE_LIST_CACHE);
        	            
        	        	String message = resourceBundle.getString("successful.update");
        	        	CarbonUIMessage.sendCarbonUIMessage(message,CarbonUIMessage.INFO, request);
                    }
                    else {
                        String message = resourceBundle.getString("invalid.domain");
                        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
                        forwardTo = "index.jsp?region=region1&item=userstores_mgt_menu";
                    }

            }catch(DataSourceException dse){
            	String message = dse.getMessage();
                CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
                forwardTo = "index.jsp?region=region1&item=userstores_mgt_menu";
            } catch (Exception e) {
                String message = resourceBundle.getString("error.update");
                CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
                forwardTo = "index.jsp?region=region1&item=userstores_mgt_menu";
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
