package pt.ist.socialsoftware.mono2micro.similarity.domain.dendrogram;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;

import static pt.ist.socialsoftware.mono2micro.utils.Constants.SCRIPTS_ADDRESS;

public class Dendrogram {
    private String dendrogramName;
    private String copheneticDistanceName;

    public Dendrogram() {}

    public Dendrogram(String similarityName, String similarityMatrixName, String linkageType) {
        generateDendrogram(similarityName, similarityMatrixName, linkageType);
    }

    public String getDendrogramName() {
        return dendrogramName;
    }

    public void setDendrogramName(String dendrogramName) {
        this.dendrogramName = dendrogramName;
    }

    public String getCopheneticDistanceName() {
        return copheneticDistanceName;
    }

    public void setCopheneticDistanceName(String copheneticDistanceName) {
        this.copheneticDistanceName = copheneticDistanceName;
    }

    public void generateDendrogram(String similarityName, String similarityMatrixName, String linkageType) {
        String response = WebClient.create(SCRIPTS_ADDRESS)
                .get()
                .uri("/scipy/{similarityName}/{similarityMatrixName}/{linkageType}/createDendrogram", similarityName, similarityMatrixName, linkageType)
                .retrieve()
                .onStatus(HttpStatus::isError, clientResponse -> {throw new RuntimeException("Error Code:" + clientResponse.statusCode());})
                .bodyToMono(String.class)
                .block();
        try {
            JSONObject jsonObject = new JSONObject(response);
            setDendrogramName(jsonObject.getString("dendrogramName"));
            setCopheneticDistanceName(jsonObject.getString("copheneticDistanceName"));
        } catch (Exception e) { throw new RuntimeException("Could not produce or extract elements from JSON Object"); }
    }
}
