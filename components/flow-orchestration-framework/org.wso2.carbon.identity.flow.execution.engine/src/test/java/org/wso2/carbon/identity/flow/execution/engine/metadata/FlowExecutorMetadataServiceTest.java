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

package org.wso2.carbon.identity.flow.execution.engine.metadata;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.flow.execution.engine.graph.AuthenticationExecutor;
import org.wso2.carbon.identity.flow.execution.engine.graph.Executor;
import org.wso2.carbon.identity.flow.execution.engine.internal.FlowExecutionEngineDataHolder;
import org.wso2.carbon.identity.flow.execution.engine.model.ExecutorResponse;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.mgt.Constants.FlowTypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.identity.flow.mgt.Constants.ExecutorBehaviorFlags.RECOVERY_FACTOR;
import static org.wso2.carbon.identity.flow.mgt.Constants.FlowTypes.INVITED_USER_REGISTRATION;
import static org.wso2.carbon.identity.flow.mgt.Constants.FlowTypes.PASSWORD_RECOVERY;
import static org.wso2.carbon.identity.flow.mgt.Constants.FlowTypes.REGISTRATION;

/**
 * Unit tests for {@link FlowExecutorMetadataService} and the metadata resolution behind it.
 */
public class FlowExecutorMetadataServiceTest {

    private static final String PLAIN_EXECUTOR = "PasswordProvisioningExecutor";
    private static final String DECLARING_EXECUTOR = "CustomVerificationExecutor";
    private static final String AUTH_EXECUTOR = "GoogleExecutor";
    private static final String FAULTY_EXECUTOR = "FaultyExecutor";
    private static final String LATE_EXECUTOR = "LateArrivingExecutor";
    private static final String ALL_FLOW_TYPES_EXECUTOR = "AllFlowTypesExecutor";
    private static final String MISDECLARED_EXECUTOR = "MisdeclaredExecutor";

    private final Map<String, Executor> originalExecutors = new HashMap<>();
    private final FlowExecutorMetadataService metadataService = FlowExecutorMetadataService.getInstance();

    /**
     * Takes the executor registry over for the duration of this class. The registry is static and
     * shared across the suite, so whatever other tests put in it is stashed and handed back by
     * {@link #restoreRegistry()}.
     */
    @BeforeClass
    public void isolateRegistry() {

        originalExecutors.putAll(FlowExecutionEngineDataHolder.getInstance().getExecutors());
        FlowExecutionEngineDataHolder.getInstance().getExecutors().clear();
    }

    /**
     * Hands the executor registry back to the rest of the suite in the state
     * {@link #isolateRegistry()} found it in.
     */
    @AfterClass
    public void restoreRegistry() {

        FlowExecutionEngineDataHolder.getInstance().getExecutors().clear();
        FlowExecutionEngineDataHolder.getInstance().getExecutors().putAll(originalExecutors);
    }

    /**
     * Empties the registry between test methods so each one starts from a known set of executors and
     * cannot see what a previous method registered.
     */
    @AfterMethod
    public void clearRegistry() {

        FlowExecutionEngineDataHolder.getInstance().getExecutors().clear();
    }

    @Test
    public void testExecutorDeclaringNothingIsNotOfferedAsAStep() {

        register(new PlainExecutor());

        assertTrue(metadataService.getSupportedExtensionExecutors(REGISTRATION.name()).isEmpty(),
                "An executor that declares no flow type must not be offered as a composer step.");
        assertTrue(metadataService.getSupportedExtensionExecutors(PASSWORD_RECOVERY.name()).isEmpty());
        assertTrue(metadataService.getSupportedExtensionExecutors(INVITED_USER_REGISTRATION.name()).isEmpty());
    }

    @Test
    public void testExecutorDeclaringNothingStillResolvesWithDerivedDefaults() {

        register(new PlainExecutor());

        List<FlowExecutorInfo> all = metadataService.getExecutors();
        assertEquals(all.size(), 1);

        FlowExecutorInfo info = all.get(0);
        assertEquals(info.getName(), PLAIN_EXECUTOR);
        assertEquals(info.getDisplayName(), PLAIN_EXECUTOR,
                "Display name should fall back to the executor name.");
        assertNull(info.getDescription());
        assertNull(info.getIcon());
        assertNull(info.getAssociatedAuthenticator());
        assertTrue(info.getBehaviorFlags().isEmpty());
        assertTrue(info.getSupportedFlowTypes().isEmpty());
        assertFalse(info.isMetadataDeclared());
        assertFalse(info.isIdpRequired());
        assertFalse(info.isConnectionRequired());
    }

    @Test
    public void testDeclaredMetadataIsSurfaced() {

        register(new DeclaringExecutor());

        FlowExecutorInfo info = metadataService.getExecutor(DECLARING_EXECUTOR).orElse(null);
        assertNotNull(info);
        assertEquals(info.getDisplayName(), "Custom Verification");
        assertEquals(info.getDescription(), "Verifies the user with an external verification service.");
        assertEquals(info.getIcon(), "assets/images/logos/custom-verification.svg");
        assertEquals(info.getBehaviorFlags(),
                Collections.singletonList(RECOVERY_FACTOR));
        assertEquals(info.getAssociatedAuthenticator(), "CustomVerificationAuthenticator");
        assertTrue(info.isConnectionRequired());
        assertTrue(info.isMetadataDeclared());
        assertEquals(info.getSupportedFlowTypes(), EnumSet.of(REGISTRATION, PASSWORD_RECOVERY));
    }

    @Test
    public void testComposerExecutorsAreFilteredByDeclaredFlowType() {

        register(new DeclaringExecutor());

        assertEquals(names(metadataService.getSupportedExtensionExecutors(REGISTRATION.name())),
                Collections.singletonList(DECLARING_EXECUTOR));
        assertEquals(names(metadataService.getSupportedExtensionExecutors(PASSWORD_RECOVERY.name())),
                Collections.singletonList(DECLARING_EXECUTOR));
        assertTrue(metadataService.getSupportedExtensionExecutors(INVITED_USER_REGISTRATION.name()).isEmpty(),
                "A flow type the executor did not declare must not include it.");
    }

    @Test
    public void testFlowTypeCanBeResolvedByTypeValueAndIsCaseInsensitive() {

        register(new DeclaringExecutor());

        assertEquals(names(metadataService.getSupportedExtensionExecutors(REGISTRATION.getType())),
                Collections.singletonList(DECLARING_EXECUTOR));
        assertEquals(names(metadataService.getSupportedExtensionExecutors("registration")),
                Collections.singletonList(DECLARING_EXECUTOR));
    }

    @Test
    public void testBlankOrUnknownFlowTypeYieldsNoComposerExecutors() {

        register(new DeclaringExecutor());

        assertTrue(metadataService.getSupportedExtensionExecutors(null).isEmpty());
        assertTrue(metadataService.getSupportedExtensionExecutors("").isEmpty());
        assertTrue(metadataService.getSupportedExtensionExecutors("ASK_PASSWORD").isEmpty());
    }

    @Test
    public void testExecutorDeclaringAllIsOfferedInEveryFlowType() {

        register(new AllFlowTypesExecutor());

        /*
         * Asserted against FlowTypes.values() rather than a hardcoded list, so a flow type added to
         * the product is automatically covered by this test too.
         */
        for (FlowTypes flowType : FlowTypes.values()) {
            assertEquals(names(metadataService.getSupportedExtensionExecutors(flowType.name())),
                    Collections.singletonList(ALL_FLOW_TYPES_EXECUTOR),
                    "An executor declaring every flow type must be offered in flow type: " + flowType.name());
        }
    }

    @Test
    public void testDeclaringEveryFlowTypeResolvesToTheFullSet() {

        register(new AllFlowTypesExecutor());

        FlowExecutorInfo info = metadataService.getExecutor(ALL_FLOW_TYPES_EXECUTOR).orElse(null);
        assertNotNull(info);
        assertEquals(info.getSupportedFlowTypes(), EnumSet.allOf(FlowTypes.class),
                "The declared set must resolve to every flow type in flow-mgt Constants.");
        for (FlowTypes flowType : FlowTypes.values()) {
            assertTrue(info.supportsFlowType(flowType));
        }
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testExecutorDeclaringMetadataWithoutFlowTypesIsRejected() {

        register(new MetadataWithoutFlowTypesExecutor());
    }

    @Test
    public void testRejectedExecutorIsNotRegistered() {

        try {
            register(new MetadataWithoutFlowTypesExecutor());
        } catch (IllegalStateException e) {
            // Expected; the assertion below is the point of this test.
        }
        assertFalse(metadataService.isExecutorRegistered(MISDECLARED_EXECUTOR),
                "An executor rejected at registration must not end up in the registry.");
    }

    @Test
    public void testExecutorDeclaringNeitherMetadataNorFlowTypesIsAccepted() {

        /*
         * The executors that consumers offer from their own hardcoded lists declare nothing at all.
         * They must keep registering, which is what stops this validation from being a breaking change.
         */
        register(new PlainExecutor());

        assertTrue(metadataService.isExecutorRegistered(PLAIN_EXECUTOR));
    }

    @Test
    public void testAuthenticationExecutorRequiresAnIdentityProvider() {

        register(new AuthExecutor());

        FlowExecutorInfo info = metadataService.getExecutor(AUTH_EXECUTOR).orElse(null);
        assertNotNull(info);
        assertTrue(info.isIdpRequired(),
                "An AuthenticationExecutor must be reported as needing an identity provider.");
        assertEquals(names(metadataService.getSupportedExtensionExecutors(REGISTRATION.name())),
                Collections.singletonList(AUTH_EXECUTOR));
    }

    @Test
    public void testFaultyExecutorDoesNotBreakResolution() {

        register(new FaultyExecutor());
        register(new DeclaringExecutor());

        // The faulty executor degrades to derived defaults instead of failing the whole lookup.
        FlowExecutorInfo faulty = metadataService.getExecutor(FAULTY_EXECUTOR).orElse(null);
        assertNotNull(faulty);
        assertEquals(faulty.getDisplayName(), FAULTY_EXECUTOR);
        assertTrue(faulty.getSupportedFlowTypes().isEmpty());

        assertEquals(names(metadataService.getSupportedExtensionExecutors(REGISTRATION.name())),
                Collections.singletonList(DECLARING_EXECUTOR));
    }

    @Test
    public void testLateRegisteredExecutorIsVisibleOnTheNextCall() {

        register(new DeclaringExecutor());
        assertEquals(names(metadataService.getSupportedExtensionExecutors(REGISTRATION.name())),
                Collections.singletonList(DECLARING_EXECUTOR));

        // Simulates an executor bundle activating after the first metadata read, as can happen while
        // the server is still starting up; nothing may be cached from that earlier call.
        register(new LateExecutor());

        assertEquals(names(metadataService.getSupportedExtensionExecutors(REGISTRATION.name())),
                Arrays.asList(DECLARING_EXECUTOR, LATE_EXECUTOR),
                "An executor registered after an earlier read must appear on the next call.");
    }

    @Test
    public void testUnregisteringAnExecutorRemovesItFromMetadata() {

        LateExecutor executor = new LateExecutor();
        register(executor);
        assertTrue(metadataService.isExecutorRegistered(LATE_EXECUTOR));

        FlowExecutionEngineDataHolder.getInstance().removeExecutor(executor);

        assertFalse(metadataService.isExecutorRegistered(LATE_EXECUTOR));
        assertEquals(metadataService.getExecutor(LATE_EXECUTOR), Optional.empty());
        assertTrue(metadataService.getSupportedExtensionExecutors(REGISTRATION.name()).isEmpty());
    }

    @Test
    public void testUnregisteringAShadowedExecutorLeavesTheRegisteredOneInPlace() {

        // Two bundles contributing the same executor name: the second wins the map entry.
        LateExecutor shadowed = new LateExecutor();
        LateExecutor winner = new LateExecutor();
        register(shadowed);
        register(winner);

        // The loser's bundle stops. Removal is identity based, so the winner must survive it.
        FlowExecutionEngineDataHolder.getInstance().removeExecutor(shadowed);
        assertTrue(metadataService.isExecutorRegistered(LATE_EXECUTOR),
                "Unbinding the shadowed executor must not delete the still active registration.");

        FlowExecutionEngineDataHolder.getInstance().removeExecutor(winner);
        assertFalse(metadataService.isExecutorRegistered(LATE_EXECUTOR),
                "Unbinding the registered executor must delete its entry.");
    }

    @Test
    public void testExecutorWithABlankNameIsNotRegistered() {

        // The name is the only handle a flow step has, so a blank one is unusable rather than merely odd.
        FlowExecutionEngineDataHolder.getInstance().addExecutor(new BlankNameExecutor());
        FlowExecutionEngineDataHolder.getInstance().removeExecutor(new BlankNameExecutor());
        assertTrue(metadataService.getExecutors().isEmpty(),
                "An executor reporting a blank name must never enter the registry.");
    }

    @Test
    public void testExecutorFailingWithALinkageErrorDoesNotBreakResolution() {

        // The characteristic failure of a connector built against another framework version is an
        // Error, not an exception, and it must be contained the same way.
        register(new LinkageFaultyExecutor());
        register(new DeclaringExecutor());

        FlowExecutorInfo faulty = metadataService.getExecutor(FAULTY_EXECUTOR).orElse(null);
        assertNotNull(faulty);
        assertEquals(faulty.getDisplayName(), FAULTY_EXECUTOR);
        assertTrue(faulty.getSupportedFlowTypes().isEmpty());

        assertEquals(names(metadataService.getSupportedExtensionExecutors(REGISTRATION.name())),
                Collections.singletonList(DECLARING_EXECUTOR));
    }

    @Test
    public void testRegisteredExecutorNamesAreSorted() {

        register(new PlainExecutor());
        register(new DeclaringExecutor());
        register(new AuthExecutor());

        assertEquals(new ArrayList<>(metadataService.getRegisteredExecutorNames()),
                Arrays.asList(DECLARING_EXECUTOR, AUTH_EXECUTOR, PLAIN_EXECUTOR));
    }

    @Test
    public void testLookupOfUnknownExecutor() {

        assertEquals(metadataService.getExecutor("NoSuchExecutor"), Optional.empty());
        assertEquals(metadataService.getExecutor(null), Optional.empty());
        assertFalse(metadataService.isExecutorRegistered("NoSuchExecutor"));
        assertFalse(metadataService.isExecutorRegistered(null));
    }

    private void register(Executor executor) {

        FlowExecutionEngineDataHolder.getInstance().addExecutor(executor);
    }

    private List<String> names(List<FlowExecutorInfo> executors) {

        return executors.stream().map(FlowExecutorInfo::getName).toList();
    }

    /**
     * Stands in for the executors that predate the metadata SPI: declares nothing at all.
     */
    private static class PlainExecutor implements Executor {

        @Override
        public String getName() {

            return PLAIN_EXECUTOR;
        }

        @Override
        public ExecutorResponse execute(FlowExecutionContext context) {

            return null;
        }

        @Override
        public List<String> getInitiationData() {

            return Collections.emptyList();
        }

        @Override
        public ExecutorResponse rollback(FlowExecutionContext context) {

            return null;
        }
    }

    /**
     * Stands in for a connector that opts in fully, declaring every property the metadata SPI offers.
     */
    private static class DeclaringExecutor extends PlainExecutor {

        @Override
        public String getName() {

            return DECLARING_EXECUTOR;
        }

        @Override
        public Set<FlowTypes> getSupportedFlowTypes() {

            return EnumSet.of(REGISTRATION, PASSWORD_RECOVERY);
        }

        @Override
        public FlowExecutorMetadata getExecutorMetadata() {

            return FlowExecutorMetadata.builder()
                    .displayName("Custom Verification")
                    .description("Verifies the user with an external verification service.")
                    .icon("assets/images/logos/custom-verification.svg")
                    .behaviorFlags(Collections.singletonList(RECOVERY_FACTOR))
                    .associatedAuthenticator("CustomVerificationAuthenticator")
                    .connectionRequired(true)
                    .build();
        }
    }

    /**
     * Declares composer metadata but no flow type, so it would be described and offered nowhere.
     */
    private static class MetadataWithoutFlowTypesExecutor extends PlainExecutor {

        @Override
        public String getName() {

            return MISDECLARED_EXECUTOR;
        }

        @Override
        public FlowExecutorMetadata getExecutorMetadata() {

            return FlowExecutorMetadata.builder()
                    .displayName("Misdeclared Verification")
                    .build();
        }
    }

    private static class LateExecutor extends PlainExecutor {

        @Override
        public String getName() {

            return LATE_EXECUTOR;
        }

        @Override
        public Set<FlowTypes> getSupportedFlowTypes() {

            return EnumSet.of(REGISTRATION);
        }
    }

    /**
     * Declares support for every flow type at once rather than enumerating them.
     */
    private static class AllFlowTypesExecutor extends PlainExecutor {

        @Override
        public String getName() {

            return ALL_FLOW_TYPES_EXECUTOR;
        }

        @Override
        public Set<FlowTypes> getSupportedFlowTypes() {

            return EnumSet.allOf(FlowTypes.class);
        }
    }

    /**
     * A badly behaved third party executor: both metadata hooks blow up.
     */
    private static class FaultyExecutor extends PlainExecutor {

        @Override
        public String getName() {

            return FAULTY_EXECUTOR;
        }

        @Override
        public Set<FlowTypes> getSupportedFlowTypes() {

            throw new IllegalStateException("Deliberately broken.");
        }

        @Override
        public FlowExecutorMetadata getExecutorMetadata() {

            throw new IllegalStateException("Deliberately broken.");
        }
    }

    /**
     * A connector compiled against a different framework version: the metadata hooks fail with a
     * linkage error rather than an exception.
     */
    private static class LinkageFaultyExecutor extends PlainExecutor {

        @Override
        public String getName() {

            return FAULTY_EXECUTOR;
        }

        @Override
        public Set<FlowTypes> getSupportedFlowTypes() {

            throw new NoClassDefFoundError("org/wso2/carbon/identity/flow/execution/engine/Missing");
        }

        @Override
        public FlowExecutorMetadata getExecutorMetadata() {

            throw new NoClassDefFoundError("org/wso2/carbon/identity/flow/execution/engine/Missing");
        }
    }

    /**
     * Contributes no usable name, which the registry must reject outright.
     */
    private static class BlankNameExecutor extends PlainExecutor {

        @Override
        public String getName() {

            return "   ";
        }
    }

    private static class AuthExecutor extends AuthenticationExecutor {

        @Override
        public String getAMRValue() {

            return AUTH_EXECUTOR;
        }

        @Override
        public String getName() {

            return AUTH_EXECUTOR;
        }

        @Override
        public ExecutorResponse execute(FlowExecutionContext context) {

            return null;
        }

        @Override
        public List<String> getInitiationData() {

            return Collections.emptyList();
        }

        @Override
        public ExecutorResponse rollback(FlowExecutionContext context) {

            return null;
        }

        @Override
        public Set<FlowTypes> getSupportedFlowTypes() {

            return EnumSet.of(REGISTRATION);
        }
    }
}
