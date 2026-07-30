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

package org.wso2.carbon.identity.device.policy.api.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Defines device policy fields and the platforms they apply to.
 */
public enum DeviceField {

    PLATFORM("platform", EnumSet.allOf(Platform.class)),
    LOCK_SCREEN("lockScreen", EnumSet.allOf(Platform.class)),

    ANDROID_OS_VERSION("androidOsVersion", EnumSet.of(Platform.ANDROID)),
    IS_ROOTED("isRooted", EnumSet.of(Platform.ANDROID)),
    USB_DEBUGGING("usbDebugging", EnumSet.of(Platform.ANDROID)),
    HARDWARE_KEYSTORE("hardwareKeystore", EnumSet.of(Platform.ANDROID)),
    BIOMETRIC("biometric", EnumSet.of(Platform.ANDROID)),
    SCREEN_LOCK_COMPLEXITY("screenLockComplexity", EnumSet.of(Platform.ANDROID)),

    IOS_OS_VERSION("iosOsVersion", EnumSet.of(Platform.IOS)),
    IOS_INTEGRITY("iosIntegrity", EnumSet.of(Platform.IOS)),
    PASSCODE("passcode", EnumSet.of(Platform.IOS)),
    TOUCH_ID_OR_FACE_ID("touchIdOrFaceId", EnumSet.of(Platform.IOS)),
    JAILBREAK("jailbreak", EnumSet.of(Platform.IOS)),

    MACOS_OS_VERSION("macosOsVersion", EnumSet.of(Platform.MACOS)),
    WINDOWS_OS_VERSION("windowsOsVersion", EnumSet.of(Platform.WINDOWS)),

    DISK_ENCRYPTION("diskEncryption", EnumSet.of(Platform.ANDROID, Platform.MACOS, Platform.WINDOWS)),

    NETWORK_PROXIES("networkProxies", EnumSet.of(Platform.ANDROID)),
    WIFI_NETWORK_SECURITY("wifiNetworkSecurity", EnumSet.of(Platform.ANDROID)),
    ANDROID_INTEGRITY("androidIntegrity", EnumSet.of(Platform.ANDROID)),

    SECURE_ENCLAVE("secureEnclave", EnumSet.of(Platform.MACOS)),

    WINDOWS_HELLO("windowsHello", EnumSet.of(Platform.WINDOWS)),
    TRUSTED_PLATFORM_MODULE("trustedPlatformModule", EnumSet.of(Platform.WINDOWS));

    /**
     * Reverse index of platform to the wire names of fields applicable to it, precomputed
     * once at class load time rather than rebuilt on every lookup. Built from
     * {@link #appliesTo(Platform)} so the applicability logic stays defined in one place.
     */
    private static final Map<Platform, List<String>> FIELDS_BY_PLATFORM;

    /**
     * All field wire names, in declaration order, precomputed once at class load time.
     */
    private static final List<String> ALL_FIELD_NAMES;

    static {
        Map<Platform, List<String>> fieldsByPlatform = new EnumMap<>(Platform.class);
        for (Platform platform : Platform.values()) {
            List<String> fields = new ArrayList<>();
            for (DeviceField field : DeviceField.values()) {
                if (field.appliesTo(platform)) {
                    fields.add(field.getName());
                }
            }
            fieldsByPlatform.put(platform, Collections.unmodifiableList(fields));
        }
        FIELDS_BY_PLATFORM = Collections.unmodifiableMap(fieldsByPlatform);

        List<String> allFieldNames = new ArrayList<>();
        for (DeviceField field : DeviceField.values()) {
            allFieldNames.add(field.getName());
        }
        ALL_FIELD_NAMES = Collections.unmodifiableList(allFieldNames);
    }

    private final String name;
    private final Set<Platform> platforms;

    DeviceField(String name, Set<Platform> platforms) {

        this.name = name;
        this.platforms = Collections.unmodifiableSet(EnumSet.copyOf(platforms));
    }

    /**
     * Returns the authoritative wire name of the device field.
     *
     * @return Field wire name.
     */
    public String getName() {

        return name;
    }

    /**
     * Returns an unmodifiable set of platforms this field applies to.
     *
     * @return Set of applicable {@link Platform} values.
     */
    public Set<Platform> getPlatforms() {

        return platforms;
    }

    /**
     * Checks if this field applies to the specified platform.
     *
     * @param platform Platform to check, or {@code null} for all platforms.
     * @return {@code true} if platform is null or if this field applies to the platform.
     */
    public boolean appliesTo(Platform platform) {

        return platform == null || platforms.contains(platform);
    }

    /**
     * Returns the wire names of the fields applicable to the specified platform, looked up
     * from the precomputed reverse index rather than recomputed per call.
     *
     * @param platform Platform to look up, or {@code null} for every known field.
     * @return List of applicable field wire names.
     */
    public static List<String> getFieldNamesForPlatform(Platform platform) {

        if (platform == null) {
            return ALL_FIELD_NAMES;
        }
        return FIELDS_BY_PLATFORM.getOrDefault(platform, Collections.emptyList());
    }

    /**
     * Returns the {@link DeviceField} matching the given wire name.
     *
     * @param name Field wire name to look up.
     * @return Matching {@link DeviceField} constant, or {@code null} if unknown or null.
     */
    public static DeviceField fromName(String name) {

        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        for (DeviceField field : DeviceField.values()) {
            if (field.getName().equals(name.trim())) {
                return field;
            }
        }
        return null;
    }
}
