/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.gateway;

import org.wso2.carbon.identity.framework.handler.HandlerIdentifier;

public class GatewayHandlerIdentifier implements HandlerIdentifier {

    private String spName;
    private String configName;
    private String handlerName;

    public GatewayHandlerIdentifier() {

    }

    public GatewayHandlerIdentifier(String spName, String configName, String handlerName) {

        this.spName = spName;
        this.configName = configName;
        this.handlerName = handlerName;
    }

    public String getSpName() {

        return spName;
    }

    public void setSpName(String spName) {

        this.spName = spName;
    }

    public String getConfigName() {

        return configName;
    }

    public void setConfigName(String configName) {

        this.configName = configName;
    }

    public String getHandlerName() {

        return handlerName;
    }

    public void setHandlerName(String handlerName) {

        this.handlerName = handlerName;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        GatewayHandlerIdentifier that = (GatewayHandlerIdentifier) o;

        if (!spName.equals(that.spName)) {
            return false;
        }
        if (!configName.equals(that.configName)) {
            return false;
        }
        return handlerName.equals(that.handlerName);
    }

    @Override
    public int hashCode() {

        int result = super.hashCode();
        result = 31 * result + spName.hashCode();
        result = 31 * result + configName.hashCode();
        result = 31 * result + handlerName.hashCode();
        return result;
    }
}
