package org.dplay.server.domain.question.service.impl;

import lombok.RequiredArgsConstructor;
import org.dplay.server.domain.question.entity.QuestionEditorPick;
import org.dplay.server.domain.question.repository.QuestionEditorPickRepository;
import org.dplay.server.domain.question.service.QuestionEditorPickService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionEditorPickServiceImpl implements QuestionEditorPickService {

    private final QuestionEditorPickRepository questionEditorPickRepository;

    @Override
    public List<QuestionEditorPick> getOrderedEditorPicks(Long questionId) {
        return questionEditorPickRepository.findByQuestionQuestionIdOrderByPositionAsc(questionId);
    }
}
