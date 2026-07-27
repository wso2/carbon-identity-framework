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

package org.wso2.carbon.identity.framework.async.operation.status.mgt.constants;

/**
 * Constants related to the test cases of Async Operation Status Management component.
 */
public class TestAsyncOperationConstants {

    // Organization IDs.
    public static final String RESIDENT_ORG_ID_1 = "10084a8d-113f-4211-a0d5-efe36b082211";
    public static final String RESIDENT_ORG_ID_2 = "c524c30a-cbd4-4169-ac9d-1ee3edf1bf16";
    public static final String RESIDENT_ORG_ID_3 = "cd5a1dcb-fff2-4c14-a073-c07b3caf1757";
    public static final String RESIDENT_ORG_ID_4 = "7cb4ab7e-9a25-44bd-a9e0-cf4e07d804dc";

    // Organization Names.
    public static final String RESIDENT_ORG_NAME_1 = "Organization 1";
    public static final String RESIDENT_ORG_NAME_2 = "Organization 2";
    public static final String RESIDENT_ORG_NAME_3 = "Organization 3";
    public static final String RESIDENT_ORG_NAME_4 = "Organization 4";

    // Tenant Domains.
    public static final String TENANT_DOMAIN_1 = "d6fc0b22-5b7f-461b-aef8-775a78187095";

    // Correlation ID.
    public static final String CORR_ID_1 = "440abcd-6a41-4da7-aadd-272995d0e5db";
    public static final String CORR_ID_1_PREFIX = "440abcd-6a41-4da7-aadd-272995d0e5d";
    public static final String CORR_ID_2 = "550abcd-6a41-4da7-aadd-272995d0e5db";
    public static final String CORR_ID_3 = "660abcd-6a41-4da7-aadd-272995d0e5db";
    public static final String CORR_ID_4 = "770abcd-6a41-4da7-aadd-272995d0e5db";

    // Operation Subjects.
    public static final String SUBJECT_ID_1 = "448e57e7-ff6b-4c31-a1eb-2a0e2d635b2a";
    public static final String SUBJECT_ID_1_PREFIX = "448e57e7-ff6b-4c31-a1eb-2a0e2d635b2";
    public static final String SUBJECT_ID_2 = "558e57e7-ff6b-4c31-a1eb-2a0e2d635b2a";
    public static final String SUBJECT_ID_3 = "668e57e7-ff6b-4c31-a1eb-2a0e2d635b2b";
    public static final String SUBJECT_ID_4 = "778e57e7-ff6b-4c31-a1eb-2a0e2d635b2c";

    // Operation Initiators.
    public static final String INITIATOR_ID_1 = "443k57a1-gl6b-4c31-a1eb-2a0e2d635b2a";

    // Operation Types.
    public static final String TYPE_USER_SHARE = "B2B_USER_SHARE";
    public static final String TYPE_USER_BULK_IMPORT = "USER_BULK_IMPORT";
    public static final String TYPE_APP_SHARE = "B2B_APPLICATION_SHARE";

    // Operation Subject Types.
    public static final String SUBJECT_TYPE_USER = "USER";
    public static final String SUBJECT_TYPE_APPLICATION = "APPLICATION";

    // Operation Policies.
    public static final String POLICY_SHARE_WITH_ALL = "SHARE_WITH_ALL";
    public static final String POLICY_SELECTIVE_SHARE = "SELECTIVE_SHARE";
    public static final String POLICY_DO_NOT_SHARE = "DO_NOT_SHARE";
}
