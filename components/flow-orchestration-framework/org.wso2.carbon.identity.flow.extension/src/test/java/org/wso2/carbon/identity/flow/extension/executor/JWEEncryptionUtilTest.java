/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.flow.extension.executor;

import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionException;

import java.security.cert.X509Certificate;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for {@link JWEEncryptionUtil} covering JWE detection, PEM certificate parsing
 * (including error branches) and the outbound encryption path against a self-signed certificate.
 */
public class JWEEncryptionUtilTest {

    // A self-signed RSA X.509 certificate (base64-encoded DER, CN=flow-ext-test), valid until 2300,
    // so validity checks never fail. Used only to exercise the outbound encryption path.
    private static final String TEST_CERTIFICATE =
            "MIIC1jCCAb6gAwIBAgIJAJW5CSgQetRIMA0GCSqGSIb3DQEBDAUAMBgxFjAUBgNVBAMTDWZsb3ctZXh0LXRlc3Qw"
            + "IBcNMjYwNzA2MDMyMTI0WhgPMjMwMDA0MjEwMzIxMjRaMBgxFjAUBgNVBAMTDWZsb3ctZXh0LXRlc3QwggEiMA0G"
            + "CSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDfg5WbkayJOskKoWZD/dLMkKNisi+3GaKqLgH9RAIMuy3NKNSQCA9p"
            + "YV4n8EUZnP0ZyM1Xv1MkB2mQLknpA6+n6fvyBuY6y2V9slIJlvrJvAUKpxhSWeyIyg3qgs2Kb+cX8NTC5vmrfN5"
            + "pw1QmjYYMAueGEz+z85Tay+C/y+bUns1LrIxTxKzheS9jIkfmAr2LE0DNoCLt7H6GdfLfT+INxfSPHTc1aa1pN4"
            + "QrQGzw91wOvkv4qIiBStJDB8BGIqsSgRMWAe5YKn0AUzYSGqUfhXJ32AnGZKBc7eFytKlpSUwpOeuwlK3ypApcn"
            + "VvNN3oAnN04jvZgdDSwGDOpbsNBAgMBAAGjITAfMB0GA1UdDgQWBBRYki8DbI6YT5peKE0DKEA9jUnPmjANBgkq"
            + "hkiG9w0BAQwFAAOCAQEAWkUK1YgFC+JTVjkhuSEiQIMUiuIprE1yENNaqaYV34LW263aMqc+wor3CDQwWN+8i/w"
            + "9UUmCqfpf06l7sNitT5XkAZSVXkry3TBqKjHKtG0PeHVbkVI4Pms+ZtLfGZut8N4GN6FJMGlYN8A9iRk5eVy+2r"
            + "HqbhC8BRVZ04FT2+IQM4F9Psa/hEvIMSF4Srfsn4tYZsiua7LJrNojX2Ai7OaFqg/+Imf/g5edV6UZaL+heERV"
            + "bRUPynphhy9HQSyR6zcT0UlzfG3G/neiNmbi17Si6LITb4EhfCo/Minv5zsDhWxppHx1YLZ46RvDZAMKKT6lY1E"
            + "rI+m/yoOhf5cj9g==";

    @Test
    public void testIsJWEEncrypted() {

        assertFalse(JWEEncryptionUtil.isJWEEncrypted(null));
        assertFalse(JWEEncryptionUtil.isJWEEncrypted(""));
        assertFalse(JWEEncryptionUtil.isJWEEncrypted("plaintext"));
        assertFalse(JWEEncryptionUtil.isJWEEncrypted("a.b.c"));            // 2 dots
        assertTrue(JWEEncryptionUtil.isJWEEncrypted("a.b.c.d.e"));         // exactly 4 dots
        assertFalse(JWEEncryptionUtil.isJWEEncrypted("a.b.c.d.e.f"));      // 5 dots
    }

    @Test
    public void testParsePEMCertificateValid() throws ActionExecutionException {

        X509Certificate certificate = JWEEncryptionUtil.parsePEMCertificate(TEST_CERTIFICATE);

        assertNotNull(certificate);
        assertEquals(certificate.getSubjectX500Principal().getName(), "CN=flow-ext-test");
    }

    @Test(expectedExceptions = ActionExecutionException.class)
    public void testParsePEMCertificateNull() throws ActionExecutionException {

        JWEEncryptionUtil.parsePEMCertificate(null);
    }

    @Test(expectedExceptions = ActionExecutionException.class)
    public void testParsePEMCertificateEmpty() throws ActionExecutionException {

        JWEEncryptionUtil.parsePEMCertificate("   ");
    }

    @Test(expectedExceptions = ActionExecutionException.class)
    public void testParsePEMCertificateInvalidBase64() throws ActionExecutionException {

        JWEEncryptionUtil.parsePEMCertificate("not-valid-base64-@@@");
    }

    @Test
    public void testEncryptProducesJWECompactSerialization() throws ActionExecutionException {

        String jwe = JWEEncryptionUtil.encrypt("secret-value", TEST_CERTIFICATE);

        assertNotNull(jwe);
        assertTrue(JWEEncryptionUtil.isJWEEncrypted(jwe),
                "encrypt() must return a JWE compact serialization with 4 dots: " + jwe);
        // The plaintext must not appear verbatim in the ciphertext.
        assertFalse(jwe.contains("secret-value"));
    }

    @Test(expectedExceptions = ActionExecutionException.class)
    public void testEncryptWithInvalidCertificateFails() throws ActionExecutionException {

        JWEEncryptionUtil.encrypt("secret-value", "not-a-certificate");
    }
}
