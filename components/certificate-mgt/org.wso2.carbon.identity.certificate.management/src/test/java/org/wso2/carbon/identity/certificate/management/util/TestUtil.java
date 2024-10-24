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
    public static final String CERTIFICATE_WITHOUT_BEGIN_END_MARKERS =
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
            "tSHcLfefKeidq6NDBJ8DhWHi/zvC9YbT0KkCToEgvCTBpRZgdSFxTJcUksqoFA==";
    public static final String CERTIFICATE_WITHOUT_BEGIN_MARKER =
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
    public static final String CERTIFICATE_WITHOUT_END_MARKER = "-----BEGIN CERTIFICATE-----" +
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
            "tSHcLfefKeidq6NDBJ8DhWHi/zvC9YbT0KkCToEgvCTBpRZgdSFxTJcUksqoFA==";
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
