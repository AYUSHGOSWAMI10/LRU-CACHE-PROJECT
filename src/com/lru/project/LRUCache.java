package com.lru.project;

import java.util.HashMap;
import java.util.Map;

public class LRUCache<K, V> {
	private class Node {
		K key;
		V value;
		Node prev, next;

		Node(K key, V value) {
			this.key = key;
			this.value = value;
		}
	}

	private final int capacity;
	private final Map<K, Node> map = new HashMap<>();
	private final Node head = new Node(null, null);
	private final Node tail = new Node(null, null);

	public LRUCache(int capacity) {
		if (capacity <= 0) {
			throw new IllegalArgumentException("Capacity 0 se zyada honi chahiye");
		}
		this.capacity = capacity;
		head.next = tail;
		tail.prev = head;
	}

	/** Key ki value return karta hai, aur usse Most Recently Used bana deta hai. */
	public V get(K key) {
		Node node = map.get(key);
		if (node == null)
			return null;
		remove(node);
		addToFront(node);
		return node.value;
	}

	/** Key-value store karta hai. Cache full ho to LRU entry hata deta hai. */
	public void put(K key, V value) {
		Node node = map.get(key);
		if (node != null) {
			node.value = value;
			remove(node);
			addToFront(node);
			return;
		}
		if (map.size() == capacity) {
			Node lru = tail.prev;
			remove(lru);
			map.remove(lru.key);
			System.out.println("  [Evicted] " + lru.key + " -> " + lru.value);
		}
		Node newNode = new Node(key, value);
		map.put(key, newNode);
		addToFront(newNode);
	}

	public boolean containsKey(K key) {
		return map.containsKey(key);
	}

	public int size() {
		return map.size();
	}

	private void remove(Node node) {
		node.prev.next = node.next;
		node.next.prev = node.prev;
	}

	private void addToFront(Node node) {
		node.next = head.next;
		node.prev = head;
		head.next.prev = node;
		head.next = node;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("[MRU] ");
		Node cur = head.next;
		while (cur != tail) {
			sb.append(cur.key).append("=").append(cur.value);
			if (cur.next != tail)
				sb.append(" -> ");
			cur = cur.next;
		}
		return sb.append(" [LRU]").toString();
	}
}
