/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.core.dao;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import static org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderConstants.SAML_STORAGE_CONFIG;

/**
 * Factory class to create instances of SAMLSSOServiceProviderDAO based on the configured storage type.
 */
public class SAMLServiceProviderPersistenceManagerFactory {

    private static final Log LOG = LogFactory.getLog(SAMLServiceProviderPersistenceManagerFactory.class);
    private static String SAML_STORAGE_TYPE = IdentityUtil.getProperty(SAML_STORAGE_CONFIG);
    private static final String HYBRID = "hybrid";
    private static final String REGISTRY = "registry";

    public SAMLSSOServiceProviderDAO getSAMLServiceProviderPersistenceManager() {

        SAMLSSOServiceProviderDAO samlSSOServiceProviderDAO = CacheBackedSAMLSSOServiceProviderDAO.getInstance();
        if (StringUtils.isNotBlank(SAML_STORAGE_TYPE)) {
            switch (SAML_STORAGE_TYPE) {
                case HYBRID:
                    // Initialize Hybrid SAML storage.
                    samlSSOServiceProviderDAO = new HybridSAMLSSOServiceProviderDAOImpl();
                    LOG.info("Hybrid SAML storage initialized.");
                    break;
                case REGISTRY:
                    // Initialize Registry SAML storage.
                    samlSSOServiceProviderDAO = new RegistrySAMLSSOServiceProviderDAOImpl();
                    LOG.warn("Registry based SAML storage initialized.");
                    break;
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(
                    "SAML SSO Service Provider DAO initialized with the type: " + samlSSOServiceProviderDAO.getClass());
        }
        return samlSSOServiceProviderDAO;
    }
}
