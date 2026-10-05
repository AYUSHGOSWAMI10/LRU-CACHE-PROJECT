package com.lru.project.model;

import java.util.List;

public final class SimulationModels {
    private SimulationModels() { }

    public record SimulateRequest(int frames, String referenceString) { }
    public record PageStep(int position, int page, String result, List<Integer> memoryState, Integer evictedPage) { }
    public record SimulationOutcome(Integer id, int frames, String referenceString, int hits, int faults,
                                    double hitRatio, String createdAt, List<PageStep> steps) { }
    public record HistoryRow(int id, int frames, String referenceString, int hits, int faults,
                             double hitRatio, String createdAt) { }
    public record CacheRequest(String operation, String key, String value, Integer capacity) { }
    public record CacheItem(String key, String value) { }
    public record CacheResponse(String operation, String message, int capacity, int size,
                                String evictedKey, List<CacheItem> entries) { }
    public record ApiError(String error) { }
}
