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

package org.wso2.carbon.identity.core.util;

import org.wso2.carbon.core.RegistryResources;
import org.wso2.carbon.identity.core.util.IdentityKeyStoreResolverConstants.ErrorMessages;

import javax.xml.namespace.QName;

/**
 * Utility methods for IdentityKeyStoreManager.
 */
public class IdentityKeyStoreResolverUtil {

    public static String buildTenantKeyStoreName(String tenantDomain) throws IdentityKeyStoreResolverException {

        if (tenantDomain == null || tenantDomain.isEmpty()) {
            throw new IdentityKeyStoreResolverException(
                    ErrorMessages.ERROR_CODE_INVALID_ARGUMENT.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_INVALID_ARGUMENT.getDescription(), "Tenant domain"));
        }
        String ksName = tenantDomain.trim().replace(".", "-");
        return ksName + IdentityKeyStoreResolverConstants.KEY_STORE_EXTENSION;
    }

    public static String buildCustomKeyStoreName(String keyStoreName) throws IdentityKeyStoreResolverException {

        if (keyStoreName == null || keyStoreName.isEmpty()) {
            throw new IdentityKeyStoreResolverException(
                    ErrorMessages.ERROR_CODE_INVALID_ARGUMENT.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_INVALID_ARGUMENT.getDescription(), "KeyStore name"));
        }
        return RegistryResources.SecurityManagement.CustomKeyStore.KEYSTORE_PREFIX + keyStoreName;
    }

    public static QName getQNameWithIdentityNameSpace(String localPart) {
        
        return new QName(IdentityCoreConstants.IDENTITY_DEFAULT_NAMESPACE, localPart);
    }
}
