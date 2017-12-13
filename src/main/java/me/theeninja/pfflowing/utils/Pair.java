package me.theeninja.pfflowing.utils;

public class Pair<K, V> {
    private K first;
    private V second;

    public Pair() {
        this(null, null);
    }

    public Pair(K first, V second) {

        this.first = first;
        this.second = second;
    }

    public K getFirst() {
        return first;
    }

    public void setFirst(K first) {
        this.first = first;
    }

    public V getSecond() {
        return second;
    }

    public void setSecond(V second) {
        this.second = second;
    }

    public void setPair(K first, V second) {
        setFirst(first);
        setSecond(second);
    }
}
