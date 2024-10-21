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

package org.wso2.carbon.identity.certificate.management.core.util;

import org.apache.commons.lang.ArrayUtils;
import org.wso2.carbon.identity.certificate.management.core.constant.CertificateMgtErrors;
import org.wso2.carbon.identity.certificate.management.core.exception.CertificateMgtClientException;
import org.wso2.carbon.identity.certificate.management.core.exception.CertificateMgtServerException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Utility class for Certificate Management.
 */
public class CertificateMgtUtil {

    /**
     * Converts the given certificate content into a stream of bytes using UTF-8 encoding.
     *
     * @param certificateContent Certificate content to be converted to blob.
     * @return InputStream of the certificate content.
     */
    public static InputStream getCertificateByteStream(String certificateContent) {

        return new ByteArrayInputStream(certificateContent.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Get the string representation of the content from the given InputStream of a blob.
     *
     * @param stream InputStream containing blob data.
     * @return String content of the blob, or null if the input stream is null.
     * @throws IOException If an I/O error occurs while reading the stream.
     */
    public static String getStringValueFromBlob(InputStream stream) throws IOException {

        if (stream == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        }

        return sb.toString();
    }

    /**
     * Raise Certificate Management client exception.
     *
     * @param error Error message.
     * @param data  Data.
     * @return CertificateMgtClientException If an error occurs from the client.
     */
    public static CertificateMgtClientException raiseClientException(CertificateMgtErrors error, String... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }

        return new CertificateMgtClientException(error.getMessage(), description, error.getCode());
    }

    /**
     * Raise Certificate Management server exception.
     *
     * @param error Error message.
     * @param e     Throwable.
     * @param data  Data.
     * @return CertificateMgtServerException If an error occurs from the server.
     */
    public static CertificateMgtServerException raiseServerException(CertificateMgtErrors error, Throwable e,
                                                                     String... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }

        return new CertificateMgtServerException(error.getMessage(), description, error.getCode(), e);
    }
}
