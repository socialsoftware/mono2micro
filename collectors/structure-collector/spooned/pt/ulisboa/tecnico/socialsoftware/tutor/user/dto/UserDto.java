package pt.ulisboa.tecnico.socialsoftware.tutor.user.dto;
public class UserDto implements java.io.Serializable {
    private int id;

    private java.lang.String username;

    private java.lang.String name;

    private pt.ulisboa.tecnico.socialsoftware.tutor.user.User.Role role;

    private java.lang.String creationDate;

    public UserDto(pt.ulisboa.tecnico.socialsoftware.tutor.user.User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.name = user.getName();
        this.role = user.getRole();
        if (user.getCreationDate() != null)
            this.creationDate = user.getCreationDate().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public java.lang.String getUsername() {
        return username;
    }

    public void setUsername(java.lang.String username) {
        this.username = username;
    }

    public java.lang.String getName() {
        return name;
    }

    public void setName(java.lang.String name) {
        this.name = name;
    }

    public pt.ulisboa.tecnico.socialsoftware.tutor.user.User.Role getRole() {
        return role;
    }

    public void setRole(pt.ulisboa.tecnico.socialsoftware.tutor.user.User.Role role) {
        this.role = role;
    }

    public java.lang.String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(java.lang.String creationDate) {
        this.creationDate = creationDate;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return (((((((((((("UserDto{" + "id=") + id) + ", username='") + username) + '\'') + ", name='") + name) + '\'') + ", role=") + role) + ", creationDate=") + creationDate) + '}';
    }
}