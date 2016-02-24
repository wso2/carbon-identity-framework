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

    }
    public static class ParameterHolder {
        public static final String TEMPLATE = "Template" ;
        public static final String  WORKFLOW_IMPL = "WorkflowImpl" ;
    }


}
