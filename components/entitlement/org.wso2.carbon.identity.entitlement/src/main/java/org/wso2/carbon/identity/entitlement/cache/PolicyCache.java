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

package org.wso2.carbon.identity.entitlement.cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.entitlement.PDPConstants;
import org.wso2.carbon.identity.entitlement.common.EntitlementConstants;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * PolicyCache is to manage cluster level distributed cache for the status of the policy file. This is not distribute
 * complete policy file within cluster itself.
 */
public class PolicyCache extends EntitlementBaseCache<IdentityCacheKey, PolicyStatus>{

    private static Log log = LogFactory.getLog(PolicyCache.class);
    private static final Object lock = new Object();
    private int myHashCode;
    private static Map<Integer,Integer> cacheInvalidationState = new HashMap<Integer, Integer>();
    private static Map<Integer,Map<String,PolicyStatus>> localPolicyCacheMap = new HashMap<Integer,Map<String,PolicyStatus>>();

    /**
     *
     * @param timeout
     */
    public PolicyCache(int timeout) {
    	super(PDPConstants.ENTITLEMENT_POLICY_INVALIDATION_CACHE,timeout);
        PolicyCacheUpdateListener policyCacheUpdateListener = new PolicyCacheUpdateListener();
        PolicyCacheCreatedListener policyCacheCreatedListener = new PolicyCacheCreatedListener();
        setCacheEntryUpdatedListener(policyCacheUpdateListener);
        setCacheEntryCreatedListener(policyCacheCreatedListener);
        initCacheBuilder();

        if(log.isDebugEnabled()){
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            log.debug("PolicyCache initializing for tenant " + tenantId);
        }
    }

    /**
     * This method triggered by cache entry listener.
     *
     * @param identityCacheKey
     * @param policyStatus
     */
    public static void updateLocalPolicyCacheMap(IdentityCacheKey identityCacheKey, PolicyStatus policyStatus){
        if(identityCacheKey.getKey()!=null) {
            if(!identityCacheKey.getKey().equals("")) {
                if(log.isDebugEnabled()){
                    log.debug("Updating local cache map for the tenant : " + identityCacheKey.getTenantId() + " and Policy : " + identityCacheKey.getKey());
                }
                synchronized (localPolicyCacheMap) {
                    if (localPolicyCacheMap.get(identityCacheKey.getTenantId()) != null) {
                        if(localPolicyCacheMap.get(identityCacheKey.getTenantId()).get(identityCacheKey.getKey())!=null){
                            PolicyStatus status = localPolicyCacheMap.get(identityCacheKey.getTenantId()).get(identityCacheKey.getKey());
                            status.setPolicyAction(getPriorityAction(status.getPolicyAction(),policyStatus.getPolicyAction()));
                            if(log.isDebugEnabled()){
                                log.debug("Updated existing policy in local cache map :  Policy : " + identityCacheKey.getKey() + " and new action : " + getPriorityAction(status.getPolicyAction(),policyStatus.getPolicyAction()));
                            }
                        }else{
                            localPolicyCacheMap.get(identityCacheKey.getTenantId()).put(policyStatus.getPolicyId(),policyStatus);
                            if(log.isDebugEnabled()){
                                log.debug("Adding policy in to the local cache policy map : policy :  " + identityCacheKey.getKey()  );
                            }
                        }
                    } else {
                        Map<String,PolicyStatus> map = new HashMap<String,PolicyStatus>();

                        map.put(policyStatus.getPolicyId(),policyStatus);
                        localPolicyCacheMap.put(identityCacheKey.getTenantId(), map);
                        if(log.isDebugEnabled()){
                            log.debug("Adding policy in to the local cache policy map : policy :  " + identityCacheKey.getKey() + " add new entry for this tenant : " + identityCacheKey.getTenantId() );
                        }

                    }
                }
            }else{
                cacheInvalidationState.put(identityCacheKey.getTenantId(),1);
                if(log.isDebugEnabled()){
                    log.debug("Trigger event to clear all cache in tenant :  " + identityCacheKey.getTenantId());
                }
            }
        }
    }

    /**
     * Do invalidate all policy cache
     */
    public void invalidateCache(){

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        if(log.isDebugEnabled()){
            log.debug("Trigger invalidateCache to tenant :  " + tenantId + " and all policy ");
        }

        IdentityCacheKey cacheKey = new IdentityCacheKey(tenantId,"");
        addToCache(cacheKey,new PolicyStatus());

    }


    /**
     * Check the state of expire in local cache.
     *
     * @return
     */
    public boolean isInvalidate(){

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        int state = 0 ;

        synchronized (cacheInvalidationState) {
            if(cacheInvalidationState.get(tenantId)!=null) {
                state = cacheInvalidationState.get(tenantId);
            }

            cacheInvalidationState.put(tenantId,0);
        }
        if(log.isDebugEnabled()){
            log.debug("Check the invalidation state of all cache : " + state);
        }

        if(state==1){
            return true ;
        }else{
            return false ;
        }

    }


    /**
     *
     * Invalidate any policy with action. It will send the cluster message to clean this policy in all the nodes.
     *
     * @param policyId
     * @param action
     */
    public void invalidateCache(String policyId, String action) {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        if(log.isDebugEnabled()){
            log.debug("Trigger invalidateCache to tenant :  " + tenantId + " and policy " + policyId + " for  action " +
                    ": " + action);
        }

        IdentityCacheKey cacheKey = new IdentityCacheKey(tenantId,policyId);
        PolicyStatus policyStatus = (PolicyStatus)getValueFromCache(cacheKey);

        if(policyStatus==null) {
            policyStatus = new PolicyStatus(policyId,0,action);
        }else{
            policyStatus.setStatusCount(policyStatus.getStatusCount() + 1);
            policyStatus.setPolicyAction(action);
        }
        updateToCache(cacheKey, policyStatus);


        synchronized (localPolicyCacheMap) {
            if(localPolicyCacheMap.get(cacheKey.getTenantId())!=null){
                if(localPolicyCacheMap.get(cacheKey.getTenantId()).get(cacheKey.getKey())!=null){
                    PolicyStatus status = localPolicyCacheMap.get(cacheKey.getTenantId()).get(cacheKey.getKey());
                    status.setPolicyAction(getPriorityAction(status.getPolicyAction(),action));
                }
            }else{
                Map<String,PolicyStatus> map = new HashMap<String,PolicyStatus>();

                map.put(policyId,policyStatus);
                localPolicyCacheMap.put(cacheKey.getTenantId(),map);
            }
        }
    }



    /**
     * Read the invalidated policies in the local cache. Local cache always synch up with the cluster cache.
     *
     * @return
     */
    public Collection<PolicyStatus> getInvalidatedPolicies(){


        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();


        if(log.isDebugEnabled()){
            log.debug("Reading invalidated policy files for : "+ tenantId);
        }
        Collection<PolicyStatus> tmpSet = null ;

        if(localPolicyCacheMap.get(tenantId)!=null) {

            Map<String,PolicyStatus> tmpMap = localPolicyCacheMap.get(tenantId);
            tmpSet = tmpMap.values();
        }

        return tmpSet ;
    }

    /**
     * This method is for get the priority of the action.
     *
     * ex: There is already updated action saying change the order of that. But another action coming syaing delete
     * this. What would be the latest state of that policy cache ? It is decided by the following order. In this case
     * even though the older state is -re-order, we have to change it to the delete because delete is highher than
     * the re-order.
     *
     * @param currentAction
     * @param newAction
     * @return
     */
    private static String getPriorityAction(String currentAction, String newAction){

        if(EntitlementConstants.PolicyPublish.ACTION_DELETE.equals(currentAction) || EntitlementConstants.PolicyPublish.ACTION_DELETE.equals(newAction)){
            return EntitlementConstants.PolicyPublish.ACTION_DELETE;
        }else if(EntitlementConstants.PolicyPublish.ACTION_DISABLE.equals(currentAction) || EntitlementConstants.PolicyPublish.ACTION_DISABLE.equals(newAction)){
            return EntitlementConstants.PolicyPublish.ACTION_DELETE;
        }else if(EntitlementConstants.PolicyPublish.ACTION_ENABLE.equals(currentAction) || EntitlementConstants.PolicyPublish.ACTION_ENABLE.equals(newAction)){
            return EntitlementConstants.PolicyPublish.ACTION_CREATE;
        }else if(EntitlementConstants.PolicyPublish.ACTION_UPDATE.equals(currentAction) || EntitlementConstants.PolicyPublish.ACTION_UPDATE.equals(newAction)){
            return EntitlementConstants.PolicyPublish.ACTION_UPDATE;
        }else if(EntitlementConstants.PolicyPublish.ACTION_ORDER.equals(currentAction) || EntitlementConstants.PolicyPublish.ACTION_ORDER.equals(newAction)){
            return EntitlementConstants.PolicyPublish.ACTION_ORDER;
        }
        return newAction ;
    }


}
