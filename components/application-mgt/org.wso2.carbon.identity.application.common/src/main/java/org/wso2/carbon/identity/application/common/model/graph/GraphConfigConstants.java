/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.common.model.graph;

import javax.xml.namespace.QName;

/**
 * Common constants for the Graph model.
 */
public class GraphConfigConstants {
    public static final String AUTHENTICATION_STEP_LOCAL_NAME = "AuthenticationStep";
    public static final String AUTHENTICATION_DECISION_LOCAL_NAME = "AuthenticationDecision";
    public static final String LINK_LOCAL_NAME = "Link";
    public static final String EVALUATOR_CLASS_LOCAL_NAME = "Evaluator";

    public static final QName ATTR_NAME = new QName(null, "name");
    public static final QName ATTR_DEFAULT_LINK= new QName(null, "defaultLink");
    public static final QName ATTR_EXPRESSION = new QName(null, "expression");
    public static final QName ATTR_END = new QName(null, "end");
    public static final QName ATTR_NEXT = new QName(null, "nextLink");
}
