package org.dplay.server.domain.question.repository;

import org.dplay.server.domain.question.entity.QuestionEditorPick;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionEditorPickRepository extends JpaRepository<QuestionEditorPick, Long> {

    List<QuestionEditorPick> findByQuestionQuestionIdOrderByPositionAsc(Long questionId);
}
