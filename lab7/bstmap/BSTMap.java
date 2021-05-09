package bstmap;

import java.util.Iterator;
import java.util.Set;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {
    private BSTNode root;
    private int size;

    public BSTMap() {
        root = null;
    }

    private class BSTNode {
        private K key;           // sorted by K
        private V val;         // associated data
        private BSTNode left, right;  // left and right subtrees

        BSTNode(K key, V val) {
            this.key = key;
            this.val = val;
            this.left = null;
            this.right = null;
        }

        public boolean isLeaf() {
            return left == null && right == null;
        }
    }


    @Override
    public void clear() {
        root = null;
        size = 0;
    }

    @Override
    public boolean containsKey(K key) {
        return containsKey(root, key);
    }

    private boolean containsKey(BSTNode node, K key) {
        if (node == null) {
            return false;
        }
        if (node.key.equals(key)) {
            return true;
        } else if (node.isLeaf()) {
            return false;
        } else {
            return containsKey(node.right, key) || containsKey(node.left, key);
        }
    }

    @Override
    public V get(K key) {
        return get(root, key);
    }

    private V get(BSTNode node, K key) {
        if (node == null) {
            return null;
        }
        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            return get(node.left, key);
        } else if (cmp > 0) {
            return get(node.right, key);
        } else {
            return node.val;
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void put(K key, V value) {
        root = put(root, key, value);
        size += 1;
    }

    private BSTNode put(BSTNode node, K key, V value) {
        if (node == null) {
            return new BSTNode(key, value);
        }
        int cmp = key.compareTo(node.key);
        if (cmp > 0) {
            node.right = put(node.right, key, value);
        } else {
            node.left = put(node.left, key, value);
        }
        return node;
    }

    public void printInOrder() {

    }

    @Override
    public Set<K> keySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(K key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<K> iterator() {
        throw new UnsupportedOperationException();
    }
}
