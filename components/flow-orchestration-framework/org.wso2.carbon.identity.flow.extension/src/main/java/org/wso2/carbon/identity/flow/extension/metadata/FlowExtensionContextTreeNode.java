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

package org.wso2.carbon.identity.flow.extension.metadata;

import java.util.Collections;
import java.util.List;

/**
 * Single node in the In-Flow Extension context tree returned by the metadata service.
 * Mirrors the shape of {@code default-flow-context-tree.json} on the Console side so the
 * existing tree component can render this without translation.
 */
public class FlowExtensionContextTreeNode {

    private final String key;
    private final String title;
    private final String path;
    private final String dataType;
    private final String nodeType;
    private final List<String> allowedOperations;
    private final boolean readOnly;
    private final boolean replaceable;
    private final boolean dynamicEntryAllowed;
    private final String dynamicEntryType;
    private final List<FlowExtensionContextTreeNode> children;

    private FlowExtensionContextTreeNode(Builder b) {

        this.key = b.key;
        this.title = b.title;
        this.path = b.path;
        this.dataType = b.dataType;
        this.nodeType = b.nodeType;
        this.allowedOperations = b.allowedOperations != null
                ? Collections.unmodifiableList(b.allowedOperations) : Collections.emptyList();
        this.readOnly = b.readOnly;
        this.replaceable = b.replaceable;
        this.dynamicEntryAllowed = b.dynamicEntryAllowed;
        this.dynamicEntryType = b.dynamicEntryType;
        this.children = b.children != null
                ? Collections.unmodifiableList(b.children) : Collections.emptyList();
    }

    public String getKey() {

        return key;
    }

    public String getTitle() {

        return title;
    }

    public String getPath() {

        return path;
    }

    public String getDataType() {

        return dataType;
    }

    public String getNodeType() {

        return nodeType;
    }

    public List<String> getAllowedOperations() {

        return allowedOperations;
    }

    public boolean isReadOnly() {

        return readOnly;
    }

    public boolean isReplaceable() {

        return replaceable;
    }

    public boolean isDynamicEntryAllowed() {

        return dynamicEntryAllowed;
    }

    public String getDynamicEntryType() {

        return dynamicEntryType;
    }

    public List<FlowExtensionContextTreeNode> getChildren() {

        return children;
    }

    public static Builder builder() {

        return new Builder();
    }

    public static final class Builder {

        private String key;
        private String title;
        private String path;
        private String dataType = "";
        private String nodeType;
        private List<String> allowedOperations;
        private boolean readOnly;
        private boolean replaceable;
        private boolean dynamicEntryAllowed;
        private String dynamicEntryType;
        private List<FlowExtensionContextTreeNode> children;

        public Builder key(String v) {

            this.key = v;
            return this;
        }

        public Builder title(String v) {

            this.title = v;
            return this;
        }

        public Builder path(String v) {

            this.path = v;
            return this;
        }

        public Builder dataType(String v) {

            this.dataType = v;
            return this;
        }

        public Builder nodeType(String v) {

            this.nodeType = v;
            return this;
        }

        public Builder allowedOperations(List<String> v) {

            this.allowedOperations = v;
            return this;
        }

        public Builder readOnly(boolean v) {

            this.readOnly = v;
            return this;
        }

        public Builder replaceable(boolean v) {

            this.replaceable = v;
            return this;
        }

        public Builder dynamicEntryAllowed(boolean v) {

            this.dynamicEntryAllowed = v;
            return this;
        }

        public Builder dynamicEntryType(String v) {

            this.dynamicEntryType = v;
            return this;
        }

        public Builder children(List<FlowExtensionContextTreeNode> v) {

            this.children = v;
            return this;
        }

        public FlowExtensionContextTreeNode build() {

            return new FlowExtensionContextTreeNode(this);
        }
    }
}
