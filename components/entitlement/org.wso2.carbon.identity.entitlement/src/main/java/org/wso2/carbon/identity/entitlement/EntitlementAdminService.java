/*
 *  Copyright (c) Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.entitlement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.entitlement.dao.ConfigDAO;
import org.wso2.carbon.identity.entitlement.dto.PDPDataHolder;
import org.wso2.carbon.identity.entitlement.dto.PIPFinderDataHolder;
import org.wso2.carbon.identity.entitlement.dto.PolicyFinderDataHolder;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;
import org.wso2.carbon.identity.entitlement.pap.store.PAPPolicyFinder;
import org.wso2.carbon.identity.entitlement.pdp.EntitlementEngine;
import org.wso2.carbon.identity.entitlement.pip.AbstractPIPAttributeFinder;
import org.wso2.carbon.identity.entitlement.pip.CarbonAttributeFinder;
import org.wso2.carbon.identity.entitlement.pip.CarbonResourceFinder;
import org.wso2.carbon.identity.entitlement.pip.PIPAttributeFinder;
import org.wso2.carbon.identity.entitlement.pip.PIPResourceFinder;
import org.wso2.carbon.identity.entitlement.policy.finder.PolicyFinderModule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Entitlement PDP related admin services are exposed
 */
public class  EntitlementAdminService {

	private static Log log = LogFactory.getLog(EntitlementAdminService.class);

    /**
     * Clears the decision cache.
     *
     * @throws EntitlementException throws
     */
    public void clearDecisionCache() throws EntitlementException {
        EntitlementEngine.getInstance().clearDecisionCache();
        if (log.isDebugEnabled()) {
            log.debug("Decision Caching is cleared by using admin service");
        }
    }

    /**
     * Clears the policy cache.
     *
     * @throws EntitlementException throws
     */
    public void clearPolicyCache() throws EntitlementException {
        EntitlementEngine.getInstance().invalidatePolicyCache();
        if (log.isDebugEnabled()) {
            log.debug("Decision Caching is cleared by using admin service");
        }
    }

    /**
     * Clears Carbon attribute finder cache and All the attribute cache implementations in each
     * PIP attribute finder level
     *
     * @throws EntitlementException throws
     */
    public void clearAllAttributeCaches() throws EntitlementException {
        CarbonAttributeFinder finder = EntitlementEngine.getInstance().getCarbonAttributeFinder();
        if (finder != null) {
            finder.clearAttributeCache();
            // we need invalidate decision cache as well.
            clearDecisionCache();
        } else {
            throw new EntitlementException("Can not clear all attribute caches - Carbon Attribute Finder "
                    + "is not initialized");
        }

        Map<PIPAttributeFinder, Properties> designators = EntitlementServiceComponent.getEntitlementConfig()
                .getDesignators();
        if (designators != null && !designators.isEmpty()) {
            Set<PIPAttributeFinder> pipAttributeFinders = designators.keySet();
            for (PIPAttributeFinder pipAttributeFinder : pipAttributeFinders) {
                pipAttributeFinder.clearCache();
            }
        }
    }


    /**
     * Clears the carbon attribute cache
     *
     * @throws EntitlementException throws
     */
    public void clearCarbonAttributeCache() throws EntitlementException {

        CarbonAttributeFinder finder = EntitlementEngine.getInstance().getCarbonAttributeFinder();
        if (finder != null) {
            finder.clearAttributeCache();
            // we need invalidate decision cache as well.
            clearDecisionCache();
        } else {
            throw new EntitlementException("Can not clear attribute cache - Carbon Attribute Finder "
                    + "is not initialized");
        }

        Map<PIPAttributeFinder, Properties> designators = EntitlementServiceComponent.getEntitlementConfig()
                .getDesignators();
        if (designators != null && !designators.isEmpty()) {
            Set<PIPAttributeFinder> pipAttributeFinders = designators.keySet();
            for (PIPAttributeFinder pipAttributeFinder : pipAttributeFinders) {
                if (pipAttributeFinder instanceof AbstractPIPAttributeFinder) {
                    pipAttributeFinder.clearCache();
                }
            }
        }
    }

    /**
     * Clears the cache maintained by the attribute finder.
     *
     * @param attributeFinder Canonical name of the attribute finder class.
     */
    public void clearAttributeFinderCache(String attributeFinder) {

        Map<PIPAttributeFinder, Properties> designators = EntitlementServiceComponent.getEntitlementConfig()
                .getDesignators();
        if (designators != null && !designators.isEmpty()) {
            Set<PIPAttributeFinder> pipAttributeFinders = designators.keySet();
            for (PIPAttributeFinder pipAttributeFinder : pipAttributeFinders) {
                if (pipAttributeFinder instanceof AbstractPIPAttributeFinder) {
                    if (pipAttributeFinder.getClass().getCanonicalName().equals(attributeFinder)) {
                        pipAttributeFinder.clearCache();
                        break;
                    }
                }
            }
        }
    }

    /**
     * Clears the cache maintained by the attribute finder - by attributes
     *
     * @param attributeFinder Canonical name of the attribute finder class.
     * @param attributeIds    An array of attribute id.
     */
    public void clearAttributeFinderCacheByAttributes(String attributeFinder, String[] attributeIds) {

        Map<PIPAttributeFinder, Properties> designators = EntitlementServiceComponent.getEntitlementConfig()
                .getDesignators();
        if (designators != null && !designators.isEmpty()) {
            Set<PIPAttributeFinder> pipAttributeFinders = designators.keySet();
            for (PIPAttributeFinder pipAttributeFinder : pipAttributeFinders) {
                if (pipAttributeFinder.getClass().getCanonicalName().equals(attributeFinder)) {
                    pipAttributeFinder.clearCache(attributeIds);
                    break;
                }
            }
        }
    }

    /**
     * Clears Carbon resource finder cache and All the resource cache implementations in each
     * PIP resource finder level
     *
     * @throws EntitlementException throws
     */
    public void clearAllResourceCaches() throws EntitlementException {
        CarbonResourceFinder finder = EntitlementEngine.getInstance().getCarbonResourceFinder();
        if (finder != null) {
            finder.clearAttributeCache();
            // we need invalidate decision cache as well.
            clearDecisionCache();
        } else {
            throw new EntitlementException("Can not clear attribute cache - Carbon Attribute Finder "
                    + "is not initialized");
        }
    }

    /**
     * Clears the carbon resource cache
     *
     * @throws EntitlementException throws
     */
    public void clearCarbonResourceCache() throws EntitlementException {
        CarbonResourceFinder finder = EntitlementEngine.getInstance().getCarbonResourceFinder();
        if (finder != null) {
            finder.clearAttributeCache();
            // we need invalidate decision cache as well.
            clearDecisionCache();
        } else {
            throw new EntitlementException("Can not clear attribute cache - Carbon Attribute Finder "
                    + "is not initialized");
        }

        Map<PIPResourceFinder, Properties> resourceConfigs = EntitlementServiceComponent.getEntitlementConfig()
                .getResourceFinders();
        if (resourceConfigs != null && !resourceConfigs.isEmpty()) {
            Set<PIPResourceFinder> resourceFinders = resourceConfigs.keySet();
            for (PIPResourceFinder pipResourceFinder : resourceFinders) {
                pipResourceFinder.clearCache();
            }
        }
    }

    /**
     * Clears the cache maintained by the resource finder.
     *
     * @param resourceFinder Canonical name of the resource finder class.
     */
    public void clearResourceFinderCache(String resourceFinder) {

        Map<PIPResourceFinder, Properties> resourceConfigs = EntitlementServiceComponent.getEntitlementConfig()
                .getResourceFinders();
        if (resourceConfigs != null && !resourceConfigs.isEmpty()) {
            Set<PIPResourceFinder> resourceFinders = resourceConfigs.keySet();
            for (PIPResourceFinder pipResourceFinder : resourceFinders) {
                if (resourceFinder.getClass().getCanonicalName().equals(resourceFinder)) {
                    pipResourceFinder.clearCache();
                    break;
                }
            }
        }
    }


    /**
     * Refreshes the supported Attribute ids of a given attribute finder module
     *
     * @param attributeFinder Canonical name of the attribute finder class.
     * @throws EntitlementException throws if fails to  refresh
     */
    public void refreshAttributeFinder(String attributeFinder) throws EntitlementException {

        Map<PIPAttributeFinder, Properties> designators = EntitlementServiceComponent.getEntitlementConfig()
                .getDesignators();
        if (attributeFinder != null && designators != null && !designators.isEmpty()) {
            Set<Map.Entry<PIPAttributeFinder, Properties>> pipAttributeFinders = designators.entrySet();
            for (Map.Entry<PIPAttributeFinder, Properties> entry : pipAttributeFinders) {
                if (attributeFinder.equals(entry.getKey().getClass().getName()) ||
                        attributeFinder.equals(entry.getKey().getModuleName())) {
                    try {
                        entry.getKey().init(entry.getValue());
                        entry.getKey().clearCache();
                        CarbonAttributeFinder carbonAttributeFinder = EntitlementEngine.
                                getInstance().getCarbonAttributeFinder();
                        carbonAttributeFinder.init();
                    } catch (Exception e) {
                        throw new EntitlementException("Error while refreshing attribute finder - " +
                                attributeFinder);
                    }
                    break;
                }
            }
        }
    }

    /**
     * Refreshes the supported resource id of a given resource finder module
     *
     * @param resourceFinder Canonical name of the resource finder class.
     * @throws EntitlementException throws if fails to  refresh
     */
    public void refreshResourceFinder(String resourceFinder) throws EntitlementException {

        Map<PIPResourceFinder, Properties> resourceFinders = EntitlementServiceComponent.getEntitlementConfig()
                .getResourceFinders();
        if (resourceFinder != null && resourceFinders != null && !resourceFinders.isEmpty()) {
            for (Map.Entry<PIPResourceFinder, Properties> entry : resourceFinders.entrySet()) {
                if (resourceFinder.equals(entry.getKey().getClass().getName()) ||
                        resourceFinder.equals(entry.getKey().getModuleName())) {
                    try {
                        entry.getKey().init(entry.getValue());
                        entry.getKey().clearCache();
                        CarbonAttributeFinder carbonAttributeFinder = EntitlementEngine.
                                getInstance().getCarbonAttributeFinder();
                        carbonAttributeFinder.init();
                    } catch (Exception e) {
                        throw new EntitlementException("Error while refreshing attribute finder - " +
                                resourceFinder);
                    }
                    break;
                }
            }
        }
    }

    /**
     * Refreshes the supported resource id of a given resource finder module
     *
     * @param policyFinder Canonical name of the resource finder class.
     * @throws EntitlementException throws if fails to  refresh
     */
    public void refreshPolicyFinders(String policyFinder) throws EntitlementException {

        Map<PolicyFinderModule, Properties> policyFinders = EntitlementServiceComponent.getEntitlementConfig()
                .getPolicyFinderModules();
        if (policyFinder != null && policyFinders != null && !policyFinders.isEmpty()) {
            for (Map.Entry<PolicyFinderModule, Properties> entry : policyFinders.entrySet()) {
                if (policyFinder.equals(entry.getKey().getClass().getName()) ||
                        policyFinder.equals(entry.getKey().getModuleName())) {
                    try {
                        entry.getKey().init(entry.getValue());
                        EntitlementEngine.getInstance().getCarbonPolicyFinder().init();
                        // need to re init all policy finder modules in the cluster.
                        // therefore calling invalidation cache
                        EntitlementEngine.getInstance().clearDecisionCache();
                    } catch (Exception e) {
                        throw new EntitlementException("Error while refreshing attribute finder - " +
                                policyFinder);
                    }
                    break;
                }
            }
        }
    }


    /**
     * Tests engine of PAP policy store
     *
     * @param xacmlRequest
     * @return
     * @throws EntitlementException
     */
    public String doTestRequest(String xacmlRequest) throws EntitlementException {
        return EntitlementEngine.getInstance().test(xacmlRequest);
    }

    /**
     * Tests engine of PAP policy store
     *
     * @param xacmlRequest
     * @param policies     policy ids that is evaluated
     * @return
     * @throws EntitlementException
     */
    public String doTestRequestForGivenPolicies(String xacmlRequest, String[] policies)
            throws EntitlementException {
        EntitlementEngine engine = EntitlementEngine.getInstance();
        PAPPolicyFinder papPolicyFinder = (PAPPolicyFinder) engine.getPapPolicyFinder().
                getModules().iterator().next();
        papPolicyFinder.setPolicyIds(Arrays.asList(policies));
        String response = EntitlementEngine.getInstance().test(xacmlRequest);
        papPolicyFinder.initPolicyIds();

        return response;
    }

    /**
     * @return
     */
    public PDPDataHolder getPDPData() {

        PDPDataHolder pdpDataHolder = new PDPDataHolder();

        Map<PolicyFinderModule, Properties> finderModules = EntitlementServiceComponent.
                getEntitlementConfig().getPolicyFinderModules();
        Map<PIPAttributeFinder, Properties> attributeModules = EntitlementServiceComponent.
                getEntitlementConfig().getDesignators();
        Map<PIPResourceFinder, Properties> resourceModules = EntitlementServiceComponent.
                getEntitlementConfig().getResourceFinders();

        if (finderModules != null) {
            List<String> list = new ArrayList<String>();
            for (Map.Entry<PolicyFinderModule, Properties> entry : finderModules.entrySet()) {
                PolicyFinderModule module = entry.getKey();
                if (module != null) {
                    if (module.getModuleName() != null) {
                        list.add(module.getModuleName());
                    } else {
                        list.add(module.getClass().getName());
                    }
                }
            }
            pdpDataHolder.setPolicyFinders(list.toArray(new String[list.size()]));
        }

        if (attributeModules != null) {
            List<String> list = new ArrayList<String>();
            for (Map.Entry<PIPAttributeFinder, Properties> entry : attributeModules.entrySet()) {
                PIPAttributeFinder module = entry.getKey();
                if (module != null) {
                    if (module.getModuleName() != null) {
                        list.add(module.getModuleName());
                    } else {
                        list.add(module.getClass().getName());
                    }
                }
            }
            pdpDataHolder.setPipAttributeFinders(list.toArray(new String[list.size()]));
        }

        if (resourceModules != null) {
            List<String> list = new ArrayList<String>();
            for (Map.Entry<PIPResourceFinder, Properties> entry : resourceModules.entrySet()) {
                PIPResourceFinder module = entry.getKey();
                if (module != null) {
                    if (module.getModuleName() != null) {
                        list.add(module.getModuleName());
                    } else {
                        list.add(module.getClass().getName());
                    }
                }
            }
            pdpDataHolder.setPipResourceFinders(list.toArray(new String[list.size()]));
        }

        return pdpDataHolder;
    }

    /**
     * @param finder
     * @return
     */
    public PolicyFinderDataHolder getPolicyFinderData(String finder) throws EntitlementException {

        PolicyFinderDataHolder holder = null;
        // get registered finder modules
        Map<PolicyFinderModule, Properties> finderModules = EntitlementServiceComponent.
                getEntitlementConfig().getPolicyFinderModules();
        if (finderModules == null || finder == null) {
            return null;
        }

        for (Map.Entry<PolicyFinderModule, Properties> entry : finderModules.entrySet()) {
            PolicyFinderModule module = entry.getKey();
            if (module != null && (finder.equals(module.getModuleName()) ||
                    finder.equals(module.getClass().getName()))) {
                holder = new PolicyFinderDataHolder();
                if (module.getModuleName() != null) {
                    holder.setModuleName(module.getModuleName());
                } else {
                    holder.setModuleName(module.getClass().getName());
                }
                holder.setClassName(module.getClass().getName());
                holder.setPolicyIdentifiers(module.getOrderedPolicyIdentifiers());
                break;
            }

        }
        return holder;
    }

    /**
     * @param finder
     * @return
     */
    public PIPFinderDataHolder getPIPAttributeFinderData(String finder) {

        PIPFinderDataHolder holder = null;
        // get registered finder modules
        Map<PIPAttributeFinder, Properties> attributeModules = EntitlementServiceComponent.
                getEntitlementConfig().getDesignators();
        if (attributeModules == null || finder == null) {
            return null;
        }

        for (Map.Entry<PIPAttributeFinder, Properties> entry : attributeModules.entrySet()) {
            PIPAttributeFinder module = entry.getKey();
            if (module != null && (finder.equals(module.getModuleName()) ||
                    finder.equals(module.getClass().getName()))) {
                holder = new PIPFinderDataHolder();
                if (module.getModuleName() != null) {
                    holder.setModuleName(module.getModuleName());
                } else {
                    holder.setModuleName(module.getClass().getName());
                }
                holder.setClassName(module.getClass().getName());
                holder.setSupportedAttributeIds(module.getSupportedAttributes().
                        toArray(new String[module.getSupportedAttributes().size()]));
                break;
            }
        }
        return holder;
    }

    /**
     * @param finder
     * @return
     */
    public PIPFinderDataHolder getPIPResourceFinderData(String finder) {

        PIPFinderDataHolder holder = null;
        // get registered finder modules
        Map<PIPResourceFinder, Properties> resourceModules = EntitlementServiceComponent.
                getEntitlementConfig().getResourceFinders();

        if (resourceModules == null || finder == null) {
            return null;
        }

        for (Map.Entry<PIPResourceFinder, Properties> entry : resourceModules.entrySet()) {
            PIPResourceFinder module = entry.getKey();
            if (module != null) {
                holder = new PIPFinderDataHolder();
                if (module.getModuleName() != null) {
                    holder.setModuleName(module.getModuleName());
                } else {
                    holder.setModuleName(module.getClass().getName());
                }
                holder.setClassName(module.getClass().getName());
                break;
            }
        }
        return holder;
    }

    /**
     * Gets globally defined policy combining algorithm
     *
     * @return policy combining algorithm as a String
     * @throws EntitlementException throws
     */
    public String getGlobalPolicyAlgorithm() throws EntitlementException {

        ConfigDAO config = new RegistryConfigDAOImpl();
        return config.getGlobalPolicyAlgorithmName();
    }

    /**
     * Sets policy combining algorithm globally
     *
     * @param policyCombiningAlgorithm policy combining algorithm as a String
     * @throws EntitlementException throws
     */
    public void setGlobalPolicyAlgorithm(String policyCombiningAlgorithm) throws EntitlementException {

        ConfigDAO config = new RegistryConfigDAOImpl();
        config.setGlobalPolicyAlgorithm(policyCombiningAlgorithm);
    }
}
