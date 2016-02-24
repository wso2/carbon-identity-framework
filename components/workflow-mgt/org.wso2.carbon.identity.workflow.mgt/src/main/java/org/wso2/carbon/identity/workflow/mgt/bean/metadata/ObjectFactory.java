/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


package org.wso2.carbon.identity.workflow.mgt.bean.metadata;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.wso2.carbon.identity.workflow.mgt.bean.metadata package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.wso2.carbon.identity.workflow.mgt.bean.metadata
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link MetaData }
     * 
     */
    public MetaData createMetaData() {
        return new MetaData();
    }

    /**
     * Create an instance of {@link MetaData.Template }
     * 
     */
    public MetaData.Template createMetaDataTemplate() {
        return new MetaData.Template();
    }

    /**
     * Create an instance of {@link MetaData.WorkflowImpl }
     * 
     */
    public MetaData.WorkflowImpl createMetaDataWorkflowImpl() {
        return new MetaData.WorkflowImpl();
    }

    /**
     * Create an instance of {@link ParametersMetaData }
     * 
     */
    public ParametersMetaData createParametersMetaData() {
        return new ParametersMetaData();
    }

    /**
     * Create an instance of {@link ParameterMetaData }
     * 
     */
    public ParameterMetaData createParameterMetaData() {
        return new ParameterMetaData();
    }

    /**
     * Create an instance of {@link InputData }
     * 
     */
    public InputData createInputData() {
        return new InputData();
    }

    /**
     * Create an instance of {@link MapType }
     * 
     */
    public MapType createMapType() {
        return new MapType();
    }

    /**
     * Create an instance of {@link Item }
     * 
     */
    public Item createItem() {
        return new Item();
    }

}
