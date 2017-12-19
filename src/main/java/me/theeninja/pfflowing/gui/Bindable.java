package me.theeninja.pfflowing.gui;

public interface Bindable<T> {
    void setBinded(T t);
    T getBinded();
}
