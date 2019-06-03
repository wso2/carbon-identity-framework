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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.balana.ParsingException;
import org.wso2.balana.attr.AttributeValue;
import org.wso2.balana.attr.BagAttribute;
import org.wso2.balana.cond.EvaluationResult;
import org.wso2.balana.ctx.Attribute;
import org.wso2.balana.ctx.EvaluationCtx;
import org.wso2.balana.ctx.Status;
import org.wso2.balana.finder.AttributeFinderModule;
import org.wso2.balana.xacml3.Attributes;
import org.wso2.carbon.identity.entitlement.EntitlementUtil;
import org.wso2.carbon.identity.entitlement.PDPConstants;
import org.wso2.carbon.identity.entitlement.cache.PIPAttributeCache;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;
import org.wso2.carbon.identity.entitlement.pdp.EntitlementEngine;

import javax.xml.transform.TransformerException;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * CarbonAttributeFinder registers with sun-xacml engine as an AttributeFinderModule and delegate
 * functionality to the attribute handlers registered with it self.
 * <p/>
 * Whenever the XACML engine finds a missing attribute in the XACML request - it will call the
 * findAttribute() method of this class.
 */
public class CarbonAttributeFinder extends AttributeFinderModule {

    private Map<String, List<PIPAttributeFinder>> attrFinders = new HashMap<String, List<PIPAttributeFinder>>();
    private static Log log = LogFactory.getLog(CarbonAttributeFinder.class);
    private PIPAttributeCache attributeFinderCache = null;
    protected int tenantId;

    public CarbonAttributeFinder(int tenantId) {
        this.tenantId = tenantId;
    }

    /**
     * Registers PIP attribute handlers with the PDP against their supported attributes. This PIP
     * attribute handlers are picked from pip-config.xml file - which should be inside
     * [CARBON_HOME]\repository\conf.
     */
    public void init() {
        Map<PIPAttributeFinder, Properties> designators = EntitlementServiceComponent.getEntitlementConfig()
                .getDesignators();
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
            attributeFinderCache = new PIPAttributeCache(attributeCachingInterval);
        }
        // clear decision cache
        if (designators != null && !designators.isEmpty()) {
            Set<PIPAttributeFinder> pipAttributeFinders = designators.keySet();
            for (Iterator iterator = pipAttributeFinders.iterator(); iterator.hasNext(); ) {
                PIPAttributeFinder pipAttributeFinder = (PIPAttributeFinder) iterator.next();
                Set<String> attrs = pipAttributeFinder.getSupportedAttributes();
                if (attrs != null) {
                    for (Iterator attrsIter = attrs.iterator(); attrsIter.hasNext(); ) {
                        String attr = (String) attrsIter.next();
                        if (attrFinders.containsKey(attr)) {
                            List<PIPAttributeFinder> finders = attrFinders.get(attr);
                            if (!finders.contains(pipAttributeFinder)) {
                                finders.add(pipAttributeFinder);
                                if (log.isDebugEnabled()) {
                                    log.debug(String
                                                      .format("PIP attribute handler %1$s registered for the " +
                                                              "supported attribute %2$s",
                                                              pipAttributeFinder.getClass(), attr));
                                }
                            }
                        } else {
                            List<PIPAttributeFinder> finders = new ArrayList<PIPAttributeFinder>();
                            finders.add(pipAttributeFinder);
                            attrFinders.put(attr, finders);
                            if (log.isDebugEnabled()) {
                                log.debug(String
                                                  .format("PIP attribute handler %1$s registered for the supported " +
                                                          "attribute %2$s",
                                                          pipAttributeFinder.getClass(), attr));
                            }
                        }
                    }
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.balana.finder.AttributeFinderModule#findAttribute(java.net.URI, java.net.URI,
     * java.net.URI, java.net.URI, org.wso2.balana.EvaluationCtx, int)
     */
    public EvaluationResult findAttribute(URI attributeType, URI attributeId, String issuer,
                                          URI category, EvaluationCtx context) {

        List<AttributeValue> attrBag = new ArrayList<AttributeValue>();
        // Get the list of attribute finders who are registered with this particular attribute.


        List<PIPAttributeFinder> finders = null;

        if (StringUtils.isNotBlank(category.toString())) {
            finders = attrFinders.get(category.toString());
            if (log.isDebugEnabled()) {
                log.debug("No attribute designators defined for the category " + category.toString());
            }
        }

        if (CollectionUtils.isEmpty(finders)) {
            finders = attrFinders.get(attributeId.toString());
            if (CollectionUtils.isEmpty(finders)) {
                if (log.isDebugEnabled()) {
                    log.debug("No attribute designators defined for the attribute " + attributeId.toString());
                }
                return new EvaluationResult(BagAttribute.createEmptyBag(attributeType));

            }
        }

        try {

            for (Iterator iterator = finders.iterator(); iterator.hasNext(); ) {
                PIPAttributeFinder pipAttributeFinder = (PIPAttributeFinder) iterator.next();
                if (log.isDebugEnabled()) {
                    log.debug(String.format(
                            "Finding attributes with the PIP attribute handler %1$s",
                            pipAttributeFinder.getClass()));
                }

                Set<String> attrs = null;
                String key = null;

                if (attributeFinderCache != null && !pipAttributeFinder.overrideDefaultCache()) {

                    key = "[" + attributeType.toString() + "][" + attributeId.toString() + "][" + category.toString() +
                            "][" + encodeContext(context) + "]";

                    if (issuer != null) {
                        key += "[" + issuer + "]";
                    }

                    if (key != null) {
                        if (log.isDebugEnabled()) {
                            log.debug("Retrieving attributes from cache, tenantId: " + tenantId + ", key: " + key);
                        }
                        attrs = attributeFinderCache.getFromCache(tenantId, key);
                    }
                }

                if (attrs == null) {
                    attrs = pipAttributeFinder.getAttributeValues(attributeType, attributeId, category,
                                                                  issuer, context);
                    if (attributeFinderCache != null && key != null
                        && !pipAttributeFinder.overrideDefaultCache()) {
                        attributeFinderCache.addToCache(tenantId, key, attrs);
                    }
                }

                if (attrs != null) {
                    for (Iterator iterAttr = attrs.iterator(); iterAttr.hasNext(); ) {
                        final String attr = (String) iterAttr.next();
                        AttributeValue attribute = EntitlementUtil.
                                getAttributeValue(attr, attributeType.toString());
                        attrBag.add(attribute);
                        if (log.isDebugEnabled()) {
                            log.debug("Attribute added to the attributeBag: \'" + attr + "\'");
                        }
                    }
                }
            }
        } catch (ParsingException e) {
            log.error("Error while parsing attribute values from EvaluationCtx : " + e);
            ArrayList<String> code = new ArrayList<String>();
            code.add(Status.STATUS_MISSING_ATTRIBUTE);
            Status status = new Status(code,
                                       "Error while parsing attribute values from EvaluationCtx : " + e.getMessage());
            return new EvaluationResult(status);
        } catch (ParseException e) {
            e.printStackTrace();
            log.error("Error while parsing attribute values from EvaluationCtx : " + e);
            ArrayList<String> code = new ArrayList<String>();
            code.add(Status.STATUS_MISSING_ATTRIBUTE);
            Status status = new Status(code,
                                       "Error while parsing attribute values from EvaluationCtx : " + e.getMessage());
            return new EvaluationResult(status);
        } catch (URISyntaxException e) {
            log.error("Error while parsing attribute values from EvaluationCtx : " + e);
            ArrayList<String> code = new ArrayList<String>();
            code.add(Status.STATUS_MISSING_ATTRIBUTE);
            Status status = new Status(code,
                                       "Error while parsing attribute values from EvaluationCtx :" + e.getMessage());
            return new EvaluationResult(status);
        } catch (Exception e) {
            log.error("Error while retrieving attribute values from PIP  attribute finder : " + e);
            ArrayList<String> code = new ArrayList<String>();
            code.add(Status.STATUS_MISSING_ATTRIBUTE);
            Status status = new Status(code, "Error while retrieving attribute values from PIP"
                                             + " attribute finder : " + e.getMessage());
            return new EvaluationResult(status);
        }
        return new EvaluationResult(new BagAttribute(attributeType, attrBag));
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.balana.finder.AttributeFinderModule#isDesignatorSupported()
     */
    public boolean isDesignatorSupported() {
        return true;
    }


    /*
     * (non-Javadoc)
     *
     * @see org.wso2.balana.finder.AttributeFinderModule#getSupportedIds()
     */
    public Set getSupportedIds() {
        return null;
    }

    /**
     * Registers PIP attribute handlers are initialized when the server is start-up. This method can
     * be used to refresh all attribute finders internally. refreshSupportedAttribute() method must be
     * implemented within the PIP attribute finder to perform this operation. Also this uses to find newly
     * defined attributes, attribute caches are would not be cleared.
     *
     * @throws Exception throws then initialization of attribute finders are failed
     */
    private void refreshAttributeFindersForNewAttributeId() throws Exception {
        Map<PIPAttributeFinder, Properties> designators = EntitlementServiceComponent.getEntitlementConfig()
                .getDesignators();
        if (designators != null && !designators.isEmpty()) {
            Set<Map.Entry<PIPAttributeFinder, Properties>> attributeFinders = designators.entrySet();
            for (Map.Entry<PIPAttributeFinder, Properties> attributeFinder : attributeFinders) {
                attributeFinder.getKey().init(attributeFinder.getValue());
            }
            init();
        }
    }


    /**
     * Clears attribute cache
     */
    public void clearAttributeCache() {
        if (attributeFinderCache != null) {
            attributeFinderCache.clearCache();
            // clear decision cache
            EntitlementEngine.getInstance().clearDecisionCache();
        }
    }

    /**
     * Converts DOM object to String. This is a helper method for creating cache key
     *
     * @param evaluationCtx EvaluationCtx
     * @return String Object
     * @throws TransformerException Exception throws if fails
     */
    private String encodeContext(EvaluationCtx evaluationCtx) throws TransformerException {
        OutputStream stream = new ByteArrayOutputStream();
        evaluationCtx.getRequestCtx().encode(stream);
        String rowContext = stream.toString();
        String contextWithAttributeValues = rowContext + "][";

        StringBuilder builder = new StringBuilder();
        for (Attributes attributes : evaluationCtx.getRequestCtx().getAttributesSet()) {
            builder.append("<Attributes ").append(">");
            for (Attribute attribute : attributes.getAttributes()) {
                attribute.encode(builder);
            }
            builder.append("</Attributes>");
        }
        contextWithAttributeValues += builder.toString();

        return contextWithAttributeValues;
    }
}
