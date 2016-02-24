<!--
/*
* Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.ObligationDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.PolicyRefIdDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.RowDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.TargetDTO" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<jsp:useBean id="entitlementPolicyBean" type="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean"
             class="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean" scope="session"/>
<jsp:setProperty name="entitlementPolicyBean" property="*" />

<%
    int rowNumber = 0;
    int targetRowIndex = -1;
    int obligationRowIndex = -1;

    int maxTargetRows = 0;
    int maxObligationRows = 0;

    String categoryType = null;
    String selectedAttributeDataType = null;
    String selectedAttributeId = null;
    TargetDTO targetDTO = new TargetDTO();
    entitlementPolicyBean.setPolicyReferenceOrder(null);

    String nextPage = request.getParameter("nextPage");
    String delete = request.getParameter("delete");
    String policyRefId = request.getParameter("policyRefId");
    String policySearchString = request.getParameter("policySearchString");
    if(policyRefId != null && policyRefId.trim().length() > 0){
        PolicyRefIdDTO policyRefIdDTO = new PolicyRefIdDTO();
        policyRefIdDTO.setId(policyRefId);
        policyRefIdDTO.setReferenceOnly(true);
        String policyType = request.getParameter("policyType");
        if("PolicySet".equals(policyType)){
            policyRefIdDTO.setPolicySet(true);
        }
        entitlementPolicyBean.addPolicyRefId(policyRefIdDTO);
    }


    String targetRowIndexString = request.getParameter("targetRowIndex");
    String obligationRowIndexString = request.getParameter("obligationRowIndex");

    String maxTargetRowsString = request.getParameter("maxTargetRows");
    String maxObligationRowsString = request.getParameter("maxObligationRows");

    try{
        if(maxTargetRowsString != null && maxTargetRowsString.trim().length() > 0){
            maxTargetRows = Integer.parseInt(maxTargetRowsString);
        }
        if(maxObligationRowsString != null && maxObligationRowsString.trim().length() > 0){
            maxObligationRows = Integer.parseInt(maxObligationRowsString);
        }

        if(targetRowIndexString != null && targetRowIndexString.trim().length() > 0){
            targetRowIndex = Integer.parseInt(targetRowIndexString);
        }
        if(obligationRowIndexString != null && obligationRowIndexString.trim().length() > 0){
            obligationRowIndex = Integer.parseInt(obligationRowIndexString);
        }
    } catch (Exception e){
        //if number format exceptions.. just ignore
    }

    String policyReferenceOrder = request.getParameter("policyReferenceOrder");

    for(rowNumber = 0; rowNumber < maxTargetRows + 1; rowNumber ++){

        RowDTO  rowDTO = new RowDTO();
        String targetCategory = request.getParameter("targetCategory_" + rowNumber);
        if(targetRowIndex == rowNumber){
            categoryType = targetCategory;
            rowDTO.setNotCompleted(true);
        }
        if(targetCategory != null && targetCategory.trim().length() > 0){
            rowDTO.setCategory(targetCategory);
        } else {
            continue;
        }

        String targetPreFunction = request.getParameter("targetPreFunction_" + rowNumber);
        if(targetPreFunction != null){
            rowDTO.setPreFunction(targetPreFunction);
        }

        String targetFunction = request.getParameter("targetFunction_" + rowNumber);
        if(targetFunction != null){
            rowDTO.setFunction(targetFunction);
        }


        String targetAttributeId = request.getParameter("targetAttributeId_" + rowNumber);
        if(targetAttributeId != null){
            rowDTO.setAttributeId(targetAttributeId);
            if(targetRowIndex == rowNumber){
                selectedAttributeId = targetAttributeId;
            }
        }

        String targetAttributeType = request.getParameter("targetAttributeTypes_" + rowNumber);
        if(targetAttributeType != null){
            rowDTO.setAttributeDataType(targetAttributeType);
            if(targetRowIndex == rowNumber){
                selectedAttributeDataType = targetAttributeType;
            }
        }

        String targetCombineFunction = request.getParameter("targetCombineFunctions_" + rowNumber);
        if(targetCombineFunction != null){
            rowDTO.setCombineFunction(targetCombineFunction);
        }

        String targetAttributeValue = request.getParameter("targetAttributeValue_" + rowNumber);
        if(targetAttributeValue != null && targetAttributeValue.trim().length() > 0){
            rowDTO.setAttributeValue(targetAttributeValue);
        } else {
            if(targetAttributeValue != null && targetAttributeValue.trim().length() > 0){
                rowDTO.setAttributeValue(targetAttributeValue);
            } else {
                if(targetRowIndex == rowNumber){
                    targetDTO.addRowDTO(rowDTO);
                }
                continue;
            }
        }
        targetDTO.addRowDTO(rowDTO);
    }

    // set target element to entitlement bean
    entitlementPolicyBean.setTargetDTO(targetDTO);

    List<ObligationDTO> obligationDTOs = new ArrayList<ObligationDTO>();
    for(rowNumber = 0; rowNumber < maxObligationRows + 1; rowNumber ++){

        ObligationDTO dto = new ObligationDTO();
        String obligationType = request.getParameter("obligationType_" + rowNumber);
        if(obligationRowIndex == rowNumber){
            categoryType = null;          // TODO
            dto.setNotCompleted(true);
        }
        if(obligationType != null){
            dto.setType(obligationType);
        } else{
            continue;
        }
        String obligationId = request.getParameter("obligationId_" + rowNumber);
        if(obligationId != null && obligationId.trim().length() > 0){
            dto.setObligationId(obligationId);
        } else {
            continue;
        }

        String obligationAttributeValue = request.getParameter("obligationAttributeValue_" + rowNumber);
        if(obligationAttributeValue != null){
            dto.setAttributeValue(obligationAttributeValue);
        }

        String obligationAttributeId = request.getParameter("obligationAttributeId_" + rowNumber);
        if(obligationAttributeId != null){
            dto.setResultAttributeId(obligationAttributeId);
        }

        String obligationEffect = request.getParameter("obligationEffect_" + rowNumber);
        if(obligationEffect != null){
            dto.setEffect(obligationEffect);
        }
        // Set obligations
        obligationDTOs.add(dto);
    }
    entitlementPolicyBean.setObligationDTOs(obligationDTOs);

    String forwardTo = nextPage + ".jsp";
    if(policyReferenceOrder != null && policyReferenceOrder.trim().length() > 0){
        if(policyRefId != null && policyRefId.trim().length() > 0 && !"true".equals(delete)){
            entitlementPolicyBean.setPolicyReferenceOrder(policyReferenceOrder + "," + policyRefId);
        } else {
            entitlementPolicyBean.setPolicyReferenceOrder(policyReferenceOrder);
        }
    }

    if("true".equals(delete)){
        forwardTo = "delete-policy-entry.jsp";
        if(policyRefId != null && policyRefId.trim().length() > 0){
            forwardTo = forwardTo + "?policyRefId=" + Encode.forUriComponent(policyRefId);
        }
    } else if(categoryType != null && categoryType.trim().length() > 0){
        forwardTo = forwardTo + "?category=" + categoryType + "&returnPage=create-policy-set";
        if(selectedAttributeDataType != null && selectedAttributeDataType.trim().length() > 0){
            forwardTo = forwardTo + "&selectedAttributeDataType=" + Encode.forUriComponent(selectedAttributeDataType);
        }
        if(selectedAttributeId != null && selectedAttributeId.trim().length() > 0){
            forwardTo = forwardTo + "&selectedAttributeId=" + Encode.forUriComponent(selectedAttributeId);
        }
    } else if(policySearchString != null && policySearchString.trim().length() > 0){
        forwardTo = forwardTo + "?policySearchString=" + Encode.forUriComponent(policySearchString);
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