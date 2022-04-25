package pt.ist.socialsoftware.mono2micro.services;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pt.ist.socialsoftware.mono2micro.domain.Dendrogram;

import static pt.ist.socialsoftware.mono2micro.utils.Constants.SCRIPTS_ADDRESS;

@Service
public class ClusterService {

    public void executeCreateDendrogram(String codebaseName, Dendrogram dendrogram, String type)
    {
        WebClient.create(SCRIPTS_ADDRESS)
                .get()
                .uri("/scipy/{codebaseName}/{dendrogramName}/createDendrogram" + type, codebaseName, dendrogram.getName())
                .exchange()
                .doOnSuccess(clientResponse -> {
                    if (clientResponse.statusCode() != HttpStatus.OK)
                        throw new RuntimeException("Error Code:" + clientResponse.statusCode());
                }).block();
    }

    public void executeCutDendrogram(String codebaseName, String dendrogramName, String decompositionName, String decompositionCutType, String decompositionCutValue, String type) {
        WebClient.create(SCRIPTS_ADDRESS)
                .get()
                .uri("/scipy/{codebaseName}/{dendrogramName}/{graphName}/{cutType}/{cutValue}/cut" + type,
                        codebaseName, dendrogramName, decompositionName, decompositionCutType, decompositionCutValue)
                .exchange()
                .doOnSuccess(clientResponse -> {
                    if (clientResponse.statusCode() != HttpStatus.OK)
                        throw new RuntimeException("Error Code:" + clientResponse.statusCode());
                }).block();
    }

    public void executeClusterAnalysis(
            String codebaseName,
            String type
    ) {
        WebClient.create(SCRIPTS_ADDRESS)
                .get()
                .uri("/scipy/{codebaseName}/analyser" + type, codebaseName)
                .exchange()
                .doOnSuccess(clientResponse -> {
                    if (clientResponse.statusCode() != HttpStatus.OK)
                        throw new RuntimeException("Error Code:" + clientResponse.statusCode());
                }).block();
    }

}
