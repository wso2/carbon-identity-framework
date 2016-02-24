/*
 * Copyright (c) 2010 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.user.mgt;

import org.wso2.carbon.CarbonConstants;

public class UserMgtConstants {

    private UserMgtConstants(){

    }

    public static final String SYSTEM_RESOURCE = "System";

    public static final String UI_PERMISSION_ROOT = CarbonConstants.UI_PERMISSION_COLLECTION + "/";
    public static final String UI_ADMIN_PERMISSION_ROOT = CarbonConstants.UI_ADMIN_PERMISSION_COLLECTION
            + "/";
    public static final String UI_PROTECTED_PERMISSION_ROOT = CarbonConstants.UI_PROTECTED_PERMISSION_COLLECTION
            + "/";

    public static final String DISPLAY_NAME = "name";
    public static final String EXECUTE_ACTION = "ui.execute";

    public static final String INTERNAL_ROLE = "Internal";

    public static final String EXTERNAL_ROLE = "External";

    public static final String APPLICATION_DOMAIN = "Application";

    //component xml constants
}
