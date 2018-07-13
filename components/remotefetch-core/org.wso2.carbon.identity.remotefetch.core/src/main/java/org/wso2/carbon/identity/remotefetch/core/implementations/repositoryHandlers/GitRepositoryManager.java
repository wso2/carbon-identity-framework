/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.remotefetch.core.implementations.repositoryHandlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.wso2.carbon.identity.remotefetch.common.repomanager.RepositoryManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class GitRepositoryManager implements RepositoryManager {

    private String uri = "";
    private String branch = "";
    private String name = "";
    private File repoPath;
    private Repository repo;
    private Git git;

    private static Log log = LogFactory.getLog(GitRepositoryManager.class);

    public GitRepositoryManager(String name, String uri, String branch) {

        this.name = name;
        this.branch = branch;
        this.uri = uri;
        this.repoPath = new File(System.getProperty("java.io.tmpdir") + this.name);

        log.info("Repository directory set to " + this.repoPath.getAbsolutePath());

        // Check if repo path exists, if so load as local repo
        try {
            if (this.repoPath.exists() && this.repoPath.isDirectory()) {
                log.info("Found local repository");
                this.repo = this.getLocalRepository();
                this.git = new Git(this.repo);
            }
        } catch (IOException e) {
            log.info("IOException setting local repository, will be cloned");
        }
    }

    private Repository cloneRepository() throws IllegalArgumentException, IOException {

        CloneCommand cloneRequest = Git.cloneRepository()
                .setURI(this.uri)
                .setDirectory(this.repoPath)
                .setBranchesToClone(Arrays.asList(branch))
                .setBranch(this.branch);
        try {
            return cloneRequest.call().getRepository();
        } catch (InvalidRemoteException e) {
            throw new IllegalArgumentException("Supplied Remote is invalid", e);
        } catch (GitAPIException e) {
            throw new IOException("Error Cloning repository", e);
        }
    }

    private Repository getLocalRepository() throws IOException {

        FileRepositoryBuilder localBuilder = new FileRepositoryBuilder();
        return localBuilder.findGitDir(this.repoPath)
                .build();
    }

    private void pullRepository() throws Exception {

        PullCommand pullRequest = this.git.pull();
        try {
            pullRequest.call();
            log.info("Pulling changes from remote");
        } catch (GitAPIException e) {
            throw new Exception("Local Repository pull failed");
        }
    }

    private RevCommit getLastCommit(File path) throws Exception {

        List<RevCommit> log = new ArrayList<>();
        Iterable<RevCommit> logIterater = git.log().addPath(path.getPath()).call();
        logIterater.forEach(log::add);
        return log.get(0);
    }

    public void fetchRepository() throws Exception {

        if (this.git != null) {
            this.pullRepository();
        } else {
            log.info("Cloning repository from remote");
            this.repo = this.cloneRepository();
            this.git = new Git(this.repo);
        }
    }

    public InputStream getFile(File location) throws Exception {

        try (ObjectReader reader = this.repo.newObjectReader()) {
            RevCommit commit = this.getLastCommit(location);
            try {
                TreeWalk treewalk = TreeWalk.forPath(this.repo, location.getPath(), commit.getTree());

                if (treewalk != null) {
                    return reader.open(treewalk.getObjectId(0)).openStream();
                }
            } catch (Exception e) {
                throw new Exception("File checkout exception");
            }

        } catch (IOException e) {
            throw new Exception("I/O Exception");
        }
        return null;
    }

    public Date getLastModified(File location) throws Exception {

        try {
            RevCommit commit = getLastCommit(location);
            return new Date((long) commit.getCommitTime() * 1000); //UNIX timestamp to seconds
        } catch (Exception e) {
            throw new Exception("Repository I/O exception");
        }
    }

    @Override
    public String getRevisionHash(File location) throws Exception {

        try {
            RevCommit rc = this.getLastCommit(location);
            return rc.getName();
        } catch (Exception e) {
            throw new Exception("Repository I/O exception");
        }
    }

    public List<File> listFiles(File root) throws Exception {

        List<File> availableFiles = new ArrayList<>();

        TreeWalk treeWalk = new TreeWalk(this.repo);
        TreeFilter pathFilter = PathFilter.create(root.getPath());

        RevWalk revWalk = new RevWalk(this.repo);
        ObjectId headRef = this.repo.resolve(Constants.HEAD);
        treeWalk.addTree(revWalk.parseCommit(headRef).getTree());

        treeWalk.setRecursive(false);
        treeWalk.setFilter(pathFilter);

        try {
            while (treeWalk.next()) {
                if (treeWalk.isSubtree()) {
                    treeWalk.enterSubtree();
                } else {
                    availableFiles.add(new File(treeWalk.getPathString()));
                }
            }
            return availableFiles;
        } catch (IOException e) {
            throw new Exception("Exception on traversing for give path");
        }
    }
}
