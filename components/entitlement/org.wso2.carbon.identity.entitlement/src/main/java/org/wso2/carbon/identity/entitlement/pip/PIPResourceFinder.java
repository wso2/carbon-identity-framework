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

import java.util.Properties;
import java.util.Set;

/**
 * To register a PIP resource finder with the PDP. you need to implement this interface and
 * add an entry to pip-config.xml file (by specifying the full qualified class name, under "ResourceFinders")
 * which can be found at [CARBON_HOME]\repository\conf.
 * PIPResourceFinder will be fired by CarbonAttributeFinder whenever it wants to find a child or
 * descendant resources for a given resource value .
 */
public interface PIPResourceFinder {

    /**
     * initializes the Resource finder module
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
     * Will be fired by CarbonResourceFinder whenever it wants to find a child resources
     *
     * @param parentResourceId parent resource value
     * @param context          EvaluationCtx which encapsulates the XACML request.
     * @return Returns a <code>Set</code> of <code>String</code>s that represent the child resources
     * @throws Exception throws if any failure is occurred
     */
    public Set<String> findChildResources(String parentResourceId, EvaluationCtx context) throws Exception;

    /**
     * Will be fired by CarbonResourceFinder whenever it wants to find a descendant resources
     *
     * @param parentResourceId parent resource value
     * @param context          EvaluationCtx which encapsulates the XACML request.
     * @return Returns a <code>Set</code> of <code>String</code>s that represent the descendant resources
     * @throws Exception throws if any failure is occurred
     */
    public Set<String> findDescendantResources(String parentResourceId, EvaluationCtx context) throws Exception;

    /**
     * This is to inform whether to ignore caching of descendant and child resources in carbon level
     *
     * @return True/False
     */
    public boolean overrideDefaultCache();

    /**
     * Clears the entire cache.
     */
    public void clearCache();
}
