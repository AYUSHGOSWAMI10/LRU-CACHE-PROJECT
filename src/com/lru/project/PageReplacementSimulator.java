package com.lru.project;

import java.util.Arrays;
import java.util.stream.Collectors;

public class PageReplacementSimulator {
	public static SimulationDAO.SimulationResult simulate(int[] pages, int frames) {
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
			System.out.printf("%-6d %-8s %s%n", page, result, memory);
		}
		double ratio = hits * 100.0 / pages.length;
		System.out.println("\nTotal Hits   : " + hits);
		System.out.println("Total Faults : " + faults);
		System.out.printf("Hit Ratio    : %.2f%%%n", ratio);
		String refString = Arrays.stream(pages).mapToObj(String::valueOf).collect(Collectors.joining(" "));
		return new SimulationDAO.SimulationResult(frames, refString, hits, faults, ratio, null);
	}
}