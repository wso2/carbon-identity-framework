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

package org.wso2.carbon.identity.flow.extensions.util;

import org.wso2.carbon.identity.flow.extensions.InFlowExtensionConstants;

import java.util.List;

/**
 * Path-matching utilities for In-Flow Extension access control.
 */
public final class InFlowExtensionPathUtil {

    private InFlowExtensionPathUtil() {

    }

    /**
     * Returns {@code true} if the path is in the read-only {@code /flow/} area.
     */
    public static boolean isReadOnly(String path) {

        if (path == null) {
            return false;
        }
        return path.startsWith(InFlowExtensionConstants.FLOW_PREFIX);
    }

    /**
     * Returns {@code true} if at least one leaf path in {@code leafPaths} starts with
     * {@code areaPrefix}. Used as an area-gate before iterating a data block.
     */
    public static boolean anyExposedUnder(String areaPrefix, List<String> leafPaths) {

        if (areaPrefix == null || leafPaths == null || leafPaths.isEmpty()) {
            return false;
        }
        for (String path : leafPaths) {
            if (path != null && path.startsWith(areaPrefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if {@code leafPath} is present in {@code leafPaths}.
     */
    public static boolean isExposedPath(String leafPath, List<String> leafPaths) {

        if (leafPath == null || leafPaths == null || leafPaths.isEmpty()) {
            return false;
        }
        return leafPaths.contains(leafPath);
    }
}
