/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.identity.application.mgt.cache;

import java.io.Serializable;

/**
 * Cache Key which will use in {@link ServiceProviderTemplateCache}.
 */
public class ServiceProviderTemplateCacheKey implements Serializable {

    private static final long serialVersionUID = 8263255365985309443L;
    protected String tenantDomain = "carbon.super";
    private String templateName;

    public ServiceProviderTemplateCacheKey(String templateName, String tenantDomain) {

        this.templateName = templateName;
        if (tenantDomain != null) {
            this.tenantDomain = tenantDomain.toLowerCase();
        }
    }

    /**
     * Get template name.
     *
     * @return name of the template
     */
    public String getTemplateName() {

        return templateName;
    }

    /**
     * Get tenant domain.
     *
     * @return tenant domain
     */
    public String getTenantDomain() {

        return this.tenantDomain;
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
        ServiceProviderTemplateCacheKey that = (ServiceProviderTemplateCacheKey) o;
        if (!templateName.equals(that.templateName)) {
            return false;
        }
        if (!tenantDomain.equals(that.tenantDomain)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {

        int result = super.hashCode();
        result = 31 * result + templateName.hashCode();
        result = 31 * result + tenantDomain.hashCode();
        return result;
    }
}
