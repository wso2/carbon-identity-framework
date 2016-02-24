/*
*  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.entitlement.pap;

import org.wso2.carbon.identity.entitlement.dto.EntitlementTreeNodeDTO;

import java.util.Properties;
import java.util.Set;

/**
 * When entitlement rules are created from  WSO2 Identity Server, It can be defined pre-defined
 * entitlement data that can be used for creating rules.  These entitlement data are related with
 * an external application. Same entitlement data can be used for more than one application.
 * This interface supports to retrieve data from external sources such as
 * databases, LDAPs,or file systems.
 */
public interface EntitlementDataFinderModule {

    /**
     * Initializes data retriever module
     *
     * @param properties properties, that need to initialize the module. These properties can be
     *                   defined in entitlement.properties file
     * @throws Exception throws when initialization is failed
     */
    public void init(Properties properties) throws Exception;

    /**
     * Gets name of this module
     *
     * @return name as String
     */
    public String getModuleName();

    /**
     * Returns application names that are related with entitlement data that is retrieved by this
     * module
     *
     * @return Set of related applications
     */
    public Set<String> getRelatedApplications();


    /**
     * Returns categories that are supported with entitlement data that is retrieved by this
     * module
     *
     * @return Set of related applications
     */
    public Set<String> getSupportedCategories();

    /**
     * Finds entitlement data values for given category type
     *
     * @param category category of the entitlement data.
     * @param regex    regex values for filter out the return data
     * @param limit    limit for filtered data
     * @return Set of entitlement data values that has been encapsulated
     * in to <code>EntitlementTreeNodeDTO</code>
     * @throws Exception throws, if fails
     */
    public EntitlementTreeNodeDTO getEntitlementData(String category, String regex,
                                                     int limit) throws Exception;

    /**
     * Finds entitlement data values for given category and for given hierarchical level
     *
     * @param category category of the entitlement data.
     * @param level    hierarchical level that data must be retrieved. If root level, value must be 1
     * @return Set of entitlement data values that has been encapsulated
     * in to <code>EntitlementTreeNodeDTO</code>
     * @throws Exception throws, if fails
     */
    public EntitlementTreeNodeDTO getEntitlementDataByLevel(String category, int level) throws Exception;

    /**
     * Returns supported hierarchical levels of that data must be retrieved. If levels are not supported
     * value must be zero
     *
     * @return number of levels
     */
    public int getSupportedHierarchicalLevels();

    /**
     * Defines whether node <code>EntitlementTreeNodeDTO</code> is defined by child node name
     * or by full path name with parent node names
     *
     * @return true or false
     */
    public boolean isFullPathSupported();

    /**
     * Defines whether tree nodes of <code>EntitlementTreeNodeDTO</code> elements are shown
     * in UI by as a tree or flat structure
     *
     * @return if as a tree -> true or else -> false
     */
    public boolean isHierarchicalTree();


    /**
     * Defines whether we can search the tree nodes of <code>EntitlementTreeNodeDTO</code> elements
     * using regexp  expressions
     *
     * @return if as a tree -> true or else -> false
     */
    public boolean isSearchSupported();

    /**
     * Defines whether entitlement data that is retrieved by this module,
     * is related with all applications
     *
     * @return true or false
     */
    public boolean isAllApplicationRelated();
}
