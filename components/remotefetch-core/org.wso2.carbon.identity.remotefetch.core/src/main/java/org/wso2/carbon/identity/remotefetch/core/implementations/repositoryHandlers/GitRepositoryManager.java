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
import org.wso2.carbon.identity.remotefetch.common.ConfigurationFileStream;
import org.wso2.carbon.identity.remotefetch.common.exceptions.RemoteFetchCoreException;
import org.wso2.carbon.identity.remotefetch.common.repomanager.RepositoryManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class GitRepositoryManager implements RepositoryManager {

    private static final Log log = LogFactory.getLog(GitRepositoryManager.class);

    private String uri = "";
    private String branch = "";
    private String name = "";
    private File repoPath;
    private Repository repo;
    private Git git;

    public GitRepositoryManager(String name, String uri, String branch) {

        this.name = name;
        this.branch = branch;
        this.uri = uri;
        this.repoPath = new File(System.getProperty("java.io.tmpdir") + this.name);

        // Check if repository path exists, if so load as local repository
        try {
            if (this.repoPath.exists() && this.repoPath.isDirectory()) {
                this.repo = this.getLocalRepository();
                this.git = new Git(this.repo);
            }
        } catch (IOException e) {
            log.info("IOException setting local repository, will be cloned");
        }
    }

    private Repository cloneRepository() throws GitAPIException {

        CloneCommand cloneRequest = Git.cloneRepository()
                .setURI(this.uri)
                .setDirectory(this.repoPath)
                .setBranchesToClone(Arrays.asList(branch))
                .setBranch(this.branch);
        return cloneRequest.call().getRepository();
    }

    private Repository getLocalRepository() throws IOException {

        FileRepositoryBuilder localBuilder = new FileRepositoryBuilder();
        return localBuilder.findGitDir(this.repoPath)
                .build();
    }

    private void pullRepository() throws GitAPIException {

        PullCommand pullRequest = this.git.pull();
        pullRequest.call();
    }

    private RevCommit getLastCommit(File path) throws GitAPIException {

        List<RevCommit> revCommits = new ArrayList<>();
        Iterable<RevCommit> logIterater = git.log().addPath(path.getPath()).call();
        logIterater.forEach(revCommits::add);
        return revCommits.get(0);
    }

    @Override
    public void fetchRepository() throws RemoteFetchCoreException {

        if (this.git != null) {
            try {
                this.pullRepository();
            } catch (GitAPIException e) {
                throw new RemoteFetchCoreException("Unable to pull repository from remote", e);
            }
        } else {
            try {
                this.repo = this.cloneRepository();
            } catch (GitAPIException e) {
                throw new RemoteFetchCoreException("Unable to clone repository from remote", e);
            }
            this.git = new Git(this.repo);
        }
    }

    @Override
    public ConfigurationFileStream getFile(File location) throws RemoteFetchCoreException {

        try (ObjectReader reader = this.repo.newObjectReader()) {
            RevCommit commit = this.getLastCommit(location);
            TreeWalk treewalk = TreeWalk.forPath(this.repo, location.getPath(), commit.getTree());
            return new ConfigurationFileStream(reader.open(treewalk.getObjectId(0)).openStream(),location);
        } catch (GitAPIException e) {
            throw new RemoteFetchCoreException("Unable to get last revision of file", e);
        } catch (NullPointerException e) {
            throw new RemoteFetchCoreException("Unable to resolve tree for file give", e);
        } catch (IOException e) {
            throw new RemoteFetchCoreException("Unable to read file from local", e);
        }
    }

    @Override
    public Date getLastModified(File location) throws RemoteFetchCoreException {

        try {
            RevCommit commit = getLastCommit(location);
            return new Date((long) commit.getCommitTime() * 1000); //UNIX timestamp to seconds
        } catch (Exception e) {
            throw new RemoteFetchCoreException("Repository I/O exception", e);
        }
    }

    @Override
    public String getRevisionHash(File location) throws RemoteFetchCoreException {

        try {
            RevCommit rc = this.getLastCommit(location);
            return rc.getName();
        } catch (Exception e) {
            throw new RemoteFetchCoreException("Repository I/O exception", e);
        }
    }

    public List<File> listFiles(File root) throws RemoteFetchCoreException {

        List<File> availableFiles = new ArrayList<>();

        TreeWalk treeWalk = new TreeWalk(this.repo);
        TreeFilter pathFilter = PathFilter.create(root.getPath());

        RevWalk revWalk = new RevWalk(this.repo);
        ObjectId headRef;
        try {
            headRef = this.repo.resolve(Constants.HEAD);
            treeWalk.addTree(revWalk.parseCommit(headRef).getTree());
        } catch (IOException e) {
            throw new RemoteFetchCoreException("Exception parsing last commit", e);
        }

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
            throw new RemoteFetchCoreException("Exception on traversing for give path", e);
        }
    }
}
