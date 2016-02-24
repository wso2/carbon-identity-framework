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

import org.wso2.balana.ctx.EvaluationCtx;

import java.net.URI;
import java.util.Properties;
import java.util.Set;

/**
 * To register a PIP attribute handler with the PDP against their supported attributes - you need to
 * implement this interface and add an entry to pip-config.xml file - which should be inside
 * [CARBON_HOME]\repository\conf. PIPAttributeFinder will be fired by CarbonAttributeFinder whenever
 * it finds an attribute supported by this module.
 */
public interface PIPAttributeFinder {

    /**
     * initializes the Attribute finder module
     *
     * @param properties properties, that need to initialize the module. These properties can be
     *                   defined in pip-config.xml file
     * @throws Exception throws when initialization is failed
     */
    public void init(Properties properties) throws Exception;

    /**
     * gets name of this module
     *
     * @return name as String
     */
    public String getModuleName();

    /**
     * Will be fired by CarbonAttributeFinder whenever it finds an attribute supported by this
     * module. This method has given the flexibility for users to implement more advance use cases
     * within PIP attribute finder. Here PIP attribute finder have been given access for <code>EvaluationCtx</code>
     * which encapsulates the XACML request.
     *
     * @param attributeType
     * @param attributeId   The unique id of the required attribute.
     * @param category      Category of the subject
     * @param issuer        The attribute issuer.
     * @param context       EvaluationCtx which encapsulates the XACML request.
     * @return Returns a <code>Set</code> of <code>String</code>s that represent the attribute
     * values.
     * @throws Exception throws, if fails
     */
    public Set<String> getAttributeValues(URI attributeType, URI attributeId, URI category,
                                          String issuer, EvaluationCtx context) throws Exception;

    /**
     * Returns a <code>Set</code> of <code>String</code>s that represent the attributeIds handled by
     * this module, or null if this module doesn't handle any specific attributeIds. A return value
     * of null means that this module will not handle any attributes.
     *
     * @return <code>Set</code> of <code>String</code>s that represent the attributeIds
     */
    public Set<String> getSupportedAttributes();

    /**
     * This is to inform whether to ignore caching of attributes registered for this attribute finer
     * or not.
     *
     * @return True/False
     */
    public boolean overrideDefaultCache();

    /**
     * Clears the entire cache.
     */
    public void clearCache();

    /**
     * Clears only the cached attribute by name.
     *
     * @param attributeId attributeId that needs to be cleared
     */
    public void clearCache(String[] attributeId);
}
