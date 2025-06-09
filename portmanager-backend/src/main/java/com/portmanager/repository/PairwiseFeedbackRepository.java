// src/main/java/com/portmanager/repository/PairwiseFeedbackRepository.java
package com.portmanager.repository;

import com.portmanager.entity.PairwiseFeedbackEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PairwiseFeedbackRepository extends JpaRepository<PairwiseFeedbackEntity, Long> {}
