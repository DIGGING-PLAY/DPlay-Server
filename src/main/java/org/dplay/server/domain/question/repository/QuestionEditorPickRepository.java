package org.dplay.server.domain.question.repository;

import org.dplay.server.domain.question.entity.QuestionEditorPick;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionEditorPickRepository extends JpaRepository<QuestionEditorPick, Long> {
}
