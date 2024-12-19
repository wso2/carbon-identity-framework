/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.core;


import org.wso2.carbon.user.api.Tenant;

import java.security.cert.X509Certificate;

/**
 * Service contract for retrieving certificates for a given certificate identifier.
 * Implementations of this interface is responsible for retrieving the certificate
 * from the relevant store. e.g. PKCS12 file, database
 */
public interface CertificateRetriever {

    /**
     * Finds and returns the certificate for the given certificateId and tenant
     *
     * @param certificateId Identifier of the certificate. The implementations might have different representations for the identifier.
     * @param tenant        Tenant where the certificate should be searched for, if applicable.
     * @return The certificate in X509 format.
     */
    X509Certificate getCertificate(String certificateId, Tenant tenant) throws CertificateRetrievingException;


}
