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

package org.wso2.carbon.identity.user.pre.update.password.action.model;

import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.action.management.api.model.Authentication;
import org.wso2.carbon.identity.action.management.api.model.EndpointConfig;
import org.wso2.carbon.identity.certificate.management.model.Certificate;
import org.wso2.carbon.identity.user.pre.update.password.action.api.model.PasswordSharing;
import org.wso2.carbon.identity.user.pre.update.password.action.api.model.PreUpdatePasswordAction;

import java.sql.Timestamp;
import java.util.Date;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_ACTION;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_ATTRIBUTES;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_CERTIFICATE;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_CERTIFICATE_ID;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_CERTIFICATE_NAME;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_DESCRIPTION;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_ID;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_PASSWORD;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_URL;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_USERNAME;

public class PreUpdatePasswordActionBuilderTest {
    
    @Test
    public void testBuildActionRequest() {
        
        Action action = new Action.ActionRequestBuilder()
                .name(TEST_ACTION)
                .description(TEST_DESCRIPTION)
                .endpoint(new EndpointConfig.EndpointConfigBuilder()
                        .uri(TEST_URL)
                        .authentication(new Authentication.BasicAuthBuilder(TEST_USERNAME, TEST_PASSWORD).build())
                        .build())
                .build();
        PreUpdatePasswordAction.RequestBuilder requestBuilder = new PreUpdatePasswordAction.RequestBuilder(action)
                .passwordSharing(new PasswordSharing.Builder()
                        .format(PasswordSharing.Format.SHA256_HASHED)
                        .certificate(new Certificate.Builder()
                                .certificateContent(TEST_CERTIFICATE)
                                .build())
                        .build())
                .attributes(TEST_ATTRIBUTES);

        PreUpdatePasswordAction preUpdatePasswordAction = requestBuilder.build();
        
        assertNotNull(preUpdatePasswordAction);
        assertNull(preUpdatePasswordAction.getId());
        assertNull(preUpdatePasswordAction.getStatus());
        assertEquals(preUpdatePasswordAction.getName(), TEST_ACTION);
        assertEquals(preUpdatePasswordAction.getDescription(), TEST_DESCRIPTION);
        assertEquals(preUpdatePasswordAction.getEndpoint().getUri(), TEST_URL);
        assertEquals(preUpdatePasswordAction.getEndpoint().getAuthentication().getType(), Authentication.Type.BASIC);
        assertEquals(preUpdatePasswordAction.getEndpoint().getAuthentication()
                        .getProperty(Authentication.Property.USERNAME).getValue(), TEST_USERNAME);
        assertEquals(preUpdatePasswordAction.getEndpoint().getAuthentication()
                        .getProperty(Authentication.Property.PASSWORD).getValue(), TEST_PASSWORD);
        assertEquals(preUpdatePasswordAction.getPasswordSharing().getFormat(), PasswordSharing.Format.SHA256_HASHED);
        assertEquals(preUpdatePasswordAction.getPasswordSharing().getCertificate().getCertificateContent(),
                TEST_CERTIFICATE);
        assertEquals(preUpdatePasswordAction.getAttributes(), TEST_ATTRIBUTES);
    }

    @Test
    public void testBuildActionResponse() {

        Timestamp createdAt = new Timestamp(new Date().getTime());
        Timestamp updatedAt = new Timestamp(new Date().getTime() + 5000);
        PreUpdatePasswordAction.ResponseBuilder responseBuilder = new PreUpdatePasswordAction.ResponseBuilder()
                .id(TEST_ID)
                .name(TEST_ACTION)
                .description(TEST_DESCRIPTION)
                .status(Action.Status.ACTIVE)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .endpoint(new EndpointConfig.EndpointConfigBuilder()
                        .uri(TEST_URL)
                        .authentication(new Authentication.BasicAuthBuilder(TEST_USERNAME, TEST_PASSWORD).build())
                        .build())
                .passwordSharing(new PasswordSharing.Builder()
                        .format(PasswordSharing.Format.SHA256_HASHED)
                        .certificate(new Certificate.Builder()
                                .id(TEST_CERTIFICATE_ID)
                                .name(TEST_CERTIFICATE_NAME)
                                .certificateContent(TEST_CERTIFICATE)
                                .build())
                        .build())
                .attributes(TEST_ATTRIBUTES);

        PreUpdatePasswordAction preUpdatePasswordAction = responseBuilder.build();

        assertNotNull(preUpdatePasswordAction);
        assertEquals(preUpdatePasswordAction.getId(), TEST_ID);
        assertEquals(preUpdatePasswordAction.getName(), TEST_ACTION);
        assertEquals(preUpdatePasswordAction.getDescription(), TEST_DESCRIPTION);
        assertEquals(preUpdatePasswordAction.getStatus(), Action.Status.ACTIVE);
        assertEquals(preUpdatePasswordAction.getCreatedAt().getTime(), createdAt.getTime());
        assertEquals(preUpdatePasswordAction.getUpdatedAt().getTime(), updatedAt.getTime());
        assertEquals(preUpdatePasswordAction.getEndpoint().getUri(), TEST_URL);
        assertEquals(preUpdatePasswordAction.getEndpoint().getAuthentication().getType(), Authentication.Type.BASIC);
        assertEquals(preUpdatePasswordAction.getEndpoint().getAuthentication()
                .getProperty(Authentication.Property.USERNAME).getValue(), TEST_USERNAME);
        assertEquals(preUpdatePasswordAction.getEndpoint().getAuthentication()
                .getProperty(Authentication.Property.PASSWORD).getValue(), TEST_PASSWORD);
        assertEquals(preUpdatePasswordAction.getPasswordSharing().getFormat(), PasswordSharing.Format.SHA256_HASHED);
        assertEquals(preUpdatePasswordAction.getPasswordSharing().getCertificate().getId(), TEST_CERTIFICATE_ID);
        assertEquals(preUpdatePasswordAction.getPasswordSharing().getCertificate().getName(), TEST_CERTIFICATE_NAME);
        assertEquals(preUpdatePasswordAction.getPasswordSharing().getCertificate().getCertificateContent(),
                TEST_CERTIFICATE);
        assertEquals(preUpdatePasswordAction.getAttributes(), TEST_ATTRIBUTES);
    }
}
