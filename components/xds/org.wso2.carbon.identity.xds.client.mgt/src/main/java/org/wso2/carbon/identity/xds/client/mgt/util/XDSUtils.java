package org.wso2.carbon.identity.xds.client.mgt.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityUtil;

/**
 *
 */
public class XDSUtils {

    private static final Log LOG = LogFactory.getLog(XDSUtils.class);

    /**
     * Read listener property from identity.xml file.
     *
     * @param key Key of the property.
     * @return Property in String format.
     */
    public static String getConfig(String key) {

        String propertyValue = IdentityUtil.getProperty(key);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Retrieved listener property. Key: " + key + " value: " + propertyValue);
        }

        if (StringUtils.isBlank(propertyValue)) {
            LOG.warn("Value for listener property key: " + key + " is EMPTY.");
        }

        return propertyValue;
    }
}
