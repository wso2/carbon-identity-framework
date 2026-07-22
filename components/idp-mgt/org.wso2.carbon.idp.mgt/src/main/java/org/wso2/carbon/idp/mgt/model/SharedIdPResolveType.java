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

package org.wso2.carbon.idp.mgt.model;

/**
 * The resolution depth applied to a shared (shadow) identity provider when it is fetched. A shadow IDP stores only
 * its own local state; the rest of its configuration lives on the parent identity provider in the resident
 * organization and is overlaid at read time. This type selects how much of the parent is overlaid.
 *
 * <p>For a non-shared identity provider all three values are equivalent (no overlay is applied).</p>
 */
public enum SharedIdPResolveType {

    /**
     * The stored shadow only — no parent overlay. This is the true persisted state of the shadow, used by the
     * update flow so that parent-derived values are never round-tripped into the shadow's own row.
     */
    RAW,

    /**
     * The shadow plus the always-parent-derived base attributes (e.g. image, description, effective enabled state
     * and the resource identities). This is the management view.
     */
    BASE_PARENT,

    /**
     * The shadow fully resolved against the parent's configuration. This is the runtime (engagement) view.
     */
    FULL_PARENT
}
