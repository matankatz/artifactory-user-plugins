/*
 * Copyright (C) 2016 JFrog Ltd.
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
 *
 *
 *  Usage: curl http://localhost:8081/artifactory/local-repo/a/b/c.zip?url?http://somesite.com
 *  Artifactory will download the artifact from http://somesite.com/a/b/c.zip and will
 *  deploy it to local-repo/a/b/c.zip
 *
 */


import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import org.artifactory.repo.RepoPath
import org.artifactory.request.Request

import static groovyx.net.http.ContentType.BINARY
import static org.artifactory.repo.RepoPathFactory.create

download {
    beforeDownloadRequest { Request request, RepoPath repoPath ->
        /*
        In the following cases this plugin will not be activated:
            - if url query parameter is missing
            - if the request if not directed to local repository
            - if the requested file already exists locally (based on the path, not checksum)
        */
        if ( request.getParameter("url") == null ) { 
            return 0
        } else if ( !isLocal(repoPath.repoKey) ) {
            log.trace("Only local repositories can be used for dynamic caching")
            return 0
        } else if ( repositories.exists(repoPath) ) {
            log.trace("File exists locally, no need to download it")
            return 0
        }

        URI uri = new URI(request.getParameter("url"))
        if (request.getParameter("url") != null) {
            fetchAndDeploy(
                    uri.toString() + "/" + repoPath.path,
                    repoPath.repoKey,
                    repoPath.path,
                    request.getParameter("username"),
                    request.getParameter("password"))
        }
    }
}

def isLocal(String repoKey) {
    return repositories.getLocalRepositories().contains(repoKey)
}

def boolean fetchAndDeploy(url, repoKey, deployPath, username, password) {
    log.info "Downloading artifact from: $url"
    def http = new HTTPBuilder(url)
    if (username && password) {
        http.auth.basic(username, password)
    }
    // GET request to retrieve remote file
    http.request(Method.GET, BINARY) { req ->
        response.success = { resp, binary ->
            log.info "Got response: ${resp.statusLine}"
            def targetRepoKey = repoKey
            def targetPath = deployPath
            RepoPath deployRepoPath = create(targetRepoKey, targetPath)
            repositories.deploy(deployRepoPath, binary)
        }
        response.failure = { resp ->
            // Can't throw an error to the client from here; returning 0, indicating failure
            log.error "Request failed with the following status code: " + resp.statusLine.statusCode
            return 0
        }
    }
}
