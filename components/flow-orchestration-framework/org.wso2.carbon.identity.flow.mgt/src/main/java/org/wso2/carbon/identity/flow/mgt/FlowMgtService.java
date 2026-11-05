/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.flow.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.annotation.bundle.Capability;
import org.wso2.carbon.identity.central.log.mgt.utils.LogConstants;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.LambdaExceptionUtils;
import org.wso2.carbon.identity.flow.mgt.Constants.OrganizationInputs;
import org.wso2.carbon.identity.flow.mgt.cache.FlowResolveCache;
import org.wso2.carbon.identity.flow.mgt.cache.FlowResolveCacheEntry;
import org.wso2.carbon.identity.flow.mgt.cache.FlowResolveCacheKey;
import org.wso2.carbon.identity.flow.mgt.dao.CacheBackedFlowDAOImpl;
import org.wso2.carbon.identity.flow.mgt.dao.FlowDAO;
import org.wso2.carbon.identity.flow.mgt.exception.FlowMgtFrameworkException;
import org.wso2.carbon.identity.flow.mgt.exception.FlowMgtServerException;
import org.wso2.carbon.identity.flow.mgt.internal.FlowMgtServiceDataHolder;
import org.wso2.carbon.identity.flow.mgt.model.ComponentDTO;
import org.wso2.carbon.identity.flow.mgt.model.ExecutorDTO;
import org.wso2.carbon.identity.flow.mgt.model.FlowConfigDTO;
import org.wso2.carbon.identity.flow.mgt.model.FlowDTO;
import org.wso2.carbon.identity.flow.mgt.model.FlowTypeVariablesDTO;
import org.wso2.carbon.identity.flow.mgt.model.FlowVariablesDTO;
import org.wso2.carbon.identity.flow.mgt.rule.FlowRuleValidator;
import org.wso2.carbon.identity.flow.mgt.model.GraphConfig;
import org.wso2.carbon.identity.flow.mgt.model.NodeConfig;
import org.wso2.carbon.identity.flow.mgt.model.StepDTO;
import org.wso2.carbon.identity.flow.mgt.utils.FlowMgtConfigUtils;
import org.wso2.carbon.identity.flow.mgt.utils.FlowMgtUtils;
import org.wso2.carbon.identity.flow.mgt.utils.FlowVariableUtils;
import org.wso2.carbon.identity.flow.mgt.utils.FlowVariableValidator;
import org.wso2.carbon.identity.flow.mgt.utils.GraphBuilder;
import org.wso2.carbon.identity.flow.mgt.utils.SystemDefaultFlowProvider;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.resource.hierarchy.traverse.service.OrgResourceResolverService;
import org.wso2.carbon.identity.organization.resource.hierarchy.traverse.service.exception.OrgResourceHierarchyTraverseException;
import org.wso2.carbon.identity.organization.resource.hierarchy.traverse.service.strategy.FirstFoundAggregationStrategy;
import org.wso2.carbon.utils.AuditLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils.triggerAuditLogEvent;
import static org.wso2.carbon.identity.flow.mgt.Constants.AuditLogConstants.EXECUTOR;
import static org.wso2.carbon.identity.flow.mgt.Constants.AuditLogConstants.EXECUTORS;
import static org.wso2.carbon.identity.flow.mgt.Constants.AuditLogConstants.FLOW_COMPLETION_CONFIGS;
import static org.wso2.carbon.identity.flow.mgt.Constants.AuditLogConstants.METADATA;
import static org.wso2.carbon.identity.flow.mgt.Constants.DEFAULT_FLOW_NAME;
import static org.wso2.carbon.identity.flow.mgt.Constants.FlowConfigConstants.IS_ENABLED;
import static org.wso2.carbon.identity.flow.mgt.utils.FlowMgtUtils.getInitiatorId;
import static org.wso2.carbon.identity.flow.mgt.utils.FlowMgtUtils.handleServerException;

/**
 * This class is responsible for managing the flow.
 */
@Capability(
        namespace = "osgi.service",
        attribute = {
                "objectClass=org.wso2.carbon.identity.flow.mgt.FlowMgtService",
                "service.scope=singleton"
        }
)
public class FlowMgtService {

    private static final FlowMgtService INSTANCE = new FlowMgtService();
    private static final FlowDAO FLOW_DAO = CacheBackedFlowDAOImpl.getInstance();
    private static final Log LOG = LogFactory.getLog(FlowMgtService.class);

    private FlowMgtService() {

    }

    public static FlowMgtService getInstance() {

        return INSTANCE;
    }

    /**
     * Update a specific flow of the given tenant.
     *
     * @param flowDTO  The flow.
     * @param tenantID The tenant ID.
     */
    public void updateFlow(FlowDTO flowDTO, int tenantID)
            throws FlowMgtFrameworkException {

        clearFlowResolveCache(flowDTO.getFlowType(), tenantID);
        // Validated first, since the rules are checked against the names the flow defines.
        FlowVariableValidator.validate(flowDTO);
        FlowRuleValidator.validate(flowDTO, IdentityTenantUtil.getTenantDomain(tenantID));
        GraphConfig flowConfig = new GraphBuilder().withSteps(flowDTO.getSteps())
                .withFlowVariables(flowDTO.getFlowVariables()).build();
        FLOW_DAO.updateFlow(flowDTO.getFlowType(), flowConfig, tenantID, DEFAULT_FLOW_NAME);
        AuditLog.AuditLogBuilder auditLogBuilder =
                new AuditLog.AuditLogBuilder(getInitiatorId(), LoggerUtils.getInitiatorType(getInitiatorId()),
                        flowConfig.getId(),
                        LoggerUtils.Target.Flow.name(),
                        String.format("%s%s", LogConstants.FlowManagement.UPDATE_FLOW, flowDTO.getFlowType()))
                        .data(buildExecutorAuditData(flowConfig));
        triggerAuditLogEvent(auditLogBuilder, true);
    }

    /**
     * Get the specified flow of the given tenant.
     *
     * @param tenantID The tenant ID.
     * @return The flow.
     * @throws FlowMgtFrameworkException If an error occurs while retrieving the default flow.
     */
    public FlowDTO getFlow(String flowType, int tenantID) throws FlowMgtFrameworkException {

        Integer tenantIdWithResource = getFirstTenantWithFlow(flowType, tenantID);
        if (tenantIdWithResource == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No flow found for type: " + flowType + " in tenant hierarchy. Returning system default.");
            }
            return SystemDefaultFlowProvider.getInstance().getSystemDefaultFlow(flowType);
        }
        return FLOW_DAO.getFlow(flowType, tenantIdWithResource);
    }

    /**
     * Get the headers and runtime values the flow of each flow type defines for the given tenant, as the flow
     * extension metadata lists them. Each flow type is read from the first tenant in the organization hierarchy that
     * has the flow, and the variables of a resolved tenant are read with one query. The system default flows are not
     * looked up. Each flow type also has the system runtime values every flow defines.
     *
     * @param tenantID The tenant ID.
     * @return One entry per flow type, or an empty list when flow variables are disabled.
     * @throws FlowMgtFrameworkException If an error occurs while retrieving the variables.
     */
    public List<FlowTypeVariablesDTO> getFlowVariables(int tenantID) throws FlowMgtFrameworkException {

        if (!FlowMgtUtils.isFlowVariablesEnabled()) {
            return Collections.emptyList();
        }
        Map<Integer, Map<String, FlowVariablesDTO>> variablesByTenant = new HashMap<>();
        List<FlowTypeVariablesDTO> flowTypeVariables = new ArrayList<>();

        for (Constants.FlowTypes flowType : Constants.FlowTypes.values()) {
            Integer tenantIdWithResource = getFirstTenantWithFlow(flowType.getType(), tenantID);
            FlowVariablesDTO stored = null;
            if (tenantIdWithResource != null) {
                Map<String, FlowVariablesDTO> storedVariables = variablesByTenant.get(tenantIdWithResource);
                if (storedVariables == null) {
                    storedVariables = FLOW_DAO.getFlowVariables(tenantIdWithResource);
                    variablesByTenant.put(tenantIdWithResource, storedVariables);
                }
                stored = storedVariables.get(flowType.getType());
            } else if (LOG.isDebugEnabled()) {
                LOG.debug("No flow found for type: " + flowType.getType() + " in tenant hierarchy. Listing only the "
                        + "system runtime values for it.");
            }

            FlowTypeVariablesDTO variables = new FlowTypeVariablesDTO(flowType.getType());
            if (stored != null) {
                variables.setHeaders(stored.getHeaders());
                variables.setRuntimeValues(stored.getRuntimeValues());
            }
            FlowVariableUtils.withSystemRuntimeValues(variables);
            flowTypeVariables.add(variables);
        }
        return flowTypeVariables;
    }

    /**
     * Get the attributes the registration flow collects for the organization it onboards, as the flow extension
     * metadata lists them. The flow is read from the first tenant in the organization hierarchy that has it, and the
     * attributes are the identifiers of its inputs marked as organization inputs. The organization name and handle
     * are left out, as every onboarded organization has them.
     *
     * @param tenantID The tenant ID.
     * @return The attribute names, in the order the flow has them.
     * @throws FlowMgtFrameworkException If an error occurs while retrieving the registration flow.
     */
    public List<String> getOrganizationAttributes(int tenantID) throws FlowMgtFrameworkException {

        FlowDTO flow = getFlow(Constants.FlowTypes.REGISTRATION.getType(), tenantID);
        Set<String> attributes = new LinkedHashSet<>();
        if (flow != null && flow.getSteps() != null) {
            for (StepDTO step : flow.getSteps()) {
                if (step != null && step.getData() != null) {
                    collectOrganizationAttributes(step.getData().getComponents(), attributes);
                }
            }
        }
        attributes.remove(OrganizationInputs.ORGANIZATION_NAME);
        attributes.remove(OrganizationInputs.ORGANIZATION_HANDLE);
        return new ArrayList<>(attributes);
    }

    /**
     * Delete the specific flow of the given non-root organization.
     *
     * @param flowType The flow type.
     * @param tenantID The tenant ID.
     * @throws FlowMgtFrameworkException If an error occurs while deleting the flow.
     */
    public void deleteFlow(String flowType, int tenantID) throws FlowMgtFrameworkException {

        try {
            // Only allow deleting flows in the non-root organization.
            String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantID);
            String orgId = FlowMgtServiceDataHolder.getInstance().getOrganizationManager()
                    .resolveOrganizationId(tenantDomain);
            if (FlowMgtServiceDataHolder.getInstance().getOrganizationManager().isPrimaryOrganization(orgId)) {
                return;
            }
        } catch (OrganizationManagementException e) {
            throw handleServerException(Constants.ErrorMessages.ERROR_CODE_DELETE_FLOW, e,
                    IdentityTenantUtil.getTenantDomain(tenantID));
        }
        clearFlowResolveCache(flowType, tenantID);
        FLOW_DAO.deleteFlow(flowType, tenantID);
        AuditLog.AuditLogBuilder auditLogBuilder =
                new AuditLog.AuditLogBuilder(getInitiatorId(), LoggerUtils.getInitiatorType(getInitiatorId()),
                        flowType,
                        LoggerUtils.Target.Flow.name(),
                        String.format("%s%s", LogConstants.FlowManagement.DELETE_FLOW, flowType));
        triggerAuditLogEvent(auditLogBuilder, true);
    }

    /**
     * Get the graph config by tenant ID.
     *
     * @param tenantID The tenant ID.
     */
    public GraphConfig getGraphConfig(String flowType, int tenantID) throws FlowMgtFrameworkException {

        // Since graph config is built from the flow, we can reuse the logic to get the first tenant with the flow.
        Integer tenantIdWithResource = getFirstTenantWithFlow(flowType, tenantID);
        if (tenantIdWithResource == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No graph config found for flow type: " + flowType + " in tenant hierarchy. Returning " +
                        "system default.");
            }
            return SystemDefaultFlowProvider.getInstance().getSystemDefaultGraphConfig(flowType);
        }
        return FLOW_DAO.getGraphConfig(flowType, tenantIdWithResource);
    }

    /**
     * Get the flow management configuration for the given tenant.
     *
     * @param tenantID The tenant ID.
     * @return The list of flow configurations.
     * @throws FlowMgtFrameworkException If an error occurs while retrieving the flow management configuration.
     */
    public List<FlowConfigDTO> getFlowConfigs(int tenantID) throws FlowMgtFrameworkException {

        String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantID);
        return FlowMgtConfigUtils.getFlowConfigs(tenantDomain);
    }

    /**
     * Get a flow configuration for the given tenant.
     *
     * @param flowType The flow type.
     * @param tenantID The tenant ID.
     * @return The flow configuration.
     * @throws FlowMgtFrameworkException If an error occurs while retrieving the flow configuration.
     */
    public FlowConfigDTO getFlowConfig(String flowType, int tenantID) throws FlowMgtFrameworkException {

        String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantID);
        return FlowMgtConfigUtils.getFlowConfig(flowType, tenantDomain);
    }

    /**
     * Update a flow configuration for the given tenant.
     *
     * @param flowConfigDTO The flow configuration.
     * @param tenantID      The tenant ID.
     * @throws FlowMgtFrameworkException If an error occurs while updating the flow configuration.
     */
    public FlowConfigDTO updateFlowConfig(FlowConfigDTO flowConfigDTO, int tenantID) throws FlowMgtFrameworkException {

        String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantID);
        FlowConfigDTO updatedFlowConfigDTO = FlowMgtConfigUtils.addFlowConfig(flowConfigDTO, tenantDomain);
        AuditLog.AuditLogBuilder auditLogBuilder =
                new AuditLog.AuditLogBuilder(getInitiatorId(), LoggerUtils.getInitiatorType(getInitiatorId()),
                        flowConfigDTO.getFlowType(),
                        LoggerUtils.Target.Flow.name(),
                        String.format("%s%s", LogConstants.FlowManagement.UPDATE_FLOW_CONFIG,
                                flowConfigDTO.getFlowType()))
                        .data(buildFlowConfigAuditData(updatedFlowConfigDTO));
        triggerAuditLogEvent(auditLogBuilder, true);
        return updatedFlowConfigDTO;
    }

    private Integer getFirstTenantWithFlow(String flowType, int tenantID) throws FlowMgtServerException {

        String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantID);
        try {
            OrganizationManager orgManager = FlowMgtServiceDataHolder.getInstance().getOrganizationManager();
            String currentOrgId = orgManager.resolveOrganizationId(tenantDomain);

            FlowResolveCacheKey cacheKey = new FlowResolveCacheKey(currentOrgId, flowType);
            FlowResolveCacheEntry cacheEntry = FlowResolveCache.getInstance().getValueFromCache(cacheKey, tenantID);
            if (cacheEntry != null) {
                return cacheEntry.getResolvedTenantId();
            }

            OrgResourceResolverService resolverService = FlowMgtServiceDataHolder.getInstance()
                    .getOrgResourceResolverService();
            Integer resolvedTenantId = resolverService.getResourcesFromOrgHierarchy(
                    currentOrgId,
                    LambdaExceptionUtils.rethrowFunction(orgId -> getTenantIdIfFlowExists(flowType, orgId)),
                    new FirstFoundAggregationStrategy<>());

            if (resolvedTenantId != null) {
                FlowResolveCache.getInstance().addToCache(cacheKey, new FlowResolveCacheEntry(resolvedTenantId), tenantID);
            }
            return resolvedTenantId;
        } catch (OrganizationManagementException | OrgResourceHierarchyTraverseException e) {
            throw handleServerException(Constants.ErrorMessages.ERROR_CODE_GET_DEFAULT_FLOW, e, tenantDomain);
        }
    }

    private Optional<Integer> getTenantIdIfFlowExists(String flowType, String orgId)
            throws OrganizationManagementException, FlowMgtServerException {

        OrganizationManager organizationManager = FlowMgtServiceDataHolder.getInstance().getOrganizationManager();
        String tenantDomain = organizationManager.resolveTenantDomain(orgId);
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        FlowDTO flowDTO = FLOW_DAO.getFlow(flowType, tenantId);
        return (flowDTO == null || flowDTO.getSteps().isEmpty()) ? Optional.empty() : Optional.of(tenantId);
    }

    private void clearFlowResolveCache(String flowType, int tenantId) throws FlowMgtServerException {

        String currentTenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
        try {
            OrganizationManager organizationManager = FlowMgtServiceDataHolder.getInstance().getOrganizationManager();
            String orgId = organizationManager.resolveOrganizationId(currentTenantDomain);
            FlowResolveCacheKey cacheKey = new FlowResolveCacheKey(orgId, flowType);
            FlowMgtUtils.clearCache(cacheKey, FlowResolveCache.getInstance(), tenantId);
        } catch (OrganizationManagementException e) {
            throw handleServerException(Constants.ErrorMessages.ERROR_CODE_CLEAR_CACHE_FAILED, e, currentTenantDomain);
        }
    }

    private void collectOrganizationAttributes(List<ComponentDTO> components, Set<String> attributes) {

        if (components == null) {
            return;
        }
        for (ComponentDTO component : components) {
            if (component == null) {
                continue;
            }
            Map<String, Object> configs = component.getConfigs();
            if (configs != null && OrganizationInputs.ORGANIZATION_IDENTIFIER_TYPE.equals(
                    configs.get(OrganizationInputs.IDENTIFIER_TYPE))) {
                Object identifier = configs.get(OrganizationInputs.IDENTIFIER);
                if (identifier != null && !String.valueOf(identifier).trim().isEmpty()) {
                    attributes.add(String.valueOf(identifier));
                }
            }
            collectOrganizationAttributes(component.getComponents(), attributes);
        }
    }

    private Map<String, Object> buildFlowConfigAuditData(FlowConfigDTO flowConfigDTO) {

        Map<String, Object> data = new LinkedHashMap<>();
        data.put(IS_ENABLED, flowConfigDTO.getIsEnabled());
        Map<String, String> completionConfigs = flowConfigDTO.getAllFlowCompletionConfigs();
        if (completionConfigs != null && !completionConfigs.isEmpty()) {
            data.put(FLOW_COMPLETION_CONFIGS, completionConfigs);
        }
        return data;
    }

    private Map<String, Object> buildExecutorAuditData(GraphConfig graphConfig) {

        List<Map<String, Object>> executors = new ArrayList<>();
        for (NodeConfig node : graphConfig.getNodeConfigs().values()) {
            ExecutorDTO executor = node.getExecutorConfig();
            if (executor == null) {
                continue;
            }
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put(EXECUTOR, executor.getName());
            if (executor.getMetadata() != null && !executor.getMetadata().isEmpty()) {
                entry.put(METADATA, executor.getMetadata());
            }
            executors.add(entry);
        }
        return Collections.singletonMap(EXECUTORS, executors);
    }
}
