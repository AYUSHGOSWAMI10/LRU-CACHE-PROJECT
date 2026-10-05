package com.lru.project;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.function.Consumer;

public class PageReplacementSimulator {
	public record PageStep(int page, String result, String memoryState) {
	}

	public static SimulationDAO.SimulationResult simulate(int[] pages, int frames) {
		return simulate(pages, frames, step -> { });
	}

	/** Runs the same LRU simulation while optionally reporting each displayed step. */
	public static SimulationDAO.SimulationResult simulate(int[] pages, int frames, Consumer<PageStep> observer) {
		LRUCache<Integer, Integer> memory = new LRUCache<>(frames);
		int hits = 0, faults = 0;
		System.out.println("Frames = " + frames);
		System.out.printf("%-6s %-8s %s%n", "Page", "Result", "Memory state");
		for (int page : pages) {
			String result;
			if (memory.get(page) != null) {
				hits++;
				result = "HIT";
			} else {
				faults++;
				result = "FAULT";
				memory.put(page, page);
			}
			String memoryState = memory.toString();
			System.out.printf("%-6d %-8s %s%n", page, result, memoryState);
			observer.accept(new PageStep(page, result, memoryState));
		}
		double ratio = hits * 100.0 / pages.length;
		System.out.println("\nTotal Hits   : " + hits);
		System.out.println("Total Faults : " + faults);
		System.out.printf("Hit Ratio    : %.2f%%%n", ratio);
		String refString = Arrays.stream(pages).mapToObj(String::valueOf).collect(Collectors.joining(" "));
		return new SimulationDAO.SimulationResult(frames, refString, hits, faults, ratio, null);
	}
}
