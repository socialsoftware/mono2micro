package pt.ulisboa.tecnico.socialsoftware.tutor.question.dto;
public class ImageDto implements java.io.Serializable {
    private java.lang.Integer id;

    private java.lang.String url;

    private java.lang.Integer width;

    public ImageDto() {
    }

    public ImageDto(pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Image image) {
        this.id = image.getId();
        this.width = image.getWidth();
        this.url = image.getUrl();
    }

    public java.lang.Integer getId() {
        return id;
    }

    public void setId(java.lang.Integer id) {
        this.id = id;
    }

    public java.lang.String getUrl() {
        return url;
    }

    public void setUrl(java.lang.String url) {
        this.url = url;
    }

    public java.lang.Integer getWidth() {
        return width;
    }

    public void setWidth(java.lang.Integer width) {
        this.width = width;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return ((((((("ImageDto{" + "id=") + id) + ", url='") + url) + '\'') + ", width=") + width) + '}';
    }
}