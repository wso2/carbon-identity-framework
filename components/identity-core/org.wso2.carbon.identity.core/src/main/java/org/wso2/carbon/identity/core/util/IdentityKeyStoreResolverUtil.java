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

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.core.RegistryResources;
import org.wso2.carbon.identity.core.util.IdentityKeyStoreResolverConstants.ErrorMessages;
import org.wso2.carbon.utils.security.KeystoreUtils;

import javax.xml.namespace.QName;

/**
 * Utility methods for IdentityKeyStoreManager.
 */
public class IdentityKeyStoreResolverUtil {

    /**
     * Builds the key store name for a tenant using tenant domain name.
     *
     * @param tenantDomain Tenant domain name.
     * @return tenant key store name as String.
     * @throws IdentityKeyStoreResolverException if tenant domain is null or empty.
     */
    public static String buildTenantKeyStoreName(String tenantDomain) throws IdentityKeyStoreResolverException {

        return buildTenantKeyStoreName(tenantDomain, null);
    }

    /**
     * Builds the keystore name for a given tenant domain and context.
     * The tenant domain is sanitized by replacing dots (.) with hyphens (-) to ensure compatibility
     * with keystore naming conventions. If a context is provided, it is appended to the sanitized
     * tenant domain with an underscore (_). The method also appends the standard keystore file
     * extension as defined in {@link IdentityKeyStoreResolverConstants}.
     *
     * @param tenantDomain The domain name of the tenant (e.g., "example.com").
     * @param context       The optional context to append to the tenant keystore name.
     * @return A sanitized and formatted keystore name for the tenant.
     * @throws IdentityKeyStoreResolverException If the tenant domain is null, empty, or invalid.
     */
    public static String buildTenantKeyStoreName(String tenantDomain, String context)
            throws IdentityKeyStoreResolverException {

        // Validate tenantDomain argument
        if (StringUtils.isEmpty(tenantDomain)) {
            throw new IdentityKeyStoreResolverException(
                    ErrorMessages.ERROR_CODE_INVALID_ARGUMENT.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_INVALID_ARGUMENT.getDescription(), "Tenant domain"));
        }

        // Sanitize tenant domain: replace '.' with '-'
        String ksName = tenantDomain.trim().replace(".", "-");

        // Append context if provided
        if (StringUtils.isNotBlank(context)) {
            ksName = buildDomainWithContext(ksName, context);
        }

        // Add the keystore extension
        return ksName + KeystoreUtils.getKeyStoreFileExtension(ksName, tenantDomain);
    }

    /**
     * Builds the custom key store name by adding the CUSTOM_KEYSTORE_PREFIX to the key store name.
     *
     * @param keyStoreName Key store file name.
     * @return Custom key store name as String.
     * @throws IdentityKeyStoreResolverException if key store name is null or empty.
     */
    public static String buildCustomKeyStoreName(String keyStoreName) throws IdentityKeyStoreResolverException {

        if (StringUtils.isEmpty(keyStoreName)) {
            throw new IdentityKeyStoreResolverException(
                    ErrorMessages.ERROR_CODE_INVALID_ARGUMENT.getCode(),
                    String.format(ErrorMessages.ERROR_CODE_INVALID_ARGUMENT.getDescription(), "KeyStore name"));
        }
        return RegistryResources.SecurityManagement.CustomKeyStore.CUSTOM_KEYSTORE_PREFIX + keyStoreName;
    }

    /**
     * Builds a QName object with the IDENTITY_DEFAULT_NAMESPACE.
     *
     * @param localPart Local part of the QName.
     * @return QName object.
     */
    public static QName getQNameWithIdentityNameSpace(String localPart) {
        
        return new QName(IdentityCoreConstants.IDENTITY_DEFAULT_NAMESPACE, localPart);
    }

    /**
     * Concatenates tenantDomain and context with the separator.
     *
     * @param tenantDomain the key store name
     * @param context the context
     * @return a concatenated string in the format tenantDomain:context
     */
    public static String buildDomainWithContext(String tenantDomain, String context) {

        return tenantDomain + IdentityKeyStoreResolverConstants.KEY_STORE_CONTEXT_SEPARATOR + context;
    }
}
