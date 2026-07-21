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

package org.wso2.carbon.identity.device.mgt.api.model;

import java.sql.Timestamp;

/**
 * Immutable model for a registered device.
 */
public class Device {

    private final String id;
    private final String userId;
    private final String deviceName;
    private final String deviceModel;
    private final String publicKey;
    private final Status status;
    private final Timestamp registeredAt;
    private final String metadata;

    private Device(Builder builder) {

        this.id = builder.id;
        this.userId = builder.userId;
        this.deviceName = builder.deviceName;
        this.deviceModel = builder.deviceModel;
        this.publicKey = builder.publicKey;
        this.status = builder.status;
        this.registeredAt = builder.registeredAt;
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
     * Returns the user identifier.
     *
     * @return User identifier.
     */
    public String getUserId() {

        return userId;
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
     * Returns the registered public key.
     *
     * @return Public key.
     */
    public String getPublicKey() {

        return publicKey;
    }

    /**
     * Returns the current device status.
     *
     * @return Device status.
     */
    public Status getStatus() {

        return status;
    }

    /**
     * Returns the registration timestamp.
     *
     * @return Registration timestamp.
     */
    public Timestamp getRegisteredAt() {

        return registeredAt;
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
     * Builder for {@link Device}.
     */
    public static class Builder {

        private String id;
        private String userId;
        private String deviceName;
        private String deviceModel;
        private String publicKey;
        private Status status = Status.ACTIVE;
        private Timestamp registeredAt;
        private String metadata;

        /**
         * Creates an empty builder.
         */
        public Builder() {
        }

        /**
         * Creates a builder pre-populated with the fields of an existing device.
         *
         * @param device Source device to copy fields from.
         */
        public Builder(Device device) {

            this.id = device.id;
            this.userId = device.userId;
            this.deviceName = device.deviceName;
            this.deviceModel = device.deviceModel;
            this.publicKey = device.publicKey;
            this.status = device.status;
            this.registeredAt = device.registeredAt;
            this.metadata = device.metadata;
        }

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
         * Sets the user identifier.
         *
         * @param userId User identifier.
         * @return Builder instance.
         */
        public Builder userId(String userId) {

            this.userId = userId;
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
         * Sets the device status.
         *
         * @param status Device status.
         * @return Builder instance.
         */
        public Builder status(Status status) {

            this.status = status;
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
         * Builds a registered device instance.
         *
         * @return Registered device.
         */
        public Device build() {

            return new Device(this);
        }
    }

    /**
     * Device Status Enum.
     */
    public enum Status {
        ACTIVE,
        INACTIVE
    }
}
