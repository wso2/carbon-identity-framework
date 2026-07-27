/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.webhook.metadata.internal.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.annotation.bundle.Capability;
import org.wso2.carbon.identity.webhook.metadata.api.exception.WebhookMetadataException;
import org.wso2.carbon.identity.webhook.metadata.api.exception.WebhookMetadataServerException;
import org.wso2.carbon.identity.webhook.metadata.api.model.Adapter;
import org.wso2.carbon.identity.webhook.metadata.api.service.EventAdapterMetadataService;
import org.wso2.carbon.identity.webhook.metadata.internal.dao.impl.FileBasedEventAdapterMetadataDAOImpl;
import org.wso2.carbon.identity.webhook.metadata.internal.util.WebhookMetadataExceptionHandler;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.wso2.carbon.identity.webhook.metadata.internal.constant.ErrorMessage.ERROR_CODE_ADAPTERS_RETRIEVE_ERROR;
import static org.wso2.carbon.identity.webhook.metadata.internal.constant.ErrorMessage.ERROR_CODE_ADAPTER_NOT_FOUND;
import static org.wso2.carbon.identity.webhook.metadata.internal.constant.ErrorMessage.ERROR_CODE_CONFIG_FILE_READ_ERROR;
import static org.wso2.carbon.identity.webhook.metadata.internal.constant.ErrorMessage.ERROR_CODE_ENABLED_ADAPTER_RETRIEVE_ERROR;
import static org.wso2.carbon.identity.webhook.metadata.internal.constant.ErrorMessage.ERROR_CODE_NO_ENABLED_ADAPTER;

/**
 * Implementation of the EventAdapterMetadataService that manages event adapters metadata.
 * This service provides methods to retrieve available adapters and the currently active adapter.
 */
@Capability(
        namespace = "osgi.service",
        attribute = {
                "objectClass=org.wso2.carbon.identity.webhook.metadata.api.service.EventAdapterMetadataService",
                "service.scope=singleton"
        }
)
public class EventAdapterMetadataServiceImpl implements EventAdapterMetadataService {

    private static final Log log = LogFactory.getLog(EventAdapterMetadataServiceImpl.class);
    private static final EventAdapterMetadataServiceImpl INSTANCE = new EventAdapterMetadataServiceImpl();

    private final FileBasedEventAdapterMetadataDAOImpl adapterMetadataDAO;
    private boolean initialized = false;
    private List<Adapter> adapters;

    private EventAdapterMetadataServiceImpl() {

        this.adapterMetadataDAO = FileBasedEventAdapterMetadataDAOImpl.getInstance();
    }

    public static EventAdapterMetadataServiceImpl getInstance() {

        return INSTANCE;
    }

    /**
     * Initialize the service.
     */
    public synchronized void init() throws WebhookMetadataServerException {

        if (initialized) {
            return;
        }
        try {
            adapterMetadataDAO.init();
            adapters = adapterMetadataDAO.getAdapters();
            initialized = true;
            List<Adapter> enabled = adapters.stream().filter(Adapter::isEnabled).collect(Collectors.toList());
            if (enabled.size() > 1) {
                log.warn("Multiple enabled adapters found: " +
                        enabled.stream().map(Adapter::getName).collect(Collectors.joining(", ")) +
                        ". Only the first will be used as active.");
            }
        } catch (WebhookMetadataException e) {
            initialized = false;
            throw WebhookMetadataExceptionHandler.handleServerException(
                    ERROR_CODE_CONFIG_FILE_READ_ERROR, e);
        }
    }

    @Override
    public List<Adapter> getAdapters() throws WebhookMetadataException {

        if (!initialized) {
            throw WebhookMetadataExceptionHandler.handleServerException(
                    ERROR_CODE_ADAPTERS_RETRIEVE_ERROR);
        }
        return Collections.unmodifiableList(adapters);
    }

    @Override
    public Adapter getCurrentActiveAdapter() throws WebhookMetadataException {

        if (!initialized) {
            throw WebhookMetadataExceptionHandler.handleServerException(
                    ERROR_CODE_ENABLED_ADAPTER_RETRIEVE_ERROR);
        }
        return adapters.stream()
                .filter(Adapter::isEnabled)
                .findFirst()
                .orElseThrow(() -> WebhookMetadataExceptionHandler.handleServerException(
                        ERROR_CODE_NO_ENABLED_ADAPTER));
    }

    @Override
    public Adapter getAdapterByName(String name) throws WebhookMetadataException {

        if (!initialized) {
            throw WebhookMetadataExceptionHandler.handleServerException(
                    ERROR_CODE_ADAPTERS_RETRIEVE_ERROR);
        }
        return adapters.stream()
                .filter(adapter -> adapter.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> WebhookMetadataExceptionHandler.handleServerException(
                        ERROR_CODE_ADAPTER_NOT_FOUND, name));
    }
}
