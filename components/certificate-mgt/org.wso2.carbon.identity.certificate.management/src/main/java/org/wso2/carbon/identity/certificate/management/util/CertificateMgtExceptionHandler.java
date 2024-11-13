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

package org.wso2.carbon.identity.certificate.management.util;

import org.apache.commons.lang.ArrayUtils;
import org.wso2.carbon.identity.certificate.management.constant.CertificateMgtErrors;
import org.wso2.carbon.identity.certificate.management.exception.CertificateMgtClientException;
import org.wso2.carbon.identity.certificate.management.exception.CertificateMgtRuntimeException;
import org.wso2.carbon.identity.certificate.management.exception.CertificateMgtServerException;

/**
 * This class is responsible for handling exceptions in the certificate management module.
 */
public class CertificateMgtExceptionHandler {

    private CertificateMgtExceptionHandler() {
    }

    /**
     * Throw Certificate Management client exception.
     *
     * @param error Error message.
     * @param data  Data.
     * @throws CertificateMgtClientException If an error occurs from the client.
     */
    public static void throwClientException(CertificateMgtErrors error, String... data)
            throws CertificateMgtClientException {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }

        throw new CertificateMgtClientException(error.getMessage(), description, error.getCode());
    }

    /**
     * Throw Certificate Management server exception.
     *
     * @param error Error message.
     * @param e     Throwable.
     * @param data  Data.
     * @throws CertificateMgtServerException If an error occurs from the server.
     */
    public static void throwServerException(CertificateMgtErrors error, Throwable e, String... data)
            throws CertificateMgtServerException {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }

        throw new CertificateMgtServerException(error.getMessage(), description, error.getCode(), e);
    }

    /**
     * Throw Certificate Management runtime exception.
     *
     * @param errorMessage Error message.
     * @param e            Throwable.
     * @throws CertificateMgtRuntimeException If an error occurs from the server in the runtime.
     */
    public static void throwRuntimeException(String errorMessage, Throwable e)
            throws CertificateMgtRuntimeException {

        throw new CertificateMgtRuntimeException(errorMessage, e);
    }
}
