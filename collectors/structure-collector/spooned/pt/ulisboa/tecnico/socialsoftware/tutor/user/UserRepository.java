package pt.ulisboa.tecnico.socialsoftware.tutor.user;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
@org.springframework.stereotype.Repository
@org.springframework.transaction.annotation.Transactional
public interface UserRepository extends org.springframework.data.jpa.repository.JpaRepository<pt.ulisboa.tecnico.socialsoftware.tutor.user.User, java.lang.Integer> {
    @org.springframework.data.jpa.repository.Query(value = "select * from users u where u.username = :username", nativeQuery = true)
    pt.ulisboa.tecnico.socialsoftware.tutor.user.User findByUsername(java.lang.String username);

    @org.springframework.data.jpa.repository.Query(value = "select * from users u where u.key = :key", nativeQuery = true)
    pt.ulisboa.tecnico.socialsoftware.tutor.user.User findByKey(java.lang.Integer key);

    @org.springframework.data.jpa.repository.Query(value = "select MAX(id) from users", nativeQuery = true)
    java.lang.Integer getMaxUserNumber();
}