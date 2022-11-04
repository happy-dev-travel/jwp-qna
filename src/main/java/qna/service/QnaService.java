package qna.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qna.CannotDeleteException;
import qna.NotFoundException;
import qna.domain.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class QnaService {
    private static final Logger log = LoggerFactory.getLogger(QnaService.class);

    private QuestionRepository questionRepository;
    private AnswerRepository answerRepository;
    private DeleteHistoryService deleteHistoryService;

    public QnaService(QuestionRepository questionRepository, AnswerRepository answerRepository, DeleteHistoryService deleteHistoryService) {
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
        this.deleteHistoryService = deleteHistoryService;
    }

    @Transactional(readOnly = true)
    public Question findQuestionById(Long id) {
        return questionRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(NotFoundException::new);
    }

    @Transactional
    public void deleteQuestion(User loginUser, Long questionId) throws CannotDeleteException {
        List<DeleteHistory> deleteHistories = new ArrayList<>();
        Question question = getQuestionToDelete(loginUser, questionId);
        deleteHistories.add(DeleteHistory.ofQuestion(question.getId(), question.getWriter()));

        List<Answer> answers = getAnswersToDelete(loginUser, question);
        List<DeleteHistory> answerHistories = getDeleteHistories(answers);
        deleteHistories.addAll(answerHistories);

        deleteHistoryService.saveAll(deleteHistories);
    }

    private List<DeleteHistory> getDeleteHistories(List<Answer> answers) {
        return answers.stream()
                .map(answer -> {
                    answer.setDeleted(true);
                    return DeleteHistory.ofAnswer(answer.getId(), answer.getWriter());
                }).
                collect(Collectors.toList());
    }

    private List<Answer> getAnswersToDelete(User loginUser, Question question) throws CannotDeleteException {
        List<Answer> answers = answerRepository.findByQuestionAndDeletedFalse(question);
        for (Answer answer : answers) {
            if (!answer.isOwner(loginUser)) {
                throw new CannotDeleteException("다른 사람이 쓴 답변이 있어 삭제할 수 없습니다.");
            }
        }
        return answers;
    }

    private Question getQuestionToDelete(User loginUser, Long questionId) throws CannotDeleteException {
        Question question = findQuestionById(questionId);
        if (!question.isOwner(loginUser)) {
            throw new CannotDeleteException("질문을 삭제할 권한이 없습니다.");
        }
        question.setDeleted(true);
        return question;
    }
}
