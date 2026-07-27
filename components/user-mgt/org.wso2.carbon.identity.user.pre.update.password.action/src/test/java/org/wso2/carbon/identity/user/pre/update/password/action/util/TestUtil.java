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

package org.wso2.carbon.identity.user.pre.update.password.action.util;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Utility class for the Pre Update Password action component tests.
 */
public class TestUtil {

    public static final String TENANT_DOMAIN = "wso2.com";
    public static final String TEST_ID = "test-id";
    public static final String TEST_ACTION = "Test Action";
    public static final String TEST_DESCRIPTION = "Test Description";
    public static final String TEST_URL = "https://test.endpoint";
    public static final String TEST_USERNAME = "test-username";
    public static final String TEST_PASSWORD = "test-password";
    public static final String TEST_CERTIFICATE_ID = "certId";
    public static final String TEST_CERTIFICATE_NAME = "ACTIONS:certId";
    public static final String TEST_CERTIFICATE = "dummyCertificate";
    public static final String TEST_UPDATED_CERTIFICATE = "dummyCertificate";
    public static final String TEST_ACCESSING_ORG_ID = "9a56eb19-23c4-4306-ae13-75299c2a40af";
    public static final String TEST_ACCESSING_ORG_NAME = "mySubOrg";
    public static final String TEST_ACCESSING_ORG_HANDLE = "mySubOrg.com";
    public static final int TEST_ACCESSING_ORG_DEPTH = 20;
    public static final String TEST_RESIDENT_ORG_ID = "6a56eba9-23c4-4306-ae13-11259c2a40ae";
    public static final String TEST_RESIDENT_ORG_NAME = "mySubOrg2";
    public static final String TEST_RESIDENT_ORG_HANDLE = "mySubOrg2.com";
    public static final int TEST_RESIDENT_ORG_DEPTH = 20;
    public static final int ROOT_ORG_TENANT_ID = 12;
    public static final String ROOT_ORG_TENANT_DOMAIN = "primaryOrg.com";
    public static final String ROOT_ORG_ID = "6a56eba9-23c4-4306-ae13-11259c2a40ae";

    public static final String TEST_USER_STORE_DOMAIN_NAME = "PRIMARY";
    public static final String TEST_USER_STORE_DOMAIN_ID = Base64.getEncoder()
            .encodeToString(TEST_USER_STORE_DOMAIN_NAME.getBytes(StandardCharsets.UTF_8));
    public static final String TEST_SAMPLE_CERTIFICATE =
            "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUQwRENDQXJpZ0F3SUJBZ0lCQVRBTkJna3Foa2lHOXcwQkFRVUZBREIvTVFzd0N" +
                    "RWURWUVFHRXdKR1VqRVQNCk1CRUdBMVVFQ0F3S1UyOXRaUzFUZEdGMFpURU9NQXdHQTFVRUJ3d0ZVR0Z5YVhNeERUQUxCZ0" +
                    "5WQkFvTUJFUnANCmJXa3hEVEFMQmdOVkJBc01CRTVUUWxVeEVEQU9CZ05WQkFNTUIwUnBiV2tnUTBFeEd6QVpCZ2txaGtpR" +
                    "zl3MEINCkNRRVdER1JwYldsQVpHbHRhUzVtY2pBZUZ3MHhOREF4TWpneU1ETTJOVFZhRncweU5EQXhNall5TURNMk5UVmEN" +
                    "Ck1Gc3hDekFKQmdOVkJBWVRBa1pTTVJNd0VRWURWUVFJREFwVGIyMWxMVk4wWVhSbE1TRXdId1lEVlFRS0RCaEoNCmJuUmx" +
                    "jbTVsZENCWGFXUm5hWFJ6SUZCMGVTQk1kR1F4RkRBU0JnTlZCQU1NQzNkM2R5NWthVzFwTG1aeU1JSUINCklqQU5CZ2txaG" +
                    "tpRzl3MEJBUUVGQUFPQ0FROEFNSUlCQ2dLQ0FRRUF2cG5hUEtMSUtkdng5OEtXNjhsejhwR2ENClJSY1llcnNOR3FQanBpZ" +
                    "k1WampFOEx1Q29YZ1BVMEhlUG5OVFVqcFNoQm55bktDdnJ0V2hOK2hhS2JTcCtRV1gNClN4aVRyVzk5SEJmQWwxTURReVdj" +
                    "dWtvRWI5Q3c2SU5jdFZVTjRpUnZrbjlUOEU2cTE3NFJiY253QS83eVRjN3ANCjFOQ3Z3KzZCL2FBTjlsMUcycFFYZ1JkWUM" +
                    "vK0c2bzFJWkVIdFdocXpFOTduWTVRS051VVZEMFYwOWRjNUNEWUINCmFLanFldHd3djZERmsvR1JkT1NFZC82YlcrMjB6MH" +
                    "FTSHBhM1lOVzZxU3AreDVweVltRHJ6UklSMDNvczZEYXUNClprQ2hTUnljL1dodnVyeDZvODVENnFwenl3bzh4d05hTFpIe" +
                    "FRRUGdjSUE1c3U5Wkl5dHY5TEgyRStsU3d3SUQNCkFRQUJvM3N3ZVRBSkJnTlZIUk1FQWpBQU1Dd0dDV0NHU0FHRytFSUJE" +
                    "UVFmRmgxUGNHVnVVMU5NSUVkbGJtVnkNCllYUmxaQ0JEWlhKMGFXWnBZMkYwWlRBZEJnTlZIUTRFRmdRVSt0dWdGdHlOK2N" +
                    "YZTF3eFVxZUE3WCt5UzNiZ3cNCkh3WURWUjBqQkJnd0ZvQVVoTXdxa2JCckdwODdIeGZ2d2dQbmxHZ1ZSNjR3RFFZSktvWk" +
                    "lodmNOQVFFRkJRQUQNCmdnRUJBSUVFbXFxaEV6ZVhaNENLaEU1VU05dkNLemtqNUl2OVRGcy9hOUNjUXVlcHpwbHQ3WVZtZ" +
                    "XZCRk5PYzANCisxWnlSNHRYZ2k0KzVNSEd6aFlDSVZ2SG80aEtxWW0rSitvNW13UUluZjFxb0FIdU83Q0xEM1dOYTFzS2NW" +
                    "VVYNCnZlcEl4Yy8xYUhackcrZFBlRUh0ME1kRmZPdzEzWWRVYzJGSDZBcUVkY0VMNGFWNVBYcTJlWVI4aFI0ektiYzENCmZ" +
                    "CdHVxVXN2QThOV1NJeXpRMTZmeUd2ZStBTmY2dlh2VWl6eXZ3RHJQUnYva2Z2TE5hM1pQbkxNTXhVOThNdmgNClBYeTNQa0" +
                    "I4Kys2VTRZM3ZkazJOaTJXWVlsSWxzOHlxYk00MzI3SUtta0RjMlRpbVM4dTYwQ1Q0N21LVTdhRFkNCmNiVFY1UkRrcmxhW" +
                    "XdtNXlxbFRJZ2x2Q3Y3bz0NCi0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0K";

    public static final List<String> TEST_ATTRIBUTES = Arrays.asList("http://wso2.org/accountDisabled",
            "http://wso2.org/claims/identity/accountLocked");
    public static final List<String> UPDATED_TEST_ATTRIBUTES = Collections.singletonList(
            "http://wso2.org/claims/identity/accountState");
    public static final List<String> TEST_EMPTY_ATTRIBUTES = Collections.emptyList();
    public static final String ROLE_CLAIM_URI = "http://wso2.org/claims/roles";
    public static final String SAMPLE_LOCAL_CLAIM_URI_1 = "http://wso2.org/accountDisabled";
    public static final String SAMPLE_LOCAL_CLAIM_URI_2 = "http://wso2.org/claims/identity/accountLocked";
    public static final String SAMPLE_LOCAL_CLAIM_URI_3 = "http://wso2.org/claims/identity/accountState";
    public static final String SAMPLE_LOCAL_CLAIM_URI_4 = "http://wso2.org/claims/active";
    public static final String INVALID_TEST_ATTRIBUTES_TYPE = "attribute1";
    public static final List<String> INVALID_TEST_ATTRIBUTES = Arrays.asList("attribute1", "attribute2");
    public static final List<Integer> INVALID_TEST_ATTRIBUTES_VALUES = Arrays.asList(10, 20);
    public static final List<String> ROLES_CLAIM_ATTRIBUTE = Collections.singletonList("http://wso2.org/claims/roles");
    public static final List<String> DUPLICATED_TEST_ATTRIBUTES = Collections.nCopies(10, SAMPLE_LOCAL_CLAIM_URI_1);
    public static final List<String> INVALID_TEST_ATTRIBUTES_COUNT = Collections.nCopies(11, SAMPLE_LOCAL_CLAIM_URI_1);
    public static final Map<String, String> CLAIM_VALUES_MAP = Arrays.stream(Claims.values())
            .collect(java.util.stream.Collectors.toMap(Claims::getClaimURI, Claims::getValueInUserStore));

    public enum Claims {
        CLAIM1("http://wso2.org/claims/claim1", false,  "value10"),
        CLAIM2("http://wso2.org/claims/claim2", true, "value21"),
        CLAIM3("http://wso2.org/claims/claim3", false, "value30"),
        CLAIM4("http://wso2.org/claims/claim4", true, "value40"),
        CLAIM5("http://wso2.org/claims/claim5", false, "value51,value52,value53"),
        CLAIM6("http://wso2.org/claims/claim6", false, "value61"),
        CLAIM7("http://wso2.org/claims/claim7", true, "value71,value72"),
        GROUPS("http://wso2.org/claims/groups", true, "group2,group3"),
        ROLES("http://wso2.org/claims/roles", true, "role1");

        private final String claimURI;
        private final boolean isMultiValued;
        private final String valueInUserStore;

        Claims(String claimURI, boolean isMultiValued, String valueInUserStore) {

            this.claimURI = claimURI;
            this.isMultiValued = isMultiValued;
            this.valueInUserStore = valueInUserStore;
        }

        public String getClaimURI() {

            return claimURI;
        }

        public boolean isMultiValued() {

            return isMultiValued;
        }

        public String getValueInUserStore() {

            return valueInUserStore;
        }
    }
}
