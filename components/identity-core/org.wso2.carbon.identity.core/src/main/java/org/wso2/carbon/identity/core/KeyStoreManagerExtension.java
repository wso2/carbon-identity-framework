/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.core;

import org.wso2.carbon.identity.base.IdentityException;

import java.security.PrivateKey;
import java.security.cert.Certificate;

/**
 * Extension point to be supplied by an extension to be used instead of the default implementation of the key provider.
 *
 */
public interface KeyStoreManagerExtension {

    /**
     * Returns the Private Key for the given tenant domain.
     * System ensures that the call to this method is performed by acceptable code, when system security
     * manager is activated, depending on security manager policies.
     * However implementer may chose to add additional security mechanisms in the implementation.
     *
     * @param tenantDomain  the tenant domain
     * @return
     * @throws IdentityException upon any error accessing the underlying key store
     */
    PrivateKey getPrivateKey(String tenantDomain) throws IdentityException;

    /**
     * Returns the Public certificate for the given tenant domain.
     * System ensures that the call to this method is performed by acceptable code, when system security
     * manager is activated, depending on security manager policies.
     * However implementer may chose to add additional security mechanisms in the implementation.
     *
     * @param tenantDomain  the tenant domain
     * @return
     * @throws IdentityException upon any error accessing the underlying key store
     */
    Certificate getCertificate(String tenantDomain) throws IdentityException;
}
