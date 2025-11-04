package org.dplay.server.domain.question.repository;

import org.dplay.server.domain.question.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    Optional<Question> findByDisplayDate(LocalDate displayDate);

    List<Question> findByDisplayDateBetweenOrderByDisplayDateAsc(LocalDate startDate, LocalDate endDate);
}
