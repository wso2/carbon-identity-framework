/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.template.mgt.handler.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.configuration.mgt.core.search.Condition;
import org.wso2.carbon.identity.template.mgt.cache.ConfigStoreBasedTemplateCache;
import org.wso2.carbon.identity.template.mgt.cache.ConfigStoreBasedTemplateCacheEntry;
import org.wso2.carbon.identity.template.mgt.cache.ConfigStoreBasedTemplateCacheKey;
import org.wso2.carbon.identity.template.mgt.exception.TemplateManagementException;
import org.wso2.carbon.identity.template.mgt.handler.TemplateHandler;
import org.wso2.carbon.identity.template.mgt.model.Template;

import java.util.List;

/**
 * Cached config store based template handler for the template management. All config store based template handling
 * happen through this layer to ensure single point of caching.
 */
public class CacheBackedConfigStoreBasedTemplateHandler implements TemplateHandler {

    private static final Log log = LogFactory.getLog(CacheBackedConfigStoreBasedTemplateHandler.class);

    private ConfigStoreBasedTemplateHandler configStoreBasedTemplateHandler = null;
    private ConfigStoreBasedTemplateCache configStoreBasedTemplateCache = null;

    public CacheBackedConfigStoreBasedTemplateHandler(ConfigStoreBasedTemplateHandler configStoreBasedTemplateHandler) {

        this.configStoreBasedTemplateHandler = configStoreBasedTemplateHandler;
        configStoreBasedTemplateCache = ConfigStoreBasedTemplateCache.getInstance();
    }

    /**
     * Add new template into the config store.
     *
     * @param template Template.
     * @return Resource id.
     * @throws TemplateManagementException Template Management Exception.
     */
    public String addTemplate(Template template) throws TemplateManagementException {

        return configStoreBasedTemplateHandler.addTemplate(template);
    }

    /**
     * Get the template for the given id from config store.
     *
     * @param templateId Template id.
     * @return Template for the given id.
     * @throws TemplateManagementException Template Management Exception.
     */
    public Template getTemplateById(String templateId) throws TemplateManagementException {

        ConfigStoreBasedTemplateCacheKey cacheKey = new ConfigStoreBasedTemplateCacheKey(templateId);
        ConfigStoreBasedTemplateCacheEntry entry = configStoreBasedTemplateCache.getValueFromCache(cacheKey);

        if (entry != null) {
            if (log.isDebugEnabled()) {
                log.debug("Cache entry found for Template with id " + templateId);
            }
            Template template = entry.getTemplate();
            return template;
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Cache entry not found for Template with id " + templateId + ". Fetching entry from DB");
            }
        }

        Template template = configStoreBasedTemplateHandler.getTemplateById(templateId);

        if (template != null) {
            if (log.isDebugEnabled()) {
                log.debug("Entry fetched from Config store for Template " + templateId + ". Updating cache");
            }
            configStoreBasedTemplateCache.addToCache(cacheKey, new ConfigStoreBasedTemplateCacheEntry(template));
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Entry for Template with id " + templateId + " not found in cache or config store");
            }
        }

        return template;
    }

    /**
     * Update the template for the given templateId.
     *
     * @param templateId Template id.
     * @param template   Updated template.
     * @throws TemplateManagementException Template Management Exception.
     */
    public void updateTemplateById(String templateId, Template template) throws TemplateManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Removing entry for Template with id " + templateId + " from cache");
        }
        clearTemplateCache(templateId);
        configStoreBasedTemplateHandler.updateTemplateById(templateId, template);
    }

    /**
     * Delete the template for the given templateId.
     *
     * @param templateId Template id.
     * @throws TemplateManagementException Template Management Exception.
     */
    public void deleteTemplateById(String templateId) throws TemplateManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Removing entry for Template with id " + templateId + " from cache");
        }
        clearTemplateCache(templateId);
        configStoreBasedTemplateHandler.deleteTemplateById(templateId);
    }

    /**
     * List the templates according to the filters.
     *
     * @param templateType    Template type.
     * @param limit           Result limit per page.
     * @param offset          Offset value.
     * @param searchCondition Filters.
     * @return List of templates.
     * @throws TemplateManagementException Template Management Exception.
     */
    public List<Template> listTemplates(String templateType, Integer limit, Integer offset, Condition searchCondition)
            throws TemplateManagementException {

        return configStoreBasedTemplateHandler.listTemplates(templateType, limit, offset, searchCondition);
    }

    /**
     * Clearing cache entries related to the template.
     *
     * @param templateId Template id.
     */
    private void clearTemplateCache(String templateId) throws TemplateManagementException {

        Template template = this.getTemplateById(templateId);
        if (template != null) {
            if (log.isDebugEnabled()) {
                log.debug("Removing entry for Template with id " + templateId + " from cache.");
            }
            ConfigStoreBasedTemplateCacheKey cacheKey = new ConfigStoreBasedTemplateCacheKey(templateId);
            configStoreBasedTemplateCache.clearCacheEntry(cacheKey);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Entry for Template with id " + templateId + " not found in cache or DB");
            }
        }
    }
}
