import jakarta.persistence.*;
import java.util.List;

@Entity
public class Author extends BaseEntity {

    private String name;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL)
    private List<Book> books;

    public Author() {
    }
}