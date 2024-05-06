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

package org.wso2.carbon.identity.entitlement.policy.finder;

import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.dto.AttributeDTO;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Policy manage module is a extension point where XACML policies can be stored and loaded in to the PDP
 * from different sources. There can be more than one policy store modules.
 */
public interface PolicyFinderModule {

    /**
     * Policy search is done by creating requests from all combinations of the attributes that is
     * found by policy manage module
     */
    public final static int ALL_COMBINATIONS = 0;

    /**
     * Policy search is done by creating requests from combinations of category of the attributes that is
     * found by policy manage module
     */
    public final static int COMBINATIONS_BY_CATEGORY = 1;

    /**
     * Policy search is done by creating requests from combinations of given parameter
     * of the attributes that is found by policy manage module
     */
    public final static int COMBINATIONS_BY_PARAMETER = 2;

    /**
     * Policy search is done by creating requests from combinations of given parameter
     * and category of the attributes that is found by policy manage module
     */
    public final static int COMBINATIONS_BY_CATEGORY_AND_PARAMETER = 3;

    /**
     * Policy search is done by creating requests from the attributes that is
     * found by policy manage module
     */
    public final static int NO_COMBINATIONS = 4;


    /**
     * initializes policy manage module
     *
     * @param properties Properties, that need to initialize the module
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
     * gets all supported active policies. policies are fetched as Strings.
     * if policy ordering is supported by module itself, these policies must be ordered.
     *
     * @return array of policies as Strings
     */
    public String[] getActivePolicies() throws EntitlementException;

    /**
     * gets all supported policy ids by this module
     * if policy ordering is supported by module itself, these policy ids must be ordered
     *
     * @return array of policy ids as Strings
     */
    public String[] getOrderedPolicyIdentifiers() throws EntitlementException;

    /**
     * gets policy for given policy Id
     *
     * @param policyId policy id as String value
     * @return policy as String
     */
    public String getPolicyString(String policyId);


    public int getPolicyOrder(String policyId);


    /**
     * gets reference policy for given policy Id
     * <p/>
     * reference policy can not be with PDP policy store,  may be in some external policy store
     * Therefore new method has been add for retrieve reference policies
     *
     * @param policyId policy id as String value
     * @return reference policy as String
     */
    public String getReferencedPolicy(String policyId);

    /**
     * gets attributes that are used for policy searching
     *
     * @param identifier     unique identifier to separate out search attributes
     * @param givenAttribute pre-given attributes to retrieve other attributes
     * @return return search attributes based on a given policy.  Map of policy id with search attributes.
     */
    public Map<String, Set<AttributeDTO>> getSearchAttributes(String identifier,
                                                              Set<AttributeDTO> givenAttribute);

    /**
     * gets support attribute searching scheme of this module
     *
     * @return return scheme identifier value
     */
    public int getSupportedSearchAttributesScheme();

    /**
     * returns whether this module supports for default category of policies
     * if means policies has been written based subject or users, resource, actions and environment
     *
     * @return whether supported or not
     */
    public boolean isDefaultCategoriesSupported();

    /**
     * returns whether this module supports for policy ordering.
     *
     * @return whether supported or not
     */
    public boolean isPolicyOrderingSupport();

    /**
     * returns whether this module supports for policy activation or de-activation.
     *
     * @return whether supported or not
     */
    public boolean isPolicyDeActivationSupport();

}
