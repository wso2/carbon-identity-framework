/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.com).
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.branding.preference.mgt;

import org.wso2.carbon.identity.branding.preference.mgt.exception.BrandingPreferenceMgtException;
import org.wso2.carbon.identity.branding.preference.mgt.model.BrandingPreference;

/**
 * Branding preference management service interface.
 */
public interface BrandingPreferenceManager {

    /**
     * This API is used to create a randing preference.
     *
     * @param brandingPreference Branding preference.
     * @return the created branding preference.
     * @throws BrandingPreferenceMgtException if any error occurred.
     */
    BrandingPreference addBrandingPreference(BrandingPreference brandingPreference)
            throws BrandingPreferenceMgtException;

    /**
     * This API is used to retrieve a branding preference.
     *
     * @param type         Type of the branding preference.
     * @param name         Name of the tenant/application.
     * @param locale       language preference of the branding.
     * @return The requested branding preference. If not exists return the default branding preference.
     * @throws BrandingPreferenceMgtException if any error occurred.
     */
    BrandingPreference getBrandingPreference(String type, String name, String locale)
            throws BrandingPreferenceMgtException;

    /**
     * This API is used to replace a given branding preference.
     *
     * @param brandingPreference Branding preference to be added.
     * @return Updated branding preference.
     * @throws BrandingPreferenceMgtException if any error occurred.
     */
    BrandingPreference replaceBrandingPreference(BrandingPreference brandingPreference)
            throws BrandingPreferenceMgtException;

    /**
     * This API is used to delete a branding preference.
     *
     * @param type         Type of the branding preference.
     * @param name         Name of the tenant/application.
     * @param locale       language preference of the branding.
     * @throws BrandingPreferenceMgtException if any error occurred.
     */
    void deleteBrandingPreference(String type, String name, String locale)
            throws BrandingPreferenceMgtException;
}
