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

<jsp:useBean id="entitlementPolicyBean" type="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean"
             class="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean" scope="session"/>
<jsp:setProperty name="entitlementPolicyBean" property="*" />

<%
    String nextPage = request.getParameter("nextPage");
    String cellNo = request.getParameter("cellNo");
    
    int dataRowNumber = 1;
    String userInputDataString = "";
    while(true) {
        if (request.getParameter("data" + dataRowNumber + "7") == null) {
            break;
        }
        if (!userInputDataString.trim().equals("")) {
             userInputDataString = userInputDataString + "!";
        }
        for(int j = 1; j < 8 ; j ++ ) {
            if (userInputDataString.trim().equals("")) {
                if(request.getParameter("data" + dataRowNumber + (j-1)) != null && request.
                        getParameter("data" + dataRowNumber + (j-1)).trim().equals("")){
                    userInputDataString = ";" + request.getParameter("data" + dataRowNumber + j).trim();
                } else {
                    userInputDataString = request.getParameter("data" + dataRowNumber + j).trim();
                }
            } else {
                userInputDataString = userInputDataString + ";" + request.
                        getParameter("data" + dataRowNumber + j).trim();
            }
        }
        dataRowNumber ++;
    }
    
    entitlementPolicyBean.setUserInputData(userInputDataString);
    String forwardTo = nextPage + ".jsp?cellNo=" + cellNo;
%>

<script type="text/javascript">
    function forward() {
        location.href = "<%=forwardTo%>";
    }
</script>

<script type="text/javascript">
    forward();
</script>