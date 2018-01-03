/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");         
* you may not use this file except in compliance with the License.        
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0 
*
* Unless required by applicable law or agreed to in writing, software     
* distributed under the License is distributed on an "AS IS" BASIS,       
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and     
* limitations under the License.
*/

package org.wso2.carbon.identity.entitlement.extension;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.balana.attr.AttributeValue;
import org.wso2.balana.attr.BooleanAttribute;
import org.wso2.balana.attr.StringAttribute;
import org.wso2.balana.cond.Evaluatable;
import org.wso2.balana.cond.EvaluationResult;
import org.wso2.balana.cond.FunctionBase;
import org.wso2.balana.ctx.EvaluationCtx;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;
import org.wso2.carbon.user.core.UserStoreException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EvalPermissionTreeFunction extends FunctionBase {

    /**
     * Standard identifier for the eval-permission-tree function
     */
    public static final String SUBJECT_HAS_PERMISSION = FUNCTION_NS + "eval-permission-tree";

    // private identifiers for the supported functions
    private static final int ID_EVAL_PERMISSION_TREE = 0;

    private static final Log log = LogFactory.getLog(EvalPermissionTreeFunction.class);

    public EvalPermissionTreeFunction() {

        super(SUBJECT_HAS_PERMISSION, ID_EVAL_PERMISSION_TREE, StringAttribute.identifier, false, 2, 2,
                BooleanAttribute.identifier, false);
    }

    public static Set getSupportedIdentifiers() {
        
        Set set = new HashSet();
        set.add(SUBJECT_HAS_PERMISSION);

        return set;
    }

    public EvaluationResult evaluate(List<Evaluatable> inputs, EvaluationCtx context) {

        AttributeValue[] argValues = new AttributeValue[inputs.size()];
        EvaluationResult result = evalArgs(inputs, context, argValues);
        if (result != null) {
            return result;
        }

        switch (getFunctionId()) {
            case ID_EVAL_PERMISSION_TREE:
                String resource = ((StringAttribute) argValues[0]).getValue().trim();
                String subject = ((StringAttribute) argValues[1]).getValue().trim();

                boolean isAuthorised = false;

                try {
                    isAuthorised = EntitlementServiceComponent.getRealmservice().getBootstrapRealm().
                            getAuthorizationManager().isUserAuthorized(subject, resource, "ui.execute");
                } catch (UserStoreException e) {
                    log.error("Error while authorising" + subject + " to perform ui.execute on " + resource, e);
                }

                result = new EvaluationResult(BooleanAttribute.getInstance(isAuthorised));
                break;
        }

        return result;
    }
}
