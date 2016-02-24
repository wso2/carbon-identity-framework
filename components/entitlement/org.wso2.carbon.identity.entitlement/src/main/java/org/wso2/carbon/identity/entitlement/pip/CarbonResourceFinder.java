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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Node;
import org.wso2.balana.attr.AttributeValue;
import org.wso2.balana.ctx.EvaluationCtx;
import org.wso2.balana.finder.ResourceFinderModule;
import org.wso2.balana.finder.ResourceFinderResult;
import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.EntitlementUtil;
import org.wso2.carbon.identity.entitlement.PDPConstants;
import org.wso2.carbon.identity.entitlement.cache.EntitlementBaseCache;
import org.wso2.carbon.identity.entitlement.cache.IdentityCacheEntry;
import org.wso2.carbon.identity.entitlement.cache.IdentityCacheKey;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * CarbonResourceFinder implements the ResourceFinderModule in the sum-xacml. This class would find
 * children and descendant resources in the Registry resources which is running on the WSO2 Identity
 * Server
 */

public class CarbonResourceFinder extends ResourceFinderModule {

    private static Log log = LogFactory.getLog(CarbonResourceFinder.class);
    boolean isResourceCachingEnabled = false;
    private int tenantId;
    private Set<PIPResourceFinder> resourceFinders = new HashSet<PIPResourceFinder>();
    //private Cache<IdentityCacheKey,IdentityCacheEntry> resourceCache = null;
    private EntitlementBaseCache<IdentityCacheKey, IdentityCacheEntry> resourceCache = null;

    public CarbonResourceFinder(int tenantId) {
        this.tenantId = tenantId;
    }

    /**
     * initializes the Carbon resource finder by listing the registered resource finders
     */
    public void init() {
        Map<PIPResourceFinder, Properties> resourceConfigs = EntitlementServiceComponent.getEntitlementConfig()
                .getResourceFinders();

        if (resourceConfigs != null && !resourceConfigs.isEmpty()) {
            resourceFinders = resourceConfigs.keySet();
        }
        Properties properties = EntitlementServiceComponent.getEntitlementConfig().getEngineProperties();
        if ("true".equals(properties.getProperty(PDPConstants.RESOURCE_CACHING))) {
            resourceCache = EntitlementUtil
                    .getCommonCache(PDPConstants.PIP_RESOURCE_CACHE);
            isResourceCachingEnabled = true;
        }
    }

    @Override
    public boolean isChildSupported() {
        return true;
    }

    @Override
    public boolean isDescendantSupported() {
        return true;
    }

    @Override
    public ResourceFinderResult findDescendantResources(AttributeValue parentResourceId,
                                                        EvaluationCtx context) {

        ResourceFinderResult resourceFinderResult = null;
        Set<AttributeValue> resources = null;
        String dataType = parentResourceId.getType().toString();

        for (PIPResourceFinder finder : resourceFinders) {
            try {
                Set<String> resourceNames = null;
                if (isResourceCachingEnabled && !finder.overrideDefaultCache()) {
                    IdentityCacheKey cacheKey = null;
                    String key = PDPConstants.RESOURCE_DESCENDANTS + parentResourceId.encode() +
                            domToString(context.getRequestRoot());
                    cacheKey = new IdentityCacheKey(tenantId, key);
                    IdentityCacheEntry cacheEntry = (IdentityCacheEntry) resourceCache.getValueFromCache(cacheKey);
                    if (cacheEntry != null) {
                        String[] values = cacheEntry.getCacheEntryArray();
                        resourceNames = new HashSet<String>(Arrays.asList(values));
                        if (log.isDebugEnabled()) {
                            log.debug("Carbon Resource Cache Hit");
                        }
                    }

                    if (resourceNames != null) {
                        resourceNames = finder.findDescendantResources(parentResourceId.encode(), context);
                        if (log.isDebugEnabled()) {
                            log.debug("Carbon Resource Cache Miss");
                        }
                        cacheEntry = new IdentityCacheEntry(resourceNames.toArray(new String[resourceNames.size()]));
                        resourceCache.addToCache(cacheKey, cacheEntry);
                    }
                } else {
                    resourceNames = finder.findDescendantResources(parentResourceId.encode(), context);
                }

                if (resourceNames != null && !resourceNames.isEmpty()) {
                    resources = new HashSet<AttributeValue>();
                    for (String resourceName : resourceNames) {
                        resources.add(EntitlementUtil.getAttributeValue(resourceName, dataType));
                    }
                }
            } catch (EntitlementException e) {
                log.error("Error while finding descendant resources", e);
            } catch (TransformerException e) {
                log.error("Error while finding descendant resources", e);
            } catch (Exception e) {
                log.error("Error while finding descendant resources", e);
            }
        }

        if (resources != null) {
            resourceFinderResult = new ResourceFinderResult(resources);
        } else {
            resourceFinderResult = new ResourceFinderResult();
        }

        return resourceFinderResult;
    }

    @Override
    public ResourceFinderResult findChildResources(AttributeValue parentResourceId,
                                                   EvaluationCtx context) {
        ResourceFinderResult resourceFinderResult = null;
        Set<AttributeValue> resources = null;
        String dataType = parentResourceId.getType().toString();

        for (PIPResourceFinder finder : resourceFinders) {
            try {
                Set<String> resourceNames = null;
                if (isResourceCachingEnabled && !finder.overrideDefaultCache()) {
                    IdentityCacheKey cacheKey = null;
                    String key = PDPConstants.RESOURCE_CHILDREN + parentResourceId.encode() +
                            domToString(context.getRequestRoot());
                    cacheKey = new IdentityCacheKey(tenantId, key);
                    IdentityCacheEntry cacheEntry = (IdentityCacheEntry) resourceCache.getValueFromCache(cacheKey);
                    if (cacheEntry != null) {
                        String cacheEntryString = cacheEntry.getCacheEntry();
                        String[] attributes = cacheEntryString.split(PDPConstants.ATTRIBUTE_SEPARATOR);
                        if (attributes != null && attributes.length > 0) {
                            List<String> list = Arrays.asList(attributes);
                            resourceNames = new HashSet<String>(list);
                        }
                        if (log.isDebugEnabled()) {
                            log.debug("Carbon Resource Cache Hit");
                        }
                    } else {
                        resourceNames = finder.findChildResources(parentResourceId.encode(), context);
                        if (log.isDebugEnabled()) {
                            log.debug("Carbon Resource Cache Miss");
                        }
                        String cacheEntryString = "";
                        if (resourceNames != null && resourceNames.size() > 0) {
                            for (String attribute : resourceNames) {
                                if (cacheEntryString.equals("")) {
                                    cacheEntryString = attribute;
                                } else {
                                    cacheEntryString = cacheEntryString + PDPConstants.ATTRIBUTE_SEPARATOR + attribute;
                                }
                            }
                        }
                        cacheEntry = new IdentityCacheEntry(cacheEntryString);
                        resourceCache.addToCache(cacheKey, cacheEntry);
                    }
                } else {
                    resourceNames = finder.findChildResources(parentResourceId.encode(), context);
                }

                if (resourceNames != null && !resourceNames.isEmpty()) {
                    resources = new HashSet<AttributeValue>();
                    for (String resourceName : resourceNames) {
                        resources.add(EntitlementUtil.getAttributeValue(resourceName, dataType));
                    }
                }
            } catch (EntitlementException e) {
                log.error("Error while finding child resources", e);
            } catch (TransformerException e) {
                log.error("Error while finding child resources", e);
            } catch (Exception e) {
                log.error("Error while finding child resources", e);
            }
        }

        if (resources != null) {
            resourceFinderResult = new ResourceFinderResult(resources);
        } else {
            resourceFinderResult = new ResourceFinderResult();
        }

        return resourceFinderResult;
    }

    /**
     * Disables resource Caches
     */
    public void disableAttributeCache() {
        resourceCache = null;
    }

    /**
     * Enables resource caches
     */
    public void enableAttributeCache() {
        resourceCache = EntitlementUtil
                .getCommonCache(PDPConstants.PIP_RESOURCE_CACHE);
    }

    /**
     * Clears attribute cache
     */
    public void clearAttributeCache() {
        if (resourceCache != null) {
            resourceCache.clear();
            if (log.isDebugEnabled()) {
                log.debug("Resource cache is cleared for tenant " + tenantId);
            }
        }
    }

    /**
     * Converts DOM object to String. This is a helper method for creating cache key
     *
     * @param node Node value
     * @return String Object
     * @throws javax.xml.transform.TransformerException Exception throws if fails
     */
    private String domToString(Node node) throws TransformerException {
        TransformerFactory transFactory = TransformerFactory.newInstance();
        Transformer transformer = transFactory.newTransformer();
        StringWriter buffer = new StringWriter();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.transform(new DOMSource(node),
                new StreamResult(buffer));
        return buffer.toString();
    }
}
