package utils;


import java.util.Optional;

public class PersistentMap<K extends Comparable<K>, V>{
    private Tree<K, V> tree;

    private PersistentMap(Tree<K, V> tree) {
        this.tree = tree;
    }

    public static <K extends Comparable<K>, V> PersistentMap<K, V> empty() {
        return new PersistentMap<>(new Leaf<K, V>());
    }

    public PersistentMap<K, V> add(K key, V value) {
        return new PersistentMap<>(tree.add(key, value));
    }

    public Optional<V> lookup(K key) {
        return tree.lookup(key);
    }

    private interface Tree<K extends Comparable<K>, V> {
        Tree<K, V> add(K key, V value);

        Optional<V> lookup(K key);
    }
    private static class Leaf<K extends Comparable<K>, V> implements Tree<K, V> {
        static <K extends Comparable<K>, V> Tree<K, V> leaf() {
            return new Leaf<>();
        }

        @Override
        public Tree<K, V> add(K key, V value) {
            return new Node<>(key, value, leaf(), leaf());
        }

        @Override
        public Optional<V> lookup(K key) {
            return Optional.empty();
        }
    }

    private static class Node<K extends Comparable<K>, V> implements Tree<K, V> {
        K key;
        V value;
        Tree<K, V> left, right;

        private Node(K key, V value, Tree<K, V> left, Tree<K, V> right) {
            this.key = key;
            this.value = value;
            this.left = left;
            this.right = right;
        }

        @Override
        public Tree<K, V> add(K key, V value) {
            var compare = key.compareTo(this.key);
            if (compare == 0) {
                return new Node<>(key, value, left, right);
            } else if (compare < 0) {
                return new Node<>(this.key, this.value, left.add(key, value), right);
            } else {
                return new Node<>(this.key, this.value, left, right.add(key, value));
            }
        }

        @Override
        public Optional<V> lookup(K key) {
            var compare = key.compareTo(this.key);
            if (compare == 0) {
                return Optional.of(this.value);
            } else if (compare < 0) {
                return left.lookup(key);
            } else {
                return right.lookup(key);
            }
        }
    }
}
