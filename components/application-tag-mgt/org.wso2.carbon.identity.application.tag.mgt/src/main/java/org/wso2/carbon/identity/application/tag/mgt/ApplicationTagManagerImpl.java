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

package org.wso2.carbon.identity.application.tag.mgt;

import org.wso2.carbon.identity.application.common.model.ApplicationTag;
import org.wso2.carbon.identity.application.common.model.ApplicationTagsItem;
import org.wso2.carbon.identity.application.common.model.ApplicationTagsListItem;
import org.wso2.carbon.identity.application.tag.mgt.dao.impl.ApplicationTagDAOImpl;
import org.wso2.carbon.identity.application.tag.mgt.dao.impl.CacheBackedApplicationTagDAO;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;

import java.util.List;

/**
 * Application Tag management service.
 */
public class ApplicationTagManagerImpl implements ApplicationTagManager {

    private static final ApplicationTagManager INSTANCE = new ApplicationTagManagerImpl();
    private static final CacheBackedApplicationTagDAO dao =
            new CacheBackedApplicationTagDAO(new ApplicationTagDAOImpl());

    private ApplicationTagManagerImpl() {

    }

    public static ApplicationTagManager getInstance() {

        return INSTANCE;
    }

    @Override
    public ApplicationTagsItem createApplicationTag(ApplicationTag applicationTagDTO, String tenantDomain)
            throws ApplicationTagMgtException {

        return dao.createApplicationTag(applicationTagDTO, IdentityTenantUtil.getTenantId(tenantDomain));
    }

    @Override
    public List<ApplicationTagsListItem> getAllApplicationTags(String tenantDomain, Integer offset, Integer limit,
                                                               String filter) throws ApplicationTagMgtException {

        return dao.getAllApplicationTags(IdentityTenantUtil.getTenantId(tenantDomain), offset, limit, filter);
    }

    @Override
    public ApplicationTagsListItem getApplicationTagById(String applicationTagId, String tenantDomain)
            throws ApplicationTagMgtException {

        return dao.getApplicationTagById(applicationTagId, IdentityTenantUtil.getTenantId(tenantDomain));
    }

    @Override
    public void deleteApplicationTagById(String applicationTagId, String tenantDomain)
            throws ApplicationTagMgtException {

        dao.deleteApplicationTagById(applicationTagId, IdentityTenantUtil.getTenantId(tenantDomain));
    }

    @Override
    public void updateApplicationTag(ApplicationTag applicationTagPatch, String applicationTagId,
                                     String tenantDomain) throws ApplicationTagMgtException {

        dao.updateApplicationTag(applicationTagPatch, applicationTagId, IdentityTenantUtil.getTenantId(tenantDomain));
    }

    @Override
    public int getCountOfApplicationTags(String filter, String tenantDomain) throws ApplicationTagMgtException {

        return dao.getCountOfApplicationTags(filter, IdentityTenantUtil.getTenantId(tenantDomain));
    }
}
