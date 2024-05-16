package org.wso2.carbon.light.registry.mgt.model;

/**
 * This represents a resource ID. It keeps the path used to uniquely identify the resource in the current state.
 * Additionally, it stores more detailed information about the path, including the path ID and resource name.
 * If the resource is a collection, pathID = REG_PATH_ID(path) and name = null.
 * If the resource is not a collection, pathID = REG_PATH_ID(parentPath(path)) and name = the name component of the path ID.
 * So path = parentPath(path) + "/" + name component of the path.
 */
public class ResourceID {

    private String path;
    private int pathID;
    private int tenantId;
    private String resourceName;

    private boolean isCollection;

    /**
     * Returning the full path, i.e. parentPath + resourceName
     *
     * @return the path.
     */
    public String getPath() {

        return path;
    }

    /**
     * Setting the full path i.e. parentPath + resourceName
     *
     * @param path the path.
     */
    public void setPath(String path) {

        this.path = path;
    }

    /**
     * Method to check whether the resource is a collection.
     *
     * @return true, if it is a collection, false otherwise.
     */
    public boolean isCollection() {

        return isCollection;
    }

    /**
     * Method to set whether the resource is a collection.
     *
     * @param collection whether this is a collection or not.
     */
    public void setCollection(boolean collection) {

        isCollection = collection;
    }

    /**
     * Method to get the path id, If the resource is a collection, pathID = REG_PATH_ID(path) name =
     * null If the resource is not a collection, pathId = REG_PATH_ID(parentPath(path))
     *
     * @return the path id.
     */
    public int getPathID() {

        return pathID;
    }

    /**
     * Method to set the path id.
     *
     * @param pathID the path id.
     */
    public void setPathID(int pathID) {

        this.pathID = pathID;
    }

    /**
     * Method to get the tenant id.
     *
     * @return the tenant id.
     */
    public int getTenantId() {

        return tenantId;
    }

    /**
     * Method to set the tenant id.
     *
     * @param tenantId the tenant id.
     */
    public void setTenantId(int tenantId) {

        this.tenantId = tenantId;
    }

    /**
     * Method to get the resource name.
     *
     * @return the resource name.
     */
    public String getResourceName() {

        return resourceName;
    }

    /**
     * Method to set the resource name.
     *
     * @param resourceName the resource name.
     */
    public void setResourceName(String resourceName) {

        this.resourceName = resourceName;
    }

    /**
     * The string value of the resource ID = getPath();
     *
     * @return the string value of the resource id.
     */
    public String toString() {
        // logic to convert the id to string for the authorization purpose of UM
        return path;
    }
}
