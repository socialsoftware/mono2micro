import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/books")
public class BookController {

    @Autowired
    private BookRepository repository;

    @GetMapping("/{id}")
    public String getBookTitle(@PathVariable Long id) {
        return getBookTitleFromRepo(id);
    }

    public String getBookTitleFromRepo(Long id) {
        return repository.findById(id) // Read operation
            .orElse(new Book("")) // Write operation
            .getTitle();
    }

}
