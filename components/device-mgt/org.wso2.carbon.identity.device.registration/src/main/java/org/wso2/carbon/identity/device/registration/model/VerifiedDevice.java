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

package org.wso2.carbon.identity.device.registration.model;

import org.wso2.carbon.identity.device.mgt.api.model.Device;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * A device whose signature has been verified but is not yet bound to a user — it mirrors an
 * IDN_DEVICE row without the IDN_USER_DEVICE binding. {@link #bindTo(String)} is the only way to
 * obtain a persistable {@link Device}, so a device can never be persisted without an owner.
 *
 * Serializable because instances are held in the serializable FlowExecutionContext.
 */
public class VerifiedDevice implements Serializable {

    private static final long serialVersionUID = 4021839127493856201L;

    private final String id;
    private final String deviceName;
    private final String deviceModel;
    private final String publicKey;
    private final Timestamp registeredAt;
    private final String metadata;

    private VerifiedDevice(Builder builder) {

        this.id = builder.id;
        this.deviceName = builder.deviceName;
        this.deviceModel = builder.deviceModel;
        this.publicKey = builder.publicKey;
        this.registeredAt = builder.registeredAt == null ? null : new Timestamp(builder.registeredAt.getTime());
        this.metadata = builder.metadata;
    }

    /**
     * Returns the device identifier.
     *
     * @return Device identifier.
     */
    public String getId() {

        return id;
    }

    /**
     * Returns the display name of the device.
     *
     * @return Device name.
     */
    public String getDeviceName() {

        return deviceName;
    }

    /**
     * Returns the hardware model.
     *
     * @return Device model.
     */
    public String getDeviceModel() {

        return deviceModel;
    }

    /**
     * Returns the verified public key.
     *
     * @return Public key.
     */
    public String getPublicKey() {

        return publicKey;
    }

    /**
     * Returns the registration timestamp.
     *
     * @return Registration timestamp.
     */
    public Timestamp getRegisteredAt() {

        return registeredAt == null ? null : new Timestamp(registeredAt.getTime());
    }

    /**
     * Returns the metadata payload.
     *
     * @return Metadata string.
     */
    public String getMetadata() {

        return metadata;
    }

    /**
     * Binds this device to the given user, producing a device that can be persisted.
     *
     * @param userId Identifier of the user that owns the device.
     * @return A device bound to the given user.
     */
    public Device bindTo(String userId) {

        return new Device.Builder()
                .userId(userId)
                .id(id)
                .deviceName(deviceName)
                .deviceModel(deviceModel)
                .publicKey(publicKey)
                .registeredAt(registeredAt == null ? null : new Timestamp(registeredAt.getTime()))
                .metadata(metadata)
                .build();
    }

    /**
     * Builder for {@link VerifiedDevice}.
     */
    public static class Builder {

        private String id;
        private String deviceName;
        private String deviceModel;
        private String publicKey;
        private Timestamp registeredAt;
        private String metadata;

        /**
         * Sets the device identifier.
         *
         * @param id Device identifier.
         * @return Builder instance.
         */
        public Builder id(String id) {

            this.id = id;
            return this;
        }

        /**
         * Sets the device name.
         *
         * @param deviceName Device name.
         * @return Builder instance.
         */
        public Builder deviceName(String deviceName) {

            this.deviceName = deviceName;
            return this;
        }

        /**
         * Sets the device model.
         *
         * @param deviceModel Device model.
         * @return Builder instance.
         */
        public Builder deviceModel(String deviceModel) {

            this.deviceModel = deviceModel;
            return this;
        }

        /**
         * Sets the public key.
         *
         * @param publicKey Public key.
         * @return Builder instance.
         */
        public Builder publicKey(String publicKey) {

            this.publicKey = publicKey;
            return this;
        }

        /**
         * Sets the registration timestamp.
         *
         * @param registeredAt Registration timestamp.
         * @return Builder instance.
         */
        public Builder registeredAt(Timestamp registeredAt) {

            this.registeredAt = registeredAt;
            return this;
        }

        /**
         * Sets metadata.
         *
         * @param metadata Metadata value.
         * @return Builder instance.
         */
        public Builder metadata(String metadata) {

            this.metadata = metadata;
            return this;
        }

        /**
         * Builds a verified, unbound device instance.
         *
         * @return Verified device.
         */
        public VerifiedDevice build() {

            return new VerifiedDevice(this);
        }
    }
}
