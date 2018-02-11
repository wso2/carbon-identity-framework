/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js;

import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.common.model.Claim;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Javascript wrapper for Java level User Claims.
 */
public class JsClaimSet extends AbstractJSObjectWrapper<Map<ClaimMapping, String>> {

    private JsClaimView localView;
    private JsClaimView remoteView;

    public JsClaimSet(Map<ClaimMapping, String> wrapped) {

        super(wrapped);
        localView = new JsClaimView(wrapped, claimMapping -> claimMapping.getLocalClaim().getClaimUri());
        remoteView = new JsClaimView(wrapped, claimMapping -> claimMapping.getRemoteClaim().getClaimUri());
    }

    @Override
    public Object getMember(String name) {

        switch (name) {
        case FrameworkConstants.JSAttributes.JS_CLAIM_MEMBER_LOCAL:
            return localView;
        case FrameworkConstants.JSAttributes.JS_CLAIM_MEMBER_REMOTE:
            return remoteView;
        case FrameworkConstants.JSAttributes.JS_CLAIM_MEMBER_PUSH:
            return (Consumer<Object>) this::addClaim;
        default:
            return super.getMember(name);
        }
    }

    @Override
    public boolean hasMember(String name) {

        switch (name) {

        default:
            return super.hasMember(name);
        }
    }

    /**
     * Checks if claim mappings is empty.
     * @return
     */
    public boolean isEmpty() {

        return getWrapped().isEmpty();
    }

    /**
     * Function to add claim.
     * Used in javascript
     * <code>
     *       user.claims.put(someClaim);
     * </code>
     * @param o
     */
    private void addClaim(Object o) {

        if (o instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) o;
            Object local = map.get(FrameworkConstants.JSAttributes.JS_CLAIM_MEMBER_LOCAL);
            Object remote = map.get(FrameworkConstants.JSAttributes.JS_CLAIM_MEMBER_REMOTE);
            Object value = map.get(FrameworkConstants.JSAttributes.JS_CLAIM_MEMBER_VALUE);

            if (local instanceof Map && remote instanceof Map && value instanceof String) {
                Map<String, String> localClaimConfig = (Map<String, String>) local;
                Map<String, String> remoteClaimConfig = (Map<String, String>) remote;

                addClaim(localClaimConfig, remoteClaimConfig, (String) value);
            } else {
                JsLogger.getInstance()
                        .error("Adding a claim to claims[] requires, local.uri, remote.uri and a value. Offending script fragment :"
                                + o.toString());
            }
        }
    }

    /**
     * Add claim mapings given the local and remote claim in Javascript level map interface.
     *
     * @param localClaimConfig
     * @param remoteClaimConfig
     * @param value
     */
    private void addClaim(Map<String, String> localClaimConfig, Map<String, String> remoteClaimConfig, String value) {

        Claim localClaim = new Claim();
        Claim remoteClaim = new Claim();
        localClaim.setClaimUri(localClaimConfig.get(FrameworkConstants.JSAttributes.JS_CLAIM_MEMBER_URI));
        remoteClaim.setClaimUri(remoteClaimConfig.get(FrameworkConstants.JSAttributes.JS_CLAIM_MEMBER_URI));

        ClaimMapping claimMapping = new ClaimMapping();
        claimMapping.setLocalClaim(localClaim);
        claimMapping.setRemoteClaim(remoteClaim);
        getWrapped().put(claimMapping, value);
    }
}
