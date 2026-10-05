package com.lru.project.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** The project's original hash-map plus doubly-linked-list LRU algorithm. */
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

    public record Entry<K, V>(K key, V value) { }

    private final int capacity;
    private final Map<K, Node> map = new HashMap<>();
    private final Node head = new Node(null, null);
    private final Node tail = new Node(null, null);

    public LRUCache(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("Capacity must be greater than zero.");
        this.capacity = capacity;
        head.next = tail;
        tail.prev = head;
    }

    public V get(K key) {
        Node node = map.get(key);
        if (node == null) return null;
        remove(node);
        addToFront(node);
        return node.value;
    }

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
        }
        Node newNode = new Node(key, value);
        map.put(key, newNode);
        addToFront(newNode);
    }

    public boolean containsKey(K key) { return map.containsKey(key); }
    public int size() { return map.size(); }
    public int capacity() { return capacity; }

    public Entry<K, V> leastRecentlyUsed() {
        if (map.isEmpty()) return null;
        Node node = tail.prev;
        return new Entry<>(node.key, node.value);
    }

    public List<Entry<K, V>> entriesMostRecentFirst() {
        List<Entry<K, V>> entries = new ArrayList<>();
        Node current = head.next;
        while (current != tail) {
            entries.add(new Entry<>(current.key, current.value));
            current = current.next;
        }
        return entries;
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
        StringBuilder out = new StringBuilder("[MRU] ");
        Node current = head.next;
        while (current != tail) {
            out.append(current.key).append("=").append(current.value);
            if (current.next != tail) out.append(" -> ");
            current = current.next;
        }
        return out.append(" [LRU]").toString();
    }
}
