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
<%@ page import="org.wso2.carbon.identity.user.store.configuration.ui.utils.UserStoreUIUtils" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder" %>
<%@ page import="javax.xml.parsers.ParserConfigurationException" %>
<%@ page import="javax.xml.transform.TransformerException" %>
<%@ page import="java.util.ResourceBundle" %>


<%

    String forwardTo;
    String BUNDLE = "org.wso2.carbon.identity.user.store.configuration.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
    String[] orders = request.getParameter("orderList").split(",");
    try{
        UserStoreUIUtils userStoreUIUtils = new UserStoreUIUtils();
        userStoreUIUtils.saveConfigurationToFile(orders);

        String message = resourceBundle.getString("wait");
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.INFO, request);
        forwardTo = "index.jsp?";

    } catch(DataSourceException dse){
  		String message = dse.getMessage();
  		CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
        forwardTo = "index.jsp?";
    } catch (ParserConfigurationException pce) {
        String message = resourceBundle.getString("error.creating.userstore");
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
        forwardTo = "index.jsp?";
    } catch (TransformerException tfe) {
        String message = resourceBundle.getString("error.creating.userstore");
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
        forwardTo = "index.jsp?";
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