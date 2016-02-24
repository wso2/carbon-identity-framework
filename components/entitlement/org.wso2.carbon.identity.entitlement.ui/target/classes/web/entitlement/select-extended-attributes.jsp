<!--
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 -->
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.ExtendAttributeDTO" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Set" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<jsp:useBean id="entitlementPolicyBean" type="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean"
             class="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean" scope="session"/>
<jsp:setProperty name="entitlementPolicyBean" property="*" />

<fmt:bundle basename="org.wso2.carbon.identity.entitlement.ui.i18n.Resources">
    <carbon:breadcrumb
            label="select.registry.resource"
            resourceBundle="org.wso2.carbon.identity.entitlement.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>"/>
    
<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
<script type="text/javascript" src="extensions/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<script type="text/javascript" src="../resources/js/registry-browser.js"></script>

<%
    Set<String> categories = entitlementPolicyBean.getCategorySet();
    Map<String, Set<String>> attributeIdMap =  entitlementPolicyBean.getDefaultAttributeIdMap();
    Map<String, Set<String>> dataTypeMap =  entitlementPolicyBean.getDefaultDataTypeMap();
    String selectedCategory  = request.getParameter("category");
    String selectFunction  = request.getParameter("function");
    String selectedAttributeId = request.getParameter("category");
    String selectedDataType = request.getParameter("category");
    String selectedParams =  request.getParameter("category");

    if(selectedCategory != null && selectedCategory.trim().length() > 0){
        ExtendAttributeDTO attributeDTO = new ExtendAttributeDTO();
        attributeDTO.setCategory(selectedCategory);
        attributeDTO.setAttributeId(selectedAttributeId);
        attributeDTO.setDataType(selectedDataType);
        entitlementPolicyBean.addExtendAttributeDTO(attributeDTO);
    } else if(selectFunction != null && selectFunction.trim().length() > 0){
        ExtendAttributeDTO attributeDTO = new ExtendAttributeDTO();
        attributeDTO.setCategory(selectedCategory);
        attributeDTO.setFunction(selectedCategory);
        attributeDTO.setAttributeId(selectedAttributeId);
        attributeDTO.setDataType(selectedDataType);
        attributeDTO.setAttributeValue(selectedDataType);
        entitlementPolicyBean.addExtendAttributeDTO(attributeDTO);
    }


%>

    <script type="text/javascript">
        var selectorAttributeIdList = new Array();
        var selectorDataTypeList = new Array();
    </script>

    <%
        for(String selector : categories){
            String tmp = "";
            Set<String> attributeIds = attributeIdMap.get(selector);
            if(attributeIds != null){
                for(String attributeId : attributeIds){
                    tmp += "<option value=\"" + attributeId + "\" >" + attributeId + "</option>";
                }
            }
    %>

    <script type="text/javascript">
        selectorAttributeIdList.push({key:'<%=Encode.forJavaScriptBlock(selector)%>',list:'<%=Encode.forJavaScriptBlock(tmp)%>'});
    </script>

    <%
        }
    %>

    <%
        for(String selector : categories){
            String tmp = "";
            Set<String> dataTypes = dataTypeMap.get(selector);
            if(dataTypes != null){
                for(String dataType : dataTypes){
                    tmp += "<option value=\"" + dataType + "\" >" + dataType + "</option>";
                }
            }
    %>

    <script type="text/javascript">
        selectorDataTypeList.push({key:'<%=Encode.forJavaScriptBlock(selector)%>',list:'<%=Encode.forJavaScriptBlock(tmp)%>'});
    </script>

    <%
        }
    %>


<script type="text/javascript">

    function selectRightList(selector, type){
        selectRightIdList(selector, type);
        selectRightDataTypeList(selector, type);
    }

    function selectRightIdList(selector, type){
        var rightList = "";
        var selectorElement = "";
        for(var i = 0; i < selectorAttributeIdList.length; i++){
            if(selectorAttributeIdList[i].key == selector){
                rightList = selectorAttributeIdList[i].list;
            }
        }

        if(type == 'category'){
            selectorElement = document.getElementById('cAttributeIds');
            selectorElement.innerHTML = rightList;
        } else if (type == 'function'){
            selectorElement = document.getElementById('fAttributeIds');
            selectorElement.innerHTML = rightList;
        }
    }

    function selectRightDataTypeList(selector, type){
        var rightList = "";
        var selectorElement  = "";
        for(var i = 0; i < selectorDataTypeList.length; i++){
            if(selectorDataTypeList[i].key == selector){
                rightList = selectorDataTypeList[i].list;
            }
        }

        if(type == 'category'){
            selectorElement = document.getElementById('cDataType');
            selectorElement.innerHTML = rightList;
        } else if (type == 'function'){
            selectorElement = document.getElementById('fDataType');
            selectorElement.innerHTML = rightList;
        }
    }

    function addNewRow(){
        document.categoryForm.submit();
    }

    function removeRow(link){
        link.parentNode.parentNode.parentNode.removeChild(link.parentNode.parentNode);

    }
</script>


    <div id="middle">
        <h2><fmt:message key="select.resources.registry"/></h2>
            <div id="workArea">
            <table class="styledLeft noBorders">
            <tr>
            <td class="nopadding">
                <form id="categoryForm" name="categoryForm" action="select-extended-attributes.jsp">
                <table cellspacing="0" id="mainTable" style="width:100%;border:none !important">
                    <tr>
                        <td>
                            Select Attributes from request....
                        </td>
                    </tr>
                    <tr>
                        <td style="padding-left:0px !important;padding-right:0px !important">
                            <select id="category" name="category"
                                    onchange="selectRightList(this.options[this.selectedIndex].value, 'category')">
                                <%
                                    for (String category : categories) {
                                        if (selectedCategory != null && category.equals(selectedCategory)) {
                                %>
                                <option value="<%=Encode.forHtmlAttribute(category)%>" selected="selected"><%=Encode.forHtmlContent(category)%></option>
                                <%
                                        } else {
                                %>
                                <option value="<%=Encode.forHtmlAttribute(category)%>"><%=Encode.forHtmlContent(category)%></option>
                                <%
                                        }
                                    }
                                %>
                            </select>
                        </td>
                        <td>
                            <select id="cAttributeIds" name="cAttributeIds"  class="leftCol-small">
                                <%
                                    if (selectedAttributeId != null && selectedAttributeId.trim().length() > 0) {
                                %>
                                    <option value="<%=Encode.forHtmlAttribute(selectedAttributeId)%>" selected="selected"><%=Encode.forHtmlContent(selectedAttributeId)%></option>
                                <%
                                    }
                                %>
                            </select>
                        </td>
                        <td>
                            <select id="cDataType" name="cDataType"  class="leftCol-small">
                                <%
                                    if (selectedDataType != null && selectedDataType.trim().length() > 0) {
                                %>
                                    <option value="<%=Encode.forHtmlAttribute(selectedDataType)%>" selected="selected"><%=Encode.forHtmlContent(selectedDataType)%></option>
                                <%
                                    }
                                %>
                            </select>
                        </td>
                        <td>
                            <a onclick="addNewRow()" style='background-image:url(images/add.gif);'
                               type="button" class="icon-link"></a>                             
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <table>
                                <thead>
                                <th>Category</th>
                                <th>Attribute Id</th>
                                <th>Attribute Data Type</th>
                                </thead>
                                <tbody>
                                <%
                                    List<ExtendAttributeDTO> list = entitlementPolicyBean.getExtendAttributeDTOs();
                                    if(list != null){
                                        for(ExtendAttributeDTO attributeDTO : list){
                                %>
                                        <tr>
                                        <td><%=Encode.forHtmlContent(attributeDTO.getCategory())%></td>
                                        <td><%=Encode.forHtmlContent(attributeDTO.getAttributeId())%></td>
                                        <td><%=Encode.forHtmlContent(attributeDTO.getDataType())%></td>
                                        </tr>
                                <%
                                        }
                                    }
                                %>
                                </tbody>
                            </table>
                        </td>
                    </tr>
                </table>
                <table cellspacing="0"  style="width:100%;border:none !important">
                    <tr>
                        <td>
                            Select Attributes from Function evaluation.....
                        </td>
                    </tr>
                    <tr>
                        <td style="padding-left:0px !important;padding-right:0px !important">
                            <select id="function" name="function"
                                    onchange="selectRightList(this.options[this.selectedIndex].value, 'function')">
                                <%
                                    for (String category : categories) {
                                        if (selectFunction != null && category.equals(selectFunction)) {
                                %>
                                <option value="<%=Encode.forHtmlAttribute(category)%>" selected="selected"><%=Encode.forHtmlContent(category)%></option>
                                <%
                                        } else {
                                %>
                                <option value="<%=Encode.forHtmlAttribute(category)%>"><%=Encode.forHtmlContent(category)%></option>
                                <%
                                        }
                                    }
                                %>
                            </select>
                        </td>
                            <%
                                if (selectedParams != null && selectedParams.trim().length() > 0) {
                            %>
                            <td><input type="text" name="params" id="params" value="<%=Encode.forHtmlAttribute(selectedParams)%>"
                                                                    class="text-box-big"/></td>
                            <%
                            } else {
                            %>
                            <td><input type="text" name="params" id="params" class="text-box-big"/></td>
                            <%
                                }
                            %>
                        <td>
                            <select id="fAttributeIds" name="fAttributeIds"  class="leftCol-small">
                                <%
                                    if (selectedAttributeId != null && selectedAttributeId.trim().length() > 0) {
                                %>
                                    <option value="<%=Encode.forHtmlAttribute(selectedAttributeId)%>" selected="selected"><%=Encode.forHtmlContent(selectedAttributeId)%></option>
                                <%
                                    }
                                %>
                            </select>
                        </td>
                        <td>
                            <select id="fDataType" name="fDataType"  class="leftCol-small">
                                <%
                                    if (selectedDataType != null && selectedDataType.trim().length() > 0) {
                                %>
                                    <option value="<%=Encode.forHtmlAttribute(selectedDataType)%>" selected="selected"><%=Encode.forHtmlContent(selectedDataType)%></option>
                                <%
                                    }
                                %>
                            </select>
                        </td>
                        <td>
                            <a onclick="addNewRow()" style='background-image:url(images/add.gif);'
                               type="button" class="icon-link"></a>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <table>
                                <thead>
                                <th class="leftCol-small">Selector Type</th>
                                <th>Category</th>
                                <th>Attribute Id</th>
                                <th>Attribute Data Type</th>
                                </thead>
                                <tbody>
                                <tr><td>
                                </td></tr>
                                </tbody>
                            </table>
                        </td>
                    </tr>
                </table>
                </form>
            </td>
            </tr>

            <tr>
                <td class="buttonRow">
                <input type="button" value="<fmt:message key="finish"/>" onclick="submitForm();" class="button"/>
                <input type="button" value="<fmt:message key="cancel" />" onclick="cancelForm();"  class="button"/>
                </td>
            </tr>
            </table>
        </div>
    </div>
</fmt:bundle>
