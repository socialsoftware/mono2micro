package pt.ist.socialsoftware.mono2micro.services;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import static pt.ist.socialsoftware.mono2micro.utils.Constants.SCRIPTS_ADDRESS;

@Service
public class ClusterService {

    public void executeClusterAnalysis(
            String codebaseName
    ) {
        WebClient.create(SCRIPTS_ADDRESS)
                .get()
                .uri("/scipy/{codebaseName}/analyser/features/methodCalls", codebaseName)
                .exchange()
                .doOnSuccess(clientResponse -> {
                    if (clientResponse.statusCode() != HttpStatus.OK)
                        throw new RuntimeException("Error Code:" + clientResponse.statusCode());
                }).block();
    }

}
