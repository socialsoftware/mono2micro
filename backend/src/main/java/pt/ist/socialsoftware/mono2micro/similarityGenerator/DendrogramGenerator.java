package pt.ist.socialsoftware.mono2micro.similarityGenerator;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import pt.ist.socialsoftware.mono2micro.similarity.domain.algorithm.Dendrogram;

import static pt.ist.socialsoftware.mono2micro.utils.Constants.SCRIPTS_ADDRESS;

public class DendrogramGenerator {
    public void generateDendrogram(Dendrogram dendrogram) {
        String response = WebClient.create(SCRIPTS_ADDRESS)
                .get()
                .uri("/scipy/{similarityName}/{similarityMatrixName}/{linkageType}/createDendrogram", dendrogram.getName(), dendrogram.getSimilarityMatrixName(), dendrogram.getLinkageType())
                .retrieve()
                .onStatus(HttpStatus::isError, clientResponse -> {throw new RuntimeException("Error Code:" + clientResponse.statusCode());})
                .bodyToMono(String.class)
                .block();
        try {
            JSONObject jsonObject = new JSONObject(response);
            dendrogram.setDendrogramName(jsonObject.getString("dendrogramName"));
            dendrogram.setCopheneticDistanceName(jsonObject.getString("copheneticDistanceName"));
        } catch(Exception e) { throw new RuntimeException("Could not produce or extract elements from JSON Object"); }
    }
}
