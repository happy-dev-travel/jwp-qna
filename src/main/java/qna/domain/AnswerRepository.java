package qna.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnswerRepository extends JpaRepository<Answer, Long> {
    List<Answer> findByQuestionAndDeletedFalse(Question question);

    List<Answer> findByDeletedTrue();

    Optional<Answer> findTopByOrderByIdDesc();

    Optional<Answer> findByIdAndDeletedFalse(Long id);

    List<Answer> findByContentsContains(String text);

    List<Answer> findByWriterAndDeletedFalse(User writer);
}
