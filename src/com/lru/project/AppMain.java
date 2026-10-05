package com.lru.project;

import java.util.List;
import java.util.Scanner;

public class AppMain {
	private static final SimulationDAO dao = new SimulationDAO();

	public static void main(String[] args) {
		DatabaseManager.init();
		Scanner sc = new Scanner(System.in);
		while (true) {
			System.out.println("\n===== LRU Project =====");
			System.out.println("1. LRU Cache demo (key-value)");
			System.out.println("2. Page Replacement Simulator (result DB me save hoga)");
			System.out.println("3. View simulation history");
			System.out.println("4. Clear history");
			System.out.println("5. Exit");
			System.out.print("Choice: ");
			int choice = sc.nextInt();
			switch (choice) {
			case 1 -> cacheDemo();
			case 2 -> pageDemo(sc);
			case 3 -> showHistory();
			case 4 -> {
				dao.deleteAll();
				System.out.println("History clear ho gayi.");
			}
			case 5 -> {
				System.out.println("Bye!");
				return;
			}
			default -> System.out.println("Galat choice!");
			}
		}
	}

	static void cacheDemo() {
		LRUCache<String, String> cache = new LRUCache<>(3);
		System.out.println("Capacity = 3");
		cache.put("A", "Apple");
		System.out.println(cache);
		cache.put("B", "Banana");
		System.out.println(cache);
		cache.put("C", "Cherry");
		System.out.println(cache);
		System.out.println("get(A) = " + cache.get("A"));
		System.out.println(cache);
		System.out.println("put(D) -> B evict hona chahiye (least recently used)");
		cache.put("D", "Date");
		System.out.println(cache);
		System.out.println("get(B) = " + cache.get("B") + " (null matlab cache se hat gaya)");
	}

	static void pageDemo(Scanner sc) {
		System.out.print("Frames kitne? ");
		int frames = sc.nextInt();
		System.out.print("Kitne pages? ");
		int n = sc.nextInt();
		int[] pages = new int[n];
		System.out.println("Page reference string daalo:");
		for (int i = 0; i < n; i++) {
			pages[i] = sc.nextInt();
		}
		SimulationDAO.SimulationResult result = PageReplacementSimulator.simulate(pages, frames);
		dao.save(result);
		System.out.println("(Result database me save ho gaya)");
	}

	static void showHistory() {
		List<SimulationDAO.SimulationResult> runs = dao.findAll();
		if (runs.isEmpty()) {
			System.out.println("Abhi koi history nahi hai.");
			return;
		}
		System.out.printf("%-20s %-7s %-6s %-7s %-9s %s%n", "Time", "Frames", "Hits", "Faults", "Hit%",
				"Reference string");
		for (SimulationDAO.SimulationResult r : runs) {
			System.out.printf("%-20s %-7d %-6d %-7d %-9.2f %s%n", r.createdAt(), r.frames(), r.hits(), r.faults(),
					r.hitRatio(), r.referenceString());
		}
	}
}