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

package org.wso2.carbon.identity.flow.mgt.model;

/**
 * Flow configuration model class.
 */
public class FlowConfigDTO {

    private String flowType;
    private Boolean isEnabled;
    private Boolean isAutoLoginEnabled;

    public String getFlowType() {

        return flowType;
    }

    public void setFlowType(String flowType) {

        this.flowType = flowType;
    }

    public Boolean getIsEnabled() {

        return isEnabled;
    }

    public void setIsEnabled(Boolean enabled) {

        isEnabled = enabled;
    }

    public Boolean getIsAutoLoginEnabled() {

        return isAutoLoginEnabled;
    }

    public void setIsAutoLoginEnabled(Boolean autoLoginEnabled) {

        isAutoLoginEnabled = autoLoginEnabled;
    }
}
