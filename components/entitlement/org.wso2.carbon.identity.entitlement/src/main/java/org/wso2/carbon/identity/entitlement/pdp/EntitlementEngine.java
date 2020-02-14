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
package org.wso2.carbon.identity.entitlement.pdp;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.wso2.balana.Balana;
import org.wso2.balana.PDP;
import org.wso2.balana.PDPConfig;
import org.wso2.balana.ParsingException;
import org.wso2.balana.ctx.AbstractRequestCtx;
import org.wso2.balana.ctx.RequestCtxFactory;
import org.wso2.balana.ctx.ResponseCtx;
import org.wso2.balana.finder.AttributeFinder;
import org.wso2.balana.finder.AttributeFinderModule;
import org.wso2.balana.finder.PolicyFinder;
import org.wso2.balana.finder.PolicyFinderModule;
import org.wso2.balana.finder.ResourceFinder;
import org.wso2.balana.finder.ResourceFinderModule;
import org.wso2.balana.finder.impl.CurrentEnvModule;
import org.wso2.balana.finder.impl.SelectorModule;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.base.IdentityConstants;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.EntitlementUtil;
import org.wso2.carbon.identity.entitlement.PDPConstants;
import org.wso2.carbon.identity.entitlement.cache.DecisionCache;
import org.wso2.carbon.identity.entitlement.cache.PolicyCache;
import org.wso2.carbon.identity.entitlement.cache.SimpleDecisionCache;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;
import org.wso2.carbon.identity.entitlement.pap.store.PAPPolicyFinder;
import org.wso2.carbon.identity.entitlement.pap.store.PAPPolicyStore;
import org.wso2.carbon.identity.entitlement.pap.store.PAPPolicyStoreReader;
import org.wso2.carbon.identity.entitlement.pip.CarbonAttributeFinder;
import org.wso2.carbon.identity.entitlement.pip.CarbonResourceFinder;
import org.wso2.carbon.identity.entitlement.pip.PIPExtension;
import org.wso2.carbon.identity.entitlement.policy.PolicyRequestBuilder;
import org.wso2.carbon.identity.entitlement.policy.finder.CarbonPolicyFinder;
import org.wso2.carbon.identity.entitlement.policy.search.PolicySearch;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class EntitlementEngine {

    private PolicyFinder papPolicyFinder;
    private CarbonAttributeFinder carbonAttributeFinder;
    private CarbonResourceFinder carbonResourceFinder;
    private PolicyFinder carbonPolicyFinder;
    private PolicySearch policySearch;
    private PDP pdp;
    private PDP pdpTest;
    private Balana balana;
    private int tenantId;
    private static final Object lock = new Object();
    private boolean pdpDecisionCacheEnable;
    private List<AttributeFinderModule> attributeModules = new ArrayList<AttributeFinderModule>();
    private List<ResourceFinderModule> resourceModules = new ArrayList<ResourceFinderModule>();
    private static EntitlementEngine entitlementEngine;
    private static final long DEFAULT_ENTITLEMENT_ENGINE_CACHING_INTERVAL = 900;
    private static LoadingCache<Integer, EntitlementEngine> entitlementEngineLoadingCache;

    private DecisionCache decisionCache = null;
    private PolicyCache policyCache = null;

    private SimpleDecisionCache simpleDecisionCache = null;

    private static final Log log = LogFactory.getLog(EntitlementEngine.class);

    public PolicyCache getPolicyCache() {
        return policyCache;
    }

    public void clearDecisionCache() {
        this.decisionCache.clear();
        this.simpleDecisionCache.clear();
    }

    /**
     * Get a EntitlementEngine instance for that tenant. This method will return an
     * EntitlementEngine instance if exists, or creates a new one
     *
     * @return EntitlementEngine instance for that tenant
     */
    public static EntitlementEngine getInstance() {

        Integer tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
            if (entitlementEngine == null) {
                synchronized (lock) {
                    if (entitlementEngine == null) {
                        entitlementEngine = new EntitlementEngine(tenantId);
                    }
                }
            }
            return entitlementEngine;
        }
        if (entitlementEngineLoadingCache == null) {
            synchronized (lock) {
                if (entitlementEngineLoadingCache == null) {
                    entitlementEngineLoadingCache = CacheBuilder.newBuilder().weakValues().expireAfterAccess
                            (getCacheInterval(), TimeUnit.SECONDS)
                            .build(new CacheLoader<Integer, EntitlementEngine>() {
                                @Override
                                public EntitlementEngine load(Integer key) {

                                    return new EntitlementEngine(key);
                                }
                            });
                }
            }
        }

        EntitlementEngine entitleEngine = entitlementEngineLoadingCache.getIfPresent(tenantId);
        if (entitleEngine == null) {
            synchronized (lock) {
                entitleEngine = entitlementEngineLoadingCache.getIfPresent(tenantId);
                if (entitleEngine == null) {
                    entitlementEngineLoadingCache.put(tenantId, new EntitlementEngine(tenantId));
                }
            }
        }
        try {
            entitleEngine = entitlementEngineLoadingCache.get(tenantId);
        } catch (ExecutionException e) {
            log.error("Error while getting the entitle engine for the tenant : " + tenantId);
        }
        return entitleEngine;
    }

    private static long getCacheInterval() {

        Properties properties = EntitlementServiceComponent.getEntitlementConfig().getEngineProperties();
        String engineCachingInterval = properties.getProperty(PDPConstants
                .ENTITLEMENT_ENGINE_CACHING_INTERVAL);
        long entitlementEngineCachingInterval = DEFAULT_ENTITLEMENT_ENGINE_CACHING_INTERVAL;
        if (engineCachingInterval != null) {
            try {
                entitlementEngineCachingInterval = Long.parseLong(engineCachingInterval);
            } catch (NumberFormatException e) {
                log.warn("Invalid value for " + PDPConstants.ENTITLEMENT_ENGINE_CACHING_INTERVAL + ". Using " +
                        "default value " + entitlementEngineCachingInterval + " seconds.");
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug(PDPConstants.ENTITLEMENT_ENGINE_CACHING_INTERVAL + " not set. Using default value " +
                        entitlementEngineCachingInterval + " seconds.");
            }
        }
        return entitlementEngineCachingInterval;
    }

    private EntitlementEngine(int tenantId) {

        boolean isPDP = Boolean.parseBoolean((String) EntitlementServiceComponent.getEntitlementConfig().
                getEngineProperties().get(PDPConstants.PDP_ENABLE));
        boolean isPAP = Boolean.parseBoolean((String) EntitlementServiceComponent.getEntitlementConfig().
                getEngineProperties().get(PDPConstants.PAP_ENABLE));

        boolean pdpMultipleDecision = Boolean.parseBoolean((String) EntitlementServiceComponent.
                getEntitlementConfig().getEngineProperties().get(PDPConstants.MULTIPLE_DECISION_PROFILE_ENABLE));

        if (!isPAP && !isPDP) {
            isPAP = true;
        }

        // if PDP config file is not configured, then balana instance is created from default configurations
        balana = Balana.getInstance();

        setUpAttributeFinders();
        setUpResourceFinders();
        setUPPolicyFinder();

        this.tenantId = tenantId;

        Properties properties = EntitlementServiceComponent.getEntitlementConfig().getEngineProperties();
        pdpDecisionCacheEnable = Boolean.parseBoolean(properties.getProperty(PDPConstants.DECISION_CACHING));

        int pdpDecisionCachingInterval = -1;
        if (pdpDecisionCacheEnable) {
            String cacheInterval = properties.getProperty(PDPConstants.DECISION_CACHING_INTERVAL);
            if (cacheInterval != null) {
                try {
                    pdpDecisionCachingInterval = Integer.parseInt(cacheInterval.trim());
                } catch (Exception e) {
                    //ignore
                }
            }
        }

        int pdpPolicyCachingInterval = -1;
        String policyCacheInterval = properties.getProperty(PDPConstants.POLICY_CACHING_INTERVAL);
        if (policyCacheInterval != null) {
            try {
                pdpPolicyCachingInterval = Integer.parseInt(policyCacheInterval.trim());
            } catch (Exception e) {
                //ignore
            }
        }


        //init caches
        decisionCache = new DecisionCache(pdpDecisionCachingInterval);
        simpleDecisionCache = new SimpleDecisionCache(pdpDecisionCachingInterval);
        this.policyCache = new PolicyCache(pdpPolicyCachingInterval);

        // policy search

        policySearch = new PolicySearch(pdpDecisionCacheEnable, pdpDecisionCachingInterval);

        // Finally, initialize
        if (isPAP) {
            // Test PDP with all finders but policy finder is different
            PolicyFinder policyFinder = new PolicyFinder();
            Set<PolicyFinderModule> policyModules = new HashSet<PolicyFinderModule>();
            PAPPolicyFinder papPolicyFinder = new PAPPolicyFinder(new PAPPolicyStoreReader(new PAPPolicyStore()));
            policyModules.add(papPolicyFinder);
            policyFinder.setModules(policyModules);
            this.papPolicyFinder = policyFinder;

            AttributeFinder attributeFinder = new AttributeFinder();
            attributeFinder.setModules(attributeModules);

            ResourceFinder resourceFinder = new ResourceFinder();
            resourceFinder.setModules(resourceModules);

            PDPConfig pdpConfig = new PDPConfig(attributeFinder, policyFinder, resourceFinder, true);
            pdpTest = new PDP(pdpConfig);
        }

        if (isPDP) {
            // Actual PDP with all finders but policy finder is different
            AttributeFinder attributeFinder = new AttributeFinder();
            attributeFinder.setModules(attributeModules);

            ResourceFinder resourceFinder = new ResourceFinder();
            resourceFinder.setModules(resourceModules);

            PDPConfig pdpConfig =
                    new PDPConfig(attributeFinder, carbonPolicyFinder, resourceFinder, pdpMultipleDecision);
            pdp = new PDP(pdpConfig);
        }
    }


    /**
     * Test request for PDP
     *
     * @param xacmlRequest XACML request as String
     * @return response as String
     */
    public String test(String xacmlRequest) {

        if (log.isDebugEnabled() && IdentityUtil.isTokenLoggable(IdentityConstants.IdentityTokens.XACML_REQUEST)) {
            log.debug("XACML Request : " + xacmlRequest);
        }

        String xacmlResponse = pdpTest.evaluate(xacmlRequest);

        if (log.isDebugEnabled() && IdentityUtil.isTokenLoggable(IdentityConstants.IdentityTokens.XACML_RESPONSE)) {
            log.debug("XACML Response : " + xacmlResponse);
        }

        return xacmlResponse;
    }

    /**
     * Evaluates the given XACML request and returns the Response that the EntitlementEngine will
     * hand back to the PEP. PEP needs construct the XACML request before sending it to the
     * EntitlementEngine
     *
     * @param xacmlRequest XACML request as String
     * @return XACML response as String
     * @throws org.wso2.balana.ParsingException                          throws
     * @throws org.wso2.carbon.identity.entitlement.EntitlementException throws
     */

    public String evaluate(String xacmlRequest) throws EntitlementException, ParsingException {

        if (log.isDebugEnabled() && IdentityUtil.isTokenLoggable(IdentityConstants.IdentityTokens.XACML_REQUEST)) {
            log.debug("XACML Request : " + xacmlRequest);
        }

        String xacmlResponse;

        if ((xacmlResponse = (String) getFromCache(xacmlRequest, false)) != null) {
            if (log.isDebugEnabled() && IdentityUtil.isTokenLoggable(IdentityConstants.IdentityTokens.XACML_RESPONSE)) {
                log.debug("XACML Response : " + xacmlResponse);
            }
            return xacmlResponse;
        }

        Map<PIPExtension, Properties> extensions = EntitlementServiceComponent.getEntitlementConfig()
                .getExtensions();

        if (extensions != null && !extensions.isEmpty()) {
            PolicyRequestBuilder policyRequestBuilder = new PolicyRequestBuilder();
            Element xacmlRequestElement = policyRequestBuilder.getXacmlRequest(xacmlRequest);
            AbstractRequestCtx requestCtx = RequestCtxFactory.getFactory().
                    getRequestCtx(xacmlRequestElement);
            Set<PIPExtension> pipExtensions = extensions.keySet();
            for (PIPExtension pipExtension : pipExtensions) {
                pipExtension.update(requestCtx);
            }
            ResponseCtx responseCtx = pdp.evaluate(requestCtx);
            xacmlResponse = responseCtx.encode();
        } else {
            xacmlResponse = pdp.evaluate(xacmlRequest);
        }

        addToCache(xacmlRequest, xacmlResponse, false);

        if (log.isDebugEnabled() && IdentityUtil.isTokenLoggable(IdentityConstants.IdentityTokens.XACML_RESPONSE)) {
            log.debug("XACML Response : " + xacmlResponse);
        }

        return xacmlResponse;

    }

    /**
     * Evaluates the given XACML request and returns the ResponseCtx Response that the EntitlementEngine will
     * hand back to the PEP. PEP needs construct the XACML request before sending it to the
     * EntitlementEngine
     *
     * @param xacmlRequest XACML request as String
     * @return ResponseCtx response
     * @throws org.wso2.balana.ParsingException                          throws
     * @throws org.wso2.carbon.identity.entitlement.EntitlementException throws
     * @throws javax.xml.parsers.ParserConfigurationException            throws
     * @throws org.xml.sax.SAXException                                  throws
     * @throws java.io.IOException                                       throws
     */

    public ResponseCtx evaluateReturnResponseCtx(String xacmlRequest) throws EntitlementException, ParsingException,
            ParserConfigurationException, SAXException, IOException {

        if (log.isDebugEnabled() && IdentityUtil.isTokenLoggable(IdentityConstants.IdentityTokens.XACML_REQUEST)) {
            log.debug("XACML Request : " + xacmlRequest);
        }

        String xacmlResponse;
        ResponseCtx responseCtx;

        if ((xacmlResponse = (String) getFromCache(xacmlRequest, false)) != null) {
            if (log.isDebugEnabled() && IdentityUtil.isTokenLoggable(IdentityConstants.IdentityTokens.XACML_RESPONSE)) {
                log.debug("XACML Response : " + xacmlResponse);
            }

            DocumentBuilderFactory documentBuilderFactory = IdentityUtil.getSecuredDocumentBuilderFactory();
            Element node = documentBuilderFactory.newDocumentBuilder().parse
                    (new ByteArrayInputStream(xacmlResponse.getBytes())).getDocumentElement();


            return (ResponseCtx.getInstance(node));

        }

        Map<PIPExtension, Properties> extensions = EntitlementServiceComponent.getEntitlementConfig()
                .getExtensions();

        if (extensions != null && !extensions.isEmpty()) {
            PolicyRequestBuilder policyRequestBuilder = new PolicyRequestBuilder();
            Element xacmlRequestElement = policyRequestBuilder.getXacmlRequest(xacmlRequest);
            AbstractRequestCtx requestCtx = RequestCtxFactory.getFactory().
                    getRequestCtx(xacmlRequestElement);
            Set<PIPExtension> pipExtensions = extensions.keySet();
            for (PIPExtension pipExtension : pipExtensions) {
                pipExtension.update(requestCtx);
            }
            responseCtx = pdp.evaluate(requestCtx);
        } else {
            responseCtx = pdp.evaluateReturnResponseCtx(xacmlRequest);
        }

        xacmlResponse = responseCtx.encode();

        addToCache(xacmlRequest, xacmlResponse, false);

        if (log.isDebugEnabled() && IdentityUtil.isTokenLoggable(IdentityConstants.IdentityTokens.XACML_RESPONSE)) {
            log.debug("XACML Response : " + xacmlResponse);
        }

        return responseCtx;

    }

    /**
     * Evaluates XACML request directly. This is used by advance search module.
     * Therefore caching and logging has not be implemented for this
     *
     * @param requestCtx Balana Object model for request
     * @return ResponseCtx  Balana Object model for response
     */
    public ResponseCtx evaluateByContext(AbstractRequestCtx requestCtx) {
        return pdp.evaluate(requestCtx);
    }

    /**
     * Evaluates the given XACML request and returns the Response
     *
     * @param requestCtx Balana Object model for request
     * @param xacmlRequest Balana Object model for request
     * @return ResponseCtx  Balana Object model for response
     */
    public ResponseCtx evaluate(AbstractRequestCtx requestCtx, String xacmlRequest) {

        if (log.isDebugEnabled() && IdentityUtil.isTokenLoggable(IdentityConstants.IdentityTokens.XACML_REQUEST)) {
            log.debug("XACML Request : " + xacmlRequest);
        }

        ResponseCtx xacmlResponse;

        if ((xacmlResponse = (ResponseCtx) getFromCache(xacmlRequest, false)) != null) {
            if (log.isDebugEnabled() && IdentityUtil.isTokenLoggable(IdentityConstants.IdentityTokens.XACML_RESPONSE)) {
                log.debug("XACML Response : " + xacmlResponse);
            }
            return xacmlResponse;
        }

        xacmlResponse = pdp.evaluate(requestCtx);

        addToCache(xacmlRequest, xacmlResponse, false);

        if (log.isDebugEnabled() && IdentityUtil.isTokenLoggable(IdentityConstants.IdentityTokens.XACML_RESPONSE)) {
            log.debug("XACML Response : " + xacmlResponse);
        }
        return xacmlResponse;
    }

    /**
     * Evaluates the given XACML request and returns the Response that the EntitlementEngine will
     * hand back to the PEP. Here PEP does not need construct the XACML request before sending it to the
     * EntitlementEngine. Just can send the single attribute value. But here default attribute ids and data types
     * are used
     *
     * @param subject     subject
     * @param resource    resource
     * @param action      action
     * @param environment environment
     * @return XACML request as String object
     * @throws Exception throws, if fails
     */
    public String evaluate(String subject, String resource, String action, String[] environment)
            throws Exception {

        String environmentValue = null;
        if (environment != null && environment.length > 0) {
            environmentValue = environment[0];
        }
        String response;
        String request = (subject != null ? subject : "") + (resource != null ? resource : "") +
                (action != null ? action : "") + (environmentValue != null ? environmentValue : "");

        if ((response = (String) getFromCache(request, true)) != null) {
            if (log.isDebugEnabled() && IdentityUtil.isTokenLoggable(IdentityConstants.IdentityTokens.XACML_REQUEST)) {
                log.debug("XACML Request : " + EntitlementUtil.
                        createSimpleXACMLRequest(subject, resource, action, environmentValue));
            }
            if (log.isDebugEnabled() && IdentityUtil.isTokenLoggable(IdentityConstants.IdentityTokens.XACML_RESPONSE)) {
                log.debug("XACML Response : " + response);
            }
            return response;
        }

        String requestAsString = EntitlementUtil.createSimpleXACMLRequest(subject, resource, action, environmentValue);

        if (log.isDebugEnabled() && IdentityUtil.isTokenLoggable(IdentityConstants.IdentityTokens.XACML_REQUEST)) {
            log.debug("XACML Request : " + requestAsString);
        }

        response = pdp.evaluate(requestAsString);

        addToCache(request, response, true);

        if (log.isDebugEnabled() && IdentityUtil.isTokenLoggable(IdentityConstants.IdentityTokens.XACML_RESPONSE)) {
            log.debug("XACML Response : " + response);
        }

        return response;
    }


    /**
     * This method is returns the registry based policy finder for current tenant
     *
     * @return RegistryBasedPolicyFinder
     */
    public PolicyFinder getPapPolicyFinder() {
        return papPolicyFinder;
    }


    /**
     * This method returns the carbon based attribute finder for the current tenant
     *
     * @return CarbonAttributeFinder
     */
    public CarbonAttributeFinder getCarbonAttributeFinder() {
        return carbonAttributeFinder;
    }

    /**
     * This method returns the carbon based resource finder for the current tenant
     *
     * @return CarbonResourceFinder
     */
    public CarbonResourceFinder getCarbonResourceFinder() {
        return carbonResourceFinder;
    }

    /**
     * This method returns the carbon based policy finder for the current tenant
     *
     * @return CarbonPolicyFinder
     */
    public PolicyFinder getCarbonPolicyFinder() {
        return carbonPolicyFinder;
    }

    /**
     * get entry from decision caching
     *
     * @param request     XACML request as String
     * @param simpleCache whether using simple cache or not
     * @return XACML response as String
     */
    private Object getFromCache(String request, boolean simpleCache) {

        if (pdpDecisionCacheEnable) {

            String tenantRequest = tenantId + "+" + request;
            Object decision;


            //There is no any local cache hereafter and always get from distribute cache if there.
            /*if (DecisionInvalidationCache.getInstance().isInvalidate()) {
                decisionCache.clearCache();
                simpleDecisionCache.clearCache();
            }*/

            // Check whether the policy cache is invalidated, if so clear the decision cache.
            if (EntitlementEngine.getInstance().getPolicyCache().isInvalidate()) {
                if (log.isDebugEnabled()) {
                    log.debug("Policy Cache is invalidated. Clearing the decision cache.");
                }
                decisionCache.clear();
                simpleDecisionCache.clear();
                return null;
            }

            if (simpleCache) {
                decision = simpleDecisionCache.getFromCache(tenantRequest);
            } else {
                decision = decisionCache.getFromCache(tenantRequest);
            }
            return decision;
        }

        if (log.isDebugEnabled()) {
            log.debug("PDP Decision Caching is disabled");
        }
        return null;
    }

    /**
     * put entry in to cache
     *
     * @param request     XACML request as String
     * @param response    XACML response as String
     * @param simpleCache whether using simple cache or not
     */
    private void addToCache(String request, Object response, boolean simpleCache) {
        if (pdpDecisionCacheEnable) {
            String tenantRequest = tenantId + "+" + request;
            if (simpleCache) {
                simpleDecisionCache.addToCache(tenantRequest, response);
            } else {
                decisionCache.addToCache(tenantRequest, response);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("PDP Decision Caching is disabled");
            }
        }
    }

    /**
     * Helper method to init engine
     */
    private void setUpAttributeFinders() {

        // Creates carbon attribute finder instance  and init it
        carbonAttributeFinder = new CarbonAttributeFinder(tenantId);
        carbonAttributeFinder.init();

        // Now setup attribute finder modules for the current date/time and
        // AttributeSelectors (selectors are optional, but this project does
        // support a basic implementation)
        CurrentEnvModule envAttributeModule = new CurrentEnvModule();
        SelectorModule selectorAttributeModule = new SelectorModule();

        attributeModules.add(carbonAttributeFinder);
        attributeModules.add(envAttributeModule);
        attributeModules.add(selectorAttributeModule);

        for (AttributeFinderModule module : balana.getPdpConfig().getAttributeFinder().getModules()) {
            if (module instanceof CurrentEnvModule || module instanceof SelectorModule) {
                continue;
            }
            attributeModules.add(module);
        }
    }

    /**
     * Helper method to init engine
     */
    private void setUpResourceFinders() {

        carbonResourceFinder = new CarbonResourceFinder(tenantId);
        carbonResourceFinder.init();
        resourceModules.add(carbonResourceFinder);

        for (ResourceFinderModule module : balana.getPdpConfig().getResourceFinder().getModules()) {
            resourceModules.add(module);
        }
    }

    /**
     * Returns instance of policy search
     *
     * @return <code>PolicySearch</code>
     */
    public PolicySearch getPolicySearch() {
        return policySearch;
    }

    private void setUPPolicyFinder() {

        carbonPolicyFinder = new PolicyFinder();
        Set<PolicyFinderModule> policyModules = new HashSet<PolicyFinderModule>();
        CarbonPolicyFinder tmpCarbonPolicyFinder = new CarbonPolicyFinder();
        policyModules.add(tmpCarbonPolicyFinder);
        carbonPolicyFinder.setModules(policyModules);
        carbonPolicyFinder.init();

    }

    /**
     * Check reset cache state
     */
    public void resetCacheInvalidateState() {

        if (policyCache != null) {
            policyCache.resetCacheInvalidateState();
        } else {
            log.error("Policy cache is null - Unable to reset cache invalidate state.");
        }
    }

    /**
     * Checking the policy cache status before cache invalidation
     */
    public void invalidatePolicyCache() {

        if (policyCache != null) {
            policyCache.invalidateCache();
        } else {
            log.error("Policy cache is null - Unable to invalidate cache.");
        }
    }

}