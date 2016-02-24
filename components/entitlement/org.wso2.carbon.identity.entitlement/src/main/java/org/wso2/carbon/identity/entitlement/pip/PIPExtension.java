/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.entitlement.pip;

import org.wso2.balana.ctx.AbstractRequestCtx;

import java.util.Properties;

/**
 * PIPExtensions will be fired for each and every XACML request - which will give a handle to the
 * incoming request.
 */
public interface PIPExtension {

    /**
     * initializes the PIPExtension  module
     *
     * @param properties properties, that need to initialize the module. These properties can be
     *                   defined in entitlement-config.xml file
     * @throws Exception throws when initialization is failed
     */
    public void init(Properties properties) throws Exception;

    /**
     * Gives a handle to the XACML request built. Can be used to carry out custom checks or updates
     * before sending to the PDP.
     *
     * @param request Incoming XACML request.
     */
    public void update(AbstractRequestCtx request);

}
