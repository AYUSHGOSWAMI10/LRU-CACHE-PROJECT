package com.lru.project.service;

import com.lru.project.model.SimulationModels.CacheItem;
import com.lru.project.model.SimulationModels.CacheRequest;
import com.lru.project.model.SimulationModels.CacheResponse;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CacheDemoService {
    private static final String SESSION_CACHE = "lruDemoCache";

    @SuppressWarnings("unchecked")
    public CacheResponse operate(HttpSession session, CacheRequest request) {
        if (request == null || request.operation() == null) {
            throw new IllegalArgumentException("Choose PUT, GET, or RESET.");
        }
        String operation = request.operation().trim().toUpperCase();
        LRUCache<String, String> cache = (LRUCache<String, String>) session.getAttribute(SESSION_CACHE);
        String message;
        String evicted = null;

        if ("RESET".equals(operation)) {
            int capacity = request.capacity() == null ? 3 : request.capacity();
            if (capacity < 1 || capacity > 20) throw new IllegalArgumentException("Cache capacity must be between 1 and 20.");
            cache = new LRUCache<>(capacity);
            session.setAttribute(SESSION_CACHE, cache);
            message = "Cache reset with capacity " + capacity + ".";
        } else {
            if (cache == null) {
                cache = new LRUCache<>(3);
                session.setAttribute(SESSION_CACHE, cache);
            }
            if ("PUT".equals(operation)) {
                requireKey(request.key());
                if (request.value() == null) throw new IllegalArgumentException("Enter a value to store.");
                boolean existed = cache.containsKey(request.key().trim());
                if (!existed && cache.size() == cache.capacity()) {
                    LRUCache.Entry<String, String> lru = cache.leastRecentlyUsed();
                    evicted = lru == null ? null : lru.key();
                }
                cache.put(request.key().trim(), request.value());
                message = existed ? "Updated the value and marked the key most recently used."
                        : evicted == null ? "Added the key to the cache." : "Added the key; the least recently used entry was evicted.";
            } else if ("GET".equals(operation)) {
                requireKey(request.key());
                String value = cache.get(request.key().trim());
                message = value == null ? "Cache miss: that key is not present." : "Cache hit: " + value;
            } else {
                throw new IllegalArgumentException("Choose PUT, GET, or RESET.");
            }
        }

        List<CacheItem> entries = cache.entriesMostRecentFirst().stream()
                .map(entry -> new CacheItem(entry.key(), entry.value())).toList();
        return new CacheResponse(operation, message, cache.capacity(), cache.size(), evicted, entries);
    }

    private void requireKey(String key) {
        if (key == null || key.isBlank()) throw new IllegalArgumentException("Enter a cache key.");
    }
}
