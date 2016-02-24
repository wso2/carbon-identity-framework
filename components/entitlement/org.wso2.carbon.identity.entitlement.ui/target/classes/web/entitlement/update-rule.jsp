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
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.ObligationDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.RowDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.RuleDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.TargetDTO" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<jsp:useBean id="entitlementPolicyBean" type="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean"
             class="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean" scope="session"/>
<jsp:setProperty name="entitlementPolicyBean" property="*" />

<%
    int rowNumber = 0;
    int targetRowIndex = -1;
    int ruleRowIndex = -1;
    int targetRuleRowIndex = -1;
    int dynamicRowIndex = -1;
    int obligationRowIndex = -1;

    int maxTargetRows = 0;
    int maxTargetRuleRows = 0;
    int maxRuleRows = 0;
    int maxObligationRuleRows = 0;
    int maxObligationRows = 0;

    String categoryType = null;
    String selectedAttributeDataType = null;
    String selectedAttributeId = null;
    RuleDTO ruleDTO = new RuleDTO();
    TargetDTO targetDTO = new TargetDTO();
    entitlementPolicyBean.setRuleElementOrder(null);

    String targetRowIndexString = request.getParameter("targetRowIndex");
    String ruleRowIndexString = request.getParameter("ruleRowIndex");
    String targetRuleRowIndexString = request.getParameter("targetRuleRowIndex");
    String dynamicRowIndexString = request.getParameter("dynamicRowIndex");
    String obligationRowIndexString = request.getParameter("obligationRowIndex");

    String maxTargetRowsString = request.getParameter("maxTargetRows");
    String maxTargetRuleRowsString = request.getParameter("maxTargetRuleRows");
    String maxRuleRowsString = request.getParameter("maxRuleRows");
    String maxObligationRuleRowsString = request.getParameter("maxObligationRuleRows");
    String maxObligationRowsString = request.getParameter("maxObligationRows");

    try{
        if(maxTargetRowsString != null && maxTargetRowsString.trim().length() > 0){
            maxTargetRows = Integer.parseInt(maxTargetRowsString);
        }
        if(maxTargetRuleRowsString != null && maxTargetRuleRowsString.trim().length() > 0){
            maxTargetRuleRows = Integer.parseInt(maxTargetRuleRowsString);
        }
        if(maxRuleRowsString != null && maxRuleRowsString.trim().length() > 0){
            maxRuleRows = Integer.parseInt(maxRuleRowsString);
        }
        if(maxObligationRuleRowsString != null && maxObligationRuleRowsString.trim().length() > 0){
            maxObligationRuleRows = Integer.parseInt(maxObligationRuleRowsString);
        }
        if(maxObligationRowsString != null && maxObligationRowsString.trim().length() > 0){
            maxObligationRows = Integer.parseInt(maxObligationRowsString);
        }

        if(targetRowIndexString != null && targetRowIndexString.trim().length() > 0){
            targetRowIndex = Integer.parseInt(targetRowIndexString);
        }
        if(ruleRowIndexString != null && ruleRowIndexString.trim().length() > 0){
            ruleRowIndex = Integer.parseInt(ruleRowIndexString);
        }
        if(targetRuleRowIndexString != null && targetRuleRowIndexString.trim().length() > 0){
            targetRuleRowIndex = Integer.parseInt(targetRuleRowIndexString);
        }
        if(dynamicRowIndexString != null && dynamicRowIndexString.trim().length() > 0){
            dynamicRowIndex = Integer.parseInt(dynamicRowIndexString);
        }
        if(obligationRowIndexString != null && obligationRowIndexString.trim().length() > 0){
            obligationRowIndex = Integer.parseInt(obligationRowIndexString);
        }
    } catch (Exception e){
        //if number format exceptions.. just ignore
    }

    String ruleElementOrder = request.getParameter("ruleElementOrder");
    String updateRule = request.getParameter("updateRule");
    String nextPage = request.getParameter("nextPage");
    String ruleId = request.getParameter("ruleId");
    String ruleEffect = request.getParameter("ruleEffect");
    String ruleDescription = request.getParameter("ruleDescription");
    String completedRule = request.getParameter("completedRule");
    String editRule = request.getParameter("editRule");

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
        if(targetAttributeId != null && targetAttributeId.trim().length() > 0){
            rowDTO.setAttributeId(targetAttributeId);
            if(targetRowIndex == rowNumber){
                selectedAttributeId = targetAttributeId;
            }
        }

        String targetAttributeType = request.getParameter("targetAttributeTypes_" + rowNumber);
        if(targetAttributeType != null && targetAttributeType.trim().length() > 0){
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
            if(targetRowIndex == rowNumber){
                targetDTO.addRowDTO(rowDTO);
            }
            continue;
        }

        targetDTO.addRowDTO(rowDTO);
    }

    // set target element to entitlement bean
    entitlementPolicyBean.setTargetDTO(targetDTO);

    if(ruleId != null && ruleId.trim().length() > 0 && !ruleId.trim().equals("null") && editRule == null ) {

        ruleDTO.setRuleId(ruleId);
        ruleDTO.setRuleEffect(ruleEffect);
        if(ruleDescription != null && ruleDescription.trim().length() > 0 ){
            ruleDTO.setRuleDescription(ruleDescription);
        }
        if(completedRule != null && completedRule.equals("true")){
            ruleDTO.setCompletedRule(true);
        }

        TargetDTO ruleTargetDTO = new TargetDTO();

        for(rowNumber = 0; rowNumber < maxTargetRuleRows + 1; rowNumber ++){

            RowDTO  rowDTO = new RowDTO();
            String targetCategory = request.getParameter("ruleTargetCategory_" + rowNumber);
            if(targetRuleRowIndex == rowNumber){
                categoryType = targetCategory;
                rowDTO.setNotCompleted(true);
            }
            if(targetCategory != null && targetCategory.trim().length() > 0){
                rowDTO.setCategory(targetCategory);
            } else {
                continue;
            }

            String targetPreFunction = request.getParameter("ruleTargetPreFunction_" + rowNumber);
            if(targetPreFunction != null){
                rowDTO.setPreFunction(targetPreFunction);
            }

            String targetFunction = request.getParameter("ruleTargetFunction_" + rowNumber);
            if(targetFunction != null){
                rowDTO.setFunction(targetFunction);
            }

            String targetAttributeId = request.getParameter("ruleTargetAttributeId_" + rowNumber);
            if(targetAttributeId != null){
                rowDTO.setAttributeId(targetAttributeId);
                if(targetRuleRowIndex == rowNumber){
                    selectedAttributeId = targetAttributeId;
                }
            }

            String targetAttributeType = request.getParameter("ruleTargetAttributeTypes_" + rowNumber);
            if(targetAttributeType != null){
                rowDTO.setAttributeDataType(targetAttributeType);
                if(targetRuleRowIndex == rowNumber){
                    selectedAttributeDataType = targetAttributeType;
                }
            }

            String targetCombineFunction = request.getParameter("ruleTargetCombineFunctions_" + rowNumber);
            if(targetCombineFunction != null){
                rowDTO.setCombineFunction(targetCombineFunction);
            }

            String targetAttributeValue = request.getParameter("ruleTargetAttributeValue_" + rowNumber);
            if(targetAttributeValue != null && targetAttributeValue.trim ().length() > 0){
                rowDTO.setAttributeValue(targetAttributeValue);
            } else {
                if(targetRuleRowIndex == rowNumber){
                    ruleTargetDTO.addRowDTO(rowDTO);
                }
                continue;
            }

            ruleTargetDTO.addRowDTO(rowDTO);
        }

        // rule's target
        ruleDTO.setTargetDTO(ruleTargetDTO);

        for(rowNumber = 0; rowNumber < maxRuleRows + 1; rowNumber ++){

            RowDTO rowDTO = new RowDTO();
            String ruleCategory = request.getParameter("ruleCategory_" + rowNumber);
            if(ruleRowIndex == rowNumber){
                categoryType = ruleCategory;
                rowDTO.setNotCompleted(true);
            }
            if(ruleCategory != null && ruleCategory.trim().length() > 0){
                rowDTO.setCategory(ruleCategory);
            } else {
                continue;
            }

            String rulePreFunction = request.getParameter("rulePreFunction_" + rowNumber);
            if(rulePreFunction != null){
                rowDTO.setPreFunction(rulePreFunction);
            }

            String ruleFunction = request.getParameter("ruleFunction_" + rowNumber);
            if(ruleFunction != null){
                rowDTO.setFunction(ruleFunction);
            }

            String ruleAttributeId = request.getParameter("ruleAttributeId_" + rowNumber);
            if(ruleAttributeId != null){
                rowDTO.setAttributeId(ruleAttributeId);
                if(ruleRowIndex == rowNumber){
                    selectedAttributeId = ruleAttributeId;
                }
            }

            String ruleAttributeType = request.getParameter("ruleAttributeTypes_" + rowNumber);
            if(ruleAttributeType != null){
                rowDTO.setAttributeDataType(ruleAttributeType);
                if(ruleRowIndex == rowNumber){
                    selectedAttributeDataType = ruleAttributeType;
                }
            }

            String ruleCombineFunction = request.getParameter("ruleCombineFunctions_" + rowNumber);
            if(ruleCombineFunction != null){
                rowDTO.setCombineFunction(ruleCombineFunction);
            }

            String ruleAttributeValue = request.getParameter("ruleAttributeValue_" + rowNumber);
            if(ruleAttributeValue != null  && ruleAttributeValue.trim().length() > 0){
                rowDTO.setAttributeValue(ruleAttributeValue);
            } else {
                if(ruleRowIndex == rowNumber){
                    ruleDTO.addRowDTO(rowDTO);
                }
                continue;
            }
            ruleDTO.addRowDTO(rowDTO);
        }

        for(rowNumber = 0; rowNumber < maxObligationRuleRows + 1; rowNumber ++){

            ObligationDTO dto = new ObligationDTO();
            String obligationType = request.getParameter("obligationRuleType_" + rowNumber);
            if(obligationType != null){
                dto.setType(obligationType);
            } else {
                continue;
            }

            String obligationId = request.getParameter("obligationRuleId_" + rowNumber);
            if(obligationId != null && obligationId.trim().length() > 0){
                dto.setObligationId(obligationId);
            } else {
                continue;
            }

            String obligationAttributeValue = request.getParameter("obligationRuleAttributeValue_" + rowNumber);
            if(obligationAttributeValue != null){
                dto.setAttributeValue(obligationAttributeValue);
            }

            String obligationAttributeId = request.getParameter("obligationRuleAttributeId_" + rowNumber);
            if(obligationAttributeId != null){
                dto.setResultAttributeId(obligationAttributeId);
            }

            dto.setEffect(ruleEffect);

            if(obligationRowIndex == rowNumber){
                categoryType = null;          // TODO
                dto.setNotCompleted(true);
            }

            // Set rule's obligation
            ruleDTO.addObligationDTO(dto);
        }

        // Set rule
        entitlementPolicyBean.setRuleDTO(ruleDTO);
    }

    List<ObligationDTO> obligationDTOs = new ArrayList<ObligationDTO>();
    for(rowNumber = 0; rowNumber < maxObligationRows + 1; rowNumber ++){

        ObligationDTO dto = new ObligationDTO();
        String obligationType = request.getParameter("obligationType_" + rowNumber);
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

        if(obligationRowIndex == rowNumber){
            categoryType = null;          // TODO
            dto.setNotCompleted(true);
        }

        // Set obligations
        obligationDTOs.add(dto);
    }
    entitlementPolicyBean.setObligationDTOs(obligationDTOs);

//    rowNumber = 0;
//    while(true){
//
//        ExtendAttributeDTO dto = new ExtendAttributeDTO();
//        String dynamicId = request.
//                getParameter("dynamicId_" + rowNumber));
//        if(dynamicId != null){
//            dto.setId(dynamicId);
//        } else {
//            break;
//        }
//
//        String dynamicSelector = request.
//                getParameter("dynamicSelector_" + rowNumber));
//        if(dynamicSelector != null){
//            dto.setSelector(dynamicSelector);
//        }
//
//        String dynamicFunction = request.
//                getParameter("dynamicFunction_" + rowNumber));
//        if(dynamicFunction != null){
//            dto.setFunction(dynamicFunction);
//        }
//
//        String dynamicCategory = request.
//                getParameter("dynamicCategory_" + rowNumber));
//        if(dynamicCategory != null){
//            dto.setCategory(dynamicCategory);
//        }
//
//        String dynamicAttributeValue = request.
//                getParameter("dynamicAttributeValue_" + rowNumber));
//        if(dynamicAttributeValue != null  && dynamicAttributeValue.trim().length() > 0){
//            dto.setAttributeValue(dynamicAttributeValue);
//        }
//
//        String dynamicAttributeId = request.
//                getParameter("dynamicAttributeId_" + rowNumber));
//        if(dynamicAttributeId != null){
//            dto.setAttributeId(dynamicAttributeId);
//        }
//
//        String dynamicAttributeTypes = request.
//                getParameter("dynamicAttributeTypes_0" + rowNumber));
//        if(dynamicAttributeTypes != null){
//            dto.setDataType(dynamicAttributeTypes);
//        }
//
//        if(dynamicRowIndex == rowNumber){
//            categoryType = null;          // TODO
//            dto.setNotCompleted(true);
//        }
//
//        // Set extend attributes
//        entitlementPolicyBean.addExtendAttributeDTO(dto);
//        rowNumber ++;
//    }

    String forwardTo;

    if(ruleElementOrder != null && ruleElementOrder.trim().length() > 0){
        if(ruleDTO.isCompletedRule() && !"true".equals(updateRule)){
            entitlementPolicyBean.setRuleElementOrder(ruleElementOrder.trim() + ", " +
                                                      ruleDTO.getRuleId());
        } else{
            entitlementPolicyBean.setRuleElementOrder(ruleElementOrder.trim());
        }
    }

    if(completedRule != null && completedRule.equals("true")){
        forwardTo = nextPage + ".jsp?";
    } else {
        forwardTo = nextPage + ".jsp?ruleId=" + ruleId;
        if(categoryType != null && categoryType.trim().length() > 0){
            forwardTo = forwardTo + "&category=" + categoryType + "&returnPage=policy-editor";
        }
        if(selectedAttributeDataType != null && selectedAttributeDataType.trim().length() > 0){
            forwardTo = forwardTo + "&selectedAttributeDataType=" + selectedAttributeDataType;
        }
        if(selectedAttributeId != null && selectedAttributeId.trim().length() > 0){
            forwardTo = forwardTo + "&selectedAttributeId=" + selectedAttributeId;
        }
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