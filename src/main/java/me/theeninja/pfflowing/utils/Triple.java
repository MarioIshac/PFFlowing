package me.theeninja.pfflowing.utils;

public class Triple<K, V, Z> {
    private K first;
    private V second;
    private Z third;

    public Triple() {
        this(null, null, null);
    }

    public Triple(K first, V second, Z third) {
        this.first = first;
        this.second = second;
        this.third = third;
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

    public Z getThird() {
        return third;
    }

    public void setThird(Z third) {
        this.third = third;
    }

    public void setTriple(K first, V second, Z third) {
        setFirst(first);
        setSecond(second);
        setThird(third);
    }
}
