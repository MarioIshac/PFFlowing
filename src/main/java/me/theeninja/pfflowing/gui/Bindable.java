package me.theeninja.pfflowing.gui;

public interface Bindable<T> {
    void setBinded(T t);
    T getBinded();

    // Whoa
    static <T extends Bindable<Y>, Y extends Bindable<T>> void bind(T firstBindable, Y secondBindable) {
        firstBindable.setBinded(secondBindable);
        secondBindable.setBinded(firstBindable);
    }
}
