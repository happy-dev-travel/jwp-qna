package qna.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import javax.persistence.EntityManager;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class QuestionRepositoryTest {

    @Autowired
    private QuestionRepository repository;

    @BeforeEach
    void setUp() {
        repository.saveAll(Arrays.asList(QuestionTest.Q1,QuestionTest.Q2));
    }

    @Test
    @DisplayName("조회 후 하나를 Deleted True로 설정하면 Delete False인 것이 하나만 조회됨")
    void findByDeletedFalse() {

        List<Question> all = repository.findAll();
        all.get(0).setDeleted(true);

        List<Question> byDeletedFalse = repository.findByDeletedFalse();

        assertThat(byDeletedFalse.size()).isEqualTo(1);
        assertThat(byDeletedFalse.get(0).getContents()).isEqualTo(QuestionTest.Q2.getContents());
    }

    @Test
    void findByIdAndDeletedFalse() {
        Optional<Question> found = repository.findByIdAndDeletedFalse(1L);

        assertThat(found.get().getContents()).isEqualTo(QuestionTest.Q1.getContents());
    }

    @Test
    @DisplayName("column 길이 제약조건을 어기면 DataIntegrityViolationException 이 발생함")
    void test4() {
        String stringLengthOver = prepareContentsOverLength(101);

        Question question = new Question(null, stringLengthOver, "contents");

        assertThatThrownBy(() -> repository.save(question))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("could not execute statement; SQL [n/a]");
    }

    private static String prepareContentsOverLength(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append('a');
        }
        return sb.toString();
    }
}