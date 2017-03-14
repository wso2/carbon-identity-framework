/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.gateway.common.model.sp;

import java.io.Serializable;
import java.util.Properties;

/**
 * RequestValidatorConfig is a SP model class.
 */
public class RequestValidatorConfig implements Serializable {

    private static final long serialVersionUID = -4069605490920681650L;

    private String type;
    private String uniquePropertyName;

    private Properties properties = new Properties();

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUniquePropertyName() {
        return uniquePropertyName;
    }

    public void setUniquePropertyName(String uniquePropertyName) {
        this.uniquePropertyName = uniquePropertyName;
    }
}
