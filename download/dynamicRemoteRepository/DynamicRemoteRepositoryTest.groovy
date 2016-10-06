package org.jfrog.plugindev.test

import groovyx.net.http.HttpResponseException
import org.jfrog.artifactory.client.Artifactory
import org.jfrog.artifactory.client.ArtifactoryClient
import org.jfrog.artifactory.client.ArtifactoryRequest
import org.jfrog.artifactory.client.impl.ArtifactoryRequestImpl
import spock.lang.Specification


/**
 * Created by matank on 06/10/2016.
 */
class DynamicRemoteRepositoryTest extends Specification {

    def "Fetch artifact before downloading" () {
        setup:
        Artifactory artifactory = ArtifactoryClient.create("http://localhost:8081/artifactory")
        when:
        ArtifactoryRequest ar = new ArtifactoryRequestImpl()
                .method(org.jfrog.artifactory.client.ArtifactoryRequest.Method.GET)
                .apiUrl("libs-release-local/org/jfrog/artifactory/client/artifactory-java-client-services/2.1.0/artifactory-java-client-services-2.1.0.jar")
                .addQueryParam("url", "http://jcenter.bintray.com")
        artifactory.restCall(ar)
        then:
        artifactory.searches().repositories("libs-release-local").artifactsByName("artifactory-java-client-services-2.1.0.jar").doSearch().size() == 1
        cleanup:
        try{
            artifactory = ArtifactoryClient.create("http://localhost:8081/artifactory","admin", "password")
            artifactory.repository("libs-release-local").delete("org/jfrog/artifactory/client/artifactory-java-client-services/2.1.0/artifactory-java-client-services-2.1.0.jar")
        } catch (HttpResponseException hre) {
            println hre.getMessage()
        }
    }

}
