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

package org.wso2.carbon.identity.flow.mgt.dao;

import org.apache.commons.dbcp.BasicDataSource;
import org.mockito.MockedStatic;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.flow.mgt.TestHelperMethods;
import org.wso2.carbon.identity.flow.mgt.cache.FlowMgtCacheKey;
import org.wso2.carbon.identity.flow.mgt.cache.GraphConfigCache;
import org.wso2.carbon.identity.flow.mgt.model.ComponentDTO;
import org.wso2.carbon.identity.flow.mgt.model.DataDTO;
import org.wso2.carbon.identity.flow.mgt.model.ExecutorDTO;
import org.wso2.carbon.identity.flow.mgt.model.GraphConfig;
import org.wso2.carbon.identity.flow.mgt.model.NodeConfig;
import org.wso2.carbon.identity.flow.mgt.model.StepDTO;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.identity.flow.mgt.TestHelperMethods.closeH2Database;
import static org.wso2.carbon.identity.flow.mgt.TestHelperMethods.getFilePath;

/**
 * Unit tests for {@link CacheBackedFlowDAOImpl}.
 * These tests focus on the defensive copying of the cached {@link GraphConfig}, which prevents callers that mutate
 * the graph during flow execution from corrupting the shared cached instance.
 */
@WithCarbonHome
public class CacheBackedFlowDAOImplTest {

    private static final String FLOW_TYPE = "PASSWORD_RECOVERY";
    private static final int TENANT_ID = 2;
    private static final String NODE_ID = "node-1";
    private static final String STEP_ID = "step-1";
    private static final String ORIGINAL_NEXT_NODE_ID = "node-2";
    private static final String EXECUTOR_NAME = "PasswordOnboardExecutor";
    private static final String DB_NAME = "cache_backed_flow_dao_db";
    private static final String DB_SCRIPT = "identity.sql";

    private BasicDataSource dataSource;
    private MockedStatic<IdentityDatabaseUtil> identityDatabaseUtilMockedStatic;
    private GraphConfigCache graphConfigCache;

    @BeforeClass
    public void setUp() throws Exception {

        // The DAO falls back to the database whenever the cached graph cannot be used.
        dataSource = TestHelperMethods.initiateH2Database(getFilePath(DB_SCRIPT), DB_NAME);
        identityDatabaseUtilMockedStatic = mockStatic(IdentityDatabaseUtil.class);
        identityDatabaseUtilMockedStatic.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSource);
    }

    @AfterClass
    public void tearDown() throws Exception {

        if (identityDatabaseUtilMockedStatic != null) {
            identityDatabaseUtilMockedStatic.close();
        }
        closeH2Database(dataSource);
    }

    @Test
    public void testGetGraphConfigReturnsDeepCopyOnCacheHit() throws Exception {

        GraphConfig cachedGraphConfig = buildGraphConfig();
        try (MockedStatic<GraphConfigCache> ignored = mockGraphConfigCache(cachedGraphConfig)) {
            GraphConfig returnedGraphConfig = CacheBackedFlowDAOImpl.getInstance()
                    .getGraphConfig(FLOW_TYPE, TENANT_ID);

            assertNotNull(returnedGraphConfig);
            // The returned graph must not be the cached instance, at any level of the object graph.
            assertNotSame(returnedGraphConfig, cachedGraphConfig);
            assertNotSame(returnedGraphConfig.getNodeConfigs(), cachedGraphConfig.getNodeConfigs());
            assertNotSame(returnedGraphConfig.getNodeConfigs().get(NODE_ID),
                    cachedGraphConfig.getNodeConfigs().get(NODE_ID));
            assertNotSame(returnedGraphConfig.getNodeConfigs().get(NODE_ID).getExecutorConfig(),
                    cachedGraphConfig.getNodeConfigs().get(NODE_ID).getExecutorConfig());
            assertNotSame(returnedGraphConfig.getNodePageMappings().get(NODE_ID),
                    cachedGraphConfig.getNodePageMappings().get(NODE_ID));

            // The copy must still carry the same content.
            assertEquals(returnedGraphConfig.getId(), cachedGraphConfig.getId());
            assertEquals(returnedGraphConfig.getFirstNodeId(), cachedGraphConfig.getFirstNodeId());
            NodeConfig copiedNode = returnedGraphConfig.getNodeConfigs().get(NODE_ID);
            assertEquals(copiedNode.getId(), NODE_ID);
            assertEquals(copiedNode.getNextNodeId(), ORIGINAL_NEXT_NODE_ID);
            assertTrue(copiedNode.isFirstNode());
            assertEquals(copiedNode.getExecutorConfig().getName(), EXECUTOR_NAME);
            assertEquals(returnedGraphConfig.getNodePageMappings().get(NODE_ID).getId(), STEP_ID);
        }
    }

    @Test
    public void testMutatingReturnedGraphConfigDoesNotAffectCache() throws Exception {

        GraphConfig cachedGraphConfig = buildGraphConfig();
        try (MockedStatic<GraphConfigCache> ignored = mockGraphConfigCache(cachedGraphConfig)) {
            GraphConfig returnedGraphConfig = CacheBackedFlowDAOImpl.getInstance()
                    .getGraphConfig(FLOW_TYPE, TENANT_ID);

            // Simulate a flow execution mutating the graph it received.
            returnedGraphConfig.setFirstNodeId("mutated-first-node");
            returnedGraphConfig.getNodeConfigs().get(NODE_ID).setNextNodeId("mutated-next-node");
            returnedGraphConfig.getNodeConfigs().get(NODE_ID).setPreviousNodeId("mutated-previous-node");
            returnedGraphConfig.addNodeConfig(new NodeConfig.Builder().id("injected-node").build());
            returnedGraphConfig.getNodePageMappings().remove(NODE_ID);

            // The cached instance must be untouched.
            assertEquals(cachedGraphConfig.getFirstNodeId(), NODE_ID);
            assertEquals(cachedGraphConfig.getNodeConfigs().get(NODE_ID).getNextNodeId(), ORIGINAL_NEXT_NODE_ID);
            assertNull(cachedGraphConfig.getNodeConfigs().get(NODE_ID).getPreviousNodeId());
            assertFalse(cachedGraphConfig.getNodeConfigs().containsKey("injected-node"));
            assertEquals(cachedGraphConfig.getNodeConfigs().size(), 1);
            assertTrue(cachedGraphConfig.getNodePageMappings().containsKey(NODE_ID));
        }
    }

    @Test
    public void testSuccessiveCacheHitsReturnIndependentGraphConfigs() throws Exception {

        GraphConfig cachedGraphConfig = buildGraphConfig();
        try (MockedStatic<GraphConfigCache> ignored = mockGraphConfigCache(cachedGraphConfig)) {
            CacheBackedFlowDAOImpl dao = CacheBackedFlowDAOImpl.getInstance();
            GraphConfig firstGraphConfig = dao.getGraphConfig(FLOW_TYPE, TENANT_ID);
            GraphConfig secondGraphConfig = dao.getGraphConfig(FLOW_TYPE, TENANT_ID);

            assertNotSame(firstGraphConfig, secondGraphConfig);
            assertNotSame(firstGraphConfig.getNodeConfigs().get(NODE_ID),
                    secondGraphConfig.getNodeConfigs().get(NODE_ID));

            // A mutation applied by one caller must not leak into a concurrent caller's graph.
            firstGraphConfig.getNodeConfigs().get(NODE_ID).setNextNodeId("mutated-next-node");
            assertEquals(secondGraphConfig.getNodeConfigs().get(NODE_ID).getNextNodeId(), ORIGINAL_NEXT_NODE_ID);
        }
    }

    @Test
    public void testGetGraphConfigFallsBackToDAOWhenCachedGraphIsNotCopyable() throws Exception {

        GraphConfig cachedGraphConfig = buildGraphConfig();
        // ComponentDTO configs are loosely typed, so a non-serializable value can end up in the graph.
        ComponentDTO component = new ComponentDTO.Builder().id("component-1").type("BUTTON").build();
        component.getConfigs().put("nonSerializable", new Object());
        DataDTO dataDTO = new DataDTO();
        dataDTO.addComponent(component);
        cachedGraphConfig.getNodePageMappings().get(NODE_ID).setData(dataDTO);

        try (MockedStatic<GraphConfigCache> ignored = mockGraphConfigCache(cachedGraphConfig)) {
            GraphConfig returnedGraphConfig = CacheBackedFlowDAOImpl.getInstance()
                    .getGraphConfig(FLOW_TYPE, TENANT_ID);

            // The unusable entry must be evicted and the graph re-read from the database instead of failing.
            verify(graphConfigCache).clearCacheEntry(any(FlowMgtCacheKey.class), eq(TENANT_ID));
            verify(graphConfigCache).addToCache(any(FlowMgtCacheKey.class), any(GraphConfig.class), eq(TENANT_ID));
            assertNotNull(returnedGraphConfig);
            assertFalse(returnedGraphConfig.getNodeConfigs().containsKey(NODE_ID));
        }
    }

    @Test
    public void testCopyOfReturnsNullForNullGraphConfig() throws Exception {

        Method copyOf = CacheBackedFlowDAOImpl.class.getDeclaredMethod("copyOf", GraphConfig.class);
        copyOf.setAccessible(true);
        try {
            assertNull(copyOf.invoke(CacheBackedFlowDAOImpl.getInstance(), (GraphConfig) null));
        } catch (InvocationTargetException e) {
            throw new AssertionError("copyOf should tolerate a null graph config.", e.getCause());
        }
    }

    /**
     * Mock {@link GraphConfigCache} so that the cache lookup returns the given graph config, isolating the test from
     * the underlying cache implementation and the database.
     *
     * @param cachedGraphConfig Graph config to be returned from the cache.
     * @return The static mock, to be closed by the caller.
     */
    private MockedStatic<GraphConfigCache> mockGraphConfigCache(GraphConfig cachedGraphConfig) {

        graphConfigCache = mock(GraphConfigCache.class);
        when(graphConfigCache.getValueFromCache(any(FlowMgtCacheKey.class), anyInt())).thenReturn(cachedGraphConfig);
        MockedStatic<GraphConfigCache> graphConfigCacheStatic = mockStatic(GraphConfigCache.class);
        graphConfigCacheStatic.when(GraphConfigCache::getInstance).thenReturn(graphConfigCache);
        return graphConfigCacheStatic;
    }

    private GraphConfig buildGraphConfig() {

        Map<String, String> executorMetadata = new HashMap<>();
        executorMetadata.put("key", "value");
        ExecutorDTO executorDTO = new ExecutorDTO(EXECUTOR_NAME);
        executorDTO.setMetadata(executorMetadata);

        NodeConfig nodeConfig = new NodeConfig.Builder()
                .id(NODE_ID)
                .type("EXECUTION")
                .isFirstNode(true)
                .nextNodeId(ORIGINAL_NEXT_NODE_ID)
                .executorConfig(executorDTO)
                .build();

        StepDTO stepDTO = new StepDTO();
        stepDTO.setId(STEP_ID);
        stepDTO.setType("VIEW");

        GraphConfig graphConfig = new GraphConfig();
        graphConfig.setId("flow-1");
        graphConfig.setFirstNodeId(NODE_ID);
        graphConfig.addNodeConfig(nodeConfig);
        graphConfig.addNodePageMapping(NODE_ID, stepDTO);
        return graphConfig;
    }
}
