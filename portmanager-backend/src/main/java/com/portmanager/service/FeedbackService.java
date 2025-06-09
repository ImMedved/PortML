package com.portmanager.service;

import ch.qos.logback.classic.Logger;
import com.portmanager.client.MlServiceClient;
import com.portmanager.dto.PairwiseFeedbackDto;
import com.portmanager.entity.PairwiseFeedbackEntity;
import com.portmanager.repository.PairwiseFeedbackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

/**
 * FeedbackService
 *
 * Persists user choice, enriches it with plan KPI/algorithm info,
 * then forwards minimal DTO to ML-service.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FeedbackService {

    private final PairwiseFeedbackRepository repo;
    private final MlServiceClient mlClient;
    private final PairwiseSessionCache sessionCache;
    private static final Logger log = (Logger) LoggerFactory.getLogger(FeedbackService.class);

    public void accept(PairwiseFeedbackDto dto) {

        // 1. Pull original pair from cache
        var request = sessionCache.pop(dto.getComparisonId());
        if (request == null) {
            log.warn("No cached pair for {}", dto.getComparisonId());
            return;
        }

        // 2. Persist enriched entity
        var entity = PairwiseFeedbackEntity.builder()
                .comparisonId(dto.getComparisonId())
                .timestamp(OffsetDateTime.now())
                .planAAlgorithm(request.getPlanA().getAlgorithmUsed().getCode())
                .planBAlgorithm(request.getPlanB().getAlgorithmUsed().getCode())
                .planAWaiting(request.getPlanA().getMetrics().getTotalWaitingTimeHours())
                .planBWaiting(request.getPlanB().getMetrics().getTotalWaitingTimeHours())
                .chosenPlan(dto.getChosenPlan())
                .build();
        repo.save(entity);

        // 3. Forward bare DTO to ML (он учится на выбранном плане)
        mlClient.sendFeedback(dto);
        log.info("Feedback stored & forwarded: {}", entity);
    }
}
