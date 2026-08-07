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
import org.wso2.carbon.identity.flow.execution.engine.Constants;
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
import static org.wso2.carbon.identity.flow.mgt.Constants.FlowTypes.INVITED_USER_REGISTRATION;
import static org.wso2.carbon.identity.flow.mgt.Constants.FlowTypes.PASSWORD_RECOVERY;
import static org.wso2.carbon.identity.flow.mgt.Constants.FlowTypes.REGISTRATION;

/**
 * Unit tests for {@link FlowExecutorMetadataService} and the metadata resolution behind it.
 */
public class FlowExecutorMetadataServiceTest {

    private static final String PLAIN_EXECUTOR = "PasswordProvisioningExecutor";
    private static final String DECLARING_EXECUTOR = "DaonExecutor";
    private static final String HIDDEN_EXECUTOR = "UserOnboardingExecutor";
    private static final String AUTH_EXECUTOR = "GoogleExecutor";
    private static final String FAULTY_EXECUTOR = "FaultyExecutor";
    private static final String LATE_EXECUTOR = "LateArrivingExecutor";

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

        assertTrue(metadataService.getComposerExecutors(REGISTRATION.name()).isEmpty(),
                "An executor that declares no flow type must not be offered as a composer step.");
        assertTrue(metadataService.getComposerExecutors(PASSWORD_RECOVERY.name()).isEmpty());
        assertTrue(metadataService.getComposerExecutors(INVITED_USER_REGISTRATION.name()).isEmpty());
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
        assertTrue(info.getTags().isEmpty());
        assertTrue(info.getSupportedFlowTypes().isEmpty());
        assertFalse(info.isMetadataDeclared());
        assertFalse(info.isIdpRequired());
        assertFalse(info.isConnectionRequired());
        assertTrue(info.isVisibleInComposer());
    }

    @Test
    public void testDeclaredMetadataIsSurfaced() {

        register(new DeclaringExecutor());

        FlowExecutorInfo info = metadataService.getExecutor(DECLARING_EXECUTOR).orElse(null);
        assertNotNull(info);
        assertEquals(info.getDisplayName(), "Daon TrustX Verification");
        assertEquals(info.getDescription(), "Verifies the user with Daon TrustX.");
        assertEquals(info.getIcon(), "assets/images/logos/daon.svg");
        assertEquals(info.getTags(),
                Collections.singletonList(Constants.ExecutorTags.RECOVERY_FACTOR));
        assertEquals(info.getAssociatedAuthenticator(), "DaonAuthenticator");
        assertTrue(info.isConnectionRequired());
        assertTrue(info.isMetadataDeclared());
        assertEquals(info.getSupportedFlowTypes(), EnumSet.of(REGISTRATION, PASSWORD_RECOVERY));
    }

    @Test
    public void testComposerExecutorsAreFilteredByDeclaredFlowType() {

        register(new DeclaringExecutor());

        assertEquals(names(metadataService.getComposerExecutors(REGISTRATION.name())),
                Collections.singletonList(DECLARING_EXECUTOR));
        assertEquals(names(metadataService.getComposerExecutors(PASSWORD_RECOVERY.name())),
                Collections.singletonList(DECLARING_EXECUTOR));
        assertTrue(metadataService.getComposerExecutors(INVITED_USER_REGISTRATION.name()).isEmpty(),
                "A flow type the executor did not declare must not include it.");
    }

    @Test
    public void testFlowTypeCanBeResolvedByTypeValueAndIsCaseInsensitive() {

        register(new DeclaringExecutor());

        assertEquals(names(metadataService.getComposerExecutors(REGISTRATION.getType())),
                Collections.singletonList(DECLARING_EXECUTOR));
        assertEquals(names(metadataService.getComposerExecutors("registration")),
                Collections.singletonList(DECLARING_EXECUTOR));
    }

    @Test
    public void testBlankOrUnknownFlowTypeYieldsNoComposerExecutors() {

        register(new DeclaringExecutor());

        assertTrue(metadataService.getComposerExecutors(null).isEmpty());
        assertTrue(metadataService.getComposerExecutors("").isEmpty());
        assertTrue(metadataService.getComposerExecutors("ASK_PASSWORD").isEmpty());
    }

    @Test
    public void testExecutorHiddenFromComposerIsStillRegistered() {

        register(new HiddenExecutor());

        assertTrue(metadataService.getComposerExecutors(REGISTRATION.name()).isEmpty(),
                "An executor marked as not composer visible must be excluded even when it declares a flow type.");
        assertEquals(names(metadataService.getExecutors()), Collections.singletonList(HIDDEN_EXECUTOR));
        assertTrue(metadataService.isExecutorRegistered(HIDDEN_EXECUTOR));
    }

    @Test
    public void testAuthenticationExecutorRequiresAnIdentityProvider() {

        register(new AuthExecutor());

        FlowExecutorInfo info = metadataService.getExecutor(AUTH_EXECUTOR).orElse(null);
        assertNotNull(info);
        assertTrue(info.isIdpRequired(),
                "An AuthenticationExecutor must be reported as needing an identity provider.");
        assertEquals(names(metadataService.getComposerExecutors(REGISTRATION.name())),
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

        assertEquals(names(metadataService.getComposerExecutors(REGISTRATION.name())),
                Collections.singletonList(DECLARING_EXECUTOR));
    }

    @Test
    public void testLateRegisteredExecutorIsVisibleWithoutRestart() {

        register(new DeclaringExecutor());
        assertEquals(names(metadataService.getComposerExecutors(REGISTRATION.name())),
                Collections.singletonList(DECLARING_EXECUTOR));

        // Simulates a connector bundle in repository/components/dropins activating after the first
        // metadata read; nothing may be cached from that earlier call.
        register(new LateExecutor());

        assertEquals(names(metadataService.getComposerExecutors(REGISTRATION.name())),
                Arrays.asList(DECLARING_EXECUTOR, LATE_EXECUTOR),
                "A late registered executor must appear without a restart.");
    }

    @Test
    public void testUnregisteringAnExecutorRemovesItFromMetadata() {

        LateExecutor executor = new LateExecutor();
        register(executor);
        assertTrue(metadataService.isExecutorRegistered(LATE_EXECUTOR));

        FlowExecutionEngineDataHolder.getInstance().removeExecutor(executor);

        assertFalse(metadataService.isExecutorRegistered(LATE_EXECUTOR));
        assertEquals(metadataService.getExecutor(LATE_EXECUTOR), Optional.empty());
        assertTrue(metadataService.getComposerExecutors(REGISTRATION.name()).isEmpty());
    }

    @Test
    public void testUnregisteringAShadowedExecutorLeavesTheRegisteredOneInPlace() {

        // Two bundles contributing the same executor name: the second wins the map entry.
        LateExecutor shadowed = new LateExecutor();
        LateExecutor winner = new LateExecutor();
        register(shadowed);
        register(winner);

        // The loser's bundle stops. Removal is identity based, so the winner must survive it.
        assertFalse(FlowExecutionEngineDataHolder.getInstance().removeExecutor(shadowed),
                "Removing a shadowed executor must report that it was not the registered holder.");
        assertTrue(metadataService.isExecutorRegistered(LATE_EXECUTOR),
                "Unbinding the shadowed executor must not delete the still active registration.");

        assertTrue(FlowExecutionEngineDataHolder.getInstance().removeExecutor(winner));
        assertFalse(metadataService.isExecutorRegistered(LATE_EXECUTOR));
    }

    @Test
    public void testExecutorWithABlankNameIsNotRegistered() {

        // The name is the only handle a flow step has, so a blank one is unusable rather than merely odd.
        assertNull(FlowExecutionEngineDataHolder.getInstance().addExecutor(new BlankNameExecutor()));
        assertFalse(FlowExecutionEngineDataHolder.getInstance().removeExecutor(new BlankNameExecutor()));
        assertTrue(metadataService.getExecutors().isEmpty());
    }

    @Test
    public void testExecutorFailingWithALinkageErrorDoesNotBreakResolution() {

        // The characteristic failure of a dropin built against another framework version is an Error,
        // not an exception, and it must be contained the same way.
        register(new LinkageFaultyExecutor());
        register(new DeclaringExecutor());

        FlowExecutorInfo faulty = metadataService.getExecutor(FAULTY_EXECUTOR).orElse(null);
        assertNotNull(faulty);
        assertEquals(faulty.getDisplayName(), FAULTY_EXECUTOR);
        assertTrue(faulty.getSupportedFlowTypes().isEmpty());

        assertEquals(names(metadataService.getComposerExecutors(REGISTRATION.name())),
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
     * Stands in for a connector that opts in fully, as the Daon connector does.
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
                    .displayName("Daon TrustX Verification")
                    .description("Verifies the user with Daon TrustX.")
                    .icon("assets/images/logos/daon.svg")
                    .tags(Collections.singletonList(Constants.ExecutorTags.RECOVERY_FACTOR))
                    .associatedAuthenticator("DaonAuthenticator")
                    .connectionRequired(true)
                    .build();
        }
    }

    /**
     * Declares a flow type but opts out of the composer, as engine plumbing executors should.
     */
    private static class HiddenExecutor extends PlainExecutor {

        @Override
        public String getName() {

            return HIDDEN_EXECUTOR;
        }

        @Override
        public Set<FlowTypes> getSupportedFlowTypes() {

            return EnumSet.allOf(FlowTypes.class);
        }

        @Override
        public FlowExecutorMetadata getExecutorMetadata() {

            return FlowExecutorMetadata.builder()
                    .displayName("User Onboarding")
                    .visibleInComposer(false)
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
     * A dropin compiled against a different framework version: the metadata hooks fail with a
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
