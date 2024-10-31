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

import org.apache.commons.dbcp.BasicDataSource;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for Certificate Management Tests.
 */
public class TestUtil {

    public static final int TEST_TENANT_ID = 2;
    public static final String TEST_TENANT_DOMAIN = "carbon.super";
    public static final String NAME_FIELD = "Name";
    public static final String CERTIFICATE_FIELD = "Certificate";
    public static final String TEST_UUID = "d6b08d41-65fb-46ff-9e87-1d62aaaace6c";
    public static final String TEST_OTHER_UUID = "f6b18d41-647fb-53ff-9e87-1d62e197ce6c";
    public static final int TEST_ID = 1;
    public static final int TEST_OTHER_ID = 100;
    public static final String CERTIFICATE_NAME = "test";
    public static final String CERTIFICATE = "-----BEGIN CERTIFICATE-----" +
            "MIIC+jCCAmOgAwIBAgIJAParOnPwEkKjMA0GCSqGSIb3DQEBBQUAMIGKMQswCQYD" +
            "VQQGEwJMSzEQMA4GA1UECBMHV2VzdGVybjEQMA4GA1UEBxMHQ29sb21ibzEWMBQG" +
            "A1UEChMNU29mdHdhcmUgVmlldzERMA8GA1UECxMIVHJhaW5pbmcxLDAqBgNVBAMT" +
            "I1NvZnR3YXJlIFZpZXcgQ2VydGlmaWNhdGUgQXV0aG9yaXR5MB4XDTEwMDcxMDA2" +
            "MzMwM1oXDTI0MDMxODA2MzMwM1owdjELMAkGA1UEBhMCTEsxEDAOBgNVBAgTB1dl" +
            "c3Rlcm4xEDAOBgNVBAcTB0NvbG9tYm8xFjAUBgNVBAoTDVNvZnR3YXJlIFZpZXcx" +
            "ETAPBgNVBAsTCFRyYWluaW5nMRgwFgYDVQQDEw9NeSBUZXN0IFNlcnZpY2UwgZ8w" +
            "DQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBAN6bi0llFz+R+93nLLK5BmnuF48tbODp" +
            "MBH7yGZ1/ESVUZoYm0GaPzg/ai3rX3r8BEr4TUrhhpKUKBpFxZvb2q+yREIeDEkD" +
            "bHJuyVdS6hvtfa89WMJtwc7gwYYkY8AoVJ94gU54GP2B6XyNpgDTXPd0d3aH/Zt6" +
            "69xGAVoe/0iPAgMBAAGjezB5MAkGA1UdEwQCMAAwHQYDVR0OBBYEFNAwSamhuJSw" +
            "XG0SJnWdIVF1PkW9MB8GA1UdIwQYMBaAFNa3YmhDO7BOwbUqmYU1k/U6p/UUMCwG" +
            "CWCGSAGG+EIBDQQfFh1PcGVuU1NMIEdlbmVyYXRlZCBDZXJ0aWZpY2F0ZTANBgkq" +
            "hkiG9w0BAQUFAAOBgQBwwC5H+U0a+ps4tDCicHQfC2SXRTgF7PlAu2rLfmJ7jyoD" +
            "X+lFEoWDUoE5qkTpMjsR1q/+2j9eTyi9xGj5sby4yFvmXf8jS5L6zMkkezSb6QAv" +
            "tSHcLfefKeidq6NDBJ8DhWHi/zvC9YbT0KkCToEgvCTBpRZgdSFxTJcUksqoFA==" +
            "-----END CERTIFICATE-----";
    public static final String UPDATED_CERTIFICATE = "-----BEGIN CERTIFICATE-----" +
            "MIIF3jCCBMagAwIBAgIEWccICDANBgkqhkiG9w0BAQsFADBTMQswCQYDVQQGEwJH" +
            "QjEUMBIGA1UEChMLT3BlbkJhbmtpbmcxLjAsBgNVBAMTJU9wZW5CYW5raW5nIFBy" +
            "ZS1Qcm9kdWN0aW9uIElzc3VpbmcgQ0EwHhcNMjQwNTI5MDMzNTIwWhcNMjUwNjI5" +
            "MDQwNTIwWjBzMQswCQYDVQQGEwJHQjEaMBgGA1UEChMRV1NPMiAoVUspIExJTUlU" +
            "RUQxKzApBgNVBGETIlBTREdCLU9CLVVua25vd24wMDE1ODAwMDAxSFFRclpBQVgx" +
            "GzAZBgNVBAMTEjAwMTU4MDAwMDFIUVFyWkFBWDCCASIwDQYJKoZIhvcNAQEBBQAD" +
            "ggEPADCCAQoCggEBAKsdF+tZecCrCuFqKoBthBNSd/GTnijLA/HMuZMUjxSXhLGb" +
            "jna10WzPl+nF/Xnxmx5obj4JIPgylDBh5wjkgoJYio6FHySYc+41i2QutyBzqS7z" +
            "p0YZO4MTUZDovJ1a2zGVSJ17jdyw3uzNTPA2x3WJeNJ+E9uxTKgEshgf1RteM7Fs" +
            "Zxlop5KaqY2S4KkPDEsGli69vzEVmE45WbjHPBRcur0o8C7Khur9o2VReEQY7EYu" +
            "S6122Shl1iPGdfcagvE9YeX6v37E6NbYGbw1EHB8mvpVwsL58nXgpXIwUUqICTim" +
            "8QicVcc0rlJFWI1bCd7A+9vVStsffxOJULDSEAMCAwEAAaOCApgwggKUMA4GA1Ud" +
            "DwEB/wQEAwIHgDCBkQYIKwYBBQUHAQMEgYQwgYEwEwYGBACORgEGMAkGBwQAjkYB" +
            "BgMwagYGBACBmCcCMGAwOTARBgcEAIGYJwECDAZQU1BfUEkwEQYHBACBmCcBAwwG" +
            "UFNQX0FJMBEGBwQAgZgnAQQMBlBTUF9JQwwbRmluYW5jaWFsIENvbmR1Y3QgQXV0" +
            "aG9yaXR5DAZHQi1GQ0EwIAYDVR0lAQH/BBYwFAYIKwYBBQUHAwEGCCsGAQUFBwMC" +
            "MIHgBgNVHSAEgdgwgdUwgdIGCysGAQQBqHWBBgFkMIHCMCoGCCsGAQUFBwIBFh5o" +
            "dHRwOi8vb2IudHJ1c3Rpcy5jb20vcG9saWNpZXMwgZMGCCsGAQUFBwICMIGGDIGD" +
            "VXNlIG9mIHRoaXMgQ2VydGlmaWNhdGUgY29uc3RpdHV0ZXMgYWNjZXB0YW5jZSBv" +
            "ZiB0aGUgT3BlbkJhbmtpbmcgUm9vdCBDQSBDZXJ0aWZpY2F0aW9uIFBvbGljaWVz" +
            "IGFuZCBDZXJ0aWZpY2F0ZSBQcmFjdGljZSBTdGF0ZW1lbnQwbQYIKwYBBQUHAQEE" +
            "YTBfMCYGCCsGAQUFBzABhhpodHRwOi8vb2IudHJ1c3Rpcy5jb20vb2NzcDA1Bggr" +
            "BgEFBQcwAoYpaHR0cDovL29iLnRydXN0aXMuY29tL29iX3BwX2lzc3VpbmdjYS5j" +
            "cnQwOgYDVR0fBDMwMTAvoC2gK4YpaHR0cDovL29iLnRydXN0aXMuY29tL29iX3Bw" +
            "X2lzc3VpbmdjYS5jcmwwHwYDVR0jBBgwFoAUUHORxiFy03f0/gASBoFceXluP1Aw" +
            "HQYDVR0OBBYEFBLK5vQAFDDbbNUNuDJteouP4+W9MA0GCSqGSIb3DQEBCwUAA4IB" +
            "AQAlGBsiJdEQp0MLrH40Cui31Z4C30UT2JRzGVZAFHOUsH3K4qdw91Xc8kWfbj0X" +
            "aGnaLTEILG84P99CX6XoTnA2FKVx7Ap0BBoxSHOjyAV78pB583I+fFyt4Q/R1ZGv" +
            "yakn1KXOexyG9ARx68HxBnrlOoM7Zd1VVbSlBjZgJhfXjsNtMh/bvdaSe/b+uVGg" +
            "wy/IT7aOlwhU75k6cyrc1xgRRujiMbWAX4Z3W4vvpW6h7Uj/rEPBYhvfCBJ0ZDWc" +
            "mMTjsc3iumHo89WCLLHOR9Uqnuwzsu2VSwmXWljW9d5fjTT/mcSg5siJXbaB7G82" +
            "no3rKs/ufQY5XiFzNmZQyRBG" +
            "-----END CERTIFICATE-----";
    public static final String INVALID_CERTIFICATE = "-----BEGIN CERTIFICATE-----" +
            "MIIC+jCCAmOgAwIBAgIJAParOnPwEkKjMA0GCSqGSIb3DQEBBQUAMIGKMQswCQYD" +
            "VQQGEwJMSzEQMA4GA1UECBMHV2VzdGVybjEQMA4GA1UEBxMHQ29sb21ibzEWMBQG" +
            "A1UEChMNU29mdHdhcmUgVmlldzERMA8GA1UECxMIVHJhaW5pbmcxLDAqBgNVBAMT" +
            "I1NvZnR3YXJlIFZpZXcgQ2VydGlmaWNhdGUgQXV0aG9yaXR5MB4XDTEwMDcxMDA2" +
            "MzMwM1oXDTI0MDMxODA2MzMwM1owdjELMAkGA1UEBhMCTEsxEDAOBgNVBAgTB1dl" +
            "c3Rlcm4xEDAOBgNVBAcTB0NvbG9tYm8xFjAUBgNVBAoTDVNvZnR3YXJlIFZpZXcx" +
            "ETAPBgNVBAsTCFRyYWluaW5nMRgwFgYDVQQDEw9NeSBUZXN0IFNlcnZpY2UwgZ8w" +
            "69xGAVoe/0iPAgMBAAGjezB5MAkGA1UdEwQCMAAwHQYDVR0OBBYEFNAwSamhuJSw" +
            "XG0SJnWdIVF1PkW9MB8GA1UdIwQYMBaAFNa3YmhDO7BOwbUqmYU1k/U6p/UUMCwG" +
            "CWCGSAGG+EIBDQQfFh1PcGVuU1NMIEdlbmVyYXRlZCBDZXJ0aWZpY2F0ZTANBgkq" +
            "hkiG9w0BAQUFAAOBgQBwwC5H+U0a+ps4tDCicHQfC2SXRTgF7PlAu2rLfmJ7jyoD" +
            "X+lFEoWDUoE5qkTpMjsR1q/+2j9eTyi9xGj5sby4yFvmXf8jS5L6zMkkezSb6QAv" +
            "tSHcLfefKeidq6NDBJ8DhWHi/zvC9YbT0KkCToEgvCTBpRZgdSFxTJcUksqoFA==" +
            "-----END CERTIFICATE-----";
    public static final String ENCODED_CERTIFICATE = "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUQwRENDQXJpZ0F3SUJBZ0" +
            "lCQVRBTkJna3Foa2lHOXcwQkFRVUZBREIvTVFzd0NRWURWUVFHRXdKR1VqRVQNCk1CRUdBMVVFQ0F3S1UyOXRaUzFUZEdGMFpURU9NQ" +
            "XdHQTFVRUJ3d0ZVR0Z5YVhNeERUQUxCZ05WQkFvTUJFUnANCmJXa3hEVEFMQmdOVkJBc01CRTVUUWxVeEVEQU9CZ05WQkFNTUIwUnBi" +
            "V2tnUTBFeEd6QVpCZ2txaGtpRzl3MEINCkNRRVdER1JwYldsQVpHbHRhUzVtY2pBZUZ3MHhOREF4TWpneU1ETTJOVFZhRncweU5EQXh" +
            "Nall5TURNMk5UVmENCk1Gc3hDekFKQmdOVkJBWVRBa1pTTVJNd0VRWURWUVFJREFwVGIyMWxMVk4wWVhSbE1TRXdId1lEVlFRS0RCaE" +
            "oNCmJuUmxjbTVsZENCWGFXUm5hWFJ6SUZCMGVTQk1kR1F4RkRBU0JnTlZCQU1NQzNkM2R5NWthVzFwTG1aeU1JSUINCklqQU5CZ2txa" +
            "GtpRzl3MEJBUUVGQUFPQ0FROEFNSUlCQ2dLQ0FRRUF2cG5hUEtMSUtkdng5OEtXNjhsejhwR2ENClJSY1llcnNOR3FQanBpZk1WampF" +
            "OEx1Q29YZ1BVMEhlUG5OVFVqcFNoQm55bktDdnJ0V2hOK2hhS2JTcCtRV1gNClN4aVRyVzk5SEJmQWwxTURReVdjdWtvRWI5Q3c2SU5" +
            "jdFZVTjRpUnZrbjlUOEU2cTE3NFJiY253QS83eVRjN3ANCjFOQ3Z3KzZCL2FBTjlsMUcycFFYZ1JkWUMvK0c2bzFJWkVIdFdocXpFOT" +
            "duWTVRS051VVZEMFYwOWRjNUNEWUINCmFLanFldHd3djZERmsvR1JkT1NFZC82YlcrMjB6MHFTSHBhM1lOVzZxU3AreDVweVltRHJ6U" +
            "klSMDNvczZEYXUNClprQ2hTUnljL1dodnVyeDZvODVENnFwenl3bzh4d05hTFpIeFRRUGdjSUE1c3U5Wkl5dHY5TEgyRStsU3d3SUQN" +
            "CkFRQUJvM3N3ZVRBSkJnTlZIUk1FQWpBQU1Dd0dDV0NHU0FHRytFSUJEUVFmRmgxUGNHVnVVMU5NSUVkbGJtVnkNCllYUmxaQ0JEWlh" +
            "KMGFXWnBZMkYwWlRBZEJnTlZIUTRFRmdRVSt0dWdGdHlOK2NYZTF3eFVxZUE3WCt5UzNiZ3cNCkh3WURWUjBqQkJnd0ZvQVVoTXdxa2" +
            "JCckdwODdIeGZ2d2dQbmxHZ1ZSNjR3RFFZSktvWklodmNOQVFFRkJRQUQNCmdnRUJBSUVFbXFxaEV6ZVhaNENLaEU1VU05dkNLemtqN" +
            "Ul2OVRGcy9hOUNjUXVlcHpwbHQ3WVZtZXZCRk5PYzANCisxWnlSNHRYZ2k0KzVNSEd6aFlDSVZ2SG80aEtxWW0rSitvNW13UUluZjFx" +
            "b0FIdU83Q0xEM1dOYTFzS2NWVVYNCnZlcEl4Yy8xYUhackcrZFBlRUh0ME1kRmZPdzEzWWRVYzJGSDZBcUVkY0VMNGFWNVBYcTJlWVI" +
            "4aFI0ektiYzENCmZCdHVxVXN2QThOV1NJeXpRMTZmeUd2ZStBTmY2dlh2VWl6eXZ3RHJQUnYva2Z2TE5hM1pQbkxNTXhVOThNdmgNCl" +
            "BYeTNQa0I4Kys2VTRZM3ZkazJOaTJXWVlsSWxzOHlxYk00MzI3SUtta0RjMlRpbVM4dTYwQ1Q0N21LVTdhRFkNCmNiVFY1UkRrcmxhW" +
            "XdtNXlxbFRJZ2x2Q3Y3bz0NCi0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0K";
    public static final String UPDATED_ENCODED_CERTIFICATE = "MIIDqDCCApCgAwIBAgIEZXBGCTANBgkqhkiG9w0BAQsFADBiMQswCQ" +
            "YDVQQGEwJVUzELMAkGA1UECAwCQ0ExFDASBgNVBAcMC1NhbnRhIENsYXJhMQ0wCwYDVQQKDARXU08yMQ0wCwYDVQQLDARXU08yMRIwE" +
            "AYDVQQDDAlsb2NhbGhvc3QwHhcNMjMxMjA2MDk1OTM3WhcNMjUwMTA3MDk1OTM3WjBiMQswCQYDVQQGEwJVUzELMAkGA1UECAwCQ0Ex" +
            "FDASBgNVBAcMC1NhbnRhIENsYXJhMQ0wCwYDVQQKDARXU08yMQ0wCwYDVQQLDARXU08yMRIwEAYDVQQDDAlsb2NhbGhvc3QwggEiMA0" +
            "GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCa4jlw8KrXs396SvKfTQ02IeRoaZquekoJSiw0l9e9BI2EavnTG8Jyop8z2rOr6C6jjd" +
            "gUytz5jBhopbgfxrP2i0NGujJFLNnSU8rchD2TJ9Qo8tWvfjAKC\\/UlRxSoErOTdz7XS1CcPf6oQfNzMZ6By29zfISuC+rWnjLqT3M" +
            "6z0F0b3+moiiZfQ05F00hwzSE9WBlL+GRxwpyQQYwsbGfZ+viI3EGv7sRv+xqpLPhW5SLzhGzsZi9C0M0G1jbvV1d+PY0MThE60rkav" +
            "jM++RRBesoi5JknZksAt9hOqxY3A1IMdDANpdKqhdF1aAyDX+vTZFrHfLsuEBec5Pp3tIXAgMBAAGjZjBkMA4GA1UdDwEB\\/wQEAwI" +
            "E8DAUBgNVHREEDTALgglsb2NhbGhvc3QwHQYDVR0OBBYEFHXXV2nlofhi8Wwjw0EoaFSYnWbSMB0GA1UdJQQWMBQGCCsGAQUFBwMBBg" +
            "grBgEFBQcDAjANBgkqhkiG9w0BAQsFAAOCAQEARe8DI8n72eUlQy9GSpiyxv8QUHFdiQa1nBW9nVTZdyJoSX0qh6N3xVNJXR3\\/zLv" +
            "L8MBVMvjkt0OQqvEiyjwnEWO6DbxTRr3vdf+rv5VwdkYn4McMKx4xF8Zag8xhyaYqUQzQXng51rV1+c4uzXugEhE5SzdDHYEXzX6joZ" +
            "Ig1yN+hEPc77RZJJHmwIQrTd3bnZpytB6RdBjnjSyh0BeHlJQGmPxomxYAS1hVszRdfWtrxDABflIJimJiHh3dykcyNlrwBu903pMdU" +
            "GQGqsUyEjhFd7s4AzuqYHJr5rYy950df9IbYShu2YflVEsWZqJR62CibWBcJKyHPYmtC0cSRQ==";
    public static final String INVALID_ENCODED_CERTIFICATE = "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUQwRENDQXJpZ0" +
            "lCQVRBTkJna3Foa2lHOXcwQkFRVUZBREIvTVFzd0NRWURWUVFHRXdKR1VqRVQNCk1CRUdBMVVFQ0F3S1UyOXRaUzFUZEdGMFpURU9NQ" +
            "XdHQTFVRUJ3d0ZVR0Z5YVhNeERUQUxCZ05WQkFvTUJFUnANCmJXa3hEVEFMQmdOVkJBc01CRTVUUWxVeEVEQU9CZ05WQkFNTUIwUnBi" +
            "V2tnUTBFeEd6QVpCZ2txaGtpRzl3MEINCkNRRVdER1JwYldsQVpHbHRhUzVtY2pBZUZ3MHhOREF4TWpneU1ETTJOVFZhRncweU5EQXh" +
            "oNCmJuUmxjbTVsZENCWGFXUm5hWFJ6SUZCMGVTQk1kR1F4RkRBU0JnTlZCQU1NQzNkM2R5NWthVzFwTG1aeU1JSUINCklqQU5CZ2txa" +
            "GtpRzl3MEJBUUVGQUFPQ0FROEFNSUlCQ2dLQ0FRRUF2cG5hUEtMSUtkdng5OEtXNjhsejhwR2ENClJSY1llcnNOR3FQanBpZk1WampF" +
            "OEx1Q29YZ1BVMEhlUG5OVFVqcFNoQm55bktDdnJ0V2hOK2hhS2JTcCtRV1gNClN4aVRyVzk5SEJmQWwxTURReVdjdWtvRWI5Q3c2SU5" +
            "jdFZVTjRpUnZrbjlUOEU2cTE3NFJiY253QS83eVRjN3ANCjFOQ3Z3KzZCL2FBTjlsMUcycFFYZ1JkWUMvK0c2bzFJWkVIdFdocXpFOT" +
            "klSMDNvczZEYXUNClprQ2hTUnljL1dodnVyeDZvODVENnFwenl3bzh4d05hTFpIeFRRUGdjSUE1c3U5Wkl5dHY5TEgyRStsU3d3SUQN" +
            "CkFRQUJvM3N3ZVRBSkJnTlZIUk1FQWpBQU1Dd0dDV0NHU0FHRytFSUJEUVFmRmgxUGNHVnVVMU5NSUVkbGJtVnkNCllYUmxaQ0JEWlh" +
            "KMGFXWnBZMkYwWlRBZEJnTlZIUTRFRmdRVSt0dWdGdHlOK2NYZTF3eFVxZUE3WCt5UzNiZ3cNCkh3WURWUjBqQkJnd0ZvQVVoTXdxa2" +
            "JCckdwODdIeGZ2d2dQbmxHZ1ZSNjR3RFFZSktvWklodmNOQVFFRkJRQUQNCmdnRUJBSUVFbXFxaEV6ZVhaNENLaEU1VU05dkNLemtqN" +
            "Ul2OVRGcy9hOUNjUXVlcHpwbHQ3WVZtZXZCRk5PYzANCisxWnlSNHRYZ2k0KzVNSEd6aFlDSVZ2SG80aEtxWW0rSitvNW13UUluZjFx" +
            "4aFI0ektiYzENCmZCdHVxVXN2QThOV1NJeXpRMTZmeUd2ZStBTmY2dlh2VWl6eXZ3RHJQUnYva2Z2TE5hM1pQbkxNTXhVOThNdmgNCl" +
            "BYeTNQa0I4Kys2VTRZM3ZkazJOaTJXWVlsSWxzOHlxYk00MzI3SUtta0RjMlRpbVM4dTYwQ1Q0N21LVTdhRFkNCmNiVFY1UkRrcmxhW" +
            "XdtNXlxbFRJZ2x2Q3Y3bz0NCi0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0K";

    public static Map<String, BasicDataSource> dataSourceMap = new HashMap<>();

    public static void initiateH2Database(String dbName) throws SQLException {

        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUsername("username");
        dataSource.setPassword("password");
        dataSource.setUrl("jdbc:h2:mem:test" + dbName);
        dataSource.setTestOnBorrow(true);
        dataSource.setValidationQuery("select 1");
        try (Connection connection = dataSource.getConnection()) {
            String scriptPath = Paths.get(System.getProperty("user.dir"), "src", "test", "resources",
                    "dbscripts", "h2.sql").toString();
            connection.createStatement().executeUpdate("RUNSCRIPT FROM '" + scriptPath + "'");
        }
        dataSourceMap.put(dbName, dataSource);
    }

    public static Connection getConnection(String dbName) throws SQLException {

        if (dataSourceMap.get(dbName) != null) {
            return dataSourceMap.get(dbName).getConnection();
        }
        throw new RuntimeException("No data source initiated for database: " + dbName);
    }

    public static void closeH2Database(String dbName) throws SQLException {

        BasicDataSource dataSource = dataSourceMap.get(dbName);
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
