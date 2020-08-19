package org.wso2.carbon.identity.cors.mgt.core.model;

/**
 * An application which has an association with a particular CORS origin.
 */
public class CORSApplication {

    /**
     * ID of the application.
     */
    private String id;

    /**
     * Name of the application.
     */
    private String name;

    /**
     * Constructor for Application.
     *
     * @param id ID of the application.
     */
    public CORSApplication(String id) {

        this.id = id;
    }

    /**
     * Constructor for CORSApplication.
     *
     * @param id   ID of the application.
     * @param name Name of the application.
     */
    public CORSApplication(String id, String name) {

        this.id = id;
        this.name = name;
    }

    /**
     * Get the {@code id}.
     *
     * @return Returns the {@code id}.
     */
    public String getId() {

        return id;
    }

    /**
     * Set the {@code id}.
     */
    public void setId(String id) {

        this.id = id;
    }

    /**
     * Get the {@code name}.
     *
     * @return Returns the {@code name}.
     */
    public String getName() {

        return name;
    }

    /**
     * Set the {@code name}.
     */
    public void setName(String name) {

        this.name = name;
    }
}
