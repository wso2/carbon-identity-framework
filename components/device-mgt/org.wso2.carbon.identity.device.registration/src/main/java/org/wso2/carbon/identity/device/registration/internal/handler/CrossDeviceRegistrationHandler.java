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

package org.wso2.carbon.identity.device.registration.internal.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.core.ServiceURLBuilder;
import org.wso2.carbon.identity.core.URLBuilderException;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.device.registration.internal.component.DeviceRegistrationComponentServiceHolder;
import org.wso2.carbon.identity.device.registration.internal.constant.ErrorMessage;
import org.wso2.carbon.identity.device.registration.internal.exception.DeviceRegistrationException;
import org.wso2.carbon.identity.device.registration.internal.util.DeviceRegistrationExceptionHandler;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.util.OrganizationManagementUtil;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Helpers to register another device by QR code: the one time flow id, the QR code data and the pairing code.
 */
public final class CrossDeviceRegistrationHandler {

    private static final String FLOW_EXECUTE_PATH = "/api/server/v1/flow/execute";
    private static final String QR_ENDPOINT = "endpoint";
    private static final String QR_METHOD = "method";
    private static final String QR_DATA = "data";
    private static final String QR_FLOW_ID = "flowId";
    private static final String HTTP_POST = "POST";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final int RANDOM_BYTES = 32;
    private static final int PAIRING_CODE_BOUND = 1_000_000;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private CrossDeviceRegistrationHandler() {

    }

    /**
     * Generate the one time flow id the device continues the flow with.
     *
     * @return 256 bit random id, base64url encoded.
     */
    public static String generateOneTimeFlowId() {

        byte[] bytes = new byte[RANDOM_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Generate the pairing code the device shows and the user enters in the browser.
     *
     * @return Six digit pairing code.
     */
    public static String generatePairingCode() {

        return String.format("%06d", SECURE_RANDOM.nextInt(PAIRING_CODE_BOUND));
    }

    /**
     * Hash a pairing code so the flow context does not hold the code.
     *
     * @param pairingCode Pairing code.
     * @return Base64url encoded SHA-256 hash of the code.
     * @throws DeviceRegistrationException If the hash cannot be computed.
     */
    public static String hashPairingCode(String pairingCode) throws DeviceRegistrationException {

        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(pairingCode.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw DeviceRegistrationExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_WHILE_VERIFYING_SIGNATURE, e, StringUtils.EMPTY);
        }
    }

    /**
     * Compare two values in constant time.
     *
     * @param value    Value.
     * @param expected Expected value.
     * @return True if both are set and equal.
     */
    public static boolean matches(String value, String expected) {

        if (value == null || expected == null) {
            return false;
        }
        return MessageDigest.isEqual(value.getBytes(StandardCharsets.UTF_8), expected.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Build the QR code data: the flow execute endpoint of the flow's tenant or organization, the method and the one
     * time flow id.
     *
     * @param oneTimeFlowId One time flow id the device continues the flow with.
     * @param tenantDomain  Tenant domain of the flow.
     * @return QR code data as JSON.
     * @throws DeviceRegistrationException If the data cannot be built.
     */
    public static String buildQrCodeData(String oneTimeFlowId, String tenantDomain)
            throws DeviceRegistrationException {

        try {
            ServiceURLBuilder builder = ServiceURLBuilder.create().addPath(FLOW_EXECUTE_PATH);
            if (OrganizationManagementUtil.isOrganization(tenantDomain)) {
                // A sub-organization flow continues on the organization's path.
                builder.setOrganization(DeviceRegistrationComponentServiceHolder.getInstance().getOrganizationManager()
                        .resolveOrganizationId(tenantDomain));
            } else {
                builder.setTenant(tenantDomain);
            }
            ObjectNode qrCodeData = OBJECT_MAPPER.createObjectNode();
            qrCodeData.put(QR_ENDPOINT, builder.build(IdentityUtil.getHostName()).getAbsolutePublicURL());
            qrCodeData.put(QR_METHOD, HTTP_POST);
            qrCodeData.putObject(QR_DATA).put(QR_FLOW_ID, oneTimeFlowId);
            return OBJECT_MAPPER.writeValueAsString(qrCodeData);
        } catch (URLBuilderException | JsonProcessingException | OrganizationManagementException e) {
            throw DeviceRegistrationExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_WHILE_BUILDING_QR_CODE_DATA, e);
        }
    }
}
