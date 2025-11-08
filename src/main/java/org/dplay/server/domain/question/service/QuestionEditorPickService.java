package org.dplay.server.domain.question.service;

import org.dplay.server.domain.question.entity.QuestionEditorPick;

import java.util.List;

public interface QuestionEditorPickService {

    List<QuestionEditorPick> getOrderedEditorPicks(Long questionId);
}
