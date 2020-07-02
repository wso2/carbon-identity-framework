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

package org.wso2.carbon.identity.template.mgt.cache;

import org.wso2.carbon.identity.application.common.cache.CacheKey;

/**
 * Cache key for lookup config store based template from the cache.
 */
public class ConfigStoreBasedTemplateCacheKey extends CacheKey {

    private static final long serialVersionUID = 8263255365985309443L;

    private String templateKey;

    public ConfigStoreBasedTemplateCacheKey(String templateId) {

        this.templateKey = templateId;
    }

    public String getTemplateKey() {

        return templateKey;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        ConfigStoreBasedTemplateCacheKey that = (ConfigStoreBasedTemplateCacheKey) o;

        if (!templateKey.equals(that.templateKey)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {

        int result = super.hashCode();
        result = 31 * result + templateKey.hashCode();
        return result;
    }
}
