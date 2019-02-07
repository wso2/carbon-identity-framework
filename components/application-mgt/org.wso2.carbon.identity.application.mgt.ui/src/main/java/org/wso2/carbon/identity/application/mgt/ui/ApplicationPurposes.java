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
package org.wso2.carbon.identity.application.mgt.ui;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents model used for management console to hold application specific purposes and shared purposes
 * associated with the application.
 */
public class ApplicationPurposes {

    List<ApplicationPurpose> appPurposes = new ArrayList<>();
    List<ApplicationPurpose> appSharedPurposes = new ArrayList<>();

    public List<ApplicationPurpose> getAppPurposes() {

        return appPurposes;
    }

    public void setAppPurposes(List<ApplicationPurpose> appPurposes) {

        this.appPurposes = appPurposes;
    }

    public List<ApplicationPurpose> getAppSharedPurposes() {

        return appSharedPurposes;
    }

    public void setAppSharedPurposes(List<ApplicationPurpose> appSharedPurposes) {

        this.appSharedPurposes = appSharedPurposes;
    }
}
