/*
 * Copyright (c) 2007, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.light.registry.mgt.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.light.registry.mgt.LightRegistryException;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * The default registry implementation of the Collection interface.
 */
public class CollectionImpl extends ResourceImpl implements Collection {

    private static final Log log = LogFactory.getLog(CollectionImpl.class);

    /**
     * The number of children in this collection.
     */
    protected int childCount;

    /**
     * The default constructor of the CollectionImpl, Create an empty collection with no children.
     */
    public CollectionImpl() {

        childCount = -1;
    }

    /**
     * Construct a collection with the provided children paths.
     *
     * @param paths the children paths.
     */
    public CollectionImpl(String[] paths) {

        try {
            setChildren(paths);
        } catch (LightRegistryException e) {
            log.warn("Unable to set child paths to this collection.", e);
        }
    }

    /**
     * Construct a collection with the provided path and the resource data object.
     *
     * @param path     the path of the collection.
     * @param resource the resource data object.
     */
    public CollectionImpl(String path, Resource resource) {

        super(path, resource);
        childCount = -1;
    }

    /**
     * A copy constructor used to create a shallow-copy of this collection.
     *
     * @param collection the collection of which the copy is created.
     */
    public CollectionImpl(CollectionImpl collection) {

        super(collection);
        if (this.content != null) {
            if (this.content instanceof String[]) {
                String[] paths = (String[]) this.content;
                int length = paths.length;
                String[] output = new String[length];
                System.arraycopy(paths, 0, output, 0, length);
                this.content = output;
            } else if (this.content instanceof Resource[]) {
                Resource[] paths = (Resource[]) this.content;
                int length = paths.length;
                Resource[] output = new Resource[length];
                System.arraycopy(paths, 0, output, 0, length);
                for (int i = 0; i < length; i++) {
                    if (output[i] instanceof CollectionImpl) {
                        output[i] = new CollectionImpl((CollectionImpl) output[i]);
                    } else if (output[i] instanceof ResourceImpl) {
                        output[i] = new ResourceImpl((ResourceImpl) output[i]);
                    }
                }
                this.content = output;
            }
        }
        this.childCount = collection.childCount;
    }

    /**
     * Implementation for the setContent. Here the content should always be an array of strings which
     * corresponding to the children paths.
     *
     * @param content array of strings which corresponding to the children paths.
     * @throws LightRegistryException if the operation fails.
     */
    public void setContent(Object content) throws LightRegistryException {

        if (content == null) {
            return;
        }
        // note that string contents are allowed in collection to support custom generated UIs.
        if (content instanceof String[]) {
            super.setContent(content);
            childCount = ((String[]) content).length;
            return;

        } else if (content instanceof Resource[]) {
            super.setContent(content);
            childCount = ((Resource[]) content).length;
            return;

        } else if (content instanceof String) {
            super.setContent(content);
            return;

        }
        throw new IllegalArgumentException("Invalid content for collection. " +
                "Content of type " + content.getClass().toString() +
                " is not allowed for collections.");
    }

    /**
     * Method to set the absolute paths of the children belonging to this collection. Absolute paths
     * begin from the ROOT collection.
     *
     * @param paths the array of absolute paths of the children
     * @throws LightRegistryException if the operation fails.
     */
    public void setChildren(String[] paths) throws LightRegistryException {

        String[] temp = fixPaths(paths);
        content = temp;
        childCount = temp.length;
    }

    /**
     * Method to return the children.
     *
     * @return an array of children paths.
     */
    public String[] getChildren() {

        if (getContent() instanceof String[]) {
            return fixPaths((String[]) getContent());
        } else {
            return new String[0];
        }
    }

    /**
     * Method to return the number of children.
     *
     * @return the number of children.
     */
    public int getChildCount() {

        if (childCount != -1) {
            return childCount;

        } else if (content instanceof String[]) {

            String[] childPaths = (String[]) content;
            return fixPaths(childPaths).length;

        }
        return 0;
    }

    /**
     * Method to set the child count.
     *
     * @param count the child count.
     */
    public void setChildCount(int count) {

        childCount = count;
    }

    /**
     * Collection's content is a string array, which contains paths of its children. These paths are
     * loaded on demand to increase performance. It is recommended to use {@link #getChildren()}
     * method to get child paths of a collection, which provides pagination. Calling this method
     * will load all child paths.
     *
     * @return String array of child paths.
     */
    public Object getContent() {

        return content;
    }

    /**
     * Method to fix duplicated entries in a collection's child paths.
     *
     * @param paths the collection's child paths.
     * @return the distinct set of children.
     */
    protected String[] fixPaths(String[] paths) {
        // We want to make sure that each element is added one after the other in the exact order
        // that they were passed in.
        Set<String> temp = new LinkedHashSet<>(Arrays.asList(paths));
        return temp.toArray(new String[0]);
    }
}
