/*
*  Copyright (c)  WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.entitlement.policy.publisher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.common.EntitlementConstants;
import org.wso2.carbon.identity.entitlement.dto.PolicyDTO;
import org.wso2.carbon.identity.entitlement.dto.PublisherDataHolder;
import org.wso2.carbon.identity.entitlement.dto.PublisherPropertyDTO;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * This is  abstract implementation of PolicyPublisherModule. Here we have implemented the init()
 * method.
 * If you want to configure properties of a publisher module from management UI,
 * you want to write your publisher module by extending this abstract class
 * Then you can init() your module each time policy is published.
 */
public abstract class AbstractPolicyPublisherModule implements PolicyPublisherModule {

    protected static final String REQUIRED = "required";

    protected static final String DISPLAY_NAME = "displayName";

    protected static final String ORDER = "order";

    protected static final String SECRET = "password";

    private static Log log = LogFactory.getLog(AbstractPolicyPublisherModule.class);

    public void init(Properties properties) {

        List<PublisherPropertyDTO> propertyDTOs = new ArrayList<PublisherPropertyDTO>();

        if (properties == null || properties.size() == 0) {
            properties = loadProperties();
        }

        if (properties != null) {
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {

                Map attributeMap;

                Object value = entry.getValue();
                if (value instanceof Map) {
                    attributeMap = (Map) value;
                } else {
                    return;
                }

                PublisherPropertyDTO dto = new PublisherPropertyDTO();
                dto.setModule(getModuleName());
                dto.setId((String) entry.getKey());
                if (attributeMap.get(DISPLAY_NAME) != null) {
                    dto.setDisplayName((String) attributeMap.get(DISPLAY_NAME));
                } else {
                    log.error("Invalid policy publisher configuration : Display name can not be null");
                }
                if (attributeMap.get(ORDER) != null) {
                    dto.setDisplayOrder(Integer.parseInt((String) attributeMap.get(ORDER)));
                }
                if (attributeMap.get(REQUIRED) != null) {
                    dto.setRequired(Boolean.parseBoolean((String) attributeMap.get(REQUIRED)));
                }
                if (attributeMap.get(SECRET) != null) {
                    dto.setSecret(Boolean.parseBoolean((String) attributeMap.get(SECRET)));
                }
                propertyDTOs.add(dto);
            }
        }

        PublisherPropertyDTO preDefined1 = new PublisherPropertyDTO();
        preDefined1.setId(PolicyPublisher.SUBSCRIBER_ID);
        preDefined1.setModule(getModuleName());
        preDefined1.setDisplayName(PolicyPublisher.SUBSCRIBER_DISPLAY_NAME);
        preDefined1.setRequired(true);
        preDefined1.setDisplayOrder(0);
        propertyDTOs.add(preDefined1);

        PublisherDataHolder holder = new PublisherDataHolder(getModuleName());
        holder.setPropertyDTOs(propertyDTOs.toArray(new PublisherPropertyDTO[propertyDTOs.size()]));
        EntitlementServiceComponent.getEntitlementConfig().
                addModulePropertyHolder(PolicyPublisherModule.class.getName(), holder);

    }

    @Override
    public Properties loadProperties() {
        return null;
    }

    @Override
    public void publish(PolicyDTO policyDTO, String action, boolean enabled, int order) throws EntitlementException {

        if (EntitlementConstants.PolicyPublish.ACTION_CREATE.equals(action)) {
            policyDTO.setPolicyOrder(order);
            policyDTO.setActive(enabled);
            publishNew(policyDTO);
        } else if (EntitlementConstants.PolicyPublish.ACTION_DELETE.equals(action)) {
            delete(policyDTO);
        } else if (EntitlementConstants.PolicyPublish.ACTION_UPDATE.equals(action)) {
            update(policyDTO);
        } else if (EntitlementConstants.PolicyPublish.ACTION_ENABLE.equals(action)) {
            policyDTO.setActive(true);
            enable(policyDTO);
        } else if (EntitlementConstants.PolicyPublish.ACTION_DISABLE.equals(action)) {
            policyDTO.setActive(false);
            disable(policyDTO);
        } else if (EntitlementConstants.PolicyPublish.ACTION_ORDER.equals(action)) {
            policyDTO.setPolicyOrder(order);
            order(policyDTO);
        } else {
            throw new EntitlementException("Unsupported publishing action. Action is : " + action);
        }
    }

    /**
     * This would init module, each time policy is published
     *
     * @param propertyHolder publisher module data as PublisherDataHolder
     * @throws EntitlementException throws if init fails
     */
    public abstract void init(PublisherDataHolder propertyHolder) throws EntitlementException;

    /**
     * Publish a new policy
     *
     * @param policyDTO <code>PolicyDTO</code>
     * @throws EntitlementException throws, if fails
     */
    public abstract void publishNew(PolicyDTO policyDTO) throws EntitlementException;

    /**
     * Update a already published policy
     *
     * @param policyDTO <code>PolicyDTO</code>
     * @throws EntitlementException throws, if fails
     */
    public abstract void update(PolicyDTO policyDTO) throws EntitlementException;

    /**
     * Deletes a published policy
     *
     * @param policyDTO <code>PolicyDTO</code>
     * @throws EntitlementException throws, if fails
     */
    public abstract void delete(PolicyDTO policyDTO) throws EntitlementException;

    /**
     * Order policy
     *
     * @param policyDTO <code>PolicyDTO</code>
     * @throws EntitlementException if fails
     */
    public abstract void order(PolicyDTO policyDTO) throws EntitlementException;

    /**
     * Disables policy in PDP
     *
     * @param policyDTO <code>PolicyDTO</code>
     * @throws EntitlementException if fails
     */
    public abstract void disable(PolicyDTO policyDTO) throws EntitlementException;

    /**
     * Enables policy in PDP
     *
     * @param policyDTO <code>PolicyDTO</code>
     * @throws EntitlementException if fails
     */
    public abstract void enable(PolicyDTO policyDTO) throws EntitlementException;
}
