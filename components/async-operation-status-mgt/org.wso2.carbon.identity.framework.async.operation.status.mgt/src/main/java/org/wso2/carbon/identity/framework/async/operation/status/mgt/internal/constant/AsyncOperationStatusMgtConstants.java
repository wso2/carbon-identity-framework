/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.FilterPlaceholders.CORRELATION_ID_FILTER;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.FilterPlaceholders.CREATED_TIME_FILTER;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.FilterPlaceholders.INITIATED_ORG_ID_FILTER;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.FilterPlaceholders.INITIATED_RESOURCE_ID_FILTER;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.FilterPlaceholders.INITIATED_USER_ID_FILTER;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.FilterPlaceholders.MODIFIED_TIME_FILTER;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.FilterPlaceholders.OPERATION_ID_FILTER;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.FilterPlaceholders.OPERATION_TYPE_FILTER;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.FilterPlaceholders.POLICY_FILTER;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.FilterPlaceholders.STATUS_FILTER;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.FilterPlaceholders.STATUS_MESSAGE_FILTER;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.FilterPlaceholders.SUBJECT_ID_FILTER;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.FilterPlaceholders.SUBJECT_TYPE_FILTER;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.FilterPlaceholders.TARGET_ORG_ID_FILTER;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.FilterPlaceholders.UNIT_OPERATION_ID_FILTER;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.SQLPlaceholders.CORRELATION_ID;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.SQLPlaceholders.CREATED_AT;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.SQLPlaceholders.INITIATED_ORG_ID;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.SQLPlaceholders.INITIATED_USER_ID;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.SQLPlaceholders.LAST_MODIFIED;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.SQLPlaceholders.OPERATION_ID;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.SQLPlaceholders.OPERATION_TYPE;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.SQLPlaceholders.POLICY;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.SQLPlaceholders.RESIDENT_RESOURCE_ID;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.SQLPlaceholders.STATUS;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.SQLPlaceholders.STATUS_MESSAGE;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.SQLPlaceholders.SUBJECT_ID;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.SQLPlaceholders.SUBJECT_TYPE;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.SQLPlaceholders.TARGET_ORG_ID;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.SQLPlaceholders.UNIT_OPERATION_ID;

/**
 * Asynchronous operation status management constants
 */
public class AsyncOperationStatusMgtConstants {

    public static final String DESC_SORT_ORDER = "DESC";
    public static final String ASC_SORT_ORDER = "ASC";
    public static final String FILTER_PLACEHOLDER_PREFIX = "FILTER_ID_";

    private static final Map<String, String> attributeColumnMap = new HashMap<>();
    public static final Map<String, String> ATTRIBURE_COLUMN_MAP = Collections.unmodifiableMap(attributeColumnMap);

    public static final String EQ = "eq";
    public static final String CO = "co";
    public static final String SW = "sw";
    public static final String EW = "ew";
    public static final String GE = "ge";
    public static final String LE = "le";
    public static final String GT = "gt";
    public static final String LT = "lt";
    public static final String AND = "and";
    public static final String OR = "or";
    public static final String NOT = "not";

    public static final String PAGINATION_AFTER = "after";
    public static final String PAGINATION_BEFORE = "before";

    static {
        attributeColumnMap.put(UNIT_OPERATION_ID_FILTER, UNIT_OPERATION_ID);
        attributeColumnMap.put(OPERATION_ID_FILTER, OPERATION_ID);
        attributeColumnMap.put(INITIATED_RESOURCE_ID_FILTER, RESIDENT_RESOURCE_ID);
        attributeColumnMap.put(TARGET_ORG_ID_FILTER, TARGET_ORG_ID);
        attributeColumnMap.put(STATUS_FILTER, STATUS);
        attributeColumnMap.put(STATUS_MESSAGE_FILTER, STATUS_MESSAGE);
        attributeColumnMap.put(CREATED_TIME_FILTER, CREATED_AT);

        attributeColumnMap.put(CORRELATION_ID_FILTER, CORRELATION_ID);
        attributeColumnMap.put(OPERATION_TYPE_FILTER, OPERATION_TYPE);
        attributeColumnMap.put(SUBJECT_TYPE_FILTER, SUBJECT_TYPE);
        attributeColumnMap.put(SUBJECT_ID_FILTER, SUBJECT_ID);
        attributeColumnMap.put(INITIATED_ORG_ID_FILTER, INITIATED_ORG_ID);
        attributeColumnMap.put(INITIATED_USER_ID_FILTER, INITIATED_USER_ID);
        attributeColumnMap.put(MODIFIED_TIME_FILTER, LAST_MODIFIED);
        attributeColumnMap.put(POLICY_FILTER, POLICY);

        attributeColumnMap.put(PAGINATION_AFTER, CREATED_AT);
        attributeColumnMap.put(PAGINATION_BEFORE, CREATED_AT);
    }
}
