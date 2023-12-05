/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.workflow.mgt.util;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.workflow.mgt.bean.RequestParameter;
import org.wso2.carbon.identity.workflow.mgt.dto.WorkflowRequest;
import org.wso2.carbon.identity.workflow.mgt.exception.WorkflowRuntimeException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WorkflowRequestBuilder {

    private static final String WF_NS = "http://schema.bpel.mgt.workflow.carbon.wso2.org/";
    private static final String WF_NS_PREFIX = "p";
    private static final String WF_REQ_ROOT_ELEM = "ProcessRequest";
    private static final String WF_REQ_UUID_ELEM = "uuid";
    private static final String WF_REQ_ACTION_ELEM = "eventType";
    private static final String WF_REQ_TASK_INITIATOR_ELEM = "taskInitiator";

    private static final String WF_REQ_CONFIG_ELEM = "configuration";
    private static final String WF_REQ_APPROVAL_STEP_ELEM = "approvalStep";
    private static final String WF_REQ_STEP_NAME_ELEM = "stepName";
    private static final String WF_REQ_HUMAN_TASK_ELEM = "humanTask";
    private static final String WF_REQ_HUMAN_TASK_SUBJECT_ELEM = "humanTaskSubject";
    private static final String WF_REQ_HUMAN_TASK_DESC_ELEM = "humanTaskDescription";
    private static final String WF_REQ_APPROVE_USER_ELEM = "user";
    private static final String WF_REQ_APPROVE_ROLE_ELEM = "role";


    //    private static final String WF_REQ_TENANT_DOMAIN_ELEM = "tenantDomain";
    private static final String WF_REQ_PARAMS_ELEM = "parameters";
    private static final String WF_REQ_PARAM_ELEM = "parameter";
    private static final String WF_REQ_PARAM_NAME_ATTRIB = "name";
    private static final String WF_REQ_LIST_ITEM_ELEM = "itemValue";
    private static final String WF_REQ_KEY_ATTRIB = "itemName";
    private static final String WF_REQ_VALUE_ELEM = "value";

    private static final Set<Class> SUPPORTED_CLASS_TYPES;

    static {
        //only following types of objects will be allowed as values to the parameters.
        SUPPORTED_CLASS_TYPES = new HashSet<>();
        SUPPORTED_CLASS_TYPES.add(String.class);
        SUPPORTED_CLASS_TYPES.add(Integer.class);
        SUPPORTED_CLASS_TYPES.add(Double.class);
        SUPPORTED_CLASS_TYPES.add(Float.class);
        SUPPORTED_CLASS_TYPES.add(Long.class);
        SUPPORTED_CLASS_TYPES.add(Character.class);
        SUPPORTED_CLASS_TYPES.add(Byte.class);
        SUPPORTED_CLASS_TYPES.add(Short.class);
        SUPPORTED_CLASS_TYPES.add(Boolean.class);
    }

    private String uuid;
    private String event;
    private Map<String, Object> singleValuedParams;
    private Map<String, List<Object>> listTypeParams;
    private Map<String, Map<String, Object>> mapTypeParams;
    private Map<String, Object> initParams;

    /**
     * Initialize the Request builder with uuid and event
     *
     * @param uuid   Uniquely identifies the workflow
     * @param action The identifier for the event for which the workflow was triggered
     */
    public WorkflowRequestBuilder(String uuid, String action) {

        this.uuid = uuid;
        this.event = action;
        singleValuedParams = new HashMap<>();
        listTypeParams = new HashMap<>();
        mapTypeParams = new HashMap<>();
        this.initParams = new HashMap<String, Object>();
    }

    /**
     * Create OM Element from workflow request parameters.
     *
     * @param workFlowRequest Workflow parameters
     * @return
     * @throws WorkflowRuntimeException
     */
    public static OMElement buildXMLRequest(WorkflowRequest workFlowRequest) throws WorkflowRuntimeException {

        WorkflowRequestBuilder requestBuilder = new WorkflowRequestBuilder(workFlowRequest.getUuid(),
                                                                           workFlowRequest.getEventType());

        for (RequestParameter parameter : workFlowRequest.getRequestParameters()) {
            if (parameter.isRequiredInWorkflow()) {
                switch (parameter.getValueType()) {
                    case WorkflowDataType.BOOLEAN_TYPE:
                    case WorkflowDataType.STRING_TYPE:
                    case WorkflowDataType.INTEGER_TYPE:
                    case WorkflowDataType.DOUBLE_TYPE:
                        requestBuilder.addSingleValuedParam(parameter.getName(), parameter.getValue());
                        break;
                    case WorkflowDataType.STRING_LIST_TYPE:
                    case WorkflowDataType.DOUBLE_LIST_TYPE:
                    case WorkflowDataType.INTEGER_LIST_TYPE:
                    case WorkflowDataType.BOOLEAN_LIST_TYPE:
                        requestBuilder.addListTypeParam(parameter.getName(), (List<Object>) parameter.getValue());
                        break;
                    case WorkflowDataType.STRING_STRING_MAP_TYPE:
                        requestBuilder.addMapTypeParam(parameter.getName(), (Map<String, Object>) parameter.getValue());
                        break;
                    //ignoring the other types
                }
            }
        }
        return requestBuilder.buildRequest();
    }

    /**
     * Create OM Element from workflow request parameters.
     *
     * @param workFlowRequest Workflow parameters
     * @param initParams      Non workflow parameters
     * @return
     * @throws WorkflowRuntimeException
     */
    public static OMElement buildXMLRequest(WorkflowRequest workFlowRequest, Map<String, Object> initParams) throws
                                                                                                             WorkflowRuntimeException {

        WorkflowRequestBuilder requestBuilder = new WorkflowRequestBuilder(workFlowRequest.getUuid(),
                                                                           workFlowRequest.getEventType());

        for (RequestParameter parameter : workFlowRequest.getRequestParameters()) {
            if (parameter.isRequiredInWorkflow()) {
                switch (parameter.getValueType()) {
                    case WorkflowDataType.BOOLEAN_TYPE:
                    case WorkflowDataType.STRING_TYPE:
                    case WorkflowDataType.INTEGER_TYPE:
                    case WorkflowDataType.DOUBLE_TYPE:
                        requestBuilder.addSingleValuedParam(parameter.getName(), parameter.getValue());
                        break;
                    case WorkflowDataType.STRING_LIST_TYPE:
                    case WorkflowDataType.DOUBLE_LIST_TYPE:
                    case WorkflowDataType.INTEGER_LIST_TYPE:
                    case WorkflowDataType.BOOLEAN_LIST_TYPE:
                        requestBuilder.addListTypeParam(parameter.getName(), (List<Object>) parameter.getValue());
                        break;
                    case WorkflowDataType.STRING_STRING_MAP_TYPE:
                        requestBuilder.addMapTypeParam(parameter.getName(), (Map<String, Object>) parameter.getValue());
                        break;
                    //ignoring the other types
                }
            }
        }
        requestBuilder.setInitParams(initParams);
        return requestBuilder.buildRequest();
    }

    public void setInitParams(Map<String, Object> initParams) {
        this.initParams = initParams;
    }

    /**
     * Adds a parameter with a single value. eg. tenantDomain="carbon.super"
     *
     * @param key   The param name
     * @param value The param value, must be either a wrapper for a primitive or String
     * @return This builder instance
     */
    public WorkflowRequestBuilder addSingleValuedParam(String key, Object value) throws WorkflowRuntimeException {

        if (StringUtils.isNotBlank(key)) {
            if (isValidValue(value)) {
                singleValuedParams.put(key, value);
                return this;
            } else {
                throw new WorkflowRuntimeException("Value provided for " + key + " is not acceptable. Use either " +
                                                   "string, boolean, or numeric value");
            }
        } else {
            throw new WorkflowRuntimeException("Key cannot be null or empty");
        }
    }

    /**
     * Check whether the given object is of valid type
     *
     * @param obj The object to be tested
     * @return
     */
    protected boolean isValidValue(Object obj) {
        //null value of one of the supported class
        return obj == null || SUPPORTED_CLASS_TYPES.contains(obj.getClass());
    }

    /**
     * Adds a parameter with a list value. eg. roleList={"admin","manager"}
     *
     * @param key   The param name
     * @param value The param value list, each item must be either a wrapper for a primitive or String
     * @return This builder instance
     */
    public WorkflowRequestBuilder addListTypeParam(String key, List<Object> value) throws WorkflowRuntimeException {

        if (StringUtils.isNotBlank(key)) {
            if (value != null) {
                for (Object o : value) {
                    if (!isValidValue(o)) {
                        throw new WorkflowRuntimeException(
                                "At least one value provided for " + key + " is not acceptable" +
                                ". Use either string, boolean, or numeric value");
                    }
                }
                listTypeParams.put(key, value);
                return this;
            } else {
                throw new WorkflowRuntimeException("Value for " + key + " cannot be null");
            }
        } else {
            throw new WorkflowRuntimeException("Key cannot be null or empty");
        }
    }

    /**
     * Adds a parameter with a map value. eg. claimList={"First Name" = "Joan","Last Name" = "Doe"}
     *
     * @param key   The param name
     * @param value The param value map, each key of the map should be String, and each value must be either a wrapper
     *              to a primitive or String
     * @return This builder instance
     */
    public WorkflowRequestBuilder addMapTypeParam(String key, Map<String, Object> value)
            throws WorkflowRuntimeException {

        if (StringUtils.isNotBlank(key)) {
            if (value != null) {
                for (Map.Entry<String, Object> entry : value.entrySet()) {
                    if (StringUtils.isBlank(entry.getKey())) {
                        throw new IllegalArgumentException("Map item's key value cannot be null");
                    }
                    if (!isValidValue(entry.getValue())) {
                        throw new WorkflowRuntimeException(
                                "Value provided for " + entry.getKey() + " is not acceptable" +
                                ". Use either string, boolean, or numeric value");
                    }
                }
                mapTypeParams.put(key, value);
                return this;
            } else {
                throw new WorkflowRuntimeException("Value for " + key + " cannot be null");
            }
        } else {
            throw new WorkflowRuntimeException("Key cannot be null or empty");
        }
    }

    /**
     * Builds the SOAP request body for the Service endpoint
     *
     * @return
     */
    public OMElement buildRequest() {

        OMFactory omFactory = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = omFactory.createOMNamespace(WF_NS, WF_NS_PREFIX);
        OMElement rootElement = omFactory.createOMElement(WF_REQ_ROOT_ELEM, omNs);
        OMElement uuidElement = omFactory.createOMElement(WF_REQ_UUID_ELEM, omNs);
        OMElement reqIdElement = omFactory.createOMElement(WF_REQ_ACTION_ELEM, omNs);
        OMElement taskInitiatorElement = omFactory.createOMElement(WF_REQ_TASK_INITIATOR_ELEM, omNs);
        OMElement configElement = omFactory.createOMElement(WF_REQ_CONFIG_ELEM, omNs);
        uuidElement.setText(uuid);
        rootElement.addChild(uuidElement);
        reqIdElement.setText(event);
        rootElement.addChild(reqIdElement);
        taskInitiatorElement.setText(CarbonContext.getThreadLocalCarbonContext().getUsername());
        rootElement.addChild(taskInitiatorElement);
        OMElement paramsElement = omFactory.createOMElement(WF_REQ_PARAMS_ELEM, omNs);

        for (Map.Entry<String, Object> entry : singleValuedParams.entrySet()) {
            OMElement paramElement = omFactory.createOMElement(WF_REQ_PARAM_ELEM, omNs);
            OMAttribute paramNameAttribute =
                    omFactory.createOMAttribute(WF_REQ_PARAM_NAME_ATTRIB, null, entry.getKey());
            paramElement.addAttribute(paramNameAttribute);
            OMElement valueElement = omFactory.createOMElement(WF_REQ_VALUE_ELEM, omNs);
            OMElement valueItemElement = omFactory.createOMElement(WF_REQ_LIST_ITEM_ELEM, omNs);
            valueItemElement.setText(entry.getValue().toString());
            valueElement.addChild(valueItemElement);
            paramElement.addChild(valueElement);
            paramsElement.addChild(paramElement);
        }
        for (Map.Entry<String, List<Object>> entry : listTypeParams.entrySet()) {
            OMElement paramElement = omFactory.createOMElement(WF_REQ_PARAM_ELEM, omNs);
            OMAttribute paramNameAttribute =
                    omFactory.createOMAttribute(WF_REQ_PARAM_NAME_ATTRIB, null, entry.getKey());
            paramElement.addAttribute(paramNameAttribute);
            OMElement valueElement = omFactory.createOMElement(WF_REQ_VALUE_ELEM, omNs);
            for (Object listItem : entry.getValue()) {
                if (listItem != null) {
                    OMElement listItemElement = omFactory.createOMElement(WF_REQ_LIST_ITEM_ELEM, omNs);
                    listItemElement.setText(listItem.toString());
                    valueElement.addChild(listItemElement);
                }
            }
            paramElement.addChild(valueElement);
            paramsElement.addChild(paramElement);
        }

        for (Map.Entry<String, Map<String, Object>> entry : mapTypeParams.entrySet()) {
            OMElement paramElement = omFactory.createOMElement(WF_REQ_PARAM_ELEM, omNs);
            OMAttribute paramNameAttribute =
                    omFactory.createOMAttribute(WF_REQ_PARAM_NAME_ATTRIB, null, entry.getKey());
            paramElement.addAttribute(paramNameAttribute);
            OMElement valueElement = omFactory.createOMElement(WF_REQ_VALUE_ELEM, omNs);
            for (Map.Entry<String, Object> mapItem : entry.getValue().entrySet()) {
                if (mapItem.getKey() != null) {
                    String valueText;
                    if (mapItem.getValue() != null) {
                        valueText = mapItem.getValue().toString();
                    } else {
                        valueText = "Null";
                    }
                    OMElement listItemElement = omFactory.createOMElement(WF_REQ_LIST_ITEM_ELEM, omNs);
                    OMAttribute itemNameAttribute = omFactory.createOMAttribute(WF_REQ_KEY_ATTRIB, null,
                                                                                mapItem.getKey());
                    listItemElement.addAttribute(itemNameAttribute);
                    listItemElement.setText(valueText);
                    valueElement.addChild(listItemElement);
                }
            }
            paramElement.addChild(valueElement);
            paramsElement.addChild(paramElement);
        }
        rootElement.addChild(paramsElement);

        //TODO:This request build is lcated in framework and it doesn't aware about BPEL, so we can't use this xml
        // to check the condition. For now I have commented following two lines to decouple BPELstuff from this xml
        // builder. In future we need to framework own format for this.

        String ht = "";// (String)this.initParams.get(WFConstant.TemplateConstants.HT_SUBJECT);
        String htDesc = "";// (String)this.initParams.get(WFConstant.TemplateConstants.HT_DESCRIPTION);

        final Map<String, Map<String, List<String>>> approvalStepMap = getApprovalStepMap();

        for (int a = 1; a <= approvalStepMap.size(); a++) {

            String key = "Step " + a;
            OMElement stepName = omFactory.createOMElement(WF_REQ_STEP_NAME_ELEM, omNs);
            stepName.setText(key);

            OMElement approvalStepElement = omFactory.createOMElement(WF_REQ_APPROVAL_STEP_ELEM, omNs);
            approvalStepElement.addChild(stepName);

            OMElement humanTaskElement = omFactory.createOMElement(WF_REQ_HUMAN_TASK_ELEM, omNs);
            OMElement humanTaskSubjectElement = omFactory.createOMElement(WF_REQ_HUMAN_TASK_SUBJECT_ELEM, omNs);
            humanTaskSubjectElement.setText(ht);
            OMElement humanTaskDescElement = omFactory.createOMElement(WF_REQ_HUMAN_TASK_DESC_ELEM, omNs);
            humanTaskDescElement.setText(htDesc);
            humanTaskElement.addChild(humanTaskSubjectElement);
            humanTaskElement.addChild(humanTaskDescElement);

            approvalStepElement.addChild(humanTaskElement);


            Map<String, List<String>> value = approvalStepMap.get(a + "");
            if (value.get("users") != null) {
                List<String> userList = value.get("users");
                for (String user : userList) {
                    OMElement userElement = omFactory.createOMElement(WF_REQ_APPROVE_USER_ELEM, omNs);
                    userElement.setText(user);
                    approvalStepElement.addChild(userElement);
                }
            }

            if (value.get("roles") != null) {
                List<String> userList = value.get("roles");
                for (String user : userList) {
                    OMElement userElement = omFactory.createOMElement(WF_REQ_APPROVE_ROLE_ELEM, omNs);
                    userElement.setText(user);
                    approvalStepElement.addChild(userElement);
                }
            }
            configElement.addChild(approvalStepElement);
        }
        rootElement.addChild(configElement);
        return rootElement;
    }

    private Map<String, Map<String, List<String>>> getApprovalStepMap() {
        Map<String, Map<String, List<String>>> map = new HashMap<String, Map<String, List<String>>>();
        for (Map.Entry<String, Object> entry : this.initParams.entrySet()) {
            if (entry.getKey().startsWith("step-")) {
                String[] key = entry.getKey().split("-");
                String step = key[1];
                Map<String, List<String>> valueMap = map.get(step);
                if (valueMap == null) {
                    valueMap = new HashMap<String, List<String>>();
                    map.put(step, valueMap);
                }

                String value = (String) entry.getValue();
                String[] values = null;
                if (StringUtils.isNotBlank(value)) {
                    values = value.split(",");
                }
                if (values != null) {
                    List<String> userList = Arrays.asList(values);
                    String stepName = "step-" + step + "-users";
                    if (stepName.equals(entry.getKey())) {
                        valueMap.put("users", userList);
                    }
                    stepName = "step-" + step + "-roles";
                    if (stepName.equals(entry.getKey())) {
                        valueMap.put("roles", userList);

                    }
                }
            }

        }
        return map;

    }
}
