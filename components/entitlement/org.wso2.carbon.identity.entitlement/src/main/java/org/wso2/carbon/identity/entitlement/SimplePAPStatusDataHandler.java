/*
 *  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.entitlement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.entitlement.common.EntitlementConstants;
import org.wso2.carbon.identity.entitlement.dto.PublisherPropertyDTO;
import org.wso2.carbon.identity.entitlement.dto.StatusHolder;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TODO
 */
public class SimplePAPStatusDataHandler implements PAPStatusDataHandler {

    private static final String ENTITLEMENT_POLICY_STATUS = "/repository/identity/entitlement/status/policy/";
    private static final String ENTITLEMENT_PUBLISHER_STATUS = "/repository/identity/entitlement/status/publisher/";
    private static final int SEARCH_BY_USER = 0;
    private static final int SEARCH_BY_POLICY = 1;
    private static Log log = LogFactory.getLog(SimplePAPStatusDataHandler.class);
    private int DEFAULT_MAX_RECODES = 50;
    private int maxRecodes;

    @Override
    public void init(Properties properties) {
        String maxRecodesString = (String) properties.get("maxRecodesToPersist");
        if (maxRecodesString != null) {
            try {
                maxRecodes = Integer.parseInt(maxRecodesString);
            } catch (Exception e) {
                //ignore
            }
        }
        if (maxRecodes == 0) {
            maxRecodes = DEFAULT_MAX_RECODES;
        }
    }

    @Override
    public void handle(String about, String key, List<StatusHolder> statusHolder)
            throws EntitlementException {

        if (EntitlementConstants.Status.ABOUT_POLICY.equals(about)) {
            String path = ENTITLEMENT_POLICY_STATUS + key;
            // policy would be deleted.
            for (StatusHolder holder : statusHolder) {
                if (EntitlementConstants.StatusTypes.DELETE_POLICY.equals(holder.getType())) {
                    deletedPersistedData(path);
                    return;
                }
            }
            persistStatus(path, statusHolder, false);
        } else {
            String path = ENTITLEMENT_PUBLISHER_STATUS + key;
            // subscriber would be deleted.
            for (StatusHolder holder : statusHolder) {
                if (EntitlementConstants.StatusTypes.DELETE_POLICY.equals(holder.getType())) {
                    deletedPersistedData(path);
                    return;
                }
            }
            persistStatus(path, statusHolder, false);
        }
    }


    @Override
    public void handle(String about, StatusHolder statusHolder) throws EntitlementException {
        List<StatusHolder> list = new ArrayList<StatusHolder>();
        list.add(statusHolder);
        handle(about, statusHolder.getKey(), list);
    }


    @Override
    public StatusHolder[] getStatusData(String about, String key, String type, String searchString)
            throws EntitlementException {

        if (EntitlementConstants.Status.ABOUT_POLICY.equals(about)) {
            String path = ENTITLEMENT_POLICY_STATUS + key;
            List<StatusHolder> holders = readStatus(path, EntitlementConstants.Status.ABOUT_POLICY);
            List<StatusHolder> filteredHolders = new ArrayList<StatusHolder>();
            if (holders != null) {
                searchString = searchString.replace("*", ".*");
                Pattern pattern = Pattern.compile(searchString, Pattern.CASE_INSENSITIVE);
                for (StatusHolder holder : holders) {
                    String id = holder.getUser();
                    Matcher matcher = pattern.matcher(id);
                    if (!matcher.matches()) {
                        continue;
                    }
                    if (type != null && type.equals(holder.getType())) {
                        filteredHolders.add(holder);
                    } else if (type == null) {
                        filteredHolders.add(holder);
                    }
                }
            }
            return filteredHolders.toArray(new StatusHolder[filteredHolders.size()]);
        } else {
            List<StatusHolder> filteredHolders = new ArrayList<StatusHolder>();
            String path = ENTITLEMENT_PUBLISHER_STATUS + key;
            List<StatusHolder> holders = readStatus(path, EntitlementConstants.Status.ABOUT_SUBSCRIBER);
            if (holders != null) {
                searchString = searchString.replace("*", ".*");
                Pattern pattern = Pattern.compile(searchString, Pattern.CASE_INSENSITIVE);
                for (StatusHolder holder : holders) {
                    String id = holder.getTarget();
                    Matcher matcher = pattern.matcher(id);
                    if (!matcher.matches()) {
                        continue;
                    }
                    filteredHolders.add(holder);
                }
            }
            return filteredHolders.toArray(new StatusHolder[filteredHolders.size()]);
        }
    }

    private synchronized void deletedPersistedData(String path) throws EntitlementException {

        Registry registry = null;
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            registry = EntitlementServiceComponent.getRegistryService().
                    getGovernanceSystemRegistry(tenantId);
            if (registry.resourceExists(path)) {
                registry.delete(path);
            }
        } catch (RegistryException e) {
            log.error(e);
            throw new EntitlementException("Error while persisting policy status", e);
        }
    }

    private synchronized void persistStatus(String path, List<StatusHolder> statusHolders, boolean isNew)
            throws EntitlementException {

        Resource resource = null;
        Registry registry = null;
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        try {
            registry = EntitlementServiceComponent.getRegistryService().
                    getGovernanceSystemRegistry(tenantId);
            boolean useLastStatusOnly = Boolean.parseBoolean(
                    IdentityUtil.getProperty(EntitlementConstants.PROP_USE_LAST_STATUS_ONLY));
            if (registry.resourceExists(path) && !isNew && !useLastStatusOnly) {
                resource = registry.get(path);
                String[] versions = registry.getVersions(path);
                // remove all versions.  As we have no way to disable versioning for specific resource
                if (versions != null) {
                    for (String version : versions) {
                        long versionInt = 0;
                        String[] versionStrings = version.split(RegistryConstants.VERSION_SEPARATOR);
                        if (versionStrings != null && versionStrings.length == 2) {
                            try {
                                versionInt = Long.parseLong(versionStrings[1]);
                            } catch (Exception e) {
                                // ignore
                            }
                        }
                        if (versionInt != 0) {
                            registry.removeVersionHistory(version, versionInt);
                        }
                    }
                }
            } else {
                resource = registry.newResource();
            }

            if (resource != null && statusHolders != null && statusHolders.size() > 0) {
                resource.setVersionableChange(false);
                populateStatusProperties(statusHolders.toArray(new StatusHolder[statusHolders.size()]), resource);
                registry.put(path, resource);
            }
        } catch (RegistryException e) {
            log.error(e);
            throw new EntitlementException("Error while persisting policy status", e);
        }

    }

    private synchronized List<StatusHolder> readStatus(String path, String about) throws EntitlementException {

        Resource resource = null;
        Registry registry = null;
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            registry = EntitlementServiceComponent.getRegistryService().
                    getGovernanceSystemRegistry(tenantId);
            if (registry.resourceExists(path)) {
                resource = registry.get(path);
            }
        } catch (RegistryException e) {
            log.error(e);
            throw new EntitlementException("Error while persisting policy status", e);
        }

        List<StatusHolder> statusHolders = new ArrayList<StatusHolder>();
        if (resource != null && resource.getProperties() != null) {
            Properties properties = resource.getProperties();
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                PublisherPropertyDTO dto = new PublisherPropertyDTO();
                dto.setId((String) entry.getKey());
                Object value = entry.getValue();
                if (value instanceof ArrayList) {
                    List list = (ArrayList) entry.getValue();
                    if (list != null && list.size() > 0 && list.get(0) != null) {
                        StatusHolder statusHolder = new StatusHolder(about);
                        if (list.size() > 0 && list.get(0) != null) {
                            statusHolder.setType((String) list.get(0));
                        }
                        if (list.size() > 1 && list.get(1) != null) {
                            statusHolder.setTimeInstance((String) list.get(1));
                        } else {
                            continue;
                        }
                        if (list.size() > 2 && list.get(2) != null) {
                            String user = (String) list.get(2);
                            statusHolder.setUser(user);
                        } else {
                            continue;
                        }
                        if (list.size() > 3 && list.get(3) != null) {
                            statusHolder.setKey((String) list.get(3));
                        }
                        if (list.size() > 4 && list.get(4) != null) {
                            statusHolder.setSuccess(Boolean.parseBoolean((String) list.get(4)));
                        }
                        if (list.size() > 5 && list.get(5) != null) {
                            statusHolder.setMessage((String) list.get(5));
                        }
                        if (list.size() > 6 && list.get(6) != null) {
                            statusHolder.setTarget((String) list.get(6));
                        }
                        if (list.size() > 7 && list.get(7) != null) {
                            statusHolder.setTargetAction((String) list.get(7));
                        }
                        if (list.size() > 8 && list.get(8) != null) {
                            statusHolder.setVersion((String) list.get(8));
                        }
                        statusHolders.add(statusHolder);
                    }
                }
            }
        }
        if (statusHolders.size() > 0) {
            StatusHolder[] array = statusHolders.toArray(new StatusHolder[statusHolders.size()]);
            java.util.Arrays.sort(array, new StatusHolderComparator());
            if (statusHolders.size() > maxRecodes) {
                statusHolders = new ArrayList<StatusHolder>();
                for (int i = 0; i < maxRecodes; i++) {
                    statusHolders.add(array[i]);
                }
                persistStatus(path, statusHolders, true);
            } else {
                statusHolders = new ArrayList<StatusHolder>(Arrays.asList(array));
            }
        }

        return statusHolders;
    }


    /**
     * @param statusHolders
     * @param resource
     */
    private void populateStatusProperties(StatusHolder[] statusHolders, Resource resource) {
        if (statusHolders != null) {
            for (StatusHolder statusHolder : statusHolders) {
                if (statusHolder != null) {
                    List<String> list = new ArrayList<String>();
                    list.add(statusHolder.getType());
                    list.add(statusHolder.getTimeInstance());
                    list.add(statusHolder.getUser());
                    list.add(statusHolder.getKey());
                    list.add(Boolean.toString(statusHolder.isSuccess()));
                    if (statusHolder.getMessage() != null) {
                        list.add(statusHolder.getMessage());
                    } else {
                        list.add("");
                    }
                    if (statusHolder.getTarget() != null) {
                        list.add(statusHolder.getTarget());
                    } else {
                        list.add("");
                    }
                    if (statusHolder.getTargetAction() != null) {
                        list.add(statusHolder.getTargetAction());
                    } else {
                        list.add("");
                    }
                    if (statusHolder.getVersion() != null) {
                        list.add(statusHolder.getVersion());
                    } else {
                        list.add("");
                    }
                    resource.setProperty(UUID.randomUUID().toString(), list);
                }
            }
        }
    }

}
