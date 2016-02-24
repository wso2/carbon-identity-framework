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
import org.wso2.carbon.identity.entitlement.dto.PublisherDataHolder;

import java.util.Properties;

/**
 * This is extension point where you can  verify the policy publish
 */
public abstract class PublisherVerificationModule {

    /**
     * initializes policy publisher retriever module
     *
     * @param properties Properties, that are needed to initialize the module
     * @throws Exception throws when initialization is failed
     */
    public abstract void init(Properties properties) throws Exception;


    /**
     * Generates the verification code based on publisher
     *
     * @param holder <code>PublisherDataHolder</code>
     * @return returns verification code as <code>String</code>
     * @throws EntitlementException if there are errors in generating verification code
     */
    public abstract String getVerificationCode(PublisherDataHolder holder) throws EntitlementException;

    /**
     * Verifies whether given verification code is valid or not
     *
     * @param code verification code as <code>String</code>
     * @return whether True or False
     */
    public abstract boolean doVerify(String code);

}
