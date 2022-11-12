package dto;

import org.json.JSONObject;

public class AttentionPath {
    private Float score;
    private String startToken;
    private String endToken;
    private String path;

    public AttentionPath() {}

    public AttentionPath(
        Float score,
        String startToken,
        String endToken,
        String path
    ) {
        this.score = score;
        this.startToken = startToken;
        this.endToken = endToken;
        this.path = path;
    }

    public AttentionPath(JSONObject jsonObject) {
        this.score = jsonObject.getFloat("score");
        this.startToken = jsonObject.getString("startToken");
        this.endToken = jsonObject.getString("endToken");
        this.path = jsonObject.getString("path");
    }

    public Float getScore() { return this.score; }
    public String getPath() { return this.path; }

    public void setScore(Float score) { this.score = score; }
    public String getStartToken() { return startToken; }

    public void setStartToken(String startToken) { this.startToken = startToken; }
    public String getEndToken() { return endToken; }

    public void setEndToken(String endToken) { this.endToken = endToken; }
    public void setPath(String path) { this.path = path; }

    @Override
    public String toString() {
        return "[" + path + ',' + score.toString() + ']';
    }

}
