/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.provisioning;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.OutboundProvisioningConfig;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataHandler;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.identity.provisioning.IdentityProvisioningConstants.APPLICATION_BASED_OUTBOUND_PROVISIONING_ENABLED;
import static org.wso2.carbon.identity.provisioning.IdentityProvisioningConstants.USE_USER_TENANT_DOMAIN_FOR_OUTBOUND_PROVISIONING_IN_SAAS_APPS;

public class ProvisioningUtil {

    private static final Log log = LogFactory.getLog(ProvisioningUtil.class);

    private ProvisioningUtil() {
    }

    /**
     * @return
     */
    public static List<String> getClaimValues(Map<ClaimMapping, List<String>> attributeMap,
                                              String claimUri, String userStoreDomainName) {

        List<String> claimValues = new ArrayList<>();
        for (Map.Entry<ClaimMapping, List<String>> entry : attributeMap.entrySet()) {
            ClaimMapping mapping = entry.getKey();
            if (mapping.getLocalClaim() != null
                    && claimUri.equals(mapping.getLocalClaim().getClaimUri())) {
                claimValues = entry.getValue();
                break;
            }
        }

        if (userStoreDomainName != null) {

            List<String> modifiedClaimValues = new ArrayList<>();

            for (Iterator<String> iterator = claimValues.iterator(); iterator.hasNext(); ) {
                String claimValue = iterator.next();
                if (StringUtils.contains(claimValue, "/")) {
                    claimValue = claimValue.substring(claimValue.indexOf("/") + 1);
                }

                claimValue = userStoreDomainName + "/" + claimValue;
                modifiedClaimValues.add(claimValue);

            }

            claimValues = modifiedClaimValues;
        }

        return claimValues;
    }

    /**
     * @param claimUri
     * @param attributeList
     */
    public static void setClaimValue(String claimUri, Map<ClaimMapping, List<String>> attributeMap,
                                     List<String> attributeList) {

        ClaimMapping clmMapping = null;

        for (Map.Entry<ClaimMapping, List<String>> entry : attributeMap.entrySet()) {
            ClaimMapping mapping = entry.getKey();
            if (mapping.getLocalClaim() != null
                    && claimUri.equals(mapping.getLocalClaim().getClaimUri())) {
                clmMapping = mapping;
                break;
            }
        }

        if (clmMapping != null) {
            attributeMap.put(clmMapping, attributeList);
        }
    }

    /**
     * Required provisioning entity attribute value can be retrieved by passing attribute key, return null if value is
     * not found
     * @param provisioningEntity
     * @param claimURI
     * @return
     */
    public static String getAttributeValue(ProvisioningEntity provisioningEntity, String claimURI){
        Map<org.wso2.carbon.identity.application.common.model.ClaimMapping, List<String>> attributes =
                provisioningEntity.getAttributes();
        if (MapUtils.isNotEmpty(attributes)) {
            List<String> valueList = attributes.get(org.wso2.carbon.identity.application.common.model.ClaimMapping
                                                            .build(claimURI, null, null, false));
            if (valueList != null && !valueList.isEmpty()) {
                return valueList.get(0);
            }
        }
        return null;
    }

    public static Map<ClaimMapping, List<String>> getMappedClaims(String outboundClaimDialect,
                                                                  Map<String, String> inboundClaimValueMap, ClaimMapping[] inboundClaimMappings,
                                                                  Map<ClaimMapping, List<String>> outboundClaimValueMappings, String tenantDomain)
            throws IdentityApplicationManagementException {

        try {

            // we do have in-bound claim mapping - but no out-bound claim mapping - no out-bound
            // default values.since we do not know the out-bound claim mapping - whatever in the
            // in-bound claims will be mapped into the out-bound claim dialect.

            if (MapUtils.isEmpty(inboundClaimValueMap)) {
                // we do not have out-bound claim mapping - and a default values to worry about.
                // just return what we got.
                return outboundClaimValueMappings;
            }

            Map<String, String> claimMap = null;

            // out-bound is not in wso2 carbon dialect. we need to find how it maps to wso2
            // carbon dialect.
            Map<String, String> outBoundToCarbonClaimMapppings = null;

            // we only know the dialect - it is a standard claim dialect.
            // this returns back a map - having carbon claim dialect as the key.
            // null argument is passed - because we do not know the required attributes for
            // out-bound provisioning. This will find carbon claim mappings for the entire out-bound
            // claim dialect.
            outBoundToCarbonClaimMapppings = ClaimMetadataHandler.getInstance()
                    .getMappingsMapFromOtherDialectToCarbon(outboundClaimDialect, null,
                            tenantDomain, true);

            if (outBoundToCarbonClaimMapppings == null) {
                // we did not find any carbon claim mappings corresponding to the out-bound claim
                // dialect - we cannot map the in-bound claim dialect to out-bound claim dialect.
                // just return what we got.
                return outboundClaimValueMappings;
            }

            // {in-bound-claim-uri / out-bound-claim-uri
            claimMap = new HashMap<String, String>();

            for (ClaimMapping inboundClaimMapping : inboundClaimMappings) {
                // there can be a claim mapping without a mapped local claim.
                // if that is the case - we cannot map it to an out-bound claim.
                if (inboundClaimMapping.getLocalClaim() == null
                        || inboundClaimMapping.getLocalClaim().getClaimUri() == null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Inbound claim - local claim is null");
                    }
                    continue;
                }

                // get the out-bound claim corresponding to the carbon dialect - which is the key.
                String outboundClaim = outBoundToCarbonClaimMapppings.get(inboundClaimMapping
                        .getLocalClaim().getClaimUri());

                if (outboundClaim != null) {
                    // in-bound claim uri / out-bound claim uri.
                    if (inboundClaimMapping.getRemoteClaim() != null
                            && inboundClaimMapping.getRemoteClaim().getClaimUri() != null) {
                        claimMap.put(inboundClaimMapping.getRemoteClaim().getClaimUri(),
                                outboundClaim);
                    }
                }
            }

            if (claimMap.isEmpty()) {
                // we do not have a claim map.
                // return what we got.
                return outboundClaimValueMappings;
            }

            for (Iterator<Map.Entry<String, String>> iterator = claimMap.entrySet().iterator(); iterator
                    .hasNext(); ) {
                Map.Entry<String, String> entry = iterator.next();

                String inboundClaimUri = entry.getKey();
                String outboundClaimUri = entry.getValue();
                String claimValue = null;

                if (outboundClaimUri != null) {
                    claimValue = inboundClaimValueMap.get(inboundClaimUri);
                }
                // null value goes there because we do not have an out-bound claim mapping - and
                // also default values.
                if (claimValue != null) {
                    outboundClaimValueMappings.put(
                            ClaimMapping.build(inboundClaimUri, outboundClaimUri, null, false),
                            Arrays.asList(new String[]{claimValue}));
                }
            }

        } catch (Exception e) {
            throw new IdentityApplicationManagementException("Error while loading claim mappings.",
                    e);
        }

        return outboundClaimValueMappings;
    }


    public static Map<ClaimMapping, List<String>> getMappedClaims(String outboundClaimDialect,
                                                                  Map<String, String> inboundClaimValueMap, String inboundClaimMappingDialect,
                                                                  Map<ClaimMapping, List<String>> outboundClaimValueMappings, String tenantDomain)
            throws IdentityApplicationManagementException {

        // we have in-bound claim dialect and out-bound claim dialect. we do not have an in-bound
        // claim mapping or an out-bound claim mapping.

        try {

            if (MapUtils.isEmpty(inboundClaimValueMap)) {
                return outboundClaimValueMappings;
            }

            Map<String, String> claimMap = null;

            if (IdentityApplicationConstants.WSO2CARBON_CLAIM_DIALECT
                    .equals(inboundClaimMappingDialect)) {
                // in-bound dialect is in default carbon dialect.
                // otherDialectURI, carbonClaimURIs, tenantDomain, carbonDialectAsKey
                // this map will have out-bound dialect as the key.
                claimMap = ClaimMetadataHandler.getInstance()
                        .getMappingsMapFromOtherDialectToCarbon(outboundClaimDialect, null,
                                tenantDomain, true);
            } else {
                // out-bound is not in wso2 carbon dialect. we need to find how it maps to wso2
                // carbon dialect.
                Map<String, String> inboundToCarbonClaimMaping = null;
                Map<String, String> outBoundToCarbonClaimMappping = null;

                // this will return back the mapped carbon dialect for the in-bound claims in the
                // in-bound provisioning request.
                // the key of this map will be in in-bound claim dialect.
                inboundToCarbonClaimMaping = ClaimMetadataHandler.getInstance()
                        .getMappingsMapFromOtherDialectToCarbon(inboundClaimMappingDialect,
                                inboundClaimValueMap.keySet(), tenantDomain, false);

                // we only know the dialect - it is standard claim dialect.
                // this will return back all the wso2 carbon claims mapped to the out-bound dialect.
                // we send null here because we do not know the required claims for out-bound
                // provisioning.
                // the key of this map will be in carbon dialect.
                outBoundToCarbonClaimMappping = ClaimMetadataHandler.getInstance()
                        .getMappingsMapFromOtherDialectToCarbon(outboundClaimDialect, null,
                                tenantDomain, true);

                // in-bound dialect / out-bound dialect.
                claimMap = new HashMap<String, String>();

                for (Iterator<Map.Entry<String, String>> iterator = inboundToCarbonClaimMaping
                        .entrySet().iterator(); iterator.hasNext(); ) {
                    Map.Entry<String, String> entry = iterator.next();
                    String outboundClaim = outBoundToCarbonClaimMappping.get(entry.getValue());
                    if (outboundClaim != null) {
                        claimMap.put(entry.getKey(), outboundClaim);
                    }
                }
            }

            if (claimMap.isEmpty()) {
                return outboundClaimValueMappings;
            }

            // when we do not defined the claim mapping for out-bound provisioning we iterate
            // through the in-bound provisioning claim map.
            for (Iterator<Map.Entry<String, String>> iterator = claimMap.entrySet().iterator(); iterator
                    .hasNext(); ) {
                Map.Entry<String, String> entry = iterator.next();
                String outboundClaimUri = entry.getValue();
                String inboundClaimUri = entry.getKey();

                String claimValue = null;

                if (outboundClaimUri != null) {
                    claimValue = inboundClaimValueMap.get(inboundClaimUri);
                }

                if (claimValue != null) {
                    outboundClaimValueMappings.put(
                            ClaimMapping.build(inboundClaimUri, outboundClaimUri, null, false),
                            Arrays.asList(new String[]{claimValue}));
                }
            }

        } catch (Exception e) {
            throw new IdentityApplicationManagementException("Error while loading claim mappings.",
                    e);
        }

        return outboundClaimValueMappings;
    }

    public static Map<ClaimMapping, List<String>> getMappedClaims(
            ClaimMapping[] outboundClaimMappings, Map<String, String> inboundClaimValueMap,
            ClaimMapping[] inboundClaimMappings,
            Map<ClaimMapping, List<String>> outboundClaimValueMappings)
            throws IdentityApplicationManagementException {

        try {

            // we have in-bound claim mapping and out-bound claim mapping.

            if (outboundClaimValueMappings == null) {
                outboundClaimValueMappings = new HashMap<ClaimMapping, List<String>>();
            }

            if (MapUtils.isEmpty(inboundClaimValueMap)) {
                // we do not have any values in the incoming provisioning request.
                // we need to populate outboundClaimValueMappings map with the default values from
                // the out-bound claim mapping.
                if (outboundClaimMappings != null && outboundClaimMappings.length > 0) {
                    for (ClaimMapping mapping : outboundClaimMappings) {
                        if (mapping.getDefaultValue() != null) {
                            outboundClaimValueMappings.put(mapping,
                                    Arrays.asList(new String[]{mapping.getDefaultValue()}));
                        }
                    }
                }

                return outboundClaimValueMappings;
            }

            if (outboundClaimMappings == null || outboundClaimMappings.length == 0) {
                // we cannot find out-bound claim dialect - return what we have.
                return outboundClaimValueMappings;
            }

            Map<String, String> claimMap = null;

            // out-bound is not in wso2 carbon dialect. we need to find how it maps to wso2
            // carbon dialect.
            Map<String, String> inboundToCarbonClaimMaping = new HashMap<String, String>();
            Map<String, String> outBoundToCarbonClaimMappping = new HashMap<String, String>();

            Map<String, String> outboundClaimDefaultValues = new HashMap<String, String>();

            for (ClaimMapping inboundClaimMapping : inboundClaimMappings) {
                // populate map with in-bound claims.
                if (inboundClaimMapping.getLocalClaim() != null) {
                    inboundToCarbonClaimMaping.put(inboundClaimMapping.getLocalClaim()
                            .getClaimUri(), inboundClaimMapping.getRemoteClaim().getClaimUri());
                } else {
                    // ignore. if you do not have a local claim we cannot map it.
                }
            }

            for (ClaimMapping outboundClaimMapping : outboundClaimMappings) {
                // populate a map with the out-bound claims.
                // use remote claim uri as the key.
                if (outboundClaimMapping.getLocalClaim() != null) {
                    outBoundToCarbonClaimMappping.put(outboundClaimMapping.getRemoteClaim()
                            .getClaimUri(), outboundClaimMapping.getLocalClaim().getClaimUri());
                } else {
                    outBoundToCarbonClaimMappping.put(outboundClaimMapping.getRemoteClaim()
                            .getClaimUri(), null);
                }

                outboundClaimDefaultValues.put(outboundClaimMapping.getRemoteClaim().getClaimUri(),
                        outboundClaimMapping.getDefaultValue());
            }

            claimMap = new HashMap<String, String>();

            // we need to have everything in the out-bound claim dialect in the claimMap.
            for (Iterator<Map.Entry<String, String>> iterator = outBoundToCarbonClaimMappping
                    .entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry<String, String> entry = iterator.next();

                String localClaimUri = entry.getValue();
                String outboundClaimUri = entry.getKey();

                String inboundClaim = inboundToCarbonClaimMaping.get(localClaimUri);
                claimMap.put(outboundClaimUri, inboundClaim);
            }

            if (claimMap.isEmpty()) {
                return outboundClaimValueMappings;
            }

            for (Iterator<Map.Entry<String, String>> iterator = claimMap.entrySet().iterator(); iterator
                    .hasNext(); ) {

                Map.Entry<String, String> entry = iterator.next();
                String outboundClaimUri = entry.getKey();
                String inboundClaimUri = entry.getValue();

                if (inboundClaimUri != null && inboundClaimValueMap.get(inboundClaimUri) != null) {
                    outboundClaimValueMappings.put(ClaimMapping.build(inboundClaimUri,
                            outboundClaimUri, outboundClaimDefaultValues.get(outboundClaimUri),
                            false), Arrays.asList(new String[]{inboundClaimValueMap
                            .get(inboundClaimUri)}));
                } else {
                    outboundClaimValueMappings.put(ClaimMapping.build(inboundClaimUri,
                            outboundClaimUri, outboundClaimDefaultValues.get(outboundClaimUri),
                            false), Arrays.asList(new String[]{outboundClaimDefaultValues
                            .get(outboundClaimUri)}));
                }
            }

        } catch (Exception e) {
            throw new IdentityApplicationManagementException("Error while loading claim mappings.",
                    e);
        }

        return outboundClaimValueMappings;
    }

    public static Map<ClaimMapping, List<String>> getMappedClaims(
            ClaimMapping[] outboundClaimMappings, Map<String, String> inboundClaimValueMap,
            String inboundClaimMappingDialect,
            Map<ClaimMapping, List<String>> outboundClaimValueMappings, String tenantDomain)
            throws IdentityApplicationManagementException {

        // we know the out-bound claim mapping - and the in-bound claim dialect.

        try {

            if (MapUtils.isEmpty(inboundClaimValueMap)) {
                // we do not have any values in the incoming provisioning request.
                // we need to populate outboundClaimValueMappings map with the default values from
                // the out-bound claim mapping.
                if (outboundClaimMappings != null && outboundClaimMappings.length > 0) {
                    for (ClaimMapping mapping : outboundClaimMappings) {
                        if (mapping.getDefaultValue() != null) {
                            outboundClaimValueMappings.put(mapping,
                                    Arrays.asList(new String[]{mapping.getDefaultValue()}));
                        }
                    }
                }

                return outboundClaimValueMappings;
            }

            if (outboundClaimMappings == null || outboundClaimMappings.length == 0) {
                // we cannot find out-bound claim dialect - return what we have.
                return outboundClaimValueMappings;
            }

            Map<String, String> claimMap = null;

            // out-bound is not in wso2 carbon dialect. we need to find how it maps to wso2
            // carbon dialect.
            Map<String, String> carbonToInboundClaimMapping = null;

            // we only know the dialect - it is standard claim dialect.
            // returns the carbon claim mapping corresponding to claims in the the in-bound
            // provisioning request with carbon in-bound claim uris as the key.
            carbonToInboundClaimMapping = ClaimMetadataHandler.getInstance()
                    .getMappingsMapFromOtherDialectToCarbon(inboundClaimMappingDialect,
                            inboundClaimValueMap.keySet(), tenantDomain, true);

            claimMap = new HashMap<String, String>();

            Map<String, String> outboundClaimDefaultValues = new HashMap<String, String>();

            for (ClaimMapping outboundClaimMapping : outboundClaimMappings) {

                String inboundClaim = null;

                if (outboundClaimMapping.getLocalClaim() != null) {
                    inboundClaim = carbonToInboundClaimMapping.get(outboundClaimMapping
                            .getLocalClaim().getClaimUri());
                }

                claimMap.put(outboundClaimMapping.getRemoteClaim().getClaimUri(), inboundClaim);

                outboundClaimDefaultValues.put(outboundClaimMapping.getRemoteClaim().getClaimUri(),
                        outboundClaimMapping.getDefaultValue());

            }

            if (claimMap.isEmpty()) {
                return outboundClaimValueMappings;
            }

            for (Iterator<Map.Entry<String, String>> iterator = claimMap.entrySet().iterator(); iterator
                    .hasNext(); ) {
                Map.Entry<String, String> entry = iterator.next();
                String outboundClaimUri = entry.getKey();
                String inboundClaimUri = entry.getValue();

                if (inboundClaimUri != null && inboundClaimValueMap.get(inboundClaimUri) != null) {
                    outboundClaimValueMappings.put(ClaimMapping.build(inboundClaimUri,
                            outboundClaimUri, outboundClaimDefaultValues.get(outboundClaimUri),
                            false), Arrays.asList(new String[]{inboundClaimValueMap
                            .get(inboundClaimUri)}));
                } else {
                    outboundClaimValueMappings.put(ClaimMapping.build(inboundClaimUri,
                            outboundClaimUri, outboundClaimDefaultValues.get(outboundClaimUri),
                            false), Arrays.asList(new String[]{outboundClaimDefaultValues
                            .get(outboundClaimUri)}));
                }
            }

        } catch (Exception e) {
            throw new IdentityApplicationManagementException("Error while loading claim mappings.",
                    e);
        }

        return outboundClaimValueMappings;
    }

    /**
     * Is user tenant used for outbound provisioning thread if user provisioning is happens through a saas app.
     *
     * @return true if useUserTenantDomainInSaasApps config is enabled.
     */
    public static boolean isUserTenantBasedOutboundProvisioningEnabled() {

        boolean userTenantBasedProvisioningThreadEnabled = false;

        if (StringUtils.isNotEmpty(
                IdentityUtil.getProperty(USE_USER_TENANT_DOMAIN_FOR_OUTBOUND_PROVISIONING_IN_SAAS_APPS))) {
            userTenantBasedProvisioningThreadEnabled = Boolean
                    .parseBoolean(IdentityUtil.getProperty(USE_USER_TENANT_DOMAIN_FOR_OUTBOUND_PROVISIONING_IN_SAAS_APPS));
        }
        return userTenantBasedProvisioningThreadEnabled;
    }

    /**
     * Check whether the outbound provisioning is enabled for the service provider.
     *
     * @param serviceProviderIdentifier service provider.
     * @param tenantDomainName          tenant domain name.
     * @throws IdentityApplicationManagementException
     */
    public static boolean isOutboundProvisioningEnabled(String serviceProviderIdentifier,
                                                        String tenantDomainName) throws IdentityApplicationManagementException {

        ServiceProvider serviceProvider = ApplicationManagementService.getInstance()
                .getServiceProvider(serviceProviderIdentifier, tenantDomainName);

        if (serviceProvider == null) {
            throw new IdentityApplicationManagementException("Cannot find the service provider " +
                    serviceProviderIdentifier);
        }

        OutboundProvisioningConfig outboundProvisioningConfiguration = serviceProvider
                .getOutboundProvisioningConfig();

        if (outboundProvisioningConfiguration == null) {
            if (log.isDebugEnabled()) {
                log.debug("No outbound provisioning configuration defined for local service provider.");
            }
            return false;
        }

        // Check whether the provisioning is configured for the service provider.
        IdentityProvider[] provisionningIdPList = outboundProvisioningConfiguration
                .getProvisioningIdentityProviders();

        if (provisionningIdPList != null && provisionningIdPList.length == 0) {
            return false;
        }
        return true;
    }

    /**
     * Check whether the application based outbound provisioning is enabled.
     *
     * @return true if applicationBasedOutboundProvisioningEnabled config is enabled.
     */
    public static boolean isApplicationBasedOutboundProvisioningEnabled() {

        boolean applicationBasedOutboundProvisioningEnabled = false;

        if (StringUtils.isNotEmpty(
                IdentityUtil.getProperty(APPLICATION_BASED_OUTBOUND_PROVISIONING_ENABLED))) {
            applicationBasedOutboundProvisioningEnabled = Boolean
                    .parseBoolean(IdentityUtil.getProperty(APPLICATION_BASED_OUTBOUND_PROVISIONING_ENABLED));
        }
        return applicationBasedOutboundProvisioningEnabled;
    }
}
