/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.core.model;

/**
 * Legacy feature config.
 */
public class LegacyFeatureConfig {

    private final String id;
    private final String version;
    private final boolean isEnabled;

    public LegacyFeatureConfig(String id, String version, boolean isEnabled) {

        this.id = id;
        this.version = version;
        this.isEnabled = isEnabled;
    }

    public String getId() {

        return id;
    }

    public String getVersion() {

        return version;
    }

    public boolean isEnabled() {

        return isEnabled;
    }
}
