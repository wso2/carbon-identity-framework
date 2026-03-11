/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.flow.execution.engine.inflow.extension.model;

import org.wso2.carbon.identity.certificate.management.model.Certificate;

/**
 * Encryption configuration for In-Flow Extension actions.
 * <p>
 * Holds the external service's X.509 public certificate used for outbound JWE encryption.
 * This model is separate from {@link AccessConfig} — following the same pattern as
 * {@code PasswordSharing} in the PreUpdatePassword action type.
 * </p>
 * <p>
 * The IS uses this certificate to encrypt expose path values marked {@code encrypted: true}
 * before sending them to the external service. For inbound encryption (Extension → IS),
 * the external service must obtain the IS's public key out-of-band.
 * </p>
 */
public class Encryption {

    private final Certificate certificate;

    /**
     * Constructs an Encryption configuration with the given certificate.
     *
     * @param certificate The external service's X.509 public certificate.
     *                    May be {@code null} if encryption is not configured.
     */
    public Encryption(Certificate certificate) {

        this.certificate = certificate;
    }

    /**
     * Returns the external service's X.509 public certificate.
     *
     * @return The certificate, or {@code null} if not configured.
     */
    public Certificate getCertificate() {

        return certificate;
    }
}
