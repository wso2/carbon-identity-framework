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
import org.wso2.carbon.identity.webhook.metadata.api.exception.WebhookMetadataException;
import org.wso2.carbon.identity.webhook.metadata.api.exception.WebhookMetadataServerException;
import org.wso2.carbon.identity.webhook.metadata.api.model.Adaptor;
import org.wso2.carbon.identity.webhook.metadata.api.service.EventAdaptorMetadataService;
import org.wso2.carbon.identity.webhook.metadata.internal.dao.EventAdopterMetadataDAO;
import org.wso2.carbon.identity.webhook.metadata.internal.dao.impl.FileBasedEventAdopterMetadataDAOImpl;
import org.wso2.carbon.identity.webhook.metadata.internal.util.WebhookMetadataExceptionHandler;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.wso2.carbon.identity.webhook.metadata.internal.constant.ErrorMessage.ERROR_CODE_ADAPTORS_RETRIEVE_ERROR;
import static org.wso2.carbon.identity.webhook.metadata.internal.constant.ErrorMessage.ERROR_CODE_CONFIG_FILE_READ_ERROR;
import static org.wso2.carbon.identity.webhook.metadata.internal.constant.ErrorMessage.ERROR_CODE_ENABLED_ADAPTOR_RETRIEVE_ERROR;
import static org.wso2.carbon.identity.webhook.metadata.internal.constant.ErrorMessage.ERROR_CODE_NO_ENABLED_ADAPTOR;

/**
 * Implementation of the EventAdaptorMetadataService that manages event adopters metadata.
 * This service provides methods to retrieve available adaptors and the currently active adaptor.
 */
public class EventAdopterMetadataServiceImpl implements EventAdaptorMetadataService {

    private static final Log log = LogFactory.getLog(EventAdopterMetadataServiceImpl.class);
    private static final EventAdopterMetadataServiceImpl INSTANCE = new EventAdopterMetadataServiceImpl();

    private final EventAdopterMetadataDAO adopterMetadataDAO;
    private boolean initialized = false;
    private List<Adaptor> adopters;

    private EventAdopterMetadataServiceImpl() {

        this.adopterMetadataDAO = FileBasedEventAdopterMetadataDAOImpl.getInstance();
    }

    public static EventAdopterMetadataServiceImpl getInstance() {

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
            adopters = adopterMetadataDAO.getAdopters();
            initialized = true;
            List<Adaptor> enabled = adopters.stream().filter(Adaptor::isEnabled).collect(Collectors.toList());
            if (enabled.size() > 1) {
                log.warn("Multiple enabled adopters found: " +
                        enabled.stream().map(Adaptor::getName).collect(Collectors.joining(", ")) +
                        ". Only the first will be used as active.");
            }
        } catch (WebhookMetadataException e) {
            initialized = false;
            throw WebhookMetadataExceptionHandler.handleServerException(
                    ERROR_CODE_CONFIG_FILE_READ_ERROR, e);
        }
    }

    @Override
    public List<Adaptor> getAdaptors() throws WebhookMetadataException {

        if (!initialized) {
            throw WebhookMetadataExceptionHandler.handleServerException(
                    ERROR_CODE_ADAPTORS_RETRIEVE_ERROR);
        }
        return Collections.unmodifiableList(adopters);
    }

    @Override
    public Adaptor getCurrentActiveAdaptor() throws WebhookMetadataException {

        if (!initialized) {
            throw WebhookMetadataExceptionHandler.handleServerException(
                    ERROR_CODE_ENABLED_ADAPTOR_RETRIEVE_ERROR);
        }
        return adopters.stream()
                .filter(Adaptor::isEnabled)
                .findFirst()
                .orElseThrow(() -> WebhookMetadataExceptionHandler.handleServerException(
                        ERROR_CODE_NO_ENABLED_ADAPTOR));
    }
}
