/*
*  Copyright (c)  WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.identity.entitlement.policy.publisher;

import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.dto.PolicyDTO;

import java.util.Properties;

/**
 * policy publisher module that is used to publish policies to external PDPs. External PDP can be
 * identity server or else can be any thing. Therefore this interface provide an extension to publish
 * policies to different modules.
 */
public interface PolicyPublisherModule {

    /**
     * initializes policy publisher retriever module
     *
     * @param properties Properties, that are needed to initialize the module or
     *                   that are needed to populate the management console ui of publisher configuration.
     *                   These properties can be defined in entitlement-properties file.
     */
    public void init(Properties properties);

    /**
     * Load the properties are needed to initialize the module or that are needed to populate
     * to populate the management console ui of publisher configuration.
     * These properties can be loaded from external source
     *
     * @return Properties
     */
    public Properties loadProperties();

    /**
     * gets name of this module
     *
     * @return name as String
     */
    public String getModuleName();

    /**
     * publishes policy to given subscriber
     *
     * @param policyDTO policy as PolicyDTO
     * @param action    publishing action
     * @param enable    enable
     * @param order
     * @throws EntitlementException
     */
    public void publish(PolicyDTO policyDTO, String action, boolean enable, int order) throws EntitlementException;

}
