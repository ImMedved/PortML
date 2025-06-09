package com.portmanager.service;

import com.portmanager.dto.PairwiseRequestDto;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PairwiseSessionCache
 *
 * Keeps recently issued PairwiseRequest objects in memory so that feedback
 * handler can enrich DB record with algorithms & KPI values.
 * In demo this is enough; in prod – move to Redis or DB.
 */
@Component
public class PairwiseSessionCache {

    private final Map<String, PairwiseRequestDto> cache = new ConcurrentHashMap<>();

    public void save(PairwiseRequestDto dto) {
        cache.put(dto.getComparisonId(), dto);
    }

    public PairwiseRequestDto pop(String comparisonId) {
        return cache.remove(comparisonId);   // remove ⇒ one-time use
    }
}
