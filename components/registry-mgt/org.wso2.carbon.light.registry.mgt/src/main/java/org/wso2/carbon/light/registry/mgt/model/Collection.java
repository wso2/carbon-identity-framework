package org.wso2.carbon.light.registry.mgt.model;

import org.wso2.carbon.light.registry.mgt.LightRegistryException;

public interface Collection extends Resource {

    /**
     * Method to return the absolute paths of the children of the collection
     *
     * @return the array of absolute paths of the children
     * @throws LightRegistryException if the operation fails.
     */
    String[] getChildren() throws LightRegistryException;

    /**
     * Method to return the number of children.
     *
     * @return the number of children.
     * @throws LightRegistryException if the operation fails.
     */
    int getChildCount() throws LightRegistryException;

    /**
     * Method to set the absolute paths of the children belonging to this collection. Absolute paths
     * begin from the ROOT collection.
     *
     * @param paths the array of absolute paths of the children
     *
     * @throws LightRegistryException if the operation fails.
     */
    void setChildren(String[] paths) throws LightRegistryException;

}
