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

import org.testng.annotations.Test;
import org.wso2.carbon.identity.flow.extension.model.FlowContextHandoverConfig;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for {@link InFlowExtensionContextTreeBuilder}.
 *
 * <p>Uses {@link FlowContextHandoverConfigTestHelper} to construct configs with explicit
 * allow-lists without touching IdentityConfigParser.</p>
 */
public class InFlowExtensionContextTreeBuilderTest {

    // ========================= redirection always enabled =========================

    @Test
    public void testRedirectionAlwaysEnabled() {

        InFlowExtensionContextTreeMetadata meta = buildWith(
                new HashSet<>(Arrays.asList("tenantDomain")),
                new HashSet<>(), null);

        assertTrue(meta.isRedirectionEnabled(),
                "Redirection must always be true regardless of config");
    }

    // ========================= allowReadOnlyClaimsModification =========================

    @Test
    public void testAllowReadOnlyClaimsModificationForRegistration() {

        InFlowExtensionContextTreeMetadata meta = buildWith(
                new HashSet<>(), new HashSet<>(), "REGISTRATION");
        assertTrue(meta.isAllowReadOnlyClaimsModification());
    }

    @Test
    public void testAllowReadOnlyClaimsModificationForInvitedUserRegistration() {

        InFlowExtensionContextTreeMetadata meta = buildWith(
                new HashSet<>(), new HashSet<>(), "INVITED_USER_REGISTRATION");
        assertTrue(meta.isAllowReadOnlyClaimsModification());
    }

    @Test
    public void testAllowReadOnlyClaimsModificationFalseForPasswordRecovery() {

        InFlowExtensionContextTreeMetadata meta = buildWith(
                new HashSet<>(), new HashSet<>(), "PASSWORD_RECOVERY");
        assertFalse(meta.isAllowReadOnlyClaimsModification());
    }

    @Test
    public void testAllowReadOnlyClaimsModificationFalseForUnknownFlowType() {

        InFlowExtensionContextTreeMetadata meta = buildWith(
                new HashSet<>(), new HashSet<>(), "SOME_FUTURE_FLOW");
        assertFalse(meta.isAllowReadOnlyClaimsModification());
    }

    @Test
    public void testAllowReadOnlyClaimsModificationTrueForNullFlowType() {

        InFlowExtensionContextTreeMetadata meta = buildWith(
                new HashSet<>(), new HashSet<>(), null);
        assertTrue(meta.isAllowReadOnlyClaimsModification());
    }

    // ========================= flow branch =========================

    /**
     * With an empty allow-list the modifiable fields (claims, credentials, properties) are still
     * present but carry only MODIFY — no EXPOSE. Read-only fields (flow branch, user read-only
     * scalar) are absent because they have no modify path.
     */
    @Test
    public void testEmptyAllowListRestrictsExposeOnly() {

        InFlowExtensionContextTreeMetadata meta = buildWith(
                new HashSet<>(), new HashSet<>(), null);

        // Modifiable nodes always present.
        assertNotNull(findNode(meta, "user"),       "user node must be present (has modifiable children)");
        assertNotNull(findNode(meta, "properties"), "properties node must be present (modifiable)");

        // Read-only-only node absent when nothing configured.
        assertNull(findNode(meta, "flow"), "flow node must be absent when no flow attrs configured");

        // Properties has only MODIFY.
        InFlowExtensionContextTreeNode propsNode = findNode(meta, "properties");
        assertFalse(propsNode.getAllowedOperations().contains("EXPOSE"),
                "properties must not have EXPOSE when not in allow-list");
        assertTrue(propsNode.getAllowedOperations().contains("MODIFY"),
                "properties must have MODIFY regardless of allow-list");

        // Claims and credentials carry only MODIFY.
        List<InFlowExtensionContextTreeNode> userChildren = findNode(meta, "user").getChildren();
        InFlowExtensionContextTreeNode claimsNode = findChildNode(userChildren, "claims");
        assertNotNull(claimsNode, "claims always present");
        assertFalse(claimsNode.getAllowedOperations().contains("EXPOSE"),
                "claims must not have EXPOSE when not in allow-list");
        assertTrue(claimsNode.getAllowedOperations().contains("MODIFY"));

        InFlowExtensionContextTreeNode credNode = findChildNode(userChildren, "credentials");
        assertNotNull(credNode, "credentials always present");
        assertFalse(credNode.getAllowedOperations().contains("EXPOSE"),
                "credentials must not have EXPOSE when not in allow-list");
        assertTrue(credNode.getAllowedOperations().contains("MODIFY"));
    }

    @Test
    public void testFlowNodeAppearsWhenFlowAttrsPresent() {

        InFlowExtensionContextTreeMetadata meta = buildWith(
                new HashSet<>(Arrays.asList("tenantDomain", "flowType")),
                new HashSet<>(), null);

        InFlowExtensionContextTreeNode flowNode = findNode(meta, "flow");
        assertNotNull(flowNode, "flow node should be present");

        // Only the allowed attrs should appear as children.
        List<InFlowExtensionContextTreeNode> children = flowNode.getChildren();
        assertTrue(hasChildKey(children, "tenantDomain"), "tenantDomain expected");
        assertTrue(hasChildKey(children, "flowType"),     "flowType expected");
        assertFalse(hasChildKey(children, "applicationId"), "applicationId not in allow-list");
        assertFalse(hasChildKey(children, "callbackUrl"),   "callbackUrl not in allow-list");
        assertFalse(hasChildKey(children, "portalUrl"),     "portalUrl not in allow-list");
    }

    @Test
    public void testFlowNodeAbsentWhenNoFlowAttrsIncluded() {

        InFlowExtensionContextTreeMetadata meta = buildWith(
                new HashSet<>(Arrays.asList("username")),   // only user attr
                new HashSet<>(Arrays.asList("username")), null);

        assertNull(findNode(meta, "flow"), "flow node must not appear if no flow attrs present");
    }

    // ========================= user branch =========================

    @Test
    public void testReadOnlyUserFieldsAbsentWhenNoUserAttrsIncluded() {

        InFlowExtensionContextTreeMetadata meta = buildWith(
                new HashSet<>(Arrays.asList("tenantDomain")),
                new HashSet<>(), null);

        // User node is always present (claims + credentials always included).
        InFlowExtensionContextTreeNode userNode = findNode(meta, "user");
        assertNotNull(userNode, "user node must be present");

        List<InFlowExtensionContextTreeNode> children = userNode.getChildren();

        // Read-only user fields absent when not configured.
        assertFalse(hasChildKey(children, "id"),          "userId must not appear");
        assertFalse(hasChildKey(children, "username"),        "username must not appear");
        assertFalse(hasChildKey(children, "userStoreDomain"), "userStoreDomain must not appear");

        // Modifiable fields still present.
        assertTrue(hasChildKey(children, "claims"),      "claims must always be present");
        assertTrue(hasChildKey(children, "credentials"), "credentials must always be present");
    }

    @Test
    public void testUserNodeAppearsWithSelectedUserAttrs() {

        InFlowExtensionContextTreeMetadata meta = buildWith(
                new HashSet<>(Arrays.asList("tenantDomain")), // no flowUser
                new HashSet<>(Arrays.asList("username", "claims")), null);

        InFlowExtensionContextTreeNode userNode = findNode(meta, "user");
        assertNotNull(userNode, "user node should be present");

        List<InFlowExtensionContextTreeNode> children = userNode.getChildren();

        // Configured read-only field is exposed.
        assertTrue(hasChildKey(children, "username"), "username expected");

        // Claims configured → EXPOSE+MODIFY.
        InFlowExtensionContextTreeNode claimsNode = findChildNode(children, "claims");
        assertNotNull(claimsNode, "claims must be present");
        assertTrue(claimsNode.getAllowedOperations().contains("EXPOSE"), "claims must have EXPOSE");
        assertTrue(claimsNode.getAllowedOperations().contains("MODIFY"), "claims must have MODIFY");

        // Read-only fields not configured → absent.
        assertFalse(hasChildKey(children, "id"),          "userId not in allow-list");
        assertFalse(hasChildKey(children, "userStoreDomain"),  "userStoreDomain not in allow-list");

        // credentials not configured but always present → MODIFY only, no EXPOSE.
        InFlowExtensionContextTreeNode credNode = findChildNode(children, "credentials");
        assertNotNull(credNode, "credentials always present");
        assertFalse(credNode.getAllowedOperations().contains("EXPOSE"),
                "credentials must not have EXPOSE when not in allow-list");
        assertTrue(credNode.getAllowedOperations().contains("MODIFY"),
                "credentials must have MODIFY regardless");
    }

    @Test
    public void testFullUserPassthroughShowsAllUserChildren() {

        // "flowUser" in context attrs → fullUserPassthrough → all user children present.
        InFlowExtensionContextTreeMetadata meta = buildWith(
                new HashSet<>(Arrays.asList("flowUser")),
                new HashSet<>(), null);   // includedUserAttributes empty — ignored in passthrough

        InFlowExtensionContextTreeNode userNode = findNode(meta, "user");
        assertNotNull(userNode, "user node should be present with full passthrough");

        List<InFlowExtensionContextTreeNode> children = userNode.getChildren();
        assertTrue(hasChildKey(children, "id"),         "userId must appear in passthrough");
        assertTrue(hasChildKey(children, "username"),       "username must appear in passthrough");
        assertTrue(hasChildKey(children, "userStoreDomain"),"userStoreDomain must appear in passthrough");
        assertTrue(hasChildKey(children, "claims"),         "claims must appear in passthrough");
        assertTrue(hasChildKey(children, "credentials"),    "credentials must appear in passthrough");
    }

    // ========================= properties branch =========================

    @Test
    public void testPropertiesHasOnlyModifyWhenNotExposed() {

        InFlowExtensionContextTreeMetadata meta = buildWith(
                new HashSet<>(Arrays.asList("tenantDomain")), // properties not in allow-list
                new HashSet<>(), null);

        InFlowExtensionContextTreeNode propsNode = findNode(meta, "properties");
        assertNotNull(propsNode, "properties node must always be present");
        assertFalse(propsNode.getAllowedOperations().contains("EXPOSE"),
                "properties must not expose when not in allow-list");
        assertTrue(propsNode.getAllowedOperations().contains("MODIFY"),
                "properties must always allow MODIFY");
    }

    @Test
    public void testPropertiesHasExposeAndModifyWhenExposed() {

        InFlowExtensionContextTreeMetadata meta = buildWith(
                new HashSet<>(Arrays.asList("properties")),
                new HashSet<>(), null);

        InFlowExtensionContextTreeNode propsNode = findNode(meta, "properties");
        assertNotNull(propsNode, "properties node should be present");
        assertTrue(propsNode.getAllowedOperations().contains("EXPOSE"),
                "properties must have EXPOSE when in allow-list");
        assertTrue(propsNode.getAllowedOperations().contains("MODIFY"),
                "properties must always have MODIFY");
    }

    @Test
    public void testClaimsHasOnlyModifyWhenNotExposed() {

        InFlowExtensionContextTreeMetadata meta = buildWith(
                new HashSet<>(),
                new HashSet<>(), null); // "claims" not in userAttrs

        List<InFlowExtensionContextTreeNode> userChildren = findNode(meta, "user").getChildren();
        InFlowExtensionContextTreeNode claimsNode = findChildNode(userChildren, "claims");
        assertNotNull(claimsNode);
        assertFalse(claimsNode.getAllowedOperations().contains("EXPOSE"));
        assertTrue(claimsNode.getAllowedOperations().contains("MODIFY"));
    }

    @Test
    public void testClaimsHasExposeAndModifyWhenExposed() {

        InFlowExtensionContextTreeMetadata meta = buildWith(
                new HashSet<>(),
                new HashSet<>(Arrays.asList("claims")), null);

        List<InFlowExtensionContextTreeNode> userChildren = findNode(meta, "user").getChildren();
        InFlowExtensionContextTreeNode claimsNode = findChildNode(userChildren, "claims");
        assertNotNull(claimsNode);
        assertTrue(claimsNode.getAllowedOperations().contains("EXPOSE"));
        assertTrue(claimsNode.getAllowedOperations().contains("MODIFY"));
    }

    @Test
    public void testCredentialsHasOnlyModifyWhenNotExposed() {

        InFlowExtensionContextTreeMetadata meta = buildWith(
                new HashSet<>(),
                new HashSet<>(), null); // "userCredentials" not in userAttrs

        List<InFlowExtensionContextTreeNode> userChildren = findNode(meta, "user").getChildren();
        InFlowExtensionContextTreeNode credNode = findChildNode(userChildren, "credentials");
        assertNotNull(credNode);
        assertFalse(credNode.getAllowedOperations().contains("EXPOSE"));
        assertTrue(credNode.getAllowedOperations().contains("MODIFY"));
    }

    @Test
    public void testCredentialsHasExposeAndModifyWhenExposed() {

        InFlowExtensionContextTreeMetadata meta = buildWith(
                new HashSet<>(),
                new HashSet<>(Arrays.asList("userCredentials")), null);

        List<InFlowExtensionContextTreeNode> userChildren = findNode(meta, "user").getChildren();
        InFlowExtensionContextTreeNode credNode = findChildNode(userChildren, "credentials");
        assertNotNull(credNode);
        assertTrue(credNode.getAllowedOperations().contains("EXPOSE"));
        assertTrue(credNode.getAllowedOperations().contains("MODIFY"));
    }

    @Test
    public void testFullPassthroughGivesExposeOnClaimsAndCredentials() {

        // flowUser in attrs → full passthrough → all user fields exposed.
        InFlowExtensionContextTreeMetadata meta = buildWith(
                new HashSet<>(Arrays.asList("flowUser")),
                new HashSet<>(), null);

        List<InFlowExtensionContextTreeNode> userChildren = findNode(meta, "user").getChildren();
        assertTrue(findChildNode(userChildren, "claims").getAllowedOperations().contains("EXPOSE"));
        assertTrue(findChildNode(userChildren, "credentials").getAllowedOperations().contains("EXPOSE"));
    }

    // ========================= flowType metadata field =========================

    @Test
    public void testFlowTypeFieldPreserved() {

        InFlowExtensionContextTreeMetadata meta = buildWith(
                new HashSet<>(), new HashSet<>(), "REGISTRATION");

        assertEquals(meta.getFlowType(), "REGISTRATION");
    }

    @Test
    public void testFlowTypeNullPreserved() {

        InFlowExtensionContextTreeMetadata meta = buildWith(
                new HashSet<>(), new HashSet<>(), null);

        assertNull(meta.getFlowType());
    }

    // ========================= resolveAllowReadOnlyClaimsModification (static) =========================

    @Test
    public void testResolveAllowReadOnlyClaimsModificationDirectly() {

        assertTrue(InFlowExtensionContextTreeBuilder.resolveAllowReadOnlyClaimsModification(null));
        assertTrue(InFlowExtensionContextTreeBuilder.resolveAllowReadOnlyClaimsModification("REGISTRATION"));
        assertTrue(InFlowExtensionContextTreeBuilder
                .resolveAllowReadOnlyClaimsModification("INVITED_USER_REGISTRATION"));
        assertFalse(InFlowExtensionContextTreeBuilder
                .resolveAllowReadOnlyClaimsModification("PASSWORD_RECOVERY"));
        assertFalse(InFlowExtensionContextTreeBuilder
                .resolveAllowReadOnlyClaimsModification("UNKNOWN_TYPE"));
    }

    // ========================= helpers =========================

    private InFlowExtensionContextTreeMetadata buildWith(Set<String> attrs,
                                                         Set<String> userAttrs,
                                                         String flowType) {

        FlowContextHandoverConfig cfg = FlowContextHandoverConfigTestHelper.of(attrs, userAttrs);
        return new InFlowExtensionContextTreeBuilder(cfg).build(flowType);
    }

    private InFlowExtensionContextTreeNode findNode(InFlowExtensionContextTreeMetadata meta,
                                                     String key) {

        if (meta.getContextTree() == null) {
            return null;
        }
        for (InFlowExtensionContextTreeNode node : meta.getContextTree()) {
            if (key.equals(node.getKey())) {
                return node;
            }
        }
        return null;
    }

    private boolean hasChildKey(List<InFlowExtensionContextTreeNode> children, String key) {

        if (children == null) {
            return false;
        }
        for (InFlowExtensionContextTreeNode child : children) {
            if (key.equals(child.getKey())) {
                return true;
            }
        }
        return false;
    }

    private InFlowExtensionContextTreeNode findChildNode(List<InFlowExtensionContextTreeNode> children,
                                                         String key) {

        if (children == null) {
            return null;
        }
        for (InFlowExtensionContextTreeNode child : children) {
            if (key.equals(child.getKey())) {
                return child;
            }
        }
        return null;
    }
}
