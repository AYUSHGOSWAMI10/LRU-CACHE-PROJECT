package com.lru.project.service;

import com.lru.project.model.SimulationModels.PageStep;
import com.lru.project.model.SimulationModels.SimulationOutcome;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class PageReplacementService {
    public SimulationOutcome simulate(int[] pages, int frames) {
        if (frames <= 0) throw new IllegalArgumentException("Frames must be greater than zero.");
        if (pages == null || pages.length == 0) throw new IllegalArgumentException("Enter at least one page reference.");

        LRUCache<Integer, Integer> memory = new LRUCache<>(frames);
        int hits = 0;
        int faults = 0;
        List<PageStep> steps = new ArrayList<>();
        for (int i = 0; i < pages.length; i++) {
            int page = pages[i];
            String result;
            Integer evicted = null;
            if (memory.get(page) != null) {
                hits++;
                result = "HIT";
            } else {
                faults++;
                result = "FAULT";
                if (memory.size() == frames) {
                    LRUCache.Entry<Integer, Integer> eldest = memory.leastRecentlyUsed();
                    evicted = eldest.key();
                }
                memory.put(page, page);
            }
            List<Integer> state = memory.entriesMostRecentFirst().stream().map(LRUCache.Entry::key).toList();
            steps.add(new PageStep(i + 1, page, result, state, evicted));
        }
        double ratio = hits * 100.0 / pages.length;
        String references = Arrays.stream(pages).mapToObj(String::valueOf).collect(Collectors.joining(" "));
        return new SimulationOutcome(null, frames, references, hits, faults, ratio, null, List.copyOf(steps));
    }

    public int[] parseReferences(String referenceString) {
        if (referenceString == null || referenceString.isBlank()) {
            throw new IllegalArgumentException("Enter at least one page reference.");
        }
        String[] tokens = referenceString.trim().split("[\\s,]+");
        if (tokens.length > 100) throw new IllegalArgumentException("Enter no more than 100 page references.");
        int[] pages = new int[tokens.length];
        try {
            for (int i = 0; i < tokens.length; i++) pages[i] = Integer.parseInt(tokens[i]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Page references must be whole numbers separated by spaces or commas.");
        }
        String normalized = Arrays.stream(pages).mapToObj(String::valueOf).collect(Collectors.joining(" "));
        if (normalized.length() > 500) throw new IllegalArgumentException("The reference string cannot exceed 500 characters.");
        return pages;
    }
}
