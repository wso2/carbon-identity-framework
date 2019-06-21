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
import org.wso2.balana.XACMLConstants;
import org.wso2.balana.attr.AttributeValue;
import org.wso2.balana.attr.BagAttribute;
import org.wso2.balana.attr.StringAttribute;
import org.wso2.balana.cond.EvaluationResult;
import org.wso2.balana.ctx.EvaluationCtx;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.entitlement.PDPConstants;
import org.wso2.carbon.identity.entitlement.cache.PIPAbstractAttributeCache;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;

import java.net.URI;
import java.util.Properties;
import java.util.Set;

/**
 * Abstract implementation of the PIPAttributeFinder.
 */
public abstract class AbstractPIPAttributeFinder implements PIPAttributeFinder {

    private static Log log = LogFactory.getLog(AbstractPIPAttributeFinder.class);
    protected int tenantId;
    private PIPAbstractAttributeCache abstractAttributeFinderCache = null;
    private boolean isAbstractAttributeCachingEnabled = false;

    /**
     * This is the overloaded simplify version of the getAttributeValues() method. Any one who extends the
     * <code>AbstractPIPAttributeFinder</code> can implement this method and get use of the default
     * implementation of the getAttributeValues() method which has been implemented within
     * <code>AbstractPIPAttributeFinder</code> class
     *
     * @param subject     Name of the subject the returned attributes should apply to.
     * @param resource    The name of the resource the subject is trying to access.
     * @param action      The name of the action the subject is trying to execute on resource
     * @param environment The name of the environment the subject is trying to access the resource
     * @param attributeId The unique id of the required attribute.
     * @param issuer      The attribute issuer.
     * @return Returns a <code>Set</code> of <code>String</code>s that represent the attribute
     * values.
     * @throws Exception throws if fails
     */
    public abstract Set<String> getAttributeValues(String subject, String resource, String action,
                                                   String environment, String attributeId, String issuer)
            throws Exception;


    @Override
    public Set<String> getAttributeValues(URI attributeType, URI attributeId, URI category,
                                          String issuer, EvaluationCtx evaluationCtx) throws Exception {

        EvaluationResult subject;
        String subjectId = null;
        EvaluationResult resource;
        String resourceId = null;
        EvaluationResult action;
        String actionId = null;
        EvaluationResult environment;
        String environmentId = null;
        Set<String> attributeValues = null;

        tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        subject = evaluationCtx.getAttribute(new URI(StringAttribute.identifier), new URI(
                PDPConstants.SUBJECT_ID_DEFAULT), issuer, new URI(XACMLConstants.SUBJECT_CATEGORY));
        if (subject != null && subject.getAttributeValue() != null &&
            subject.getAttributeValue().isBag()) {
            BagAttribute bagAttribute = (BagAttribute) subject.getAttributeValue();
            if (bagAttribute.size() > 0) {
                subjectId = ((AttributeValue) bagAttribute.iterator().next()).encode();
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Finding attributes for the subject %1$s",
                                            subjectId));
                }
            }
        }

        resource = evaluationCtx.getAttribute(new URI(StringAttribute.identifier), new URI(
                PDPConstants.RESOURCE_ID_DEFAULT), issuer, new URI(XACMLConstants.RESOURCE_CATEGORY));
        if (resource != null && resource.getAttributeValue() != null &&
            resource.getAttributeValue().isBag()) {
            BagAttribute bagAttribute = (BagAttribute) resource.getAttributeValue();
            if (bagAttribute.size() > 0) {
                resourceId = ((AttributeValue) bagAttribute.iterator().next()).encode();
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Finding attributes for the resource %1$s",
                                            resourceId));
                }
            }
        }

        action = evaluationCtx.getAttribute(new URI(StringAttribute.identifier), new URI(
                PDPConstants.ACTION_ID_DEFAULT), issuer, new URI(XACMLConstants.ACTION_CATEGORY));
        if (action != null && action.getAttributeValue() != null &&
            action.getAttributeValue().isBag()) {
            BagAttribute bagAttribute = (BagAttribute) action.getAttributeValue();
            if (bagAttribute.size() > 0) {
                actionId = ((AttributeValue) bagAttribute.iterator().next()).encode();
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Finding attributes for the action %1$s",
                                            actionId));
                }
            }
        }

        environment = evaluationCtx.getAttribute(new URI(StringAttribute.identifier), new URI(
                PDPConstants.ENVIRONMENT_ID_DEFAULT), issuer, new URI(XACMLConstants.ENT_CATEGORY));
        if (environment != null && environment.getAttributeValue() != null &&
            environment.getAttributeValue().isBag()) {
            BagAttribute bagAttribute = (BagAttribute) environment.getAttributeValue();
            if (bagAttribute.size() > 0) {
                environmentId = ((AttributeValue) bagAttribute.iterator().next()).encode();
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Finding attributes for the environment %1$s",
                                            environmentId));
                }
            }
        }

        String key = null;

        if (isAbstractAttributeCachingEnabled) {
            key = (subjectId != null ? subjectId : "") + (resourceId != null ? resourceId : "") +
                  (environmentId != null ? environmentId : "") + (attributeId != null ? attributeId : "") +
                  (issuer != null ? issuer : "") +
                  (actionId != null ? actionId : "");

            attributeValues = abstractAttributeFinderCache.getFromCache(tenantId, key);
            if (log.isDebugEnabled()) {
                log.debug("Retrieving attributes from cache, tenantId: " + tenantId + ", key: " + key);
            }
        }

        if (attributeValues == null) {
            if (log.isDebugEnabled()) {
                log.debug("Carbon Attribute Cache Miss");
            }
            attributeValues = getAttributeValues(subjectId, resourceId, actionId, environmentId,
                                                 attributeId.toString(), issuer);
            if (isAbstractAttributeCachingEnabled && key != null) {
                if (attributeValues != null && !attributeValues.isEmpty()) {
                    abstractAttributeFinderCache.addToCache(tenantId, key, attributeValues);
                }
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Carbon Attribute Cache Hit");
            }
        }

        return attributeValues;

    }

    @Override
    public boolean overrideDefaultCache() {

        if (abstractAttributeFinderCache == null) {
            Properties properties = EntitlementServiceComponent.getEntitlementConfig().getEngineProperties();
            if ("true".equals(properties.getProperty(PDPConstants.ATTRIBUTE_CACHING))) {
                int attributeCachingInterval = -1;
                String cacheInterval = properties.getProperty(PDPConstants.ATTRIBUTE_CACHING_INTERVAL);
                if (cacheInterval != null) {
                    try {
                        attributeCachingInterval = Integer.parseInt(cacheInterval.trim());
                    } catch (Exception e) {
                        //ignore
                    }
                }
                abstractAttributeFinderCache = new PIPAbstractAttributeCache(attributeCachingInterval);
                isAbstractAttributeCachingEnabled = true;
            }
        } else {
            return true;
        }

        return isAbstractAttributeCachingEnabled;
    }

    @Override
    public void clearCache() {
        if (abstractAttributeFinderCache != null) {
            abstractAttributeFinderCache.clearCache();
        }
    }

    @Override
    public void clearCache(String[] attributeId) {
    }

}
