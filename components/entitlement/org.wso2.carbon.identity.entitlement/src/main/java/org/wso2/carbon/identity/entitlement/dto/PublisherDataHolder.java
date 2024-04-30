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

package org.wso2.carbon.identity.entitlement.dto;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.registry.core.Resource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 *
 */
public class PublisherDataHolder {

    public static final String MODULE_NAME = "EntitlementModuleName";
    private static Log log = LogFactory.getLog(PublisherDataHolder.class);
    private String moduleName;
    private PublisherPropertyDTO[] propertyDTOs = new PublisherPropertyDTO[0];

    public PublisherDataHolder() {
    }

    public PublisherDataHolder(String moduleName) {
        this.moduleName = moduleName;
    }

    public PublisherDataHolder(Resource resource, boolean returnSecrets) {
        List<PublisherPropertyDTO> propertyDTOs = new ArrayList<PublisherPropertyDTO>();
        if (resource != null && resource.getProperties() != null) {
            Properties properties = resource.getProperties();
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                PublisherPropertyDTO dto = new PublisherPropertyDTO();
                dto.setId((String) entry.getKey());
                Object value = entry.getValue();
                if (value instanceof ArrayList) {
                    List list = (ArrayList) entry.getValue();
                    if (list != null && list.size() > 0 && list.get(0) != null) {
                        dto.setValue((String) list.get(0));

                        if (list.size() > 1 && list.get(1) != null) {
                            dto.setDisplayName((String) list.get(1));
                        }
                        if (list.size() > 2 && list.get(2) != null) {
                            dto.setDisplayOrder(Integer.parseInt((String) list.get(2)));
                        }
                        if (list.size() > 3 && list.get(3) != null) {
                            dto.setRequired(Boolean.parseBoolean((String) list.get(3)));
                        }
                        if (list.size() > 4 && list.get(4) != null) {
                            dto.setSecret(Boolean.parseBoolean((String) list.get(4)));
                        }

                        if (dto.isSecret()) {
                            if (returnSecrets) {
                                String password = dto.getValue();
                                try {
                                    password = new String(CryptoUtil.getDefaultCryptoUtil().
                                            base64DecodeAndDecrypt(dto.getValue()));
                                } catch (CryptoException e) {
                                    log.error(e);
                                    // ignore
                                }
                                dto.setValue(password);
                            }
                        }
                    }
                }
                if (MODULE_NAME.equals(dto.getId())) {
                    moduleName = dto.getValue();
                    continue;
                }

                propertyDTOs.add(dto);
            }
        }
        this.propertyDTOs = propertyDTOs.toArray(new PublisherPropertyDTO[propertyDTOs.size()]);
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public PublisherPropertyDTO[] getPropertyDTOs() {
        return Arrays.copyOf(propertyDTOs, propertyDTOs.length);
    }

    public void setPropertyDTOs(PublisherPropertyDTO[] propertyDTOs) {
        this.propertyDTOs = Arrays.copyOf(propertyDTOs, propertyDTOs.length);
    }


    public PublisherPropertyDTO getPropertyDTO(String id) {
        for (PublisherPropertyDTO dto : propertyDTOs) {
            if (dto.getId().equalsIgnoreCase(id)) {
                return dto;
            }
        }
        return null;
    }
}
