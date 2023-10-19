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

import java.util.HashSet;
import java.util.Set;

public class WFConstant {

    public static final String REQUEST_ID = "REQUEST ID";

    public static final String HT_STATE_SKIPPED = "SKIPPED";
    public static final String HT_STATE_PENDING = "PENDING";

    public static final String KEYSTORE_SYSTEM_PROPERTY_ID = "javax.net.ssl.keyStore";
    public static final String KEYSTORE_PASSWORD_SYSTEM_PROPERTY_ID = "javax.net.ssl.keyStorePassword";
    public static final String KEYSTORE_CARBON_CONFIG_PATH = "Security.KeyStore.Location";
    public static final String KEYSTORE_PASSWORD_CARBON_CONFIG_PATH = "Security.KeyStore.Password";

    public static final String WORKFLOW_ENTITY_TYPE = "USER";
    public static final String WORKFLOW_REQUEST_TYPE = "ADD_USER";

    public static final int DEFAULT_RESULTS_PER_PAGE = 15;

    public static final String DEFAULT_FILTER = "*";

    public static final Set<Class> NUMERIC_CLASSES;

    static {
        NUMERIC_CLASSES = new HashSet<>();
        NUMERIC_CLASSES.add(Integer.class);
        NUMERIC_CLASSES.add(Long.class);
        NUMERIC_CLASSES.add(Short.class);
        NUMERIC_CLASSES.add(Character.class);
        NUMERIC_CLASSES.add(Byte.class);
        NUMERIC_CLASSES.add(Float.class);
        NUMERIC_CLASSES.add(Double.class);
    }

    public static class TemplateConstants {
        public static final String SERVICE_SUFFIX = "Service";

    }

    public static class ParameterName {
        //Template specific parameters
        public static final String WORKFLOW_NAME = "WorkflowName";
        public static final String ITEMS_PER_PAGE_PROPERTY = "ItemsPerPage";

    }
    public static class ParameterHolder {
        public static final String TEMPLATE = "Template" ;
        public static final String  WORKFLOW_IMPL = "WorkflowImpl" ;
    }

    public static class Exceptions{
        // Association errors
        public static final String ERROR_WHILE_LOADING_ASSOCIATIONS =  "Error while loading associations from DB: " +
                "Database driver could not be identified or not supported.";
        public static final String ERROR_LISTING_ASSOCIATIONS =  "Server error when listing associations";
        public static final String SQL_ERROR_LISTING_ASSOCIATIONS =  "SQL error when getting associations from DB";
        public static final String ERROR_GETTING_ASSOC_COUNT =  "Server error while getting associations count for the tenantId: ";
        public static final String SQL_ERROR_GETTING_ASSOC_COUNT =  "SQL error when getting associations count from DB";

        // Workflow errors
        public static final String ERROR_WHILE_LOADING_WORKFLOWS =  "Error while loading workflows from DB: " +
                "Database driver could not be identified or not supported.";
        public static final String ERROR_LISTING_WORKFLOWS =  "Server error when listing workflows";
        public static final String SQL_ERROR_LISTING_WORKFLOWS =  "SQL error when getting workflows from DB";
        public static final String ERROR_GETTING_WORKFLOW_COUNT =  "Server error while getting workflows count for the tenantId: ";
        public static final String SQL_ERROR_GETTING_WORKFLOW_COUNT =  "SQL error when getting workflows count.";

        public static final String ERROR_INVALID_LIMIT = "Invalid limit requested. The limit should "
                + "be a value greater than 0.";
        public static final String ERROR_INVALID_OFFSET = "Invalid offset requested. The offset should "
                + "be a value greater than 0.";
    }
}
