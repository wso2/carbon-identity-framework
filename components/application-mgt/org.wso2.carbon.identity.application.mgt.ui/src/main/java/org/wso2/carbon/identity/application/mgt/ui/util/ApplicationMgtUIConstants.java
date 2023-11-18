/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.mgt.ui.util;

import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.base.IdentityConstants;

/**
 * Holds the application UI related constants.
 */
public class ApplicationMgtUIConstants {

    private ApplicationMgtUIConstants() {
    }

    public static final String PURPOSE_GROUP_SHARED = "SHARED";
    public static final String PURPOSE_GROUP_TYPE_SP = "SP";
    public static final String PURPOSE_GROUP_TYPE_SYSTEM = "SYSTEM";
    public static final int DEFAULT_DISPLAY_ORDER = 0;
    public static final String TENANT_DEFAULT_SP_TEMPLATE_NAME = "default";
    public static final String DEFAULT_AUTH_SEQ = "default_sequence";
    public static final String JWKS_URI = IdentityApplicationConstants.JWKS_URI_SP_PROPERTY_NAME;
    public static final String SKIP_CONSENT = IdentityConstants.SKIP_CONSENT;
    public static final String ITEMS_PER_PAGE_PROPERTY = "ItemsPerPage";
    public static final int DEFAULT_RESULTS_PER_PAGE = 10;
    public static final String DEFAULT_FILTER = "*";
    public static final String SP_NAME_FILTER = "filterString";
    public static final String PAGE_NUMBER_PARAMETER = "pageNumber";
    public static final String DEFAULT_REGION_VALUE = "region1";
    public static final String DEFAULT_ITEM_VALUE = "service_providers_list";
    public static final String PAGINATION_VALUE = "region=%s&item=%s";
    public static final String PAGINATION_VALUE_WITH_FILTER = "region=%s&item=%s&filterString=%s";
    public static final String PASSIVE_STS = "passivests";

    /**
     * Holds the application UI param related constants.
     */
    public static class Params {

        public static final String SP_CLAIM_DIALECT = "spClaimDialects";

        private Params() {
        }
    }

}
