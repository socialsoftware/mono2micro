package pt.ulisboa.tecnico.socialsoftware.tutor.question.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
@org.springframework.stereotype.Repository
@org.springframework.transaction.annotation.Transactional
public interface ImageRepository extends org.springframework.data.jpa.repository.JpaRepository<pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Image, java.lang.Integer> {}