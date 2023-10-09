/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.application.tag.mgt.dao.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.model.ApplicationTag;
import org.wso2.carbon.identity.application.common.model.ApplicationTagsItem;
import org.wso2.carbon.identity.application.common.model.ApplicationTagsListItem;
import org.wso2.carbon.identity.application.tag.mgt.ApplicationTagMgtException;
import org.wso2.carbon.identity.application.tag.mgt.cache.ApplicationTagCacheById;
import org.wso2.carbon.identity.application.tag.mgt.cache.ApplicationTagCacheEntry;
import org.wso2.carbon.identity.application.tag.mgt.cache.ApplicationTagIdCacheKey;
import org.wso2.carbon.identity.application.tag.mgt.dao.ApplicationTagDAO;

import java.util.List;

/**
 * This class implements the {@link ApplicationTagDAO} interface.
 */
public class CacheBackedApplicationTagDAO implements ApplicationTagDAO {

    private static final Log LOG = LogFactory.getLog(CacheBackedApplicationTagDAO.class);
    private final ApplicationTagCacheById applicationTagCacheById;
    private final ApplicationTagDAO applicationTagDAO;

    public CacheBackedApplicationTagDAO(ApplicationTagDAO applicationTagDAO) {
        this.applicationTagDAO = applicationTagDAO;
        applicationTagCacheById = ApplicationTagCacheById.getInstance();
    }

    @Override
    public ApplicationTagsItem createApplicationTag(ApplicationTag applicationTag, Integer tenantID)
            throws ApplicationTagMgtException {

        return applicationTagDAO.createApplicationTag(applicationTag, tenantID);
    }

    @Override
    public List<ApplicationTagsListItem> getAllApplicationTags(Integer tenantID, Integer offset, Integer limit,
                                                               String filter) throws ApplicationTagMgtException {

        return applicationTagDAO.getAllApplicationTags(tenantID, offset, limit, filter);
    }

    @Override
    public ApplicationTagsListItem getApplicationTagById(String applicationTagId, Integer tenantID)
            throws ApplicationTagMgtException {

        ApplicationTagIdCacheKey cacheKey = new ApplicationTagIdCacheKey(applicationTagId);
        ApplicationTagCacheEntry entry = applicationTagCacheById.getValueFromCache(cacheKey, tenantID);

        if (entry != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cache entry found for Application Tag " + applicationTagId);
            }
            return entry.getApplicationTag();
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cache entry not found for Application Tag " + applicationTagId +
                        ". Fetching entry from DB");
            }
        }

        ApplicationTagsListItem applicationTag = applicationTagDAO.getApplicationTagById(applicationTagId, tenantID);

        if (applicationTag != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Entry fetched from DB for Application Tag " + applicationTagId + ". Updating cache");
            }
            applicationTagCacheById.addToCache(cacheKey, new ApplicationTagCacheEntry(applicationTag), tenantID);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Entry for Application Tag " + applicationTagId + " not found in cache or DB");
            }
        }

        return applicationTag;
    }

    @Override
    public void deleteApplicationTagById(String applicationTagId, Integer tenantID)
            throws ApplicationTagMgtException {

        clearApplicationTagCache(applicationTagId, tenantID);
        applicationTagDAO.deleteApplicationTagById(applicationTagId, tenantID);
    }

    @Override
    public void updateApplicationTag(ApplicationTag applicationTagPatch, String applicationTagId,
                                     Integer tenantID) throws ApplicationTagMgtException {

        clearApplicationTagCache(applicationTagId, tenantID);
        applicationTagDAO.updateApplicationTag(applicationTagPatch, applicationTagId, tenantID);
    }

    @Override
    public int getCountOfApplicationTags(String filter, Integer tenantID) throws ApplicationTagMgtException {

        return applicationTagDAO.getCountOfApplicationTags(filter, tenantID);
    }

    private void clearApplicationTagCache(String applicationTagId, Integer tenantID) throws
            ApplicationTagMgtException {

        // clearing cache entries related to the Application Tag.
        ApplicationTagsListItem applicationTag = null;
        if (StringUtils.isNotBlank(applicationTagId)) {
            applicationTag = this.getApplicationTagById(applicationTagId, tenantID);
        }

        if (applicationTag != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Removing entry for Application Tag " + applicationTag.getName() + " of tenantDomain:"
                        + tenantID + " from cache.");
            }

            ApplicationTagIdCacheKey applicationTagIdCacheKey = new ApplicationTagIdCacheKey(applicationTagId);
            applicationTagCacheById.clearCacheEntry(applicationTagIdCacheKey, tenantID);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Entry for Application Tag " + applicationTagId + " not found in cache or DB");
            }
        }
    }
}
