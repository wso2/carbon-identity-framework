/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.gateway.common.model;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.Arrays;

public class ProvisioningConnectorConfig implements Serializable {

    private static final long serialVersionUID = -4569973060498183209L;

    protected Property[] provisioningProperties = new Property[0];
    protected String name;
    protected boolean enabled;
    protected boolean blocking;
    protected boolean rulesEnabled;


    /**
     * @return
     */
    public Property[] getProvisioningProperties() {
        return provisioningProperties;
    }

    /**
     * @param provisioningProperties
     */
    public void setProvisioningProperties(Property[] provisioningProperties) {


    }

    /**
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * @param string
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return
     */
    public boolean isValid() {
        return false;
    }

    /**
     * @return
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param string
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isBlocking() {
        return blocking;
    }

    public void setBlocking(boolean blocking) {
        this.blocking = blocking;
    }

    public boolean isRulesEnabled() {
        return rulesEnabled;
    }

    public void setRulesEnabled(boolean rulesEnabled) {
        this.rulesEnabled = rulesEnabled;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProvisioningConnectorConfig))
            return false;

        ProvisioningConnectorConfig that = (ProvisioningConnectorConfig) o;

        if (!StringUtils.equals(name, that.name)) {
            return false;
        }
        if (!Arrays.equals(provisioningProperties, that.provisioningProperties)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        hashCode = hashCode * 17 + (name != null ? name.hashCode() : 0);
        hashCode = hashCode * 31 + (provisioningProperties != null ? Arrays.hashCode(provisioningProperties) : 0);
        return hashCode;
    }
}
